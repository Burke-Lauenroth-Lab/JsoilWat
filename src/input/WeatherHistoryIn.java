package input;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import times.Times;

public class WeatherHistoryIn {
	
	private int nCurrentYear;
	private List<SW_WEATHER_HIST> weatherHist;
	private Map<Integer, Integer> yearToIndex;
	private boolean data;

	private class SW_WEATHER_HIST implements Comparable<SW_WEATHER_HIST> {
		private int nYear;
		private double[] temp_max;
		private double[] temp_min;
		private double[] temp_avg;
		private double[] ppt;
		private double[] temp_month_avg;
		private double temp_year_avg;
		private int nDaysInYear;
		private boolean data;
		
		public SW_WEATHER_HIST() {
			this.temp_max = new double[Times.MAX_DAYS];
			this.temp_min = new double[Times.MAX_DAYS];
			this.temp_avg = new double[Times.MAX_DAYS];
			this.ppt = new double[Times.MAX_DAYS];
			this.temp_month_avg = new double[Times.MAX_MONTHS];
			this.temp_year_avg = 0;	
			this.nYear = 0;
			this.nDaysInYear = 0;
			this.data = false;
		}
		
		public void onRead(Path WeatherHistoryFile, int year) throws IOException {
			LogFileIn f = LogFileIn.getInstance();
			this.nYear = year;
			List<String> lines = Files.readAllLines(WeatherHistoryFile, StandardCharsets.UTF_8);
			double acc=0;
			int k=0,x=0;
			int doy=0;
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("[ \t]+");
					if(values.length != 4)
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherHistoryFile : Items != 4.");
					try {
						doy = Integer.parseInt(values[0])-1;
						if(doy < 0 || doy > (Times.MAX_DAYS-1))
							f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherHistoryFile : Day of Year out of range doy "+String.valueOf(doy));
						this.temp_max[doy] = Double.parseDouble(values[1]);
						this.temp_min[doy] = Double.parseDouble(values[2]);
						this.temp_avg[doy] = (this.temp_max[doy]+this.temp_min[doy])/2.0;
						this.ppt[doy] = Double.parseDouble(values[3]);
						this.nDaysInYear++;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherHistoryFile : Convert Error :" +e.getMessage());
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
		
		public void onWrite(Path WeatherHistoryFolder, String prefix) throws IOException {
			if(this.data) {
				List<String> lines = new ArrayList<String>();
				lines.add("# weather for year = "+this.getYear());
				lines.add("# DOY Tmax(C) Tmin(C) PPT(cm)");
				for(int i=0; i<this.nDaysInYear; i++)
					lines.add(String.valueOf(i+1)+"\t"+String.valueOf(this.temp_max[i])+"\t"+String.valueOf(this.temp_min[i])+"\t"+String.valueOf(this.ppt[i]));
				Files.write(WeatherHistoryFolder.resolve(prefix+"."+this.toString()), lines, StandardCharsets.UTF_8);
			} else {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogFileIn.LogMode.WARN, "WeatherIn onWriteWeatherHistory : No data from files or default.");
			}
		}
		
		public void onClear() {
			/*for(int i=0; i<Times.MAX_DAYS; i++) {
				this.temp_max[i]=0;
				this.temp_min[i]=0;
				this.temp_avg[i]=0;
				this.ppt[i] = 0;
			}
			for(int i=0;i<Times.MAX_MONTHS;i++)
				this.temp_month_avg[i]=0;
			*/
			this.temp_year_avg=0;
			this.nDaysInYear=0;
			this.nYear = 0;
			this.data = false;
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof SW_WEATHER_HIST))
				return false;
			SW_WEATHER_HIST n = (SW_WEATHER_HIST) o;
			return n.toString().equals(this.toString());
		}
		public String toString() {
			return String.valueOf(this.nYear);
		}
		public int getYear() {
			return this.nYear;
		}
		//public void setYear(int year) {
		//	onClear();
		//	this.nYear = year;
		//}
		public double getYearAvg() {
			return this.temp_year_avg;
		}
		public int compareTo(SW_WEATHER_HIST n) {
			int lastCmp = Integer.compare(this.nYear, n.getYear());
			return lastCmp;
		}
	}

	public WeatherHistoryIn() {
		this.weatherHist = new ArrayList<SW_WEATHER_HIST>();
		yearToIndex = new HashMap<Integer,Integer>();
		this.data = false;
	}
	
	public void onRead(Path WeatherHistoryFile) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		int year = 0;
		try {
			year = Integer.parseInt(WeatherHistoryFile.getFileName().toString().split("\\.")[1]);
		} catch(NumberFormatException n) {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistoryIn onRead : Convert Year From Path Failed :" +n.getMessage());
		}
		if(yearToIndex.containsKey(year)) {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistoryIn onRead : Contains Data for Year :" +String.valueOf(year));
		} else {
			if(Files.exists(WeatherHistoryFile)) {
				if(this.yearToIndex.size() < this.weatherHist.size()) {//reuse an object
					for(int i=0; i<this.weatherHist.size(); i++) {
						if(!this.yearToIndex.containsValue(i)) {//This object is not used
							this.weatherHist.get(i).onRead(WeatherHistoryFile, year);
							this.yearToIndex.put(year, i);
							break;
						}
					}
				} else {
					SW_WEATHER_HIST weathHist = new SW_WEATHER_HIST();
					weathHist.onRead(WeatherHistoryFile, year);
					this.weatherHist.add(weathHist);
					this.yearToIndex.put(year, this.weatherHist.indexOf(weathHist));
				}
				this.data=true;
			} else {
				f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistIn onRead : Path '"+WeatherHistoryFile.toString()+"' doesn't exists.");
			}
		}
	}
	
	public void onRead(Path WeatherHistoryFolder, String prefix, int startYear, int endYear) throws IOException {
		for(int i=startYear; i<=endYear; i++) {
			this.onRead(WeatherHistoryFolder.resolve(prefix+"."+String.valueOf(i)));
		}
	}
	
	public void onWrite(Path WeatherHistoryFolder, String prefix, int year) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		if(yearToIndex.containsKey(year)) {
			int i = this.yearToIndex.get(year);
			if(this.weatherHist.get(i).getYear() == year) {
				this.weatherHist.get(i).onWrite(WeatherHistoryFolder, prefix);
			} else {
				f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistIn onWrite : Year and Year of Data do not match.");
			}
		} else {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistIn onWrite : Year and Year of Data do not match.");
		}
	}
	public void onWrite(Path WeatherHistoryFolder, String prefix) throws IOException {
		if(this.data) {
			Iterator<Entry<Integer, Integer>> it = this.yearToIndex.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
				this.onWrite(WeatherHistoryFolder, prefix, pair.getKey());
			}
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onWriteWeatherHistories : No Historical Data.");
		}
	}
	
	public void onClear() {
		this.yearToIndex.clear();
		if(this.data) {
			for(int i=0; i<this.weatherHist.size(); i++) {
				this.weatherHist.get(i).onClear();
			}
		}
		this.data = false;
	}
	
	public double getYearAvg(int year) {
		if(this.data)
			return this.weatherHist.get(this.yearToIndex.get(year)).getYearAvg();
		else
			return 0;
	}

	public int getCurrentYear() {
		return nCurrentYear;
	}

	public void setCurrentYear(int nCurrentYear) {
		this.nCurrentYear = nCurrentYear;
	}
	
}
