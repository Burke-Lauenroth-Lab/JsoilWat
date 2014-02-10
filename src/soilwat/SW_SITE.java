package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.LogFileIn.LogMode;

public class SW_SITE {
	public static class SWC {
		public double swc_min=0; /* lower bound on swc.          */
		public double swc_init=0; /* initialization value for swc */
		public double swc_wet=0; /* value for a "wet" day,       */
		public void onSet(double swc_min, double swc_init, double swc_wet) {
			this.swc_min = swc_min;
			this.swc_init = swc_init;
			this.swc_wet = swc_wet;
		}
	}
	public static class Model {
		public class Flags {
			public boolean reset_yr;		/* 1: reset values at start of each year */
			public boolean deepdrain;	/* 1: allow drainage into deepest layer  */
		}
		public class Coefficients {
			public double petMultiplier;
			public double percentRunoff;
		}
		public Flags flags;
		public Coefficients coefficients;
		public Model() {
			this.flags = new Flags();
			this.coefficients = new Coefficients();
		}
		public void onSet(boolean reset, boolean deepdrain, double petMultiplier, double percentRunoff) {
			this.flags.reset_yr = reset;
			this.flags.deepdrain = deepdrain;
			this.coefficients.petMultiplier = petMultiplier;
			this.coefficients.percentRunoff = percentRunoff;
		}
	}
	public static class Snow {
		public double TminAccu2;
		public double TmaxCrit;
		public double lambdasnow;
		public double RmeltMin;
		public double RmeltMax;
		public void onSet(double TminAccu2, double TmaxCrit, double lambdasnow, double RmeltMin, double RmeltMax) {
			this.TminAccu2 = TminAccu2;
			this.TmaxCrit = TmaxCrit;
			this.lambdasnow = lambdasnow;
			this.RmeltMin = RmeltMin;
			this.RmeltMax = RmeltMax;
		}
	}
	public static class Drainage {
		public double slow_drain_coeff;
		public void onSet(double slow_drain_coeff) {
			this.slow_drain_coeff = slow_drain_coeff;
		}
	}
	public static class Evaporation {
		public double xinflec;
		public double slope;
		public double yinflec;
		public double range;
		public void onSet(double rate_shift, double rate_slope, double inflection_point, double range) {
			this.xinflec=rate_shift;
			this.slope = rate_slope;
			this.yinflec = inflection_point;
			this.range = range;
		}
	}
	public static class Transpiration {
		public double xinflec;
		public double slope;
		public double yinflec;
		public double range;
		public void onSet(double rate_shift, double rate_shape, double inflection_point, double range) {
			this.xinflec=rate_shift;
			this.slope = rate_shape;
			this.yinflec = inflection_point;
			this.range = range;
		}
	}
	public static class Intrinsic {
		public double latitude;
		public double altitude;
		public double slope;
		public double aspect;
		public void onSet(double latitude, double altitude, double slope, double aspect) {
			this.latitude = latitude;
			this.altitude = altitude;
			this.slope = slope;
			this.aspect = aspect;
		}
	}
	public static class SoilTemperature {
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
		public boolean use_soil_temp;	/* whether or not to do soil_temperature calculations */
		public void onSet(double bmLimiter, double t1Param1, double t1Param2, double t1Param3, double csParam1,
				double csParam2, double shParam, double meanAirTemp, double stDeltaX, double stMaxDepth, boolean use_soil_temp) {
			this.bmLimiter=bmLimiter;
			this.t1Param1=t1Param1;
			this.t1Param2=t1Param2;
			this.t1Param3=t1Param3;
			this.csParam1=csParam1;
			this.csParam2=csParam2;
			this.shParam=shParam;
			this.meanAirTemp=meanAirTemp;
			this.stDeltaX=stDeltaX;
			this.stMaxDepth=stMaxDepth;
			this.use_soil_temp=use_soil_temp;
		}
	}
	/* transpiration regions  shallow, moderately shallow,  */
	/* deep and very deep. units are in layer numbers. */
	public static class TranspirationRegions {
		private int[][] table;
		private int nTranspRgn;
		public final int MAX_TRANSP_REGIONS = 4;
		
