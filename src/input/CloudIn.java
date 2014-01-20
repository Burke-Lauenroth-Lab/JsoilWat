package input;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import times.Times;


public class CloudIn {
	private final String[] comments = new String[]{"# (site:	002_-119.415_39.046	), sky cover (sunrise-sunset),%,Climate Atlas of the US,http://cdo.ncdc.noaa.gov/cgi-bin/climaps/climaps.pl",
			"# Wind speed (m/s),Climate Atlas of the US,http://cdo.ncdc.noaa.gov/cgi-bin/climaps/climaps.pl",
			"# rel. Humidity (%),Climate Atlas of the US,http://cdo.ncdc.noaa.gov/cgi-bin/climaps/climaps.pl",
			"# transmissivity (rel), only used in petfunc, but falls out of the equations (a = trans * b, c = a / trans)",
			"# snow density (kg/m3): Brown, R. D. and P. W. Mote. 2009. The response of Northern Hemisphere snow cover to a changing climate. Journal of Climate 22:2124-2145."};
	private double[] cloudcov; /* monthly cloud cover (frac) */
	private double[] windspeed; /* windspeed (m/s) */
	private double[] r_humidity; /* relative humidity (%) */
	private double[] transmission; /* frac light transmitted by atmos. */ /* used as input for petfunc, but algorithm cancels it out */
	private double[] snow_density; /* snow density (kg/m3) */
	private boolean data;
	
	public double[] getCloudcov() {
		return cloudcov;
	}

	public void setCloudcov(double[] cloudcov) {
		this.cloudcov = cloudcov;
	}

	public double[] getWindspeed() {
		return windspeed;
	}

	public void setWindspeed(double[] windspeed) {
		this.windspeed = windspeed;
	}

	public double[] getR_humidity() {
		return r_humidity;
	}

	public void setR_humidity(double[] r_humidity) {
		this.r_humidity = r_humidity;
	}

	public double[] getSnow_density() {
		return snow_density;
	}

	public void setSnow_density(double[] snow_density) {
		this.snow_density = snow_density;
	}

	public CloudIn() {
		this.data = false;
		cloudcov = new double[Times.MAX_MONTHS];//{71,61,61,51,41,31,23,23,31,41,61,61};
		windspeed = new double[Times.MAX_MONTHS];//{1.3,2.9,3.3,3.8,3.8,3.8,3.3,3.3,2.9,1.3,1.3,1.3};
		r_humidity = new double[Times.MAX_MONTHS];//{61,61,61,51,51,51,41,41,51,51,61,61};
		transmission = new double[Times.MAX_MONTHS];//{1,1,1,1,1,1,1,1,1,1,1,1};
		snow_density = new double[Times.MAX_MONTHS];//{213.7,241.6,261,308,398.1,464.5,0,0,0,140,161.6,185.1};
	}
	public void onClear() {
		this.data = false;
		cloudcov = new double[Times.MAX_MONTHS];//{71,61,61,51,41,31,23,23,31,41,61,61};
		windspeed = new double[Times.MAX_MONTHS];//{1.3,2.9,3.3,3.8,3.8,3.8,3.3,3.3,2.9,1.3,1.3,1.3};
		r_humidity = new double[Times.MAX_MONTHS];//{61,61,61,51,51,51,41,41,51,51,61,61};
		transmission = new double[Times.MAX_MONTHS];//{1,1,1,1,1,1,1,1,1,1,1,1};
		snow_density = new double[Times.MAX_MONTHS];//{213.7,241.6,261,308,398.1,464.5,0,0,0,140,161.6,185.1};
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
	
	public void onReadCloudIn(Path CloudIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(CloudIn, StandardCharsets.UTF_8);

		if(lines.size() < 5)
			f.LogError(LogFileIn.LogMode.LOGERROR, "swCloud onRead : not enough lines.");
		try {
			for (int i=0;i<5;i++) {
				if(!lines.get(i).equals("[ \t]*")) {
					String[] values = lines.get(i).split("[ \t]+");
					if(values.length < 12)
						f.LogError(LogFileIn.LogMode.LOGERROR, "swCloud onRead : Line "+String.valueOf(i+1)+": Not enough values.");
					for (int j=0; j<12; j++) {
						switch (i) {
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
					}
				}
			}
		} catch(NumberFormatException e) {
			f.LogError(LogFileIn.LogMode.LOGERROR, "swCloud onRead : Could not convert string to number." + e.getMessage());
		}
		this.data = true;
	}
	public void onWriteCloudIn(Path CloudIn) throws IOException {
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
			f.LogError(LogFileIn.LogMode.LOGERROR, "swCloud onWrite : No Data to Write.");
		}
	}
}
