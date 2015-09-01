package de.tud.wsn.locator;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragment;

import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.database.MyService;
import de.tud.wsn.locator.util.NotificationHelper;
import de.tud.wsn.locator.util.ServiceHelper;

public class DCCFragment extends SherlockFragment implements OnSharedPreferenceChangeListener {

	private SharedPreferences preferences;

	private Button dataCollectionCampaignButton;
	private Button questionnaireButton;
	private Button registrationButton;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.dccfragment, container, false);

		// register for preference changes
		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		preferences.registerOnSharedPreferenceChangeListener(this);

		dataCollectionCampaignButton = (Button) view.findViewById(R.id.startCollection);
		dataCollectionCampaignButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean isDCCRunning = preferences.getBoolean(getString(R.string.data_collection_key), false);
				boolean isDCCPaused = !ServiceHelper.isTimeDuringCollectionDay(getActivity());

				// IS RUNNING AND NOT PAUSED
				if (isDCCRunning && !isDCCPaused) {
					showStopDialog();
				}

				// IS RUNNING BUT PAUSED
				if (isDCCRunning && isDCCPaused) {
					showStopDialog();
				}

				// IS STOPPED
				if (!isDCCRunning || ServiceHelper.isConferenceFinished(getActivity())) {
					Editor editor = preferences.edit();
					editor.putBoolean(getString(R.string.data_collection_key), true);
					editor.commit();

					BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					if (bluetoothAdapter != null && bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
						Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
						discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
						startActivity(discoverableIntent);
					}
					if (isDCCPaused)
						setDCCButtonToPaused();
					else
						setDCCButtonToRunning();
				}

			}

		});

		setupDCCButton();

		questionnaireButton = (Button) view.findViewById(R.id.completeQuestionnaire);
		questionnaireButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean isSurveyCompleted = preferences.getBoolean(getString(R.string.settings_inital_survey_completed), false);

				if (!isSurveyCompleted) {
					TabHost mTabHost = (TabHost) getActivity().findViewById(android.R.id.tabhost);
					mTabHost.setup();
					mTabHost.setCurrentTab(1);
				}
			}
		});

		setupQuestionnaireButton();

		registrationButton = (Button) view.findViewById(R.id.completeRegistration);
		registrationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean isRegistrationCompleted = preferences.getBoolean(getString(R.string.settings_registration_for_data_completed), false);
				if (!isRegistrationCompleted) {
					TabHost mTabHost = (TabHost) getActivity().findViewById(android.R.id.tabhost);
					mTabHost.setup();
					mTabHost.setCurrentTab(2);
				}
			}
		});

		setupRegistrationButton();

		return view;
	}


	private void setupDCCButton() {

		boolean isDCCRunning = preferences.getBoolean(getString(R.string.data_collection_key), false);
		boolean isDCCPaused = !ServiceHelper.isTimeDuringCollectionDay(getActivity());

		if (isDCCRunning && !isDCCPaused) {
			setDCCButtonToRunning();
		}

		// IS RUNNING BUT PAUSED
		if (isDCCRunning && isDCCPaused) {
			setDCCButtonToPaused();
		}

		// IS STOPPED
		if (!isDCCRunning || ServiceHelper.isConferenceFinished(getActivity())) {
			setDCCButtonToStopped();
		}
	}


	private void setupQuestionnaireButton() {

		boolean isSurveyCompleted = preferences.getBoolean(getString(R.string.settings_inital_survey_completed), false);
		boolean isSurveyUploaded = preferences.getBoolean(getString(R.string.settings_inital_survey_uploaded), false);

		if (!isSurveyCompleted)
			setQuestionnaireButtonToIncomplete();
		if (isSurveyCompleted && !isSurveyUploaded)
			setQuestionnaireButtonToUploadPending();
		if (isSurveyUploaded)
			setQuestionnaireButtonToComplete();
	}


	private void setupRegistrationButton() {

		boolean isRegistrationCompleted = preferences.getBoolean(getString(R.string.settings_registration_for_data_completed), false);
		boolean isRegistrationUploaded = preferences.getBoolean(getString(R.string.settings_registration_for_data_uploaded), false);
		if (!isRegistrationCompleted)
			setRegistrationButtonToIncomplete();
		if (isRegistrationCompleted && !isRegistrationUploaded)
			setRegistrationButtonToUploadPending();
		if (isRegistrationUploaded)
			setRegistrationButtonToComplete();
	}


	private void setDCCButtonToRunning() {

		dataCollectionCampaignButton.setText(getString(R.string.dataCollectionRunning));
		dataCollectionCampaignButton.setTextColor(Color.GREEN);
	}


	private void setDCCButtonToPaused() {

		dataCollectionCampaignButton.setText(getString(R.string.dataCollectionPaused));
		dataCollectionCampaignButton.setTextColor(Color.YELLOW);
	}


	private void setDCCButtonToStopped() {

		dataCollectionCampaignButton.setText(getString(R.string.dataCollectionStopped));
		dataCollectionCampaignButton.setTextColor(Color.RED);
	}


	private void setQuestionnaireButtonToIncomplete() {

		questionnaireButton.setText(getString(R.string.questionnaireIncomplete));
		questionnaireButton.setTextColor(Color.RED);
		questionnaireButton.setEnabled(true);
	}


	private void setQuestionnaireButtonToUploadPending() {

		questionnaireButton.setText(getString(R.string.questionnairePending));
		questionnaireButton.setTextColor(Color.YELLOW);
		questionnaireButton.setEnabled(false);
	}


	private void setQuestionnaireButtonToComplete() {

		questionnaireButton.setText(getString(R.string.questionnaireUploaded));
		questionnaireButton.setTextColor(Color.GREEN);
		questionnaireButton.setEnabled(false);
	}


	private void setRegistrationButtonToIncomplete() {

		registrationButton.setText(getString(R.string.registrationIncomplete));
		registrationButton.setTextColor(Color.RED);
		registrationButton.setEnabled(true);
	}


	private void setRegistrationButtonToUploadPending() {

		registrationButton.setText(getString(R.string.registrationPending));
		registrationButton.setTextColor(Color.YELLOW);
		registrationButton.setEnabled(false);
	}


	private void setRegistrationButtonToComplete() {

		registrationButton.setText(getString(R.string.registrationUploaded));
		registrationButton.setTextColor(Color.GREEN);
		registrationButton.setEnabled(false);
	}


	@Override
	public void onResume() {

		super.onResume();
		setSensorStatus();
		setQuestionnaireStatus();
		setRegistrationStatus();

	}


	private void setSensorStatus() {

		if (preferences.getBoolean(getString(R.string.data_collection_key), false)) {
			// sensorStatus.setText(getString(R.string.displayDataCollectionStatusRunning));
			// sensorStatus.setTextColor(Color.GREEN);
			if (ServiceHelper.isTimeDuringCollectionDay(getActivity()))
				NotificationHelper.showRecordingNotification(getActivity(), "");
			else
				NotificationHelper.showRecordingNotification(getActivity(), "but in standby");
		} else {
			// sensorStatus.setText(getString(R.string.displayDataCollectionStatusStopped));
			// sensorStatus.setTextColor(Color.RED);
			NotificationHelper.stopRecordingNotification(getActivity(), "");
		}

		if (ServiceHelper.shouldServiceBeStarted(getActivity())) {
			if (!ServiceHelper.isMyServiceRunning(getActivity())) {
				Intent startService = new Intent(getActivity(), MyService.class);
				getActivity().startService(startService);
			}
		} else {
			if (ServiceHelper.isMyServiceRunning(getActivity())) {
				Intent stopService = new Intent(getActivity(), MyService.class);
				getActivity().stopService(stopService);
			}
		}
	}


	private void setQuestionnaireStatus() {

		if (preferences.getBoolean(getString(R.string.settings_inital_survey_completed), false)) {
			if (preferences.getBoolean(getString(R.string.settings_inital_survey_uploaded), false)) {
				// questionnaireStatus.setText(getString(R.string.displayQuestionnaireStatusCompletedAndUploaded));
				// questionnaireStatus.setTextColor(Color.GREEN);
			} else {
				// questionnaireStatus.setText(getString(R.string.displayQuestionnaireStatusCompleted));
				// questionnaireStatus.setTextColor(Color.BLUE);
			}
		} else {
			// questionnaireStatus.setText(getString(R.string.displayQuestionnaireStatusUncompleted));
			// questionnaireStatus.setTextColor(Color.RED);
		}
	}


	private void setRegistrationStatus() {

		if (preferences.getBoolean(getString(R.string.settings_registration_for_data_completed), false)) {
			if (preferences.getBoolean(getString(R.string.settings_registration_for_data_uploaded), false)) {
				// registrationStatus.setText(getString(R.string.displayRegistrationStatusCompletedAndUploaded));
				// registrationStatus.setTextColor(Color.GREEN);
			} else {
				// registrationStatus.setText(getString(R.string.displayRegistrationStatusCompleted));
				// registrationStatus.setTextColor(Color.BLUE);
			}
		} else {
			// registrationStatus.setText(getString(R.string.displayRegistrationStatusUncompleted));
			// registrationStatus.setTextColor(Color.RED);
		}
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if (isAdded()) {
			if (key.equals(getString(R.string.data_collection_key)))
				setupDCCButton();
			if (key.equals(getString(R.string.settings_inital_survey_completed)) || key.equals(getString(R.string.settings_inital_survey_uploaded)))
				setupQuestionnaireButton();
			if (key.equals(getString(R.string.settings_registration_for_data_completed))
					|| key.equals(getString(R.string.settings_registration_for_data_uploaded)))
				setupRegistrationButton();
		}
	}


	private void showStopDialog() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

		// set title
		alertDialogBuilder.setTitle(getString(R.string.stop_collection_dialog_title));

		// set dialog message
		alertDialogBuilder.setMessage(getString(R.string.stop_collection_dialog_message)).setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						Editor editor = preferences.edit();
						editor.putBoolean(getString(R.string.data_collection_key), false);
						editor.commit();

						BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

						if (bluetoothAdapter != null) {
							bluetoothAdapter.cancelDiscovery();
						}

						setDCCButtonToStopped();
						dialog.dismiss();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {

						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
}
