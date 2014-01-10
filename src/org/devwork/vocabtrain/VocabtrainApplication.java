package org.devwork.vocabtrain;

import java.io.IOException;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.Log;

@ReportsCrashes(formKey = "dGN3VEJ1b1NyaGtrMVp5Z0JBYXptSEE6MQ", mode = ReportingInteractionMode.NOTIFICATION, resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
resNotifTickerText = R.string.crash_notif_ticker_text, resNotifTitle = R.string.crash_notif_title, resNotifText = R.string.crash_notif_text, resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, // optional. default is a warning sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class VocabtrainApplication extends Application
{

	@Override
	public void onCreate()
	{

		super.onCreate();
		ACRA.init(this); // The following line triggers the initialization of ACRA
		PreferenceManager.setDefaultValues(this, R.xml.preference_acra, false);
		PreferenceManager.setDefaultValues(this, R.xml.preference_aids, false);
		PreferenceManager.setDefaultValues(this, R.xml.preference_display, false);
		PreferenceManager.setDefaultValues(this, R.xml.preference_general, false);
		PreferenceManager.setDefaultValues(this, R.xml.preference_session, false);
		PreferenceManager.setDefaultValues(this, R.xml.preference_tweaks, false);
		DatabaseInitialisizer.initialize(this);
		try
		{
			CharacterTranslator.createCharacterTranslator(getAssets());
		}
		catch(final IOException e)
		{
			assert false;
		}
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String vernicular = prefs.getString("language", null);
		if(vernicular == null)
		{
			LanguageLocale locale = new LanguageLocale(this.getApplicationContext(), Locale.getDefault().getISO3Language());
			if(!locale.equals(LanguageLocale.Language.UNKNOWN))
			{
				vernicular = locale.toString();
				Log.e("APPVERNICAL", vernicular);
				Editor edit = prefs.edit();
				edit.putString("language", vernicular);
				edit.commit();
			}
		}
		if(vernicular != null)
		{
			final Locale locale = new Locale(vernicular);
			if(locale != null)
			{
				Locale.setDefault(locale);
				Configuration config = new Configuration();
				config.locale = locale;
				getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
			}
		}

	}
}
