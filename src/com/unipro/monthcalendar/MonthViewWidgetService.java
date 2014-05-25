package com.unipro.monthcalendar;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import com.unipro.monthcalendar.CalendarUtil;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.text.format.Time;
import android.util.Log;

public class MonthViewWidgetService extends IntentService {
	private static final String TAG = "MonthViewWidgetService";

	public MonthViewWidgetService() {
		super("MonthViewWidgetService");
		android.util.Log.d("abc","MonthViewWidgetService");
	}

    @Override
	protected void onHandleIntent(Intent intent) {
    	android.util.Log.d("abc","onHandleIntent");
    	updateMonthViewWidget(this);
	}
    
	public static  void updateMonthViewWidget(Context context){
		android.util.Log.d("abc","updateMonthViewWidget");
		final int dayOfWeek; 
		final int daysOfMonth;
		final int startofweek = Calendar.SUNDAY;
		final int[] wk_id = {R.id.day1,R.id.day2,R.id.day3,R.id.day4,R.id.day5,R.id.day6,R.id.day7};
		final int[] weeks = {R.id.date11, R.id.date12, R.id.date13, R.id.date14, R.id.date15, R.id.date16, R.id.date17,
			   				      R.id.date21, R.id.date22, R.id.date23, R.id.date24, R.id.date25, R.id.date26, R.id.date27,
			   					  R.id.date31, R.id.date32, R.id.date33, R.id.date34, R.id.date35, R.id.date36, R.id.date37,
			   					  R.id.date41, R.id.date42, R.id.date43, R.id.date44, R.id.date45, R.id.date46, R.id.date47,
			   					  R.id.date51, R.id.date52, R.id.date53, R.id.date54, R.id.date55, R.id.date56, R.id.date57,
			   					  R.id.date61, R.id.date62, R.id.date63, R.id.date64, R.id.date65, R.id.date66, R.id.date67};
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.month_widget);
    	Calendar cal = Calendar.getInstance();
    	
    	Time tm = new Time();
    	tm.setToNow();
    	
    	cal.set(tm.year, tm.month, 1);
    	dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
    	daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    	
    	//reset all TextView
    	for(int step = 0; step < weeks.length; step++){
    		views.setTextViewText(weeks[step], "");
    	}
    	
    	//set date & time on title
        views.setTextViewText(R.id.date, tm.year + " - " + String.format("%02d", tm.month +1) + " (" +  new CalendarUtil(cal).getMonth() + ")");
        String time = String.format("%02d", tm.hour) + ":" + String.format("%02d", tm.minute);// + ":" + String.format("%02d", tm.second);
        views.setTextViewText(R.id.time, time);

        // header
        ShowWeekHeader(wk_id, startofweek, views);
        
        //weeks
        ShowWeeks(daysOfMonth, weeks, startofweek, views, cal, tm);
        
		Intent intent = new Intent();
		
		intent.setComponent(new ComponentName("com.android.calendar","com.android.calendar.AllInOneActivity"));
		intent.setAction("android.intent.action.VIEW");
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		views.setOnClickPendingIntent(R.id.date, pendingIntent);
		for(int index = 0; index < daysOfMonth; index++){
			views.setOnClickPendingIntent(weeks[index+dayOfWeek], pendingIntent);
		}
		
		Intent intent_tm = new Intent();
		intent_tm.setAction("android.intent.action.SET_ALARM");
		pendingIntent = PendingIntent.getActivity(context, 0, intent_tm, 0);
		views.setOnClickPendingIntent(R.id.time, pendingIntent);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName componentName = new ComponentName(context, MonthViewWidget.class);
		appWidgetManager.updateAppWidget(componentName, views);

	}
	
/**
 * 计算一个月中第一周的第一天和一个星期开始的第一天的偏移
 * @param startwk  first day of week
 * @param firstwk first day of month 's weekday
 * */
	private static  int CalShiftofFirstWeek(int startwk, int firstwk){
		int gap = startwk - firstwk;
		return gap <= 0 ? gap : gap -7 ; 
	}
	
	private static void ShowWeekHeader(int[] wk_id, int startwk,RemoteViews views ){
		int sft;
	    DateFormatSymbols dfs = DateFormatSymbols.getInstance();
	    String[] weekdays = dfs.getShortWeekdays();
	    
	    for (int day = 0; day < Calendar.SATURDAY; day++) {
	    	sft = startwk+day <= Calendar.SATURDAY ? startwk+day :  startwk+day  - Calendar.SATURDAY;
			views.setTextViewText(wk_id[day],weekdays[sft]);
		}
	}
	private static  void ShowWeeks(int daysOfMonth, int[] weeks, int startofweek, RemoteViews views, Calendar cal, Time tm){
		int shift = CalShiftofFirstWeek(startofweek,cal.get(Calendar.DAY_OF_WEEK));
		int sft = Calendar.SATURDAY - startofweek ;
		
		cal.add(Calendar.DAY_OF_MONTH,shift);
		
		for(int step = 0; step < weeks.length; step++){
			if(cal.get(Calendar.DAY_OF_MONTH)  == tm.monthDay){
				views.setTextColor(weeks[step],Color.GREEN);
			}else if(step >= Math.abs( shift)  && step < daysOfMonth + Math.abs(shift)){
				 if(Math.abs(step - sft)%7 == 0 || Math.abs(step - (sft+1))%7 == 0){
						views.setTextColor(weeks[step],0xffff7a7a);
					}else{
						views.setTextColor(weeks[step],0xffeeeeee);
					}
			}else{
				views.setTextColor(weeks[step],0xaa888888);
			}
			views.setTextViewText(weeks[step], cal.get(Calendar.DAY_OF_MONTH)+ "\n" + new CalendarUtil(cal).toString());
			
			cal.add(Calendar.DAY_OF_MONTH, 1);//农历设置之后再加
		}
	}
}
