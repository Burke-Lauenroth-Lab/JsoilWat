package soilwat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SW_FILES {
	public static class FILES_INPUT_DATA {
		public String ProjectDirectory = "";
		public String OutputDirectory = "";
		public String WeatherPathAndPrefix ="";
		public String FilesIn = "";
		public String YearsIn = "";
		public String LogFile = "";
		public String SiteParametersIn="";
		public String SoilsIn="";
		public String WeatherSetupIn="";
		public String MarkovProbabilityIn="";
		public String MarkovCovarianceIn="";
		public String CloudIn="";
		public String PlantProductivityIn="";
		public String EstablishmentIn="";
		public String SWCSetupIn="";
		public String OutputSetupIn="";
		private LogFileIn log;
		
		public FILES_INPUT_DATA(LogFileIn log) {
			this.log = log;
		}
		
		public void onClear() {
			ProjectDirectory = "";
			OutputDirectory = "";
			WeatherPathAndPrefix ="";
			FilesIn = "";
			YearsIn = "";
			LogFile = "";
			SiteParametersIn="";
			SoilsIn="";
			WeatherSetupIn="";
			MarkovProbabilityIn="";
			MarkovCovarianceIn="";
			CloudIn="";
			PlantProductivityIn="";
			EstablishmentIn="";
			SWCSetupIn="";
			OutputSetupIn="";
		}
		
		public static int LongestString(String... args) {
			int maxLength = 0;
		    //String longestString = null;
		    for (String s : args)
		    {
		      if (s.length() > maxLength)
		      {
		        maxLength = s.length();
		        //longestString = s;
		      }
		    }
		    return maxLength;
		}
		
		protected void onCreateDirectoriesAndFiles() throws Exception {
			LogFileIn f = log;

				if(Files.notExists(Paths.get(ProjectDirectory))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory).toString() + " : Directory does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,OutputDirectory))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,OutputDirectory));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,OutputDirectory).toString()+" : Directory does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,Paths.get(WeatherPathAndPrefix).getParent().toString()))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,Paths.get(WeatherPathAndPrefix).getParent().toString()));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,Paths.get(WeatherPathAndPrefix).getParent().toString()).toString()+" : Directory does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,FilesIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,FilesIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,FilesIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,FilesIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,YearsIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,YearsIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,YearsIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,YearsIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,SiteParametersIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,SiteParametersIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,SiteParametersIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,SiteParametersIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,SoilsIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,SoilsIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,SoilsIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,SoilsIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,WeatherSetupIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,WeatherSetupIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,WeatherSetupIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,WeatherSetupIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,MarkovProbabilityIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,MarkovProbabilityIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,MarkovProbabilityIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,MarkovProbabilityIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,MarkovCovarianceIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,MarkovCovarianceIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,MarkovCovarianceIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,MarkovCovarianceIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,CloudIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,CloudIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,CloudIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,CloudIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,PlantProductivityIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,PlantProductivityIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,PlantProductivityIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,PlantProductivityIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,EstablishmentIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,EstablishmentIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,EstablishmentIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,EstablishmentIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,SWCSetupIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,SWCSetupIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,SWCSetupIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,SWCSetupIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
				if(Files.notExists(Paths.get(ProjectDirectory,OutputSetupIn))) {
					try {
						Files.createDirectories(Paths.get(ProjectDirectory,OutputSetupIn).getParent());
						Files.createFile(Paths.get(ProjectDirectory,OutputSetupIn));
					} catch (IOException e) {
						f.LogError(LogFileIn.LogMode.ERROR, Paths.get(ProjectDirectory,OutputSetupIn).toString()+" : file does not exist and can not be created."+e.getMessage());
					}
				}
			
		}
		
		public void onRead(String swFiles) throws IOException{
			Path pFilesIn = Paths.get(swFiles);
			this.ProjectDirectory = pFilesIn.getParent().toString();
			this.FilesIn = pFilesIn.getFileName().toString();
			List<String> lines = SW_FILES.readFile(swFiles, getClass().getClassLoader());
			
			int nFileItemsRead = 0;
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					switch (nFileItemsRead) {
					case 0:
						this.YearsIn =  values[0];
						break;
					case 1:
						this.LogFile = values[0];
						break;
					case 2:
						this.SiteParametersIn = values[0];
						break;
					case 3:
						this.SoilsIn = values[0];
						break;
					case 4:
						this.WeatherSetupIn = values[0];
						break;
					case 5:
						this.WeatherPathAndPrefix = values[0];
						break;
					case 6:
						this.MarkovProbabilityIn = values[0];
						break;
					case 7:
						this.MarkovCovarianceIn = values[0];
						break;
					case 8:
						this.CloudIn = values[0];
						break;
					case 9:
						this.PlantProductivityIn = values[0];
						break;
					case 10:
						this.EstablishmentIn = values[0];
						break;
					case 11:
						this.SWCSetupIn = values[0];
						break;
					case 12:
						this.OutputDirectory = values[0];
						break;
					case 13:
						this.OutputSetupIn = values[0];
						break;
					}
					nFileItemsRead++;
				}
			}			
		}
		
		public void onWrite(String swFiles) throws Exception{
			Path pFilesIn = Paths.get(swFiles);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(pFilesIn, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			int length = LongestString(ProjectDirectory,OutputDirectory,WeatherPathAndPrefix,FilesIn,YearsIn,LogFile,SiteParametersIn,SoilsIn,WeatherSetupIn,MarkovProbabilityIn,MarkovCovarianceIn,CloudIn,PlantProductivityIn,EstablishmentIn,SWCSetupIn,OutputSetupIn);
			if(length == 0)
				length++;
			String format = "%-"+String.valueOf(length)+"s%s";
			String out = "";
			out+="# List of input files for SOILWAT v32\n";
			out+="# This is the first file read. Simulation information = \n";
			out+="\n";
			out+="# Model\n";
			out+=String.format(format, YearsIn,"\t# years for model operation\n");
			out+=String.format(format, LogFile, "\t# errors or important info (can also be stdout)\n");
			out+="\n";
			out+="#Site\n";
			out+=String.format(format, SiteParametersIn, "\t# site parameters\n");
			out+=String.format(format, SoilsIn, "\t# soil layer definitions\n");
			out+="\n";
			out+="#Weather\n";
			out+=String.format(format, WeatherSetupIn, "\t# weather parameters\n");
			out+=String.format(format, WeatherPathAndPrefix, "\t# data file containing historical weather (can include path)\n");
			out+=String.format(format, MarkovProbabilityIn, "\t# precip probs; required for markov weather\n");
			out+=String.format(format, MarkovCovarianceIn, "\t# covariance table required for markov weather\n");
			out+=String.format(format, CloudIn, "\t# general atmospheric params\n");
			out+="\n";
			out+="#Vegetation\n";
			out+=String.format(format, PlantProductivityIn, "\t# productivity values\n");
			out+=String.format(format, EstablishmentIn, "\t# plant establishment start file\n");
			out+="\n";
			out+="#SWC measurements\n";
			out+=String.format(format, SWCSetupIn, "\t# params for handling measured swc\n");
			out+="\n";
			out+="#Output\n";
			out+=String.format(format, OutputDirectory, "\t# 'relative' path for output files: / for same directory, or e.g., Output/\n");
			out+=String.format(format, OutputSetupIn, "\t# define output quantities\n");
			return out;
		}
	}

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
	public boolean verified;
	private LogFileIn log;
	
	/* Constructor */
	protected SW_FILES(LogFileIn log) {
		this.data = false;
		this.verified = false;
		this.log = log;
	}
	
	/***
	 * Helper function to read from file or resource contained within the Jar file.
	 * @param File
	 * @return
	 * @throws IOException
	 */
	public static List<String> readFile(String File, ClassLoader classLoader) throws IOException {
		List<String> lines = new ArrayList<String>();
		if(File.startsWith("resource:")) {
			File = File.replaceFirst("resource:", "");
			InputStream in = classLoader.getResourceAsStream(File);
			Scanner scanner = new Scanner(in);
			while (scanner.hasNextLine()) {
				lines.add(scanner.nextLine());
			}
			scanner.close();
		} else {
			lines = Files.readAllLines(Paths.get(File), StandardCharsets.UTF_8);
		}
		return lines;
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
	
	protected boolean onVerify() throws Exception {
		LogFileIn f = log;
		if(this.data) {
			//create directories if they do not exist
			List<String> messages = new ArrayList<String>();
			if(Files.notExists(pProjectDirectory))
				messages.add(pProjectDirectory.toString() + " : Directory does not exist.");
			if(Files.notExists(this.getOutputDirectory(true)))
				messages.add(this.getOutputDirectory(true).toString()+" : Directory does not exist.");
			if(Files.notExists(getWeatherPath(true)))
				messages.add( getWeatherPath(true).toString()+" : Directory does not exist.");
			if(Files.notExists(this.getFilesIn(true)))
				messages.add(this.getFilesIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getYearsIn(true)))
				messages.add(this.getYearsIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getSiteParametersIn(true)))
				messages.add(this.getSiteParametersIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getSoilsIn(true)))
				messages.add(this.getSoilsIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getWeatherSetupIn(true))) 
				messages.add(this.getWeatherSetupIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getMarkovProbabilityIn(true)))
				f.LogError(LogFileIn.LogMode.NOTE, this.getMarkovProbabilityIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getMarkovCovarianceIn(true)))
				f.LogError(LogFileIn.LogMode.NOTE, this.getMarkovCovarianceIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getCloudIn(true)))
				messages.add(this.getCloudIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getPlantProductivityIn(true)))
				messages.add(this.getPlantProductivityIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getEstablishmentIn(true)))
				messages.add(this.getEstablishmentIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getSWCSetupIn(true)))
				messages.add(this.getSWCSetupIn(true).toString()+" : file does not exist.");
			if(Files.notExists(this.getOutputSetupIn(true)))
				messages.add(this.getOutputSetupIn(true).toString()+" : file does not exist.");
			
			if(messages.size() > 0) {
				String message = "";
				for (String s : messages)
					message += s + "\n";
				
				f.LogError(LogFileIn.LogMode.NOTE, message);
			}
			this.verified = true;
			return true;
		} else {
			f.LogError(LogFileIn.LogMode.NOTE, "FilesIn : No data to verify.");
			this.verified = false;
			return false;
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
