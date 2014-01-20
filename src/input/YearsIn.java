package input;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import times.Times;


public class YearsIn {
	/* Private Member Variables */
	private final int nLineStartYear=3, nLineEndYear=4, nLineFDOFY=5, nLineEDOEY=6;
	private final String[] comments = {"\t# starting year (but see weather and swc inputs)",
			"\t# ending year", "\t# first day of first year", "\t# ending day of last year",
			"\t# ending day of last year"};
	private int nLineIsNorth=7;
	private int nStartYear;
	private int nEndYear;
	//FDOFY
	private int nFirstDayOfFirstYear;
	//EDOEY
	private int nEndDayOfEndYear;
	private int nDayMiddle;
	private boolean bIsNorth;
	private boolean data;
	
	public YearsIn() {
		this.data = false;
	}
	
	public void onClear() {
		this.data = false;
		this.setStartYear(0);
		this.setEndYear(0);
		this.setFirstDayOfFirstYear(0);
		this.setEndDayOfEndYear(0);
		this.setIsNorth(true);
	}
	
	public void onSetDefault() {
		this.data = true;
		this.setStartYear(1982);
		this.setEndYear(1986);
		this.setFirstDayOfFirstYear(1);
		this.setEndDayOfEndYear(365);
		this.setIsNorth(true);
		this.nDayMiddle = (this.isIsNorth()) ? Times.DAYMID_NORTH : Times.DAYMID_SOUTH;
	}
	
	public boolean onVerify() {
		if(this.data) {
			LogFileIn f = LogFileIn.getInstance();
			if(this.nStartYear < 0)
				f.LogError(LogFileIn.LogMode.LOGFATAL, "swYears StartYear Negative.");
			if(this.nEndYear < 0)
				f.LogError(LogFileIn.LogMode.LOGFATAL, "swYears EndYear Negative.");
			if(this.nEndYear < this.nStartYear)
				f.LogError(LogFileIn.LogMode.LOGFATAL, "swYears StartYear > EndYear Negative.");
			if(this.nDayMiddle == 0)
				f.LogError(LogFileIn.LogMode.LOGFATAL, "swYears Day Middle not set.");
			return true;
		} else {
			return false;
		}
	}

	public int getStartYear() {
		return nStartYear;
	}
	public void setStartYear(int nStartYear) {
		this.nStartYear = nStartYear;
	}
	
	public int getEndYear() {
		return nEndYear;
	}
	public void setEndYear(int nEndYear) {
		this.nEndYear = nEndYear;
	}

	public int getFirstDayOfFirstYear() {
		return nFirstDayOfFirstYear;
	}
	public void setFirstDayOfFirstYear(int nFirstDayOfFirstYear) {
		this.nFirstDayOfFirstYear = nFirstDayOfFirstYear;
	}

	public int getEndDayOfEndYear() {
		return nEndDayOfEndYear;
	}
	public void setEndDayOfEndYear(int nEndDayOfEndYear) {
		this.nEndDayOfEndYear = nEndDayOfEndYear;
	}

	public boolean isIsNorth() {
		return bIsNorth;
	}
	public void setIsNorth(boolean bIsNorth) {
		this.bIsNorth = bIsNorth;
	}
	
	public void onWriteYearsIn(Path YearsIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Model time definition file");
			lines.add("# Location: P1");
			lines.add("");
			lines.add(String.valueOf(this.nStartYear) + comments[0]);
			lines.add(String.valueOf(this.nEndYear) + comments[1]);
			lines.add(String.valueOf(this.nFirstDayOfFirstYear) + comments[2]);
			lines.add(String.valueOf(this.nEndDayOfEndYear) + comments[3]);
			if(this.isIsNorth())
				lines.add("N" + comments[4]);
			else
				lines.add("S" + comments[4]);
			Files.write(YearsIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.LOGERROR, "swYears onWriteYears : No Data.");
		}
	}
	public void onReadYearsIn(Path YearsIn) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(YearsIn, StandardCharsets.UTF_8);
		boolean FirstEnd = false, endString = false; //used if FDOFY or EDOEY are not present
		boolean fhemi=false,fenddy=false,fstartdy=false;
		int d=0;
		if(lines.isEmpty() | lines.size() < 5 ) {
			f.LogError(LogFileIn.LogMode.LOGFATAL, "swYears onReadYearsIn Empty or not enough Lines.");
		}
		try {
			this.nStartYear = Integer.parseInt(lines.get(nLineStartYear).split("[ \t]+")[0]);
			this.nEndYear = Integer.parseInt(lines.get(nLineEndYear).split("[ \t]+")[0]);
			if(lines.size() > 5) {
				if(!(lines.get(nLineEndYear+1).split("[ \t]")[0] == "N" | lines.get(nLineEndYear+1).split("[ \t]+")[0] == "S")) {
					this.nFirstDayOfFirstYear = Integer.parseInt(lines.get(nLineFDOFY).split("[ \t]+")[0]);
					fstartdy=true;
					if(lines.get(nLineEDOEY).split("[ \t]+")[0].equals("end")) {
						d = Integer.parseInt(lines.get(nLineEDOEY).split("[ \t]+")[0]);
					} else {
						endString=true;
					}
					fenddy=true;
				} else {
					FirstEnd = true;
					fhemi = true;
				}
			}
		} catch (NumberFormatException e) {
			f.LogError(LogFileIn.LogMode.LOGERROR, "swYears onReadYearsIn could not read integer: "+e.getMessage());
		}
		if(lines.size() > 5) {
			if(FirstEnd)
				nLineIsNorth=nLineEndYear+1;
			if(lines.get(nLineIsNorth).split("[ \t]+")[0].equals("N")) {
				this.bIsNorth = true;
				fhemi = true;
			} else if(lines.get(nLineIsNorth).split("[ \t]+")[0].equals("s")) {
				this.bIsNorth = false;
				fhemi = true;
			} else {
				f.LogError(LogFileIn.LogMode.LOGWARN, "swYears onReadYearsIn isNorth format wrong: Set to TRUE");
				this.bIsNorth = true;
			}
		}
		if(!(fstartdy && fenddy && fhemi)) {
			String message = "\n Not found in "+YearsIn.toString()+":\n";
			if(!fstartdy) {
				message += "\tStart Day - using 1\n";
				this.nFirstDayOfFirstYear = 1;
			} else if(!fenddy) {
				message += "\tEnd Day - using 'end'\n";
				endString = true;
			} else if(!fhemi) {
				message += "\tHemisphere - using 'N'\n";
				this.bIsNorth = true;
			}
			f.LogError(LogFileIn.LogMode.LOGWARN, "swYears onReadYearsIn : "+message);
		}
		this.nFirstDayOfFirstYear += ((this.isIsNorth()) ? Times.DAYFIRST_NORTH : Times.DAYFIRST_SOUTH) - 1;
		if(endString)
			this.nEndDayOfEndYear = (this.isIsNorth()) ? Times.Time_get_lastdoy_y(this.nEndYear) : Times.DAYLAST_SOUTH;
		else {
			this.nEndDayOfEndYear = (d<365) ? d : Times.Time_get_lastdoy_y(this.nEndYear);
		}
		this.nDayMiddle = (this.isIsNorth()) ? Times.DAYMID_NORTH : Times.DAYMID_SOUTH;
		this.data = true;
	}
}
