import soilwat.InputData;
import soilwat.SW_CONTROL;

public class Main {
	public static void main(String[] args) {
		InputData test = new InputData();
		test.onSetDefaults();
		test.filesIn.ProjectDirectory = "/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestProject/";
		SW_CONTROL sim = new SW_CONTROL();
		sim.onSetInput(test);
		//sim.onReadInputs("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestProject/files_v30.in");
		sim.onStartModel(true);
	}
	/*public static void main(String[] args) {
		LogFileIn f = LogFileIn.getInstance();
		FilesIn files = new FilesIn();
		CloudIn cloud = new CloudIn();
		YearsIn years = new YearsIn();
		WeatherSetupIn weatherSetup = new WeatherSetupIn();
		WeatherHistoryIn weatherHistory = new WeatherHistoryIn();
		ProductionIn prod = new ProductionIn();
		SiteIn site = new SiteIn();
		SoilsIn soils = new SoilsIn();
		OutputSetupIn outSetup = new OutputSetupIn();
		SwcSetupIn swcsetup = new SwcSetupIn();
		EstabIn estab = new EstabIn();
		
		try {
			//Test Read
			files.onReadFilesIn("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestProject/files_v30.in");
			f.setLogFile(files.getLogFileIn(true));
			
			//Read Data
			cloud.onRead(files.getCloudIn(true));
			years.onReadYearsIn(files.getYearsIn(true));
			weatherSetup.setLastYear(years.getEndYear());
			weatherSetup.onRead(files.getWeatherSetupIn(true));
			weatherHistory.onRead(files.getWeatherPath(true),files.getWeatherPrefix(),weatherSetup.getFirstYear(), weatherSetup.getLastYear());
			prod.onRead(files.getPlantProductivityIn(true));
			site.onRead(files.getSiteParametersIn(true));
			soils.onRead(files.getSoilsIn(true));
			outSetup.onRead(files.getOutputSetupIn(true), files.getOutputDirectory(true), site.getDeepdrain());
			swcsetup.onRead(files.getSWCSetupIn(true));
			estab.onRead(files.getEstablishmentIn(true), files.getProjectDirectory());
			
			//Verify Data
			cloud.onVerify();
			years.onVerify();
			weatherSetup.onVerify(years.getEndYear());
			prod.onVerify();
			site.onVerify();
			soils.onVerify(site.getDeepdrain());
			
			//Write
			files.setProjectDirectory(Paths.get("/home/ryan/workspace/Rsoilwat_v31/tests/soilwat_v31_TestWrite/"));
			files.onCreateFiles();
			files.onVerify();
			files.onWriteFilesIn();
			cloud.onWrite(files.getCloudIn(true));
			years.onWriteYearsIn(files.getYearsIn(true));
			weatherSetup.onWrite(files.getWeatherSetupIn(true));
			weatherHistory.onWrite(files.getWeatherPath(true),files.getWeatherPrefix());
			prod.onWrite(files.getPlantProductivityIn(true));
			site.onWrite(files.getSiteParametersIn(true));
			soils.onWrite(files.getSoilsIn(true));
			outSetup.onWrite(files.getOutputSetupIn(true));
			swcsetup.onWrite(files.getSWCSetupIn(true));
			estab.onWrite(files.getEstablishmentIn(true), files.getProjectDirectory());
			
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
	}*/
}
