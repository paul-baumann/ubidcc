package de.tud.wsn.locator.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.MainActivity;

public class NotificationHelper {

	static NotificationManager mNotificationManager;
	static int i = 0;


	public static void showRegistrationIsUnlockedNotification(Context context) {

		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle("UbiComp 2013").setContentText(context.getString(R.string.registrationAchievementUnlocked))
				.setSmallIcon(R.drawable.locator).setContentIntent(pIntent);

		Notification notification = builder.getNotification();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(2, notification);
	}


	public static void showRecordingNotification(Context context, String additional) {

		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0); // PendingIntent.FLAG_UPDATE_CURRENT
																					// |
																					// PendingIntent.FLAG_ONE_SHOT

		Notification not = null;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle("UbiComp 2013").setContentText("Data collection running " + additional).setSmallIcon(R.drawable.locator)
				.setContentIntent(pIntent);

		not = builder.getNotification();

		not.flags |= Notification.FLAG_ONGOING_EVENT;

		mNotificationManager.cancelAll();
		if (!ServiceHelper.isConferenceFinished(context))
			mNotificationManager.notify(1, not);
	}


	public static void stopRecordingNotification(Context context, String additional) {

		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0); // PendingIntent.FLAG_UPDATE_CURRENT
																					// |
																					// PendingIntent.FLAG_ONE_SHOT

		Notification not = null;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle("UbiComp 2013").setContentText("Data collection stopped " + additional).setSmallIcon(R.drawable.locator)
				.setContentIntent(pIntent);

		not = builder.getNotification();

		not.flags |= Notification.FLAG_ONGOING_EVENT;

		mNotificationManager.cancelAll();
		if (!ServiceHelper.isConferenceFinished(context))
			mNotificationManager.notify(1, not);

	}


	public static void conferenceEndedNotification(Context context, String text) {

		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

		Notification not = null;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle("UbiComp 2013 finished").setContentText(text).setSmallIcon(R.drawable.locator).setContentIntent(pIntent);

		not = builder.getNotification();

		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Hide the notification after its selected
		not.flags = Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.cancelAll();
		mNotificationManager.notify(0, not);
	}

}
