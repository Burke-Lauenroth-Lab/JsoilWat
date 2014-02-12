package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.LogFileIn.LogMode;
import soilwat.SW_WEATHER.SW_WEATHER_2DAYS;

public class SW_SOILWATER {
	public static final int SW_Adjust_Avg = 1;
	public static final int SW_Adjust_StdErr = 2;
	
	protected class SW_SOILWAT_OUTPUTS {
		protected double surfaceWater, total_evap, surfaceWater_evap, tree_evap, forb_evap, shrub_evap,
			grass_evap, litter_evap, total_int, tree_int, forb_int, shrub_int, grass_int, litter_int, snowpack, snowdepth, et, aet, pet, deep;
		protected double[] wetdays, vwcBulk, /* soil water content cm/cm */
		vwcMatric, swcBulk, /* soil water content cm/layer */
		swpMatric, /* soil water potential */
		swaBulk, /* available soil water cm/layer, swc-(wilting point) */
		swaMatric, transp_total, transp_tree, transp_forb, transp_shrub, transp_grass, evap,
				lyrdrain, hydred_total, hydred_tree, /* hydraulic redistribution cm/layer */
				hydred_forb, hydred_shrub, hydred_grass, 
				sTemp; // soil temperature in celcius for each layer
		protected SW_SOILWAT_OUTPUTS() {
			surfaceWater=total_evap=surfaceWater_evap=tree_evap=forb_evap=shrub_evap=0;
			grass_evap=litter_evap=total_int=tree_int=forb_int=shrub_int=grass_int=litter_int=snowpack=snowdepth=et=aet=pet=deep=0;
			litter_int=tree_int=forb_int=shrub_int=grass_int=snowpack=0;
			wetdays = new double[Defines.MAX_LAYERS];
			vwcBulk = new double[Defines.MAX_LAYERS];
			vwcMatric = new double[Defines.MAX_LAYERS];
			swcBulk = new double[Defines.MAX_LAYERS];
			swpMatric = new double[Defines.MAX_LAYERS];
			swaBulk = new double[Defines.MAX_LAYERS];
			swaMatric = new double[Defines.MAX_LAYERS];
			transp_total= new double[Defines.MAX_LAYERS];
			transp_tree = new double[Defines.MAX_LAYERS];
			transp_forb = new double[Defines.MAX_LAYERS];
			transp_shrub = new double[Defines.MAX_LAYERS];
			transp_grass = new double[Defines.MAX_LAYERS];
			evap = new double[Defines.MAX_LAYERS];
			lyrdrain = new double[Defines.MAX_LAYERS];
			hydred_total = new double[Defines.MAX_LAYERS];
			hydred_grass = new double[Defines.MAX_LAYERS];
			hydred_shrub = new double[Defines.MAX_LAYERS];
			hydred_tree = new double[Defines.MAX_LAYERS];
			hydred_forb = new double[Defines.MAX_LAYERS];
			sTemp = new double[Defines.MAX_LAYERS];
			
			
		}
		protected void onClear() {
			surfaceWater=total_evap=surfaceWater_evap=tree_evap=forb_evap=shrub_evap=0;
			grass_evap=litter_evap=total_int=tree_int=forb_int=shrub_int=grass_int=litter_int=snowpack=snowdepth=et=aet=pet=deep=0;
			litter_int=tree_int=forb_int=shrub_int=grass_int=snowpack=0;
			for(int i=0; i<Defines.MAX_LAYERS; i++) {
				wetdays[i]=vwcBulk[i]=vwcMatric[i]=swcBulk[i]=swpMatric[i]=swaBulk[i]=swaMatric[i]=transp_total[i]=transp_tree[i]=transp_forb[i]=0;
				transp_shrub[i]=transp_grass[i]=evap[i]=lyrdrain[i]=hydred_total[i]=hydred_tree[i]=hydred_forb[i]=hydred_shrub[i]=hydred_grass[i]=sTemp[i]=0;
			}
		}
	}
	
	public static class SWC_INPUT_DATA {
		public boolean hist_use;
		public String filePrefix;
		public SW_TIMES yr;
		public int method;
		public SWC_INPUT_DATA() {
			yr = new SW_TIMES();
		}
	}
	
	protected class SOILWAT extends SWC_INPUT_DATA {
		/* current daily soil water related values */
		protected boolean[] is_wet; /* swc sufficient to count as wet today */
		protected double snowdepth, surfaceWater, surfaceWater_evap, pet, aet, litter_evap, tree_evap, forb_evap,
			shrub_evap, grass_evap, litter_int, tree_int, forb_int, shrub_int, grass_int;
		protected double[] snowpack, /* swe of snowpack, if accumulation flag set */
		transpiration_tree, transpiration_forb, transpiration_shrub, transpiration_grass, evaporation,
				drain, /* amt of swc able to drain from curr layer to next */
				hydred_tree, /* hydraulic redistribution cm/layer */
				hydred_forb, hydred_shrub, hydred_grass, 
				sTemp; // soil temperature
		protected double[][] swcBulk;
		SW_SOILWAT_OUTPUTS dysum, /* helpful placeholder */
		wksum, mosum, yrsum, /* accumulators for *avg */
		wkavg, moavg, yravg; /* averages or sums as appropriate */
		//SWC Setup File Settings
		//protected boolean hist_use;
		//protected int method;
		//protected SW_TIMES yr;
		//protected String filePrefix;
		
