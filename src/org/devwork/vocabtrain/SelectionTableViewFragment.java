package org.devwork.vocabtrain;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.android.ActionBarFragmentActivity;

public class SelectionTableViewFragment extends TableViewFragment
{

	public static SelectionTableViewFragment createInstance(boolean clearselection_onDismiss)
	{
		SelectionTableViewFragment fragment = new SelectionTableViewFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("clearselection_onDismiss", clearselection_onDismiss);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if((isRemoving() || isDetached()) && getArguments() != null)
		{
			if(getArguments().getBoolean("clearselection_onDismiss"))
			{
				DatabaseHelper helper = new DatabaseHelper(getActivity()); // already called onPause -> getDatabaseHelper() is closed
				helper.clearSelection();
				if(getActivity() instanceof ActionBarFragmentActivity)
				{
					FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
					Fragment fragment = fragmentManager.findFragmentByTag(DashboardFragment.TAG);
					if(fragment instanceof DashboardFragment)
					{
						DashboardFragment dashboard = (DashboardFragment) fragmentManager.findFragmentByTag(DashboardFragment.TAG);
						dashboard.updateCardsCount();
					}
				}
				helper.close();
			}
		}
	}

	@Override
	protected Cursor getCursor(String search)
	{
		final SQLiteDatabase db = getDatabaseHelper().getRead();
		final LanguageLocale language = DatabaseFunctions.getVernicular(getActivity(), db);

		if(!hasSpeech())
		{
			return search == null ? db.rawQuery("SELECT `selection`.`_id`, `card_script`, `translation_content`, '(' || `card_script_comment` || ')' AS card_script_comment, '(' || `translation_comment` || ')' AS translation_comment FROM `selection` INNER JOIN `cards` ON `selection_card_id` = `cards`.`_id` LEFT JOIN `translations` ON `translation_card_id` = `selection_card_id` AND `translation_language` = ?", new String[] { language.toString() }) : db.rawQuery("SELECT `selection`.`_id`, `card_script`, `translation_content`, '(' || `card_script_comment` || ')' AS card_script_comment, '(' || `translation_comment` || ')' AS translation_comment FROM `selection` INNER JOIN `cards` ON `selection_card_id` = `cards`.`_id` LEFT JOIN `translations` ON `translation_card_id` = `selection_card_id` AND `translation_language` = ? " + "WHERE card_speech LIKE '%" + search + "%' OR " + "card_speech LIKE '%" + CharacterTranslator.getCharacterTranslator().getHiragana(search) + "%' OR " + "card_speech LIKE '%" + CharacterTranslator.getCharacterTranslator().getKatakana(search) + "%' OR " + "translation_content LIKE '%" + search + "%'", new String[] { language.toString() });
		}
		else
		{
			return search == null ? db.rawQuery("SELECT `selection`.`_id`, `card_script`, `card_speech`, `translation_content`, '(' || `card_script_comment` || ')' AS card_script_comment, '(' || `card_speech_comment` || ')' AS card_speech_comment, '(' || `translation_comment` || ')' AS translation_comment FROM `selection` INNER JOIN `cards` ON `selection_card_id` = `cards`.`_id` LEFT JOIN `translations` ON `translation_card_id` = `selection_card_id` AND `translation_language` = ?", new String[] { language.toString() }) : db.rawQuery("SELECT `selection`.`_id`, `card_script`, `card_speech`, `translation_content`, '(' || `card_script_comment` || ')' AS card_script_comment, '(' || `card_speech_comment` || ')' AS card_speech_comment, '(' || `translation_comment` || ')' AS translation_comment FROM `selection` INNER JOIN `cards` ON `selection_card_id` = `cards`.`_id` LEFT JOIN `translations` ON `translation_card_id` = `selection_card_id` AND `translation_language` = ? " + "WHERE card_speech LIKE '%" + search + "%' OR " + "card_speech LIKE '%" + CharacterTranslator.getCharacterTranslator().getHiragana(search) + "%' OR " + "card_speech LIKE '%" + CharacterTranslator.getCharacterTranslator().getKatakana(search) + "%' OR " + "translation_content LIKE '%" + search + "%' OR " + "card_script LIKE '%" + search + "%'", new String[] { language.toString() });

		}
	}

	@Override
	protected LanguageLocale getTargetLanguage()
	{
		final SQLiteDatabase db = getDatabaseHelper().getRead();
		LanguageLocale locale = null;
		Cursor languageCursor = db.rawQuery("select book_language from books join chapters on chapter_book_id = books._id join content on content_chapter_id = chapters._id join selection on content_card_id = selection_card_id group by book_language", null);
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
		final SQLiteDatabase db = getDatabaseHelper().getRead();
		{
			Cursor c = db.rawQuery("SELECT COUNT(*) FROM `selection` JOIN `cards` ON `cards`.`_id` = `selection_card_id` WHERE `card_speech` is not null", null);
			if(c.getCount() == 1 && c.moveToFirst()) hasSpeech = c.getInt(0) != 0;
			c.close();
		}
		return hasSpeech;
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.table_fragment;
	}

	@Override
	protected Card getCardFromRow(long row)
	{
		return getDatabaseHelper().getCard(row);
	}

}
