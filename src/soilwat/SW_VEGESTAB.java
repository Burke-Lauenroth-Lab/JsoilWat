package soilwat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import soilwat.InputData.EstabIn;
import soilwat.LogFileIn.LogMode;

//NEED SW_LAYER_INFO

public class SW_VEGESTAB {

	public static final int SW_GERM_BARS=0;
	public static final int SW_ESTAB_BARS=1;
	
	public static class SPP_INPUT_DATA {
		/* THESE VARIABLES DO NOT CHANGE DURING THE NORMAL MODEL RUN */
		public class SoilLayerParams {
			public int estab_lyrs;	/* estab could conceivably need more than one layer */
									/* swc is averaged over these top layers to compare to */
									/* the converted value from min_swc_estab */
			public double bars[] = new double[2]; 			/* read from input, saved for reporting */
			public void onSet(int estab_lyrs, double swpGermination, double swpEstablishment) {
				this.estab_lyrs = estab_lyrs;
				this.bars[SW_GERM_BARS] = swpGermination;
				this.bars[SW_ESTAB_BARS] = swpEstablishment;
			}
			
			public String toString() {
				String out = "";
				out += "# soil layer parameters\n";
				out += String.format("%-7d%s", estab_lyrs,"\t# number of layers affecting establishment\n");
				out += String.format("%-7.2f%s", bars[SW_GERM_BARS], "\t# SWP (bars) requirement for germination (top layer)\n");
				out += String.format("%-7.2f%s", bars[SW_ESTAB_BARS], "\t# SWP (bars) requirement for establishment (average of top layers)\n");
				return out;
			}
		}
		public class TimingParams {
			public int min_pregerm_days, /* first possible day of germination */
			max_pregerm_days, 		/* last possible day of germination */
			min_wetdays_for_germ, 	/* number of consecutive days top layer must be */
									/* "wet" in order for germination to occur. */
			max_drydays_postgerm, 	/* maximum number of consecutive dry days after */
									/* germination before establishment can no longer occur. */
			min_wetdays_for_estab, 	/* minimum number of consecutive days the top layer */
									/* must be "wet" in order to establish */
			min_days_germ2estab, 	/* minimum number of days to wait after germination */
									/* and seminal roots wet before check for estab. */
			max_days_germ2estab; 	/* maximum number of days after germination to wait */
									/* for establishment */
			public void onSet(int min_pregerm_days, int max_pregerm_days, int min_wetdays_for_germ, int max_drydays_postgerm,
					int min_wetdays_for_estab, int min_days_germ2estab, int max_days_germ2estab) {
				this.min_pregerm_days = min_pregerm_days;
				this.max_pregerm_days = max_pregerm_days;
				this.min_wetdays_for_germ = min_wetdays_for_germ;
				this.max_drydays_postgerm = max_drydays_postgerm;
				this.min_wetdays_for_estab = min_wetdays_for_estab;
				this.min_days_germ2estab = min_days_germ2estab;
				this.max_days_germ2estab = max_days_germ2estab;
			}
			
			public String toString() {
				String out = "";
				out += "# timing parameters in days\n";
				out += String.format("%-7d%s", min_pregerm_days, "\t# first possible day of germination\n");
				out += String.format("%-7d%s", max_pregerm_days, "\t# last possible day of germination\n");
				out += String.format("%-7d%s", min_wetdays_for_germ, "\t# min number of consecutive 'wet' days for germination to occur\n");
				out += String.format("%-7d%s", max_drydays_postgerm, "\t# max number of consecutive 'dry' days after germination allowing estab\n");
				out += String.format("%-7d%s", min_wetdays_for_estab, "\t# min number of consecutive 'wet' days after germination before establishment\n");
				out += String.format("%-7d%s", min_days_germ2estab, "\t# min number of days between germination and establishment\n");
				out += String.format("%-7d%s", max_days_germ2estab, "\t# max number of days between germination and establishment\n");
				return out;
			}
		}
		public class TempParams {
			public double min_temp_germ, 	/* min avg daily temp req't for germination */
			max_temp_germ, 	/* max temp for germ in degC */
			min_temp_estab, /* min avg daily temp req't for establishment */
			max_temp_estab; /* max temp for estab in degC */
			public void onSet(double min_temp_germ, double max_temp_germ, double min_temp_estab, double max_temp_estab) {
				this.min_temp_germ = min_temp_germ;
				this.max_temp_germ = max_temp_germ;
				this.min_temp_estab = min_temp_estab;
				this.max_temp_estab = max_temp_estab;
			}
			
