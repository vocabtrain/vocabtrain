package org.devwork.vocabtrain;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class SearchTableLoader extends AsyncTask<Void, Void, Void>
{
	public static final String TAG = Constants.PACKAGE_NAME + ".AlgorithmLoader";

	private final ProgressDialogFragment progressDialog;
	private final FragmentActivity activity;

	public SearchTableLoader(FragmentActivity activity)
	{
		this.activity = activity;
		this.progressDialog = ProgressDialogFragment.createInstance(activity.getString(R.string.loader_search), activity.getString(R.string.loader_search_desc), true);
		progressDialog.setCancelable(false);
	}

	@Override
	protected void onPreExecute()
	{
		progressDialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		DatabaseHelper dbh = new DatabaseHelper(activity);
		SQLiteDatabase db = dbh.getWritableDatabase();
		DatabaseFunctions.createSearchTable(db);
		DatabaseFunctions.generateSearchTable(activity, db);
		db.close();
		dbh.close();
		return null;
	}

	// TODO: vereinfachen???
	@Override
	protected void onPostExecute(Void result)
	{
		progressDialog.dismiss();
		Toast toast = Toast.makeText(activity, activity.getString(R.string.loader_search_finished), Toast.LENGTH_LONG);
		toast.show();
	}

};
