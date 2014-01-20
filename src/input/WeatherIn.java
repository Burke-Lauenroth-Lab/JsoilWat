package input;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import times.SW_TIMES;
import times.Times;

public class WeatherIn {
	private String name_prefix;
	private Path weatherFolder;
	private boolean use_markov;
	private boolean use_snow;
	private double pct_snowdrift;
	private double pct_snowRunoff;
	private double[] scale_precip;
	private double[] scale_temp_max;
	private double[] scale_temp_min;
	private int days_in_runavg;
	private SW_TIMES yr;
	private List<SW_WEATHER_HIST> weatherHist;
	private boolean verified;
	private boolean read;
	private int nFileItemsRead;
	private final int nFileItems=18;
	
	private class SW_WEATHER_HIST implements Comparable<SW_WEATHER_HIST> {
		private String sYear;
		private String sPrefix;
		private Path WeatherFolder;
		private int nYear;
		private double[] temp_max;
		private double[] temp_min;
		private double[] temp_avg;
		private double[] ppt;
		private double[] temp_month_avg;
		private double temp_year_avg;
		private int nDaysInYear;
		private boolean data;
		
		public SW_WEATHER_HIST(Path weatherFolder, String Prefix, int year) {
			this.temp_max = new double[Times.MAX_DAYS];
			this.temp_min = new double[Times.MAX_DAYS];
			this.temp_avg = new double[Times.MAX_DAYS];
			this.ppt = new double[Times.MAX_DAYS];
			this.temp_month_avg = new double[Times.MAX_MONTHS];
			this.temp_year_avg = 0;
			this.sYear = String.valueOf(year);
			this.nYear = year;
			this.nDaysInYear = 0;
			this.WeatherFolder = weatherFolder;
			this.sPrefix = Prefix;
			this.data = false;
		}
		
