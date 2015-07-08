package fcmps.ui;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
* <p>Title: 周计算类，星期一为一周的开始，星期日为一周的结束</p>
* <p>Description: 在两年的交接地带还有疑问。</p>
* <p>比如2006-12-29到2009-01-04，属于2008年的最后一周，</p>
* <p>2009-01-05位2009年第一周的开始。</p>
* <p>db2种的week_iso也是这样计算的</p>
* <p>Copyright: Copyright (c) 2006</p>
* <p>DateTime: 2006-4-11 23:36:39</p>
*
* @author gumpgz
* @version 1.0
*/
public class WeekUtil {
/**
* 取得当前日期是多少周
*
* @param date
* @return
*/
public static int getWeekOfYear(Date date) {
	Calendar c = new GregorianCalendar();
	c.setFirstDayOfWeek(Calendar.MONDAY);
	c.setMinimalDaysInFirstWeek(4);
	c.setTime (date);
	
	return c.get(Calendar.WEEK_OF_YEAR);
}

/**
 * 取得当前日期是多少周
 * @param date
 * @param MinimalDaysInFirstWeek [1]
 * @return
 */
public static int getWeekOfYear(Date date,int MinimalDaysInFirstWeek) {
	Calendar c = new GregorianCalendar();
	c.setFirstDayOfWeek(Calendar.MONDAY);
	c.setMinimalDaysInFirstWeek(MinimalDaysInFirstWeek);
	c.setTime (date);
	
	return c.get(Calendar.WEEK_OF_YEAR);
}

public static String getWeekOfYear(Date date,boolean FourDigit) {
	Calendar c = new GregorianCalendar();
	c.setFirstDayOfWeek(Calendar.MONDAY);
	c.setMinimalDaysInFirstWeek(4);
	c.setTime (date);

	//第一周, 但月份是12月,說明最後一周本年只占三天,屬於下一年度的第一周
	if(c.get(Calendar.WEEK_OF_YEAR)==1 && c.get(Calendar.MONTH)==Calendar.DECEMBER) {
		return String.valueOf(c.get(Calendar.YEAR)+1).substring(2)+Pad(String.valueOf(c.get(Calendar.WEEK_OF_YEAR)),"0",2,0);
	}
	
	//第一周, 但月份是1月,說明第一周本年只占三天,屬於前一年度的最後周
	if(c.get(Calendar.WEEK_OF_YEAR)>=52 && c.get(Calendar.MONTH)==Calendar.JANUARY) {
		return String.valueOf(c.get(Calendar.YEAR)-1).substring(2)+Pad(String.valueOf(c.get(Calendar.WEEK_OF_YEAR)),"0",2,0);
	}
		
	return String.valueOf(c.get(Calendar.YEAR)).substring(2)+Pad(String.valueOf(c.get(Calendar.WEEK_OF_YEAR)),"0",2,0);
	
//	return c.get(Calendar.WEEK_OF_YEAR);
}

/**
* 得到某一年周的总数
*
* @param year
* @return
*/
public static int getMaxWeekNumOfYear(int year) {
	Calendar c = new GregorianCalendar();
	c.set(year, Calendar.DECEMBER, 31, 23, 59, 59);

	c.setFirstDayOfWeek(Calendar.MONDAY);// 每周以周一开始
	c.setMinimalDaysInFirstWeek(4);// 每年的第一周必须大于或等于4天，否则就算上一年的最后一周
	
	//第一周, 但月份是12月,說明最後一周本年只占三天,屬於下一年度的第一周
	if(c.get(Calendar.WEEK_OF_YEAR)==1 && c.get(Calendar.MONTH)==Calendar.DECEMBER) {
		c.set(Calendar.DATE, c.get(Calendar.DATE)-(7-c.get(Calendar.DAY_OF_WEEK)+1));
	}    	
	
	return c.get(Calendar.WEEK_OF_YEAR);
}



/**
* 得到某年某周的第一天
*
* @param year eg:2014
* @param week eg:52
* @return
*/
public static Date getFirstDayOfWeek(int year, int week) {
	Calendar c = new GregorianCalendar();
	c.set(Calendar.YEAR, year);
	c.set(Calendar.MONTH, Calendar.JANUARY);
	c.set(Calendar.DATE, 1);
	c.setMinimalDaysInFirstWeek(4);
	c.setFirstDayOfWeek(Calendar.MONDAY);
	
	Calendar cal = (GregorianCalendar) c.clone();

	//第一周, 但月份是12月,說明最後一周本年只占三天,屬於下一年度的第一周
	if(c.get(Calendar.WEEK_OF_YEAR)==1 && c.get(Calendar.MONTH)==Calendar.DECEMBER) {
		if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
			cal.add(Calendar.DATE, week * 7);
		}else {
			cal.add(Calendar.DATE, week * 7-(7-c.get(Calendar.DAY_OF_WEEK)+1));
		}
	//第一周, 但月份是1月,說明第一周本年只占三天,屬於前一年度的最後周
	}else if(c.get(Calendar.WEEK_OF_YEAR)>=52 && c.get(Calendar.MONTH)==Calendar.JANUARY) {
		if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
			cal.add(Calendar.DATE, week * 7);
		}else {
			cal.add(Calendar.DATE, week * 7-(7-c.get(Calendar.DAY_OF_WEEK)+1));
		}
		
	}else {
		cal.add(Calendar.DATE , (week-1) * 7);
	}

	
