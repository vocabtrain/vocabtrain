package org.devwork.vocabtrain;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FindFragment extends TableViewFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".FindFragment";

	public static FindFragment createInstance(String search)
	{
		FindFragment fragment = new FindFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SearchManager.QUERY, search);
		fragment.setArguments(bundle);
		return fragment;
	}

	public void setSearchString(String search)
	{
		getArguments().putString(SearchManager.QUERY, search);
		generateSearchTable();
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.find_fragment;
	}

	@Override
	public void onResume()
	{
		generateSearchTable();
		super.onResume();
	}

	private void generateSearchTable()
	{
		Cursor cursor = getDatabaseHelper().getRead().rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = ?", new String[] { "search" });
		if(cursor.getCount() == 0)
		{
			SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
			DatabaseFunctions.createSearchTable(db);
			db.close();
			SearchTableLoader task = new SearchTableLoader(getActivity());
			task.execute((Void[]) null);
		}
		cursor.close();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		generateSearchTable();

		View v = super.onCreateView(inflater, container, savedInstanceState);

		caption = (TextView) v.findViewById(R.id.search_caption);

		language = DatabaseFunctions.getVernicular(getActivity(), getDatabaseHelper().getRead());
		return v;
	}

	private TextView caption;
	private LanguageLocale language;

	@Override
	protected Cursor getCursor(String search)
	{

		final SQLiteDatabase db = getDatabaseHelper().getRead();
		String searchstring = null;
		if(getArguments() != null) searchstring = getArguments().getString(SearchManager.QUERY);

		if(search != null && search.length() == 0) search = null;
		if(searchstring != null && searchstring.length() == 0) searchstring = null;
		String where = null;
		if(searchstring != null && search != null)
		{
			// where = "WHERE search MATCH '\"*" + searchstring + "*\" AND \"*" + search + "*\"'"; // bug!!!
			where = getWhere(searchstring, search);
		}
		else
		{
			if(searchstring != null)
				where = getWhere(searchstring);
			else if(search != null)
				where = getWhere(search);
			else
				where = "";
		}

		Cursor cursor = db.rawQuery("SELECT `cards`.`_id`, `card_script`, `card_speech`, `translation_content`, '(' || `card_script_comment` || ')' AS card_script_comment, '(' || `card_speech_comment` || ')' AS card_speech_comment, '(' || `translation_comment` || ')' AS translation_comment FROM `search` " + "LEFT JOIN `cards` ON cards._id = search_card_id " + "LEFT JOIN `translations` ON `translation_card_id` = cards._id AND translation_language = ? " + where, new String[] { language.toString() });

		// cursor = db.rawQuery("SELECT `cards`.`_id`, `card_script`, `card_speech`, `translation_content`, '(' || `card_script_comment` || ')' AS card_script_comment, '(' || `card_speech_comment` || ')' AS card_speech_comment, '(' || `translation_comment` || ')' AS translation_comment FROM cards "
		// + getWhereClause(search), null);

		final int count = cursor.getCount();
		getActivity().runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				caption.setText(getResources().getQuantityString(R.plurals.search_count, count, count));
			}
		});
		if(count > 0 && search == null && searchstring != null)
		{
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(), SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
			suggestions.saveRecentQuery(searchstring, null);
		}
		return cursor;
	}

	private String getWhere(String search)
	{
		final CharacterTranslator trans = CharacterTranslator.getCharacterTranslator();

		return (trans == null || !trans.isReady()) ? "WHERE search MATCH '\"*" + search + "*\"'" : "WHERE search MATCH '\"*" + search + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getHiragana(search) + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getKatakana(search) + "*\"'";
	}

	private String getWhere(String searchA, String searchB)
	{
		final CharacterTranslator trans = CharacterTranslator.getCharacterTranslator();

		return (trans == null || !trans.isReady()) ? "WHERE search MATCH '\"*" + searchA + "*\" \"*" + searchB + "*\"'" : "WHERE search MATCH '" + "\"*" + searchA + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getHiragana(searchA) + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getKatakana(searchA) + "*\" " + "\"*" + searchB + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getHiragana(searchB) + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getKatakana(searchB) + "*\"" + "'";

		/*
		 * "'\"*" + searchA + "*\" \"*" + searchB + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getHiragana(searchA) + "*\" \"*" + CharacterTranslator.getCharacterTranslator().getHiragana(searchB) + "*\" OR \"*" + CharacterTranslator.getCharacterTranslator().getKatakana(searchA) + "*\" \"*" + CharacterTranslator.getCharacterTranslator().getKatakana(searchB) + "*\"'";
		 */
	}

	@Override
	protected LanguageLocale getTargetLanguage()
	{
		final SQLiteDatabase db = getDatabaseHelper().getRead();
		String search = getArguments().getString(SearchManager.QUERY);
		LanguageLocale locale = null;
		Cursor languageCursor = db.rawQuery("select book_language from books join chapters on chapter_book_id = books._id join content on content_chapter_id = chapters._id join search on content_card_id = search_card_id " + (search == null ? "" : getWhere(search)) + " group by book_language", null);
		if(languageCursor.getCount() == 1 && languageCursor.moveToFirst())
		{
			locale = new LanguageLocale(getActivity(), languageCursor.getString(0));
		}
		languageCursor.close();
		return locale;
	}

	@Override
	protected boolean hasSpeechColumn()
	{
		boolean hasSpeech = true;
		String search = getArguments().getString(SearchManager.QUERY);
		final SQLiteDatabase db = getDatabaseHelper().getRead();
		{
			Cursor c = db.rawQuery("SELECT COUNT(*) FROM search " + (search == null ? "" : getWhere(search)) + " AND `search_speech` is not null", null);
			if(c.getCount() == 1 && c.moveToFirst()) hasSpeech = c.getInt(0) != 0;
			c.close();
		}

		/*
		 * final SQLiteDatabase db = getDatabaseHelper().getRead(); { Cursor c = db.rawQuery("SELECT COUNT(*) FROM `cards` " + getWhereClause(null) + " AND `card_script` is not null", null); if(c.getCount() == 1 && c.moveToFirst()) hasScript = c.getInt(0) != 0; c.close(); }
		 */
		return hasSpeech;
	}

	@Override
	protected Card getCardFromRow(long row)
	{
		return getDatabaseHelper().getCardById(row);
	}

}
