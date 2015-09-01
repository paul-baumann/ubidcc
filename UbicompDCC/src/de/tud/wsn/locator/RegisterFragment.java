package de.tud.wsn.locator;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.database.UploadRegistrationService;
import de.tud.wsn.locator.util.NotificationHelper;
import edu.mit.media.funf.storage.UserDatabaseHelper;

public class RegisterFragment extends SherlockFragment implements OnSharedPreferenceChangeListener {

	private TextView informationView;
	private TextView progressStatusText;
	private EditText nameEdit;
	private EditText emailEdit;
	private EditText affiliationEdit;

	private Button registerButton;
	private SharedPreferences preferences;

	private EmailValidator emailValidator;

	private ProgressBar progressBar;
	private RelativeLayout relativeLayout;
	private LinearLayout linearLayout;

	private View view;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		preferences.registerOnSharedPreferenceChangeListener(this);
		view = inflater.inflate(R.layout.registerfragment, container, false);
		relativeLayout = (RelativeLayout) view.findViewById(R.id.progressLayout);
		linearLayout = (LinearLayout) view.findViewById(R.id.filloutlayout);

		checkRequirements();

		boolean isRegistrationUnlocked = preferences.getBoolean(getActivity().getString(R.string.settings_registration_unlocked), false);

		View view = (isRegistrationUnlocked) ? loadRegisterView() : loadProgressBarView();

