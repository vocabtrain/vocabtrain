package org.devwork.vocabtrain;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class DatabaseHelper
{
	private SQLiteDatabase read;
	private final LanguageLocale language;

	private class OpenHelper extends SQLiteOpenHelper
	{

		public OpenHelper(Context context, String path)
		{
			super(context, path, null, Constants.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			// TODO Auto-generated method stub

		}

	}

	private final OpenHelper openhelper;
	private final boolean externalized;
	private final String filename;
	private final Context context;

	public String getFilename()
	{
		return externalized ? filename : Constants.DATABASE_PATH;
	}

	private final int sequence;

	public DatabaseHelper(Context context)
	{
		this.context = context;
		read = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		language = DatabaseFunctions.getVernicular(context);

		boolean externalized = prefs.getBoolean("database_externalize", false);
		if(externalized)
		{
			filename = prefs.getString("database_filename", null);
			try
			{
				read = SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READONLY);
			}
			catch(Throwable e)
			{
				externalized = false;
				Editor edit = prefs.edit();
				edit.putBoolean("database_externalize", false);
				edit.commit();

			}

		}
		else
			filename = null;
		this.externalized = externalized;
		if(!externalized)
			openhelper = new OpenHelper(context, Constants.DATABASE_NAME);
		else if(externalized && Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1)
			openhelper = new OpenHelper(context, filename);
		else
			openhelper = null;
		sequence = DatabaseFunctions.getSequence(context);
		Log.e("OpenHelper", "" + (openhelper == null) + " - " + filename);
	}

	public void close()
	{
		if(read != null)
		{
			read.close();
			read = null;
		}
		if(openhelper != null)
		{
			openhelper.close();
		}
	}

	@Override
	protected void finalize()
	{
		close();
	}

	public SQLiteDatabase getRead()
	{
		if(read == null) read = getReadableDatabase();
		if(!read.isOpen())
		{
			read = getReadableDatabase();
		}
		return read;
	}

	public SQLiteDatabase getReadableDatabase()
	{
		if(openhelper != null) return openhelper.getReadableDatabase();
		return SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READONLY);
	}

	public SQLiteDatabase getWritableDatabase()
	{
		if(openhelper != null) return openhelper.getWritableDatabase();
		return SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READWRITE);
	}

	public Cursor getBooks()
	{
		SQLiteDatabase db = getRead();
		return db.rawQuery("SELECT * FROM `books` ORDER BY `book_name` ASC", null);
	}

	public int getChaptersCount(long book_id)
	{
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM `chapters` WHERE `chapter_book_id` = '" + book_id + "'", null);
		cursor.moveToFirst();
		int ret = cursor.getInt(0);
		cursor.close();
		db.close();
		return ret;
	}

	public Cursor getChapters(long book_id)
	{
		SQLiteDatabase db = getRead(); // Bugfix Android 1.6: does not strip `` from chapter_volume, thus it must have this form.
		// Cursor cursor = db.rawQuery("SELECT `chapters`.`_id`, chapter_volume, COUNT(*) AS chapter_cards FROM `chapters` JOIN `content` ON `content_chapter_id` = `chapters`.`_id` WHERE `chapter_book_id` = '" + book_id + "' GROUP BY `chapters`.`_id`", null);
		Cursor cursor = db.rawQuery("SELECT `chapters`.`_id` AS _id, chapter_volume, COUNT(`content_card_id`) AS chapter_cards_count, COUNT(`filing_card_id`) AS chapter_filing_count FROM `chapters` JOIN `content` ON `content_chapter_id` = `chapters`.`_id` LEFT JOIN `filing` ON `filing_card_id` = `content_card_id` WHERE `filing_sequence` = ? AND `chapter_book_id` = '" + book_id + "' GROUP BY `chapters`.`_id`", new String[] { "" + sequence });
		return cursor;
	}

	public void clearSelection()
	{
		SQLiteDatabase db = getWritableDatabase();
		DatabaseFunctions.truncateSelection(db);
		db.close();
	}

	public Card getCardById(final long card_id)
	{
		SQLiteDatabase db = this.getRead();
		LanguageLocale currentLanguage = language;
		if(currentLanguage.equals(LanguageLocale.Language.UNKNOWN)) currentLanguage = DatabaseFunctions.getVernicular(context, db);
		{
			final Cursor cursor = db.query("translations", new String[] { "translation_language" }, "translation_card_id = ? AND translation_language = ?", new String[] { "" + card_id, currentLanguage.toString() }, null, null, null);
			if(cursor.getCount() == 0) currentLanguage = null;
			cursor.close();
		}
		final Cursor cursor = currentLanguage == null ? db.rawQuery("SELECT `card_speech`, `card_speech_comment`, `card_script`, `card_script_comment`, `translation_content`, `translation_comment`, `card_type` FROM `cards` JOIN `translations` ON `translation_card_id` = `cards`.`_id` WHERE `cards`.`_id` = '" + card_id + "' LIMIT 1", null) : db.rawQuery("SELECT `card_speech`, `card_speech_comment`, `card_script`, `card_script_comment`, `translation_content`, `translation_comment`, `card_type` FROM `cards` LEFT JOIN `translations` ON `translation_card_id` = `cards`.`_id` WHERE `cards`.`_id` = '" + card_id + "' AND `translation_language` = '" + currentLanguage.toString() + "'", null);

		CardFiling filing = null;
		try
		{
			filing = new CardFiling(card_id, sequence, db);
		}
		catch(RowNotFoundException e)
		{
		}
		if(cursor.getCount() == 0)
		{
			cursor.close();
			return null;
		}

		Cursor languageCursor = db.rawQuery("select book_language from books join chapters on chapter_book_id = books._id join content on content_chapter_id = chapters._id join cards on content_card_id = cards._id where cards._id = ?", new String[] { "" + card_id });
		final LanguageLocale card_language = new LanguageLocale(context, languageCursor.moveToFirst() ? languageCursor.getString(0) : "jpn");
		languageCursor.close();

		cursor.moveToFirst();
		CardType card_type = new CardType(context, card_language, cursor.getInt(cursor.getColumnIndex("card_type")));
		Card ret = new Card(card_id, cursor.getString(cursor.getColumnIndex("card_script")), cursor.getString(cursor.getColumnIndex("card_speech")), cursor.getString(cursor.getColumnIndex("translation_content")), cursor.getString(cursor.getColumnIndex("card_script_comment")), cursor.getString(cursor.getColumnIndex("card_speech_comment")), cursor.getString(cursor.getColumnIndex("translation_comment")), card_type, card_language, filing
		// cursor.getInt(cursor.getColumnIndex(Database.CardTable.WRONG)),
		// cursor.getInt(cursor.getColumnIndex(Database.CardTable.CORRECT))
		);
		cursor.close();
		return ret;
	}

	public Card getCard(long id)
	{
		SQLiteDatabase db = this.getRead();

		Cursor cursor = db.rawQuery("SELECT `selection_card_id` FROM `selection` WHERE `_id` = " + id, null);
		if(cursor.getCount() != 1)
		{
			cursor.close();
			return null;
		}
		cursor.moveToFirst();
		final long card_id = cursor.getLong(0);
		cursor.close();
		return getCardById(card_id);

	}

	public int getSelectionLength()
	{
		try
		// This here is a race condition, if we're using PorterDialog and flip the screen
		{
			SQLiteDatabase db = getReadableDatabase();
			StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM `selection`");
			Cursor cursor = db.rawQuery(query.toString(), null);
			if(cursor.getCount() != 1)
			{
				cursor.close();
				db.close();
				return 0;
			}
			cursor.moveToFirst();
			int ret = cursor.getInt(0);
			cursor.close();
			db.close();
			return ret;
		}
		catch(SQLiteException e)
		{
			return 0;
		}
	}

	void markWrong(final Card card)
	{
		SQLiteDatabase db = getWritableDatabase();
		ContentValues v = new ContentValues();
		v.put("filing_rank", "0");
		db.update("filing", v, "filing_card_id = ? AND filing_sequence = ?", new String[] { "" + card.getId(), "" + sequence });
		db.close();
	}

	void markCorrect(final Card card)
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("UPDATE `filing` SET `filing_rank` = `filing_rank`+'1' WHERE `filing_card_id` = ? AND filing_sequence = ?", new String[] { "" + card.getId(), "" + sequence });
		db.close();
	}

	public static void addCardToSelection(SQLiteDatabase db, long card_id)
	{
		Cursor cursor = db.query("selection", new String[] { "COUNT(*)" }, "selection_card_id = ?", new String[] { "" + card_id }, null, null, null);
		cursor.moveToFirst();
		int exists = cursor.getInt(0);
		cursor.close();
		if(exists != 0) return;
		{
			ContentValues v = new ContentValues();
			v.put("selection_card_id", card_id);
			db.insert("selection", null, v);
		}
	}

}
