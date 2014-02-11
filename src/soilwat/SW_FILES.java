package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SW_FILES {
	public static class FILES_INPUT_DATA {
		public String ProjectDirectory;
		public String OutputDirectory;
		public String WeatherPathAndPrefix;
		public String FilesIn;
		public String YearsIn;
		public String LogFile;
		public String SiteParametersIn;
		public String SoilsIn;
		public String WeatherSetupIn;
		public String MarkovProbabilityIn;
		public String MarkovCovarianceIn;
		public String CloudIn;
		public String PlantProductivityIn;
		public String EstablishmentIn;
		public String SWCSetupIn;
		public String OutputSetupIn;
	}
	/* Private Member Values*/
	//Line index Values
	private final static int nYearsIn=4, nLogFile=5, nSiteParameters=8, nSoils=9, nWeatherSetup=12, nWeatherPath=13, nWeatherPrefix=13,
			nMarkovProbability=14,nMarkovCovariance=15,nCloud=16,nPlantProductivity=19,nEstablishment=20,nSWCSetup=23, nOutputDirectory=26,
			nOutputSetup=27;
	//Path to Project Directory
	private Path pProjectDirectory;
	//'relative' path for output files: / for same directory, or e.g., Output/
	private Path pOutputDirectory;
	//Weather Data Path
	private Path pWeatherPath;
	//Weather Prefix
	private String sWeatherPrefix;
	//File with the list of File Paths to SoilWat configuration files
	private Path pFilesIn;
	//Years for model operation
	private Path pYearsIn;
	//errors or important info
	private Path pLogFile;
	//Site Parameters
	private Path pSiteParametersIn;
	//Soil Layer definitions
	private Path pSoilsIn;
	//Weather parameters
	private Path pWeatherSetupIn;
	//precip probs; required for markov weather
	private Path pMarkovProbabilityIn;
	//covariance table required for markov weather
	private Path pMarkovCovarianceIn;
	//General Atmospheric params
	private Path pCloudIn;
	//Productivity values
	private Path pPlantProductivityIn;
	//Plant Establishment start file
	private Path pEstablishmentIn;
	//params for handling measured swc
	private Path pSWCSetupIn;
	//define output quantities
	private Path pOutputSetupIn;
	private boolean data;
	private boolean verified;
	
	/* Constructor */
	protected SW_FILES() {
		this.data = false;
		this.verified = false;
	}
	protected void onSetDefault() {
		this.pProjectDirectory = Paths.get("");//should get the current working directory
		this.pOutputDirectory = Paths.get("Output/");
		this.pWeatherPath = Paths.get("Input/data_39.0625_-119.4375/");
		this.sWeatherPrefix = "weath";
		this.pFilesIn = Paths.get("files_v30.in");
		this.pYearsIn = Paths.get("Input/years.in");
		this.pLogFile = Paths.get("Output/logfile.log");
		this.pSiteParametersIn = Paths.get("Input/siteparam_v26.in");
		this.pSoilsIn = Paths.get("Input/soils_v23.in");
		this.pWeatherSetupIn = Paths.get("Input/weathsetup_v20.in");
		this.pMarkovProbabilityIn = Paths.get("Input/data_39.0625_-119.4375/mkv_prob.in");
		this.pMarkovCovarianceIn = Paths.get("Input/data_39.0625_-119.4375/mkv_covar.in");
		this.pCloudIn = Paths.get("Input/data_39.0625_-119.4375/cloud_v20.in");
		this.pPlantProductivityIn = Paths.get("Input/sbe_prod_v21.in");
		this.pEstablishmentIn = Paths.get("Input/estab.in");
		this.pSWCSetupIn = Paths.get("Input/swcsetup.in");
		this.pOutputSetupIn = Paths.get("Input/outsetup_v27.in");
		this.data = true;
		this.verified = false;
	}
	
	protected void onClear() {
		this.data = false;
		this.verified = false;
		this.pProjectDirectory = Paths.get("/");
		this.pOutputDirectory = Paths.get("/");
		this.pWeatherPath = Paths.get("/");
		this.sWeatherPrefix = "/";
		this.pFilesIn = Paths.get("/");
		this.pYearsIn = Paths.get("/");
		this.pLogFile = Paths.get("/");
		this.pSiteParametersIn = Paths.get("/");
		this.pSoilsIn = Paths.get("/");
		this.pWeatherSetupIn = Paths.get("/");
		this.pMarkovProbabilityIn = Paths.get("/");
		this.pMarkovCovarianceIn = Paths.get("/");
		this.pCloudIn = Paths.get("/");
		this.pPlantProductivityIn = Paths.get("/");
		this.pEstablishmentIn = Paths.get("/");
		this.pSWCSetupIn = Paths.get("/");
		this.pOutputSetupIn = Paths.get("/");
	}
	
	protected boolean onVerify() {
		if(this.data) {
			LogFileIn f = LogFileIn.getInstance();
			//create directories if they do not exist
			if(Files.notExists(pProjectDirectory))
				f.LogError(LogFileIn.LogMode.ERROR, pProjectDirectory.toString() + " : Directory does not exist.");
			if(Files.notExists(this.getOutputDirectory(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getOutputDirectory(true).toString()+" : Directory does not exist.");
			if(Files.notExists(getWeatherPath(true)))
				f.LogError(LogFileIn.LogMode.ERROR, getWeatherPath(true).toString()+" : Directory does not exist.");
			if(Files.notExists(this.getFilesIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getFilesIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getYearsIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getYearsIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getSiteParametersIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getSiteParametersIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getSoilsIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getSoilsIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getWeatherSetupIn(true))) 
				f.LogError(LogFileIn.LogMode.ERROR, this.getWeatherSetupIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getMarkovProbabilityIn(true)))
				f.LogError(LogFileIn.LogMode.NOTE, this.getMarkovProbabilityIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getMarkovCovarianceIn(true)))
				f.LogError(LogFileIn.LogMode.NOTE, this.getMarkovCovarianceIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getCloudIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getCloudIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getPlantProductivityIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getPlantProductivityIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getEstablishmentIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getEstablishmentIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getSWCSetupIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getSWCSetupIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getOutputSetupIn(true)))
				f.LogError(LogFileIn.LogMode.ERROR, this.getOutputSetupIn(true).toString()+" : file does not exist.");
			this.verified = true;
			return true;
		} else {
			System.out.println("FilesIn : No data to verify.");
			this.verified = false;
			return false;
		}
	}
	
	protected void onCreateFiles() {
		LogFileIn f = LogFileIn.getInstance();
		if(this.data) {
			if(Files.notExists(pProjectDirectory)) {
				try {
					Files.createDirectories(pProjectDirectory);
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, pProjectDirectory.toString() + " : Directory does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getOutputDirectory(true))) {
				try {
					Files.createDirectories(this.getOutputDirectory(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getOutputDirectory(true).toString()+" : Directory does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(getWeatherPath(true))) {
				try {
					Files.createDirectories(getWeatherPath(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, getWeatherPath(true).toString()+" : Directory does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getFilesIn(true))) {
				try {
					Files.createFile(this.getFilesIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getFilesIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getYearsIn(true))) {
				try {
					Files.createFile(this.getYearsIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getYearsIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getSiteParametersIn(true))) {
				try {
					Files.createFile(this.getSiteParametersIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getSiteParametersIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getSoilsIn(true))) {
				try {
					Files.createFile(this.getSoilsIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getSoilsIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getWeatherSetupIn(true))) {
				try {
					Files.createFile(this.getWeatherSetupIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getWeatherSetupIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getMarkovProbabilityIn(true))) {
				try {
					Files.createFile(this.getMarkovProbabilityIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getMarkovProbabilityIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getMarkovCovarianceIn(true))) {
				try {
					Files.createFile(this.getMarkovCovarianceIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getMarkovCovarianceIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getCloudIn(true))) {
				try {
					Files.createFile(this.getCloudIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getCloudIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getPlantProductivityIn(true))) {
				try {
					Files.createFile(this.getPlantProductivityIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getPlantProductivityIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getEstablishmentIn(true))) {
				try {
					Files.createFile(this.getEstablishmentIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getEstablishmentIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getSWCSetupIn(true))) {
				try {
					Files.createFile(this.getSWCSetupIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getSWCSetupIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getSWCSetupIn(true))) {
				try {
					Files.createFile(this.getSWCSetupIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getSWCSetupIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
			if(Files.notExists(this.getOutputSetupIn(true))) {
				try {
					Files.createFile(this.getOutputSetupIn(true));
				} catch (IOException e) {
					f.LogError(LogFileIn.LogMode.ERROR, this.getOutputSetupIn(true).toString()+" : file does not exist and can not be created."+e.getMessage());
				}
			}
		} else {
			f.LogError(LogFileIn.LogMode.ERROR, "FilesIn CreateFiles : No Data To Create File Structure.");
		}
	}
	protected void onSetInput(FILES_INPUT_DATA filesIn) {
		this.pProjectDirectory = Paths.get(filesIn.ProjectDirectory);
		this.pFilesIn = Paths.get(filesIn.FilesIn);
		this.pYearsIn =  Paths.get(filesIn.YearsIn);
		this.pLogFile = Paths.get(filesIn.LogFile);
		this.pSiteParametersIn = Paths.get(filesIn.SiteParametersIn);
		this.pSoilsIn = Paths.get(filesIn.SoilsIn);
		this.pWeatherSetupIn = Paths.get(filesIn.WeatherSetupIn);
		this.pWeatherPath = Paths.get(filesIn.WeatherPathAndPrefix).getParent();
		this.sWeatherPrefix = Paths.get(filesIn.WeatherPathAndPrefix).getFileName().toString();
		this.pMarkovProbabilityIn = Paths.get(filesIn.MarkovProbabilityIn);
		this.pMarkovCovarianceIn = Paths.get(filesIn.MarkovCovarianceIn);
		this.pCloudIn = Paths.get(filesIn.CloudIn);
		this.pPlantProductivityIn = Paths.get(filesIn.PlantProductivityIn);
		this.pEstablishmentIn = Paths.get(filesIn.EstablishmentIn);
		this.pSWCSetupIn = Paths.get(filesIn.SWCSetupIn);
		this.pOutputDirectory = Paths.get(filesIn.OutputDirectory);
		this.pOutputSetupIn = Paths.get(filesIn.OutputSetupIn);
		this.data = true;
	}
	protected void onGetInput(FILES_INPUT_DATA filesIn) {
		filesIn.ProjectDirectory = this.pProjectDirectory.toString();
		filesIn.FilesIn = this.pFilesIn.toString();
		filesIn.YearsIn = this.pYearsIn.toString();
		filesIn.LogFile = this.pLogFile.toString();
		filesIn.SiteParametersIn = this.pSiteParametersIn.toString();
		filesIn.SoilsIn = this.pSoilsIn.toString();
		filesIn.WeatherSetupIn = this.pWeatherSetupIn.toString();
		filesIn.WeatherPathAndPrefix = this.pWeatherPath.toString()+ "/" + this.sWeatherPrefix;
		filesIn.MarkovProbabilityIn = this.pMarkovProbabilityIn.toString();
		filesIn.MarkovCovarianceIn = this.pMarkovCovarianceIn.toString();
		filesIn.CloudIn = this.pCloudIn.toString();
		filesIn.PlantProductivityIn = this.pPlantProductivityIn.toString();
		filesIn.EstablishmentIn = this.pEstablishmentIn.toString();
		filesIn.SWCSetupIn = this.pSWCSetupIn.toString();
		filesIn.OutputDirectory = this.pOutputDirectory.toString();
		filesIn.OutputSetupIn = this.pOutputSetupIn.toString();
	}
	/* Public Functions */
	protected void onRead(String swFiles) throws IOException{
		this.pFilesIn = Paths.get(swFiles);
		this.pProjectDirectory = this.pFilesIn.getParent();
		this.pFilesIn = this.pFilesIn.getFileName();
		List<String> lines = Files.readAllLines(getFilesIn(true), StandardCharsets.UTF_8);
		
		this.pYearsIn =  Paths.get(lines.get(nYearsIn).split("[ \t]+")[0]);
		this.pLogFile = Paths.get(lines.get(nLogFile).split("[ \t]+")[0]);
		this.pSiteParametersIn = Paths.get(lines.get(nSiteParameters).split("[ \t]+")[0]);
		this.pSoilsIn = Paths.get(lines.get(nSoils).split("[ \t]+")[0]);
		this.pWeatherSetupIn = Paths.get(lines.get(nWeatherSetup).split("[ \t]+")[0]);
		this.pWeatherPath = Paths.get(lines.get(nWeatherPath).split("[ \t]+")[0]).getParent();
		this.sWeatherPrefix = Paths.get(lines.get(nWeatherPrefix).split("[ \t]+")[0]).getFileName().toString();
		this.pMarkovProbabilityIn = Paths.get(lines.get(nMarkovProbability).split("[ \t]+")[0]);
		this.pMarkovCovarianceIn = Paths.get(lines.get(nMarkovCovariance).split("[ \t]+")[0]);
		this.pCloudIn = Paths.get(lines.get(nCloud).split("[ \t]+")[0]);
		this.pPlantProductivityIn = Paths.get(lines.get(nPlantProductivity).split("[ \t]+")[0]);
		this.pEstablishmentIn = Paths.get(lines.get(nEstablishment).split("[ \t]+")[0]);
		this.pSWCSetupIn = Paths.get(lines.get(nSWCSetup).split("[ \t]+")[0]);
		this.pOutputDirectory = Paths.get(lines.get(nOutputDirectory).split("[ \t]+")[0]);
		this.pOutputSetupIn = Paths.get(lines.get(nOutputSetup).split("[ \t]+")[0]);
		this.data = true;
	}
	
	protected void onWrite() throws IOException{
		if(this.data && this.verified) {
			List<String> lines = new ArrayList<String>();
			lines.add("# List of input files for SOILWAT v32");
			lines.add("# This is the first file read. Simulation information = ");
			lines.add("");
			lines.add("# Model");
			lines.add(this.pYearsIn.toString() + "\t# years for model operation");
			lines.add(this.pLogFile.toString() + "\t# errors or important info (can also be stdout)");
			lines.add("");
			lines.add("#Site");
			lines.add(this.pSiteParametersIn.toString() + "\t# site parameters");
			lines.add(this.pSoilsIn.toString() + "\t# soil layer definitions");
			lines.add("");
			lines.add("#Weather");
			lines.add(this.pWeatherSetupIn.toString() + "\t# weather parameters");
			lines.add(this.pWeatherPath.toString()+ "/" + this.sWeatherPrefix + "\t# data file containing historical weather (can include path)");
			lines.add(this.pMarkovProbabilityIn.toString() + "\t# precip probs; required for markov weather");
			lines.add(this.pMarkovCovarianceIn.toString() + "\t# covariance table required for markov weather");
			lines.add(this.pCloudIn.toString() + "\t# general atmospheric params");
			lines.add("");
			lines.add("#Vegetation");
			lines.add(this.pPlantProductivityIn.toString() + "\t# productivity values");
			lines.add(this.pEstablishmentIn.toString() + "\t# plant establishment start file");
			lines.add("");
			lines.add("#SWC measurements");
			lines.add(this.pSWCSetupIn.toString() + "\t# params for handling measured swc");
			lines.add("");
			lines.add("#Output");
			lines.add(this.pOutputDirectory + "\t# 'relative' path for output files: / for same directory, or e.g., Output/");
			lines.add(this.pOutputSetupIn.toString() + "\t# define output quantities");
			Files.write(getFilesIn(true), lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.WARN, "FilesIn onWrite : No Data to Write or not Verified.");
		}
	}
	
	/* GETTERS AND SETTERS */
	protected Path getProjectDirectory() {
		return this.pProjectDirectory;
	}
	protected void setProjectDirectory(Path ProjectDirectory) {
		this.pProjectDirectory = ProjectDirectory;
	}
	
	protected Path getOutputDirectory(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pOutputDirectory);
		else
			return this.pOutputDirectory;
	}
	protected void setOutputDirectory(Path OutputDirectory) {
		this.pOutputDirectory = OutputDirectory;
	}
	
	protected Path getWeatherPath(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pWeatherPath);
		else
			return this.pWeatherPath;
	}
	protected Path getWeatherHistoryFilePath(int year) {
		return getWeatherPath(true).resolve(getWeatherPrefix()+"."+String.valueOf(year));
	}
	protected void setWeatherPath(Path WeatherPath) {
		this.pWeatherPath = WeatherPath;
	}
	protected String getWeatherPrefix() {
		return sWeatherPrefix;
	}
	protected void setWeatherPrefix(String sWeatherPrefix) {
		this.sWeatherPrefix = sWeatherPrefix;
	}
	protected Path getFilesIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pFilesIn);
		else
			return this.pFilesIn;
	}
	protected void setFilesIn(Path FilesIn) {
		this.pFilesIn = FilesIn;
	}
	protected Path getYearsIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pYearsIn);
		else
			return this.pYearsIn;
	}
	protected void setYearsIn(Path YearsIn) {
		this.pYearsIn = YearsIn;
	}
	protected Path getLogFileIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pLogFile);
		else
			return this.pLogFile;
	}
	protected void setLogFileIn(Path LogFileIn) {
		this.pLogFile = LogFileIn;
	}
	protected Path getSiteParametersIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pSiteParametersIn);
		else
			return this.pSiteParametersIn;
	}
	protected void setSiteParametersIn(Path SiteParametersIn) {
		this.pSiteParametersIn = SiteParametersIn;
	}
	protected Path getSoilsIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pSoilsIn);
		else
			return this.pSoilsIn;
	}
	protected void setSoilsIn(Path SoilsIn) {
		this.pSoilsIn = SoilsIn;
	}
	protected Path getWeatherSetupIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pWeatherSetupIn);
		else
			return this.pWeatherSetupIn;
	}
	protected void setWeatherSetupIn(Path WeatherSetupIn) {
		this.pWeatherSetupIn = WeatherSetupIn;
	}
	protected Path getMarkovProbabilityIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pMarkovProbabilityIn);
		else
			return this.pMarkovProbabilityIn;
	}
	protected void setMarkovProbabilityIn(Path MarkovProbabilityIn) {
		this.pMarkovProbabilityIn = MarkovProbabilityIn;
	}
	protected Path getMarkovCovarianceIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pMarkovCovarianceIn);
		else
			return this.pMarkovCovarianceIn;
	}
	protected void setMarkovCovarianceIn(Path MarkovCovarianceIn) {
		this.pMarkovCovarianceIn = MarkovCovarianceIn;
	}
	protected Path getCloudIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pCloudIn);
		else
			return this.pCloudIn;
	}
	protected void setCloudIn(Path CloudIn) {
		this.pCloudIn = CloudIn;
	}
	protected Path getPlantProductivityIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pPlantProductivityIn);
		else
			return this.pPlantProductivityIn;
	}
	protected void setPlantProductivityIn(Path PlantProductivityIn) {
		this.pPlantProductivityIn = PlantProductivityIn;
	}
	protected Path getEstablishmentIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pEstablishmentIn);
		else
			return this.pEstablishmentIn;
	}
	protected void setEstablishmentIn(Path EstablishmentIn) {
		this.pEstablishmentIn = EstablishmentIn;
	}
	protected Path getSWCSetupIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pSWCSetupIn);
		else
			return this.pSWCSetupIn;
	}
	protected void setSWCSetupIn(Path SWCSetupIn) {
		this.pSWCSetupIn = SWCSetupIn;
	}
	protected Path getOutputSetupIn(boolean fullPath) {
		if(fullPath)
			return this.pProjectDirectory.resolve(this.pOutputSetupIn);
		else
			return this.pOutputSetupIn;
	}
	protected void setOutputSetupIn(Path OutputSetupIn) {
		this.pOutputSetupIn = OutputSetupIn;
	}
}
