package aggregation;

import java.util.List;

public abstract class Aggregate {
	protected int count;
	protected List<String> columnNames;
	protected List<String> indexNames;
	protected double[][] values;
	
	protected abstract void setNames();
	protected abstract void setValues();
	
	public Aggregate() {
		setNames();
		this.count = columnNames.size();
	}
	
	public int get_count() {
		return count;
	}
	public List<String> get_names() {
		return columnNames;
	}
	public double[] get_values(int row) {
		return values[row];
	}
	@Override
	public String toString() {
		if(columnNames.size() == values.length) {
			String out="";
			int length=0;
			for (String name : columnNames) {
				if(name.length() > length)
					length=name.length();
			}
			for (String name : columnNames) {
				out+=String.format("%"+length+"s\t", name);
			}
			out+="\n";
			for(int i=0;i<values.length;i++) {
				out+=String.format("%"+length+"f\t", values[i]);
			}
			return out;
		} else if(columnNames.size() < values.length) {
			String out="";
			int length=0;
			for (String name : columnNames) {
				if(name.length() > length)
					length=name.length();
			}
			//out+=getMultiLinePrefix(true, 0);
			for (String name : columnNames) {
				out+=String.format("%"+length+"s\t", name);
			}
			out+="\n";
			for(int i=0;i<(values.length/count);i++) {
				//out+=getMultiLinePrefix(false, i+1);
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
