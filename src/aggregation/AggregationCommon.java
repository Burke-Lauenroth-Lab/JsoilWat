package aggregation;

import soilwat.InputData;
import soilwat.SW_CONTROL;
import soilwat.SW_OUTPUT.OutKey;
import soilwat.SW_OUTPUT.OutPeriod;

public class AggregationCommon {
	public class GrassTypeFractions {
		public double c3;
		public double c4;
		public double ann;
	}
	public class LayerInformation {
		int TopL_start;
		int TopL_stop;
		int BottomL_start;
		int BottomL_stop;
		double[] layers_width;
	}
	public class Texture {
		double sand_top;
		double sand_bottom;
		double clay_top;
		double clay_bottom;
		
		public Texture(LayerInformation Lyrs, InputData in) {
			sand_top = 0;
			clay_top = 0;
			//Weighted Mean
			for(int i=(Lyrs.TopL_start-1); i<(Lyrs.TopL_stop-1); i++) {
				sand_top += in.soilsIn.layers[i].fractionWeightMatric_sand * Lyrs.layers_width[i];
				clay_top += in.soilsIn.layers[i].fractionWeightMatric_clay * Lyrs.layers_width[i];
			}
			sand_bottom=0;
			clay_bottom=0;
			for(int i=(Lyrs.BottomL_start-1); i<(Lyrs.BottomL_stop-1); i++) {
				sand_top += in.soilsIn.layers[i].fractionWeightMatric_sand * Lyrs.layers_width[i];
				clay_top += in.soilsIn.layers[i].fractionWeightMatric_clay * Lyrs.layers_width[i];
			}
		}
	}
	public class SimTime {
		public int startyr;
		public int endyr;
		public int no_useyr;
		private int no_usemo;
		private int no_usedy;
		private int discardyr;
		private int discardmo;
		private int discarddy;
		public int idx_yr_s;
		public int idx_mo_s;
		public int idx_dy_s;
		public int idx_yr_e;
		public int idx_mo_e;
		public int idx_dy_e;
		
		public SimTime(int startYr, int simStartYear, int endYear) {
			startyr=startYr;
			endyr = endYear;
			no_useyr = endyr - startyr + 1;
			no_usemo = no_useyr * 12;
			
		}
	}
	public class PPT {
		public double[] ppt;
		public double[] rain;
		public double[] snowfall;
		public double[] snowmelt;
		public double[] snowloss;
		private boolean set=false;
	}
	
	private double[] temp_yr_mean;
	private double[] temp_mo_mean;
	private double[] temp_mo_min;
	private double[] temp_dy_mean;
	private double[] temp_dy_min;
	private PPT PPT_yr;
	private PPT PPT_mo;
	private PPT PPT_dy;

	private SW_CONTROL SW_Control;
	
	public void get_Temp_yr_mean() {
		double[][] temp = SW_Control.onGetOutput(OutKey.eSW_Temp, OutPeriod.SW_YEAR);
		if(temp_yr_mean == null) {
			temp_yr_mean = new double[temp.length];
		} else {
			if(temp_yr_mean.length < temp.length)
				temp_yr_mean = new double[temp.length];
		}
		for(int i=0; i<temp.length; i++) {
			temp_yr_mean[i] = temp[i][2];
		}
	}
	public void get_Temp_mo_mean() {
		double[][] temp = SW_Control.onGetOutput(OutKey.eSW_Temp, OutPeriod.SW_MONTH);
		if(temp_mo_mean == null) {
			temp_mo_mean = new double[temp.length];
		} else {
			if(temp_mo_mean.length < temp.length)
				temp_mo_mean = new double[temp.length];
		}
		for(int i=0; i<temp.length; i++) {
			temp_mo_mean[i] = temp[i][2];
		}
	}
	
	public double[] get_Temp_mo_min() {
		double[][] temp = SW_Control.onGetOutput(OutKey.eSW_Temp, OutPeriod.SW_MONTH);
		double[] min = new double[temp.length];
		for(int i=0; i<min.length; i++) {
			min[i] = temp[i][1];
		}
		return min;
	}
	
	public double[] get_Temp_dy_mean() {
		double[][] temp = SW_Control.onGetOutput(OutKey.eSW_Temp, OutPeriod.SW_DAY);
		double[] mean = new double[temp.length];
		for(int i=0; i<mean.length; i++) {
			mean[i] = temp[i][2];
		}
		return mean;
	}
	
	public double[] get_Temp_dy_min() {
		double[][] temp = SW_Control.onGetOutput(OutKey.eSW_Temp, OutPeriod.SW_DAY);
		double[] min = new double[temp.length];
		for(int i=0; i<min.length; i++) {
			min[i] = temp[i][1];
		}
		return min;
	}
}
