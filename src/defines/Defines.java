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
}
