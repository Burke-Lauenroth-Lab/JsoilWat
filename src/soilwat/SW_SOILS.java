package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.InputData.SoilsIn;
import soilwat.LogFileIn.LogMode;

public class SW_SOILS {
	public static class SOILS_INPUT_DATA {
		public double depth; /*for printing out line*/
		public double soilMatric_density; /* matric soil density, i.e., gravel component excluded, (g/cm3) */
		public double fractionVolBulk_gravel; /* gravel content (> 2 mm) as volume-fraction of bulk soil (g/cm3) */
		public double evap_coeff; /* prop. of total soil evap from this layer */
		public double transp_coeff_forb, transp_coeff_tree, transp_coeff_shrub, transp_coeff_grass; /* prop. of total transp from this layer    */
		public double fractionWeightMatric_sand; /* sand content (< 2 mm & > . mm) as weight-fraction of matric soil (g/g) */
		public double fractionWeightMatric_clay; /* clay content (< . mm & > . mm) as weight-fraction of matric soil (g/g) */
		public double impermeability; /* fraction of how impermeable a layer is (0=permeable, 1=impermeable)    */
		public double sTemp; /* initial soil temperature for each soil layer */
		
		public void onSet(double depth, double matricd, double gravel_content, double evco, double trco_grass, double trco_shrub, double trco_tree,
				double trco_forb, double perc_sand, double perc_clay, double imperm, double soiltemp) {
			this.depth = depth;
			this.soilMatric_density = matricd;
			this.fractionVolBulk_gravel = gravel_content;
			this.evap_coeff = evco;
			this.transp_coeff_grass = trco_grass;
			this.transp_coeff_shrub = trco_shrub;
			this.transp_coeff_tree = trco_tree;
			this.transp_coeff_forb = trco_forb;
			this.fractionWeightMatric_sand = perc_sand;
			this.fractionWeightMatric_clay = perc_clay;
			this.impermeability = imperm;
			this.sTemp = soiltemp;
		}
	}
	protected class SW_LAYER_INFO extends SOILS_INPUT_DATA {
		protected double width, /* width of the soil layer (cm) */
		soilBulk_density; /* bulk soil density, i.e., including gravel component, (g/cm3) */
		protected double swcBulk_fieldcap, /* field_cap * width                        */
		swcBulk_wiltpt, /* wilting_pt * width                       */
		swcBulk_wet, /* swc considered "wet" (cm) *width         */
		swcBulk_init, /* start the model at this swc (cm) *width  */
		swcBulk_min, /* swc cannot go below this (cm) *width     */
		swcBulk_saturated; /* saturated soil water content (cm) * width */
		protected double swcBulk_atSWPcrit_forb, swcBulk_atSWPcrit_tree, swcBulk_atSWPcrit_shrub, swcBulk_atSWPcrit_grass, /* swc at the critical soil water potential */
		thetasMatric, /* This group is parameters for */
		psisMatric, /* Cosby et al. (1982) SWC <. SWP */
		bMatric, /* conversion functions. */
		binverseMatric;
		
		protected int my_transp_rgn_forb, my_transp_rgn_tree, my_transp_rgn_shrub, my_transp_rgn_grass; /* which transp zones from Site am I in? */
		
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
	protected class LayersInfo {
		public int n_layers, /* total number of soil layers */
		n_transp_rgn, /* soil layers are grouped into n transp. regions */
		n_evap_lyrs, /* number of layers in which evap is possible */
		n_transp_lyrs_forb, n_transp_lyrs_tree, n_transp_lyrs_shrub, n_transp_lyrs_grass, /* layer index of deepest transp. region       */
		deep_lyr; /* index of deep drainage layer if deepdrain, 0 otherwise */
	}
	
	private SW_LAYER_INFO[] layers;
	private LayersInfo layersInfo;
	private double[] widths;
	private boolean EchoInits;
	private boolean data;
	private boolean deepdrainSet;
	
	protected SW_SOILS() {
		this.layersInfo = new LayersInfo();
		this.layers = new SW_LAYER_INFO[Defines.MAX_LAYERS];
		for(int i=0; i<Defines.MAX_LAYERS;  i++)
			this.layers[i] = new SW_LAYER_INFO();
		this.data = false;
		this.deepdrainSet = false;
		layersInfo.n_layers = 0;
		layersInfo.n_transp_rgn=0;
		layersInfo.n_evap_lyrs=0;
		layersInfo.n_transp_lyrs_forb=layersInfo.n_transp_lyrs_grass=layersInfo.n_transp_lyrs_shrub=layersInfo.n_transp_lyrs_tree=0;
		layersInfo.deep_lyr = 0;
		widths = null;
	}
	
