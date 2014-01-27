package data;

import input.LogFileIn;
import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import defines.Defines;

public class SW_SOILWATER {
	
	public class SW_SOILWAT_OUTPUTS {
		public double surfaceWater, total_evap, surfaceWater_evap, tree_evap, forb_evap, shrub_evap,
			grass_evap, litter_evap, total_int, tree_int, forb_int, shrub_int, grass_int, litter_int, snowpack, snowdepth, et, aet, pet, deep;
		public double[] wetdays, vwcBulk, /* soil water content cm/cm */
		vwcMatric, swcBulk, /* soil water content cm/layer */
		swpMatric, /* soil water potential */
		swaBulk, /* available soil water cm/layer, swc-(wilting point) */
		swaMatric, transp_total, transp_tree, transp_forb, transp_shrub, transp_grass, evap,
				lyrdrain, hydred_total, hydred_tree, /* hydraulic redistribution cm/layer */
				hydred_forb, hydred_shrub, hydred_grass, 
				sTemp; // soil temperature in celcius for each layer
		public SW_SOILWAT_OUTPUTS() {
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
			hydred_grass = new double[Defines.MAX_LAYERS];
			hydred_shrub = new double[Defines.MAX_LAYERS];
			hydred_tree = new double[Defines.MAX_LAYERS];
			hydred_forb = new double[Defines.MAX_LAYERS];
			sTemp = new double[Defines.MAX_LAYERS];
		
		}
	}
	
	public class SOILWAT {
		/* current daily soil water related values */
		public boolean[] is_wet; /* swc sufficient to count as wet today */
		public double snowdepth, surfaceWater, surfaceWater_evap, pet, aet, litter_evap, tree_evap, forb_evap,
			shrub_evap, grass_evap, litter_int, tree_int, forb_int, shrub_int, grass_int;
		public double[] snowpack, /* swe of snowpack, if accumulation flag set */
		transpiration_tree, transpiration_forb, transpiration_shrub, transpiration_grass, evaporation,
				drain, /* amt of swc able to drain from curr layer to next */
				hydred_tree, /* hydraulic redistribution cm/layer */
				hydred_forb, hydred_shrub, hydred_grass, 
				sTemp; // soil temperature
		public double[][] swcBulk;
		SW_SOILWAT_OUTPUTS dysum, /* helpful placeholder */
		wksum, mosum, yrsum, /* accumulators for *avg */
		wkavg, moavg, yravg; /* averages or sums as appropriate */
		boolean hist_use;
		
		public SOILWAT() {
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
		}
	}
	
	
	public SW_SOILWATER() {
		
	}
	
	public void onRead(Path swcSetupIn) throws IOException {
		int nitems=4, lineno=0;
		//TODO: copy over soil temp from soils to soilwat stemp
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
						this.useSWCHistoryData = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Could not convert swc history use line.");
					}
					break;
				case 1:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Expected only one value for swc file prefix.");
					this.filePrefix = values[0];
					break;
				case 2:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Expected only one value for swc history start year.");
					try {
						this.yr.setFirst(Integer.parseInt(values[0]));
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Could not convert swc first year line.");
					}
					break;
				case 3:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "SwcSetupIn onRead : Expected only one value for method.");
					try {
						this.method = Integer.parseInt(values[0]);
						if(this.method < 1 || this.method >2)
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
		this.data = true;
	}
	public void onWrite(Path swcSetupIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Setup parameters for measured swc");
			lines.add("# Location: -");
			lines.add("#");
			lines.add(String.valueOf(this.useSWCHistoryData?1:0)+"\t\t"+"# 1=use swcdata history data file, 0= don't use");
			lines.add(this.filePrefix+"\t\t"+"# input data file prefix");
			lines.add(String.valueOf(this.yr.getFirst())+"\t\t"+"# first year of measurement data files");
			lines.add(String.valueOf(this.method)+"\t\t"+"# first year of measurement data files ");
			Files.write(swcSetupIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "SwcSetupIn : onWrite : No data.");
		}
	}
	
	public static double SW_SWPmatric2VWCBulk(double fractionGravel, double swpMatric,  double psisMatric, double binverseMatric, double thetasMatric) {
		/* =================================================== */
		/* used to be swfunc in the fortran version */
		/* 27-Aug-03 (cwb) moved from the Site module. */
		/* return the volume as cmH2O/cmSOIL */
		double t, p;

		swpMatric *= Defines.BARCONV;
		p = Math.pow(psisMatric / swpMatric, binverseMatric);
		t = thetasMatric * p * 0.01 * (1 - fractionGravel);
		return (t);
	}
	
	public static double SW_VWCBulkRes(double fractionGravel, double sand, double clay, double porosity) {
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

		res = (-0.0182482 + 0.00087269 * sand + 0.00513488 * clay + 0.02939286 * porosity - 0.00015395 * Math.pow(clay,2.0) - 0.0010827 * sand * porosity
				- 0.00018233 * Math.pow(clay,2.0) * Math.pow(porosity,2.0) + 0.00030703 * Math.pow(clay,2.0) * porosity - 0.0023584 * Math.pow(porosity,2.0) * clay) * (1 - fractionGravel);

		return (Math.max(res, 0.));
	}
}
