package input;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import defines.SW_TIMES;
import times.Times;

public class WeatherSetupIn {
	private boolean use_markov;
	private boolean use_snow;
	private double pct_snowdrift;
	private double pct_snowRunoff;
	private double[] scale_precip;
	private double[] scale_temp_max;
	private double[] scale_temp_min;
	private int days_in_runavg;
	private SW_TIMES yr;
	private boolean verified;
	private boolean read;
	private int nFileItemsRead;
	private final int nFileItems=18;
	
	public WeatherSetupIn(){
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
		this.verified = false;
		this.read = false;
	}
	
	public void onClear() {
		this.read = false;
		this.verified = false;
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
	}
	
	public void onSetDefault(Path ProjectDirectory) {
		this.verified = false;
		this.read = true;
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
	}
	
	public boolean getUseMarkov() {
		return this.use_markov;
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
			if(this.nFileItemsRead != nFileItems)
				f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Too few input lines.");
			if(!this.yr.totalSet()) {
				if(!this.yr.firstSet())
					f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Year First Not Set.");
				if(!this.yr.lastSet())
					f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Year Last Not Set.");
			}
			if(!this.use_markov && (ModelStartYear < this.yr.getFirst()))
				f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onVerify : Model Year ("+String.valueOf(ModelStartYear)+") starts before Weather Files ("+String.valueOf(this.yr.getFirst())+")"+
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
						this.use_snow = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Use Snow : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 1:
					try {
						this.pct_snowdrift = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : pct snow drift : Could not convert string to double." + e.getMessage());
					}
					break;
				case 2:
					try {
						this.pct_snowRunoff = Double.parseDouble(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : pct snow Runoff : Could not convert string to double." + e.getMessage());
					}
					break;
				case 3:
					try {
						this.use_markov = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Use Markov : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 4:
					try {
						this.yr.setFirst(Integer.parseInt(values[0]));
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Year First : Could not convert string to integer." + e.getMessage());
					}
					break;
				case 5:
					try{
						this.days_in_runavg = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "WeatherIn onReadWeatherIn : Days in Runavg : Could not convert string to integer." + e.getMessage());
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
			f.LogError(LogFileIn.LogMode.WARN, "WeatherIn onWriteWeatherIn : No data from files or default.");
		}
	}
}
