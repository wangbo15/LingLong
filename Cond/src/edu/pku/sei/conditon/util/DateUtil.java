package edu.pku.sei.conditon.util;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * The class for date operations
 * @author Bo Wang
 */
public class DateUtil {
	
	public static boolean in24HoursFromNow(long past) {
		long current = System.currentTimeMillis();
		if(past >= current) {
			return false;
		}
		current -= past;
		double result = current * 1.0 / (1000 * 60 *60);
		return result <= 24 ? true : false;
	}
	
	
	
	public static String getFormatedCurrDateForFileName() {
		Date date = new Date();
		return getFormatedDateForFileName(date);
	}
	
	public static String getFormatedDateForFileName(Date date) {
		return getFormatedDate(date, "yyyy-MM-dd-HH-mm-ss");
	}
	
	public static String getFormatedCurrDate(String format) {
		Date date = new Date();
		return getFormatedDate(date, format);
	}
	
	public static String getFormatedDate(Date date, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String dateString = formatter.format(date);
		return dateString;
	}
	
	private static String unitFormat(long i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Long.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
	
	public static int millisecToSecond(long millisecond) {
		assert millisecond >= 0;
		int res = (int) (millisecond / 1000);
		if(millisecond % 1000 == 0) {
			res += 1;
		}
		return res;
	}
	
	public static int millisecToMinute(long millisecond) {
		assert millisecond >= 0;
		final int count = 1000 * 60; 
		int res = (int) (millisecond / count);
		if(millisecond % count == 0) {
			res += 1;
		}
		return res;
	}
	
    public static String millisecToTimeStr(long millisecond) {
    	millisecond = millisecond/1000;
        String timeStr = null;
        long hour = 0;
        long minute = 0;
        long second = 0;
        if (millisecond <= 0)
            return "0s";
        else {
            minute = millisecond / 60;
            if (minute == 0) {
                second = millisecond % 60;
                timeStr = second + "s";
            } else if (minute < 60) {
                second = millisecond % 60;
                timeStr = minute + "m" + (second > 0 ? unitFormat(second) + "s" : "");
            } else {
                hour = minute / 60;
                minute = minute % 60;
                second = millisecond - hour * 3600 - minute * 60;
                timeStr = hour + "h" + (minute > 0 ? unitFormat(minute) + "m" : "") + 
                		(second > 0 ? unitFormat(second) + "s" : "");
            }
        }
        return timeStr;
    }

}
