package soilwat;

import java.io.IOException;
import java.nio.file.Paths;

import soilwat.Defines.ObjType;
import soilwat.SW_OUTPUT.OutKey;
import soilwat.SW_OUTPUT.OutPeriod;
import soilwat.SW_WEATHER.WEATHER;
import soilwat.SW_OUTPUT.SW_OUT_TIME;

public class SW_CONTROL {
	private SW_FILES SW_Files;
	private SW_MODEL SW_Model;
	private SW_SKY SW_Sky;
	private SW_WEATHER SW_Weather;
	private SW_VEGPROD SW_VegProd;
	private SW_SOILS SW_Soils;
	private SW_SITE SW_Site;
	private SW_SOILWATER SW_SoilWater;
	private SW_VEGESTAB SW_VegEstab;
	private SW_OUTPUT SW_Output;
	
	public SW_CONTROL() {
		SW_Files = new SW_FILES();
		SW_Model = new SW_MODEL();
		SW_Sky = new SW_SKY();
		SW_Weather = new SW_WEATHER(SW_Model);
		SW_Soils = new SW_SOILS();
		SW_VegProd = new SW_VEGPROD();
		SW_Site = new SW_SITE(SW_VegProd, SW_Soils);
		SW_SoilWater = new SW_SOILWATER(SW_Model, SW_Site, SW_Soils, SW_Weather, SW_VegProd, SW_Sky);
		SW_VegEstab = new SW_VEGESTAB(SW_Weather, SW_SoilWater, SW_Model, SW_Soils);
		SW_Output = new SW_OUTPUT(SW_Soils, SW_SoilWater, SW_Model, SW_Weather, SW_VegEstab);
		SW_Weather.setSoilWater(SW_SoilWater);
	}
	
	public double[][] onGetOutput(OutKey key, OutPeriod period) {
		return SW_Output.get_data(key, period);
	}
	
	public SW_OUT_TIME onGet_Timing() {
		return SW_Output.get_Timing();
	}
	
	public void onSetInput(InputData data) throws Exception {
		SW_Files.onSetInput(data.filesIn);
		SW_Model.onSetInput(data.yearsIn);
		SW_Sky.onSetInput(data.cloudIn);
		SW_Weather.onSetInput(data.weatherSetupIn);
		SW_Weather.onSetWeatherHist(data.weatherHist);
		SW_VegProd.onSetInput(data.prodIn);
		SW_Soils.onSetInput(data.soilsIn);
		SW_Site.onSetInput(data.siteIn);
		SW_SoilWater.onSetInput(data.swcSetupIn);
		if(data.swcSetupIn.hist_use)
			SW_SoilWater.onSetHist(data.swcHist);
		SW_VegEstab.onSetInput(data.estabIn);
		SW_Output.onSetInput(data.outputSetupIn);
	}
	
	public void onGetInput(InputData data) throws Exception {
		SW_Files.onGetInput(data.filesIn);
		SW_Model.onGetInput(data.yearsIn);
		SW_Sky.onGetInput(data.cloudIn);
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
	
	public void onReadInputs(String swFiles) throws Exception {
		try {
			SW_Files.onRead(swFiles);
			SW_Model.onRead(SW_Files.getYearsIn(true));
			SW_Sky.onRead(SW_Files.getCloudIn(true));
			SW_Weather.onRead(SW_Files.getWeatherSetupIn(true), SW_Files.getMarkovProbabilityIn(true), SW_Files.getMarkovCovarianceIn(true));
			SW_Weather.onReadHistory(SW_Files.getWeatherPath(true), SW_Files.getWeatherPrefix());
			SW_VegProd.onRead(SW_Files.getPlantProductivityIn(true));
			SW_Soils.onRead(SW_Files.getSoilsIn(true));
			SW_Site.onRead(SW_Files.getSiteParametersIn(true));
			SW_SoilWater.onRead(SW_Files.getSWCSetupIn(true), SW_Files.getWeatherPath(true));
			SW_VegEstab.onRead(SW_Files.getEstablishmentIn(true), SW_Files.getProjectDirectory());
			SW_Output.onRead(SW_Files.getOutputSetupIn(true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onWriteOutputs(String ProjectDirectory) throws Exception {
		SW_Files.setProjectDirectory(Paths.get(ProjectDirectory));
		SW_Files.onCreateFiles();
		SW_Files.onVerify();
		try {
			SW_Files.onWrite();
			SW_Model.onWrite(SW_Files.getYearsIn(true));
			SW_Sky.onWrite(SW_Files.getCloudIn(true));
			SW_Weather.onWrite(SW_Files.getWeatherSetupIn(true));
			//if(SW_Weather.getWeather().use_markov) {
			//	SW_Weather.getMarkov().
			//}
			SW_Weather.onWriteHistory(SW_Files.getWeatherPath(true), SW_Files.getWeatherPrefix());
			SW_VegProd.onWrite(SW_Files.getPlantProductivityIn(true));
			SW_Soils.onWrite(SW_Files.getSoilsIn(true));
			SW_Site.onWrite(SW_Files.getSiteParametersIn(true));
			SW_SoilWater.onWrite(SW_Files.getSWCSetupIn(true));
			if(SW_SoilWater.getSoilWat().hist_use) {
				SW_SoilWater.onWriteHistory(SW_Files.getWeatherPath(true), SW_SoilWater.getSoilWat().filePrefix);
			}
			SW_VegEstab.onWrite(SW_Files.getEstablishmentIn(true), SW_Files.getProjectDirectory());
			SW_Output.onWrite(SW_Files.getOutputSetupIn(true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onStartModel(boolean echo) throws Exception {
		int year;
		
		_set_echo(echo);
		
		if(onVerify()) {
			for(year = SW_Model.getStartYear(); year<=SW_Model.getEndYear(); year++) {
				SW_Model.setYear(year);
				SW_CTL_run_current_year();
			}

			try {
				SW_Output.onWriteOutputs();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		SW_Weather.onClear();
		LogFileIn.getInstance().onClear();
	}
		
	private boolean onVerify() throws Exception {
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
	
	private void SW_CTL_run_current_year() throws Exception {
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
}
