package soilwat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soilwat.LogFileIn.LogMode;
import soilwat.SW_FILES.FILES_INPUT_DATA;
import soilwat.SW_MODEL.MODEL_INPUT_DATA;
import soilwat.SW_OUTPUT.OUTPUT_INPUT_DATA;
import soilwat.SW_OUTPUT.OutKey;
import soilwat.SW_OUTPUT.OutPeriod;
import soilwat.SW_OUTPUT.OutSum;
import soilwat.SW_SITE.Drainage;
import soilwat.SW_SITE.Evaporation;
import soilwat.SW_SITE.Intrinsic;
import soilwat.SW_SITE.Model;
import soilwat.SW_SITE.SWC;
import soilwat.SW_SITE.Snow;
import soilwat.SW_SITE.SoilTemperature;
import soilwat.SW_SITE.Transpiration;
import soilwat.SW_SITE.TranspirationRegions;
import soilwat.SW_SOILS.SOILS_INPUT_DATA;
import soilwat.SW_SOILWATER.SWC_INPUT_DATA;
import soilwat.SW_VEGESTAB.SPP_INPUT_DATA;
import soilwat.SW_VEGPROD.Albedo;
import soilwat.SW_VEGPROD.CanopyHeight;
import soilwat.SW_VEGPROD.CoverPercent;
import soilwat.SW_VEGPROD.CriticalSWP;
import soilwat.SW_VEGPROD.EsParamLimit;
import soilwat.SW_VEGPROD.EsTpartitioning;
import soilwat.SW_VEGPROD.HydraulicRedistribution;
import soilwat.SW_VEGPROD.LitterInterceptionParameters;
import soilwat.SW_VEGPROD.MonthlyProductionValues;
import soilwat.SW_VEGPROD.Shade;
import soilwat.SW_VEGPROD.VegetationComposition;
import soilwat.SW_VEGPROD.VegetationInterceptionParameters;
import soilwat.SW_WEATHER.WEATHER_INPUT_DATA;
import soilwat.SW_MARKOV.Probability;
import soilwat.SW_MARKOV.Covariance;

public class InputData {
	
	public static class SoilsIn {
		public int nLayers;
		public SOILS_INPUT_DATA[] layers = new SOILS_INPUT_DATA[25];
		private LogFileIn log;
		
		public SoilsIn(LogFileIn log) {
			this.log = log;
			for(int i=0; i<25; i++)
				this.layers[i] = new SOILS_INPUT_DATA();
		}
		
		public void onClear() {
			if(layers.length != 25) {
				for(int i=0; i<25; i++)
					this.layers[i] = new SOILS_INPUT_DATA();
			}
			for(int i=0; i<layers.length; i++) {
				this.layers[i].onClear();
			}
		}
		
		public void onRead(String swSoilsIn) throws Exception {
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swSoilsIn, getClass().getClassLoader());
			
