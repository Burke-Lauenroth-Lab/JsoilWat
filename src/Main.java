import soilwat.InputData;
import soilwat.SW_CONTROL;

public class Main {
	public static class InputOptions {
		String filesIn = "files.in";
		boolean EchoInits = false;
		boolean QuietMode = false;
		String ProjectDirectory = "./";		
	}
	
	public static void main(String[] args) {
		try {
			InputOptions opts = new InputOptions();
			init_args(opts, args);

			InputData idata = new InputData();
			idata.onRead(opts.ProjectDirectory+"/"+opts.filesIn);
			
			SW_CONTROL sim = new SW_CONTROL();
			sim.onSetInput(idata);
			sim.onStartModel(opts.EchoInits, opts.QuietMode, true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	static void init_args(InputOptions options, String[] args) {
		String[] opts = {"-d","-f","-e","-q"};
		int[] valopts = {1,1,0,0};
		int op;
		int a = 0;
		String str = "";
		for(int i=0; i<opts.length; i++) {
			if(a >= args.length)
				break;
			
			//figure out which option by its position 0-(nopts-1)
			for(op=0; op<opts.length; op++) {
				if(opts[op].compareToIgnoreCase(args[a].substring(0, 2))==0) {
					break;
				}
			}
			
			if(op == opts.length) {
				System.out.println("Invalid option "+args[a]);
				usage();
			}
			
			if(valopts[op] > 0) {
				if(args[a].length() > 2) { //no space betw opt-value
					str = args[a].substring(2);
				} else if(!args[a+1].startsWith("-")) { //space betw opt-value
					str = args[++a];
				} else if(0 < valopts[op]) { //required opt-val not found
					System.out.println("Incomplete option "+ opts[op]);
					usage();
				}
			}
			
			switch(op) {
			case 0:
				options.ProjectDirectory = str;
				break;
			case 1:
				options.filesIn = str;
				break;
			case 2:
				options.EchoInits = true;
				break;
			case 3:
				options.QuietMode = true;
				break;
			default:
				System.out.println("Programmer: bad option in main");
				usage();
			}
			a++;
		}
		
	}
	
	static void usage() {
		System.out.println("JSoilWater model version 0.1a (SGS-LTER Oct-2003).\n"+
				"Usage: soilwat [-d startdir] [-f files.in] [-e] [-q]\n"+
				"  -d : Sets the project directory (default=.)\n"+
				"  -f : supply list of input files (default=files.in)\n"+
				"       a preceeding path applies to all input files\n"+
				"  -e : echo initial values from site and estab to logfile\n"+
				"  -q : quiet mode (true default), will not print to console.\n");
		System.exit(0);
	}
}
