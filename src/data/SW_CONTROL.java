package data;

import java.io.IOException;

import defines.Defines.ObjType;

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
	
	public void onReadInputs(String swFiles) {
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
			SW_Output.onRead(SW_Files.getOutputSetupIn(true), SW_Files.getOutputDirectory(true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean onVerify() {
		return SW_Files.onVerify() &&
		SW_Model.onVerify() &&
		SW_Sky.onVerify() &&
		SW_Weather.onVerify() &&
		SW_VegProd.onVerify() &&
		SW_Site.onVerify() &&
		SW_Soils.onVerify(SW_Site.getDeepdrain()) &&
		SW_SoilWater.onVerify() &&
		SW_Output.onVerify(SW_Site.getDeepdrain());
		//SW_VegEstab.onVerify();
	}
	
	public void onStartModel() {
		int year;
		
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
	
	private void SW_CTL_run_current_year() {
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
	
	private void _begin_year() {
		SW_Model.SW_MDL_new_year();
		SW_Weather.SW_WTH_new_year();
		SW_SoilWater.SW_SWC_new_year();
		SW_VegEstab.SW_VES_new_year();
		SW_Output.SW_OUT_new_year();
	}
	
	private void _begin_day() {
		SW_Model.SW_MDL_new_day();
		SW_Weather.SW_WTH_new_day();
	}
	
	private void _end_day() {
		_collect_values();
		SW_Weather.SW_WTH_end_day();
		SW_SoilWater.SW_SWC_end_doy();
	}
	
	private void _collect_values() {
		SW_Output.SW_OUT_sum_today(ObjType.eSWC);
		SW_Output.SW_OUT_sum_today(ObjType.eWTH);
		SW_Output.SW_OUT_sum_today(ObjType.eVES);
		
		SW_Output.SW_OUT_write_today();
	}
}
