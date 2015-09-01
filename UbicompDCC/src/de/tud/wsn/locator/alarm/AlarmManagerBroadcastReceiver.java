package de.tud.wsn.locator.alarm;

import java.util.Calendar;
import java.util.TimeZone;

import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.database.MyService;
import de.tud.wsn.locator.util.NotificationHelper;
import de.tud.wsn.locator.util.ServiceHelper;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
	final public static String ONE_TIME = "onetime";
	final public static String SERVICE_TIMER = "servicetimer";

	@Override
	public void onReceive(final Context context, Intent intent) {
		Bundle extras = intent.getExtras();

		if (extras != null && extras.getBoolean(SERVICE_TIMER, Boolean.TRUE)) {

			if (ServiceHelper.shouldServiceBeStarted(context)) {
				Log.i("MyService", "Alarm service regular active.");
				if (!ServiceHelper.isMyServiceRunning(context)) {
					Intent startService = new Intent(context, MyService.class);
					context.startService(startService);
				}
			}

			else {
				Log.i("MyService", "Alarm service regular passive.");
				if (ServiceHelper.isMyServiceRunning(context)) {
					Intent stopService = new Intent(context, MyService.class);
					context.stopService(stopService);
				}
				ServiceHelper.isConferenceFinished(context);
			}

		}

	}

	public static void SetAlarmService(Context context) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
		cal.set(Calendar.HOUR_OF_DAY, 8);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long interval = 60 * 60 * 1000; //Check every hour if sensing should be done

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		intent.putExtra(SERVICE_TIMER, Boolean.TRUE);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
		am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pi);
		Log.i("MyService", "Alarm service activated!");
	}

	public static void CancelAlarm(Context context) {
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

}
