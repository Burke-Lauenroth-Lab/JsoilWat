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
import defines.Defines;

public class SwcHistoryIn {
	
	private int nCurrentYear;
	private List<SW_SOILWAT_HIST> swcHist;
	private Map<Integer, Integer> yearToIndex;
	private boolean data; 
	
	private class SW_SOILWAT_HIST {
		private double swc[][];
		private double std_err[][];
		private int nDaysInYear;
		private int nLayers;
		private int nYear;
		private boolean data;
		
		public SW_SOILWAT_HIST() {
			this.swc = new double[Times.MAX_DAYS][Defines.MAX_LAYERS];
			this.std_err = new double[Times.MAX_DAYS][Defines.MAX_LAYERS];
			this.data = false;
			this.nYear = 0;
			this.nDaysInYear = 0;
			this.nLayers = 0;
		}
		
		public void onRead(Path swcHistoryFile, int year) throws IOException {
			LogFileIn f = LogFileIn.getInstance();
			this.nYear = year;
			List<String> lines = Files.readAllLines(swcHistoryFile, StandardCharsets.UTF_8);
			int doy=0, lyr=0;
			
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
							f.LogError(LogFileIn.LogMode.ERROR, "SwcHistory onRead : Day of Year out of range doy "+String.valueOf(doy));
						lyr = Integer.parseInt(values[1])-1;
						if(doy == 0)
							if(this.nLayers < (lyr+1))
								this.nLayers = lyr+1;
						if(lyr < 0 || lyr > (Defines.MAX_LAYERS-1))
							f.LogError(LogFileIn.LogMode.ERROR, "SwcHistory onRead : Layer out of range doy "+String.valueOf(doy));
						this.swc[doy][lyr] = Double.parseDouble(values[2]);
						this.std_err[doy][lyr] = Double.parseDouble(values[3]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "SwcHistory onRead : Convert Error :" +e.getMessage());
					}
				}
			}
			this.nDaysInYear=doy+1;
			this.data = true;
		}
		
		public void onWrite(Path swcHistoryFile, String prefix) throws IOException {
			if(this.data) {
				List<String> lines = new ArrayList<String>();
				lines.add("# SWC history for year = "+String.valueOf(this.nYear));
				lines.add(String.format("#%3s %5s   %3s   %s", "DOY", "Layer", "SWC", "stderr"));
				for(int i=0; i<this.nDaysInYear; i++)
					for(int j=0; j<this.nLayers; j++)
						lines.add(String.format("%4d %5d %8f %8f", i+1, j+1, this.swc[i][j], this.std_err[i][j]));
				Files.write(swcHistoryFile.resolve(prefix+"."+this.toString()), lines, StandardCharsets.UTF_8);
			} else {
				LogFileIn f = LogFileIn.getInstance();
				f.LogError(LogFileIn.LogMode.WARN, "SwcHistory onWrite : No data from files or default.");
			}
		}
		public void onClear() {
			this.nDaysInYear=0;
			this.nLayers=0;
			this.nYear = 0;
			this.data = false;
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof SW_SOILWAT_HIST))
				return false;
			SW_SOILWAT_HIST n = (SW_SOILWAT_HIST) o;
			return n.toString().equals(this.toString());
		}
		public String toString() {
			return String.valueOf(this.nYear);
		}
		public int getYear() {
			return this.nYear;
		}
	}
	
	public SwcHistoryIn() {
		this.swcHist = new ArrayList<SW_SOILWAT_HIST>();
		yearToIndex = new HashMap<Integer,Integer>();
		this.data = false;
	}
	
	public void onRead(Path swcHistoryFile) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		int year = 0;
		try {
			year = Integer.parseInt(swcHistoryFile.getFileName().toString().split("\\.")[1]);
		} catch(NumberFormatException n) {
			f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onRead : Convert Year From Path Failed :" +n.getMessage());
		}
		if(yearToIndex.containsKey(year)) {
			f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onRead : Contains Data for Year :" +String.valueOf(year));
		} else {
			if(Files.exists(swcHistoryFile)) {
				if(this.yearToIndex.size() < this.swcHist.size()) {//reuse an object
					for(int i=0; i<this.swcHist.size(); i++) {
						if(!this.yearToIndex.containsValue(i)) {//This object is not used
							this.swcHist.get(i).onRead(swcHistoryFile, year);
							this.yearToIndex.put(year, i);
							break;
						}
					}
				} else {
					SW_SOILWAT_HIST weathHist = new SW_SOILWAT_HIST();
					weathHist.onRead(swcHistoryFile, year);
					this.swcHist.add(weathHist);
					this.yearToIndex.put(year, this.swcHist.indexOf(weathHist));
				}
				this.data=true;
			} else {
				f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onRead : Path '"+swcHistoryFile.toString()+"' doesn't exists.");
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
			if(this.swcHist.get(i).getYear() == year) {
				this.swcHist.get(i).onWrite(WeatherHistoryFolder, prefix);
			} else {
				f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onWrite : Year and Year of Data do not match.");
			}
		} else {
			f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onWrite : Year and Year of Data do not match.");
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
			f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onWrite : No Historical Data.");
		}
	}
	
	public void onClear() {
		this.yearToIndex.clear();
		if(this.data) {
			for(int i=0; i<this.swcHist.size(); i++) {
				this.swcHist.get(i).onClear();
			}
		}
		this.data = false;
	}
	
	public int getCurrentYear() {
		return nCurrentYear;
	}

	public void setCurrentYear(int nCurrentYear) {
		this.nCurrentYear = nCurrentYear;
	}
}
