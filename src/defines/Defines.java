package defines;

public final class Defines {
	public static final double BARCONV = 1024.0;
	
	public static boolean isZero(double value){
		return Math.abs(value) <= 0+5*Math.ulp(value);
	}
	
	public static double SW_SWPmatric2VWCBulk(double fractionGravel, double swpMatric,  double psisMatric, double binverseMatric, double thetasMatric) {
		/* =================================================== */
		/* used to be swfunc in the fortran version */
		/* 27-Aug-03 (cwb) moved from the Site module. */
		/* return the volume as cmH2O/cmSOIL */
		double t, p;

		swpMatric *= BARCONV;
		p = Math.pow(psisMatric / swpMatric, binverseMatric);
		t = thetasMatric * p * 0.01 * (1 - fractionGravel);
		return (t);
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