	protected void onClear() {
		this.data = false;
		layersInfo.n_layers = 0;
		layersInfo.n_transp_rgn=0;
		layersInfo.n_evap_lyrs=0;
		layersInfo.n_transp_lyrs_forb=layersInfo.n_transp_lyrs_grass=layersInfo.n_transp_lyrs_shrub=layersInfo.n_transp_lyrs_tree=0;
		layersInfo.deep_lyr=0;
		this.deepdrainSet = false;
		widths = null;
		for(int i=0; i<Defines.MAX_LAYERS;  i++)
			this.layers[i].onClear();
	}
	
	protected boolean onVerify(boolean siteModelDeepdrain) {
		boolean fail = false;
		LogFileIn f = LogFileIn.getInstance();
		String message = "";
		if(data) {
			boolean evap_ok = true, /* mitigate gaps in layers' evap coeffs */
					transp_ok_forb = true, transp_ok_tree = true, /* same for transpiration coefficients */
					transp_ok_shrub = true, /* same for transpiration coefficients */
					transp_ok_grass = true; /* same for transpiration coefficients */

			for(int i=0; i<layersInfo.n_layers; i++) {
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

			for(int i=0; i<layersInfo.n_layers; i++) {
				if(evap_ok) {
					if(this.layers[i].evap_coeff > 0.0)
						layersInfo.n_evap_lyrs++;
					else
						evap_ok=false;
				}
				if(transp_ok_forb) {
					if(this.layers[i].transp_coeff_forb > 0)
						layersInfo.n_transp_lyrs_forb++;
					else
						transp_ok_forb=false;
				}
				if(transp_ok_tree) {
					if(this.layers[i].transp_coeff_tree > 0)
						layersInfo.n_transp_lyrs_tree++;
					else
						transp_ok_tree=false;
				}
				if(transp_ok_shrub) {
					if(this.layers[i].transp_coeff_shrub > 0)
						layersInfo.n_transp_lyrs_shrub++;
					else
						transp_ok_shrub=false;
				}
				if(transp_ok_grass) {
					if(this.layers[i].transp_coeff_grass > 0)
						layersInfo.n_transp_lyrs_grass++;
					else
						transp_ok_grass=false;
				}

				water_eqn(this.layers[i].fractionVolBulk_gravel, this.layers[i].fractionWeightMatric_sand,
						this.layers[i].fractionWeightMatric_clay, i);

				this.layers[i].swcBulk_fieldcap = SW_SOILWATER.SW_SWPmatric2VWCBulk(layers[i].fractionVolBulk_gravel, 0.333, layers[i].psisMatric, layers[i].binverseMatric, layers[i].thetasMatric) * this.layers[i].width;
				this.layers[i].swcBulk_wiltpt = SW_SOILWATER.SW_SWPmatric2VWCBulk(layers[i].fractionVolBulk_gravel, 15, layers[i].psisMatric, layers[i].binverseMatric, layers[i].thetasMatric) * this.layers[i].width;

				calculate_soilBulkDensity(layers[i].soilMatric_density, layers[i].fractionVolBulk_gravel, i);
			}

			setDeepdrain(siteModelDeepdrain);
			
			widths = getLayerWidths();
			
			return true;
		} else {
			return false;
		}
	}
	protected void onSetInput(SoilsIn soilsIn) {
		//Copy over all layers - could improve
		double dmin=0.0,dmax;
		layersInfo.n_layers=soilsIn.nLayers;
		for(int i=0; i<Defines.MAX_LAYERS; i++) {
			dmax = soilsIn.layers[i].depth;
			this.layers[i].depth = dmax;
			this.layers[i].width = dmax-dmin;
			dmin = dmax;
			this.layers[i].soilMatric_density = soilsIn.layers[i].soilMatric_density;
			this.layers[i].fractionVolBulk_gravel = soilsIn.layers[i].fractionVolBulk_gravel;
			this.layers[i].evap_coeff = soilsIn.layers[i].evap_coeff;
			this.layers[i].transp_coeff_grass = soilsIn.layers[i].transp_coeff_grass ;
			this.layers[i].transp_coeff_shrub = soilsIn.layers[i].transp_coeff_shrub;
			this.layers[i].transp_coeff_tree = soilsIn.layers[i].transp_coeff_tree;
			this.layers[i].transp_coeff_forb = soilsIn.layers[i].transp_coeff_forb;
			this.layers[i].fractionWeightMatric_sand = soilsIn.layers[i].fractionWeightMatric_sand;
			this.layers[i].fractionWeightMatric_clay = soilsIn.layers[i].fractionWeightMatric_clay;
			this.layers[i].impermeability = soilsIn.layers[i].impermeability;
			this.layers[i].my_transp_rgn_grass = 0;
			this.layers[i].my_transp_rgn_shrub = 0;
			this.layers[i].my_transp_rgn_tree = 0;
			this.layers[i].my_transp_rgn_forb = 0;
			this.layers[i].sTemp = soilsIn.layers[i].sTemp;
		}
		this.data = true;
	}
	protected void onRead(Path soilsIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(soilsIn, StandardCharsets.UTF_8);
		this.data = false;
		layersInfo.n_layers=0;
		double dmin=0.0,dmax;
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				if(values.length != 12)
					f.LogError(LogFileIn.LogMode.ERROR, "SoilsIn onRead : Expected 10 Values read "+String.valueOf(values.length));
				if(layersInfo.n_layers == Defines.MAX_LAYERS)
					f.LogError(LogFileIn.LogMode.ERROR, "SoilsIn onRead : Too many layers specified "+String.valueOf(values.length));
				try {
					dmax = Double.parseDouble(values[0]);
					this.layers[layersInfo.n_layers].depth = dmax;
					this.layers[layersInfo.n_layers].width = dmax-dmin;
					dmin = dmax;
					this.layers[layersInfo.n_layers].soilMatric_density = Double.parseDouble(values[1]);
					this.layers[layersInfo.n_layers].fractionVolBulk_gravel = Double.parseDouble(values[2]);
					this.layers[layersInfo.n_layers].evap_coeff = Double.parseDouble(values[3]);
					this.layers[layersInfo.n_layers].transp_coeff_grass = Double.parseDouble(values[4]);
					this.layers[layersInfo.n_layers].transp_coeff_shrub = Double.parseDouble(values[5]);
					this.layers[layersInfo.n_layers].transp_coeff_tree = Double.parseDouble(values[6]);
					this.layers[layersInfo.n_layers].transp_coeff_forb = Double.parseDouble(values[7]);
					this.layers[layersInfo.n_layers].fractionWeightMatric_sand = Double.parseDouble(values[8]);
					this.layers[layersInfo.n_layers].fractionWeightMatric_clay = Double.parseDouble(values[9]);
					this.layers[layersInfo.n_layers].impermeability = Double.parseDouble(values[10]);
					this.layers[layersInfo.n_layers].my_transp_rgn_grass = 0;
					this.layers[layersInfo.n_layers].my_transp_rgn_shrub = 0;
					this.layers[layersInfo.n_layers].my_transp_rgn_tree = 0;
					this.layers[layersInfo.n_layers].my_transp_rgn_forb = 0;
					this.layers[layersInfo.n_layers].sTemp = Double.parseDouble(values[11]);
					
				} catch(NumberFormatException e) {
					f.LogError(LogFileIn.LogMode.ERROR, "Soils onRead : Could not convert string to double. " + e.getMessage());
				}
				layersInfo.n_layers++;
			}
		}
		this.data = true;
	}
	
