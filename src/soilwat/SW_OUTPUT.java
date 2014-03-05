package soilwat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import soilwat.Defines.ObjType;
import soilwat.InputData.OutputIn;
import soilwat.LogFileIn.LogMode;

public class SW_OUTPUT {

	
	public static final int SW_OUTNKEYS=28;/* must also match number of items in enum (minus eSW_NoKey and eSW_LastKey) */
	
	/* These are the keywords to be found in the output setup file */
	/* some of them are from the old fortran model and are no longer */
	/* implemented, but are retained for some tiny measure of backward */
	/* compatibility */
	public enum OutKey {
		eSW_NoKey (-1, "NOKEY",Defines.ObjType.eNONE),
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
		
		public String getComment() {
			return comment[this.idx()];
		}
		
		private OutKey(int index, String name, Defines.ObjType type) {
			this.index = index;
			this.name = name;
			this.object = type;
		}
		
		public static OutKey fromInt(int key) {
			switch (key) {
			case -1: return eSW_NoKey;
			case 0: return eSW_AllWthr;
			case 1: return eSW_Temp;
			case 2: return eSW_Precip;
			case 3: return eSW_SoilInf;
			case 4: return eSW_Runoff;
			case 5: return eSW_AllH2O;
			case 6: return eSW_VWCBulk;
			case 7: return eSW_VWCMatric;
			case 8: return eSW_SWCBulk;
			case 9: return eSW_SWABulk;
			case 10: return eSW_SWAMatric;
			case 11: return eSW_SWPMatric;
			case 12: return eSW_SurfaceWater;
			case 13: return eSW_Transp;
			case 14: return eSW_EvapSoil;
			case 15: return eSW_EvapSurface;
			case 16: return eSW_Interception;
			case 17: return eSW_LyrDrain;
			case 18: return eSW_HydRed;
			case 19: return eSW_ET;
			case 20: return eSW_AET;
			case 21: return eSW_PET;
			case 22: return eSW_WetDays;
			case 23: return eSW_SnowPack;
			case 24: return eSW_DeepSWC;
			case 25: return eSW_SoilTemp;
			case 26: return eSW_AllVeg;
			case 27: return eSW_Estab;
			case 28: return eSW_LastKey;
			default: return eSW_NoKey;
			}
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
		public static OutPeriod fromInt(int key) {
			switch (key) {
			case 0: return SW_DAY;
			case 1: return SW_WEEK;
			case 2: return SW_MONTH;
			case 3: return SW_YEAR;
			default: return SW_DAY;
			}
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
		public String toString() {
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
		public static OutSum fromInt(int key) {
			switch (key) {
			case 0: return eSW_Off;
			case 1: return eSW_Sum;
			case 2: return eSW_Avg;
			case 3: return eSW_Fnl;
			default: return eSW_Off;
			}
		}
		public static OutSum getEnum(String value) {
			if(value==null)
				throw new IllegalArgumentException();
			for (OutSum v : values()) {
				if(value.equalsIgnoreCase(v.key())) return v;
			}
			throw new IllegalArgumentException();
		}
		public String toString() {
			return this.name;
		}
	}
	
	public class SW_OUT_TIME {
		public int[][] days;
		public int[][] weeks;
		public int[][] months;
		public int[] years;
		public int yrow;
		public int mrow;
		public int wrow;
		public int drow;
		
		public SW_OUT_TIME() {
			days = new int[SW_Model.getDaysInSimulation()][2];
			weeks = new int[SW_Model.getYearsInSimulation() * 53][2];
			months = new int[SW_Model.getYearsInSimulation() * 12][2];
			years = new int[SW_Model.getYearsInSimulation()];
			yrow=0;
			mrow=0;
			wrow=0;
			drow=0;
		}
		
		public void onClear() {
			yrow=mrow=drow=wrow=0;
		}
		
		public int get_nColumns(OutPeriod period) {
			int columns = 0;
			switch (period) {
			case SW_DAY:
				columns = 2;
				break;
			case SW_WEEK:
				columns = 2;
				break;
			case SW_MONTH:
				columns = 2;
				break;
			case SW_YEAR:
				columns = 1;
				break;
			}
			return columns;
		}
		
		public int get_nRows(OutPeriod period) {
			int rows = 0;
			switch (period) {
			case SW_DAY:
				rows = drow;
				break;
			case SW_WEEK:
				rows = wrow;
				break;
			case SW_MONTH:
				rows = mrow;
				break;
			case SW_YEAR:
				rows = yrow;
				break;
			}
			return rows;
		}
		
		public int timingValue(OutPeriod period, int row, int column) {
			int value = 0;
			switch (period) {
			case SW_DAY:
				value = days[row][column];
				break;
			case SW_WEEK:
				value = weeks[row][column];
				break;
			case SW_MONTH:
				value = months[row][column];
				break;
			case SW_YEAR:
				value = years[row];
				break;
			}
			return value;
		}
		
		public String[] getColumnNames(OutPeriod period) {
			String[] value = null;
			switch (period) {
			case SW_DAY:
				value = new String[] {"Year","Day"};
				break;
			case SW_WEEK:
				value = new String[] {"Year","Week"};
				break;
			case SW_MONTH:
				value = new String[] {"Year","Month"};
				break;
			case SW_YEAR:
				value = new String[] {"Year"};
				break;
			}
			return value;
		}
	}
	
	public static class OUTPUT_INPUT_DATA {
		public OutKey mykey;
		public OutSum sumtype;
		public OutPeriod periodColumn;//We need to store this just in case we write out
		public int first_orig, last_orig;
		public String filename_prefix;
		public boolean use = false;
		public void onSet(boolean use, OutSum sumtype, OutPeriod period, int start, int end, String filenamePrefix) {
			this.use = use;
			this.sumtype = sumtype;
			this.periodColumn = period;
			this.first_orig = start;
			this.last_orig = end;
			this.filename_prefix = filenamePrefix;
		}
		@Override
		public String toString() {
			return this.mykey.toString();
		}
	}
	
	private class SW_OUT extends OUTPUT_INPUT_DATA {
		private Defines.ObjType myobj;
		private OutPeriod period;//current period used to pass to get_ functions
		private boolean[] usePeriods;//One is True if !useTimeStep else multiple possible
		private int first, last; /* updated for each year */
		private int yr_row, mo_row, wk_row, dy_row;
		private double[][] dy_data;
		private double[][] wk_data;
		private double[][] mo_data;
		private double[][] yr_data;
		private Path file_dy, file_wk, file_mo, file_yr;
		
		protected SW_OUT() {
			this.usePeriods = new boolean[numPeriods];
			for(int i=0; i<numPeriods; i++)
				this.usePeriods[i] = false;
		}
		protected void onClear() {
			for(int i=0; i<numPeriods; i++) {
				this.usePeriods[i] = false;
			}
			yr_row=mo_row=wk_row=dy_row=0;
			this.use = false;
		}
		protected void setRow(double[] row) {
			switch (period) {
			case SW_DAY:
				if(usePeriods[0]) {
					dy_data[dy_row] = row;
				}
				break;
			case SW_WEEK:
				if(usePeriods[1]) {
					wk_data[wk_row] = row;
				}
				break;
			case SW_MONTH:
				if(usePeriods[2]) {
					mo_data[mo_row] = row;
				}
				break;
			case SW_YEAR:
				if(usePeriods[3]) {
					yr_data[yr_row] = row;
				}
				break;
			default:
				break;
			}
			onIncrement();
		}
		protected void onIncrement() {
			switch (period) {
			case SW_DAY:
				if(usePeriods[0]) dy_row++;
				break;
			case SW_WEEK:
				if(usePeriods[1]) wk_row++;
				break;
			case SW_MONTH:
				if(usePeriods[2]) mo_row++;
				break;
			case SW_YEAR:
				if(usePeriods[3]) yr_row++;
				break;
			default:
				break;
			}
		}
		protected void onAlloc() {
			if(use) {
				for(int i=0; i<4; i++) {
					int rows=get_nRows(i);
					int columns=get_nColumns();
					if(rows>0 && columns>0) {
						switch(i) {
						case 0:
							dy_data = new double[rows][];
							dy_row=0;
							break;
						case 1:
							wk_data = new double[rows][];
							wk_row = 0;
							break;
						case 2:
							mo_data = new double[rows][];
							mo_row = 0;
							break;
						case 3:
							yr_data = new double[rows][];
							yr_row = 0;
							break;
						}
					} else {
						switch(i) {
						case 0:
							dy_data = null;
							dy_row=0;
							break;
						case 1:
							wk_data = null;
							wk_row = 0;
							break;
						case 2:
							mo_data = null;
							mo_row = 0;
							break;
						case 3:
							yr_data = null;
							yr_row = 0;
							break;
						}
					}
				}
			}
		}
		protected void onWrite(String sep) throws IOException {
			if(use) {
				List<String> lines = new ArrayList<String>();
				String line="";
				for(int i=0; i<4; i++) {
					if(usePeriods[i]) {
						switch(i) {
						case 0:
							lines.clear();
							for(int j=0; j<dy_data.length; j++) {
								line="";
								line+=String.format("%4d%s%3d%s", SW_OutTimes.days[j][0], sep, SW_OutTimes.days[j][1], sep);
								for(int k=0; k<dy_data[j].length; k++) {
									line+=String.format("%7f", dy_data[j][k]);
									if(k!=(dy_data[j].length-1))
										line+=sep;
								}
								lines.add(line);
							}
							Files.write(file_dy, lines, StandardCharsets.UTF_8);
							break;
						case 1:
							lines.clear();
							for(int j=0; j<wk_data.length; j++) {
								line="";
								line+=String.format("%4d%s%3d%s", SW_OutTimes.weeks[j][0], sep, SW_OutTimes.weeks[j][1], sep);
								for(int k=0; k<wk_data[j].length; k++) {
									line+=String.format("%7f", wk_data[j][k]);
									if(k!=(wk_data[j].length-1))
										line+=sep;
								}
								lines.add(line);
							}
							Files.write(file_wk, lines, StandardCharsets.UTF_8);
							break;
						case 2:
							lines.clear();
							for(int j=0; j<mo_data.length; j++) {
								line="";
								line+=String.format("%4d%s%3d%s", SW_OutTimes.months[j][0], sep, SW_OutTimes.months[j][1], sep);
								for(int k=0; k<mo_data[j].length; k++) {
									line+=String.format("%7f", mo_data[j][k]);
									if(k!=(mo_data[j].length-1))
										line+=sep;
								}
								lines.add(line);
							}
							Files.write(file_mo, lines, StandardCharsets.UTF_8);
							break;
						case 3:
							lines.clear();
							for(int j=0; j<yr_data.length; j++) {
								line="";
								line+=String.format("%4d%s", SW_OutTimes.years[j], sep);
								for(int k=0; k<yr_data[j].length; k++) {
									line+=String.format("%7f", yr_data[j][k]);
									if(k!=(yr_data[j].length-1))
										line+=sep;
								}
								lines.add(line);
							}
							Files.write(file_yr, lines, StandardCharsets.UTF_8);
							break;
						}
					}
				}
			}
		}
//		
//		public boolean get_PeriodUse(OutPeriod pd) {
//			return usePeriods[pd.idx()];
//		}
		protected boolean get_PeriodUse(int pd) {
			return usePeriods[pd];
		}
		protected int get_nColumns() {
			int i=0;
			switch (mykey) {
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
		protected int get_nRows(int pd) {
			int tYears = (SW_Model.getEndYear() - SW_Model.getStartYear() + 1);
			switch (pd) {
			case 0:
				return SW_Model.getDaysInSimulation()*(usePeriods[pd]?1:0);
			case 1:
				return tYears * 53 * (usePeriods[pd]?1:0);
			case 2:
				return tYears * 12 * (usePeriods[pd]?1:0);
			case 3:
				return tYears * (usePeriods[pd]?1:0);
			default:
				return 0;
			}
		}
		protected double[][] get_dy_data() {
			return dy_data;
		}
		protected double[][] get_wk_data() {
			return wk_data;
		}
		protected double[][] get_mo_data() {
			return mo_data;
		}
		protected double[][] get_yr_data() {
			return yr_data;
		}
		protected String[] getColumnNames() {
			String[] names = null;
			switch (mykey) {
			case eSW_NoKey:
				break;
			case eSW_AllWthr:
				break;
			case eSW_Temp:
				names = new String[]{"Max","Min","Average"};
				break;
			case eSW_Precip:
				names = new String[]{"Total","Rain","Snow Fall","Snowmelt","Snow Loss"};
				break;
			case eSW_SoilInf:
				names = new String[]{"Runoff"};
				break;
			case eSW_Runoff:
				names = new String[]{"Total Runoff","Runoff from Ponded Water","Runoff Snowmelt"};
				break;
			case eSW_AllH2O:
				break;
			case eSW_VWCBulk:
			case eSW_VWCMatric:
			case eSW_SWCBulk:
			case eSW_SWABulk:
			case eSW_SWAMatric:
			case eSW_SWPMatric:
				names = new String[SW_Soils.getLayersInfo().n_layers];
				for(int i=0;i<SW_Soils.getLayersInfo().n_layers;i++) {
					names[i] = "Layer "+String.valueOf(i+1);
				}
				break;
			case eSW_SurfaceWater:
				names = new String[]{"Surface Water"};
				break;
			case eSW_Transp:
				names = new String[SW_Soils.getLayersInfo().n_layers*5];
				for(int i=0;i<SW_Soils.getLayersInfo().n_layers;i++) {
					names[i*5+0] = "Total Layer "+String.valueOf(i+1);
					names[i*5+1] = "Trees Layer "+String.valueOf(i+1);
					names[i*5+2] = "Shrubs Layer "+String.valueOf(i+1);
					names[i*5+3] = "Forbs Layer "+String.valueOf(i+1);
					names[i*5+4] = "Grasses Layer "+String.valueOf(i+1);
				}
				break;
			case eSW_EvapSoil:
				names = new String[SW_Soils.getLayersInfo().n_evap_lyrs];
				for(int i=0;i<SW_Soils.getLayersInfo().n_evap_lyrs;i++) {
					names[i] = "Evaporation Layer "+String.valueOf(i+1);
				}
				break;
			case eSW_EvapSurface:
				names = new String[]{"Total", "Trees", "Shrubs", "Forbs", "Grasses", "Litter", "Surface Water"};
				break;
			case eSW_Interception:
				names = new String[]{"Total", "Trees", "Shrubs", "Forbs", "Grasses", "Litter"};
				break;
			case eSW_LyrDrain:
				names = new String[SW_Soils.getLayersInfo().n_layers-1];
				for(int i=0;i<SW_Soils.getLayersInfo().n_layers-1;i++) {
					names[i] = "Layer "+String.valueOf(i+1);
				}
				break;
			case eSW_HydRed:
				names = new String[SW_Soils.getLayersInfo().n_layers*5];
				for(int i=0;i<SW_Soils.getLayersInfo().n_layers;i++) {
					names[i*5+0] = "Total Layer "+String.valueOf(i+1);
					names[i*5+1] = "Trees Layer "+String.valueOf(i+1);
					names[i*5+2] = "Shrubs Layer "+String.valueOf(i+1);
					names[i*5+3] = "Forbs Layer "+String.valueOf(i+1);
					names[i*5+4] = "Grasses Layer "+String.valueOf(i+1);
				}
				break;
			case eSW_ET:
				break;
			case eSW_AET:
				names = new String[]{"Actual Evaporation"};
				break;
			case eSW_PET:
				names = new String[]{"Potential Evaporation"};
				break;
			case eSW_WetDays:
				names = new String[]{"Days Above SWC_Wet"};
				break;
			case eSW_SnowPack:
				names = new String[]{"Snowpack Water Equivalent","Snowdepth"};
				break;
			case eSW_DeepSWC:
				names = new String[]{"Deep Drainage"};
				break;
			case eSW_SoilTemp:
				names = new String[SW_Soils.getLayersInfo().n_layers];
				for(int i=0;i<SW_Soils.getLayersInfo().n_layers;i++) {
					names[i] = "Layer "+String.valueOf(i+1);
				}
				break;
			case eSW_AllVeg:
				break;
			case eSW_Estab:
				names = new String[SW_VegEstab.count()];
				for(int i=0; i<SW_VegEstab.count();i++) {
					names[i] = SW_VegEstab.get_INFO(i).sppName;
				}
				break;
			case eSW_LastKey:
				break;
			default:
				break;
			}
			return names;
		}
		protected String getUnits() {
			String names = null;
			switch (mykey) {
			case eSW_NoKey:
				break;
			case eSW_AllWthr:
				break;
			case eSW_Temp:
			case eSW_SoilTemp:
				names = "Celsius";
				break;
			case eSW_Precip:
			case eSW_SoilInf:
			case eSW_Runoff:
				names = "cm";
				break;
			case eSW_AllH2O:
				break;
			case eSW_VWCBulk:
			case eSW_VWCMatric:
			case eSW_SWCBulk:
			case eSW_SWABulk:
			case eSW_SWAMatric:
				names = "cm/layer";
				break;
			case eSW_SWPMatric:
				names = "-bars";
				break;
			case eSW_SurfaceWater:
			case eSW_Transp:
			case eSW_EvapSoil:
			case eSW_EvapSurface:
			case eSW_Interception:
			case eSW_LyrDrain:
			case eSW_HydRed:
				names = "cm";
				break;
			case eSW_ET:
				break;
			case eSW_AET:
			case eSW_PET:
				names = "cm";
				break;
			case eSW_WetDays:
				names = "days";
				break;
			case eSW_SnowPack:
			case eSW_DeepSWC:
				names = "cm";
				break;
			case eSW_AllVeg:
				break;
			case eSW_Estab:
				names = "days";
			case eSW_LastKey:
				break;
			default:
				break;
			}
			return names;
		}
		public String toString() {
			return String.format("%13s %7s   %6s      %2s     %3s %15s      %s", mykey.key(),sumtype.key(),periodColumn.key(),this.first_orig, this.last_orig==366?"end":this.last_orig,filename_prefix, mykey.getComment());
		}
	}
	
	private SW_OUT[] SW_Output;
	private SW_OUT_TIME SW_OutTimes;
	private String _sep;
	private boolean data;
	//private int numPeriod;
	private int numPeriods=4;
	private boolean useTimeStep;
	private boolean timeStep[];
	//private int[][] timeSteps;
	private boolean bFlush;
	private boolean tOffset;
	
	private SW_SOILS SW_Soils;
	private SW_SOILWATER SW_SoilWater;
	private SW_MODEL SW_Model;
	private SW_WEATHER SW_Weather;
	private SW_VEGESTAB SW_VegEstab;
	private boolean EchoInits;
	
	
	protected SW_OUTPUT(SW_SOILS SW_Soils, SW_SOILWATER SW_SoilWater, SW_MODEL SW_Model, SW_WEATHER SW_Weather, SW_VEGESTAB SW_VegEstab) {
		this.SW_Output = new SW_OUT[SW_OUTNKEYS];
		for(int i=0; i<SW_OUTNKEYS; i++)
			this.SW_Output[i] = new SW_OUT();
		this.data = false;
		//numPeriod = 0;
		useTimeStep = false;
		timeStep = new boolean[numPeriods];
		for(int i=0; i<numPeriods; i++)
			timeStep[i] = false;//not used default
		this.SW_Soils = SW_Soils;
		this.SW_SoilWater = SW_SoilWater;
		this.SW_Model = SW_Model;
		this.SW_Weather = SW_Weather;
		this.SW_VegEstab = SW_VegEstab;
		bFlush = false;
		tOffset = true;
	}
	
	protected boolean onVerify(boolean deepdrain, Path OutputDirectory) throws Exception {
		LogFileIn f = LogFileIn.getInstance();
		if(data) {
			//Set the outputs for the Periods
			for (OutKey k : OutKey.values()) {
				if(k==OutKey.eSW_NoKey || k==OutKey.eSW_LastKey)
					continue;
				
				for (int i = 0; i < numPeriods; i++) {
					if (SW_Output[k.idx()].get_PeriodUse(i)) {
						String temp = SW_Output[k.idx()].filename_prefix+".";
						switch (i) {
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
				if(SW_Output[k.idx()].sumtype != null)
					SW_Output[k.idx()].use = (SW_Output[k.idx()].sumtype == OutSum.eSW_Off) ? false : true;
				/* Check validity of output key */
				if(k==OutKey.eSW_Estab) {
					SW_Output[k.idx()].use = SW_VegEstab.get_use();
					SW_Output[k.idx()].sumtype = OutSum.eSW_Sum;
					SW_Output[k.idx()].periodColumn = OutPeriod.SW_YEAR;
					SW_Output[k.idx()].last = 366;
				} else if(k==OutKey.eSW_AllVeg || k==OutKey.eSW_ET || k==OutKey.eSW_AllWthr || k==OutKey.eSW_AllH2O) {
					if(SW_Output[k.idx()].use == true) {
						SW_Output[k.idx()].use = false;
						f.LogError(LogMode.NOTE, "OutputSetupIn onRead: Unimplemented output key.");
					}
					continue;
				}
				/* check validity of summary type */
				if (SW_Output[k.idx()].sumtype == OutSum.eSW_Fnl && !(k == OutKey.eSW_VWCBulk || k == OutKey.eSW_VWCMatric || k == OutKey.eSW_SWPMatric || k == OutKey.eSW_SWCBulk || k == OutKey.eSW_SWABulk || k == OutKey.eSW_SWAMatric || k == OutKey.eSW_DeepSWC)) {
					f.LogError(LogMode.WARN, " Summary Type FIN with key "+k.key()+" is meaningless.\n"+"  Using type AVG instead.");
					SW_Output[k.idx()].sumtype = OutSum.eSW_Avg;
				}
				/* verify deep drainage parameters */
				if (k == OutKey.eSW_DeepSWC && SW_Output[k.idx()].sumtype != OutSum.eSW_Off && !deepdrain) {
					f.LogError(LogMode.WARN, " : DEEPSWC cannot be output if flag not set in Site Param.");
					continue;
				}
			}
			onOutputsAlloc();
			SW_OutTimes =  new SW_OUT_TIME();
			
			if(EchoInits)
				_echo_outputs();
			
			return true;
		} else {
			return false;
		}
	}
	
	protected void onClear() {
		for(int i=0; i<numPeriods; i++) {
			SW_Output[i].onClear();
		}
		for(int i=0; i<numPeriods; i++)
			timeStep[i] = false;//not used default
		SW_OutTimes.onClear();
		bFlush = false;
		tOffset = true;
		useTimeStep = false;
		this.data = false;
	}
	
	protected void onSetInput(OutputIn out) {
		this._sep = out.outsep;
		if(out.TimeSteps[0] || out.TimeSteps[1] || out.TimeSteps[2] || out.TimeSteps[3]) {
			this.useTimeStep = true;
			this.timeStep[0] = out.TimeSteps[0];
			this.timeStep[1] = out.TimeSteps[1];
			this.timeStep[2] = out.TimeSteps[2];
			this.timeStep[3] = out.TimeSteps[3];
			for(int i=0; i<SW_OUTNKEYS; i++)//Go through the OUTs set the use Period flags
				this.SW_Output[i].usePeriods = this.timeStep;
		} else {
			this.useTimeStep = false;
		}
		for (OutKey k : OutKey.values()) {
			if(k != OutKey.eSW_NoKey && k != OutKey.eSW_LastKey && k!=OutKey.eSW_AllVeg && k!=OutKey.eSW_ET && k!=OutKey.eSW_AllWthr && k!=OutKey.eSW_AllH2O) {
				if(!useTimeStep) {
					this.timeStep[out.outputs[k.idx()].periodColumn.idx()] = true;
					SW_Output[k.idx()].usePeriods[out.outputs[k.idx()].periodColumn.idx()] = true;
					SW_Output[k.idx()].periodColumn = out.outputs[k.idx()].periodColumn;
				}
				//Set the values		
				SW_Output[k.idx()].mykey = out.outputs[k.idx()].mykey;
				SW_Output[k.idx()].use = out.outputs[k.idx()].use;
				SW_Output[k.idx()].myobj = k.objType();
				SW_Output[k.idx()].sumtype = out.outputs[k.idx()].sumtype;
				SW_Output[k.idx()].periodColumn = out.outputs[k.idx()].periodColumn;
				SW_Output[k.idx()].filename_prefix = out.outputs[k.idx()].filename_prefix;
				SW_Output[k.idx()].first_orig = out.outputs[k.idx()].first_orig;
				SW_Output[k.idx()].last_orig = out.outputs[k.idx()].last_orig;
			}
		}
		this.data = true;
	}
	
	protected void onGetInput(OutputIn out) {
		out.outsep = this._sep;
		if(this.useTimeStep) {
			out.TimeSteps[0] = this.timeStep[0];
			out.TimeSteps[1] = this.timeStep[1];
			out.TimeSteps[2] = this.timeStep[2];
			out.TimeSteps[3] = this.timeStep[3];
		}
		for (OutKey k : OutKey.values()) {
			if(k != OutKey.eSW_NoKey && k != OutKey.eSW_LastKey) {
				//Set the values		
				out.outputs[k.idx()].mykey = k;
				
				out.outputs[k.idx()].use = (SW_Output[k.idx()].sumtype == OutSum.eSW_Off) ? false : true;
				if(k==OutKey.eSW_Estab) {
					out.outputs[k.idx()].use = SW_VegEstab.get_use();
				} else if(k==OutKey.eSW_AllVeg || k==OutKey.eSW_ET || k==OutKey.eSW_AllWthr || k==OutKey.eSW_AllH2O) {
					out.outputs[k.idx()].use = false;
				}

				out.outputs[k.idx()].sumtype = SW_Output[k.idx()].sumtype;
				out.outputs[k.idx()].periodColumn = SW_Output[k.idx()].periodColumn;
				out.outputs[k.idx()].filename_prefix = SW_Output[k.idx()].filename_prefix;
				out.outputs[k.idx()].first_orig = SW_Output[k.idx()].first_orig;
				out.outputs[k.idx()].last_orig = SW_Output[k.idx()].last_orig;
			}
		}
	}
	
	protected void onRead(Path OutputSetupIn) throws Exception {
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
						timeStep[OutPeriod.getEnum(values[i].toUpperCase()).idx()] = true;
						//numPeriod++;
					}
					for(int i=0; i<SW_OUTNKEYS; i++)//Go through the OUTs set the use Period flags
						SW_Output[i].usePeriods = timeStep;
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
					if(!useTimeStep) {
						timeStep[OutPeriod.getEnum(values[2]).idx()] = true;
						SW_Output[k.idx()].usePeriods[OutPeriod.getEnum(values[2]).idx()] = true;
						SW_Output[k.idx()].periodColumn = OutPeriod.getEnum(values[2]);
					}
					
				}
				
				//Set the values		
				SW_Output[k.idx()].mykey = k;
				SW_Output[k.idx()].myobj = k.objType();
				SW_Output[k.idx()].sumtype = OutSum.getEnum(values[1]);
				SW_Output[k.idx()].periodColumn = OutPeriod.getEnum(values[2]);
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
		}
		this.data = true;
	}
	
	protected void onWrite(Path OutputSetupIn) throws Exception {
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
					if(timeStep[i] == true) {
						temp+=" ";
						switch(i) {
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
	
	protected void onWriteOutputs() throws IOException {
		for(int i=0; i<SW_OUTNKEYS; i++)
			SW_Output[i].onWrite(_sep);
	}
	
	protected void onOutputsAlloc() {
		for(int i=0; i<SW_OUTNKEYS; i++)
			SW_Output[i].onAlloc();
	}
	
	public void SW_OUT_flush() throws Exception {
		bFlush = true;
		tOffset = false;
		SW_OUT_sum_today(ObjType.eSWC);
		SW_OUT_sum_today(ObjType.eWTH);
		SW_OUT_sum_today(ObjType.eVES);
		SW_OUT_write_today();
		
		bFlush=false;
		tOffset=true;
	}
	
	protected void SW_OUT_new_year() {
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
	
	protected void SW_OUT_sum_today(Defines.ObjType otyp) throws Exception {
		/* =================================================== */
		/* adds today's output values to week, month and year
		 * accumulators and puts today's values in yesterday's
		 * registers. This is different from the Weather.c approach
		 * which updates Yesterday's registers during the _new_day()
		 * function. It's more logical to update yesterday just
		 * prior to today's calculations, but there's no logical
		 * need to perform _new_day() on the soilwater.
		 */
		SW_SOILWATER.SOILWAT s = SW_SoilWater.getSoilWat();
		SW_WEATHER.WEATHER w = SW_Weather.getWeather();
		
		/* do this every day (kinda expensive but more general than before)*/
		switch (otyp) {
		case eSWC:
			s.dysum.onClear();
			break;
		case eWTH:
			w.dysum.onClear();
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
				w.wksum.onClear();
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
				w.mosum.onClear();
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
				w.mosum.onClear();
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

	protected void SW_OUT_write_today() throws Exception {
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
		
		
		if(SW_Model.get_newyear() || bFlush)
			SW_OutTimes.years[SW_OutTimes.yrow++] = SW_Model.getYear();
		if((SW_Model.get_newmonth() || bFlush) && (((SW_Model.getMonth() + 1) - (tOffset?1:0)) >= 1)) {
			SW_OutTimes.months[SW_OutTimes.mrow][0] = SW_Model.getYear();
			SW_OutTimes.months[SW_OutTimes.mrow++][1] = (SW_Model.getMonth()+1) - (tOffset?1:0);
		}
		if((SW_Model.get_newweek() || bFlush)  && (((SW_Model.getWeek() + 1) - (tOffset?1:0)) >= 1)) {
			SW_OutTimes.weeks[SW_OutTimes.wrow][0] = SW_Model.getYear();
			SW_OutTimes.weeks[SW_OutTimes.wrow++][1] = (SW_Model.getWeek()+1) - (tOffset?1:0);
		}
		if(SW_Model.getDOY() >= 1 && SW_Model.getDOY() <= SW_Model.getLastdoy()) {
			SW_OutTimes.days[SW_OutTimes.drow][0] = SW_Model.getYear();
			SW_OutTimes.days[SW_OutTimes.drow++][1] = SW_Model.getDOY();
		}
		
		
				
		SW_WEATHER.WEATHER w = SW_Weather.getWeather();
		SW_SOILWATER.SOILWAT v = SW_SoilWater.getSoilWat();
		
		for(int k=0; k < SW_OUTNKEYS; k++) {
			SW_OUT output = SW_Output[k];
			if (output.use) {
				for (i = 0; i < numPeriods; i++) { /* will run through this loop for as many periods are being used */
					if (output.get_PeriodUse(i)) {
						writeit = true;
						output.period = OutPeriod.fromInteger(i); /* set the desired period based on the iteration */
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
							t = output.first; /* always output this period */
							break;
						default:
							LogFileIn f = LogFileIn.getInstance();
							f.LogError(LogMode.FATAL, "Invalid period in SW_OUT_write_today().");
						}
						if (!writeit || t < SW_Output[k].first || t > SW_Output[k].last)
							continue;

						
						switch (output.mykey) {
						case eSW_Temp:
							output.setRow(w.getTempRow(output.period));
							break;
						case eSW_Precip:
							output.setRow(w.getPrecipRow(output.period));
							break;
						case eSW_VWCBulk:
							output.setRow(v.get_vwcBulkRow(output.period));
							break;
						case eSW_VWCMatric:
							output.setRow(v.get_vwcMatricRow(output.period));
							break;
						case eSW_SWCBulk:
							output.setRow(v.get_swcBulkRow(output.period));
							break;
						case eSW_SWPMatric:
							output.setRow(v.get_swpMatricRow(output.period));
							break;
						case eSW_SWABulk:
							output.setRow(v.get_swaBulkRow(output.period));
							break;
						case eSW_SWAMatric:
							output.setRow(v.get_swaMatricRow(output.period));
							break;
						case eSW_SurfaceWater:
							output.setRow(v.get_surfaceWater(output.period));
							break;
						case eSW_Runoff:
							output.setRow(w.getRunoffRow(output.period));
							break;
						case eSW_Transp:
							output.setRow(v.get_transp(output.period));
							break;
						case eSW_EvapSoil:
							output.setRow(v.get_evapSoil(output.period));
							break;
						case eSW_EvapSurface:
							output.setRow(v.get_evapSurface(output.period));
							break;
						case eSW_Interception:
							output.setRow(v.get_interception(output.period));
							break;
						case eSW_SoilInf:
							output.setRow(w.getSoilinf(output.period));
							break;
						case eSW_LyrDrain:
							output.setRow(v.get_lyrdrain(output.period));
							break;
						case eSW_HydRed:
							output.setRow(v.get_hydred(output.period));
							break;
						case eSW_AET:
							output.setRow(v.get_aet(output.period));
							break;
						case eSW_PET:
							output.setRow(v.get_pet(output.period));
							break;
						case eSW_WetDays:
							output.setRow(v.get_wetdays(output.period));
							break;
						case eSW_SnowPack:
							output.setRow(v.get_snowpack(output.period));
							break;
						case eSW_DeepSWC:
							output.setRow(v.get_deepswc(output.period));
							break;
						case eSW_SoilTemp:
							output.setRow(v.get_soiltemp(output.period));
							break;
						case eSW_Estab:
							double[] row_data = new double[SW_VegEstab.count()];
							for(int s=0; s<SW_VegEstab.count(); s++) {
								row_data[s] = SW_VegEstab.get_INFO(s).estab_doy;
							}
							output.setRow(row_data);
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}
	
	private void sumof_ves(SW_VEGESTAB v, SW_VEGESTAB.SW_VEGESTAB_OUTPUTS s, OutKey k) {
		/* --------------------------------------------------- */
		/* k is always eSW_Estab, and this only gets called yearly */
		/* in fact, there's nothing to do here as the get_estab()
		 * function does everything needed.  This stub is here only
		 * to facilitate the loop everything else uses.
		 * That is, until we need to start outputting as-yet-unknown
		 * establishment variables.
		 */
		return;
	}
	
	private void sumof_wth(SW_WEATHER.WEATHER v, SW_WEATHER.SW_WEATHER_OUTPUTS s, OutKey k) throws Exception {
		int Today = Defines.Today;

		switch(k) {
		case eSW_Temp:
			s.temp_max += v.now.temp_max[Today];
			s.temp_min += v.now.temp_min[Today];
			s.temp_avg += v.now.temp_avg[Today];
			break;
		case eSW_Precip:
			s.ppt += v.now.ppt[Today];
			s.rain += v.now.rain[Today];
			s.snow += v.now.snow[Today];
			s.snowmelt += v.now.snowmelt[Today];
			s.snowloss += v.now.snowloss[Today];
			break;
		case eSW_SoilInf:
			s.soil_inf += v.soil_inf;
			break;
		case eSW_Runoff:
			s.snowRunoff += v.snowRunoff;
			s.surfaceRunoff += v.surfaceRunoff;
			break;
		default:
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.FATAL, "PGMR: Invalid key in sumof_wth");
			break;
		}
	}
	
	private void sumof_swc(SW_SOILWATER.SOILWAT v, SW_SOILWATER.SW_SOILWAT_OUTPUTS s, OutKey k) {
		int lyrs = SW_Soils.getLayersInfo().n_layers;
		int elyrs = SW_Soils.getLayersInfo().n_evap_lyrs;
		int Today = Defines.Today;
		
		switch (k) {
		case eSW_VWCBulk: /* get swcBulk and convert later */
			for(int i=0; i<lyrs; i++)
				s.vwcBulk[i] += v.swcBulk[Today][i];
			break;

		case eSW_VWCMatric: /* get swcBulk and convert later */
			for(int i=0; i<lyrs; i++)
				s.vwcMatric[i] += v.swcBulk[Today][i];
			break;

		case eSW_SWCBulk:
			for(int i=0; i<lyrs; i++)
				s.swcBulk[i] += v.swcBulk[Today][i];
			break;

		case eSW_SWPMatric: /* can't avg swp so get swcBulk and convert later */
			for(int i=0; i<lyrs; i++)
				s.swpMatric[i] += v.swcBulk[Today][i];
			break;

		case eSW_SWABulk:
			for(int i=0; i<lyrs; i++)
				s.swaBulk[i] += Math.max(v.swcBulk[Today][i] - SW_Soils.getLayer(i).swcBulk_wiltpt, 0.);
			break;

		case eSW_SWAMatric: /* get swaBulk and convert later */
			for(int i=0; i<lyrs; i++)
				s.swaMatric[i] += Math.max(v.swcBulk[Today][i] - SW_Soils.getLayer(i).swcBulk_wiltpt, 0.);
			break;

		case eSW_SurfaceWater:
			s.surfaceWater += v.surfaceWater;
			break;

		case eSW_Transp:
			for(int i=0; i<lyrs; i++)
			{
				s.transp_total[i] += v.transpiration_tree[i] + v.transpiration_forb[i] + v.transpiration_shrub[i] + v.transpiration_grass[i];
				s.transp_tree[i] += v.transpiration_tree[i];
				s.transp_shrub[i] += v.transpiration_shrub[i];
				s.transp_forb[i] += v.transpiration_forb[i];
				s.transp_grass[i] += v.transpiration_grass[i];
			}
			break;

		case eSW_EvapSoil:
			for(int i=0; i<elyrs; i++)
				s.evap[i] += v.evaporation[i];
			break;

		case eSW_EvapSurface:
			s.total_evap += v.tree_evap + v.forb_evap + v.shrub_evap + v.grass_evap + v.litter_evap + v.surfaceWater_evap;
			s.tree_evap += v.tree_evap;
			s.shrub_evap += v.shrub_evap;
			s.forb_evap += v.forb_evap;
			s.grass_evap += v.grass_evap;
			s.litter_evap += v.litter_evap;
			s.surfaceWater_evap += v.surfaceWater_evap;
			break;

		case eSW_Interception:
			s.total_int += v.tree_int + v.forb_int + v.shrub_int + v.grass_int + v.litter_int;
			s.tree_int += v.tree_int;
			s.shrub_int += v.shrub_int;
			s.forb_int += v.forb_int;
			s.grass_int += v.grass_int;
			s.litter_int += v.litter_int;
			break;

		case eSW_LyrDrain:
			for (int i = 0; i < (lyrs - 1); i++)
				s.lyrdrain[i] += v.drain[i];
			break;

		case eSW_HydRed:
			for(int i=0; i<lyrs; i++)
			{
				s.hydred_total[i] += v.hydred_tree[i] + v.hydred_forb[i] + v.hydred_shrub[i] + v.hydred_grass[i];
				s.hydred_tree[i] += v.hydred_tree[i];
				s.hydred_shrub[i] += v.hydred_shrub[i];
				s.hydred_forb[i] += v.hydred_forb[i];
				s.hydred_grass[i] += v.hydred_grass[i];
			}
			break;

		case eSW_AET:
			s.aet += v.aet;
			break;

		case eSW_PET:
			s.pet += v.pet;
			break;

		case eSW_WetDays:
			for(int i=0; i<lyrs; i++)
				if (v.is_wet[i])
					s.wetdays[i]++;
			break;

		case eSW_SnowPack:
			s.snowpack += v.snowpack[Today];
			s.snowdepth += v.snowdepth;
			break;

		case eSW_DeepSWC:
			s.deep += v.swcBulk[Today][SW_Soils.getLayersInfo().deep_lyr];
			break;

		case eSW_SoilTemp:
			for(int i=0; i<lyrs; i++)
				s.sTemp[i] += v.sTemp[i];
			break;
		default:
			break;
		}
	}

	private void average_for(ObjType otyp, OutPeriod pd) throws Exception {
		/* --------------------------------------------------- */
		/* separates the task of obtaining a periodic average.
		 * no need to average days, so this should never be
		 * called with eSW_Day.
		 * Enter this routine just after the summary period
		 * is completed, so the current week and month will be
		 * one greater than the period being summarized.
		 */
		/* 	20091015 (drs) ppt is divided into rain and snow and all three values are output into precip */
		LogFileIn f = LogFileIn.getInstance();
		int Yesterday = Defines.Yesterday;

		SW_SOILWATER.SW_SOILWAT_OUTPUTS savg = null, ssumof = null;
		SW_WEATHER.SW_WEATHER_OUTPUTS wavg = null, wsumof = null;
		int curr_pd = 0;
		double div = 0.; /* if sumtype=AVG, days in period; if sumtype=SUM, 1 */
		
		int lyrs = SW_Soils.getLayersInfo().n_layers;
		int elyrs = SW_Soils.getLayersInfo().n_evap_lyrs;		

		if (!(otyp == ObjType.eSWC || otyp == ObjType.eWTH))
			f.LogError(LogMode.FATAL, "Invalid object type in OUT_averagefor().");

		for(int k=0; k<SW_OUTNKEYS; k++)
		{
			if (SW_Output[k].use) {
				for (int j = 0; j < numPeriods; j++) { /* loop through this code for as many periods that are being used */
					if (SW_Output[k].get_PeriodUse(j)) {
						SW_Output[k].period = OutPeriod.fromInteger(j); /* set the period to use based on the iteration of the for loop */
						switch (pd) {
						case SW_WEEK:
							curr_pd = (SW_Model.getWeek() + 1) - (tOffset?1:0);
							savg =  SW_SoilWater.getSoilWat().wkavg;
							ssumof =  SW_SoilWater.getSoilWat().wksum;
							wavg = SW_Weather.getWeather().wkavg;
							wsumof = SW_Weather.getWeather().wksum;
							div = (bFlush) ? SW_Model.getLastdoy() % Times.WKDAYS : Times.WKDAYS;
							break;

						case SW_MONTH:
							curr_pd = (SW_Model.getMonth() + 1) - (tOffset?1:0);
							savg = SW_SoilWater.getSoilWat().moavg;
							ssumof = SW_SoilWater.getSoilWat().mosum;
							wavg = SW_Weather.getWeather().moavg;
							wsumof = SW_Weather.getWeather().mosum;
							div = Times.Time_days_in_month(Times.Months.fromInteger(SW_Model.getMonth() - (tOffset?1:0)));
							break;

						case SW_YEAR:
							curr_pd = SW_Output[k].first;
							savg = SW_SoilWater.getSoilWat().yravg;
							ssumof = SW_SoilWater.getSoilWat().yrsum;
							wavg = SW_Weather.getWeather().yravg;
							wsumof = SW_Weather.getWeather().yrsum;
							div = SW_Output[k].last - SW_Output[k].first + 1;
							break;

						default:
							f.LogError(LogMode.FATAL, "Programmer: Invalid period in average_for().");
						} /* end switch(pd) */

						if (SW_Output[k].period != pd || SW_Output[k].myobj != otyp || curr_pd < SW_Output[k].first || curr_pd > SW_Output[k].last)
							continue;

						if (SW_Output[k].sumtype == OutSum.eSW_Sum)
							div = 1.;

						/* notice that all valid keys are in this switch */
						switch (OutKey.fromInt(k)) {

						case eSW_Temp:
							wavg.temp_max = wsumof.temp_max / div;
							wavg.temp_min = wsumof.temp_min / div;
							wavg.temp_avg = wsumof.temp_avg / div;
							break;

						case eSW_Precip:
							wavg.ppt = wsumof.ppt / div;
							wavg.rain = wsumof.rain / div;
							wavg.snow = wsumof.snow / div;
							wavg.snowmelt = wsumof.snowmelt / div;
							wavg.snowloss = wsumof.snowloss / div;
							break;

						case eSW_SoilInf:
							wavg.soil_inf = wsumof.soil_inf / div;
							break;

						case eSW_Runoff:
							wavg.snowRunoff = wsumof.snowRunoff / div;
							wavg.surfaceRunoff = wsumof.surfaceRunoff / div;
							break;

						case eSW_SoilTemp:
							for(int i=0; i<lyrs; i++)
							savg.sTemp[i] = (SW_Output[k].sumtype == OutSum.eSW_Fnl) ? SW_SoilWater.getSoilWat().sTemp[i] : ssumof.sTemp[i] / div;
							break;

						case eSW_VWCBulk:
							for(int i=0; i<lyrs; i++)
							/* vwcBulk at this point is identical to swcBulk */
							savg.vwcBulk[i] = (SW_Output[k].sumtype == OutSum.eSW_Fnl) ? SW_SoilWater.getSoilWat().swcBulk[Yesterday][i] : ssumof.vwcBulk[i] / div;
							break;

						case eSW_VWCMatric:
							for(int i=0; i<lyrs; i++)
							/* vwcMatric at this point is identical to swcBulk */
							savg.vwcMatric[i] = (SW_Output[k].sumtype == OutSum.eSW_Fnl) ? SW_SoilWater.getSoilWat().swcBulk[Yesterday][i] : ssumof.vwcMatric[i] / div;
							break;

						case eSW_SWCBulk:
							for(int i=0; i<lyrs; i++)
								savg.swcBulk[i] = (SW_Output[k].sumtype == OutSum.eSW_Fnl) ? SW_SoilWater.getSoilWat().swcBulk[Yesterday][i] : ssumof.swcBulk[i] / div;
							break;

						case eSW_SWPMatric:
							for(int i=0; i<lyrs; i++)
							/* swpMatric at this point is identical to swcBulk */
								savg.swpMatric[i] = (SW_Output[k].sumtype == OutSum.eSW_Fnl) ? SW_SoilWater.getSoilWat().swcBulk[Yesterday][i] : ssumof.swpMatric[i] / div;
							break;

						case eSW_SWABulk:
							for(int i=0; i<lyrs; i++)
								savg.swaBulk[i] =
									(SW_Output[k].sumtype == OutSum.eSW_Fnl) ? Math.max (SW_SoilWater.getSoilWat().swcBulk[Yesterday][i] - SW_Soils.getLayer(i).swcBulk_wiltpt, 0.) : ssumof.swaBulk[i] / div;
							break;

						case eSW_SWAMatric: /* swaMatric at this point is identical to swaBulk */
							for(int i=0; i<lyrs; i++)
								savg.swaMatric[i] =
									(SW_Output[k].sumtype == OutSum.eSW_Fnl) ? Math.max (SW_SoilWater.getSoilWat().swcBulk[Yesterday][i] - SW_Soils.getLayer(i).swcBulk_wiltpt, 0.) : ssumof.swaMatric[i] / div;
							break;

						case eSW_DeepSWC:
							savg.deep = (SW_Output[k].sumtype == OutSum.eSW_Fnl) ? SW_SoilWater.getSoilWat().swcBulk[Yesterday][SW_Soils.getLayersInfo().deep_lyr] : ssumof.deep / div;
							break;

						case eSW_SurfaceWater:
							savg.surfaceWater = ssumof.surfaceWater / div;
							break;

						case eSW_Transp:
							for(int i=0; i<lyrs; i++)
							{
								savg.transp_total[i] = ssumof.transp_total[i] / div;
								savg.transp_tree[i] = ssumof.transp_tree[i] / div;
								savg.transp_shrub[i] = ssumof.transp_shrub[i] / div;
								savg.transp_forb[i] = ssumof.transp_forb[i] / div;
								savg.transp_grass[i] = ssumof.transp_grass[i] / div;
							}
							break;

						case eSW_EvapSoil:
							for(int i=0; i<elyrs; i++)
								savg.evap[i] = ssumof.evap[i] / div;
							break;

						case eSW_EvapSurface:
							savg.total_evap = ssumof.total_evap / div;
							savg.tree_evap = ssumof.tree_evap / div;
							savg.shrub_evap = ssumof.shrub_evap / div;
							savg.forb_evap = ssumof.forb_evap / div;
							savg.grass_evap = ssumof.grass_evap / div;
							savg.litter_evap = ssumof.litter_evap / div;
							savg.surfaceWater_evap = ssumof.surfaceWater_evap / div;
							break;

						case eSW_Interception:
							savg.total_int = ssumof.total_int / div;
							savg.tree_int = ssumof.tree_int / div;
							savg.shrub_int = ssumof.shrub_int / div;
							savg.forb_int = ssumof.forb_int / div;
							savg.grass_int = ssumof.grass_int / div;
							savg.litter_int = ssumof.litter_int / div;
							break;

						case eSW_AET:
							savg.aet = ssumof.aet / div;
							break;

						case eSW_LyrDrain:
							for (int i = 0; i < (lyrs - 1); i++)
								savg.lyrdrain[i] = ssumof.lyrdrain[i] / div;
							break;

						case eSW_HydRed:
							for(int i=0; i<lyrs; i++)
							{
								savg.hydred_total[i] = ssumof.hydred_total[i] / div;
								savg.hydred_tree[i] = ssumof.hydred_tree[i] / div;
								savg.hydred_shrub[i] = ssumof.hydred_shrub[i] / div;
								savg.hydred_forb[i] = ssumof.hydred_forb[i] / div;
								savg.hydred_grass[i] = ssumof.hydred_grass[i] / div;
							}
							break;

						case eSW_PET:
							savg.pet = ssumof.pet / div;
							break;

						case eSW_WetDays:
							for(int i=0; i<lyrs; i++)
							savg.wetdays[i] = ssumof.wetdays[i] / div;
							break;

						case eSW_SnowPack:
							savg.snowpack = ssumof.snowpack / div;
							savg.snowdepth = ssumof.snowdepth / div;
							break;

						case eSW_Estab: /* do nothing, no averaging required */
							break;

						default:
							f.LogError(LogMode.FATAL, "PGMR: Invalid key in average_for "+OutKey.fromInt(k).toString());
						}
					}
				} /* end of for loop */
			}
		} /* end ForEachKey */
	}
	
	private void collect_sums(ObjType otyp, OutPeriod op) throws Exception {
		/* --------------------------------------------------- */
		SW_SOILWATER.SOILWAT s = SW_SoilWater.getSoilWat();
		SW_SOILWATER.SW_SOILWAT_OUTPUTS ssum = null;
		SW_WEATHER.WEATHER w = SW_Weather.getWeather();
		SW_WEATHER.SW_WEATHER_OUTPUTS wsum = null;
		SW_VEGESTAB v = SW_VegEstab; /* vegestab only gets summed yearly */
		SW_VEGESTAB.SW_VEGESTAB_OUTPUTS vsum = null;

		LogFileIn f = LogFileIn.getInstance();
		int pd = 0;

		for(int k=0; k<SW_OUTNKEYS; k++)
		{
			if (otyp != SW_Output[k].myobj || !SW_Output[k].use)
				continue;
			switch (op) {
			case SW_DAY:
				pd = SW_Model.getDOY();
				ssum = s.dysum;
				wsum = w.dysum;
				break;
			case SW_WEEK:
				pd = SW_Model.getWeek() + 1;
				ssum = s.wksum;
				wsum = w.wksum;
				break;
			case SW_MONTH:
				pd = SW_Model.getMonth() + 1;
				ssum = s.mosum;
				wsum = w.mosum;
				break;
			case SW_YEAR:
				pd = SW_Model.getDOY();
				ssum = s.yrsum;
				wsum = w.yrsum;
				vsum = v.get_yrsum(); /* yearly, y'see */
				break;
			default:
				f.LogError(LogMode.FATAL, "PGMR: Invalid outperiod in collect_sums()");
			}

			if (pd >= SW_Output[k].first && pd <= SW_Output[k].last) {
				switch (otyp) {
				case eSWC:
					sumof_swc(s, ssum, OutKey.fromInt(k));
					break;
				case eWTH:
					sumof_wth(w, wsum, OutKey.fromInt(k));
					break;
				case eVES:
					sumof_ves(v, vsum, OutKey.fromInt(k));
					break;
				default:
					break;
				}
			}

		} /* end ForEachOutKey */
	}
	
	private void _echo_outputs() throws Exception {
		/* --------------------------------------------------- */
		LogFileIn f = LogFileIn.getInstance();
		String outconfig ="";
		outconfig += "\n===============================================\n  Output Configuration:\n";
		for(int k=0; k<SW_OUTNKEYS; k++)
		{
			if (!SW_Output[k].use)
				continue;
			outconfig += "---------------------------\nKey "+OutKey.fromInt(k).toString();
			outconfig += "\n\tSummary Type: " + SW_Output[k].sumtype.toString();
			outconfig += "\n\tOutput Period: " + SW_Output[k].periodColumn.toString();
			outconfig += String.format("\n\tStart period: %d", SW_Output[k].first_orig);
			outconfig += String.format("\n\tEnd period  : %d", SW_Output[k].last_orig);
			String temp = "";
			if(SW_Output[k].usePeriods[0])
				temp+=SW_Output[k].file_dy.getFileName().toString()+"  ";
			if(SW_Output[k].usePeriods[1])
				temp+=SW_Output[k].file_wk.getFileName().toString()+"  ";
			if(SW_Output[k].usePeriods[2])
				temp+=SW_Output[k].file_mo.getFileName().toString()+"  ";
			if(SW_Output[k].usePeriods[3])
				temp+=SW_Output[k].file_yr.getFileName().toString()+"  ";
			outconfig += "\n\tOutput File(s): " + temp;
			outconfig += "\n";
		}
		outconfig += "\n----------  End of Output Configuration ---------- \n";
		f.LogError(LogMode.NOTE, outconfig);
	}
	
	protected boolean get_echoinits() {
		return this.EchoInits;
	}
	
	protected void set_echoinits(boolean echo) {
		this.EchoInits = echo;
	}
	
	protected double[][] get_data(OutKey key, OutPeriod period) {
		SW_OUT out = this.SW_Output[key.idx()];
		switch (period) {
		case SW_DAY:
			return out.get_dy_data();
		case SW_WEEK:
			return out.get_wk_data();
		case SW_MONTH:
			return out.get_mo_data();
		case SW_YEAR:
			return out.get_yr_data();
		default:
			return null;
		}
	}
	
	protected SW_OUT_TIME get_Timing() {
		return this.SW_OutTimes;
	}
	
	protected String[] get_ColumnNames(OutKey key) {
		SW_OUT out = this.SW_Output[key.idx()];
		return out.getColumnNames();
	}
	
	protected String get_Unit(OutKey key) {
		SW_OUT out = this.SW_Output[key.idx()];
		return out.getUnits();
	}
	
	protected int get_nColumns(OutKey key) {
		SW_OUT out = this.SW_Output[key.idx()];
		return out.get_nColumns();
	}
}
