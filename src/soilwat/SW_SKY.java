package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.InputData.CloudIn;

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
	
	protected SW_SKY() {
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
	
	protected void onSetDefault() {
		this.data = true;
		cloudcov = new double[] {71,61,61,51,41,31,23,23,31,41,61,61};
		windspeed = new double[] {1.3,2.9,3.3,3.8,3.8,3.8,3.3,3.3,2.9,1.3,1.3,1.3};
		r_humidity = new double[] {61,61,61,51,51,51,41,41,51,51,61,61};
		transmission = new double[] {1,1,1,1,1,1,1,1,1,1,1,1};
		snow_density = new double[] {213.7,241.6,261,308,398.1,464.5,0,0,0,140,161.6,185.1};
	}
	
	protected boolean onVerify(double[] scale_sky, double[] scale_wind, double[] scale_rH, double[] scale_transmissivity) {
		if(this.data) {
			SW_SKY_init(scale_sky, scale_wind, scale_rH, scale_transmissivity);
			return true;
		} else
			return false;
	}
	
	protected void onSetInput(CloudIn cloudIn) {
		for(int i=0; i<Times.MAX_MONTHS; i++) {
			this.cloudcov[i] = cloudIn.cloudcov[i];
			this.windspeed[i] = cloudIn.windspeed[i];
			this.r_humidity[i] = cloudIn.r_humidity[i];
			this.transmission[i] = cloudIn.transmission[i];
			this.snow_density[i] = cloudIn.snow_density[i];
		}
		this.data = true;
	}
	
	protected void onGetInput(CloudIn cloudIn) {
		for(int i=0; i<Times.MAX_MONTHS; i++) {
			cloudIn.cloudcov[i] = this.cloudcov[i];
			cloudIn.windspeed[i] = this.windspeed[i];
			cloudIn.r_humidity[i] = this.r_humidity[i];
			cloudIn.transmission[i] = this.transmission[i];
			cloudIn.snow_density[i] = this.snow_density[i];
		}
	}
	
	protected void onRead(Path CloudIn) throws IOException {
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
	
	protected void onWrite(Path CloudIn) throws IOException {
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
	
	protected void onClear() {
		this.data = false;
		/*for(int i=0; i<(Times.MAX_DAYS+1); i++) {
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
		}*/
	}
	
	protected void SW_SKY_init(double[] scale_sky, double[] scale_wind, double[] scale_rH, double[] scale_transmissivity) {
		for(int i=0; i<Times.MAX_MONTHS; i++) {
			this.cloudcov[i] = Math.min(100, Math.max(0.0,scale_sky[i]+this.cloudcov[i]));
			this.windspeed[i] = Math.max(0.0, scale_wind[i] * this.windspeed[i]);
			this.r_humidity[i] = Math.min(100, Math.max(0.0, scale_rH[i] + this.r_humidity[i]));
			this.transmission[i] = Math.min(1, Math.max(0.0, scale_transmissivity[i]*transmission[i]));
		}
		Times.interpolate_monthlyValues(this.cloudcov, this.cloudcov_daily);
		Times.interpolate_monthlyValues(this.windspeed, this.windspeed_daily);
		Times.interpolate_monthlyValues(this.r_humidity, this.r_humidity_daily);
		Times.interpolate_monthlyValues(this.transmission, this.transmission_daily);
		Times.interpolate_monthlyValues(this.snow_density, this.snow_density_daily);
	}
	
	protected double[] getCloudcov_daily() {
		return cloudcov_daily;
	}
	
	protected double getCloudcov_daily(int doy) {
		return cloudcov_daily[doy];
	}

	protected void setCloudcov_daily(double[] cloudcov_daily) {
		if(cloudcov_daily.length == Times.MAX_DAYS+1)
			this.cloudcov_daily = cloudcov_daily;
	}
	
	protected void setCloudcov_daily(double cloudcov_daily, int doy) {
		this.cloudcov_daily[doy] = cloudcov_daily;
	}

	protected double[] getWindspeed_daily() {
		return windspeed_daily;
	}
	
	protected double getWindspeed_daily(int doy) {
		return windspeed_daily[doy];
	}

	protected void setWindspeed_daily(double[] windspeed_daily) {
		if(windspeed_daily.length == Times.MAX_DAYS+1)
			this.windspeed_daily = windspeed_daily;
	}
	
	protected void setWindspeed_daily(double windspeed_daily, int doy) {
		this.windspeed_daily[doy] = windspeed_daily;
	}

	protected double[] getR_humidity_daily_daily() {
		return r_humidity_daily;
	}
	
	protected double getR_humidity_daily(int doy) {
		return r_humidity_daily[doy];
	}

	protected void setR_humidity_daily(double[] r_humidity_daily) {
		if(r_humidity_daily.length == Times.MAX_DAYS+1)
			this.r_humidity_daily = r_humidity_daily;
	}
	
	protected void setR_humidity_daily(double r_humidity_daily, int doy) {
		this.r_humidity_daily[doy] = r_humidity_daily;
	}

	protected double[] getTransmission_daily() {
		return transmission_daily;
	}
	
	protected double getTransmission_daily(int doy) {
		return transmission_daily[doy];
	}

	protected void setTransmission_daily(double[] transmission_daily) {
		if(transmission_daily.length == Times.MAX_DAYS+1)
			this.transmission_daily = transmission_daily;
	}
	
	protected void setTransmissiony_daily(double transmission_daily, int doy) {
		this.transmission_daily[doy] = transmission_daily;
	}
	
	protected double[] getSnow_density_daily() {
		return snow_density_daily;
	}
	
	protected double getSnow_density_daily(int doy) {
		return snow_density_daily[doy];
	}

	protected void setSnow_density_daily(double[] snow_density_daily) {
		if(snow_density_daily.length == Times.MAX_DAYS+1)
			this.snow_density_daily = snow_density_daily;
	}
	
	protected void setSnow_density_daily(double snow_density_daily, int doy) {
		this.snow_density_daily[doy] = snow_density_daily;
	}
	
	
	
	
	protected double[] getCloudcov() {
		return cloudcov;
	}
	
	protected double getCloudcov(Times.Months month) {
		return cloudcov[month.ordinal()];
	}

	protected void setCloudcov(double[] cloudcov) {
		if(cloudcov.length == Times.MAX_MONTHS)
			this.cloudcov = cloudcov;
	}
	
	protected void setCloudcov(double cloudcov, Times.Months month) {
		this.cloudcov[month.ordinal()] = cloudcov;
	}

	protected double[] getWindspeed() {
		return windspeed;
	}
	
	protected double getWindspeed(Times.Months month) {
		return windspeed[month.ordinal()];
	}

	protected void setWindspeed(double[] windspeed) {
		if(windspeed.length == Times.MAX_MONTHS)
			this.windspeed = windspeed;
	}
	
	protected void setWindspeed(double windspeed, Times.Months month) {
		this.windspeed[month.ordinal()] = windspeed;
	}

	protected double[] getR_humidity() {
		return r_humidity;
	}
	
	protected double getR_humidity(Times.Months month) {
		return r_humidity[month.ordinal()];
	}

	protected void setR_humidity(double[] r_humidity) {
		if(r_humidity.length == Times.MAX_MONTHS)
			this.r_humidity = r_humidity;
	}
	
	protected void setR_humidity(double r_humidity, Times.Months month) {
		this.r_humidity[month.ordinal()] = r_humidity;
	}
	
	protected double[] getTransmission() {
		return transmission;
	}
	
	protected double getTransmission(Times.Months month) {
		return transmission[month.ordinal()];
	}

	protected void setTransmission(double[] transmission) {
		if(transmission.length == Times.MAX_MONTHS)
			this.transmission = transmission;
	}
	
	protected void setTransmission(double transmission, Times.Months month) {
		this.transmission[month.ordinal()] = transmission;
	}

	protected double[] getSnow_density() {
		return snow_density;
	}

	protected void setSnow_density(double[] snow_density) {
		if(snow_density.length == Times.MAX_MONTHS)
			this.snow_density = snow_density;
	}
	
	protected void setSnow_density(double snow_density, Times.Months month) {
		this.snow_density[month.ordinal()] = snow_density;
	}
	
}
