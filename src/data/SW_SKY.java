package data;

import input.LogFileIn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import times.Times;

public class SW_SKY {
	private final String[] comments = new String[]{"# (site:	002_-119.415_39.046	), sky cover (sunrise-sunset),%,Climate Atlas of the US,http://cdo.ncdc.noaa.gov/cgi-bin/climaps/climaps.pl",
			"# Wind speed (m/s),Climate Atlas of the US,http://cdo.ncdc.noaa.gov/cgi-bin/climaps/climaps.pl",
			"# rel. Humidity (%),Climate Atlas of the US,http://cdo.ncdc.noaa.gov/cgi-bin/climaps/climaps.pl",
			"# transmissivity (rel), only used in petfunc, but falls out of the equations (a = trans * b, c = a / trans)",
			"# snow density (kg/m3): Brown, R. D. and P. W. Mote. 2009. The response of Northern Hemisphere snow cover to a changing climate. Journal of Climate 22:2124-2145."};
	
	private double[] cloudcov,	/* monthly cloud cover (frac) */
    windspeed,					/* windspeed (m/s) */
    r_humidity,					/* relative humidity (%) */
    transmission,				/* frac light transmitted by atmos. */ /* used as input for petfunc, but algorithm cancels it out */
    snow_density;				/* snow density (kg/m3) */

	private double[] cloudcov_daily,	/* interpolated daily cloud cover (frac) */
    windspeed_daily, 					/* interpolated daily windspeed (m/s) */
    r_humidity_daily, 					/* interpolated daily relative humidity (%) */
    transmission_daily,					/* interpolated daily frac light transmitted by atmos. */ /* used as input for petfunc, but algorithm cancels it out */
    snow_density_daily;					/* interpolated daily snow density (kg/m3) */
	
	private boolean data;
	
	public SW_SKY() {
		this.data = false;
		
		this.cloudcov = new double[Times.MAX_MONTHS];
		this.windspeed = new double[Times.MAX_MONTHS];
		this.r_humidity = new double[Times.MAX_MONTHS];
		this.transmission = new double[Times.MAX_MONTHS];
		this.snow_density = new double[Times.MAX_MONTHS];
		
		this.cloudcov_daily = new double[Times.MAX_DAYS+1];
		this.windspeed_daily = new double[Times.MAX_DAYS+1];
		this.r_humidity_daily = new double[Times.MAX_DAYS+1];
		this.transmission_daily = new double[Times.MAX_DAYS+1];
		this.snow_density_daily = new double[Times.MAX_DAYS+1];
	}
	
	public void onSetDefault() {
		this.data = true;
		cloudcov = new double[] {71,61,61,51,41,31,23,23,31,41,61,61};
		windspeed = new double[] {1.3,2.9,3.3,3.8,3.8,3.8,3.3,3.3,2.9,1.3,1.3,1.3};
		r_humidity = new double[] {61,61,61,51,51,51,41,41,51,51,61,61};
		transmission = new double[] {1,1,1,1,1,1,1,1,1,1,1,1};
		snow_density = new double[] {213.7,241.6,261,308,398.1,464.5,0,0,0,140,161.6,185.1};
	}
	
	public boolean onVerify() {
		if(this.data)
			return true;
		else
			return false;
	}
		
