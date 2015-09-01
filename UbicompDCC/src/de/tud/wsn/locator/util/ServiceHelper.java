package de.tud.wsn.locator.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.database.MyService;
import de.tud.wsn.locator.database.UploadUserDataService;

public class ServiceHelper {

	private static SharedPreferences preferences;


	public static boolean isMyServiceRunning(Context context) {

		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (MyService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}


	public static boolean shouldServiceBeStarted(Context context) {

		return isDataCollectionActivated(context) && isTimeDuringCollectionDay(context);
	}


	public static boolean isDataCollectionActivated(Context context) {

		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean activated = preferences.getBoolean(context.getString(R.string.data_collection_key), false);
		return activated;
	}


	public static boolean isConferenceFinished(Context context) {

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
		boolean finished = cal.after(getLastDay());
		if (finished) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			boolean questionnaire = preferences.getBoolean(context.getString(R.string.settings_inital_survey_uploaded), false);
			boolean registration = preferences.getBoolean(context.getString(R.string.settings_registration_for_data_uploaded), false);
			if (questionnaire && registration)
				NotificationHelper.conferenceEndedNotification(context, context.getString(R.string.notification_allUploaded));
			else
				NotificationHelper.conferenceEndedNotification(context, context.getString(R.string.notification_checkRegistration));
		}
		return finished;
	}


	public static boolean isTimeDuringCollectionDay(Context context) {

		// SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss z");

		// sdf.format(cal.getTime())
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));

		boolean during = cal.getTimeInMillis() >= getFirstDay().getTimeInMillis() && cal.getTimeInMillis() >= getStartOfADay().getTimeInMillis()
				&& cal.getTimeInMillis() < getEndOfADay().getTimeInMillis() && cal.getTimeInMillis() < getLastDay().getTimeInMillis();

		if (isConferenceFinished(context))
			return false;
		else
			return during;

	}


	private static Calendar getStartOfADay() {

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
		cal.set(Calendar.HOUR_OF_DAY, 8);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.getTime();
		return cal;
	}


	private static Calendar getEndOfADay() {

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
		cal.set(Calendar.HOUR_OF_DAY, 18);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.getTime();
		return cal;
	}


	private static Calendar getFirstDay() {

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
		cal.set(Calendar.YEAR, 2013);
		cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 7); // FIXME
		cal.set(Calendar.HOUR_OF_DAY, 8);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.getTime();
		return cal;
	}


	private static Calendar getLastDay() {

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2"));
		cal.set(Calendar.YEAR, 2013);
		cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 12);
		cal.set(Calendar.HOUR_OF_DAY, 18);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.getTime();
		return cal;
	}


	public static boolean isUploadUserDataServiceRunning(Context context) {

		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (UploadUserDataService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}


	public static void startQuestionnaireUpload(Context context) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(context, UploadUserDataService.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				Long.valueOf(context.getString(R.string.uploadQuestionnaireInterval)), pintent);
	}

}