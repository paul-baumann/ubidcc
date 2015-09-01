package de.tud.wsn.locator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import de.darmstadt.tu.wsn.locator.R;

/**
 * Basic contact data that is required to run the app, and the "README" with
 * terms of service, etc.
 * 
 * @author bjoern
 * 
 */
public class WelcomeActivity extends SherlockActivity {

	public static int THEME = R.style.Sherlock___Theme_DarkActionBar;
	private Button continueButton;
	private SharedPreferences preferences;
	private TextView textView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setTheme(THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		textView = (TextView) findViewById(R.id.welcomeMessageTextView);
		// textView.setMovementMethod(LinkMovementMethod.getInstance());
		// textView.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(getApplicationContext(),
		// RecipeActivity.class);
		// startActivity(intent);
		// }
		// });

		// Resources res = getResources();
		// String text =
		// String.format(res.getString(R.string.welcomeMessageText));
		// CharSequence styledText = Html.fromHtml(text);
		// textView.setText(styledText);

		textView.setMovementMethod(LinkMovementMethod.getInstance());

		continueButton = (Button) findViewById(R.id.btn_continue);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.getBoolean(getString(R.string.impressum), false))
			continueButton.setText("Close");

		if (!preferences.getBoolean(getString(R.string.impressum), false)
				&& !preferences.getBoolean(getString(R.string.settings_first_start_key), true)) {
			Intent intent = new Intent(WelcomeActivity.this, InitialInformationActivity.class);
			startActivity(intent);
		}

		Button continueButton = (Button) findViewById(R.id.btn_continue);

		continueButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				if (!preferences.getBoolean(getString(R.string.impressum), false)) {
					Intent intent = new Intent(WelcomeActivity.this, InitialInformationActivity.class);
					startActivity(intent);
				} else {
					Editor editor = preferences.edit();
					editor.putBoolean(getString(R.string.impressum), false);
					editor.commit();
					finish();
				}

			}
		});

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

		return true;
	}
}
