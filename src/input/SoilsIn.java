package input;

import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import defines.Defines;

public class SoilsIn {
	private class SW_LAYER_INFO {
		public double depth,/*for printing out line*/
		width, /* width of the soil layer (cm) */
		soilBulk_density, /* bulk soil density, i.e., including gravel component, (g/cm3) */
		evap_coeff, /* prop. of total soil evap from this layer */
		transp_coeff_forb, transp_coeff_tree, transp_coeff_shrub, transp_coeff_grass, /* prop. of total transp from this layer    */
		soilMatric_density, /* matric soil density, i.e., gravel component excluded, (g/cm3) */
		fractionVolBulk_gravel, /* gravel content (> 2 mm) as volume-fraction of bulk soil (g/cm3) */
		fractionWeightMatric_sand, /* sand content (< 2 mm & > . mm) as weight-fraction of matric soil (g/g) */
		fractionWeightMatric_clay, /* clay content (< . mm & > . mm) as weight-fraction of matric soil (g/g) */
		swcBulk_fieldcap, /* field_cap * width                        */
		swcBulk_wiltpt, /* wilting_pt * width                       */
		swcBulk_wet, /* swc considered "wet" (cm) *width         */
		swcBulk_init, /* start the model at this swc (cm) *width  */
		swcBulk_min, /* swc cannot go below this (cm) *width     */
		swcBulk_saturated, /* saturated soil water content (cm) * width */
		impermeability, /* fraction of how impermeable a layer is (0=permeable, 1=impermeable)    */
		swcBulk_atSWPcrit_forb, swcBulk_atSWPcrit_tree, swcBulk_atSWPcrit_shrub, swcBulk_atSWPcrit_grass, /* swc at the critical soil water potential */

		thetasMatric, /* This group is parameters for */
		psisMatric, /* Cosby et al. (1982) SWC <-> SWP */
		bMatric, /* conversion functions. */
		binverseMatric,

		sTemp; /* initial soil temperature for each soil layer */
		
		public int my_transp_rgn_forb, my_transp_rgn_tree, my_transp_rgn_shrub, my_transp_rgn_grass; /* which transp zones from Site am I in? */
	
		public String toString() {
			return String.format("%7.3f %10.3f      %-12.3f %7.3f %13.3f %13.3f %12.3f %12.3f %8.3f %8.3f %9.3f %11.3f", this.depth, this.soilMatric_density, this.fractionVolBulk_gravel,
					this.evap_coeff, this.transp_coeff_grass, this.transp_coeff_shrub, this.transp_coeff_tree, this.transp_coeff_forb, this.fractionWeightMatric_sand,
					this.fractionWeightMatric_clay, this.impermeability, this.sTemp);
		}
		public void onClear() {
			my_transp_rgn_grass = 0;
			my_transp_rgn_shrub = 0;
			my_transp_rgn_tree = 0;
			my_transp_rgn_forb = 0;
		}
	}
	
	private SW_LAYER_INFO[] layers;
	private boolean data;
	private boolean deepdrainSet;
	private int n_layers, /* total number of soil layers */
	n_transp_rgn, /* soil layers are grouped into n transp. regions */
	n_evap_lyrs, /* number of layers in which evap is possible */
	n_transp_lyrs_forb, n_transp_lyrs_tree, n_transp_lyrs_shrub, n_transp_lyrs_grass, /* layer index of deepest transp. region       */
	deep_lyr; /* index of deep drainage layer if deepdrain, 0 otherwise */
	
	public SoilsIn() {
		this.layers = new SW_LAYER_INFO[Defines.MAX_LAYERS];
		for(int i=0; i<Defines.MAX_LAYERS;  i++)
			this.layers[i] = new SW_LAYER_INFO();
		this.data = false;
		this.deepdrainSet = false;
		this.n_layers = 0;
		this.n_transp_rgn=0;
		this.n_evap_lyrs=0;
		this.n_transp_lyrs_forb=this.n_transp_lyrs_grass=this.n_transp_lyrs_shrub=this.n_transp_lyrs_tree=0;
		this.deep_lyr=0;
	}
	
