package data;

import input.LogFileIn;
import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import times.Times;
import times.Times.Months;

public class SW_VEGPROD {
	public class VegetationComposition {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public double bareGround;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=this.bareGround=0;
		}
	}
	public class Albedo {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public double bareGround;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=this.bareGround=0;
		}
	}
	public class CoverPercent {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=0;
		}
	}
	public class CanopyHeight {
		private class Params {
			public double xinflec;
			public double yinflec;
			public double range;
			public double slope;
			public double canopyHeight;
			public void onClear() {
				this.xinflec=this.yinflec=this.range=this.slope=this.canopyHeight=0;
			}
		}
		public Params grasses;
		public Params shrubs;
		public Params trees;
		public Params forbs;
		public CanopyHeight() {
			this.grasses = new Params();
			this.shrubs = new Params();
			this.trees = new Params();
			this.forbs = new Params();
		}
		public void onClear() {
			this.grasses.onClear();
			this.shrubs.onClear();
			this.trees.onClear();
			this.forbs.onClear();
		}
	}
	public class VegetationInterceptionParameters {
		public class Params {
			public double a;
			public double b;
			public double c;
			public double d;
			public void onClear() {
				this.a=this.b=this.c=this.d=0;
			}
		}
		public Params grasses;
		public Params shrubs;
		public Params trees;
		public Params forbs;
		public VegetationInterceptionParameters() {
			this.grasses = new Params();
			this.shrubs = new Params();
			this.trees = new Params();
			this.forbs = new Params();
		}
		public void onClear() {
			this.grasses.onClear();
			this.shrubs.onClear();
			this.trees.onClear();
			this.forbs.onClear();
		}
	}
	public class LitterInterceptionParameters {
		private class Params {
			public double a;
			public double b;
			public double c;
			public double d;
			public void onClear() {
				this.a=this.b=this.c=this.d=0;
			}
		}
		public Params grasses;
		public Params shrubs;
		public Params trees;
		public Params forbs;
		public LitterInterceptionParameters() {
			this.grasses = new Params();
			this.shrubs = new Params();
			this.trees = new Params();
			this.forbs = new Params();
		}
		public void onClear() {
			this.grasses.onClear();
			this.shrubs.onClear();
			this.trees.onClear();
			this.forbs.onClear();
		}
	}
	public class EsTpartitioning {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=0;
		}
	}
	public class EsParamLimit {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=0;
		}
	}
	public class Shade {
		public class Params {
			public double shadeScale;
			public double shadeMaximalDeadBiomass;
			public double xinflec;
			public double yinflec;
			public double range;
			public double slope;
			public void onClear() {
				this.shadeScale=this.shadeMaximalDeadBiomass=this.xinflec=this.yinflec=this.range=this.slope=0;
			}
		}
		public Params grasses;
		public Params shrubs;
		public Params trees;
		public Params forbs;
		public Shade() {
			this.grasses = new Params();
			this.shrubs = new Params();
			this.trees = new Params();
			this.forbs = new Params();
		}
		public void onClear() {
			this.grasses.onClear();
			this.shrubs.onClear();
			this.trees.onClear();
			this.forbs.onClear();
		}
	}
	public class HydraulicRedistribution {
		public class Params {
			public boolean flag;
			public double maxCondRoot;
			public double swp50;
			public double shapeCond;
			public void onClear() {
				this.flag=false;
				this.maxCondRoot=this.swp50=this.shapeCond=0;
			}
		}
		public Params grasses;
		public Params shrubs;
		public Params trees;
		public Params forbs;
		public HydraulicRedistribution() {
			this.grasses = new Params();
			this.shrubs = new Params();
			this.trees = new Params();
			this.forbs = new Params();
		}
		public void onClear() {
			this.grasses.onClear();
			this.shrubs.onClear();
			this.trees.onClear();
			this.forbs.onClear();
		}
	}
	public class CriticalSWP {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=0;
		}
	}
	public class MonthlyProductionValues {
		private class Params {
			public double[] litter = new double[Times.MAX_MONTHS];
			public double[] biomass = new double[Times.MAX_MONTHS];
			public double[] percLive = new double[Times.MAX_MONTHS];
			public double[] lai_conv = new double[Times.MAX_MONTHS];
			public void onClear() {
				for(int i=0; i<Times.MAX_MONTHS;i++) {
					this.litter[i]=this.biomass[i]=this.percLive[i]=this.lai_conv[i]=0;
				}
			}
			public String getString(int month) {
				return String.valueOf(this.litter[month])+"\t"+String.valueOf(this.biomass[month])+"\t"+String.valueOf(this.percLive[month])+"\t"+String.valueOf(this.lai_conv[month]);
			}
		}
		public Params grasses;
		public Params shrubs;
		public Params trees;
		public Params forbs;
		public MonthlyProductionValues() {
			this.grasses = new Params();
			this.shrubs = new Params();
			this.trees = new Params();
			this.forbs = new Params();
		}
		public void onClear() {
			this.grasses.onClear();
			this.shrubs.onClear();
			this.trees.onClear();
			this.forbs.onClear();
		}
	}
	public class DailyVegProd {
		public class Params {
			public double[] litter_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of monthly litter values (g/m**2)    */
			public double[] biomass_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of monthly aboveground biomass (g/m**2) */
			public double[] biolive_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of biomass * pct_live               */
			public double[] biodead_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of biomass - biolive                */
			public double[] pct_live_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of monthly live biomass in percent   */
			public double[] lai_conv_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of monthly amount of biomass needed to produce lai=1 (g/m**2) */
			public double[] lai_live_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of lai of live biomass               */
			public double[] total_agb_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of sum of aboveground biomass & litter */
			public double[] veg_height_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of monthly height of vegetation (cm)   */
			public double[] pct_cover_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of veg cover for today; function of monthly biomass */
			public double[] vegcov_daily = new double[Times.MAX_DAYS+1]; /* daily interpolation of veg cover for today; function of monthly biomass */
			public void onClear() {
				for(int i=0; i<Times.MAX_DAYS+1; i++) {
					litter_daily[i]=biomass_daily[i]=biolive_daily[i]=biodead_daily[i]=pct_live_daily[i]=lai_conv_daily[i]=lai_live_daily[i]=0;
					total_agb_daily[i]=veg_height_daily[i]=pct_cover_daily[i]=vegcov_daily[i]=0;
				}
			}
		}
		public Params grass;
		public Params shrub;
		public Params tree;
		public Params forb;
		public DailyVegProd() {
			this.grass = new Params();
			this.shrub = new Params();
			this.tree = new Params();
			this.forb = new Params();
		}
		public void onClear() {
			this.grass.onClear();
			this.shrub.onClear();
			this.tree.onClear();
			this.forb.onClear();
		}
	}

	private VegetationComposition vegComp;
	private Albedo albedo;
	private CoverPercent cover;
	private CanopyHeight canopy;
	private VegetationInterceptionParameters vegInterception;
	private LitterInterceptionParameters litterInterception;
	private EsTpartitioning estPartitioning;
	private EsParamLimit esLimit;
	private Shade shade;
	private HydraulicRedistribution hydraulicRedistribution;
	private CriticalSWP criticalSWP;
	private MonthlyProductionValues monthlyProd;
	private DailyVegProd daily;
	private int nFileItemsRead;
	private boolean data;
	
	public SW_VEGPROD() {
		this.vegComp = new VegetationComposition();
		this.albedo = new Albedo();
		this.cover = new CoverPercent();
		this.canopy = new CanopyHeight();
		this.vegInterception = new VegetationInterceptionParameters();
		this.litterInterception = new LitterInterceptionParameters();
		this.estPartitioning = new EsTpartitioning();
		this.esLimit = new EsParamLimit();
		this.shade = new Shade();
		this.hydraulicRedistribution = new HydraulicRedistribution();
		this.criticalSWP = new CriticalSWP();
		this.monthlyProd = new MonthlyProductionValues();
		this.daily = new DailyVegProd();
		this.nFileItemsRead = 0;
		this.data = false;
	}
	
	public void setLitter(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grasses.litter[month] = grasses;
		this.monthlyProd.shrubs.litter[month] = shrubs;
		this.monthlyProd.trees.litter[month] = trees;
		this.monthlyProd.forbs.litter[month] = forbs;
	}
	public void setBiomass(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grasses.biomass[month] = grasses;
		this.monthlyProd.shrubs.biomass[month] = shrubs;
		this.monthlyProd.trees.biomass[month] = trees;
		this.monthlyProd.forbs.biomass[month] = forbs;
	}
	public void setPercLive(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grasses.percLive[month] = grasses;
		this.monthlyProd.shrubs.percLive[month] = shrubs;
		this.monthlyProd.trees.percLive[month] = trees;
		this.monthlyProd.forbs.percLive[month] = forbs;
	}
	public void setLAI_conv(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grasses.lai_conv[month] = grasses;
		this.monthlyProd.shrubs.lai_conv[month] = shrubs;
		this.monthlyProd.trees.lai_conv[month] = trees;
		this.monthlyProd.forbs.lai_conv[month] = forbs;
	}
	
	public void onClear() {
		if(this.data) {
			this.vegComp.onClear();
			this.albedo.onClear();
			this.cover.onClear();
			this.canopy.onClear();
			this.vegInterception.onClear();
			this.litterInterception.onClear();
			this.estPartitioning.onClear();
			this.esLimit.onClear();
			this.shade.onClear();
			this.hydraulicRedistribution.onClear();
			this.criticalSWP.onClear();
			this.monthlyProd.onClear();
			this.data = false;
		}
	}
	
	public boolean onVerify() {
		if(this.data)
			return true;
		else 
			return false;
	}
	
	public void onRead(Path prodIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(prodIn, StandardCharsets.UTF_8);
		int month=0;
		this.nFileItemsRead=0;
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				switch (this.nFileItemsRead) {
				case 0:
					if(values.length != 5)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : VegetationComposition : Expected 5 Values read "+String.valueOf(values.length));
					try {
						this.vegComp.grasses = Double.parseDouble(values[0]);
						this.vegComp.shrubs = Double.parseDouble(values[1]);
						this.vegComp.trees = Double.parseDouble(values[2]);
						this.vegComp.forbs = Double.parseDouble(values[3]);
						this.vegComp.bareGround = Double.parseDouble(values[4]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : VegetationComposition : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 1:
					if(values.length != 5)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Albedo : Expected 5 Values read "+String.valueOf(values.length));
					try {
						this.albedo.grasses = Double.parseDouble(values[0]);
						this.albedo.shrubs = Double.parseDouble(values[1]);
						this.albedo.trees = Double.parseDouble(values[2]);
						this.albedo.forbs = Double.parseDouble(values[3]);
						this.albedo.bareGround = Double.parseDouble(values[4]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Albedo : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 2:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Cover Percent : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.cover.grasses = Double.parseDouble(values[0]);
						this.cover.shrubs = Double.parseDouble(values[1]);
						this.cover.trees = Double.parseDouble(values[2]);
						this.cover.forbs = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Cover Percent : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 3:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - xinflec : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grasses.xinflec = Double.parseDouble(values[0]);
						this.canopy.shrubs.xinflec = Double.parseDouble(values[1]);
						this.canopy.trees.xinflec = Double.parseDouble(values[2]);
						this.canopy.forbs.xinflec = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - xinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 4:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - yinflec : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grasses.yinflec = Double.parseDouble(values[0]);
						this.canopy.shrubs.yinflec = Double.parseDouble(values[1]);
						this.canopy.trees.yinflec = Double.parseDouble(values[2]);
						this.canopy.forbs.yinflec = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - yinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 5:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - range : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grasses.range = Double.parseDouble(values[0]);
						this.canopy.shrubs.range = Double.parseDouble(values[1]);
						this.canopy.trees.range = Double.parseDouble(values[2]);
						this.canopy.forbs.range = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - range : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 6:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - slope : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grasses.slope = Double.parseDouble(values[0]);
						this.canopy.shrubs.slope = Double.parseDouble(values[1]);
						this.canopy.trees.slope = Double.parseDouble(values[2]);
						this.canopy.forbs.slope = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 7:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - height: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grasses.canopyHeight = Double.parseDouble(values[0]);
						this.canopy.shrubs.canopyHeight = Double.parseDouble(values[1]);
						this.canopy.trees.canopyHeight = Double.parseDouble(values[2]);
						this.canopy.forbs.canopyHeight = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - height : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 8:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - a: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grasses.a = Double.parseDouble(values[0]);
						this.vegInterception.shrubs.a = Double.parseDouble(values[1]);
						this.vegInterception.trees.a = Double.parseDouble(values[2]);
						this.vegInterception.forbs.a = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - a: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 9:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - b: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grasses.b = Double.parseDouble(values[0]);
						this.vegInterception.shrubs.b = Double.parseDouble(values[1]);
						this.vegInterception.trees.b = Double.parseDouble(values[2]);
						this.vegInterception.forbs.b = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - b: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 10:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - c: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grasses.c = Double.parseDouble(values[0]);
						this.vegInterception.shrubs.c = Double.parseDouble(values[1]);
						this.vegInterception.trees.c = Double.parseDouble(values[2]);
						this.vegInterception.forbs.c = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - c: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 11:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - d: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grasses.d = Double.parseDouble(values[0]);
						this.vegInterception.shrubs.d = Double.parseDouble(values[1]);
						this.vegInterception.trees.d = Double.parseDouble(values[2]);
						this.vegInterception.forbs.d = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - d: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 12:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - a: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grasses.a = Double.parseDouble(values[0]);
						this.litterInterception.shrubs.a = Double.parseDouble(values[1]);
						this.litterInterception.trees.a = Double.parseDouble(values[2]);
						this.litterInterception.forbs.a = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - a: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 13:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - b: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grasses.b = Double.parseDouble(values[0]);
						this.litterInterception.shrubs.b = Double.parseDouble(values[1]);
						this.litterInterception.trees.b = Double.parseDouble(values[2]);
						this.litterInterception.forbs.b = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - b: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 14:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - c: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grasses.c = Double.parseDouble(values[0]);
						this.litterInterception.shrubs.c = Double.parseDouble(values[1]);
						this.litterInterception.trees.c = Double.parseDouble(values[2]);
						this.litterInterception.forbs.c = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - c: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 15:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - d: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grasses.d = Double.parseDouble(values[0]);
						this.litterInterception.shrubs.d = Double.parseDouble(values[1]);
						this.litterInterception.trees.d = Double.parseDouble(values[2]);
						this.litterInterception.forbs.d = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - d: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 16:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : estPartitioning: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.estPartitioning.grasses = Double.parseDouble(values[0]);
						this.estPartitioning.shrubs = Double.parseDouble(values[1]);
						this.estPartitioning.trees = Double.parseDouble(values[2]);
						this.estPartitioning.forbs = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : estPartitioning: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 17:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : esLimit: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.esLimit.grasses = Double.parseDouble(values[0]);
						this.esLimit.shrubs = Double.parseDouble(values[1]);
						this.esLimit.trees = Double.parseDouble(values[2]);
						this.esLimit.forbs = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : esLimit: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 18:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeScale: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.shade.grasses.shadeScale = Double.parseDouble(values[0]);
						this.shade.shrubs.shadeScale = Double.parseDouble(values[1]);
						this.shade.trees.shadeScale = Double.parseDouble(values[2]);
						this.shade.forbs.shadeScale = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeScale: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 19:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeMaximalDeadBiomass: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.shade.grasses.shadeMaximalDeadBiomass = Double.parseDouble(values[0]);
						this.shade.shrubs.shadeMaximalDeadBiomass = Double.parseDouble(values[1]);
						this.shade.trees.shadeMaximalDeadBiomass = Double.parseDouble(values[2]);
						this.shade.forbs.shadeMaximalDeadBiomass = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - shadeMaximalDeadBiomass: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 20:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - xinflec: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.shade.grasses.xinflec = Double.parseDouble(values[0]);
						this.shade.shrubs.xinflec = Double.parseDouble(values[1]);
						this.shade.trees.xinflec = Double.parseDouble(values[2]);
						this.shade.forbs.xinflec = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - xinflec: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 21:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - yinflec: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.shade.grasses.yinflec = Double.parseDouble(values[0]);
						this.shade.shrubs.yinflec = Double.parseDouble(values[1]);
						this.shade.trees.yinflec = Double.parseDouble(values[2]);
						this.shade.forbs.yinflec = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - yinflec: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 22:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - range: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.shade.grasses.range = Double.parseDouble(values[0]);
						this.shade.shrubs.range = Double.parseDouble(values[1]);
						this.shade.trees.range = Double.parseDouble(values[2]);
						this.shade.forbs.range = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - range: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 23:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - slope: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.shade.grasses.slope = Double.parseDouble(values[0]);
						this.shade.shrubs.slope = Double.parseDouble(values[1]);
						this.shade.trees.slope = Double.parseDouble(values[2]);
						this.shade.forbs.slope = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Shade - slope: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 24:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - flag: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.hydraulicRedistribution.grasses.flag = Integer.parseInt(values[0])>0 ? true : false;
						this.hydraulicRedistribution.shrubs.flag = Integer.parseInt(values[1])>0 ? true : false;
						this.hydraulicRedistribution.trees.flag = Integer.parseInt(values[2])>0 ? true : false;
						this.hydraulicRedistribution.forbs.flag = Integer.parseInt(values[3])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - flag: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 25:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - maxCondroot: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.hydraulicRedistribution.grasses.maxCondRoot = Double.parseDouble(values[0]);
						this.hydraulicRedistribution.shrubs.maxCondRoot = Double.parseDouble(values[1]);
						this.hydraulicRedistribution.trees.maxCondRoot = Double.parseDouble(values[2]);
						this.hydraulicRedistribution.forbs.maxCondRoot = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - maxCondroot: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 26:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - swp50: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.hydraulicRedistribution.grasses.swp50 = Double.parseDouble(values[0]);
						this.hydraulicRedistribution.shrubs.swp50 = Double.parseDouble(values[1]);
						this.hydraulicRedistribution.trees.swp50 = Double.parseDouble(values[2]);
						this.hydraulicRedistribution.forbs.swp50 = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - swp50: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 27:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - shapeCond: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.hydraulicRedistribution.grasses.shapeCond = Double.parseDouble(values[0]);
						this.hydraulicRedistribution.shrubs.shapeCond = Double.parseDouble(values[1]);
						this.hydraulicRedistribution.trees.shapeCond = Double.parseDouble(values[2]);
						this.hydraulicRedistribution.forbs.shapeCond = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - shapeCond: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 28:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Critical soil water potential (MPa): Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.criticalSWP.grasses = Double.parseDouble(values[0]);
						this.criticalSWP.shrubs = Double.parseDouble(values[1]);
						this.criticalSWP.trees = Double.parseDouble(values[2]);
						this.criticalSWP.forbs = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Critical soil water potential (MPa): Could not convert string to double. " + e.getMessage());
					}
					break;
				default:
					if(this.nFileItemsRead > 28 && this.nFileItemsRead <= 40) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-29;
							this.monthlyProd.grasses.litter[month] = Double.parseDouble(values[0]);
							this.monthlyProd.grasses.biomass[month] = Double.parseDouble(values[1]);
							this.monthlyProd.grasses.percLive[month] = Double.parseDouble(values[2]);
							this.monthlyProd.grasses.lai_conv[month] = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else if(this.nFileItemsRead > 40 && this.nFileItemsRead <= 52) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-41;
							this.monthlyProd.shrubs.litter[month] = Double.parseDouble(values[0]);
							this.monthlyProd.shrubs.biomass[month] = Double.parseDouble(values[1]);
							this.monthlyProd.shrubs.percLive[month] = Double.parseDouble(values[2]);
							this.monthlyProd.shrubs.lai_conv[month] = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else if(this.nFileItemsRead > 52 && this.nFileItemsRead <= 64) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-53;
							this.monthlyProd.trees.litter[month] = Double.parseDouble(values[0]);
							this.monthlyProd.trees.biomass[month] = Double.parseDouble(values[1]);
							this.monthlyProd.trees.percLive[month] = Double.parseDouble(values[2]);
							this.monthlyProd.trees.lai_conv[month] = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else if(this.nFileItemsRead > 64 && this.nFileItemsRead <= 76) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-65;
							this.monthlyProd.forbs.litter[month] = Double.parseDouble(values[0]);
							this.monthlyProd.forbs.biomass[month] = Double.parseDouble(values[1]);
							this.monthlyProd.forbs.percLive[month] = Double.parseDouble(values[2]);
							this.monthlyProd.forbs.lai_conv[month] = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else {
						f.LogError(LogMode.ERROR, "Unknown line read. "+line);
					}
					break;
				}
				this.nFileItemsRead++;
			}
		}
		this.data = true;
	}

	public void onWrite(Path prodIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Plant production data file for SOILWAT");
			lines.add("# Location:  ");
			lines.add("");
			lines.add("# ---- Composition of vegetation type components (0-1; must add up to 1)");
			lines.add("# Grasses	Shrubs		Trees		Forbs		Bare Ground");
			lines.add(String.valueOf(this.vegComp.grasses)+"\t"+String.valueOf(this.vegComp.shrubs)+"\t"+String.valueOf(this.vegComp.trees)+"\t"+String.valueOf(this.vegComp.forbs)+"\t"+String.valueOf(this.vegComp.bareGround));
			lines.add("");
			lines.add("");
			lines.add("# ---- Albedo");
			lines.add("# Grasses	Shrubs		Trees		Forbs		Bare Ground");
			lines.add(String.valueOf(this.albedo.grasses)+"\t"+String.valueOf(this.albedo.shrubs)+"\t"+String.valueOf(this.albedo.trees)+"\t"+String.valueOf(this.albedo.forbs)+"\t"+String.valueOf(this.albedo.bareGround)+
					"\t# albedo:	(Houldcroft et al. 2009) MODIS snowfree 'grassland', 'open shrub', ‘evergreen needle forest’ with MODIS albedo aggregated over pure IGBP cells where NDVI is greater than the 98th percentile NDVI");
			lines.add("");
			lines.add("");
			lines.add("# ---- % Cover: divide standing LAI by this to get % cover");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.cover.grasses)+"\t"+String.valueOf(this.cover.shrubs)+"\t"+String.valueOf(this.cover.trees)+"\t"+String.valueOf(this.cover.forbs));
			lines.add("");
			lines.add("");
			lines.add("# -- Canopy height (cm) parameters either constant through season or as tanfunc with respect to biomass (g/m^2)");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.canopy.grasses.xinflec)+"\t"+String.valueOf(this.canopy.shrubs.xinflec)+"\t"+String.valueOf(this.canopy.trees.xinflec)+"\t"+String.valueOf(this.canopy.forbs.xinflec)+"\t# xinflec");
			lines.add(String.valueOf(this.canopy.grasses.yinflec)+"\t"+String.valueOf(this.canopy.shrubs.yinflec)+"\t"+String.valueOf(this.canopy.trees.yinflec)+"\t"+String.valueOf(this.canopy.forbs.yinflec)+"\t# yinflec");
			lines.add(String.valueOf(this.canopy.grasses.range)+"\t"+String.valueOf(this.canopy.shrubs.range)+"\t"+String.valueOf(this.canopy.trees.range)+"\t"+String.valueOf(this.canopy.forbs.range)+"\t# range");
			lines.add(String.valueOf(this.canopy.grasses.slope)+"\t"+String.valueOf(this.canopy.shrubs.slope)+"\t"+String.valueOf(this.canopy.trees.slope)+"\t"+String.valueOf(this.canopy.forbs.slope)+"\t# slope");
			lines.add(String.valueOf(this.canopy.grasses.canopyHeight)+"\t"+String.valueOf(this.canopy.shrubs.canopyHeight)+"\t"+String.valueOf(this.canopy.trees.canopyHeight)+"\t"+String.valueOf(this.canopy.forbs.canopyHeight)+"\t# if > 0 then constant canopy height (cm)");
			lines.add("");
			lines.add("");
			lines.add("# --- Vegetation interception parameters for equation: intercepted rain = (a + b*veg) + (c+d*veg) * ppt; Grasses+Shrubs: veg=vegcov, Trees: veg=LAI");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.vegInterception.grasses.a)+"\t"+String.valueOf(this.vegInterception.shrubs.a)+"\t"+String.valueOf(this.vegInterception.trees.a)+"\t"+String.valueOf(this.vegInterception.forbs.a)+"\t# a");
			lines.add(String.valueOf(this.vegInterception.grasses.b)+"\t"+String.valueOf(this.vegInterception.shrubs.b)+"\t"+String.valueOf(this.vegInterception.trees.b)+"\t"+String.valueOf(this.vegInterception.forbs.b)+"\t# b");
			lines.add(String.valueOf(this.vegInterception.grasses.c)+"\t"+String.valueOf(this.vegInterception.shrubs.c)+"\t"+String.valueOf(this.vegInterception.trees.c)+"\t"+String.valueOf(this.vegInterception.forbs.c)+"\t# c");
			lines.add(String.valueOf(this.vegInterception.grasses.d)+"\t"+String.valueOf(this.vegInterception.shrubs.d)+"\t"+String.valueOf(this.vegInterception.trees.d)+"\t"+String.valueOf(this.vegInterception.forbs.d)+"\t# d");
			lines.add("");
			lines.add("");
			lines.add("# --- Litter interception parameters for equation: intercepted rain = (a + b*litter) + (c+d*litter) * ppt");
			lines.add("# Grass-Litter	Shrub-Litter	Tree-Litter	Forbs-Litter");
			lines.add(String.valueOf(this.litterInterception.grasses.a)+"\t"+String.valueOf(this.litterInterception.shrubs.a)+"\t"+String.valueOf(this.litterInterception.trees.a)+"\t"+String.valueOf(this.litterInterception.forbs.a)+"\t# a");
			lines.add(String.valueOf(this.litterInterception.grasses.b)+"\t"+String.valueOf(this.litterInterception.shrubs.b)+"\t"+String.valueOf(this.litterInterception.trees.b)+"\t"+String.valueOf(this.litterInterception.forbs.b)+"\t# b");
			lines.add(String.valueOf(this.litterInterception.grasses.c)+"\t"+String.valueOf(this.litterInterception.shrubs.c)+"\t"+String.valueOf(this.litterInterception.trees.c)+"\t"+String.valueOf(this.litterInterception.forbs.c)+"\t# c");
			lines.add(String.valueOf(this.litterInterception.grasses.d)+"\t"+String.valueOf(this.litterInterception.shrubs.d)+"\t"+String.valueOf(this.litterInterception.trees.d)+"\t"+String.valueOf(this.litterInterception.forbs.d)+"\t# d");
			lines.add("");
			lines.add("");
			lines.add("# ---- Parameter for partitioning of bare-soil evaporation and transpiration as in Es = exp(-param*LAI)");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.estPartitioning.grasses)+"\t"+String.valueOf(this.estPartitioning.shrubs)+"\t"+String.valueOf(this.estPartitioning.trees)+"\t"+String.valueOf(this.estPartitioning.forbs)+
					"\t# Trees: According to a regression based on a review by Daikoku, K., S. Hattori, A. Deguchi, Y. Aoki, M. Miyashita, K. Matsumoto, J. Akiyama, S. Iida, T. Toba, Y. Fujita, and T. Ohta. 2008. Influence of evaporation from the forest floor on evapotranspiration from the dry canopy. Hydrological Processes 22:4083-4096.");
			lines.add("");
			lines.add("");
			lines.add("# ---- Parameter for scaling and limiting bare soil evaporation rate: if totagb (g/m2) > param then no bare-soil evaporation");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.esLimit.grasses)+"\t"+String.valueOf(this.esLimit.shrubs)+"\t"+String.valueOf(this.esLimit.trees)+"\t"+String.valueOf(this.esLimit.forbs)+"\t#");
			lines.add("");
			lines.add("");
			lines.add("# --- Shade effects on transpiration based on live and dead biomass ");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.shade.grasses.shadeScale)+"\t"+String.valueOf(this.shade.shrubs.shadeScale)+"\t"+String.valueOf(this.shade.trees.shadeScale)+"\t"+String.valueOf(this.shade.forbs.shadeScale)+"\t# shade scale");
			lines.add(String.valueOf(this.shade.grasses.shadeMaximalDeadBiomass)+"\t"+String.valueOf(this.shade.shrubs.shadeMaximalDeadBiomass)+"\t"+String.valueOf(this.shade.trees.shadeMaximalDeadBiomass)+"\t"+String.valueOf(this.shade.forbs.shadeMaximalDeadBiomass)+"\t# shade maximal dead biomass");
			lines.add(String.valueOf(this.shade.grasses.xinflec)+"\t"+String.valueOf(this.shade.shrubs.xinflec)+"\t"+String.valueOf(this.shade.trees.xinflec)+"\t"+String.valueOf(this.shade.forbs.xinflec)+"\t# tanfunc: xinflec");
			lines.add(String.valueOf(this.shade.grasses.yinflec)+"\t"+String.valueOf(this.shade.shrubs.yinflec)+"\t"+String.valueOf(this.shade.trees.yinflec)+"\t"+String.valueOf(this.shade.forbs.yinflec)+"\t# yinflec");
			lines.add(String.valueOf(this.shade.grasses.range)+"\t"+String.valueOf(this.shade.shrubs.range)+"\t"+String.valueOf(this.shade.trees.range)+"\t"+String.valueOf(this.shade.forbs.range)+"\t# range");
			lines.add(String.valueOf(this.shade.grasses.slope)+"\t"+String.valueOf(this.shade.shrubs.slope)+"\t"+String.valueOf(this.shade.trees.slope)+"\t"+String.valueOf(this.shade.forbs.slope)+"\t# slope");
			lines.add("");
			lines.add("");
			lines.add("# ---- Hydraulic redistribution: Ryel, Ryel R, Caldwell, Caldwell M, Yoder, Yoder C, Or, Or D, Leffler, Leffler A. 2002. Hydraulic redistribution in a stand of Artemisia tridentata: evaluation of benefits to transpiration assessed with a simulation model. Oecologia 130: 173-184.");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.hydraulicRedistribution.grasses.flag?1:0)+"\t"+String.valueOf(this.hydraulicRedistribution.shrubs.flag?1:0)+"\t"+String.valueOf(this.hydraulicRedistribution.trees.flag?1:0)+"\t"+String.valueOf(this.hydraulicRedistribution.forbs.flag?1:0)+"\t# flag to turn on/off (1/0) hydraulic redistribution");
			lines.add(String.valueOf(this.hydraulicRedistribution.grasses.maxCondRoot)+"\t"+String.valueOf(this.hydraulicRedistribution.shrubs.maxCondRoot)+"\t"+String.valueOf(this.hydraulicRedistribution.trees.maxCondRoot)+"\t"+String.valueOf(this.hydraulicRedistribution.forbs.maxCondRoot)+"\t# maxCondroot - maximum radial soil-root conductance of the entire active root system for water (cm/-bar/day) = 0.097 cm/MPa/h");
			lines.add(String.valueOf(this.hydraulicRedistribution.grasses.swp50)+"\t"+String.valueOf(this.hydraulicRedistribution.shrubs.swp50)+"\t"+String.valueOf(this.hydraulicRedistribution.trees.swp50)+"\t"+String.valueOf(this.hydraulicRedistribution.forbs.swp50)+"\t# swp50 - soil water potential (-bar) where conductance is reduced by 50% = -1. MPa");
			lines.add(String.valueOf(this.hydraulicRedistribution.grasses.shapeCond)+"\t"+String.valueOf(this.hydraulicRedistribution.shrubs.shapeCond)+"\t"+String.valueOf(this.hydraulicRedistribution.trees.shapeCond)+"\t"+String.valueOf(this.hydraulicRedistribution.forbs.shapeCond)+"\t# shapeCond - shaping parameter for the empirical relationship from van Genuchten to model relative soil-root conductance for water");
			lines.add("");
			lines.add("");
			lines.add("# ---- Critical soil water potential (MPa), i.e., when transpiration rates cannot sustained anymore, for instance, for many crop species -1.5 MPa is assumed and called wilting point");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.criticalSWP.grasses)+"\t"+String.valueOf(this.criticalSWP.shrubs)+"\t"+String.valueOf(this.criticalSWP.trees)+"\t"+String.valueOf(this.criticalSWP.forbs));
			lines.add("");
			lines.add("");
			lines.add("# Grasslands component:");
			lines.add("# -------------- Monthly production values ------------");
			lines.add("# Litter   - dead leafy material on the ground (g/m^2 ).");
			lines.add("# Biomass  - living and dead/woody aboveground standing biomass (g/m^2).");
			lines.add("# %Live    - proportion of Biomass that is actually living (0-1.0).");
			lines.add("# LAI_conv - monthly amount of biomass needed to produce LAI=1.0 (g/m^2).");
			lines.add("# There should be 12 rows, one for each month, starting with January.");
			lines.add("#");
			lines.add("#Litter\tBiomass\t%Live\tLAI_conv");
			lines.add(this.monthlyProd.grasses.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.grasses.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.grasses.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.grasses.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.grasses.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.grasses.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.grasses.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.grasses.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.grasses.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.grasses.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.grasses.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.grasses.getString(Months.Dec.ordinal())+"\t# December");
			lines.add("");
			lines.add("# Shrublands component:");
			lines.add("#Litter\tBiomass\t%Live\tLAI_conv");
			lines.add(this.monthlyProd.shrubs.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.shrubs.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.shrubs.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.shrubs.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.shrubs.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.shrubs.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.shrubs.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.shrubs.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.shrubs.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.shrubs.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.shrubs.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.shrubs.getString(Months.Dec.ordinal())+"\t# December");
			lines.add("");
			lines.add("# Forest component:");
			lines.add("#Litter\tBiomass\t%Live\tLAI_conv");
			lines.add(this.monthlyProd.trees.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.trees.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.trees.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.trees.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.trees.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.trees.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.trees.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.trees.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.trees.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.trees.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.trees.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.trees.getString(Months.Dec.ordinal())+"\t# December");
			lines.add("");
			lines.add("# FORB component:");
			lines.add("#Litter\tBiomass\t%Live\tLAI_conv");
			lines.add(this.monthlyProd.forbs.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.forbs.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.forbs.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.forbs.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.forbs.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.forbs.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.forbs.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.forbs.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.forbs.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.forbs.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.forbs.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.forbs.getString(Months.Dec.ordinal())+"\t# December");
			Files.write(prodIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "ProductionIn : onWrite : No data.");
		}
	}
	public VegetationComposition getVegetationComposition() {
		return this.vegComp;
	}
	public Albedo getAlbedo() {
		return this.albedo;
	}
	public CoverPercent getCoverPercent() {
		return this.cover;
	}
	public CanopyHeight getCanopyHeight() {
		return this.canopy;
	}
	public VegetationInterceptionParameters getVegetationInterceptionParameters() {
		return this.vegInterception;
	}
	public LitterInterceptionParameters getLitterInterceptionParameters() {
		return this.litterInterception;
	}
	public EsTpartitioning getEsTpartitioning() {
		return this.estPartitioning;
	}
	public EsParamLimit getEsParamLimit() {
		return this.esLimit;
	}
	public Shade getShade() {
		return this.shade;
	}
	public HydraulicRedistribution getHydraulicRedistribution() {
		return this.hydraulicRedistribution;
	}
	public CriticalSWP getCriticalSWP() {
		return this.criticalSWP;
	}
	public MonthlyProductionValues getMonthlyProductionValues() {
		return this.monthlyProd;
	}
	public DailyVegProd getDailyValues() {
		return this.daily;
	}
}
