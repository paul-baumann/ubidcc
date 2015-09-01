package de.tud.wsn.locator.database;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;
import de.darmstadt.tu.wsn.locator.R;
import edu.mit.media.funf.storage.HttpArchive;

public class UploadTask extends AsyncTask<Object, Void, String> {

	private ProgressDialog dialog;
	private Context context;
	private String backup;


	public UploadTask(Activity activity) {

		context = activity.getApplicationContext();
		dialog = new ProgressDialog(activity);
	}


	public UploadTask(Context context) {

		this.context = context;
	}


	@Override
	protected void onPreExecute() {

		if (dialog != null) {
			dialog.setMessage("Backup in progress");
			dialog.show();
		}
	}


	@Override
	protected String doInBackground(Object... params) {

		String backupDBPath = (String) params[0];
		context = (Context) params[1];
		File sd = Environment.getExternalStorageDirectory();
		File backupDB = new File(sd, backupDBPath);
		SQLiteDatabase checkDB = SQLiteDatabase.openOrCreateDatabase(backupDB, null);
		checkDB.close();
		backup = "";

		if (HttpArchive.uploadFile(backupDB, context.getString(R.string.urlUpload))) {
			backup = "Backup successful!";
		} else {
			backup = "Backup failed!";
		}

		return backup;
	}


	@Override
	protected void onProgressUpdate(Void... values) {

		super.onProgressUpdate(values);
	}


	@Override
	protected void onPostExecute(String result) {

		if (dialog != null) {
			dialog.dismiss();
			Toast.makeText(context, backup, Toast.LENGTH_LONG).show();
		}
		super.onPostExecute(result);
	}

}
