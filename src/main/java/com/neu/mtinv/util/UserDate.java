package com.neu.mtinv.util;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

@Component
public class UserDate {
	Date d = new Date();
	GregorianCalendar gc = new GregorianCalendar();
	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");


	public String getYears() {
		gc.setTime(d);
		gc.add(1, +1);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc
				.get(Calendar.DATE));
		return sf.format(gc.getTime());
	}

	public String getHours(Date da,int h) {
		gc.setTime(da);
		gc.set(Calendar.HOUR_OF_DAY, gc.get(Calendar.HOUR_OF_DAY)+h);
		SimpleDateFormat sfh = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sfh.format(gc.getTime());
	}

	public String getHours(String da,int h) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			gc.setTime(sdf.parse(da));
		} catch (ParseException e) {
			System.out.println(e);
		}
		gc.set(Calendar.HOUR_OF_DAY, gc.get(Calendar.HOUR_OF_DAY)+h);
		SimpleDateFormat sfh = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sfh.format(gc.getTime());
	}

	public String getHalfYearLater() {
		gc.setTime(d);
		gc.add(2, +6);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc
				.get(Calendar.DATE));
		return sf.format(gc.getTime());
	}
	

	public String getHalfYear() {
		gc.setTime(d);
		gc.add(2, -6);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc
				.get(Calendar.DATE));
		return sf.format(gc.getTime());
	}


	public String getQuarters() {
		gc.setTime(d);
		gc.add(5, -7);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc
				.get(Calendar.DATE));
		return sf.format(gc.getTime());
	}
	

	public String getDateBefore(int day) {
		gc.setTime(d);
		gc.add(5, -day);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc
				.get(Calendar.DATE));
		return sf.format(gc.getTime());
	}
	

	public String getDateAfter(int day) {
		gc.setTime(d);
		gc.add(5, day);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc
				.get(Calendar.DATE));
		return sf.format(gc.getTime());
	}

	public String getLastMonthDay() {
		gc.setTime(d);
		gc.add(Calendar.MONTH, -1);
		int Maxday = gc.getActualMaximum(Calendar.DAY_OF_MONTH);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), Maxday);
		return sf.format(gc.getTime());
	}

	public String getFristMonthDay() {
		gc.setTime(d);
		gc.add(Calendar.MONTH, -1);
		gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), 1);
		return sf.format(gc.getTime());
	}

	public String getLocalDate() {
		return sf.format(d);
	}
	

	public String[] getSixMonth() {
		String dd = sf.format(d);
		String year_s = dd.substring(0,4);
		String month_s = dd.substring(5,7);
		
		int year = Integer.parseInt(year_s);
		int month = Integer.parseInt(month_s);
		
		int month_n = 0;
		int year_n = 0;
		
		String ym = "";
		String m[] = new String[6];
		for(int i=0; i<6; i++){
			
			year_n = year;
			month_n = month - i;
			if(month_n<=0){
				year_n = year - 1;
				month_n = month_n + 12;
			}
			
			if(month_n<10){
				ym = year_n + "0" + month_n;
				m[i] = ym;
			}else{
				ym = year_n + "" + month_n;
				m[i] = ym;
			}
		}
		return m;
	}

	public int getDate(int yy, int mm,int dd) {
		if (((yy % 4 == 0) & (yy % 100 != 0)) | (yy % 400 == 0)) {
			// ����
			if (mm == 1) {
				return dd;
			}
			if (mm == 2) {
				return 31 + dd;
			}
			if (mm == 3) {
				return 31 + 29 + dd;
			}
			if (mm == 4) {
				return 31 + 29 + 31 + dd;
			}
			if (mm == 5) {
				return 31 + 29 + 31 + 30 + dd;
			}
			if (mm == 6) {
				return 31 + 29 + 31 + 30 + 31 + dd;
			}
			if (mm == 7) {
				return 31 + 29 + 31 + 30 + 31 + 30 + dd;
			}
			if (mm == 8) {
				return 31 + 29 + 31 + 30 + 31 + 30 + 31 + dd;
			}
			if (mm == 9) {
				return 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + dd;
			}
			if (mm == 10) {
				return 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + dd;
			}
			if (mm == 11) {
				return 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + dd;
			}
			if (mm == 12) {
				return 31 + 29 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30 + dd;
			}else {
				return -1;
			}
		} else {
			// ������
			if (mm == 1) {
				return dd;
			}
			if (mm == 2) {
				return 31 + dd;
			}
			if (mm == 3) {
				return 31 + 28 + dd;
			}
			if (mm == 4) {
				return 31 + 28 + 31 + dd;
			}
			if (mm == 5) {
				return 31 + 28 + 31 + 30 + dd;
			}
			if (mm == 6) {
				return 31 + 28 + 31 + 30 + 31 + dd;
			}
			if (mm == 7) {
				return 31 + 28 + 31 + 30 + 31 + 30 + dd;
			}
			if (mm == 8) {
				return 31 + 28 + 31 + 30 + 31 + 30 + 31 + dd;
			}
			if (mm == 9) {
				return 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + dd;
			}
			if (mm == 10) {
				return 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + dd;
			}
			if (mm == 11) {
				return 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + dd;
			}
			if (mm == 12) {
				return 31 + 28 + 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30 + dd;
			}else {
				return -1;
			}
		}
	}
	
	
	public String getStrDate() throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("",Locale.SIMPLIFIED_CHINESE);
		
		sdf.applyPattern("yyyy-MM-dd HH:mm:ss"); 

		String timeStr = sdf.format(new Date()); 
		
		return timeStr;
	}
	
	public Date getDate() throws ParseException{
		SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");      
		String timeStr = dateformat.format(new Date()); 
		Date date = dateformat.parse(timeStr);
		return date;
	}
	
	public Date getCcDate() throws ParseException{
		SimpleDateFormat dateformat=new SimpleDateFormat("yyyy-MM-dd HH:mm");      
		String timeStr = dateformat.format(new Date()); 
		Date date = dateformat.parse(timeStr);
		return date;
	}
}