package com.unipro.monthcalendar;

import java.text.DateFormatSymbols;
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.text.format.Time;
import android.util.Log;

public class MonthViewWidgetService extends Service {
	private static final String TAG = "MonthViewWidgetService";

    private int[] wk_id = {R.id.day1,R.id.day2,R.id.day3,R.id.day4,R.id.day5,R.id.day6,R.id.day7};
    private int[] weeks = {R.id.date11, R.id.date12, R.id.date13, R.id.date14, R.id.date15, R.id.date16, R.id.date17,
		   				      R.id.date21, R.id.date22, R.id.date23, R.id.date24, R.id.date25, R.id.date26, R.id.date27,
		   					  R.id.date31, R.id.date32, R.id.date33, R.id.date34, R.id.date35, R.id.date36, R.id.date37,
		   					  R.id.date41, R.id.date42, R.id.date43, R.id.date44, R.id.date45, R.id.date46, R.id.date47,
		   					  R.id.date51, R.id.date52, R.id.date53, R.id.date54, R.id.date55, R.id.date56, R.id.date57,
		   					  R.id.date61, R.id.date62, R.id.date63, R.id.date64, R.id.date65, R.id.date66, R.id.date67};

    private int dayOfWeek; 
    private int daysOfMonth;
    private int startofweek = Calendar.SUNDAY;
    
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override  
        public void onReceive(Context context, Intent intent) {  
        	Log.d(TAG,"onReceive:"+intent.getAction());
        	updateMonthViewWidget();
        }
    };
    
	@SuppressWarnings({ "deprecation" })
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
        String time = String.format("%02d", tm.hour) + ":" + String.format("%02d", tm.minute) + ":" + String.format("%02d", tm.second);
        views.setImageViewBitmap(R.id.time, buildUpdate(time));

        // header
        ShowWeekHeader(startofweek, views);
        
        //weeks
        ShowWeeks(views,cal,tm);
        
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
		//ifilter.addAction(Intent.ACTION_TIME_TICK);
		ifilter.addAction(Intent.ACTION_TIME_CHANGED);
		ifilter.addAction(Intent.ACTION_DATE_CHANGED);
		ifilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		ifilter.addAction("com.unipro.monthcalendar.action.update");
		registerReceiver(myReceiver, ifilter);
		//onStartSecondUpdate();
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
		aAlarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000, pendingIntent);
	}

	public Bitmap buildUpdate(String time){
		Bitmap myBitmap = Bitmap.createBitmap(150, 40, Bitmap.Config.ARGB_8888);
		Canvas myCanvas = new Canvas(myBitmap);
		Paint paint = new Paint();
		Typeface clock = Typeface.createFromAsset(this.getAssets(),"fonts/digi.ttf");

		paint.setStyle(Paint.Style.FILL);
		paint.setTypeface(clock);
		paint.setColor(0xffc5e880);
		paint.setTextSize(42);
		paint.setTextAlign(Align.CENTER);
		myCanvas.drawText(time, 75, 40, paint);

		return myBitmap;
    }
/**
 * 计算一个月中第一周的第一天和一个星期开始的第一天的偏移
 * @param startwk  first day of week
 * @param firstwk first day of month 's weekday
 * */
	private int CalShiftofFirstWeek(int startwk, int firstwk){
		int gap = startwk - firstwk;
		return gap <= 0 ? gap : gap -7 ; 
	}
	
	private void ShowWeekHeader(int startwk,RemoteViews views ){
		int sft;
	    DateFormatSymbols dfs = DateFormatSymbols.getInstance();
	    String[] weekdays = dfs.getShortWeekdays();
	    
	    for (int day = 0; day < Calendar.SATURDAY; day++) {
	    	sft = startwk+day <= Calendar.SATURDAY ? startwk+day :  startwk+day  - Calendar.SATURDAY;
			views.setTextViewText(wk_id[day],weekdays[sft]);
		}
	}
	private void ShowWeeks(RemoteViews views, Calendar cal, Time tm){
		int shift = CalShiftofFirstWeek(startofweek,cal.get(Calendar.DAY_OF_WEEK));
		int sft = Calendar.SATURDAY - startofweek ;
		
		cal.add(Calendar.DAY_OF_MONTH,shift);
		
		for(int step = 0; step < weeks.length; step++){
			if(cal.get(Calendar.DAY_OF_MONTH)  == tm.monthDay){
				views.setTextColor(weeks[step],Color.GREEN);
			}else if(Math.abs(step - sft)%7 == 0 || Math.abs(step - (sft+1))%7 == 0){
				views.setTextColor(weeks[step],0xffff7a7a);
			}else if(step >= Math.abs( shift)  && step < daysOfMonth + Math.abs(shift)){
				views.setTextColor(weeks[step],0xffeeeeee);
			}else{
				views.setTextColor(weeks[step],0xaa888888);
			}
			views.setTextViewText(weeks[step], cal.get(Calendar.DAY_OF_MONTH)+ "\n" + new CalendarUtil(cal).toString());
			
			cal.add(Calendar.DAY_OF_MONTH, 1);//农历设置之后再加
		}
	}
}
