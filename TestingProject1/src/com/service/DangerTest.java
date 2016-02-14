/**
 * 
 */
package com.service;

import com.pojo.FireIndex;

/**
 * @author Madhura Tendulkar
 *
 */
public class DangerTest {
	/**
	 * Values used to compute danger rating
	 */
	double[] A = {-0.185900, -0.85900, -0.059660, -0.077373};
	double[] B = {30.0, 19.2, 13.8, 22.5};
	double[] C = {4.5, 12.5, 27.5};
	double[] D = {16.0, 10.0, 7.0, 5.0, 4.0, 3.0};
	
	/**
	 * 
	 * @param dryBulbTemperature This is dry bulb temperature
	 * @param wetBulbTemperature This is wet bulb temperature
	 * @param iSnow              This is positive non zero number if there is snow on the ground
	 * @param windSpeed          This is speed of wind miles per hour 
	 * @param buildUpIndex		 This is last value of build up index	
	 * @param iHerb 			 This is current herb state of the district 1=cured 2=transition 3=green 
	 * @return 
	 */
	public FireIndex danger (double dryBulbTemperature, double wetBulbTemperature, 
			int iSnow, double precipitation, double windSpeed, double buildUpIndex, int iHerb) {
		
		FireIndex fireIndex = new FireIndex();
		
		if(iSnow > 0){
			/*  1.
			 *  There is snow on the ground timberSpreadIndex and the grassSpreadIndex must be set to zero.
			 *  With the zero timberSpreadIndex, fireLoadRating is equal to zero 
			 */
			fireIndex.setTimberSpreadIndex(0);
			fireIndex.setGrassSpreadIndex(0);
			fireIndex.setFireLoadRating(0);
			
			if(precipitation > 0.1){
				fireIndex.setBuildUpIndex(calculateBuildUpIndex(buildUpIndex, precipitation));
				return fireIndex;
			}
			else if(precipitation <= 0.1){
				return fireIndex;
			}
			   
			
		 } else if (iSnow <= 0){
			 /*
			  *  5. 
			  *  There is no snow on the ground we will compute the spread indices and fireLoadRating.
			  */
			 double dif = dryBulbTemperature - wetBulbTemperature;
			 
			 for (int i = 0; i < 3; i ++) {
				 
				 if ((dif - C[i]) <= 0){
					 /*
					  *  7.
					  */
					 fireIndex.setFineFuelMoisture(calculateFineFuelMoisture(dif, i));
				 }
				 
				 else if ((dif - C[i]) > 0){
					 /*
					  *  6.
					  *  Continue
					  */
					 continue;
				 }
			 }
			 
			 /*
			  *  Calculate dryingFactor for the day
			  */
			 for (int i=0; i < 6; i++) {
				 
				 if((fireIndex.getFineFuelMoisture() - D[i]) > 0){
					 /*
					  *  9.
					  */
					 fireIndex.setDryingFactor(i-1);
				 }
				 else if((fireIndex.getFineFuelMoisture() - D[i]) <= 0){
					 /*
					  *  8.
					  */
					 continue;
				 }
			 }
			 
			 /*
			  *  10. 
			  *  Test to see if fineFuelMoisture is 1 or less
			  *  if fineFuelMoisture is one or less we set it to one
			  */
			 if (fireIndex.getFineFuelMoisture() <= 1 ) {
				 /*
				  *  11.
				  */
				 fireIndex.setFineFuelMoisture(1);
			 } else if (fireIndex.getFineFuelMoisture() > 1 ) {
				 /*
				  *  12.
				  *  Add five percent fine fuel moisture for each herb stage greater than one
				  */
				 fireIndex.setFineFuelMoisture(fireIndex.getFineFuelMoisture() + (iHerb - 1) * 5);
			 }
			 
			 /*
			  *  We must adjust buildUpIndex for precipitation before adding the dryingFactor
			  */
			 
			 /*
			  *  13.
			  */
			 double buiTemp = calculateBuildUpIndex(buildUpIndex, precipitation);
			 
			  if ((precipitation > 0.1) && (buiTemp >= 0) ) {
				 /*
				  *  13.
				  */
				 fireIndex.setBuildUpIndex(buiTemp);
			 } else if ((precipitation > 0.1) && (buiTemp < 0) ) {
				 /*
				  *  14.
				  */
				 fireIndex.setBuildUpIndex(0);
			 }
			
			 /*
			  *  15.
			  */
			 fireIndex.setBuildUpIndex( fireIndex.getBuildUpIndex() + fireIndex.getDryingFactor());
			 
			 /*
			  * Calculate the adjustedFuelMoisture
			  */
			 fireIndex.setAdjustedFuelMoisture(calculateAdjustedFuelMoisture(fireIndex.getFineFuelMoisture(),
					 fireIndex.getBuildUpIndex()));
			 /*
			  * Check if fineFuelMoisture and adjustedFuelMoisture are greater than 30%. If they are set grassSpreadIndex 
			  * and timberSpreadIndex to 1
			  */
			 if(fireIndex.getAdjustedFuelMoisture() >= 30){
				 /*
				  * 16. 
				  */
				 if(fireIndex.getFineFuelMoisture() >= 30)
				 {		
					 /*
					  * 17.
					  */
					 fireIndex.setGrassSpreadIndex(1);
					 fireIndex.setTimberSpreadIndex(1);
					 return fireIndex;
				 }
				 else if(fireIndex.getFineFuelMoisture() < 30){
					 /*
					  * 18.
					  */
					 fireIndex.setTimberSpreadIndex(1);
				 }
				 
			 }
			 /*
			  * 19. Check if windSpeed greater than or equal to zero
			  */
			 else if(fireIndex.getAdjustedFuelMoisture() < 30){
				 /*
				  * set grassSpreadIndex and timberSpreadIndex
				  */
				 fireIndex.setGrassSpreadIndex(calculateGrassSpreadIndex(windSpeed,fireIndex.getFineFuelMoisture()));
				 fireIndex.setTimberSpreadIndex(calculateTimberSpreadIndex(windSpeed,fireIndex.getAdjustedFuelMoisture()));
			 }
			 /*
			  * check if buildUpIndex and timberSpreadIndex is greater than 0
			  */
			 if ((fireIndex.getTimberSpreadIndex() == 0) || (fireIndex.getBuildUpIndex() == 0)) {
				 fireIndex.setFireLoadRating(0);
				 return fireIndex;
			 }
			 else {
				 fireIndex.setFireLoadRating(calculateFireLoadIndex(fireIndex.getTimberSpreadIndex(),
						 fireIndex.getBuildUpIndex()));
				
			 }
			 
			 if(fireIndex.getFireLoadRating() <= 0){
				 fireIndex.setFireLoadRating(0);
			 }
			 else{
				 fireIndex.setFireLoadRating(Math.pow(10,fireIndex.getFireLoadRating()));
			 }
			 
		 }
		
		return fireIndex;
		
	}
	
