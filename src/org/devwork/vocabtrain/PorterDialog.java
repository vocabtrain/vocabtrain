package org.devwork.vocabtrain;

import java.io.File;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PorterDialog extends DialogFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".PorterActivity";

	public static PorterDialog createInstance(final boolean export)
	{
		final PorterDialog dialog = new PorterDialog();
		final Bundle bundle = new Bundle();
		bundle.putBoolean("export", export);
		dialog.setArguments(bundle);
		return dialog;
	}

	private EditText edittext;

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{

		// Log.e(TAG, "" + requestCode + " " + resultCode + " " + data);
		if(requestCode == Constants.REQUEST_FILEMANAGER_FOR_PORTING && data != null)
		{
			edittext.setText(data.getData().getPath()); // .toString().replace("file://", ""));
		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View v = inflater.inflate(R.layout.porter_dialog, container, false);

		final boolean export = getArguments().getBoolean("export");

		getDialog().setTitle(export ? getString(R.string.porter_export_desc) : getString(R.string.porter_import_desc));
		edittext = (EditText) v.findViewById(R.id.porter_text);
		final Button selectButton = (Button) v.findViewById(R.id.porter_selectbutton);
		final Button actionButton = (Button) v.findViewById(R.id.porter_actionbutton);
		edittext.setText(new File(Environment.getExternalStorageDirectory().toString(), "export.db").toString());

		actionButton.setText(export ? getString(R.string.porter_button_export) : getString(R.string.porter_button_import));
		actionButton.setOnClickListener(new View.OnClickListener()
		{

			abstract class PorterTask extends AsyncTask<Void, Void, Void> implements OnCancelListener
			{
				protected boolean successful = false;
				protected String errormsg = null;
				protected final String filename;

				protected PorterTask(final String filename)
				{
					this.filename = filename;
				}

				@Override
				public void onCancel(final DialogInterface dialog)
				{
					cancel(true);
				}

				@Override
				protected void onCancelled()
				{
					if(getActivity() == null) return;
					final Toast toast = Toast.makeText(getActivity(), "Operation cancelled", Toast.LENGTH_LONG);
					toast.show();
					dismiss();
				}

				@Override
				protected void onPostExecute(final Void result)
				{
					progressDialog.dismiss();
					if(!successful)
					{
						if(getActivity() == null) return;
						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(errormsg).setTitle(getString(R.string.error_title)).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
						{
							public void onClick(final DialogInterface dialog, final int id)
							{
								dialog.dismiss();
							}
						});
						final AlertDialog alert = builder.create();
						alert.show();
					}
					else
					{
						if(getActivity() == null) return;
						final Toast toast = Toast.makeText(getActivity(), export ? getString(R.string.porter_export_success) : getString(R.string.porter_import_success), Toast.LENGTH_SHORT);
						toast.show();
					}
					dismiss();

					final FragmentManager manager = getActivity().getSupportFragmentManager();
					final Fragment fragment = manager.findFragmentByTag(DashboardFragment.TAG);
					if(fragment != null && fragment instanceof DashboardFragment)
					{
						((DashboardFragment) fragment).updateCardsCount();
					}

				}

				@Override
				protected void onPreExecute()
				{
					progressDialog.setOnCancelListener(this);
					progressDialog.show(getActivity().getSupportFragmentManager(), TAG);
				}

			}

			class ExportTask extends PorterTask
			{
				ExportTask(final String filename)
				{
					super(filename);
				}

				@Override
				protected Void doInBackground(final Void... arg0)
				{
					try
					{
						final File f = new File(filename);
						if(f.exists() && !f.delete())
						{
							errormsg = String.format(getString(R.string.file_fail_delete), filename);
							return null;
						}
						if(!f.createNewFile())
						{
							errormsg = String.format(getString(R.string.file_fail_write), filename);
							return null;
						}

						final DatabaseHelper dbh = new DatabaseHelper(getActivity());
						DatabaseFunctions.copyData(dbh.getFilename(), filename);
						dbh.close();
						successful = true;
					}
					catch(final Throwable e)
					{
						errormsg = e.toString();
					}
					return null;
				}

			}

			class ImportTask extends PorterTask
			{
				ImportTask(final String filename)
				{
					super(filename);
				}

				@Override
				protected Void doInBackground(final Void... arg0)
				{
					try
					{
						final File f = new File(filename);
						if(!f.exists())
						{
							errormsg = String.format(getString(R.string.file_fail_exists), filename);
							return null;
						}
						if(!f.canRead())
						{
							errormsg = String.format(getString(R.string.file_fail_read), filename);
							return null;
						}
						final DatabaseHelper dbh = new DatabaseHelper(getActivity());
						DatabaseFunctions.copyData(filename, dbh.getFilename());
						final SQLiteDatabase dbw = dbh.getWritableDatabase();
						DatabaseFunctions.cleanOrphans(dbw);
						dbw.close();
						dbh.close();
						successful = true;
					}
					catch(final Throwable e)
					{
						errormsg = e.toString();
						final DatabaseHelper dbh = new DatabaseHelper(getActivity());
						final SQLiteDatabase db = dbh.getWritableDatabase();
						DatabaseFunctions.createTables(db);
						db.close();
						dbh.close();
					}
					return null;
				}
			}

			private ProgressDialogFragment progressDialog;
			boolean askedForOverride = false;

			@Override
			public void onClick(final View view)
			{
				OnClick();
			}

			private void OnClick()
			{
				final String filename = edittext.getText().toString();

				if(export && !askedForOverride)
				{
					final File f = new File(filename);
					if(f.exists())
					{
						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(String.format(getString(R.string.file_override), filename)).setTitle(getString(R.string.porter_export_desc)).setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener()
						{
							public void onClick(final DialogInterface dialog, final int id)
							{
								askedForOverride = true;
								OnClick();
							}
						}).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
						{
							public void onClick(final DialogInterface dialog, final int id)
							{
							}
						});
						final AlertDialog alert = builder.create();
						alert.show();
						return;
					}
				}
				askedForOverride = false;

				progressDialog = ProgressDialogFragment.createInstance(export ? getString(R.string.porter_export) : getString(R.string.porter_import), export ? getString(R.string.porter_export_longdesc) : getString(R.string.porter_import_longdesc), true);

				final PorterTask task = export ? new ExportTask(filename) : new ImportTask(filename);
				task.execute((Void[]) null);
			}
		});

		if(!DatabaseFunctions.hasFileManager(getActivity()))
		{
			selectButton.setEnabled(false);
			selectButton.setVisibility(View.GONE);
		}
		else
			selectButton.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(final View v)
				{
					try
					{
						final Intent intent = DatabaseFunctions.getFileManagerIntent(getActivity(), export ? getString(R.string.porter_file_export_title) : getString(R.string.porter_file_import_title), export ? getString(R.string.button_save) : getString(R.string.button_load), new File(edittext.getText().toString()), DatabaseFunctions.PICK_FILE);
						startActivityForResult(intent, Constants.REQUEST_FILEMANAGER_FOR_PORTING);
					}
					catch(final ActivityNotFoundException e)
					{
						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(getString(R.string.missing_oi_filemanager)).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
						{
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

}
