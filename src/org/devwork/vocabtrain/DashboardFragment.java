package org.devwork.vocabtrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.devwork.vocabtrain.sync.SyncActivity;
import org.openintents.intents.AboutIntents;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.android.ActionBarFragmentActivity;

public class DashboardFragment extends Fragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".DashboardFragment";
	public static final String TAG_SELECTION = TAG + "_selection";
	public static final String TAG_TRAINING = TAG + "_training";
	public static final String TAG_DATABASE = TAG + "_database";

	private Button ontableview;

	private Button onflashcard;
	private Button onshowcard;
	private Button ontextcard;
	private TextView cardscount;
	private Button button_algo;
	private Button button_filinglist;
	private SeekBar clearselection;
	private TextView clearselection_label;

	//private ImageView language_image;
	private int sequence;

	private ToggleButton subbutton_selection;

	private ToggleButton subbutton_database;
	private ToggleButton subbutton_training;
	private ProgressBar progressbar;
	private Button button_sequence;
	private Button button_bluetooth;
	private boolean useCustomSkin;

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode)
        {  
            case Constants.REQUEST_ENABLE_BLUETOOTH:
                if(resultCode == Activity.RESULT_OK)
                {
                	button_bluetooth.performClick();
                }
                break;
        }
		updateCardsCount();
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		final MenuItem refresh = menu.findItem(R.id.menu_refresh);
		inflater.inflate(R.menu.dashboard, menu);
		if(refresh != null) menu.removeItem(R.id.menu_refresh);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) IncompatibleFunctions.createSearchView(getActivity(), menu, R.id.dashboard_menu_search);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		useCustomSkin = prefs.getBoolean("custom_skin", false);

		final View v = inflater.inflate(R.layout.dashboard_fragment, container, false);

		final Button button_booklist = (Button) v.findViewById(R.id.dashboard_button_books);
		button_booklist.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				((MainActivity) getActivity()).showFragment(new FragmentCreator()
				{
					@Override
					public Fragment create()
					{
						return new BooklistFragment();
					}

					@Override
					public boolean equals(final Fragment fragment)
					{
						return (fragment instanceof BooklistFragment);
					}

					@Override
					public String getTag()
					{
						return BooklistFragment.TAG;
					}

					@Override
					public Fragment update(final Fragment fragment)
					{
						return fragment;
					}

				});
			}
		});
		button_filinglist = (Button) v.findViewById(R.id.dashboard_button_filing);
		button_filinglist.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				((MainActivity) getActivity()).showFragment(new FragmentCreator()
				{
					@Override
					public Fragment create()
					{
						return new FilinglistFragment();
					}

					@Override
					public boolean equals(final Fragment fragment)
					{
						return (fragment instanceof FilinglistFragment);
					}

					@Override
					public String getTag()
					{
						return FilinglistFragment.TAG;
					}

					@Override
					public Fragment update(final Fragment fragment)
					{
						return fragment;
					}

				});
			}

		});

		cardscount = (TextView) v.findViewById(R.id.dashboard_cardscount);

		button_algo = (Button) v.findViewById(R.id.dashboard_button_algo);

		button_algo.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final ImportSelectionFromAlgorithmLoader task = new ImportSelectionFromAlgorithmLoader(getActivity());
				task.execute((Void[]) null);
			}
		});
		clearselection_label = (TextView) v.findViewById(R.id.dashboard_clear_selection_label);
		clearselection = (SeekBar) v.findViewById(R.id.dashboard_clear_selection);
		clearselection.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			private boolean sliding = false;

			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser)
			{
				if(!fromUser) return;
				final int max = seekBar.getMax();
				if(!sliding && (seekBar.getProgress() > max / 3))
				seekBar.setProgress(0);
				else
				sliding = true;
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar)
			{
				clearselection_label.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onStopTrackingTouch(final SeekBar seekBar)
			{
				clearselection_label.setVisibility(View.VISIBLE);
				final int max = seekBar.getMax();
				if(sliding && max < seekBar.getProgress() + max / 10)
				{
					final DatabaseHelper dbh = new DatabaseHelper(getActivity());
					dbh.clearSelection();
					updateCardsCount();
					dbh.close();
				}
				else
				seekBar.setProgress(0);
				sliding = false;
			}
		});
		clearselection.setThumb(getActivity().getResources().getDrawable(R.drawable.btn_slidelockthumb));

		//language_image = (ImageView) v.findViewById(R.id.dashboard_language_image);

		ontableview = (Button) v.findViewById(R.id.dashboard_ontable);
		onflashcard = (Button) v.findViewById(R.id.dashboard_onflashcard);
		onshowcard = (Button) v.findViewById(R.id.dashboard_onshowcard);
		ontextcard = (Button) v.findViewById(R.id.dashboard_ontextcard);

		ontableview.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{

				((MainActivity) getActivity()).showFragment(new FragmentCreator()
				{
					@Override
					public Fragment create()
					{
						return SelectionTableViewFragment.createInstance(false);
					}

					@Override
					public boolean equals(final Fragment fragment)
					{
						return (fragment instanceof SelectionTableViewFragment);
					}

					@Override
					public String getTag()
					{
						return SelectionTableViewFragment.TAG;
					}

					@Override
					public Fragment update(final Fragment fragment)
					{
						((SelectionTableViewFragment) fragment).onRefresh();
						return fragment;
					}

				});
			}

		});
		onflashcard.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(getActivity(), TrainingActivity.class);
				intent.putExtra("fragment", FlashCardFragment.TAG);
				DashboardFragment.this.startActivityForResult(intent, 0);
			}

		});

		onshowcard.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(getActivity(), TrainingActivity.class);
				intent.putExtra("fragment", ShowCardFragment.TAG);
				DashboardFragment.this.startActivityForResult(intent, 0);
			}

		});
		ontextcard.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(getActivity(), TrainingActivity.class);
				intent.putExtra("fragment", TextCardFragment.TAG);
				DashboardFragment.this.startActivityForResult(intent, 0);
			}

		});
		/*
		 * Button button_search = (Button) v.findViewById(R.id.dashboard_button_search); button_search.setOnClickListener( new OnClickListener(){
		 * 
		 * @Override public void onClick(View v) { getActivity().onSearchRequested(); }
		 * 
		 * });
		 */

		final Button button_createsearch = (Button) v.findViewById(R.id.dashboard_button_createsearch);

		button_createsearch.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final SearchTableLoader task = new SearchTableLoader(getActivity());
				task.execute((Void[]) null);
			}
		});

		final Button button_sync = (Button) v.findViewById(R.id.dashboard_button_sync);
		button_sync.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(getActivity(), SyncActivity.class);
				DashboardFragment.this.startActivityForResult(intent, 0);
			}

		});
		button_sequence = (Button) v.findViewById(R.id.dashboard_button_sequence);
		button_sequence.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final FragmentManager manager = getActivity().getSupportFragmentManager();
				final SequenceChooserDialog dialog = new SequenceChooserDialog();
				dialog.show(manager, SequenceChooserDialog.TAG);
			}

		});

		final Button button_import = (Button) v.findViewById(R.id.dashboard_button_import);
		final Button button_export = (Button) v.findViewById(R.id.dashboard_button_export);
		button_export.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				((MainActivity) getActivity()).showPorter(true);
			}
		});
		button_import.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				((MainActivity) getActivity()).showPorter(false);
			}
		});
		
		button_bluetooth = (Button) v.findViewById(R.id.dashboard_button_bluetooth);
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.DONUT)
		{
			button_bluetooth.setEnabled(false);
			button_bluetooth.setVisibility(View.GONE);
			v.findViewById(R.id.dashboard_button_dummyhelper).setVisibility(View.GONE);
		}
		else
		button_bluetooth.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				if(Build.VERSION.SDK_INT > Build.VERSION_CODES.DONUT)
					new BluetoothSync(getActivity());
			}
		});

		final View sub_selection = v.findViewById(R.id.dashboard_sub_selection);
		final View sub_database = v.findViewById(R.id.dashboard_sub_database);
		final View sub_training = v.findViewById(R.id.dashboard_sub_training);

		subbutton_selection = (ToggleButton) v.findViewById(R.id.dashboard_drawerbutton_selection);
		subbutton_database = (ToggleButton) v.findViewById(R.id.dashboard_drawerbutton_database);
		subbutton_training = (ToggleButton) v.findViewById(R.id.dashboard_drawerbutton_training);
		if(subbutton_selection != null)
		{
			subbutton_selection.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View view)
				{
					
					sub_selection.setVisibility(View.VISIBLE);
					sub_database.setVisibility(View.INVISIBLE);
					sub_training.setVisibility(View.INVISIBLE);
					subbutton_selection.setChecked(true);
					subbutton_database.setChecked(false);
					subbutton_training.setChecked(false);
					if(useCustomSkin)
					setViewBackground(v, BACKGROUND_COLOR_SELECTION, "selection");
					else
					v.setBackgroundColor(BACKGROUND_COLOR_SELECTION);
				}
			});
			sub_selection.setVisibility(View.VISIBLE);
			subbutton_selection.setChecked(true);
			if(prefs.getBoolean("tourtips", true)) TourTipsDialog.createInstance(TAG_SELECTION, getActivity());

		}

		if(subbutton_database != null) subbutton_database.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				if(prefs.getBoolean("tourtips", true)) TourTipsDialog.createInstance(TAG_DATABASE, getActivity());

				sub_selection.setVisibility(View.INVISIBLE);
				sub_database.setVisibility(View.VISIBLE);
				sub_training.setVisibility(View.INVISIBLE);
				subbutton_selection.setChecked(false);
				subbutton_database.setChecked(true);
				subbutton_training.setChecked(false);

				if(useCustomSkin)
				setViewBackground(v, BACKGROUND_COLOR_DATABASE, "database");
				else
				v.setBackgroundColor(BACKGROUND_COLOR_DATABASE);
			}
		});
		else sub_database.setVisibility(View.VISIBLE);
		if(subbutton_training != null) subbutton_training.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View view)
			{
				if(prefs.getBoolean("tourtips", true)) TourTipsDialog.createInstance(TAG_TRAINING, getActivity());
				
				sub_selection.setVisibility(View.INVISIBLE);
				sub_database.setVisibility(View.INVISIBLE);
				sub_training.setVisibility(View.VISIBLE);
				subbutton_selection.setChecked(false);
				subbutton_database.setChecked(false);
				subbutton_training.setChecked(true);

				if(useCustomSkin)
				setViewBackground(v, BACKGROUND_COLOR_TRAINING, "training");
				else
				v.setBackgroundColor(BACKGROUND_COLOR_TRAINING);
			}
		});
		else 
		{
			if(prefs.getBoolean("tourtips", true) && prefs.getBoolean("license_accepted", false))
				TourTipsDialog.createInstance(TAG, getActivity());
			sub_training.setVisibility(View.VISIBLE);
		}
		
		progressbar = (ProgressBar) v.findViewById(R.id.dashboard_progress);
		return v;

	}
	public static final int BACKGROUND_COLOR_TRAINING = Color.argb(255, 0xff, 0xd9, 0xd9);
	public static final int BACKGROUND_COLOR_SELECTION =  Color.argb(255, 0xd9, 0xff, 0xd9);
	public static final int BACKGROUND_COLOR_DATABASE = Color.argb(255, 0xd9, 0xdc, 0xff);

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		// Handle item selection
		switch(item.getItemId())
		{
			case R.id.dashboard_menu_help:
				try
				{
					final Intent intent = new Intent(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);
					startActivityForResult(intent, 1);
				}
				catch(final ActivityNotFoundException e)
				{
					try
					{
						final BufferedReader bf = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.license_short)));
						final StringBuilder sb = new StringBuilder();
						while(true)
						{
							final String s = bf.readLine();
							if(s == null) break;
							sb.append(s);
							sb.append("\n");
						}
						bf.close();

						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(sb.toString()).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								dialog.dismiss();
							}
						}).setNeutralButton(getString(R.string.button_donate), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=RCRXP4DYESAZC"));
								startActivity(intent);
							}
						});
						final AlertDialog alert = builder.create();
						alert.show();
					}
					catch(final IOException io)
					{

					}
				}
				return true;
			case R.id.dashboard_menu_search:
				getActivity().onSearchRequested();
				return true;
			case R.id.menu_refresh:
				final Runnable run = new Runnable()
				{
					@Override
					public void run()
					{
						OnRefresh();
					}
				};
				if(getActivity() instanceof ActionBarFragmentActivity)
				{
					final ActionBarFragmentActivity activity = (ActionBarFragmentActivity) getActivity();
					activity.onRefresh(run);
				}
				else run.run();
				return false;
				/*
				 * case R.id.dashboard_menu_sync: { new DatabaseSync(getActivity()).execute((Void[])null); } return true;
				 */
			case R.id.dashboard_menu_preferences:
			{
				final Intent intent = new Intent(getActivity(), (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) ? SettingsFragmentActivity.class
						: SettingsActivity.class);
				startActivity(intent);
			}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void OnRefresh()
	{
		updateCardsCount();
		
		//language_image.setImageResource(DatabaseFunctions.getVernicular(getActivity()).getFlagId());
		if(subbutton_selection != null)
		{
			if(subbutton_selection.isChecked()) subbutton_selection.performClick();
			if(subbutton_database.isChecked()) subbutton_database.performClick();
			if(subbutton_training.isChecked()) subbutton_training.performClick();

		}
	}

	/*
	 * 
	 * @Override public void onActivityCreated (Bundle savedInstanceState) { super.onActivityCreated(savedInstanceState); setRetainInstance(true); }
	 */

	@Override
	public void onResume()
	{
		super.onResume();
		OnRefresh();
		progressbar.setVisibility(View.GONE);
		CharacterTranslator.getCharacterTranslator().hookProgressBar(progressbar);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
		sequence = DatabaseFunctions.getSequence(getActivity());
}
	private void setViewBackground(final View view, final int color, final String name)
	{
		setViewBackground(getActivity(), view, color, name);
	}

	
	
	public static void setViewBackground(Activity activity, final View view, final int color, final String name)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		String customSkinDirectory = prefs.getString("custom_skin_directory", Environment.getExternalStorageDirectory().toString() + "/skin");
		File file = new File(customSkinDirectory + File.separator + name + ".jpg");
		if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && activity.findViewById(R.id.main_layout_second) == null)
		{
			final File portrait = new File(customSkinDirectory + File.separator + name + "_land.jpg");
			if(portrait.isFile()) file = portrait;
		}
		if(!file.isFile()) return;
		final Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
		if(bitmap == null) return;
		final ColorDrawable color_drawable = new ColorDrawable(color);
		final BitmapDrawable bitmap_drawable = new BitmapDrawable(view.getContext().getResources(), bitmap);
		if(bitmap_drawable != null)
		{
			bitmap_drawable.setAlpha(50);
			view.setBackgroundDrawable(new LayerDrawable(new Drawable[] { color_drawable, bitmap_drawable }));
		}
	}

	public void updateCardsCount()
	{
		if(cardscount == null) return;
		final DatabaseHelper database = new DatabaseHelper(getActivity());

		final int size = database.getSelectionLength();
		cardscount.setText(getActivity().getResources().getQuantityString(R.plurals.cards_selected, size, size));
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		
		
		Drawable d = getResources().getDrawable(DatabaseFunctions.getVernicular(getActivity()).getFlagId());
		d.setBounds(0,0, (int) (dm.density*40), (int) (((float)d.getIntrinsicHeight()/d.getIntrinsicWidth())* dm.density*40) );
		cardscount.setCompoundDrawables(d, null, null, null);
		cardscount.setCompoundDrawablePadding(5);
		
		if(size == 0)
		{
			ontableview.setEnabled(false);
			onflashcard.setEnabled(false);
			onshowcard.setEnabled(false);
			ontextcard.setEnabled(false);
			clearselection.setEnabled(false);
			clearselection.setVisibility(View.GONE);
			clearselection_label.setVisibility(View.GONE);
			if(subbutton_training != null) subbutton_training.setEnabled(false);
		}
		else
		{
			ontableview.setEnabled(true);
			onflashcard.setEnabled(true);
			onshowcard.setEnabled(true);
			ontextcard.setEnabled(true);
			clearselection.setEnabled(true);
			clearselection.setProgress(0);
			clearselection.setVisibility(View.VISIBLE);
			clearselection_label.setVisibility(View.VISIBLE);
			if(subbutton_training != null) subbutton_training.setEnabled(true);
		}
		{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			if(prefs.getBoolean("use_default_sequence", true)) button_sequence.setEnabled(false);
			else
			{	
				try
				{
					final Cursor cursor = database.getRead().query("filing", new String[] { "filing_sequence" }, null, null, "filing_sequence", null, null);
					button_sequence.setEnabled(cursor.getCount() > 1);
					cursor.close();	
				}
				catch(final SQLiteException e)
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(getString(R.string.database_error, e.getMessage())).setTitle(getString(R.string.database_error_title))
							.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
									getActivity().finish();
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();
				}
			}
		}

		{
			final Cursor c = database.getRead().rawQuery("SELECT COUNT(*) FROM `filing` WHERE filing_sequence = ?", new String[] { "" + sequence });
			if(c.moveToFirst() && c.getLong(0) == 0)
			{
				button_algo.setEnabled(false);
				button_filinglist.setEnabled(false);
			}
			else
			{
				button_algo.setEnabled(true);
				button_filinglist.setEnabled(true);
			}
			c.close();
		}
		database.close();

	}

}
