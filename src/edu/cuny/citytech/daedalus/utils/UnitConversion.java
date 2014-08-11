package edu.cuny.citytech.daedalus.utils;

public class UnitConversion {

	public static int dpToPixels(int dp, float scale){
		
		// Formula to convert dp to pixels.
		return (int) (dp * scale + 0.5f);
	}
}