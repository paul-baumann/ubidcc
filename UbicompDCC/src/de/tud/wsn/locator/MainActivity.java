package de.tud.wsn.locator;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import de.darmstadt.tu.wsn.locator.R;
import de.tud.wsn.locator.database.MyService;
import de.tud.wsn.locator.database.UploadRegistrationService;
import de.tud.wsn.locator.database.UploadUserDataService;
import de.tud.wsn.locator.util.ServiceHelper;

public class MainActivity extends SherlockFragmentActivity implements OnSharedPreferenceChangeListener {

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	public static int THEME = R.style.Sherlock___Theme_DarkActionBar;

	public static final String PIPELINE_NAME = "default";
	public static final String QUESTIONNAIRE = "questionnaire";

	private SharedPreferences preferences;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTheme(THEME); // Used for theme switching in samples
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);

		if (!preferences.getBoolean(getString(R.string.data_collection_key), false)) {
			/**
			 * start initial information activity, if these details have not yet
			 * been provided
			 */
			SharedPreferences prefs = getSharedPreferences("edu.mit.media.funf.FunfManager", 0);
			prefs.edit().putString("__DISABLED__", "default").commit();
		}

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager) findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

		mTabsAdapter.addTab(mTabHost.newTabSpec("datacollection").setIndicator("Data collection"), DCCFragment.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec("questionnaire").setIndicator("Questionnaire"), QuestionnaireFragment.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec("register").setIndicator("Data set"), RegisterFragment.class, null);

		if (savedInstanceState != null)
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		// register for preference changes

		MainActivity.startUploadServices(preferences, this);
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

				Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
				startActivity(intent);
				return true;
			}
		});

		return true;
	}


	@Override
	protected void onResume() {

		super.onResume();
		if (!preferences.getBoolean(getString(R.string.settings_inital_survey_completed), false)) {
			/**
			 * start initial information activity, if these details have not yet
			 * been provided
			 */
			SharedPreferences prefs = getSharedPreferences("edu.mit.media.funf.FunfManager", 0);
			prefs.edit().putString("__DISABLED__", "default").commit();
		}
	}


	public static void startUploadServices(SharedPreferences preferences, Context context) {

		if (preferences.getBoolean(context.getString(R.string.settings_inital_survey_completed), false)
				&& !preferences.getBoolean(context.getString(R.string.settings_inital_survey_uploaded), false))
			startUploadQuestionnaire(context);
		if (preferences.getBoolean(context.getString(R.string.settings_registration_for_data_completed), false)
				&& !preferences.getBoolean(context.getString(R.string.settings_registration_for_data_uploaded), false))
			startUploadRegistration(context);
	}


	private static void startUploadQuestionnaire(Context context) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Intent intent = new Intent(context, UploadUserDataService.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				Long.valueOf(context.getString(R.string.uploadQuestionnaireInterval)), pintent);
	}


	private static void startUploadRegistration(Context context) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		Intent intent = new Intent(context, UploadRegistrationService.class);
		PendingIntent pintent = PendingIntent.getService(context, 0, intent, 0);
		AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), Long.valueOf(context.getString(R.string.uploadRegistrationInterval)),
				pintent);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if (key.equals(getString(R.string.data_collection_key))) {
			Intent broadcastIntent = new Intent();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

			if (ServiceHelper.shouldServiceBeStarted(this)) {
				if (preferences.getBoolean(getString(R.string.data_collection_key), false))
					if (!ServiceHelper.isMyServiceRunning(this)) {
						Intent startService = new Intent(this, MyService.class);
						this.startService(startService);
					} else {
						if (ServiceHelper.isMyServiceRunning(this)) {
							Intent stopService = new Intent(this, MyService.class);
							this.stopService(stopService);
						}
					}

				this.sendBroadcast(broadcastIntent);
			}
		}
	}


	/**
	 * SHERLOCK CODE BEGINS HERE...
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

	public static class TabsAdapter extends FragmentPagerAdapter implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {

			private final String tag;
			private final Class<?> clss;
			private final Bundle args;


			TabInfo(String _tag, Class<?> _class, Bundle _args) {

				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {

			private final Context mContext;


			public DummyTabFactory(Context context) {

				mContext = context;
			}


			@Override
			public View createTabContent(String tag) {

				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}


		public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {

			super(activity.getSupportFragmentManager());
			mContext = activity;
			mTabHost = tabHost;
			mViewPager = pager;
			mTabHost.setOnTabChangedListener(this);
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}


		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {

			tabSpec.setContent(new DummyTabFactory(mContext));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			mTabHost.addTab(tabSpec);
			notifyDataSetChanged();
		}


		@Override
		public int getCount() {

			return mTabs.size();
		}


		@Override
		public Fragment getItem(int position) {

			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}


		@Override
		public void onTabChanged(String tabId) {

			int position = mTabHost.getCurrentTab();
			mViewPager.setCurrentItem(position);
		}


		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		}


		@Override
		public void onPageSelected(int position) {

			// Unfortunately when TabHost changes the current tab, it kindly
			// also takes care of putting focus on it when not in touch mode.
			// The jerk.
			// This hack tries to prevent this from pulling focus out of our
			// ViewPager.
			TabWidget widget = mTabHost.getTabWidget();
			int oldFocusability = widget.getDescendantFocusability();
			widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
			mTabHost.setCurrentTab(position);
			widget.setDescendantFocusability(oldFocusability);
		}


		@Override
		public void onPageScrollStateChanged(int state) {

		}
	}

}
