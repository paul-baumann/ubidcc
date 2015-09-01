package de.tud.wsn.locator;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.util.ServiceHelper;
import edu.mit.media.funf.storage.UserDatabaseHelper;

public class QuestionnaireFragment extends SherlockFragment {

	private EditText ageEdit;
	private RadioGroup genderRadioGroup;
	private Spinner roleSpinner;
	private RadioGroup academiaOrIndustryRadioGroup;
	private ArrayAdapter<CharSequence> adapter;

	private Button updateButton;
	private Button resetButton;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.questionnairefragment, container, false);
		setupWidgets(view);

		return view;
	}


	private void setupWidgets(final View view) {

		/*
		 * Age, gender, etc. are only editable ONCE in the initial activity.
		 */
		ageEdit = (EditText) view.findViewById(R.id.ageEdit);
		genderRadioGroup = (RadioGroup) view.findViewById(R.id.genderRadioGroup);
		academiaOrIndustryRadioGroup = (RadioGroup) view.findViewById(R.id.academiaRadioGroup);

		roleSpinner = (Spinner) view.findViewById(R.id.roleSpinner);
		adapter = ArrayAdapter.createFromResource(getActivity(), R.array.userRoles, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		roleSpinner.setAdapter(adapter);

		updateButton = (Button) view.findViewById(R.id.btn_accept);
		updateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ContentValues values = new ContentValues();

				String age = ageEdit.getText().toString();
				int genderSelectedID = genderRadioGroup.getCheckedRadioButtonId();
				int academiaSelectedID = academiaOrIndustryRadioGroup.getCheckedRadioButtonId();
				if (academiaSelectedID == -1 || genderSelectedID == -1 || age.length() < 1) {
					showQuestionnaireUncompleteDialog();
					return;
				}

				String android_id = Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID);

				RadioButton selectedButton = (RadioButton) view.findViewById(academiaSelectedID);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY, selectedButton.getText().toString());
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_AGE, age);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_DEVICEUID, android_id);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_MACADDRESS, getMacAddress());
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_BLUETOOTHADDRESS, getBluetoothAddress());

				selectedButton = (RadioButton) view.findViewById(genderSelectedID);
				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_GENDER, selectedButton.getText().toString());

				values.put(UserDatabaseHelper.TABLE_QUESTIONNAIRE_ROLE, roleSpinner.getSelectedItem().toString());

				UserDatabaseHelper helper = new UserDatabaseHelper(getActivity());
				SQLiteDatabase database = helper.getWritableDatabase();
				database.insert(UserDatabaseHelper.TABLE_QUESTIONNAIRE, null, values);
				database.close();

				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				Editor editor = preferences.edit();
				editor.putBoolean(getString(R.string.settings_inital_survey_completed), true);
				editor.putBoolean(getString(R.string.settings_inital_survey_uploaded), false);
				editor.commit();

				ProgressDialog dialog = new ProgressDialog(getActivity());
				if (dialog != null) {
					dialog.setMessage("Backup in progress");
					dialog.setCancelable(true);
					dialog.show();
				}

				if (dialog != null) {
					dialog.dismiss();
				}
				Toast.makeText(getActivity(), "Data saved successfully. It will be uploaded as soon as Wi-Fi connection is available.",
						Toast.LENGTH_SHORT).show();
				ServiceHelper.startQuestionnaireUpload(getActivity());
				loadData();
			}
		});

		resetButton = (Button) view.findViewById(R.id.reset_button);
		resetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				loadData();
			}
		});
	}


	private String getMacAddress() {

		try {
			// get wifi mac
			WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
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

			if (bluetoothAdapter != null) {
				return bluetoothAdapter.getAddress();
			}
		} catch (Exception e) {
		}
		return "No Devide ID";
	}


	@Override
	public void onResume() {

		super.onResume();
		loadData();
	}


	private void loadData() {

		final String android_id = Secure.getString(getActivity().getContentResolver(), Secure.ANDROID_ID);

		UserDatabaseHelper helper = new UserDatabaseHelper(getActivity());
		SQLiteDatabase database = helper.getWritableDatabase();

		String where = UserDatabaseHelper.TABLE_QUESTIONNAIRE_DEVICEUID + " = ?";
		String[] whereArgs = { android_id };
		String[] columns = { UserDatabaseHelper.TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY, UserDatabaseHelper.TABLE_QUESTIONNAIRE_AGE,
				UserDatabaseHelper.TABLE_QUESTIONNAIRE_GENDER, UserDatabaseHelper.TABLE_QUESTIONNAIRE_ROLE };
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

		/**
		 * Load values
		 */
		if (getString(R.string.male).equals(paramsMap.get(UserDatabaseHelper.TABLE_QUESTIONNAIRE_GENDER)))
			genderRadioGroup.check(R.id.genderMale);
		else if (getString(R.string.female).equals(paramsMap.get(UserDatabaseHelper.TABLE_QUESTIONNAIRE_GENDER)))
			genderRadioGroup.check(R.id.genderFemale);

		if (getString(R.string.academia).equals(paramsMap.get(UserDatabaseHelper.TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY)))
			academiaOrIndustryRadioGroup.check(R.id.academiaRadioAcademia);
		else if (getString(R.string.industry).equals(paramsMap.get(UserDatabaseHelper.TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY)))
			academiaOrIndustryRadioGroup.check(R.id.academiaRadioIndustry);

		ageEdit.setText(paramsMap.get(UserDatabaseHelper.TABLE_QUESTIONNAIRE_AGE));
		int index = adapter.getPosition(paramsMap.get(UserDatabaseHelper.TABLE_QUESTIONNAIRE_ROLE));
		roleSpinner.setSelection(index);
	}


	private void showQuestionnaireUncompleteDialog() {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.questionnaireIncompleteTitle).setMessage(R.string.dialogmessage)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

					}
				});
		// Create the AlertDialog object and return it
		builder.create();
		builder.show();
	}
}
