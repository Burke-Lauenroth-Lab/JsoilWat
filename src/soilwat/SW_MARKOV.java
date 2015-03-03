package soilwat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SW_MARKOV {
	/* pointers to arrays of probabilities for each day saves some space */
	/* by not being allocated if markov weather not requested by user */
	/* alas, multi-dimensional arrays aren't so convenient */
	public static class Probability {
		public double[] wetprob;	/* probability of being wet today */
		public double[] dryprob;	/* probability of being dry today */
		public double[] avg_ppt;	/* mean precip (cm) */
		public double[] std_ppt;	/* std dev. for precip */
		private LogFileIn log;
		
		public Probability(LogFileIn log) {
			this.log = log;
			this.wetprob = new double[Times.MAX_DAYS];
			this.dryprob = new double[Times.MAX_DAYS];
			this.avg_ppt = new double[Times.MAX_DAYS];
			this.std_ppt = new double[Times.MAX_DAYS];
		}
		
		public void onClear() {
			for(int i=0; i<Times.MAX_DAYS;i++) {
				wetprob[i]=dryprob[i]=avg_ppt[i]=std_ppt[i]=0;
			}
		}
		
		public void onRead(String swMarkovProbabilityIn) throws Exception {
			int nDays = 0;
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swMarkovProbabilityIn, getClass().getClassLoader());
			
			for (String line : lines) {
				//Do not go past MaxDays
				if(Times.MAX_DAYS == nDays)
					break;
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("[ \t]+");
					if(values.length < 5)
						f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovProbIn : Line "+String.valueOf(lines.indexOf(line))+": Not enough values.");
					try {
						int day = Integer.parseInt(values[0])-1;
						this.wetprob[day] = Double.parseDouble(values[1]);
						this.dryprob[day] = Double.parseDouble(values[2]);
						this.avg_ppt[day] = Double.parseDouble(values[3]);
						this.std_ppt[day] = Double.parseDouble(values[4]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovProbIn : Could not convert string to number." + e.getMessage());
					}
					nDays++;
				}
			}
		}
		
		public void onWrite(String swMarkovProbabilityIn) throws Exception {
			Path markprobin = Paths.get(swMarkovProbabilityIn);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(markprobin, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out = "";
			out += "# Markov Prob In v1.0 (RJM) 2015 update\n";
			out += "# day\twet\t\tdry\t\tavg\t\tstd\n";
			for(int i=0; i<(Times.MAX_DAYS); i++)
				out += String.format("  %-5d %-7.4f %-7.4f %-7.4f %.4f\n",i+1, wetprob[i], dryprob[i], avg_ppt[i], std_ppt[i]);
			return out;
		}
	}
	public static class Covariance {
		public double[][] u_cov;	/* mean temp (max, min) Celsius */
		public double[][][] v_cov;	/* covariance matrix */
		private LogFileIn log;
		
		public Covariance(LogFileIn log) {
			this.log = log;
			this.u_cov = new double[Times.MAX_WEEKS][2];
			this.v_cov = new double[Times.MAX_WEEKS][2][2];
		}
		
		public void onClear() {
			for(int i=0; i<Times.MAX_WEEKS;i++) {
				u_cov[i][0]=0;
				u_cov[i][1]=0;
				v_cov[i][0][0]=0;
				v_cov[i][0][1]=0;
				v_cov[i][1][0]=0;
				v_cov[i][1][1]=0;
			}
		}
		
		public void onRead(String swMarkovCovarianceIn) throws Exception {
			int nWeeks = 0;
			LogFileIn f = log;
			List<String> lines = SW_FILES.readFile(swMarkovCovarianceIn, getClass().getClassLoader());
			
			for (String line : lines) {
				//Do not go past MaxWeeks
				if(Times.MAX_WEEKS == nWeeks)
					break;
				//Skip Comments and empty lines
				if(!line.matches("^\\s*#.*") && !line.matches("^[\\s]*$")) {
					line = line.trim();
					String[] values = line.split("[ \t]+");
					if(values.length < 7)
						f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovCovIn : Line "+String.valueOf(lines.indexOf(line))+": Not enough values.");
					try {
						int week = Integer.parseInt(values[0])-1;
						this.u_cov[week][0] = Double.parseDouble(values[1]);
						this.u_cov[week][1] = Double.parseDouble(values[2]);
						this.v_cov[week][0][0] = Double.parseDouble(values[3]);
						this.v_cov[week][0][1] = Double.parseDouble(values[4]);
						this.v_cov[week][1][0] = Double.parseDouble(values[5]);
						this.v_cov[week][1][1] = Double.parseDouble(values[6]);
					} catch(NumberFormatException e) {
						f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovCovIn : Could not convert string to number." + e.getMessage());
					}
					nWeeks++;
				}
			}
		}
		
		public void onWrite(String swMarkovCovarianceIn) throws Exception {
			Path markcovin = Paths.get(swMarkovCovarianceIn);
			List<String> lines = new ArrayList<String>();
			lines.add(this.toString());
			Files.write(markcovin, lines, StandardCharsets.UTF_8);
		}
		
		public String toString() {
			String out = "";
			out += "# Markov Covariance In v1.0 (RJM) 2015 update\n";
			out += "# week\tu_cov1\t\tu_cov2\t\tv_cov1\t\tv_cov2\t\tv_cov3\t\tv_cov4\n";
			for(int i=0; i<(Times.MAX_WEEKS); i++)
				out += String.format("  %-5d %-11.5f %-11.5f %-11.5f %-11.5f %-11.5f %.5f\n",i+1, u_cov[i][0], u_cov[i][1], v_cov[i][0][0], v_cov[i][0][1], v_cov[i][1][0], v_cov[i][1][1]);
			return out;
		}
	}
	private Probability prob;
	private Covariance cov;
	private int ppt_events;		/* number of ppt events generated this year */
	private double[][] _vcov;
	private double[] _ucov;
	private MaxMinRain values;
	private Random random;
	private boolean data;
	private LogFileIn log;

	
	public class MaxMinRain {
		public double tmax,tmin,rain;
		public MaxMinRain() {
			tmax=tmin=rain=0;
		}
	}
	
	public SW_MARKOV(LogFileIn log) {
		this.log = log;
		this.data = false;
		this.prob = new Probability(log);
		this.cov = new Covariance(log);
		this._ucov = new double[2];
		this._vcov = new double[2][2];
		this.values = new MaxMinRain();
		this.setPpt_events(0);
		this.random = new Random();
	}
	public boolean onVerify() {
		if(this.data)
			return true;
		else
			return false;
	}
	
	public void SW_MKV_today(int doy) throws Exception {
		int week;
		double prob,p,x;
		
		prob = Double.compare(values.rain, 0.0)>0 ? this.prob.wetprob[doy] : this.prob.dryprob[doy];
		p = random.nextDouble();
		if(Double.compare(p, prob) < 0) {
			x = this.prob.avg_ppt[doy] + random.nextGaussian()*this.prob.std_ppt[doy];
			values.rain = Math.max(0., x);
		} else {
			values.rain = 0.;
		}

		if (!Defines.isZero(values.rain))
			this.setPpt_events(this.getPpt_events() + 1);

		/* Calculate temperature */
		week = Times.Doy2Week(doy+1);
		this.cov.v_cov[week][0][0] = _vcov[0][0];
		this.cov.v_cov[week][0][1] = _vcov[0][1];
		this.cov.v_cov[week][1][0] = _vcov[1][0];
		this.cov.v_cov[week][1][1] = _vcov[1][1];
		_ucov[0] = this.cov.u_cov[week][0];
		_ucov[1] = this.cov.u_cov[week][1];
		mvnorm(values);
	}
	
	public void onSetInput(InputData.MarkovIn markovIn) {
		//copy prob
		for(int doy=0; doy<Times.MAX_DAYS; doy++) {
			this.prob.avg_ppt[doy] = markovIn.probability.avg_ppt[doy];
			this.prob.dryprob[doy] = markovIn.probability.dryprob[doy];
			this.prob.std_ppt[doy] = markovIn.probability.std_ppt[doy];
			this.prob.wetprob[doy] = markovIn.probability.wetprob[doy];
		}
		//copy cov
		for(int week=0; week<Times.MAX_WEEKS; week++) {
			this.cov.u_cov[week][0] = markovIn.covariance.u_cov[week][0];
			this.cov.u_cov[week][1] = markovIn.covariance.u_cov[week][1];
			this.cov.v_cov[week][0][0] = markovIn.covariance.v_cov[week][0][0];
			this.cov.v_cov[week][0][1] = markovIn.covariance.v_cov[week][0][1];
			this.cov.v_cov[week][1][0] = markovIn.covariance.v_cov[week][1][0];
			this.cov.v_cov[week][1][1] = markovIn.covariance.v_cov[week][1][1];
		}
		this.data = true;
	}
	
	public void onGetInput(InputData.MarkovIn markovIn) {
		// copy prob
		for (int doy = 0; doy < Times.MAX_DAYS; doy++) {
			markovIn.probability.avg_ppt[doy] = this.prob.avg_ppt[doy];
			markovIn.probability.dryprob[doy] = this.prob.dryprob[doy];
			markovIn.probability.std_ppt[doy] = this.prob.std_ppt[doy];
			markovIn.probability.wetprob[doy] = this.prob.wetprob[doy];
		}
		// copy cov
		for (int week = 0; week < Times.MAX_WEEKS; week++) {
			markovIn.covariance.u_cov[week][0] = this.cov.u_cov[week][0];
			markovIn.covariance.u_cov[week][1] = this.cov.u_cov[week][1];
			markovIn.covariance.v_cov[week][0][0] = this.cov.v_cov[week][0][0];
			markovIn.covariance.v_cov[week][0][1] = this.cov.v_cov[week][0][1];
			markovIn.covariance.v_cov[week][1][0] = this.cov.v_cov[week][1][0];
			markovIn.covariance.v_cov[week][1][1] = this.cov.v_cov[week][1][1];
		}
	}
	
	private void mvnorm(MaxMinRain t) throws Exception {
		/* --------------------------------------------------- */
		/* This proc is distilled from a much more general function
		 * in the original fortran version which was prepared to
		 * handle any number of variates.  In our case, there are
		 * only two, tmax and tmin, so there can be many fewer
		 * lines.  The purpose is to compute a random normal tmin
		 * value that covaries with tmax based on the covariance
		 * file read in at startup.
		 *
		 * cwb - 09-Dec-2002 -- This used to be two functions but
		 *       after some extensive debugging in this and the
		 *       RandNorm() function, it seems silly to maintain
		 *       the extra function call.
		 * cwb - 24-Oct-03 -- Note the switch to double (RealD).
		 *       C converts the floats transparently.
		 * In java one can not simply pass reference to primitives.
		 * Wrapping these primitives in a array allows the value to
		 * be used outside the function.
		 */
		double s, z1, z2, vc00 = _vcov[0][0], vc10 = _vcov[1][0], vc11 = _vcov[1][1];

		vc00 = Math.sqrt(vc00);
		vc10 = (vc00 > 0.) ? vc10 / vc00 : 0;
		s = vc10 * vc10;
		if (s > vc11)
			log.LogError(LogFileIn.LogMode.ERROR, "\nBad covariance matrix in mvnorm()");
		vc11 = (Double.compare(vc11, s))==0 ? 0. : Math.sqrt(vc11 -s);
		
		
		z1 = random.nextGaussian();
		z2 = random.nextGaussian();
		t.tmin = (vc10 * z1) + (vc11 * z2) + _ucov[1];
		t.tmax = vc00 * z1 + _ucov[0];
	}
	public int getPpt_events() {
		return ppt_events;
	}
	public void setPpt_events(int ppt_events) {
		this.ppt_events = ppt_events;
	}
	public MaxMinRain get_MaxMinRain() {
		return this.values;
	}
	public void set_MaxMinRain(double max, double min, double rain) {
		this.values.tmax = max;
		this.values.tmin = min;
		this.values.rain = rain;
	}
}
