package de.tud.wsn.locator.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.tud.wsn.locator.MainActivity;
import de.tud.wsn.locator.alarm.AlarmManagerBroadcastReceiver;
import de.tud.wsn.locator.util.NotificationHelper;
import de.tud.wsn.locator.util.ServiceHelper;

public class BroadcastReceiverSensor extends BroadcastReceiver {

	private static BroadcastReceiverSensor broadcastReceiverSensor;
	private SharedPreferences preferences;

	public static BroadcastReceiverSensor getInstance() {

		if (broadcastReceiverSensor == null)
			broadcastReceiverSensor = new BroadcastReceiverSensor();
		return broadcastReceiverSensor;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			preferences = PreferenceManager.getDefaultSharedPreferences(context);
			Log.i("MyService", "Register service after boot?");
			if (ServiceHelper.shouldServiceBeStarted(context)) {
				Intent startService = new Intent(context, MyService.class);
				context.startService(startService);
				Log.w("MyService", "Service after boot registered");
			} else if (ServiceHelper.isDataCollectionActivated(context)) {
				NotificationHelper.showRecordingNotification(context, "but in standby");
				AlarmManagerBroadcastReceiver.SetAlarmService(context);
			} else {
				NotificationHelper.stopRecordingNotification(context, "");
			}
			MainActivity.startUploadServices(preferences, context);
		}
	}

}
