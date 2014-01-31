package data;

import input.LogFileIn;
import input.LogFileIn.LogMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import defines.Defines;

//NEED SW_LAYER_INFO

public class SW_VEGESTAB {

	private static final int SW_GERM_BARS=0;
	private static final int SW_ESTAB_BARS=1;
	
	public class SW_VEGESTAB_INFO {
		/* THESE VARIABLES CAN CHANGE VALUE IN THE MODEL */
		public int estab_doy, 	/* day of establishment for this plant */
		germ_days, 				/* elapsed days since germination with no estab */
		drydays_postgerm, 		/* did sprout get too dry for estab? */
		wetdays_for_germ, 		/* keep track of consecutive wet days */
		wetdays_for_estab;
		public boolean germd, 	/* has this plant germinated yet?  */
		no_estab; 				/* if TRUE, can't attempt estab for remainder of year */
		
		/* THESE VARIABLES DO NOT CHANGE DURING THE NORMAL MODEL RUN */
		public String sppFileName;
		public String sppName;
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
		public int estab_lyrs;	/* estab could conceivably need more than one layer */
								/* swc is averaged over these top layers to compare to */
								/* the converted value from min_swc_estab */
		public double bars[], 			/* read from input, saved for reporting */
		min_swc_germ, 	/* wetting point required for germination converted from */
						/* bars to cm per layer for efficiency in the loop */
		min_swc_estab, 	/* same as min_swc_germ but for establishment */
						/* this is the average of the swc of the first estab_lyrs */
		min_temp_germ, 	/* min avg daily temp req't for germination */
		max_temp_germ, 	/* max temp for germ in degC */
		min_temp_estab, /* min avg daily temp req't for establishment */
		max_temp_estab; /* max temp for estab in degC */
		
		private boolean data;
		
		public SW_VEGESTAB_INFO() {
			this.data=false;
			this.bars = new double[2];
		}
	}
	
	public class SW_VEGESTAB_OUTPUTS {
		private int[] days = null;
	}
	
	private boolean use;//if true use establishment parms and chkestab()
	private int count; 	//number of species to check
	private List<SW_VEGESTAB_INFO> params; /* dynamic array of parms for each species */
	private SW_VEGESTAB_OUTPUTS yrsum, /* conforms to the requirements of the output module */
								yravg; /* note that there's only one period for output */
									   /* see also the soilwater and weather modules */
	private boolean data;
	private SW_WEATHER SW_Weather;
	private SW_SOILWATER SW_Soilwat;
	private SW_MODEL SW_Model;
	private SW_SOILS SW_Soils;
	private boolean EchoInits;
	
	public SW_VEGESTAB(boolean echoInits, SW_WEATHER SW_Weather, SW_SOILWATER SW_Soilwat, SW_MODEL SW_Model, SW_SOILS SW_Soils) {
		this.use = false;
		this.count = 0;
		this.params = new ArrayList<SW_VEGESTAB_INFO>();
		this.yravg = new SW_VEGESTAB_OUTPUTS();
		this.yrsum = new SW_VEGESTAB_OUTPUTS();
		this.data = false;
		this.SW_Weather = SW_Weather;
		this.SW_Soilwat = SW_Soilwat;
		this.SW_Model = SW_Model;
		this.SW_Soils = SW_Soils;
		this.EchoInits = echoInits;
	}
	
	public SW_VEGESTAB_OUTPUTS get_yrsum() {
		return yrsum;
	}
	
	public void onClear() {
		this.data = false;
		this.params.clear();
		this.yravg.days = null;
		this.yravg.days = null;
	}
	
	public void onNewYear() {
		for(int i=0; i<this.count; i++)
			this.yrsum.days[i] = 0;
	}
	
	public void onCheckEstab(int modelDOY) {
		for(int i=0; i<this.count;i++)
			_checkit(modelDOY, i);
	}
	
