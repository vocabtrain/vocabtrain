package org.devwork.vocabtrain;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class ChangePriorityLoader extends ChapterBatchLoader
{
	private CardFiling.Priority priority;
	private final int sequence;

	public ChangePriorityLoader(FragmentActivity activity, Integer[] lesson_ids, CardFiling.Priority priority)
	{
		super(activity, lesson_ids, activity.getString(R.string.loader_priority_change), activity.getString(R.string.loader_priority_change_desc));
		this.priority = priority;
		sequence = DatabaseFunctions.getSequence(getActivity());
	}
	@Override
	protected BasicTask createTask()
	{
		return new Task();
	}
	private class Task extends BasicTask {
		protected Task() {
			super(R.plurals.loader_priority_change_finished);
		}



		
		@Override
		protected void run(SQLiteDatabase db)
		{
//			String sql = "update filing set filing_priority=( SELECT ? FROM cards " + getWhereClause(db) + ")";
	//		Log.e("SQL", sql);
	    	db.execSQL("UPDATE filing SET filing_priority = ? WHERE filing_sequence = ? AND filing_card_id IN ( SELECT cards._id FROM cards " + getWhereClause(db) + ")", new String[] { "" + priority.get(), "" + sequence});
	    	sum = DatabaseFunctions.getRowsAffected(db);
		}

		/*
		@Override
		protected Void doInBackground(Integer... lesson_ids) {
			if(lesson_ids.length == 0) return null;
			DatabaseHelper dbh = new DatabaseHelper(getActivity());
			SQLiteDatabase db = dbh.getWritableDatabase();
			
			
	    	db.execSQL("update filing set filing_priority=( SELECT ? " + getSelectedCards(db) + ")", new String[] { "" + priority.get()});
	    	sum = DatabaseFunctions.getRowsAffected(db);
	    	db.close();
			
			
			Cursor cursor = getSelectedCards(db);
			try
			{
				db.beginTransaction();
				while(cursor.moveToNext())
				{
					if(isCancelled()) throw new TaskCancelledException();
					long card_id = cursor.getLong(0);
					
					ContentValues v = new ContentValues();
					v.put("filing_priority", priority.get());
					db.update("filing", v, "filing_card_id = ?", new String[] { "" + card_id });
					setProgress(++sum);
				}
				db.setTransactionSuccessful();
			}
        	catch(TaskCancelledException e)    	{}
        	finally
        	{
        		cursor.close();
        		db.endTransaction();
        		db.close();
        		dbh.close();
        	}
			return null;
		}*/
		
		
	}
}
