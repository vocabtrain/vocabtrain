package org.devwork.vocabtrain;

import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;

import com.tomgibara.android.veecheck.Veecheck;

public class SettingsActivity extends PreferenceActivity
{

	public static class AcraActivity extends PreferenceActivity
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_acra);
		}
	}

	@TargetApi(11)
	public static class AcraFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_acra);
		}
	}

	public static class AidsActivity extends PreferenceActivity
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_aids);
		}
	}

	@TargetApi(11)
	public static class AidsFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_aids);
		}
	}

	public static class DisplayActivity extends PreferenceActivity implements SkinPreference.IntentLauncher
	{
		private SkinPreference skinPreference = null;

		@Override
		public void launch(final Intent intent, final int result)
		{
			startActivityForResult(intent, Constants.REQUEST_FILEMANAGER_FOR_SKIN);
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
		{
			if(skinPreference != null)
			{
				if(skinPreference.onActivityResult(requestCode, resultCode, data)) return;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		protected void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_display);
			skinPreference = (SkinPreference) findPreference("custom_skin_directory");
			if(skinPreference != null) skinPreference.setIntentLaunchListener(this);
		}
	}
	
	@TargetApi(11)
	public static class DisplayFragment extends PreferenceFragment implements SkinPreference.IntentLauncher
	{
		private SkinPreference skinPreference = null;

		@Override
		public void launch(final Intent intent, final int result)
		{
			startActivityForResult(intent, Constants.REQUEST_FILEMANAGER_FOR_SKIN);
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
		{
			if(skinPreference != null)
			{
				if(skinPreference.onActivityResult(requestCode, resultCode, data)) return;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_display);
			skinPreference = (SkinPreference) findPreference("custom_skin_directory");
			if(skinPreference != null) skinPreference.setIntentLaunchListener(this);
		}
	}

	public static class GeneralActivity extends PreferenceActivity implements DatabasePreference.IntentLauncher
	{
		private DatabasePreference databasePreference = null;

		@Override
		public void launch(final Intent intent, final int result)
		{
			startActivityForResult(intent, Constants.REQUEST_FILEMANAGER_FOR_DATABASE);
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
		{
			if(databasePreference != null)
			{
				if(databasePreference.onActivityResult(requestCode, resultCode, data)) return;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_general);
			populateLanguageList(this, (ListPreference) findPreference("language"));
			databasePreference = (DatabasePreference) findPreference("database");
			if(databasePreference != null) databasePreference.setIntentLaunchListener(this);
		}
	}
	
	@TargetApi(11)
	public static class GeneralFragment extends PreferenceFragment implements DatabasePreference.IntentLauncher
	{
		private DatabasePreference databasePreference = null;

		@Override
		public void launch(final Intent intent, final int result)
		{
			startActivityForResult(intent, Constants.REQUEST_FILEMANAGER_FOR_DATABASE);
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
		{
			if(databasePreference != null)
			{
				if(databasePreference.onActivityResult(requestCode, resultCode, data)) return;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_general);
			populateLanguageList(getActivity(), (ListPreference) findPreference("language"));
			databasePreference = (DatabasePreference) findPreference("database");
			if(databasePreference != null) databasePreference.setIntentLaunchListener(this);
		}
	}

	public static class SessionActivity extends PreferenceActivity
	{
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_session);
			populateSequenceList(this, (ListPreference) findPreference("default_sequence"));
		}
	}

	@TargetApi(11)
	public static class SessionFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_session);
			populateSequenceList(getActivity(), (ListPreference) findPreference("default_sequence"));
		}
	}

	public static class TweaksActivity extends PreferenceActivity
	{
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_tweaks);
		}

		@Override
		protected void onPause()
		{
			super.onPause();
			sendBroadcast(new Intent(Veecheck.getRescheduleAction(this)));
		}
	}

	@TargetApi(11)
	public static class TweaksFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_tweaks);
		}

		@Override
		public void onPause()
		{
			super.onPause();
			getActivity().sendBroadcast(new Intent(Veecheck.getRescheduleAction(getActivity())));
		}

	}

	public static class TypefaceActivity extends PreferenceActivity implements TypefacePreference.IntentLauncher
	{
		private final List<TypefacePreference> list = new LinkedList<TypefacePreference>();

		@Override
		public void addActivityResultListener(final TypefacePreference listener)
		{
			list.add(listener);
		}

		@Override
		public void launch(final Intent intent, final int result)
		{
			startActivityForResult(intent, Constants.REQUEST_FILEMANAGER_FOR_TYPEFACE);
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
		{
			for(final TypefacePreference pref : list)
			{
				final Dialog dlg = pref.getDialog();
				if(dlg == null || !dlg.isShowing()) continue;
				pref.onActivityResult(requestCode, resultCode, data);
				return;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setPreferenceScreen(createTypefacePreferenceScreen(this, this, this.getPreferenceManager()));
		}
	}
	
	@TargetApi(11)
	public static class TypefaceFragment extends PreferenceFragment implements TypefacePreference.IntentLauncher
	{
		private final List<TypefacePreference> list = new LinkedList<TypefacePreference>();

		@Override
		public void addActivityResultListener(final TypefacePreference listener)
		{
			list.add(listener);
		}

		@Override
		public void launch(final Intent intent, final int result)
		{
			startActivityForResult(intent, Constants.REQUEST_FILEMANAGER_FOR_TYPEFACE);
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
		{
			for(final TypefacePreference pref : list)
			{
				final Dialog dlg = pref.getDialog();
				if(dlg == null || !dlg.isShowing()) continue;
				pref.onActivityResult(requestCode, resultCode, data);
				return;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setPreferenceScreen(createTypefacePreferenceScreen(getActivity(), this, this.getPreferenceManager()));
		}
	}

	private final static CharSequence[] typefaceStyles = { "" + Typeface.BOLD, "" + Typeface.BOLD_ITALIC, "" + Typeface.ITALIC, "" + Typeface.NORMAL };

	private static PreferenceCategory addTypefaceCategory(final Context context, final TypefacePreference.IntentLauncher launcher,
			final SharedPreferences prefs, final PreferenceScreen root, final LanguageLocale language)
	{
		final String lang = language.getLanguage();
		Log.e("HALLO", lang);
		final String useSystemFontKey = "typeface_use_system_" + lang;
		final PreferenceCategory category = new PreferenceCategory(context);
		category.setTitle(language.getDisplayLanguage());
		final CheckBoxPreference useSystemFont = new CheckBoxPreference(context);
		useSystemFont.setKey(useSystemFontKey);
		useSystemFont.setDefaultValue(true);
		useSystemFont.setTitle(R.string.pref_typeface_use_system_font_title);
		useSystemFont.setSummaryOn(R.string.pref_typeface_use_system_font_on);
		useSystemFont.setSummaryOff(R.string.pref_typeface_use_system_font_off);
		final ListPreference systemFonts = new ListPreference(context);
		systemFonts.setTitle(R.string.pref_typeface_system_font_title);
		systemFonts.setSummary(R.string.pref_typeface_system_font_summary);
		systemFonts.setKey("typeface_system_" + lang);
		systemFonts.setEntries(R.array.typeface_names);
		systemFonts.setEntryValues(R.array.typeface_values);
		final TypefacePreference customFonts = new TypefacePreference(context, null);
		customFonts.setTitle(R.string.pref_typeface_custom_title);
		customFonts.setSummary(R.string.pref_typeface_custom_summary);
		customFonts.setKey("typeface_custom_" + lang);
		customFonts.setIntentLaunchListener(launcher);
		final ListPreference fontStyle = new ListPreference(context);
		fontStyle.setTitle(R.string.pref_typeface_style_title);
		fontStyle.setSummary(R.string.pref_typeface_style_summary);
		fontStyle.setKey("typeface_style_" + lang);
		fontStyle.setEntries(R.array.typeface_styles);
		fontStyle.setEntryValues(typefaceStyles);
		fontStyle.setDefaultValue("" + Typeface.NORMAL);
		Log.e("HALLO", "" + typefaceStyles[0]);

		final OnPreferenceChangeListener preferenceChangeListener = new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(final Preference preference, final Object newValue)
			{
				if(((Boolean) newValue).booleanValue())
				{
					systemFonts.setEnabled(true);
					customFonts.setEnabled(false);
					fontStyle.setEnabled(true);
				}
				else
				{
					systemFonts.setEnabled(false);
					customFonts.setEnabled(true);
					fontStyle.setEnabled(false);
				}
				return true;
			}
		};

		preferenceChangeListener.onPreferenceChange(useSystemFont, prefs.getBoolean(useSystemFontKey, true));
		useSystemFont.setOnPreferenceChangeListener(preferenceChangeListener);

		root.addPreference(category);
		category.addPreference(useSystemFont);
		category.addPreference(fontStyle);
		category.addPreference(systemFonts);
		category.addPreference(customFonts);
		return category;
	}

	private static PreferenceScreen createTypefacePreferenceScreen(final Context context, final TypefacePreference.IntentLauncher launcher,
			final PreferenceManager pref)
	{
		final DatabaseHelper helper = new DatabaseHelper(context);
		final SharedPreferences prefs = pref.getSharedPreferences();

		final PreferenceScreen root = pref.createPreferenceScreen(context);
		final LanguageLocale vernicular = DatabaseFunctions.getVernicular(context, helper.getRead());
		addTypefaceCategory(context, launcher, prefs, root, vernicular);

		final Cursor cursor = helper.getRead().query("books", new String[] { "book_language" }, null, null, "book_language", null, null);
		while(cursor.moveToNext())
		{
			addTypefaceCategory(context, launcher, prefs, root, new LanguageLocale(context, cursor.getString(0)));
		}
		cursor.close();

		helper.close();
		return root;
	}

	private static void populateLanguageList(final Context context, final ListPreference languageList)
	{
		final DatabaseHelper database = new DatabaseHelper(context);
		final Cursor cursor = database.getRead().query("translations", new String[] { "translation_language" }, null, null, "translation_language", null, null);

		final CharSequence[] entries = new CharSequence[cursor.getCount()];
		final CharSequence[] entryvalues = new CharSequence[cursor.getCount()];

		int i = 0;
		while(cursor.moveToNext())
		{
			final String language = cursor.getString(cursor.getColumnIndex("translation_language"));
			entryvalues[i] = language;
			final String entryname = new LanguageLocale(context, language).getDisplayLanguage();
			entries[i++] = entryname == null ? language : entryname;
		}
		cursor.close();
		database.close();
		languageList.setEntries(entries);
		languageList.setEntryValues(entryvalues);
	}

	private static void populateSequenceList(final Context context, final ListPreference sequenceList)
	{
		final DatabaseHelper database = new DatabaseHelper(context);
		final Cursor cursor = database.getRead().query("filing", new String[] { "filing_sequence" }, null, null, "filing_sequence", null, null);

		final CharSequence[] entries = new CharSequence[cursor.getCount()];
		final CharSequence[] entryvalues = new CharSequence[cursor.getCount()];

		int i = 0;
		while(cursor.moveToNext())
		{
			final int sequence = cursor.getInt(cursor.getColumnIndex("filing_sequence"));
			entryvalues[i] = "" + sequence;
			final byte[] array = Sequence.decodeSequence(sequence);
			final String[] entrynames = new String[Sequence.data.length];
			for(int j = 0; j < entrynames.length; ++j)
				entrynames[j] = context.getString(Sequence.getStringId(array[j]));
			entries[i++] = StringUtils.join(entrynames, " \u2192 ");
		}
		cursor.close();
		database.close();
		sequenceList.setEntries(entries);
		sequenceList.setEntryValues(entryvalues);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) return;
		addPreferencesFromResource(R.xml.preference_old);
	}

	/*
	 * private interface OnActivityResultListener { public void onActivityResult(int requestCode, int resultCode, Intent data); }
	 * 
	 * List<OnActivityResultListener> listeners = new LinkedList<OnActivityResultListener>(); public void addOnActivityResultListener(OnActivityResultListener
	 * listener) { listeners.add(listener); }
	 * 
	 * public void onActivityResult(int requestCode, int resultCode, Intent data) { for(OnActivityResultListener listener : listeners)
	 * listener.onActivityResult(requestCode, resultCode, data); if(databasePreference != null) { if(databasePreference.onActivityResult(requestCode,
	 * resultCode, data)) return; } super.onActivityResult(requestCode, resultCode, data); }
	 */

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				final Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
