package de.tud.wsn.locator.database;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.alarm.AlarmManagerBroadcastReceiver;
import de.tud.wsn.locator.util.NotificationHelper;
import de.tud.wsn.locator.util.ServiceHelper;
import edu.mit.media.funf.FunfManager;

public class MyService extends Service {

	private static final String TAG = "MyService";
	NotificationManager mNotificationManager;
	SharedPreferences preferences;


	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}


	@Override
	public void onCreate() {

		LocalBroadcastManager.getInstance(this).unregisterReceiver(BroadcastReceiverSensor.getInstance());
		LocalBroadcastManager.getInstance(this).registerReceiver(BroadcastReceiverSensor.getInstance(), new IntentFilter("pipeline"));
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (ServiceHelper.shouldServiceBeStarted(this)) {
			startDCCService(this);
		}

		AlarmManagerBroadcastReceiver.SetAlarmService(this);

	}


	@Override
	public void onDestroy() {

		Log.w(TAG, "onDestroy");

		if (preferences.getBoolean(getString(R.string.data_collection_key), false)) {
			AlarmManagerBroadcastReceiver.SetAlarmService(this);
			NotificationHelper.showRecordingNotification(this, "but in standby");
		} else {
			NotificationHelper.stopRecordingNotification(this, "");
		}

		stopDCCService(this);
		// NotificationHelper.stopRecordingNotification(this);

	}


	@Override
	public void onStart(Intent intent, int startid) {

		Log.w(TAG, "onStart");

	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_STICKY;
	}

	public static final String PIPELINE_NAME = "default";
	private FunfManager funfManager;


	private void startDCCService(Context context) {

		NotificationHelper.showRecordingNotification(context, "");
		// Bind to the service, to create the connection with FunfManager
		try {
			context.bindService(new Intent(context, FunfManager.class), funfManagerConn, Context.BIND_AUTO_CREATE);
		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage());
		}
	}


	private void stopDCCService(Context context) {

		try {
			if (funfManager != null) {
				funfManager.disablePipeline(PIPELINE_NAME);
				if (ServiceHelper.isMyServiceRunning(context)) {
					Intent stopService = new Intent(context, MyService.class);
					context.stopService(stopService);
				}
			}
			unbindService(funfManagerConn);
		} catch (Exception e) {
			Log.e("MainActivity", e.getMessage());
		}
	}

	private ServiceConnection funfManagerConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			funfManager = ((FunfManager.LocalBinder) service).getManager();
			funfManager.enablePipeline(PIPELINE_NAME);
			Log.i("MyService", "Funf Service connected.");
		}


		@Override
		public void onServiceDisconnected(ComponentName name) {

			funfManager = null;
		}
	};
}