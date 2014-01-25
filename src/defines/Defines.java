package defines;

public final class Defines {
	public static final double BARCONV = 1024.0;
	public static final int MAX_LAYERS=25;
	public static final int MAX_TRANSP_REGIONS=4;
	public static final int MAX_ST_RGR=30;
	public static final int SLOW_DRAIN_DEPTH=15;
	
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
	
	public class SW_TIMES {
		private int first, last, total;
		private boolean bFirst, bLast, bTotal;
		
		public SW_TIMES() {
			bFirst=bLast=bTotal=false;
			this.first=this.last=this.total=0;
		}
		
		public void onClear() {
			bFirst=bLast=bTotal=false;
			this.first=this.last=this.total=0;
		}

		public int getFirst() {
			return first;
		}

		public void setFirst(int first) {
			if(!bFirst)
				bFirst=true;
			this.first = first;
			if(bLast) {
				if(!bTotal)
					bTotal = true;
				this.total = this.last - this.first + 1;
			}
		}

		public int getLast() {
			return last;
		}

		public void setLast(int last) {
			if(!bLast)
				bLast=true;
			this.last = last;
			if(bFirst) {
				if(!bTotal)
					bTotal = true;
				this.total = this.last - this.first + 1;
			}
		}

		public int getTotal() {
			return total;
		}
		
		public boolean totalSet() {
			return bTotal;
		}
		public boolean firstSet() {
			return bFirst;
		}
		public boolean lastSet() {
			return bLast;
		}
	}
}
