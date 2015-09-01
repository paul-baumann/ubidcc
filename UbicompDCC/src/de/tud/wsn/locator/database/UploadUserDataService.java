package de.tud.wsn.locator.database;

import java.util.HashMap;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import de.darmstadt.tu.wsn.locator.R;
import edu.mit.media.funf.storage.HttpArchive;
import edu.mit.media.funf.storage.UserDatabaseHelper;

public class UploadUserDataService extends Service {

	private static final String TAG = "UploadUserDataService";
	private SharedPreferences preferences;


	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}


	@Override
	public void onStart(Intent intent, int startid) {

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		new UploadUserDataServiceTask().execute();
	}

	private class UploadUserDataServiceTask extends AsyncTask<Object, Void, Void> {

		@Override
		protected synchronized Void doInBackground(Object... params) {

			Log.d(TAG, "Starting to upload user's questionnaire");

			String android_id = Secure.getString(getApplication().getContentResolver(), Secure.ANDROID_ID);
			// create and execute sql query
			UserDatabaseHelper helper = new UserDatabaseHelper(getApplicationContext());
			SQLiteDatabase database = helper.getWritableDatabase();

			String where = UserDatabaseHelper.TABLE_QUESTIONNAIRE_DEVICEUID + " = ?";
			String[] whereArgs = { android_id };
			String[] columns = { UserDatabaseHelper.TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY, UserDatabaseHelper.TABLE_QUESTIONNAIRE_AGE,
					UserDatabaseHelper.TABLE_QUESTIONNAIRE_GENDER, UserDatabaseHelper.TABLE_QUESTIONNAIRE_ROLE,
					UserDatabaseHelper.TABLE_QUESTIONNAIRE_MACADDRESS, UserDatabaseHelper.TABLE_QUESTIONNAIRE_BLUETOOTHADDRESS };
			Cursor cursor = database.query(UserDatabaseHelper.TABLE_QUESTIONNAIRE, columns, where, whereArgs, null, null, null);

			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_DEVICEUID, android_id);
			cursor.moveToLast();
			if (cursor.getCount() > 0) {
				for (int index = 0; index < columns.length; index++) {
					String currentParameter = columns[index];
					paramsMap.put(currentParameter, cursor.getString(index));
				}
			}

			database.close();

			boolean isSuccessful = (!preferences.getBoolean(getString(R.string.settings_inital_survey_uploaded), false)) ? HttpArchive.sendPOST(
					getApplication().getString(R.string.urlAddUser), paramsMap) : true;

			if (isSuccessful) {
				Log.d(TAG, "Upload of user's questionnaire was successful");

				Editor editor = preferences.edit();
				editor.putBoolean(getString(R.string.settings_inital_survey_uploaded), true);
				editor.commit();

				Intent intent = new Intent(getApplicationContext(), UploadUserDataService.class);
				PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
				AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
				alarm.cancel(pintent);

				onDestroy();
			} else
				Log.d(TAG, "Upload of user's questionnaire has failed");
			return null;
		}
	}
}
