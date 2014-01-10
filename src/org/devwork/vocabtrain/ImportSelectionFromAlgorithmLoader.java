package org.devwork.vocabtrain;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

public class ImportSelectionFromAlgorithmLoader extends AsyncTask<Void, Void, Void> implements OnCancelListener {
	public static final String TAG = Constants.PACKAGE_NAME + ".AlgorithmLoader";

	private final ProgressDialogFragment progressDialog;
	private int count = 0;
	private final FragmentActivity activity;
	
	
	public ImportSelectionFromAlgorithmLoader(FragmentActivity activity)
	{
		this.activity = activity;
		this.progressDialog = ProgressDialogFragment.createInstance(activity.getString(R.string.loader_algorithm), activity.getString(R.string.loader_algorithm_desc), true);
		progressDialog.setCancelable(false);
		sequence = DatabaseFunctions.getSequence(activity);
	}
	private final int sequence;



	@Override
	protected void onPreExecute()
	{
		progressDialog.show(activity.getSupportFragmentManager(), TAG);
	}
	
	
	
	@Override
	protected Void doInBackground(Void... params) {
		final DatabaseHelper dbh = new DatabaseHelper(activity);
		final SQLiteDatabase db = dbh.getWritableDatabase();
		long session = 0;
		
		final Cursor sessionCursor = db.query("filing_data", new String [] { "filing_session" }, "filing_sequence = ?", new String[] { "" + sequence  }, null, null, null);
    	if(sessionCursor.getCount() == 1 && sessionCursor.moveToFirst())
    		session = sessionCursor.getLong(0);
    	sessionCursor.close();
    	
    	
    	final Cursor testCursor = db.rawQuery("SELECT `filing_card_id` FROM `filing` WHERE filing_sequence = ? AND (`filing_session`+`filing_interval`) <= " + session, new String[] { "" + sequence });
    	if(testCursor.getCount() == 0)
    	{
    		final Cursor minCursor = db.rawQuery("SELECT MIN(`filing_session`+`filing_interval`) FROM `filing` WHERE `filing_sequence` = ? ", new String[] { "" + sequence });
    		minCursor.moveToFirst();
    		session = minCursor.getLong(0);
    		minCursor.close();
    		final ContentValues cv = new ContentValues();
    		cv.put("filing_session", session);
    		cv.put("filing_sequence", sequence);
    		db.replace("filing_data", null, cv);
    	}
    	testCursor.close();
    	
    	Log.e("LOG", "insert into selection (selection_card_id) select filing_card_id from filing left join selection on selection_card_id = filing_card_id where (filing_session + filing_interval) <= ? AND selection_card_id is null");
    	db.execSQL(
    	"insert into selection (selection_card_id) select filing_card_id from filing left join selection on selection_card_id = filing_card_id where filing_sequence = ? AND (filing_session + filing_interval) <= " + session + " AND selection_card_id is null", new String[] { "" + sequence});

    	count = DatabaseFunctions.getRowsAffected(db);
    	db.close();
    	dbh.close();
		return null;
	}



//TODO: vereinfachen???
	@Override
	protected void onPostExecute(Void result)
	{
		if(activity.isFinishing()) return;
		progressDialog.dismiss();
		
		Toast toast = Toast.makeText(activity, activity.getResources().getQuantityString(R.plurals.loader_algorithm_finished, count, count), Toast.LENGTH_LONG);
		toast.show();
		
        FragmentManager manager = activity.getSupportFragmentManager();
        Fragment foundFragment = manager.findFragmentByTag(DashboardFragment.TAG);
        if(foundFragment != null)
        	((DashboardFragment)foundFragment).updateCardsCount();
		
	}
	@Override
	public void onCancel(DialogInterface dialog) {
		cancel(true);

	}
	
	
};
