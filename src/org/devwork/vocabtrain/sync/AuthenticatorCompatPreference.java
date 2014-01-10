package org.devwork.vocabtrain.sync;

import org.devwork.vocabtrain.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AuthenticatorCompatPreference extends DialogPreference
{

	public AuthenticatorCompatPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setDialogLayoutResource(R.layout.authenticator_compat_preference);
		setDialogTitle(context.getString(R.string.pref_database_location));
	}

	ProgressBar progress;
	TextView text;

	@Override
	protected View onCreateDialogView()
	{
		View v = super.onCreateDialogView();
		progress = (ProgressBar) v.findViewById(R.id.authenticator_compat_progress);
		text = (TextView) v.findViewById(R.id.authenticator_compat_text);
		if(Build.BOARD.equals("zoom2") && Build.BRAND.equals("nook") && Build.DEVICE.equals("zoom2")) // nook touch renders default white text on white background
		{
			text.setTextColor(Color.BLACK);
		}
		text.setText(R.string.ui_activity_authenticating);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		String username = prefs.getString("account_username", null);
		String password = prefs.getString("account_password", null);
		if(username != null && username.length() > 0 && password != null && password.length() > 0)
		{
			task = new UserLoginTask(username, password);
			task.execute();
		}
		else
		{
			text.setText(R.string.login_activity_loginfail_text_both);
			progress.setVisibility(View.INVISIBLE);
		}

		return v;
	}

	private UserLoginTask task;

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if(task != null) task.cancel(true);
		if(positiveResult && authToken != null)
		{
			SharedPreferences prefs = getSharedPreferences();
			Editor edit = prefs.edit();
			edit.putString("account_authToken", authToken);
			edit.commit();
		}
	}

	private String authToken = null;

	private class UserLoginTask extends AsyncTask<Void, Void, String>
	{

		final private String username;
		final private String password;

		UserLoginTask(String username, String password)
		{
			this.password = password;
			this.username = username;
		}

		@Override
		protected String doInBackground(Void... params)
		{
			try
			{
				return SyncFunctions.authenticate(username, password);
			}
			catch(Exception ex)
			{
				return null;
			}

		}

		@Override
		protected void onCancelled()
		{
			progress.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onPostExecute(final String authToken)
		{
			progress.setVisibility(View.INVISIBLE);
			if(authToken == null)
			{
				text.setText(R.string.login_activity_loginfail_text_pwonly);
				return;
			}
			text.setText(R.string.authenticator_succeed);
			AuthenticatorCompatPreference.this.authToken = authToken;
		}
	}

}
