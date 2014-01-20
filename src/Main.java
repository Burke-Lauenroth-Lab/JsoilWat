import input.CloudIn;
import input.FilesIn;
import input.LogFileIn;
import input.ProductionIn;
import input.WeatherIn;
import input.YearsIn;

import java.io.IOException;
import java.nio.file.Paths;


public class Main {
	public static void main(String[] args) {
		LogFileIn f = LogFileIn.getInstance();
		FilesIn files = new FilesIn();
		CloudIn cloud = new CloudIn();
		YearsIn years = new YearsIn();
		WeatherIn weather = new WeatherIn();
		ProductionIn prod = new ProductionIn();
		
		try {
			//Test Read
			files.onReadFilesIn("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestProject/files_v30.in");
			f.setLogFile(files.getLogFileIn(true));
			
			cloud.onReadCloudIn(files.getCloudIn(true));
			cloud.onVerify();
			years.onReadYearsIn(files.getYearsIn(true));
			years.onVerify();
			weather.setWeatherFolder(files.getWeatherPath(true));
			weather.setWeatherPrefix(files.getWeatherPrefix());
			weather.setLastYear(years.getEndYear());
			weather.onReadWeatherIn(files.getWeatherSetupIn(true));
			weather.onVerify(years.getEndYear());
			weather.onReadWeatherHistories();
			prod.onRead(files.getPlantProductivityIn(true));
			
			
			//Test Write
			files.setProjectDirectory(Paths.get("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestWrite/"));
			files.onCreateFiles();
			files.onVerify();
			files.onWriteFilesIn();
			cloud.onWriteCloudIn(files.getCloudIn(true));
			years.onWriteYearsIn(files.getYearsIn(true));
			weather.onWriteWeatherIn(files.getWeatherSetupIn(true));
			weather.setWeatherFolder(files.getWeatherPath(true));
			weather.onWriteWeatherHistories();
			prod.onWrite(files.getPlantProductivityIn(true));
		} catch (IOException e) {
			System.out.println("Problem: "+e.getMessage());
			e.printStackTrace();
		}
		System.out.println("done");
	}
}
