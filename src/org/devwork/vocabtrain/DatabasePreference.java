package org.devwork.vocabtrain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class DatabasePreference extends DialogPreference implements OnCheckedChangeListener, PreferenceManager.OnActivityResultListener
{

	public interface IntentLauncher
	{
		public void launch(Intent intent, int result);
	}

	private IntentLauncher intentlauncher = null;

	private CheckBox externalize;

	private EditText filename;

	private Button selectButton;

	public DatabasePreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.database_preference);
		setDialogTitle(context.getString(R.string.pref_database_location));
	}

	@Override
	public boolean onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if(requestCode == Constants.REQUEST_FILEMANAGER_FOR_DATABASE && data != null)
		{
			filename.setText(data.getData().toString().replace("file://", ""));
			return true;
		}
		return false;
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
	{
		if(isChecked)
		{
			filename.setEnabled(true);
			selectButton.setEnabled(true);
		}
		else
		{
			filename.setEnabled(false);
			selectButton.setEnabled(false);
		}
	}

	@Override
	protected View onCreateDialogView()
	{
		final View v = super.onCreateDialogView();
		externalize = (CheckBox) v.findViewById(R.id.database_externalize);
		filename = (EditText) v.findViewById(R.id.database_text);
		selectButton = (Button) v.findViewById(R.id.database_selectbutton);

		if(Build.BOARD.equals("zoom2") && Build.BRAND.equals("nook") && Build.DEVICE.equals("zoom2")) // nook touch renders default white text on white
																										// background
		{
			externalize.setTextColor(Color.BLACK);
		}

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		externalize.setChecked(prefs.getBoolean("database_externalize", false));
		filename.setText(prefs.getString("database_filename", new File(Environment.getExternalStorageDirectory(), "vocabtrain.db").toString()));

		externalize.setOnCheckedChangeListener(this);
		onCheckedChanged(externalize, externalize.isChecked());

		if(!DatabaseFunctions.hasFileManager(getContext()))
		{
			selectButton.setEnabled(false);
			selectButton.setVisibility(View.GONE);
		}
		else selectButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				try
				{
					final Intent intent = DatabaseFunctions.getFileManagerIntent(getContext(), getContext().getString(R.string.database_file_title),
							getContext().getString(R.string.button_save), new File(filename.getText().toString()), DatabaseFunctions.PICK_FILE);
					if(intentlauncher != null) intentlauncher.launch(intent, Constants.REQUEST_FILEMANAGER_FOR_DATABASE);
				}
				catch(final ActivityNotFoundException e)
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setMessage(getContext().getString(R.string.missing_oi_filemanager)).setPositiveButton(getContext().getString(android.R.string.ok),
							new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
									dialog.dismiss();
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();
				}
			}

		});

		return v;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean database_externalized = prefs.getBoolean("database_externalize", false);
		if(positiveResult)
		{
			try
			{

				if(externalize.isChecked())
				{
					if(database_externalized && filename.getText().toString().equals(prefs.getString("database_filename", ""))) return;
					final File dest = new File(filename.getText().toString());
					if(dest.exists())
					{
						final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage(String.format(getContext().getString(R.string.file_override), dest))
								.setPositiveButton(getContext().getString(R.string.button_override), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(final DialogInterface dialog, final int id)
									{
										dest.delete();
										dialog.dismiss();
										onDialogClosed(true);
									}
								}).setNeutralButton(getContext().getString(R.string.button_exchange), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(final DialogInterface dialog, final int id)
									{
										if(!database_externalized) new File(Constants.DATABASE_PATH).delete();
										final Editor edit = prefs.edit();
										edit.putBoolean("database_externalize", true);
										edit.putString("database_filename", filename.getText().toString());
										edit.commit();
										dialog.dismiss();
									}
								}).setNegativeButton(getContext().getString(R.string.button_cancel), new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(final DialogInterface dialog, final int id)
									{
										dialog.dismiss();
									}
								});
						final AlertDialog alert = builder.create();
						alert.show();
						return;
					}
					final File src = new File(database_externalized ? prefs.getString("database_filename", null) : Constants.DATABASE_PATH);
					DatabaseFunctions.copyFile(new FileInputStream(src), new FileOutputStream(dest));
					src.delete();
				}
				else if(database_externalized)
				{
					Log.e("Pref", "a");
					final File dest = new File(Constants.DATABASE_PATH);
					final File src = new File(prefs.getString("database_filename", null));
					DatabaseFunctions.copyFile(new FileInputStream(src), new FileOutputStream(dest));
				}
				final Editor edit = prefs.edit();
				edit.putBoolean("database_externalize", externalize.isChecked());
				edit.putString("database_filename", filename.getText().toString());
				edit.commit();

			}
			catch(final IOException e)
			{

			}
		}
	}

	public void setIntentLaunchListener(final IntentLauncher launcher)
	{
		this.intentlauncher = launcher;
	}

}
