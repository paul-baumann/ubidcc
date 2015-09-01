package de.tud.wsn.locator.funf;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonObject;

import de.darmstadt.tu.wsn.locator.R;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.probe.Probe.Base;
import edu.mit.media.funf.probe.Probe.PassiveProbe;
import edu.mit.media.funf.time.TimeUtil;
import edu.mit.media.funf.util.LogUtil;

@Schedule.DefaultSchedule(interval = 20)
public class HelperProbe extends Base implements PassiveProbe {

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault());
	private SharedPreferences preferences;


	@Override
	protected void onStart() {

		super.onStart();
		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		Calendar cal = Calendar.getInstance();
		long time = cal.getTimeInMillis();

		TimeUtil.alignedTimestamp = time;
		Log.d("Database", "Alignment: " + dateFormat.format(cal.getTime()));

		ConnectivityManager conMan = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo.State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		Log.d(LogUtil.TAG, "HelperProbe sending log information.");
		JsonObject data = new JsonObject();
		data.addProperty("wifi", wifi.equals(android.net.NetworkInfo.State.CONNECTED));
		data.addProperty("mobile", ""); // mobile.equals(android.net.NetworkInfo.State.CONNECTED));
		data.addProperty("bluetooth", mBluetoothAdapter.isEnabled());
		data.remove(PROBE); // Remove probe so that it fills with our probe
		sendData(data);

		updateDCCProgress();
		stop();
	}


	private void updateDCCProgress() {

		int minNumberOfScans = preferences.getInt(getContext().getString(R.string.settings_registration_required_number_of_scans), 0);

		int numberOfScans = preferences.getInt(getContext().getString(R.string.settings_registration_number_of_scans), 0);
		numberOfScans++;
		Editor editor = preferences.edit();
		editor.putInt(getContext().getString(R.string.settings_registration_number_of_scans), numberOfScans);

		if (numberOfScans == minNumberOfScans) {
			editor.putBoolean(getContext().getString(R.string.settings_registration_unlocked), true);
		}
		editor.commit();
	}


	@Override
	protected void onStop() {

		super.onStop();
	}

}
