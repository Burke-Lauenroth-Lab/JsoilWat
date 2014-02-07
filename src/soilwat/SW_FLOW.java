package soilwat;

import soilwat.LogFileIn.LogMode;
import soilwat.SW_SOILWATER.SOILWAT;


public class SW_FLOW {
	
	/*
	 * FLOW LIB STUFF
	 */
	private boolean soil_temp_error;  // simply keeps track of whether or not an error has been reported in the soil_temperature function.  0 for no, 1 for yes.
	private boolean soil_temp_init;   // simply keeps track of whether or not the regression values for the soil_temperature function have been initialized.  0 for no, 1 for yes.
	private boolean fusion_pool_init;
	private boolean debug=false;
	private int debugDOY=130;

	ST_RGR_VALUES stValues; // keeps track of the regression values, for use in soil_temperature function
	// this structure is for keeping track of the variables used in the soil_temperature function (mainly the regressions)
	private class ST_RGR_VALUES {
		double[] depths, depthsR, fcR, wpR, bDensityR, oldsFusionPool, oldsTempR;
		int[] lyrFrozen, x1BoundsR, x2BoundsR, x1Bounds, x2Bounds;
		
		public ST_RGR_VALUES() {
			depths = new double[Defines.MAX_LAYERS];
			depthsR = new double[Defines.MAX_ST_RGR+1];
			fcR = new double[Defines.MAX_ST_RGR];
			wpR = new double[Defines.MAX_ST_RGR];
			bDensityR = new double[Defines.MAX_ST_RGR];
			oldsFusionPool = new double[Defines.MAX_LAYERS];
			oldsTempR = new double[Defines.MAX_ST_RGR+1];
			
			lyrFrozen = new int[Defines.MAX_LAYERS];
			x1BoundsR = new int[Defines.MAX_ST_RGR];
			x2BoundsR = new int[Defines.MAX_ST_RGR];
			x1Bounds = new int[Defines.MAX_LAYERS];
			x2Bounds = new int[Defines.MAX_LAYERS];
		}
		public void onClear() {
			for(int i=0; i<Defines.MAX_LAYERS; i++)
				depths[i]=oldsFusionPool[i]=lyrFrozen[i]=x1Bounds[i]=x2Bounds[i]=0;
			for(int i=0; i<Defines.MAX_ST_RGR; i++) 
				fcR[i]=wpR[i]=bDensityR[i]=x1BoundsR[i]=x2BoundsR[i]=0;
			for(int i=0; i<(Defines.MAX_ST_RGR+1); i++)
				depthsR[i]=oldsTempR[i]=0;
		}
	}
	/*
	 * 
	 */
	
	/* temporary arrays for SoWat_flow_subs.c subroutines.
	 * array indexing in those routines will be from
	 * zero rather than 1.  see records2arrays().
	 */
	private class SW_FLOW_PARAMS {
		public double swpot_avg_forb, swpot_avg_tree, swpot_avg_shrub, swpot_avg_grass, soil_evap_forb, soil_evap_tree, soil_evap_shrub, soil_evap_grass, soil_evap_rate_forb,
				soil_evap_rate_tree, soil_evap_rate_shrub, soil_evap_rate_grass, soil_evap_rate_bs, transp_forb, transp_tree, transp_shrub, transp_grass,
				transp_rate_forb, transp_rate_tree, transp_rate_shrub, transp_rate_grass, snow_evap_rate, surface_evap_forb_rate, surface_evap_tree_rate,
				surface_evap_shrub_rate, surface_evap_grass_rate, surface_evap_litter_rate, surface_evap_standingWater_rate, grass_h2o, shrub_h2o, tree_h2o, forb_h2o, litter_h2o,
				litter_h2o_help, surface_h2o, h2o_for_soil, ppt_toUse, snowmelt, snowdepth_scale_grass, snowdepth_scale_shrub, snowdepth_scale_tree,
				snowdepth_scale_forb, rate_help;
		public SW_FLOW_PARAMS() {
			swpot_avg_forb=swpot_avg_tree=swpot_avg_shrub=swpot_avg_grass=soil_evap_forb=soil_evap_tree=soil_evap_shrub=soil_evap_grass=0;
			soil_evap_rate_forb=soil_evap_rate_tree=soil_evap_rate_shrub=soil_evap_rate_grass=soil_evap_rate_bs= 1.;
			transp_forb=transp_tree=transp_shrub=transp_grass=0;
			transp_rate_forb=transp_rate_tree=transp_rate_shrub=transp_rate_grass=1.;
			snow_evap_rate=surface_evap_forb_rate=surface_evap_tree_rate=surface_evap_shrub_rate=surface_evap_grass_rate=0;
			surface_evap_litter_rate=surface_evap_standingWater_rate=grass_h2o=shrub_h2o=tree_h2o=forb_h2o=litter_h2o=0;
			litter_h2o_help=surface_h2o=h2o_for_soil=ppt_toUse=snowmelt=snowdepth_scale_grass=rate_help=0;
			snowdepth_scale_shrub=snowdepth_scale_tree = snowdepth_scale_forb = 1.;
		}
		public void onClear() {
			swpot_avg_forb=swpot_avg_tree=swpot_avg_shrub=swpot_avg_grass=soil_evap_forb=soil_evap_tree=soil_evap_shrub=soil_evap_grass=0;
			soil_evap_rate_forb=soil_evap_rate_tree=soil_evap_rate_shrub=soil_evap_rate_grass=soil_evap_rate_bs= 1.;
			transp_forb=transp_tree=transp_shrub=transp_grass=0;
			transp_rate_forb=transp_rate_tree=transp_rate_shrub=transp_rate_grass=1.;
			snow_evap_rate=surface_evap_forb_rate=surface_evap_tree_rate=surface_evap_shrub_rate=surface_evap_grass_rate=0;
			surface_evap_litter_rate=surface_evap_standingWater_rate=grass_h2o=shrub_h2o=tree_h2o=forb_h2o=litter_h2o=0;
			litter_h2o_help=surface_h2o=h2o_for_soil=ppt_toUse=snowmelt=snowdepth_scale_grass=rate_help=0;
			snowdepth_scale_shrub=snowdepth_scale_tree = snowdepth_scale_forb = 1.;
		}
	}
	
	private int[] lyrTrRegions_Forb, lyrTrRegions_Tree, lyrTrRegions_Shrub, lyrTrRegions_Grass;
	
	private double drainout; /* h2o drained out of deepest layer */
	
	private double[] forb_h2o_qum, tree_h2o_qum, shrub_h2o_qum, grass_h2o_qum, litter_h2o_qum, standingWater; /* water on soil surface if layer below is saturated */
	
	private double[] lyrSWCBulk, lyrDrain, lyrTransp_Forb, lyrTransp_Tree, lyrTransp_Shrub, lyrTransp_Grass,
			lyrTranspCo_Forb, lyrTranspCo_Tree, lyrTranspCo_Shrub, lyrTranspCo_Grass, lyrEvap_BareGround,
			lyrEvap_Forb, lyrEvap_Tree, lyrEvap_Shrub, lyrEvap_Grass, lyrEvapCo, lyrSWCBulk_FieldCaps,
			lyrWidths, lyrSWCBulk_Wiltpts, lyrSWCBulk_HalfWiltpts, lyrSWCBulk_Mins, lyrSWCBulk_atSWPcrit_Forb,
			lyrSWCBulk_atSWPcrit_Tree, lyrSWCBulk_atSWPcrit_Shrub, lyrSWCBulk_atSWPcrit_Grass, lyrpsisMatric,
			lyrthetasMatric, lyrBetasMatric, lyrBetaInvMatric, /*lyrSumTrCo,*/ lyrHydRed_Forb,
			lyrHydRed_Tree, lyrHydRed_Shrub, lyrHydRed_Grass, lyrImpermeability, lyrSWCBulk_Saturated,
			lyroldsTemp, lyrsTemp, lyrbDensity;
	
	private SW_MODEL SW_Model;
	private SW_SITE SW_Site;
	private SW_SOILS SW_Soils;
	private SW_SOILWATER SW_Soilwat;
	private SW_WEATHER SW_Weather;
	private SW_VEGPROD SW_VegProd;
	private SW_SKY SW_Sky;
	private SW_FLOW_PARAMS p;
	
	public SW_FLOW(SW_MODEL SW_Model, SW_SITE SW_Site, SW_SOILS SW_Soils, SW_SOILWATER SW_Soilwat, SW_WEATHER SW_Weather, SW_VEGPROD SW_VegProd, SW_SKY SW_Sky) {
		soil_temp_error = false;
		soil_temp_init = false;
		fusion_pool_init = false;
		stValues = new ST_RGR_VALUES();
		
		this.SW_Model = SW_Model;
		this.SW_Site = SW_Site;
		this.SW_Sky = SW_Sky;
		this.SW_Soilwat = SW_Soilwat;
		this.SW_VegProd = SW_VegProd;
		this.SW_Weather = SW_Weather;
		this.SW_Soils = SW_Soils;
		
		this.p = new SW_FLOW_PARAMS();
		
		lyrTrRegions_Forb=new int[Defines.MAX_LAYERS];
		lyrTrRegions_Tree=new int[Defines.MAX_LAYERS];
		lyrTrRegions_Shrub=new int[Defines.MAX_LAYERS];
		lyrTrRegions_Grass=new int[Defines.MAX_LAYERS];
		
		drainout = 0;

		forb_h2o_qum=new double[Defines.TWO_DAYS];
		tree_h2o_qum=new double[Defines.TWO_DAYS];
		shrub_h2o_qum=new double[Defines.TWO_DAYS];
		grass_h2o_qum=new double[Defines.TWO_DAYS];
		litter_h2o_qum=new double[Defines.TWO_DAYS];
		standingWater=new double[Defines.TWO_DAYS];
		
		lyrSWCBulk=new double[Defines.MAX_LAYERS];
		lyrDrain=new double[Defines.MAX_LAYERS];
		lyrTransp_Forb=new double[Defines.MAX_LAYERS];
		lyrTransp_Tree=new double[Defines.MAX_LAYERS];
		lyrTransp_Shrub=new double[Defines.MAX_LAYERS];
		lyrTransp_Grass=new double[Defines.MAX_LAYERS];
		lyrTranspCo_Forb=new double[Defines.MAX_LAYERS];
		lyrTranspCo_Tree=new double[Defines.MAX_LAYERS];
		lyrTranspCo_Shrub=new double[Defines.MAX_LAYERS];
		lyrTranspCo_Grass=new double[Defines.MAX_LAYERS];
		lyrEvap_BareGround=new double[Defines.MAX_LAYERS];
		lyrEvap_Forb=new double[Defines.MAX_LAYERS];
		lyrEvap_Tree=new double[Defines.MAX_LAYERS];
		lyrEvap_Shrub=new double[Defines.MAX_LAYERS];
		lyrEvap_Grass=new double[Defines.MAX_LAYERS];
		lyrEvapCo=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_FieldCaps=new double[Defines.MAX_LAYERS];
		lyrWidths=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_Wiltpts=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_HalfWiltpts=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_Mins=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_atSWPcrit_Forb=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_atSWPcrit_Tree=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_atSWPcrit_Shrub=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_atSWPcrit_Grass=new double[Defines.MAX_LAYERS];
		lyrpsisMatric=new double[Defines.MAX_LAYERS];
		lyrthetasMatric=new double[Defines.MAX_LAYERS];
		lyrBetasMatric=new double[Defines.MAX_LAYERS];
		lyrBetaInvMatric=new double[Defines.MAX_LAYERS];
		//lyrSumTrCo=new double[Defines.MAX_TRANSP_REGIONS+1];
		lyrHydRed_Forb=new double[Defines.MAX_LAYERS];
		lyrHydRed_Tree=new double[Defines.MAX_LAYERS];
		lyrHydRed_Shrub=new double[Defines.MAX_LAYERS];
		lyrHydRed_Grass=new double[Defines.MAX_LAYERS];
		lyrImpermeability=new double[Defines.MAX_LAYERS];
		lyrSWCBulk_Saturated=new double[Defines.MAX_LAYERS];
		lyroldsTemp=new double[Defines.MAX_LAYERS];
		lyrsTemp=new double[Defines.MAX_LAYERS];
		lyrbDensity=new double[Defines.MAX_LAYERS];
	}
	
	public void onClear() {
		soil_temp_error = false;
		soil_temp_init = false;
		fusion_pool_init = false;
		stValues.onClear();
		drainout = 0;
		forb_h2o_qum[0]=tree_h2o_qum[0]=shrub_h2o_qum[0]=grass_h2o_qum[0]=litter_h2o_qum[0]=standingWater[0]=0;
		forb_h2o_qum[1]=tree_h2o_qum[1]=shrub_h2o_qum[1]=grass_h2o_qum[1]=litter_h2o_qum[1]=standingWater[1]=0;
	}
	