//	cal.add(Calendar.DATE, week * 7);

	return getFirstDayOfWeek(cal.getTime());
}


/**
* 得到某年某周的第幾天
*
* @param year eg:2014
* @param week eg:52
* @return
*/
public static Date getDayOfWeek(int year, int week,int days) {
	Calendar c = new GregorianCalendar();
	c.set(Calendar.YEAR, year);
	c.set(Calendar.MONTH, Calendar.JANUARY);
	c.set(Calendar.DATE, 1);
	c.setMinimalDaysInFirstWeek(4);
	c.setFirstDayOfWeek(Calendar.MONDAY);
	
	Calendar cal = (GregorianCalendar) c.clone();

	//第一周, 但月份是12月,說明最後一周本年只占三天,屬於下一年度的第一周
	if(c.get(Calendar.WEEK_OF_YEAR)==1 && c.get(Calendar.MONTH)==Calendar.DECEMBER) {
		if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
			cal.add(Calendar.DATE, week * 7);
		}else {
			cal.add(Calendar.DATE, week * 7-(7-c.get(Calendar.DAY_OF_WEEK)+1));
		}
	//第一周, 但月份是1月,說明第一周本年只占三天,屬於前一年度的最後周
	}else if(c.get(Calendar.WEEK_OF_YEAR)>=52 && c.get(Calendar.MONTH)==Calendar.JANUARY) {
		if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
			cal.add(Calendar.DATE, week * 7);
		}else {
			cal.add(Calendar.DATE, week * 7-(7-c.get(Calendar.DAY_OF_WEEK)+1));
		}
		
	}else {
		cal.add(Calendar.DATE , (week-1) * 7);
	}

	Calendar c2 = new GregorianCalendar();
	c2.setFirstDayOfWeek(Calendar.MONDAY);
	c2.setMinimalDaysInFirstWeek(4);
	c2.setTime(cal.getTime());
	c2.set(Calendar.DAY_OF_WEEK, c2.getFirstDayOfWeek() + days); // Sunday
	return c2.getTime();
	
}

/**
* 得到某年某周的最后一天
*
* @param year eg:2014
* @param week eg:52
* @return
*/
public static Date getLastDayOfWeek(int year, int week) {
	Calendar c = new GregorianCalendar();
	c.set(Calendar.YEAR, year);
	c.set(Calendar.MONTH, Calendar.JANUARY);
	c.set(Calendar.DATE, 1);
	c.setMinimalDaysInFirstWeek(4);
	c.setFirstDayOfWeek(Calendar.MONDAY);
	
	Calendar cal = (GregorianCalendar) c.clone();

	//第一周, 但月份是12月,說明最後一周本年只占三天,屬於下一年度的第一周
	if(c.get(Calendar.WEEK_OF_YEAR)==1 && c.get(Calendar.MONTH)==Calendar.DECEMBER) {
		if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
			cal.add(Calendar.DATE, week * 7);
		}else {
			cal.add(Calendar.DATE, week * 7-(7-c.get(Calendar.DAY_OF_WEEK)+1));
		}
	//第一周, 但月份是1月,說明第一周本年只占三天,屬於前一年度的最後周
	}else if(c.get(Calendar.WEEK_OF_YEAR)>=52 && c.get(Calendar.MONTH)==Calendar.JANUARY) {
		if(c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
			cal.add(Calendar.DATE, week * 7);
		}else {
			cal.add(Calendar.DATE, week * 7-(7-c.get(Calendar.DAY_OF_WEEK)+1));
		}
	}else {
		cal.add(Calendar.DATE , (week-1) * 7);
	}
	
	return getLastDayOfWeek(cal.getTime());
}

/**
* 取得当前日期所在周的第一天
*
* @param date
* @return
*/
public static Date getFirstDayOfWeek(Date date) {
	Calendar c = new GregorianCalendar();
	c.setFirstDayOfWeek(Calendar.MONDAY);
	c.setMinimalDaysInFirstWeek(4);
	
	c.setTime(date);
	c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
	return c.getTime ();
}

/**
* 取得当前日期所在周的最后一天
*
* @param date
* @return
*/
public static Date getLastDayOfWeek(Date date) {
	Calendar c = new GregorianCalendar();
	c.setFirstDayOfWeek(Calendar.MONDAY);
	c.setMinimalDaysInFirstWeek(4);
	c.setTime(date);
	c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6); // Sunday
	return c.getTime();
}

/**
 * 將字串左邊或右邊補特定字符以達到指定的長度,並返回新字串
 * @param str  需要補充的字串
 * @param pstr 補充的字符
 * @param len  長度
 * @param direction 方向,0為左邊,1為右邊
 * @return
 */
public static String Pad(String str,String pstr,int len,int direction){
	String iRet=str;
	
	while(iRet.length()<len){
		if(direction==0){
			iRet=pstr+iRet;
		}else{
			iRet=iRet+pstr;
		}    		
	}
	return iRet;
}
}
