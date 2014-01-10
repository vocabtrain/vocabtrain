package org.devwork.vocabtrain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.view.MotionEvent;

public final class DatabaseFunctions
{

	public static String PICK_FILE = "org.openintents.action.PICK_FILE";

	public static String PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";

	public static void cleanOrphans(final SQLiteDatabase db)
	{
		db.execSQL("DELETE FROM filing WHERE filing._id IN (SELECT `filing`.`_id` FROM `filing` LEFT JOIN `cards` ON `cards`.`_id` = `filing_card_id` WHERE `cards`.`_id` is null)");
		db.execSQL("DELETE FROM selection WHERE selection._id IN (SELECT `selection`.`_id` FROM `selection` LEFT JOIN `cards` ON `cards`.`_id` = `selection_card_id` WHERE `cards`.`_id` is null)");
	}

	/*
	 * 
	 * public static void cleanOrphans(SQLiteDatabase db) { try { db.beginTransaction(); Cursor c =
	 * db.rawQuery("SELECT `filing`.`_id` FROM `filing` LEFT JOIN `cards` ON `cards`.`_id` = `filing_card_id` WHERE `cards`.`_id` is null", null);
	 * while(c.moveToNext()) { db.delete("filing", "_id = ?", new String[] { c.getString(0) }); } c.close(); c =
	 * db.rawQuery("SELECT `selection`.`_id` FROM `selection` LEFT JOIN `cards` ON `cards`.`_id` = `selection_card_id` WHERE `cards`.`_id` is null", null);
	 * while(c.moveToNext()) { db.delete("selection", "_id = ?", new String[] { c.getString(0) }); } c.close();
	 * 
	 * db.setTransactionSuccessful(); } finally { db.endTransaction(); }
	 * 
	 * }
	 */

	public static void copyData(final String db_read, final String db_write)
	{
		final SQLiteDatabase dbw = SQLiteDatabase.openDatabase(db_write, null, SQLiteDatabase.OPEN_READWRITE);
		try
		{
			dbw.execSQL("DROP TABLE IF EXISTS filing");
			dbw.execSQL("DROP TABLE IF EXISTS selection");
			dbw.execSQL("DROP TABLE IF EXISTS filing_data");
			createExportTables(dbw);

			dbw.execSQL("ATTACH '" + db_read + "' AS i"); // We cannot use BindArgs as usual due to this bug appeared in Honeycomb ::
															// http://code.google.com/p/android/issues/detail?id=15499

			dbw.execSQL("INSERT INTO filing SELECT * FROM i.filing");
			dbw.execSQL("INSERT INTO selection SELECT * FROM i.selection");
			dbw.execSQL("INSERT INTO filing_data SELECT * FROM i.filing_data");
		}
		finally
		{
			dbw.close();
		}
	}
	public static void copyFile(final InputStream input, final OutputStream output) throws IOException
	{
		final byte data[] = new byte[1024];
		int count = 0;
		while((count = input.read(data)) != -1)
		{
			output.write(data, 0, count);
		}
		output.flush();
		output.close();
		input.close();
	}

	public static void createExportTables(final SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS `filing` (`_id` INTEGER, `filing_card_id` INTEGER NOT NULL, `filing_rank` INTEGER NOT NULL, `filing_session` INTEGER DEFAULT 0, `filing_interval` INTEGER DEFAULT 0, `filing_grades` INTEGER DEFAULT 0, `filing_priority` INTEGER DEFAULT 0, `filing_count` INTEGER DEFAULT 0, `filing_difficulty` FLOAT DEFAULT 0, `filing_sequence` INTEGER DEFAULT 12, PRIMARY KEY(`_id`), UNIQUE(`filing_card_id`, `filing_sequence`))");
		db.execSQL("CREATE TABLE IF NOT EXISTS `filing_data` (`_id` INTEGER, `filing_timestamp` INTEGER NOT NULL DEFAULT 0, `filing_session` INTEGER NOT NULL DEFAULT 0, `filing_sequence` INTEGER NOT NULL, PRIMARY KEY(`_id`), UNIQUE(`filing_sequence`))");
		createSelection(db);
	}

	static void createSearchTable(final SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE IF EXISTS search");
		db.execSQL("CREATE VIRTUAL TABLE search USING fts3(search_card_id, search_speech, search_script, search_vernicular)");
	}

	static void createSelection(final SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS `selection` (`_id` INTEGER, `selection_card_id` INTEGER NOT NULL, `selection_forgotten` INTEGER DEFAULT '0', PRIMARY KEY(`_id`), UNIQUE(`selection_card_id`))");
	}

