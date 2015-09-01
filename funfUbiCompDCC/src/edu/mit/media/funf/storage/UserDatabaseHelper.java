package edu.mit.media.funf.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

public class UserDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "UbiCompDCCUser";

	public static final String TABLE_STANDARD_ID = "_id";
	public static final String TABLE_STANDARD_NAME = "name";
	public static final String TABLE_STANDARD_TIMESTAMP = "createdTimestamp";
	public static final String TABLE_STANDARD_VALUE = "value";
	public static final String TABLE_STANDARD_ADDITIONAL = "additional";
	public static final String TABLE_STANDARD_PROBE = "probe";
	public static final String TABLE_STANDARD_DEVICEUID = "deviceUID";

	public static final String TABLE_REGISTRATION = "registration";
	public static final String TABLE_REGISTRATION_NAME = "name";
	public static final String TABLE_REGISTRATION_EMAIL = "email";
	public static final String TABLE_REGISTRATION_AFFILIATION = "affiliation";

	public static final String TABLE_QUESTIONNAIRE = "questionnaire";
	public static final String TABLE_QUESTIONNAIRE_ROLE = "role";
	public static final String TABLE_QUESTIONNAIRE_AGE = "age";
	public static final String TABLE_QUESTIONNAIRE_GENDER = "gender";
	public static final String TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY = "academiaorindustry";
	public static final String TABLE_QUESTIONNAIRE_DEVICEUID = "deviceUID";
	public static final String TABLE_QUESTIONNAIRE_MACADDRESS = "macAddress";
	public static final String TABLE_QUESTIONNAIRE_BLUETOOTHADDRESS = "bluetoothAddress";

	private static final Table TABLE_REGISTRATION_CREATE = new Table(TABLE_REGISTRATION, Arrays.asList(new Column(TABLE_REGISTRATION_NAME, "TEXT"),
			new Column(TABLE_REGISTRATION_EMAIL, "TEXT"), new Column(TABLE_REGISTRATION_AFFILIATION, "TEXT")));

	private static final Table TABLE_QUESTIONNAIRE_CREATE = new Table(TABLE_QUESTIONNAIRE, Arrays.asList(new Column(TABLE_QUESTIONNAIRE_AGE, "TEXT"),
			new Column(TABLE_QUESTIONNAIRE_ROLE, "TEXT"), new Column(TABLE_QUESTIONNAIRE_GENDER, "TEXT"), new Column(
					TABLE_QUESTIONNAIRE_ACADEMIAORINDUSTRY, "TEXT"), new Column(TABLE_QUESTIONNAIRE_DEVICEUID, "TEXT"), new Column(
					TABLE_QUESTIONNAIRE_MACADDRESS, "TEXT"), new Column(TABLE_QUESTIONNAIRE_BLUETOOTHADDRESS, "TEXT")));

	private static SQLiteOpenHelper databaseHelper = null;


	public UserDatabaseHelper(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	private static UserDatabaseHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (databaseHelper == null) {
			databaseHelper = new DatabaseHelper(context);
		}
		return (UserDatabaseHelper) databaseHelper;
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

		db.execSQL(TABLE_QUESTIONNAIRE_CREATE.getCreateTableSQL());
		db.execSQL(TABLE_REGISTRATION_CREATE.getCreateTableSQL());
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