			public String toString() {
				String out = "";
				out += "# temperature parameters in C\n";
				out += String.format("%-7.2f%s", min_temp_germ, "\t# min temp threshold for germination\n");
				out += String.format("%-7.2f%s", max_temp_germ, "\t# max temp threshold for germination\n");
				out += String.format("%-7.2f%s", min_temp_estab, "\t# min temp threshold for establishment\n");
				out += String.format("%-7.2f%s", max_temp_estab, "\t# max temp threshold for establishment\n");
				return out;
			}
		}
		public SoilLayerParams soilLayerParams = new SoilLayerParams();
		public TimingParams timingParams = new TimingParams();
		public TempParams tempParams = new TempParams();
		public String sppName = "";
		private LogFileIn log;
		
		public SPP_INPUT_DATA(LogFileIn log) {
			this.log = log;
		}
		
		public void onRead(String sppFile) throws Exception {
			int nitems=15, lineno=0;
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(sppFile, getClass().getClassLoader());
			
			for (String line : lines) {
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
					switch (lineno) {
					case 0:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for name line.");
						sppName = values[0];
						break;
					case 1:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for number of layers affecting establishment.");
						try {
							soilLayerParams.estab_lyrs = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert number of layers affecting establishment line.");
						}
						break;
					case 2:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for SWP (bars) requirement for germination (top layer).");
						try {
							soilLayerParams.bars[SW_GERM_BARS] = Double.valueOf(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert SWP (bars) requirement for germination (top layer) line.");
						}
						break;
					case 3:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for SWP (bars) requirement for establishment (average of top layers).");
						try {
							soilLayerParams.bars[SW_ESTAB_BARS] = Double.valueOf(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert SWP (bars) requirement for establishment (average of top layers) line.");
						}
						break;
					case 4:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for first possible day of germination.");
						try {
							timingParams.min_pregerm_days = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert first possible day of germination line.");
						}
						break;
					case 5:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for last possible day of germination.");
						try {
							timingParams.max_pregerm_days = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert last possible day of germination line.");
						}
						break;
					case 6:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min number of consecutive 'wet' days for germination to occur.");
						try {
							timingParams.min_wetdays_for_germ = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min number of consecutive 'wet' days for germination to occur line.");
						}
						break;
					case 7:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max number of consecutive 'dry' days after germination allowing estab.");
						try {
							timingParams.max_drydays_postgerm = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max number of consecutive 'dry' days after germination allowing estab line.");
						}
						break;
					case 8:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min number of consecutive 'wet' days after germination before establishment.");
						try {
							timingParams.min_wetdays_for_estab = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert consecutive 'wet' days after germination before establishment line.");
						}
						break;
					case 9:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min number of days between germination and establishment.");
						try {
							timingParams.min_days_germ2estab = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min number of days between germination and establishment line.");
						}
						break;
					case 10:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max number of days between germination and establishment.");
						try {
							timingParams.max_days_germ2estab = Integer.parseInt(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max number of days between germination and establishment line.");
						}
						break;
					case 11:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min temp threshold for germination.");
						try {
							tempParams.min_temp_germ = Double.valueOf(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min temp threshold for germination line.");
						}
						break;
					case 12:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max temp threshold for germination.");
						try {
							tempParams.max_temp_germ = Double.valueOf(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max temp threshold for germination line.");
						}
						break;
					case 13:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min temp threshold for establishment.");
						try {
							tempParams.min_temp_estab = Double.valueOf(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min temp threshold for establishment line.");
						}
						break;
					case 14:
						if(values.length > 1)
							f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max temp threshold for establishment.");
						try {
							tempParams.max_temp_estab = Double.valueOf(values[0]);
						} catch(NumberFormatException e) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max temp threshold for establishment line.");
						}
						break;
					}
					if(lineno == 0) {
						if(sppName.length() > 4) {
							f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max temp threshold for establishment line.");
						}
					}
					lineno++;
				}
			}
			if(lineno  < nitems) {
				f.LogError(LogMode.ERROR, "EstabIn _spp_read : Too few input parameters.");
			}
		}
		
		public void onWrite(String sppFile) throws Exception {
			Path sppfile = Paths.get(sppFile);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(sppfile, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out = "";
			out += sppName + "\t" + "# 4-char name of species\n";
			out += soilLayerParams.toString();
			out += timingParams.toString();
			out += tempParams.toString();
			return out;
		}
		//public void onSet(String sppName, int )
	}
	
	protected class SW_VEGESTAB_INFO extends SPP_INPUT_DATA {
		public String sppFileName;
		/* THESE VARIABLES CAN CHANGE VALUE IN THE MODEL */
		public int estab_doy, 	/* day of establishment for this plant */
		germ_days, 				/* elapsed days since germination with no estab */
		drydays_postgerm, 		/* did sprout get too dry for estab? */
		wetdays_for_germ, 		/* keep track of consecutive wet days */
		wetdays_for_estab;
		public boolean germd, 	/* has this plant germinated yet?  */
		no_estab; 				/* if TRUE, can't attempt estab for remainder of year */
		public double min_swc_germ, 	/* wetting point required for germination converted from */
										/* bars to cm per layer for efficiency in the loop */
		min_swc_estab; 					/* same as min_swc_germ but for establishment */
										/* this is the average of the swc of the first estab_lyrs */
				
		public boolean data;
		
		protected SW_VEGESTAB_INFO() {
			super(null);//This will not use read write functions
			this.data=false;
		}
	}
	
	protected class SW_VEGESTAB_OUTPUTS {
		private int[] days = null;
	}
	
	private boolean use;//if true use establishment parms and chkestab()
	private int count; 	//number of species to check
	private List<Path> tempPath;
	private Path prjDirPath;
	private List<SW_VEGESTAB_INFO> params; /* dynamic array of parms for each species */
	private SW_VEGESTAB_OUTPUTS yrsum, /* conforms to the requirements of the output module */
								yravg; /* note that there's only one period for output */
									   /* see also the soilwater and weather modules */
	public boolean data;
	private SW_WEATHER SW_Weather;
	private SW_SOILWATER SW_Soilwat;
	private SW_MODEL SW_Model;
	private SW_SOILS SW_Soils;
	private boolean EchoInits;
	private LogFileIn log;
	
	protected SW_VEGESTAB(LogFileIn log, SW_WEATHER SW_Weather, SW_SOILWATER SW_SoilWater, SW_MODEL SW_Model, SW_SOILS SW_Soils) {
		this.log = log;
		this.use = false;
		this.count = 0;
		this.params = new ArrayList<SW_VEGESTAB_INFO>();
		this.tempPath = new ArrayList<Path>();
		this.yravg = new SW_VEGESTAB_OUTPUTS();
		this.yrsum = new SW_VEGESTAB_OUTPUTS();
		this.data = false;
		this.SW_Weather = SW_Weather;
		this.SW_Soilwat = SW_SoilWater;
		this.SW_Model = SW_Model;
		this.SW_Soils = SW_Soils;
	}
	
	protected boolean onVerify() throws Exception {
		if(use) {
			for(int i=0; i<this.count; i++)
				_spp_init(i);
			if(this.params.size() > 0) {
				this.yrsum.days = new int[this.params.size()];
			}
			if(EchoInits)
				_echo_inits();
		}
		return true;
	}
	
	protected SW_VEGESTAB_OUTPUTS get_yrsum() {
		return yrsum;
	}
	
	protected void onClear() {
		this.tempPath.clear();
		this.params.clear();
		this.data = false;
		this.yravg.days = null;
		this.yravg.days = null;
	}
	
	protected void SW_VES_new_year() {
		if(this.count == 0)
			return;
		
		for(int i=0; i<this.count; i++)
			this.yrsum.days[i] = 0;
	}
	
	protected void onCheckEstab(int modelDOY) {
		for(int i=0; i<this.count;i++)
			_checkit(modelDOY, i);
	}
	
	protected void onSetInput(EstabIn estabIn) {
		tempPath.clear();
		this.count = 0;
		this.params.clear();
		
		this.use = estabIn.use;
		for (String spp : estabIn.estabFiles) {
			tempPath.add(Paths.get(spp));
		}
		if(use) {
			int i=0;
			for (SPP_INPUT_DATA spp : estabIn.spps) {
				this.onSetSPP(spp, estabIn.estabFiles.get(i));
				i++;
			}
		}
	}
	
	protected void onGetInput(EstabIn estabIn) {
		estabIn.use = this.use;
		estabIn.estabFiles.clear();
		estabIn.spps.clear();
		if(use) {
			for(int i=0; i<this.count; i++) {
				estabIn.estabFiles.add(this.prjDirPath.relativize(tempPath.get(i)).toString());
				estabIn.spps.add(new SPP_INPUT_DATA(log));
				onGetSPP(estabIn.spps.get(i), this.params.get(i));
			}
		}
	}
	
	private void onSetSPP(SPP_INPUT_DATA spp, String sppFileName) {
		this.params.add(new SW_VEGESTAB_INFO());
		this.count = this.params.size();
		SW_VEGESTAB_INFO v = this.params.get(this.count-1);
		v.sppFileName = sppFileName;
		
		v.sppName = spp.sppName;
		v.soilLayerParams.estab_lyrs = spp.soilLayerParams.estab_lyrs;
		v.soilLayerParams.bars[SW_GERM_BARS] = spp.soilLayerParams.bars[SW_GERM_BARS];
		v.soilLayerParams.bars[SW_ESTAB_BARS] = spp.soilLayerParams.bars[SW_ESTAB_BARS];
		v.timingParams.min_pregerm_days = spp.timingParams.min_pregerm_days;
		v.timingParams.max_pregerm_days = spp.timingParams.max_pregerm_days;
		v.timingParams.min_wetdays_for_germ = spp.timingParams.min_wetdays_for_germ;
		v.timingParams.max_drydays_postgerm = spp.timingParams.max_drydays_postgerm;
		v.timingParams.min_wetdays_for_estab = spp.timingParams.min_wetdays_for_estab;
		v.timingParams.min_days_germ2estab = spp.timingParams.min_days_germ2estab;
		v.timingParams.max_days_germ2estab = spp.timingParams.max_days_germ2estab;
		v.tempParams.min_temp_germ = spp.tempParams.min_temp_germ;
		v.tempParams.max_temp_germ = spp.tempParams.max_temp_germ;
		v.tempParams.min_temp_estab = spp.tempParams.min_temp_estab;
		v.tempParams.max_temp_estab = spp.tempParams.max_temp_estab;
		v.data = true;
		
		this.data = true;
	}
	private void onGetSPP(SPP_INPUT_DATA spp, SW_VEGESTAB_INFO v) {
		spp.sppName = v.sppName;
		spp.soilLayerParams.estab_lyrs = v.soilLayerParams.estab_lyrs;
		spp.soilLayerParams.bars[SW_GERM_BARS] = v.soilLayerParams.bars[SW_GERM_BARS];
		spp.soilLayerParams.bars[SW_ESTAB_BARS] = v.soilLayerParams.bars[SW_ESTAB_BARS];
		spp.timingParams.min_pregerm_days = v.timingParams.min_pregerm_days;
		spp.timingParams.max_pregerm_days = v.timingParams.max_pregerm_days;
		spp.timingParams.min_wetdays_for_germ = v.timingParams.min_wetdays_for_germ;
		spp.timingParams.max_drydays_postgerm = v.timingParams.max_drydays_postgerm;
		spp.timingParams.min_wetdays_for_estab = v.timingParams.min_wetdays_for_estab;
		spp.timingParams.min_days_germ2estab = v.timingParams.min_days_germ2estab;
		spp.timingParams.max_days_germ2estab = v.timingParams.max_days_germ2estab;
		spp.tempParams.min_temp_germ = v.tempParams.min_temp_germ;
		spp.tempParams.max_temp_germ = v.tempParams.max_temp_germ;
		spp.tempParams.min_temp_estab = v.tempParams.min_temp_estab;
		spp.tempParams.max_temp_estab = v.tempParams.max_temp_estab;
	}
	
	private void _checkit(int doy, int sppnum) {

		SW_VEGESTAB_INFO v = params.get(sppnum);
		SW_WEATHER.SW_WEATHER_2DAYS wn = SW_Weather.getNow();
		SW_SOILWATER.SOILWAT sw = SW_Soilwat.getSoilWat();
		int Today = Defines.Today;

		int i;
		
		double avgtemp = wn.temp_avg[Today], /* avg of today's min/max temp */
				avgswc; /* avg_swc today */

		if (doy == SW_Model.getFirstdoy()) {
			_zero_state(sppnum);
		}

		if (v.no_estab || v.estab_doy > 0)
			return;

		/* keep up with germinating wetness regardless of current state */
		if (Defines.GT(sw.swcBulk[Today][0], v.min_swc_germ))
			v.wetdays_for_germ++;
		else
			v.wetdays_for_germ = 0;

		if (doy < v.timingParams.min_pregerm_days)
			return;

		/* ---- check for germination, establishment */
		if (!v.germd && v.wetdays_for_germ >= v.timingParams.min_wetdays_for_germ) {

			if (doy < v.timingParams.min_pregerm_days)
				return;
			if (doy > v.timingParams.max_pregerm_days) {
				v.no_estab = true;
				return;
			}
			/* temp doesn't affect wetdays */
			if (Defines.LT(avgtemp, v.tempParams.min_temp_germ) || Defines.GT(avgtemp, v.tempParams.max_temp_germ))
				return;

			v.germd = true;
			return;

		} else { /* continue monitoring sprout's progress */

			/* any dry period (> max_drydays) or temp out of range
			 * after germination means restart */
			avgswc = 0;
			for (i = 0; i < v.soilLayerParams.estab_lyrs;)
				avgswc += sw.swcBulk[Today][i++];
			avgswc /= (double) v.soilLayerParams.estab_lyrs;
			if (Defines.LT(avgswc, v.min_swc_estab)) {
				v.drydays_postgerm++;
				v.wetdays_for_estab = 0;
			} else {
				v.drydays_postgerm = 0;
				v.wetdays_for_estab++;
			}

			if (v.drydays_postgerm > v.timingParams.max_drydays_postgerm || Defines.LT(avgtemp, v.tempParams.min_temp_estab) || Defines.GT(avgtemp, v.tempParams.max_temp_estab)) {
				/* too bad: discontinuity in environment, plant dies, start over */
				_EstabFailed(sppnum);
				return;
			}

			v.germ_days++;

			if (v.wetdays_for_estab < v.timingParams.min_wetdays_for_estab || v.germ_days < v.timingParams.min_days_germ2estab) {
				return;
				/* no need to zero anything */
			}

			if (v.germ_days > v.timingParams.max_days_germ2estab) {
				_EstabFailed(sppnum);
				return;
			}

			v.estab_doy = SW_Model.getDOY();
			return;
		}
	}
	
	private void _EstabFailed(int sppnum) {
		SW_VEGESTAB_INFO v = params.get(sppnum);
		/* allows us to try again if not too late */
		v.wetdays_for_estab = 0;
		v.germ_days = 0;
		v.germd = false;
	}
	
	private void _zero_state(int sppnum) {
		SW_VEGESTAB_INFO v = this.params.get(sppnum);
		v.no_estab = v.germd = false;
		v.estab_doy=v.germ_days=v.drydays_postgerm=0;
		v.wetdays_for_germ=v.wetdays_for_estab=0;
	}
	
	private void _spp_init(int sppnum) throws Exception {
		/* =================================================== */
		/* initializations performed after acquiring parameters
		 * after read() or some other function call.
		 */
		SW_VEGESTAB_INFO v = params.get(sppnum);
		int i;

		/* The thetas and psis etc should be initialized by now */
		/* because init_layers() must be called prior to this routine */
		/* (see watereqn() ) */
		v.min_swc_germ = SW_SOILWATER.SW_SWPmatric2VWCBulk(SW_Soils.getLayer(0).fractionVolBulk_gravel, v.soilLayerParams.bars[SW_GERM_BARS], SW_Soils.getLayer(0).psisMatric, SW_Soils.getLayer(0).binverseMatric, SW_Soils.getLayer(0).thetasMatric) * SW_Soils.getLayer(0).width;

		/* due to possible differences in layer textures and widths, we need
		 * to average the estab swc across the given layers to peoperly
		 * compare the actual swc average in the checkit() routine */
		v.min_swc_estab = 0.;
		for (i = 0; i < v.soilLayerParams.estab_lyrs; i++)
			v.min_swc_estab += SW_SOILWATER.SW_SWPmatric2VWCBulk(SW_Soils.getLayer(i).fractionVolBulk_gravel, v.soilLayerParams.bars[SW_ESTAB_BARS], SW_Soils.getLayer(i).psisMatric, SW_Soils.getLayer(i).binverseMatric, SW_Soils.getLayer(i).thetasMatric) * SW_Soils.getLayer(i).width;
		v.min_swc_estab /= v.soilLayerParams.estab_lyrs;

		_sanity_check(sppnum);
	}
	
	private void _sanity_check(int sppnum) throws Exception {
		/* =================================================== */
		LogFileIn f = log;
		
		SW_VEGESTAB_INFO v = params.get(sppnum);
		double min_transp_lyrs;

		min_transp_lyrs = Math.min(SW_Soils.layersInfo.n_transp_lyrs_tree, Math.min(SW_Soils.layersInfo.n_transp_lyrs_forb, Math.min(SW_Soils.layersInfo.n_transp_lyrs_shrub, SW_Soils.layersInfo.n_transp_lyrs_grass)));

		if (v.soilLayerParams.estab_lyrs > min_transp_lyrs) {
			f.LogError(LogMode.FATAL, String.format( "%s : Layers requested (estab_lyrs) > (# transpiration layers=%d).", v.sppFileName, min_transp_lyrs));
		}

		if (v.timingParams.min_pregerm_days > v.timingParams.max_pregerm_days) {
			f.LogError(LogMode.FATAL, String.format( "%s : First day of germination > last day of germination.", v.sppFileName));
		}

		if (v.timingParams.min_wetdays_for_estab > v.timingParams.max_days_germ2estab) {
			f.LogError(LogMode.FATAL, String.format( "%s : Minimum wetdays after germination (%d) > maximum days allowed for establishment (%d).", v.sppFileName, v.timingParams.min_wetdays_for_estab, v.timingParams.max_days_germ2estab));
		}

		if (v.min_swc_germ < SW_Soils.getLayer(0).swcBulk_wiltpt) {
			f.LogError(LogMode.FATAL, String.format( "%s : Minimum swc for germination (%.4f) < wiltpoint (%.4f)", v.sppFileName, v.min_swc_germ, SW_Soils.getLayer(0).swcBulk_wiltpt));
		}

		if (v.min_swc_estab < SW_Soils.getLayer(0).swcBulk_wiltpt) {
			f.LogError(LogMode.FATAL, String.format( "%s : Minimum swc for establishment (%.4f) < wiltpoint (%.4f)", v.sppFileName, v.min_swc_germ, v.min_swc_estab, SW_Soils.getLayer(0).swcBulk_wiltpt));
		}
	}
	
	private void _echo_inits() throws Exception {
		/* --------------------------------------------------- */
		LogFileIn f = log;
		
		f.LogError(LogMode.NOTE, String.format("\n=========================================================\n\n"+
				"Parameters for the SoilWat Vegetation Establishment Check.\n"+
				"----------------------------------------------------------\n"+
				"Number of species to be tested: %d\n", count));

		for (int i = 0; i < count; i++) {
			SW_VEGESTAB_INFO v = params.get(i);
			f.LogError(LogMode.NOTE, String.format("Species: %s\n----------------\n"+
					"Germination parameters:\n"+
					"\tMinimum SWP (bars)  : -%.4f\n"+
					"\tMinimum SWC (cm/cm) : %.4f\n"+
					"\tMinimum SWC (cm/lyr): %.4f\n"+
					"\tMinimum temperature : %.1f\n"+
					"\tMaximum temperature : %.1f\n"+
					"\tFirst possible day  : %d\n"+
					"\tLast  possible day  : %d\n"+
					"\tMinimum consecutive wet days (after first possible day): %d\n"+
					"Establishment parameters:\n"+
					"\tNumber of layers affecting successful establishment: %d\n"+
					"\tMinimum SWP (bars) : -%.4f\n"+
					"\tMinimum SWC (cm/layer) averaged across top %d layers: %.4f\n"+
					"\tMinimum temperature : %.1f\n"+
					"\tMaximum temperature : %.1f\n"+
					"\tMinimum number of days after germination      : %d\n"+
					"\tMaximum number of days after germination      : %d\n"+
					"\tMinimum consecutive wet days after germination: %d\n"+
					"\tMaximum consecutive dry days after germination: %d\n"+
					"---------------------------------------------------------------\n\n",

			v.sppName, v.soilLayerParams.bars[SW_GERM_BARS], v.min_swc_germ / SW_Soils.getLayer(0).width, v.min_swc_germ, v.tempParams.min_temp_germ, v.tempParams.max_temp_germ, v.timingParams.min_pregerm_days,
				v.timingParams.max_pregerm_days, v.timingParams.min_wetdays_for_germ,

					v.soilLayerParams.estab_lyrs, v.soilLayerParams.bars[SW_ESTAB_BARS], v.soilLayerParams.estab_lyrs, v.min_swc_estab, v.tempParams.min_temp_estab, v.tempParams.max_temp_estab, v.timingParams.min_days_germ2estab,
					v.timingParams.max_days_germ2estab, v.timingParams.min_wetdays_for_estab, v.timingParams.max_drydays_postgerm

					));
		}
		f.LogError(LogMode.NOTE, "\n-----------------  End of Establishment Parameters ------------\n");
	}
	
	public int count() {
		return this.count;
	}
	public SW_VEGESTAB_INFO get_INFO(int sppnum) {
		return this.params.get(sppnum);
	}
	public boolean get_echoinits() {
		return this.EchoInits;
	}
	public void set_echoinits(boolean echo) {
		this.EchoInits = echo;
	}
	public boolean get_use() {
		return this.use;
	}
}
