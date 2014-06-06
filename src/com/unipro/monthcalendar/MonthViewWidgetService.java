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
import android.widget.RemoteViews;
import android.text.format.Time;

public class MonthViewWidgetService extends IntentService {
	private static int CurrentMonth; 
	public MonthViewWidgetService() {
		super("MonthViewWidgetService");
	}

    @Override
	protected void onHandleIntent(Intent intent) {
    	updateMonthViewWidget(this);
	}
    
	public static  void updateMonthViewWidget(Context context){
		final int daysOfMonth;
		final int startofweek = Calendar.SUNDAY;
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.month_widget);
    	Calendar cal = Calendar.getInstance();
    	
    	Time tm = new Time();
    	tm.setToNow();
    	
    	cal.set(tm.year, tm.month, 1);
    	daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		CurrentMonth = cal.get(Calendar.MONTH);
    	
    	
    	//set date & time on title
        views.setTextViewText(R.id.date, tm.year + " - " + String.format("%02d", tm.month +1) + " (" +  new CalendarUtil(cal).getMonth() + ")");
        String time = String.format("%02d", tm.hour) + ":" + String.format("%02d", tm.minute);// + ":" + String.format("%02d", tm.second);
        views.setTextViewText(R.id.time, time);

        // header
        ShowWeekHeader(context, startofweek, views);
        
        //weeks
        ShowWeeks(context, daysOfMonth, startofweek, views, cal, tm);
        
        SetPendingInteng(context, "com.android.calendar", "com.android.calendar.AllInOneActivity", "android.intent.action.VIEW", views, R.id.date);
        SetPendingInteng(context, null, null, "android.intent.action.SET_ALARM", views, R.id.time);
		
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
	
	private static void ShowWeekHeader(Context context, int startwk,RemoteViews views ){
		int sft;
	    DateFormatSymbols dfs = DateFormatSymbols.getInstance();
	    String[] weekdays = dfs.getShortWeekdays();
	    
	    views.removeAllViews(R.id.hearder_container);
	    
	    for (int day = 0; day < Calendar.SATURDAY; day++) {
	    	RemoteViews cell_hdr = new RemoteViews(context.getPackageName(), R.layout.cell_header);
	    	sft = startwk+day <= Calendar.SATURDAY ? startwk+day :  startwk+day  - Calendar.SATURDAY;
	    	cell_hdr.setTextViewText(R.id.text0, weekdays[sft]);
	    	views.addView(R.id.hearder_container, cell_hdr);
		}
	}
	private static  void ShowWeeks(Context context, int daysOfMonth, int startofweek, RemoteViews views, Calendar cal, Time tm){
		int shift = CalShiftofFirstWeek(startofweek,cal.get(Calendar.DAY_OF_WEEK));
		int totalline = Math.abs(shift) + daysOfMonth > 35 ? 6 : Math.abs(shift) + daysOfMonth == 28 ? 4 : 5;
		cal.add(Calendar.DAY_OF_MONTH,shift);
		
        views.removeAllViews(R.id.calendar_container);
		for(int week = 0; week < totalline; week++){
			
			RemoteViews weekline = new RemoteViews(context.getPackageName(), R.layout.week_line);
	        for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
	        	int res_id = R.layout.out_day;
	        	int lunar_res_id = R.layout.lunar_out_day;
	        	int cell_id = R.layout.cell_out_day;
	        	if( (cal.get(Calendar.DAY_OF_MONTH)  == tm.monthDay)
	        			&&  (cal.get(Calendar.MONTH) == CurrentMonth)){
	        		res_id = R.layout.today;
	        		lunar_res_id = R.layout.lunar_today;
	        		cell_id = R.layout.cell_today;
	        	}else if(cal.get(Calendar.MONTH) == CurrentMonth){
	        		if(cal.get(Calendar.DAY_OF_WEEK)%7 == 1 || cal.get(Calendar.DAY_OF_WEEK)%7 == 0){
	        			cell_id = R.layout.cell_weekend;
	        		}else{
	        			cell_id = R.layout.cell_day;
	        		}
	        		res_id = R.layout.normal_day;
	        		if( CalendarUtil.IsFestival(new CalendarUtil(cal).toString()) ){
	        			lunar_res_id = R.layout.lunar_festival;
	        		}else{
	        			lunar_res_id = R.layout.lunar_normal;
	        		}
	        	}
	            RemoteViews daycell = new RemoteViews(context.getPackageName(), res_id);
	            RemoteViews cell_day = new RemoteViews(context.getPackageName(), cell_id);
	            daycell.setTextViewText(R.id.text1, ""+cal.get(Calendar.DAY_OF_MONTH));
	            cell_day.addView(R.id.cell_container, daycell);
	            
	            RemoteViews lunarcell = new RemoteViews(context.getPackageName(), lunar_res_id);
	            lunarcell.setTextViewText(R.id.text2, new CalendarUtil(cal).toString());
                cell_day.addView(R.id.cell_container, lunarcell);
	            
	            weekline.addView(R.id.week_container, cell_day);

	            if(cal.get(Calendar.MONTH) == CurrentMonth){
	            	SetPendingInteng(context, "com.android.calendar", "com.android.calendar.AllInOneActivity", "android.intent.action.VIEW", cell_day, R.id.text1);
	            	SetPendingInteng(context, "com.android.calendar", "com.android.calendar.AllInOneActivity", "android.intent.action.VIEW", cell_day, R.id.text2);
	            }
	            
				cal.add(Calendar.DAY_OF_MONTH, 1);
	        }
	        views.addView(R.id.calendar_container, weekline);
		}
  }
	
	private static void SetPendingInteng(Context context, String pkg, String cls,  String action, RemoteViews rv, int viewId ){
		
		    if(context == null && rv == null && viewId <= 0){
		    	return;
		    }
		    
			Intent intent = new Intent();
			if(pkg != null && cls != null){
				intent.setComponent(new ComponentName(pkg, cls));
			}
			if(action != null){
				intent.setAction(action);
			}
			
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);	            	
			rv.setOnClickPendingIntent(viewId, pendingIntent);
	}
}
