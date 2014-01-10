package org.devwork.vocabtrain.sync;

import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.ProgressDialogFragment;
import org.devwork.vocabtrain.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

public abstract class AbstractLoader extends AsyncTask<Void, String, Void> implements OnCancelListener
{
	private static final String TAG = Constants.PACKAGE_NAME + ".BasicLoader";
	private final FragmentActivity activity;
	protected final ProgressDialogFragment progressDialog; // TODO
	private final Context context;

	protected final static int MAX_RETRIES = 10;

	private boolean successful = true;

	public AbstractLoader(final Context context, final int dialog_title, final int dialog_desc, final boolean intermediate)
	{
		this.context = context;
		if(context instanceof FragmentActivity)
		{
			this.activity = (FragmentActivity) context;
			progressDialog = ProgressDialogFragment.createInstance(activity.getString(dialog_title), activity.getString(dialog_desc), intermediate);
			progressDialog.setCancelable(true);
			progressDialog.setOnCancelListener(this);
		}
		else
		{
			this.activity = null;
			this.progressDialog = null;
		}
	}

	protected void addProgress(final int value)
	{
		if(progressDialog != null) progressDialog.setProgress(progressDialog.getProgress() + value);
	}

	protected void displayError(final String message)
	{
		successful = false;
		if(activity != null) activity.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				if(activity.isFinishing()) return;
				if(progressDialog != null) progressDialog.dismiss();
				final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(context.getString(R.string.sync_loader_error, message)).setTitle(context.getString(R.string.sync_loader_error_title))
						.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								dialog.dismiss();
							}
						});
				final AlertDialog alert = builder.create();
				if(activity.isFinishing()) return;
				alert.show();
			}
		});
	}

	abstract public void doInForeground();

	protected Context getContext()
	{
		return context;
	}

	protected void incrementProgress()
	{
		if(progressDialog != null) progressDialog.setProgress(progressDialog.getProgress() + 1);
	}

	protected boolean isSuccessful()
	{
		return successful;
	}

	@Override
	public void onCancel(final DialogInterface dialog)
	{
		cancel(true);
	}

	@Override
	protected void onCancelled()
	{
		Log.e(TAG, "Cancelled");
		showToast(context.getString(R.string.sync_loader_cancelled));
	}

	@Override
	protected void onPostExecute(final Void result)
	{
		if(activity == null || activity.isFinishing()) return;
		if(progressDialog != null) progressDialog.dismiss();
	}

	@Override
	protected void onPreExecute()
	{
		if(progressDialog == null) return;

		progressDialog.setOnCancelListener(new OnCancelListener()
		{
			@Override
			public void onCancel(final DialogInterface arg0)
			{
				cancel(true);
			}
		});
		if(activity.isFinishing()) return;
		try
		{
			progressDialog.show(activity.getSupportFragmentManager(), TAG);
		}
		catch(final IllegalStateException e)
		{
		}
	}

	protected void setCancelable(final boolean flag)
	{
		if(progressDialog != null) progressDialog.setCancelable(flag);
	}

	protected void setProgress(final int value)
	{
		if(progressDialog != null) progressDialog.setProgress(value);
	}

	protected void setProgressMax(final long l)
	{
		if(progressDialog != null) progressDialog.setMax((int) l);
	}

	protected void setProgressMessage(final String message)
	{
		if(progressDialog != null) activity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				progressDialog.setMessage(message);
			}

		});
	}

	protected void setSuccess(final boolean success)
	{
		successful = success;
	}

	protected void showToast(final String message)
	{
		if(activity != null && !activity.isFinishing())
		{
			final Toast t = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			t.show();
		}
	}

}