	protected void onWrite(Path soilsIn) throws IOException {
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
			for(int i=0; i<(layersInfo.n_layers-(deepdrainSet?1:0)); i++)
				lines.add(this.layers[i].toString());
			Files.write(soilsIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "SoilsIn : onWrite : No data.");
		}
	}
	
	public void setDeepdrain(boolean deepdrain) {
		if(deepdrain) {
			layers[layersInfo.n_layers].width=1.0;
			layersInfo.n_layers++;
			this.deepdrainSet = true;
		}
	}
	
	protected void water_eqn(double fractionGravel, double sand, double clay, int n) {
		double theta33, theta33t, OM = 0., thetasMatric33, thetasMatric33t; /* Saxton et al. auxiliary variables */

		this.layers[n].thetasMatric = -14.2 * sand - 3.7 * clay + 50.5;
		this.layers[n].psisMatric = Defines.powe(10.0, (-1.58* sand - 0.63*clay + 2.17));
		this.layers[n].bMatric = -0.3 * sand + 15.7 * clay + 3.10;

		if (Defines.isZero(this.layers[n].bMatric)) {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "SoilsIn : water_eqn : Value of beta Possible division by zero. Exit."+String.valueOf(this.layers[n].bMatric));
		}

		this.layers[n].binverseMatric = 1.0 / this.layers[n].bMatric;

		/* saturated soil water content: Saxton, K. E. and W. J. Rawls. 2006. Soil water characteristic estimates by texture and organic matter for hydrologic solutions. Soil Science Society of America Journal 70:1569-1578. */
		theta33t = -0.251 * sand + 0.195 * clay + 0.011 * OM + 0.006 * (sand * OM) - 0.027 * (clay * OM) + 0.452 * (sand * clay) + 0.299;
		theta33 = theta33t + (1.283 * Defines.powe(theta33t, 2) - 0.374 * theta33t - 0.015);

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

	private double[] getLayerWidths() {
		double[] widths = new double[layersInfo.n_layers];
		for(int i=0; i<layersInfo.n_layers; i++)
			widths[i] = layers[i].width;
		return widths;
	}
	
	protected void _echo_inputs(String soilsFile) {
		LogFileIn f = LogFileIn.getInstance();
		
		f.LogError(LogFileIn.LogMode.NOTE, String.format("\nLayer Related Values:\n----------------------\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Soils File: %s\n", soilsFile));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of soil layers: %d\n", this.layersInfo.n_layers));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of evaporation layers: %d\n", this.layersInfo.n_evap_lyrs));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of forb transpiration layers: %d\n", this.layersInfo.n_transp_lyrs_forb));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of tree transpiration layers: %d\n", this.layersInfo.n_transp_lyrs_tree));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of shrub transpiration layers: %d\n", this.layersInfo.n_transp_lyrs_shrub));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of grass transpiration layers: %d\n", this.layersInfo.n_transp_lyrs_grass));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Number of transpiration regions: %d\n", this.layersInfo.n_transp_rgn));

		f.LogError(LogFileIn.LogMode.NOTE, String.format("\nLayer Specific Values:\n----------------------\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("\n  Layer information on a per centimeter depth basis:\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Lyr Width   BulkD 	%%Gravel    FieldC   WiltPt   %%Sand  %%Clay VWC at Forb-critSWP 	VWC at Tree-critSWP	VWC at Shrub-critSWP	VWC at Grass-critSWP	EvCo   	TrCo_Forb   TrCo_Tree  TrCo_Shrub  TrCo_Grass   TrRgn_Forb    TrRgn_Tree   TrRgn_Shrub   TrRgn_Grass   Wet     Min      Init     Saturated    Impermeability\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("       (cm)   (g/cm^3)  (prop)    (cm/cm)  (cm/cm)   (prop) (prop)  (cm/cm)			(cm/cm)                (cm/cm)            		(cm/cm)         (prop)    (prop)      (prop)     (prop)    (prop)        (int)           (int) 	      	(int) 	    (int) 	    (cm/cm)  (cm/cm)  (cm/cm)  (cm/cm)      (frac)\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  --- -----   ------    ------     ------   ------   -----  ------   ------                	-------			------            		------          ------    ------      ------      ------   ------       ------   	 -----	        -----       -----   	 ----     ----     ----    ----         ----\n"));
		for(int i=0; i<this.layersInfo.n_layers; i++)
		{
			f.LogError(LogFileIn.LogMode.NOTE, String.format("  %3d %5.1f %9.5f %6.2f %8.5f %8.5f %6.2f %6.2f %6.2f %6.2f %6.2f %6.2f %9.2f %9.2f %9.2f %9.2f %9.2f %10d %10d %15d %15d %15.4f %9.4f %9.4f %9.4f %9.4f\n",
					i + 1, this.layers[i].width, this.layers[i].soilBulk_density, this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_fieldcap / this.layers[i].width,
					this.layers[i].swcBulk_wiltpt / this.layers[i].width, this.layers[i].fractionWeightMatric_sand, this.layers[i].fractionWeightMatric_clay,
					this.layers[i].swcBulk_atSWPcrit_forb / this.layers[i].width, this.layers[i].swcBulk_atSWPcrit_tree / this.layers[i].width,
					this.layers[i].swcBulk_atSWPcrit_shrub / this.layers[i].width, this.layers[i].swcBulk_atSWPcrit_grass / this.layers[i].width, this.layers[i].evap_coeff,
					this.layers[i].transp_coeff_forb, this.layers[i].transp_coeff_tree, this.layers[i].transp_coeff_shrub, this.layers[i].transp_coeff_grass, this.layers[i].my_transp_rgn_forb,
					this.layers[i].my_transp_rgn_tree, this.layers[i].my_transp_rgn_shrub, this.layers[i].my_transp_rgn_grass, this.layers[i].swcBulk_wet / this.layers[i].width,
					this.layers[i].swcBulk_min / this.layers[i].width, this.layers[i].swcBulk_init / this.layers[i].width, this.layers[i].swcBulk_saturated / this.layers[i].width,
					this.layers[i].impermeability));

		}
		f.LogError(LogFileIn.LogMode.NOTE, String.format("\n  Actual per-layer values:\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Lyr Width  BulkD	 %%Gravel   FieldC   WiltPt %%Sand  %%Clay	SWC at Forb-critSWP     SWC at Tree-critSWP	SWC at Shrub-critSWP	SWC at Grass-critSWP	 Wet    Min      Init  Saturated	SoilTemp\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("       (cm)  (g/cm^3)	(prop)    (cm)     (cm)  (prop) (prop)   (cm)    	(cm)        		(cm)            (cm)            (cm)   (cm)      (cm)     (cm)		(celcius)\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  --- -----  -------	------   ------   ------ ------ ------   ------        	------            	------          ----   		----     ----     ----    ----		----\n"));

		for(int i=0; i<this.layersInfo.n_layers; i++)
		{
			f.LogError(LogFileIn.LogMode.NOTE, String.format("  %3d %5.1f %9.5f %6.2f %8.5f %8.5f %6.2f %6.2f %7.4f %7.4f %7.4f %7.4f %7.4f %7.4f %8.4f %7.4f %5.4f\n", i + 1, this.layers[i].width,
					this.layers[i].soilBulk_density, this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_fieldcap, this.layers[i].swcBulk_wiltpt, this.layers[i].fractionWeightMatric_sand,
					this.layers[i].fractionWeightMatric_clay, this.layers[i].swcBulk_atSWPcrit_forb, this.layers[i].swcBulk_atSWPcrit_tree, this.layers[i].swcBulk_atSWPcrit_shrub,
					this.layers[i].swcBulk_atSWPcrit_grass, this.layers[i].swcBulk_wet, this.layers[i].swcBulk_min, this.layers[i].swcBulk_init, this.layers[i].swcBulk_saturated, this.layers[i].sTemp));
		}

		f.LogError(LogFileIn.LogMode.NOTE, String.format("\n  Water Potential values:\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  Lyr       FieldCap         WiltPt            Forb-critSWP     Tree-critSWP     Shrub-critSWP    Grass-critSWP    Wet            Min            Init\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("            (bars)           (bars)            (bars)           (bars)           (bars)           (bars)           (bars)         (bars)         (bars)\n"));
		f.LogError(LogFileIn.LogMode.NOTE, String.format("  ---       -----------      ------------      -----------      -----------      -----------      -----------      -----------    -----------    --------------    --------------\n"));

		for(int i=0; i<this.layersInfo.n_layers; i++)
		{
			f.LogError(LogFileIn.LogMode.NOTE, String.format("  %3d   %15.4f   %15.4f  %15.4f %15.4f  %15.4f  %15.4f  %15.4f   %15.4f   %15.4f\n", i + 1,
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_fieldcap, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_wiltpt, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_forb, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_tree, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_shrub, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_grass, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_wet, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_min, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_init, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i)));
		}
	}
	
	protected boolean get_echoinits() {
		return this.EchoInits;
	}
	protected void set_echoinits(boolean echo) {
		this.EchoInits = echo;
	}
	protected LayersInfo getLayersInfo() {
		return layersInfo;
	}
	protected SW_LAYER_INFO getLayer(int lyr) {
		return this.layers[lyr];
	}
	protected double[] getWidths() {
		return this.widths;
	}
}
