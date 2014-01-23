package input;

import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import defines.Defines;

public class OutputSetupIn {
	
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
		public static final String[] comment = {"/* NO KEY */",
			
			"/* max., min, average temperature (C) */",
			"/* total precip = sum(rain, snow), rain, snow-fall, snowmelt, and snowloss (cm)     */"
		};
		
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
	
	private class SW_OUTPUT {
		private OutKey mykey;
		private Defines.ObjType myobj;
		private OutPeriod period;
		private OutSum sumtype;
		private String filename_prefix;
		private boolean use;
		private int first, last, /* updated for each year */
			first_orig, last_orig;
		private int yr_row, mo_row, wk_row, dy_row;
		private Path file_dy, file_wk, file_mo, file_yr;
		
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
	
	private SW_OUTPUT[] SW_Output;
	private String _sep;
	private boolean data;
	private int numPeriod;
	private int numPeriods=4;
	private boolean useTimeStep;
	private int timeStep[];
	private int[][] timeSteps;
	
	public OutputSetupIn() {
		this.SW_Output = new SW_OUTPUT[SW_OUTNKEYS];
		for(int i=0; i<SW_OUTNKEYS; i++)
			this.SW_Output[i] = new SW_OUTPUT();
		this.data = false;
		timeSteps = new int[SW_OUTNKEYS][4];
		numPeriod = 0;
		useTimeStep = false;
		timeStep = new int[4];
	}
	
	public void onRead(Path OutputSetupIn, Path OutputDirectory, boolean deepdrain) throws IOException {
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(OutputSetupIn, StandardCharsets.UTF_8);
		 
		int first=0;
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
							f.LogError(LogMode.LOGERROR, "OutputSetupIn onRead: Insufficient key parameters for item.");
							continue;
						}
					}
					k = OutKey.getEnum(values[0]);
					for(int i=0; i< numPeriods; i++) {
						if(i<1 && !useTimeStep) {
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
					first=1;
					SW_Output[k.idx()].period = OutPeriod.SW_YEAR;
					SW_Output[k.idx()].last = 366;
				} else if(k==OutKey.eSW_AllVeg || k==OutKey.eSW_ET || k==OutKey.eSW_AllWthr || k==OutKey.eSW_AllH2O) {
					SW_Output[k.idx()].use = false;
					f.LogError(LogMode.LOGNOTE, "OutputSetupIn onRead: Unimplemented output key.");
					continue;
				}
				/* check validity of summary type */
				SW_Output[k.idx()].sumtype = OutSum.getEnum(values[1]);
				if (SW_Output[k.idx()].sumtype == OutSum.eSW_Fnl && !(k == OutKey.eSW_VWCBulk || k == OutKey.eSW_VWCMatric || k == OutKey.eSW_SWPMatric || k == OutKey.eSW_SWCBulk || k == OutKey.eSW_SWABulk || k == OutKey.eSW_SWAMatric || k == OutKey.eSW_DeepSWC)) {
					f.LogError(LogMode.LOGWARN, OutputSetupIn.toString()+" : Summary Type FIN with key "+k.key()+" is meaningless.\n"+"  Using type AVG instead.");
					SW_Output[k.idx()].sumtype = OutSum.eSW_Avg;
				}
				/* verify deep drainage parameters */
				if (k == OutKey.eSW_DeepSWC && SW_Output[k.idx()].sumtype != OutSum.eSW_Off && !deepdrain) {
					f.LogError(LogMode.LOGWARN, OutputSetupIn.toString()+" : DEEPSWC cannot be output if flag not set in Site Param.");
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
						f.LogError(LogMode.LOGERROR, "OutputSetupIn onRead: Could not covert start or end."+e.getMessage());
					}
					if (SW_Output[k.idx()].last_orig == 0) {
						f.LogError(LogMode.LOGERROR, "OutputSetupIn onRead : Invalid ending day");
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
			f.LogError(LogMode.LOGWARN, "ProductionIn : onWrite : No data.");
		}
	}
}
