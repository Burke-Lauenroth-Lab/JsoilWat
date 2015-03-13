package soilwat;

import java.util.ArrayList;
import java.util.Collections;
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
		
		public void onClear() {
			this.depth = 0;
			this.soilMatric_density = 0;
			this.fractionVolBulk_gravel = 0;
			this.evap_coeff = 0;
			this.transp_coeff_grass = 0;
			this.transp_coeff_shrub = 0;
			this.transp_coeff_tree = 0;
			this.transp_coeff_forb = 0;
			this.fractionWeightMatric_sand = 0;
			this.fractionWeightMatric_clay = 0;
			this.impermeability = 0;
			this.sTemp = 0;
		}
		
		public double[] getValues() {
			double[] row = new double[] {depth,soilMatric_density,fractionVolBulk_gravel,evap_coeff,transp_coeff_grass,transp_coeff_shrub,transp_coeff_tree,transp_coeff_forb,fractionWeightMatric_sand,fractionWeightMatric_clay,impermeability,sTemp};
			return(row);
		}
		
		public String toString() {
			return String.format("%7.0f %10.3f      %-12.3f %7.3f %13.3f %13.3f %12.3f %12.3f %8.3f %8.3f %9.3f %11.3f", depth, soilMatric_density, fractionVolBulk_gravel, evap_coeff, transp_coeff_grass, transp_coeff_shrub, transp_coeff_tree,
					transp_coeff_forb, fractionWeightMatric_sand, fractionWeightMatric_clay, impermeability, sTemp);
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
	public class LayersInfo {
		public int n_layers, /* total number of soil layers */
		n_transp_rgn, /* soil layers are grouped into n transp. regions */
		n_evap_lyrs, /* number of layers in which evap is possible */
		n_transp_lyrs_forb, n_transp_lyrs_tree, n_transp_lyrs_shrub, n_transp_lyrs_grass, /* layer index of deepest transp. region       */
		deep_lyr; /* index of deep drainage layer if deepdrain, 0 otherwise */
		
		/**
		 * This function returns the max number of transp lyrs of grass,shrub,tree,forb
		 * @return int between 0 and max layers.
		 */
		public int getTrLyrs() {
			List<Integer> lengths = new ArrayList<Integer>();
			lengths.add(n_transp_lyrs_forb);
			lengths.add(n_transp_lyrs_grass);
			lengths.add(n_transp_lyrs_shrub);
			lengths.add(n_transp_lyrs_tree);
			return Collections.max(lengths);
		}
		
	}
	
	private SW_LAYER_INFO[] layers;
	protected LayersInfo layersInfo;
	private double[] widths;
	private boolean EchoInits;
	private boolean data;
	public boolean deepdrainSet;
	public LogFileIn log;
	
	protected SW_SOILS(LogFileIn log) {
		this.log = log;
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
	
	protected boolean onVerify(boolean siteModelDeepdrain) throws Exception {
		boolean fail = false;
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
				log.LogError(LogFileIn.LogMode.ERROR, message);

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
		for(int i=0; i<layersInfo.n_layers; i++) {
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
	protected void onGetInput(SoilsIn soilsIn) {
		soilsIn.nLayers = layersInfo.n_layers;
		for(int i=0; i<layersInfo.n_layers; i++) {
			soilsIn.layers[i].depth = this.layers[i].depth;
			soilsIn.layers[i].soilMatric_density = this.layers[i].soilMatric_density;
			soilsIn.layers[i].fractionVolBulk_gravel = this.layers[i].fractionVolBulk_gravel;
			soilsIn.layers[i].evap_coeff = this.layers[i].evap_coeff;
			soilsIn.layers[i].transp_coeff_grass = this.layers[i].transp_coeff_grass;
			soilsIn.layers[i].transp_coeff_shrub = this.layers[i].transp_coeff_shrub;
			soilsIn.layers[i].transp_coeff_tree = this.layers[i].transp_coeff_tree;
			soilsIn.layers[i].transp_coeff_forb = this.layers[i].transp_coeff_forb;
			soilsIn.layers[i].fractionWeightMatric_sand = this.layers[i].fractionWeightMatric_sand;
			soilsIn.layers[i].fractionWeightMatric_clay = this.layers[i].fractionWeightMatric_clay;
			soilsIn.layers[i].impermeability = this.layers[i].impermeability;
			soilsIn.layers[i].sTemp = this.layers[i].sTemp;
		}
	}
	
	public void setDeepdrain(boolean deepdrain) {
		if(deepdrain) {
			layers[layersInfo.n_layers].width=1.0;
			layersInfo.n_layers++;
			this.deepdrainSet = true;
		}
	}
	
	protected void water_eqn(double fractionGravel, double sand, double clay, int n) throws Exception {
		double theta33, theta33t, OM = 0., thetasMatric33, thetasMatric33t; /* Saxton et al. auxiliary variables */

		this.layers[n].thetasMatric = -14.2 * sand - 3.7 * clay + 50.5;
		this.layers[n].psisMatric = Defines.powe(10.0, (-1.58* sand - 0.63*clay + 2.17));
		this.layers[n].bMatric = -0.3 * sand + 15.7 * clay + 3.10;

		if (Defines.isZero(this.layers[n].bMatric)) {
			log.LogError(LogMode.WARN, "SoilsIn : water_eqn : Value of beta Possible division by zero. Exit."+String.valueOf(this.layers[n].bMatric));
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
	
	protected void _echo_inputs(String soilsFile) throws Exception {
		LogFileIn f = log;
		
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
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_fieldcap, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_wiltpt, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_forb, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_tree, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_shrub, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_atSWPcrit_grass, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_wet, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_min, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i),
					SW_SOILWATER.SW_SWCbulk2SWPmatric(log,this.layers[i].fractionVolBulk_gravel, this.layers[i].swcBulk_init, this.layers[i].width, this.layers[i].psisMatric, this.layers[i].thetasMatric, this.layers[i].bMatric, 0, 0, i)));
		}
	}
	
	protected boolean get_echoinits() {
		return this.EchoInits;
	}
	protected void set_echoinits(boolean echo) {
		this.EchoInits = echo;
	}
	protected LayersInfo getLayersInfo() {
		LayersInfo li = new LayersInfo();
		li.deep_lyr = this.layersInfo.deep_lyr;
		li.n_evap_lyrs = this.layersInfo.n_evap_lyrs;
		li.n_layers = this.layersInfo.n_layers;
		li.n_transp_lyrs_forb = this.layersInfo.n_transp_lyrs_forb;
		li.n_transp_lyrs_grass = this.layersInfo.n_transp_lyrs_grass;
		li.n_transp_lyrs_shrub = this.layersInfo.n_transp_lyrs_shrub;
		li.n_transp_lyrs_tree = this.layersInfo.n_transp_lyrs_tree;
		li.n_transp_rgn = this.layersInfo.n_transp_rgn;
		return li;
	}
	protected SW_LAYER_INFO getLayer(int lyr) {
		return this.layers[lyr];
	}
	protected double[] getWidths() {
		return this.widths;
	}
}
