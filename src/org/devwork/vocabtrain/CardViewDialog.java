package org.devwork.vocabtrain;

import java.io.IOException;

import android.app.Dialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.android.ActionBarFragmentActivity;

public class CardViewDialog extends DialogFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".CardViewDialog";

	private ListView listview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View v = inflater.inflate(R.layout.cardview_dialog, container, false);
		listview = (ListView) v.findViewById(R.id.cardviewdialog_listview);
		final Dialog dialog = getDialog();
		if(dialog != null) dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		/*
		 * WindowManager.LayoutParams lp = new WindowManager.LayoutParams(); lp.copyFrom(getDialog().getWindow().getAttributes()); lp.width = WindowManager.LayoutParams.FILL_PARENT; lp.height = WindowManager.LayoutParams.FILL_PARENT; getDialog().getWindow().setAttributes(lp);
		 */
		return v;
	}

	public void setCardId(long card_id)
	{
		getArguments().putLong("card_id", card_id);
	}

	public long getCardId()
	{
		return getArguments().getLong("card_id");
	}

	private Cursor listview_cursor = null;

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();

		final View v = this.getView();
		final long card_id = getArguments().getLong("card_id");
		Log.e(TAG, "card_id : " + card_id);
		TextView speech = (TextView) v.findViewById(R.id.cardviewdialog_speech);
		TextView romaji = (TextView) v.findViewById(R.id.cardviewdialog_romaji);

		TextView speech_comment = (TextView) v.findViewById(R.id.cardviewdialog_speech_comment);
		TextView script = (TextView) v.findViewById(R.id.cardviewdialog_script);
		TextView script_comment = (TextView) v.findViewById(R.id.cardviewdialog_script_comment);
		TextView type = (TextView) v.findViewById(R.id.cardviewdialog_type);
		TextView vernicular = (TextView) v.findViewById(R.id.cardviewdialog_vernicular);
		TextView vernicular_comment = (TextView) v.findViewById(R.id.cardviewdialog_vernicular_comment);
		TextView vernicular_label = (TextView) v.findViewById(R.id.cardviewdialog_vernicular_label);

		TableRow romaji_row = (TableRow) v.findViewById(R.id.cardviewdialog_romaji_row);
		TableRow vernicular_comment_row = (TableRow) v.findViewById(R.id.cardviewdialog_vernicular_comment_row);
		TableRow speech_comment_row = (TableRow) v.findViewById(R.id.cardviewdialog_speech_comment_row);
		TableRow script_comment_row = (TableRow) v.findViewById(R.id.cardviewdialog_script_comment_row);
		// TableRow script_row = (TableRow) v.findViewById(R.id.cardviewdialog_script_row);
		TableRow speech_row = (TableRow) v.findViewById(R.id.cardviewdialog_speech_row);

		// final long card_id = this.getArguments().getLong("card_id");

		LinearLayout filing_layout = (LinearLayout) v.findViewById(R.id.cardviewdialog_filing_layout);

		DatabaseHelper dbh = new DatabaseHelper(getActivity());

		Card card = dbh.getCardById(card_id);
		if(card == null)
		{
			dbh.close();
			return;
		}
		script.setText(card.getScript());
		vernicular.setText(card.getVernicular());
		final CharacterTranslator trans = CharacterTranslator.getCharacterTranslator();
		boolean hasRomanji = false;
		if(trans != null && trans.isReady())
		{
			String rawtext = card.getSpeech() == null ? card.getScript() : card.getSpeech();
			String text = trans.fromKatakana(trans.fromHiragana(rawtext));
			if(!text.equals(rawtext))
			{
				romaji.setText(text);
				hasRomanji = true;
			}
		}
		if(!hasRomanji) romaji_row.setVisibility(View.GONE);

		if(card.getSpeech() == null)
			speech_row.setVisibility(View.GONE);
		else
			speech.setText(card.getSpeech());

		if(card.getSpeechComment() == null)
			speech_comment_row.setVisibility(View.GONE);
		else
			speech_comment.setText(card.getSpeechComment());

		if(card.getScriptComment() == null)
			script_comment_row.setVisibility(View.GONE);
		else
			script_comment.setText(card.getScriptComment());

		if(card.getVernicularComment() == null)
			vernicular_comment_row.setVisibility(View.GONE);
		else
			vernicular_comment.setText(card.getVernicularComment());

		type.setText(card.getType().toDetailedString());

		vernicular_label.setText(DatabaseFunctions.getVernicular(getActivity(), dbh.getRead()).getDisplayLanguage() + ": ");

		if(card.getFiling() == null)
		{
			filing_layout.setVisibility(View.GONE);
		}
		else
		{
			TextView rank = (TextView) v.findViewById(R.id.cardviewdialog_rank);
			TextView session = (TextView) v.findViewById(R.id.cardviewdialog_session);
			TextView priority = (TextView) v.findViewById(R.id.cardviewdialog_priority);
			TextView interval = (TextView) v.findViewById(R.id.cardviewdialog_interval);
			TextView average_grade = (TextView) v.findViewById(R.id.cardviewdialog_average_grade);
			TextView count = (TextView) v.findViewById(R.id.cardviewdialog_count);
			TextView difficulty = (TextView) v.findViewById(R.id.cardviewdialog_difficulty);

			CardFiling filing = card.getFiling();
			session.setText("" + filing.session);
			rank.setText("" + filing.rank);
			priority.setText(getActivity().getResources().getString(filing.priority.getStringId()));
			interval.setText("" + filing.interval);
			average_grade.setText(filing.count == 0 ? getString(R.string.no_grades) : "" + ((float) filing.grades / (float) filing.count));
			count.setText("" + filing.count);
			difficulty.setText("" + filing.difficulty);
		}

		SQLiteDatabase db = dbh.getRead();
		if(listview_cursor != null) listview_cursor.close();
		listview_cursor = db.rawQuery("SELECT chapters._id, book_name, chapter_volume FROM chapters JOIN books ON chapter_book_id = books._id JOIN content ON content_chapter_id = chapters._id AND content_card_id = ?", new String[] { "" + card_id });
		{
			int layout_id = (getActivity() instanceof MainActivity) ? R.layout.cardview_child : R.layout.cardview_child_dark;
			if(Build.BOARD.equals("zoom2") && Build.BRAND.equals("nook") && Build.DEVICE.equals("zoom2")) // nook touch renders default white text on white background
				layout_id = R.layout.cardview_child;
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), layout_id, listview_cursor, new String[] { "book_name", "chapter_volume" }, new int[] { R.id.cardedit_child_book, R.id.cardedit_child_chapter });

			listview.setAdapter(adapter);
		}
		db.close();
		dbh.close();
		
		try 
		{
			Typeface foreignTypeface = DatabaseFunctions.getTypefaceFromLocale(getActivity(), card.getLanguage());
			Typeface vernicularTypeface = DatabaseFunctions.getTypefaceFromLocale(getActivity(), DatabaseFunctions.getVernicular(getActivity()));
			
			speech_comment.setTypeface(foreignTypeface);
			script.setTypeface(foreignTypeface);
			script_comment.setTypeface(foreignTypeface);
			speech.setTypeface(foreignTypeface);
			vernicular.setTypeface(vernicularTypeface);
			vernicular_comment.setTypeface(vernicularTypeface);
		} 
		catch (IOException e) {}
		
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if(listview_cursor != null)
		{
			listview_cursor.close();
			listview_cursor = null;
		}
	}

	public static DialogFragment createInstance(long card_id)
	{
		CardViewDialog dialog = new CardViewDialog();
		Bundle args = new Bundle();
		args.putLong("card_id", card_id);
		dialog.setArguments(args);
		return dialog;
	}

}