	/**
	 * @param timberSpreadIndex
	 * @param buildUpIndex
	 * @return
	 */
	private double calculateFireLoadIndex(double timberSpreadIndex, double buildUpIndex) {
		double fload = 1.75 * Math.log10(timberSpreadIndex) + 0.32 * Math.log10(buildUpIndex) - 1.640;
		return fload;
	}

	/**
	 * 
	 * @param buildUpIndex
	 * @param precipitation
	 * @return
	 */
	private double calculateBuildUpIndex(double buildUpIndex, double precipitation){
		
		double bui = -50*(Math.log(1-(-Math.exp(-buildUpIndex/50)) * Math.exp(-1.175* precipitation-0.1)) );
		return bui;
	}
	
	/**
	 * 
	 * @param dif
	 * @param i
	 * @return
	 */
	private double calculateFineFuelMoisture(double dif, int i){
		double ffm = B[i] * Math.exp(A[i] * dif);
		return ffm;
		
	}
	
	private double calculateAdjustedFuelMoisture(double ffm, double bui){
		double adfm = 0.9 * ffm + 0.5 + 9.5 * Math.exp(-bui/50);
		return adfm;
		
	}
	
	/**
	 * 
	 * @param windSpeed
	 * @param ffm
	 * @return
	 */
	private double calculateGrassSpreadIndex(double windSpeed, double ffm){
		double grassIndex = 0;
		if(windSpeed >= 14){
			grassIndex = 0.00918* (windSpeed + 14) * Math.pow((33-ffm), 1.65) - 3;
			
			
		}
		else 
			grassIndex = 0.01312 * (windSpeed + 6) * Math.pow((33-ffm), 1.65) - 3;
		return grassIndex;
	}
	
	private double calculateTimberSpreadIndex(double windSpeed, double adfm){
		double timberIndex = 0;
		if(windSpeed >= 14){
			timberIndex = 0.00918* (windSpeed + 14) * Math.pow((33-adfm), 1.65) - 3;
			
		}
		else 
			timberIndex = 0.01312 * (windSpeed + 6) * Math.pow((33-adfm), 1.65) - 3;
		return timberIndex;
	}
	
	
	
	

}
