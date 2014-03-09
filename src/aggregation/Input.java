package aggregation;

import java.util.ArrayList;
import java.util.Collections;

import circular.Circular;
import soilwat.InputData;
import aggregation.AggregationCommon;
import aggregation.AggregationCommon.GrassTypeFractions;
import aggregation.AggregationCommon.LayerInformation;
import aggregation.AggregationCommon.Texture;


public class Input {
	
	private Texture texture;
	private LayerInformation lyr_info;
	private InputData SW_Input;
	private GrassTypeFractions grass_c3c4ann_fractions;
	
/*	public class SoilProfile extends Aggregate {
		@Override
		protected void setNames() {
			columnNames = new ArrayList<String>();
			columnNames.add("SWinput.Soil.maxDepth_cm");
			columnNames.add("SWinput.Soil.soilLayers_N");
			columnNames.add("SWinput.Soil.topLayers.Sand_fraction");
			columnNames.add("SWinput.Soil.bottomLayers.Sand_fraction");
			columnNames.add("SWinput.Soil.topLayers.Clay_fraction");
			columnNames.add("SWinput.Soil.bottomLayers.Clay_fraction");
		}
		@Override
		protected void setValues() {
			values = new double[count];
			//soilDepth_cm
			values[0] = SW_Input.soilsIn.layers[SW_Input.soilsIn.nLayers-1].depth;
			//soilLayers_N
			values[1] = SW_Input.soilsIn.nLayers;
			//sand.top
			values[2] = texture.sand_top;
			//sand.bottom
			values[3] = texture.sand_bottom;
			//clay.top
			values[4] = texture.clay_top;
			//clay.bottom
			values[5] = texture.clay_bottom;
		}
	}
	public class FractionVegetationComposition extends Aggregate {
		@Override
		protected void setNames() {
			columnNames = new ArrayList<String>();
			columnNames.add("SWinput.Composition.Grasses_fraction_const");
			columnNames.add("SWinput.Composition.Shrubs_fraction_const");
			columnNames.add("SWinput.Composition.Trees_fraction_const");
			columnNames.add("SWinput.Composition.Forbs_fraction_const");
			columnNames.add("SWinput.Composition.BareGround_fraction_const");
			columnNames.add("SWinput.Composition.C3ofGrasses_fraction_const");
			columnNames.add("SWinput.Composition.C4ofGrasses_fraction_const");
			columnNames.add("SWinput.Composition.AnnualsofGrasses_fraction_const");
		}

		@Override
		protected void setValues() {
			values = new double[count];
			values[0] = SW_Input.prodIn.vegComp.grass;
			values[1] = SW_Input.prodIn.vegComp.shrub;
			values[2] = SW_Input.prodIn.vegComp.tree;
			values[3] = SW_Input.prodIn.vegComp.forb;
			values[4] = SW_Input.prodIn.vegComp.bareGround;
			values[5] = grass_c3c4ann_fractions.c3;
			values[6] = grass_c3c4ann_fractions.c4;
			values[7] = grass_c3c4ann_fractions.ann;
		}
	}
	public class VegetationBiomassMonthly extends Aggregate {
		private int headerColumnWidth;
		public VegetationBiomassMonthly() {
			super();
			this.count=3;
			this.multiline=true;
		}
		@Override
		protected void setNames() {
			columnNames = new ArrayList<String>();
			columnNames.add("Type");//integer
			columnNames.add("Month");//integer
			columnNames.add("Litter_gPERm2");
			columnNames.add("TotalBiomass_gPERm2");
			columnNames.add("LiveBiomass_gPERm2");
		}
		@Override
		protected void setValues() {
			values = new double[count*4*12];
			for(int i=0; i<12; i++) {//Grass
				values[0 + (i*3)] = SW_Input.prodIn.monthlyProd.grass.litter[i];
				values[1 + (i*3)] = SW_Input.prodIn.monthlyProd.grass.biomass[i];
				values[2 + (i*3)] = SW_Input.prodIn.monthlyProd.grass.biomass[i]*SW_Input.prodIn.monthlyProd.grass.percLive[i];
			}
			for(int i=0; i<12; i++) {//Shrub
				values[0 + (i*3) + 36] = SW_Input.prodIn.monthlyProd.shrub.litter[i];
				values[1 + (i*3) + 36] = SW_Input.prodIn.monthlyProd.shrub.biomass[i];
				values[2 + (i*3) + 36] = SW_Input.prodIn.monthlyProd.shrub.biomass[i]*SW_Input.prodIn.monthlyProd.shrub.percLive[i];
			}
			for(int i=0; i<12; i++) {//Tree
				values[0 + (i*3) + 72] = SW_Input.prodIn.monthlyProd.tree.litter[i];
				values[1 + (i*3) + 72] = SW_Input.prodIn.monthlyProd.tree.biomass[i];
				values[2 + (i*3) + 72] = SW_Input.prodIn.monthlyProd.tree.biomass[i]*SW_Input.prodIn.monthlyProd.tree.percLive[i];
			}
			for(int i=0; i<12; i++) {//Forb
				values[0 + (i*3) + 108] = SW_Input.prodIn.monthlyProd.forb.litter[i];
				values[1 + (i*3) + 108] = SW_Input.prodIn.monthlyProd.forb.biomass[i];
				values[2 + (i*3) + 108] = SW_Input.prodIn.monthlyProd.forb.biomass[i]*SW_Input.prodIn.monthlyProd.forb.percLive[i];
			}
		}
	}
	public class VegetationPeak extends Aggregate {
		@Override
		protected void setNames() {
			columnNames = new ArrayList<String>();
			columnNames.add("SWinput.PeakLiveBiomass_month_mean");
			columnNames.add("SWinput.PeakLiveBiomass_months_duration");
		}

		@Override
		protected void setValues() {
			values = new double[count];
			
			double[] grass = new double[12];
			double[] shrub = new double[12];
			double[] tree = new double[12];
			double[] forb = new double[12];
			
			ArrayList<Double> sumWeightedLiveBiomassByMonth = new ArrayList<Double>();
			double max;
			//Get biomass for each month
			for(int i=0; i<12; i++) {
				grass[i] = SW_Input.prodIn.monthlyProd.grass.biomass[i]*SW_Input.prodIn.monthlyProd.grass.percLive[i];
				shrub[i] = SW_Input.prodIn.monthlyProd.shrub.biomass[i]*SW_Input.prodIn.monthlyProd.shrub.percLive[i];
				tree[i] = SW_Input.prodIn.monthlyProd.tree.biomass[i]*SW_Input.prodIn.monthlyProd.tree.percLive[i];
				forb[i] = SW_Input.prodIn.monthlyProd.forb.biomass[i]*SW_Input.prodIn.monthlyProd.forb.percLive[i];
			}
			//Scale those values by veg Frac
			for(int i=0; i<12; i++) {
				grass[i] = grass[i] * SW_Input.prodIn.vegComp.grass;;
				shrub[i] = shrub[i] * SW_Input.prodIn.vegComp.shrub;
				tree[i] = tree[i] * SW_Input.prodIn.vegComp.tree;
				forb[i] = forb[i] * SW_Input.prodIn.vegComp.forb;
			}
			//Sum for months
			for(int i=0; i<12; i++) {
				sumWeightedLiveBiomassByMonth.add(grass[i]+shrub[i]+tree[i]+forb[i]);
			}
			max = Collections.max(sumWeightedLiveBiomassByMonth);
			ArrayList<Integer> maxMonths = new ArrayList<Integer>();
			int i=0;
			for (i=0; i<12; i++) {
				if(Double.compare(max, sumWeightedLiveBiomassByMonth.get(i))==0) {
					maxMonths.add(i);
				}
			}
			values[0] = Circular.circ_mean((Double[]) maxMonths.toArray(), 12, false);
			values[1] = Circular.circ_range((Double[]) maxMonths.toArray(), 12, false)+1;
		}

		@Override
		String getMultiLinePrefix(boolean header, int line) {
			return "";
		}
		
	}
	public class Phenology extends Aggregate {

		@Override
		protected void setNames() {
			columnNames = new ArrayList<String>();
			columnNames.add("SWinput.PeakLiveBiomass_month_mean");
			columnNames.add("SWinput.PeakLiveBiomass_months_duration");
		}

		@Override
		protected void setValues() {
			
		}

		@Override
		String getMultiLinePrefix(boolean header, int line) {
			return "";
		}
	
	}
	public class TranspirationCoeff extends Aggregate {

		@Override
		protected void setNames() {
			
		}

		@Override
		protected void setValues() {
			
		}

		@Override
		String getMultiLinePrefix(boolean header, int line) {
			return "";
		}
		
	}
	public class ClimatePerturbations extends Aggregate {

		@Override
		protected void setNames() {
			
		}

		@Override
		protected void setValues() {
			
		}

		@Override
		String getMultiLinePrefix(boolean header, int line) {
			return "";
		}
		
	}*/
}
