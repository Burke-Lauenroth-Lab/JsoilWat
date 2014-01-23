package input;

import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import defines.Defines;
import times.SW_TIMES;
import times.Times;

public class SwcSetupIn {
	private boolean useSWCHistoryData;
	private int method;
	private SW_TIMES yr;
	private String filePrefix;
	private boolean data;
	
	private class SW_SOILWAT_HIST {
		private double swc[][];
		private double std_err[][];
		public SW_SOILWAT_HIST() {
			this.swc = new double[Times.MAX_DAYS][Defines.MAX_LAYERS];
			this.std_err = new double[Times.MAX_DAYS][Defines.MAX_LAYERS];
		}
	}
	
	public SwcSetupIn() {
		method = 0;
		yr = new SW_TIMES();
		filePrefix = "";
		data =false;
	}
	
	public void onRead(Path swcSetupIn) throws IOException {
		int nitems=4, lineno=0;
		//TODO: copy over soil temp from soils to soilwat stemp
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(swcSetupIn, StandardCharsets.UTF_8);
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				switch (lineno) {
				case 0:
					if(values.length > 1)
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Expected only one value for swc history use line.");
					try {
						this.useSWCHistoryData = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Could not convert swc history use line.");
					}
					break;
				case 1:
					if(values.length > 1)
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Expected only one value for swc file prefix.");
					this.filePrefix = values[0];
					break;
				case 2:
					if(values.length > 1)
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Expected only one value for swc history start year.");
					try {
						this.yr.setFirst(Integer.parseInt(values[0]));
					} catch(NumberFormatException e) {
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Could not convert swc first year line.");
					}
					break;
				case 3:
					if(values.length > 1)
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Expected only one value for method.");
					try {
						this.method = Integer.parseInt(values[0]);
						if(this.method < 1 || this.method >2)
							f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Invalid swc adjustment method.");
					} catch(NumberFormatException e) {
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Could not convert method line.");
					}
					break;
				default:
					System.out.println(line);
					if(lineno > nitems)
						f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Too many lines.");
					break;
				}
				lineno++;
			}
		}
		if(lineno < nitems)
			f.LogError(LogMode.LOGERROR, "SwcSetupIn onRead : Too few lines.");
		this.data = true;
	}
	public void onWrite(Path swcSetupIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Setup parameters for measured swc");
			lines.add("# Location: -");
			lines.add("#");
			lines.add(String.valueOf(this.useSWCHistoryData?1:0)+"\t\t"+"# 1=use swcdata history data file, 0= don't use");
			lines.add(this.filePrefix+"\t\t"+"# input data file prefix");
			lines.add(String.valueOf(this.yr.getFirst())+"\t\t"+"# first year of measurement data files");
			lines.add(String.valueOf(this.method)+"\t\t"+"# first year of measurement data files ");
			Files.write(swcSetupIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.LOGWARN, "SwcSetupIn : onWrite : No data.");
		}
	}
}
