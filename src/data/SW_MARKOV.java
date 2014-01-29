package data;

import input.LogFileIn;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import defines.Defines;
import times.Times;

public class SW_MARKOV {
	/* pointers to arrays of probabilities for each day saves some space */
	/* by not being allocated if markov weather not requested by user */
	/* alas, multi-dimensional arrays aren't so convenient */
	
	private double[] wetprob;	/* probability of being wet today */
	private double[] dryprob;	/* probability of being dry today */
	private double[] avg_ppt;	/* mean precip (cm) */
	private double[] std_ppt;	/* std dev. for precip */
	private double[][] u_cov;	/* mean temp (max, min) Celsius */
	private double[][][] v_cov;	/* covariance matrix */
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
		this.wetprob = new double[Times.MAX_DAYS];
		this.dryprob = new double[Times.MAX_DAYS];
		this.avg_ppt = new double[Times.MAX_DAYS];
		this.std_ppt = new double[Times.MAX_DAYS];
		this.u_cov = new double[Times.MAX_WEEKS][2];
		this.v_cov = new double[Times.MAX_WEEKS][2][2];
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
	
	public void SW_MKV_today(int doy) {
		int week;
		double prob,p,x;
		
		prob = Double.compare(values.rain, 0.0)>0 ? this.wetprob[doy] : this.dryprob[doy];
		p = random.nextDouble();
		if(Double.compare(p, prob) < 0) {
			x = this.avg_ppt[doy] + random.nextGaussian()*this.std_ppt[doy];
			values.rain = Math.max(0., x);
		} else {
			values.rain = 0.;
		}

		if (!Defines.isZero(values.rain))
			this.setPpt_events(this.getPpt_events() + 1);

		/* Calculate temperature */
		week = Times.Doy2Week(doy+1);
		this.v_cov[week][0][0] = _vcov[0][0];
		this.v_cov[week][0][1] = _vcov[0][1];
		this.v_cov[week][1][0] = _vcov[1][0];
		this.v_cov[week][1][1] = _vcov[1][1];
		_ucov[0] = this.u_cov[week][0];
		_ucov[1] = this.u_cov[week][1];
		mvnorm(values);
	}
		
	public void onReadMarkov(Path MarkovProbabilityIn, Path MarkovCovarianceIn) throws IOException {
		onReadMarkovCovIn(MarkovCovarianceIn);
		onReadMarkovProbIn(MarkovProbabilityIn);
		this.data = true;
	}
	private void onReadMarkovProbIn(Path MarkovProbabilityIn) throws IOException {
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
	private void onReadMarkovCovIn(Path MarkovCovarianceIn) throws IOException {
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
	
	private void mvnorm(MaxMinRain t) {
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