	public void onClear() {
		this.data = false;
		this.n_layers = 0;
		this.n_transp_rgn=0;
		this.n_evap_lyrs=0;
		this.n_transp_lyrs_forb=this.n_transp_lyrs_grass=this.n_transp_lyrs_shrub=this.n_transp_lyrs_tree=0;
		this.deep_lyr=0;
		this.deepdrainSet = false;
	}
	
	public void onVerify(boolean siteModelDeepdrain) {
		boolean fail = false;
		LogFileIn f = LogFileIn.getInstance();
		String message = "";
		
		if(siteModelDeepdrain && !this.deepdrainSet)
			setDeepdrain(true);
		
		for(int i=0; i<this.n_layers; i++) {
			if(this.layers[i].soilMatric_density < 0.0) {
				fail=true;
				message += "SoilsIn onVerify : Layer "+String.valueOf(i+1)+": matricd has to be greater than 0. matricd="+String.valueOf(this.layers[i].soilMatric_density)+"\n";
			} else if(this.layers[i].fractionVolBulk_gravel < 0.0 || this.layers[i].fractionVolBulk_gravel > 1.0) {
				fail=true;
				message += "SoilsIn onVerify : Layer "+String.valueOf(i+1)+": gravel_content has to be greater than 0 and less than 1. gravel_content="+String.valueOf(this.layers[i].fractionVolBulk_gravel)+"\n";
			} else if(this.layers[i].fractionWeightMatric_sand < 0.0) {
				fail=true;
				message += "SoilsIn onVerify : Layer "+String.valueOf(i+1)+": %sand has to be greater than 0. %sand="+String.valueOf(this.layers[i].fractionWeightMatric_sand)+"\n";
			} else if(this.layers[i].fractionWeightMatric_clay < 0.0) {
				fail=true;
				message += "SoilsIn onVerify : Layer "+String.valueOf(i+1)+": %clay has to be greater than 0. %clay="+String.valueOf(this.layers[i].fractionWeightMatric_clay)+"\n";
			} else if(this.layers[i].impermeability < 0.0) {
				fail=true;
				message += "SoilsIn onVerify : Layer "+String.valueOf(i+1)+": imperm has to be greater than 0. imperm="+String.valueOf(this.layers[i].soilMatric_density)+"\n";
			}
			
		}
		if(fail)
			f.LogError(LogFileIn.LogMode.ERROR, message);
	}
	
