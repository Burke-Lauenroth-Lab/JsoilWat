package input;

import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SiteIn {
	private class SWC {
		public double swc_min=0;
		public double swc_init=0;
		public double swc_wet=0;
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
		public String toString() {
			if(nTranspRgn > 0) {
				String sTable = "";
				for(int i=0;i<nTranspRgn;i++) {
					sTable+="\t"+String.valueOf(i)+"\t"+String.valueOf(table[i-1][1])+"\n";
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
	private boolean data;
	private int nFileItemsRead;
	
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
	
	public void onClear() {
		this.data = false;
		this.transpirationRegions.onClear();
	}
	
	public void onRead(Path siteIn) throws IOException {
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
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : SWC Limit - swc_min : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 1:
					try {
						this.swc.swc_init = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : SWC Limit - swc_init : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 2:
					try {
						this.swc.swc_wet = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : SWC Limit - swc_wet : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 3:
					try {
						this.model.flags.reset = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Model Flags - reset : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 4:
					try {
						this.model.flags.deepdrain = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Model Flags - deepdrain : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 5:
					try {
						this.model.coefficients.petMultiplier = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Model Coefficients - PET multiplier : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 6:
					try {
						this.model.coefficients.percentRunoff = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Model Coefficients - runoff : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 7:
					try {
						this.snow.TminAccu2 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Snow Simulation Parameters - TminAccu2 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 8:
					try {
						this.snow.TmaxCrit = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Snow Simulation Parameters - TmaxCrit : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 9:
					try {
						this.snow.lambdasnow = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Snow Simulation Parameters - lambdasnow : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 10:
					try {
						this.snow.RmeltMin = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Snow Simulation Parameters - RmeltMin : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 11:
					try {
						this.snow.RmeltMax = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Snow Simulation Parameters - RmeltMax : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 12:
					try {
						this.drainage.slow_drain_coeff = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Drainage Coefficient - slow drain: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 13:
					try {
						this.evaporation.xinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Evaporation Coefficients - xinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 14:
					try {
						this.evaporation.slope = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Evaporation Coefficients - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 15:
					try {
						this.evaporation.yinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Evaporation Coefficients - yinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 16:
					try {
						this.evaporation.range = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Evaporation Coefficients - range : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 17:
					try {
						this.transpiration.xinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Transpiration Coefficients - xinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 18:
					try {
						this.transpiration.slope = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Transpiration Coefficients - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 19:
					try {
						this.transpiration.yinflec = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Transpiration Coefficients - yinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 20:
					try {
						this.transpiration.range = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Transpiration Coefficients - range : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 21:
					try {
						this.intrinsic.latitude = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Intrinsic Site Params - Latitiude : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 22:
					try {
						this.intrinsic.altitude = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Intrinsic Site Params - Altitude : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 23:
					try {
						this.intrinsic.slope = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Intrinsic Site Params - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 24:
					try {
						this.intrinsic.aspect = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Intrinsic Site Params - aspect : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 25:
					try {
						this.soilTemperature.bmLimiter = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - Biomass Limiter : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 26:
					try {
						this.soilTemperature.t1Param1 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - t1Param1 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 27:
					try {
						this.soilTemperature.t1Param2 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - t1Param2 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 28:
					try {
						this.soilTemperature.t1Param3 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - t1Param3 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 29:
					try {
						this.soilTemperature.csParam1 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - csParam1 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 30:
					try {
						this.soilTemperature.csParam2 = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - csParam2 : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 31:
					try {
						this.soilTemperature.shParam = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - shParam : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 32:
					try {
						this.soilTemperature.meanAirTemp = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - meanAirTemp : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 33:
					try {
						this.soilTemperature.stDeltaX = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - stDeltaX : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 34:
					try {
						this.soilTemperature.stMaxDepth = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - stMaxDepth : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 35:
					try {
						this.soilTemperature.use_soil_temp = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Soil Temperature Constants - use_soil_temp : Could not convert string to double. " + e.getMessage());
					}
					break;
				default:
					if(this.nFileItemsRead > 35 && this.nFileItemsRead <= 38 && !too_many_regions) {
						if(values.length != 2)
							f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Transpiration Regions : Expected 2 values, read "+String.valueOf(values.length));
						try {
							if(this.transpirationRegions.MAX_TRANSP_REGIONS < this.transpirationRegions.nTranspRgn) {
								too_many_regions = true;
								break;
							}
							this.transpirationRegions.set(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else {
						f.LogError(LogFileIn.LogMode.LOGERROR, "SiteIn onRead : Transpiration Regions : Too many regions.");
					}
					break;
				}
				this.nFileItemsRead++;
			}
		}
		this.data = true;
	}

	public void onWrite(Path siteIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# ---- SWC limits ----");
			lines.add(String.valueOf(this.swc.swc_min)+"\t\t\t# swc_min : cm/cm if 0 - <1.0, -bars if >= 1.0.; if < 0. then estimate residual water content for each layer");
			lines.add(String.valueOf(this.swc.swc_init)+"\t\t\t# swc_init: cm/cm if < 1.0, -bars if >= 1.0. ");
			lines.add(String.valueOf(this.swc.swc_wet)+"\t\t\t# swc_wet : cm/cm if < 1.0, -bars if >= 1.0. ");
			lines.add("");
			lines.add("# ---- Model flags and coefficients ----");
			lines.add(String.valueOf(this.model.flags.reset?1:0)+"\t\t\t# reset (1/0): reset/don't reset swc each new year");
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
			f.LogError(LogMode.LOGWARN, "ProductionIn : onWrite : No data.");
		}
	}
}
