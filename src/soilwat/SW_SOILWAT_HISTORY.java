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
import java.util.Set;
import java.util.Map.Entry;


public class SW_SOILWAT_HISTORY {

	public static final double MISSING=999.;
	private int nCurrentYear;
	private List<SW_SOILWAT_HIST> swcHist;
	private Map<Integer, Integer> yearToIndex;
	private boolean data; 
	private LogFileIn log;
	
	public String toString() {
		String out = "SWC History List\n";
		Set<Integer> years = yearToIndex.keySet();
		for(Integer year : years) {
			out += year.toString()+"\n";
		}
		return out;
	}
	
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
		
		public void onSet(int year, int layers, double[][] swc, double[][] std_err) {
			this.nYear = year;
			if((swc.length == std_err.length) && (swc.length >= 365) && (std_err.length <= 366)) {
				this.nDaysInYear = swc.length;
				this.nLayers = layers;
				for(int i=0; i<this.nDaysInYear; i++) {
					for(int j=0; j<layers; j++) {
						this.swc[i][j] = swc[i][j];
						this.std_err[i][j] = std_err[i][j];
					}
				}
				this.data = true;
			}
		}
		
		public void onRead(Path swcHistoryFile, int year) throws Exception {
			LogFileIn f = log;
			this.nYear = year;
			List<String> lines = SW_FILES.readFile(swcHistoryFile.toString(), getClass().getClassLoader());
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
		
		public void onWrite(Path swcHistoryFile, String prefix) throws Exception {
			if(this.data) {
				List<String> lines = new ArrayList<String>();
				lines.add("# SWC history for year = "+String.valueOf(this.nYear));
				lines.add(String.format("#%3s %5s   %3s   %s", "DOY", "Layer", "SWC", "stderr"));
				for(int i=0; i<this.nDaysInYear; i++)
					for(int j=0; j<this.nLayers; j++)
						lines.add(String.format("%4d %5d %8f %8f", i+1, j+1, this.swc[i][j], this.std_err[i][j]));
				Files.write(swcHistoryFile.resolve(prefix+"."+this.toString()), lines, StandardCharsets.UTF_8);
			} else {
				log.LogError(LogFileIn.LogMode.WARN, "SwcHistory onWrite : No data from files or default.");
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
	
	public SW_SOILWAT_HISTORY(LogFileIn log) {
		this.log = log;
		this.swcHist = new ArrayList<SW_SOILWAT_HIST>();
		yearToIndex = new HashMap<Integer,Integer>();
		this.data = false;
	}
	
	public void onRead(Path swcHistoryFile) throws Exception {
		LogFileIn f = log;
		int year = 0;
		try {
			year = Integer.parseInt(swcHistoryFile.getFileName().toString().split("\\.")[1]);
		} catch(NumberFormatException n) {
			f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onRead : Convert Year From Path Failed :" +n.getMessage());
		}
		if(yearToIndex.containsKey(year)) {
			f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onRead : Contains Data for Year :" +String.valueOf(year));
		} else {
			if(Files.exists(swcHistoryFile) || swcHistoryFile.toString().startsWith("resource:")) {
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
	
	/***
	 * R was having a problem calling onRead so testing this one.
	 * @param folder
	 * @param prefix
	 */
	public void onReadAll(String folder, String prefix) {
		Path WeatherHistoryFolder = Paths.get(folder);
		try {
			this.onRead(WeatherHistoryFolder, prefix, 0, 20000);
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	
	public void onRead(Path SWCHistoryFolder, final String prefix, int startYear, int endYear) throws Exception {
		LogFileIn f = log;
		File[] files = null;
		if(this.data) {
			onClear();
		}
		if(SWCHistoryFolder.toString().startsWith("resource:")) {
			String[] names = {""};//EXAMPLE PROJECT DOESN'T HAVE
			if(names == null || names[0]=="")
				return;
			List<File> temp = new ArrayList<File>(names.length);
			for(int i=0;i<names.length;i++)
				temp.add(new File(names[i]));
			files = new File[names.length];
			files = temp.toArray(files);
		} else {
			File dir = SWCHistoryFolder.toFile();
			files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.contains(prefix);
				}
			});
		}
		if(files == null) {
			return;
		} else if(files.length == 0) {
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
				this.onRead(WeatherHistoryFile.toPath());
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
			if(this.swcHist.get(i).getYear() == year) {
				this.swcHist.get(i).onWrite(WeatherHistoryFolder, prefix);
			} else {
				f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onWrite : Year and Year of Data do not match.");
			}
		} else {
			f.LogError(LogFileIn.LogMode.ERROR, "swcHistory onWrite : Year and Year of Data do not match.");
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
			f.LogError(LogFileIn.LogMode.NOTE, "swcHistory onWrite : No Historical Data.");
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

	public boolean setCurrentYear(int nCurrentYear) {
		if(this.yearToIndex.containsKey(nCurrentYear)) {
			this.nCurrentYear = nCurrentYear;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean swcMissing(int doy) {
		if(Defines.EQ(this.swcHist.get(yearToIndex.get(nCurrentYear)).swc[doy-1][1], MISSING))
			return true;
		else
			return false;
	}
	
	public int getFirstYear() {
		Object[] years = this.yearToIndex.keySet().toArray();
		Arrays.sort(years);
		return (int)years[0];
	}
	
	public double[] getSWC(int doy) {
		return this.swcHist.get(yearToIndex.get(nCurrentYear)).swc[doy];
	}
	
	public double[] getStd_err(int doy) {
		return this.swcHist.get(yearToIndex.get(nCurrentYear)).std_err[doy];
	}
	
	public double getSWC(int doy, int layer) {
		return this.swcHist.get(yearToIndex.get(nCurrentYear)).swc[doy][layer];
	}
	
	public double getStd_err(int doy, int layer) {
		return this.swcHist.get(yearToIndex.get(nCurrentYear)).std_err[doy][layer];
	}
	
	public void setSWC(int doy, int layer, double value) {
		this.swcHist.get(yearToIndex.get(nCurrentYear)).swc[doy][layer] = value;
	}
	
	public void setStd_err(int doy, int layer, double value) {
		this.swcHist.get(yearToIndex.get(nCurrentYear)).std_err[doy][layer] = value;
	}
	
	public void set_day(int doy, int layer, double swc, double std_err) {
		this.swcHist.get(yearToIndex.get(nCurrentYear)).swc[doy][layer] = swc;
		this.swcHist.get(yearToIndex.get(nCurrentYear)).std_err[doy][layer] = std_err;
	}
	
	public void add_year(int year, int layers, double[][] swc, double[][] std_error) {
		if(this.yearToIndex.size() < this.swcHist.size()) {//reuse an object
			for(int i=0; i<this.swcHist.size(); i++) {
				if(!this.yearToIndex.containsValue(i)) {//This object is not used
					this.swcHist.get(i).onClear();
					this.swcHist.get(i).onSet(year, layers, swc, std_error);
					this.yearToIndex.put(year, i);
					break;
				}
			}
		} else {
			SW_SOILWAT_HIST swcHist = new SW_SOILWAT_HIST();
			swcHist.nYear = year;
			swcHist.data = true;
			swcHist.onSet(year, layers, swc, std_error);
			this.swcHist.add(swcHist);
			this.yearToIndex.put(year, this.swcHist.indexOf(swcHist));
		}
		this.data=true;
	}

	public void remove(int year) {
		this.yearToIndex.remove(year);
	}
	
	public String[] getHistYears() {
		String[] temp = new String[yearToIndex.size()];
		int i=0;
		for(Map.Entry<Integer, Integer> entry : yearToIndex.entrySet()) {
			Integer key = entry.getKey();
			temp[i] = key.toString();
			i++;
		}
		return temp;
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
		if(data) {
			for(Map.Entry<Integer, Integer> entry : yearToIndex.entrySet()) {
				Integer key = entry.getKey();
				temp.add(key);
			}
			Collections.sort(temp);
		}
		return temp;
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
		return this.swcHist.get(yearToIndex.get(nCurrentYear)).nDaysInYear;
	}
	public int getLayers() {
		return this.swcHist.get(yearToIndex.get(nCurrentYear)).nLayers;
	}
	public void setLayers(int nLayers) {
		assert nLayers >0 && nLayers<=25;
		Iterator<Entry<Integer, Integer>> it = this.yearToIndex.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
			this.swcHist.get(pair.getValue()).nLayers = nLayers;
		}
	}
	public boolean data() {
		return this.data;
	}
}