		protected SOILWAT() {
			snowdepth=surfaceWater=surfaceWater_evap=pet=aet=litter_evap=tree_evap=forb_evap=shrub_evap=grass_evap=0;
			litter_int=tree_int=forb_int=shrub_int=grass_int=0;
			is_wet = new boolean[Defines.MAX_LAYERS];
			swcBulk = new double[Defines.TWO_DAYS][Defines.MAX_LAYERS];
			snowpack = new double[Defines.TWO_DAYS];
			transpiration_grass = new double[Defines.MAX_LAYERS];
			transpiration_shrub = new double[Defines.MAX_LAYERS];
			transpiration_tree = new double[Defines.MAX_LAYERS];
			transpiration_forb = new double[Defines.MAX_LAYERS];
			evaporation = new double[Defines.MAX_LAYERS];
			drain = new double[Defines.MAX_LAYERS];
			hydred_grass = new double[Defines.MAX_LAYERS];
			hydred_shrub = new double[Defines.MAX_LAYERS];
			hydred_tree = new double[Defines.MAX_LAYERS];
			hydred_forb = new double[Defines.MAX_LAYERS];
			sTemp = new double[Defines.MAX_LAYERS];
			method = 0;
			dysum=new SW_SOILWAT_OUTPUTS();
			wksum=new SW_SOILWAT_OUTPUTS();
			mosum=new SW_SOILWAT_OUTPUTS();
			yrsum=new SW_SOILWAT_OUTPUTS();
			wkavg=new SW_SOILWAT_OUTPUTS();
			moavg=new SW_SOILWAT_OUTPUTS();
			yravg=new SW_SOILWAT_OUTPUTS();
			filePrefix = "";
		}
		protected void onClear() {
			snowdepth=surfaceWater=surfaceWater_evap=pet=aet=litter_evap=tree_evap=forb_evap=shrub_evap=grass_evap=0;
			litter_int=tree_int=forb_int=shrub_int=grass_int=0;
			for(int i=0; i<Defines.MAX_LAYERS; i++) {
				is_wet[i]=false;
				snowpack[i]=transpiration_tree[i]=transpiration_forb[i]=transpiration_shrub[i]=transpiration_grass[i]=0;
				evaporation[i]=drain[i]=hydred_tree[i]=hydred_forb[i]=hydred_shrub[i]=hydred_grass[i]=sTemp[i]=0;
				swcBulk[Defines.Yesterday][i]=0;
				swcBulk[Defines.Today][i] =0;
			}
			hist_use = false;
			method=0;
			yr.onClear();
			filePrefix="";
			dysum.onClear();
			wksum.onClear();
			mosum.onClear();
			yrsum.onClear();
			wkavg.onClear();
			moavg.onClear();
			yravg.onClear();
		}
		protected double[] get_vwcBulkRow(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.vwcBulk[i]/SW_Soils.getWidths()[i];
					break;
				case SW_WEEK:
					tmp[i] = wkavg.vwcBulk[i]/SW_Soils.getWidths()[i];
					break;
				case SW_MONTH:
					tmp[i] = moavg.vwcBulk[i]/SW_Soils.getWidths()[i];
					break;
				case SW_YEAR:
					tmp[i] = yravg.vwcBulk[i]/SW_Soils.getWidths()[i];
					break;
				}			
			}
			return tmp;
		}
		protected double[] get_vwcMatricRow(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double convert=0;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				convert = 1. / (1. - SW_Soils.getLayer(i).fractionVolBulk_gravel) / SW_Soils.getWidths()[i];
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.vwcMatric[i]*convert;
					break;
				case SW_WEEK:
					tmp[i] = wkavg.vwcMatric[i]*convert;
					break;
				case SW_MONTH:
					tmp[i] = moavg.vwcMatric[i]*convert;
					break;
				case SW_YEAR:
					tmp[i] = yravg.vwcMatric[i]*convert;
					break;
				}
			}
			return tmp;
		}
		protected double[] get_swcBulkRow(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.swcBulk[i];
					break;
				case SW_WEEK:
					tmp[i] = wkavg.swcBulk[i];
					break;
				case SW_MONTH:
					tmp[i] = moavg.swcBulk[i];
					break;
				case SW_YEAR:
					tmp[i] = yravg.swcBulk[i];
					break;
				}
			}
			return tmp;
		}
		protected double[] get_swpMatricRow(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, dysum.swpMatric[i], SW_Soils.getWidths()[i],
							SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, 0, 0, i);
					break;
				case SW_WEEK:
					tmp[i] = SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, wkavg.swpMatric[i], SW_Soils.getWidths()[i],
							SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, 0, 0, i);
					break;
				case SW_MONTH:
					tmp[i] = SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, moavg.swpMatric[i], SW_Soils.getWidths()[i],
							SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, 0, 0, i);
					break;
				case SW_YEAR:
					tmp[i] = SW_SWCbulk2SWPmatric(SW_Soils.getLayer(i).fractionVolBulk_gravel, yravg.swpMatric[i], SW_Soils.getWidths()[i],
							SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).thetasMatric, SW_Soils.getLayer(i).bMatric, 0, 0, i);
					break;
				}
			}
			return tmp;
		}
		protected double[] get_swaBulkRow(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.swaBulk[i];
					break;
				case SW_WEEK:
					tmp[i] = wkavg.swaBulk[i];
					break;
				case SW_MONTH:
					tmp[i] = moavg.swaBulk[i];
					break;
				case SW_YEAR:
					tmp[i] = yravg.swaBulk[i];
					break;
				}
			}
			return tmp;
		}
		protected double[] get_swaMatricRow(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double convert=0;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				convert = 1. / (1. - SW_Soils.getLayer(i).fractionVolBulk_gravel);
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.swaMatric[i]*convert;
					break;
				case SW_WEEK:
					tmp[i] = wkavg.swaMatric[i]*convert;
					break;
				case SW_MONTH:
					tmp[i] = moavg.swaMatric[i]*convert;
					break;
				case SW_YEAR:
					tmp[i] = yravg.swaMatric[i]*convert;
					break;
				}
			}
			return tmp;
		}
		protected double[] get_surfaceWater(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.surfaceWater};
			case SW_WEEK:
				return new double[] {wkavg.surfaceWater};
			case SW_MONTH:
				return new double[] {moavg.surfaceWater};
			case SW_YEAR:
				return new double[] {yravg.surfaceWater};
			default:
				return null;
			}
		}
		protected double[] get_transp(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr*5];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i+(nlyr*0)] = dysum.transp_total[i];
					tmp[i+(nlyr*1)] = dysum.transp_tree[i];
					tmp[i+(nlyr*2)] = dysum.transp_shrub[i];
					tmp[i+(nlyr*3)] = dysum.transp_forb[i];
					tmp[i+(nlyr*4)] = dysum.transp_grass[i];
					break;
				case SW_WEEK:
					tmp[i+(nlyr*0)] = wkavg.transp_total[i];
					tmp[i+(nlyr*1)] = wkavg.transp_tree[i];
					tmp[i+(nlyr*2)] = wkavg.transp_shrub[i];
					tmp[i+(nlyr*3)] = wkavg.transp_forb[i];
					tmp[i+(nlyr*4)] = wkavg.transp_grass[i];
					break;
				case SW_MONTH:
					tmp[i+(nlyr*0)] = moavg.transp_total[i];
					tmp[i+(nlyr*1)] = moavg.transp_tree[i];
					tmp[i+(nlyr*2)] = moavg.transp_shrub[i];
					tmp[i+(nlyr*3)] = moavg.transp_forb[i];
					tmp[i+(nlyr*4)] = moavg.transp_grass[i];
					break;
				case SW_YEAR:
					tmp[i+(nlyr*0)] = yravg.transp_total[i];
					tmp[i+(nlyr*1)] = yravg.transp_tree[i];
					tmp[i+(nlyr*2)] = yravg.transp_shrub[i];
					tmp[i+(nlyr*3)] = yravg.transp_forb[i];
					tmp[i+(nlyr*4)] = yravg.transp_grass[i];
					break;
				}
			}
			return tmp;
		}
		protected double[] get_evapSoil(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_evap_lyrs;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.evap[i];
					break;
				case SW_WEEK:
					tmp[i] = wkavg.evap[i];
					break;
				case SW_MONTH:
					tmp[i] = moavg.evap[i];
					break;
				case SW_YEAR:
					tmp[i] = yravg.evap[i];
					break;
				}
			}
			return tmp;
		}
		protected double[] get_evapSurface(SW_OUTPUT.OutPeriod pd) {
			double[] tmp = new double[7];
			switch (pd) {
			case SW_DAY:
				tmp[0] = dysum.total_evap;
				tmp[1] = dysum.tree_evap;
				tmp[2] = dysum.forb_evap;
				tmp[3] = dysum.shrub_evap;
				tmp[4] = dysum.grass_evap;
				tmp[5] = dysum.litter_evap;
				tmp[6] = dysum.surfaceWater_evap;
				break;
			case SW_WEEK:
				tmp[0] = wkavg.total_evap;
				tmp[1] = wkavg.tree_evap;
				tmp[2] = wkavg.forb_evap;
				tmp[3] = wkavg.shrub_evap;
				tmp[4] = wkavg.grass_evap;
				tmp[5] = wkavg.litter_evap;
				tmp[6] = wkavg.surfaceWater_evap;
				break;
			case SW_MONTH:
				tmp[0] = moavg.total_evap;
				tmp[1] = moavg.tree_evap;
				tmp[2] = moavg.forb_evap;
				tmp[3] = moavg.shrub_evap;
				tmp[4] = moavg.grass_evap;
				tmp[5] = moavg.litter_evap;
				tmp[6] = moavg.surfaceWater_evap;
				break;
			case SW_YEAR:
				tmp[0] = yravg.total_evap;
				tmp[1] = yravg.tree_evap;
				tmp[2] = yravg.forb_evap;
				tmp[3] = yravg.shrub_evap;
				tmp[4] = yravg.grass_evap;
				tmp[5] = yravg.litter_evap;
				tmp[6] = yravg.surfaceWater_evap;
				break;
			}
			return tmp;
		}
		protected double[] get_interception(SW_OUTPUT.OutPeriod pd) {
			double[] tmp = new double[6];
			switch (pd) {
			case SW_DAY:
				tmp[0] = dysum.total_int;
				tmp[1] = dysum.tree_int;
				tmp[2] = dysum.forb_int;
				tmp[3] = dysum.shrub_int;
				tmp[4] = dysum.grass_int;
				tmp[5] = dysum.litter_int;
				break;
			case SW_WEEK:
				tmp[0] = wkavg.total_int;
				tmp[1] = wkavg.tree_int;
				tmp[2] = wkavg.forb_int;
				tmp[3] = wkavg.shrub_int;
				tmp[4] = wkavg.grass_int;
				tmp[5] = wkavg.litter_int;
				break;
			case SW_MONTH:
				tmp[0] = moavg.total_int;
				tmp[1] = moavg.tree_int;
				tmp[2] = moavg.forb_int;
				tmp[3] = moavg.shrub_int;
				tmp[4] = moavg.grass_int;
				tmp[5] = moavg.litter_int;
				break;
			case SW_YEAR:
				tmp[0] = yravg.total_int;
				tmp[1] = yravg.tree_int;
				tmp[2] = yravg.forb_int;
				tmp[3] = yravg.shrub_int;
				tmp[4] = yravg.grass_int;
				tmp[5] = yravg.litter_int;
				break;
			}
			return tmp;
		}
		protected double[] get_lyrdrain(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers-1;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.lyrdrain[i];
					break;
				case SW_WEEK:
					tmp[i] = wkavg.lyrdrain[i];
					break;
				case SW_MONTH:
					tmp[i] = moavg.lyrdrain[i];
					break;
				case SW_YEAR:
					tmp[i] = yravg.lyrdrain[i];
					break;
				}
			}
			return tmp;
		}
		protected double[] get_hydred(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr*5];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i+(nlyr*0)] = dysum.hydred_total[i];
					tmp[i+(nlyr*1)] = dysum.hydred_tree[i];
					tmp[i+(nlyr*2)] = dysum.hydred_shrub[i];
					tmp[i+(nlyr*3)] = dysum.hydred_forb[i];
					tmp[i+(nlyr*4)] = dysum.hydred_grass[i];
					break;
				case SW_WEEK:
					tmp[i+(nlyr*0)] = wkavg.hydred_total[i];
					tmp[i+(nlyr*1)] = wkavg.hydred_tree[i];
					tmp[i+(nlyr*2)] = wkavg.hydred_shrub[i];
					tmp[i+(nlyr*3)] = wkavg.hydred_forb[i];
					tmp[i+(nlyr*4)] = wkavg.hydred_grass[i];
					break;
				case SW_MONTH:
					tmp[i+(nlyr*0)] = moavg.hydred_total[i];
					tmp[i+(nlyr*1)] = moavg.hydred_tree[i];
					tmp[i+(nlyr*2)] = moavg.hydred_shrub[i];
					tmp[i+(nlyr*3)] = moavg.hydred_forb[i];
					tmp[i+(nlyr*4)] = moavg.hydred_grass[i];
					break;
				case SW_YEAR:
					tmp[i+(nlyr*0)] = yravg.hydred_total[i];
					tmp[i+(nlyr*1)] = yravg.hydred_tree[i];
					tmp[i+(nlyr*2)] = yravg.hydred_shrub[i];
					tmp[i+(nlyr*3)] = yravg.hydred_forb[i];
					tmp[i+(nlyr*4)] = yravg.hydred_grass[i];
					break;
				}
			}
			return tmp;
		}
		protected double[] get_aet(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.aet};
			case SW_WEEK:
				return new double[] {wkavg.aet};
			case SW_MONTH:
				return new double[] {moavg.aet};
			case SW_YEAR:
				return new double[] {yravg.aet};
			default:
				return null;
			}
		}
		protected double[] get_pet(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.pet};
			case SW_WEEK:
				return new double[] {wkavg.pet};
			case SW_MONTH:
				return new double[] {moavg.pet};
			case SW_YEAR:
				return new double[] {yravg.pet};
			default:
				return null;
			}
		}
		protected double[] get_wetdays(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = is_wet[i]?1.:0.;
					break;
				case SW_WEEK:
					tmp[i] = wkavg.wetdays[i];
					break;
				case SW_MONTH:
					tmp[i] = moavg.wetdays[i];
					break;
				case SW_YEAR:
					tmp[i] = yravg.wetdays[i];
					break;
				}
			}
			return tmp;
		}
		protected double[] get_snowpack(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.snowpack, dysum.snowdepth};
			case SW_WEEK:
				return new double[] {wkavg.snowpack, wkavg.snowdepth};
			case SW_MONTH:
				return new double[] {moavg.snowpack, moavg.snowdepth};
			case SW_YEAR:
				return new double[] {yravg.snowpack, yravg.snowdepth};
			default:
				return null;
			}
		}
		protected double[] get_deepswc(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.deep};
			case SW_WEEK:
				return new double[] {wkavg.deep};
			case SW_MONTH:
				return new double[] {moavg.deep};
			case SW_YEAR:
				return new double[] {yravg.deep};
			default:
				return null;
			}
		}
		public double[] get_soiltemp(SW_OUTPUT.OutPeriod pd) {
			int nlyr = SW_Soils.getLayersInfo().n_layers;
			double[] tmp = new double[nlyr];
			for(int i=0; i<nlyr; i++) {
				switch (pd) {
				case SW_DAY:
					tmp[i] = dysum.sTemp[i];
					break;
				case SW_WEEK:
					tmp[i] = wkavg.sTemp[i];
					break;
				case SW_MONTH:
					tmp[i] = moavg.sTemp[i];
					break;
				case SW_YEAR:
					tmp[i] = yravg.sTemp[i];
					break;
				}
			}
			return tmp;
		}
	}
	
	private SOILWAT soilwat;
	private SW_SOILWAT_HISTORY hist;
	private double temp_snow;
	private boolean data;
	
	private SW_MODEL SW_Model;
	private SW_FLOW SW_Flow;
	private SW_SOILS SW_Soils;
	private SW_SITE SW_Site;
	
	protected SW_SOILWATER(SW_MODEL SW_Model, SW_SITE SW_Site, SW_SOILS SW_Soils, SW_WEATHER SW_Weather, SW_VEGPROD SW_VegProd, SW_SKY SW_Sky) {
		this.soilwat = new SOILWAT();
		this.hist = new SW_SOILWAT_HISTORY();
		this.data = false;
		this.SW_Model = SW_Model;
		this.SW_Soils = SW_Soils;
		this.SW_Site = SW_Site;
		this.SW_Flow = new SW_FLOW(SW_Model, SW_Site, SW_Soils, this, SW_Weather, SW_VegProd, SW_Sky);
		temp_snow = 0;
	}
	
	protected void onClear() {
		this.temp_snow = 0;
		this.soilwat.onClear();
		this.data = false;
	}
	
	protected boolean onVerify() {
		// gets the soil temperatures from where they are read in the SW_Site struct for use later
		// SW_Site.c must call it's read function before this, or it won't work
		for(int i=0; i<SW_Soils.getLayersInfo().n_layers; i++) {
			soilwat.sTemp[i] = SW_Soils.getLayer(i).sTemp;
		}
		soilwat.yr.equals(SW_Model.getEndYear());
		
		return true;
	}
	protected void onSetInput(SWC_INPUT_DATA swcSetupIn) {
		soilwat.hist_use = swcSetupIn.hist_use;
		soilwat.filePrefix = swcSetupIn.filePrefix;
		soilwat.yr.setFirst(swcSetupIn.yr.getFirst());
		soilwat.method = swcSetupIn.method;
		this.data = true;
	}
	protected void onGetInput(SWC_INPUT_DATA swcSetupIn) {
		swcSetupIn.hist_use = soilwat.hist_use;
		swcSetupIn.filePrefix = soilwat.filePrefix;
		swcSetupIn.yr.setFirst(soilwat.yr.getFirst());
		swcSetupIn.method = soilwat.method;
	}
	protected void onSetHist(SW_SOILWAT_HISTORY hist) {
		this.hist = hist;
	}
	protected void onGetHist(SW_SOILWAT_HISTORY hist) {
		hist = this.hist;
	}
	protected void onReadHist(Path WeatherHistoryFolder) {
		//We only read the sw hist if hist_use is set and
		//only grab the years set in setup file.
		if(soilwat.hist_use)
			try {
				if(SW_Model.getStartYear() >= soilwat.yr.getFirst())
					hist.onRead(WeatherHistoryFolder, soilwat.filePrefix, SW_Model.getStartYear(), SW_Model.getEndYear());
				else
					hist.onRead(WeatherHistoryFolder, soilwat.filePrefix, soilwat.yr.getFirst(), SW_Model.getEndYear());
			} catch (IOException e) {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogMode.ERROR, "SwcSetupIn onReadHist : Problem.");
			}
	}
	protected void onRead(Path swcSetupIn, Path WeatherHistoryFolder) throws IOException {
		int nitems=4, lineno=0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(swcSetupIn, StandardCharsets.UTF_8);
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				switch (lineno) {
				case 0:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Expected only one value for swc history use line.");
					try {
						soilwat.hist_use = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Could not convert swc history use line.");
					}
					break;
				case 1:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Expected only one value for swc file prefix.");
					soilwat.filePrefix = values[0];
					break;
				case 2:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Expected only one value for swc history start year.");
					try {
						soilwat.yr.setFirst(Integer.parseInt(values[0]));
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Could not convert swc first year line.");
					}
					break;
				case 3:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Expected only one value for method.");
					try {
						soilwat.method = Integer.parseInt(values[0]);
						if(soilwat.method < 1 || soilwat.method >2)
							f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Invalid swc adjustment method.");
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Could not convert method line.");
					}
					break;
				default:
					System.out.println(line);
					if(lineno > nitems)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Too many lines.");
					break;
				}
				lineno++;
			}
		}
		if(lineno < nitems)
			f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Too few lines.");
		
		onReadHist(WeatherHistoryFolder);
		this.data = true;
	}
	protected void onWrite(Path swcSetupIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Setup parameters for measured swc");
			lines.add("# Location: -");
			lines.add("#");
			lines.add(String.valueOf(soilwat.hist_use?1:0)+"\t\t"+"# 1=use swcdata history data file, 0= don't use");
			lines.add(soilwat.filePrefix+"\t\t"+"# input data file prefix");
			lines.add(String.valueOf(soilwat.yr.getFirst())+"\t\t"+"# first year of measurement data files");
			lines.add(String.valueOf(soilwat.method)+"\t\t"+"# first year of measurement data files ");
			Files.write(swcSetupIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "SwcSetupIn : onWrite : No data.");
		}
	}
	protected void onWriteHistory(Path WeatherHistoryFolder, String prefix) throws IOException {
		this.hist.onWrite(WeatherHistoryFolder, prefix);
	}
	protected void SW_SWC_water_flow() {
		/* =================================================== */
		/* Adjust SWC according to historical (measured) data
		 * if available, compute water flow, and check if swc
		 * is above threshold for "wet" condition.
		 */

		/* if there's no swc observation for today,
		 * it shows up as SW_MISSING.  The input must
		 * define historical swc for at least the top
		 * layer to be recognized.
		 * IMPORTANT: swc can't be adjusted on day 1 of first year of simulation.
		 10/25/2010	(drs)	in SW_SWC_water_flow(): replaced test that "swc can't be adjusted on day 1 of year 1" to "swc can't be adjusted on start day of first year of simulation"
		 */

		if (soilwat.hist_use && !hist.swcMissing(SW_Model.getDOY())) {
			if (!(SW_Model.getDOY() == SW_Model.getFirstDayOfFirstYear() && SW_Model.getYear() == SW_Model.getStartYear())) {
				SW_SWC_adjust_swc(SW_Model.getDOY());
			} else {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogMode.WARN, "Attempt to set SWC on start day of first year of simulation disallowed.");
			}

		} else {
			SW_Flow.SW_Water_Flow();
		}

		for(int i=0; i<SW_Soils.getLayersInfo().n_layers; i++)
			soilwat.is_wet[i] = (Defines.GE( soilwat.swcBulk[Defines.Today][i], SW_Soils.getLayer(i).swcBulk_wet));
	}
	protected void SW_SWC_end_doy() {
		for(int i=0; i<SW_Soils.getLayersInfo().n_layers; i++) {
			this.soilwat.swcBulk[Defines.Yesterday][i] = soilwat.swcBulk[Defines.Today][i];
		}
		soilwat.snowpack[Defines.Yesterday] = soilwat.snowpack[Defines.Today];
	}
	protected void SW_SWC_new_year() {
		int year = SW_Model.getYear();
		int Today = Defines.Today;
		int Yesterday = Defines.Yesterday;
		
		boolean reset = (SW_Site.getModel().flags.reset_yr || SW_Model.getYear()==SW_Model.getStartYear());
		soilwat.yrsum.onClear();
		
		/* reset the swc */
		for(int lyr=0; lyr<SW_Soils.getLayersInfo().n_layers; lyr++) {
			if(reset) {
				soilwat.swcBulk[Today][lyr] = soilwat.swcBulk[Yesterday][lyr] = SW_Soils.getLayer(lyr).swcBulk_init;
				soilwat.drain[lyr]=0;
			} else {
				soilwat.swcBulk[Today][lyr] = soilwat.swcBulk[Yesterday][lyr];
			}
		}
		
		/* reset the snowpack */
		if (reset) {
			soilwat.snowpack[Today] = soilwat.snowpack[Yesterday] = 0.;
		} else {
			soilwat.snowpack[Today] = soilwat.snowpack[Yesterday];
		}
		
		/* reset the historical (measured) values, if needed */
		if (soilwat.hist_use && year >= hist.getFirstYear()) {
			hist.setCurrentYear(year);
		}
		if(SW_Site.getDeepdrain()) {
			soilwat.swcBulk[Today][SW_Soils.getLayersInfo().deep_lyr] = 0.;
		}
	}
	protected void SW_SWC_adjust_swc(int doy) {
		/* =================================================== */
		/* 01/07/02 (cwb) added final loop to guarantee swc > swcBulk_min
		 */
		doy--;
		double upper, lower;
		int Today = Defines.Today;
		//int Yesterday = TwoDays.Yesterday.ordinal();
		
		switch (soilwat.method) {
		case SW_Adjust_Avg:
			for(int lyr=0; lyr<SW_Soils.getLayersInfo().n_layers; lyr++)
			{
				soilwat.swcBulk[Today][lyr] += hist.getSWC(doy)[lyr];
				soilwat.swcBulk[Today][lyr] /= 2.;
			}
			break;
		case SW_Adjust_StdErr:
			for(int lyr=0; lyr<SW_Soils.getLayersInfo().n_layers; lyr++)
			{
				upper = hist.getSWC(doy)[lyr] + hist.getStd_err(doy)[lyr];
				lower = hist.getSWC(doy)[lyr] - hist.getStd_err(doy)[lyr];
				if (Defines.GT(soilwat.swcBulk[Today][lyr], upper))
					soilwat.swcBulk[Today][lyr] = upper;
				else if (Defines.LT(soilwat.swcBulk[Today][lyr], lower))
					soilwat.swcBulk[Today][lyr] = lower;
			}
			break;

		default:
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, "SW_SOILWATER SW_SWC_adjust_swc : Invalid SWC adjustment method.");
		}

		/* this will guarantee that any method will not lower swc */
		/* below the minimum defined for the soil layers          */
		for(int lyr=0; lyr<SW_Soils.getLayersInfo().n_layers; lyr++)
			soilwat.swcBulk[Today][lyr] = Math.max(soilwat.swcBulk[Today][lyr], SW_Soils.getLayer(lyr).swcBulk_min);
	}
	protected void SW_SWC_adjust_snow(SW_WEATHER_2DAYS now) {
		/*---------------------
		 10/04/2010	(drs) added snowMAUS snow accumulation, sublimation and melt algorithm: Trnka, M., Kocmánková, E., Balek, J., Eitzinger, J., Ruget, F., Formayer, H., Hlavinka, P., Schaumberger, A., Horáková, V., Mozny, M. & Zalud, Z. (2010) Simple snow cover model for agrometeorological applications. Agricultural and Forest Meteorology, 150, 1115-1127.
		 replaced SW_SWC_snow_accumulation, SW_SWC_snow_sublimation, and SW_SWC_snow_melt with SW_SWC_adjust_snow
		 10/19/2010	(drs) replaced snowMAUS simulation with SWAT2K routines: Neitsch S, Arnold J, Kiniry J, Williams J. 2005. Soil and water assessment tool (SWAT) theoretical documentation. version 2005. Blackland Research Center, Texas Agricultural Experiment Station: Temple, TX.
		 Inputs:	temp_min: daily minimum temperature (C)
		 temp_max: daily maximum temperature (C)
		 ppt: daily precipitation (cm)
		 snowpack[Yesterday]: yesterday's snowpack (water-equivalent cm)
		 Outputs:	snowpack[Today], partitioning of ppt into rain and snow, snowmelt and snowloss
		 ---------------------*/
		int Today = Defines.Today;
		
		double doy = (double) SW_Model.getDOY();
		double temp_ave, Rmelt, snow_cov = 1., cov_soil = 0.5, SnowAccu = 0., SnowMelt = 0., SnowLoss = 0.;

		temp_ave = (now.temp_min[Today] + now.temp_max[Today]) / 2.;
		/* snow accumulation */
		if (Double.compare(temp_ave, SW_Site.getSnow().TminAccu2)<=0) {
			SnowAccu = now.ppt[Today];
		} else {
			SnowAccu = 0.;
		}
		now.rain[Today] = Math.max(0., now.ppt[Today] - SnowAccu);
		now.snow[Today] = Math.max(0., SnowAccu);
		soilwat.snowpack[Today] += SnowAccu;

		/* snow melt */
		Rmelt = (SW_Site.getSnow().RmeltMax + SW_Site.getSnow().RmeltMin) / 2. + Math.sin((doy - 81.) / 58.09) * (SW_Site.getSnow().RmeltMax - SW_Site.getSnow().RmeltMin) / 2.;
		temp_snow = temp_snow * (1 - SW_Site.getSnow().lambdasnow) + temp_ave * SW_Site.getSnow().lambdasnow;
		if (Double.compare(temp_snow, SW_Site.getSnow().TmaxCrit) > 0) {
			SnowMelt = Math.min( soilwat.snowpack[Today], Rmelt * snow_cov * ((temp_snow + now.temp_max[Today])/2. - SW_Site.getSnow().TmaxCrit) );
		} else {
			SnowMelt = 0.;
		}
		if (Double.compare(soilwat.snowpack[Today], 0.) > 0) {
			now.snowmelt[Today] = Math.max(0., SnowMelt);
			soilwat.snowpack[Today] = Math.max(0., soilwat.snowpack[Today] - now.snowmelt[Today] );
		} else {
			now.snowmelt[Today] = 0.;
		}

		/* snow loss through sublimation and other processes */
		SnowLoss = Math.min( soilwat.snowpack[Today], cov_soil * soilwat.pet );
		if (Double.compare(soilwat.snowpack[Today], 0.) > 0) {
			now.snowloss[Today] = Math.max(0., SnowLoss);
			soilwat.snowpack[Today] = Math.max(0., soilwat.snowpack[Today] - now.snowloss[Today] );
		} else {
			now.snowloss[Today] = 0.;
		}
	}
	protected static double SW_SnowDepth(double SWE, double snowdensity) {
		/*---------------------
		 08/22/2011	(drs)	calculates depth of snowpack
		 Input:	SWE: snow water equivalents (cm = 10kg/m2)
		 snowdensity (kg/m3)
		 Output: snow depth (cm)
		 ---------------------*/
		if(Defines.GT(snowdensity, 0.)) {
			return SWE/snowdensity * 10. * 100.;
		} else {
			return 0.;
		}
	}
	protected static double SW_SWCbulk2SWPmatric(double fractionGravel, double swcBulk, double width, double psisMatric, double thetasMatric, double bMatric, int year, int doy, int lyr) {
		/**********************************************************************
		 PURPOSE: Calculate the soil water potential or the soilwater
		 content of the current layer,
		 as a function of soil texture at the layer.

		 DATE:  April 2, 1992

		 HISTORY:
		 9/1/92  (SLC) if swc comes in as zero, set swpotentl to
		 upperbnd.  (Previously, we flagged this
		 as an error, and set swpotentl to zero).

		 27-Aug-03 (cwb) removed the upperbnd business. Except for
		 missing values, swc < 0 is impossible, so it's an error,
		 and the previous limit of swp to 80 seems unreasonable.
		 return 0.0 if input value is MISSING

		 INPUTS:
		 swcBulk - soilwater content of the current layer (cm/layer)
		 n   - layer number to index the **lyr pointer.

		 These are the values for each layer obtained via lyr[n]:
		 width  - width of current soil layer
		 psisMatric   - "saturation" matric potential
		 thetasMatric - saturated moisture content.
		 bMatric       - see equation below.
		 swc_lim - limit for matric potential

		 LOCAL VARIABLES:
		 theta1 - volumetric soil water content

		 DEFINED CONSTANTS:
		 barconv - conversion factor from bars to cm water.  (i.e.
		 1 bar = 1024cm water)

		 COMMENT:
		 See the routine "watreqn" for a description of how the variables
		 psisMatric, bMatric, binverseMatric, thetasMatric are initialized.

		 OUTPUTS:
		 swpotentl - soilwater potential of the current layer
		 (if swflag=TRUE)
		 or
		 soilwater content (if swflag=FALSE)

		 DESCRIPTION: The equation and its coefficients are based on a
		 paper by Cosby,Hornberger,Clapp,Ginn,  in WATER RESOURCES RESEARCH
		 June 1984.  Moisture retention data was fit to the power function

		 **********************************************************************/
		
		double theta1, swp = 0.;

		if (Double.compare(swcBulk, SW_SOILWAT_HISTORY.MISSING) == 0 || Double.compare(swcBulk,0.)==0)
			return 0.0;

		if (Defines.GT(swcBulk, 0.0)) {
			if (Defines.EQ(fractionGravel,1.0))
				theta1 = 0.0;
			else
				theta1 = (swcBulk / width) * 100. / (1. - fractionGravel);
			swp = psisMatric / Defines.powe(theta1/thetasMatric, bMatric) / Defines.BARCONV;
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, String.format("Invalid SWC value (%.4f) in SW_SWC_swc2potential.\n"+
					"    Year = %d, DOY=%d, Layer = %d\n", swcBulk, year, doy, lyr));
		}

		return swp;
	}
	protected static double SW_SWPmatric2VWCBulk(double fractionGravel, double swpMatric,  double psisMatric, double binverseMatric, double thetasMatric) {
		/* =================================================== */
		/* used to be swfunc in the fortran version */
		/* 27-Aug-03 (cwb) moved from the Site module. */
		/* return the volume as cmH2O/cmSOIL */
		double t, p;

		swpMatric *= Defines.BARCONV;
		p = Defines.powe(psisMatric / swpMatric, binverseMatric); //x^y == exponential(y * ln(x)) or e^(y * ln(x)).  NOTE: this will only work when x > 0 I believe
		t = thetasMatric * p * 0.01 * (1 - fractionGravel);
		return (t);
	}
	protected static double SW_VWCBulkRes(double fractionGravel, double sand, double clay, double porosity) {
		/*---------------------
		 02/03/2012	(drs)	calculates 'Brooks-Corey' residual volumetric soil water based on Rawls WJ, Brakensiek DL (1985) Prediction of soil water properties for hydrological modeling. In Watershed management in the Eighties (eds Jones EB, Ward TJ), pp. 293-299. American Society of Civil Engineers, New York.
		 however, equation is only valid if (0.05 < clay < 0.6) & (0.05 < sand < 0.7)

		 Input:	sand: soil texture sand content (fraction)
		 clay: soil texture clay content (fraction)
		 porosity: soil porosity = saturated VWC (fraction)
		 Output: residual volumetric soil water (cm/cm)
		 ---------------------*/
		double res;

		sand *= 100.;
		clay *= 100.;

		res = (-0.0182482 + 0.00087269 * sand + 0.00513488 * clay + 0.02939286 * porosity - 0.00015395 * Defines.squared(clay) - 0.0010827 * sand * porosity
				- 0.00018233 * Defines.squared(clay) * Defines.squared(porosity) + 0.00030703 * Defines.squared(clay) * porosity - 0.0023584 * Defines.squared(porosity) * clay) * (1 - fractionGravel);

		return (Math.max(res, 0.));
	}
	
	protected SOILWAT getSoilWat() {
		return this.soilwat;
	}
}
