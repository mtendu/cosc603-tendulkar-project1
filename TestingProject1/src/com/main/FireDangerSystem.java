/**
 * 
 */
package com.main;

import com.service.FireDanger;

// TODO: Auto-generated Javadoc
/**
 * The Class FireDangerSystem.
 *
 * @author Madhura Tendulkar
 */
public class FireDangerSystem {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FireDanger fireDanger = new FireDanger();
		/**
		 * fireDanger.fireIndexCalculator(dryBulbTemperature, wetBulbTemperature, iSnow, precipitation, windSpeed, buildUpIndex, iHerb));
		 */
		System.out.println("When there is no snow on the ground and no rain");
		System.out.println(fireDanger.fireIndexCalculator(1.5, 1.5, 0, 0, 12, 1, 1));
		
	}

}
