package org.devwork.vocabtrain;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class ProgressDialogFragment extends DialogFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".ProgressDialogFragment";

	public interface ProgressDialogFragmentListener
	{
		void onDismiss();

		void onCreate(ProgressDialog dialog);
	}

	@Override
	public void dismiss()
	{
		if(getActivity() == null) return;
		FragmentManager fm = getActivity().getSupportFragmentManager();
		Fragment self = fm.findFragmentByTag(getTag());
		if(self == null) return;
		try
		{
			if(!getActivity().isFinishing()) super.dismiss();
		}
		catch(java.lang.IllegalStateException e)
		{
		}
	}

	public static ProgressDialogFragment createInstance(String title, String message, boolean indeterminate)
	{
		ProgressDialogFragment fragment = new ProgressDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean("indeterminate", indeterminate);
		bundle.putString("title", title);
		bundle.putString("message", message);

		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState)
	{
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		Bundle bundle = getArguments();
		dialog.setTitle(bundle.getString("title"));
		dialog.setMessage(bundle.getString("message"));

		if(bundle.getBoolean("indeterminate"))
		{
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(true);
		}
		else
		{
			dialog.setIndeterminate(false);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		if(bundle.containsKey("progress")) dialog.setProgress(bundle.getInt("progress"));
		if(bundle.containsKey("second_progress")) dialog.setSecondaryProgress(bundle.getInt("second_progress"));
		if(bundle.containsKey("max")) dialog.setMax(bundle.getInt("max"));

		return dialog;
	}

	private List<OnDismissListener> dismisslisteners = new LinkedList<OnDismissListener>();

	public void setOnDismissListener(OnDismissListener listener)
	{
		dismisslisteners.add(listener);
	}

	@Override
	public void onDismiss(DialogInterface dialog)
	{
		Log.e(TAG, "onDismiss");
		// super.onDismiss(dialog); will dismiss the dialog here! But we want to retain the instance!
		Iterator<OnDismissListener> it = dismisslisteners.iterator();
		while(it.hasNext())
			it.next().onDismiss(dialog);
	}

	public void setMax(int max)
	{
		if(getDialog() != null)
		{
			((ProgressDialog) getDialog()).setMax(max);
		}
		getArguments().putInt("max", max);
	}

	public void setProgress(int value)
	{
		if(getDialog() != null)
		{
			((ProgressDialog) getDialog()).setProgress(value);
		}
		getArguments().putInt("progress", value);
	}

	public int getProgress()
	{
		return getArguments().getInt("progress");
	}

	private List<OnCancelListener> cancellisteners = new LinkedList<OnCancelListener>();

	public void setOnCancelListener(OnCancelListener listener)
	{
		cancellisteners.add(listener);
	}

	@Override
	public void onCancel(DialogInterface dialog)
	{
		Log.e(TAG, "oncancel");
		super.onCancel(dialog);
		super.onDismiss(dialog);
		Iterator<OnCancelListener> it = cancellisteners.iterator();
		while(it.hasNext())
			it.next().onCancel(dialog);
	}

	public void setMessage(String message)
	{
		if(getDialog() != null)
		{
			((ProgressDialog) getDialog()).setMessage(message);
		}
		getArguments().putString("message", message);
	}

	public void setSecondaryProgress(int value)
	{
		if(getDialog() != null)
		{
			((ProgressDialog) getDialog()).setSecondaryProgress(value);
		}
		getArguments().putInt("second_progress", value);
	}

}
