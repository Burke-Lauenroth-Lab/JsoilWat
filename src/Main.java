import input.CloudIn;
import input.FilesIn;
import input.LogFileIn;
import input.ProductionIn;
import input.WeatherHistoryIn;
import input.WeatherSetupIn;
import input.YearsIn;

import java.io.IOException;
import java.nio.file.Paths;


public class Main {
	public static void main(String[] args) {
		LogFileIn f = LogFileIn.getInstance();
		FilesIn files = new FilesIn();
		CloudIn cloud = new CloudIn();
		YearsIn years = new YearsIn();
		WeatherSetupIn weatherSetup = new WeatherSetupIn();
		WeatherHistoryIn weatherHistory = new WeatherHistoryIn();
		ProductionIn prod = new ProductionIn();
		
		try {
			//Test Read
			files.onReadFilesIn("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestProject/files_v30.in");
			f.setLogFile(files.getLogFileIn(true));
			
			cloud.onReadCloudIn(files.getCloudIn(true));
			cloud.onVerify();
			years.onReadYearsIn(files.getYearsIn(true));
			years.onVerify();
			
			weatherSetup.setLastYear(years.getEndYear());
			weatherSetup.onRead(files.getWeatherSetupIn(true));
			weatherSetup.onVerify(years.getEndYear());
			weatherHistory.onRead(files.getWeatherPath(true),files.getWeatherPrefix(),weatherSetup.getFirstYear(), weatherSetup.getLastYear());
			prod.onRead(files.getPlantProductivityIn(true));
			
			
			//Test Write
			files.setProjectDirectory(Paths.get("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestWrite/"));
			files.onCreateFiles();
			files.onVerify();
			files.onWriteFilesIn();
			cloud.onWriteCloudIn(files.getCloudIn(true));
			years.onWriteYearsIn(files.getYearsIn(true));
			weatherSetup.onWrite(files.getWeatherSetupIn(true));
			weatherHistory.onWrite(files.getWeatherPath(true),files.getWeatherPrefix());
			prod.onWrite(files.getPlantProductivityIn(true));
			
			//test weatherHistory functionality
			files.setProjectDirectory(Paths.get("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestProject/"));
			weatherHistory.onClear();
			weatherHistory.onRead(files.getWeatherHistoryFilePath(1951));
			weatherHistory.onRead(files.getWeatherHistoryFilePath(1962));
			files.setProjectDirectory(Paths.get("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestWrite/"));
			weatherHistory.onWrite(files.getWeatherPath(true),files.getWeatherPrefix());
			
		} catch (IOException e) {
			System.out.println("Problem: "+e.getMessage());
			e.printStackTrace();
		}
		System.out.println("done");
	}
}
