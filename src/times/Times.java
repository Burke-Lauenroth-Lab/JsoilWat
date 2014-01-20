package times;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class Times {
	public static enum TwoDays {
		Yesterday, Today
	}
	public static enum Months {
		Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, NoMonth
	}
	public static final int DAYFIRST_NORTH = 1;
	public static final int DAYLAST_NORTH = 366;
	public static final int DAYFIRST_SOUTH = 183;
	public static final int DAYLAST_SOUTH = 182;
	public static final int DAYMID_NORTH = 183;
	public static final int DAYMID_SOUTH = 366;
	/* The above define the beginning, ending and middle
	 * days of the year for northern and southern
	 * hemispheres, so there won't be a coding accident.
	 * The user need only supply a 0/1 flag in the file
	 * containing the start/end years.
	 */
	public static final int MAX_MONTHS = 12;
	public static final int MAX_WEEKS = 53;
	public static final int MAX_DAYS = 366;
	
	public static final int WKDAYS = 7;
	/* number of days in each week. unlikely to change, but
	 * useful as a readable indicator of usage where it occurs.
	 * On the other hand, it is conceivable that one might be
	 * interested in 4, 5, or 6 day periods, but redefine it
	 * in specific programs and take responsibility there,
	 * not here.
	 */
	
	
	private static int last_doy;
	private static int[] monthdays = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static int[] days_in_month = new int[MAX_MONTHS], cum_monthdays = new int[MAX_MONTHS + 1];
	private static Date _timestamp;
	private static Calendar _tym; /* "current" time for the code */
	
	public static Date get_timestamp() {
		return _timestamp;
	}
	public static void set_timestamp(Date _timestamp) {
		Times._timestamp = _timestamp;
	}
	private static void _reinit() {
		int m;
		
		days_in_month[Months.Feb.ordinal()] = (isleapyear_now()) ? 29 : 28;
		cum_monthdays[Months.Jan.ordinal()] = days_in_month[Months.Jan.ordinal()];
		for (m = Months.Feb.ordinal(); m < Months.NoMonth.ordinal(); m++)
			cum_monthdays[m] = days_in_month[m] + cum_monthdays[m - 1];

		last_doy = cum_monthdays[Months.Dec.ordinal()];
	}
	private static int _yearto4digit_t() {
		return _tym.get(Calendar.YEAR);
	}
	private static void _remaketime() {
		set_timestamp(_tym.getTime());
	}
	
	
	public static void Time_init() {
		cum_monthdays[Months.NoMonth.ordinal()] = 1000;
		Time_now();
	}
	public static void Time_now() {
		_tym = Calendar.getInstance();
		_reinit();
	}
	public static void Time_new_year(int year) {
		_tym.set(Calendar.YEAR, year);
		_reinit();
		Time_set_doy(1);
	}
	public static void Time_next_day() {
		_tym.add(Calendar.DAY_OF_MONTH, 1);
		_reinit();
	}
	public static void Time_set_year(int year) {
		_tym.set(Calendar.YEAR, year);
		_reinit();
		_remaketime();
	}
	public static void Time_set_doy(int doy) {
		_tym.set(Calendar.DAY_OF_YEAR, doy);
		_remaketime();
	}
	public static void Time_set_mday(int day) {
		_tym.set(Calendar.DAY_OF_MONTH, day);
		_remaketime();
	}
	public static void Time_set_month(int mon) {
		_tym.set(Calendar.MONTH, mon);
		_remaketime();
	}
	public static Date Time_timestamp() {
		/* returns the timestamp of the "model" time.  to get
		 * actual timestamp, call Time_timestamp_now()
		 */
		return _tym.getTime();
	}
	public static Date Time_timestamp_now() {
		/* returns the timestamp of the current real time.
		 */
		return Calendar.getInstance().getTime();
	}
	public static int Time_lastDOY() {
		return cum_monthdays[Months.Dec.ordinal()];
	}
	public static int Time_days_in_month(Times.Months month) {
		return days_in_month[month.ordinal()];
	}
	public static String Time_printtime() {
		return new SimpleDateFormat("E MMM dd HH:mm:ss").format(_tym.getTime());
	}
	public static String Time_daynmshort() {
		return new SimpleDateFormat("E").format(_tym.getTime());
	}
	public static String Time_daynmshort_d(int doy) {
		Calendar tmp = _tym;
		tmp.set(Calendar.DAY_OF_YEAR, doy);
		return new SimpleDateFormat("E").format(tmp.getTime());
	}
	public static String Time_daynmshort_dm(int mday, int mon) {
		Calendar tmp = _tym;
		tmp.set(Calendar.MONTH, mon);
		tmp.set(Calendar.DAY_OF_MONTH, mday);
		return new SimpleDateFormat("E").format(tmp.getTime());
	}
	public static String Time_daynmlong() {
		return new SimpleDateFormat("EEEE").format(_tym.getTime());
	}
	public static String Time_daynmlong_d(int doy) {
		Calendar tmp = _tym;
		tmp.set(Calendar.DAY_OF_YEAR, doy);
		return new SimpleDateFormat("EEEE").format(tmp.getTime());
	}
	public static String Time_daynmlong_dm(int mday, int mon) {
		Calendar tmp = _tym;
		tmp.set(Calendar.MONTH, mon);
		tmp.set(Calendar.DAY_OF_MONTH, mday);
		return new SimpleDateFormat("EEEE").format(tmp.getTime());
	}
	/* =================================================== */
	/* simple methods to return state values */

	public static int Time_get_year() {
		return _tym.get(Calendar.YEAR);
	}
	public static int Time_get_doy() {
		return _tym.get(Calendar.DAY_OF_YEAR);
	}
	public static int Time_get_month() {
		return _tym.get(Calendar.MONTH);
	}
	public static int Time_get_week() {
		return _tym.get(Calendar.WEEK_OF_YEAR);
	}
	public static int Time_get_mday() {
		return _tym.get(Calendar.DAY_OF_MONTH);
	}
	public static int Time_get_hour() {
		return _tym.get(Calendar.HOUR);
	}
	public static int Time_get_mins() {
		return _tym.get(Calendar.MINUTE);
	}
	public static int Time_get_secs() {
		return _tym.get(Calendar.SECOND);
	}
	public static int Time_get_lastdoy() {
		
		return last_doy;
	}
	public static int Time_get_lastdoy_y(int year) {
		return isleapyear(year) ? 366 : 365;
	}
//=========================================================//
	public static int doy2month(int doy) {
		/* =================================================== */
		/* doy is base1, mon becomes base0 month containing doy.
		 * note mon can't become 13, so any day after Nov 30
		 * returns Dec.
		 */
		int mon;

		for (mon = Months.Jan.ordinal(); mon < Months.Dec.ordinal() && doy > cum_monthdays[mon]; mon++);
		return mon;
	}

	public static int doy2mday(int doy) {
		/* =================================================== */
		/* doy is base1, mon becomes base0 month containing doy.
		 * note mon can't become 13, so any day after Nov 30
		 * returns Dec.
		 */

		int mon = doy2month(doy);
		return (mon == Months.Jan.ordinal()) ? doy : doy - cum_monthdays[mon - 1];

	}

	public static int doy2week(int doy) {
		/* =================================================== */
		/* Enter with doy base1 and return base0 number of 7 day
		 * periods (weeks) since beginning of year. Note that week
		 * number doesn't necessarily correspond to the calendar week.
		 * Jan 1 starts on different days depending on the year.  In
		 * 2000 it started on Sun: each year later it starts 1 day
		 * later, each year earlier it started one day earlier.
		 */
		return ((((doy) - 1) / WKDAYS));
	}

	public static int yearto4digit(int yr) {
		/* =================================================== */
		/* handle the Y2K problems */

		return ((yr > 100) ? yr : (yr < 50) ? 2000 + yr : 1900 + yr);
	}

	public static boolean isleapyear_now() {
		/* =================================================== */
		/* check current year from struct tm */
		return isleapyear(_yearto4digit_t());
	}

	public static boolean isleapyear(int year) {
		/* =================================================== */

		int yr = (year > 100) ? year : yearto4digit(year);
		int t = (yr / 100) * 100;

		return (((yr % 4) == 0) && (((t) != yr) || ((yr % 400) == 0)));
	}
	public static void interpolate_monthlyValues(double[] monthlyValues, double[] dailyValues) {
		int doy, mday, month, month2 = Months.NoMonth.ordinal();
		double sign = 1.;
		for (doy = 1; doy <= MAX_DAYS; doy++) {
			mday = doy2mday(doy);
			month = doy2month(doy);

			if (mday == 15) {
				dailyValues[doy] = monthlyValues[month];
			} else {
				if (mday >= 15) {
					month2 = (month == Months.Dec.ordinal()) ? Months.Jan.ordinal() : month + 1;
					sign = 1;
				} else {
					month2 = (month == Months.Jan.ordinal()) ? Months.Dec.ordinal() : month - 1;
					sign = -1;
				}

				dailyValues[doy] = monthlyValues[month] + sign * (monthlyValues[month2] - monthlyValues[month]) / (monthdays[month]) * (mday - 15.);
			}
		}
	}
	
}
