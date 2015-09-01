package de.tud.wsn.locator.database;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import de.darmstadt.tu.wsn.locator.R;
import edu.mit.media.funf.storage.UserDatabaseHelper;

public class UploadUserDataTask extends AsyncTask<Object, Void, Void> {

	private ProgressDialog dialog;
	private Context context;


	public UploadUserDataTask(Activity activity) {

		context = activity.getApplicationContext();
		dialog = new ProgressDialog(activity);
	}


	public UploadUserDataTask(Context context) {

		this.context = context;
	}


	@Override
	protected void onPreExecute() {

		if (dialog != null) {
			dialog.setMessage("Backup in progress");
			dialog.setCancelable(true);
			dialog.show();
		}
	}


	@Override
	protected Void doInBackground(Object... params) {

		ContentValues contentValues = (ContentValues) params[0];
		UserDatabaseHelper helper = new UserDatabaseHelper(context);
		SQLiteDatabase database = helper.getWritableDatabase();
		database.insert(UserDatabaseHelper.TABLE_QUESTIONNAIRE, null, contentValues);
		database.close();

		return null;
	}


	@Override
	protected void onPostExecute(Void voids) {

		if (dialog != null) {
			dialog.dismiss();
		}
		startUpload();
	}


	private void startUpload() {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(context, UploadUserDataService.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				Long.valueOf(context.getString(R.string.uploadQuestionnaireInterval)), pintent);
	}

}
