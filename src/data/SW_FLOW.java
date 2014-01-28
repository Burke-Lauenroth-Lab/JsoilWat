package data;

import times.Times;
import times.Times.TwoDays;
import data.SW_SOILWATER.SOILWAT;
import defines.Defines;

public class SW_FLOW {
	
	/* temporary arrays for SoWat_flow_subs.c subroutines.
	 * array indexing in those routines will be from
	 * zero rather than 1.  see records2arrays().
	 */
	private int[] lyrTrRegions_Forb, lyrTrRegions_Tree, lyrTrRegions_Shrub, lyrTrRegions_Grass;
	
	private double drainout; /* h2o drained out of deepest layer */
	
	private double[] forb_h2o_qum, tree_h2o_qum, shrub_h2o_qum, grass_h2o_qum, litter_h2o_qum, standingWater; /* water on soil surface if layer below is saturated */
	
	private double[] lyrSWCBulk, lyrDrain, lyrTransp_Forb, lyrTransp_Tree, lyrTransp_Shrub, lyrTransp_Grass,
			lyrTranspCo_Forb, lyrTranspCo_Tree, lyrTranspCo_Shrub, lyrTranspCo_Grass, lyrEvap_BareGround,
			lyrEvap_Forb, lyrEvap_Tree, lyrEvap_Shrub, lyrEvap_Grass, lyrEvapCo, lyrSWCBulk_FieldCaps,
			lyrWidths, lyrSWCBulk_Wiltpts, lyrSWCBulk_HalfWiltpts, lyrSWCBulk_Mins, lyrSWCBulk_atSWPcrit_Forb,
			lyrSWCBulk_atSWPcrit_Tree, lyrSWCBulk_atSWPcrit_Shrub, lyrSWCBulk_atSWPcrit_Grass, lyrpsisMatric,
			lyrthetasMatric, lyrBetasMatric, lyrBetaInvMatric, lyrSumTrCo, lyrHydRed_Forb,
			lyrHydRed_Tree, lyrHydRed_Shrub, lyrHydRed_Grass, lyrImpermeability, lyrSWCBulk_Saturated,
			lyroldsTemp, lyrsTemp, lyrbDensity;
	
	private SW_MODEL SW_Model;
	private SW_SITE SW_Site;
	private SW_SOILS SW_Soils;
	private SW_SOILWATER SW_Soilwat;
	private SW_WEATHER SW_Weather;
	private SW_VEGPROD SW_VegProd;
	private SW_SKY SW_Sky;
	
	public SW_FLOW(SW_MODEL SW_Model, SW_SITE SW_Site, SW_SOILS SW_Soils, SW_SOILWATER SW_Soilwat, SW_WEATHER SW_Weather, SW_VEGPROD SW_VegProd, SW_SKY SW_Sky) {
		this.SW_Model = SW_Model;
		this.SW_Site = SW_Site;
		this.SW_Sky = SW_Sky;
		this.SW_Soilwat = SW_Soilwat;
		this.SW_VegProd = SW_VegProd;
		this.SW_Weather = SW_Weather;
		this.SW_Soils = SW_Soils;
		
		
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
		lyrSumTrCo=new double[Defines.MAX_TRANSP_REGIONS+1];
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
		drainout = 0;
		forb_h2o_qum[0]=tree_h2o_qum[0]=shrub_h2o_qum[0]=grass_h2o_qum[0]=litter_h2o_qum[0]=standingWater[0]=0;
		forb_h2o_qum[1]=tree_h2o_qum[1]=shrub_h2o_qum[1]=grass_h2o_qum[1]=litter_h2o_qum[1]=standingWater[1]=0;
	}
	
