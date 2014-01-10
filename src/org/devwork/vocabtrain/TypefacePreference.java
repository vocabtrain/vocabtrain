package org.devwork.vocabtrain;

import java.io.File;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TypefacePreference extends DialogPreference implements PreferenceManager.OnActivityResultListener
{

	public interface IntentLauncher
	{
		public void addActivityResultListener(TypefacePreference pref);

		public void launch(Intent intent, int result);
	}

	private IntentLauncher intentlauncher = null;
	private TextView label;
	private EditText filename;

	public TypefacePreference(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.typeface_preference);
		setDialogTitle(context.getString(R.string.pref_typeface_title));

	}

	@Override
	public boolean onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		if(requestCode == Constants.REQUEST_FILEMANAGER_FOR_TYPEFACE && data != null)
		{
			filename.setText(data.getData().toString().replace("file://", ""));
			setTypeface();
			return true;
		}
		return false;
	}

	@Override
	protected View onCreateDialogView()
	{
		final View v = super.onCreateDialogView();
		filename = (EditText) v.findViewById(R.id.typeface_filename);
		label = (TextView) v.findViewById(R.id.typeface_label);
		final Button selectButton = (Button) v.findViewById(R.id.typeface_selectbutton);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		filename.setText(prefs.getString(getKey(), new File(Environment.getExternalStorageDirectory(), "sample.ttf").toString()));

		if(new File(filename.getText().toString()).isFile())
			setTypeface();

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
					final Intent intent = DatabaseFunctions.getFileManagerIntent(getContext(), getContext().getString(R.string.pref_typeface_file_title),
							getContext().getString(R.string.button_load), new File(filename.getText().toString()), DatabaseFunctions.PICK_FILE);
					if(intentlauncher != null) intentlauncher.launch(intent, Constants.REQUEST_FILEMANAGER_FOR_TYPEFACE);
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
		if(positiveResult)
		{
			final Editor edit = prefs.edit();
			edit.putString(getKey(), filename.getText().toString());
			edit.commit();
		}
	}

	public void setIntentLaunchListener(final IntentLauncher launcher)
	{
		this.intentlauncher = launcher;
		launcher.addActivityResultListener(this);
	}

	private void setTypeface()
	{
		try
		{
			label.setTypeface(Typeface.createFromFile(filename.getText().toString()));
		}
		catch(final RuntimeException e)
		{
			final Toast toast = Toast.makeText(getContext(), getContext().getString(R.string.typeface_missing, filename.getText().toString()),
					Toast.LENGTH_LONG);
			toast.show();
		}
	}

}
