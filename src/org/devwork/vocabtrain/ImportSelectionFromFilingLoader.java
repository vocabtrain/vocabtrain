package org.devwork.vocabtrain;

import java.util.LinkedList;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

public class ImportSelectionFromFilingLoader {
	public static final String TAG = FilinglistFragment.TAG + ".ImportSelectionFromFilingLoader";
	private final ProgressDialogFragment progressDialog;
	private final FragmentActivity activity;
	private final LinkedList<Long> filing_ranks;
	private final int sequence;
	public ImportSelectionFromFilingLoader(FragmentActivity activity, LinkedList<Long> filing_ranks, int sequence)
	{
		this.sequence = sequence;
		this.activity = activity;
		this.filing_ranks = filing_ranks;
		progressDialog = ProgressDialogFragment.createInstance(activity.getString(R.string.loader_select), activity.getString(R.string.loader_select_filing_desc), true);
		progressDialog.setCancelable(false);
		
	}
	protected OnFinishListener onfinish = null;

	public void execute()
	{
		new Task().execute();
	}

	public void setOnFinishListener(OnFinishListener onfinish)
	{	
		this.onfinish = onfinish;
	}
	
	private class Task extends AsyncTask<Void, Integer, Integer> {
		int sum = 0;
		@Override
		protected Integer doInBackground(Void... a) {
			if(filing_ranks.size() == 0) return null;
			DatabaseHelper dbh = new DatabaseHelper(activity);
			SQLiteDatabase db = dbh.getWritableDatabase();
			filing_ranks.addFirst(new Long(sequence));
			String[] where_clause = StringUtils.createArray(filing_ranks);
	    	db.execSQL("insert into selection (selection_card_id) select filing_card_id FROM filing LEFT JOIN selection on selection_card_id = filing_card_id WHERE filing_sequence = ? AND filing_rank " + StringUtils.generateQuestionTokens(where_clause.length) + " and selection_card_id is null", where_clause);
	    	sum = DatabaseFunctions.getRowsAffected(db);
			db.close();
			dbh.close();
			return sum;
		}
		@Override
	    protected void onPreExecute() {
			progressDialog.show(activity.getSupportFragmentManager(), TAG);
		}
		
		
		//TODO: vereinfachen?
		@Override
	    protected void onPostExecute(Integer result) {
			progressDialog.dismiss();
			
			Toast toast = Toast.makeText(activity, activity.getResources().getQuantityString(R.plurals.loader_select_finished, sum, sum), Toast.LENGTH_LONG);
			toast.show();
			
	        FragmentManager fragmentManager = activity.getSupportFragmentManager();
	        Fragment fragment = fragmentManager.findFragmentByTag(DashboardFragment.TAG);
	        if(fragment instanceof DashboardFragment)
	        {
	        	DashboardFragment dashboard = (DashboardFragment) fragmentManager.findFragmentByTag(DashboardFragment.TAG);
	        	dashboard.updateCardsCount();
	        }
	        if(onfinish != null) onfinish.onFinish();
	    }
	}
}
