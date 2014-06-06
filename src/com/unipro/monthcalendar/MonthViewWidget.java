package com.unipro.monthcalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class MonthViewWidget extends AppWidgetProvider{
	
	private Context widgetcontext;
		@Override
		public void onUpdate(Context context,  AppWidgetManager appWidgetManager, int[] appWidgetIds) {
			initService(context);
		}

		@Override
		public void onDeleted(Context context, int[] appWidgetIds) {
			super.onDeleted(context, appWidgetIds);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			try{
				String action = intent.getAction();
				if (action.equals(Intent.ACTION_TIME_TICK)
						|| action.equals(Intent.ACTION_TIME_CHANGED)
						|| action.equals(Intent.ACTION_DATE_CHANGED)
						|| action.equals(Intent.ACTION_TIMEZONE_CHANGED)
						|| action.equals(Intent.ACTION_LOCALE_CHANGED)
						) {
					MonthViewWidgetService.updateMonthViewWidget(context);
				}else{
					   super.onReceive(context, intent);
					   return;
				}
			}catch(NullPointerException e){
				initService(context); 
			}
		}
		
		private void initService(Context context) {
			Intent intent = new Intent(context, MonthViewWidgetService.class);
			context.startService(intent);
		}

		@Override
		public void onDisabled(Context context) {
			if (widgetcontext != null) {
				widgetcontext.unregisterReceiver(this);
			}
		}

		@Override
		public void onEnabled(Context context) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_DATE_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			filter.addAction(Intent.ACTION_LOCALE_CHANGED);
			widgetcontext = context.getApplicationContext();
			widgetcontext.registerReceiver(this, filter);
		}		
}
