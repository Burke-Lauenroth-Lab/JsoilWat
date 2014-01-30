package data;

import input.LogFileIn;
import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import times.Times;
import defines.Defines;
import defines.Defines.ObjType;

public class SW_OUTPUT {

	
	private static final int SW_OUTNKEYS=28;/* must also match number of items in enum (minus eSW_NoKey and eSW_LastKey) */
	
	/* These are the keywords to be found in the output setup file */
	/* some of them are from the old fortran model and are no longer */
	/* implemented, but are retained for some tiny measure of backward */
	/* compatibility */
	public enum OutKey {
		eSW_NoKey (-1, "",Defines.ObjType.eNONE),
		eSW_AllWthr (0, "WTHR",Defines.ObjType.eWTH), /* includes all weather vars */
		eSW_Temp (1, "TEMP",Defines.ObjType.eWTH),
		eSW_Precip (2, "PRECIP",Defines.ObjType.eWTH),
		eSW_SoilInf (3, "SOILINFILT",Defines.ObjType.eWTH),
		eSW_Runoff (4, "RUNOFF",Defines.ObjType.eWTH),
		/* soil related water quantities */
		eSW_AllH2O (5, "ALLH2O",Defines.ObjType.eSWC),
		eSW_VWCBulk (6, "VWCBULK",Defines.ObjType.eSWC),
		eSW_VWCMatric (7, "VWCMATRIC",Defines.ObjType.eSWC),
		eSW_SWCBulk (8, "SWCBULK",Defines.ObjType.eSWC),
		eSW_SWABulk (9, "SWABULK",Defines.ObjType.eSWC),
		eSW_SWAMatric (10, "SWAMATRIC",Defines.ObjType.eSWC),
		eSW_SWPMatric (11, "SWPMATRIC",Defines.ObjType.eSWC),
		eSW_SurfaceWater (12, "SURFACEWATER",Defines.ObjType.eSWC),
		eSW_Transp (13, "TRANSP",Defines.ObjType.eSWC),
		eSW_EvapSoil (14, "EVAPSOIL",Defines.ObjType.eSWC),
		eSW_EvapSurface (15, "EVAPSURFACE",Defines.ObjType.eSWC),
		eSW_Interception (16, "INTERCEPTION",Defines.ObjType.eSWC),
		eSW_LyrDrain (17, "LYRDRAIN",Defines.ObjType.eSWC),
		eSW_HydRed (18,"HYDRED",Defines.ObjType.eSWC),
		eSW_ET (19,"ET",Defines.ObjType.eSWC),
		eSW_AET (20,"AET",Defines.ObjType.eSWC),
		eSW_PET (21,"PET",Defines.ObjType.eSWC), /* really belongs in wth), but for historical reasons we'll keep it here */
		eSW_WetDays (22,"WETDAY",Defines.ObjType.eSWC),
		eSW_SnowPack (23,"SNOWPACK",Defines.ObjType.eSWC),
		eSW_DeepSWC (24,"DEEPSWC",Defines.ObjType.eSWC),
		eSW_SoilTemp (25,"SOILTEMP",Defines.ObjType.eSWC),
		/* vegetation quantities */
		eSW_AllVeg (26,"ALLVEG",Defines.ObjType.eVES),
		eSW_Estab (27,"ESTABL",Defines.ObjType.eVES), /* make sure this is the last one */
		eSW_LastKey (28, "END",Defines.ObjType.eNONE);
			
		private final int index;
		private final String name;
		private final Defines.ObjType object;
		public static final String[] comment = {"/* */",
		"/* max., min, average temperature (C) */",
		"/* total precip = sum(rain, snow), rain, snow-fall, snowmelt, and snowloss (cm) */",
		"/* water to infiltrate in top soil layer (cm), runoff (cm); (not-intercepted rain)+(snowmelt-runoff) */",
		"/* runoff (cm): total runoff, runoff from ponded water, runoff from snowmelt */",
		"/* */",
		"/* bulk volumetric soilwater (cm / layer) */",
		"/* matric volumetric soilwater (cm / layer) */",
		"/* bulk soilwater content (cm / cm layer); swc.l1(today) = swc.l1(yesterday)+inf_soil-lyrdrain.l1-transp.l1-evap_soil.l1; swc.li(today) = swc.li(yesterday)+lyrdrain.l(i-1)-lyrdrain.li-transp.li-evap_soil.li; swc.llast(today) = swc.llast(yesterday)+lyrdrain.l(last-1)-deepswc-transp.llast-evap_soil.llast */",
		"/* bulk available soil water (cm/layer) = swc - wilting point */",
		"/* matric available soil water (cm/layer) = swc - wilting point */",
		"/* matric soilwater potential (-bars) */",
		"/* surface water (cm) */",
		"/* transpiration from each soil layer (cm): total, trees, shrubs, forbs, grasses */",
		"/* bare-soil evaporation from each soil layer (cm) */",
		"/* evaporation (cm): total, trees, shrubs, forbs, grasses, litter, surface water */",
		"/* intercepted rain (cm): total, trees, shrubs, forbs, grasses, and litter (cm) */",
		"/* water percolated from each layer (cm) */",
		"/* hydraulic redistribution from each layer (cm): total, trees, shrubs, forbs, grasses */",
		"/* */",
		"/* actual evapotr. (cm) */",
		"/* potential evaptr (cm) */",
		"/* days above swc_wet */",
		"/* snowpack water equivalent (cm), snowdepth (cm); since snowpack is already summed, use avg - sum sums the sums = nonsense */",
		"/* deep drainage into lowest layer (cm) */",
		"/* soil temperature from each soil layer (in celsius) */",
		"/* */",
		"/* yearly establishment results */"};
		
