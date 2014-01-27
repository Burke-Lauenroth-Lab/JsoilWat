package defines;

public final class Defines {
		
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
