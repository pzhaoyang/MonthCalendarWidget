package com.unipro.monthcalendar;

import java.util.Calendar;

import com.unipro.monthcalendar.CalendarUtil;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.text.format.Time;
import android.util.Log;

public class MonthViewWidgetService extends Service {
	private static final String TAG = "MonthViewWidgetService";

    private int[] wk_id = {R.id.day1,R.id.day2,R.id.day3,R.id.day4,R.id.day5,R.id.day6,R.id.day7};
    private String[] wk_label = {"周日","周一","周二","周三","周四","周五","周六"};
    private int[] weeks = {R.id.date11, R.id.date12, R.id.date13, R.id.date14, R.id.date15, R.id.date16, R.id.date17,
		   				      R.id.date21, R.id.date22, R.id.date23, R.id.date24, R.id.date25, R.id.date26, R.id.date27,
		   					  R.id.date31, R.id.date32, R.id.date33, R.id.date34, R.id.date35, R.id.date36, R.id.date37,
		   					  R.id.date41, R.id.date42, R.id.date43, R.id.date44, R.id.date45, R.id.date46, R.id.date47,
		   					  R.id.date51, R.id.date52, R.id.date53, R.id.date54, R.id.date55, R.id.date56, R.id.date57,
		   					  R.id.date61, R.id.date62, R.id.date63, R.id.date64, R.id.date65, R.id.date66, R.id.date67};

    private int dayOfWeek; 
    private int daysOfMonth;
    
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override  
        public void onReceive(Context context, Intent intent) {  
        	Log.d(TAG,"onReceive:"+intent.getAction());
        	updateMonthViewWidget();
        }
    };
    
	private void updateMonthViewWidget(){
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.month_widget);
		
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
		views.setTextViewText(R.id.time, String.format("%02d", tm.hour) + ":" + String.format("%02d", tm.minute) + ":" + String.format("%02d", tm.second));
		for(int i=0; i<7; i++){
			views.setTextViewText(wk_id[i],wk_label[i]);
		}
		
		//星期日开始第一天
    	for(int step = 0; step < daysOfMonth; step++){
    		if(step+1 == tm.monthDay){
    			views.setTextColor(weeks[step+dayOfWeek],Color.GREEN);
    		}else{
    			views.setTextColor(weeks[step+dayOfWeek],0xffb7bab1);
    		}
    		views.setTextViewText(weeks[step+dayOfWeek], new Integer(step+1).toString() + "\n" + new CalendarUtil(cal).toString());
    		cal.add(Calendar.DAY_OF_MONTH, 1);//农历设置之后再加
    		
//    		views.setTextViewText(weeks[step+dayOfWeek], new CalendarUtil(cal).toString());
//    		cal.add(Calendar.DAY_OF_MONTH, 1);//农历设置之后再加
    	}
    	
		Intent intent = new Intent();
		
		intent.setComponent(new ComponentName("com.android.calendar","com.android.calendar.AllInOneActivity"));
		intent.setAction("android.intent.action.VIEW");
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		
		views.setOnClickPendingIntent(R.id.date, pendingIntent);
		for(int index = 0; index < daysOfMonth; index++){
			views.setOnClickPendingIntent(weeks[index+dayOfWeek], pendingIntent);
		}
		
		Intent intent_tm = new Intent();
		intent_tm.setAction("android.intent.action.SET_ALARM");
		pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent_tm, 0);
		views.setOnClickPendingIntent(R.id.time, pendingIntent);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		ComponentName componentName = new ComponentName(getApplicationContext(), MonthViewWidget.class);
		appWidgetManager.updateAppWidget(componentName, views);
			
	}
    
    
	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(Intent.ACTION_TIME_TICK);
		ifilter.addAction(Intent.ACTION_TIME_CHANGED);
		ifilter.addAction(Intent.ACTION_DATE_CHANGED);
		ifilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		ifilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // [UNIPRO-pengzhaoyang-2014-5-4] for bug2991 {		
		ifilter.addAction("com.unipro.monthcalendar.action.update");
	    // [UNIPRO-pengzhaoyang-2014-5-4] for bug2991 }
		registerReceiver(myReceiver, ifilter);
		onStartSecondUpdate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG,"onBind");
		return null;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(myReceiver);	
		Log.d(TAG,"onDestroy");		
		super.onDestroy();
	}
	
	private void onStartSecondUpdate(){
		Intent intent_am = new Intent("com.unipro.monthcalendar.action.update");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,intent_am, 0);

		AlarmManager aAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		aAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, pendingIntent);
	}
}
