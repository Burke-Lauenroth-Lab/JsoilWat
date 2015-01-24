package soilwat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.LogFileIn.LogMode;

public class SW_WEATHER {
	/* missing values may be different than with other things */
	public static final double WTH_MISSING=999.;

	/*  all temps are in degrees C, all precip is in cm */
	/*  in fact, all water variables are in cm throughout
	 *  the model.  this facilitates additions and removals
	 *  as they're always in the right units.
	 */

	protected static class SW_WEATHER_2DAYS {
		/* comes from markov weather day-to-day */
		protected double temp_yr_avg,gsppt; /* gr. season ppt only needs one day */
		protected double[] temp_avg, temp_run_avg, /* year's avg for STEPPE */
		temp_max, temp_min, ppt, /* 20091015 (drs) ppt is divided into rain and snow */
		rain, snow, snowmelt, snowloss, ppt_actual; /* 20091015 (drs) was here previously, but not used in code as of today */
		
		
		protected SW_WEATHER_2DAYS() {
			temp_avg = new double[Defines.TWO_DAYS];
			temp_run_avg = new double[Defines.TWO_DAYS];
			temp_max = new double[Defines.TWO_DAYS];
			temp_min = new double[Defines.TWO_DAYS];
			ppt = new double[Defines.TWO_DAYS];
			rain = new double[Defines.TWO_DAYS];
			snow = new double[Defines.TWO_DAYS];
			snowmelt = new double[Defines.TWO_DAYS];
			snowloss = new double[Defines.TWO_DAYS];
			ppt_actual = new double[Defines.TWO_DAYS];
			gsppt = 0;
			temp_yr_avg=0;
		}
		protected void onClear() {
			for(int i=0; i<Defines.TWO_DAYS; i++) {
				temp_avg[i] = 0;
				temp_run_avg[i] = 0;
				temp_max[i] = 0;
				temp_min[i] = 0;
				ppt[i] = 0;
				rain[i] = 0;
				snow[i] = 0;
				snowmelt[i] = 0;
				snowloss[i] = 0;
				ppt_actual[i] =0;
			}
			gsppt = 0;
			temp_yr_avg = 0;
		}
	}

	/* accumulators for output values hold only the */
	/* current period's values (eg, weekly or monthly) */
	protected static class SW_WEATHER_OUTPUTS {
		protected double temp_max, temp_min, temp_avg, ppt, rain, snow, snowmelt, snowloss, /* 20091015 (drs) ppt is divided into rain and snow */
		snowRunoff, surfaceRunoff, soil_inf, et, aet, pet;
		
		protected SW_WEATHER_OUTPUTS() {
			
		}
		
		protected void onClear() {
			temp_max=temp_min=temp_avg=ppt=rain=snow=snowmelt=snowloss=0;
			snowRunoff=surfaceRunoff=soil_inf=et=aet=pet=0;
		}
	}
	
	public static class WEATHER_INPUT_DATA {
		public boolean use_markov; /* TRUE=use markov for any year missing a weather */
		/*      file, which means markov must be initialized */
		/* FALSE = fail if any weather file is missing.  */
		public boolean use_snow;
		public double pct_snowdrift, pct_snowRunoff;
		public int days_in_runavg;
		public SW_TIMES yr;		
		public double[] scale_precip = new double[Times.MAX_MONTHS], scale_temp_max = new double[Times.MAX_MONTHS], scale_temp_min = new double[Times.MAX_MONTHS];
		public double[] scale_skyCover = new double[Times.MAX_MONTHS], scale_wind = new double[Times.MAX_MONTHS], scale_rH = new double[Times.MAX_MONTHS], scale_transmissivity = new double[Times.MAX_MONTHS];
		public String get_MonthlyScaling_toString(int month) {
			return String.format("%4s\t%5f\t%5f\t%5f\t%8f\t%5f\t%5f\t%14f", String.valueOf(month+1),scale_precip[month],scale_temp_max[month],scale_temp_min[month],scale_skyCover[month],scale_wind[month],scale_rH[month],scale_transmissivity[month]);
		}
	}

	protected static class WEATHER extends WEATHER_INPUT_DATA {
		protected double[] runavg_list;
		protected String name_prefix;
		protected double snowRunoff, surfaceRunoff, soil_inf;