		public void onReadWeatherHistoryFile() throws IOException {
			LogFileIn f = LogFileIn.getInstance();
			List<String> lines = Files.readAllLines(getPath(), StandardCharsets.UTF_8);
			double acc=0;
			int k=0,x=0;
			int doy=0;
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					String[] values = line.split("[ \t]+");
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherHistoryFile : Items != 4.");
					try {
						doy = Integer.parseInt(values[0])-1;
						if(doy < 0 || doy > (Times.MAX_DAYS-1))
							f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherHistoryFile : Day of Year out of range doy "+String.valueOf(doy));
						this.temp_max[doy] = Double.parseDouble(values[1]);
						this.temp_min[doy] = Double.parseDouble(values[2]);
						this.temp_avg[doy] = (this.temp_max[doy]+this.temp_min[doy])/2.0;
						this.ppt[doy] = Double.parseDouble(values[3]);
						this.nDaysInYear++;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherHistoryFile : Convert Error :" +e.getMessage());
					}
					acc+=this.temp_avg[doy];
					k++;
				}
			}
			this.temp_year_avg = acc/(k+0.0);
			for(int i=0; i<Times.MAX_MONTHS;i++) {
				k=31;
				if(i==8 || i==3 || i==5 || i==10)
					k=30;// september, april, june, & november all have 30 days...
				else if (i==1) {
					k=28;// february has 28 days, except if it's a leap year, in which case it has 29 days...
					if(Times.isleapyear(this.nYear))
						k=29;
				}
				acc=0;
				for(int j=0; j<k; j++)
					acc+=this.temp_avg[j+x];
				this.temp_month_avg[i] = acc/(k+0.0);
				x+=k;
			}
			this.data = true;
		}
		
		public void onWriteWeatherHistoryFile() throws IOException {
			if(this.data) {
				List<String> lines = new ArrayList<String>();
				lines.add("# weather for site 002_-119.415_39.046 year =  1949");
				lines.add("# DOY Tmax(C) Tmin(C) PPT(cm)");
				for(int i=0; i<this.nDaysInYear; i++)
					lines.add(String.valueOf(i+1)+"\t"+String.valueOf(this.temp_max[i])+"\t"+String.valueOf(this.temp_min[i])+"\t"+String.valueOf(this.ppt[i]));
				Files.write(getPath(), lines, StandardCharsets.UTF_8);
			} else {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogFileIn.LogMode.LOGWARN, "WeatherIn onWriteWeatherHistory : No data from files or default.");
			}
		}
		
		public void onClear() {
			for(int i=0; i<Times.MAX_DAYS; i++) {
				this.temp_max[i]=0;
				this.temp_min[i]=0;
				this.temp_avg[i]=0;
				this.ppt[i] = 0;
			}
			for(int i=0;i<Times.MAX_MONTHS;i++)
				this.temp_month_avg[i]=0;
			
			this.temp_year_avg=0;
			this.nDaysInYear=0;
			this.nYear = 0;
			this.sPrefix = "";
			this.sYear = "";
			this.data = false;
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof SW_WEATHER_HIST))
				return false;
			SW_WEATHER_HIST n = (SW_WEATHER_HIST) o;
			return n.toString().equals(this.toString());
		}
		public String toString() {
			return sYear;
		}
		public int getYear() {
			return this.nYear;
		}
		public void setYear(int year) {
			onClear();
			this.nYear = year;
		}
		public Path getPath() {
			return getWeatherFolder().resolve(getPrefix()+"."+this.toString());
		}
		public String getPrefix() {
			return this.sPrefix;
		}
		public void setPrefix(String prefix) {
			this.sPrefix = prefix;
		}
		public Path getWeatherFolder() {
			return this.WeatherFolder;
		}
		public void setWeatherFolder(Path weatherFolder) {
			this.WeatherFolder = weatherFolder;
		}
		@SuppressWarnings("unused")
		public double getYearAvg() {
			return this.temp_year_avg;
		}
		public int compareTo(SW_WEATHER_HIST n) {
			int lastCmp = Integer.compare(this.nYear, n.getYear());
			return lastCmp;
		}
	}
	
	public WeatherIn(){
		this.name_prefix = "";
		this.weatherFolder=Paths.get("/");
		this.use_markov = false;
		this.use_snow = true;
		this.pct_snowdrift = 0;
		this.pct_snowRunoff = 0;
		this.scale_precip = new double[Times.MAX_MONTHS];
		this.scale_temp_max = new double[Times.MAX_MONTHS];
		this.scale_temp_min = new double[Times.MAX_MONTHS];
		this.days_in_runavg = 5;
		this.yr = new SW_TIMES();
		this.nFileItemsRead=0;
		this.weatherHist = new ArrayList<SW_WEATHER_HIST>();
		this.verified = false;
		this.read = false;
	}
	
	public void onClear() {
		this.read = false;
		this.verified = false;
		this.name_prefix = "";
		this.weatherFolder=Paths.get("/");
		this.use_markov = false;
		this.use_snow = false;
		this.pct_snowdrift = 0;
		this.pct_snowRunoff = 0;
		this.scale_precip = new double[Times.MAX_MONTHS];
		this.scale_temp_max = new double[Times.MAX_MONTHS];
		this.scale_temp_min = new double[Times.MAX_MONTHS];
		this.days_in_runavg = 0;
		this.yr = new SW_TIMES();
		this.nFileItemsRead=0;
		this.weatherHist = new ArrayList<SW_WEATHER_HIST>();
	}
	
	public void onSetDefault(Path ProjectDirectory) {
		this.verified = false;
		this.read = true;
		this.name_prefix = "weath";
		this.weatherFolder=ProjectDirectory.resolve("Input/data_39.0625_-119.4375/");
		this.use_markov = false;
		this.use_snow = true;
		this.pct_snowdrift = 0;
		this.pct_snowRunoff = 0;
		this.scale_precip = new double[] {1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
		this.scale_temp_max = new double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		this.scale_temp_min = new double[] {0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		this.days_in_runavg = 5;
		this.yr = new SW_TIMES();
		this.yr.setFirst(1982);
		this.yr.setLast(1990);//model endyear
		this.nFileItemsRead=nFileItems;
		this.weatherHist = new ArrayList<SW_WEATHER_HIST>();
		
	}
	
	public String getWeatherPrefix() {
		return name_prefix;
	}

	public void setWeatherPrefix(String name_prefix) {
		this.name_prefix = name_prefix;
	}
	
	public Path getWeatherFolder() {
		return weatherFolder;
	}

	public void setWeatherFolder(Path weatherFolder) {
		this.weatherFolder = weatherFolder;
	}

	public boolean getUseMarkov() {
		return this.use_markov;
	}
	
	public void onReadWeatherHistory(int year) {
		this.weatherHist.add(new SW_WEATHER_HIST(this.weatherFolder,this.name_prefix, year));
		Collections.sort(this.weatherHist);
	}
	
	public void onReadWeatherHistories() throws IOException {
		int j=0;
		for(int i=this.yr.getFirst(); i<=this.yr.getLast(); i++, j++) {
			this.weatherHist.add(new SW_WEATHER_HIST(this.weatherFolder, this.name_prefix, i));
			this.weatherHist.get(j).onReadWeatherHistoryFile();
		}
	}
	
	public void onWriteWeatherHistory(int year) throws IOException {
		if(year >= this.yr.getFirst() && year <= this.getLastYear()) {
			int i = this.yr.getTotal()-(this.yr.getLast()-year)-1;
			if(this.weatherHist.get(i).getYear() == year) {
				this.weatherHist.get(i).setWeatherFolder(this.weatherFolder);
				this.weatherHist.get(i).setPrefix(this.name_prefix);
				this.weatherHist.get(i).onWriteWeatherHistoryFile();
			} else {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onWriteWeatherHistory : Year and Year of Data do not match.");
			}
		}
	}
	public void onWriteWeatherHistories() throws IOException {
		if(!this.weatherHist.isEmpty()) {
			if(!(this.weatherHist.size() < this.yr.getTotal())) {
				for(int year=this.yr.getFirst(); year<=this.yr.getLast(); year++) {
					this.onWriteWeatherHistory(year);
				}
			} else {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onWriteWeatherHistories : Historical Data does not contain all years.");
			}
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onWriteWeatherHistories : No Historical Data.");
		}
	}
	
	public void onSortWeatherHistory() {
		Collections.sort(this.weatherHist);
	}
	
	public int getFirstYear() {
		return this.yr.getFirst();
	}
	public void setFirstYear(int first) {
		this.yr.setFirst(first);
	}
	public int getLastYear() {
		return this.yr.getLast();
	}
	public void setLastYear(int ModelEndYear) {
		this.yr.setLast(ModelEndYear);
	}
	
	public boolean onVerify(int ModelStartYear) {
		LogFileIn f = LogFileIn.getInstance();
		if(this.read) {
			if(Files.notExists(this.weatherFolder))
				f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onVerify : Check WeatherFolder : No Path");
			if(this.name_prefix.matches("")) 
				f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onVerify : Check WeatherPrefix : No Prefix");
			if(this.nFileItemsRead != nFileItems)
				f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onVerify : Too few input lines.");
			if(!this.yr.totalSet()) {
				if(!this.yr.firstSet())
					f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onVerify : Year First Not Set.");
				if(!this.yr.lastSet())
					f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onVerify : Year Last Not Set.");
			}
			if(!this.use_markov && (ModelStartYear < this.yr.getFirst()))
				f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onVerify : Model Year ("+String.valueOf(ModelStartYear)+") starts before Weather Files ("+String.valueOf(this.yr.getFirst())+")"+
						" and use_Markov=FALSE.\nPlease synchronize the years or setup the Markov Weather Files.");
			this.verified = true;
			return this.verified;
		} else {
			f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onVerify : No Data.");
			this.verified = false;
			return false;
		}
	}

	public void onReadWeatherIn(Path WeatherSetupIn) throws IOException {
		this.nFileItemsRead=0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(WeatherSetupIn, StandardCharsets.UTF_8);
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				String[] values = line.split("[ \t]+");
				switch (this.nFileItemsRead) {
				case 0:
					try {
						this.use_snow = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherIn : Use Snow : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 1:
					try {
						this.pct_snowdrift = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherIn : pct snow drift : Could not convert string to double." + e.getMessage());
					}
					break;
				case 2:
					try {
						this.pct_snowRunoff = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherIn : pct snow Runoff : Could not convert string to double." + e.getMessage());
					}
					break;
				case 3:
					try {
						this.use_markov = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherIn : Use Markov : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 4:
					try {
						this.yr.setFirst(Integer.parseInt(values[0]));
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherIn : Year First : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 5:
					try{
						this.days_in_runavg = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherIn : Days in Runavg : Could not convert string to integer." + e.getMessage());
					}
					break;
				default:
					if(this.nFileItemsRead == 6+Times.MAX_MONTHS)
						break;
					try {
						int month = Integer.parseInt(values[0])-1;
						this.scale_precip[month] = Double.parseDouble(values[1]);
						this.scale_temp_max[month] = Double.parseDouble(values[2]);
						this.scale_temp_min[month] = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.LOGERROR, "WeatherIn onReadWeatherIn : Monthly scaling parameters : Could not convert line:"+String.valueOf(this.nFileItemsRead)+". " + e.getMessage());
					}
					break;
				}
				this.nFileItemsRead++;
			}
		}
		this.read = true;
	}

	public void onWriteWeatherIn(Path WeatherSetupIn) throws IOException {
		if(this.read) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Weather setup parameters");
			lines.add("# Location: Chimney Park, WY (41.068° N, 106.1195° W, 2740 m elevation)");
			lines.add("#");
			lines.add(String.valueOf(this.use_snow?1:0)+"\t# 1=allow snow accumulation,   0=no snow effects.");
			lines.add(String.valueOf(this.pct_snowdrift)+"\t# % of snow drift per snow event (+ indicates snow addition, - indicates snow taken away from site)");
			lines.add(String.valueOf(this.pct_snowRunoff)+"\t# % of snowmelt water as runoff/on per event (>0 indicates runoff, <0 indicates runon)");
			lines.add(String.valueOf(this.use_markov?1:0)+"\t# 0=use historical data only, 1=use markov process for missing weather.");
			lines.add(String.valueOf(this.yr.getFirst())+"\t# first year to begin historical weather.");
			lines.add(String.valueOf(this.days_in_runavg)+"\t # number of days to use in the running average of temperature.");
			lines.add("");
			lines.add("# Monthly scaling parameters.");
			lines.add("# Month 1 = January, Month 2 = February, etc.");
			lines.add("# PPT = multiplicative for PPT (scale*ppt).");
			lines.add("# MaxT = additive for max temp (scale+maxtemp).");
			lines.add("# MinT = additive for min temp (scale+mintemp).");
			lines.add("#Mon  PPT  MaxT  MinT");
			for(int i=0; i<Times.MAX_MONTHS; i++)
				lines.add(String.valueOf(i+1)+"\t"+String.valueOf(this.scale_precip[i])+"\t"+String.valueOf(this.scale_temp_max[i])+"\t"+String.valueOf(this.scale_temp_min[i]));
			Files.write(WeatherSetupIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.LOGWARN, "WeatherIn onWriteWeatherIn : No data from files or default.");
		}
	}
}
