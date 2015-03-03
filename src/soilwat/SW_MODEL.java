package soilwat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SW_MODEL {
	public static class MODEL_INPUT_DATA {
		public int startYear,
		endYear,
		startstart,
		endend;
		public boolean isNorth;
		private LogFileIn log;
		
		public MODEL_INPUT_DATA(LogFileIn log) {
			this.log = log;
		}
		
		public void onClear() {
			startYear = 0;
			endYear = 0;
			startstart = 0;
			endend = 0;
			isNorth = false;
		}
		
		public void onRead(String swYearsIn) throws Exception {
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swYearsIn, getClass().getClassLoader());
			
			int nFileItemsRead = 0;
			String enddyval="";
			boolean fhemi=false,fenddy=false,fstartdy=false;

			int cnt = 0;
			loop: for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					switch1: switch (nFileItemsRead) {
					case 0:
						try {
							startYear = Integer.parseInt(values[0]);
						} catch (NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "swYears onReadYearsIn could not read Start Year integer: "+e.getMessage());
						}
						break switch1;
					case 1:
						try {
							endYear = Integer.parseInt(values[0]);
						} catch (NumberFormatException e) {
							f.LogError(LogFileIn.LogMode.ERROR, "swYears onReadYearsIn could not read End Year integer: "+e.getMessage());
						}
						break switch1;
					default:
						cnt++;
						if(values[0].matches("[a-zA-Z]+") && values[0].compareToIgnoreCase("N") == 0) {//I think the C code is wrong
							isNorth = true;
							fhemi = true;
							break loop;
						}
						swith2: switch (cnt) {
						case 1:
							try {
								startstart = Integer.parseInt(values[0]);
							} catch (NumberFormatException e) {
								f.LogError(LogFileIn.LogMode.ERROR, "swYears onReadYearsIn could not read Start Year integer: "+e.getMessage());
							}
							fstartdy = true;
							break swith2;
						case 2:
							enddyval = values[0];
							fenddy=true;
							break loop;
						case 3:
							isNorth = true;
							fhemi = true;
							break loop;
						}
						break switch1;
					}
					nFileItemsRead++;
				}
			}
			
			if(!(fstartdy && fenddy && fhemi)) {
				String message = "\n Not found in "+swYearsIn+":\n";
				if(!fstartdy) {
					message += "\tStart Day - using 1\n";
					this.startstart = 1;
				} else if(!fenddy) {
					message += "\tEnd Day - using 'end'\n";
					enddyval = "end";
				} else if(!fhemi) {
					message += "\tHemisphere - using 'N'\n";
					isNorth = true;
				}
				f.LogError(LogFileIn.LogMode.WARN, "swYears onReadYearsIn : "+message);
			}
			
			this.startstart += ((isNorth) ? Times.DAYFIRST_NORTH : Times.DAYFIRST_SOUTH) - 1;
			if(enddyval.compareToIgnoreCase("end") == 0)
				this.endend = (isNorth) ? Times.Time_get_lastdoy_y(endYear) : Times.DAYLAST_SOUTH;
			else {
				int d = 0;
				try {
					d = Integer.parseInt(enddyval);
				} catch (NumberFormatException e) {
					f.LogError(LogFileIn.LogMode.ERROR, "swYears onReadYearsIn could not read end year integer: "+e.getMessage());
				}
				this.endend = (d<365) ? d : Times.Time_get_lastdoy_y(endYear);
			}
		}
		
		public void onWrite(String swYearsIn) throws Exception {
			Path yearsIn = Paths.get(swYearsIn);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(yearsIn, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out = "";
			out+="# Model time definition file\n";
			out+="# Location: \n";
			out+="\n";
			out+=String.format("%-5d%s%s",startYear,comments[0],"\n");
			out+=String.format("%-5d%s%s",endYear,comments[1],"\n");
			out+=String.format("%-5d%s%s",startstart,comments[2],"\n");
			out+=String.format("%-5d%s%s",endend,comments[3],"\n");
			if(isNorth)
				out+=String.format("%-5s%s%s", "N",comments[4],"\n");
			else
				out+=String.format("%-5s%s%s", "S",comments[4],"\n");
			return out;
		}
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
	
	private static final String[] comments = {"\t# starting year (but see weather and swc inputs)",
			"\t# ending year", "\t# first day of first year", "\t# ending day of last year",
			"\t# ending day of last year"};
	private boolean data;
	private LogFileIn log;
	
	protected SW_MODEL(LogFileIn log) {
		Times.Time_init();
		this.newweek=this.newmonth=this.newyear=false;
		this.data = false;
		this.log = log;
	}
	
	protected void onClear() {
		this.data = false;
	}
	
	protected boolean onVerify() throws Exception {
		LogFileIn f = log;
		if(this.data) {
			List<String> messages = new ArrayList<String>();
			if(this.startyr < 0)
				messages.add("swYears StartYear Negative.");
			if(this.endyr < 0)
				messages.add("swYears EndYear Negative.");
			if(this.endyr < this.startyr)
				messages.add("swYears StartYear > EndYear Negative.");
			if(this.daymid == 0)
				messages.add("swYears Day Middle not set.");
			
			if(messages.size() > 0) {
				String message = "";
				for (String s : messages)
					message += s + "\n";
				f.LogError(LogFileIn.LogMode.FATAL, message);
			}
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
