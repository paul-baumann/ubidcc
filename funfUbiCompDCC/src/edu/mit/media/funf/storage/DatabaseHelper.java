package edu.mit.media.funf.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.time.TimeUtil;
import edu.mit.media.funf.util.StringUtil;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "UbiCompDCCSensors";

	public static final String TABLE_STANDARD_ID = "_id";
	public static final String TABLE_STANDARD_NAME = "name";
	public static final String TABLE_STANDARD_TIMESTAMP = "createdTimestamp";
	public static final String TABLE_STANDARD_VALUE = "value";
	public static final String TABLE_STANDARD_ADDITIONAL = "additional";
	public static final String TABLE_STANDARD_PROBE = "probe";
	public static final String TABLE_STANDARD_DEVICEUID = "deviceuid";

	public static final String TABLE_HELPER = "HelperProbe";
	public static final String TABLE_HELPER_BLUETOOTH = "bluetooth";
	public static final String TABLE_HELPER_WIFI = "wifi";
	public static final String TABLE_HELPER_MOBILE = "mobile";

	public static final String TABLE_WIFI = "WifiProbe";
	public static final String TABLE_WIFI_SSID = "ssid";
	public static final String TABLE_WIFI_BSSID = "bssid";
	public static final String TABLE_WIFI_LEVEL = "level";
	public static final String TABLE_WIFI_FREQUENCY = "frequency";
	public static final String TABLE_WIFI_CAPABILITIES = "capabilities";

	public static final String TABLE_BLUETOOTH = "BluetoothProbe";
	public static final String TABLE_BLUETOOTH_DEVICE = "device";
	public static final String TABLE_BLUETOOTH_RSSI = "rssi";
	public static final String TABLE_BLUETOOTH_BSSID = "bssid";
	public static final String TABLE_BLUETOOTH_CLASS = "class";

	private static final Table TABLE_WIFI_CREATE = new Table(TABLE_WIFI, Arrays.asList(new Column(TABLE_STANDARD_DEVICEUID, "TEXT"), new Column(
			TABLE_STANDARD_PROBE, "TEXT"), new Column(TABLE_STANDARD_TIMESTAMP, "LONG"), new Column(TABLE_WIFI_SSID, "TEXT"), new Column(
			TABLE_WIFI_BSSID, "TEXT"), new Column(TABLE_WIFI_LEVEL, "TEXT"), new Column(TABLE_WIFI_FREQUENCY, "FLOAT"), new Column(
			TABLE_WIFI_CAPABILITIES, "TEXT"), new Column(TABLE_STANDARD_VALUE, "TEXT")));

	private static final Table TABLE_HELPER_CREATE = new Table(TABLE_HELPER, Arrays.asList(new Column(TABLE_STANDARD_DEVICEUID, "TEXT"), new Column(
			TABLE_STANDARD_PROBE, "TEXT"), new Column(TABLE_STANDARD_TIMESTAMP, "LONG"), new Column(TABLE_HELPER_WIFI, "TEXT"), new Column(
			TABLE_HELPER_BLUETOOTH, "TEXT"), new Column(TABLE_HELPER_MOBILE, "TEXT"), new Column(TABLE_STANDARD_VALUE, "TEXT")));

	private static final Table TABLE_BLUETOOTH_CREATE = new Table(TABLE_BLUETOOTH, Arrays.asList(new Column(TABLE_STANDARD_DEVICEUID, "TEXT"),
			new Column(TABLE_STANDARD_PROBE, "TEXT"), new Column(TABLE_STANDARD_TIMESTAMP, "LONG"), new Column(TABLE_BLUETOOTH_DEVICE, "TEXT"),
			new Column(TABLE_BLUETOOTH_BSSID, "TEXT"), new Column(TABLE_BLUETOOTH_CLASS, "TEXT"), new Column(TABLE_BLUETOOTH_RSSI, "TEXT"),
			new Column(TABLE_STANDARD_VALUE, "TEXT")));

	private static SQLiteOpenHelper databaseHelper = null;

	public static List<String> allowedFields = getAllowedList();


	public static List<String> getAllowedList() {

		allowedFields = new LinkedList<String>();
		allowedFields.add(TABLE_WIFI_SSID);
		allowedFields.add(TABLE_WIFI_BSSID);
		allowedFields.add(TABLE_WIFI_LEVEL);
		allowedFields.add(TABLE_WIFI_FREQUENCY);
		allowedFields.add(TABLE_WIFI_CAPABILITIES);
		allowedFields.add(TABLE_BLUETOOTH_DEVICE);
		allowedFields.add(TABLE_BLUETOOTH_RSSI);
		allowedFields.add(TABLE_BLUETOOTH_BSSID);
		allowedFields.add(TABLE_BLUETOOTH_CLASS);
		allowedFields.add(TABLE_HELPER_BLUETOOTH);
		allowedFields.add(TABLE_HELPER_WIFI);
		allowedFields.add(TABLE_HELPER_MOBILE);
		allowedFields.add(TABLE_STANDARD_DEVICEUID);

		return allowedFields;
	}


	public DatabaseHelper(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}


	private static DatabaseHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (databaseHelper == null) {
			databaseHelper = new DatabaseHelper(context);
		}
		return (DatabaseHelper) databaseHelper;
	}


	protected void writeData(String name, IJsonObject data) {

	}


	protected void readData(String name, IJsonObject data) {

	}


	public static void writeDatabase(Context context, String table, String nullColumnHack, ContentValues values) {

		getInstance(context);

		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		if (!values.containsKey(TABLE_STANDARD_ID)) {
			if (TimeUtil.alignedTimestamp == 0) {
				TimeUtil.alignedTimestamp = new Date().getTime();
			}
			values.put(TABLE_STANDARD_TIMESTAMP, TimeUtil.alignedTimestamp);
		}

		try {
			db.insertOrThrow(table, nullColumnHack, values);
		} catch (SQLException e) {
			Integer id = values.getAsInteger(TABLE_STANDARD_ID);
			String where = DatabaseHelper.TABLE_STANDARD_ID + " = ?";
			String[] whereArgs = { String.valueOf(id) };

			db.update(table, values, where, whereArgs);
		}

		db.close();
		databaseHelper.close();
	}


	public static Cursor readDatabase(Context context, String table, String[] columns, String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {

		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		return cursor;
	}


	public static Cursor rawQuery(Context context, String query) {

		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		return cursor;
	}


	public static void execSQL(Context context, String sql) {

		SQLiteDatabase db = getInstance(context).getWritableDatabase();
		db.execSQL(sql);
	}


	public static void deleteDatabase(Context context, String table, String whereClause, String[] whereArgs) {

		SQLiteDatabase db = getInstance(context).getWritableDatabase();
		db.delete(table, whereClause, whereArgs);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(TABLE_WIFI_CREATE.getCreateTableSQL());
		db.execSQL(TABLE_BLUETOOTH_CREATE.getCreateTableSQL());
		db.execSQL(TABLE_HELPER_CREATE.getCreateTableSQL());
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * Immutable Table Definition
	 */
	private static class Table {

		private static final String CREATE_TABLE_FORMAT = "CREATE TABLE %s (_id INTEGER primary key autoincrement, %s);";

		public final String name;
		private final List<Column> columns;


		public Table(final String name, final List<Column> columns) {

			this.name = name;
			this.columns = new ArrayList<Column>(columns);
		}


		public List<Column> getColumns() {

			return new ArrayList<Column>(columns);
		}


		public String getCreateTableSQL() {

			return String.format(CREATE_TABLE_FORMAT, name, StringUtil.join(columns, ", "));
		}
	}

	/**
	 * Immutable Column Definition
	 * 
	 */
	private static class Column {

		public final String name, type;


		public Column(final String name, final String type) {

			this.name = name;
			this.type = type;
		}


		@Override
		public String toString() {

			return name + " " + type;
		}
	}

}