	public static void createTables(final SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE IF NOT EXISTS `metadata` ( `_id` INTEGER, `metadata_key` TEXT NOT NULL, `metadata_value` TEXT NOT NULL, PRIMARY KEY(`_id`), UNIQUE ( `metadata_key`))");
		createExportTables(db);

		db.execSQL("CREATE TABLE IF NOT EXISTS `changes_type` (`_id` INTEGER, `changes_value` INTEGER, PRIMARY KEY(`_id`))");
		db.execSQL("CREATE TABLE IF NOT EXISTS `changes_speech` (`_id` INTEGER, `changes_value` TEXT, PRIMARY KEY(`_id`))");
		db.execSQL("CREATE TABLE IF NOT EXISTS `changes_script` (`_id` INTEGER, `changes_value` TEXT, PRIMARY KEY(`_id`))");
		db.execSQL("CREATE TABLE IF NOT EXISTS `changes_speech_comment` (`_id` INTEGER, `changes_value` TEXT, PRIMARY KEY(`_id`))");
		db.execSQL("CREATE TABLE IF NOT EXISTS `changes_script_comment` (`_id` INTEGER, `changes_value` TEXT, PRIMARY KEY(`_id`))");
		db.execSQL("CREATE TABLE IF NOT EXISTS `changes_vernicular` (`_id` INTEGER, `changes_card_id` INTEGER NOT NULL, `changes_language` TEXT NOT NULL, `changes_value` TEXT, PRIMARY KEY(`_id`), UNIQUE(`changes_card_id`,`changes_language` ))");
		db.execSQL("CREATE TABLE IF NOT EXISTS `changes_vernicular_comment` (`_id` INTEGER, `changes_card_id` INTEGER NOT NULL, `changes_language` TEXT NOT NULL, `changes_value` TEXT, PRIMARY KEY(`_id`), UNIQUE(`changes_card_id`,`changes_language` ))");

	}

	/*
	 * public static void copyData(SQLiteDatabase db_read, SQLiteDatabase db_write, ProgressDialogFragment progress, AsyncTask<?,?,?> task) { try {
	 * db_write.beginTransaction(); createTables(db_write); int count = 0; { Cursor cc = db_read.rawQuery("SELECT COUNT(*) FROM `filing`", null);
	 * cc.moveToFirst(); count = cc.getInt(0); cc.close(); cc = db_read.rawQuery("SELECT COUNT(*) FROM `selection`", null); cc.moveToFirst(); count +=
	 * cc.getInt(0); cc.close(); } if(progress != null) progress.setMax(count); count = 0;
	 * 
	 * Cursor c = db_read.rawQuery("SELECT * FROM `filing`", null); while(c.moveToNext()) { if(task != null && task.isCancelled()) return; ContentValues v = new
	 * ContentValues(); v.put("filing_card_id", c.getLong(c.getColumnIndex("filing_card_id"))); v.put("filing_rank",
	 * c.getLong(c.getColumnIndex("filing_rank"))); v.put("filing_session", c.getLong(c.getColumnIndex("filing_session"))); v.put("filing_interval",
	 * c.getLong(c.getColumnIndex("filing_interval"))); v.put("filing_grades", c.getLong(c.getColumnIndex("filing_grades"))); v.put("filing_priority",
	 * c.getLong(c.getColumnIndex("filing_priority"))); v.put("filing_count", c.getLong(c.getColumnIndex("filing_count"))); v.put("filing_difficulty",
	 * c.getFloat(c.getColumnIndex("filing_difficulty"))); db_write.insert("filing", null, v); if(progress != null) progress.setProgress(++count); } c.close();
	 * c = db_read.rawQuery("SELECT * FROM `selection`", null); while(c.moveToNext()) { if(task != null && task.isCancelled()) return; ContentValues v = new
	 * ContentValues(); v.put("selection_forgotten", c.getInt(c.getColumnIndex("selection_forgotten"))); v.put("selection_card_id",
	 * c.getLong(c.getColumnIndex("selection_card_id"))); db_write.insert("selection", null, v); if(progress != null) progress.setProgress(++count); }
	 * c.close(); c = db_read.rawQuery("SELECT * FROM `filing_data`", null); while(c.moveToNext()) { if(task != null && task.isCancelled()) return;
	 * ContentValues v = new ContentValues(); v.put("fdata_key", c.getLong(c.getColumnIndex("fdata_key"))); v.put("fdata_value",
	 * c.getLong(c.getColumnIndex("fdata_value"))); db_write.insert("filing_data", null, v); } c.close();
	 * 
	 * db_write.setTransactionSuccessful(); } finally { db_write.endTransaction(); } }
	 */
	public static File createTempFile(final File path)
	{
		return new File(path, Constants.PACKAGE_NAME + Constants.DATABASE_NAME + ".tmp");
	}

	public static void dropChanges(final SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE changes_speech");
		db.execSQL("DROP TABLE changes_script");
		db.execSQL("DROP TABLE changes_speech_comment");
		db.execSQL("DROP TABLE changes_script_comment");
		db.execSQL("DROP TABLE changes_vernicular");
		db.execSQL("DROP TABLE changes_vernicular_comment");
		db.execSQL("DROP TABLE changes_type");

	}

	static void generateSearchTable(final Context context, final SQLiteDatabase db)
	{
		final LanguageLocale language = getVernicular(context, db);
		db.execSQL(
				"INSERT INTO search SELECT cards._id AS search_card_id, card_speech AS search_speech, card_script AS search_script, translation_content AS search_vernicular FROM cards LEFT JOIN translations on translation_card_id = cards._id AND translation_language = ?",
				new String[] { language.toString() });
	}