		return view;
	}


	private boolean checkRequirements() {

		int numberOfScans = preferences.getInt(getString(R.string.settings_registration_number_of_scans), 0);
		int minNumberOfScans = Integer.parseInt(getActivity().getString(R.string.settings_registration_required_number_of_scans));

		if (numberOfScans >= minNumberOfScans) {
			Editor editor = preferences.edit();
			editor.putBoolean(getActivity().getString(R.string.settings_registration_unlocked), true);
			editor.commit();
			return true;
		}
		return false;
	}


	private View loadRegisterView() {

		relativeLayout.setVisibility(View.GONE);
		linearLayout.setVisibility(View.VISIBLE);
		informationView = (TextView) view.findViewById(R.id.registrationDescriptionView);
		informationView.setText(R.string.registrationDescription);

		nameEdit = (EditText) view.findViewById(R.id.nameText);
		emailEdit = (EditText) view.findViewById(R.id.emailText);
		affiliationEdit = (EditText) view.findViewById(R.id.affiliationText);

		registerButton = (Button) view.findViewById(R.id.saveUserDataButton);

		emailValidator = new EmailValidator();

		return view;
	}


	private View loadProgressBarView() {

		relativeLayout.setVisibility(View.VISIBLE);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBarForRegistration);
		progressStatusText = (TextView) view.findViewById(R.id.progressStatusNumber);

		return view;
	}


	@Override
	public void onResume() {

		super.onResume();
		if (preferences.getBoolean(getActivity().getString(R.string.settings_registration_unlocked), false))
			if (preferences.getBoolean(getString(R.string.settings_registration_for_data_completed), false))
				loadData();
			else
				setupRegistrationButton();
		else {
			updateStatus();
		}
	}


	private void updateStatus() {

		checkRequirements();

		int numberOfScans = preferences.getInt(getString(R.string.settings_registration_number_of_scans), 0);
		int minNumberOfScans = Integer.parseInt(getActivity().getString(R.string.settings_registration_required_number_of_scans));

		double progress = Math.min(((double) numberOfScans) * 100 / ((double) minNumberOfScans), 100);

		try {
			progressBar.setProgress(((int) progress));
			DecimalFormat f = new DecimalFormat("##.##");
			progressStatusText.setText(f.format(progress) + " %");
		} catch (Exception e) {

		}
	}


	private void loadData() {

		if (preferences.getBoolean(getString(R.string.settings_registration_for_data_uploaded), false))
			informationView.setText(R.string.registrationYesUploadYes);
		else
			informationView.setText(R.string.registrationYesUploadNo);

		UserDatabaseHelper helper = new UserDatabaseHelper(getActivity());
		SQLiteDatabase database = helper.getWritableDatabase();

		String[] columns = { UserDatabaseHelper.TABLE_REGISTRATION_AFFILIATION, UserDatabaseHelper.TABLE_REGISTRATION_NAME,
				UserDatabaseHelper.TABLE_REGISTRATION_EMAIL };
		Cursor cursor = database.query(UserDatabaseHelper.TABLE_REGISTRATION, columns, null, null, null, null, null);
		cursor.moveToLast();

		try {
			affiliationEdit.setText(cursor.getString(0));
			nameEdit.setText(cursor.getString(1));
			emailEdit.setText(cursor.getString(2));
		} catch (Exception e) {
			e.printStackTrace();
		}

		database.close();

		affiliationEdit.setEnabled(false);
		nameEdit.setEnabled(false);
		emailEdit.setEnabled(false);
		registerButton.setVisibility(Button.GONE);
	}


	private void setupRegistrationButton() {

		registerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (emailEdit.getText().toString() != null && emailValidator.validate(emailEdit.getText().toString())) {
					Editor editor = preferences.edit();
					editor.putBoolean(getString(R.string.settings_registration_for_data_completed), true);
					editor.commit();

					ContentValues parameters = new ContentValues();
					parameters.put(UserDatabaseHelper.TABLE_REGISTRATION_AFFILIATION, affiliationEdit.getText().toString());
					parameters.put(UserDatabaseHelper.TABLE_REGISTRATION_NAME, nameEdit.getText().toString());
					parameters.put(UserDatabaseHelper.TABLE_REGISTRATION_EMAIL, emailEdit.getText().toString());

					new RegistrationTask().execute(parameters);
				} else {
					showDialog();
				}

			}
		});
	}

	private class RegistrationTask extends AsyncTask<Object, Void, Void> {

		private ProgressDialog dialog;


		@Override
		protected void onPreExecute() {

			dialog = new ProgressDialog(getActivity());
			if (dialog != null) {
				dialog.setMessage("Backup in progress");
				dialog.setCancelable(true);
				dialog.show();
			}
		}


		@Override
		protected Void doInBackground(Object... params) {

			ContentValues contentValues = (ContentValues) params[0];
			UserDatabaseHelper helper = new UserDatabaseHelper(getActivity());
			SQLiteDatabase database = helper.getWritableDatabase();
			database.insert(UserDatabaseHelper.TABLE_REGISTRATION, null, contentValues);
			database.close();
			return null;
		}


		@Override
		protected void onPostExecute(Void voids) {

			Editor editor = preferences.edit();
			editor.putBoolean(getString(R.string.settings_registration_for_data_completed), true);
			editor.commit();

			loadData();
			if (dialog != null) {
				dialog.dismiss();
			}
			startUpload();
		}

	}

	private class EmailValidator {

		private Pattern pattern;
		private Matcher matcher;

		private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


		private EmailValidator() {

			pattern = Pattern.compile(EMAIL_PATTERN);
		}


		/**
		 * Validate hex with regular expression
		 * 
		 * @param hex
		 *            hex for validation
		 * @return true valid hex, false invalid hex
		 */
		private boolean validate(final String hex) {

			matcher = pattern.matcher(hex);
			return matcher.matches();

		}
	}


	private void showDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.dialogmessageemail).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {

			}
		});
		// Create the AlertDialog object and return it
		builder.create();
		builder.show();
	}


	private void startUpload() {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(getActivity(), UploadRegistrationService.class);
		PendingIntent pintent = PendingIntent.getService(getActivity(), 0, intent, 0);
		AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				Long.valueOf(getActivity().getString(R.string.uploadRegistrationInterval)), pintent);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if (isAdded()) {
			if (key.equals(getString(R.string.settings_registration_unlocked))) {
				relativeLayout.setVisibility(View.GONE);
				loadRegisterView();
				NotificationHelper.showRegistrationIsUnlockedNotification(getActivity());
			}
			if (key.equals(getString(R.string.settings_registration_number_of_scans))) {
				updateStatus();
			}
		}
	}

}