	/* *************************************************** */
	/* *************************************************** */
	/*            The Water Flow                           */
	/* --------------------------------------------------- */
	public void SW_Water_Flow() {

		double swpot_avg_forb, swpot_avg_tree, swpot_avg_shrub, swpot_avg_grass, soil_evap_forb, soil_evap_tree, soil_evap_shrub, soil_evap_grass, soil_evap_rate_forb = 1.,
				soil_evap_rate_tree = 1., soil_evap_rate_shrub = 1., soil_evap_rate_grass = 1., soil_evap_rate_bs = 1., transp_forb, transp_tree, transp_shrub, transp_grass,
				transp_rate_forb = 1., transp_rate_tree = 1., transp_rate_shrub = 1., transp_rate_grass = 1., snow_evap_rate, surface_evap_forb_rate, surface_evap_tree_rate,
				surface_evap_shrub_rate, surface_evap_grass_rate, surface_evap_litter_rate, surface_evap_standingWater_rate, grass_h2o, shrub_h2o, tree_h2o, forb_h2o, litter_h2o,
				litter_h2o_help, surface_h2o, h2o_for_soil = 0., ppt_toUse, snowmelt, snowdepth_scale_grass = 1., snowdepth_scale_shrub = 1., snowdepth_scale_tree = 1.,
				snowdepth_scale_forb = 1., rate_help;

		int doy;

		doy = SW_Model.getDOY(); /* base1 */
		/*	month = SW_Model.month;*//* base0 */

		records2arrays();
		
		SOILWAT soilWat = SW_Soilwat.getSoilWat();
		SW_VEGPROD.DailyVegProd vegDaily = SW_VegProd.getDailyValues();

		/* snowdepth scaling */
		soilWat.snowdepth = SW_SOILWATER.SW_SnowDepth(soilWat.snowpack[TwoDays.Today.ordinal()], SW_Sky.getSnow_density_daily(doy));
		/* if snow depth is deeper than vegetation height then
		 - rain and snowmelt infiltrates directly to soil (no vegetation or litter interception of today)
		 only
		 - evaporation of yesterdays interception
		 - infiltrate water high
		 - infiltrate water low */

		if (Double.compare(vegDaily.grass.veg_height_daily[doy], 0.)>0) {
			snowdepth_scale_grass = 1. - soilWat.snowdepth / vegDaily.grass.veg_height_daily[doy];
		} else {
			snowdepth_scale_grass = 1.;
		}
		if (Double.compare(vegDaily.forb.veg_height_daily[doy], 0.)>0) {
			snowdepth_scale_forb = 1. - soilWat.snowdepth / vegDaily.forb.veg_height_daily[doy];
		} else {
			snowdepth_scale_forb = 1.;
		}
		if (Double.compare(vegDaily.shrub.veg_height_daily[doy], 0.)>0) {
			snowdepth_scale_shrub = 1. - soilWat.snowdepth / vegDaily.shrub.veg_height_daily[doy];
		} else {
			snowdepth_scale_shrub = 1.;
		}
		if (Double.compare(vegDaily.tree.veg_height_daily[doy], 0.)>0) {
			snowdepth_scale_tree = 1. - soilWat.snowdepth / vegDaily.tree.veg_height_daily[doy];
		} else {
			snowdepth_scale_tree = 1.;
		}

		/* Interception */
		ppt_toUse = SW_Weather.now.rain[TwoDays.Today.ordinal()]; /* ppt is partioned into ppt = snow + rain */
		if (Double.compare(SW_VegProd.fractionTree, 0.)>0 && Double.compare(snowdepth_scale_tree, 0.)>0) { /* trees present AND trees not fully covered in snow */
			tree_intercepted_water(&h2o_for_soil, &tree_h2o, ppt_toUse, SW_VegProd.tree.lai_live_daily[doy], snowdepth_scale_tree * SW_VegProd.fractionTree,
					SW_VegProd.tree.veg_intPPT_a, SW_VegProd.tree.veg_intPPT_b, SW_VegProd.tree.veg_intPPT_c, SW_VegProd.tree.veg_intPPT_d);
			ppt_toUse = h2o_for_soil; /* amount of rain that is not intercepted by the forest canopy */
		} else { /* snow depth is more than vegetation height  */
			h2o_for_soil = ppt_toUse;
			tree_h2o = 0.;
		} /* end forest interception */

		if (Double.compare(SW_VegProd.fractionShrub, 0.)>0 && Double.compare(snowdepth_scale_shrub, 0.)>0) {
			shrub_intercepted_water(&h2o_for_soil, &shrub_h2o, ppt_toUse, SW_VegProd.shrub.vegcov_daily[doy], snowdepth_scale_shrub * SW_VegProd.fractionShrub,
					SW_VegProd.shrub.veg_intPPT_a, SW_VegProd.shrub.veg_intPPT_b, SW_VegProd.shrub.veg_intPPT_c, SW_VegProd.shrub.veg_intPPT_d);
			ppt_toUse = h2o_for_soil; /* amount of rain that is not intercepted by the shrub canopy */
		} else {
			shrub_h2o = 0.;
		} /* end shrub interception */

		if (Double.compare(SW_VegProd.fractionForb, 0.)>0 && Double.compare(snowdepth_scale_forb, 0.)>0) { /* forbs present AND not fully covered in snow */
			forb_intercepted_water(&h2o_for_soil, &forb_h2o, ppt_toUse, SW_VegProd.forb.vegcov_daily[doy], snowdepth_scale_forb * SW_VegProd.fractionForb,
					SW_VegProd.forb.veg_intPPT_a, SW_VegProd.forb.veg_intPPT_b, SW_VegProd.forb.veg_intPPT_c, SW_VegProd.forb.veg_intPPT_d);
			ppt_toUse = h2o_for_soil; /* amount of rain that is not intercepted by the forbs */
		} else { /* snow depth is more than vegetation height  */
			forb_h2o = 0.;

		} /* end forb interception */

		if (Double.compare(SW_VegProd.fractionGrass, 0.)>0 && Double.compare(snowdepth_scale_grass, 0.)>0) {
			grass_intercepted_water(&h2o_for_soil, &grass_h2o, ppt_toUse, SW_VegProd.grass.vegcov_daily[doy], snowdepth_scale_grass * SW_VegProd.fractionGrass,
					SW_VegProd.grass.veg_intPPT_a, SW_VegProd.grass.veg_intPPT_b, SW_VegProd.grass.veg_intPPT_c, SW_VegProd.grass.veg_intPPT_d);
		} else {
			grass_h2o = 0.;
		} /* end grass interception */

		if (EQ(SW_Soilwat.snowpack[TwoDays.Today.ordinal()], 0.)) { /* litter interception only when no snow */
			litter_h2o_help = 0.;

			if (Double.compare(SW_VegProd.fractionTree, 0.)>0) {
				litter_intercepted_water(&h2o_for_soil, &litter_h2o, SW_VegProd.tree.litter_daily[doy], SW_VegProd.fractionTree, SW_VegProd.tree.litt_intPPT_a,
						SW_VegProd.tree.litt_intPPT_b, SW_VegProd.tree.litt_intPPT_c, SW_VegProd.tree.litt_intPPT_d);
				litter_h2o_help += litter_h2o;
			}

			if (Double.compare(SW_VegProd.fractionShrub, 0.)>0) {
				litter_intercepted_water(&h2o_for_soil, &litter_h2o, SW_VegProd.shrub.litter_daily[doy], SW_VegProd.fractionShrub, SW_VegProd.shrub.litt_intPPT_a,
						SW_VegProd.shrub.litt_intPPT_b, SW_VegProd.shrub.litt_intPPT_c, SW_VegProd.shrub.litt_intPPT_d);
				litter_h2o_help += litter_h2o;
			}

			if (Double.compare(SW_VegProd.fractionForb, 0.)>0) {
				litter_intercepted_water(&h2o_for_soil, &litter_h2o, SW_VegProd.forb.litter_daily[doy], SW_VegProd.fractionForb, SW_VegProd.forb.litt_intPPT_a,
						SW_VegProd.forb.litt_intPPT_b, SW_VegProd.forb.litt_intPPT_c, SW_VegProd.forb.litt_intPPT_d);
				litter_h2o_help += litter_h2o;
			}

			if (Double.compare(SW_VegProd.fractionGrass, 0.)>0) {
				litter_intercepted_water(&h2o_for_soil, &litter_h2o, SW_VegProd.grass.litter_daily[doy], SW_VegProd.fractionGrass, SW_VegProd.grass.litt_intPPT_a,
						SW_VegProd.grass.litt_intPPT_b, SW_VegProd.grass.litt_intPPT_c, SW_VegProd.grass.litt_intPPT_d);
				litter_h2o_help += litter_h2o;
			}

			litter_h2o = litter_h2o_help;
		} else {
			litter_h2o = 0.;
		}

		/* Sum cumulative intercepted components */
		SW_Soilwat.tree_int = tree_h2o;
		SW_Soilwat.shrub_int = shrub_h2o;
		SW_Soilwat.forb_int = forb_h2o;
		SW_Soilwat.grass_int = grass_h2o;
		SW_Soilwat.litter_int = litter_h2o;

		tree_h2o_qum[TwoDays.Today.ordinal()] = tree_h2o_qum[TwoDays.Yesterday.ordinal()] + tree_h2o;
		shrub_h2o_qum[TwoDays.Today.ordinal()] = shrub_h2o_qum[TwoDays.Yesterday.ordinal()] + shrub_h2o;
		forb_h2o_qum[TwoDays.Today.ordinal()] = forb_h2o_qum[TwoDays.Yesterday.ordinal()] + forb_h2o;
		grass_h2o_qum[TwoDays.Today.ordinal()] = grass_h2o_qum[TwoDays.Yesterday.ordinal()] + grass_h2o;
		litter_h2o_qum[TwoDays.Today.ordinal()] = litter_h2o_qum[TwoDays.Yesterday.ordinal()] + litter_h2o;
		/* End Interception */

		/* Surface water */
		standingWater[TwoDays.Today.ordinal()] = standingWater[TwoDays.Yesterday.ordinal()];

		/* Soil infiltration = rain+snowmelt - interception, but should be = rain+snowmelt - interception + (throughfall+stemflow) */
		surface_h2o = standingWater[TwoDays.Today.ordinal()];
		snowmelt = SW_Weather.now.snowmelt[TwoDays.Today.ordinal()];
		snowmelt = fmax( 0., snowmelt * (1. - SW_Weather.pct_snowRunoff/100.) ); /* amount of snowmelt is changed by runon/off as percentage */
		SW_Weather.snowRunoff = SW_Weather.now.snowmelt[TwoDays.Today.ordinal()] - snowmelt;
		h2o_for_soil += snowmelt; /* if there is snowmelt, it goes un-intercepted to the soil */
		h2o_for_soil += surface_h2o;
		SW_Weather.soil_inf = h2o_for_soil;

		/* Percolation for saturated soil conditions */
		infiltrate_water_high(lyrSWCBulk, lyrDrain, &drainout, h2o_for_soil, SW_Site.n_layers, lyrSWCBulk_FieldCaps, lyrSWCBulk_Saturated, lyrImpermeability,
				&standingWater[TwoDays.Today.ordinal()]);

		SW_Weather.soil_inf -= standingWater[TwoDays.Today.ordinal()]; /* adjust soil_infiltration for pushed back or infiltrated surface water */

		/* Surface water runoff */
		SW_Weather.surfaceRunoff = standingWater[TwoDays.Today.ordinal()] * SW_Site.percentRunoff;
		standingWater[TwoDays.Today.ordinal()] = fmax(0.0, (standingWater[TwoDays.Today.ordinal()] - SW_Weather.surfaceRunoff));
		surface_h2o = standingWater[TwoDays.Today.ordinal()];

		/* PET */
		SW_Soilwat.pet = SW_Site.pet_scale
				* petfunc(doy, SW_Weather.now.temp_avg[TwoDays.Today.ordinal()], SW_Site.latitude, SW_Site.altitude, SW_Site.slope, SW_Site.aspect,
						SW_VegProd.grass.albedo * SW_VegProd.fractionGrass + SW_VegProd.shrub.albedo * SW_VegProd.fractionShrub + SW_VegProd.forb.albedo * SW_VegProd.fractionForb
								+ SW_VegProd.tree.albedo * SW_VegProd.fractionTree + SW_VegProd.bareGround_albedo * SW_VegProd.fractionBareGround, SW_Sky.r_humidity_daily[doy],
						SW_Sky.windspeed_daily[doy], SW_Sky.cloudcov_daily[doy], SW_Sky.transmission_daily[doy]);

		/* Bare-soil evaporation rates */
		if (Double.compare(SW_VegProd.fractionBareGround, 0.) && EQ(SW_Soilwat.snowpack[TwoDays.Today.ordinal()], 0.)) /* bare ground present AND no snow on ground */
		{
			pot_soil_evap_bs(&soil_evap_rate_bs, SW_Site.n_evap_lyrs, lyrEvapCo, SW_Soilwat.pet, SW_Site.evap.xinflec, SW_Site.evap.slope, SW_Site.evap.yinflec,
					SW_Site.evap.range, lyrWidths, lyrSWCBulk);
			soil_evap_rate_bs *= SW_VegProd.fractionBareGround;
		} else {
			soil_evap_rate_bs = 0;
		}

		/* Tree transpiration & bare-soil evaporation rates */
		if (Double.compare(SW_VegProd.fractionTree, 0.) && Double.compare(snowdepth_scale_tree, 0.)) { /* trees present AND trees not fully covered in snow */
			tree_EsT_partitioning(&soil_evap_tree, &transp_tree, SW_VegProd.tree.lai_live_daily[doy], SW_VegProd.tree.EsTpartitioning_param);

			if (EQ(SW_Soilwat.snowpack[TwoDays.Today.ordinal()], 0.)) { /* bare-soil evaporation only when no snow */
				pot_soil_evap(&soil_evap_rate_tree, SW_Site.n_evap_lyrs, lyrEvapCo, SW_VegProd.tree.total_agb_daily[doy], soil_evap_tree, SW_Soilwat.pet, SW_Site.evap.xinflec,
						SW_Site.evap.slope, SW_Site.evap.yinflec, SW_Site.evap.range, lyrWidths, lyrSWCBulk, SW_VegProd.tree.Es_param_limit);
				soil_evap_rate_tree *= SW_VegProd.fractionTree;
			} else {
				soil_evap_rate_tree = 0.;
			}

			transp_weighted_avg(&swpot_avg_tree, SW_Site.n_transp_rgn, SW_Site.n_transp_lyrs_tree, lyrTrRegions_Tree, lyrTranspCo_Tree, lyrSWCBulk);

			pot_transp(&transp_rate_tree, swpot_avg_tree, SW_VegProd.tree.biolive_daily[doy], SW_VegProd.tree.biodead_daily[doy], transp_tree, SW_Soilwat.pet,
					SW_Site.transp.xinflec, SW_Site.transp.slope, SW_Site.transp.yinflec, SW_Site.transp.range, SW_VegProd.tree.shade_scale, SW_VegProd.tree.shade_deadmax,
					SW_VegProd.tree.tr_shade_effects.xinflec, SW_VegProd.tree.tr_shade_effects.slope, SW_VegProd.tree.tr_shade_effects.yinflec,
					SW_VegProd.tree.tr_shade_effects.range);
			transp_rate_tree *= snowdepth_scale_tree * SW_VegProd.fractionTree;
		} else {
			soil_evap_rate_tree = 0.;
			transp_rate_tree = 0.;
		}

		/* Shrub transpiration & bare-soil evaporation rates */
		if (Double.compare(SW_VegProd.fractionShrub, 0.) && Double.compare(snowdepth_scale_shrub, 0.)) { /* shrubs present AND shrubs not fully covered in snow */
			shrub_EsT_partitioning(&soil_evap_shrub, &transp_shrub, SW_VegProd.shrub.lai_live_daily[doy], SW_VegProd.shrub.EsTpartitioning_param);

			if (EQ(SW_Soilwat.snowpack[TwoDays.Today.ordinal()], 0.)) { /* bare-soil evaporation only when no snow */
				pot_soil_evap(&soil_evap_rate_shrub, SW_Site.n_evap_lyrs, lyrEvapCo, SW_VegProd.shrub.total_agb_daily[doy], soil_evap_shrub, SW_Soilwat.pet, SW_Site.evap.xinflec,
						SW_Site.evap.slope, SW_Site.evap.yinflec, SW_Site.evap.range, lyrWidths, lyrSWCBulk, SW_VegProd.shrub.Es_param_limit);
				soil_evap_rate_shrub *= SW_VegProd.fractionShrub;
			} else {
				soil_evap_rate_shrub = 0.;
			}

			transp_weighted_avg(&swpot_avg_shrub, SW_Site.n_transp_rgn, SW_Site.n_transp_lyrs_shrub, lyrTrRegions_Shrub, lyrTranspCo_Shrub, lyrSWCBulk);

			pot_transp(&transp_rate_shrub, swpot_avg_shrub, SW_VegProd.shrub.biolive_daily[doy], SW_VegProd.shrub.biodead_daily[doy], transp_shrub, SW_Soilwat.pet,
					SW_Site.transp.xinflec, SW_Site.transp.slope, SW_Site.transp.yinflec, SW_Site.transp.range, SW_VegProd.shrub.shade_scale, SW_VegProd.shrub.shade_deadmax,
					SW_VegProd.shrub.tr_shade_effects.xinflec, SW_VegProd.shrub.tr_shade_effects.slope, SW_VegProd.shrub.tr_shade_effects.yinflec,
					SW_VegProd.shrub.tr_shade_effects.range);
			transp_rate_shrub *= snowdepth_scale_shrub * SW_VegProd.fractionShrub;

		} else {
			soil_evap_rate_shrub = 0.;
			transp_rate_shrub = 0.;
		}

		/* Forb transpiration & bare-soil evaporation rates */
		if (Double.compare(SW_VegProd.fractionForb, 0.) && Double.compare(snowdepth_scale_forb, 0.)) { /* forbs present AND forbs not fully covered in snow */
			forb_EsT_partitioning(&soil_evap_forb, &transp_forb, SW_VegProd.forb.lai_live_daily[doy], SW_VegProd.forb.EsTpartitioning_param);

			if (EQ(SW_Soilwat.snowpack[TwoDays.Today.ordinal()], 0.)) { /* bare-soil evaporation only when no snow */
				pot_soil_evap(&soil_evap_rate_forb, SW_Site.n_evap_lyrs, lyrEvapCo, SW_VegProd.forb.total_agb_daily[doy], soil_evap_forb, SW_Soilwat.pet, SW_Site.evap.xinflec,
						SW_Site.evap.slope, SW_Site.evap.yinflec, SW_Site.evap.range, lyrWidths, lyrSWCBulk, SW_VegProd.forb.Es_param_limit);
				soil_evap_rate_forb *= SW_VegProd.fractionForb;
			} else {
				soil_evap_rate_forb = 0.;
			}

			transp_weighted_avg(&swpot_avg_forb, SW_Site.n_transp_rgn, SW_Site.n_transp_lyrs_forb, lyrTrRegions_Forb, lyrTranspCo_Forb, lyrSWCBulk);

			pot_transp(&transp_rate_forb, swpot_avg_forb, SW_VegProd.forb.biolive_daily[doy], SW_VegProd.forb.biodead_daily[doy], transp_forb, SW_Soilwat.pet,
					SW_Site.transp.xinflec, SW_Site.transp.slope, SW_Site.transp.yinflec, SW_Site.transp.range, SW_VegProd.forb.shade_scale, SW_VegProd.forb.shade_deadmax,
					SW_VegProd.forb.tr_shade_effects.xinflec, SW_VegProd.forb.tr_shade_effects.slope, SW_VegProd.forb.tr_shade_effects.yinflec,
					SW_VegProd.forb.tr_shade_effects.range);
			transp_rate_forb *= snowdepth_scale_forb * SW_VegProd.fractionForb;

		} else {
			soil_evap_rate_forb = 0.;
			transp_rate_forb = 0.;
		}

		/* Grass transpiration & bare-soil evaporation rates */
		if (Double.compare(SW_VegProd.fractionGrass, 0.) && Double.compare(snowdepth_scale_grass, 0.)) { /* grasses present AND grasses not fully covered in snow */
			grass_EsT_partitioning(&soil_evap_grass, &transp_grass, SW_VegProd.grass.lai_live_daily[doy], SW_VegProd.grass.EsTpartitioning_param);

			if (EQ(SW_Soilwat.snowpack[TwoDays.Today.ordinal()], 0.)) { /* bare-soil evaporation only when no snow */
				pot_soil_evap(&soil_evap_rate_grass, SW_Site.n_evap_lyrs, lyrEvapCo, SW_VegProd.grass.total_agb_daily[doy], soil_evap_grass, SW_Soilwat.pet, SW_Site.evap.xinflec,
						SW_Site.evap.slope, SW_Site.evap.yinflec, SW_Site.evap.range, lyrWidths, lyrSWCBulk, SW_VegProd.grass.Es_param_limit);
				soil_evap_rate_grass *= SW_VegProd.fractionGrass;
			} else {
				soil_evap_rate_grass = 0.;
			}

			transp_weighted_avg(&swpot_avg_grass, SW_Site.n_transp_rgn, SW_Site.n_transp_lyrs_grass, lyrTrRegions_Grass, lyrTranspCo_Grass, lyrSWCBulk);

			pot_transp(&transp_rate_grass, swpot_avg_grass, SW_VegProd.grass.biolive_daily[doy], SW_VegProd.grass.biodead_daily[doy], transp_grass, SW_Soilwat.pet,
					SW_Site.transp.xinflec, SW_Site.transp.slope, SW_Site.transp.yinflec, SW_Site.transp.range, SW_VegProd.grass.shade_scale, SW_VegProd.grass.shade_deadmax,
					SW_VegProd.grass.tr_shade_effects.xinflec, SW_VegProd.grass.tr_shade_effects.slope, SW_VegProd.grass.tr_shade_effects.yinflec,
					SW_VegProd.grass.tr_shade_effects.range);
			transp_rate_grass *= snowdepth_scale_grass * SW_VegProd.fractionGrass;
		} else {
			soil_evap_rate_grass = 0.;
			transp_rate_grass = 0.;
		}

		/* Potential evaporation rates of intercepted and surface water */
		surface_evap_tree_rate = tree_h2o_qum[TwoDays.Today.ordinal()];
		surface_evap_shrub_rate = shrub_h2o_qum[TwoDays.Today.ordinal()];
		surface_evap_forb_rate = forb_h2o_qum[TwoDays.Today.ordinal()];
		surface_evap_grass_rate = grass_h2o_qum[TwoDays.Today.ordinal()];
		surface_evap_litter_rate = litter_h2o_qum[TwoDays.Today.ordinal()];
		surface_evap_standingWater_rate = standingWater[TwoDays.Today.ordinal()];
		snow_evap_rate = SW_Weather.now.snowloss[TwoDays.Today.ordinal()]; /* but this is fixed and can also include snow redistribution etc., so don't scale to PET */

		/* Scale all (potential) evaporation and transpiration flux rates to PET */
		rate_help = surface_evap_tree_rate + surface_evap_forb_rate + surface_evap_shrub_rate + surface_evap_grass_rate + surface_evap_litter_rate
				+ surface_evap_standingWater_rate + soil_evap_rate_tree + transp_rate_tree + soil_evap_rate_forb + transp_rate_forb + soil_evap_rate_shrub + transp_rate_shrub
				+ soil_evap_rate_grass + transp_rate_grass + soil_evap_rate_bs;

		if (Double.compare(rate_help, SW_Soilwat.pet)) {
			rate_help = SW_Soilwat.pet / rate_help;

			surface_evap_tree_rate *= rate_help;
			surface_evap_forb_rate *= rate_help;
			surface_evap_shrub_rate *= rate_help;
			surface_evap_grass_rate *= rate_help;
			surface_evap_litter_rate *= rate_help;
			surface_evap_standingWater_rate *= rate_help;
			soil_evap_rate_tree *= rate_help;
			transp_rate_tree *= rate_help;
			soil_evap_rate_forb *= rate_help;
			transp_rate_forb *= rate_help;
			soil_evap_rate_shrub *= rate_help;
			transp_rate_shrub *= rate_help;
			soil_evap_rate_grass *= rate_help;
			transp_rate_grass *= rate_help;
			soil_evap_rate_bs *= rate_help;
		}

		/* Start adding components to AET */
		SW_Soilwat.aet = 0.; /* init aet for the day */
		SW_Soilwat.aet += snow_evap_rate;

		/* Evaporation of intercepted and surface water */
		evap_fromSurface(&tree_h2o_qum[TwoDays.Today.ordinal()], &surface_evap_tree_rate, &SW_Soilwat.aet);
		evap_fromSurface(&shrub_h2o_qum[TwoDays.Today.ordinal()], &surface_evap_shrub_rate, &SW_Soilwat.aet);
		evap_fromSurface(&forb_h2o_qum[TwoDays.Today.ordinal()], &surface_evap_forb_rate, &SW_Soilwat.aet);
		evap_fromSurface(&grass_h2o_qum[TwoDays.Today.ordinal()], &surface_evap_grass_rate, &SW_Soilwat.aet);
		evap_fromSurface(&litter_h2o_qum[TwoDays.Today.ordinal()], &surface_evap_litter_rate, &SW_Soilwat.aet);
		evap_fromSurface(&standingWater[TwoDays.Today.ordinal()], &surface_evap_standingWater_rate, &SW_Soilwat.aet);

		SW_Soilwat.tree_evap = surface_evap_tree_rate;
		SW_Soilwat.shrub_evap = surface_evap_shrub_rate;
		SW_Soilwat.forb_evap = surface_evap_forb_rate;
		SW_Soilwat.grass_evap = surface_evap_grass_rate;
		SW_Soilwat.litter_evap = surface_evap_litter_rate;
		SW_Soilwat.surfaceWater_evap = surface_evap_standingWater_rate;

		/* bare-soil evaporation */
		if (Double.compare(SW_VegProd.fractionBareGround, 0.) && EQ(SW_Soilwat.snowpack[TwoDays.Today.ordinal()], 0.)) {
			/* remove bare-soil evap from swv */
			remove_from_soil(lyrSWCBulk, lyrEvap_BareGround, &SW_Soilwat.aet, SW_Site.n_evap_lyrs, lyrEvapCo, soil_evap_rate_bs, lyrSWCBulk_HalfWiltpts);
		} else {
			/* Set daily array to zero, no evaporation */
			LyrIndex i;
			for (i = 0; i < SW_Site.n_evap_lyrs;)
				lyrEvap_BareGround[i++] = 0.;
		}

		/* Tree transpiration and bare-soil evaporation */
		if (Double.compare(SW_VegProd.fractionTree, 0.) && Double.compare(snowdepth_scale_tree, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrSWCBulk, lyrEvap_Tree, &SW_Soilwat.aet, SW_Site.n_evap_lyrs, lyrEvapCo, soil_evap_rate_tree, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrSWCBulk, lyrTransp_Tree, &SW_Soilwat.aet, SW_Site.n_transp_lyrs_tree, lyrTranspCo_Tree, transp_rate_tree, lyrSWCBulk_atSWPcrit_Tree);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			LyrIndex i;
			for (i = 0; i < SW_Site.n_evap_lyrs;)
				lyrEvap_Tree[i++] = 0.;
			for (i = 0; i < SW_Site.n_transp_lyrs_tree;)
				lyrTransp_Tree[i++] = 0.;
		}

		/* Shrub transpiration and bare-soil evaporation */
		if (Double.compare(SW_VegProd.fractionShrub, 0.) && Double.compare(snowdepth_scale_shrub, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrSWCBulk, lyrEvap_Shrub, &SW_Soilwat.aet, SW_Site.n_evap_lyrs, lyrEvapCo, soil_evap_rate_shrub, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrSWCBulk, lyrTransp_Shrub, &SW_Soilwat.aet, SW_Site.n_transp_lyrs_shrub, lyrTranspCo_Shrub, transp_rate_shrub, lyrSWCBulk_atSWPcrit_Shrub);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			LyrIndex i;
			for (i = 0; i < SW_Site.n_evap_lyrs;)
				lyrEvap_Shrub[i++] = 0.;
			for (i = 0; i < SW_Site.n_transp_lyrs_shrub;)
				lyrTransp_Shrub[i++] = 0.;
		}

		/* Forb transpiration and bare-soil evaporation */
		if (Double.compare(SW_VegProd.fractionForb, 0.) && Double.compare(snowdepth_scale_forb, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrSWCBulk, lyrEvap_Forb, &SW_Soilwat.aet, SW_Site.n_evap_lyrs, lyrEvapCo, soil_evap_rate_forb, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrSWCBulk, lyrTransp_Forb, &SW_Soilwat.aet, SW_Site.n_transp_lyrs_forb, lyrTranspCo_Forb, transp_rate_forb, lyrSWCBulk_atSWPcrit_Forb);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			LyrIndex i;
			for (i = 0; i < SW_Site.n_evap_lyrs;)
				lyrEvap_Forb[i++] = 0.;
			for (i = 0; i < SW_Site.n_transp_lyrs_forb;)
				lyrTransp_Forb[i++] = 0.;
		}

		/* Grass transpiration & bare-soil evaporation */
		if (Double.compare(SW_VegProd.fractionGrass, 0.) && Double.compare(snowdepth_scale_grass, 0.)) {
			/* remove bare-soil evap from swc */
			remove_from_soil(lyrSWCBulk, lyrEvap_Grass, &SW_Soilwat.aet, SW_Site.n_evap_lyrs, lyrEvapCo, soil_evap_rate_grass, lyrSWCBulk_HalfWiltpts);

			/* remove transp from swc */
			remove_from_soil(lyrSWCBulk, lyrTransp_Grass, &SW_Soilwat.aet, SW_Site.n_transp_lyrs_grass, lyrTranspCo_Grass, transp_rate_grass, lyrSWCBulk_atSWPcrit_Grass);
		} else {
			/* Set daily array to zero, no evaporation or transpiration */
			LyrIndex i;
			for (i = 0; i < SW_Site.n_evap_lyrs;)
				lyrEvap_Grass[i++] = 0.;
			for (i = 0; i < SW_Site.n_transp_lyrs_grass;)
				lyrTransp_Grass[i++] = 0.;
		}

		/* Hydraulic redistribution */
		if (SW_VegProd.grass.flagHydraulicRedistribution && Double.compare(SW_VegProd.fractionGrass, 0.) && Double.compare(SW_VegProd.grass.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrSWCBulk, lyrSWCBulk_Wiltpts, lyrTranspCo_Grass, lyrHydRed_Grass, SW_Site.n_layers, SW_VegProd.grass.maxCondroot,
					SW_VegProd.grass.swpMatric50, SW_VegProd.grass.shapeCond, SW_VegProd.fractionGrass);
		}
		if (SW_VegProd.forb.flagHydraulicRedistribution && Double.compare(SW_VegProd.fractionForb, 0.) && Double.compare(SW_VegProd.forb.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrSWCBulk, lyrSWCBulk_Wiltpts, lyrTranspCo_Forb, lyrHydRed_Forb, SW_Site.n_layers, SW_VegProd.forb.maxCondroot, SW_VegProd.forb.swpMatric50,
					SW_VegProd.forb.shapeCond, SW_VegProd.fractionForb);
		}
		if (SW_VegProd.shrub.flagHydraulicRedistribution && Double.compare(SW_VegProd.fractionShrub, 0.) && Double.compare(SW_VegProd.shrub.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrSWCBulk, lyrSWCBulk_Wiltpts, lyrTranspCo_Shrub, lyrHydRed_Shrub, SW_Site.n_layers, SW_VegProd.shrub.maxCondroot,
					SW_VegProd.shrub.swpMatric50, SW_VegProd.shrub.shapeCond, SW_VegProd.fractionShrub);
		}
		if (SW_VegProd.tree.flagHydraulicRedistribution && Double.compare(SW_VegProd.fractionTree, 0.) && Double.compare(SW_VegProd.tree.biolive_daily[doy], 0.)) {
			hydraulic_redistribution(lyrSWCBulk, lyrSWCBulk_Wiltpts, lyrTranspCo_Tree, lyrHydRed_Tree, SW_Site.n_layers, SW_VegProd.tree.maxCondroot, SW_VegProd.tree.swpMatric50,
					SW_VegProd.tree.shapeCond, SW_VegProd.fractionTree);
		}

		/* Calculate percolation for unsaturated soil water conditions. */
		/* 01/06/2011	(drs) call to infiltrate_water_low() has to be the last swc affecting calculation */

		infiltrate_water_low(lyrSWCBulk, lyrDrain, &drainout, SW_Site.n_layers, SW_Site.slow_drain_coeff, SLOW_DRAIN_DEPTH, lyrSWCBulk_FieldCaps, lyrWidths, lyrSWCBulk_Mins,
				lyrSWCBulk_Saturated, lyrImpermeability, &standingWater[TwoDays.Today.ordinal()]);

		SW_Soilwat.surfaceWater = standingWater[TwoDays.Today.ordinal()];

		/* Soil Temperature starts here */

		double biomass; // computing the standing crop biomass real quickly to condense the call to soil_temperature
		biomass = SW_VegProd.grass.biomass_daily[doy] * SW_VegProd.fractionGrass + SW_VegProd.shrub.biomass_daily[doy] * SW_VegProd.fractionShrub
				+ SW_VegProd.forb.biomass_daily[doy] * SW_VegProd.fractionForb + SW_VegProd.tree.biolive_daily[doy] * SW_VegProd.fractionTree; // changed to exclude tree biomass, bMatric/c it was breaking the soil_temperature function

				// soil_temperature function computes the soil temp for each layer and stores it in lyrsTemp
				// doesn't affect SWC at all, but needs it for the calculation, so therefore the temperature is the last calculation done
		if (SW_Site.use_soil_temp)
			soil_temperature(SW_Weather.now.temp_avg[TwoDays.Today.ordinal()], SW_Soilwat.pet, SW_Soilwat.aet, biomass, lyrSWCBulk, lyrbDensity, lyrWidths, lyroldsTemp, lyrsTemp, SW_Site.n_layers,
					lyrSWCBulk_FieldCaps, lyrSWCBulk_Wiltpts, SW_Site.bmLimiter, SW_Site.t1Param1, SW_Site.t1Param2, SW_Site.t1Param3, SW_Site.csParam1, SW_Site.csParam2,
					SW_Site.shParam, SW_Soilwat.snowpack[TwoDays.Today.ordinal()], SW_Site.meanAirTemp /*SW_Weather.hist.temp_year_avg*/, SW_Site.stDeltaX, SW_Site.stMaxDepth, SW_Site.stNRGR);

		/* Soil Temperature ends here */

		/* Move local values into main arrays */
		arrays2records();

		standingWater[TwoDays.Yesterday.ordinal()] = standingWater[TwoDays.Today.ordinal()];
		litter_h2o_qum[TwoDays.Yesterday.ordinal()] = litter_h2o_qum[TwoDays.Today.ordinal()];
		tree_h2o_qum[TwoDays.Yesterday.ordinal()] = tree_h2o_qum[TwoDays.Today.ordinal()];
		shrub_h2o_qum[TwoDays.Yesterday.ordinal()] = shrub_h2o_qum[TwoDays.Today.ordinal()];
		forb_h2o_qum[TwoDays.Yesterday.ordinal()] = forb_h2o_qum[TwoDays.Today.ordinal()];
		grass_h2o_qum[TwoDays.Yesterday.ordinal()] = grass_h2o_qum[TwoDays.Today.ordinal()];

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
			lyrSWCBulk[i] = soilWat.swcBulk[TwoDays.Today.ordinal()][i];
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
			soilWat.swcBulk[TwoDays.Today.ordinal()][i] = lyrSWCBulk[i];
			soilWat.drain[i] = lyrDrain[i];
			soilWat.hydred_tree[i] = lyrHydRed_Tree[i];
			soilWat.hydred_shrub[i] = lyrHydRed_Shrub[i];
			soilWat.hydred_forb[i] = lyrHydRed_Forb[i];
			soilWat.hydred_grass[i] = lyrHydRed_Grass[i];
			soilWat.sTemp[i] = lyrsTemp[i];
		}

		if (SW_Site.getDeepdrain())
			soilWat.swcBulk[TwoDays.Today.ordinal()][SW_Soils.getLayersInfo().deep_lyr] = drainout;

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
}