			nLayers=0;
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					if(values.length != 12)
						f.LogError(LogFileIn.LogMode.ERROR, "SoilsIn onRead : Expected 10 Values read "+String.valueOf(values.length));
					if(nLayers == Defines.MAX_LAYERS)
						f.LogError(LogFileIn.LogMode.ERROR, "SoilsIn onRead : Too many layers specified "+String.valueOf(values.length));
					try {
						this.layers[nLayers].depth = Double.parseDouble(values[0]);
						this.layers[nLayers].soilMatric_density = Double.parseDouble(values[1]);
						this.layers[nLayers].fractionVolBulk_gravel = Double.parseDouble(values[2]);
						this.layers[nLayers].evap_coeff = Double.parseDouble(values[3]);
						this.layers[nLayers].transp_coeff_grass = Double.parseDouble(values[4]);
						this.layers[nLayers].transp_coeff_shrub = Double.parseDouble(values[5]);
						this.layers[nLayers].transp_coeff_tree = Double.parseDouble(values[6]);
						this.layers[nLayers].transp_coeff_forb = Double.parseDouble(values[7]);
						this.layers[nLayers].fractionWeightMatric_sand = Double.parseDouble(values[8]);
						this.layers[nLayers].fractionWeightMatric_clay = Double.parseDouble(values[9]);
						this.layers[nLayers].impermeability = Double.parseDouble(values[10]);
						this.layers[nLayers].sTemp = Double.parseDouble(values[11]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "Soils onRead : Could not convert string to double. " + e.getMessage());
					}
					nLayers++;
				}
			}
		}
		
		public void onWrite(String swSoilsIn) throws Exception{
			Path soilsin = Paths.get(swSoilsIn);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(soilsin, lines, StandardCharsets.UTF_8);
		}
		
		public double[][] getValues() {
			double[][] values = new double[nLayers][];
			for(int i=0; i<nLayers; i++)
				values[i] = this.layers[i].getValues();
			return values;
		}
		
		public void setValues(double[][] values) {
			this.nLayers = values.length;
			if(nLayers > 25)
				return;
			for(int i=0; i<nLayers; i++) {
				if (values[i].length == 12) {
					this.layers[i].depth = values[i][0];
					this.layers[i].soilMatric_density = values[i][1];
					this.layers[i].fractionVolBulk_gravel = values[i][2];
					this.layers[i].evap_coeff = values[i][3];
					this.layers[i].transp_coeff_grass = values[i][4];
					this.layers[i].transp_coeff_shrub = values[i][5];
					this.layers[i].transp_coeff_tree = values[i][6];
					this.layers[i].transp_coeff_forb = values[i][7];
					this.layers[i].fractionWeightMatric_sand = values[i][8];
					this.layers[i].fractionWeightMatric_clay = values[i][9];
					this.layers[i].impermeability = values[i][10];
					this.layers[i].sTemp = values[i][11];
				}
			}
		}
		
		public String toString() {
			String out="";
			out += "# Soil layer definitions";
			out += "# Location: ";
			out += "#";
			out += "# depth = (cm) lower limit of layer; layers must be in order of depth.";
			out += "# matricd = (g/cm^3) bulk density of soil in this layer.";
			out += "# gravel_content = the percent volume of each layer composed of gravel (i.e., particles > 2mm)";
			out += "# evco = (frac) proportion of total baresoil evap from this layer.";
			out += "# trco = (frac) proportion of total transpiration from this layer for each vegetation type (tree, forb, shrub, grass)";
			out += "# %sand = (frac) proportion of sand in layer (0-1.0).";
			out += "# %clay = (frac) proportion of clay in layer (0-1.0).";
			out += "# imperm = (frac) proportion of 'impermeability' to water percolation(/infiltration/drainage) in layer (0-1.0)";
			out += "# soiltemp = the initial temperature of each soil layer (in celcius), from the day before the simulation starts";
			out += "# Note that the evco and trco columns must sum to 1.0 or they will";
			out += "# be normalized.";
			out += "#";
			out += String.format("# %5s %10s %17s %7s %13s %13s %12s %12s %8s %8s %9s %11s\n","depth","matricd","gravel_content","evco","trco_grass","trco_shrub","trco_tree","trco_forb","%sand","%clay","imperm","soiltemp");
			for(int i=0; i<nLayers; i++) {
				out+=layers[i].toString()+"\n";
			}
			return out;
		}
	}
	public static class SiteIn {
		public SWC swcLimits = new SW_SITE.SWC();
		public Model modelFlagsCoef = new Model();
		public Snow snowSimParams = new Snow();
		public Drainage drainageCoef = new Drainage();
		public Evaporation evaporationCoef = new Evaporation();
		public Transpiration transpCoef = new Transpiration();
		public Intrinsic siteIntrinsicParams = new Intrinsic();
		public SoilTemperature soilTempConst = new SoilTemperature();
		public TranspirationRegions transpRegions;
		private LogFileIn log;
		
		public SiteIn(LogFileIn log) {
			this.log = log;
			transpRegions  = new TranspirationRegions(log);
		}
		
		public void onClear() {
			swcLimits.onClear();
			modelFlagsCoef.onClear();
			snowSimParams.onClear();
			drainageCoef.onClear();
			evaporationCoef.onClear();
			transpCoef.onClear();
			siteIntrinsicParams.onClear();
			soilTempConst.onClear();
			transpRegions.onClear();
		}
		
		public void onRead(String swSiteIn) throws Exception {
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swSiteIn, getClass().getClassLoader());
			int nFileItemsRead=0;
			boolean too_many_regions = false;
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					switch (nFileItemsRead) {
					case 0:
						try {
							swcLimits.swc_min = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : SWC Limit - swc_min : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 1:
						try {
							swcLimits.swc_init = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : SWC Limit - swc_init : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 2:
						try {
							swcLimits.swc_wet = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : SWC Limit - swc_wet : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 3:
						try {
							modelFlagsCoef.flags.reset_yr = Integer.parseInt(values[0])>0 ? true : false;
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Flags - reset : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 4:
						try {
							modelFlagsCoef.flags.deepdrain = Integer.parseInt(values[0])>0 ? true : false;
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Flags - deepdrain : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 5:
						try {
							modelFlagsCoef.coefficients.petMultiplier = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Coefficients - PET multiplier : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 6:
						try {
							modelFlagsCoef.coefficients.percentRunoff = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Model Coefficients - runoff : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 7:
						try {
							snowSimParams.TminAccu2 = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - TminAccu2 : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 8:
						try {
							snowSimParams.TmaxCrit = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - TmaxCrit : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 9:
						try {
							snowSimParams.lambdasnow = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - lambdasnow : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 10:
						try {
							snowSimParams.RmeltMin = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - RmeltMin : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 11:
						try {
							snowSimParams.RmeltMax = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Snow Simulation Parameters - RmeltMax : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 12:
						try {
							drainageCoef.slow_drain_coeff = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Drainage Coefficient - slow drain: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 13:
						try {
							evaporationCoef.xinflec = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - xinflec : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 14:
						try {
							evaporationCoef.slope = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - slope : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 15:
						try {
							evaporationCoef.yinflec = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - yinflec : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 16:
						try {
							evaporationCoef.range = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Evaporation Coefficients - range : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 17:
						try {
							transpCoef.xinflec = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - xinflec : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 18:
						try {
							transpCoef.slope = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - slope : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 19:
						try {
							transpCoef.yinflec = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - yinflec : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 20:
						try {
							transpCoef.range = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Coefficients - range : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 21:
						try {
							siteIntrinsicParams.latitude = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - Latitiude : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 22:
						try {
							siteIntrinsicParams.altitude = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - Altitude : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 23:
						try {
							siteIntrinsicParams.slope = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - slope : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 24:
						try {
							siteIntrinsicParams.aspect = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Intrinsic Site Params - aspect : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 25:
						try {
							soilTempConst.bmLimiter = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - Biomass Limiter : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 26:
						try {
							soilTempConst.t1Param1 = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - t1Param1 : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 27:
						try {
							soilTempConst.t1Param2 = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - t1Param2 : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 28:
						try {
							soilTempConst.t1Param3 = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - t1Param3 : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 29:
						try {
							soilTempConst.csParam1 = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - csParam1 : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 30:
						try {
							soilTempConst.csParam2 = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - csParam2 : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 31:
						try {
							soilTempConst.shParam = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - shParam : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 32:
						try {
							soilTempConst.meanAirTemp = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - meanAirTemp : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 33:
						try {
							soilTempConst.stDeltaX = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - stDeltaX : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 34:
						try {
							soilTempConst.stMaxDepth = Double.parseDouble(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - stMaxDepth : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 35:
						try {
							soilTempConst.use_soil_temp = Integer.parseInt(values[0])>0 ? true : false;
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Soil Temperature Constants - use_soil_temp : Could not convert string to double. " + e.getMessage());
						}
						break;
					default:
						if(nFileItemsRead > 35 && nFileItemsRead <= 38 && !too_many_regions) {
							if(values.length != 2)
								f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Regions : Expected 2 values, read "+String.valueOf(values.length));
							try {
								if(transpRegions.MAX_TRANSP_REGIONS < transpRegions.getnTranspRgn()) {
									too_many_regions = true;
									break;
								}
								transpRegions.set(Integer.parseInt(values[0]), Integer.parseInt(values[1])-1);
							} catch(NumberFormatException e) {
								f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
							}
						} else {
							f.LogError(LogFileIn.LogMode.ERROR, "SiteIn onRead : Transpiration Regions : Too many regions.");
						}
						break;
					}
					nFileItemsRead++;
				}
			}
		}
		
		public void onWrite(String swSiteIn) throws Exception{
			Path sitein = Paths.get(swSiteIn);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(sitein, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out="";
			out+=swcLimits.toString();
			out+="\n";
			out+=modelFlagsCoef.toString();
			out+="\n";
			out+=snowSimParams.toString();
			out+="\n";
			out+=drainageCoef.toString();
			out+="\n";
			out+=evaporationCoef.toString();
			out+="\n";
			out+=transpCoef.toString();
			out+="\n";
			out+=siteIntrinsicParams.toString();
			out+="\n";
			out+=soilTempConst.toString();
			out+="\n";
			out+="# ---- Transpiration regions ----\n";
			out+="# ndx  : 1=shallow, 2=medium, 3=deep, 4=very deep\n";
			out+="# layer: deepest layer number of the region. \n";
			out+="# Grasses	Shrubs		Trees		Forbs\n";
			out+="#        Layers are defined in soils.in.\n";
			out+="# ndx    layer\n";
			out+=this.transpRegions.toString();
			return out;
		}
	}
	public static class ProdIn {
		public VegetationComposition vegComp = new VegetationComposition();
		public Albedo albedo = new Albedo();
		public CoverPercent coverPercent = new CoverPercent();
		public CanopyHeight canopyHeight = new CanopyHeight();
		public VegetationInterceptionParameters vegIntercParams = new VegetationInterceptionParameters();
		public LitterInterceptionParameters litterIntercParams = new LitterInterceptionParameters();
		public EsTpartitioning esTpart = new EsTpartitioning();
		public EsParamLimit esLimit = new EsParamLimit();
		public Shade shade = new Shade();
		public HydraulicRedistribution hydraulicRedist = new HydraulicRedistribution();
		public CriticalSWP criticalSWP = new CriticalSWP();
		public MonthlyProductionValues monthlyProd = new MonthlyProductionValues();
		private LogFileIn log;
		
		public ProdIn(LogFileIn log) {
			this.log = log;
		}
		
		public void onClear() {
			vegComp.onClear();
			albedo.onClear();
			coverPercent.onClear();
			canopyHeight.onClear();
			vegIntercParams.onClear();
			litterIntercParams.onClear();
			esTpart.onClear();
			esLimit.onClear();
			shade.onClear();
			hydraulicRedist.onClear();
			criticalSWP.onClear();
			monthlyProd.onClear();
		}
		
		public void onRead(String swProdIn) throws Exception {
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swProdIn, getClass().getClassLoader());
			int month=0;
			int nFileItemsRead=0;
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					switch (nFileItemsRead) {
					case 0:
						if(values.length != 5)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : VegetationComposition : Expected 5 Values read "+String.valueOf(values.length));
						try {
							this.vegComp.grass = Double.parseDouble(values[0]);
							this.vegComp.shrub = Double.parseDouble(values[1]);
							this.vegComp.tree = Double.parseDouble(values[2]);
							this.vegComp.forb = Double.parseDouble(values[3]);
							this.vegComp.bareGround = Double.parseDouble(values[4]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : VegetationComposition : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 1:
						if(values.length != 5)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Albedo : Expected 5 Values read "+String.valueOf(values.length));
						try {
							this.albedo.grass = Double.parseDouble(values[0]);
							this.albedo.shrub = Double.parseDouble(values[1]);
							this.albedo.tree = Double.parseDouble(values[2]);
							this.albedo.forb = Double.parseDouble(values[3]);
							this.albedo.bareGround = Double.parseDouble(values[4]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Albedo : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 2:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Cover Percent : Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.coverPercent.grass = Double.parseDouble(values[0]);
							this.coverPercent.shrub = Double.parseDouble(values[1]);
							this.coverPercent.tree = Double.parseDouble(values[2]);
							this.coverPercent.forb = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Cover Percent : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 3:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - xinflec : Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.canopyHeight.grass.xinflec = Double.parseDouble(values[0]);
							this.canopyHeight.shrub.xinflec = Double.parseDouble(values[1]);
							this.canopyHeight.tree.xinflec = Double.parseDouble(values[2]);
							this.canopyHeight.forb.xinflec = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - xinflec : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 4:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - yinflec : Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.canopyHeight.grass.yinflec = Double.parseDouble(values[0]);
							this.canopyHeight.shrub.yinflec = Double.parseDouble(values[1]);
							this.canopyHeight.tree.yinflec = Double.parseDouble(values[2]);
							this.canopyHeight.forb.yinflec = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - yinflec : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 5:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - range : Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.canopyHeight.grass.range = Double.parseDouble(values[0]);
							this.canopyHeight.shrub.range = Double.parseDouble(values[1]);
							this.canopyHeight.tree.range = Double.parseDouble(values[2]);
							this.canopyHeight.forb.range = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - range : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 6:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - slope : Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.canopyHeight.grass.slope = Double.parseDouble(values[0]);
							this.canopyHeight.shrub.slope = Double.parseDouble(values[1]);
							this.canopyHeight.tree.slope = Double.parseDouble(values[2]);
							this.canopyHeight.forb.slope = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - slope : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 7:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - height: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.canopyHeight.grass.canopyHeight = Double.parseDouble(values[0]);
							this.canopyHeight.shrub.canopyHeight = Double.parseDouble(values[1]);
							this.canopyHeight.tree.canopyHeight = Double.parseDouble(values[2]);
							this.canopyHeight.forb.canopyHeight = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - height : Could not convert string to double. " + e.getMessage());
						}
						break;
					case 8:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - a: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.vegIntercParams.grass.a = Double.parseDouble(values[0]);
							this.vegIntercParams.shrub.a = Double.parseDouble(values[1]);
							this.vegIntercParams.tree.a = Double.parseDouble(values[2]);
							this.vegIntercParams.forb.a = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - a: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 9:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - b: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.vegIntercParams.grass.b = Double.parseDouble(values[0]);
							this.vegIntercParams.shrub.b = Double.parseDouble(values[1]);
							this.vegIntercParams.tree.b = Double.parseDouble(values[2]);
							this.vegIntercParams.forb.b = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - b: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 10:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - c: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.vegIntercParams.grass.c = Double.parseDouble(values[0]);
							this.vegIntercParams.shrub.c = Double.parseDouble(values[1]);
							this.vegIntercParams.tree.c = Double.parseDouble(values[2]);
							this.vegIntercParams.forb.c = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - c: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 11:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - d: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.vegIntercParams.grass.d = Double.parseDouble(values[0]);
							this.vegIntercParams.shrub.d = Double.parseDouble(values[1]);
							this.vegIntercParams.tree.d = Double.parseDouble(values[2]);
							this.vegIntercParams.forb.d = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - d: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 12:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - a: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.litterIntercParams.grass.a = Double.parseDouble(values[0]);
							this.litterIntercParams.shrub.a = Double.parseDouble(values[1]);
							this.litterIntercParams.tree.a = Double.parseDouble(values[2]);
							this.litterIntercParams.forb.a = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - a: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 13:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - b: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.litterIntercParams.grass.b = Double.parseDouble(values[0]);
							this.litterIntercParams.shrub.b = Double.parseDouble(values[1]);
							this.litterIntercParams.tree.b = Double.parseDouble(values[2]);
							this.litterIntercParams.forb.b = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - b: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 14:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - c: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.litterIntercParams.grass.c = Double.parseDouble(values[0]);
							this.litterIntercParams.shrub.c = Double.parseDouble(values[1]);
							this.litterIntercParams.tree.c = Double.parseDouble(values[2]);
							this.litterIntercParams.forb.c = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - c: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 15:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - d: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.litterIntercParams.grass.d = Double.parseDouble(values[0]);
							this.litterIntercParams.shrub.d = Double.parseDouble(values[1]);
							this.litterIntercParams.tree.d = Double.parseDouble(values[2]);
							this.litterIntercParams.forb.d = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - d: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 16:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : estPartitioning: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.esTpart.grass = Double.parseDouble(values[0]);
							this.esTpart.shrub = Double.parseDouble(values[1]);
							this.esTpart.tree = Double.parseDouble(values[2]);
							this.esTpart.forb = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : estPartitioning: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 17:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : esLimit: Expected 4 Values read "+String.valueOf(values.length)+". Line:"+line);
						try {
							this.esLimit.grass = Double.parseDouble(values[0]);
							this.esLimit.shrub = Double.parseDouble(values[1]);
							this.esLimit.tree = Double.parseDouble(values[2]);
							this.esLimit.forb = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : esLimit: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 18:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeScale: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.shade.grass.shadeScale = Double.parseDouble(values[0]);
							this.shade.shrub.shadeScale = Double.parseDouble(values[1]);
							this.shade.tree.shadeScale = Double.parseDouble(values[2]);
							this.shade.forb.shadeScale = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeScale: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 19:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeMaximalDeadBiomass: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.shade.grass.shadeMaximalDeadBiomass = Double.parseDouble(values[0]);
							this.shade.shrub.shadeMaximalDeadBiomass = Double.parseDouble(values[1]);
							this.shade.tree.shadeMaximalDeadBiomass = Double.parseDouble(values[2]);
							this.shade.forb.shadeMaximalDeadBiomass = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeMaximalDeadBiomass: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 20:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - xinflec: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.shade.grass.xinflec = Double.parseDouble(values[0]);
							this.shade.shrub.xinflec = Double.parseDouble(values[1]);
							this.shade.tree.xinflec = Double.parseDouble(values[2]);
							this.shade.forb.xinflec = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - xinflec: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 21:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - yinflec: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.shade.grass.yinflec = Double.parseDouble(values[0]);
							this.shade.shrub.yinflec = Double.parseDouble(values[1]);
							this.shade.tree.yinflec = Double.parseDouble(values[2]);
							this.shade.forb.yinflec = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - yinflec: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 22:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - range: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.shade.grass.range = Double.parseDouble(values[0]);
							this.shade.shrub.range = Double.parseDouble(values[1]);
							this.shade.tree.range = Double.parseDouble(values[2]);
							this.shade.forb.range = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - range: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 23:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - slope: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.shade.grass.slope = Double.parseDouble(values[0]);
							this.shade.shrub.slope = Double.parseDouble(values[1]);
							this.shade.tree.slope = Double.parseDouble(values[2]);
							this.shade.forb.slope = Double.parseDouble(values[3]); 
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - slope: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 24:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - flag: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.hydraulicRedist.grass.flag = Integer.parseInt(values[0])>0 ? true : false;
							this.hydraulicRedist.shrub.flag = Integer.parseInt(values[1])>0 ? true : false;
							this.hydraulicRedist.tree.flag = Integer.parseInt(values[2])>0 ? true : false;
							this.hydraulicRedist.forb.flag = Integer.parseInt(values[3])>0 ? true : false;
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - flag: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 25:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - maxCondroot: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.hydraulicRedist.grass.maxCondRoot = Double.parseDouble(values[0]);
							this.hydraulicRedist.shrub.maxCondRoot = Double.parseDouble(values[1]);
							this.hydraulicRedist.tree.maxCondRoot = Double.parseDouble(values[2]);
							this.hydraulicRedist.forb.maxCondRoot = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - maxCondroot: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 26:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - swp50: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.hydraulicRedist.grass.swp50 = Double.parseDouble(values[0]);
							this.hydraulicRedist.shrub.swp50 = Double.parseDouble(values[1]);
							this.hydraulicRedist.tree.swp50 = Double.parseDouble(values[2]);
							this.hydraulicRedist.forb.swp50 = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - swp50: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 27:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - shapeCond: Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.hydraulicRedist.grass.shapeCond = Double.parseDouble(values[0]);
							this.hydraulicRedist.shrub.shapeCond = Double.parseDouble(values[1]);
							this.hydraulicRedist.tree.shapeCond = Double.parseDouble(values[2]);
							this.hydraulicRedist.forb.shapeCond = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - shapeCond: Could not convert string to double. " + e.getMessage());
						}
						break;
					case 28:
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Critical soil water potential (MPa): Expected 4 Values read "+String.valueOf(values.length));
						try {
							this.criticalSWP.grass = Double.parseDouble(values[0]);
							this.criticalSWP.shrub = Double.parseDouble(values[1]);
							this.criticalSWP.tree = Double.parseDouble(values[2]);
							this.criticalSWP.forb = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Critical soil water potential (MPa): Could not convert string to double. " + e.getMessage());
						}
						break;
					default:
						if(nFileItemsRead > 28 && nFileItemsRead <= 40) {
							if(values.length != 4)
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
							try {
								month = nFileItemsRead-29;
								this.monthlyProd.grass.litter[month] = Double.parseDouble(values[0]);
								this.monthlyProd.grass.biomass[month] = Double.parseDouble(values[1]);
								this.monthlyProd.grass.percLive[month] = Double.parseDouble(values[2]);
								this.monthlyProd.grass.lai_conv[month] = Double.parseDouble(values[3]);
							} catch(NumberFormatException e) {
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
							}
						} else if(nFileItemsRead > 40 && nFileItemsRead <= 52) {
							if(values.length != 4)
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
							try {
								month = nFileItemsRead-41;
								this.monthlyProd.shrub.litter[month] = Double.parseDouble(values[0]);
								this.monthlyProd.shrub.biomass[month] = Double.parseDouble(values[1]);
								this.monthlyProd.shrub.percLive[month] = Double.parseDouble(values[2]);
								this.monthlyProd.shrub.lai_conv[month] = Double.parseDouble(values[3]);
							} catch(NumberFormatException e) {
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
							}
						} else if(nFileItemsRead > 52 && nFileItemsRead <= 64) {
							if(values.length != 4)
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
							try {
								month = nFileItemsRead-53;
								this.monthlyProd.tree.litter[month] = Double.parseDouble(values[0]);
								this.monthlyProd.tree.biomass[month] = Double.parseDouble(values[1]);
								this.monthlyProd.tree.percLive[month] = Double.parseDouble(values[2]);
								this.monthlyProd.tree.lai_conv[month] = Double.parseDouble(values[3]);
							} catch(NumberFormatException e) {
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
							}
						} else if(nFileItemsRead > 64 && nFileItemsRead <= 76) {
							if(values.length != 4)
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
							try {
								month = nFileItemsRead-65;
								this.monthlyProd.forb.litter[month] = Double.parseDouble(values[0]);
								this.monthlyProd.forb.biomass[month] = Double.parseDouble(values[1]);
								this.monthlyProd.forb.percLive[month] = Double.parseDouble(values[2]);
								this.monthlyProd.forb.lai_conv[month] = Double.parseDouble(values[3]);
							} catch(NumberFormatException e) {
								f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
							}
						} else {
							f.LogError(LogMode.ERROR, "Unknown line read. "+line);
						}
						break;
					}
					nFileItemsRead++;
				}
			}
		}
		
		public void onWrite(String swProdIn) throws Exception{
			Path prodin = Paths.get(swProdIn);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(prodin, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out="";
			out += "# Plant production data file for SOILWAT\n";
			out += vegComp.toString();
			out += "\n\n";
			out += albedo.toString();
			out += "\n\n";
			out += coverPercent.toString();
			out += "\n\n";
			out += canopyHeight.toString();
			out += "\n\n";
			out += vegIntercParams.toString();
			out += "\n\n";
			out += litterIntercParams.toString();
			out += "\n\n";
			out += esTpart.toString();
			out += "\n\n";
			out += esLimit.toString();
			out += "\n\n";
			out += shade.toString();
			out += "\n\n";
			out += hydraulicRedist.toString();
			out += "\n\n";
			out += criticalSWP.toString();
			out += "\n\n";
			out += "# -------------- Monthly production values ------------\n";
			out += "# Litter   - dead leafy material on the ground (g/m^2 ).\n";
			out += "# Biomass  - living and dead/woody aboveground standing biomass (g/m^2).\n";
			out += "# %Live    - proportion of Biomass that is actually living (0-1.0).\n";
			out += "# LAI_conv - monthly amount of biomass needed to produce LAI=1.0 (g/m^2).\n";
			out += "# There should be 12 rows, one for each month, starting with January.\n";
			out += "#\n";
			out += monthlyProd.toString();
			return out;
		}
	}
	public static class OutputIn {
		public String outsep = "\t";
		public boolean[] TimeSteps = new boolean[4];
		public OUTPUT_INPUT_DATA[] outputs = new OUTPUT_INPUT_DATA[SW_OUTPUT.SW_OUTNKEYS];
		private LogFileIn log;
		
		public OutputIn(LogFileIn log) {
			this.log = log;
			for(int i=0; i<4; i++)
				this.TimeSteps[i] = false;
			for(int i=0; i<SW_OUTPUT.SW_OUTNKEYS; i++) {
				outputs[i] = new OUTPUT_INPUT_DATA();
				outputs[i].mykey = OutKey.fromInt(i);
				outputs[i].periodColumn = OutPeriod.SW_DAY;
				outputs[i].sumtype = OutSum.eSW_Off;
				outputs[i].use = false;
			}
		}
		
		public void onClear() {
			for(int i=0; i<4; i++)
				this.TimeSteps[i] = false;
			for(int i=0; i<SW_OUTPUT.SW_OUTNKEYS; i++) {
				outputs[i] = new OUTPUT_INPUT_DATA();
				outputs[i].mykey = OutKey.fromInt(i);
				outputs[i].periodColumn = OutPeriod.SW_DAY;
				outputs[i].sumtype = OutSum.eSW_Off;
				outputs[i].use = false;
			}
		}
		
		public void onRead(String swOutputSetupIn) throws Exception {
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swOutputSetupIn, getClass().getClassLoader());
			
			boolean useTimeStep = false;
			OutKey k = OutKey.eSW_NoKey;
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					
					if(values[0].equals("TIMESTEP")) {
						for(int i=1; i<values.length; i++) {
							TimeSteps[OutPeriod.getEnum(values[i].toUpperCase()).idx()] = true;
							//numPeriod++;
						}
						useTimeStep=true;
						continue;
					} else {
						if(values.length < 6) {
							if(values[0].equals("OUTSEP")) {
								if(values[1].equals("t"))
									outsep="\t";
								else if(values[1].equals("s"))
									outsep=" ";
								else
									outsep=values[1];
								continue;
							} else {
								f.LogError(LogMode.ERROR, "OutputSetupIn onRead: Insufficient key parameters for item.");
								continue;
							}
						}
						k = OutKey.getEnum(values[0]);
						if(!useTimeStep) {
							TimeSteps[OutPeriod.getEnum(values[2]).idx()] = true;
							outputs[k.idx()].periodColumn = OutPeriod.getEnum(values[2]);
						}
						
					}
					
					//Set the values		
					outputs[k.idx()].mykey = k;
					outputs[k.idx()].use = true;
					outputs[k.idx()].sumtype = OutSum.getEnum(values[1]);
					outputs[k.idx()].periodColumn = OutPeriod.getEnum(values[2]);
					outputs[k.idx()].filename_prefix = values[5];
					try {
						outputs[k.idx()].first_orig = Integer.valueOf(values[3]);
						if(values[4].toLowerCase().equals("end"))
							outputs[k.idx()].last_orig = 366;
						else
							outputs[k.idx()].last_orig = Integer.valueOf(values[4]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "OutputSetupIn onRead: Could not covert start or end."+e.getMessage());
					}
					if(outputs[k.idx()].sumtype == OutSum.eSW_Off && outputs[k.idx()].last_orig == 0) {
						outputs[k.idx()].last_orig = 366;
					}
					if (outputs[k.idx()].last_orig == 0) {
						f.LogError(LogMode.ERROR, "OutputSetupIn onRead : Invalid ending day");
					}
				}
			}
		}
		
		public void onWrite(String swOutputSetupIn) throws Exception{
			Path outin = Paths.get(swOutputSetupIn);
			List<String> lines = new ArrayList<String>();
			lines.add("# Output setup file for SOILWAT v4 compiled on Mac OS X (20100202)");
			lines.add("#");
			lines.add("# Notes:");
			lines.add("# Time periods available:  DY,WK,MO,YR");
			lines.add("#   eg, if DY is chosen then 100,200 would mean to use the second hundred days");
			lines.add("#   But if YR is chosen, start and end numbers are in days so only those days");
			lines.add("#   are reported for the yearly average.");
			lines.add("# Some keys from older versions (fortran and the c versions mimicking the fortran");
			lines.add("#   version) are not currently implemented:");
			lines.add("#   ALLH20, WTHR.");
			lines.add("#");
			lines.add("# ESTABL only produces yearly output, namely, DOY for each species requested.");
			lines.add("#   Thus, to minimize typo errors, all flags are ignored except the filename.");
			lines.add("#   Output is simply the day of the year establishment occurred for each species");
			lines.add("#   in each year of the model run.  Refer to the estabs.in file for more info.");
			lines.add("#");
			lines.add("# DEEPSWC produces output only if the deepdrain flag is set in siteparam.in.");
			lines.add("#");
			lines.add("# Filename prefixes should not have a file extension.");
			lines.add("# Case is unimportant.");
			lines.add("#");
			lines.add("# SUMTYPEs are one of the following:");
			lines.add("#  OFF - no output for this variable");
			lines.add("#  SUM - sum the variable for each day in the output period");
			lines.add("#  AVG - average the variable over the output period");
			lines.add("#  FIN - output value of final day in the period; soil water variables only.");
			lines.add("# Note that SUM and AVG are the same if timeperiod = dy.");
			lines.add("#");
			lines.add("# (3-Sep-03) OUTSEP key indicates the output separator.  This method");
			lines.add("# allows older files to work with the new version.  The default is a");
			lines.add("# tab.  Other options are 's' or 't' for space or tab (no quotes)");
			lines.add("# or any other printable character as itself (eg, :;| etc).  The given");
			lines.add("# separator will apply to all of the output files.  Note that only lowercase");
			lines.add("# letters 's' or 't' are synonyms.");
			lines.add("#");
			lines.add("# (01/17/2013) TIMESTEP key indicates which periods you want to output.");
			lines.add("# You can output all the periods at a time, just one, or however many");
			lines.add("# you want. To change which periods to output type 'dy' for day,");
			lines.add("# 'wk' for week, 'mo' for month, and 'yr' for year after TIMESTEP");
			lines.add("# in any order. For example: 'TIMESTEP mo wk' will output for month and week");
			lines.add(this.toString());
			Files.write(outin, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out = "";
			if(outsep.equals("\t"))
				out += "OUTSEP t\n";
			else if(outsep.equals(" "))
				out += "OUTSEP s\n";
			else
				out += "OUTSEP "+outsep+"\n";
			if(TimeSteps[0] || TimeSteps[1] || TimeSteps[2] || TimeSteps[3]) {
				String temp="TIMESTEP";
				for(int i=0; i<4; i++) {
					if(TimeSteps[i] == true) {
						temp+=" ";
						switch(i) {
						case 0:
							temp+="dy";
							break;
						case 1:
							temp+="wk";
							break;
						case 2:
							temp+="mo";
							break;
						case 3:
							temp+="yr";
							break;
						}
					}
				}
				out += temp+"\n";
			}
			out += "\n";
			out += String.format("#     %4s     %7s   %6s   %5s    %3s    %15s   %7s\n","key","SUMTYPE","PERIOD","start","end","filename_prefix","comment");
			for(int i=0; i<28; i++) {
				if(outputs[i].use)
					out+=String.format("%13s %7s   %6s      %2s     %3s %15s      %s\n", outputs[i].mykey.key(),outputs[i].sumtype.key(),outputs[i].periodColumn.key(),String.valueOf(outputs[i].first_orig), outputs[i].last_orig==366?"end":String.valueOf(outputs[i].last_orig),outputs[i].filename_prefix, outputs[i].mykey.getComment());
			}
			return out;
		}
	}
	public static class EstabIn {
		public boolean use = false;
		public List<String> estabFiles = new ArrayList<String>();
		public List<SPP_INPUT_DATA> spps = new ArrayList<SPP_INPUT_DATA>();
		private LogFileIn log;
		
		public EstabIn(LogFileIn log) {
			this.log = log;
		}
		
		//Soilwat Explorer needs log to create SPPS. This is a short cut.
		public LogFileIn getLog() {
			return log;
		}
		
		public void onClear() {
			this.use = false;
			this.estabFiles.clear();
			this.spps.clear();
		}
		
		public String[] getSPPSnames() {
			String[] names = new String[spps.size()];
			for(int i=0;i<spps.size();i++) {
				names[i] = spps.get(i).sppName;
			}
			return names;
		}
		
		public SPP_INPUT_DATA getSPP(String sppName) {
			String[] names = getSPPSnames();
			if(Arrays.asList(names).contains(sppName)) {
				return spps.get(Arrays.asList(names).indexOf(sppName));
			} else {
				return null;
			}
		}
		
		public boolean addSPP(SPP_INPUT_DATA sp, String fileNamePath) {
			if(!spps.contains(sp)) {
				spps.add(sp);
				estabFiles.add(fileNamePath);
				return true;
			} else {
				return false;
			}
		}
		
		public boolean removeSPP(SPP_INPUT_DATA sp) {
			if(spps.contains(sp)) {
				estabFiles.remove(spps.indexOf(sp));
				spps.remove(sp);
				return true;
			} else {
				return false;
			}
		}
		
		/**
		 * This will read establishment setup file and also read in the spp files listed.
		 * @param projectDirectory
		 * @param swEstabIn
		 * @throws Exception 
		 */
		public void onRead(String projectDirectory, String swEstabIn) throws Exception {
			int lineno=0;
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(Paths.get(projectDirectory, swEstabIn).toString(), getClass().getClassLoader());
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					switch (lineno) {
					case 0:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn onRead : Expected only one value for use line.");
						try {
							this.use = Integer.parseInt(values[0])>0 ? true : false;
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn onRead : Could not convert use line.");
						}
						break;
					default:
						estabFiles.add(values[0]);
						//_read_spp(prjDir.resolve(values[0]), values[0]);
						break;
					}
					lineno++;
				}
			}
			if(lineno == 0) {
				this.use = false;
			}
			if(this.use) {
				for (String path : estabFiles) {
					SPP_INPUT_DATA sp = new SPP_INPUT_DATA(log);
					sp.onRead(Paths.get(projectDirectory, path).toString());
					spps.add(sp);
				}
			}
		}
		
		public void onWrite(String projectDirectory, String swEstabIn) throws Exception{
			Path estabin = Paths.get(projectDirectory, swEstabIn);
			List<String> lines = new ArrayList<String>();
			lines.add("# list of filenames for which to check establishment");
			lines.add("# each filename pertains to a species and contains the");
			lines.add("# soil moisture and timing parameters required for the");
			lines.add("# species to establish in a given year.");
			lines.add("# There is no limit to the number of files in the list.");
			lines.add("# to suppress checking establishment, comment all the");
			lines.add("# lines below.");
			lines.add("");
			lines.add(String.valueOf(this.use?1:0)+"\t"+"# use flag; 1=check establishment, 0=don't check, ignore following\n");
			if(this.estabFiles.size() > 0) {
				for(int i=0; i<this.estabFiles.size(); i++) {
					lines.add(this.estabFiles.get(i)+"\n");
				}
			}
			Files.write(estabin, lines, StandardCharsets.UTF_8);
			
			int i=0;
			for (SPP_INPUT_DATA v : spps) {
				v.onWrite(Paths.get(projectDirectory,this.estabFiles.get(i)).toString());
				i++;
			}
		}
				
		public String toString() {
			String out = "";
			out += String.valueOf(this.use?1:0)+"\t"+"# use flag; 1=check establishment, 0=don't check, ignore following\n";
			if(this.estabFiles.size() > 0) {
				for(int i=0; i<this.estabFiles.size(); i++) {
					out += this.estabFiles.get(i)+"\n";
				}
			}
			out+="\n";
			for (SPP_INPUT_DATA v : spps) {
				out  += v.toString()+"\n";
			}
	
			return out;
		}
	}
	public static class CloudIn {
		public double[] cloudcov = new double[Times.MAX_MONTHS],	/* monthly cloud cover (frac) */
	    windspeed = new double[Times.MAX_MONTHS],					/* windspeed (m/s) */
	    r_humidity = new double[Times.MAX_MONTHS],					/* relative humidity (%) */
	    transmission = new double[Times.MAX_MONTHS],				/* frac light transmitted by atmos. */ /* used as input for petfunc, but algorithm cancels it out */
	    snow_density = new double[Times.MAX_MONTHS];				/* snow density (kg/m3) */
		private LogFileIn log;
		
		public CloudIn(LogFileIn log) {
			this.log = log;
		}
		
		public void onClear() {
			for(int i=0; i<Times.MAX_MONTHS; i++) {
				cloudcov[i]=windspeed[i]=r_humidity[i]=transmission[i]=snow_density[i]=0;
			}
		}
		public void onSetCloudCov(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
			cloudcov[0] = m1;
			cloudcov[1] = m2;
			cloudcov[2] = m3;
			cloudcov[3] = m4;
			cloudcov[4] = m5;
			cloudcov[5] = m6;
			cloudcov[6] = m7;
			cloudcov[7] = m8;
			cloudcov[8] = m9;
			cloudcov[9] = m10;
			cloudcov[10] = m11;
			cloudcov[11] = m12;
		}
		public void onSetWindspeed(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
			windspeed[0] = m1;
			windspeed[1] = m2;
			windspeed[2] = m3;
			windspeed[3] = m4;
			windspeed[4] = m5;
			windspeed[5] = m6;
			windspeed[6] = m7;
			windspeed[7] = m8;
			windspeed[8] = m9;
			windspeed[9] = m10;
			windspeed[10] = m11;
			windspeed[11] = m12;
		}
		public void onSetRhumidity(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
			r_humidity[0] = m1;
			r_humidity[1] = m2;
			r_humidity[2] = m3;
			r_humidity[3] = m4;
			r_humidity[4] = m5;
			r_humidity[5] = m6;
			r_humidity[6] = m7;
			r_humidity[7] = m8;
			r_humidity[8] = m9;
			r_humidity[9] = m10;
			r_humidity[10] = m11;
			r_humidity[11] = m12;
		}
		public void onSetTransmission(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
			transmission[0] = m1;
			transmission[1] = m2;
			transmission[2] = m3;
			transmission[3] = m4;
			transmission[4] = m5;
			transmission[5] = m6;
			transmission[6] = m7;
			transmission[7] = m8;
			transmission[8] = m9;
			transmission[9] = m10;
			transmission[10] = m11;
			transmission[11] = m12;
		}
		public void onSetSnowDensity(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
			snow_density[0] = m1;
			snow_density[1] = m2;
			snow_density[2] = m3;
			snow_density[3] = m4;
			snow_density[4] = m5;
			snow_density[5] = m6;
			snow_density[6] = m7;
			snow_density[7] = m8;
			snow_density[8] = m9;
			snow_density[9] = m10;
			snow_density[10] = m11;
			snow_density[11] = m12;
		}
		
		public void onRead(String swCloudIn) throws Exception {
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swCloudIn, getClass().getClassLoader());
			int lineno=0;
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					if(values.length < 12)
						f.LogError(LogFileIn.LogMode.ERROR, "swCloud onRead : Line "+String.valueOf(lineno+1)+": Not enough values.");
					for (int j=0; j<12; j++) {
						try {
							switch (lineno) {
							case 0:
								cloudcov[j] = Double.parseDouble(values[j]);
								break;
							case 1:
								windspeed[j] = Double.parseDouble(values[j]);
								break;
							case 2:
								r_humidity[j] = Double.parseDouble(values[j]);
								break;
							case 3:
								transmission[j] = Double.parseDouble(values[j]);
								break;
							case 4:
								snow_density[j] = Double.parseDouble(values[j]);
							default:
								break;
							}
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "swCloud onRead : Line:"+String.valueOf(lineno)+" Could not convert string to number." + e.getMessage());
						}
					}
					lineno++;
				}
			}
		}
		
		protected void onWrite(String swCloudIn) throws Exception {
			Path cloudin = Paths.get(swCloudIn);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(cloudin, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out = "";
			out+=String.format("#%-6s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s%-7s\n", "Jan", "Feb","Mar","Apr","May","June","July","Aug","Sept","Oct","Nov","Dec");
			out+=String.format("%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%s\n", cloudcov[0],cloudcov[1],cloudcov[2],cloudcov[3],cloudcov[4],cloudcov[5],cloudcov[6],cloudcov[7],cloudcov[8],cloudcov[9],cloudcov[10],cloudcov[11],SW_SKY.comments[0]);
			out+=String.format("%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%s\n", windspeed[0],windspeed[1],windspeed[2],windspeed[3],windspeed[4],windspeed[5],windspeed[6],windspeed[7],windspeed[8],windspeed[9],windspeed[10],windspeed[11],SW_SKY.comments[1]);
			out+=String.format("%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%s\n", r_humidity[0],r_humidity[1],r_humidity[2],r_humidity[3],r_humidity[4],r_humidity[5],r_humidity[6],r_humidity[7],r_humidity[8],r_humidity[9],r_humidity[10],r_humidity[11],SW_SKY.comments[2]);
			out+=String.format("%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%s\n", transmission[0],transmission[1],transmission[2],transmission[3],transmission[4],transmission[5],transmission[6],transmission[7],transmission[8],transmission[9],transmission[10],transmission[11],SW_SKY.comments[3]);
			out+=String.format("%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%-7.2f%s\n", snow_density[0],snow_density[1],snow_density[2],snow_density[3],snow_density[4],snow_density[5],snow_density[6],snow_density[7],snow_density[8],snow_density[9],snow_density[10],snow_density[11],SW_SKY.comments[4]);
			return out;
		}
	}
	public static class MarkovIn {
		public Probability probability;
		public Covariance covariance;
		private LogFileIn log;
		
		public MarkovIn(LogFileIn log) {
			this.log = log;
			probability = new Probability(this.log);
			covariance  = new Covariance(this.log);
		}
		
		public void onClear() {
			probability.onClear();
			covariance.onClear();
		}
		
		public void onRead(String swMarkovProbabilityIn, String swMarkovCovarianceIn) throws Exception {
			Path MarkovProbabilityIn = Paths.get(swMarkovProbabilityIn);
			Path MarkovCovarianceIn = Paths.get(swMarkovCovarianceIn);
			if(Files.exists(MarkovCovarianceIn) || swMarkovCovarianceIn.startsWith("resource:")) {
				covariance.onRead(swMarkovCovarianceIn);
			}
			if(Files.exists(MarkovProbabilityIn) || swMarkovProbabilityIn.startsWith("resource:")) {
				probability.onRead(swMarkovProbabilityIn);
			}
		}
		
		public void onWrite(String swMarkovProbabilityIn, String swMarkovCovarianceIn) throws Exception {
			probability.onWrite(swMarkovProbabilityIn);
			covariance.onWrite(swMarkovCovarianceIn);
		}
		
		public String toString() {
			return probability.toString() + "\n" + covariance.toString();
		}
		
		//Soilwat Explorer needs log to create temp markov. This is a short cut.
		public LogFileIn getLog() {
			return log;
		}
	}
	
	public LogFileIn log = new LogFileIn();
	public FILES_INPUT_DATA filesIn = new FILES_INPUT_DATA(log);
	public MODEL_INPUT_DATA yearsIn = new MODEL_INPUT_DATA(log);
	public SWC_INPUT_DATA swcSetupIn = new SWC_INPUT_DATA(log);
	public WEATHER_INPUT_DATA weatherSetupIn = new WEATHER_INPUT_DATA(log);
	public SoilsIn soilsIn = new SoilsIn(log);
	public SiteIn siteIn = new SiteIn(log);
	public ProdIn prodIn = new ProdIn(log);
	public OutputIn outputSetupIn = new OutputIn(log);
	public EstabIn estabIn = new EstabIn(log);
	public CloudIn cloudIn = new CloudIn(log);
	public SW_WEATHER_HISTORY weatherHist = new SW_WEATHER_HISTORY(log);
	public SW_SOILWAT_HISTORY swcHist = new SW_SOILWAT_HISTORY(log);
	public MarkovIn markovIn = new MarkovIn(log);
	
	public InputData() {
		
	}
	
	public void onClear() {
		filesIn.onClear();
		yearsIn.onClear();
		swcSetupIn.onClear();
		weatherSetupIn.onClear();
		soilsIn.onClear();
		siteIn.onClear();
		prodIn.onClear();
		outputSetupIn.onClear();
		estabIn.onClear();
		cloudIn.onClear();
		weatherHist.onClear();
		swcHist.onClear();
		markovIn.onClear();
	}
	
	public void onRead(String swFiles) throws Exception {
		filesIn.onRead(swFiles);
		yearsIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.YearsIn).toString());
		swcSetupIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.SWCSetupIn).toString());
		weatherSetupIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.WeatherSetupIn).toString());
		soilsIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.SoilsIn).toString());
		siteIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.SiteParametersIn).toString());
		prodIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.PlantProductivityIn).toString());
		outputSetupIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.OutputSetupIn).toString());
		estabIn.onRead(filesIn.ProjectDirectory, filesIn.EstablishmentIn);
		cloudIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.CloudIn).toString());
		Path WeatherHistoryFolder = Paths.get(filesIn.ProjectDirectory, Paths.get(filesIn.WeatherPathAndPrefix).getParent().toString());
		weatherHist.onRead(WeatherHistoryFolder, Paths.get(filesIn.WeatherPathAndPrefix).getFileName().toString(), 0, 10000, weatherSetupIn.use_markov);
		swcHist.onRead(WeatherHistoryFolder, swcSetupIn.filePrefix, 0, 10000);
		markovIn.onRead(Paths.get(filesIn.ProjectDirectory, filesIn.MarkovProbabilityIn).toString(), Paths.get(filesIn.ProjectDirectory, filesIn.MarkovCovarianceIn).toString());
	}
	
	public void onWrite(String ProjectDirectory) throws Exception {
		filesIn.ProjectDirectory = ProjectDirectory;
		filesIn.onCreateDirectoriesAndFiles();
		filesIn.onWrite(Paths.get(ProjectDirectory, filesIn.FilesIn).toString());
		yearsIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.YearsIn).toString());
		swcSetupIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.SWCSetupIn).toString());
		weatherSetupIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.WeatherSetupIn).toString());
		soilsIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.SoilsIn).toString());
		siteIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.SiteParametersIn).toString());
		prodIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.PlantProductivityIn).toString());
		outputSetupIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.OutputSetupIn).toString());
		estabIn.onWrite(filesIn.ProjectDirectory, filesIn.EstablishmentIn);
		cloudIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.CloudIn).toString());
		Path WeatherHistoryFolder = Paths.get(filesIn.ProjectDirectory, Paths.get(filesIn.WeatherPathAndPrefix).getParent().toString());
		weatherHist.onWrite(WeatherHistoryFolder, Paths.get(filesIn.WeatherPathAndPrefix).getFileName().toString());
		swcHist.onWrite(WeatherHistoryFolder, swcSetupIn.filePrefix);
		markovIn.onWrite(Paths.get(filesIn.ProjectDirectory, filesIn.MarkovProbabilityIn).toString(), Paths.get(filesIn.ProjectDirectory, filesIn.MarkovCovarianceIn).toString());
	}
	
	public void onSetDefaults() throws Exception {
		//System.out.println(SW_FILES.readFile("resource:soilwat/data/files_v30.in", getClass().getClassLoader()));
		this.onRead("resource:soilwat/data/files_v30.in");
	}
	
	public String toString() {
		String out = "";
		out+="####################\n##########Files Input\n####################\n";
		out+=filesIn.toString();
		out+="\n####################\n##########Model Input\n####################\n";
		out+=yearsIn.toString();
		out+="\n####################\n##########SWC Setup Input\n####################\n";
		out+=swcSetupIn.toString();
		out+="\n####################\n##########Weather Setup Input\n####################\n";
		out+=weatherSetupIn.toString();
		out+="\n####################\n##########Soils Input\n####################\n";
		out+=soilsIn.toString();
		out+="\n####################\n##########Site Input\n####################\n";
		out+=siteIn.toString();
		out+="\n####################\n##########Prod Input\n####################\n";
		out+=prodIn.toString();
		out+="\n####################\n##########Output Setup Input\n####################\n";
		out+=outputSetupIn.toString();
		out+="\n####################\n##########Estab Input\n####################\n";
		out+=estabIn.toString();
		out+="\n####################\n##########Cloud Input\n####################\n";
		out+=cloudIn.toString();
		out+="\n####################\n##########Weather History Input\n####################\n";
		out+=weatherHist.toString();
		out+="\n####################\n##########SWC History Input\n####################\n";
		out+=swcHist.toString();
		out+="\n####################\n##########Markov Input\n####################\n";
		out+=markovIn.toString();
		return out;
	}
}
