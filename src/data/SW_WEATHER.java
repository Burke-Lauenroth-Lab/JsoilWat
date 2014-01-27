package data;

import input.LogFileIn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import times.Times;
import defines.Defines;
import defines.SW_TIMES;

public class SW_WEATHER {
	/* missing values may be different than with other things */
	public static final double WTH_MISSING=999.;

	/*  all temps are in degrees C, all precip is in cm */
	/*  in fact, all water variables are in cm throughout
	 *  the model.  this facilitates additions and removals
	 *  as they're always in the right units.
	 */

	public class SW_WEATHER_2DAYS {
		/* comes from markov weather day-to-day */
		public double temp_yr_avg,gsppt; /* gr. season ppt only needs one day */
		public double[] temp_avg, temp_run_avg, /* year's avg for STEPPE */
		temp_max, temp_min, ppt, /* 20091015 (drs) ppt is divided into rain and snow */
		rain, snow, snowmelt, snowloss, ppt_actual; /* 20091015 (drs) was here previously, but not used in code as of today */
		
		
		public SW_WEATHER_2DAYS() {
			temp_avg = new double[Defines.TWO_DAYS];
			temp_run_avg = new double[Defines.TWO_DAYS];
			temp_max = new double[Defines.TWO_DAYS];
			temp_min = new double[Defines.TWO_DAYS];
			ppt = new double[Defines.TWO_DAYS];
			rain = new double[Defines.TWO_DAYS];
			snow = new double[Defines.TWO_DAYS];
			snowmelt = new double[Defines.TWO_DAYS];
			snowloss = new double[Defines.TWO_DAYS];
			gsppt = 0;
			temp_yr_avg=0;
		}
		public void onClear() {
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
			}
			gsppt = 0;
			temp_yr_avg = 0;
		}
	}

	/* accumulators for output values hold only the */
	/* current period's values (eg, weekly or monthly) */
	public class SW_WEATHER_OUTPUTS {
		double temp_max, temp_min, temp_avg, ppt, rain, snow, snowmelt, snowloss, /* 20091015 (drs) ppt is divided into rain and snow */
		snowRunoff, surfaceRunoff, soil_inf, et, aet, pet;
		
		public SW_WEATHER_OUTPUTS() {
			
		}
		
		public void onClear() {
			temp_max=temp_min=temp_avg=ppt=rain=snow=snowmelt=snowloss=0;
			snowRunoff=surfaceRunoff=soil_inf=et=aet=pet=0;
		}
	}

	public class WEATHER {
		boolean use_markov, /* TRUE=use markov for any year missing a weather */
		/*      file, which means markov must be initialized */
		/* FALSE = fail if any weather file is missing.  */
		use_snow;
		double pct_snowdrift, pct_snowRunoff;
		int days_in_runavg;
		SW_TIMES yr;
		double[] scale_precip, scale_temp_max, scale_temp_min;
		String name_prefix;
		double snowRunoff, surfaceRunoff, soil_inf;

		/* This section is required for computing the output quantities.  */
		SW_WEATHER_OUTPUTS dysum, /* helpful placeholder */
		wksum, mosum, yrsum, /* accumulators for *avg */
		wkavg, moavg, yravg; /* averages or sums as appropriate*/
		
		SW_WEATHER_2DAYS now;
		
		public WEATHER() {
			scale_precip = new double[Times.MAX_MONTHS];
			scale_temp_max = new double[Times.MAX_MONTHS];
			scale_temp_min = new double[Times.MAX_MONTHS];
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
	}
	
	private SW_SOILWAT_HISTORY hist;
	private WEATHER weather;
	private boolean verified;
	private boolean read;
	private int nFileItemsRead;
	private final int nFileItems=18;
	
	public SW_WEATHER() {
		this.hist = new SW_SOILWAT_HISTORY();
		this.nFileItemsRead=0;
		this.verified = false;
		this.read = false;
		this.weather = new WEATHER();
	}
	
	public void onClear() {
		this.read = false;
		this.verified = false;
		weather.use_markov = false;
		weather.use_snow = false;
		weather.pct_snowdrift = 0;
		weather.pct_snowRunoff = 0;
		weather.days_in_runavg = 0;
		this.nFileItemsRead=0;
	}
	
	public void onSetDefault(Path ProjectDirectory) {
		this.verified = false;
		this.read = true;
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
	}

	public boolean onVerify(int ModelStartYear) {
		LogFileIn f = LogFileIn.getInstance();
		if(this.read) {
			if(this.nFileItemsRead != nFileItems)
				f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Too few input lines.");
			if(!weather.yr.totalSet()) {
				if(!weather.yr.firstSet())
					f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Year First Not Set.");
				if(!weather.yr.lastSet())
					f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Year Last Not Set.");
			}
			if(!weather.use_markov && (ModelStartYear < weather.yr.getFirst()))
				f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Model Year ("+String.valueOf(ModelStartYear)+") starts before Weather Files ("+String.valueOf(weather.yr.getFirst())+")"+
						" and use_Markov=FALSE.\nPlease synchronize the years or setup the Markov Weather Files.");
			this.verified = true;
			return this.verified;
		} else {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : No Data.");
			this.verified = false;
			return false;
		}
	}

	public void onRead(Path WeatherSetupIn) throws IOException {
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
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Monthly scaling parameters : Could not convert line:"+String.valueOf(this.nFileItemsRead)+". " + e.getMessage());
					}
					break;
				}
				this.nFileItemsRead++;
			}
		}
		this.read = true;
	}

	public void onWrite(Path WeatherSetupIn) throws IOException {
		if(this.read) {
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
			lines.add("#Mon  PPT  MaxT  MinT");
			for(int i=0; i<Times.MAX_MONTHS; i++)
				lines.add(String.valueOf(i+1)+"\t"+String.valueOf(weather.scale_precip[i])+"\t"+String.valueOf(weather.scale_temp_max[i])+"\t"+String.valueOf(weather.scale_temp_min[i]));
			Files.write(WeatherSetupIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.WARN, "WeatherIn onWriteWeatherIn : No data from files or default.");
		}
	}

	public WEATHER getWeather() {
		return this.weather;
	}
}
