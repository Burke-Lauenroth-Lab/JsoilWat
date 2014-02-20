package aggregation;

import java.util.List;

public abstract class Aggregate {
	protected int count;
	protected List<String> names;
	protected double[] values;
	protected boolean multiline;
	
	protected abstract void setNames();
	protected abstract void setValues();
	abstract String getMultiLinePrefix(boolean header, int line);
	
	public Aggregate() {
		setNames();
		this.count = names.size();
	}
	
	public int get_count() {
		return count;
	}
	public List<String> get_names() {
		return names;
	}
	public double[] get_values() {
		return values;
	}
	@Override
	public String toString() {
		if(names.size() == values.length) {
			String out="";
			int length=0;
			for (String name : names) {
				if(name.length() > length)
					length=name.length();
			}
			for (String name : names) {
				out+=String.format("%"+length+"s\t", name);
			}
			out+="\n";
			for(int i=0;i<values.length;i++) {
				out+=String.format("%"+length+"f\t", values[i]);
			}
			return out;
		} else if(names.size() < values.length) {
			String out="";
			int length=0;
			for (String name : names) {
				if(name.length() > length)
					length=name.length();
			}
			out+=getMultiLinePrefix(true, 0);
			for (String name : names) {
				out+=String.format("%"+length+"s\t", name);
			}
			out+="\n";
			for(int i=0;i<(values.length/count);i++) {
				out+=getMultiLinePrefix(false, i+1);
				for(int j=0;j<count;j++) {
					out+=String.format("%"+length+"f\t", values[j + (i*count)]);
				}
				out+="\n";
			}
			return out;
		} else {
			return "";
		}
	}
}
