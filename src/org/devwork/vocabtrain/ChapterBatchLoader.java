package org.devwork.vocabtrain;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;




public abstract class ChapterBatchLoader implements OnDismissListener
{

	public final static String TAG = Constants.PACKAGE_NAME + ".BatchTask";

	protected final ProgressDialogFragment progressDialog;
	protected OnFinishListener onfinish = null;
	protected final boolean isSecondaryProcess;
	private final Integer[] chapter_ids;
	private final FragmentActivity activity;
	
	protected FragmentActivity getActivity()
	{
		return activity;
	}
	
	public ChapterBatchLoader(FragmentActivity activity, Integer[] chapter_ids, String title, String message)
	{
		this.activity = activity;
		this.chapter_ids = chapter_ids;
		this.isSecondaryProcess = false;
		this.progressDialog = ProgressDialogFragment.createInstance(title, message, true);
		progressDialog.setCancelable(false);
		progressDialog.setOnDismissListener(this);
	}
	public ChapterBatchLoader(ChapterBatchLoader firstProcess)
	{
		this.activity = firstProcess.activity;
		this.chapter_ids = firstProcess.chapter_ids;
		this.isSecondaryProcess = true;
		this.progressDialog = firstProcess.progressDialog;
		progressDialog.setOnDismissListener(this);
	}
	public void setOnFinishListener(OnFinishListener onfinish)
	{	
		this.onfinish = onfinish;
	}
	
	public void execute()
	{
		if(!dialogDismissed && !progressDialog.isAdded())
			progressDialog.show(activity.getSupportFragmentManager(), TAG);
		final BasicTask task = createTask();
		task.execute(chapter_ids);	
	}
	
	
	private boolean dialogDismissed = false; 

	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.e("DISM", "OSNTOEH");
		dialogDismissed = true;
	}
	
	
	
	protected abstract BasicTask createTask();
	
	protected abstract class BasicTask extends AsyncTask<Integer, Void, Void>  {
		protected int sum = 0;
		private final int message;
		protected BasicTask(int loaderSelectFinished)
		{
			this.message = loaderSelectFinished;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			if(!isSecondaryProcess) 
			{
				progressDialog.dismiss();	
				Toast toast = Toast.makeText(activity, activity.getResources().getQuantityString(message, sum, sum), Toast.LENGTH_LONG);
				toast.show();
			}
			if(onfinish != null) onfinish.onFinish();
		}
		protected void setProgress(int progress)
		{
				if(!isSecondaryProcess) progressDialog.setProgress(progress);
				else progressDialog.setSecondaryProgress(progress);
		}
		
		protected String getWhereClause(SQLiteDatabase db)
		{
			StringBuilder query = new StringBuilder(" JOIN `content` ON `content_card_id` = `cards`.`_id` WHERE `content_chapter_id` in (");
			for(int lesson_id : chapter_ids)
			{
				query.append('\'');;
				query.append(lesson_id);
				query.append("',");
			}
			query.deleteCharAt(query.length()-1);
			query.append(')');
			return query.toString();
		}
		
		protected abstract void run(SQLiteDatabase db);
		@Override
		protected Void doInBackground(Integer... lesson_ids) {
			if(lesson_ids.length == 0) return null;
			DatabaseHelper dbh = new DatabaseHelper(getActivity());
			SQLiteDatabase db = dbh.getWritableDatabase();
			run(db);
			db.close();
			dbh.close();
			return null;
		}

	}
	
}











