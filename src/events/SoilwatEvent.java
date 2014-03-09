package events;

public class SoilwatEvent {
	protected int year;
	protected double Percent;
	
	public SoilwatEvent(int Year, double Percent) {
		this.year = Year;
		this.Percent = Percent;
	}
	
	protected void setYear(int Year) {
		year = Year;
	}
	public int getYear() {
		return year;
	}
	public double getPercent() {
		return Percent;
	}
}
