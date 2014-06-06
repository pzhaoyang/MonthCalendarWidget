package com.unipro.monthcalendar;

import java.util.Calendar;

import android.text.format.Time;

public class GregorianUtil {
    private final static String[][] GRE_FESTVIAL = {
            {"元旦", "", "", "", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",""},
            {"", "湿地", "", "", "", "", "", "", "", "", "", "", "", "情人", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", "妇女", "", "", "", "植树", "", "", "消权", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", ""},
            {"愚人", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "地球", "", "", "", "", "", "", "", "", ""},
            {"劳动", "", "", "青年", "", "", "", "", "", "", "", "护士", "", "", "", "", "", "博物", "","", "", "", "", "", "", "", "", "", "", "", ""},
            {"儿童", "", "", "", "环境", "", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "", "奥匹", "骨质", "", "", "", "", "", "", ""},
            {"建党", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", ""},
            {"建军", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", "", "", "教师", "", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", ""},
            {"国庆", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", "", "", "", "光棍", "", "", "", "", "", "学生", "", "","", "", "", "", "", "", "", "", "", "", "", ""},
            {"艾滋", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "","", "", "", "", "平安", "圣诞", "", "", "", "", "", ""},
    };
    private int mMonth;
    private int mDay;

    public GregorianUtil(Calendar calendar) {
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DATE);
    }

    public String getGremessage() {
    	if(IsMotherDay()){
    		return "母亲";
    	}else if(IsFatherDay()){
    		return "父亲";
    	}
        return GRE_FESTVIAL[mMonth][mDay - 1];
    }
    
    private boolean IsMotherDay(){
    	
    	if(mMonth == 4){
    		Calendar cal = Calendar.getInstance();
        	Time tm = new Time();
        	tm.setToNow();
        	cal.set(tm.year, tm.month, 1);
        	
    		int firstsunday = mDay-7;
    		int shift = 1 + Calendar.SUNDAY - cal.get(Calendar.DAY_OF_WEEK );
    		int sft =  shift <0 ? shift + 7 : shift;
    		return sft == firstsunday ? true : false;
    	}
    	return false;
    	
    }

    private boolean IsFatherDay(){
    	if(mMonth == 5){
    		Calendar cal = Calendar.getInstance();
        	Time tm = new Time();
        	tm.setToNow();
        	cal.set(tm.year, tm.month, 1);
        	
    		int fatherday = mDay - 2*7;
    		int shift = 1 + Calendar.SUNDAY - cal.get(Calendar.DAY_OF_WEEK );
    		int firstsunday =  shift <0 ? shift +7 : shift;
    		return firstsunday == fatherday ? true : false;
    	}
    	return false;
    }
}
