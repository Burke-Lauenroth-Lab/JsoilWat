package input;

public class SiteIn {
	private class SWC {
		public int swc_min=0;
		public int swc_init=0;
		public int swc_wet=0;
	}
	private class Model {
		private class Flags {
			public boolean reset;
			public boolean deepdrain;
		}
		private class Coefficients {
			public double petMultiplier;
			public double percentRunoff;
		}
		public Flags flags;
		public Coefficients coefficients;
	}
	private class Snow {
		public double TminAccu2;
		public double TmaxCrit;
		public double lambdasnow;
		public double RmeltMin;
		public double RmeltMax;
	}
	private class Drainage {
		public double slow_drain_coeff;
	}
	private class Evaporation {
		public double xinflec;
		public double slope;
		public double yinflec;
		public double range;
	}
	private class Transpiration {
		public double xinflec;
		public double slope;
		public double yinflec;
		public double range;
	}
	private class Intrinsic {
		public double latitude;
		public double altitude;
		public double slope;
		public double aspect;
	}
	private class SoilTemperature {
		public double bmLimiter;
		public double t1Param1;
		public double t1Param2;
		public double t1Param3;
		public double csParam1;
		public double csParam2;
		public double shParam;
		public double meanAirTemp;
		public double stDeltaX;
		public double stMaxDepth;
		public boolean use_soil_temp;
	}
	private class TranspirationRegions {
		private int[][] table;
		private int nTranspRgn;
		
		public TranspirationRegions() {
			this.table = new int[4][2];
			for(int i=0; i<4; i++)
				this.table[i][0] = (i+1);
			this.nTranspRgn = 0;
		}
		public void set(int ndx, int layer) {
			if(ndx > 0 && ndx < 4) {
				if(ndx <= (this.nTranspRgn+1)) {
					this.table[ndx-1][1] = layer;
					this.nTranspRgn++;
				}
			}
		}
		public int get(int ndx) {
			if(ndx <= this.nTranspRgn)
				return this.table[ndx-1][1];
			else {
				return -1;
			}
		}
		public void onClear() {
			this.nTranspRgn=0;
		}
	}
	private SWC swc;
	private Model model;
	private Snow snow;
	private Drainage drainage;
	private Evaporation evaporation;
	private Transpiration transpiration;
	private Intrinsic intrinsic;
	private SoilTemperature soilTemperature;
	private TranspirationRegions transpirationRegions;
	private boolean data;
	
	public SiteIn() {
		this.swc = new SWC();
		this.model = new Model();
		this.snow = new Snow();
		this.drainage = new Drainage();
		this.evaporation = new Evaporation();
		this.transpiration = new Transpiration();
		this.intrinsic = new Intrinsic();
		this.soilTemperature = new SoilTemperature();
		this.transpirationRegions = new TranspirationRegions();
		this.data = false;
	}
	
	
}
