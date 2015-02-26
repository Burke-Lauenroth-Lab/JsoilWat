package soilwat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import soilwat.LogFileIn.LogMode;

public class SW_MARKOV {
	/* pointers to arrays of probabilities for each day saves some space */
	/* by not being allocated if markov weather not requested by user */
	/* alas, multi-dimensional arrays aren't so convenient */
	public static class Probability {
		public double[] wetprob;	/* probability of being wet today */
		public double[] dryprob;	/* probability of being dry today */
		public double[] avg_ppt;	/* mean precip (cm) */
		public double[] std_ppt;	/* std dev. for precip */
		
		public Probability() {
			this.wetprob = new double[Times.MAX_DAYS];
			this.dryprob = new double[Times.MAX_DAYS];
			this.avg_ppt = new double[Times.MAX_DAYS];
			this.std_ppt = new double[Times.MAX_DAYS];
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
		
		public Covariance() {
			this.u_cov = new double[Times.MAX_WEEKS][2];
			this.v_cov = new double[Times.MAX_WEEKS][2][2];
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

	
	public class MaxMinRain {
		public double tmax,tmin,rain;
		public MaxMinRain() {
			tmax=tmin=rain=0;
		}
	}
	
	public SW_MARKOV() {
		this.data = false;
		this.prob = new Probability();
		this.cov = new Covariance();
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
		
	public void onReadMarkov(Path MarkovProbabilityIn, Path MarkovCovarianceIn) throws Exception {
		if(Files.exists(MarkovProbabilityIn)) {
			onReadMarkovCovIn(MarkovCovarianceIn);
		}
		if(Files.exists(MarkovCovarianceIn)) {
			onReadMarkovProbIn(MarkovProbabilityIn);
		}
		this.data = true;
	}
	private void onReadMarkovProbIn(Path MarkovProbabilityIn) throws Exception {
		int nDays = 0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(MarkovProbabilityIn, StandardCharsets.UTF_8);
		
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
					this.prob.wetprob[day] = Double.parseDouble(values[1]);
					this.prob.dryprob[day] = Double.parseDouble(values[2]);
					this.prob.avg_ppt[day] = Double.parseDouble(values[3]);
					this.prob.std_ppt[day] = Double.parseDouble(values[4]);
				} catch(NumberFormatException e) {
					f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovProbIn : Could not convert string to number." + e.getMessage());
				}
				
				nDays++;
			}
		}
		
	}
	private void onReadMarkovCovIn(Path MarkovCovarianceIn) throws Exception {
		int nWeeks = 0;
		LogFileIn f = LogFileIn.getInstance();
		List<String> lines = Files.readAllLines(MarkovCovarianceIn, StandardCharsets.UTF_8);
		
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
					this.cov.u_cov[week][0] = Double.parseDouble(values[1]);
					this.cov.u_cov[week][1] = Double.parseDouble(values[2]);
					this.cov.v_cov[week][0][0] = Double.parseDouble(values[3]);
					this.cov.v_cov[week][0][1] = Double.parseDouble(values[4]);
					this.cov.v_cov[week][1][0] = Double.parseDouble(values[5]);
					this.cov.v_cov[week][1][1] = Double.parseDouble(values[6]);
				} catch(NumberFormatException e) {
					f.LogError(LogFileIn.LogMode.ERROR, "swMarkov onReadMarkovCovIn : Could not convert string to number." + e.getMessage());
				}
				nWeeks++;
			}
		}
	}
	
	public void onWriteMarkov(Path MarkovProbabilityIn, Path MarkovCovarianceIn) throws Exception {
		onWriteMarkovProbIn(MarkovProbabilityIn);
		onWriteMarkovCovIn(MarkovCovarianceIn);
	}
	
	private void onWriteMarkovProbIn(Path MarkovProbabilityIn) throws Exception {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Markov Prob In v1.0 (RJM) 2015 update");
			lines.add("# day\t\twet\t\tdry\t\tavg\t\tstd ");
			for(int i=0; i<(Times.MAX_DAYS); i++)
				lines.add(String.format("%d %.4f %.4f %.4f %.4f",i+1, this.prob.wetprob[i], this.prob.dryprob[i], this.prob.avg_ppt[i], this.prob.std_ppt[i]));
			Files.write(MarkovProbabilityIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "MarkovProbabilityIn : onWrite : No data.");
		}
	}
	
	private void onWriteMarkovCovIn(Path MarkovCovarianceIn) throws Exception {
		if(this.data) {
			List<String> lines = new ArrayList<String>();
			lines.add("# Markov Covariance In v1.0 (RJM) 2015 update");
			lines.add("# week\t\tu_cov1\t\tu_cov2\t\tv_cov1\t\tv_cov2\t\tv_cov3\t\tv_cov4");
			for(int i=0; i<(Times.MAX_WEEKS); i++)
				lines.add(String.format("%d %.5f %.5f %.5f %.5f %.5f %.5f",i+1, this.cov.u_cov[i][0], this.cov.u_cov[i][1], this.cov.v_cov[i][0][0], this.cov.v_cov[i][0][1], this.cov.v_cov[i][1][0], this.cov.v_cov[i][1][1]));
			Files.write(MarkovCovarianceIn, lines, StandardCharsets.UTF_8);
		} else {
			LogFileIn f = LogFileIn.getInstance();
			f.LogError(LogMode.WARN, "MarkovCovarianceIn : onWrite : No data.");
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
		LogFileIn f = LogFileIn.getInstance();
		double s, z1, z2, vc00 = _vcov[0][0], vc10 = _vcov[1][0], vc11 = _vcov[1][1];

		vc00 = Math.sqrt(vc00);
		vc10 = (vc00 > 0.) ? vc10 / vc00 : 0;
		s = vc10 * vc10;
		if (s > vc11)
			f.LogError(LogFileIn.LogMode.ERROR, "\nBad covariance matrix in mvnorm()");
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
