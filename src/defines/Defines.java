package defines;

public final class Defines {
	
	public static final int Yesterday=0;
	public static final int Today=1;
	
	public static final double BARCONV = 1024.0;
	public static final int MAX_LAYERS=25;
	public static final int MAX_TRANSP_REGIONS=4;
	public static final int MAX_ST_RGR=30;
	public static final int SLOW_DRAIN_DEPTH=15;
	
	/* convenience indices to arrays in the model */
	public static final int TWO_DAYS=2;
	public static final int SW_TOP=0;
	public static final int SW_BOT=1;
	public static final int SW_MIN=0;
	public static final int SW_MAX=1;
	
	public static boolean isZero(double value){
		return Math.abs(value) <= 0+5*Math.ulp(value);
	}
	
	/* types to identify the various modules/objects */
	public enum ObjType { eF,   /* file management */
	               eMDL, /* model */
	               eWTH, /* weather */
	               eSIT, /* site */
	               eSWC, /* soil water */
	               eVES, /* vegetation establishement */
	               eVPD, /* vegetation production */
	               eOUT,  /* output */
	               eNONE /* none */
	}
	
	/**************************************************************************************************************************************
	 PURPOSE: Calculate a linear regression between two points, for use in soil_temperature function

	 HISTORY:
	 05/25/2012 (DLM) initial coding
	 **************************************************************************************************************************************/
	public static double regression(double x1, double x2, double y1, double y2, double deltaX) {
		return y1 + (((y2 - y1) / (x2 - x1)) * (deltaX - x1));
	}
	
	public static boolean LE(double d1, double d2) {
		if(Double.compare(d1, d2) <= 0)
			return true;
		else
			return false;
	}
	public static boolean LT(double d1, double d2) {
		if(Double.compare(d1, d2) < 0)
			return true;
		else
			return false;
	}
	public static boolean GE(double d1, double d2) {
		if(Double.compare(d1, d2) >= 0)
			return true;
		else
			return false;
	}
	public static boolean GT(double d1, double d2) {
		if(Double.compare(d1, d2) > 0)
			return true;
		else
			return false;
	}
	public static boolean EQ(double d1, double d2) {
		if(Double.compare(d1, d2) == 0)
			return true;
		else
			return false;
	}
	
	public static double tanfunc(double z, double a, double b, double c, double d) {
		return ((b)+((c)/Math.PI)*Math.atan(Math.PI*(d)*((z)-(a))) );
	}
	
	/**************************************************************************************************************************************
	 PURPOSE: To get the index of the lower(x1) and upper bound(x2) at the depth, for use in soil_temperature function.  written in it's own separate function to reduce code duplication.  located here so it doesn't take up space in SW_Flow_lib.c

	 *NOTE* Works with positive values.  Hasn't been tested with negative values, might not work correctly

	 HISTORY:
	 05/31/2012 (DLM) initial coding

	 INPUTS:
	 size - the size of the array bounds[]
	 depth - the depth of the index you are looking for, should be less than the highest bound (ie. bounds[size - i]) or the function won't work properly
	 bounds[] - the depths of the bounds (needs to be in order from lowest to highest)

	 OUTPUTS:
	 x1 - the index of the lower bound (-1 means below the lowest depth of bounds[] (aka UINT_MAX), in this case x2 will be 0)
	 x2 - the index of the upper bound (-1 means above the highest depth of bounds[] (aka UINT_MAX), in this case x1 will be size - 1)
	 equal - is this equals 1, then the depth is equal to the depth at bounds[x1]
	 **************************************************************************************************************************************/
	public static void st_getBounds(int[] equal_x1_x2, int size, double depth, double bounds[]) {
		int i;
		equal_x1_x2[0] = 0;
		equal_x1_x2[1] = -1; 			// -1 means below the lowest bound
		equal_x1_x2[2] = size - 1; 	// size - 1 is the upmost bound...

		// makes sure the depth is within the bounds before starting the for loop... to save time if it's not in between the bounds
		if (LT(depth, bounds[0])) {
			equal_x1_x2[2] = 0;
			return;
		} else if (GT(depth, bounds[size - 1])) {
			equal_x1_x2[1] = size - 1;
			equal_x1_x2[2] = -1;
			return;
		}

		for (i = 0; i < size; i++) {

			if (i < size - 1) { // to avoid going out of the bounds of the array and subsequently blowing up the program
				if (LE(bounds[i], depth) && (!(LE(bounds[i + 1], depth)))) {
					equal_x1_x2[1] = i;
					if (EQ(bounds[i], depth)) {
						equal_x1_x2[0] = 1;
						equal_x1_x2[2] = i;
						return; // return since they're equal & no more calculation is necessary
					}
				}
			}
			if (i > 0) {  // to avoid going out of the bounds of the array
				if (GE(bounds[i], depth) && (!(GE(bounds[i - 1], depth)))) {
					equal_x1_x2[2] = i;
					if (EQ(bounds[i], depth)) {
						equal_x1_x2[0] = 1;
						equal_x1_x2[1] = i;
					}
					return; // if it's found the upperbound, then the lowerbound has already been calculated, so return
				}
			}
		}
	}
}
