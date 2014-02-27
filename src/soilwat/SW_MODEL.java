package soilwat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SW_MODEL {
	public static class MODEL_INPUT_DATA {
		public int startYear,
		endYear,
		startstart,
		endend;
		public boolean isNorth;
	}
	/* Private Member Variables */
	private int /* controlling dates for model run */
	startyr, /* beginning year for model run */
	endyr, /* ending year for model run */
	startstart, /* startday in start year */
	endend, /* end day in end year */
	daymid, /* mid year depends on hemisphere */
	/* current year dates */
	firstdoy, /* start day for this year */
	lastdoy, /* 366 if leapyear or endend if endyr */
	doy, week, month, year; /* current model time */
	/* however, week and month are base0 because they
	 * are used as array indices, so take care.
	 * doy and year are base1. */

	/* first day of new week/month is checked for
	 * printing and summing weekly/monthly values */
	private boolean newweek, newmonth, newyear;
	private boolean isnorth;
	
	private int _prevweek, /* check for new week */
	_prevmonth, /* check for new month */
	//_prevyear, /* check for new year */
	_notime = 0xffff; /* init value for _prev* */
	
	private final int nLineStartYear=3, nLineEndYear=4, nLineFDOFY=5, nLineEDOEY=6;
	private final String[] comments = {"\t# starting year (but see weather and swc inputs)",
			"\t# ending year", "\t# first day of first year", "\t# ending day of last year",
			"\t# ending day of last year"};
	private int nLineIsNorth=7;
	private boolean data;
	
	protected SW_MODEL() {
		Times.Time_init();
		this.newweek=this.newmonth=this.newyear=false;
		this.data = false;
	}
	
	protected void onClear() {
		this.data = false;
		//this.setStartYear(0);
		//this.setEndYear(0);
		//this.setFirstDayOfFirstYear(0);
		//this.setEndDayOfEndYear(0);
		//this.setIsNorth(true);
	}
	
	/*public void onSetDefault() {
		this.data = true;
		this.setStartYear(1982);
		this.setEndYear(1986);
		this.setFirstDayOfFirstYear(1);
		this.setEndDayOfEndYear(365);
		this.setIsNorth(true);
		this.daymid = (this.isIsNorth()) ? Times.DAYMID_NORTH : Times.DAYMID_SOUTH;
	}*/
	
	protected boolean onVerify() throws Exception {
		if(this.data) {
			LogFileIn f = LogFileIn.getInstance();
			if(this.startyr < 0)
				f.LogError(LogFileIn.LogMode.FATAL, "swYears StartYear Negative.");
			if(this.endyr < 0)
				f.LogError(LogFileIn.LogMode.FATAL, "swYears EndYear Negative.");
			if(this.endyr < this.startyr)
				f.LogError(LogFileIn.LogMode.FATAL, "swYears StartYear > EndYear Negative.");
			if(this.daymid == 0)
				f.LogError(LogFileIn.LogMode.FATAL, "swYears Day Middle not set.");
			return true;
		} else {
			return false;
		}
	}
	
	protected void SW_MDL_new_year() {
		_prevweek=_prevmonth/*=_prevyear*/=_notime;
		int year = this.year;
		Times.Time_new_year(year);
		this.firstdoy = (year == this.year) ? this.startstart : 1;
		this.lastdoy = (year == this.endyr) ? this.endend : Times.Time_lastDOY();
	}
	
	protected void SW_MDL_new_day() {
		/* =================================================== */
		/* sets the output period elements of SW_Model
		 * based on the current day.
		 */
		this.month = Times.doy2month(this.doy);
		this.week = Times.doy2week(this.doy);
		
		/* in this case, we've finished the daily loop and are about
		 * to flush the output */
		if(this.doy > this.lastdoy) {
			this.newyear = this.newmonth = this.newweek = true;
			return;
		}
		if(this.month != _prevmonth) {
			this.newmonth = (_prevmonth != _notime) ? true : false;
			this._prevmonth = this.month;
		} else
			this.newmonth = false;
		
		/*  if (SW_Model.week != _prevweek || SW_Model.month == NoMonth) { */
		if(this.week != this._prevweek) {
			this.newweek = (_prevweek != _notime) ? true : false;
			_prevweek = this.week;
		} else
			this.newweek = false;
	}
	protected void onSetInput(MODEL_INPUT_DATA yearsIn) {
		this.startyr = yearsIn.startYear;
		this.endyr = yearsIn.endYear;
		this.startstart = yearsIn.startstart;
		this.endend = yearsIn.endend;
		this.isnorth = yearsIn.isNorth;
		this.daymid = (this.isIsNorth()) ? Times.DAYMID_NORTH : Times.DAYMID_SOUTH;
		this.data = true;
	}
	protected void onGetInput(MODEL_INPUT_DATA yearsIn) {
		yearsIn.startYear = this.startyr;
		yearsIn.endYear = this.endyr;
		yearsIn.startstart=this.startstart;
		yearsIn.endend = this.endend;
		yearsIn.isNorth = this.isnorth;
	}
	protected void onRead(Path YearsIn) throws Exception {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(YearsIn, StandardCharsets.UTF_8);
		boolean FirstEnd = false, endString = false; //used if FDOFY or EDOEY are not present
		boolean fhemi=false,fenddy=false,fstartdy=false;
		int d=0;
		if(lines.isEmpty() | lines.size() < 5 ) {
			f.LogError(LogFileIn.LogMode.FATAL, "swYears onReadYearsIn Empty or not enough Lines.");
		}
		try {
			this.startyr = Integer.parseInt(lines.get(nLineStartYear).split("[ \t]+")[0]);
			this.endyr = Integer.parseInt(lines.get(nLineEndYear).split("[ \t]+")[0]);
			if(lines.size() > 5) {
				if(!(lines.get(nLineEndYear+1).split("[ \t]")[0] == "N" | lines.get(nLineEndYear+1).split("[ \t]+")[0] == "S")) {
					this.startstart = Integer.parseInt(lines.get(nLineFDOFY).split("[ \t]+")[0]);
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
			f.LogError(LogFileIn.LogMode.ERROR, "swYears onReadYearsIn could not read integer: "+e.getMessage());
		}
		if(lines.size() > 5) {
			if(FirstEnd)
				nLineIsNorth=nLineEndYear+1;
			if(lines.get(nLineIsNorth).split("[ \t]+")[0].equals("N")) {
				this.isnorth = true;
				fhemi = true;
			} else if(lines.get(nLineIsNorth).split("[ \t]+")[0].equals("s")) {
				this.isnorth = false;
				fhemi = true;
			} else {
				f.LogError(LogFileIn.LogMode.WARN, "swYears onReadYearsIn isNorth format wrong: Set to TRUE");
				this.isnorth = true;
			}
		}
		if(!(fstartdy && fenddy && fhemi)) {
			String message = "\n Not found in "+YearsIn.toString()+":\n";
			if(!fstartdy) {
				message += "\tStart Day - using 1\n";
				this.startstart = 1;
			} else if(!fenddy) {
				message += "\tEnd Day - using 'end'\n";
				endString = true;
			} else if(!fhemi) {
				message += "\tHemisphere - using 'N'\n";
				this.isnorth = true;
			}
			f.LogError(LogFileIn.LogMode.WARN, "swYears onReadYearsIn : "+message);
		}
		this.startstart += ((this.isIsNorth()) ? Times.DAYFIRST_NORTH : Times.DAYFIRST_SOUTH) - 1;
		if(endString)
			this.endend = (this.isIsNorth()) ? Times.Time_get_lastdoy_y(this.endyr) : Times.DAYLAST_SOUTH;
		else {
			this.endend = (d<365) ? d : Times.Time_get_lastdoy_y(this.endyr);
		}
		this.daymid = (this.isIsNorth()) ? Times.DAYMID_NORTH : Times.DAYMID_SOUTH;
		this.data = true;
	}
	
	protected void onWrite(Path YearsIn) throws Exception {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Model time definition file");
			lines.add("# Location: P1");
			lines.add("");
			lines.add(String.valueOf(this.startyr) + comments[0]);
			lines.add(String.valueOf(this.endyr) + comments[1]);
			lines.add(String.valueOf(this.startstart) + comments[2]);
			lines.add(String.valueOf(this.endend) + comments[3]);
			if(this.isIsNorth())
				lines.add("N" + comments[4]);
			else
				lines.add("S" + comments[4]);
			Files.write(YearsIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogFileIn.LogMode.ERROR, "swYears onWriteYears : No Data.");
		}
	}
	protected int getYearsInSimulation() {
		return getEndYear() - getStartYear() + 1;
	}
	protected int getDaysInSimulation() {
		int n=0;
		
		for(int i=this.getStartYear(); i<=this.getEndYear(); i++) {
			if(i==this.getStartYear()) {
				n+=Times.Time_get_lastdoy_y(i) - this.startstart + 1;
			} else if(i==this.getEndYear()) {
				n+=this.endend;
			} else {
				n+=Times.Time_get_lastdoy_y(i);
			}
		}
		return n;
	}

	protected int getStartYear() {
		return startyr;
	}
	protected void setStartYear(int nStartYear) {
		this.startyr = nStartYear;
	}
	
	protected int getEndYear() {
		return endyr;
	}
	protected void setEndYear(int nEndYear) {
		this.endyr = nEndYear;
	}

	protected int getFirstDayOfFirstYear() {
		return startstart;
	}
	protected void setFirstDayOfFirstYear(int nFirstDayOfFirstYear) {
		this.startstart = nFirstDayOfFirstYear;
	}

	protected int getEndDayOfEndYear() {
		return endend;
	}
	protected void setEndDayOfEndYear(int nEndDayOfEndYear) {
		this.endend = nEndDayOfEndYear;
	}

	protected boolean isIsNorth() {
		return isnorth;
	}
	protected void setIsNorth(boolean bIsNorth) {
		this.isnorth = bIsNorth;
	}
	
	protected int getFirstdoy() {
		return this.firstdoy;
	}
	
	protected int getLastdoy() {
		return this.lastdoy;
	}
	
	protected boolean get_newweek() {
		return this.newweek;
	}
	protected boolean get_newmonth() {
		return this.newmonth;
	}
	protected boolean get_newyear() {
		return this.newyear;
	}
	
	protected int getYear() {
		return this.year;
	}
	protected int getMonth() {
		return this.month;
	}
	protected int getWeek() {
		return this.week;
	}
	protected int getDOY() {
		return this.doy;
	}
	
	protected void setYear(int year) {
		this.year = year;
	}
	protected void setMonth(int month) {
		this.month = month;
	}
	protected void setWeek(int week) {
		this.week = week;
	}
	protected void setDOY(int doy) {
		this.doy = doy;
	}
	
	protected boolean get_HasData() {
		return this.data;
	}
}
