package org.devwork.vocabtrain;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class RemoveSelectionLoader extends ChapterBatchLoader
{
	public RemoveSelectionLoader(FragmentActivity activity, Integer[] lesson_ids)
	{
		super(activity, lesson_ids, activity.getString(R.string.loader_remove), activity.getString(R.string.loader_remove_selection_disc));
	}
	@Override
	protected BasicTask createTask()
	{
		return new Task();
	}
	private class Task extends BasicTask {
		protected Task() {
			super(R.plurals.loader_remove_selection_finished);
		}
		

		@Override
		protected void run(SQLiteDatabase db)
		{
			String sql = "delete from selection WHERE selection_card_id IN ( SELECT cards._id FROM cards " + getWhereClause(db) + ")";
			Log.e("SQL", sql);
	    	db.execSQL("delete from selection WHERE selection_card_id IN ( SELECT cards._id FROM cards " + getWhereClause(db) + ")", new String[] {});
	    	sum = DatabaseFunctions.getRowsAffected(db);
		}
		

		
	}
}