	public void onRead(Path soilsIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(soilsIn, StandardCharsets.UTF_8);
		this.data = false;
		this.n_layers=0;
		double dmin=0.0,dmax;
		boolean evap_ok = true, /* mitigate gaps in layers' evap coeffs */
				transp_ok_forb = true, transp_ok_tree = true, /* same for transpiration coefficients */
				transp_ok_shrub = true, /* same for transpiration coefficients */
				transp_ok_grass = true; /* same for transpiration coefficients */
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				if(values.length != 12)
					f.LogError(LogFileIn.LogMode.ERROR, "SoilsIn onRead : Expected 10 Values read "+String.valueOf(values.length));
				if(this.n_layers == Defines.MAX_LAYERS)
					f.LogError(LogFileIn.LogMode.ERROR, "SoilsIn onRead : Too many layers specified "+String.valueOf(values.length));
				try {
					dmax = Double.parseDouble(values[0]);
					this.layers[this.n_layers].depth = dmax;
					this.layers[this.n_layers].width = dmax-dmin;
					dmin = dmax;
					this.layers[this.n_layers].soilMatric_density = Double.parseDouble(values[1]);
					this.layers[this.n_layers].fractionVolBulk_gravel = Double.parseDouble(values[2]);
					this.layers[this.n_layers].evap_coeff = Double.parseDouble(values[3]);
					this.layers[this.n_layers].transp_coeff_grass = Double.parseDouble(values[4]);
					this.layers[this.n_layers].transp_coeff_shrub = Double.parseDouble(values[5]);
					this.layers[this.n_layers].transp_coeff_tree = Double.parseDouble(values[6]);
					this.layers[this.n_layers].transp_coeff_forb = Double.parseDouble(values[7]);
					this.layers[this.n_layers].fractionWeightMatric_sand = Double.parseDouble(values[8]);
					this.layers[this.n_layers].fractionWeightMatric_clay = Double.parseDouble(values[9]);
					this.layers[this.n_layers].impermeability = Double.parseDouble(values[10]);
					this.layers[this.n_layers].my_transp_rgn_grass = 0;
					this.layers[this.n_layers].my_transp_rgn_shrub = 0;
					this.layers[this.n_layers].my_transp_rgn_tree = 0;
					this.layers[this.n_layers].my_transp_rgn_forb = 0;
					this.layers[this.n_layers].sTemp = Double.parseDouble(values[11]);
					
				} catch(NumberFormatException e) {
					f.LogError(LogFileIn.LogMode.ERROR, "Soils onRead : Could not convert string to double. " + e.getMessage());
				}
				if(evap_ok) {
					if(this.layers[this.n_layers].evap_coeff > 0.0)
						this.n_evap_lyrs++;
					else
						evap_ok=false;
				}
				if(transp_ok_forb) {
					if(this.layers[this.n_layers].transp_coeff_forb > 0)
						this.n_transp_lyrs_forb++;
					else
						transp_ok_forb=false;
				}
				if(transp_ok_tree) {
					if(this.layers[this.n_layers].transp_coeff_tree > 0)
						this.n_transp_lyrs_tree++;
					else
						transp_ok_tree=false;
				}
				if(transp_ok_shrub) {
					if(this.layers[this.n_layers].transp_coeff_shrub > 0)
						this.n_transp_lyrs_shrub++;
					else
						transp_ok_shrub=false;
				}
				if(transp_ok_grass) {
					if(this.layers[this.n_layers].transp_coeff_grass > 0)
						this.n_transp_lyrs_grass++;
					else
						transp_ok_grass=false;
				}
				
				water_eqn(this.layers[this.n_layers].fractionVolBulk_gravel, this.layers[this.n_layers].fractionWeightMatric_sand,
						this.layers[this.n_layers].fractionWeightMatric_clay, this.n_layers);
				
				this.layers[this.n_layers].swcBulk_fieldcap = Defines.SW_SWPmatric2VWCBulk(layers[n_layers].fractionVolBulk_gravel, 0.333, layers[n_layers].psisMatric, layers[n_layers].binverseMatric, layers[n_layers].thetasMatric) * this.layers[this.n_layers].width;
				this.layers[this.n_layers].swcBulk_wiltpt = Defines.SW_SWPmatric2VWCBulk(this.layers[this.n_layers].fractionVolBulk_gravel, 15, layers[n_layers].psisMatric, layers[n_layers].binverseMatric, layers[n_layers].thetasMatric) * this.layers[this.n_layers].width;
				
				calculate_soilBulkDensity(layers[n_layers].soilMatric_density, layers[n_layers].fractionVolBulk_gravel, n_layers);
				
				this.n_layers++;
			}
		}
		
		this.data = true;
	}
	
	public void onWrite(Path soilsIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Soil layer definitions");
			lines.add("# Location: ");
			lines.add("#");
			lines.add("# depth = (cm) lower limit of layer; layers must be in order of depth.");
			lines.add("# matricd = (g/cm^3) bulk density of soil in this layer.");
			lines.add("# gravel_content = the percent volume of each layer composed of gravel (i.e., particles > 2mm)");
			lines.add("# evco = (frac) proportion of total baresoil evap from this layer.");
			lines.add("# trco = (frac) proportion of total transpiration from this layer for each vegetation type (tree, forb, shrub, grass)");
			lines.add("# %sand = (frac) proportion of sand in layer (0-1.0).");
			lines.add("# %clay = (frac) proportion of clay in layer (0-1.0).");
			lines.add("# imperm = (frac) proportion of 'impermeability' to water percolation(/infiltration/drainage) in layer (0-1.0)");
			lines.add("# soiltemp = the initial temperature of each soil layer (in celcius), from the day before the simulation starts");
			lines.add("# Note that the evco and trco columns must sum to 1.0 or they will");
			lines.add("# be normalized.");
			lines.add("#");
			//lines.add("# depth 	matricd   gravel_content   	evco  	trco_grass  	trco_shrub  	trco_tree  	trco_forb	%sand  	%clay 	imperm 	soiltemp");
			lines.add(String.format("# %5s %10s %17s %7s %13s %13s %12s %12s %8s %8s %9s %11s","depth","matricd","gravel_content","evco","trco_grass","trco_shrub","trco_tree","trco_forb","%sand","%clay","imperm","soiltemp"));
			for(int i=0; i<(this.n_layers-(deepdrainSet?1:0)); i++)
				lines.add(this.layers[i].toString());
			Files.write(soilsIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "SoilsIn : onWrite : No data.");
		}
	}
	
	public void setDeepdrain(boolean deepdrain) {
		if(deepdrain) {
			layers[n_layers].width=1.0;
			this.n_layers++;
			this.deepdrainSet = true;
		}
	}
	
	private void water_eqn(double fractionGravel, double sand, double clay, int n) {
		double theta33, theta33t, OM = 0., thetasMatric33, thetasMatric33t; /* Saxton et al. auxiliary variables */

		this.layers[n].thetasMatric = -14.2 * sand - 3.7 * clay + 50.5;
		this.layers[n].psisMatric = Math.pow(10.0, (-1.58* sand - 0.63*clay + 2.17));
		this.layers[n].bMatric = -0.3 * sand + 15.7 * clay + 3.10;

		if (Defines.isZero(this.layers[n].bMatric)) {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "SoilsIn : water_eqn : Value of beta Possible division by zero. Exit."+String.valueOf(this.layers[n].bMatric));
		}

		this.layers[n].binverseMatric = 1.0 / this.layers[n].bMatric;

		/* saturated soil water content: Saxton, K. E. and W. J. Rawls. 2006. Soil water characteristic estimates by texture and organic matter for hydrologic solutions. Soil Science Society of America Journal 70:1569-1578. */
		theta33t = -0.251 * sand + 0.195 * clay + 0.011 * OM + 0.006 * (sand * OM) - 0.027 * (clay * OM) + 0.452 * (sand * clay) + 0.299;
		theta33 = theta33t + (1.283 * Math.pow(theta33t, 2) - 0.374 * theta33t - 0.015);

		thetasMatric33t = 0.278 * sand + 0.034 * clay + 0.022 * OM - 0.018 * sand * OM - 0.027 * clay * OM - 0.584 * sand * clay + 0.078;
		thetasMatric33 = thetasMatric33t + (0.636 * thetasMatric33t - 0.107);

		this.layers[n].swcBulk_saturated = this.layers[n].width * (theta33 + thetasMatric33 - 0.097 * sand + 0.043) * (1 - fractionGravel);		
	}
	private void calculate_soilBulkDensity(double matricDensity, double fractionGravel, int n) {
		/* ---------------------------------------------------------------- */
		/* used to calculate the bulk density from the given matric density */
		/* ---------------------------------------------------------------- */
		layers[n].soilBulk_density = matricDensity * (1 - fractionGravel) + (fractionGravel * 2.65); /*eqn. 20 from Saxton et al. 2006  to calculate the bulk density of soil */
	}
}
