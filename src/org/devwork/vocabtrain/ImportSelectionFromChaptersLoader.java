package org.devwork.vocabtrain;

import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class ImportSelectionFromChaptersLoader extends ChapterBatchLoader
{
	public ImportSelectionFromChaptersLoader(FragmentActivity activity, Integer[] lesson_ids)
	{
		super(activity, lesson_ids, activity.getString(R.string.loader_select), activity.getString(R.string.loader_select_chapters_desc));
	}
	public ImportSelectionFromChaptersLoader(ChapterBatchLoader firstProcess)
	{
		super(firstProcess);
//		progressDialog.setMessage("Importing cards to selection");
	}



	@Override
	protected BasicTask createTask()
	{
		return new Task();
	}

	protected class Task extends BasicTask {
		
		Task() {
			super(R.plurals.loader_select_finished);
		}


		@Override
		protected void run(SQLiteDatabase db)
		{
	    	db.execSQL("insert into selection (selection_card_id) select cards._id from cards LEFT JOIN selection on selection_card_id = cards._id " + 
	    			getWhereClause(db) + 
	    			" AND selection_card_id is null " +
	    			"GROUP BY cards._id");
	    	sum = DatabaseFunctions.getRowsAffected(db);
		}

		
		
		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
	        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
	        Fragment fragment = fragmentManager.findFragmentByTag(DashboardFragment.TAG);
	        if(fragment instanceof DashboardFragment)
	        {
	        	DashboardFragment dashboard = (DashboardFragment) fragmentManager.findFragmentByTag(DashboardFragment.TAG);
	        	dashboard.updateCardsCount();
	        }
		}
		
	}
	
}
