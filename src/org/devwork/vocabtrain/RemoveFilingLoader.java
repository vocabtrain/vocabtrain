package org.devwork.vocabtrain;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class RemoveFilingLoader extends ChapterBatchLoader
{
	public RemoveFilingLoader(FragmentActivity activity, Integer[] lesson_ids)
	{
		super(activity, lesson_ids, activity.getString(R.string.loader_remove), activity.getString(R.string.loader_remove_filing_disc));
		sequence = DatabaseFunctions.getSequence(getActivity());
	}

	private final int sequence;

	@Override
	protected BasicTask createTask()
	{
		return new Task();
	}
	private class Task extends BasicTask {
		protected Task() {
			super(R.plurals.loader_remove_filing_finished);
		}


		
		@Override
		protected void run(SQLiteDatabase db)
		{
			//String sql = "delete from filing WHERE filing_card_id IN ( SELECT cards._id FROM cards " + getWhereClause(db) + ")";
			//Log.e("SQL", sql);
	    	db.execSQL("delete from filing WHERE filing_sequence = ? AND filing_card_id IN " +
	    			"( SELECT cards._id FROM cards " + getWhereClause(db) + 
	    			(limit < 0 ? "" : " LIMIT " + limit) +
	    			")", new String[] {"" + sequence });
	    	sum = DatabaseFunctions.getRowsAffected(db);
		}

		
	}
}