	public static float getDistance(final MotionEvent event)
	{
		final float x = DatabaseFunctions.getX(event, 0) - DatabaseFunctions.getX(event, 1);
		final float y = DatabaseFunctions.getY(event, 0) - DatabaseFunctions.getY(event, 1);
		/*
		 * final float x = event.getX(0) - event.getX(1); final float y = event.getY(0) - event.getY(1);
		 */
		return FloatMath.sqrt(x * x + y * y);
	}

	public static Intent getFileManagerIntent(final Context context, final String title, final String button_text, final File file, final String pick)
	{
		if(!context.getPackageManager().queryIntentActivities(new Intent("org.openintents.action.PICK_FILE"), 0).isEmpty())
		{
			final Intent intent = new Intent(pick);
			intent.putExtra("org.openintents.extra.TITLE", title);
			intent.putExtra("org.openintents.extra.BUTTON_TEXT", button_text);
			intent.setData(Uri.fromFile(file));
			return intent;
		}
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		return intent;

	}

	public static int getRowsAffected(final SQLiteDatabase db)
	{
		int count = 0;
		final Cursor changesCursor = db.rawQuery("SELECT changes()", null);
		if(changesCursor.getCount() == 1 && changesCursor.moveToFirst()) count = changesCursor.getInt(0);
		changesCursor.close();
		return count;
	}

	public static int getSequence(final Context context)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.getBoolean("use_default_sequence", true))
		{
			final String seq = prefs.getString("default_sequence", null);
			return seq == null ? prefs.getInt("sequence", Sequence.DEFAULT_SEQUENCE) : Integer.parseInt(seq);
		}
		return prefs.getInt("sequence", Sequence.DEFAULT_SEQUENCE);
	}

	public static LanguageLocale getVernicular(final Context context)
	{
		return new LanguageLocale(context, PreferenceManager.getDefaultSharedPreferences(context).getString("language", null));
	}

	public static LanguageLocale getVernicular(final Context context, final SQLiteDatabase db)
	{
		String language = PreferenceManager.getDefaultSharedPreferences(context).getString("language", null);
		if(language != null) return new LanguageLocale(context, language);
		final Cursor c = db.rawQuery("SELECT translation_language from translations WHERE translation_language is not null LIMIT 1", null);
		try
		{
			if(c.getCount() != 0 && c.moveToFirst())
			{
				language = c.getString(0);
				return new LanguageLocale(context, language);
			}
			else return new LanguageLocale(context, LanguageLocale.Language.UNKNOWN.toString());
		}
		finally
		{
			c.close();
		}

	}

	public static Typeface getTypefaceFromLocale(Context context, LanguageLocale locale) throws IOException
	{
		final String lang = locale.getLanguage();
		return getTypefaceFromLocale(context, lang);
	}
	
	public static Typeface getTypefaceFromLocale(Context context, String lang) throws IOException
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if(prefs.getBoolean("typeface_use_system_" + lang, true))
		{
			final String family = prefs.getString("typeface_system_" + lang, null);
			final String style = prefs.getString("typeface_style_" + lang, null);
			if(family != null)
			{
				if(style != null)
					return Typeface.create(family, Integer.parseInt(style));
				else
					return Typeface.create(family, Typeface.NORMAL);
			}
			else if(style != null) return Typeface.defaultFromStyle(Integer.parseInt(style));
			return Typeface.DEFAULT;
		}
		else
		{
			final String filename = prefs.getString("typeface_custom_" + lang, null);
			try
			{
				return (filename == null) ? Typeface.DEFAULT : Typeface.createFromFile(filename);
			}
			catch(RuntimeException e)
			{
				throw new IOException(context.getString(R.string.typeface_missing, filename));
			}
		}
	}
	
	
	

	public static final float getX(final MotionEvent event, final int num)
	{
		try
		{
			final Class<?>[] args = new Class[] { int.class };
			final Method start = MotionEvent.class.getMethod("getX", args);
			return (Float) start.invoke(event, num);
		}
		catch(final Throwable e)
		{
		}
		return 0;
	}

	public static final float getY(final MotionEvent event, final int num)
	{
		try
		{
			final Class<?>[] args = new Class[] { int.class };
			final Method start = MotionEvent.class.getMethod("getY", args);
			return (Float) start.invoke(event, num);
		}
		catch(final Throwable e)
		{
		}
		return 0;
	}

	public static boolean hasDatabase(final String filename)
	{
		final File f = new File(filename);
		if(!f.exists()) return false;
		SQLiteDatabase db = null;
		try
		{
			db = SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READONLY);
			db.close();
			return true;
		}
		catch(final SQLiteException e)
		{
			return false;
		}
	}

	public static boolean hasFileManager(final Context context)
	{
		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		return !context.getPackageManager().queryIntentActivities(intent, 0).isEmpty();

	}

	static void truncateSelection(final SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE IF EXISTS `selection`");
		createSelection(db);
	}

}