		private OutKey(int index, String name, Defines.ObjType type) {
			this.index = index;
			this.name = name;
			this.object = type;
		}
		
		public int idx() {
			return this.index;
		}
		
		public String key() {
			return this.name;
		}
		
		public Defines.ObjType objType() {
			return this.object;
		}
		public static OutKey getEnum(String value) {
			if(value==null)
				throw new IllegalArgumentException();
			for (OutKey v : values()) {
				if(value.equalsIgnoreCase(v.key())) return v;
			}
			throw new IllegalArgumentException();
		}
	}
	/* output period specifiers found in input file */
	public enum OutPeriod {
		SW_DAY (0,"DY"),
		SW_WEEK (1,"WK"),
		SW_MONTH (2,"MO"),
		SW_YEAR (3,"YR");
		
		private final int index;
		private final String name;
		
		public static OutPeriod getEnum(String value) {
			if(value==null)
				throw new IllegalArgumentException();
			for (OutPeriod v : values()) {
				if(value.equalsIgnoreCase(v.key())) return v;
			}
			throw new IllegalArgumentException();
		}
		public static OutPeriod fromInteger(int x) {
			switch (x) {
			case 0:
				return SW_DAY;
			case 1:
				return SW_WEEK;
			case 2:
				return SW_MONTH;
			case 3:
				return SW_YEAR;
			default:
				return null;
			}
		}
		private OutPeriod(int index, String name) {
			this.index = index;
			this.name = name;
		}
		public int idx() {
			return this.index;
		}
		public String key() {
			return this.name;
		}
	}
	/* summary methods */
	public enum OutSum {
		eSW_Off (0,"OFF"),
		eSW_Sum (1,"SUM"),
		eSW_Avg (2,"AVG"),
		eSW_Fnl (3,"FIN");
		private final int index;
		private final String name;
		private OutSum(int index, String name) {
			this.index = index;
			this.name = name;
		}		
		public int idx() {
			return this.index;
		}
		public String key() {
			return this.name;
		}
		public static OutSum getEnum(String value) {
			if(value==null)
				throw new IllegalArgumentException();
			for (OutSum v : values()) {
				if(value.equalsIgnoreCase(v.key())) return v;
			}
			throw new IllegalArgumentException();
		}
	}
	
	private class SW_OUT {
		private OutKey mykey;
		private Defines.ObjType myobj;
		private OutPeriod period;
		private boolean[] usePeriods;
		private OutSum sumtype;
		private String filename_prefix;
		private boolean use;
		private int first, last, /* updated for each year */
			first_orig, last_orig;
		private int yr_row, mo_row, wk_row, dy_row;
		private Path file_dy, file_wk, file_mo, file_yr;
		
		public SW_OUT() {
			this.usePeriods = new boolean[numPeriods];
			for(int i=0; i<numPeriods; i++)
				this.usePeriods[i] = false;
		}
		
		public boolean get_PeriodUse(OutPeriod pd) {
			return usePeriods[pd.idx()];
		}
		
