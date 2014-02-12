package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.InputData.ProdIn;
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
		public double shrub;
		public double tree;
		public double forb;
		public double bareGround;
		public void onClear() {
			this.grass=this.shrub=this.tree=this.forb=this.bareGround=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb, double bareGround) {
			this.grass = grass;
			this.shrub = shrub;
			this.tree = tree;
			this.forb = forb;
			this.bareGround = bareGround;
		}
	}
	public static class CoverPercent {
		public double grass;
		public double shrub;
		public double tree;
		public double forb;
		public void onClear() {
			this.grass=this.shrub=this.tree=this.forb=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grass = grass;
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
		public Params grass;
		public Params shrub;
		public Params tree;
		public Params forb;
		public CanopyHeight() {
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
		public Params grass;
		public Params shrub;
		public Params tree;
		public Params forb;
		public VegetationInterceptionParameters() {
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
		public Params grass;
		public Params shrub;
		public Params tree;
		public Params forb;
		public LitterInterceptionParameters() {
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
	public static class EsTpartitioning {
		public double grass;
		public double shrub;
		public double tree;
		public double forb;
		public void onClear() {
			this.grass=this.shrub=this.tree=this.forb=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grass = grass;
			this.shrub = shrub;
			this.tree = tree;
			this.forb = forb;
		}
	}
	public static class EsParamLimit {
		public double grass;
		public double shrub;
		public double tree;
		public double forb;
		public void onClear() {
			this.grass=this.shrub=this.tree=this.forb=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grass = grass;
			this.shrub = shrub;
			this.tree = tree;
			this.forb = forb;
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
		public Params grass;
		public Params shrub;
		public Params tree;
		public Params forb;
		public Shade() {
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
		public Params grass;
		public Params shrub;
		public Params tree;
		public Params forb;
		public HydraulicRedistribution() {
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
	public static class CriticalSWP {
		public double grass;
		public double shrub;
		public double tree;
		public double forb;
		public void onClear() {
			this.grass=this.shrub=this.tree=this.forb=0;
		}
		public void onSet(double grass, double shrub, double tree, double forb) {
			this.grass = grass;
			this.shrub = shrub;
			this.tree = tree;
			this.forb = forb;
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
		public Params grass;
		public Params shrub;
		public Params tree;
		public Params forb;
		public MonthlyProductionValues() {
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
	
	protected SW_VEGPROD() {
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
	
	protected void setLitter(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grass.litter[month] = grasses;
		this.monthlyProd.shrub.litter[month] = shrubs;
		this.monthlyProd.tree.litter[month] = trees;
		this.monthlyProd.forb.litter[month] = forbs;
	}
	protected void setBiomass(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grass.biomass[month] = grasses;
		this.monthlyProd.shrub.biomass[month] = shrubs;
		this.monthlyProd.tree.biomass[month] = trees;
		this.monthlyProd.forb.biomass[month] = forbs;
	}
	protected void setPercLive(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grass.percLive[month] = grasses;
		this.monthlyProd.shrub.percLive[month] = shrubs;
		this.monthlyProd.tree.percLive[month] = trees;
		this.monthlyProd.forb.percLive[month] = forbs;
	}
	protected void setLAI_conv(int month, double grasses, double shrubs, double trees, double forbs) {
		month--;
		this.monthlyProd.grass.lai_conv[month] = grasses;
		this.monthlyProd.shrub.lai_conv[month] = shrubs;
		this.monthlyProd.tree.lai_conv[month] = trees;
		this.monthlyProd.forb.lai_conv[month] = forbs;
	}
	
	protected void onClear() {
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
	
	protected boolean onVerify() {
		if(this.data) {
			LogFileIn f = LogFileIn.getInstance();
			
			this.criticalSWP.grass *= -10;
			this.criticalSWP.shrub *= -10;
			this.criticalSWP.tree *= -10;
			this.criticalSWP.forb *= -10;
			
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
	protected void onSetInput(ProdIn prodIn) {
		this.vegComp.onSet(prodIn.vegComp.grass, prodIn.vegComp.shrub, prodIn.vegComp.tree, prodIn.vegComp.forb, prodIn.vegComp.bareGround);
		this.albedo.onSet(prodIn.albedo.grass, prodIn.albedo.shrub, prodIn.albedo.tree, prodIn.albedo.forb, prodIn.albedo.bareGround);
		this.cover.onSet(prodIn.coverPercent.grass, prodIn.coverPercent.shrub, prodIn.coverPercent.tree, prodIn.coverPercent.forb);
		this.canopy.grass.onSet(prodIn.canopyHeight.grass.xinflec, prodIn.canopyHeight.grass.yinflec, prodIn.canopyHeight.grass.range, prodIn.canopyHeight.grass.slope, prodIn.canopyHeight.grass.canopyHeight);
		this.canopy.shrub.onSet(prodIn.canopyHeight.shrub.xinflec, prodIn.canopyHeight.shrub.yinflec, prodIn.canopyHeight.shrub.range, prodIn.canopyHeight.shrub.slope, prodIn.canopyHeight.shrub.canopyHeight);
		this.canopy.tree.onSet(prodIn.canopyHeight.tree.xinflec, prodIn.canopyHeight.tree.yinflec, prodIn.canopyHeight.tree.range, prodIn.canopyHeight.tree.slope, prodIn.canopyHeight.tree.canopyHeight);
		this.canopy.forb.onSet(prodIn.canopyHeight.forb.xinflec, prodIn.canopyHeight.forb.yinflec, prodIn.canopyHeight.forb.range, prodIn.canopyHeight.forb.slope, prodIn.canopyHeight.forb.canopyHeight);
		this.vegInterception.grass.onSet(prodIn.vegIntercParams.grass.a, prodIn.vegIntercParams.grass.b, prodIn.vegIntercParams.grass.c, prodIn.vegIntercParams.grass.d);
		this.vegInterception.shrub.onSet(prodIn.vegIntercParams.shrub.a, prodIn.vegIntercParams.shrub.b, prodIn.vegIntercParams.shrub.c, prodIn.vegIntercParams.shrub.d);
		this.vegInterception.tree.onSet(prodIn.vegIntercParams.tree.a, prodIn.vegIntercParams.tree.b, prodIn.vegIntercParams.tree.c, prodIn.vegIntercParams.tree.d);
		this.vegInterception.forb.onSet(prodIn.vegIntercParams.forb.a, prodIn.vegIntercParams.forb.b, prodIn.vegIntercParams.forb.c, prodIn.vegIntercParams.forb.d);
		this.litterInterception.grass.onSet(prodIn.litterIntercParams.grass.a, prodIn.litterIntercParams.grass.b, prodIn.litterIntercParams.grass.c, prodIn.litterIntercParams.grass.d);
		this.litterInterception.shrub.onSet(prodIn.litterIntercParams.shrub.a, prodIn.litterIntercParams.shrub.b, prodIn.litterIntercParams.shrub.c, prodIn.litterIntercParams.shrub.d);
		this.litterInterception.tree.onSet(prodIn.litterIntercParams.tree.a, prodIn.litterIntercParams.tree.b, prodIn.litterIntercParams.tree.c, prodIn.litterIntercParams.tree.d);
		this.litterInterception.forb.onSet(prodIn.litterIntercParams.forb.a, prodIn.litterIntercParams.forb.b, prodIn.litterIntercParams.forb.c, prodIn.litterIntercParams.forb.d);
		this.estPartitioning.onSet(prodIn.esTpart.grass, prodIn.esTpart.shrub, prodIn.esTpart.tree, prodIn.esTpart.forb);
		this.esLimit.onSet(prodIn.esLimit.grass, prodIn.esLimit.shrub, prodIn.esLimit.tree, prodIn.esLimit.forb);
		this.shade.grass.onSet(prodIn.shade.grass.shadeScale, prodIn.shade.grass.shadeMaximalDeadBiomass, prodIn.shade.grass.xinflec, prodIn.shade.grass.yinflec, prodIn.shade.grass.range, prodIn.shade.grass.slope);
		this.shade.shrub.onSet(prodIn.shade.shrub.shadeScale, prodIn.shade.shrub.shadeMaximalDeadBiomass, prodIn.shade.shrub.xinflec, prodIn.shade.shrub.yinflec, prodIn.shade.shrub.range, prodIn.shade.shrub.slope);
		this.shade.tree.onSet(prodIn.shade.tree.shadeScale, prodIn.shade.tree.shadeMaximalDeadBiomass, prodIn.shade.tree.xinflec, prodIn.shade.tree.yinflec, prodIn.shade.tree.range, prodIn.shade.tree.slope);
		this.shade.forb.onSet(prodIn.shade.forb.shadeScale, prodIn.shade.forb.shadeMaximalDeadBiomass, prodIn.shade.forb.xinflec, prodIn.shade.forb.yinflec, prodIn.shade.forb.range, prodIn.shade.forb.slope);
		this.hydraulicRedistribution.grass.onSet(prodIn.hydraulicRedist.grass.flag, prodIn.hydraulicRedist.grass.maxCondRoot, prodIn.hydraulicRedist.grass.swp50, prodIn.hydraulicRedist.grass.shapeCond);
		this.hydraulicRedistribution.shrub.onSet(prodIn.hydraulicRedist.shrub.flag, prodIn.hydraulicRedist.shrub.maxCondRoot, prodIn.hydraulicRedist.shrub.swp50, prodIn.hydraulicRedist.shrub.shapeCond);
		this.hydraulicRedistribution.tree.onSet(prodIn.hydraulicRedist.tree.flag, prodIn.hydraulicRedist.tree.maxCondRoot, prodIn.hydraulicRedist.tree.swp50, prodIn.hydraulicRedist.tree.shapeCond);
		this.hydraulicRedistribution.forb.onSet(prodIn.hydraulicRedist.forb.flag, prodIn.hydraulicRedist.forb.maxCondRoot, prodIn.hydraulicRedist.forb.swp50, prodIn.hydraulicRedist.forb.shapeCond);
		this.criticalSWP.onSet(prodIn.criticalSWP.grass, prodIn.criticalSWP.shrub, prodIn.criticalSWP.tree, prodIn.criticalSWP.forb);
		
		
		this.monthlyProd.grass.onSetLitter(prodIn.monthlyProd.grass.litter[0], prodIn.monthlyProd.grass.litter[1], prodIn.monthlyProd.grass.litter[2], prodIn.monthlyProd.grass.litter[3], prodIn.monthlyProd.grass.litter[4], prodIn.monthlyProd.grass.litter[5],
				prodIn.monthlyProd.grass.litter[6], prodIn.monthlyProd.grass.litter[7], prodIn.monthlyProd.grass.litter[8], prodIn.monthlyProd.grass.litter[9], prodIn.monthlyProd.grass.litter[10], prodIn.monthlyProd.grass.litter[11]);
		this.monthlyProd.grass.onSetBiomass(prodIn.monthlyProd.grass.biomass[0], prodIn.monthlyProd.grass.biomass[1], prodIn.monthlyProd.grass.biomass[2], prodIn.monthlyProd.grass.biomass[3], prodIn.monthlyProd.grass.biomass[4], prodIn.monthlyProd.grass.biomass[5],
				prodIn.monthlyProd.grass.biomass[6], prodIn.monthlyProd.grass.biomass[7], prodIn.monthlyProd.grass.biomass[8], prodIn.monthlyProd.grass.biomass[9], prodIn.monthlyProd.grass.biomass[10], prodIn.monthlyProd.grass.biomass[11]);
		this.monthlyProd.grass.onSetPercLive(prodIn.monthlyProd.grass.percLive[0], prodIn.monthlyProd.grass.percLive[1], prodIn.monthlyProd.grass.percLive[2], prodIn.monthlyProd.grass.percLive[3], prodIn.monthlyProd.grass.percLive[4], prodIn.monthlyProd.grass.percLive[5],
				prodIn.monthlyProd.grass.percLive[6], prodIn.monthlyProd.grass.percLive[7], prodIn.monthlyProd.grass.percLive[8], prodIn.monthlyProd.grass.percLive[9], prodIn.monthlyProd.grass.percLive[10], prodIn.monthlyProd.grass.percLive[11]);
		this.monthlyProd.grass.onSetLai_conv(prodIn.monthlyProd.grass.lai_conv[0], prodIn.monthlyProd.grass.lai_conv[1], prodIn.monthlyProd.grass.lai_conv[2], prodIn.monthlyProd.grass.lai_conv[3], prodIn.monthlyProd.grass.lai_conv[4], prodIn.monthlyProd.grass.lai_conv[5],
				prodIn.monthlyProd.grass.lai_conv[6], prodIn.monthlyProd.grass.lai_conv[7], prodIn.monthlyProd.grass.lai_conv[8], prodIn.monthlyProd.grass.lai_conv[9], prodIn.monthlyProd.grass.lai_conv[10], prodIn.monthlyProd.grass.lai_conv[11]);
		
		this.monthlyProd.shrub.onSetLitter(prodIn.monthlyProd.shrub.litter[0], prodIn.monthlyProd.shrub.litter[1], prodIn.monthlyProd.shrub.litter[2], prodIn.monthlyProd.shrub.litter[3], prodIn.monthlyProd.shrub.litter[4], prodIn.monthlyProd.shrub.litter[5],
				prodIn.monthlyProd.shrub.litter[6], prodIn.monthlyProd.shrub.litter[7], prodIn.monthlyProd.shrub.litter[8], prodIn.monthlyProd.shrub.litter[9], prodIn.monthlyProd.shrub.litter[10], prodIn.monthlyProd.shrub.litter[11]);
		this.monthlyProd.shrub.onSetBiomass(prodIn.monthlyProd.shrub.biomass[0], prodIn.monthlyProd.shrub.biomass[1], prodIn.monthlyProd.shrub.biomass[2], prodIn.monthlyProd.shrub.biomass[3], prodIn.monthlyProd.shrub.biomass[4], prodIn.monthlyProd.shrub.biomass[5],
				prodIn.monthlyProd.shrub.biomass[6], prodIn.monthlyProd.shrub.biomass[7], prodIn.monthlyProd.shrub.biomass[8], prodIn.monthlyProd.shrub.biomass[9], prodIn.monthlyProd.shrub.biomass[10], prodIn.monthlyProd.shrub.biomass[11]);
		this.monthlyProd.shrub.onSetPercLive(prodIn.monthlyProd.shrub.percLive[0], prodIn.monthlyProd.shrub.percLive[1], prodIn.monthlyProd.shrub.percLive[2], prodIn.monthlyProd.shrub.percLive[3], prodIn.monthlyProd.shrub.percLive[4], prodIn.monthlyProd.shrub.percLive[5],
				prodIn.monthlyProd.shrub.percLive[6], prodIn.monthlyProd.shrub.percLive[7], prodIn.monthlyProd.shrub.percLive[8], prodIn.monthlyProd.shrub.percLive[9], prodIn.monthlyProd.shrub.percLive[10], prodIn.monthlyProd.shrub.percLive[11]);
		this.monthlyProd.shrub.onSetLai_conv(prodIn.monthlyProd.shrub.lai_conv[0], prodIn.monthlyProd.shrub.lai_conv[1], prodIn.monthlyProd.shrub.lai_conv[2], prodIn.monthlyProd.shrub.lai_conv[3], prodIn.monthlyProd.shrub.lai_conv[4], prodIn.monthlyProd.shrub.lai_conv[5],
				prodIn.monthlyProd.shrub.lai_conv[6], prodIn.monthlyProd.shrub.lai_conv[7], prodIn.monthlyProd.shrub.lai_conv[8], prodIn.monthlyProd.shrub.lai_conv[9], prodIn.monthlyProd.shrub.lai_conv[10], prodIn.monthlyProd.shrub.lai_conv[11]);
		
		this.monthlyProd.tree.onSetLitter(prodIn.monthlyProd.tree.litter[0], prodIn.monthlyProd.tree.litter[1], prodIn.monthlyProd.tree.litter[2], prodIn.monthlyProd.tree.litter[3], prodIn.monthlyProd.tree.litter[4], prodIn.monthlyProd.tree.litter[5],
				prodIn.monthlyProd.tree.litter[6], prodIn.monthlyProd.tree.litter[7], prodIn.monthlyProd.tree.litter[8], prodIn.monthlyProd.tree.litter[9], prodIn.monthlyProd.tree.litter[10], prodIn.monthlyProd.tree.litter[11]);
		this.monthlyProd.tree.onSetBiomass(prodIn.monthlyProd.tree.biomass[0], prodIn.monthlyProd.tree.biomass[1], prodIn.monthlyProd.tree.biomass[2], prodIn.monthlyProd.tree.biomass[3], prodIn.monthlyProd.tree.biomass[4], prodIn.monthlyProd.tree.biomass[5],
				prodIn.monthlyProd.tree.biomass[6], prodIn.monthlyProd.tree.biomass[7], prodIn.monthlyProd.tree.biomass[8], prodIn.monthlyProd.tree.biomass[9], prodIn.monthlyProd.tree.biomass[10], prodIn.monthlyProd.tree.biomass[11]);
		this.monthlyProd.tree.onSetPercLive(prodIn.monthlyProd.tree.percLive[0], prodIn.monthlyProd.tree.percLive[1], prodIn.monthlyProd.tree.percLive[2], prodIn.monthlyProd.tree.percLive[3], prodIn.monthlyProd.tree.percLive[4], prodIn.monthlyProd.tree.percLive[5],
				prodIn.monthlyProd.tree.percLive[6], prodIn.monthlyProd.tree.percLive[7], prodIn.monthlyProd.tree.percLive[8], prodIn.monthlyProd.tree.percLive[9], prodIn.monthlyProd.tree.percLive[10], prodIn.monthlyProd.tree.percLive[11]);
		this.monthlyProd.tree.onSetLai_conv(prodIn.monthlyProd.tree.lai_conv[0], prodIn.monthlyProd.tree.lai_conv[1], prodIn.monthlyProd.tree.lai_conv[2], prodIn.monthlyProd.tree.lai_conv[3], prodIn.monthlyProd.tree.lai_conv[4], prodIn.monthlyProd.tree.lai_conv[5],
				prodIn.monthlyProd.tree.lai_conv[6], prodIn.monthlyProd.tree.lai_conv[7], prodIn.monthlyProd.tree.lai_conv[8], prodIn.monthlyProd.tree.lai_conv[9], prodIn.monthlyProd.tree.lai_conv[10], prodIn.monthlyProd.tree.lai_conv[11]);
		
		this.monthlyProd.forb.onSetLitter(prodIn.monthlyProd.forb.litter[0], prodIn.monthlyProd.forb.litter[1], prodIn.monthlyProd.forb.litter[2], prodIn.monthlyProd.forb.litter[3], prodIn.monthlyProd.forb.litter[4], prodIn.monthlyProd.forb.litter[5],
				prodIn.monthlyProd.forb.litter[6], prodIn.monthlyProd.forb.litter[7], prodIn.monthlyProd.forb.litter[8], prodIn.monthlyProd.forb.litter[9], prodIn.monthlyProd.forb.litter[10], prodIn.monthlyProd.forb.litter[11]);
		this.monthlyProd.forb.onSetBiomass(prodIn.monthlyProd.forb.biomass[0], prodIn.monthlyProd.forb.biomass[1], prodIn.monthlyProd.forb.biomass[2], prodIn.monthlyProd.forb.biomass[3], prodIn.monthlyProd.forb.biomass[4], prodIn.monthlyProd.forb.biomass[5],
				prodIn.monthlyProd.forb.biomass[6], prodIn.monthlyProd.forb.biomass[7], prodIn.monthlyProd.forb.biomass[8], prodIn.monthlyProd.forb.biomass[9], prodIn.monthlyProd.forb.biomass[10], prodIn.monthlyProd.forb.biomass[11]);
		this.monthlyProd.forb.onSetPercLive(prodIn.monthlyProd.forb.percLive[0], prodIn.monthlyProd.forb.percLive[1], prodIn.monthlyProd.forb.percLive[2], prodIn.monthlyProd.forb.percLive[3], prodIn.monthlyProd.forb.percLive[4], prodIn.monthlyProd.forb.percLive[5],
				prodIn.monthlyProd.forb.percLive[6], prodIn.monthlyProd.forb.percLive[7], prodIn.monthlyProd.forb.percLive[8], prodIn.monthlyProd.forb.percLive[9], prodIn.monthlyProd.forb.percLive[10], prodIn.monthlyProd.forb.percLive[11]);
		this.monthlyProd.forb.onSetLai_conv(prodIn.monthlyProd.forb.lai_conv[0], prodIn.monthlyProd.forb.lai_conv[1], prodIn.monthlyProd.forb.lai_conv[2], prodIn.monthlyProd.forb.lai_conv[3], prodIn.monthlyProd.forb.lai_conv[4], prodIn.monthlyProd.forb.lai_conv[5],
				prodIn.monthlyProd.forb.lai_conv[6], prodIn.monthlyProd.forb.lai_conv[7], prodIn.monthlyProd.forb.lai_conv[8], prodIn.monthlyProd.forb.lai_conv[9], prodIn.monthlyProd.forb.lai_conv[10], prodIn.monthlyProd.forb.lai_conv[11]);
		
		this.data = true;
	}
	protected void onGetInput(ProdIn prodIn) {
		prodIn.vegComp.onSet(this.vegComp.grass, this.vegComp.shrub, this.vegComp.tree, this.vegComp.forb, this.vegComp.bareGround);
		prodIn.albedo.onSet(this.albedo.grass, this.albedo.shrub, this.albedo.tree, this.albedo.forb, this.albedo.bareGround);
		prodIn.coverPercent.onSet(this.cover.grass, this.cover.shrub, this.cover.tree, this.cover.forb);
		prodIn.canopyHeight.grass.onSet(this.canopy.grass.xinflec, this.canopy.grass.yinflec, this.canopy.grass.range, this.canopy.grass.slope, this.canopy.grass.canopyHeight);
		prodIn.canopyHeight.shrub.onSet(this.canopy.shrub.xinflec, this.canopy.shrub.yinflec, this.canopy.shrub.range, this.canopy.shrub.slope, this.canopy.shrub.canopyHeight);
		prodIn.canopyHeight.tree.onSet(this.canopy.tree.xinflec, this.canopy.tree.yinflec, this.canopy.tree.range, this.canopy.tree.slope, this.canopy.tree.canopyHeight);
		prodIn.canopyHeight.forb.onSet(this.canopy.forb.xinflec, this.canopy.forb.yinflec, this.canopy.forb.range, this.canopy.forb.slope, this.canopy.forb.canopyHeight);
		prodIn.vegIntercParams.grass.onSet(this.vegInterception.grass.a, this.vegInterception.grass.b, this.vegInterception.grass.c, this.vegInterception.grass.d);
		prodIn.vegIntercParams.shrub.onSet(this.vegInterception.shrub.a, this.vegInterception.shrub.b, this.vegInterception.shrub.c, this.vegInterception.shrub.d);
		prodIn.vegIntercParams.tree.onSet(this.vegInterception.tree.a, this.vegInterception.tree.b, this.vegInterception.tree.c, this.vegInterception.tree.d);
		prodIn.vegIntercParams.forb.onSet(this.vegInterception.forb.a, this.vegInterception.forb.b, this.vegInterception.forb.c, this.vegInterception.forb.d);
		prodIn.litterIntercParams.grass.onSet(this.litterInterception.grass.a, this.litterInterception.grass.b, this.litterInterception.grass.c, this.litterInterception.grass.d);
		prodIn.litterIntercParams.shrub.onSet(this.litterInterception.shrub.a, this.litterInterception.shrub.b, this.litterInterception.shrub.c, this.litterInterception.shrub.d);
		prodIn.litterIntercParams.tree.onSet(this.litterInterception.tree.a, this.litterInterception.tree.b, this.litterInterception.tree.c, this.litterInterception.tree.d);
		prodIn.litterIntercParams.forb.onSet(this.litterInterception.forb.a, this.litterInterception.forb.b, this.litterInterception.forb.c, this.litterInterception.forb.d);
		prodIn.esTpart.onSet(this.estPartitioning.grass, this.estPartitioning.shrub, this.estPartitioning.tree, this.estPartitioning.forb);
		prodIn.esLimit.onSet(this.esLimit.grass, this.esLimit.shrub, this.esLimit.tree, this.esLimit.forb);
		prodIn.shade.grass.onSet(this.shade.grass.shadeScale, this.shade.grass.shadeMaximalDeadBiomass, this.shade.grass.xinflec, this.shade.grass.yinflec, this.shade.grass.range, this.shade.grass.slope);
		prodIn.shade.shrub.onSet(this.shade.shrub.shadeScale, this.shade.shrub.shadeMaximalDeadBiomass, this.shade.shrub.xinflec, this.shade.shrub.yinflec, this.shade.shrub.range, this.shade.shrub.slope);
		prodIn.shade.tree.onSet(this.shade.tree.shadeScale, this.shade.tree.shadeMaximalDeadBiomass, this.shade.tree.xinflec, this.shade.tree.yinflec, this.shade.tree.range, this.shade.tree.slope);
		prodIn.shade.forb.onSet(this.shade.forb.shadeScale, this.shade.forb.shadeMaximalDeadBiomass, this.shade.forb.xinflec, this.shade.forb.yinflec, this.shade.forb.range, this.shade.forb.slope);
		prodIn.hydraulicRedist.grass.onSet(this.hydraulicRedistribution.grass.flag, this.hydraulicRedistribution.grass.maxCondRoot, this.hydraulicRedistribution.grass.swp50, this.hydraulicRedistribution.grass.shapeCond);
		prodIn.hydraulicRedist.shrub.onSet(this.hydraulicRedistribution.shrub.flag, this.hydraulicRedistribution.shrub.maxCondRoot, this.hydraulicRedistribution.shrub.swp50, this.hydraulicRedistribution.shrub.shapeCond);
		prodIn.hydraulicRedist.tree.onSet(this.hydraulicRedistribution.tree.flag, this.hydraulicRedistribution.tree.maxCondRoot, this.hydraulicRedistribution.tree.swp50, this.hydraulicRedistribution.tree.shapeCond);
		prodIn.hydraulicRedist.forb.onSet(this.hydraulicRedistribution.forb.flag, this.hydraulicRedistribution.forb.maxCondRoot, this.hydraulicRedistribution.forb.swp50, this.hydraulicRedistribution.forb.shapeCond);
		prodIn.criticalSWP.onSet(this.criticalSWP.grass, this.criticalSWP.shrub, this.criticalSWP.tree, this.criticalSWP.forb);
		
		prodIn.monthlyProd.grass.onSetLitter(this.monthlyProd.grass.litter[0], this.monthlyProd.grass.litter[1], this.monthlyProd.grass.litter[2], this.monthlyProd.grass.litter[3], this.monthlyProd.grass.litter[4], this.monthlyProd.grass.litter[5],
				this.monthlyProd.grass.litter[6], this.monthlyProd.grass.litter[7], this.monthlyProd.grass.litter[8], this.monthlyProd.grass.litter[9], this.monthlyProd.grass.litter[10], this.monthlyProd.grass.litter[11]);
		prodIn.monthlyProd.grass.onSetBiomass(this.monthlyProd.grass.biomass[0], this.monthlyProd.grass.biomass[1], this.monthlyProd.grass.biomass[2], this.monthlyProd.grass.biomass[3], this.monthlyProd.grass.biomass[4], this.monthlyProd.grass.biomass[5],
				this.monthlyProd.grass.biomass[6], this.monthlyProd.grass.biomass[7], this.monthlyProd.grass.biomass[8], this.monthlyProd.grass.biomass[9], this.monthlyProd.grass.biomass[10], this.monthlyProd.grass.biomass[11]);
		prodIn.monthlyProd.grass.onSetPercLive(this.monthlyProd.grass.percLive[0], this.monthlyProd.grass.percLive[1], this.monthlyProd.grass.percLive[2], this.monthlyProd.grass.percLive[3], this.monthlyProd.grass.percLive[4], this.monthlyProd.grass.percLive[5],
				this.monthlyProd.grass.percLive[6], this.monthlyProd.grass.percLive[7], this.monthlyProd.grass.percLive[8], this.monthlyProd.grass.percLive[9], this.monthlyProd.grass.percLive[10], this.monthlyProd.grass.percLive[11]);
		prodIn.monthlyProd.grass.onSetLai_conv(this.monthlyProd.grass.lai_conv[0], this.monthlyProd.grass.lai_conv[1], this.monthlyProd.grass.lai_conv[2], this.monthlyProd.grass.lai_conv[3], this.monthlyProd.grass.lai_conv[4], this.monthlyProd.grass.lai_conv[5],
				this.monthlyProd.grass.lai_conv[6], this.monthlyProd.grass.lai_conv[7], this.monthlyProd.grass.lai_conv[8], this.monthlyProd.grass.lai_conv[9], this.monthlyProd.grass.lai_conv[10], this.monthlyProd.grass.lai_conv[11]);
		
		prodIn.monthlyProd.shrub.onSetLitter(this.monthlyProd.shrub.litter[0], this.monthlyProd.shrub.litter[1], this.monthlyProd.shrub.litter[2], this.monthlyProd.shrub.litter[3], this.monthlyProd.shrub.litter[4], this.monthlyProd.shrub.litter[5],
				this.monthlyProd.shrub.litter[6], this.monthlyProd.shrub.litter[7], this.monthlyProd.shrub.litter[8], this.monthlyProd.shrub.litter[9], this.monthlyProd.shrub.litter[10], this.monthlyProd.shrub.litter[11]);
		prodIn.monthlyProd.shrub.onSetBiomass(this.monthlyProd.shrub.biomass[0], this.monthlyProd.shrub.biomass[1], this.monthlyProd.shrub.biomass[2], this.monthlyProd.shrub.biomass[3], this.monthlyProd.shrub.biomass[4], this.monthlyProd.shrub.biomass[5],
				this.monthlyProd.shrub.biomass[6], this.monthlyProd.shrub.biomass[7], this.monthlyProd.shrub.biomass[8], this.monthlyProd.shrub.biomass[9], this.monthlyProd.shrub.biomass[10], this.monthlyProd.shrub.biomass[11]);
		prodIn.monthlyProd.shrub.onSetPercLive(this.monthlyProd.shrub.percLive[0], this.monthlyProd.shrub.percLive[1], this.monthlyProd.shrub.percLive[2], this.monthlyProd.shrub.percLive[3], this.monthlyProd.shrub.percLive[4], this.monthlyProd.shrub.percLive[5],
				this.monthlyProd.shrub.percLive[6], this.monthlyProd.shrub.percLive[7], this.monthlyProd.shrub.percLive[8], this.monthlyProd.shrub.percLive[9], this.monthlyProd.shrub.percLive[10], this.monthlyProd.shrub.percLive[11]);
		prodIn.monthlyProd.shrub.onSetLai_conv(this.monthlyProd.shrub.lai_conv[0], this.monthlyProd.shrub.lai_conv[1], this.monthlyProd.shrub.lai_conv[2], this.monthlyProd.shrub.lai_conv[3], this.monthlyProd.shrub.lai_conv[4], this.monthlyProd.shrub.lai_conv[5],
				this.monthlyProd.shrub.lai_conv[6], this.monthlyProd.shrub.lai_conv[7], this.monthlyProd.shrub.lai_conv[8], this.monthlyProd.shrub.lai_conv[9], this.monthlyProd.shrub.lai_conv[10], this.monthlyProd.shrub.lai_conv[11]);
		
		prodIn.monthlyProd.tree.onSetLitter(this.monthlyProd.tree.litter[0], this.monthlyProd.tree.litter[1], this.monthlyProd.tree.litter[2], this.monthlyProd.tree.litter[3], this.monthlyProd.tree.litter[4], this.monthlyProd.tree.litter[5],
				this.monthlyProd.tree.litter[6], this.monthlyProd.tree.litter[7], this.monthlyProd.tree.litter[8], this.monthlyProd.tree.litter[9], this.monthlyProd.tree.litter[10], this.monthlyProd.tree.litter[11]);
		prodIn.monthlyProd.tree.onSetBiomass(this.monthlyProd.tree.biomass[0], this.monthlyProd.tree.biomass[1], this.monthlyProd.tree.biomass[2], this.monthlyProd.tree.biomass[3], this.monthlyProd.tree.biomass[4], this.monthlyProd.tree.biomass[5],
				this.monthlyProd.tree.biomass[6], this.monthlyProd.tree.biomass[7], this.monthlyProd.tree.biomass[8], this.monthlyProd.tree.biomass[9], this.monthlyProd.tree.biomass[10], this.monthlyProd.tree.biomass[11]);
		prodIn.monthlyProd.tree.onSetPercLive(this.monthlyProd.tree.percLive[0], this.monthlyProd.tree.percLive[1], this.monthlyProd.tree.percLive[2], this.monthlyProd.tree.percLive[3], this.monthlyProd.tree.percLive[4], this.monthlyProd.tree.percLive[5],
				this.monthlyProd.tree.percLive[6], this.monthlyProd.tree.percLive[7], this.monthlyProd.tree.percLive[8], this.monthlyProd.tree.percLive[9], this.monthlyProd.tree.percLive[10], this.monthlyProd.tree.percLive[11]);
		prodIn.monthlyProd.tree.onSetLai_conv(this.monthlyProd.tree.lai_conv[0], this.monthlyProd.tree.lai_conv[1], this.monthlyProd.tree.lai_conv[2], this.monthlyProd.tree.lai_conv[3], this.monthlyProd.tree.lai_conv[4], this.monthlyProd.tree.lai_conv[5],
				this.monthlyProd.tree.lai_conv[6], this.monthlyProd.tree.lai_conv[7], this.monthlyProd.tree.lai_conv[8], this.monthlyProd.tree.lai_conv[9], this.monthlyProd.tree.lai_conv[10], this.monthlyProd.tree.lai_conv[11]);
		
		prodIn.monthlyProd.forb.onSetLitter(this.monthlyProd.forb.litter[0], this.monthlyProd.forb.litter[1], this.monthlyProd.forb.litter[2], this.monthlyProd.forb.litter[3], this.monthlyProd.forb.litter[4], this.monthlyProd.forb.litter[5],
				this.monthlyProd.forb.litter[6], this.monthlyProd.forb.litter[7], this.monthlyProd.forb.litter[8], this.monthlyProd.forb.litter[9], this.monthlyProd.forb.litter[10], this.monthlyProd.forb.litter[11]);
		prodIn.monthlyProd.forb.onSetBiomass(this.monthlyProd.forb.biomass[0], this.monthlyProd.forb.biomass[1], this.monthlyProd.forb.biomass[2], this.monthlyProd.forb.biomass[3], this.monthlyProd.forb.biomass[4], this.monthlyProd.forb.biomass[5],
				this.monthlyProd.forb.biomass[6], this.monthlyProd.forb.biomass[7], this.monthlyProd.forb.biomass[8], this.monthlyProd.forb.biomass[9], this.monthlyProd.forb.biomass[10], this.monthlyProd.forb.biomass[11]);
		prodIn.monthlyProd.forb.onSetPercLive(this.monthlyProd.forb.percLive[0], this.monthlyProd.forb.percLive[1], this.monthlyProd.forb.percLive[2], this.monthlyProd.forb.percLive[3], this.monthlyProd.forb.percLive[4], this.monthlyProd.forb.percLive[5],
				this.monthlyProd.forb.percLive[6], this.monthlyProd.forb.percLive[7], this.monthlyProd.forb.percLive[8], this.monthlyProd.forb.percLive[9], this.monthlyProd.forb.percLive[10], this.monthlyProd.forb.percLive[11]);
		prodIn.monthlyProd.forb.onSetLai_conv(this.monthlyProd.forb.lai_conv[0], this.monthlyProd.forb.lai_conv[1], this.monthlyProd.forb.lai_conv[2], this.monthlyProd.forb.lai_conv[3], this.monthlyProd.forb.lai_conv[4], this.monthlyProd.forb.lai_conv[5],
				this.monthlyProd.forb.lai_conv[6], this.monthlyProd.forb.lai_conv[7], this.monthlyProd.forb.lai_conv[8], this.monthlyProd.forb.lai_conv[9], this.monthlyProd.forb.lai_conv[10], this.monthlyProd.forb.lai_conv[11]);
	}
	protected void onRead(Path prodIn) throws IOException {
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
						this.cover.grass = Double.parseDouble(values[0]);
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
						this.canopy.grass.xinflec = Double.parseDouble(values[0]);
						this.canopy.shrub.xinflec = Double.parseDouble(values[1]);
						this.canopy.tree.xinflec = Double.parseDouble(values[2]);
						this.canopy.forb.xinflec = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - xinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 4:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - yinflec : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grass.yinflec = Double.parseDouble(values[0]);
						this.canopy.shrub.yinflec = Double.parseDouble(values[1]);
						this.canopy.tree.yinflec = Double.parseDouble(values[2]);
						this.canopy.forb.yinflec = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - yinflec : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 5:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - range : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grass.range = Double.parseDouble(values[0]);
						this.canopy.shrub.range = Double.parseDouble(values[1]);
						this.canopy.tree.range = Double.parseDouble(values[2]);
						this.canopy.forb.range = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - range : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 6:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - slope : Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grass.slope = Double.parseDouble(values[0]);
						this.canopy.shrub.slope = Double.parseDouble(values[1]);
						this.canopy.tree.slope = Double.parseDouble(values[2]);
						this.canopy.forb.slope = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - slope : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 7:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - height: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.canopy.grass.canopyHeight = Double.parseDouble(values[0]);
						this.canopy.shrub.canopyHeight = Double.parseDouble(values[1]);
						this.canopy.tree.canopyHeight = Double.parseDouble(values[2]);
						this.canopy.forb.canopyHeight = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Canopy Height - height : Could not convert string to double. " + e.getMessage());
					}
					break;
				case 8:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - a: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grass.a = Double.parseDouble(values[0]);
						this.vegInterception.shrub.a = Double.parseDouble(values[1]);
						this.vegInterception.tree.a = Double.parseDouble(values[2]);
						this.vegInterception.forb.a = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - a: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 9:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - b: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grass.b = Double.parseDouble(values[0]);
						this.vegInterception.shrub.b = Double.parseDouble(values[1]);
						this.vegInterception.tree.b = Double.parseDouble(values[2]);
						this.vegInterception.forb.b = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - b: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 10:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - c: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grass.c = Double.parseDouble(values[0]);
						this.vegInterception.shrub.c = Double.parseDouble(values[1]);
						this.vegInterception.tree.c = Double.parseDouble(values[2]);
						this.vegInterception.forb.c = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - c: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 11:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - d: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.vegInterception.grass.d = Double.parseDouble(values[0]);
						this.vegInterception.shrub.d = Double.parseDouble(values[1]);
						this.vegInterception.tree.d = Double.parseDouble(values[2]);
						this.vegInterception.forb.d = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Vegetation interception - d: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 12:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - a: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grass.a = Double.parseDouble(values[0]);
						this.litterInterception.shrub.a = Double.parseDouble(values[1]);
						this.litterInterception.tree.a = Double.parseDouble(values[2]);
						this.litterInterception.forb.a = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - a: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 13:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - b: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grass.b = Double.parseDouble(values[0]);
						this.litterInterception.shrub.b = Double.parseDouble(values[1]);
						this.litterInterception.tree.b = Double.parseDouble(values[2]);
						this.litterInterception.forb.b = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - b: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 14:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - c: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grass.c = Double.parseDouble(values[0]);
						this.litterInterception.shrub.c = Double.parseDouble(values[1]);
						this.litterInterception.tree.c = Double.parseDouble(values[2]);
						this.litterInterception.forb.c = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - c: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 15:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - d: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.litterInterception.grass.d = Double.parseDouble(values[0]);
						this.litterInterception.shrub.d = Double.parseDouble(values[1]);
						this.litterInterception.tree.d = Double.parseDouble(values[2]);
						this.litterInterception.forb.d = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Litter interception - d: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 16:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : estPartitioning: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.estPartitioning.grass = Double.parseDouble(values[0]);
						this.estPartitioning.shrub = Double.parseDouble(values[1]);
						this.estPartitioning.tree = Double.parseDouble(values[2]);
						this.estPartitioning.forb = Double.parseDouble(values[3]); 
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : estPartitioning: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 17:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : esLimit: Expected 4 Values read "+String.valueOf(values.length));
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
						this.hydraulicRedistribution.grass.flag = Integer.parseInt(values[0])>0 ? true : false;
						this.hydraulicRedistribution.shrub.flag = Integer.parseInt(values[1])>0 ? true : false;
						this.hydraulicRedistribution.tree.flag = Integer.parseInt(values[2])>0 ? true : false;
						this.hydraulicRedistribution.forb.flag = Integer.parseInt(values[3])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - flag: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 25:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - maxCondroot: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.hydraulicRedistribution.grass.maxCondRoot = Double.parseDouble(values[0]);
						this.hydraulicRedistribution.shrub.maxCondRoot = Double.parseDouble(values[1]);
						this.hydraulicRedistribution.tree.maxCondRoot = Double.parseDouble(values[2]);
						this.hydraulicRedistribution.forb.maxCondRoot = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - maxCondroot: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 26:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - swp50: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.hydraulicRedistribution.grass.swp50 = Double.parseDouble(values[0]);
						this.hydraulicRedistribution.shrub.swp50 = Double.parseDouble(values[1]);
						this.hydraulicRedistribution.tree.swp50 = Double.parseDouble(values[2]);
						this.hydraulicRedistribution.forb.swp50 = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - swp50: Could not convert string to double. " + e.getMessage());
					}
					break;
				case 27:
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Hydraulic redistribution - shapeCond: Expected 4 Values read "+String.valueOf(values.length));
					try {
						this.hydraulicRedistribution.grass.shapeCond = Double.parseDouble(values[0]);
						this.hydraulicRedistribution.shrub.shapeCond = Double.parseDouble(values[1]);
						this.hydraulicRedistribution.tree.shapeCond = Double.parseDouble(values[2]);
						this.hydraulicRedistribution.forb.shapeCond = Double.parseDouble(values[3]);
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
					if(this.nFileItemsRead > 28 && this.nFileItemsRead <= 40) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-29;
							this.monthlyProd.grass.litter[month] = Double.parseDouble(values[0]);
							this.monthlyProd.grass.biomass[month] = Double.parseDouble(values[1]);
							this.monthlyProd.grass.percLive[month] = Double.parseDouble(values[2]);
							this.monthlyProd.grass.lai_conv[month] = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else if(this.nFileItemsRead > 40 && this.nFileItemsRead <= 52) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-41;
							this.monthlyProd.shrub.litter[month] = Double.parseDouble(values[0]);
							this.monthlyProd.shrub.biomass[month] = Double.parseDouble(values[1]);
							this.monthlyProd.shrub.percLive[month] = Double.parseDouble(values[2]);
							this.monthlyProd.shrub.lai_conv[month] = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else if(this.nFileItemsRead > 52 && this.nFileItemsRead <= 64) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-53;
							this.monthlyProd.tree.litter[month] = Double.parseDouble(values[0]);
							this.monthlyProd.tree.biomass[month] = Double.parseDouble(values[1]);
							this.monthlyProd.tree.percLive[month] = Double.parseDouble(values[2]);
							this.monthlyProd.tree.lai_conv[month] = Double.parseDouble(values[3]);
						} catch(NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Could not convert string to double. " + e.getMessage());
						}
					} else if(this.nFileItemsRead > 64 && this.nFileItemsRead <= 76) {
						if(values.length != 4)
							f.LogError(LogFileIn.LogMode.ERROR, "ProductionIn onRead : Monthly Production - Grasslands: Expected 4 Values read "+String.valueOf(values.length));
						try {
							month = this.nFileItemsRead-65;
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
				this.nFileItemsRead++;
			}
		}
		this.data = true;
	}

	protected void onWrite(Path prodIn) throws IOException {
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
			lines.add(String.valueOf(this.albedo.grass)+"\t"+String.valueOf(this.albedo.shrub)+"\t"+String.valueOf(this.albedo.tree)+"\t"+String.valueOf(this.albedo.forb)+"\t"+String.valueOf(this.albedo.bareGround)+
					"\t# albedo:	(Houldcroft et al. 2009) MODIS snowfree 'grassland', 'open shrub', ‘evergreen needle forest’ with MODIS albedo aggregated over pure IGBP cells where NDVI is greater than the 98th percentile NDVI");
			lines.add("");
			lines.add("");
			lines.add("# ---- % Cover: divide standing LAI by this to get % cover");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.cover.grass)+"\t"+String.valueOf(this.cover.shrub)+"\t"+String.valueOf(this.cover.tree)+"\t"+String.valueOf(this.cover.forb));
			lines.add("");
			lines.add("");
			lines.add("# -- Canopy height (cm) parameters either constant through season or as tanfunc with respect to biomass (g/m^2)");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.canopy.grass.xinflec)+"\t"+String.valueOf(this.canopy.shrub.xinflec)+"\t"+String.valueOf(this.canopy.tree.xinflec)+"\t"+String.valueOf(this.canopy.forb.xinflec)+"\t# xinflec");
			lines.add(String.valueOf(this.canopy.grass.yinflec)+"\t"+String.valueOf(this.canopy.shrub.yinflec)+"\t"+String.valueOf(this.canopy.tree.yinflec)+"\t"+String.valueOf(this.canopy.forb.yinflec)+"\t# yinflec");
			lines.add(String.valueOf(this.canopy.grass.range)+"\t"+String.valueOf(this.canopy.shrub.range)+"\t"+String.valueOf(this.canopy.tree.range)+"\t"+String.valueOf(this.canopy.forb.range)+"\t# range");
			lines.add(String.valueOf(this.canopy.grass.slope)+"\t"+String.valueOf(this.canopy.shrub.slope)+"\t"+String.valueOf(this.canopy.tree.slope)+"\t"+String.valueOf(this.canopy.forb.slope)+"\t# slope");
			lines.add(String.valueOf(this.canopy.grass.canopyHeight)+"\t"+String.valueOf(this.canopy.shrub.canopyHeight)+"\t"+String.valueOf(this.canopy.tree.canopyHeight)+"\t"+String.valueOf(this.canopy.forb.canopyHeight)+"\t# if > 0 then constant canopy height (cm)");
			lines.add("");
			lines.add("");
			lines.add("# --- Vegetation interception parameters for equation: intercepted rain = (a + b*veg) + (c+d*veg) * ppt; Grasses+Shrubs: veg=vegcov, Trees: veg=LAI");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.vegInterception.grass.a)+"\t"+String.valueOf(this.vegInterception.shrub.a)+"\t"+String.valueOf(this.vegInterception.tree.a)+"\t"+String.valueOf(this.vegInterception.forb.a)+"\t# a");
			lines.add(String.valueOf(this.vegInterception.grass.b)+"\t"+String.valueOf(this.vegInterception.shrub.b)+"\t"+String.valueOf(this.vegInterception.tree.b)+"\t"+String.valueOf(this.vegInterception.forb.b)+"\t# b");
			lines.add(String.valueOf(this.vegInterception.grass.c)+"\t"+String.valueOf(this.vegInterception.shrub.c)+"\t"+String.valueOf(this.vegInterception.tree.c)+"\t"+String.valueOf(this.vegInterception.forb.c)+"\t# c");
			lines.add(String.valueOf(this.vegInterception.grass.d)+"\t"+String.valueOf(this.vegInterception.shrub.d)+"\t"+String.valueOf(this.vegInterception.tree.d)+"\t"+String.valueOf(this.vegInterception.forb.d)+"\t# d");
			lines.add("");
			lines.add("");
			lines.add("# --- Litter interception parameters for equation: intercepted rain = (a + b*litter) + (c+d*litter) * ppt");
			lines.add("# Grass-Litter	Shrub-Litter	Tree-Litter	Forbs-Litter");
			lines.add(String.valueOf(this.litterInterception.grass.a)+"\t"+String.valueOf(this.litterInterception.shrub.a)+"\t"+String.valueOf(this.litterInterception.tree.a)+"\t"+String.valueOf(this.litterInterception.forb.a)+"\t# a");
			lines.add(String.valueOf(this.litterInterception.grass.b)+"\t"+String.valueOf(this.litterInterception.shrub.b)+"\t"+String.valueOf(this.litterInterception.tree.b)+"\t"+String.valueOf(this.litterInterception.forb.b)+"\t# b");
			lines.add(String.valueOf(this.litterInterception.grass.c)+"\t"+String.valueOf(this.litterInterception.shrub.c)+"\t"+String.valueOf(this.litterInterception.tree.c)+"\t"+String.valueOf(this.litterInterception.forb.c)+"\t# c");
			lines.add(String.valueOf(this.litterInterception.grass.d)+"\t"+String.valueOf(this.litterInterception.shrub.d)+"\t"+String.valueOf(this.litterInterception.tree.d)+"\t"+String.valueOf(this.litterInterception.forb.d)+"\t# d");
			lines.add("");
			lines.add("");
			lines.add("# ---- Parameter for partitioning of bare-soil evaporation and transpiration as in Es = exp(-param*LAI)");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.estPartitioning.grass)+"\t"+String.valueOf(this.estPartitioning.shrub)+"\t"+String.valueOf(this.estPartitioning.tree)+"\t"+String.valueOf(this.estPartitioning.forb)+
					"\t# Trees: According to a regression based on a review by Daikoku, K., S. Hattori, A. Deguchi, Y. Aoki, M. Miyashita, K. Matsumoto, J. Akiyama, S. Iida, T. Toba, Y. Fujita, and T. Ohta. 2008. Influence of evaporation from the forest floor on evapotranspiration from the dry canopy. Hydrological Processes 22:4083-4096.");
			lines.add("");
			lines.add("");
			lines.add("# ---- Parameter for scaling and limiting bare soil evaporation rate: if totagb (g/m2) > param then no bare-soil evaporation");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.esLimit.grass)+"\t"+String.valueOf(this.esLimit.shrub)+"\t"+String.valueOf(this.esLimit.tree)+"\t"+String.valueOf(this.esLimit.forb)+"\t#");
			lines.add("");
			lines.add("");
			lines.add("# --- Shade effects on transpiration based on live and dead biomass ");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.shade.grass.shadeScale)+"\t"+String.valueOf(this.shade.shrub.shadeScale)+"\t"+String.valueOf(this.shade.tree.shadeScale)+"\t"+String.valueOf(this.shade.forb.shadeScale)+"\t# shade scale");
			lines.add(String.valueOf(this.shade.grass.shadeMaximalDeadBiomass)+"\t"+String.valueOf(this.shade.shrub.shadeMaximalDeadBiomass)+"\t"+String.valueOf(this.shade.tree.shadeMaximalDeadBiomass)+"\t"+String.valueOf(this.shade.forb.shadeMaximalDeadBiomass)+"\t# shade maximal dead biomass");
			lines.add(String.valueOf(this.shade.grass.xinflec)+"\t"+String.valueOf(this.shade.shrub.xinflec)+"\t"+String.valueOf(this.shade.tree.xinflec)+"\t"+String.valueOf(this.shade.forb.xinflec)+"\t# tanfunc: xinflec");
			lines.add(String.valueOf(this.shade.grass.yinflec)+"\t"+String.valueOf(this.shade.shrub.yinflec)+"\t"+String.valueOf(this.shade.tree.yinflec)+"\t"+String.valueOf(this.shade.forb.yinflec)+"\t# yinflec");
			lines.add(String.valueOf(this.shade.grass.range)+"\t"+String.valueOf(this.shade.shrub.range)+"\t"+String.valueOf(this.shade.tree.range)+"\t"+String.valueOf(this.shade.forb.range)+"\t# range");
			lines.add(String.valueOf(this.shade.grass.slope)+"\t"+String.valueOf(this.shade.shrub.slope)+"\t"+String.valueOf(this.shade.tree.slope)+"\t"+String.valueOf(this.shade.forb.slope)+"\t# slope");
			lines.add("");
			lines.add("");
			lines.add("# ---- Hydraulic redistribution: Ryel, Ryel R, Caldwell, Caldwell M, Yoder, Yoder C, Or, Or D, Leffler, Leffler A. 2002. Hydraulic redistribution in a stand of Artemisia tridentata: evaluation of benefits to transpiration assessed with a simulation model. Oecologia 130: 173-184.");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.hydraulicRedistribution.grass.flag?1:0)+"\t"+String.valueOf(this.hydraulicRedistribution.shrub.flag?1:0)+"\t"+String.valueOf(this.hydraulicRedistribution.tree.flag?1:0)+"\t"+String.valueOf(this.hydraulicRedistribution.forb.flag?1:0)+"\t# flag to turn on/off (1/0) hydraulic redistribution");
			lines.add(String.valueOf(this.hydraulicRedistribution.grass.maxCondRoot)+"\t"+String.valueOf(this.hydraulicRedistribution.shrub.maxCondRoot)+"\t"+String.valueOf(this.hydraulicRedistribution.tree.maxCondRoot)+"\t"+String.valueOf(this.hydraulicRedistribution.forb.maxCondRoot)+"\t# maxCondroot - maximum radial soil-root conductance of the entire active root system for water (cm/-bar/day) = 0.097 cm/MPa/h");
			lines.add(String.valueOf(this.hydraulicRedistribution.grass.swp50)+"\t"+String.valueOf(this.hydraulicRedistribution.shrub.swp50)+"\t"+String.valueOf(this.hydraulicRedistribution.tree.swp50)+"\t"+String.valueOf(this.hydraulicRedistribution.forb.swp50)+"\t# swp50 - soil water potential (-bar) where conductance is reduced by 50% = -1. MPa");
			lines.add(String.valueOf(this.hydraulicRedistribution.grass.shapeCond)+"\t"+String.valueOf(this.hydraulicRedistribution.shrub.shapeCond)+"\t"+String.valueOf(this.hydraulicRedistribution.tree.shapeCond)+"\t"+String.valueOf(this.hydraulicRedistribution.forb.shapeCond)+"\t# shapeCond - shaping parameter for the empirical relationship from van Genuchten to model relative soil-root conductance for water");
			lines.add("");
			lines.add("");
			lines.add("# ---- Critical soil water potential (MPa), i.e., when transpiration rates cannot sustained anymore, for instance, for many crop species -1.5 MPa is assumed and called wilting point");
			lines.add("# Grasses	Shrubs		Trees		Forbs");
			lines.add(String.valueOf(this.criticalSWP.grass)+"\t"+String.valueOf(this.criticalSWP.shrub)+"\t"+String.valueOf(this.criticalSWP.tree)+"\t"+String.valueOf(this.criticalSWP.forb));
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
			lines.add(this.monthlyProd.grass.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.grass.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.grass.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.grass.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.grass.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.grass.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.grass.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.grass.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.grass.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.grass.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.grass.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.grass.getString(Months.Dec.ordinal())+"\t# December");
			lines.add("");
			lines.add("# Shrublands component:");
			lines.add("#Litter\tBiomass\t%Live\tLAI_conv");
			lines.add(this.monthlyProd.shrub.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.shrub.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.shrub.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.shrub.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.shrub.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.shrub.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.shrub.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.shrub.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.shrub.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.shrub.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.shrub.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.shrub.getString(Months.Dec.ordinal())+"\t# December");
			lines.add("");
			lines.add("# Forest component:");
			lines.add("#Litter\tBiomass\t%Live\tLAI_conv");
			lines.add(this.monthlyProd.tree.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.tree.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.tree.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.tree.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.tree.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.tree.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.tree.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.tree.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.tree.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.tree.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.tree.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.tree.getString(Months.Dec.ordinal())+"\t# December");
			lines.add("");
			lines.add("# FORB component:");
			lines.add("#Litter\tBiomass\t%Live\tLAI_conv");
			lines.add(this.monthlyProd.forb.getString(Months.Jan.ordinal())+"\t# January");
			lines.add(this.monthlyProd.forb.getString(Months.Feb.ordinal())+"\t# February");
			lines.add(this.monthlyProd.forb.getString(Months.Mar.ordinal())+"\t# March");
			lines.add(this.monthlyProd.forb.getString(Months.Apr.ordinal())+"\t# April");
			lines.add(this.monthlyProd.forb.getString(Months.May.ordinal())+"\t# May");
			lines.add(this.monthlyProd.forb.getString(Months.Jun.ordinal())+"\t# June");
			lines.add(this.monthlyProd.forb.getString(Months.Jul.ordinal())+"\t# July");
			lines.add(this.monthlyProd.forb.getString(Months.Aug.ordinal())+"\t# August");
			lines.add(this.monthlyProd.forb.getString(Months.Sep.ordinal())+"\t# September");
			lines.add(this.monthlyProd.forb.getString(Months.Oct.ordinal())+"\t# October");
			lines.add(this.monthlyProd.forb.getString(Months.Nov.ordinal())+"\t# November");
			lines.add(this.monthlyProd.forb.getString(Months.Dec.ordinal())+"\t# December");
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
			Times.interpolate_monthlyValues(monthlyProd.grass.litter, daily.grass.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.grass.biomass, daily.grass.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.grass.percLive, daily.grass.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.grass.lai_conv, daily.grass.lai_conv_daily);
		}

		if (Defines.GT(vegComp.shrub, 0.)) {
			Times.interpolate_monthlyValues(monthlyProd.shrub.litter, daily.shrub.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.shrub.biomass, daily.shrub.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.shrub.percLive, daily.shrub.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.shrub.lai_conv, daily.shrub.lai_conv_daily);
		}

		if (Defines.GT(vegComp.tree, 0.)) {
			Times.interpolate_monthlyValues(monthlyProd.tree.litter, daily.tree.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.tree.biomass, daily.tree.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.tree.percLive, daily.tree.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.tree.lai_conv, daily.tree.lai_conv_daily);
		}

		if (Defines.GT(vegComp.forb, 0.)) {
			Times.interpolate_monthlyValues(monthlyProd.forb.litter, daily.forb.litter_daily);
			Times.interpolate_monthlyValues(monthlyProd.forb.biomass, daily.forb.biomass_daily);
			Times.interpolate_monthlyValues(monthlyProd.forb.percLive, daily.forb.pct_live_daily);
			Times.interpolate_monthlyValues(monthlyProd.forb.lai_conv, daily.forb.lai_conv_daily);
		}

		for (doy = 1; doy <= Times.MAX_DAYS; doy++) {
			if (Defines.GT(vegComp.grass, 0.)) {
				lai_standing = daily.grass.biomass_daily[doy] / daily.grass.lai_conv_daily[doy];
				daily.grass.pct_cover_daily[doy] = lai_standing / cover.grass;
				if (Defines.GT(canopy.grass.canopyHeight, 0.)) {
					daily.grass.veg_height_daily[doy] = canopy.grass.canopyHeight;
				} else {
					daily.grass.veg_height_daily[doy] = Defines.tanfunc(daily.grass.biomass_daily[doy],
							canopy.grass.xinflec,
							canopy.grass.yinflec,
							canopy.grass.range,
							canopy.grass.slope); /* used for vegcov and for snowdepth_scale */
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
				if (Defines.GT(canopy.shrub.canopyHeight, 0.)) {
					daily.shrub.veg_height_daily[doy] = canopy.shrub.canopyHeight;
				} else {
					daily.shrub.veg_height_daily[doy] = Defines.tanfunc(daily.shrub.biomass_daily[doy],
							canopy.shrub.xinflec,
							canopy.shrub.yinflec,
							canopy.shrub.range,
							canopy.shrub.slope); /* used for vegcov and for snowdepth_scale */
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
				if (Defines.GT(canopy.tree.canopyHeight, 0.)) {
					daily.tree.veg_height_daily[doy] = canopy.tree.canopyHeight;
				} else {
					daily.tree.veg_height_daily[doy] = Defines.tanfunc(daily.tree.biomass_daily[doy],
							canopy.tree.xinflec,
							canopy.tree.yinflec,
							canopy.tree.range,
							canopy.tree.slope); /* used for vegcov and for snowdepth_scale */
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
				if (Defines.GT(canopy.forb.canopyHeight, 0.)) {
					daily.forb.veg_height_daily[doy] = canopy.forb.canopyHeight;
				} else {
					daily.forb.veg_height_daily[doy] = Defines.tanfunc(daily.forb.biomass_daily[doy],
							canopy.forb.xinflec,
							canopy.forb.yinflec,
							canopy.forb.range,
							canopy.forb.slope); /* used for vegcov and for snowdepth_scale */
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
				"\tHydraulic redistribution flag\t= %b\n", vegComp.grass, albedo.grass, hydraulicRedistribution.grass.flag));

		f.LogError(LogMode.NOTE, String.format("Shrubland component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n"+
				"\tHydraulic redistribution flag\t= %b\n", vegComp.shrub, albedo.shrub, hydraulicRedistribution.shrub.flag));

		f.LogError(LogMode.NOTE, String.format("Forest-Tree component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n"+
				"\tHydraulic redistribution flag\t= %b\n", vegComp.tree, albedo.tree, hydraulicRedistribution.tree.flag));

		f.LogError(LogMode.NOTE, String.format("FORB component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n"+
				"\tHydraulic redistribution flag\t= %b\n", vegComp.forb, albedo.forb, hydraulicRedistribution.forb.flag));

		f.LogError(LogMode.NOTE, String.format("Bare Ground component\t= %1.2f\n"+
				"\tAlbedo\t= %1.2f\n", vegComp.bareGround, albedo.bareGround));
	}
	
	protected boolean get_echoinits() {
		return this.EchoInits;
	}
	protected void set_echoinits(boolean echo) {
		this.EchoInits = echo;
	}	
	protected VegetationComposition getVegetationComposition() {
		return this.vegComp;
	}
	protected Albedo getAlbedo() {
		return this.albedo;
	}
	protected CoverPercent getCoverPercent() {
		return this.cover;
	}
	protected CanopyHeight getCanopyHeight() {
		return this.canopy;
	}
	protected VegetationInterceptionParameters getVegetationInterceptionParameters() {
		return this.vegInterception;
	}
	protected LitterInterceptionParameters getLitterInterceptionParameters() {
		return this.litterInterception;
	}
	protected EsTpartitioning getEsTpartitioning() {
		return this.estPartitioning;
	}
	protected EsParamLimit getEsParamLimit() {
		return this.esLimit;
	}
	protected Shade getShade() {
		return this.shade;
	}
	protected HydraulicRedistribution getHydraulicRedistribution() {
		return this.hydraulicRedistribution;
	}
	protected CriticalSWP getCriticalSWP() {
		return this.criticalSWP;
	}
	protected MonthlyProductionValues getMonthlyProductionValues() {
		return this.monthlyProd;
	}
	protected DailyVegProd getDailyValues() {
		return this.daily;
	}
}
