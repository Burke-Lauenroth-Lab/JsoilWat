package soilwat;

import java.io.IOException;
import java.util.List;

import events.SoilwatEvent;
import events.SoilwatListener;
import soilwat.Defines.ObjType;
import soilwat.SW_OUTPUT.OutKey;
import soilwat.SW_OUTPUT.OutPeriod;
import soilwat.SW_WEATHER.WEATHER;
import soilwat.SW_OUTPUT.SW_OUT_TIME;
import soilwat.SW_SOILS.LayersInfo;

public class SW_CONTROL {
	private SW_FILES SW_Files;
	private SW_MODEL SW_Model;
	private SW_SKY SW_Sky;
	private SW_WEATHER SW_Weather;
	private SW_MARKOV SW_Markov;
	private SW_VEGPROD SW_VegProd;
	private SW_SOILS SW_Soils;
	private SW_SITE SW_Site;
	private SW_SOILWATER SW_SoilWater;
	private SW_VEGESTAB SW_VegEstab;
	private SW_OUTPUT SW_Output;
	private LogFileIn log;
	
	protected SoilwatListener soilwatListener;
	
	public void addSoilwatEventListener(SoilwatListener e) {
		soilwatListener = e;
	}
	
	public SW_CONTROL() {
		log = new LogFileIn();
		SW_Files = new SW_FILES(log);
		SW_Model = new SW_MODEL(log);
		SW_Sky = new SW_SKY();
		SW_Markov = new SW_MARKOV(log);
		SW_Weather = new SW_WEATHER(log,SW_Model, SW_Markov);
		SW_Soils = new SW_SOILS(log);
		SW_VegProd = new SW_VEGPROD(log);
		SW_Site = new SW_SITE(log,SW_VegProd, SW_Soils);
		SW_SoilWater = new SW_SOILWATER(log,SW_Model, SW_Site, SW_Soils, SW_Weather, SW_VegProd, SW_Sky);
		SW_VegEstab = new SW_VEGESTAB(log,SW_Weather, SW_SoilWater, SW_Model, SW_Soils);
		SW_Output = new SW_OUTPUT(log,SW_Soils, SW_SoilWater, SW_Model, SW_Weather, SW_VegEstab);
		SW_Weather.setSoilWater(SW_SoilWater);
	}
	
	public double[][] onGetOutput(OutKey key, OutPeriod period) {
		return SW_Output.get_data(key, period);
	}
	
	public SW_OUT_TIME onGet_Timing() {
		return SW_Output.get_Timing();
	}
	
	public int onGet_nColumns(OutKey key) {
		return SW_Output.get_nColumns(key);
	}
	
	public String[] onGet_OutputColumnNames(OutKey key) {
		return SW_Output.get_ColumnNames(key);
	}
	
	public String onGet_Unit(OutKey key) {
		return SW_Output.get_Unit(key);
	}
	
	public void onSetInput(InputData data) throws Exception {
		//Copy the lines so far from input log file.
		for(String m : data.log.onGetLog()) {
			this.log.onGetLog().add(m);
		}
		
		SW_Files.onSetInput(data.filesIn);
		//System.out.println("filesIn");
		SW_Model.onSetInput(data.yearsIn);
		//System.out.println("yearsIn");
		SW_Sky.onSetInput(data.cloudIn);
		//System.out.println("cloudIn");
		SW_Markov.onSetInput(data.markovIn);
		//System.out.println("markovIn");
		SW_Weather.onSetInput(data.weatherSetupIn);
		//System.out.println("weatherSetupIn");
		SW_Weather.onSetWeatherHist(data.weatherHist);
		//System.out.println("weatherHist");
		SW_VegProd.onSetInput(data.prodIn);
		//System.out.println("prodIn");
		SW_Soils.onSetInput(data.soilsIn);
		//System.out.println("soilsIn");
		SW_Site.onSetInput(data.siteIn);
		//System.out.println("siteIn");
		SW_SoilWater.onSetInput(data.swcSetupIn);
		//System.out.println("swcSetupIn");
		if(data.swcSetupIn.hist_use) {
			SW_SoilWater.onSetHist(data.swcHist);
			//System.out.println("swcHist");
		}
		SW_VegEstab.onSetInput(data.estabIn);
		//System.out.println("estabIn");
		SW_Output.onSetInput(data.outputSetupIn);
		//System.out.println("outputSetupIn");
	}
	