		private final String[] comments = {"/* */",
			"/* max., min, average temperature (C) */",
			"/* total precip = sum(rain, snow), rain, snow-fall, snowmelt, and snowloss (cm) */",
			"/* water to infiltrate in top soil layer (cm), runoff (cm); (not-intercepted rain)+(snowmelt-runoff) */",
			"/* runoff (cm): total runoff, runoff from ponded water, runoff from snowmelt */",
			"/* */",
			"/* bulk volumetric soilwater (cm / layer) */",
			"/* matric volumetric soilwater (cm / layer) */",
			"/* bulk soilwater content (cm / cm layer); swc.l1(today) = swc.l1(yesterday)+inf_soil-lyrdrain.l1-transp.l1-evap_soil.l1; swc.li(today) = swc.li(yesterday)+lyrdrain.l(i-1)-lyrdrain.li-transp.li-evap_soil.li; swc.llast(today) = swc.llast(yesterday)+lyrdrain.l(last-1)-deepswc-transp.llast-evap_soil.llast */",
			"/* bulk available soil water (cm/layer) = swc - wilting point */",
			"/* matric available soil water (cm/layer) = swc - wilting point */",
			"/* matric soilwater potential (-bars) */",
			"/* surface water (cm) */",
			"/* transpiration from each soil layer (cm): total, trees, shrubs, forbs, grasses */",
			"/* bare-soil evaporation from each soil layer (cm) */",
			"/* evaporation (cm): total, trees, shrubs, forbs, grasses, litter, surface water */",
			"/* intercepted rain (cm): total, trees, shrubs, forbs, grasses, and litter (cm) */",
			"/* water percolated from each layer (cm) */",
			"/* hydraulic redistribution from each layer (cm): total, trees, shrubs, forbs, grasses */",
			"/* */",
			"/* actual evapotr. (cm) */",
			"/* potential evaptr (cm) */",
			"/* days above swc_wet */",
			"/* snowpack water equivalent (cm), snowdepth (cm); since snowpack is already summed, use avg - sum sums the sums = nonsense */",
			"/* deep drainage into lowest layer (cm) */",
			"/* soil temperature from each soil layer (in celsius) */",
			"/* */",
			"/* yearly establishment results */"};
		
		public String toString() {
			return String.format("%13s %7s   %6s      %2s     %3s %15s      %s", mykey.key(),sumtype.key(),period.key(),this.first_orig, this.last_orig==366?"end":this.last_orig,filename_prefix, comments[mykey.idx()]);
		}
	}
	
	private SW_OUT[] SW_Output;
	private String _sep;
	private boolean data;
	private int numPeriod;
	private int numPeriods=4;
	private boolean useTimeStep;
	private int timeStep[];
	private int[][] timeSteps;
	private boolean bFlush;
	private boolean tOffset;
	
	private SW_SITE SW_Site;
	private SW_SOILS SW_Soils;
	private SW_SOILWATER SW_Soilwat;
	private SW_MODEL SW_Model;
	private SW_WEATHER SW_Weather;
	private SW_VEGESTAB SW_VegEstab;
	
	public SW_OUTPUT(SW_SITE site, SW_SOILS soils, SW_SOILWATER soilwat, SW_MODEL model, SW_WEATHER weather, SW_VEGESTAB estab) {
		this.SW_Output = new SW_OUT[SW_OUTNKEYS];
		for(int i=0; i<SW_OUTNKEYS; i++)
			this.SW_Output[i] = new SW_OUT();
		this.data = false;
		timeSteps = new int[SW_OUTNKEYS][4];
		numPeriod = 0;
		useTimeStep = false;
		timeStep = new int[numPeriods];
		for(int i=0; i<numPeriods; i++)
			timeStep[i] = 4;//not used default
		bFlush = false;
		tOffset = true;
		
		SW_Site=site;
		SW_Soilwat=soilwat;
		SW_Model = model;
		SW_Weather = weather;
		SW_VegEstab = estab;
		SW_Soils = soils;
		
		bFlush = false;
		tOffset = true;
	}
	
	public void onRead(Path OutputSetupIn, Path OutputDirectory, boolean deepdrain) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(OutputSetupIn, StandardCharsets.UTF_8);
		