	public void onRead(Path CloudIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(CloudIn, StandardCharsets.UTF_8);
		int lineno=0;
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				if(values.length < 12)
					f.LogError(LogFileIn.LogMode.ERROR, "swCloud onRead : Line "+String.valueOf(lineno+1)+": Not enough values.");
				for (int j=0; j<12; j++) {
					try {
						switch (lineno) {
						case 0:
							cloudcov[j] = Double.parseDouble(values[j]);
							break;
						case 1:
							windspeed[j] = Double.parseDouble(values[j]);
							break;
						case 2:
							r_humidity[j] = Double.parseDouble(values[j]);
							break;
						case 3:
							transmission[j] = Double.parseDouble(values[j]);
							break;
						case 4:
							snow_density[j] = Double.parseDouble(values[j]);
						default:
							break;
						}
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "swCloud onRead : Line:"+String.valueOf(lineno)+" Could not convert string to number." + e.getMessage());
					}
				}
				lineno++;
			}
		}
		this.data = true;
	}
	
	public void onWrite(Path CloudIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			String temp = "";
			for(int i=0;i<5;i++){
				for(int j=0;j<12;j++) {
					switch (i) {
					case 0:
						temp += Double.toString(cloudcov[j])+" ";
						break;
					case 1:
						temp += Double.toString(windspeed[j])+" ";
						break;
					case 2:
						temp += Double.toString(r_humidity[j])+" ";
						break;
					case 3:
						temp += Double.toString(transmission[j])+" ";
						break;
					case 4:
						temp += Double.toString(snow_density[j])+" ";
					default:
						break;
					}
				}
				temp += "\t"+comments[i];
				lines.add(temp);
				temp="";
			}

			Files.write(CloudIn, lines, StandardCharsets.UTF_8);
		} else {
			f.LogError(LogFileIn.LogMode.ERROR, "swCloud onWrite : No Data to Write.");
		}
	}
	
	public void onClear() {
		for(int i=0; i<(Times.MAX_DAYS+1); i++) {
			if(i<Times.MAX_MONTHS) {
				this.cloudcov[i]=0;
				this.windspeed[i]=0;
				this.r_humidity[i]=0;
				this.transmission[i]=0;
				this.snow_density[0]=0;
			}
			this.cloudcov_daily[i] = 0;
			this.windspeed_daily[i] = 0;
			this.r_humidity_daily[i] = 0;
			this.transmission_daily[i] = 0;
			this.snow_density_daily[i] = 0;
		}
	}
	
	public void SW_SKY_init() {
		Times.interpolate_monthlyValues(this.cloudcov, this.cloudcov_daily);
		Times.interpolate_monthlyValues(this.windspeed, this.windspeed_daily);
		Times.interpolate_monthlyValues(this.r_humidity, this.r_humidity_daily);
		Times.interpolate_monthlyValues(this.transmission, this.transmission_daily);
		Times.interpolate_monthlyValues(this.snow_density, this.snow_density_daily);
	}
	
	public double[] getCloudcov() {
		return cloudcov;
	}
	
	public double getCloudcov(Times.Months month) {
		return cloudcov[month.ordinal()];
	}

	public void setCloudcov(double[] cloudcov) {
		if(cloudcov.length == Times.MAX_MONTHS)
			this.cloudcov = cloudcov;
	}
	
	public void setCloudcov(double cloudcov, Times.Months month) {
		this.cloudcov[month.ordinal()] = cloudcov;
	}

	public double[] getWindspeed() {
		return windspeed;
	}
	
	public double getWindspeed(Times.Months month) {
		return windspeed[month.ordinal()];
	}

	public void setWindspeed(double[] windspeed) {
		if(windspeed.length == Times.MAX_MONTHS)
			this.windspeed = windspeed;
	}
	
	public void setWindspeed(double windspeed, Times.Months month) {
		this.windspeed[month.ordinal()] = windspeed;
	}

	public double[] getR_humidity() {
		return r_humidity;
	}
	
	public double getR_humidity(Times.Months month) {
		return r_humidity[month.ordinal()];
	}

	public void setR_humidity(double[] r_humidity) {
		if(r_humidity.length == Times.MAX_MONTHS)
			this.r_humidity = r_humidity;
	}
	
	public void setR_humidity(double r_humidity, Times.Months month) {
		this.r_humidity[month.ordinal()] = r_humidity;
	}

	public double[] getSnow_density() {
		return snow_density;
	}

	public void setSnow_density(double[] snow_density) {
		if(snow_density.length == Times.MAX_MONTHS)
			this.snow_density = snow_density;
	}
	
	public void setSnow_density(double snow_density, Times.Months month) {
		this.snow_density[month.ordinal()] = snow_density;
	}
}
