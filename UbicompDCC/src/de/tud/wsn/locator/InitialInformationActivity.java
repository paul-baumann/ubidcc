package de.tud.wsn.locator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.util.ServiceHelper;
import edu.mit.media.funf.storage.UserDatabaseHelper;

/**
 * Basic contact data that is required to run the app, and the "README" with
 * terms of service, etc.
 * 
 * @author bjoern
 * 
 */
public class InitialInformationActivity extends SherlockActivity {

	public static int THEME = R.style.Sherlock___Theme_DarkActionBar;

	private EditText ageEdit;
	private RadioGroup genderRadioGroup;
	private Spinner roleSpinner;
	private RadioGroup academiaOrIndustryRadioGroup;

	private SharedPreferences preferences;
	private ImageView imageView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// requestWindowFeature((int) Window.FEATURE_NO_TITLE);
		setTheme(THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_initialinformation);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (!preferences.getBoolean(getString(R.string.settings_first_start_key), true)) {
			// setResult(RESULT_CANCELED);
			nextActivity();
			// finish();
		}

		ageEdit = (EditText) findViewById(R.id.ageEdit);
		genderRadioGroup = (RadioGroup) findViewById(R.id.genderRadioGroup);
		academiaOrIndustryRadioGroup = (RadioGroup) findViewById(R.id.academiaRadioGroup);

		roleSpinner = (Spinner) findViewById(R.id.roleSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.userRoles, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roleSpinner.setAdapter(adapter);

		imageView = (ImageView) findViewById(R.id.ubicomplogo);
		imageView.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				startImpressum();
			}
		});
		// bind buttons
		Button btnAccept = (Button) findViewById(R.id.btn_accept);
		Button notNowButton = (Button) findViewById(R.id.btn_cancel);

		btnAccept.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				ContentValues values = new ContentValues();

				String age = ageEdit.getText().toString();
				int genderSelectedID = genderRadioGroup.getCheckedRadioButtonId();
				int academiaSelectedID = academiaOrIndustryRadioGroup.getCheckedRadioButtonId();
				if (academiaSelectedID == -1 || genderSelectedID == -1 || age.length() < 1) {
					showQuestionnaireUncompleteDialog();
					return;
				}

				String android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

				RadioButton selectedButton = (RadioButton) findViewById(academiaSelectedID);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY, selectedButton.getText().toString());
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_AGE, age);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_DEVICEUID, android_id);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_MACADDRESS, getMacAddress());
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_BLUETOOTHADDRESS, getBluetoothAddress());

				selectedButton = (RadioButton) findViewById(genderSelectedID);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_GENDER, selectedButton.getText().toString());

				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_ROLE, roleSpinner.getSelectedItem().toString());

				UserDatabaseHelper helper = new UserDatabaseHelper(InitialInformationActivity.this);
				SQLiteDatabase database = helper.getWritableDatabase();
				database.insert(UserDatabaseHelper.TABLE_QUESTIONNAIRE, null, values);
				database.close();

				ProgressDialog dialog = new ProgressDialog(InitialInformationActivity.this);
				if (dialog != null) {
					dialog.setMessage("Backup in progress");
					dialog.setCancelable(true);
					dialog.show();
				}

				if (dialog != null) {
					dialog.dismiss();
				}
				ServiceHelper.startQuestionnaireUpload(InitialInformationActivity.this);
				// finishWithResult(null);
				showThanksDialog();
				Editor editor = preferences.edit();
				editor.putBoolean(getString(R.string.settings_first_start_key), false);
				editor.putBoolean(getString(R.string.settings_inital_survey_completed), true);
				editor.commit();
			}
		});

		notNowButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				showNotNowDialog();
				// Intent startMain = new Intent(Intent.ACTION_MAIN);
				// startMain.addCategory(Intent.CATEGORY_HOME);
				// startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// startActivity(startMain);
				Editor editor = preferences.edit();
				editor.putBoolean(getString(R.string.settings_first_start_key), false);
				editor.commit();
			}
		});

	}


	private void showNotNowDialog() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(getString(R.string.skip_questionnaire_title));
		alertDialogBuilder.setMessage(getString(R.string.skip_questionnaire_text)).setCancelable(false)
				.setPositiveButton("Yes, I am sure!", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						// finishWithResult(null);
						// nextActivity();
						dialog.dismiss();
						showThanksDialog();
					}
				}).setNegativeButton("Back", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						dialog.cancel();
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}


	private String getMacAddress() {

		try {
			// get wifi mac
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (!wifiManager.isWifiEnabled())
				wifiManager.setWifiEnabled(true);
			return wifiManager.getConnectionInfo().getMacAddress();
		} catch (Exception e) {

		}
		return "No Device ID";
	}


	private String getBluetoothAddress() {

		try {
			// get bluetooth address
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			// make the device always discoverable and get the bluetooth
			// address
			if (bluetoothAdapter != null) {
				// Intent discoverableIntent = new
				// Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				// discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
				// 0);
				// startActivity(discoverableIntent);
				return bluetoothAdapter.getAddress();
			}

		} catch (Exception e) {
		}
		return "No Devide ID";
	}


	private void nextActivity() {

		Intent intent = new Intent(InitialInformationActivity.this, MainActivity.class);
		startActivity(intent);
	}


	private void showQuestionnaireUncompleteDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.questionnaireIncompleteTitle).setMessage(R.string.dialogmessage)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

					}
				});
		// Create the AlertDialog object and return it
		builder.create();
		builder.show();
	}


	private void showThanksDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.thanksDialogTitle).setMessage(R.string.thanksDialog)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						Editor editor = preferences.edit();
						editor.putBoolean(getString(R.string.data_collection_key), true);
						editor.commit();
						nextActivity();
					}
				});
		// Create the AlertDialog object and return it
		builder.create();
		builder.show();
	}


	private void startImpressum() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.impressumTitle).setMessage(R.string.impressumText)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

					}
				});
		// Create the AlertDialog object and return it
		builder.create();
		builder.show();
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		MenuItem menuItem2 = menu.add("Mail");
		menuItem2.setIcon(R.drawable.content_email).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menuItem2.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(final MenuItem item) {

				final Intent _Intent = new Intent(android.content.Intent.ACTION_SEND);
				_Intent.setType("text/html");
				_Intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { getString(R.string.mail_feedback_email) });
				_Intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
				String name = preferences.getString(getString(R.string.login_name_key), "");
				_Intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message) + name);
				startActivity(Intent.createChooser(_Intent, getString(R.string.title_send_feedback)));
				return true;
			}
		});

		MenuItem menuItem = menu.add("About");
		menuItem.setIcon(R.drawable.action_about).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(final MenuItem item) {

				Editor editor = preferences.edit();
				editor.putBoolean(getString(R.string.impressum), true);
				editor.commit();

				Intent intent = new Intent(InitialInformationActivity.this, WelcomeActivity.class);
				startActivity(intent);
				return true;
			}
		});

		return true;
	}
}
