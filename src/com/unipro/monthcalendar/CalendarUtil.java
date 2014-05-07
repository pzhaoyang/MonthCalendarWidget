package com.unipro.monthcalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.unipro.monthcalendar.SolarTermsUtil;
public class CalendarUtil {

    private final static String CHINESE_NUMBER[] = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "腊"};

    private final static String WEEK_NUMBER[] = {"日", "一", "二", "三", "四", "五", "六"};

    private final static long[] LUNAR_INFO = new long[]{0x04bd8, 0x04ae0, 0x0a570,
            0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
            0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0,
            0x0ada2, 0x095b0, 0x14977, 0x04970, 0x0a4b0, 0x0b4b5, 0x06a50,
            0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, 0x06566,
            0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0,
            0x1c8d7, 0x0c950, 0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4,
            0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, 0x06ca0, 0x0b550,
            0x15355, 0x04da0, 0x0a5d0, 0x14573, 0x052d0, 0x0a9a8, 0x0e950,
            0x06aa0, 0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260,
            0x0f263, 0x0d950, 0x05b57, 0x056a0, 0x096d0, 0x04dd5, 0x04ad0,
            0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b5a0, 0x195a6,
            0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40,
            0x0af46, 0x0ab60, 0x09570, 0x04af5, 0x04970, 0x064b0, 0x074a3,
            0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, 0x0c960,
            0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0,
            0x092d0, 0x0cab5, 0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9,
            0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, 0x07954, 0x06aa0,
            0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65,
            0x0d530, 0x05aa0, 0x076a3, 0x096d0, 0x04bd7, 0x04ad0, 0x0a4d0,
            0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, 0x0b5a0, 0x056d0, 0x055b2,
            0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0};

    private static SimpleDateFormat chineseDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    //计算得到的农历年月日
    private int mLuchYear;
    private int mLuchMonth;
    private int mLuchDay;
    private boolean isLoap;//闰年标志
    private Calendar mCurrenCalendar;
    private static int yearDays(int year) {
        int i, sum = 348;
        for (i = 0x8000; i > 0x8; i >>= 1) {
            if ((LUNAR_INFO[year - 1900] & i) != 0)
                sum += 1;
        }
        return (sum + leapDays(year));
    }

    private static int leapDays(int year) {
        if (leapMonth(year) != 0) {
            if ((LUNAR_INFO[year - 1900] & 0x10000) != 0)
                return 30;
            else
                return 29;
        } else
            return 0;
    }
    
    private static int leapMonth(int year) {
        return (int) (LUNAR_INFO[year - 1900] & 0xf);
    }

    private static int monthDays(int year, int month) {
        if ((LUNAR_INFO[year - 1900] & (0x10000 >> month)) == 0)
            return 29;
        else
            return 30;
    }
    public String animalsYear() {
        final String[] Animals = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇",
                "马", "羊", "猴", "鸡", "狗", "猪"};
        return Animals[(mLuchYear - 4) % 12];
    }
    
    private static String cyclicalm(int num) {
        final String[] Gan = new String[]{"甲", "乙", "丙", "丁", "戊", "己", "庚",
                "辛", "壬", "癸"};
        final String[] Zhi = new String[]{"子", "丑", "寅", "卯", "辰", "巳", "午",
                "未", "申", "酉", "戌", "亥"};

        return (Gan[num % 10] + Zhi[num % 12]);
    }

    public String cyclical() {
        int num = mLuchYear - 1900 + 36;
        return (cyclicalm(num));
    }
    
    public CalendarUtil(Calendar cal) {
        int yearCyl, monCyl, dayCyl;
        mCurrenCalendar = cal;
        int leapMonth = 0;
        Date baseDate = null;
        try {
            baseDate = chineseDateFormat.parse("1900年1月31日");
        } catch (ParseException e) {
            e.printStackTrace(); 
        }

        int offset = (int) ((cal.getTime().getTime() - baseDate.getTime()) / 86400000L);
        dayCyl = offset + 40;
        monCyl = 14;
        
        int iYear, daysOfYear = 0;
        for (iYear = 1900; iYear < 2050 && offset > 0; iYear++) {
            daysOfYear = yearDays(iYear);
            offset -= daysOfYear;
            monCyl += 12;
        }
        
        if (offset < 0) {
            offset += daysOfYear;
            iYear--;
            monCyl -= 12;
        }
        
        mLuchYear = iYear;

        yearCyl = iYear - 1864;
        leapMonth = leapMonth(iYear); // 闰哪个月,1-12
        isLoap = false;

        int iMonth, daysOfMonth = 0;
        for (iMonth = 1; iMonth < 13 && offset > 0; iMonth++) {

            if (leapMonth > 0 && iMonth == (leapMonth + 1) && !isLoap) {
                --iMonth;
                isLoap = true;
                daysOfMonth = leapDays(mLuchYear);
            } else
                daysOfMonth = monthDays(mLuchYear, iMonth);

            offset -= daysOfMonth;

            if (isLoap && iMonth == (leapMonth + 1))
                isLoap = false;
            if (!isLoap)
                monCyl++;
        }

        if (offset == 0 && leapMonth > 0 && iMonth == leapMonth + 1) {
            if (isLoap) {
                isLoap = false;
            } else {
                isLoap = true;
                --iMonth;
                --monCyl;
            }
        }

        if (offset < 0) {
            offset += daysOfMonth;
            --iMonth;
            --monCyl;

        }
        mLuchMonth = iMonth;
        mLuchDay = offset + 1;
    }

    public static String getChinaDayString(int day) {
        String chineseTen[] = {"初", "十", "廿", "卅"};
        int n = day % 10 == 0 ? 9 : day % 10 - 1;
        if (day > 30)
            return "";
        if (day == 10)
            return "初十";
        else
            return chineseTen[day / 10] + CHINESE_NUMBER[n];
    }
    
    public String toString() {
        String message = "";
        int n = mLuchDay % 10 == 0 ? 9 : mLuchDay % 10 - 1;
        message = getChinaCalendarMsg(mLuchYear, mLuchMonth, mLuchDay);
        if (isNullOrEmpty(message)) {
            String solarMsg = new SolarTermsUtil(mCurrenCalendar).getSolartermsMsg();
            //判断当前日期是否为节气
            if (!isNullOrEmpty(solarMsg)) {
                message = solarMsg;
            } else {
                /**
                 * 判断当前日期是否为公历节日
                 */
                String gremessage = new GregorianUtil(mCurrenCalendar).getGremessage();
                if (!isNullOrEmpty(gremessage)) {
                    message = gremessage;
                } else if (mLuchDay == 1) {
                    message = CHINESE_NUMBER[mLuchMonth - 1] + "月";
                } else {
                    message = getChinaDayString(mLuchDay);
                }

            }
        }
        return message;
    }
    
    public String getDay() {
        return (isLoap ? "闰" : "") + CHINESE_NUMBER[mLuchMonth - 1] + "月"
                + getChinaDayString(mLuchDay);
    }
    
    public String getMonth() {
        return (isLoap ? "闰" : "") + CHINESE_NUMBER[mLuchMonth - 1];
    }
    
    public static String getDay(Calendar calendar) {
        return simpleDateFormat.format(calendar.getTime());
    }

    public static boolean compare(Date compareDate, Date currentDate) {
        return chineseDateFormat.format(compareDate).compareTo(chineseDateFormat.format(currentDate)) >= 0;
    }

    public static String getWeek(Calendar calendar) {
        return "周" + WEEK_NUMBER[calendar.get(Calendar.DAY_OF_WEEK) - 1] + "";
    }

    public static String getCurrentDay(Calendar calendar) {
        return getDay(calendar) + " 农历" + new CalendarUtil(calendar).getDay() + " " + getWeek(calendar);
    }


    private String getChinaCalendarMsg(int year, int month, int day) {
        String message = "";
        if (((month) == 1) && day == 1) {
            message = "春节";
        } else if (((month) == 1) && day == 15) {
            message = "元宵";
        } else if (((month) == 5) && day == 5) {
            message = "端午";
        } else if ((month == 7) && day == 7) {
            message = "七夕";
        } else if (((month) == 8) && day == 15) {
            message = "中秋";
        } else if ((month == 9) && day == 9) {
            message = "重阳";
        } else if ((month == 12) && day == 8) {
            message = "腊八";
        } else {
            if (month == 12) {
                if ((((monthDays(year, month) == 29) && day == 29)) || ((((monthDays(year, month) == 30) && day == 30)))) {
                    message = "除夕";
                }
            }
        }
        return message;
    }
    public boolean isNullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
    
}