	/* *************************************************** */
	/* *************************************************** */
	/*            The Water Flow                           */
	/* --------------------------------------------------- */
	public void SW_Water_Flow() {
		int Today = Defines.Today;
		int Yesterday = Defines.Yesterday;
		
		p.onClear();

		int doy = SW_Model.getDOY(); /* base1 */
		/*	month = SW_Model.month;*//* base0 */
		if(debug && doy==debugDOY)
			System.out.println("Debug Year");

		records2arrays();
		
		SOILWAT soilWat = SW_Soilwat.getSoilWat();
		SW_VEGPROD.DailyVegProd vegDaily = SW_VegProd.getDailyValues();

		/* snowdepth scaling */
		soilWat.snowdepth = SW_SOILWATER.SW_SnowDepth(soilWat.snowpack[Today], SW_Sky.getSnow_density_daily(doy));
		/* if snow depth is deeper than vegetation height then
		 - rain and snowmelt infiltrates directly to soil (no vegetation or litter interception of today)
		 only
		 - evaporation of yesterdays interception
		 - infiltrate water high
		 - infiltrate water low */

		if (Defines.GT(vegDaily.grass.veg_height_daily[doy], 0.)) {
			p.snowdepth_scale_grass = 1. - soilWat.snowdepth / vegDaily.grass.veg_height_daily[doy];
		} else {
			p.snowdepth_scale_grass = 1.;
		}
		if (Defines.GT(vegDaily.forb.veg_height_daily[doy], 0.)) {
			p.snowdepth_scale_forb = 1. - soilWat.snowdepth / vegDaily.forb.veg_height_daily[doy];
		} else {
			p.snowdepth_scale_forb = 1.;
		}
		if (Defines.GT(vegDaily.shrub.veg_height_daily[doy], 0.)) {
			p.snowdepth_scale_shrub = 1. - soilWat.snowdepth / vegDaily.shrub.veg_height_daily[doy];
		} else {
			p.snowdepth_scale_shrub = 1.;
		}
		if (Defines.GT(vegDaily.tree.veg_height_daily[doy], 0.)) {
			p.snowdepth_scale_tree = 1. - soilWat.snowdepth / vegDaily.tree.veg_height_daily[doy];
		} else {
			p.snowdepth_scale_tree = 1.;
		}

		/* Interception */
		p.ppt_toUse = SW_Weather.getNow().rain[Today]; /* ppt is partioned into ppt = snow + rain */
		if (Defines.GT(SW_VegProd.getVegetationComposition().trees, 0.) && Defines.GT(p.snowdepth_scale_tree, 0.)) { /* trees present AND trees not fully covered in snow */
			tree_intercepted_water(doy);
			p.ppt_toUse = p.h2o_for_soil; /* amount of rain that is not intercepted by the forest canopy */
		} else { /* snow depth is more than vegetation height  */
			p.h2o_for_soil = p.ppt_toUse;
			p.tree_h2o = 0.;
		} /* end forest interception */

		if (Defines.GT(SW_VegProd.getVegetationComposition().shrubs, 0.) && Defines.GT(p.snowdepth_scale_shrub, 0.)) {
			shrub_intercepted_water(doy);
			p.ppt_toUse = p.h2o_for_soil; /* amount of rain that is not intercepted by the shrub canopy */
		} else {
			p.shrub_h2o = 0.;
		} /* end shrub interception */

		if (Defines.GT(SW_VegProd.getVegetationComposition().forbs, 0.) && Defines.GT(p.snowdepth_scale_forb, 0.)) { /* forbs present AND not fully covered in snow */
			forb_intercepted_water(doy);
			p.ppt_toUse = p.h2o_for_soil; /* amount of rain that is not intercepted by the forbs */
		} else { /* snow depth is more than vegetation height  */
			p.forb_h2o = 0.;
		} /* end forb interception */

		if (Defines.GT(SW_VegProd.getVegetationComposition().grasses, 0.) && Defines.GT(p.snowdepth_scale_grass, 0.)) {
			grass_intercepted_water(doy);
		} else {
			p.grass_h2o = 0.;
		} /* end grass interception */

		if (Defines.EQ(soilWat.snowpack[Today], 0.)) { /* litter interception only when no snow */
			p.litter_h2o_help = 0.;

			if (Defines.GT(SW_VegProd.getVegetationComposition().trees, 0.)) {
				litter_intercepted_water(SW_VegProd.getDailyValues().tree.litter_daily[doy], SW_VegProd.getVegetationComposition().trees, SW_VegProd.getLitterInterceptionParameters().trees.a,
						SW_VegProd.getLitterInterceptionParameters().trees.b, SW_VegProd.getLitterInterceptionParameters().trees.c, SW_VegProd.getLitterInterceptionParameters().trees.d);
				p.litter_h2o_help += p.litter_h2o;
			}

			if (Defines.GT(SW_VegProd.getVegetationComposition().shrubs, 0.)) {
				litter_intercepted_water(SW_VegProd.getDailyValues().shrub.litter_daily[doy], SW_VegProd.getVegetationComposition().shrubs, SW_VegProd.getLitterInterceptionParameters().shrubs.a,
						SW_VegProd.getLitterInterceptionParameters().shrubs.b, SW_VegProd.getLitterInterceptionParameters().shrubs.c, SW_VegProd.getLitterInterceptionParameters().shrubs.d);
				p.litter_h2o_help += p.litter_h2o;
			}

			if (Defines.GT(SW_VegProd.getVegetationComposition().forbs, 0.)) {
				litter_intercepted_water(SW_VegProd.getDailyValues().forb.litter_daily[doy], SW_VegProd.getVegetationComposition().forbs, SW_VegProd.getLitterInterceptionParameters().forbs.a,
						SW_VegProd.getLitterInterceptionParameters().forbs.b, SW_VegProd.getLitterInterceptionParameters().forbs.c, SW_VegProd.getLitterInterceptionParameters().forbs.d);
				p.litter_h2o_help += p.litter_h2o;
			}

			if (Defines.GT(SW_VegProd.getVegetationComposition().grasses, 0.)) {
				litter_intercepted_water(SW_VegProd.getDailyValues().grass.litter_daily[doy], SW_VegProd.getVegetationComposition().grasses, SW_VegProd.getLitterInterceptionParameters().grasses.a,
						SW_VegProd.getLitterInterceptionParameters().grasses.b, SW_VegProd.getLitterInterceptionParameters().grasses.c, SW_VegProd.getLitterInterceptionParameters().grasses.d);
				p.litter_h2o_help += p.litter_h2o;
			}

			p.litter_h2o = p.litter_h2o_help;
		} else {
			p.litter_h2o = 0.;
		}

		/* Sum cumulative intercepted components */
		soilWat.tree_int = p.tree_h2o;
		soilWat.shrub_int = p.shrub_h2o;
		soilWat.forb_int = p.forb_h2o;
		soilWat.grass_int = p.grass_h2o;
		soilWat.litter_int = p.litter_h2o;

		tree_h2o_qum[Today] = tree_h2o_qum[Yesterday] + p.tree_h2o;
		shrub_h2o_qum[Today] = shrub_h2o_qum[Yesterday] + p.shrub_h2o;
		forb_h2o_qum[Today] = forb_h2o_qum[Yesterday] + p.forb_h2o;
		grass_h2o_qum[Today] = grass_h2o_qum[Yesterday] + p.grass_h2o;
		litter_h2o_qum[Today] = litter_h2o_qum[Yesterday] + p.litter_h2o;
		/* End Interception */

		/* Surface water */
		standingWater[Today] = standingWater[Yesterday];

		/* Soil infiltration = rain+snowmelt - interception, but should be = rain+snowmelt - interception + (throughfall+stemflow) */
		p.surface_h2o = standingWater[Today];
		p.snowmelt = SW_Weather.getNow().snowmelt[Today];
		p.snowmelt = Math.max( 0., p.snowmelt * (1. - SW_Weather.getWeather().pct_snowRunoff/100.) ); /* amount of snowmelt is changed by runon/off as percentage */
		SW_Weather.getWeather().snowRunoff = SW_Weather.getNow().snowmelt[Today] - p.snowmelt;
		p.h2o_for_soil += p.snowmelt; /* if there is snowmelt, it goes un-intercepted to the soil */
		p.h2o_for_soil += p.surface_h2o;
		SW_Weather.getWeather().soil_inf = p.h2o_for_soil;

		/* Percolation for saturated soil conditions */
		infiltrate_water_high();

		SW_Weather.getWeather().soil_inf -= standingWater[Today]; /* adjust soil_infiltration for pushed back or infiltrated surface water */

		/* Surface water runoff */
		SW_Weather.getWeather().surfaceRunoff = standingWater[Today] * SW_Site.getModel().coefficients.percentRunoff;
		standingWater[Today] = Math.max(0.0, (standingWater[Today] - SW_Weather.getWeather().surfaceRunoff));
		p.surface_h2o = standingWater[Today];

		/* PET */
		soilWat.pet = SW_Site.getModel().coefficients.petMultiplier * petfunc(doy);

		/* Bare-soil evaporation rates */
		if (Defines.GT(SW_VegProd.getVegetationComposition().bareGround, 0.) && Defines.EQ(soilWat.snowpack[Today], 0.)) /* bare ground present AND no snow on ground */
		{
			pot_soil_evap_bs(doy);
			p.soil_evap_rate_bs *= SW_VegProd.getVegetationComposition().bareGround;
		} else {
			p.soil_evap_rate_bs = 0;
		}

		/* Tree transpiration & bare-soil evaporation rates */
		if (Defines.GT(SW_VegProd.getVegetationComposition().trees, 0.) && Defines.GT(p.snowdepth_scale_tree, 0.)) { /* trees present AND trees not fully covered in snow */
			tree_EsT_partitioning(doy);

			if (Defines.EQ(soilWat.snowpack[Today], 0.)) { /* bare-soil evaporation only when no snow */
				p.soil_evap_rate_tree = pot_soil_evap("tree", doy);
				p.soil_evap_rate_tree *= SW_VegProd.getVegetationComposition().trees;
			} else {
				p.soil_evap_rate_tree = 0.;
			}

			p.swpot_avg_tree = transp_weighted_avg("tree",doy);

			p.transp_rate_tree = pot_transp("tree", doy);
			p.transp_rate_tree *= p.snowdepth_scale_tree * SW_VegProd.getVegetationComposition().trees;
		} else {
			p.soil_evap_rate_tree = 0.;
			p.transp_rate_tree = 0.;
		}

		/* Shrub transpiration & bare-soil evaporation rates */
		if (Defines.GT(SW_VegProd.getVegetationComposition().shrubs, 0.) && Defines.GT(p.snowdepth_scale_shrub, 0.)) { /* shrubs present AND shrubs not fully covered in snow */
			shrub_EsT_partitioning(doy);

			if (Defines.EQ(soilWat.snowpack[Today], 0.)) { /* bare-soil evaporation only when no snow */
				p.soil_evap_rate_shrub = pot_soil_evap("shrub", doy);
				p.soil_evap_rate_shrub *= SW_VegProd.getVegetationComposition().shrubs;
			} else {
				p.soil_evap_rate_shrub = 0.;
			}

			p.swpot_avg_shrub = transp_weighted_avg("shrub", doy);

			p.transp_rate_shrub = pot_transp("shrub",doy);
			p.transp_rate_shrub *= p.snowdepth_scale_shrub * SW_VegProd.getVegetationComposition().shrubs;

		} else {
			p.soil_evap_rate_shrub = 0.;
			p.transp_rate_shrub = 0.;
		}

		/* Forb transpiration & bare-soil evaporation rates */
		if (Defines.GT(SW_VegProd.getVegetationComposition().forbs, 0.) && Defines.GT(p.snowdepth_scale_forb, 0.)) { /* forbs present AND forbs not fully covered in snow */
			forb_EsT_partitioning(doy);

			if (Defines.EQ(soilWat.snowpack[Today], 0.)) { /* bare-soil evaporation only when no snow */
				p.soil_evap_rate_forb = pot_soil_evap("forb", doy);
				p.soil_evap_rate_forb *= SW_VegProd.getVegetationComposition().forbs;
			} else {
				p.soil_evap_rate_forb = 0.;
			}

			p.swpot_avg_forb = transp_weighted_avg("forb", doy);

			p.transp_rate_forb = pot_transp("forb", doy);
			p.transp_rate_forb *= p.snowdepth_scale_forb * SW_VegProd.getVegetationComposition().forbs;

		} else {
			p.soil_evap_rate_forb = 0.;
			p.transp_rate_forb = 0.;
		}

		/* Grass transpiration & bare-soil evaporation rates */
		if (Defines.GT(SW_VegProd.getVegetationComposition().grasses, 0.) && Defines.GT(p.snowdepth_scale_grass, 0.)) { /* grasses present AND grasses not fully covered in snow */
			grass_EsT_partitioning(doy);

			if (Defines.EQ(soilWat.snowpack[Today], 0.)) { /* bare-soil evaporation only when no snow */
				p.soil_evap_rate_grass = pot_soil_evap("grass", doy);
				p.soil_evap_rate_grass *= SW_VegProd.getVegetationComposition().grasses;
			} else {
				p.soil_evap_rate_grass = 0.;
			}

			p.swpot_avg_grass = transp_weighted_avg("grass", doy);

			p.transp_rate_grass = pot_transp("grass", doy);
			p.transp_rate_grass *= p.snowdepth_scale_grass * SW_VegProd.getVegetationComposition().grasses;
		} else {
			p.soil_evap_rate_grass = 0.;
			p.transp_rate_grass = 0.;
		}

		/* Potential evaporation rates of intercepted and surface water */
		p.surface_evap_tree_rate = tree_h2o_qum[Today];
		p.surface_evap_shrub_rate = shrub_h2o_qum[Today];
		p.surface_evap_forb_rate = forb_h2o_qum[Today];
		p.surface_evap_grass_rate = grass_h2o_qum[Today];
		p.surface_evap_litter_rate = litter_h2o_qum[Today];
		p.surface_evap_standingWater_rate = standingWater[Today];
		p.snow_evap_rate = SW_Weather.getNow().snowloss[Today]; /* but this is fixed and can also include snow redistribution etc., so don't scale to PET */

		/* Scale all (potential) evaporation and transpiration flux rates to PET */
		p.rate_help = p.surface_evap_tree_rate + p.surface_evap_forb_rate + p.surface_evap_shrub_rate + p.surface_evap_grass_rate + p.surface_evap_litter_rate
				+ p.surface_evap_standingWater_rate + p.soil_evap_rate_tree + p.transp_rate_tree + p.soil_evap_rate_forb + p.transp_rate_forb + p.soil_evap_rate_shrub + p.transp_rate_shrub
				+ p.soil_evap_rate_grass + p.transp_rate_grass + p.soil_evap_rate_bs;

		if (Defines.GT(p.rate_help, soilWat.pet)) {
			p.rate_help = soilWat.pet / p.rate_help;

			p.surface_evap_tree_rate *= p.rate_help;
			p.surface_evap_forb_rate *= p.rate_help;
			p.surface_evap_shrub_rate *= p.rate_help;
			p.surface_evap_grass_rate *= p.rate_help;
			p.surface_evap_litter_rate *= p.rate_help;
			p.surface_evap_standingWater_rate *= p.rate_help;
			p.soil_evap_rate_tree *= p.rate_help;
			p.transp_rate_tree *= p.rate_help;
			p.soil_evap_rate_forb *= p.rate_help;
			p.transp_rate_forb *= p.rate_help;
			p.soil_evap_rate_shrub *= p.rate_help;
			p.transp_rate_shrub *= p.rate_help;
			p.soil_evap_rate_grass *= p.rate_help;
			p.transp_rate_grass *= p.rate_help;
			p.soil_evap_rate_bs *= p.rate_help;
		}

		/* Start adding components to AET */
		soilWat.aet = 0.; /* init aet for the day */
		soilWat.aet += p.snow_evap_rate;

		/* Evaporation of intercepted and surface water */
		evap_fromSurface();
		
		soilWat.tree_evap = p.surface_evap_tree_rate;
		soilWat.shrub_evap = p.surface_evap_shrub_rate;
		soilWat.forb_evap = p.surface_evap_forb_rate;
		soilWat.grass_evap = p.surface_evap_grass_rate;
		soilWat.litter_evap = p.surface_evap_litter_rate;
		soilWat.surfaceWater_evap = p.surface_evap_standingWater_rate;

		/* bare-soil evaporation */
		if (Defines.GT(SW_VegProd.getVegetationComposition().bareGround, 0.) && Defines.EQ(soilWat.snowpack[Today], 0.)) {
			/* remove bare-soil evap from swv */
			remove_from_soil(lyrEvap_BareGround, SW_Soils.getLayersInfo().n_evap_lyrs, lyrEvapCo, p.soil_evap_rate_bs, lyrSWCBulk_HalfWiltpts);
		} else {
			/* Set daily array to zero, no evaporation */
			for (int i = 0; i < SW_Soils.getLayersInfo().n_evap_lyrs;)
				lyrEvap_BareGround[i++] = 0.;
		}

		/* Tree transpiration and bare-soil evaporation */
		if (Defines.GT(SW_VegProd.getVegetationComposition().trees, 0.) && Defines.GT(p.snowdepth_scale_tree, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrEvap_Tree, SW_Soils.getLayersInfo().n_evap_lyrs, lyrEvapCo, p.soil_evap_rate_tree, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrTransp_Tree, SW_Soils.getLayersInfo().n_transp_lyrs_tree, lyrTranspCo_Tree, p.transp_rate_tree, lyrSWCBulk_atSWPcrit_Tree);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			for (int i = 0; i < SW_Soils.getLayersInfo().n_evap_lyrs;)
				lyrEvap_Tree[i++] = 0.;
			for (int i = 0; i < SW_Soils.getLayersInfo().n_transp_lyrs_tree;)
				lyrTransp_Tree[i++] = 0.;
		}

		/* Shrub transpiration and bare-soil evaporation */
		if (Defines.GT(SW_VegProd.getVegetationComposition().shrubs, 0.) && Defines.GT(p.snowdepth_scale_shrub, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrEvap_Shrub, SW_Soils.getLayersInfo().n_evap_lyrs, lyrEvapCo, p.soil_evap_rate_shrub, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrTransp_Shrub, SW_Soils.getLayersInfo().n_transp_lyrs_shrub, lyrTranspCo_Shrub, p.transp_rate_shrub, lyrSWCBulk_atSWPcrit_Shrub);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			for (int i = 0; i < SW_Soils.getLayersInfo().n_evap_lyrs;)
				lyrEvap_Shrub[i++] = 0.;
			for (int i = 0; i < SW_Soils.getLayersInfo().n_transp_lyrs_shrub;)
				lyrTransp_Shrub[i++] = 0.;
		}

		/* Forb transpiration and bare-soil evaporation */
		if (Defines.GT(SW_VegProd.getVegetationComposition().forbs, 0.) && Defines.GT(p.snowdepth_scale_forb, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrEvap_Forb, SW_Soils.getLayersInfo().n_evap_lyrs, lyrEvapCo, p.soil_evap_rate_forb, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrTransp_Forb, SW_Soils.getLayersInfo().n_transp_lyrs_forb, lyrTranspCo_Forb, p.transp_rate_forb, lyrSWCBulk_atSWPcrit_Forb);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			int i;
			for (i = 0; i < SW_Soils.getLayersInfo().n_evap_lyrs;)
				lyrEvap_Forb[i++] = 0.;
			for (i = 0; i < SW_Soils.getLayersInfo().n_transp_lyrs_forb;)
				lyrTransp_Forb[i++] = 0.;
		}

		/* Grass transpiration & bare-soil evaporation */
		if (Defines.GT(SW_VegProd.getVegetationComposition().grasses, 0.) && Defines.GT(p.snowdepth_scale_grass, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrEvap_Grass, SW_Soils.getLayersInfo().n_evap_lyrs, lyrEvapCo, p.soil_evap_rate_grass, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrTransp_Grass, SW_Soils.getLayersInfo().n_transp_lyrs_grass, lyrTranspCo_Grass, p.transp_rate_grass, lyrSWCBulk_atSWPcrit_Grass);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			int i;
			for (i = 0; i < SW_Soils.getLayersInfo().n_evap_lyrs;)
				lyrEvap_Grass[i++] = 0.;
			for (i = 0; i < SW_Soils.getLayersInfo().n_transp_lyrs_grass;)
				lyrTransp_Grass[i++] = 0.;
		}

		/* Hydraulic redistribution */
		if (SW_VegProd.getHydraulicRedistribution().grasses.flag && Defines.GT(SW_VegProd.getVegetationComposition().grasses, 0.) && Defines.GT(SW_VegProd.getDailyValues().grass.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrTranspCo_Grass, lyrHydRed_Grass, SW_VegProd.getHydraulicRedistribution().grasses.maxCondRoot, SW_VegProd.getHydraulicRedistribution().grasses.swp50,
					SW_VegProd.getHydraulicRedistribution().grasses.shapeCond, SW_VegProd.getVegetationComposition().grasses);
		}
		if (SW_VegProd.getHydraulicRedistribution().forbs.flag && Defines.GT(SW_VegProd.getVegetationComposition().forbs, 0.) && Defines.GT(SW_VegProd.getDailyValues().forb.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrTranspCo_Forb, lyrHydRed_Forb, SW_VegProd.getHydraulicRedistribution().forbs.maxCondRoot, SW_VegProd.getHydraulicRedistribution().forbs.swp50,
					SW_VegProd.getHydraulicRedistribution().forbs.shapeCond, SW_VegProd.getVegetationComposition().forbs);
		}
		if (SW_VegProd.getHydraulicRedistribution().shrubs.flag && Defines.GT(SW_VegProd.getVegetationComposition().shrubs, 0.) && Defines.GT(SW_VegProd.getDailyValues().shrub.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrTranspCo_Shrub, lyrHydRed_Shrub, SW_VegProd.getHydraulicRedistribution().shrubs.maxCondRoot, SW_VegProd.getHydraulicRedistribution().shrubs.swp50,
					SW_VegProd.getHydraulicRedistribution().shrubs.shapeCond, SW_VegProd.getVegetationComposition().shrubs);
		}
		if (SW_VegProd.getHydraulicRedistribution().trees.flag && Defines.GT(SW_VegProd.getVegetationComposition().trees, 0.) && Defines.GT(SW_VegProd.getDailyValues().tree.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrTranspCo_Tree, lyrHydRed_Tree, SW_VegProd.getHydraulicRedistribution().trees.maxCondRoot, SW_VegProd.getHydraulicRedistribution().trees.swp50,
					SW_VegProd.getHydraulicRedistribution().trees.shapeCond, SW_VegProd.getVegetationComposition().trees);
		}

		/* Calculate percolation for unsaturated soil water conditions. */
		/* 01/06/2011	(drs) call to infiltrate_water_low() has to be the last swc affecting calculation */

		infiltrate_water_low();

		soilWat.surfaceWater = standingWater[Today];

		/* Soil Temperature starts here */

		double biomass; // computing the standing crop biomass real quickly to condense the call to soil_temperature
		biomass = SW_VegProd.getDailyValues().grass.biomass_daily[doy] * SW_VegProd.getVegetationComposition().grasses + SW_VegProd.getDailyValues().shrub.biomass_daily[doy] * SW_VegProd.getVegetationComposition().shrubs
				+ SW_VegProd.getDailyValues().forb.biomass_daily[doy] * SW_VegProd.getVegetationComposition().forbs + SW_VegProd.getDailyValues().tree.biolive_daily[doy] * SW_VegProd.getVegetationComposition().trees; // changed to exclude tree biomass, bMatric/c it was breaking the soil_temperature function

				// soil_temperature function computes the soil temp for each layer and stores it in lyrsTemp
				// doesn't affect SWC at all, but needs it for the calculation, so therefore the temperature is the last calculation done
		if (SW_Site.getSoilTemperature().use_soil_temp)
			soil_temperature(biomass);

		/* Soil Temperature ends here */

		/* Move local values into main arrays */
		arrays2records();

		standingWater[Yesterday] = standingWater[Today];
		litter_h2o_qum[Yesterday] = litter_h2o_qum[Today];
		tree_h2o_qum[Yesterday] = tree_h2o_qum[Today];
		shrub_h2o_qum[Yesterday] = shrub_h2o_qum[Today];
		forb_h2o_qum[Yesterday] = forb_h2o_qum[Today];
		grass_h2o_qum[Yesterday] = grass_h2o_qum[Today];

	} /* END OF WATERFLOW */
	
