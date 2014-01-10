package org.devwork.vocabtrain;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

public class ImportFilingLoader extends ChapterBatchLoader
{
	public ImportFilingLoader(FragmentActivity activity, Integer[] lesson_ids)
	{
		super(activity, lesson_ids, activity.getString(R.string.loader_import_filing), activity.getString(R.string.loader_import_filing_desc));
		progressDialog.setCancelable(false);
		sequence = DatabaseFunctions.getSequence(activity);
	}

	private final int sequence;

	/*
	 * public ImportFilingLoader(ChapterBatchLoader firstProcess) { super(firstProcess); sequence = DatabaseFunctions.getSequence(activity); }
	 */
	@Override
	protected BasicTask createTask()
	{
		return new Task();
	}

	private class Task extends BasicTask
	{
		Task()
		{
			super(R.plurals.loader_import_filing_finished);
		}

		@Override
		protected void run(SQLiteDatabase db)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			final int default_priority = Integer.parseInt(prefs.getString("default_priority", "0"));

			long session = 0;
			{
				final Cursor sessionCursor = db.query("filing_data", new String[] { "filing_session" }, "filing_sequence = ?", new String[] { "" + sequence }, null, null, null);
				if(sessionCursor.getCount() == 1 && sessionCursor.moveToFirst()) session = sessionCursor.getLong(0);
				sessionCursor.close();
			}
			db.execSQL("insert into filing (filing_session, filing_priority, filing_rank, filing_sequence, filing_card_id) select ?, ?, ?, ?, `cards`.`_id` FROM cards " + " left join filing on filing_card_id = cards._id AND filing_sequence = ? " + getWhereClause(db) + " AND filing_card_id is null GROUP BY cards._id"

			, new String[] { "" + session, "" + CardFiling.Priority.get(default_priority).get(), "0", "" + sequence, "" + sequence });
			sum = DatabaseFunctions.getRowsAffected(db);
		}

	}
}
