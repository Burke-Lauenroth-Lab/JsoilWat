package soilwat;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SW_WEATHER_HISTORY {
	
	private int nCurrentYear;
	private List<SW_WEATHER_HIST> weatherHist;
	private Map<Integer, Integer> yearToIndex;
	private boolean data;
	private LogFileIn log;
	
	public class WeatherException extends Exception {
		private static final long serialVersionUID = 1L;

		public WeatherException(String message) {
			super(message);
		}
	}
	
	public String toString() {
		String out = "Weather History List\n";
		Set<Integer> years = yearToIndex.keySet();
		for(Integer year : years) {
			out += year.toString()+"\n";
		}
		return out;
	}

	private class SW_WEATHER_HIST implements Comparable<SW_WEATHER_HIST> {
		private int nYear;
		private double[] temp_max;
		private double[] temp_min;
		private double[] temp_avg;
		private double[] ppt;
		private double[] temp_month_max_avg;
		private double[] temp_month_min_avg;
		private double[] temp_month_avg;
		private double[] ppt_month_avg;
		private double temp_year_min_avg;
		private double temp_year_max_avg;
		private double temp_year_avg;
		private double PPT_year_sum;
		private int nDaysInYear;
		private boolean data;
		
		public SW_WEATHER_HIST(int year) {
			this.temp_max = new double[Times.MAX_DAYS];
			this.temp_min = new double[Times.MAX_DAYS];
			this.temp_avg = new double[Times.MAX_DAYS];
			this.ppt = new double[Times.MAX_DAYS];
			this.temp_month_max_avg = new double[Times.MAX_MONTHS];
			this.temp_month_min_avg = new double[Times.MAX_MONTHS];
			this.temp_month_avg = new double[Times.MAX_MONTHS];
			this.ppt_month_avg = new double[Times.MAX_MONTHS];
			this.temp_year_avg = 0;	
			this.PPT_year_sum = 0;
			this.nYear = year;
			this.nDaysInYear = 0;
			this.data = false;
		}
		
		public void onSet(int year, double[] ppt, double[] temp_max, double[] temp_min) {
			this.nYear = year;
			if((ppt.length == temp_max.length) && (ppt.length == temp_min.length) && (temp_max.length == temp_min.length) && (ppt.length >= 365) && (ppt.length <= 366)) {
				double acc_max=0,acc_min=0,acc=0,acc_ppt=0;
				int k=0,x=0;
				this.nDaysInYear = ppt.length;
				for(int i=0; i<this.nDaysInYear; i++) {
					this.ppt[i] = ppt[i];
					this.temp_max[i] = temp_max[i];
					this.temp_min[i] = temp_min[i];
					this.temp_avg[i] = (this.temp_max[i]+this.temp_min[i])/2.0;
					acc+=this.temp_avg[i];
					acc_max += this.temp_max[i];
					acc_min += this.temp_min[i];
					k++;
				}
				this.temp_year_avg = acc/(k+0.0);
				this.temp_year_min_avg = acc_min/(k+0.0);
				this.temp_year_max_avg = acc_max/(k+0.0);
				for(int i=0; i<Times.MAX_MONTHS;i++) {
					k=31;
					if(i==8 || i==3 || i==5 || i==10)
						k=30;// september, april, june, & november all have 30 days...
					else if (i==1) {
						k=28;// february has 28 days, except if it's a leap year, in which case it has 29 days...
						if(Times.isleapyear(this.nYear))
							k=29;
					}
					acc=acc_max=acc_min=0;
					acc_ppt=0;
					for(int j=0; j<k; j++) {
						acc+=this.temp_avg[j+x];
						acc_max += this.temp_max[j+x];
						acc_min += this.temp_min[j+x];
						acc_ppt += this.ppt[j+x];
					}
					this.temp_month_avg[i] = acc/(k+0.0);
					this.temp_month_min_avg[i] = acc_min/(k+0.0);
					this.temp_month_max_avg[i] = acc_max/(k+0.0);
					this.ppt_month_avg[i] = acc_ppt;
					x+=k;
				}
				for(int i=0; i<Times.MAX_MONTHS;i++)
					this.PPT_year_sum += this.ppt_month_avg[i];
				this.data = true;
			}
			 
		}
		
		public void onCalc() {
			double acc_max=0,acc_min=0,acc=0,acc_ppt=0;
			int k=0,x=0;
			for(int i=0; i<this.nDaysInYear; i++) {
				this.temp_avg[i] = (this.temp_max[i]+this.temp_min[i])/2.0;
				acc+=this.temp_avg[i];
				acc_max += this.temp_max[i];
				acc_min += this.temp_min[i];
				k++;
			}
			this.temp_year_avg = acc/(k+0.0);
			this.temp_year_min_avg = acc_min/(k+0.0);
			this.temp_year_max_avg = acc_max/(k+0.0);
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
				acc_ppt=0;
				for(int j=0; j<k; j++) {
					acc+=this.temp_avg[j+x];
					acc_max += this.temp_max[j+x];
					acc_min += this.temp_min[j+x];
					acc_ppt += this.ppt[j+x];
				}
				this.temp_month_avg[i] = acc/(k+0.0);
				this.temp_month_min_avg[i] = acc_min/(k+0.0);
				this.temp_month_max_avg[i] = acc_max/(k+0.0);
				this.ppt_month_avg[i] = acc_ppt;
				x+=k;
			}
			for(int i=0; i<Times.MAX_MONTHS;i++)
				this.PPT_year_sum += this.ppt_month_avg[i];
		}
		
		public void onRead(Path WeatherHistoryFile, int year) throws Exception {
			LogFileIn f = log;
			this.nYear = year;
			List<String> lines = SW_FILES.readFile(WeatherHistoryFile.toString(), getClass().getClassLoader());
			double acc_max=0,acc_min=0,acc=0,acc_ppt=0;
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
					acc_max += this.temp_max[doy];
					acc_min += this.temp_min[doy];
					k++;
				}
			}
			this.temp_year_avg = acc/(k+0.0);
			this.temp_year_min_avg = acc_min/(k+0.0);
			this.temp_year_max_avg = acc_max/(k+0.0);
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
				acc_ppt=0;
				for(int j=0; j<k; j++) {
					acc+=this.temp_avg[j+x];
					this.temp_month_min_avg[i] = acc_min/(k+0.0);
					this.temp_month_max_avg[i] = acc_max/(k+0.0);
					acc_ppt += this.ppt[j+x];
				}
				this.temp_month_avg[i] = acc/(k+0.0);
				this.temp_month_min_avg[i] = acc_min/(k+0.0);
				this.temp_month_max_avg[i] = acc_max/(k+0.0);
				this.ppt_month_avg[i] = acc_ppt;
				x+=k;
			}
			for(int i=0; i<Times.MAX_MONTHS;i++)
				this.PPT_year_sum += this.ppt_month_avg[i];
			this.data = true;
		}
		
		public void onWrite(Path WeatherHistoryFolder, String prefix) throws Exception {
			if(this.data) {
				List<String> lines = new ArrayList<String>();
				lines.add("# weather for year = "+this.getYear());
				lines.add("# DOY Tmax(C) Tmin(C) PPT(cm)");
				for(int i=0; i<this.nDaysInYear; i++)
					lines.add(String.valueOf(i+1)+"\t"+String.valueOf(this.temp_max[i])+"\t"+String.valueOf(this.temp_min[i])+"\t"+String.valueOf(this.ppt[i]));
				Files.write(WeatherHistoryFolder.resolve(prefix+"."+this.toString()), lines, StandardCharsets.UTF_8);
			} else {
				LogFileIn f = log;
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
			this.temp_year_max_avg=0;
			this.temp_year_min_avg=0;
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
		public double getYearMinAvg() {
			return this.temp_year_min_avg;
		}
		public double getYearMaxAvg() {
			return this.temp_year_max_avg;
		}
		public int compareTo(SW_WEATHER_HIST n) {
			int lastCmp = Integer.compare(this.nYear, n.getYear());
			return lastCmp;
		}
	}

	public SW_WEATHER_HISTORY(LogFileIn log) {
		this.log = log;
		this.weatherHist = new ArrayList<SW_WEATHER_HIST>();
		yearToIndex = new HashMap<Integer,Integer>();
		this.data = false;
	}
	
	public void onSetYear(int year, double[] tempMax, double[] tempMin, double[] ppt) throws Exception {
		LogFileIn f = log;
		if((tempMax.length != tempMin.length) || (tempMax.length != ppt.length) || (tempMin.length != ppt.length)) {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistoryIn onSet : tempMin tempMax PPT lengths are different from eachother.");
		}
		if(tempMax.length < 365) {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistoryIn onSet : Missing Data.");
		}
		int idx;

		if(yearToIndex.containsKey(year)) {
			idx = yearToIndex.get(year);
		} else {
			SW_WEATHER_HIST weathHist = new SW_WEATHER_HIST(year);
			this.weatherHist.add(weathHist);
			idx = this.weatherHist.indexOf(weathHist);
			this.yearToIndex.put(year, idx);
		}
		//Set Data
		this.weatherHist.get(idx).nDaysInYear = tempMax.length;
		for(int i=0; i<Times.MAX_DAYS; i++) {
			if(i<tempMax.length) {
				this.weatherHist.get(idx).temp_max[i] = tempMax[i];
				this.weatherHist.get(idx).temp_min[i] = tempMin[i];
				this.weatherHist.get(idx).ppt[i] = ppt[i];
			}
		}
		this.weatherHist.get(idx).onCalc();
		this.weatherHist.get(idx).data = true;
		this.data = true;
	}
	
	public void onRead(Path WeatherHistoryFile, Boolean useMarkov) throws Exception {
		LogFileIn f = log;
		int year = 0;
		try {
			year = Integer.parseInt(WeatherHistoryFile.getFileName().toString().split("\\.")[1]);
		} catch(NumberFormatException n) {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistoryIn onRead : Convert Year From Path Failed :" +n.getMessage());
		}
		if(yearToIndex.containsKey(year)) {
			f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistoryIn onRead : Contains Data for Year :" +String.valueOf(year));
		} else {
			if(Files.exists(WeatherHistoryFile) || WeatherHistoryFile.toString().startsWith("resource:")) {
				if(this.yearToIndex.size() < this.weatherHist.size()) {//reuse an object
					for(int i=0; i<this.weatherHist.size(); i++) {
						if(!this.yearToIndex.containsValue(i)) {//This object is not used
							this.weatherHist.get(i).onRead(WeatherHistoryFile, year);
							this.yearToIndex.put(year, i);
							break;
						}
					}
				} else {
					SW_WEATHER_HIST weathHist = new SW_WEATHER_HIST(year);
					weathHist.onRead(WeatherHistoryFile, year);
					this.weatherHist.add(weathHist);
					this.yearToIndex.put(year, this.weatherHist.indexOf(weathHist));
				}
				this.data=true;
			} else {
				if(!useMarkov)
					f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistIn onRead : Path '"+WeatherHistoryFile.toString()+"' doesn't exists.");
			}
		}
	}
	
	/***
	 * R was having a problem calling onRead so testing this one.
	 * @param folder
	 * @param prefix
	 */
	public void onReadAll(String folder, String prefix) {
		Path WeatherHistoryFolder = Paths.get(folder);
		try {
			this.onRead(WeatherHistoryFolder, prefix, 0, 20000, false);
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	
	public void onRead(Path WeatherHistoryFolder, final String prefix, int startYear, int endYear, Boolean useMarkov) throws Exception {
		LogFileIn f = log;
		File[] files = null;
		if(this.data) {
			onClear();
		}
		if(WeatherHistoryFolder.toString().startsWith("resource:")) {
			String[] names = { "resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1949","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1950","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1951","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1952","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1953","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1954","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1955","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1956","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1957","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1958","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1959","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1960","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1961","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1962","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1963","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1964","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1965","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1966","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1967","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1968","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1969","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1970","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1971","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1972","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1973","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1974","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1975","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1976","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1977","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1978","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1979","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1980","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1981","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1982","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1983","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1984","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1985","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1986","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1987","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1988","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1989","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1990","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1991","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1992","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1993","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1994","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1995","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1996","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1997","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1998","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.1999","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2000","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2001","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2002","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2003","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2004","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2005","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2006","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2007","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2008","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2009","resource:soilwat/data/Input/data_39.0625_-119.4375/weath.2010"};
			List<File> temp = new ArrayList<File>(names.length);
			for(int i=0;i<names.length;i++)
				temp.add(new File(names[i]));
			files = new File[names.length];
			files = temp.toArray(files);
		} else {
			File dir = WeatherHistoryFolder.toFile();
			files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.contains(prefix);
				}
			});
		}
		if (files == null) {
			return;
		} else if (files.length == 0) {
			return;
		}
		Arrays.sort(files);
		boolean first = true;
		for(File WeatherHistoryFile : files) {
			int year = 0;
			try {
				year = Integer.parseInt(WeatherHistoryFile.toPath().getFileName().toString().split("\\.")[1]);
			} catch(NumberFormatException n) {
				f.LogError(LogFileIn.LogMode.ERROR, "WeatherHistoryIn onRead : Convert Year From Path Failed :" +n.getMessage());
			}
			if(year >= startYear && year <= endYear) {
				this.onRead(WeatherHistoryFile.toPath(), useMarkov);
				if(first) {
					this.nCurrentYear=year;
					first = false;
				}
			}
		}
		this.data = true;
	}
	
	public void onWrite(Path WeatherHistoryFolder, String prefix, int year) throws Exception {
		LogFileIn f = log;
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
	public void onWrite(Path WeatherHistoryFolder, String prefix) throws Exception {
		if(this.data) {
			Iterator<Entry<Integer, Integer>> it = this.yearToIndex.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
				this.onWrite(WeatherHistoryFolder, prefix, pair.getKey());
			}
		} else {
			LogFileIn f = log;
			f.LogError(LogFileIn.LogMode.WARN, "WeatherIn onWriteWeatherHistories : No Historical Data.");
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
	/**
	 * Returns the Temperature Average for a given year. Daily sum of ((min+max)/2) divided by number of days.
	 * @return double
	 * @throws Exception 
	 */
	public double getTempYearAvg(int year) {
		if(this.data)
			return this.weatherHist.get(this.yearToIndex.get(year)).getYearAvg();
		else
			return 0;
	}
	/**
	 * Returns the Temperature Min Average for a given year. Daily sum of (temp min) divided by number of days.
	 * @return double
	 * @throws Exception 
	 */
	public double getTempYearMinAvg(int year) {
		if(this.data)
			return this.weatherHist.get(this.yearToIndex.get(year)).getYearMinAvg();
		else
			return 0;
	}
	/**
	 * Returns the Temperature Max Average for a given year. Daily sum of (temp max) divided by number of days.
	 * @return double
	 * @throws Exception 
	 */
	public double getTempYearMaxAvg(int year) {
		if(this.data)
			return this.weatherHist.get(this.yearToIndex.get(year)).getYearMaxAvg();
		else
			return 0;
	}

	public int getCurrentYear() {
		return nCurrentYear;
	}

	public boolean setCurrentYear(int nCurrentYear) {
		if(this.yearToIndex.containsKey(nCurrentYear)) {
			this.nCurrentYear = nCurrentYear;
			return true;
		} else {
			return false;
		}
	}
	public int get_nDays(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).nDaysInYear;
	}
	public double[] get_ppt_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).ppt;
	}
	public double[] get_temp_max_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).temp_max;
	}
	public double[] get_temp_min_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).temp_min;
	}
	public double[] get_temp_avg_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).temp_avg;
	}
	public double[] get_temp_month_avg_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).temp_month_avg;
	}
	public double[] get_temp_month_min_avg_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).temp_month_min_avg;
	}
	public double[] get_temp_month_max_avg_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).temp_month_max_avg;
	}
	public double[] get_ppt_month_avg_array(int year) {
		return this.weatherHist.get(yearToIndex.get(year)).ppt_month_avg;
	}
	public double get_temp_year_avg(int year) {//mean annual temperature
		return this.weatherHist.get(yearToIndex.get(year)).temp_year_avg;
	}
	public double get_temp_year_min_avg(int year) {//mean annual temperature
		return this.weatherHist.get(yearToIndex.get(year)).temp_year_min_avg;
	}
	public double get_temp_year_max_avg(int year) {//mean annual temperature
		return this.weatherHist.get(yearToIndex.get(year)).temp_year_max_avg;
	}
	public double get_ppt_year_sum(int year) {//mean annual temperature
		return this.weatherHist.get(yearToIndex.get(year)).PPT_year_sum;
	}
	/**
	 * Returns the meanMonthlyTempC across selected years for avgerage monthly temp values
	 * @return double[]
	 * @throws Exception 
	 */
	public double[] meanMonthlyTempC(int nStartYear, int nEndYear) throws WeatherException {
		if(nStartYear < this.getStartYear() || nEndYear > this.getEndYear())
			throw new WeatherException("Requested data is not present");
		if(nStartYear > nEndYear)
			throw new WeatherException("Start Year can not be greater then end year;");
		double nYears = (double) (nEndYear - nStartYear + 1);
		double[] temp = new double[12];//Will init to 0
		for(int i=nStartYear; i<=nEndYear; i++) {
			double[] temp_y = get_temp_month_avg_array(i);//get the monthly avg
			for(int j=0; j<12; j++) {
				temp[j] += temp_y[j];//add it to temp
			}
		}
		for(int i=0; i<12; i++)
			temp[i] = temp[i]/nYears;
		return temp;
	}
	/**
	 * Returns the meanMonthlyTempC across selected years for Min monthly temp values
	 * @return double[]
	 * @throws WeatherException 
	 */
	public double[] meanMonthlyTempC_Min(int nStartYear, int nEndYear) throws WeatherException {
		if(nStartYear < this.getStartYear() || nEndYear > this.getEndYear())
			throw new WeatherException("Requested data is not present");
		if(nStartYear > nEndYear)
			throw new WeatherException("Start Year can not be greater then end year;");
		double nYears = (double) (nEndYear - nStartYear + 1);
		double[] temp = new double[12];//Will init to 0
		for(int i=nStartYear; i<=nEndYear; i++) {
			double[] temp_y = get_temp_month_min_avg_array(i);//get the monthly avg
			for(int j=0; j<12; j++) {
				temp[j] += temp_y[j];//add it to temp
			}
		}
		for(int i=0; i<12; i++)
			temp[i] = temp[i]/nYears;
		return temp;
	}
	/**
	 * Returns the meanMonthlyTempC across selected years for max monthly temp values
	 * @return double[]
	 * @throws WeatherException 
	 */
	public double[] meanMonthlyTempC_Max(int nStartYear, int nEndYear) throws WeatherException {
		if(nStartYear < this.getStartYear() || nEndYear > this.getEndYear())
			throw new WeatherException("Requested data is not present");
		if(nStartYear > nEndYear)
			throw new WeatherException("Start Year can not be greater then end year;");
		double nYears = (double) (nEndYear - nStartYear + 1);
		double[] temp = new double[12];//Will init to 0
		for(int i=nStartYear; i<=nEndYear; i++) {
			double[] temp_y = get_temp_month_max_avg_array(i);//get the monthly avg
			for(int j=0; j<12; j++) {
				temp[j] += temp_y[j];//add it to temp
			}
		}
		for(int i=0; i<12; i++)
			temp[i] = temp[i]/nYears;
		return temp;
	}
	/**
	 * Returns the meanMonthlyPPTcm across selected years
	 * @return double[]
	 * @throws WeatherException 
	 */
	public double[] meanMonthlyPPTcm(int nStartYear, int nEndYear) throws WeatherException {
		if(nStartYear < this.getStartYear() || nEndYear > this.getEndYear())
			throw new WeatherException("Requested data is not present");
		if(nStartYear > nEndYear)
			throw new WeatherException("Start Year can not be greater then end year;");
		double nYears = (double) (nEndYear - nStartYear + 1);
		double[] temp = new double[12];//Will init to 0
		for(int i=nStartYear; i<=nEndYear; i++) {
			double[] temp_y = get_ppt_month_avg_array(i);//get the monthly avg
			for(int j=0; j<12; j++) {
				temp[j] += temp_y[j];//add it to temp
			}
		}
		for(int i=0; i<12; i++)
			temp[i] = temp[i]/nYears;
		return temp;
	}
	
	/**
	 * Returns the mean annual temperature in C across selected years
	 * @return double
	 * @throws WeatherException 
	 */
	public double MAT_C(int nStartYear, int nEndYear) throws WeatherException {
		if(nStartYear < this.getStartYear() || nEndYear > this.getEndYear())
			throw new WeatherException("Requested data is not present");
		if(nStartYear > nEndYear)
			throw new WeatherException("Start Year can not be greater then end year;");
		double nYears = (double) (nEndYear - nStartYear + 1);
		
		double mat_c = 0;
		for(int i=nStartYear; i<=nEndYear; i++) {
			mat_c += get_temp_year_avg(i);
		}
		mat_c = mat_c/nYears;
		return mat_c;
	}
	
	/**
	 * Returns the mean annual ppt in (cm) across selected years
	 * @return double
	 * @throws WeatherException 
	 */
	public double MAP_cm(int nStartYear, int nEndYear) throws WeatherException {
		if(nStartYear < this.getStartYear() || nEndYear > this.getEndYear())
			throw new WeatherException("Requested data is not present");
		if(nStartYear > nEndYear)
			throw new WeatherException("Start Year can not be greater then end year;");
		
		double[] temp = meanMonthlyPPTcm(nStartYear, nEndYear);
		double map_cm = 0;
		for(int j=0; j<12; j++) {
			map_cm += temp[j];//add it to temp
		}
		return map_cm;
	}
	
	public double get_ppt(int doy) {
		return this.weatherHist.get(yearToIndex.get(nCurrentYear)).ppt[doy];
	}
	public double get_temp_max(int doy) {
		return this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_max[doy];
	}
	public double get_temp_min(int doy) {
		return this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_min[doy];
	}
	public double get_temp_avg(int doy) {
		return this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_avg[doy];
	}
	public double get_temp_month_avg(int month) {
		return this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_month_avg[month];
	}
	
	public void set_ppt(int doy, double value) {
		this.weatherHist.get(yearToIndex.get(nCurrentYear)).ppt[doy] = value;
	}
	public void set_temp_max(int doy, double value) {
		this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_max[doy] = value;
	}
	public void set_temp_min(int doy, double value) {
		this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_min[doy] = value;
	}
	
	public void set_day(int doy, double ppt, double temp_max, double temp_min) {
		this.weatherHist.get(yearToIndex.get(nCurrentYear)).ppt[doy] = ppt;
		this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_max[doy] = temp_max;
		this.weatherHist.get(yearToIndex.get(nCurrentYear)).temp_min[doy] = temp_min;
	}
	
	public void add_year(int year, double[] ppt, double[] temp_max, double[] temp_min) {
		if(this.yearToIndex.size() < this.weatherHist.size()) {//reuse an object
			for(int i=0; i<this.weatherHist.size(); i++) {
				if(!this.yearToIndex.containsValue(i)) {//This object is not used
					this.weatherHist.get(i).onClear();
					this.weatherHist.get(i).onSet(year, ppt, temp_max, temp_min);
					this.yearToIndex.put(year, i);
					break;
				}
			}
		} else {
			SW_WEATHER_HIST weathHist = new SW_WEATHER_HIST(year);
			weathHist.data = true;
			weathHist.onSet(year, ppt, temp_max, temp_min);
			this.weatherHist.add(weathHist);
			this.yearToIndex.put(year, this.weatherHist.indexOf(weathHist));
		}
		this.data=true;
	}
	
	public void remove(int year) {
		this.yearToIndex.remove(year);
	}
	
	public void removeAll() {
		this.yearToIndex.clear();
	}
	
	public void onCalcData() {
		this.weatherHist.get(yearToIndex.get(nCurrentYear)).onCalc();
	}
	
	public String[] getHistYearsString() {
		String[] temp = new String[yearToIndex.size()];
		int i=0;
		for(Map.Entry<Integer, Integer> entry : yearToIndex.entrySet()) {
			Integer key = entry.getKey();
			temp[i] = key.toString();
			i++;
		}
		Arrays.sort(temp);
		return temp;
	}
	
	public List<Integer> getHistYearsInteger() {
		List<Integer> temp = new ArrayList<Integer>();
		for(Map.Entry<Integer, Integer> entry : yearToIndex.entrySet()) {
			Integer key = entry.getKey();
			temp.add(key);
		}
		Collections.sort(temp);
		return temp;
	}
	
	public int get_nYears() {
		return yearToIndex.size();
	}
	
	public int getStartYear() {
		List<Integer> temp = new ArrayList<Integer>();
		for(Map.Entry<Integer, Integer> entry : yearToIndex.entrySet()) {
			Integer key = entry.getKey();
			temp.add(key);
		}
		Collections.sort(temp);
		return Collections.min(temp);
	}
	
	public int getEndYear() {
		List<Integer> temp = new ArrayList<Integer>();
		for(Map.Entry<Integer, Integer> entry : yearToIndex.entrySet()) {
			Integer key = entry.getKey();
			temp.add(key);
		}
		Collections.sort(temp);
		return Collections.max(temp);
	}
	
	public int getDays() {
		return this.weatherHist.get(yearToIndex.get(nCurrentYear)).nDaysInYear;
	}
	
	public boolean data() {
		return this.data;
	}
}
