package circular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Circular {
	public enum Type {
		angles,
		directions;
	}
	public enum Units {
		radians,
		degrees,
		hours;
	}
	public enum Template {
		none,
		geographics,
		clock12,
		clock24;
	}
	public enum Modulo {
		asis,
		TwoPi,
		pi;
	}
	public enum Rotation {
		counter,
		clock;
	}
	
	private Type type;
	private Units units;
	private Template template;
	private Modulo modulo;
	private Rotation rotation;
	private ArrayList<Double> data;
	
	private double zero;
	//private String names;
	
	public Circular(Double[] x, Type type, Units units, Template template, Modulo modulo, double zero, Rotation rotation) {
		this.type = type;
		this.units = units;
		this.template = template;
		this.modulo = modulo;
		this.zero = zero;
		this.rotation = rotation;
		this.data = new ArrayList<Double>(Arrays.asList(x));
		if(this.template==Template.geographics) {
			zero = Math.PI/2;
			this.rotation = Rotation.clock;
		} else if(this.template==Template.clock24) {
			zero = Math.PI/2;
			this.rotation = Rotation.clock;
		} else if (this.template==Template.clock12) {
			zero = Math.PI/2;
			this.rotation = Rotation.clock;
		}
		double ang =0;
		if(this.modulo != Modulo.asis) {
			if(this.modulo == Modulo.TwoPi) {
				ang = 2;
			} else {
				ang=1;
			}
			if(this.units == Units.radians) {
				for(int i=0; i<data.size(); i++) {
					data.set(i, data.get(i) % (ang*Math.PI));
				}
			} else if(this.units == Units.degrees) {
				for(int i=0; i<data.size(); i++) {
					data.set(i, data.get(i) % (ang*180));
				}
			} else {
				for(int i=0; i<data.size(); i++) {
					data.set(i, data.get(i) % (ang*12)); //hours
				}
			}
		}
	}
	
	public static double circ_mean(Double[] x, int div, boolean na_rm) {
		Circular x_circ;
		int NANs = 0;
		for(int i=0;i<x.length; i++)
			if(Double.isNaN(x[i]))
				NANs++;
		if(x.length == 0 || x.length == NANs)
			return Double.NaN;
		
		double circ = 2*Math.PI/div;
		for(int i=0; i<x.length; i++) {
			x[i] = x[i]*circ;
		}
		x_circ = new Circular(x, Circular.Type.angles, Units.radians, Template.none, Modulo.TwoPi, 0.0, Rotation.clock);
		double x_int = x_circ.mean_circular(na_rm)/circ;
		
		return (x_int - 1) % div+1;
	}
	
	public static double circ_range(Double[] x, int div, boolean na_rm) {
		Circular x_circ;
		int NANs = 0;
		for(int i=0;i<x.length; i++)
			if(Double.isNaN(x[i]))
				NANs++;
		if(x.length == 0 || x.length == NANs)
			return Double.NaN;
		
		double circ = 2*Math.PI/div;
		for(int i=0; i<x.length; i++) {
			x[i] = x[i]*circ;
		}
		x_circ = new Circular(x, Circular.Type.angles, Units.radians, Template.none, Modulo.TwoPi, 0.0, Rotation.clock);
		double x_int = x_circ.range_circular(na_rm, false)/circ;
		
		return x_int;
	}
	
	public static double circ_sd(Double[] x, int div, boolean na_rm) {
		Circular x_circ;
		int NANs = 0;
		for(int i=0;i<x.length; i++)
			if(Double.isNaN(x[i]))
				NANs++;
		if(x.length == 0 || x.length == NANs)
			return Double.NaN;
		
		double circ = 2*Math.PI/div;
		for(int i=0; i<x.length; i++) {
			x[i] = x[i]*circ;
		}
		x_circ = new Circular(x, Circular.Type.angles, Units.radians, Template.none, Modulo.TwoPi, 0.0, Rotation.clock);
		double x_int = x_circ.sd_circular(na_rm)/circ;
		
		return x_int;
	}
	
	public Double mean_circular(boolean na_rm) {
		if(na_rm) {
			for(int i=0;i<data.size(); i++) {
				if(Double.isNaN(data.get(i))) {
					data.remove(i);
				}
			}
		}
		if(data.size() == 0) {
			return null;
		}
		Units temp = this.units;
		conversion_circular(this.type, Units.radians, this.template, this.modulo, this.zero, this.rotation);
		double circmean = MeanCircularRad();
		conversion_circular(this.type, temp, this.template, this.modulo, this.zero, this.rotation);
		return circmean;
	}
	
	public Double range_circular(boolean na_rm, boolean finite) {
		if(na_rm) {
			for(int i=0;i<data.size(); i++) {
				if(Double.isNaN(data.get(i))) {
					data.remove(i);
				}
			}
		} else {
			for(int i=0;i<data.size(); i++)
				if(Double.isNaN(data.get(i)))
					return null;
		}
		if(finite) {
			for(int i=0;i<data.size(); i++) {
				if(Double.isInfinite(data.get(i)) || (data.get(i)==null)) {
					data.remove(i);
				}
			}
		}
		Units temp_units = this.units;
		Template temp_template = this.template;
		Modulo temp_modulo = this.modulo;
		double temp_zero = this.zero;
		Rotation temp_rotation = this.rotation;
		conversion_circular(this.type, Units.radians, this.template, Modulo.TwoPi, 0, Rotation.counter);
		Double result = RangeCircularRad();
		conversion_circular(this.type, temp_units, temp_template, temp_modulo, temp_zero, temp_rotation);
		return result;
	}
	
	public Double sd_circular(boolean na_rm) {
		if(na_rm) {
			for(int i=0;i<data.size(); i++) {
				if(Double.isNaN(data.get(i))) {
					data.remove(i);
				}
			}
		}
		if(this.data.size() == 0) {
			return null;
		}
		Units temp_units = this.units;
		double temp_zero = this.zero;
		Rotation temp_rot = this.rotation;
		conversion_circular(this.type, Units.radians, this.template, this.modulo, 0, Rotation.counter);
		//rbar
		ArrayList<Double> sin_data = new ArrayList<Double>();
		ArrayList<Double> cos_data = new ArrayList<Double>();
		for(int i=0;i<data.size(); i++) {
			sin_data.add(Math.sin(this.data.get(i)));
			cos_data.add(Math.cos(this.data.get(i)));
		}
		conversion_circular(this.type, temp_units, this.template, this.modulo, temp_zero, temp_rot);
		double sinr = 0;
		double cosr = 0;
		for(int i=0; i<data.size(); i++) {
			sinr += sin_data.get(i);
			cosr += cos_data.get(i);
		}
		double rbar = Math.sqrt(Math.pow(sinr,2) + Math.pow(cosr,2))/data.size();
		double circsd = Math.sqrt(-2*Math.log(rbar));
		return circsd;
	}
	
	private void conversion_circular(Type type, Units units, Template template, Modulo modulo, double zero, Rotation rotation) {
		Type typep = this.type;
		Units unitsp = this.units;
		Rotation rotationp = this.rotation;
		double zerop = zero;
		if(this.template==Template.geographics) {
			zero = Math.PI/2;
			this.rotation = Rotation.clock;
		} else if(this.template==Template.clock24) {
			zero = Math.PI/2;
			this.rotation = Rotation.clock;
		} else if (this.template==Template.clock12) {
			zero = Math.PI/2;
			this.rotation = Rotation.clock;
		}
		if(type==Type.directions && typep!=type) {
			for(int i=0; i<this.data.size(); i++) {
				this.data.set(i, 2*this.data.get(i));
				this.type = type;
			}
		}
		if (unitsp==Units.degrees && units==Units.radians) {
			for(int i=0; i<this.data.size(); i++)
				this.data.set(i, this.data.get(i)/180*Math.PI);
		} else if (unitsp==Units.radians && units==Units.degrees) {
			for(int i=0; i<this.data.size(); i++)
				this.data.set(i, this.data.get(i)/Math.PI*180);
		} else if (unitsp==Units.degrees && units==Units.hours) {
			for(int i=0; i<this.data.size(); i++)
				this.data.set(i, this.data.get(i)/180*12);
		} else if (unitsp==Units.radians && units==Units.hours) {
			for(int i=0; i<this.data.size(); i++)
				this.data.set(i, this.data.get(i)/Math.PI*12);
		} else if (unitsp==Units.hours && units==Units.degrees) {
			for(int i=0; i<this.data.size(); i++)
				this.data.set(i, this.data.get(i)/12*180);
		} else if (unitsp==Units.hours && units==Units.radians) {
			for(int i=0; i<this.data.size(); i++)
				this.data.set(i, this.data.get(i)/12*Math.PI);
		}
		this.units = units;
		
		if(zerop!=zero) {
			double zerod;
			double zeropd;
			if(units==Units.degrees) {
				zerod = zero*180/Math.PI;
				zeropd = zerop*180/Math.PI;
			} else if(units==Units.hours) {
				zerod = zero*12/Math.PI;
				zeropd = zerop*12/Math.PI;
			} else {
				zerod = zero;
				zeropd = zerop;
			}
			
			if(rotationp==Rotation.counter) {
				for(int i=0; i<this.data.size(); i++)
					this.data.set(i, this.data.get(i) + zeropd - zerod);
			} else {
				for(int i=0; i<this.data.size(); i++)
					this.data.set(i, this.data.get(i) - zeropd + zerod);
			}
			this.zero = zero;
		}
		if(rotationp != rotation) {
			for(int i=0; i<this.data.size(); i++)
				this.data.set(i, -this.data.get(i));
			this.rotation = rotation;
		}
		if(modulo != Modulo.asis) {
			double ang;
			if(modulo==Modulo.TwoPi) {
				ang = 2;
			} else {
				ang = 1;
			}
			if(this.units == Units.radians) {
				for(int i=0; i<data.size(); i++) {
					data.set(i, data.get(i) % (ang*Math.PI));
				}
			} else if(this.units == Units.degrees) {
				for(int i=0; i<data.size(); i++) {
					data.set(i, data.get(i) % (ang*180));
				}
			} else {
				for(int i=0; i<data.size(); i++) {
					data.set(i, data.get(i) % (ang*12)); //hours
				}
			}
		}
		this.modulo = modulo;
		if(zero%(2*Math.PI) != Math.PI/2)
			this.template = Template.none;
		if(rotation == Rotation.counter) {
			this.template = Template.none;
		}
	}

	private Double MeanCircularRad() {
		double sinr = 0.0;
		double cosr = 0.0;
		Double circmean = null;
		int i;
		
		for(i=0; i<this.data.size(); i++) {
			sinr += Math.sin(this.data.get(i));
			cosr += Math.cos(this.data.get(i));
		}
		if(Math.sqrt(Math.pow(sinr, 2)+Math.pow(cosr, 2))/this.data.size() > 2.22e-16) {
			circmean = Math.atan2(sinr, cosr);
		}
		return circmean;
	}
	
	private Double RangeCircularRad() {
		ArrayList<Double> temp = new ArrayList<Double>();
		ArrayList<Double> spacings = new ArrayList<Double>();
		for(int i=0; i<this.data.size(); i++) {
			temp.add(this.data.get(i) % (2*Math.PI));
		}
		Collections.sort(temp);
		if(temp.size() > 1) {
			for(int i=1; i<temp.size(); i++) {
				spacings.add(temp.get(i) - temp.get(i-1));
			}
		}
		spacings.add(temp.get(0) - temp.get(temp.size()-1) + 2*Math.PI);
		return 2*Math.PI - Collections.max(spacings);
	}
}