	private void records2arrays() {
		/* some values are unchanged by the water subs but
		 * are still required in an array format.
		 * Also, some arrays start out empty and are
		 * filled during the water flow.
		 * See arrays2records() for the modified arrays.
		 *
		 * 3/24/2003 - cwb - when running with steppe, the
		 *       static variable firsttime would only be set once
		 *       so the firsttime tasks were done only the first
		 *       year, but what we really want with stepwat is
		 *       to firsttime tasks on the first day of each year.
		 * 1-Oct-03 (cwb) - Removed references to sum_transp_coeff.
		 *       see also Site.c.
		 */
		SOILWAT soilWat = SW_Soilwat.getSoilWat();
		
		
		for(int i=0; i<SW_Soils.getLayersInfo().n_layers; i++) {
			lyrSWCBulk[i] = soilWat.swcBulk[Defines.Today][i];
			lyroldsTemp[i] = soilWat.sTemp[i];
		}
		
		if(SW_Model.getDOY() == SW_Model.getFirstdoy()) {
			for(int i=0; i<SW_Soils.getLayersInfo().n_layers; i++) {
				SW_SOILS.SW_LAYER_INFO lyr = SW_Soils.getLayer(i);
				lyrTrRegions_Tree[i] = lyr.my_transp_rgn_tree;
				lyrTrRegions_Forb[i] = lyr.my_transp_rgn_forb;
				lyrTrRegions_Shrub[i] = lyr.my_transp_rgn_shrub;
				lyrTrRegions_Grass[i] = lyr.my_transp_rgn_grass;
				lyrSWCBulk_FieldCaps[i] = lyr.swcBulk_fieldcap;
				lyrWidths[i] = lyr.width;
				lyrSWCBulk_Wiltpts[i] = lyr.swcBulk_wiltpt;
				lyrSWCBulk_HalfWiltpts[i] = lyr.swcBulk_wiltpt / 2.;
				lyrSWCBulk_atSWPcrit_Tree[i] = lyr.swcBulk_atSWPcrit_tree;
				lyrSWCBulk_atSWPcrit_Forb[i] = lyr.swcBulk_atSWPcrit_forb;
				lyrSWCBulk_atSWPcrit_Shrub[i] = lyr.swcBulk_atSWPcrit_shrub;
				lyrSWCBulk_atSWPcrit_Grass[i] = lyr.swcBulk_atSWPcrit_grass;
				lyrSWCBulk_Mins[i] = lyr.swcBulk_min;
				lyrpsisMatric[i] = lyr.psisMatric;
				lyrthetasMatric[i] = lyr.thetasMatric;
				lyrBetasMatric[i] = lyr.bMatric;
				lyrBetaInvMatric[i] = lyr.binverseMatric;
				lyrImpermeability[i] = lyr.impermeability;
				lyrSWCBulk_Saturated[i] = lyr.swcBulk_saturated;
				lyrbDensity[i] = lyr.soilBulk_density;

				/*Init hydraulic redistribution to zero */
				lyrHydRed_Tree[i] = 0.;
				lyrHydRed_Shrub[i] = 0.;
				lyrHydRed_Forb[i] = 0.;
				lyrHydRed_Grass[i] = 0.;
			}
			
			for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_tree; i++)
			{
				lyrTranspCo_Tree[i] = SW_Soils.getLayer(i).transp_coeff_tree;
			}

			for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_shrub; i++)
			{
				lyrTranspCo_Shrub[i] = SW_Soils.getLayer(i).transp_coeff_shrub;
			}