	public void onRead(Path estabIn, Path prjDir) throws IOException {
		int lineno=0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(estabIn, StandardCharsets.UTF_8);
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				switch (lineno) {
				case 0:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn onRead : Expected only one value for use line.");
					try {
						this.use = Integer.parseInt(values[0])>0 ? true : false;
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn onRead : Could not convert use line.");
					}
					break;
				default:
					_read_spp(prjDir.resolve(values[0]), values[0]);
					break;
				}
				lineno++;
			}
		}
		if(lineno == 0) {
			this.use = false;
		}
		for(int i=0; i<this.count; i++)
			_spp_init(i);
		if(this.params.size() > 0) {
			this.yrsum.days = new int[this.params.size()];
		}
		this.data = true;
		
		if(EchoInits)
			_echo_inits();
	}
	
	public void onWrite(Path estabIn, Path prjDir) throws IOException {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# list of filenames for which to check establishment");
			lines.add("# each filename pertains to a species and contains the");
			lines.add("# soil moisture and timing parameters required for the");
			lines.add("# species to establish in a given year.");
			lines.add("# There is no limit to the number of files in the list.");
			lines.add("# to suppress checking establishment, comment all the");
			lines.add("# lines below.");
			lines.add("");
			lines.add(String.valueOf(this.use?1:0)+"\t"+"# use flag; 1=check establishment, 0=don't check, ignore following");
			if(this.params.size() > 0) {
				for(int i=0; i<this.params.size(); i++) {
					lines.add(this.params.get(i).sppFileName);
					_write_spp(prjDir.resolve(this.params.get(i).sppFileName), this.params.get(i));
				}
			}
			Files.write(estabIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "EstabIn onWrite : onWrite : No data.");
		}
	}
	
	private void _read_spp(Path sppFile, String sppFileName) throws IOException {
		int nitems=15, lineno=0;
		
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(sppFile, StandardCharsets.UTF_8);
		
		this.params.add(new SW_VEGESTAB_INFO());
		this.count = this.params.size()-1;
		SW_VEGESTAB_INFO v = this.params.get(this.count);
		v.sppFileName = sppFileName;
		
		for (String line : lines) {
			//Skip Comments and empty lines
			if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
				line = line.trim();
				String[] values = line.split("#")[0].split("[ \t]+");//Remove comment after data
				switch (lineno) {
				case 0:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for name line.");
					v.sppName = values[0];
					break;
				case 1:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for number of layers affecting establishment.");
					try {
						v.estab_lyrs = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert number of layers affecting establishment line.");
					}
					break;
				case 2:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for SWP (bars) requirement for germination (top layer).");
					try {
						v.bars[SW_GERM_BARS] = Double.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert SWP (bars) requirement for germination (top layer) line.");
					}
					break;
				case 3:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for SWP (bars) requirement for establishment (average of top layers).");
					try {
						v.bars[SW_ESTAB_BARS] = Double.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert SWP (bars) requirement for establishment (average of top layers) line.");
					}
					break;
				case 4:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for first possible day of germination.");
					try {
						v.min_pregerm_days = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert first possible day of germination line.");
					}
					break;
				case 5:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for last possible day of germination.");
					try {
						v.max_pregerm_days = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert last possible day of germination line.");
					}
					break;
				case 6:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min number of consecutive 'wet' days for germination to occur.");
					try {
						v.min_wetdays_for_germ = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min number of consecutive 'wet' days for germination to occur line.");
					}
					break;
				case 7:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max number of consecutive 'dry' days after germination allowing estab.");
					try {
						v.max_drydays_postgerm = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max number of consecutive 'dry' days after germination allowing estab line.");
					}
					break;
				case 8:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min number of consecutive 'wet' days after germination before establishment.");
					try {
						v.min_wetdays_for_estab = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert consecutive 'wet' days after germination before establishment line.");
					}
					break;
				case 9:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min number of days between germination and establishment.");
					try {
						v.min_days_germ2estab = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min number of days between germination and establishment line.");
					}
					break;
				case 10:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max number of days between germination and establishment.");
					try {
						v.max_days_germ2estab = Integer.parseInt(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max number of days between germination and establishment line.");
					}
					break;
				case 11:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min temp threshold for germination.");
					try {
						v.min_temp_germ = Double.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min temp threshold for germination line.");
					}
					break;
				case 12:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max temp threshold for germination.");
					try {
						v.max_temp_germ = Double.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max temp threshold for germination line.");
					}
					break;
				case 13:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for min temp threshold for establishment.");
					try {
						v.min_temp_estab = Double.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert min temp threshold for establishment line.");
					}
					break;
				case 14:
					if(values.length > 1)
						f.LogError(LogMode.ERROR, "EstabIn _read_spp : Expected only one value for max temp threshold for establishment.");
					try {
						v.max_temp_estab = Double.valueOf(values[0]);
					} catch(NumberFormatException e) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max temp threshold for establishment line.");
					}
					break;
				}
				if(lineno == 0) {
					if(v.sppName.length() > 4) {
						f.LogError(LogMode.ERROR, "EstabIn _spp_read : Could not convert max temp threshold for establishment line.");
					}
				}
				lineno++;
			}
		}
		if(lineno  < nitems) {
			f.LogError(LogMode.ERROR, "EstabIn _spp_read : Too few input parameters.");
		}
		v.data=true;
	}
	
	private void _write_spp(Path sppFile, SW_VEGESTAB_INFO v) throws IOException {
		if(v.data) {
			List<String> lines = new ArrayList<String>();
			lines.add(v.sppName + "\t" + "# 4-char name of species");
			lines.add("# soil layer parameters");
			lines.add(v.estab_lyrs + "\t" + "# number of layers affecting establishment");
			lines.add(v.bars[SW_GERM_BARS] + "\t" + "# SWP (bars) requirement for germination (top layer)");
			lines.add(v.bars[SW_ESTAB_BARS] + "\t" + "# SWP (bars) requirement for establishment (average of top layers)");
			lines.add("# timing parameters in days");
			lines.add(v.min_pregerm_days + "\t" + "# first possible day of germination");
			lines.add(v.max_pregerm_days + "\t" + "# last possible day of germination");
			lines.add(v.min_wetdays_for_germ + "\t" + "# min number of consecutive 'wet' days for germination to occur");
			lines.add(v.max_drydays_postgerm + "\t" + "# max number of consecutive 'dry' days after germination allowing estab");
			lines.add(v.min_wetdays_for_estab + "\t" + "# min number of consecutive 'wet' days after germination before establishment");
			lines.add(v.min_days_germ2estab + "\t" + "# min number of days between germination and establishment");
			lines.add(v.max_days_germ2estab + "\t" + "# max number of days between germination and establishment");
			lines.add("# temperature parameters in C");
			lines.add(v.min_temp_germ + "\t" + "# min temp threshold for germination");
			lines.add(v.max_temp_germ + "\t" + "# max temp threshold for germination");
			lines.add(v.min_temp_estab + "\t" + "# min temp threshold for establishment");
			lines.add(v.max_temp_estab + "\t" + "# max temp threshold for establishment");
			Files.write(sppFile, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "EstabIn _write_spp : onWrite : No data.");
		}
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

		if (doy < v.min_pregerm_days)
			return;

		/* ---- check for germination, establishment */
		if (!v.germd && v.wetdays_for_germ >= v.min_wetdays_for_germ) {

			if (doy < v.min_pregerm_days)
				return;
			if (doy > v.max_pregerm_days) {
				v.no_estab = true;
				return;
			}
			/* temp doesn't affect wetdays */
			if (Defines.LT(avgtemp, v.min_temp_germ) || Defines.GT(avgtemp, v.max_temp_germ))
				return;

			v.germd = true;
			return;

		} else { /* continue monitoring sprout's progress */

			/* any dry period (> max_drydays) or temp out of range
			 * after germination means restart */
			avgswc = 0;
			for (i = 0; i < v.estab_lyrs;)
				avgswc += sw.swcBulk[Today][i++];
			avgswc /= (double) v.estab_lyrs;
			if (Defines.LT(avgswc, v.min_swc_estab)) {
				v.drydays_postgerm++;
				v.wetdays_for_estab = 0;
			} else {
				v.drydays_postgerm = 0;
				v.wetdays_for_estab++;
			}

			if (v.drydays_postgerm > v.max_drydays_postgerm || Defines.LT(avgtemp, v.min_temp_estab) || Defines.GT(avgtemp, v.max_temp_estab)) {
				/* too bad: discontinuity in environment, plant dies, start over */
				_EstabFailed(sppnum);
				return;
			}

			v.germ_days++;

			if (v.wetdays_for_estab < v.min_wetdays_for_estab || v.germ_days < v.min_days_germ2estab) {
				return;
				/* no need to zero anything */
			}

			if (v.germ_days > v.max_days_germ2estab) {
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
	
	private void _spp_init(int sppnum) {
		/* =================================================== */
		/* initializations performed after acquiring parameters
		 * after read() or some other function call.
		 */
		SW_VEGESTAB_INFO v = params.get(sppnum);
		int i;

		/* The thetas and psis etc should be initialized by now */
		/* because init_layers() must be called prior to this routine */
		/* (see watereqn() ) */
		v.min_swc_germ = SW_SOILWATER.SW_SWPmatric2VWCBulk(SW_Soils.getLayer(0).fractionVolBulk_gravel, v.bars[SW_GERM_BARS], SW_Soils.getLayer(0).psisMatric, SW_Soils.getLayer(0).binverseMatric, SW_Soils.getLayer(0).thetasMatric) * SW_Soils.getLayer(0).width;

		/* due to possible differences in layer textures and widths, we need
		 * to average the estab swc across the given layers to peoperly
		 * compare the actual swc average in the checkit() routine */
		v.min_swc_estab = 0.;
		for (i = 0; i < v.estab_lyrs; i++)
			v.min_swc_estab += SW_SOILWATER.SW_SWPmatric2VWCBulk(SW_Soils.getLayer(0).fractionVolBulk_gravel, v.bars[SW_ESTAB_BARS], SW_Soils.getLayer(0).psisMatric, SW_Soils.getLayer(0).binverseMatric, SW_Soils.getLayer(0).thetasMatric) * SW_Soils.getLayer(0).width;
		v.min_swc_estab /= v.estab_lyrs;

		_sanity_check(sppnum);
	}
	
	private void _sanity_check(int sppnum) {
		/* =================================================== */
		LogFileIn f = LogFileIn.getInstance();
		
		SW_VEGESTAB_INFO v = params.get(sppnum);
		double min_transp_lyrs;

		min_transp_lyrs = Math.min(SW_Soils.getLayersInfo().n_transp_lyrs_tree, Math.min(SW_Soils.getLayersInfo().n_transp_lyrs_forb, Math.min(SW_Soils.getLayersInfo().n_transp_lyrs_shrub, SW_Soils.getLayersInfo().n_transp_lyrs_grass)));

		if (v.estab_lyrs > min_transp_lyrs) {
			f.LogError(LogMode.FATAL, String.format( "%s : Layers requested (estab_lyrs) > (# transpiration layers=%d).", v.sppFileName, min_transp_lyrs));
		}

		if (v.min_pregerm_days > v.max_pregerm_days) {
			f.LogError(LogMode.FATAL, String.format( "%s : First day of germination > last day of germination.", v.sppFileName));
		}

		if (v.min_wetdays_for_estab > v.max_days_germ2estab) {
			f.LogError(LogMode.FATAL, String.format( "%s : Minimum wetdays after germination (%d) > maximum days allowed for establishment (%d).", v.sppFileName, v.min_wetdays_for_estab, v.max_days_germ2estab));
		}

		if (v.min_swc_germ < SW_Soils.getLayer(0).swcBulk_wiltpt) {
			f.LogError(LogMode.FATAL, String.format( "%s : Minimum swc for germination (%.4f) < wiltpoint (%.4f)", v.sppFileName, v.min_swc_germ, SW_Soils.getLayer(0).swcBulk_wiltpt));
		}

		if (v.min_swc_estab < SW_Soils.getLayer(0).swcBulk_wiltpt) {
			f.LogError(LogMode.FATAL, String.format( "%s : Minimum swc for establishment (%.4f) < wiltpoint (%.4f)", v.sppFileName, v.min_swc_germ, v.min_swc_estab, SW_Soils.getLayer(0).swcBulk_wiltpt));
		}
	}
	
	private void _echo_inits() {
		/* --------------------------------------------------- */
		LogFileIn f = LogFileIn.getInstance();
		
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

			v.sppName, v.bars[SW_GERM_BARS], v.min_swc_germ / SW_Soils.getLayer(0).width, v.min_swc_germ, v.min_temp_germ, v.max_temp_germ, v.min_pregerm_days,
					v.max_pregerm_days, v.min_wetdays_for_germ,

					v.estab_lyrs, v.bars[SW_ESTAB_BARS], v.estab_lyrs, v.min_swc_estab, v.min_temp_estab, v.max_temp_estab, v.min_days_germ2estab,
					v.max_days_germ2estab, v.min_wetdays_for_estab, v.max_drydays_postgerm

					));
		}
		f.LogError(LogMode.NOTE, "\n-----------------  End of Establishment Parameters ------------\n");
	}
	
	public int count() {
		return this.count();
	}
	public SW_VEGESTAB_INFO get_INFO(int sppnum) {
		return this.params.get(sppnum);
	}
}
