package input;

import java.nio.file.Path;

import defines.Defines;

public class OutputIn {
	
	private static final int SW_OUTNKEYS=28;/* must also match number of items in enum (minus eSW_NoKey and eSW_LastKey) */
	
	/* These are the keywords to be found in the output setup file */
	/* some of them are from the old fortran model and are no longer */
	/* implemented, but are retained for some tiny measure of backward */
	/* compatibility */
	public enum OutKey {
		eSW_NoKey (1, ""),
		eSW_AllWthr (0, "WTHR"), /* includes all weather vars */
		eSW_Temp (1, "TEMP"),
		eSW_Precip (2, "PRECIP"),
		eSW_SoilInf (3, "SOILINFILT"),
		eSW_Runoff (4, "RUNOFF"),
		/* soil related water quantities */
		eSW_AllH2O (5, "ALLH2O"),
		eSW_VWCBulk (6, "VWCBULK"),
		eSW_VWCMatric (7, "VWCMATRIC"),
		eSW_SWCBulk (8, "SWCBULK"),
		eSW_SWABulk (9, "SWPMATRIC"),
		eSW_SWAMatric (10, "SWABULK"),
		eSW_SWPMatric (11, "SWAMATRIC"),
		eSW_SurfaceWater (12, "SURFACEWATER"),
		eSW_Transp (13, "TRANSP"),
		eSW_EvapSoil (14, "EVAPSOIL"),
		eSW_EvapSurface (15, "EVAPSURFACE"),
		eSW_Interception (16, "INTERCEPTION"),
		eSW_LyrDrain (17, "LYRDRAIN"),
		eSW_HydRed (18,"HYDRED"),
		eSW_ET (19,"ET"),
		eSW_AET (20,"AET"),
		eSW_PET (21,"PET"), /* really belongs in wth), but for historical reasons we'll keep it here */
		eSW_WetDays (22,"WETDAY"),
		eSW_SnowPack (23,"SNOWPACK"),
		eSW_DeepSWC (24,"DEEPSWC"),
		eSW_SoilTemp (25,"SOILTEMP"),
		/* vegetation quantities */
		eSW_AllVeg (26,"ALLVEG"),
		eSW_Estab (27,"ESTABL"), /* make sure this is the last one */
		eSW_LastKey (28, "END");
		
		private final int index;
		private final String name;
		
		private OutKey(int index, String name) {
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
	/* output period specifiers found in input file */
	public enum OutPeriod {
		SW_DAY (0,"DY"),
		SW_WEEK (1,"WK"),
		SW_MONTH (2,"MO"),
		SW_YEAR (3,"YR");
		
		private final int index;
		private final String name;
		
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
	}
	
	private class SW_OUTPUT {
		private OutKey mykey;
		private Defines.ObjType myobj;
		private OutPeriod period;
		private OutSum sumtype;
		private boolean use;
		private int first, last, /* updated for each year */
			first_orig, last_orig;
		private int yr_row, mo_row, wk_row, dy_row;
		private Path file_dy, file_wk, file_mo, file_yr;
		
	}
	
	
	
	public OutputIn() {
		
	}
	
	
}