		public TranspirationRegions() {
			this.table = new int[MAX_TRANSP_REGIONS][2];
			for(int i=0; i<MAX_TRANSP_REGIONS; i++)
				this.table[i][0] = (i+1);
			this.nTranspRgn = 0;
		}
		public void set(int ndx, int layer) {
			if(ndx > 0 && ndx < MAX_TRANSP_REGIONS) {
				if(ndx <= (this.nTranspRgn+1)) {
					this.table[ndx-1][1] = layer;
					this.nTranspRgn++;
				}
			} else {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogFileIn.LogMode.ERROR, "SW_SITE TranspirationRegions : Index out of bounds");
			}
		}
		public void onVerify() {
			for(int r=1; r<this.nTranspRgn; r++) {
				if(this.table[r-r][1] >= this.table[r][1]) {
					LogFileIn f = LogFileIn.getInstance();
					f.LogError(LogFileIn.LogMode.ERROR, "SW_SITE TranspirationRegions : Discontinuity/reversal in transpiration regions.");
				}
			}
		}
		public int getRegion(int ndx) {
			if(ndx <= this.nTranspRgn)
				return this.table[ndx][1];
			else {
				return -1;
			}
		}
		public int get_nRegions() {
			return this.nTranspRgn;
		}
		public void onClear() {
			this.nTranspRgn=0;
		}
		public String toString() {
			if(nTranspRgn > 0) {
				String sTable = "";
				for(int i=0;i<nTranspRgn;i++) {
					sTable+="\t"+String.valueOf(i+1)+"\t"+String.valueOf(table[i][1])+"\n";
				}
				return sTable;
			} else
				return "";
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
	
	private int stNRGR; /* number of regressions, for the soil_temperature function */
	
	private boolean data;
	private boolean EchoInits;
	private int nFileItemsRead;
	
	private SW_VEGPROD SW_VegProd;
	private SW_SOILS SW_Soils;
	
	protected SW_SITE(SW_VEGPROD SW_VegProd, SW_SOILS SW_Soils) {
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
		this.SW_VegProd = SW_VegProd;
		this.SW_Soils = SW_Soils;
	}
	
	protected void onClear() {
		this.data = false;
		this.transpirationRegions.onClear();
	}
	
	protected boolean onVerify() {
		SW_Soils.getLayersInfo().n_transp_rgn = transpirationRegions.get_nRegions();
		_init_site_info();
		if(EchoInits)
			_echo_inputs("");
		if(SW_Soils.get_echoinits())
			SW_Soils._echo_inputs("");
		return true;
	}
	
	protected void onRead(Path siteIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(siteIn, StandardCharsets.UTF_8);
		this.nFileItemsRead=0;
		boolean too_many_regions = false;
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				switch (this.nFileItemsRead) {
				case 0:
					try {
						this.swc.swc_min = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : SWC Limit - swc_min : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 1:
					try {
						this.swc.swc_init = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : SWC Limit - swc_init : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 2:
					try {
						this.swc.swc_wet = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : SWC Limit - swc_wet : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 3:
					try {
						this.model.flags.reset_yr = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Flags - reset : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 4:
					try {
						this.model.flags.deepdrain = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Flags - deepdrain : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 5:
					try {
						this.model.coefficients.petMultiplier = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Coefficients - PET multiplier : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 6:
					try {
						this.model.coefficients.percentRunoff = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Coefficients - runoff : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 7:
					try {
						this.snow.TminAccu2 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - TminAccu2 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 8:
					try {
						this.snow.TmaxCrit = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - TmaxCrit : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 9:
					try {
						this.snow.lambdasnow = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - lambdasnow : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 10:
					try {
						this.snow.RmeltMin = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - RmeltMin : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 11:
					try {
						this.snow.RmeltMax = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - RmeltMax : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 12:
					try {
						this.drainage.slow_drain_coeff = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Drainage Coefficient - slow drain: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 13:
					try {
						this.evaporation.xinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - xinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 14:
					try {
						this.evaporation.slope = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 15:
					try {
						this.evaporation.yinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - yinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 16:
					try {
						this.evaporation.range = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - range : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 17:
					try {
						this.transpiration.xinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - xinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 18:
					try {
						this.transpiration.slope = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 19:
					try {
						this.transpiration.yinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - yinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 20:
					try {
						this.transpiration.range = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - range : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 21:
					try {
						this.intrinsic.latitude = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - Latitiude : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 22:
					try {
						this.intrinsic.altitude = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - Altitude : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 23:
					try {
						this.intrinsic.slope = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 24:
					try {
						this.intrinsic.aspect = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - aspect : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 25:
					try {
						this.soilTemperature.bmLimiter = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - Biomass Limiter : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 26:
					try {
						this.soilTemperature.t1Param1 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - t1Param1 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 27:
					try {
						this.soilTemperature.t1Param2 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - t1Param2 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 28:
					try {
						this.soilTemperature.t1Param3 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - t1Param3 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 29:
					try {
						this.soilTemperature.csParam1 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - csParam1 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 30:
					try {
						this.soilTemperature.csParam2 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - csParam2 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 31:
					try {
						this.soilTemperature.shParam = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - shParam : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 32:
					try {
						this.soilTemperature.meanAirTemp = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - meanAirTemp : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 33:
					try {
						this.soilTemperature.stDeltaX = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - stDeltaX : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 34:
					try {
						this.soilTemperature.stMaxDepth = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - stMaxDepth : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 35:
					try {
						this.soilTemperature.use_soil_temp = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - use_soil_temp : Could not convert string to double. " + e.getMessage());
					}
					break;
				default:
					if(this.nFileItemsRead > 35 && this.nFileItemsRead <= 38 && !too_many_regions) {
						if(values.length != 2)
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Regions : Expected 2 values, read "+String.valueOf(values.length));
						try {
							if(this.transpirationRegions.MAX_TRANSP_REGIONS < this.transpirationRegions.nTranspRgn) {
								too_many_regions = true;
								break;
							}
							this.transpirationRegions.set(Integer.parseInt(values[0]), Integer.parseInt(values[1])-1);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else {
						f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Regions : Too many regions.");
					}
					break;
				}
				this.nFileItemsRead++;
			}
		}
		this.transpirationRegions.onVerify();
		this.data = true;
	}

	protected void onWrite(Path siteIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# ---- SWC limits ----");
			lines.add(String.valueOf(this.swc.swc_min)+"\t\t\t# swc_min : cm/cm if 0 - <1.0, -bars if >= 1.0.; if < 0. then estimate residual water content for each layer");
			lines.add(String.valueOf(this.swc.swc_init)+"\t\t\t# swc_init: cm/cm if < 1.0, -bars if >= 1.0. ");
			lines.add(String.valueOf(this.swc.swc_wet)+"\t\t\t# swc_wet : cm/cm if < 1.0, -bars if >= 1.0. ");
			lines.add("");
			lines.add("# ---- Model flags and coefficients ----");
			lines.add(String.valueOf(this.model.flags.reset_yr?1:0)+"\t\t\t# reset (1/0): reset/don't reset swc each new year");
			lines.add(String.valueOf(this.model.flags.deepdrain?1:0)+"\t\t\t# deepdrain (1/0): allow/disallow deep drainage function.");
			lines.add("\t\t\t#   if deepdrain == 1, model expects extra layer in soils file.");
			lines.add(String.valueOf(this.model.coefficients.petMultiplier)+"\t\t\t# multiplier for PET (eg for climate change).");
			lines.add(String.valueOf(this.model.coefficients.percentRunoff)+"\t\t\t#proportion of ponded surface water removed as runoff daily (value ranges between 0 and 1; 0=no loss of surface water, 1=all ponded water lost via runoff)");
			lines.add("");
			lines.add("# ---- Snow simulation parameters (SWAT2K model): Neitsch S, Arnold J, Kiniry J, Williams J. 2005. Soil and water assessment tool (SWAT) theoretical documentation. version 2005. Blackland Research Center, Texas Agricultural Experiment Station: Temple, TX.");
			lines.add("# these parameters are RMSE optimized values for 10 random SNOTEL sites for western US");
			lines.add(String.valueOf(this.snow.TminAccu2)+"\t\t\t"+"# TminAccu2 = Avg. air temp below which ppt is snow ( C)");
			lines.add(String.valueOf(this.snow.TmaxCrit)+"\t\t\t"+"# TmaxCrit = Snow temperature at which snow melt starts ( C)");
			lines.add(String.valueOf(this.snow.lambdasnow)+"\t\t\t"+"# lambdasnow = Relative contribution of avg. air temperature to todays snow temperture vs. yesterday's snow temperature (0-1)");
			lines.add(String.valueOf(this.snow.RmeltMin)+"\t\t\t"+"# RmeltMin = Minimum snow melt rate on winter solstice (cm/day/C)");
			lines.add(String.valueOf(this.snow.RmeltMax)+"\t\t\t"+"# RmeltMax = Maximum snow melt rate on summer solstice (cm/day/C)");
			lines.add("");
			lines.add("# ---- Drainage coefficient ----");
			lines.add(String.valueOf(this.drainage.slow_drain_coeff)+"\t\t\t"+"# slow-drain coefficient per layer (cm/day).  See Eqn 2.9 in ELM doc.");
			lines.add("\t\t\t"+"# ELM shows this as a value for each layer, but this way it's applied to all.");
			lines.add("\t\t\t"+"# (Q=.02 in ELM doc, .06 in FORTRAN version).");
			lines.add("");
			lines.add("# ---- Evaporation coefficients ----");
			lines.add("# These control the tangent function (tanfunc) which affects the amount of soil");
			lines.add("# water extractable by evaporation and transpiration.");
			lines.add("# These constants aren't documented by the ELM doc.");
			lines.add(String.valueOf(this.evaporation.xinflec)+"\t\t\t"+"# rate shift (x value of inflection point).  lower value shifts curve ");
			lines.add("\t\t\t"+"# leftward, meaning less water lost to evap at a given swp.  effectively");
			lines.add("\t\t\t"+"# shortens/extends high rate.");
			lines.add(String.valueOf(this.evaporation.slope)+"\t\t\t"+"# rate slope: lower value (eg .01) straightens S shape meaning more gradual");
			lines.add("\t\t\t"+"# reduction effect; higher value (.5) makes abrupt transition");
			lines.add(String.valueOf(this.evaporation.yinflec)+"\t\t\t"+"# inflection point (y-value of inflection point)");
			lines.add(String.valueOf(this.evaporation.range)+"\t\t\t"+"# range: diff btw upper and lower rates at the limits");
			lines.add("");
			lines.add("# ---- Transpiration Coefficients ----");
			lines.add("# comments from Evap constants apply.");
			lines.add(String.valueOf(this.transpiration.xinflec)+"\t\t\t"+"# rate shift");
			lines.add(String.valueOf(this.transpiration.slope)+"\t\t\t"+"# rate shape");
			lines.add(String.valueOf(this.transpiration.yinflec)+"\t\t\t"+"# inflection point");
			lines.add(String.valueOf(this.transpiration.range)+"\t\t\t"+"# range");
			lines.add("");
			lines.add("# ---- Intrinsic site params: Chimney Park, WY (41.068° N, 106.1195° W, 2740 m elevation) ----");
			lines.add(String.valueOf(this.intrinsic.latitude)+"\t\t\t"+"# latitude of the site in radians, site = 002_-119.415_39.046");
			lines.add(String.valueOf(this.intrinsic.altitude)+"\t\t\t"+"# altitude of site (m a.s.l.)");
			lines.add(String.valueOf(this.intrinsic.slope)+"\t\t\t"+"# slope at site (degrees): no slope = 0");
			lines.add(String.valueOf(this.intrinsic.aspect)+"\t\t\t"+"# aspect at site (degrees): N=0, E=90, S=180, W=270, no slope:-1");
			lines.add("");
			lines.add("# ---- Soil Temperature Constants ----");
			lines.add("# from Parton 1978, ch. 2.2.2 Temperature-profile Submodel");
			lines.add(String.valueOf(this.soilTemperature.bmLimiter)+"\t\t\t"+"# biomass limiter, 300 g/m^2 in Parton's equation for T1(avg daily temperature at the top of the soil)");
			lines.add(String.valueOf(this.soilTemperature.t1Param1)+"\t\t\t"+"# constant for T1 equation (used if biomass <= biomass limiter), 15 in Parton's equation");
			lines.add(String.valueOf(this.soilTemperature.t1Param2)+"\t\t\t"+"# constant for T1 equation (used if biomass > biomass limiter), -4 in Parton's equation");
			lines.add(String.valueOf(this.soilTemperature.t1Param3)+"\t\t\t"+"# constant for T1 equation (used if biomass > biomass limiter), 600 in Parton's equation");
			lines.add(String.valueOf(this.soilTemperature.csParam1)+"\t\t\t"+"# constant for cs (soil-thermal conductivity) equation, 0.00070 in Parton's equation");
			lines.add(String.valueOf(this.soilTemperature.csParam2)+"\t\t\t"+"# constant for cs equation, 0.00030 in Parton's equation");
			lines.add(String.valueOf(this.soilTemperature.shParam)+"\t\t\t"+"# constant for sh (specific heat capacity) equation, 0.18 in Parton's equation");
			lines.add(String.valueOf(this.soilTemperature.meanAirTemp)+"\t\t\t"+"# constant mean air temperature (the soil temperature at the lower boundary, 180 cm) in celsius");
			lines.add(String.valueOf(this.soilTemperature.stDeltaX)+"\t\t\t"+"# deltaX parameter for soil_temperature function, default is 15.  (distance between profile points in cm)  max depth (the next number) should be evenly divisible by this number");
			lines.add(String.valueOf(this.soilTemperature.stMaxDepth)+"\t\t\t"+"# max depth for the soil_temperature function equation, default is 180.  this number should be evenly divisible by deltaX");
			lines.add(String.valueOf(this.soilTemperature.use_soil_temp?1:0)+"\t\t\t"+"# flag, 1 to calculate soil_temperature, 0 to not calculate soil_temperature");
			lines.add("");
			lines.add("");
			lines.add("# ---- Transpiration regions ----");
			lines.add("# ndx  : 1=shallow, 2=medium, 3=deep, 4=very deep");
			lines.add("# layer: deepest layer number of the region. ");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add("#        Layers are defined in soils.in.");
			lines.add("# ndx    layer");
			lines.add(this.transpirationRegions.toString());
			Files.write(siteIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "SiteIn : onWrite : No data.");
		}
	}
	
	protected boolean getDeepdrain() {
		return this.model.flags.deepdrain;
	}

	protected SWC getSWC() {
		return this.swc;
	}
	protected Model getModel() {
		return this.model;
	}
	protected Snow getSnow() {
		return this.snow;
	}
	protected Drainage getDrainage() {
		return this.drainage;
	}
	protected Evaporation getEvaporation() {
		return this.evaporation;
	}
	protected Transpiration getTranspiration() {
		return this.transpiration;
	}
	protected Intrinsic getIntrinsic() {
		return this.intrinsic;
	}
	protected SoilTemperature getSoilTemperature() {
		return this.soilTemperature;
	}
	protected TranspirationRegions getTranspirationRegions() {
		return this.transpirationRegions;
	}
	protected boolean get_echoinits() {
		return this.EchoInits;
	}
	protected void set_echoinits(boolean echo) {
		this.EchoInits = echo;
	}
	protected int get_stNRGR() {
		return this.stNRGR;
	}
	
	private void _init_site_info() {
		LogFileIn f = LogFileIn.getInstance();
		
		int r,currentregion;
		int wiltminflag=0, initminflag=0;
		double evsum=0., trsum_forb = 0., trsum_tree = 0., trsum_shrub = 0., trsum_grass = 0., swcmin_help1, swcmin_help2;
		
		SW_SOILS.LayersInfo layersInfo = SW_Soils.getLayersInfo();
		
		/* sp->deepdrain indicates an extra (dummy) layer for deep drainage
		 * has been added, so n_layers really should be n_layers -1
		 * otherwise, the bottom layer is functional, so don't decrement n_layers
		 * and set deep_layer to zero as a flag.
		 * NOTE: deep_lyr is base0, n_layers is BASE1
		 */
		layersInfo.deep_lyr = this.model.flags.deepdrain ? --layersInfo.n_layers : 0;
		for(int s=0; s<layersInfo.n_layers; s++) {
			SW_SOILS.SW_LAYER_INFO lyr = SW_Soils.getLayer(s);
			/* sum ev and tr coefficients for later */
			evsum += lyr.evap_coeff;
			trsum_forb += lyr.transp_coeff_forb;
			trsum_tree += lyr.transp_coeff_tree;
			trsum_shrub += lyr.transp_coeff_shrub;
			trsum_grass += lyr.transp_coeff_grass;
			
			/* calculate soil water content at SWPcrit for each vegetation type */
			lyr.swcBulk_atSWPcrit_forb = SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, SW_VegProd.getCriticalSWP().forbs, lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width;
			lyr.swcBulk_atSWPcrit_tree = SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, SW_VegProd.getCriticalSWP().trees, lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width;
			lyr.swcBulk_atSWPcrit_shrub = SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, SW_VegProd.getCriticalSWP().shrubs, lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width;
			lyr.swcBulk_atSWPcrit_grass = SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, SW_VegProd.getCriticalSWP().grasses, lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width;
			
			/* Find which transpiration region the current soil layer
			 * is in and check validity of result. Region bounds are
			 * base1 but s is base0.
			 */
			/* for forbs */
			currentregion = 0;
			for(r=0; r<this.transpirationRegions.get_nRegions(); r++)
			{
				if (s < this.transpirationRegions.getRegion(r)) {
					if (Defines.isZero(lyr.transp_coeff_forb))
						break; /* end of transpiring layers */
					currentregion = r + 1;
					break;
				}
			}
			if (currentregion>0 || Defines.isZero(this.transpirationRegions.getRegion(currentregion))) {
				lyr.my_transp_rgn_forb = currentregion;
				layersInfo.n_transp_lyrs_forb = Math.max(layersInfo.n_transp_lyrs_forb, s);
			} else if (s == 0) {
				f.LogError(LogFileIn.LogMode.ERROR, "SW_SITE _init_site_info : Top soil layer must be included in your forb tranpiration regions.");
			} else if (r < layersInfo.n_transp_rgn) {
				f.LogError(LogFileIn.LogMode.ERROR,  "SW_SITE _init_site_info : Transpiration region "+String.valueOf(r+1)+" is deeper than the deepest layer with a"+
						"  forb transpiration coefficient > 0 "+String.valueOf(s)+". Please fix the discrepancy and try again.");
			}

			/* for trees */
			currentregion = 0;
			for(r=0; r<this.transpirationRegions.get_nRegions(); r++)
			{
				if (s < this.transpirationRegions.getRegion(r)) {
					if (Defines.isZero(lyr.transp_coeff_tree))
						break; /* end of transpiring layers */
					currentregion = r + 1;
					break;
				}
			}
			if (currentregion>0 || Defines.isZero(this.transpirationRegions.getRegion(currentregion))) {
				lyr.my_transp_rgn_tree = currentregion;
				layersInfo.n_transp_lyrs_tree = Math.max(layersInfo.n_transp_lyrs_tree, s);
			} else if (s == 0) {
				f.LogError(LogFileIn.LogMode.ERROR, "SW_SITE _init_site_info : Top soil layer must be included in your tree tranpiration regions.");
			} else if (r < layersInfo.n_transp_rgn) {
				f.LogError(LogFileIn.LogMode.ERROR,  "SW_SITE _init_site_info : Transpiration region "+String.valueOf(r+1)+" is deeper than the deepest layer with a"+
						"  tree transpiration coefficient > 0 "+String.valueOf(s)+". Please fix the discrepancy and try again.");
			}
			
			/* for shrubs */
			currentregion = 0;
			for(r=0; r<this.transpirationRegions.get_nRegions(); r++)
			{
				if (s < this.transpirationRegions.getRegion(r)) {
					if (Defines.isZero(lyr.transp_coeff_shrub))
						break; /* end of transpiring layers */
					currentregion = r + 1;
					break;
				}
			}
			if (currentregion>0 || Defines.isZero(this.transpirationRegions.getRegion(currentregion))) {
				lyr.my_transp_rgn_shrub = currentregion;
				layersInfo.n_transp_lyrs_shrub = Math.max(layersInfo.n_transp_lyrs_shrub, s);
			} else if (s == 0) {
				f.LogError(LogFileIn.LogMode.ERROR, "SW_SITE _init_site_info : Top soil layer must be included in your shrub tranpiration regions.");
			} else if (r < layersInfo.n_transp_rgn) {
				f.LogError(LogFileIn.LogMode.ERROR,  "SW_SITE _init_site_info : Transpiration region "+String.valueOf(r+1)+" is deeper than the deepest layer with a"+
						"  shrub transpiration coefficient > 0 "+String.valueOf(s)+". Please fix the discrepancy and try again.");
			}
			
			/* for grasses */
			currentregion = 0;
			for(r=0; r<this.transpirationRegions.get_nRegions(); r++)
			{
				if (s < this.transpirationRegions.getRegion(r)) {
					if (Defines.isZero(lyr.transp_coeff_grass))
						break; /* end of transpiring layers */
					currentregion = r + 1;
					break;
				}
			}
			if (currentregion>0 || Defines.isZero(this.transpirationRegions.getRegion(currentregion))) {
				lyr.my_transp_rgn_grass = currentregion;
				layersInfo.n_transp_lyrs_grass = Math.max(layersInfo.n_transp_lyrs_grass, s);
			} else if (s == 0) {
				f.LogError(LogFileIn.LogMode.ERROR, "SW_SITE _init_site_info : Top soil layer must be included in your grass tranpiration regions.");
			} else if (r < layersInfo.n_transp_rgn) {
				f.LogError(LogFileIn.LogMode.ERROR,  "SW_SITE _init_site_info : Transpiration region "+String.valueOf(r+1)+" is deeper than the deepest layer with a"+
						"  grass transpiration coefficient > 0 "+String.valueOf(s)+". Please fix the discrepancy and try again.");
			}
			
			/* Compute swc wet and dry limits and init value */
			if (Double.compare(this.swc.swc_min, 0.0)<0) { /* estimate swcBulk_min for each layer based on residual SWC from an equation in Rawls WJ, Brakensiek DL (1985) Prediction of soil water properties for hydrological modeling. In Watershed management in the Eighties (eds Jones EB, Ward TJ), pp. 293-299. American Society of Civil Engineers, New York.
			 or based on SWC at -3 MPa if smaller (= suction at residual SWC from Fredlund DG, Xing AQ (1994) EQUATIONS FOR THE SOIL-WATER CHARACTERISTIC CURVE. Canadian Geotechnical Journal, 31, 521-532.) */
				swcmin_help1 = SW_SOILWATER.SW_VWCBulkRes(lyr.fractionVolBulk_gravel, lyr.fractionWeightMatric_sand, lyr.fractionWeightMatric_clay, lyr.swcBulk_saturated / lyr.width) * lyr.width;
				swcmin_help2 = SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, 30., lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width;
				lyr.swcBulk_min = Math.max(0., Math.min(swcmin_help1, swcmin_help2));
			} else if (Double.compare(this.swc.swc_min, 1.0)>0) { /* assume that unit(_SWCMinVal) == -bar */
				lyr.swcBulk_min = SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, this.swc.swc_min, lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width;
			} else { /* assume that unit(_SWCMinVal) == cm/cm */
				lyr.swcBulk_min = this.swc.swc_min * lyr.width;
			}

			lyr.swcBulk_wet = Defines.GT(this.swc.swc_wet, 1.0) ? SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, this.swc.swc_wet, lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width : this.swc.swc_wet * lyr.width;
			lyr.swcBulk_init = Defines.GT(this.swc.swc_init, 1.0) ? SW_SOILWATER.SW_SWPmatric2VWCBulk(lyr.fractionVolBulk_gravel, this.swc.swc_init, lyr.psisMatric, lyr.binverseMatric, lyr.thetasMatric) * lyr.width : this.swc.swc_init * lyr.width;

			/* test validity of values */
			if (Double.compare(lyr.swcBulk_init, lyr.swcBulk_min)<0)
				initminflag++;
			if (Double.compare(lyr.swcBulk_wiltpt, lyr.swcBulk_min)<0)
				wiltminflag++;
			if (Double.compare(lyr.swcBulk_wet, lyr.swcBulk_min)<0) {
				f.LogError(LogFileIn.LogMode.ERROR, "SW_SITE _init_site_info : Layer "+String.valueOf(s+1)+" calculated swcBulk_wet "+String.valueOf(lyr.swcBulk_wet)+" <= swcBulk_min "+
						String.valueOf(lyr.swcBulk_min)+".  Recheck parameters and try again.");
			}

		} /*end ForEachSoilLayer */
		if(wiltminflag>0)
			f.LogError(LogFileIn.LogMode.WARN, String.format("SW_SITE _init_site_info : %d layers were found in which wiltpoint < swcBulk_min.\n"+
					"  You should reconsider wiltpoint or swcBulk_min.\n"+
					"  See site parameter file for swcBulk_min and site.log for swc details.", wiltminflag));
		if (initminflag>0)
			f.LogError(LogFileIn.LogMode.WARN, String.format("SW_SITE _init_site_info : %d layers were found in which swcBulk_init < swcBulk_min.\n"+
					"  You should reconsider swcBulk_init or swcBulk_min.\n"+
					"  See site parameter file for swcBulk_init and site.log for swc details.", initminflag));
		
		/* normalize the evap and transp coefficients separately
		 * to avoid obfuscation in the above loop */
		if (!(Double.compare(evsum, 1.0)==0)) {
			f.LogError(LogFileIn.LogMode.WARN, String.format("SW_SITE _init_site_info : Evap coefficients were normalized, "+
					"ev_co sum (%5.4f) != 1.0.\nNew coefficients are:", evsum));
			for(int s=0; s<layersInfo.n_layers; s++)
			{
				SW_Soils.getLayer(s).evap_coeff /= evsum;
				f.LogError(LogFileIn.LogMode.NOTE, String.format("  Layer %d : %5.4f", s + 1, SW_Soils.getLayer(s).evap_coeff));
			}
		}
		if (!(Double.compare(trsum_forb, 1.0)==0)) {
			f.LogError(LogFileIn.LogMode.WARN, String.format("SW_SITE _init_site_info : Transp coefficients for forbs were normalized, "+
					"tr_co_forb sum (%5.4f) != 1.0.\nNew Coefficients are:", trsum_forb));
			for(int s=0; s<layersInfo.n_layers; s++)
			{
				SW_Soils.getLayer(s).transp_coeff_forb /= trsum_forb;
				f.LogError(LogFileIn.LogMode.NOTE, String.format("  Layer %d : %5.4f", s + 1, SW_Soils.getLayer(s).transp_coeff_forb));
			}
		}
		if (!(Double.compare(trsum_tree, 1.0)==0)) {
			f.LogError(LogFileIn.LogMode.WARN, String.format("SW_SITE _init_site_info : Transp coefficients for trees were normalized, "+
					"tr_co_tree sum (%5.4f) != 1.0.\nNew coefficients are:", trsum_tree));
			for(int s=0; s<layersInfo.n_layers; s++)
			{
				SW_Soils.getLayer(s).transp_coeff_tree /= trsum_tree;
				f.LogError(LogFileIn.LogMode.NOTE, String.format("  Layer %d : %5.4f", s + 1, SW_Soils.getLayer(s).transp_coeff_tree));
			}
		}
		if (!(Double.compare(trsum_shrub, 1.0)==0)) {
			f.LogError(LogFileIn.LogMode.WARN, String.format("SW_SITE _init_site_info : Transp coefficients for shrubs were normalized, "+
					"tr_co_shrub sum (%5.4f) != 1.0.\nNew coefficients are:", trsum_shrub));
			for(int s=0; s<layersInfo.n_layers; s++)
			{
				SW_Soils.getLayer(s).transp_coeff_shrub /= trsum_shrub;
				f.LogError(LogFileIn.LogMode.NOTE, String.format("  Layer %d : %5.4f", s + 1, SW_Soils.getLayer(s).transp_coeff_shrub));
			}
		}
		if (!(Double.compare(trsum_grass, 1.0)==0)) {
			f.LogError(LogFileIn.LogMode.WARN, String.format("SW_SITE _init_site_info : Transp coefficients for grasses were normalized, "+
					"tr_co_grass sum (%5.4f) != 1.0.\nNew coefficients are:", trsum_grass));
			for(int s=0; s<layersInfo.n_layers; s++)
			{
				SW_Soils.getLayer(s).transp_coeff_grass /= trsum_grass;
				f.LogError(LogFileIn.LogMode.NOTE, String.format("  Layer %d : %5.4f", s + 1, SW_Soils.getLayer(s).transp_coeff_grass));
			}
		}
		
		this.stNRGR = (int)(this.soilTemperature.stMaxDepth / this.soilTemperature.stDeltaX) - 1; // getting the number of regressions, for use in the soil_temperature function
		if (!(Double.compare(Math.IEEEremainder(this.soilTemperature.stMaxDepth, this.soilTemperature.stDeltaX), 0.0)==0) || (this.stNRGR > Defines.MAX_ST_RGR)) {
			// resets it to the default values if the remainder of the division != 0.  fmod is like the % symbol except it works with double values
			// without this reset, then there wouldn't be a whole number of regressions in the soil_temperature function (ie there is a remainder from the division), so this way I don't even have to deal with that possibility
			if (this.stNRGR > Defines.MAX_ST_RGR)
				f.LogError(LogFileIn.LogMode.WARN, "\nSOIL_TEMP FUNCTION ERROR: the number of regressions is > the maximum number of regressions.  resetting max depth, deltaX, nRgr values to 180, 15, & 11 respectively\n");
			else
				f.LogError(LogFileIn.LogMode.WARN, "\nSOIL_TEMP FUNCTION ERROR: max depth is not evenly divisible by deltaX (ie the remainder != 0).  resetting max depth, deltaX, nRgr values to 180, 15, & 11 respectively\n");

			this.soilTemperature.stMaxDepth = 180.0;
			this.stNRGR = 11;
			this.soilTemperature.stDeltaX = 15.0;
		}
	}
	
	private void _echo_inputs(String fileSite) {
		/* =================================================== */
		LogFileIn f = LogFileIn.getInstance();
		
		f.LogError(LogFileIn.LogMode.NOTE, String.format("\n\n=====================================================\n"+
				"Site Related Parameters:\n"+
				"---------------------\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Site File: %s\n", fileSite));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Reset SWC values each year: %B\n", this.model.flags.reset_yr));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Use deep drainage reservoir: %B\n", this.model.flags.deepdrain));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Slow Drain Coefficient: %5.4f\n", this.drainage.slow_drain_coeff));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  PET Scale: %5.4f\n", this.model.coefficients.petMultiplier));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Proportion of surface water lost: %5.4f\n", this.model.coefficients.percentRunoff));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Latitude (radians): %4.2f\n", this.intrinsic.latitude));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Altitude (m a.s.l.): %4.2f \n", this.intrinsic.altitude));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Slope (degrees): %4.2f\n", this.intrinsic.slope));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Aspect (degrees): %4.2f\n", this.intrinsic.aspect));

		f.LogError(LogFileIn.LogMode.NOTE, String.format("\nSnow simulation parameters (SWAT2K model):\n----------------------\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Avg. air temp below which ppt is snow ( C): %5.4f\n", this.snow.TminAccu2));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Snow temperature at which snow melt starts ( C): %5.4f\n", this.snow.TmaxCrit));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Relative contribution of avg. air temperature to todays snow temperture vs. yesterday's snow temperature (0-1): %5.4f\n", this.snow.lambdasnow));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Minimum snow melt rate on winter solstice (cm/day/C): %5.4f\n", this.snow.RmeltMin));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Maximum snow melt rate on summer solstice (cm/day/C): %5.4f\n", this.snow.RmeltMax));

		f.LogError(LogFileIn.LogMode.NOTE, String.format("\nSoil Temperature Constants:\n----------------------\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Biomass Limiter constant: %5.4f\n", this.soilTemperature.bmLimiter));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  T1Param1: %5.4f\n", this.soilTemperature.t1Param1));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  T1Param2: %5.4f\n", this.soilTemperature.t1Param2));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  T1Param3: %5.4f\n", this.soilTemperature.t1Param3));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  csParam1: %5.4f\n", this.soilTemperature.csParam1));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  csParam2: %5.4f\n", this.soilTemperature.csParam2));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  shParam: %5.4f\n", this.soilTemperature.shParam));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  meanAirTemp: %5.4f\n", this.soilTemperature.meanAirTemp));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  deltaX: %5.4f\n", this.soilTemperature.stDeltaX));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  max depth: %5.4f\n", this.soilTemperature.stMaxDepth));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Make soil temperature calculations: %s\n", (this.soilTemperature.use_soil_temp) ? "TRUE" : "FALSE"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of regressions for the soil temperature function: %d\n", this.stNRGR));

		f.LogError(LogFileIn.LogMode.NOTE, String.format("\n------------ End of Site Parameters ------------------\n"));
		//fflush(logfp);

	}
}