	public void onGetInput(InputData data) throws Exception {
		for(String m : this.log.onGetLog()) {
			data.log.onGetLog().add(m);
		}
		SW_Files.onGetInput(data.filesIn);
		SW_Model.onGetInput(data.yearsIn);
		SW_Sky.onGetInput(data.cloudIn);
		SW_Markov.onGetInput(data.markovIn);
		SW_Weather.onGetInput(data.weatherSetupIn);
		SW_Weather.onGetWeatherHist(data.weatherHist);
		SW_VegProd.onGetInput(data.prodIn);
		SW_Soils.onGetInput(data.soilsIn);
		SW_Site.onGetInput(data.siteIn);
		SW_SoilWater.onGetInput(data.swcSetupIn);
		if(data.swcSetupIn.hist_use)
			SW_SoilWater.onGetHist(data.swcHist);
		SW_VegEstab.onGetInput(data.estabIn);
		SW_Output.onGetInput(data.outputSetupIn);
	}
	
	public void onStartModel(boolean echo, boolean quiet, boolean writeOutput) throws Exception {
		int year;
			
		_set_echo(echo);
		log.setQuiet(quiet);
		
		int years = SW_Model.getYearsInSimulation();
		double Percent = 0;
		if(onVerify()) {
			for(year = SW_Model.getStartYear(); year<=SW_Model.getEndYear(); year++) {
				SW_Model.setYear(year);
				SW_CTL_run_current_year();
				if(soilwatListener != null) {
					Percent = ((double) year-SW_Model.getStartYear()+1)/((double)years);
					soilwatListener.soilwatEvent(new SoilwatEvent(year, Percent));
				}
			}
			if(writeOutput) {
				try {
					SW_Output.onWriteOutputs();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<String> onGetLog() {
		return log.onGetLog();
	}
	
	public void onClear() {
		SW_Files.onClear();
		SW_Model.onClear();
		SW_Sky.onClear();
		SW_Weather.onClear();
		SW_Soils.onClear();
		SW_VegProd.onClear();
		SW_Site.onClear();
		SW_SoilWater.onClear();
		SW_VegEstab.onClear();
		SW_Output.onClear();
		log.onClear();
	}
		
	public boolean onVerify() throws Exception {
		WEATHER w = SW_Weather.getWeather();
		return SW_Files.onVerify() &&
		SW_Model.onVerify() &&
		SW_Sky.onVerify(w.scale_skyCover, w.scale_wind, w.scale_rH, w.scale_transmissivity) &&
		SW_Weather.onVerify() &&
		SW_VegProd.onVerify() &&
		SW_Soils.onVerify(SW_Site.getDeepdrain()) &&
		SW_Site.onVerify() &&
		SW_SoilWater.onVerify() &&
		SW_Output.onVerify(SW_Site.getDeepdrain(), SW_Files.getOutputDirectory(true)) &&
		SW_VegEstab.onVerify();
	}
	
	public void SW_CTL_run_current_year() throws Exception {
		/*=======================================================*/
		_begin_year();

		for (SW_Model.setDOY(SW_Model.getFirstdoy()); SW_Model.getDOY() <= SW_Model.getLastdoy(); SW_Model.setDOY(SW_Model.getDOY()+1)) {
			//System.out.println(String.valueOf(SW_Model.getYear()) + " " + String.valueOf(SW_Model.getDOY()));
			_begin_day();

			SW_SoilWater.SW_SWC_water_flow();

			if (SW_VegEstab.get_use())
				SW_VegEstab.onCheckEstab(SW_Model.getDOY());;
			_end_day();
		}
		SW_Output.SW_OUT_flush();
	}
	
	private void _begin_year() throws Exception {
		SW_Model.SW_MDL_new_year();
		SW_Weather.SW_WTH_new_year();
		SW_SoilWater.SW_SWC_new_year();
		SW_VegEstab.SW_VES_new_year();
		SW_Output.SW_OUT_new_year();
	}
	
	private void _begin_day() throws Exception {
		SW_Model.SW_MDL_new_day();
		SW_Weather.SW_WTH_new_day();
	}
	
	private void _end_day() throws Exception {
		_collect_values();
		SW_Weather.SW_WTH_end_day();
		SW_SoilWater.SW_SWC_end_doy();
	}
	
	private void _collect_values() throws Exception {
		SW_Output.SW_OUT_sum_today(ObjType.eSWC);
		SW_Output.SW_OUT_sum_today(ObjType.eWTH);
		SW_Output.SW_OUT_sum_today(ObjType.eVES);
		SW_Output.SW_OUT_write_today();
	}
	
	private void _set_echo(boolean echo) {
		this.SW_Output.set_echoinits(echo);
		this.SW_Site.set_echoinits(echo);
		this.SW_Soils.set_echoinits(echo);
		this.SW_VegEstab.set_echoinits(echo);
		this.SW_VegProd.set_echoinits(echo);
	}
	
	//These functions are for STEPWAT
	/**
	 * This function will return a copy of the Layers info.
	 * @return
	 */
	public LayersInfo getLayersInfo() {
		return this.SW_Soils.getLayersInfo();
	}
	
	public void setTranspCoeffTree(int layer, double value) {
		this.SW_Soils.getLayer(layer).transp_coeff_tree = value;
	}
	public void setTranspCoeffShrub(int layer, double value) {
		this.SW_Soils.getLayer(layer).transp_coeff_shrub = value;
	}
	public void setTranspCoeffGrass(int layer, double value) {
		this.SW_Soils.getLayer(layer).transp_coeff_grass = value;
	}
	public void setTranspCoeffForb(int layer, double value) {
		this.SW_Soils.getLayer(layer).transp_coeff_forb = value;
	}
	
	public double getTranspCoeffTree(int layer) {
		return this.SW_Soils.getLayer(layer).transp_coeff_tree;
	}
	public double getTranspCoeffShrub(int layer) {
		return this.SW_Soils.getLayer(layer).transp_coeff_shrub;
	}
	public double getTranspCoeffGrass(int layer) {
		return this.SW_Soils.getLayer(layer).transp_coeff_grass;
	}
	public double getTranspCoeffForb(int layer) {
		return this.SW_Soils.getLayer(layer).transp_coeff_forb;
	}
	
	public void resetProdDaily() {
		this.SW_VegProd.getDailyValues().onClear();
		this.SW_VegProd.SW_VPD_init();
	}
	
	public double getProdDailyLai_live(int doyStart, int doyEnd) {
		double sum = 0;
		for(int i=doyStart; i<doyEnd; i++) {
			sum += this.SW_VegProd.getDailyValues().tree.lai_live_daily[i] +
					this.SW_VegProd.getDailyValues().shrub.lai_live_daily[i] +
					this.SW_VegProd.getDailyValues().grass.lai_live_daily[i] +
					this.SW_VegProd.getDailyValues().forb.lai_live_daily[i];
		}
		return sum;
	}
	
	public double getProdDailyVegCov(int doyStart, int doyEnd) {
		double sum = 0;
		for(int i=doyStart; i<doyEnd; i++) {
			sum += this.SW_VegProd.getDailyValues().tree.vegcov_daily[i] +
					this.SW_VegProd.getDailyValues().shrub.vegcov_daily[i] +
					this.SW_VegProd.getDailyValues().grass.vegcov_daily[i] +
					this.SW_VegProd.getDailyValues().forb.vegcov_daily[i];
		}
		return sum;
	}
	
	public double getProdDailyTotalAgb(int doyStart, int doyEnd) {
		double sum = 0;
		for(int i=doyStart; i<doyEnd; i++) {
			sum += this.SW_VegProd.getDailyValues().tree.total_agb_daily[i] +
					this.SW_VegProd.getDailyValues().shrub.total_agb_daily[i] +
					this.SW_VegProd.getDailyValues().grass.total_agb_daily[i] +
					this.SW_VegProd.getDailyValues().forb.total_agb_daily[i];
		}
		return sum;
	}
	
	public double getProdDailyPCTLive(int doyStart, int doyEnd) {
		double sum = 0;
		for(int i=doyStart; i<doyEnd; i++) {
			sum += this.SW_VegProd.getDailyValues().tree.pct_live_daily[i] +
					this.SW_VegProd.getDailyValues().shrub.pct_live_daily[i] +
					this.SW_VegProd.getDailyValues().grass.pct_live_daily[i] +
					this.SW_VegProd.getDailyValues().forb.pct_live_daily[i];
		}
		return sum;
	}
	
	public double getProdDailyBiomass(int doyStart, int doyEnd) {
		double sum = 0;
		for(int i=doyStart; i<doyEnd; i++) {
			sum += this.SW_VegProd.getDailyValues().tree.biomass_daily[i] +
					this.SW_VegProd.getDailyValues().shrub.biomass_daily[i] +
					this.SW_VegProd.getDailyValues().grass.biomass_daily[i] +
					this.SW_VegProd.getDailyValues().forb.biomass_daily[i];
		}
		return sum;
	}
	
	public void setMonthlyLitter(int month, double grasses, double shrubs, double trees, double forbs) {
		this.SW_VegProd.setLitter(month, grasses, shrubs, trees, forbs);
	}
	public void setMonthlyBiomass(int month, double grasses, double shrubs, double trees, double forbs) {
		this.SW_VegProd.setBiomass(month, grasses, shrubs, trees, forbs);
	}
	public void setMonthlyPCTLive(int month, double grasses, double shrubs, double trees, double forbs) {
		this.SW_VegProd.setPercLive(month, grasses, shrubs, trees, forbs);
	}
	
	public double[][] getMonthlyProductionGrass() {
		double[][] m = new double[12][4];
		for(int i=0; i<12; i++) {
			for(int j=0; j<4; j++) {
				switch(j) {
				case 0:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().grass.litter[i];
					break;
				case 1:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().grass.biomass[i];
					break;
				case 3:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().grass.percLive[i];
					break;
				case 4:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().grass.lai_conv[i];
					break;
				}
			}
		}
		return m;
	}
	
	public double[][] getMonthlyProductionShrub() {
		double[][] m = new double[12][4];
		for(int i=0; i<12; i++) {
			for(int j=0; j<4; j++) {
				switch(j) {
				case 0:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().shrub.litter[i];
					break;
				case 1:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().shrub.biomass[i];
					break;
				case 3:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().shrub.percLive[i];
					break;
				case 4:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().shrub.lai_conv[i];
					break;
				}
			}
		}
		return m;
	}
	
	public double[][] getMonthlyProductionTree() {
		double[][] m = new double[12][4];
		for(int i=0; i<12; i++) {
			for(int j=0; j<4; j++) {
				switch(j) {
				case 0:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().tree.litter[i];
					break;
				case 1:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().tree.biomass[i];
					break;
				case 3:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().tree.percLive[i];
					break;
				case 4:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().tree.lai_conv[i];
					break;
				}
			}
		}
		return m;
	}
	
	public double[][] getMonthlyProductionForb() {
		double[][] m = new double[12][4];
		for(int i=0; i<12; i++) {
			for(int j=0; j<4; j++) {
				switch(j) {
				case 0:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().forb.litter[i];
					break;
				case 1:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().forb.biomass[i];
					break;
				case 3:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().forb.percLive[i];
					break;
				case 4:
					m[i][j] = this.SW_VegProd.getMonthlyProductionValues().forb.lai_conv[i];
					break;
				}
			}
		}
		return m;
	}
	
	public double getGrassFrac() {
		return this.SW_VegProd.getVegetationComposition().grass;
	}
	
	public double getShrubFrac() {
		return this.SW_VegProd.getVegetationComposition().shrub;
	}
	
	public double getTreeFrac() {
		return this.SW_VegProd.getVegetationComposition().tree;
	}
	
	public double getForbFrac() {
		return this.SW_VegProd.getVegetationComposition().forb;
	}
	
	public double getBareGroundFrac() {
		return this.SW_VegProd.getVegetationComposition().bareGround;
	}
	
	public void setVegComposition(double grassFrac, double shrubFrac, double treeFrac, double forbFrac, double baregroundFrac) {
		this.SW_VegProd.getVegetationComposition().onSet(grassFrac, shrubFrac, treeFrac, forbFrac, baregroundFrac);
	}
	
	public int getStartYear() {
		return SW_Model.getStartYear();
	}
	
	public int getYear() {
		return this.SW_Model.getYear();
	}
	
	public void setYear(int year) {
		this.SW_Model.setYear(year);
	}
	
	/**
	 * This is used to get a total year PPT value with the given row as year index from 0. 
	 * @param row
	 * @return
	 */
	public double getYearTotalPPT(int row) {
		return onGetOutput(OutKey.eSW_Precip, OutPeriod.SW_YEAR)[row][0];
	}
	
	/**
	 * This is used to get the year avg Temp with the given row as the year index from 0.
	 * @param row
	 * @return
	 */
	public double getYearAvgTemp(int row) {
		return onGetOutput(OutKey.eSW_Temp, OutPeriod.SW_YEAR)[row][2];
	}
	
	public double getYearAET(int row) {
		return onGetOutput(OutKey.eSW_AET, OutPeriod.SW_YEAR)[row][0];
	}
	
	public double[][] getMonthlyTranspirationGrass(int row) {
		int nLyrs = this.SW_Soils.layersInfo.n_layers;
		double[][] transpGrass = new double[12][nLyrs];
		double[][] values = onGetOutput(OutKey.eSW_Transp, OutPeriod.SW_MONTH);
		for(int i=0; i<12; i++) {
			for(int j=0; j<nLyrs; j++) {
				transpGrass[i][j] = values[row*12+i][4*nLyrs+j];
			}
		}
		return transpGrass;
	}
	
	public double[][] getMonthlyTranspirationShrub(int row) {
		int nLyrs = this.SW_Soils.layersInfo.n_layers;
		double[][] transpShrub = new double[12][nLyrs];
		double[][] values = onGetOutput(OutKey.eSW_Transp, OutPeriod.SW_MONTH);
		for(int i=0; i<12; i++) {
			for(int j=0; j<nLyrs; j++) {
				transpShrub[i][j] = values[row*12+i][2*nLyrs+j];
			}
		}
		return transpShrub;
	}
	
	public double[][] getMonthlyTranspirationTree(int row) {
		int nLyrs = this.SW_Soils.layersInfo.n_layers;
		double[][] transpTree = new double[12][nLyrs];
		double[][] values = onGetOutput(OutKey.eSW_Transp, OutPeriod.SW_MONTH);
		for(int i=0; i<12; i++) {
			for(int j=0; j<nLyrs; j++) {
				transpTree[i][j] = values[row*12+i][1*nLyrs+j];
			}
		}
		return transpTree;
	}
	
	public double[][] getMonthlyTranspirationForb(int row) {
		int nLyrs = this.SW_Soils.layersInfo.n_layers;
		double[][] transpForb = new double[12][nLyrs];
		double[][] values = onGetOutput(OutKey.eSW_Transp, OutPeriod.SW_MONTH);
		for(int i=0; i<12; i++) {
			for(int j=0; j<nLyrs; j++) {
				transpForb[i][j] = values[row*12+i][3*nLyrs+j];
			}
		}
		return transpForb;
	}
	
	public double[][] getMonthlyTranspirationTotal(int row) {
		int nLyrs = this.SW_Soils.layersInfo.n_layers;
		double[][] transpTotal = new double[12][nLyrs];
		double[][] values = onGetOutput(OutKey.eSW_Transp, OutPeriod.SW_MONTH);
		for(int i=0; i<12; i++) {
			for(int j=0; j<nLyrs; j++) {
				transpTotal[i][j] = values[row*12+i][0*nLyrs+j];
			}
		}
		return transpTotal;
	}
	
	public double[][] getMonthlySWCBulk(int row) {
		int nLyrs = this.SW_Soils.layersInfo.n_layers;
		double[][] swc = new double[12][nLyrs];
		double[][] values = onGetOutput(OutKey.eSW_SWCBulk, OutPeriod.SW_MONTH);
		for(int i=0; i<12; i++) {
			for(int j=0; j<nLyrs; j++) {
				swc[i][j] = values[row*12+i][j];
			}
		}
		return swc;
	}
}