		useTimeStep = false;
		OutKey k = OutKey.eSW_NoKey;
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				
				if(values[0].equals("TIMESTEP")) {
					for(int i=1; i<values.length; i++) {
						timeStep[i-1] = OutPeriod.getEnum(values[i].toUpperCase()).idx();
						numPeriod++;
					}
					for(int i=0; i<SW_OUTNKEYS; i++)//Go through the OUTs set the use Period flags
						for(int j=0; j<numPeriods; j++)
							SW_Output[i].usePeriods[timeStep[j]] = true;
					useTimeStep=true;
					continue;
				} else {
					if(values.length < 6) {
						if(values[0].equals("OUTSEP")) {
							if(values[1].equals("t"))
								_sep="\t";
							else if(values[1].equals("s"))
								_sep=" ";
							else
								_sep=values[1];
							continue;
						} else {
							f.LogError(LogMode.ERROR, "OutputSetupIn onRead: Insufficient key parameters for item.");
							continue;
						}
					}
					k = OutKey.getEnum(values[0]);
					for(int i=0; i< numPeriods; i++) {
						if(i<1 && !useTimeStep) {
							SW_Output[k.idx()].usePeriods[OutPeriod.getEnum(values[2]).idx()] = true;
							timeSteps[k.idx()][i] = OutPeriod.getEnum(values[2]).idx();
						} else if(i<numPeriod && useTimeStep) {
							timeSteps[k.idx()][i] = timeStep[i];
						} else {
							timeSteps[k.idx()][i] = 4;
						}
					}
				}
				/* Check validity of output key */
				if(k==OutKey.eSW_Estab) {
					SW_Output[k.idx()].sumtype = OutSum.eSW_Sum;
					SW_Output[k.idx()].period = OutPeriod.SW_YEAR;
					SW_Output[k.idx()].last = 366;
				} else if(k==OutKey.eSW_AllVeg || k==OutKey.eSW_ET || k==OutKey.eSW_AllWthr || k==OutKey.eSW_AllH2O) {
					SW_Output[k.idx()].use = false;
					f.LogError(LogMode.NOTE, "OutputSetupIn onRead: Unimplemented output key.");
					continue;
				}
				/* check validity of summary type */
				SW_Output[k.idx()].sumtype = OutSum.getEnum(values[1]);
				if (SW_Output[k.idx()].sumtype == OutSum.eSW_Fnl && !(k == OutKey.eSW_VWCBulk || k == OutKey.eSW_VWCMatric || k == OutKey.eSW_SWPMatric || k == OutKey.eSW_SWCBulk || k == OutKey.eSW_SWABulk || k == OutKey.eSW_SWAMatric || k == OutKey.eSW_DeepSWC)) {
					f.LogError(LogMode.WARN, OutputSetupIn.toString()+" : Summary Type FIN with key "+k.key()+" is meaningless.\n"+"  Using type AVG instead.");
					SW_Output[k.idx()].sumtype = OutSum.eSW_Avg;
				}
				/* verify deep drainage parameters */
				if (k == OutKey.eSW_DeepSWC && SW_Output[k.idx()].sumtype != OutSum.eSW_Off && !deepdrain) {
					f.LogError(LogMode.WARN, OutputSetupIn.toString()+" : DEEPSWC cannot be output if flag not set in Site Param.");
					continue;
				}
				//Set the values
				SW_Output[k.idx()].use = (SW_Output[k.idx()].sumtype == OutSum.eSW_Off) ? false : true;
				if (SW_Output[k.idx()].use) {
					SW_Output[k.idx()].mykey = k;
					SW_Output[k.idx()].myobj = k.objType();
					SW_Output[k.idx()].period = OutPeriod.getEnum(values[2]);
					SW_Output[k.idx()].filename_prefix = values[5];
					try {
						SW_Output[k.idx()].first_orig = Integer.valueOf(values[3]);
						if(values[4].toLowerCase().equals("end"))
							SW_Output[k.idx()].last_orig = 366;
						else
							SW_Output[k.idx()].last_orig = Integer.valueOf(values[4]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "OutputSetupIn onRead: Could not covert start or end."+e.getMessage());
					}
					if (SW_Output[k.idx()].last_orig == 0) {
						f.LogError(LogMode.ERROR, "OutputSetupIn onRead : Invalid ending day");
					}
				}
				//Set the outputs for the Periods
				for (int i = 0; i < numPeriods; i++) {
					if (SW_Output[k.idx()].use) {
						if (timeSteps[k.idx()][i] < 4) {
							String temp = values[5]+".";
							switch (timeSteps[k.idx()][i]) {
							case 0:
								temp+="dy";
								SW_Output[k.idx()].file_dy = OutputDirectory.resolve(temp);
								break;
							case 1:
								temp+="wk";
								SW_Output[k.idx()].file_wk = OutputDirectory.resolve(temp);
								break;
							case 2:
								temp+="mo";
								SW_Output[k.idx()].file_mo = OutputDirectory.resolve(temp);
								break;
							case 3:
								temp+="yr";
								SW_Output[k.idx()].file_yr = OutputDirectory.resolve(temp);
								break;
							}
						}
					}
				}
			}
		}
		this.data = true;
	}
	
	public void onWrite(Path OutputSetupIn) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Output setup file for SOILWAT v4 compiled on Mac OS X (20100202)");
			lines.add("#");
			lines.add("# Notes:");
			lines.add("# Time periods available:  DY,WK,MO,YR");
			lines.add("#   eg, if DY is chosen then 100,200 would mean to use the second hundred days");
			lines.add("#   But if YR is chosen, start and end numbers are in days so only those days");
			lines.add("#   are reported for the yearly average.");
			lines.add("# Some keys from older versions (fortran and the c versions mimicking the fortran");
			lines.add("#   version) are not currently implemented:");
			lines.add("#   ALLH20, WTHR.");
			lines.add("#");
			lines.add("# ESTABL only produces yearly output, namely, DOY for each species requested.");
			lines.add("#   Thus, to minimize typo errors, all flags are ignored except the filename.");
			lines.add("#   Output is simply the day of the year establishment occurred for each species");
			lines.add("#   in each year of the model run.  Refer to the estabs.in file for more info.");
			lines.add("#");
			lines.add("# DEEPSWC produces output only if the deepdrain flag is set in siteparam.in.");
			lines.add("#");
			lines.add("# Filename prefixes should not have a file extension.");
			lines.add("# Case is unimportant.");
			lines.add("#");
			lines.add("# SUMTYPEs are one of the following:");
			lines.add("#  OFF - no output for this variable");
			lines.add("#  SUM - sum the variable for each day in the output period");
			lines.add("#  AVG - average the variable over the output period");
			lines.add("#  FIN - output value of final day in the period; soil water variables only.");
			lines.add("# Note that SUM and AVG are the same if timeperiod = dy.");
			lines.add("#");
			lines.add("# (3-Sep-03) OUTSEP key indicates the output separator.  This method");
			lines.add("# allows older files to work with the new version.  The default is a");
			lines.add("# tab.  Other options are 's' or 't' for space or tab (no quotes)");
			lines.add("# or any other printable character as itself (eg, :;| etc).  The given");
			lines.add("# separator will apply to all of the output files.  Note that only lowercase");
			lines.add("# letters 's' or 't' are synonyms.");
			lines.add("#");
			lines.add("# (01/17/2013) TIMESTEP key indicates which periods you want to output.");
			lines.add("# You can output all the periods at a time, just one, or however many");
			lines.add("# you want. To change which periods to output type 'dy' for day,");
			lines.add("# 'wk' for week, 'mo' for month, and 'yr' for year after TIMESTEP");
			lines.add("# in any order. For example: 'TIMESTEP mo wk' will output for month and week");
			if(_sep.equals("\t"))
				lines.add("OUTSEP t");
			else if(_sep.equals(" "))
				lines.add("OUTSEP s");
			else
				lines.add("OUTSEP "+_sep);
			if(useTimeStep) {
				String temp="TIMESTEP";
				for(int i=0; i<numPeriods; i++) {
					if(timeStep[i] < 4) {
						temp+=" ";
						switch(timeStep[i]) {
						case 0:
							temp+="dy";
							break;
						case 1:
							temp+="wk";
							break;
						case 2:
							temp+="mo";
							break;
						case 3:
							temp+="yr";
							break;
						}
					}
				}
				lines.add(temp);
			}
			lines.add("");
			lines.add(String.format("#     %4s     %7s   %6s   %5s    %3s    %15s   %7s","key","SUMTYPE","PERIOD","start","end","filename_prefix","comment"));
			for(int i=0; i<SW_OUTNKEYS; i++) {
				if(SW_Output[i].use)
					lines.add(SW_Output[i].toString());
			}
			Files.write(OutputSetupIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "OutputSetupIn : onWrite : No data.");
		}
	}
	
	public void SW_OUT_new_year() {
		for(int k=0; k < SW_OUTNKEYS; k++) {
			if(!SW_Output[k].use)
				continue;
			if(SW_Output[k].first_orig <= SW_Model.getFirstdoy())
				SW_Output[k].first = SW_Model.getFirstdoy();
			else
				SW_Output[k].first = SW_Output[k].first_orig;
			if (SW_Output[k].last_orig >= SW_Model.getLastdoy())
				SW_Output[k].last = SW_Model.getLastdoy();
			else
				SW_Output[k].last = SW_Output[k].last_orig;
		}
	}
	
	public void SW_OUT_sum_today(Defines.ObjType otyp) {
		/* =================================================== */
		/* adds today's output values to week, month and year
		 * accumulators and puts today's values in yesterday's
		 * registers. This is different from the Weather.c approach
		 * which updates Yesterday's registers during the _new_day()
		 * function. It's more logical to update yesterday just
		 * prior to today's calculations, but there's no logical
		 * need to perform _new_day() on the soilwater.
		 */
		SW_SOILWATER.SOILWAT s = SW_Soilwat.getSoilWat();
		SW_WEATHER.WEATHER w = SW_Weather.getWeather();
		
		/* do this every day (kinda expensive but more general than before)*/
		switch (otyp) {
		case eSWC:
			s.dysum.onClear();
			break;
		case eWTH:
			s.dysum.onClear();
			break;
		case eVES:
			return;
		default:
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, "Invalid object type in SW_OUT_sum_today().");
			break;
		}
		
		/* the rest only get done if new period */
		if(SW_Model.get_newweek() || this.bFlush) {
			average_for(otyp, OutPeriod.SW_WEEK);
			switch(otyp) {
			case eSWC:
				s.wksum.onClear();
				break;
			case eWTH:
				s.wksum.onClear();
				break;
			default:
				break;
			}
		}
		
		if(SW_Model.get_newmonth() || this.bFlush) {
			average_for(otyp, OutPeriod.SW_MONTH);
			switch(otyp) {
			case eSWC:
				s.mosum.onClear();
				break;
			case eWTH:
				s.mosum.onClear();
				break;
			default:
				break;
			}
		}
		
		if(SW_Model.get_newyear() || this.bFlush) {
			average_for(otyp, OutPeriod.SW_YEAR);
			switch(otyp) {
			case eSWC:
				s.mosum.onClear();
				break;
			case eWTH:
				s.mosum.onClear();
				break;
			default:
				break;
			}
		}
		
		if(!bFlush) {
			for (OutPeriod pd : OutPeriod.values()) {
				collect_sums(otyp, pd);
			}
		}
	}

	public void SW_OUT_write_today() {
		/* --------------------------------------------------- */
		/* all output values must have been summed, averaged or
		 * otherwise completed before this is called [now done
		 * by SW_*_sum_*<daily|yearly>()] prior.
		 * This subroutine organizes only the calling loop and
		 * sending the string to output.
		 * Each output quantity must have a print function
		 * defined and linked to SW_Output.pfunc (currently all
		 * starting with 'get_').  Those funcs return a properly
		 * formatted string to be output via the module variable
		 * 'outstr'. Furthermore, those funcs must know their
		 * own time period.  This version of the program only
		 * prints one period for each quantity.
		 *
		 * The t value tests whether the current model time is
		 * outside the output time range requested by the user.
		 * Recall that times are based at 0 rather than 1 for
		 * array indexing purposes but the user request is in
		 * natural numbers, so we add one before testing.
		 */
		/* 10-May-02 (cwb) Added conditional to interface with STEPPE.
		 *           We want no output if running from STEPPE.
		 */
		int t = 0xffff;
		boolean writeit;
		int i;

		for(int k=0; k < SW_OUTNKEYS; k++) {
			for (i = 0; i < numPeriods; i++) { /* will run through this loop for as many periods are being used */
				if (!SW_Output[k].use)
					continue;
				if (timeSteps[k][i] < 4) {
					writeit = true;
					SW_Output[k].period = OutPeriod.fromInteger(timeSteps[k][i]); /* set the desired period based on the iteration */
					switch (SW_Output[k].period) {
					case SW_DAY:
						t = SW_Model.getDOY();
						break;
					case SW_WEEK:
						writeit = (SW_Model.get_newweek() || bFlush);
						t = (SW_Model.getWeek() + 1) - (tOffset?1:0);
						break;
					case SW_MONTH:
						writeit = (SW_Model.get_newmonth() || bFlush);
						t = (SW_Model.getMonth() + 1) - (tOffset?1:0);
						break;
					case SW_YEAR:
						writeit = (SW_Model.get_newyear() || bFlush);
						t = SW_Output[k].first; /* always output this period */
						break;
					default:
						LogFileIn f = LogFileIn.getInstance();
						f.LogError(LogMode.FATAL, "Invalid period in SW_OUT_write_today().");
					}
					if (!writeit || t < SW_Output[k].first || t > SW_Output[k].last)
						continue;

					switch (SW_Output[k].mykey) {
					case eSW_Temp:
						get_temp();
						break;
					case eSW_Precip:
						get_precip();
						break;
					case eSW_VWCBulk:
						get_vwcBulk();
						break;
					case eSW_VWCMatric:
						get_vwcMatric();
						break;
					case eSW_SWCBulk:
						get_swcBulk();
						break;
					case eSW_SWPMatric:
						get_swpMatric();
						break;
					case eSW_SWABulk:
						get_swaBulk();
						break;
					case eSW_SWAMatric:
						get_swaMatric();
						break;
					case eSW_SurfaceWater:
						get_surfaceWater();
						break;
					case eSW_Runoff:
						get_runoff();
						break;
					case eSW_Transp:
						get_transp();
						break;
					case eSW_EvapSoil:
						get_evapSoil();
						break;
					case eSW_EvapSurface:
						get_evapSurface();
						break;
					case eSW_Interception:
						get_interception();
						break;
					case eSW_SoilInf:
						get_soilinf();
						break;
					case eSW_LyrDrain:
						get_lyrdrain();
						break;
					case eSW_HydRed:
						get_hydred();
						break;
					case eSW_AET:
						get_aet();
						break;
					case eSW_PET:
						get_pet();
						break;
					case eSW_WetDays:
						get_wetdays();
						break;
					case eSW_SnowPack:
						get_snowpack();
						break;
					case eSW_DeepSWC:
						get_deepswc();
						break;
					case eSW_SoilTemp:
						get_soiltemp();
						break;
					case eSW_Estab:
						get_estab();
						break;
					default:
						get_none();
						break;

					}
				}
			}
		}
		
	}
	
	private void get_none() {
		
	}
	private void get_estab() {
		
	}
	private void get_temp() {
		
	}
	private void get_precip() {
		
	}
	private void get_vwcBulk() {
		
	}
	private void get_vwcMatric() {
		
	}
	private void get_swcBulk() {
		
	}
	private void get_swpMatric() {
		
	}
	private void get_swaBulk() {
		
	}
	private void get_swaMatric() {
		
	}
	private void get_surfaceWater() {
		
	}
	private void get_runoff() {
		
	}
	private void get_transp() {
		
	}
	private void get_evapSoil() {
		
	}
	private void get_evapSurface() {
		
	}
	private void get_interception() {
		
	}
	private void get_soilinf() {
		
	}
	private void get_lyrdrain() {
		
	}
	private void get_hydred() {
		
	}
	private void get_aet() {
		
	}
	private void get_pet() {
		
	}
	private void get_wetdays() {
		
	}
	private void get_snowpack() {
		
	}
	private void get_deepswc() {
		
	}
	private void get_soiltemp() {
		
	}
	private void sumof_ves(OutKey k) {
		/* --------------------------------------------------- */
		/* k is always eSW_Estab, and this only gets called yearly */
		/* in fact, there's nothing to do here as the get_estab()
		 * function does everything needed.  This stub is here only
		 * to facilitate the loop everything else uses.
		 * That is, until we need to start outputting as-yet-unknown
		 * establishment variables.
		 */
		SW_VEGESTAB v = this.SW_VegEstab;
		
		return;
	}
	
	private void sumof_wth(SW_WEATHER v, OutKey k) {
		switch(k) {
		case eSW_Temp:
			break;
		case eSW_Precip:
			break;
		case eSW_SoilInf:
			break;
		case eSW_Runoff:
			break;
		default:
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, "PGMR: Invalid key in sumof_wth");
			break;
		}
	}
	
	private void sumof_swc(SW_SOILWATER v, OutKey k) {
//		switch (k) {
//		case eSW_VWCBulk: /* get swcBulk and convert later */
//			for(int i=0; i<SW_Site.getLayers(); i++)
//				v->vwcBulk[i] += v->swcBulk[Today][i];
//			break;
//
//		case eSW_VWCMatric: /* get swcBulk and convert later */
//			ForEachSoilLayer(i)
//				s->vwcMatric[i] += v->swcBulk[Today][i];
//			break;
//
//		case eSW_SWCBulk:
//			ForEachSoilLayer(i)
//				s->swcBulk[i] += v->swcBulk[Today][i];
//			break;
//
//		case eSW_SWPMatric: /* can't avg swp so get swcBulk and convert later */
//			ForEachSoilLayer(i)
//				s->swpMatric[i] += v->swcBulk[Today][i];
//			break;
//
//		case eSW_SWABulk:
//			ForEachSoilLayer(i)
//				s->swaBulk[i] += fmax (v->swcBulk[Today][i] - SW_Site.lyr[i]->swcBulk_wiltpt, 0.);
//			break;
//
//		case eSW_SWAMatric: /* get swaBulk and convert later */
//			ForEachSoilLayer(i)
//				s->swaMatric[i] += fmax (v->swcBulk[Today][i] - SW_Site.lyr[i]->swcBulk_wiltpt, 0.);
//			break;
//
//		case eSW_SurfaceWater:
//			s->surfaceWater += v->surfaceWater;
//			break;
//
//		case eSW_Transp:
//			ForEachSoilLayer(i)
//			{
//				s->transp_total[i] += v->transpiration_tree[i] + v->transpiration_forb[i] + v->transpiration_shrub[i] + v->transpiration_grass[i];
//				s->transp_tree[i] += v->transpiration_tree[i];
//				s->transp_shrub[i] += v->transpiration_shrub[i];
//				s->transp_forb[i] += v->transpiration_forb[i];
//				s->transp_grass[i] += v->transpiration_grass[i];
//			}
//			break;
//
//		case eSW_EvapSoil:
//			ForEachEvapLayer(i)
//				s->evap[i] += v->evaporation[i];
//			break;
//
//		case eSW_EvapSurface:
//			s->total_evap += v->tree_evap + v->forb_evap + v->shrub_evap + v->grass_evap + v->litter_evap + v->surfaceWater_evap;
//			s->tree_evap += v->tree_evap;
//			s->shrub_evap += v->shrub_evap;
//			s->forb_evap += v->forb_evap;
//			s->grass_evap += v->grass_evap;
//			s->litter_evap += v->litter_evap;
//			s->surfaceWater_evap += v->surfaceWater_evap;
//			break;
//
//		case eSW_Interception:
//			s->total_int += v->tree_int + v->forb_int + v->shrub_int + v->grass_int + v->litter_int;
//			s->tree_int += v->tree_int;
//			s->shrub_int += v->shrub_int;
//			s->forb_int += v->forb_int;
//			s->grass_int += v->grass_int;
//			s->litter_int += v->litter_int;
//			break;
//
//		case eSW_LyrDrain:
//			for (i = 0; i < SW_Site.n_layers - 1; i++)
//				s->lyrdrain[i] += v->drain[i];
//			break;
//
//		case eSW_HydRed:
//			ForEachSoilLayer(i)
//			{
//				s->hydred_total[i] += v->hydred_tree[i] + v->hydred_forb[i] + v->hydred_shrub[i] + v->hydred_grass[i];
//				s->hydred_tree[i] += v->hydred_tree[i];
//				s->hydred_shrub[i] += v->hydred_shrub[i];
//				s->hydred_forb[i] += v->hydred_forb[i];
//				s->hydred_grass[i] += v->hydred_grass[i];
//			}
//			break;
//
//		case eSW_AET:
//			s->aet += v->aet;
//			break;
//
//		case eSW_PET:
//			s->pet += v->pet;
//			break;
//
//		case eSW_WetDays:
//			ForEachSoilLayer(i)
//				if (v->is_wet[i])
//					s->wetdays[i]++;
//			break;
//
//		case eSW_SnowPack:
//			s->snowpack += v->snowpack[Today];
//			s->snowdepth += v->snowdepth;
//			break;
//
//		case eSW_DeepSWC:
//			s->deep += v->swcBulk[Today][SW_Site.deep_lyr];
//			break;
//
//		case eSW_SoilTemp:
//			ForEachSoilLayer(i)
//				s->sTemp[i] += v->sTemp[i];
//			break;
//		default:
//			break;
//		}
	
	}

	private void average_for(ObjType otyp, OutPeriod pd) {
		
	}
	private void collect_sums(ObjType otyp, OutPeriod pd) {
		
	}
	private void _echo_outputs() {
		
	}
	private int get_nColumns(OutKey k) {
		int i=0;
		switch (k) {
		case eSW_NoKey:
			break;
		case eSW_AllWthr:
			break;
		case eSW_Temp:
			i+=3;
			break;
		case eSW_Precip:
			i+=5;
			break;
		case eSW_SoilInf:
			i+=1;
			break;
		case eSW_Runoff:
			i+=3;
			break;
		case eSW_AllH2O:
			break;
		case eSW_VWCBulk:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_VWCMatric:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_SWCBulk:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_SWABulk:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_SWAMatric:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_SWPMatric:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_SurfaceWater:
			i+=1;
			break;
		case eSW_Transp:
			i+=SW_Soils.getLayersInfo().n_layers*5;
			break;
		case eSW_EvapSoil:
			i+=SW_Soils.getLayersInfo().n_evap_lyrs;
			break;
		case eSW_EvapSurface:
			i+=7;
			break;
		case eSW_Interception:
			i+=6;
			break;
		case eSW_LyrDrain:
			i+=SW_Soils.getLayersInfo().n_layers-1;
			break;
		case eSW_HydRed:
			i+=SW_Soils.getLayersInfo().n_layers*5;
			break;
		case eSW_ET:
			break;
		case eSW_AET:
			i+=1;
			break;
		case eSW_PET:
			i+=1;
			break;
		case eSW_WetDays:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_SnowPack:
			i+=2;
			break;
		case eSW_DeepSWC:
			i+=1;
			break;
		case eSW_SoilTemp:
			i+=SW_Soils.getLayersInfo().n_layers;
			break;
		case eSW_AllVeg:
			break;
		case eSW_Estab:
			i+=SW_VegEstab.count();
			break;
		case eSW_LastKey:
			break;
		default:
			break;
		}
		return i;
	}
	private int get_nRows(OutKey k, OutPeriod pd) {
		int tYears = (SW_Model.getEndYear() - SW_Model.getStartYear() + 1);
		int n=0;
		
		switch (pd) {
		case SW_DAY:
			for(int i=SW_Model.getStartYear(); i<=SW_Model.getEndYear(); i++)
				n+=Times.Time_get_lastdoy_y(i);
			n*=(SW_Output[k.idx()].get_PeriodUse(pd)?1:0);
			break;
		case SW_WEEK:
			n = tYears * 53 * (SW_Output[k.idx()].get_PeriodUse(pd)?1:0);
			break;
		case SW_MONTH:
			n = tYears * 12 * (SW_Output[k.idx()].get_PeriodUse(pd)?1:0);
			break;
		case SW_YEAR:
			n = tYears * (SW_Output[k.idx()].get_PeriodUse(pd)?1:0);
			break;
		default:
			n=0;
			break;
		}
		return n;
	}
}
