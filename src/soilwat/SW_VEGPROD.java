package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.LogFileIn.LogMode;
import soilwat.Times.Months;

public class SW_VEGPROD {
	public static class VegetationComposition {
		public double grass;
		public double shrub;
		public double tree;
		public double forb;
		public double bareGround;
		public void onClear() {
			this.grass=this.shrub=this.tree=this.forb=this.bareGround=0;
		}
		public void onSet(double grassFrac, double shrubFrac, double treeFrac, double forbFrac, double baregroundFrac) {
			this.grass = grassFrac;
			this.tree = treeFrac;
			this.shrub = shrubFrac;
			this.forb = forbFrac;
			this.bareGround = baregroundFrac;
		}
	}
	public static class Albedo {
		public double grass;
		public double shrubs;
		public double trees;
		public double forbs;
		public double bareGround;
		public void onClear() {
			this.grass=this.shrubs=this.trees=this.forbs=this.bareGround=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb, double bareGround) {
			this.grass = grass;
			this.shrubs = shrub;
			this.trees = tree;
			this.forbs = forb;
			this.bareGround = bareGround;
		}
	}
	public static class CoverPercent {
		public double grasses;
		public double shrub;
		public double tree;
		public double forb;
		public void onClear() {
			this.grasses=this.shrub=this.tree=this.forb=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grasses = grass;
			this.shrub = shrub;
			this.tree = tree;
			this.forb = forb;
		}
	}
	public static class CanopyHeight {
		public class Params {
			public double xinflec;
			public double yinflec;
			public double range;
			public double slope;
			public double canopyHeight;
			public void onClear() {
				this.xinflec=this.yinflec=this.range=this.slope=this.canopyHeight=0;
			}
			public void onSet(double xinflec, double yinflec, double range, double slope, double canopyHeight) {
				this.xinflec = xinflec;
				this.yinflec = yinflec;
				this.range = range;
				this.slope = slope;
				this.canopyHeight = canopyHeight;
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
	public static class VegetationInterceptionParameters {
		public class Params {
			public double a;
			public double b;
			public double c;
			public double d;
			public void onClear() {
				this.a=this.b=this.c=this.d=0;
			}
			public void onSet(double a, double b, double c, double d) {
				this.a = a;
				this.b = b;
				this.c = c;
				this.d = d;
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
	public static class LitterInterceptionParameters {
		public class Params {
			public double a;
			public double b;
			public double c;
			public double d;
			public void onClear() {
				this.a=this.b=this.c=this.d=0;
			}
			public void onSet(double a, double b, double c, double d) {
				this.a = a;
				this.b = b;
				this.c = c;
				this.d = d;
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
	public static class EsTpartitioning {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grasses = grass;
			this.shrubs = shrub;
			this.trees = tree;
			this.forbs = forb;
		}
	}
	public static class EsParamLimit {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grasses = grass;
			this.shrubs = shrub;
			this.trees = tree;
			this.forbs = forb;
		}
	}
	public static class Shade {
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
			public void onSet(double shadeScale, double shadeMaximalDeadBiomass, double xinflec, double yinflec, double range, double slope) {
				this.shadeScale = shadeScale;
				this.shadeMaximalDeadBiomass = shadeMaximalDeadBiomass;
				this.xinflec = xinflec;
				this.yinflec = yinflec;
				this.range = range;
				this.slope = slope;
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
	public static class HydraulicRedistribution {
		public class Params {
			public boolean flag;
			public double maxCondRoot;
			public double swp50;
			public double shapeCond;
			public void onClear() {
				this.flag=false;
				this.maxCondRoot=this.swp50=this.shapeCond=0;
			}
			public void onSet(boolean flag, double maxCondRoot, double swp50, double shapeCond) {
				this.flag = flag;
				this.maxCondRoot = maxCondRoot;
				this.swp50 = swp50;
				this.shapeCond = shapeCond;
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
	public static class CriticalSWP {
		public double grasses;
		public double shrubs;
		public double trees;
		public double forbs;
		public void onClear() {
			this.grasses=this.shrubs=this.trees=this.forbs=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grasses = grass;
			this.shrubs = shrub;
			this.trees = tree;
			this.forbs = forb;
		}
	}
	public static class MonthlyProductionValues {
		public class Params {
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
			public void onSetMonth(int month, double litter, double biomass, double percLive, double lai_conv) {
				month--;
				this.litter[month]=litter;
				this.biomass[month]=biomass;
				this.percLive[month]=percLive;
				this.lai_conv[month]=lai_conv;
			}
			public void onSetLitter(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
				this.litter[0] = m1;
				this.litter[1] = m2;
				this.litter[2] = m3;
				this.litter[3] = m4;
				this.litter[4] = m5;
				this.litter[5] = m6;
				this.litter[6] = m7;
				this.litter[7] = m8;
				this.litter[8] = m9;
				this.litter[9] = m10;
				this.litter[10] = m11;
				this.litter[11] = m12;
			}
			public void onSetBiomass(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
				this.biomass[0] = m1;
				this.biomass[1] = m2;
				this.biomass[2] = m3;
				this.biomass[3] = m4;
				this.biomass[4] = m5;
				this.biomass[5] = m6;
				this.biomass[6] = m7;
				this.biomass[7] = m8;
				this.biomass[8] = m9;
				this.biomass[9] = m10;
				this.biomass[10] = m11;
				this.biomass[11] = m12;
			}
			public void onSetPercLive(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
				this.percLive[0] = m1;
				this.percLive[1] = m2;
				this.percLive[2] = m3;
				this.percLive[3] = m4;
				this.percLive[4] = m5;
				this.percLive[5] = m6;
				this.percLive[6] = m7;
				this.percLive[7] = m8;
				this.percLive[8] = m9;
				this.percLive[9] = m10;
				this.percLive[10] = m11;
				this.percLive[11] = m12;
			}
			public void onSetLai_conv(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12) {
				this.lai_conv[0] = m1;
				this.lai_conv[1] = m2;
				this.lai_conv[2] = m3;
				this.lai_conv[3] = m4;
				this.lai_conv[4] = m5;
				this.lai_conv[5] = m6;
				this.lai_conv[6] = m7;
				this.lai_conv[7] = m8;
				this.lai_conv[8] = m9;
				this.lai_conv[9] = m10;
				this.lai_conv[10] = m11;
				this.lai_conv[11] = m12;
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
	protected class DailyVegProd {
		protected class Params {
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
		protected Params grass;
		protected Params shrub;
		protected Params tree;
		protected Params forb;
		protected DailyVegProd() {
			this.grass = new Params();
			this.shrub = new Params();
			this.tree = new Params();
			this.forb = new Params();
		}
		protected void onClear() {
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
	private double lai_standing;
	private boolean EchoInits;
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
		this.lai_standing = 0;
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
			this.nFileItemsRead = 0;
			this.lai_standing = 0;
			this.data = false;
		}
	}
	
	public boolean onVerify() {
		if(this.data) {
			LogFileIn f = LogFileIn.getInstance();
			
			this.criticalSWP.grasses *= -10;
			this.criticalSWP.shrubs *= -10;
			this.criticalSWP.trees *= -10;
			this.criticalSWP.forbs *= -10;
			
			double fraction_sum = this.vegComp.grass + this.vegComp.shrub + this.vegComp.tree + this.vegComp.forb + this.vegComp.bareGround;
			if (!Defines.EQ(fraction_sum, 1.0)) {
				f.LogError(LogFileIn.LogMode.ERROR, String.format("Fractions of vegetation components were normalized, "+
						"sum of fractions (%5.4f) != 1.0.\nNew coefficients are:", fraction_sum));
				this.vegComp.grass /= fraction_sum;
				this.vegComp.shrub /= fraction_sum;
				this.vegComp.tree /= fraction_sum;
				this.vegComp.bareGround /= fraction_sum;
				this.vegComp.forb /= fraction_sum;
				f.LogError(LogFileIn.LogMode.ERROR, String.format("  Grassland fraction : %5.4f", this.vegComp.grass));
				f.LogError(LogFileIn.LogMode.ERROR, String.format("  Shrubland fraction : %5.4f", this.vegComp.shrub));
				f.LogError(LogFileIn.LogMode.ERROR, String.format("  Forest/tree fraction : %5.4f", this.vegComp.tree));
				f.LogError(LogFileIn.LogMode.ERROR, String.format("  FORB fraction : %5.4f", this.vegComp.forb));
				f.LogError(LogFileIn.LogMode.ERROR, String.format("  Bare Ground fraction : %5.4f", this.vegComp.bareGround));
			}
			SW_VPD_init();
			if(EchoInits)
				_echo_inits();
			return true;
		} else 
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
						this.cover.shrub = Double.parseDouble(values[1]);
						this.cover.tree = Double.parseDouble(values[2]);
						this.cover.forb = Double.parseDouble(values[3]);
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
			lines.add(String.valueOf(this.vegComp.grass)+"\t"+String.valueOf(this.vegComp.shrub)+"\t"+String.valueOf(this.vegComp.tree)+"\t"+String.valueOf(this.vegComp.forb)+"\t"+String.valueOf(this.vegComp.bareGround));
			lines.add("");
			lines.add("");
			lines.add("# ---- Albedo");
			lines.add("# Grasses	Shrubs		Trees		Forbs		Bare Ground");
			lines.add(String.valueOf(this.albedo.grass)+"\t"+String.valueOf(this.albedo.shrubs)+"\t"+String.valueOf(this.albedo.trees)+"\t"+String.valueOf(this.albedo.forbs)+"\t"+String.valueOf(this.albedo.bareGround)+
					"\t# albedo:	(Houldcroft et al. 2009) MODIS snowfree 'grassland', 'open shrub', ‘evergreen needle forest’ with MODIS albedo aggregated over pure IGBP cells where NDVI is greater than the 98th percentile NDVI");
			lines.add("");
			lines.add("");
			lines.add("# ---- % Cover: divide standing LAI by this to get % cover");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.cover.grasses)+"\t"+String.valueOf(this.cover.shrub)+"\t"+String.valueOf(this.cover.tree)+"\t"+String.valueOf(this.cover.forb));
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
	
	/** ==================================================
	 * set up vegetation parameters to be used in
	 * the "watrflow" subroutine.
	 *
	 * History:
	 *     Originally included in the FORTRAN model.
	 *
	 *     20-Oct-03 (cwb) removed the calculation of
	 *        lai_corr and changed the lai_conv value of 80
	 *        as found in the prod.in file.  The conversion
	 *        factor is now simply a divisor rather than
	 *        an equation.  Removed the following code:
	 lai_corr = v.lai_conv[m] * (1. - pstem) + aconst * pstem;
	 lai_standing    = v.biomass[m] / lai_corr;
	 where pstem = 0.3,
	 aconst = 464.0,
	 conv_stcr = 3.0;
	 *
	 *
	 */
	private void SW_VPD_init() {
		int doy; /* base1 */

		if (Defines.GT(vegComp.grass, 0.)) {
			Times.interpolate_monthlyValues(monthlyProd.grasses.litter, daily.grass.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.grasses.biomass, daily.grass.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.grasses.percLive, daily.grass.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.grasses.lai_conv, daily.grass.lai_conv_daily);
		}

		if (Defines.GT(vegComp.shrub, 0.)) {
			Times.interpolate_monthlyValues(monthlyProd.shrubs.litter, daily.shrub.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.shrubs.biomass, daily.shrub.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.shrubs.percLive, daily.shrub.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.shrubs.lai_conv, daily.shrub.lai_conv_daily);
		}

		if (Defines.GT(vegComp.tree, 0.)) {
			Times.interpolate_monthlyValues(monthlyProd.trees.litter, daily.tree.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.trees.biomass, daily.tree.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.trees.percLive, daily.tree.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.trees.lai_conv, daily.tree.lai_conv_daily);
		}

		if (Defines.GT(vegComp.forb, 0.)) {
			Times.interpolate_monthlyValues(monthlyProd.forbs.litter, daily.forb.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.forbs.biomass, daily.forb.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.forbs.percLive, daily.forb.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.forbs.lai_conv, daily.forb.lai_conv_daily);
		}

		for (doy = 1; doy <= Times.MAX_DAYS; doy++) {
			if (Defines.GT(vegComp.grass, 0.)) {
				lai_standing = daily.grass.biomass_daily[doy] / daily.grass.lai_conv_daily[doy];
				daily.grass.pct_cover_daily[doy] = lai_standing / cover.grasses;
				if (Defines.GT(canopy.grasses.canopyHeight, 0.)) {
					daily.grass.veg_height_daily[doy] = canopy.grasses.canopyHeight;
				} else {
					daily.grass.veg_height_daily[doy] = Defines.tanfunc(daily.grass.biomass_daily[doy],
							canopy.grasses.xinflec,
							canopy.grasses.yinflec,
							canopy.grasses.range,
							canopy.grasses.slope); /* used for vegcov and for snowdepth_scale */
				}
				daily.grass.lai_live_daily[doy] = lai_standing * daily.grass.pct_live_daily[doy];
				daily.grass.vegcov_daily[doy] = daily.grass.pct_cover_daily[doy] * daily.grass.veg_height_daily[doy]; /* used for vegetation interception */
				daily.grass.biolive_daily[doy] = daily.grass.biomass_daily[doy] * daily.grass.pct_live_daily[doy];
				daily.grass.biodead_daily[doy] = daily.grass.biomass_daily[doy] - daily.grass.biolive_daily[doy]; /* used for transpiration */
				daily.grass.total_agb_daily[doy] = daily.grass.litter_daily[doy] + daily.grass.biomass_daily[doy]; /* used for bare-soil evaporation */
			} else {
				daily.grass.lai_live_daily[doy] = 0.;
				daily.grass.vegcov_daily[doy] = 0.;
				daily.grass.biolive_daily[doy] = 0.;
				daily.grass.biodead_daily[doy] = 0.;
				daily.grass.total_agb_daily[doy] = 0.;
			}

			if (Defines.GT(vegComp.shrub, 0.)) {
				lai_standing = daily.shrub.biomass_daily[doy] / daily.shrub.lai_conv_daily[doy];
				daily.shrub.pct_cover_daily[doy] = lai_standing / cover.shrub;
				if (Defines.GT(canopy.shrubs.canopyHeight, 0.)) {
					daily.shrub.veg_height_daily[doy] = canopy.shrubs.canopyHeight;
				} else {
					daily.shrub.veg_height_daily[doy] = Defines.tanfunc(daily.shrub.biomass_daily[doy],
							canopy.shrubs.xinflec,
							canopy.shrubs.yinflec,
							canopy.shrubs.range,
							canopy.shrubs.slope); /* used for vegcov and for snowdepth_scale */
				}
				daily.shrub.lai_live_daily[doy] = lai_standing * daily.shrub.pct_live_daily[doy];
				daily.shrub.vegcov_daily[doy] = daily.shrub.pct_cover_daily[doy] * daily.shrub.veg_height_daily[doy]; /* used for vegetation interception */
				daily.shrub.biolive_daily[doy] = daily.shrub.biomass_daily[doy] * daily.shrub.pct_live_daily[doy];
				daily.shrub.biodead_daily[doy] = daily.shrub.biomass_daily[doy] - daily.shrub.biolive_daily[doy]; /* used for transpiration */
				daily.shrub.total_agb_daily[doy] = daily.shrub.litter_daily[doy] + daily.shrub.biomass_daily[doy]; /* used for bare-soil evaporation */
			} else {
				daily.shrub.lai_live_daily[doy] = 0.;
				daily.shrub.vegcov_daily[doy] = 0.;
				daily.shrub.biolive_daily[doy] = 0.;
				daily.shrub.biodead_daily[doy] = 0.;
				daily.shrub.total_agb_daily[doy] = 0.;
			}

			if (Defines.GT(vegComp.tree, 0.)) {
				lai_standing = daily.tree.biomass_daily[doy] / daily.tree.lai_conv_daily[doy];
				daily.tree.pct_cover_daily[doy] = lai_standing / cover.tree;
				if (Defines.GT(canopy.trees.canopyHeight, 0.)) {
					daily.tree.veg_height_daily[doy] = canopy.trees.canopyHeight;
				} else {
					daily.tree.veg_height_daily[doy] = Defines.tanfunc(daily.tree.biomass_daily[doy],
							canopy.trees.xinflec,
							canopy.trees.yinflec,
							canopy.trees.range,
							canopy.trees.slope); /* used for vegcov and for snowdepth_scale */
				}
				daily.tree.lai_live_daily[doy] = lai_standing * daily.tree.pct_live_daily[doy]; /* used for vegetation interception */
				daily.tree.vegcov_daily[doy] = daily.tree.pct_cover_daily[doy] * daily.tree.veg_height_daily[doy];
				daily.tree.biolive_daily[doy] = daily.tree.biomass_daily[doy] * daily.tree.pct_live_daily[doy];
				daily.tree.biodead_daily[doy] = daily.tree.biomass_daily[doy] - daily.tree.biolive_daily[doy]; /* used for transpiration */
				daily.tree.total_agb_daily[doy] = daily.tree.litter_daily[doy] + daily.tree.biolive_daily[doy]; /* used for bare-soil evaporation */
			} else {
				daily.tree.lai_live_daily[doy] = 0.;
				daily.tree.vegcov_daily[doy] = 0.;
				daily.tree.biolive_daily[doy] = 0.;
				daily.tree.biodead_daily[doy] = 0.;
				daily.tree.total_agb_daily[doy] = 0.;
			}

			if (Defines.GT(vegComp.forb, 0.)) {
				lai_standing = daily.forb.biomass_daily[doy] / daily.forb.lai_conv_daily[doy];
				daily.forb.pct_cover_daily[doy] = lai_standing / cover.forb;
				if (Defines.GT(canopy.forbs.canopyHeight, 0.)) {
					daily.forb.veg_height_daily[doy] = canopy.forbs.canopyHeight;
				} else {
					daily.forb.veg_height_daily[doy] = Defines.tanfunc(daily.forb.biomass_daily[doy],
							canopy.forbs.xinflec,
							canopy.forbs.yinflec,
							canopy.forbs.range,
							canopy.forbs.slope); /* used for vegcov and for snowdepth_scale */
				}
				daily.forb.lai_live_daily[doy] = lai_standing * daily.forb.pct_live_daily[doy]; /* used for vegetation interception */
				daily.forb.vegcov_daily[doy] = daily.forb.pct_cover_daily[doy] * daily.forb.veg_height_daily[doy];
				daily.forb.biolive_daily[doy] = daily.forb.biomass_daily[doy] * daily.forb.pct_live_daily[doy];
				daily.forb.biodead_daily[doy] = daily.forb.biomass_daily[doy] - daily.forb.biolive_daily[doy]; /* used for transpiration */
				daily.forb.total_agb_daily[doy] = daily.forb.litter_daily[doy] + daily.forb.biolive_daily[doy]; /* used for bare-soil evaporation */
			} else {
				daily.forb.lai_live_daily[doy] = 0.;
				daily.forb.vegcov_daily[doy] = 0.;
				daily.forb.biolive_daily[doy] = 0.;
				daily.forb.biodead_daily[doy] = 0.;
				daily.forb.total_agb_daily[doy] = 0.;
			}
		}
	}
	
	private void _echo_inits() {
		/* ================================================== */
		LogFileIn f = LogFileIn.getInstance();

		f.LogError(LogMode.NOTE, String.format("\n==============================================\n"+
				"Vegetation Production Parameters\n\n"));

		f.LogError(LogMode.NOTE, String.format("Grassland component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n"+
				"\tHydraulic redistribution flag\t= %b\n", vegComp.grass, albedo.grass, hydraulicRedistribution.grasses.flag));

		f.LogError(LogMode.NOTE, String.format("Shrubland component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n"+
				"\tHydraulic redistribution flag\t= %b\n", vegComp.shrub, albedo.shrubs, hydraulicRedistribution.shrubs.flag));

		f.LogError(LogMode.NOTE, String.format("Forest-Tree component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n"+
				"\tHydraulic redistribution flag\t= %b\n", vegComp.tree, albedo.trees, hydraulicRedistribution.trees.flag));

		f.LogError(LogMode.NOTE, String.format("FORB component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n"+
				"\tHydraulic redistribution flag\t= %b\n", vegComp.forb, albedo.forbs, hydraulicRedistribution.forbs.flag));

		f.LogError(LogMode.NOTE, String.format("Bare Ground component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n", vegComp.bareGround, albedo.bareGround));
	}
	
	public boolean get_echoinits() {
		return this.EchoInits;
	}
	public void set_echoinits(boolean echo) {
		this.EchoInits = echo;
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
