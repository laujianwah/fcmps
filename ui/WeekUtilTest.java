package fcmps.ui;

import java.util.Date;

import junit.framework.TestCase;

public class WeekUtilTest extends TestCase {
	public void test_WEEK() {
		System.out.println(WeekUtil.getDayOfWeek(2015,5,4));
//		
//		System.out.println(WeekUtil.getFirstDayOfWeek(2013,52));
//		System.out.println(WeekUtil.getLastDayOfWeek(2013,52));
//		
//		System.out.println(WeekUtil.getWeekOfYear(new Date()));
//		
		
//		System.out.println(WeekUtil.getFirstDayOfWeek(2014,1));
//		System.out.println(WeekUtil.getLastDayOfWeek(2014,1));
//		
//		System.out.println(WeekUtil.getFirstDayOfWeek(2015,1));
//		System.out.println(WeekUtil.getLastDayOfWeek(2015,1));
//		
//		System.out.println(WeekUtil.getFirstDayOfWeek(2016,1));
//		System.out.println(WeekUtil.getLastDayOfWeek(2016,1));
//		
//		System.out.println(WeekUtil.getFirstDayOfWeek(2017,1));
//		System.out.println(WeekUtil.getLastDayOfWeek(2017,1));
//		
//		System.out.println(WeekUtil.getFirstDayOfWeek(2018,1));
//		System.out.println(WeekUtil.getLastDayOfWeek(2018,1));
		
//		System.out.println(getPrevious_Week(953, 1));
//		
//		System.out.println(getPrevious_Week(1001, 1));
//		
//		System.out.println(getPrevious_Week(1051, 1));
//		System.out.println(getPrevious_Week(1101, 1));
		
//		System.out.println(FCMPS_PUBLIC.getPrevious_Week(1001, 1));
		
		
	}

	private int getNext_Week(int WeekOfYear,int weeks) {
		int iRet=-1;
		int year=0;
		int week=0;
		
		if(String.valueOf(WeekOfYear).length()<3) return iRet;
		
		if(String.valueOf(WeekOfYear).length()==3) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,1))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(1));
		}
		   		
		if(String.valueOf(WeekOfYear).length()==4) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,2))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(2));
		}
		
		for(int i=1;i<=weeks;i++) {
			if(WeekUtil.getMaxWeekNumOfYear(year)==week) {
				year=year+1;
				week=1;
			}else {
				week=week+1;				
			}
		}

		return Integer.valueOf(String.valueOf(year).substring(2)+WeekUtil.Pad(String.valueOf(Integer.valueOf(week)),"0",2,0));

	}
	
	private int getPrevious_Week(int WeekOfYear,int weeks) {
		int iRet=-1;
		int year=0;
		int week=0;
		
		if(String.valueOf(WeekOfYear).length()<3) return iRet;
		
		if(String.valueOf(WeekOfYear).length()==3) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,1))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(1));
		}
		   		
		if(String.valueOf(WeekOfYear).length()==4) {
			year=Integer.valueOf(String.valueOf(WeekOfYear).substring(0,2))+2000;
			week=Integer.valueOf(String.valueOf(WeekOfYear).substring(2));
		}
		
		for(int i=1;i<=weeks;i++) {
			if(1==week) {
				year=year-1;
				week=WeekUtil.getMaxWeekNumOfYear(year);
			}else {
				week=week-1;				
			}
		}

		return Integer.valueOf(String.valueOf(year).substring(2)+WeekUtil.Pad(String.valueOf(Integer.valueOf(week)),"0",2,0));

	}
	
}