		/* This section is required for computing the output quantities.  */
		protected SW_WEATHER_OUTPUTS dysum, /* helpful placeholder */
		wksum, mosum, yrsum, /* accumulators for *avg */
		wkavg, moavg, yravg; /* averages or sums as appropriate*/
		
		protected SW_WEATHER_2DAYS now;
		
		public WEATHER() {
			yr = new SW_TIMES();
			dysum = new SW_WEATHER_OUTPUTS();
			wksum = new SW_WEATHER_OUTPUTS();
			mosum = new SW_WEATHER_OUTPUTS();
			yrsum = new SW_WEATHER_OUTPUTS();
			wkavg = new SW_WEATHER_OUTPUTS();
			moavg = new SW_WEATHER_OUTPUTS();
			yravg = new SW_WEATHER_OUTPUTS();
			now = new SW_WEATHER_2DAYS();
		}
		
		public void onClear() {
			for(int i=0; i<Times.MAX_MONTHS; i++) {
				scale_precip[i] = 0;
				scale_temp_max[i] = 0;
				scale_temp_min[i] = 0;
			}
			yr.onClear();
			dysum.onClear();
			wksum.onClear();
			mosum.onClear();
			yrsum.onClear();
			wkavg.onClear();
			moavg.onClear();
			yravg.onClear();
			now.onClear();
		}
		public double[] getTempRow(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.temp_max, dysum.temp_min, dysum.temp_avg};
			case SW_WEEK:
				return new double[] {wkavg.temp_max, wkavg.temp_min, wkavg.temp_avg};
			case SW_MONTH:
				return new double[] {moavg.temp_max, moavg.temp_min, moavg.temp_avg};
			case SW_YEAR:
				return new double[] {yravg.temp_max, yravg.temp_min, yravg.temp_avg};
			default:
				return null;
			}
		}
		public double[] getPrecipRow(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.ppt, dysum.rain, dysum.snow, dysum.snowmelt, dysum.snowloss};
			case SW_WEEK:
				return new double[] {wkavg.ppt, wkavg.rain, wkavg.snow, wkavg.snowmelt, wkavg.snowloss};
			case SW_MONTH:
				return new double[] {moavg.ppt, moavg.rain, moavg.snow, moavg.snowmelt, moavg.snowloss};
			case SW_YEAR:
				return new double[] {yravg.ppt, yravg.rain, yravg.snow, yravg.snowmelt, yravg.snowloss};
			default:
				return null;
			}
		}
		public double[] getRunoffRow(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.surfaceRunoff + dysum.snowRunoff, dysum.surfaceRunoff, dysum.snowRunoff};
			case SW_WEEK:
				return new double[] {wkavg.surfaceRunoff + wkavg.snowRunoff, wkavg.surfaceRunoff, wkavg.snowRunoff};
			case SW_MONTH:
				return new double[] {moavg.surfaceRunoff + moavg.snowRunoff, moavg.surfaceRunoff, moavg.snowRunoff};
			case SW_YEAR:
				return new double[] {yravg.surfaceRunoff + yravg.snowRunoff, yravg.surfaceRunoff, yravg.snowRunoff};
			default:
				return null;
			}
		}
		public double[] getSoilinf(SW_OUTPUT.OutPeriod pd) {
			switch (pd) {
			case SW_DAY:
				return new double[] {dysum.soil_inf};
			case SW_WEEK:
				return new double[] {wkavg.soil_inf};
			case SW_MONTH:
				return new double[] {moavg.soil_inf};
			case SW_YEAR:
				return new double[] {yravg.soil_inf};
			default:
				return null;
			}
		}
	}
	
	private int tail;
	private SW_MODEL SW_Model;
	private SW_MARKOV SW_Markov;
	private SW_SOILWATER SW_SoilWater;
	private SW_WEATHER_HISTORY hist;
	private WEATHER weather;
	private boolean firsttime;
	private boolean weth_found; /* TRUE=success reading this years weather file */
	private boolean data;
	private int nFileItemsRead;
	private final int nFileItems=18;
	
	protected SW_WEATHER(SW_MODEL SW_Model, SW_MARKOV SW_Markov) {
		this.hist = new SW_WEATHER_HISTORY();
		this.SW_Markov = SW_Markov;//new SW_MARKOV();
		this.weather = new WEATHER();
		this.nFileItemsRead=0;
		this.data = false;
		this.weth_found = false;
		this.firsttime = true;
		this.SW_Model = SW_Model;
		tail=0;
		SW_Markov.setPpt_events(0);
	}
	
	protected void setSoilWater(SW_SOILWATER SW_SoilWater) {
		this.SW_SoilWater = SW_SoilWater;
	}
	
	protected void onClear() {
		this.data = false;
		this.weth_found = false;
		hist.onClear();
		//weather.use_markov = false;
		//weather.use_snow = false;
		//weather.pct_snowdrift = 0;
		//weather.pct_snowRunoff = 0;
		//weather.days_in_runavg = 0;
		this.nFileItemsRead=0;
		tail=0;
		SW_Markov.setPpt_events(0);
		this.firsttime = true;
	}
	
	/*protected void onSetDefault(Path ProjectDirectory) {
		this.data = true;
		this.firsttime = true;
		this.weth_found = false;
		weather.use_markov = false;
		weather.use_snow = true;
		weather.pct_snowdrift = 0;
		weather.pct_snowRunoff = 0;
		weather.scale_precip = new double[] {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
		weather.scale_temp_max = new double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		weather.scale_temp_min = new double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		weather.days_in_runavg = 5;
		weather.yr = new SW_TIMES();
		weather.yr.setFirst(1982);
		weather.yr.setLast(1990);//model endyear
		this.nFileItemsRead=nFileItems;
		tail=0;
		SW_Markov.setPpt_events(0);
	}*/

	protected boolean onVerify() throws Exception {
		LogFileIn f = LogFileIn.getInstance();
		if(this.data) {
			weather.runavg_list = new double[weather.days_in_runavg];
			
			List<String> messages = new ArrayList<String>();
			
			if(SW_Model.get_HasData())
				weather.yr.setLast(SW_Model.getEndYear());
			if(this.nFileItemsRead != nFileItems)
				messages.add("WeatherIn onVerify : Too few input lines.");
			if(!weather.yr.totalSet()) {
				if(!weather.yr.firstSet())
					messages.add("WeatherIn onVerify : Year First Not Set.");
				if(!weather.yr.lastSet())
					messages.add("WeatherIn onVerify : Year Last Not Set.");
			}
			if(!weather.use_markov && (SW_Model.getStartYear() < weather.yr.getFirst()))
				messages.add("WeatherIn onVerify : Model Year ("+String.valueOf(SW_Model.getStartYear())+") starts before Weather Files ("+String.valueOf(weather.yr.getFirst())+")"+
						" and use_Markov=FALSE.\nPlease synchronize the years or setup the Markov Weather Files.");
			if(messages.size() > 0) {
				String message = "";
				for (String s : messages)
					message += s + "\n";
				f.LogError(LogFileIn.LogMode.FATAL, message);
			}
			return true;
		} else {
			f.LogError(LogMode.NOTE, "WeatherIn onVerify : No Data.");
			return false;
		}
	}
	protected void onReadHistory(Path WeatherHistoryFolder, String prefix) throws Exception {
		hist.onRead(WeatherHistoryFolder, prefix, weather.yr.getFirst(), SW_Model.getEndYear(), weather.use_markov);
	}
	protected void onSetInput(WEATHER_INPUT_DATA weatherSetupIn) {
		weather.use_snow = weatherSetupIn.use_snow;
		weather.pct_snowdrift = weatherSetupIn.pct_snowdrift;
		weather.pct_snowRunoff = weatherSetupIn.pct_snowRunoff;
		weather.use_markov = weatherSetupIn.use_markov;
		weather.yr.setFirst(weatherSetupIn.yr.getFirst());
		weather.days_in_runavg = weatherSetupIn.days_in_runavg;
		for(int i=0; i<Times.MAX_MONTHS; i++) {
			weather.scale_precip[i] = weatherSetupIn.scale_precip[i];
			weather.scale_temp_max[i] = weatherSetupIn.scale_temp_max[i];
			weather.scale_temp_min[i] = weatherSetupIn.scale_temp_min[i];
			weather.scale_skyCover[i] = weatherSetupIn.scale_skyCover[i];
			weather.scale_wind[i] = weatherSetupIn.scale_wind[i];
			weather.scale_rH[i] = weatherSetupIn.scale_rH[i];
			weather.scale_transmissivity[i] = weatherSetupIn.scale_transmissivity[i];
		}
		this.nFileItemsRead = nFileItems;
		this.data = true;
	}
	protected void onGetInput(WEATHER_INPUT_DATA weatherSetupIn) {
		weatherSetupIn.use_snow = weather.use_snow;
		weatherSetupIn.pct_snowdrift = weather.pct_snowdrift;
		weatherSetupIn.pct_snowRunoff = weather.pct_snowRunoff;
		weatherSetupIn.use_markov = weather.use_markov;
		weatherSetupIn.yr.setFirst(weather.yr.getFirst());
		weatherSetupIn.days_in_runavg = weather.days_in_runavg;
		for(int i=0; i<Times.MAX_MONTHS; i++) {
			weatherSetupIn.scale_precip[i] = weather.scale_precip[i];
			weatherSetupIn.scale_temp_max[i] = weather.scale_temp_max[i];
			weatherSetupIn.scale_temp_min[i] = weather.scale_temp_min[i];
			weatherSetupIn.scale_skyCover[i] = weather.scale_skyCover[i];
			weatherSetupIn.scale_wind[i] = weather.scale_wind[i];
			weatherSetupIn.scale_rH[i] = weather.scale_rH[i];
			weatherSetupIn.scale_transmissivity[i] = weather.scale_transmissivity[i];
		}
	}
	protected void onSetWeatherHist(SW_WEATHER_HISTORY history) {
		List<Integer> years = history.getHistYearsInteger();
		this.hist.removeAll();
		for (Integer year : years) {
			this.hist.add_year(year, history.get_ppt_array(year), history.get_temp_max_array(year), history.get_temp_min_array(year));
		}
	}
	protected void onGetWeatherHist(SW_WEATHER_HISTORY history) {
		List<Integer> years = this.hist.getHistYearsInteger();
		history.removeAll();
		for (Integer year : years) {
			history.add_year(year, this.hist.get_ppt_array(year), this.hist.get_temp_max_array(year), this.hist.get_temp_min_array(year));
		}
	}
	protected void onRead(Path WeatherSetupIn) throws Exception {
		this.nFileItemsRead=0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(WeatherSetupIn, StandardCharsets.UTF_8);
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				switch (this.nFileItemsRead) {
				case 0:
					try {
						weather.use_snow = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Use Snow : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 1:
					try {
						weather.pct_snowdrift = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : pct snow drift : Could not convert string to double." + e.getMessage());
					}
					break;
				case 2:
					try {
						weather.pct_snowRunoff = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : pct snow Runoff : Could not convert string to double." + e.getMessage());
					}
					break;
				case 3:
					try {
						weather.use_markov = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Use Markov : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 4:
					try {
						weather.yr.setFirst(Integer.parseInt(values[0]));
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Year First : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 5:
					try{
						weather.days_in_runavg = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Days in Runavg : Could not convert string to integer." + e.getMessage());
					}
					break;
				default:
					if(this.nFileItemsRead == 6+Times.MAX_MONTHS)
						break;
					try {
						int month = Integer.parseInt(values[0])-1;
						weather.scale_precip[month] = Double.parseDouble(values[1]);
						weather.scale_temp_max[month] = Double.parseDouble(values[2]);
						weather.scale_temp_min[month] = Double.parseDouble(values[3]);
						weather.scale_skyCover[month] = Double.parseDouble(values[4]);
						weather.scale_wind[month] = Double.parseDouble(values[5]);
						weather.scale_rH[month] = Double.parseDouble(values[6]);
						weather.scale_transmissivity[month] = Double.parseDouble(values[7]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Monthly scaling parameters : Could not convert line:"+String.valueOf(this.nFileItemsRead)+". " + e.getMessage());
					}
					break;
				}
				this.nFileItemsRead++;
			}
		}
		this.data = true;
	}

	protected void onWrite(Path WeatherSetupIn) throws Exception {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Weather setup parameters");
			lines.add("# Location: Chimney Park, WY (41.068° N, 106.1195° W, 2740 m elevation)");
			lines.add("#");
			lines.add(String.valueOf(weather.use_snow?1:0)+"\t# 1=allow snow accumulation,   0=no snow effects.");
			lines.add(String.valueOf(weather.pct_snowdrift)+"\t# % of snow drift per snow event (+ indicates snow addition, - indicates snow taken away from site)");
			lines.add(String.valueOf(weather.pct_snowRunoff)+"\t# % of snowmelt water as runoff/on per event (>0 indicates runoff, <0 indicates runon)");
			lines.add(String.valueOf(weather.use_markov?1:0)+"\t# 0=use historical data only, 1=use markov process for missing weather.");
			lines.add(String.valueOf(weather.yr.getFirst())+"\t# first year to begin historical weather.");
			lines.add(String.valueOf(weather.days_in_runavg)+"\t # number of days to use in the running average of temperature.");
			lines.add("");
			lines.add("# Monthly scaling parameters.");
			lines.add("# Month 1 = January, Month 2 = February, etc.");
			lines.add("# PPT = multiplicative for PPT (scale*ppt).");
			lines.add("# MaxT = additive for max temp (scale+maxtemp).");
			lines.add("# MinT = additive for min temp (scale+mintemp).");
			lines.add("# SkyCover = additive for mean monthly sky cover [%]; min(100, max(0, scale + sky cover))");
			lines.add("# Wind = multiplicative for mean monthly wind speed; max(0, scale * wind speed)");
			lines.add("# rH = additive for mean monthly relative humidity [%]; min(100, max(0, scale + rel. Humidity))");
			lines.add("# Transmissivity = multiplicative for mean monthly relative transmissivity; min(1, max(0, scale * transmissivity))");
			lines.add(String.format("#%3s\t%5s\t%5s\t%5s\t%8s\t%5s\t%5s\t%14s", "Mon","PPT","MaxT","MinT","SkyCover","Wind","rH","Transmissivity"));
			for(int i=0; i<Times.MAX_MONTHS; i++)
				lines.add(weather.get_MonthlyScaling_toString(i));
			Files.write(WeatherSetupIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.WARN, "WeatherIn onWriteWeatherIn : No data from files or default.");
		}
	}
	
	protected void onWriteHistory(Path WeatherHistoryFolder, String prefix) throws Exception {
		this.hist.onWrite(WeatherHistoryFolder, prefix);
	}

	protected WEATHER getWeather() {
		return this.weather;
	}
	protected void SW_WTH_clear_runavg_list() {
		weather.runavg_list = null;
	}
	protected void SW_WTH_init() {
		/* =================================================== */
		/* nothing to initialize */
		/* this is a stub to make all objects more consistent */
	}
	protected void SW_WTH_new_year() throws Exception {
		SW_WEATHER_2DAYS wn = weather.now;
		int year = SW_Model.getYear();
		int Today = Defines.Today;
		
		_clear_runavg();
		weather.yrsum.onClear();
		
		if(year < weather.yr.getFirst()) {
			weth_found=false;
		} else {
			_clear_hist_weather();
			weth_found = hist.setCurrentYear(year);
		}
		
		if(!weth_found && !weather.use_markov) {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, String.format("Markov Simulator turned off and weather file found not for year %d",year));
		}
		/* setup today's weather because it's used as a default
		 * value when weather for the first day is missing.
		 * Notice that temps of 0. are reasonable for January
		 * (doy=1) and are below the critical temps for freezing
		 * and with ppt=0 there's nothing to freeze.
		 */
		if (!weth_found && firsttime) {
			wn.temp_max[Today] = wn.temp_min[Today] = wn.ppt[Today] = wn.rain[Today] = wn.snow[Today] = wn.snowmelt[Today] = wn.snowloss[Today] = wn.gsppt = 0.;

			weather.snowRunoff = weather.surfaceRunoff = weather.soil_inf = 0.;
		}

		firsttime = false;
	}
	protected void SW_WTH_end_day() {
		_update_yesterday();
	}
	protected void SW_WTH_new_day() throws Exception {
		/* =================================================== */
		/* guarantees that today's weather will not be invalid
		 * via _todays_weth()
		 *
		 *  20-Jul-2002 -- added growing season computation to
		 *                 facilitate the steppe/soilwat interface.
		 *  06-Dec-2002 -- modified the seasonal computation to
		 *                 account for n-s hemispheres.
		 *	16-Sep-2009 -- (drs) scaling factors were only applied to Tmin and Tmax
		 *					but not to Taverage -> corrected
		 *	09-Oct-2009	-- (drs) commented out snow adjustement, because call moved to SW_Flow.c
		 * 	20091015 (drs) ppt is divided into rain and snow
		 */
		int Today=Defines.Today;
		int Yesterday = Defines.Yesterday;
		
		SW_WEATHER_2DAYS wn = weather.now;
		double tmpmax=0, tmpmin=0, ppt=0;
		int month = SW_Model.getMonth();

		/* get the plain unscaled values */
		int doy = SW_Model.getDOY()-1;
		if(!weth_found) {
			ppt = wn.ppt[Yesterday]; /* reqd for markov */
			SW_Markov.set_MaxMinRain(tmpmax, tmpmin, ppt);
			SW_Markov.SW_MKV_today(doy);
			ppt = SW_Markov.get_MaxMinRain().rain;
			tmpmax = SW_Markov.get_MaxMinRain().tmax;
			tmpmin = SW_Markov.get_MaxMinRain().tmin;
		} else {
			tmpmax = (Double.compare(hist.get_temp_max(doy), WTH_MISSING)!=0) ? hist.get_temp_max(doy) : wn.temp_max[Yesterday];
			tmpmin = (Double.compare(hist.get_temp_min(doy), WTH_MISSING)!=0) ? hist.get_temp_min(doy) : wn.temp_min[Yesterday];
			ppt =  (Double.compare(hist.get_ppt(doy), WTH_MISSING)!=0) ? hist.get_ppt(doy) : 0.;
		}

		/* scale the weather according to monthly factors */
		wn.temp_max[Today] = tmpmax + weather.scale_temp_max[month];
		wn.temp_min[Today] = tmpmin + weather.scale_temp_min[month];
		wn.ppt_actual[Today] = ppt;

		wn.temp_avg[Today] = (wn.temp_max[Today] + wn.temp_min[Today]) / 2.;
		wn.temp_run_avg[Today] = _runavg_temp(wn.temp_avg[Today]);

		ppt *= weather.scale_precip[month];

		wn.ppt[Today] = wn.rain[Today] = ppt;
		wn.snowmelt[Today] = wn.snowloss[Today] = wn.snow[Today] = 0.;
		weather.snowRunoff = weather.surfaceRunoff = weather.soil_inf = 0.;

		if (weather.use_snow) {
			SW_SoilWater.SW_SWC_adjust_snow(wn);
		}
	}
	
	protected void _clear_runavg() {
		for(int i=0; i<weather.days_in_runavg; i++) {
			weather.runavg_list[i] = 0;
		}
	}
	protected double _runavg_temp(double avg) {
		int cnt=0, numdays;
		double sum = 0.;
		
		weather.runavg_list[tail] = avg;
		numdays = (SW_Model.getDOY() < weather.days_in_runavg) ? SW_Model.getDOY() : weather.days_in_runavg;
		
		for(int i=0; i<numdays; i++) {
			if(Double.compare(weather.runavg_list[i], WTH_MISSING) != 0)
				sum+=weather.runavg_list[i];
			cnt++;
		}
		tail = (tail<(weather.days_in_runavg - 1)) ? tail+1 : 0;
		return ((cnt>0) ? sum/cnt : WTH_MISSING);
	}
	protected void _update_yesterday() {
		int Today=Defines.Today;
		int Yesterday = Defines.Yesterday;
		SW_WEATHER_2DAYS wn = weather.now;
		
		wn.temp_max[Yesterday] = wn.temp_max[Today];
		wn.temp_min[Yesterday] = wn.temp_min[Today];
		wn.temp_avg[Yesterday] = wn.temp_avg[Today];
		wn.temp_run_avg[Yesterday] = wn.temp_run_avg[Today];

		wn.ppt_actual[Yesterday] = wn.ppt_actual[Today];
		wn.ppt[Yesterday] = wn.ppt[Today];
		wn.snow[Yesterday] = wn.snow[Today];
		wn.rain[Yesterday] = wn.rain[Today];
		wn.snowmelt[Yesterday] = wn.snowmelt[Today];
		wn.snowloss[Yesterday] = wn.snowloss[Today];
	}
	
	protected void _clear_hist_weather() {
		weather.dysum.onClear();
		weather.wksum.onClear();
		weather.mosum.onClear();
		weather.yrsum.onClear();
		weather.wkavg.onClear();
		weather.moavg.onClear();
		weather.yravg.onClear();
	}

	protected SW_WEATHER_2DAYS getNow() {
		return weather.now;
	}
	
	protected SW_MARKOV getMarkov() {
		return this.SW_Markov;
	}
}