			for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_forb; i++)
			{
				lyrTranspCo_Forb[i] = SW_Soils.getLayer(i).transp_coeff_forb;
			}

			for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_grass; i++)
			{
				lyrTranspCo_Grass[i] = SW_Soils.getLayer(i).transp_coeff_grass;
			}

			for(int i=0; i<SW_Soils.getLayersInfo().n_evap_lyrs; i++) {
				lyrEvapCo[i] = SW_Soils.getLayer(i).evap_coeff;
			}
		}/* end firsttime stuff */
	}
	
	private void arrays2records() {
		/* move output quantities from arrays to
		 * the appropriate records.
		 */
		SOILWAT soilWat = SW_Soilwat.getSoilWat();
		
		for(int i=0; i<SW_Soils.getLayersInfo().n_layers; i++)
		{
			soilWat.swcBulk[Defines.Today][i] = lyrSWCBulk[i];
			soilWat.drain[i] = lyrDrain[i];
			soilWat.hydred_tree[i] = lyrHydRed_Tree[i];
			soilWat.hydred_shrub[i] = lyrHydRed_Shrub[i];
			soilWat.hydred_forb[i] = lyrHydRed_Forb[i];
			soilWat.hydred_grass[i] = lyrHydRed_Grass[i];
			soilWat.sTemp[i] = lyrsTemp[i];
		}

		if (SW_Site.getDeepdrain())
			soilWat.swcBulk[Defines.Today][SW_Soils.getLayersInfo().deep_lyr] = drainout;

		for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_tree; i++)
		{
			soilWat.transpiration_tree[i] = lyrTransp_Tree[i];
		}

		for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_shrub; i++)
		{
			soilWat.transpiration_shrub[i] = lyrTransp_Shrub[i];
		}

		for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_forb; i++)
		{
			soilWat.transpiration_forb[i] = lyrTransp_Forb[i];
		}

		for(int i=0; i<SW_Soils.getLayersInfo().n_transp_lyrs_grass; i++)
		{
			soilWat.transpiration_grass[i] = lyrTransp_Grass[i];
		}

		for(int i=0; i<SW_Soils.getLayersInfo().n_evap_lyrs; i++)
		{
			soilWat.evaporation[i] = lyrEvap_BareGround[i] + lyrEvap_Tree[i] + lyrEvap_Forb[i] + lyrEvap_Shrub[i] + lyrEvap_Grass[i];
		}
	}
	
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	private void grass_intercepted_water(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate the water intercepted by grasses.

		 HISTORY:
		 4/30/92  (SLC)
		 7/1/92   (SLC) Reset pptleft to 0 if less than 0 (due to round off)
		 1/19/93  (SLC) Check if vegcov is zero (in case there was no biomass),
		 then no standing crop interception is possible.
		 15-Oct-03 (cwb) replaced Parton's original equations with new ones
		 developed by John Bradford based on Corbet and Crouse 1968.
		 Replaced the following code:
		 par1 = LE(vegcov, 8.5) ?  0.9 + 0.04 * vegcov
		 : 1.24 + (vegcov-8.5) *.35;
		 par2 = LE(vegcov, 3.0) ? vegcov * .33333333
		 : 1. + (vegcov-3.)*0.182;
		 *wintstcr = par1 * .026 * ppt + 0.094 * par2;

		 21-Oct-03 (cwb) added MAX_WINTLIT line

		 INPUTS:
		 ppt     - precip. for the day
		 vegcov  - vegetation cover for the day (based on monthly biomass
		 values, see the routine "initprod")

		 OUTPUT:
		 pptleft -  precip. left after interception by standing crop.
		 wintstcr - amount of water intercepted by standing crop.
		 **********************************************************************/
		double intcpt, slope;
		
		if (Defines.GT(SW_VegProd.getDailyValues().grass.vegcov_daily[doy], 0.) && Defines.GT(p.ppt_toUse, 0.)) {
			intcpt = SW_VegProd.getVegetationInterceptionParameters().grasses.b * SW_VegProd.getDailyValues().grass.vegcov_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().grasses.a;
			slope = SW_VegProd.getVegetationInterceptionParameters().grasses.d * SW_VegProd.getDailyValues().grass.vegcov_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().grasses.c;

			p.grass_h2o = (intcpt + slope * p.ppt_toUse) * (p.snowdepth_scale_grass * SW_VegProd.getVegetationComposition().grasses);

			p.grass_h2o = Math.min(p.grass_h2o, p.ppt_toUse);
			p.grass_h2o = Math.min(p.grass_h2o, SW_VegProd.getDailyValues().grass.vegcov_daily[doy]*.1);
			p.h2o_for_soil = Math.max( p.ppt_toUse - p.grass_h2o, 0.0);
		} else { /*  no precip., so obviously nothing is intercepted by standing crop. */
			p.h2o_for_soil = p.ppt_toUse;
			p.grass_h2o = 0.0;
		}
	}

	private void shrub_intercepted_water(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate the water intercepted by shrubs
		 
		OUTPUT:
		 pptleft -  precip. left after interception by shrub in cm.
		 wintfor - amount of water intercepted by shrub in cm.
		 **********************************************************************/
		double intcpt, slope;
		
		if (Defines.GT(SW_VegProd.getDailyValues().shrub.vegcov_daily[doy], 0.) && Defines.GT(p.ppt_toUse, 0.)) {
			intcpt = SW_VegProd.getVegetationInterceptionParameters().shrubs.b * SW_VegProd.getDailyValues().shrub.vegcov_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().shrubs.a;
			slope = SW_VegProd.getVegetationInterceptionParameters().shrubs.d * SW_VegProd.getDailyValues().shrub.vegcov_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().shrubs.c;

			p.shrub_h2o = (intcpt + slope * p.ppt_toUse) * p.snowdepth_scale_shrub * SW_VegProd.getVegetationComposition().shrubs;

			p.shrub_h2o = Math.min(p.shrub_h2o, p.ppt_toUse);
			p.shrub_h2o = Math.min(p.shrub_h2o, (SW_VegProd.getDailyValues().shrub.vegcov_daily[doy] * .1));
			p.h2o_for_soil = Math.max( p.ppt_toUse - p.shrub_h2o, 0.0);
		} else { /*  no precip., so obviously nothing is intercepted by standing crop. */
			p.h2o_for_soil = p.ppt_toUse;
			p.shrub_h2o = 0.0;
		}
	}

	private void tree_intercepted_water(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate water intercepted by forests

		 HISTORY:
		 11/16/2010	(drs)

		 INPUTS:
		 ppt     - precip. for the day in cm
		 LAI	- forest LAI in cm/cm
		 scale - scale interception with fraction of tree vegetation component or with snowdepth-scaler

		 OUTPUT:
		 pptleft -  precip. left after interception by forest in cm.
		 wintfor - amount of water intercepted by forest in cm.
		 **********************************************************************/
		
		double intcpt, slope;

		if (Defines.GT(SW_VegProd.getDailyValues().tree.lai_live_daily[doy], 0.) && Defines.GT(p.ppt_toUse, 0.)) {
			intcpt = SW_VegProd.getVegetationInterceptionParameters().trees.b * SW_VegProd.getDailyValues().tree.lai_live_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().trees.a;
			slope = SW_VegProd.getVegetationInterceptionParameters().trees.d * SW_VegProd.getDailyValues().tree.lai_live_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().trees.c;

			p.tree_h2o = (intcpt + slope * p.ppt_toUse) * (p.snowdepth_scale_tree * SW_VegProd.getVegetationComposition().trees);

			p.tree_h2o = Math.min(p.tree_h2o, p.ppt_toUse);
			//p.tree_h2o = Math.min(p.tree_h2o, p.ppt_toUse);
			p.h2o_for_soil = Math.max( p.ppt_toUse - p.tree_h2o, 0.0);
		} else { /*  no precip., so obviously nothing is intercepted by forest. */
			p.h2o_for_soil = p.ppt_toUse;
			p.tree_h2o = 0.0;
		}
	}

	private void forb_intercepted_water(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate water intercepted by forbs


		 HISTORY:
		 07/09/2013	(clk)

		 INPUTS:

		 ppt     - precip. for the day in cm
		 vegcov	- vegetation cover for the day (based on monthly biomass
		 values, see the routine "initprod")
		 scale - scale interception with fraction of forb vegetation component or with snowdepth-scaler

		 OUTPUT:

		 pptleft -  precip. left after interception by forb in cm.
		 wintforb - amount of water intercepted by forb in cm.
		 **********************************************************************/
		double intcpt, slope;

		if (Defines.GT(SW_VegProd.getDailyValues().forb.vegcov_daily[doy], 0.) && Defines.GT(p.ppt_toUse, 0.)) {
			intcpt = SW_VegProd.getVegetationInterceptionParameters().forbs.b * SW_VegProd.getDailyValues().forb.vegcov_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().forbs.a;
			slope = SW_VegProd.getVegetationInterceptionParameters().forbs.d * SW_VegProd.getDailyValues().forb.vegcov_daily[doy] + SW_VegProd.getVegetationInterceptionParameters().forbs.c;

			p.forb_h2o = (intcpt + slope * p.ppt_toUse) * (p.snowdepth_scale_forb * SW_VegProd.getVegetationComposition().forbs);

			p.forb_h2o = Math.min(p.forb_h2o, p.ppt_toUse);
			p.forb_h2o = Math.min(p.forb_h2o, SW_VegProd.getDailyValues().forb.vegcov_daily[doy]*.1);
			p.h2o_for_soil = Math.max( p.ppt_toUse - p.forb_h2o, 0.0);
		} else { /*  no precip., so obviously nothing is intercepted by forest. */
			p.h2o_for_soil = p.ppt_toUse;
			p.forb_h2o = 0.0;
		}
	}

	private void litter_intercepted_water(double blitter, double scale, double a, double b, double c, double d) {
		/**********************************************************************
		 PURPOSE: Calculate water intercepted by litter

		 HISTORY:
		 4/30/92  (SLC)
		 7/1/92   (SLC) Reset pptleft to 0 if less than 0 (due to round off)
		 6-Oct-03 (cwb) wintlit = 0 if no litter.
		 15-Oct-03 (cwb) replaced Parton's original equations with new ones
		 developed by John Bradford based on Corbet and Crouse, 1968.
		 Replaced the following code:
		 par1 = exp((-1. + .45 * log10(blitter+1.)) * log(10.));
		 *wintlit = (.015 * (*pptleft) + .0635) * exp(par1);

		 21-Oct-03 (cwb) added MAX_WINTLIT line

		 INPUTS:
		 blitter - biomass of litter for the day

		 OUTPUTS:
		 pptleft -  precip. left after interception by litter.
		 wintlit  - amount of water intercepted by litter .
		 **********************************************************************/
		double intcpt, slope;

		if (Defines.isZero(blitter)) {
			p.litter_h2o = 0.0;
		} else if (Defines.GT(p.h2o_for_soil, 0.0)) {
			intcpt = b * blitter + a;
			slope = d * blitter + c;

			p.litter_h2o = (intcpt + slope * (p.h2o_for_soil)) * scale;

			p.litter_h2o = Math.min(p.h2o_for_soil,p.litter_h2o);
			p.litter_h2o = Math.min(p.litter_h2o, (blitter * .2));
			p.h2o_for_soil -= p.litter_h2o;
			p.h2o_for_soil = Math.max(p.h2o_for_soil, 0.0);

		} else {
			p.h2o_for_soil = 0.0;
			p.litter_h2o = 0.0;
		}
	}

	private void infiltrate_water_high() {
		/**********************************************************************
		 PURPOSE: Infilitrate water into soil layers under high water
		 conditions.

		 HISTORY:
		 4/30/92  (SLC)
		 1/14/02 - (cwb) fixed off by one error in loop.
		 10/20/03 - (cwb) added drainout variable to return drainage
		 out of lowest layer

		 INPUTS:
		 swc - soilwater content before drainage.
		 swcfc    - soilwater content at field capacity.
		 pptleft  - precip. available to the soil.
		 nlyrs - number of layers to drain from

		 OUTPUTS:
		 drain  - drainage from layers
		 swc_local - soilwater content after water has been drained
		 **********************************************************************/
		int i;
		int j;
		double[] d = new double[SW_Soils.getLayersInfo().n_layers];
		double push;

		if (stValues.lyrFrozen[0] == 0) {
			lyrSWCBulk[0] += p.h2o_for_soil;
			(standingWater[Defines.Today]) = 0.;
		}

		for (i = 0; i < SW_Soils.getLayersInfo().n_layers; i++) {
			if (stValues.lyrFrozen[i] == 0 && stValues.lyrFrozen[i + 1] == 0) {
				/* calculate potential saturated percolation */
				d[i] = Math.max(0., (1. - lyrImpermeability[i]) * (lyrSWCBulk[i] - lyrSWCBulk_FieldCaps[i]) );
				lyrDrain[i] = d[i];

				if (i < SW_Soils.getLayersInfo().n_layers - 1) { /* percolate up to next-to-last layer */
					lyrSWCBulk[i + 1] += d[i];
					lyrSWCBulk[i] -= d[i];
				} else { /* percolate last layer */
					(this.drainout) = d[i];
					lyrSWCBulk[i] -= (this.drainout);
				}
			}
		}

		/* adjust (i.e., push water upwards) if water content of a layer is now above saturated water content */
		for (j = SW_Soils.getLayersInfo().n_layers; j >= 0; j--) {
			if (stValues.lyrFrozen[i] == 0) {
				if (Defines.GT(lyrSWCBulk[j], lyrSWCBulk_Saturated[j])) {
					push = lyrSWCBulk[j] - lyrSWCBulk_Saturated[j];
					lyrSWCBulk[j] -= push;
					if (j > 0) {
						lyrDrain[j - 1] -= push;
						lyrSWCBulk[j - 1] += push;
					} else {
						(standingWater[Defines.Today]) = push;
					}
				}
			}
		}
		if(debug) {
			String temp = "";
			for (i = 0; i < SW_Soils.getLayersInfo().n_layers; i++) {
				temp+=String.format(" %7f", lyrSWCBulk[i]);
			}
			System.out.println(String.format("%26s%s", "infiltrate_water_high: ",temp));
		}
	}

	private double petfunc(int doy) {
		/***********************************************************************
		 PURPOSE: Calculate the potential evapotranspiration [mm/day] rate using pennmans equation (1948)

		 HISTORY:
		 4/30/92  (SLC)
		 10/11/2012	(drs)	annotated all equations;
		 replaced unknown equation for solar declination with one by Spencer (1971);
		 replaced unknown equation for 'Slope of the Saturation Vapor Pressure-Temperature Curve' = arads with one provided by Allen et al. (1998) and (2005);
		 replaced constant psychrometric constant (0.27 [mmHg/F]) as function of pressure, and pressure as function of elevation of site (Allen et al. (1998) and (2005));
		 windspeed data is in [m/s] and not in code-required [miles/h] -> fixed conversation factor so that code now requires [m/s];
		 replaced conversion addend from C to K (previously, 273) with 273.15;
		 updated solar constant from S = 2.0 [ly/min] to S = 1.952 [ly/min] (Kopp et al. 2011) in the equation to calculate solrad;
		 replaced unknown numerical factor of 0.201 in black-body long wave radiation to sigma x conversion-factor = 0.196728 [mm/day/K4];
		 --> further update suggestions:
		 - add Seller's factor '(mean(d)/d)^2' with (mean(d) = mean distance and d = instantaneous distance of the earth from the sun) to shortwave calculations
		 11/06/2012	(clk)	allowed slope and aspect to be used in the calculation for solar radiation;
		 if slope is 0, will still use old equation,
		 else will sum up Seller (1965), page 35, eqn. 3.15 from sunrise to sunset.

		 SOURCES:
		 Allen RG, Pereira LS, Raes D, Smith M (1998) In Crop evapotranspiration - Guidelines for computing crop water requirements. FAO - Food and Agriculture Organizations of the United Nations, Rome.
		 Allen RG, Walter IA, Elliott R, Howell T, Itenfisu D, Jensen M (2005) In The ASCE standardized reference evapotranspiration equation, pp. 59. ASCE-EWRI Task Committee Report.
		 Bowen IS (1926) The Ratio of Heat Losses by Conduction and by Evaporation from any Water Surface. Physical Review, 27, 779.
		 Brunt D (1939) Physical and dynamical meteorology. Cambridge University Press.
		 Kopp G, Lean JL (2011) A new, lower value of total solar irradiance: Evidence and climate significance. Geophysical Research Letters, 38, L01706.
		 Merva GE (1975) Physioengineering principles. Avi Pub. Co., Westport, Conn., ix, 353 p. pp.
		 Penman HL (1948) Natural evaporation form open water, bare soil and grass. Proceedings of the Royal Society of London. Series A, Mathematical and Physical Sciences, 193, 120-145.
		 Sellers WD (1965) Physical climatology. University of Chicago Press, Chicago, USA.
		 Spencer JW (1971) Fourier Series Representation of the Position of the Sun. Search, 2, 172-172.


		 INPUTS:
		 Time.Model:
		 doy            		- current day number
		 sky_parms:
		 humid(month)   		- average relative humidity for the month. (%)
		 windsp(month)   	- average wind speed for the month at 2-m above ground. (m/s)
		 cloudcov(month)   	- average cloud cover for the month. (%)
		 transcoeff(month) 	- transmission coefficient for the month (not used in result)
		 weather:
		 avgtemp         	- average temperature for the day [C]
		 site_parm:
		 reflec          	- albedo [-]
		 rlat	       		- latitude of the site (in radians)
		 elev				- elevation of site (m above sea level)
		 slope			- slope of the site (in degrees)
		 aspect			- aspect of the site (in degrees)

		 LOCAL VARIABLES:
		 solrad - solar radiation (ly/day)
		 declin - solar declination (radians)
		 ahou   - sunset hour angle
		 azmth  - azimuth angle of the sun
		 azmthSlope - azimuth angle of the slope
		 rslope - slope of the site (radians)
		 hou    - hour angle
		 shwave - short wave solar radiation (mm/day)
		 kelvin - average air temperature [K]
		 arads  - 'Slope of the Saturation Vapor Pressure-Temperature Curve' [mmHg/F]
		 clrsky - relative amount of clear sky
		 fhumid - saturation vapor pressure at dewpoint [mmHg]
		 ftemp  - theoretical black-body radiation [mm/day]
		 par1,par2 - parameters in computation of pet.
		 cosZ,sinZ - parameters in computation of pet.
		 cosA,sinA - parameters in computation of pet.
		 stepSize - the step size to use in integration

		 ***********************************************************************/
		double avgtemp=SW_Weather.getNow().temp_avg[Defines.Today];
		double rlat=SW_Site.getIntrinsic().latitude;
		double elev=SW_Site.getIntrinsic().altitude;
		double slope=SW_Site.getIntrinsic().slope;
		double aspect=SW_Site.getIntrinsic().aspect;
		
		double reflec=SW_VegProd.getAlbedo().grasses * SW_VegProd.getVegetationComposition().grasses + SW_VegProd.getAlbedo().shrubs * SW_VegProd.getVegetationComposition().shrubs + SW_VegProd.getAlbedo().forbs * SW_VegProd.getVegetationComposition().forbs + SW_VegProd.getAlbedo().trees * SW_VegProd.getVegetationComposition().trees + SW_VegProd.getAlbedo().bareGround * SW_VegProd.getVegetationComposition().bareGround;
		double humid=SW_Sky.getR_humidity_daily(doy);
		double windsp=SW_Sky.getWindspeed_daily(doy);
		double cloudcov=SW_Sky.getCloudcov_daily(doy);
		double transcoeff=SW_Sky.getTransmission_daily(doy);

		double declin, par1, par2, ahou, hou, azmth, solrad, shwave, kelvin, arads, clrsky, ftemp, vapor, result, dayAngle, P, gamma, cosZ, sinZ, cosA, sinA, stepSize, azmthSlope,
				rslope;

		/* Unit conversion factors:
		 1 langley = 1 ly = 41840 J/m2 = 0.0168 evaporative-mm (1 [ly] / 2490 [kJ/kg heat of vaporization at about T = 10-15 C], see also Allen et al. (1998, ch. 1))
		 1 mmHg = 101.325/760 kPa = 0.1333 kPa
		 1 mile = 1609.344 m
		 0 C = 273.15 K */
		
		/* calculate solar declination */
		/* pre-Oct/11/2012 equation (unknown source): declin = .401426 *sin(6.283185 *(doy -77.) /365.); */
		dayAngle = 6.283185 * (doy - 1.) / 365.; /* Spencer (1971): dayAngle = day angle [radians] */
		declin = 0.006918 - 0.399912 * Math.cos(dayAngle) + 0.070257 * Math.sin(dayAngle) - 0.006758 * Math.cos(2. * dayAngle) + 0.000907 * Math.sin(2. * dayAngle) - 0.002697 * Math.cos(3. * dayAngle)
				+ 0.00148 * Math.sin(3. * dayAngle); /* Spencer (1971): declin = solar declination [radians] */

		/* calculate the short wave solar radiation on a clear day using an equation presented by Sellers (1965)*/
		par2 = -Math.tan(rlat) * Math.tan(declin); /* Sellers (1965), page 15, eqn. 3.3: par2 = Math.cos(H) with H = half-day length = ahou = sunset hourDefines.GTngle */
		par1 = Math.sqrt(1. - (par2*par2)); /* trigonometric identities: par1 = Math.sin(H) */
		ahou = Math.max(Math.atan2(par1,par2), 0.0); /* calculate ahou = H from trigonometric function: Math.tan(H) = Math.sin(H)/Math.cos(H) */

		if (slope != 0) {
			stepSize = (ahou / 24); /* step size is calculated by the the difference in our limits of integrations, for hou, using 0 to ahou, divided by some resolution. The best resolution size seems to be around 24*/
			azmthSlope = 6.283185 * (aspect - 180) / 360; /* convert the aspect of the slope from degrees into radians */
			rslope = 6.283185 * slope / 360; /* convert the slope from degrees into radians */
			solrad = 0; /* start with an initial solrad of zero, then begin the summation */
			for (hou = -ahou; hou <= ahou; hou += stepSize) /* sum Sellers (1965), page 35, eqn. 3.15 over the period of sunrise to sunset, h=-ahou to h=ahou */
			{
				cosZ = Math.sin(rlat) * Math.sin(declin) + Math.cos(rlat) * Math.cos(declin) * Math.cos(hou); /* calculate the current value for Math.cos(Z), Z = zenith angle of the sun, for current hour angle */
				sinZ = Math.sqrt(1. - (cosZ*cosZ)); /* calculate the current value for Math.sin(Z), Z = zenith angle of the sun, for current hour angle */
				cosA = (Math.sin(rlat) * cosZ - Math.sin(declin)) / (Math.cos(rlat) * sinZ); /* Math.cos(A) = cosine of the azimuth angle of the sun */
				sinA = (Math.cos(declin) * Math.sin(hou)) / sinZ; /* Math.sin(A) = sine of the azimuth angle of the sun */
				azmth = Math.atan2(sinA, cosA); /* determines the current azimuth angle of the sun based on the current hour angle */
				solrad += stepSize * (cosZ * Math.cos(rslope) + sinZ * Math.sin(rslope) * Math.cos(azmth - azmthSlope)); /* Sellers (1965), page 35, eqn. 3.15: Qs [langlay] = solrad = instantaneous solar radiation on a sloped surface. */
			}
		} else /* if no slope, use old equation that doesn't account for slope to save some time */
		{
			solrad = ahou * Math.sin(rlat) * Math.sin(declin) + Math.cos(rlat) * Math.cos(declin) * Math.sin(ahou); /* Sellers (1965), page 16, eqn. 3.7: Qs [langlay/day] = solrad = daily total solar radiation incident on a horizontal surface at the top of the atmosphere; factor '(mean(d)/d)^2' with (mean(d) = mean distance and d = instantaneous distance of the earth from the sun) of Seller's equation is missing here */
			solrad = solrad * 2; /* multiply solrad by two to account for both halves of the day, as eqn. 3.7 only integrates half a day */
		}
		solrad = (1440 / 6.283185) * 1.952 * solrad * transcoeff; /* 917. = S * 1440/pi with S = solar constant = 2.0 [langlay/min] (Sellers (1965), page 11) and with 1440 = min/day; however, solar constant S (Kopp et al. 2011) = 1.952 [langley/min] = 1361 [W/m2] <> Seller's value of S = 2.0 [langlay/min] = 1440 [W/m2] => instead of factor 917 (pre-Oct 11, 2012), it should be 895;factor 'transcoeff' is not in Seller's equation and drops out of the result with next line of code; */

		shwave = solrad * .0168 / transcoeff; /* shwave used in Penman (1948), eqn. 13: shwave [evaporation equivalent-mm/day] = RA = total radiation if the atmosphere were perfectly clear; Rc = Short-wave radiation from sun and sky [usually in evaporation equivalent of mm/day] ? [radiation/cm2/day,] = RA*(0.18+0.55n/N) is approximation for Rothamsted based on monthly values over the period 1931-40; with x = 0.0168 = conversion factor from [ly] to [mm] */

		/* calculate long wave radiation */
		kelvin = avgtemp + 273.15; /* kelvin = Ta = average air temperature of today [C] converted to [K] */
		ftemp = kelvin * .01;
		ftemp = ftemp * ftemp * ftemp * ftemp * 11.71 * 0.0168; /* Sellers (1965), eqn. 3.8: ftemp [mm/day] = theoretical black-body radiation at Ta [K] = Stefan-Boltzmann law = sigma*Ta^4 [W/m2] with sigma = 5.670373*10\88-8 [W/m2/K4] = 11.71*10\88-8 [ly/day/K4] (http://physics.nist.gov/cgi-bin/cuu/Value?sigma);
		 ftemp is used in Penman (1948), eqn. 13 with units = [evaporation equivalent-mm/day];
		 with unknown x = 0.201*10\88-8 (value pre-Oct 11, 2012), though assuming x = sigma * conversion factor([ly] to [mm]) = 11.71*10\88-8 [ly/day/K4] * 0.0168 [mm/ly] = 0.196728 [mm/day/K4] ca.= 0.201 ? */

		/* calculate the PET using Penman (1948) */
		vapor = svapor(avgtemp); /* Penman (1948), ea = vapor = saturation vapor pressure at air-Tave [mmHg] */
		/* pre-Oct/11/2012 equation (unknown source): arads = vapor *3010.21 / (kelvin*kelvin); with unknown: x = 3010.12 =? 5336 [mmHg*K] (Merva (1975)) * 9/5 [F/K] = 2964 [mmHg*F]; however, result virtually identical with FAO and ASCE formulations (Allen et al. 1998, 2005) --> replaced  */
		arads = 4098. * vapor / ((avgtemp + 237.3) * (avgtemp + 237.3)) * 5. / 9.; /* Allen et al. (1998, ch.3 eqn. 13) and (2005, eqn. 5): arads used in Penman (1948), eqn. 16: arads [mmHg/F] = Delta [mmHg/C] * [C/F] = slope of e:T at T=Ta = 'Slope of the Saturation Vapor Pressure-Temperature Curve' */
		clrsky = 1. - cloudcov / 100.; /* Penman (1948): n/N = clrsky = Ratio of actual/possible hours of sunshine = 1 - m/10 = 1 - fraction of sky covered by cloud */
		humid *= vapor / 100.; /* Penman (1948): ed = humid = saturation vapor pressure at dewpoint [mmHg] = relative humidity * ea */
		windsp *= 53.70; /* u2 [miles/day at 2-m above ground] = windsp [miles/h at 2-m above ground] * 24 [h/day] = windsp [m/s at 2-m above ground] * 86400 [s/day] * 1/1609.344 [miles/m] with 86400/1609 = 53.70 */
		par1 = .35 * (vapor - humid) * (1. + .0098 * windsp); /* Penman (1948), eqn. 19: par1 = Ea [mm/day] = evaporation rate from open water with ea instead of es as required in eqn. 16 */
		par2 = (1. - reflec) * shwave * (.18 + .55 * clrsky) /* Penman (1948), eqn. 13 [mm/day]: par2 = H = net radiant energy available at surface [mm/day] */
		- ftemp * (.56 - .092 * Math.sqrt(humid)) * (.10 + .90 * clrsky);
		P = 101.3 * Defines.powe((293. - 0.0065 * elev) / 293., 5.26); /* Allen et al. (1998, ch.3 eqn. 7) and (2005, eqn. 3): P [kPa] = atmospheric pressure with elev [m] */
		gamma = 0.000665 * P * 760. / 101.325 * 5. / 9.; /* Allen et al. (1998, ch.3 eqn. 8) and (2005, eqn. 4): gamma [mmHg/F] = psychrometric constant [kPa/C] * [mmHG/kPa] * [C/F] */
		result = ((arads * par2 + gamma * par1) / (arads + gamma)) / 10.;/* Penman (1948), eqn. 16: result*10 = E [mm/day] = evaporation from open water */
		/* originally and pre-Oct/11/2012, Penman (1948) gamma [mmHg/F] == 0.27*/

		return Math.max(result, 0.01);
	}

	private double svapor(double temp) {
		/*********************************************************************
		 PURPOSE: calculate the saturation vapor pressure of water
		 the clausius-clapeyron equation (hess, 1959) is used
		 HISTORY:
		 4/30/92  (SLC)

		 Hess SL (1959) Introduction to theoretical meteorology. Holt, New York.

		 INPUTS:
		 atemp - average temperature for the day

		 OUTPUT:
		 svapor - saturation vapor pressure (mm of hg)

		 *********************************************************************/
		double par1, par2;

		par1 = 1. / (temp + 273.);
		par2 = Math.log(6.11) + 5418.38 * (.00366 - par1); /*drs: par2 = ln(es [mbar]) = ln(es(at T = 273.15K) = 6.11 [mbar]) + (mean molecular mass of water vapor) * (latent heat of vaporization) / (specific gas constant) * (1/(273.15 [K]) - 1/(air temperature [K])) */

		return (Math.exp(par2) * .75);
	}

	private double transp_weighted_avg(String type, int doy) {
		/**********************************************************************

		 PURPOSE: Compute weighted average of soilwater potential to be
		 used for transpiration calculations.

		 HISTORY:
		 Original:
		 4/30/92  SLC
		 6/9/93   (SLC) check that (sum_tr_co <> 0) before dividing swp by this
		 number
		 4/10/2000 CWB -- began recoding in C
		 9/21/01   cwb -- adjusted method for determining transpiration
		 regions to reflect the new design.  removed
		 tr_reg_min and max, added n_layers and tr_regions[].
		 4-Mar-02  cwb -- moved this function after ppt enters soil.  Originally,
		 it was the first function called, so evapotransp was
		 based on swp_avg prior to wetting.  Also, set return
		 value as a pointer argument to be more consistent with
		 the rest of the code.
		 1-Oct-03  cwb -- Removed sum_tr_coeff[] requirement as it might as well
		 be calculated here and save the confusion of
		 having to keep up with it in the rest of the code.

		 INPUTS:
		 n_tr_lyrs  - number of layer regions used in weighted average
		 (typically 3, to represent shallow, mid, & deep depths)
		 to compute transpiration rate.
		 n_layers - number of soil layers
		 tr_regions - list of n_tr_lyrs elements of transp regions each
		 soil layer belongs to.
		 tr_coeff  - transpiration coefficients per layer.
		 sum_tr_coeff - sum of transpiration coefficients per layer structure.
		 swc -- current swc per layer

		 1-Oct-03 - local sumco replaces previous sum_tr_coeff[]

		 OUTPUT:
		 swp_avg - weighted average of soilwater potential and transpiration
		 coefficients
		 **********************************************************************/
		double swp_avg=0;
		int n_layers=0;
		int[] tr_regions = lyrTrRegions_Grass;
		double[] tr_coeff = lyrTranspCo_Grass;
		
		switch (type) {
		case "grass":
			n_layers=SW_Soils.getLayersInfo().n_transp_lyrs_grass;
			tr_regions = lyrTrRegions_Grass;
			tr_coeff = lyrTranspCo_Grass;
			swp_avg = p.swpot_avg_grass;
			break;
		case "shrub":
			n_layers=SW_Soils.getLayersInfo().n_transp_lyrs_shrub;
			tr_regions = lyrTrRegions_Shrub;
			tr_coeff = lyrTranspCo_Shrub;
			swp_avg = p.swpot_avg_shrub;
			break;
		case "tree":
			n_layers=SW_Soils.getLayersInfo().n_transp_lyrs_tree;
			tr_regions = lyrTrRegions_Tree;
			tr_coeff = lyrTranspCo_Tree;
			swp_avg = p.swpot_avg_tree;
			break;
		case "forb":
			n_layers=SW_Soils.getLayersInfo().n_transp_lyrs_forb;
			tr_regions = lyrTrRegions_Forb;
			tr_coeff = lyrTranspCo_Forb;
			swp_avg = p.swpot_avg_forb;
			break;
		default:
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, "SW_FLOW transp_weighted_avg type:"+type+" not Supported.");
			break;
		}
		
		int r, i;
		double swp, sumco;

		for (r = 1; r <= SW_Soils.getLayersInfo().n_transp_rgn; r++) {
			swp = sumco = 0.0;

			for (i = 0; i < n_layers; i++) {
				if (tr_regions[i] == r) {
					swp += tr_coeff[i] * SW_SOILWATER.SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, lyrSWCBulk[i], SW_Soils.getLayer(i).width, SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, SW_Model.getYear(), doy, i);
					sumco += tr_coeff[i];
				}
			}

			swp /= Defines.GT(sumco, 0.) ? sumco : 1.;

			/* use smallest weighted average of regions */
			(swp_avg) = (r == 1) ? swp : Math.min( swp, (swp_avg));
		}
		return swp_avg;
	}

	private void grass_EsT_partitioning(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate fraction of water loss from bare soil
		 evaporation and transpiration

		 HISTORY:
		 4/30/92  (SLC)
		 24-Oct-03 (cwb) changed exp(-blivelai*bsepar1) + bsepar2;
		 to exp(-blivelai);

		 INPUTS:
		 blivelai - live biomass leaf area index

		 OUTPUTS:
		 fbse - fraction of water loss from bare soil evaporation.
		 fbst - "                           " transpiration.

		 **********************************************************************/
		/* CWB- 4/00 Not sure what's the purpose of bsepar2, unless it's a
		 * fudge-factor to be played with.
		 */

		double bsemax = 0.995;

		p.soil_evap_grass = Math.exp(-SW_VegProd.getEsTpartitioning().grasses * SW_VegProd.getDailyValues().grass.lai_live_daily[doy]);

		p.soil_evap_grass = Math.min(p.soil_evap_grass, bsemax);
		p.transp_grass = 1. - (p.soil_evap_grass);
	}

	private void shrub_EsT_partitioning(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate fraction of water loss from bare soil
		 evaporation and transpiration
		 **********************************************************************/
		
		double bsemax = 0.995;

		p.soil_evap_shrub = Math.exp(-SW_VegProd.getEsTpartitioning().shrubs * SW_VegProd.getDailyValues().shrub.lai_live_daily[doy]);

		p.soil_evap_shrub = Math.min(p.soil_evap_shrub, bsemax);
		p.transp_shrub = 1. - (p.soil_evap_shrub);
	}

	private void tree_EsT_partitioning(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate fraction of water loss from bare soil
		 evaporation and transpiration

		 08/22/2011	(drs)	According to a regression based on a review by Daikoku, K., S. Hattori, A. Deguchi, Y. Aoki, M. Miyashita, K. Matsumoto, J. Akiyama, S. Iida, T. Toba, Y. Fujita, and T. Ohta. 2008. Influence of evaporation from the forest floor on evapotranspiration from the dry canopy. Hydrological Processes 22:4083-4096.
		 **********************************************************************/
		double bsemax = 0.995;

		p.soil_evap_tree = Math.exp(-SW_VegProd.getEsTpartitioning().trees * SW_VegProd.getDailyValues().tree.lai_live_daily[doy]);

		p.soil_evap_tree = Math.min(p.soil_evap_tree, bsemax);
		p.transp_tree = 1. - (p.soil_evap_tree);
	}

	private void forb_EsT_partitioning(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate fraction of water loss from bare soil
		 evaporation and transpiration

		 **********************************************************************/

		double bsemax = 0.995;

		p.soil_evap_forb = Math.exp(-SW_VegProd.getEsTpartitioning().forbs * SW_VegProd.getDailyValues().forb.lai_live_daily[doy]);

		p.soil_evap_forb = Math.min(p.soil_evap_forb, bsemax);
		p.transp_forb = 1. - (p.soil_evap_forb);
	}

	private double pot_soil_evap(String type, int doy) {
		/**********************************************************************
		 PURPOSE: Calculate potential bare soil evaporation rate.
		 See 2.11 in ELM doc.

		 HISTORY:
		 4/30/92  (SLC)
		 8/27/92  (SLC) Put in a check so that bserate cannot become
		 negative.  If total aboveground biomass (i.e.
		 litter+bimoass) is > 999., bserate=0.
		 6 Mar 02 (cwb) renamed watrate's parameters (see also SW_Site.h)
		 shift,  shift the x-value of the inflection point
		 shape,  slope of the line at the inflection point
		 inflec, y-value of the inflection point
		 range;  max y-val - min y-val at the limits
		 1-Oct-03 - cwb - removed the sumecoeff variable as it should
		 always be 1.0.  Also removed the line
		 avswp = sumswp / sumecoeff;

		 INPUTS:
		 nelyrs    - number of layers to consider in evaporation
		 sumecoeff - sum of evaporation coefficients
		 ecoeff    - array of evaporation coefficients
		 totagb    - sum of abovegraound biomass and litter
		 fbse      - fraction of water loss from bare soil evaporation
		 petday       - potential evapotranspiration rate
		 width     - array containing width of each layer.
		 swc  - array of soil water content per layer.

		 LOCAL:
		 avswp     - average soil water potential over all layers
		 evpar1    - input parameter to watrate.

		 OUTPUTS:
		 bserate   - bare soil evaporation loss rate. (cm/day)

		 FUNCTION CALLS:
		 watrate   - calculate evaporation rate.
		 swpotentl - compute soilwater potential
		 **********************************************************************/
		double totagb=0;
		double fbse=0;
		double Es_param_limit=0;
		
		switch (type) {
		case "grass":
			totagb = SW_VegProd.getDailyValues().grass.total_agb_daily[doy];
			fbse = p.soil_evap_grass;
			Es_param_limit = SW_VegProd.getEsParamLimit().grasses;
			break;
		case "shrub":
			totagb = SW_VegProd.getDailyValues().shrub.total_agb_daily[doy];
			fbse = p.soil_evap_shrub;
			Es_param_limit = SW_VegProd.getEsParamLimit().shrubs;
			break;
		case "tree":
			totagb = SW_VegProd.getDailyValues().tree.total_agb_daily[doy];
			fbse = p.soil_evap_tree;
			Es_param_limit = SW_VegProd.getEsParamLimit().trees;
			break;
		case "forb":
			totagb = SW_VegProd.getDailyValues().forb.total_agb_daily[doy];
			fbse = p.soil_evap_forb;
			Es_param_limit = SW_VegProd.getEsParamLimit().forbs;
			break;
		default:
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, "SW_FLOW pot_soil_evap type:"+type+" not Supported.");
			break;
		}
		
		double x, avswp = 0.0, sumwidth = 0.0;
		int i;

		/* get the weighted average of swp in the evap layers */
		for (i = 0; i < SW_Soils.getLayersInfo().n_layers; i++) {
			x = lyrWidths[i] * lyrEvapCo[i];
			sumwidth += x;
			avswp += x * SW_SOILWATER.SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, lyrSWCBulk[i], SW_Soils.getLayer(i).width, SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, SW_Model.getYear(), doy, i);
		}

		avswp /= sumwidth;

		/*  8/27/92 (SLC) if totagb > Es_param_limit, assume soil surface is
		 * completely covered with litter and that bare soil
		 * evaporation is inhibited.
		 */

		if (Defines.GE(totagb, Es_param_limit)) {
			return 0.;
		} else {
			return SW_Soilwat.getSoilWat().pet * watrate(avswp, SW_Soilwat.getSoilWat().pet, SW_Site.getEvaporation().xinflec, SW_Site.getEvaporation().slope, SW_Site.getEvaporation().yinflec, SW_Site.getEvaporation().range) * (1. - (totagb / Es_param_limit)) * fbse;
		}

	}

	private void pot_soil_evap_bs(int doy) {
		/**********************************************************************
		 PURPOSE: Calculate potential bare soil evaporation rate of bare ground.
		 See 2.11 in ELM doc.

		 INPUTS:
		 nelyrs    - number of layers to consider in evaporation
		 sumecoeff - sum of evaporation coefficients
		 ecoeff    - array of evaporation coefficients
		 petday    - potential evapotranspiration rate
		 width     - array containing width of each layer.
		 swc  	    - array of soil water content per layer.

		 LOCAL:
		 avswp     - average soil water potential over all layers
		 evpar1    - input parameter to watrate.

		 OUTPUTS:
		 bserate   - bare soil evaporation loss rate. (cm/day)

		 FUNCTION CALLS:
		 watrate   - calculate evaporation rate.
		 swpotentl - compute soilwater potential
		 **********************************************************************/
		double x, avswp = 0.0, sumwidth = 0.0;
		int i;
		
		/* get the weighted average of swp in the evap layers */
		for (i = 0; i < SW_Soils.getLayersInfo().n_evap_lyrs; i++) {
			x = lyrWidths[i] * lyrEvapCo[i];
			sumwidth += x;
			avswp += x * SW_SOILWATER.SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, lyrSWCBulk[i], SW_Soils.getLayer(i).width, SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, SW_Model.getYear(), doy, i);
		}

		avswp /= sumwidth;

		p.soil_evap_rate_bs = SW_Soilwat.getSoilWat().pet * watrate(avswp, SW_Soilwat.getSoilWat().pet, SW_Site.getEvaporation().xinflec, SW_Site.getEvaporation().slope, SW_Site.getEvaporation().yinflec, SW_Site.getEvaporation().range);

	}

	private double pot_transp(String type, int doy) {
		/**********************************************************************
		 PURPOSE: Calculate potential transpiration rate.
		 See 2.11 in ELM doc.

		 HISTORY:
		 4/30/92  (SLC)
		 7/1/92   (SLC) Fixed bug.  Equation for bstrate not right.
		 9/1/92   (SLC) Allow transpiration, even if biodead is zero.  This
		 was in the original model, we will compute shade if biodead
		 is greater than deadmax.  Otherwise, shadeaf = 1.0
		 6 Mar 02 (cwb) renamed watrate's parameters (see also SW_Site.h)
		 shift,  shift the x-value of the inflection point
		 shape,  slope of the line at the inflection point
		 inflec, y-value of the inflection point
		 range;  max y-val - min y-val at the limits

		 INPUTS:
		 swpavg    - weighted average of soil water potential (from
		 function "transp_weighted_avg")
		 biolive   - biomass of live
		 biodead   - biomass of dead
		 fbst      - fraction of water loss from transpiration
		 petday       - potential evapotranspiration

		 LOCAL VARIABLES:
		 shadeaf - shade affect on transpiration rate
		 scale1  - scale for shade affect
		 trpar1  - input paramter to watrate
		 deadmax - maximum biomass of dead, before shade has any affect.

		 OUTPUTS:
		 bstrate   - transpiration loss rate. (cm/day)

		 FUNCTION CALLS:
		 watrate - compute transpiration rate.
		 tanfunc - tangent function
		 **********************************************************************/
		double swpavg=0;
		double biolive=0;
		double biodead=0;
		double fbst=0;
		double shade_scale=0;
		double shade_deadmax=0;
		double shade_xinflex=0;
		double shade_slope=0;
		double shade_yinflex=0;
		double shade_range=0;
		
		switch (type) {
		case "grass":
			swpavg = p.swpot_avg_grass;
			biolive = SW_VegProd.getDailyValues().grass.biolive_daily[doy];
			biodead = SW_VegProd.getDailyValues().grass.biodead_daily[doy];
			fbst = p.transp_grass;
			shade_scale = SW_VegProd.getShade().grasses.shadeScale;
			shade_deadmax = SW_VegProd.getShade().grasses.shadeMaximalDeadBiomass;
			shade_xinflex = SW_VegProd.getShade().grasses.xinflec;
			shade_slope = SW_VegProd.getShade().grasses.slope;
			shade_yinflex = SW_VegProd.getShade().grasses.yinflec;
			shade_range = SW_VegProd.getShade().grasses.range;
			break;
		case "shrub":
			swpavg = p.swpot_avg_shrub;
			biolive = SW_VegProd.getDailyValues().shrub.biolive_daily[doy];
			biodead = SW_VegProd.getDailyValues().shrub.biodead_daily[doy];
			fbst = p.transp_shrub;
			shade_scale = SW_VegProd.getShade().shrubs.shadeScale;
			shade_deadmax = SW_VegProd.getShade().shrubs.shadeMaximalDeadBiomass;
			shade_xinflex = SW_VegProd.getShade().shrubs.xinflec;
			shade_slope = SW_VegProd.getShade().shrubs.slope;
			shade_yinflex = SW_VegProd.getShade().shrubs.yinflec;
			shade_range = SW_VegProd.getShade().shrubs.range;
			break;
		case "tree":
			swpavg = p.swpot_avg_tree;
			biolive = SW_VegProd.getDailyValues().tree.biolive_daily[doy];
			biodead = SW_VegProd.getDailyValues().tree.biodead_daily[doy];
			fbst = p.transp_tree;
			shade_scale = SW_VegProd.getShade().trees.shadeScale;
			shade_deadmax = SW_VegProd.getShade().trees.shadeMaximalDeadBiomass;
			shade_xinflex = SW_VegProd.getShade().trees.xinflec;
			shade_slope = SW_VegProd.getShade().trees.slope;
			shade_yinflex = SW_VegProd.getShade().trees.yinflec;
			shade_range = SW_VegProd.getShade().trees.range;
			break;
		case "forb":
			swpavg = p.swpot_avg_forb;
			biolive = SW_VegProd.getDailyValues().forb.biolive_daily[doy];
			biodead = SW_VegProd.getDailyValues().forb.biodead_daily[doy];
			fbst = p.transp_forb;
			shade_scale = SW_VegProd.getShade().forbs.shadeScale;
			shade_deadmax = SW_VegProd.getShade().forbs.shadeMaximalDeadBiomass;
			shade_xinflex = SW_VegProd.getShade().forbs.xinflec;
			shade_slope = SW_VegProd.getShade().forbs.slope;
			shade_yinflex = SW_VegProd.getShade().forbs.yinflec;
			shade_range = SW_VegProd.getShade().forbs.range;
			break;
		default:
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, "SW_FLOW pot_soil_evap type:"+type+" not Supported.");
			break;
		}
		
		double par1, par2, shadeaf;

		if (Defines.LE(biolive, 0.)) {
			return 0.;

		} else {
			if (Defines.GE(biodead, shade_deadmax)) {
				par1 = Defines.tanfunc(biolive, shade_xinflex, shade_yinflex, shade_range, shade_slope);
				par2 = Defines.tanfunc(biodead, shade_xinflex, shade_yinflex, shade_range, shade_slope);
				shadeaf = (par1 / par2) * (1.0 - shade_scale) + shade_scale;
				shadeaf = Math.min(shadeaf, 1.0);
			} else {
				shadeaf = 1.0;
			}

			return watrate(swpavg, SW_Soilwat.getSoilWat().pet, SW_Site.getTranspiration().xinflec, SW_Site.getTranspiration().slope, SW_Site.getTranspiration().yinflec, SW_Site.getTranspiration().range) * shadeaf * SW_Soilwat.getSoilWat().pet * fbst;
		}
	}

	private double watrate(double swp, double petday, double shift, double shape, double inflec, double range) {
		/**********************************************************************
		 PURPOSE: Calculate the evaporation (or transpiration) rate, as
		 a function of potential evapotranspiration and soil
		 water potential. The ratio of evaporation (transpiration)
		 rate to PET is inversely proportional to soil water
		 poential (see Fig2.5a,b, pp.39, "Abiotic Section of ELM")

		 HISTORY:
		 4/30/92  (SLC)
		 6 Mar 02 (cwb) - Changed arguments from a/b/c to shift,shape,
		 inflec, range because I finally found the source
		 for tanfunc.

		 INPUTS:
		 swp - soil water potential (-bars)
		 petday - potential evapotranspiration rate for the day.
		 a   - equation parameter (relative to transpiration or evapor. rate)
		 b   - equation parameter (relative to transpiration or evapor. rate)
		 (usually b=.06 for evaporation, and b=.07 for transpiration)

		 OUTPUT:
		 watrate - rate of evaporation (or transpiration) from the
		 soil.

		 **********************************************************************/

		double par1, par2, result;

		if (Defines.LT(petday, .2))
			par1 = 3.0;
		else if (Defines.LT(petday, .4))
			par1 = (.4 - petday) * -10. + 5.;
		else if (Defines.LT(petday, .6))
			par1 = (.6 - petday) * -15. + 8.;
		else
			par1 = 8.;

		par2 = shift - swp;

		result = Defines.tanfunc(par2, par1, inflec, range, shape);

		return (Math.min( Math.max( result, 0.0), 1.0));

	}

	private void evap_fromSurface() {
		/**********************************************************************
		 PURPOSE: Evaporate water from surface water pool, i.e., intercepted water (tree, shrub, grass, litter) or standingWater
		 call once for all pools

		 INPUTS:

		 OUTPUTS: (changes)
		 water_pool	- pool of surface water minus evaporated water
		 evap_rate	- actual evaporation from this pool
		 aet			- aet + evaporated water
		 **********************************************************************/
		if (Defines.GT(tree_h2o_qum[Defines.Today], p.surface_evap_tree_rate)) { /* potential rate is smaller than available water -> entire potential is evaporated */
			tree_h2o_qum[Defines.Today] -= p.surface_evap_tree_rate;
			SW_Soilwat.getSoilWat().aet += p.surface_evap_tree_rate;
		} else { /* potential rate is larger than available water -> entire pool is evaporated */
			p.surface_evap_tree_rate = tree_h2o_qum[Defines.Today];
			SW_Soilwat.getSoilWat().aet += tree_h2o_qum[Defines.Today];
			tree_h2o_qum[Defines.Today] = 0.;
		}
		if (Defines.GT(shrub_h2o_qum[Defines.Today], p.surface_evap_shrub_rate)) { /* potential rate is smaller than available water -> entire potential is evaporated */
			shrub_h2o_qum[Defines.Today] -= p.surface_evap_shrub_rate;
			SW_Soilwat.getSoilWat().aet += p.surface_evap_shrub_rate;
		} else { /* potential rate is larger than available water -> entire pool is evaporated */
			p.surface_evap_shrub_rate = shrub_h2o_qum[Defines.Today];
			SW_Soilwat.getSoilWat().aet += shrub_h2o_qum[Defines.Today];
			shrub_h2o_qum[Defines.Today] = 0.;
		}
		if (Defines.GT(forb_h2o_qum[Defines.Today], p.surface_evap_forb_rate)) { /* potential rate is smaller than available water -> entire potential is evaporated */
			forb_h2o_qum[Defines.Today] -= p.surface_evap_forb_rate;
			SW_Soilwat.getSoilWat().aet += p.surface_evap_forb_rate;
		} else { /* potential rate is larger than available water -> entire pool is evaporated */
			p.surface_evap_forb_rate = forb_h2o_qum[Defines.Today];
			SW_Soilwat.getSoilWat().aet += forb_h2o_qum[Defines.Today];
			forb_h2o_qum[Defines.Today] = 0.;
		}
		if (Defines.GT(grass_h2o_qum[Defines.Today], p.surface_evap_grass_rate)) { /* potential rate is smaller than available water -> entire potential is evaporated */
			grass_h2o_qum[Defines.Today] -= p.surface_evap_grass_rate;
			SW_Soilwat.getSoilWat().aet += p.surface_evap_grass_rate;
		} else { /* potential rate is larger than available water -> entire pool is evaporated */
			p.surface_evap_grass_rate = grass_h2o_qum[Defines.Today];
			SW_Soilwat.getSoilWat().aet += grass_h2o_qum[Defines.Today];
			grass_h2o_qum[Defines.Today] = 0.;
		}
		if (Defines.GT(litter_h2o_qum[Defines.Today], p.surface_evap_litter_rate)) { /* potential rate is smaller than available water -> entire potential is evaporated */
			litter_h2o_qum[Defines.Today] -= p.surface_evap_litter_rate;
			SW_Soilwat.getSoilWat().aet += p.surface_evap_litter_rate;
		} else { /* potential rate is larger than available water -> entire pool is evaporated */
			p.surface_evap_litter_rate = litter_h2o_qum[Defines.Today];
			SW_Soilwat.getSoilWat().aet += litter_h2o_qum[Defines.Today];
			litter_h2o_qum[Defines.Today] = 0.;
		}
		if (Defines.GT(standingWater[Defines.Today], p.surface_evap_standingWater_rate)) { /* potential rate is smaller than available water -> entire potential is evaporated */
			standingWater[Defines.Today] -= p.surface_evap_standingWater_rate;
			SW_Soilwat.getSoilWat().aet += p.surface_evap_standingWater_rate;
		} else { /* potential rate is larger than available water -> entire pool is evaporated */
			p.surface_evap_standingWater_rate = standingWater[Defines.Today];
			SW_Soilwat.getSoilWat().aet += standingWater[Defines.Today];
			standingWater[Defines.Today] = 0.;
		}
	}

	private void remove_from_soil(double[] qty, int nlyrs, double[] coeff, double rate, double[] swcmin) {
		/**********************************************************************
		 PURPOSE: Remove water from the soil.  This replaces earlier versions'
		 call to separate functions for evaporation and transpiration
		 which did exactly the same thing but simply looked
		 different.  I prefer using one function over two to avoid
		 possible errors in the same transaction.

		 See Eqns. 2.12 - 2.18 in "Abiotic Section of ELM".

		 HISTORY: 10 Jan 2002 - cwb - replaced two previous functions with
		 this one.
		 12 Jan 2002 - cwb - added aet arg.
		 4 Dec 2002  - cwb - Adding STEPWAT code uncovered possible
		 div/0 error. If no transp coeffs, return.
		 INPUTS:
		 nlyrs  - number of layers considered in water removal
		 coeff - coefficients of removal for removal layers, either
		 evap_coeff[] or transp_coeff[].
		 rate - removal rate, either soil_evap_rate or soil_transp_rate.
		 swcmin - lower limit on soilwater content (per layer).

		 OUTPUTS:
		 swc  - soil water content adjusted after evaporation
		 qty - removal quantity from each layer, evap or transp.
		 aet -

		 FUNCTION CALLS:
		 swpotentl - compute soilwater potential of the layer.
		 **********************************************************************/

		int i;
		double[] swpfrac = new double[Defines.MAX_LAYERS];
		double sumswp = 0.0, swc_avail, q;

		for (i = 0; i < nlyrs; i++) {
			swpfrac[i] = coeff[i] / SW_SOILWATER.SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, lyrSWCBulk[i], SW_Soils.getLayer(i).width, SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, SW_Model.getYear(), SW_Model.getDOY(), i);
			sumswp += swpfrac[i];
		}

		if (Defines.isZero(sumswp))
			return;

		for (i = 0; i < nlyrs; i++) {
			if (stValues.lyrFrozen[i] == 0) {
				q = (swpfrac[i] / sumswp) * rate;
				swc_avail = Math.max(0., lyrSWCBulk[i] - swcmin[i]);
				qty[i] = Math.min( q, swc_avail);
				lyrSWCBulk[i] -= qty[i];
				SW_Soilwat.getSoilWat().aet += qty[i];
			}
		}
		if(debug) {
			String temp = "";
			for (i = 0; i < SW_Soils.getLayersInfo().n_layers; i++) {
				temp+=String.format(" %7f", lyrSWCBulk[i]);
			}
			System.out.println(String.format("%26s%s", "remove_from_soil: ",temp));
		}
	}

	private void infiltrate_water_low() {
		/**********************************************************************
		 PURPOSE:Calculate soilwater drainage for low soil water conditions
		 See Equation 2.9 in ELM doc.

		 HISTORY:
		 4/30/92  (SLC)
		 7/2/92   (fixed bug.  equation for drainlw needed fixing)
		 8/13/92 (SLC) Changed call to function which checks lower bound
		 on soilwater content.  REplaced call to "chkzero" with
		 the function "getdiff".
		 - lower bound is used in place of zero as lower bound
		 here.  Old code used 0.cm water as a lower bound in
		 low water drainage.
		 9/22/01 - (cwb) replaced tr_reg_max[] with transp_rgn[]
		 see INPUTS
		 1/14/02 - (cwb) fixed off by one error in loop.
		 6-Oct-03  (cwb) removed condition disallowing gravitational
		 drainage from transpiration region 1.

		 INPUTS:
		 drain - drainage from each layer
		 ndeeplyr - bottom layer to stop at for drainage
		 transp_rgn - array of transpiration regions of each layer
		 sdrainpar - slow drainage parameter
		 swcfc   - soilwater content at field capacity
		 swcwp   - soilwater content at wilting point
		 swc  - soil water content adjusted by drainage
		 drain - drainage from each soil water layer

		 OUTPUTS:
		 swc  - soil water content adjusted by drainage
		 drain - drainage from each soil water layer
		 drainout - added low drainout (to already calculated high drainout)
		 **********************************************************************/
		int nlyrs = SW_Soils.getLayersInfo().n_layers;	
		
		int i;
		int j;
		double drainlw = 0.0, swc_avail, drainpot, push;
		double[] d = new double[nlyrs];

		for (i = 0; i < nlyrs; i++) {
			if (stValues.lyrFrozen[i] == 0 && stValues.lyrFrozen[i + 1] == 0) {
				/* calculate potential unsaturated percolation */
				if (Defines.LE(lyrSWCBulk[i], lyrSWCBulk_Mins[i])) { /* in original code was !GT(swc[i], swcwp[i]) equivalent to LE(swc[i], swcwp[i]), but then water is drained to swcmin nevertheless - maybe should be LE(swc[i], swcmin[i]) */
					d[i] = 0.;
				} else {
					swc_avail = Math.max(0., lyrSWCBulk[i] - lyrSWCBulk_Mins[i]);
					drainpot = Defines.GT(lyrSWCBulk[i], lyrSWCBulk_FieldCaps[i]) ? SW_Site.getDrainage().slow_drain_coeff : SW_Site.getDrainage().slow_drain_coeff * Math.exp((lyrSWCBulk[i] - lyrSWCBulk_FieldCaps[i]) * Defines.SLOW_DRAIN_DEPTH / lyrWidths[i]);
					d[i] = (1. - lyrImpermeability[i]) * Math.min(swc_avail, drainpot);
				}
				lyrDrain[i] += d[i];

				if (i < nlyrs - 1) { /* percolate up to next-to-last layer */
					lyrSWCBulk[i + 1] += d[i];
					lyrSWCBulk[i] -= d[i];
				} else { /* percolate last layer */
					drainlw = Math.max( d[i], 0.0);
					drainout += drainlw;
					lyrSWCBulk[i] -= drainlw;
				}
			}
		}

		/* adjust (i.e., push water upwards) if water content of a layer is now above saturated water content */
		for (j = nlyrs; j >= 0; j--) {
			if (stValues.lyrFrozen[i] == 0) {
				if (Defines.GT(lyrSWCBulk[j], lyrSWCBulk_Saturated[j])) {
					push = lyrSWCBulk[j] - lyrSWCBulk_Saturated[j];
					lyrSWCBulk[j] -= push;
					if (j > 0) {
						lyrDrain[j - 1] -= push;
						lyrSWCBulk[j - 1] += push;
					} else {
						standingWater[Defines.Today] += push;
					}
				}
			}
		}
		if(debug) {
			String temp = "";
			for (i = 0; i < SW_Soils.getLayersInfo().n_layers; i++) {
				temp+=String.format(" %7f", lyrSWCBulk[i]);
			}
			System.out.println(String.format("%26s%s", "infiltrate_water_low: ",temp));
		}
	}

	private void hydraulic_redistribution(double[] lyrRootCo, double[] hydred, double maxCondroot, double swp50, double shapeCond,
			double scale) {
		/**********************************************************************
		 PURPOSE:Calculate hydraulic redistribution according to Ryel, Ryel R, Caldwell, Caldwell M, Yoder, Yoder C, Or, Or D, Leffler, Leffler A. 2002. Hydraulic redistribution in a stand of Artemisia tridentata: evaluation of benefits to transpiration assessed with a simulation model. Oecologia 130: 173-184.

		 HISTORY:
		 10/19/2010 (drs)
		 11/13/2010 (drs) limited water extraction for hydred to swp above wilting point
		 03/23/2012 (drs) excluded hydraulic redistribution from top soil layer (assuming that this layer is <= 5 cm deep)

		 INPUTS:
		 swc  - soil water content
		 lyrRootCo - fraction of active roots in layer i
		 nlyrs  - number of soil layers
		 maxCondroot - maximum radial soil-root conductance of the entire active root system for water (cm/-bar/day)
		 swp50 - soil water potential (-bar) where conductance is reduced by 50%
		 shapeCond - shaping parameter for the empirical relationship from van Genuchten to model relative soil-root conductance for water
		 scale - fraction of vegetation type to scale hydred

		 OUTPUTS:
		 swc  - soil water content adjusted by hydraulic redistribution
		 hydred - hydraulic redistribtion for each soil water layer (cm/day/layer)

		 **********************************************************************/
		int nlyrs = SW_Soils.getLayersInfo().n_layers;
		int i, j;
		double[] swp = new double[nlyrs], swpwp = new double[nlyrs], relCondroot = new double[nlyrs];
		double[][] hydredmat = new double[nlyrs][nlyrs];
		double Rx, swa, hydred_sum;

		hydred[0] = 0.; /* no hydred in top layer */

		for (i = 0; i < nlyrs; i++) {
			swp[i] = SW_SOILWATER.SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, lyrSWCBulk[i], SW_Soils.getLayer(i).width, SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, SW_Model.getYear(), SW_Model.getDOY(), i);
			relCondroot[i] = Math.min( 1., Math.max(0., 1./(1. + Defines.powe(swp[i]/swp50, shapeCond) ) ) );
			swpwp[i] = SW_SOILWATER.SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, lyrSWCBulk_Wiltpts[i], SW_Soils.getLayer(i).width, SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, SW_Model.getYear(), SW_Model.getDOY(), i);
			hydredmat[0][i] = hydredmat[i][0] = 0.; /* no hydred in top layer */
		}

		for (i = 1; i < nlyrs; i++) {
			hydred[i] = hydredmat[i][i] = 0.; /* no hydred within any layer */

			for (j = i + 1; j < nlyrs; j++) {

				if (Defines.LT(swp[i], swpwp[i]) || Defines.LT(swp[j], swpwp[j])) { /* hydred occurs only if source layer's swp is above wilting point */
					if (Defines.GT(lyrSWCBulk[i], lyrSWCBulk[j])) {
						Rx = lyrRootCo[i];
					} else {
						Rx = lyrRootCo[j];
					}

					hydredmat[i][j] = maxCondroot * 10. / 24. * (swp[j] - swp[i]) * Math.max(relCondroot[i], relCondroot[j]) * (lyrRootCo[i] * lyrRootCo[j] / (1. - Rx)); /* assuming a 10-hour night */
					hydredmat[j][i] = -hydredmat[i][j];
				} else {
					hydredmat[i][j] = hydredmat[j][i] = 0.;
				}
			}
		}

		for (i = 0; i < nlyrs; i++) { /* total hydred from layer i cannot extract more than its swa */
			hydred_sum = 0.;
			for (j = 0; j < nlyrs; j++) {
				hydred_sum += hydredmat[i][j];
			}

			swa = Math.max( 0., lyrSWCBulk[i] - lyrSWCBulk_Wiltpts[i] );
			if (Defines.LT(hydred_sum, 0.) && Defines.GT( -hydred_sum, swa)) {
				for (j = 0; j < nlyrs; j++) {
					hydredmat[i][j] *= (swa / -hydred_sum);
					hydredmat[j][i] *= (swa / -hydred_sum);
				}
			}
		}

		for (i = 0; i < nlyrs; i++) {
			if (stValues.lyrFrozen[i] == 0) {
				for (j = 0; j < nlyrs; j++) {
					hydred[i] += hydredmat[i][j] * scale;
				}
				lyrSWCBulk[i] += hydred[i];
			}
		}
		if(debug) {
			String temp = "";
			for (i = 0; i < SW_Soils.getLayersInfo().n_layers; i++) {
				if(stValues.lyrFrozen[i] == 0)
					temp+=String.format(" %7f", lyrSWCBulk[i]);
			}
			System.out.println(String.format("%26s%s", "hydraulic_redistribution: ",temp));
		}
	}

	/*
	 *PURPOSE: Initialize soil temperature regression values, only needs to be called once (ie the first time the soil_temperature function is called).  this is not included in the header file since it is NOT an external function
	 *
	 *HISTORY:
	 *05/31/2012 (DLM) initial coding
	 *
	 *INPUTS: they are all defined in the soil_temperature function, so go look there to see their meaning as it would be redundant to explain them all here as well.
	 *
	 *OUTPUT: none, but places the regression values in stValues struct for use in the soil_temperature function later
	 */
	
	private void soil_temperature_init() {
		
		double[] bDensity = lyrbDensity;
		double[] width = lyrWidths;
		double[] oldsTemp = lyroldsTemp;
		int nlyrs = SW_Soils.getLayersInfo().n_layers;
		double[] fc = lyrSWCBulk_FieldCaps;
		double[] wp = lyrSWCBulk_Wiltpts;
		double deltaX = SW_Site.getSoilTemperature().stDeltaX;
		double theMaxDepth = SW_Site.getSoilTemperature().stMaxDepth;
		double meanAirTemp = SW_Site.getSoilTemperature().meanAirTemp;
		int nRgr = SW_Site.get_stNRGR();
		
		LogFileIn f = LogFileIn.getInstance();
		// local vars
		int i, k;
		int[] equal_x1_x2 = new int[3];
		equal_x1_x2[0] = 1;
		equal_x1_x2[1] = 1;
		equal_x1_x2[2] = 1;
	
		double acc = 0.0;

		soil_temp_init = true; // make this value 1 to make sure that this function isn't called more than once... (b/c it doesn't need to be)

		for (i = 0; i < nlyrs; i++) {
			acc += width[i];
			stValues.depths[i] = acc;
		}

		for (i = 0; i < nRgr + 1; i++)
			stValues.depthsR[i] = ((deltaX * i) + deltaX);

		// if there's less then 2 lyrs of soil, or the max layer depth < 30 cm the function quits (& prints out an error message) so it doesn't blow up later...
		if ((nlyrs < 2) || Defines.LT(stValues.depths[nlyrs - 1], deltaX + deltaX)) {
			if (!soil_temp_error) { // if the error hasn't been reported yet... print an error to the stderr and one to the logfile
				f.LogError(LogMode.NOTE, String.format("\nSOIL_TEMP FUNCTION ERROR: (there needs to be >= 2 soil layers, with a maximum combined depth of >= %5.2f cm)... soil temperature will NOT be calculated\n", (deltaX + deltaX)) );
				soil_temp_error = true;
			}
			return; // exits the function
		}

		acc = deltaX;
		k = 0;
		// linear regression time complexity of this should be something like O(k * nlyrs).  might be able to do it faster... but would also be a lot more code & would require a rewrite (shouldn't matter anyways, because this function is only called once)
		while (Defines.LE(acc, stValues.depths[nlyrs - 1])) {
			Defines.st_getBounds(equal_x1_x2, nlyrs, acc, stValues.depths);

			i = -1;
			if (equal_x1_x2[1] == i) { // sets the values to the first layer of soils values, since theres nothing else that can be done... fc * wp must be scaled appropriately
				stValues.fcR[k] = (fc[0] / width[0]) * deltaX; // all the division and multiplication is to make sure that the regressions are scaled appropriately, since the widths of the layers & the layers of the regressions, may not be the same
				stValues.wpR[k] = (wp[0] / width[0]) * deltaX;
				// stValues.oldsTempR[k] = regression(0.0, stValues.depths[0], T1, oldsTemp[0], acc); // regression using the temp at the top of the soil, commented out b/c it's giving worse results
				stValues.oldsTempR[k] = oldsTemp[0]; // no scaling is necessary with temperature & bulk density
				stValues.bDensityR[k] = bDensity[0];
			} else if ((equal_x1_x2[1] == equal_x1_x2[2]) || (equal_x1_x2[0] != 0)) { // sets the values to the layers values, since x1 and x2 are the same, no regression is necessary
				stValues.fcR[k] = (fc[equal_x1_x2[1]] / width[equal_x1_x2[1]]) * deltaX;
				stValues.wpR[k] = (wp[equal_x1_x2[1]] / width[equal_x1_x2[1]]) * deltaX;
				stValues.oldsTempR[k] = oldsTemp[equal_x1_x2[1]];
				stValues.bDensityR[k] = bDensity[equal_x1_x2[1]];
			} else { // double regression( double equal_x1_x2[1], double x2, double y1, double y2, double deltaX ), located in generic.c
				stValues.fcR[k] = Defines.regression(stValues.depths[equal_x1_x2[1]], stValues.depths[equal_x1_x2[2]], (fc[equal_x1_x2[1]] / width[equal_x1_x2[1]]) * deltaX, (fc[equal_x1_x2[2]] / width[equal_x1_x2[2]]) * deltaX, acc);
				stValues.wpR[k] = Defines.regression(stValues.depths[equal_x1_x2[1]], stValues.depths[equal_x1_x2[2]], (wp[equal_x1_x2[1]] / width[equal_x1_x2[1]]) * deltaX, (wp[equal_x1_x2[2]] / width[equal_x1_x2[2]]) * deltaX, acc);
				stValues.oldsTempR[k] = Defines.regression(stValues.depths[equal_x1_x2[1]], stValues.depths[equal_x1_x2[2]], oldsTemp[equal_x1_x2[1]], oldsTemp[equal_x1_x2[2]], acc);
				stValues.bDensityR[k] = Defines.regression(stValues.depths[equal_x1_x2[1]], stValues.depths[equal_x1_x2[2]], bDensity[equal_x1_x2[1]], bDensity[equal_x1_x2[2]], acc);
			}

			if (equal_x1_x2[0] != 0)
				equal_x1_x2[2] = equal_x1_x2[1];
			stValues.x1BoundsR[k] = equal_x1_x2[1];
			stValues.x2BoundsR[k] = equal_x1_x2[2];

			k++;
			acc += deltaX;
		}

		// this next chunk is commented out... the code is kept here though if if we want to change back to extrapolating the rest of the regression values

		// to fill the rest of the regression values, simply use the last two values since there is no more actual data to go off of
		// if k is < 2 this code will blow up... but that should never happen since the function quits if there isn't enough soil data earlier
		/*for( i=k; i < nRgr; i++) {
		 stValues.wpR[i] = regression(stValues.depthsR[i - 2], stValues.depthsR[i - 1], stValues.wpR[i - 2], stValues.wpR[i - 1], stValues.depthsR[i]);
		 stValues.fcR[i] = regression(stValues.depthsR[i - 2], stValues.depthsR[i - 1], stValues.fcR[i - 2], stValues.fcR[i - 1], stValues.depthsR[i]);
		 stValues.bDensityR[i] = regression(stValues.depthsR[i - 2], stValues.depthsR[i - 1], stValues.bDensityR[i - 2], stValues.bDensityR[i - 1], stValues.depthsR[i]);
		 }

		 // getting the average for a regression...
		 for( i=0; i < nlyrs; i++) {
		 wpAverage += wp[i] / width[i];
		 fcAverage += fc[i] / width[i];
		 bDensityAverage += bDensity[i];
		 }
		 wpAverage = deltaX * (wpAverage / (nlyrs + 0.0));
		 fcAverage = deltaX * (fcAverage / (nlyrs + 0.0));
		 bDensityAverage = (bDensityAverage / (nlyrs + 0.0));

		 // if the values are too small, we reset them to the average for the regression... it's a safeguard
		 for( i=k; i < nRgr; i++ ) {
		 if(LT(stValues.wpR[i], 1))
		 stValues.wpR[i] = wpAverage;
		 if(LT(stValues.fcR[i], 2))
		 stValues.fcR[i] = fcAverage;
		 stValues.bDensityR[i] = bDensityAverage; // just set the bulk density to the average, it seems to be a better approximation...
		 }*/

		// just use the last soil layers values...
		for (i = k; i < nRgr; i++) {
			stValues.wpR[i] = deltaX * (wp[nlyrs - 1] / width[nlyrs - 1]);
			stValues.fcR[i] = deltaX * (fc[nlyrs - 1] / width[nlyrs - 1]);
			stValues.bDensityR[i] = bDensity[nlyrs - 1];
		}

		if (k < nRgr) //was k < 11
			stValues.oldsTempR[k] = Defines.regression(stValues.depths[nlyrs - 1], theMaxDepth, oldsTemp[nlyrs - 1], meanAirTemp, stValues.depthsR[k]); // to give a slightly better temp approximation
		for (i = k + 1; i < nRgr; i++) {
			stValues.oldsTempR[i] = Defines.regression(stValues.depthsR[i - 1], theMaxDepth, stValues.oldsTempR[i - 1], meanAirTemp, stValues.depthsR[i]); // we do temperature differently, since we already have the temperature for the last layer of soil
		}

		stValues.oldsTempR[nRgr] = meanAirTemp; // the soil temp at the last layer of the regression is equal_x1_x2[0] to the meanAirTemp, this is constant so it's the same for yesterdays temp & todays temp

		// getting all the xBounds values for later use in the soil_temperature function...
		for (i = 0; i < nlyrs; i++) {
			Defines.st_getBounds(equal_x1_x2, nRgr + 1, stValues.depths[i], stValues.depthsR);
			if (equal_x1_x2[0] != 0)
				equal_x1_x2[2] = equal_x1_x2[1];
			stValues.x1Bounds[i] = equal_x1_x2[1];
			stValues.x2Bounds[i] = equal_x1_x2[2];
		}
	}

	/*
	 PURPOSE: Calculate soil temperature for each layer as described in Parton 1978, ch. 2.2.2 Temperature-profile Submodel, regression values are gotten from a mixture of interpolation & extrapolation

	 *NOTE* There will be some degree of error because the original equation is written for soil layers of 15 cm.  if soil layers aren't all 15 cm then linear regressions are used to estimate the values (error should be relatively small though).
	 *NOTE* Function might not work correctly if the maxDepth of the soil is > 180 cm, since Parton's equation goes only to 180 cm
	 *NOTE* Function will run if maxLyrDepth > maxDepth of the equation, but the results might be slightly off...

	 HISTORY:
	 05/24/2012 (DLM) initial coding, still need to add to header file, handle if the layer height is > 15 cm properly, & test
	 05/25/2012 (DLM) added all this 'fun' crazy linear regression stuff
	 05/29/2012 (DLM) still working on the function, linear regression stuff should work now.  needs testing
	 05/30/2012 (DLM) got rid of nasty segmentation fault error, also tested math seems correct after checking by hand.  added the ability to change the value of deltaX
	 05/30/2012 (DLM) added # of lyrs check & maxdepth check at the beginning to make sure code doesn't blow up... if there isn't enough lyrs (ie < 2) or the maxdepth is too little (ie < deltaX * 2), the function quits out and reports an error to the user
	 05/31/2012 (DLM) added theMaxDepth variable to allow the changing of the maxdepth of the equation, also now stores most regression data in a structure to reduce redundant regression calculations & speeds things up
	 06/01/2012 (DLM) changed deltaT variable from hours to seconds, also changed some of the regression calculations so that swc, fc, & wp regressions are scaled properly... results are actually starting to look usable!
	 06/13/2012 (DLM) no longer extrapolating values for regression layers that are out of the bounds of the soil layers... instead they are now set to the last soil layers values.  extrapolating code is still in the function and can be commented out and used if wishing to go back to extrapolating the values...
	 03/28/2013 (clk) added a check to see if the soil was freezing/thawing and adjusted the soil temperature correctly during this phase change. If the temperature was in this area, also needed to re run soil_temperature_init on next call because you need to also change the regression temperatures to match the change in soil temperature.

	 INPUTS:
	 airTemp - the average daily air temperature in celsius
	 pet - the potential evapotranspiration rate
	 aet - the actual evapotranspiration rate
	 biomass - the standing-crop biomass
	 swc - soil water content
	 bDensity - bulk density of the soil layers
	 width - width of layers
	 oldsTemp - soil layer temperatures from the previous day in celsius
	 nlyrs - number of soil layers, must be greater than 1 or the function won't work right
	 fc - field capacity for each soil layer
	 wp - wilting point for each soil layer
	 bmLimiter - biomass limiter constant (300 g/m^2)
	 t1Params - constants for the avg temp at the top of soil equation (15, -4, 600) there is 3 of them
	 csParams - constants for the soil thermal conductivity equation (0.00070, 0.00030) there is 2 of them
	 shParam - constant for the specific heat capacity equation (0.18)
	 snowpack - the amount of snow on the ground
	 meanAirTemp - the avg air temperature for the month in celsius
	 deltaX - the distance between profile points (default is 15 from Parton's equation, wouldn't recommend changing the value from that).  180 must be evenly divisible by this number.
	 theMaxDepth - the lower bound of the equation (default is 180 from Parton's equation, wouldn't recommend changing the value from that).
	 nRgr - the number of regressions (1 extra value is needed for the sTempR and oldsTempR for the last layer

	 OUTPUT:
	 sTemp - soil layer temperatures in celsius
	*/

	private void soil_temperature(double biomass) {
		
		double airTemp = SW_Weather.getNow().temp_avg[Defines.Today];
		double pet = SW_Soilwat.getSoilWat().pet;
		double aet = SW_Soilwat.getSoilWat().aet;
		double[] swc = lyrSWCBulk;
		double[] bDensity = lyrbDensity;
		double[] width = lyrWidths;
		double[] oldsTemp = lyroldsTemp;
		double[] sTemp = lyrsTemp;
		int nlyrs = SW_Soils.getLayersInfo().n_layers;
		double[] fc = lyrSWCBulk_FieldCaps;
		double[] wp = lyrSWCBulk_Wiltpts;
		double bmLimiter = SW_Site.getSoilTemperature().bmLimiter;
		double t1Param1 = SW_Site.getSoilTemperature().t1Param1;
		double t1Param2 = SW_Site.getSoilTemperature().t1Param2;
		double t1Param3 = SW_Site.getSoilTemperature().t1Param3;
		double csParam1 = SW_Site.getSoilTemperature().csParam1;
		double csParam2 = SW_Site.getSoilTemperature().csParam2;
		double shParam = SW_Site.getSoilTemperature().shParam;
		double snowpack = SW_Soilwat.getSoilWat().snowpack[Defines.Today];
		double meanAirTemp = SW_Site.getSoilTemperature().meanAirTemp;
		double deltaX = SW_Site.getSoilTemperature().stDeltaX;
		int nRgr = SW_Site.get_stNRGR();
		
		LogFileIn f = LogFileIn.getInstance();
		boolean toDebug = false;
		int i, j, k, x1 = 1, x2 = 1; 
		double T1, cs, sh, sm, pe, deltaT, deltaTemp, tc, fH2O, fp, part1, part2, acc, maxLyrDepth;
		double[] swcR = new double[nRgr], sTempR = new double[nRgr + 1], sFusionPool = new double[nlyrs];
		
		
		/* local variables explained: 

		 toDebug - 1 to print out debug messages & then exit the program after completing the function, 0 to not.  default is 0.
		 T1 - the average daily temperature at the top of the soil in celsius
		 sm - volumetric soil-water content
		 pe - ratio of the difference between volumetric soil-water content & soil-water content
		 at the wilting point to the difference between soil water content at field capacity &
		 soil-water content at wilting point.
		 cs - soil thermal conductivity
		 sh - specific heat capacity
		 deltaT - time step (24 hr)
		 deltaTemp - the change in temperature for each day
		 tc - correction factor for fusion pool calculation
		 fH2O - fusion energy of water
		 fp - freezing point of water in soil
		 depths[nlyrs] - the depths of each layer of soil, calculated in the function
		 sFusionPool[] - the calculated fusion pool for each soil layer
		 swcR[], sTempR[] - anything with a R at the end of the variable name stands for the regression of that array
		 */

		deltaT = 86400.0; // the # of seconds in a day... (24 hrs * 60 mins/hr * 60 sec/min = 86400 seconds)
		tc = 0.02; 		// this correction value is given by Eitzinger 2000
		fp = -1.00;		// this freezing point value was also used in Eitzinger 2000
		fH2O = 80;		// this fusion enegry was also given in Eitzinger 2000

		// calculating T1, the average daily air temperature at the top of the soil
		if (Defines.LE(biomass, bmLimiter)) { // bmLimiter = 300
			T1 = airTemp + (t1Param1 * pet * (1. - ((aet / pet) * (1. - (biomass / bmLimiter))))); // t1Param1 = 15; math is correct
			if (toDebug)
				f.LogError(LogMode.NOTE, String.format("\nT1 = %5.4f + (%5.4f * %5.4f * (1 - ((%5.4f / %5.4f) * (1 - (%5.4f / %5.4f))) ) )", airTemp, t1Param1, pet, aet, pet, biomass, bmLimiter));	
		} else {
			T1 = airTemp + ((t1Param2 * (biomass - bmLimiter)) / t1Param3); // t1Param2 = -4, t1Param3 = 600; math is correct
			if (toDebug)
				f.LogError(LogMode.NOTE, String.format("\nT1 = %5.4f + ((%5.4f * (%5.4f - %5.4f)) / %5.4f)", airTemp, t1Param2, biomass, bmLimiter, t1Param3));
		}

		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\nAirTemp : %5.4f pet : %5.4f aet : %5.4f biomass : %5.4f bmLimiter : %5.4f", airTemp, pet, aet, biomass, bmLimiter));
		if (Defines.GT(snowpack, 0.0)) { // if there is snow on the ground, then T1 is simply set to -2
			T1 = -2.0;
			if (toDebug)
				f.LogError(LogMode.NOTE, String.format("\nThere is snow on the ground, T1 set to -2\n"));
		}

		if (!soil_temp_init)
			soil_temperature_init();

		if (!fusion_pool_init) {
			for (i = 0; i < nlyrs; i++) {
				stValues.oldsFusionPool[i] = 0.00;	// sets the inital fusion pool to zero
				if (Defines.LE(oldsTemp[i],fp))		// determines if the current layer is frozen or not
					stValues.lyrFrozen[i] = 1;
				else
					stValues.lyrFrozen[i] = 0;
			}
			fusion_pool_init = true;
		}

		if (soil_temp_error) // if there is an error found in the soil_temperature_init function, return so that the function doesn't blow up later
			return;

		maxLyrDepth = stValues.depths[nlyrs - 1];

		k = 0; // k keeps track of which layer of the regression we are on...
		acc = deltaX;
		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\nT1 : %5.4f meanAirTemp : %5.4f \nnlyrs : %d maxLyrDepth : %5.4f \n \n", T1, meanAirTemp, nlyrs, maxLyrDepth));

		// linear regression
		while (Defines.LE(acc, maxLyrDepth)) {

			x1 = stValues.x1BoundsR[k];
			x2 = stValues.x2BoundsR[k];
			i = -1;
			if (toDebug) {
				if (x1 != i) { // makes sure that we're not sending stValues.depths[-1] to printf, b/c as it turns out that causes a nasty segmentation fault error...
					f.LogError(LogMode.NOTE, String.format("k %d %d - %d depthLow %5.4f acc %5.4f depthHigh %5.4f\n", k, x1, x2, stValues.depths[x1], acc, stValues.depths[x2]));
				} else {
					f.LogError(LogMode.NOTE, String.format("k %d %d - %d depthLow %5.4f acc %5.4f depthHigh %5.4f\n", k, x1, x2, 0.0, acc, stValues.depths[x2]));
				}
			}

			if (x1 == i) { // sets the values to the first layer of soils values, since theres nothing else that can be done...
				swcR[k] = (swc[0] / width[0]) * deltaX; // division & multiplication is to make sure that the values are scaled appropriately, since the width of the layer & the width of a layer of the regression may not be the same
			} else if (x1 == x2) { // sets the values to the layers values, since x1 and x2 are the same, no regression is necessary (scaling still is necessary, however)
				swcR[k] = (swc[x1] / width[x1]) * deltaX;
			} else { // double regression( double x1, double x2, double y1, double y2, double deltaX ), located in generic.c
				part1 = (x2 != i) ? ((swc[x2] / width[x2]) * deltaX) : ((swc[nlyrs - 1] / width[nlyrs - 1]) * deltaX);
				swcR[k] = Defines.regression(stValues.depths[x1], (x2 != i) ? stValues.depths[x2] : stValues.depths[nlyrs - 1], (swc[x1] / width[x1]) * deltaX, part1, acc);
			}

			k++;
			acc += deltaX;
		}

		// uncomment out this next part if wanting to change back to extrapolating the rest of the regression values...

		// to fill the rest of the regression values, simply use the last two values since there is no more actual data to go off of
		/*for( i=k; i < nRgr; i++)
		 swcR[i] = regression(stValues.depthsR[i - 2], stValues.depthsR[i - 1], swcR[i - 2], swcR[i - 1], stValues.depthsR[i]);


		 // getting the average for a regression...
		 for( i=0; i < k; i++) {
		 swcAverage += swcR[i];
		 }
		 swcAverage = swcAverage / (k + 0.0);

		 // resets the regression values if they're too small... it's a safeguard...
		 for( i=0; i < nRgr; i++ )
		 if(LT(swcR[i], 1))
		 swcR[i] = swcAverage;*/

		//just use the last layers values...		
		for (i = k; i < nRgr; i++)
			swcR[i] = deltaX * (swc[nlyrs - 1] / width[nlyrs - 1]);

		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\nregression values: \n"));
		
		if (toDebug)
			for (i = 0; i < nRgr; i++)
				f.LogError(LogMode.NOTE, String.format("k %d swcR %5.4f fcR %5.4f wpR %5.4f oldsTempR %5.4f bDensityR %5.4f \n", i, swcR[i], stValues.fcR[i], stValues.wpR[i], stValues.oldsTempR[i], stValues.bDensityR[i]));
		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\nlayer values: \n"));
		if (toDebug)
			for (i = 0; i < nlyrs; i++)
				f.LogError(LogMode.NOTE, String.format("i %d width %5.4f depth %5.4f swc %5.4f fc %5.4f wp %5.4f oldsTemp %5.4f bDensity %5.4f \n", i, width[i], stValues.depths[i], swc[i], fc[i], wp[i], oldsTemp[i],
						bDensity[i]));

		// FINALLY done with the regressions!!! this is where we calculate the temperature for each soil layer of the regression
		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\n"));

		for (i = 0; i < nRgr; i++) { // goes to nRgr, because the soil temp of the last regression layer (nRgr) is the meanAirTemp

			// first we must calculate cs & sh (& subsequently sm & pe), for use later
			sm = swcR[i];
			pe = (sm - stValues.wpR[i]) / (stValues.fcR[i] - stValues.wpR[i]);
			cs = csParam1 + (pe * csParam2); // csParam1 = 0.0007, csParam2 = 0.0003
			sh = sm + (shParam * (1. - sm)); // shParam = 0.18

			if (toDebug)
				f.LogError(LogMode.NOTE, String.format("k %d cs %5.4f sh %5.4f\n", i, cs, sh));

			// breaking the equation down into parts to make it easier for me to process
			part1 = cs / (sh * stValues.bDensityR[i]);

			if (i > 0) { // handles all layers except the first soil layer
				part2 = (sTempR[i - 1] - (2 * stValues.oldsTempR[i]) + stValues.oldsTempR[i + 1]) / Defines.squared(deltaX);
			} else { // handles the first soil layer, since it needs the temp of the top of the soil
				part2 = (T1 - (2 * stValues.oldsTempR[0]) + stValues.oldsTempR[1]) / Defines.squared(deltaX);
			}

			sTempR[i] = ((part1 * part2) * deltaT) + stValues.oldsTempR[i];
		}
		sTempR[nRgr] = meanAirTemp; // again... the last layer of the regression is set to the constant meanAirTemp

		// MORE REGRESSIONS! to change sTempR into sTemp for outputting correctly
		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\n"));
		
		j = -1; // have to do this to avoid signed / unsigned comparison warning.  it's a pain really but have to do since the program was all written using unsigned ints (which can be no negative value except for -1) for some reason.  keep in mind that -1 is the largest possible value that an unsigned int can be according to c (ie -1 > 100 is a true statement if they're both unsigned ints b/c -1 is converted to UINT_MAX), very confusing.
		for (i = 0; i < nlyrs; i++) {
			x1 = stValues.x1Bounds[i];
			x2 = stValues.x2Bounds[i];

			if (toDebug)
				f.LogError(LogMode.NOTE, String.format("i %d %d - %d depthLow %5.4f acc %5.4f depthHigh %5.4f\n", i, x1, x2, ((x1 + 1) * deltaX), stValues.depths[i], (x2 != j) ? stValues.depthsR[x2] : -1.0));

			if (x1 == j) { // makes a regression with the temp at the top of the soil & the first soil layer...
				//sTemp[i] = regression(0.0, deltaX, T1, sTempR[0], stValues.depths[i]); // commented out, b/c it was actually giving a worse approximation
				sTemp[i] = Defines.regression(deltaX, deltaX + deltaX, sTempR[0], sTempR[1], stValues.depths[i]);
			} else if (x1 == x2) { // sets the values to the layers values, since x1 and x2 are the same, no regression is necessary
				sTemp[i] = sTempR[x1];
			} else {
				if (x2 != j)
					sTemp[i] = Defines.regression(stValues.depthsR[x1], stValues.depthsR[x2], sTempR[x1], sTempR[x2], stValues.depths[i]);
				else
					sTemp[i] = Defines.regression(stValues.depthsR[x1], (x2 != j) ? stValues.depthsR[x2] : stValues.depthsR[nRgr - 1], sTempR[x1], (x2 != j) ? sTempR[x2] : sTempR[nRgr - 1],
							stValues.depths[i]);
			}
		}

		for (i = 0; i < nlyrs; i++)	// now that you have calculated the new temperatures can determine whether the soil layers should be frozen or not.
				{
			j = 0;
			while (stValues.lyrFrozen[j] == 1) //do this to determine the i-th non frozen layer, to use in the fusion pool calculation
			{
				j++;
			}
			// only need to do something if the soil temperature is at the freezing point, or the soil temperature is transitioning over the freezing point
			if (Defines.EQ(oldsTemp[i], fp) || (Defines.GT(oldsTemp[i],fp) && Defines.LT(sTemp[i],fp))|| (Defines.LT(oldsTemp[i],fp) && Defines.GT(sTemp[i],fp)) ){
			deltaTemp = sTemp[i] - oldsTemp[i];	// determine how much the temperature of the soil layer changed
					sm = swc[j];
					sh = sm + (shParam * (1. - sm));
					sFusionPool[i] = ((fH2O*(swc[i]/width[i]))/sh)*tc*(deltaTemp/Math.abs(deltaTemp));// calculate the fusion pool of the current soil layer, or how much temperature change must happen to freeze/thaw a soil layer

					if( Defines.EQ(oldsTemp[i], fp) )// if the temperature of the soil layer is at the freezing point, then we need to use the old fusion pool value with the newly calculated one
					if( (Defines.LT(sFusionPool[i],0.00) && Defines.GT(stValues.oldsFusionPool[i],0.00)) || (Defines.GT(sFusionPool[i],0.00) && Defines.LT(stValues.oldsFusionPool[i],0.00)) )// here is just a condition to make sure that you weren't trying to freeze a layer, got half way through, and then the next day, start to thaw that layer. More for sign issues
					sFusionPool[i] += stValues.oldsFusionPool[i];// if you have partially froze and now want to thaw, you need to take the newly calculated pool and add the old one. Since the signs should be different in this case, the new fusion pool should become a smaller value
					else
					sFusionPool[i] = stValues.oldsFusionPool[i];// if you are still freezing/thawing from the day before, you can just use the old fusion pool
					else
					{
						deltaTemp -= (fp - oldsTemp[i]);// if you aren't at the freezing point initially, then you need to adjust the deltaTemp to not account for the temperature getting to the freezing point, since you just want to determine how much past the freezing poin you get, if you do get past.
					}

					if( Defines.LT( Math.abs(deltaTemp), Math.abs(sFusionPool[i])) ) // in this case, you don't have enough change in temperature to freeze/thaw the current layer fully
					{
						sFusionPool[i] -= deltaTemp; // adjust the fusion pool by the change in temperature
						sTemp[i] = fp;// set the new temperature to the freezing point
						stValues.lyrFrozen[i] = 1;// set the layer as frozen. For this I used that if the temperature was equal to the freezing point, the soil was at least partially frozen, so just used it as a frozen layer for simplicity
					}
					else if( Defines.GT( Math.abs(deltaTemp), Math.abs(sFusionPool[i])) ) // in this case you had more temperature change then the fusion pool
					{
						deltaTemp -= sFusionPool[i];	// adjust the deltaTemp by the total fusion pool to find out how much more you can change the temperature by
						sFusionPool[i] = 0.00;// fusion pool is now zero
						sTemp[i] = fp + deltaTemp;// adjust the temperature by the freezing point plus the deltaTemp, signs will work out so that freezing will lower temperature
						if ( Defines.LE(sTemp[i], fp) )// now determine whether the soil layer is frozen based on the new temperature, as mentioned above, the freezing point counts as a frozen layer, even though it is partially frozen
						stValues.lyrFrozen[i]=1;
						else
						stValues.lyrFrozen[i]=0;
					}
					else							// in this case the deltaTemp and fusion pool were equal, so the soil layer should be just barely frozen/thawed
					{
						sFusionPool[i] = 0.00;		// fusion pool is now zero
						sTemp[i] = fp + (deltaTemp/Math.abs(deltaTemp));// adjust the temperature by the freezing pool plus the (deltaTemp/abs(deltaTemp)) which should pull out the sign of the deltaTemp. i.e. if deltaTemp is negative, should get a -1.
						stValues.lyrFrozen[i] = 1 - stValues.lyrFrozen[i];// determine if the layer is frozen now. It should be the opposite of what it was going into this scenario, so 1 minus the current value should return the opposite, i.e. if layer is frozen lyrFrozen = 1, so lyrFrozen = 1 -1 = 0, which means not frozen.
					}

					soil_temp_init = false;
				}
				else
				sFusionPool[i] = 0.00;		//if your temperatures don't match any of those conditions, just set the fusion pool to zero and don't change the temperature at all
			}

		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\nregression temp values: \n"));

		if (toDebug)
			for (i = 0; i < nRgr + 1; i++)
				f.LogError(LogMode.NOTE, String.format("k %d oldsTempR %5.4f sTempR %5.4f depth %5.4f \n", i, stValues.oldsTempR[i], sTempR[i], ((i + 1) * deltaX)) ); // *(oldsTempR + i) is equivalent to writing oldsTempR[i]

		if (toDebug)
			f.LogError(LogMode.NOTE, String.format("\nlayer temp values: \n"));

		if (toDebug)
			for (i = 0; i < nlyrs; i++)
				f.LogError(LogMode.NOTE, String.format("i %d oldTemp %5.4f sTemp %5.4f depth %5.4f  \n", i, oldsTemp[i], sTemp[i], stValues.depths[i]));

		// updating the values of yesterdays temperature regression for the next time the function is called...
		// also added fusion pool to this update so that we can store old fusion pool for the cases where we might need that value
		for (i = 0; i < nRgr + 1; i++) {
			stValues.oldsTempR[i] = sTempR[i];
			stValues.oldsFusionPool[i] = sFusionPool[i];
		}

		if (toDebug) {
			f.LogError(LogMode.FATAL, "EXIT. PRINT DEBUG ON IN SOIL TEMP"); // terminates the program, make sure to take this out later
		}
	}
	
}
