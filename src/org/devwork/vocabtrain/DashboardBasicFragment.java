package org.devwork.vocabtrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.devwork.vocabtrain.sync.SyncActivity;
import org.openintents.intents.AboutIntents;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ProgressBar;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.example.android.ActionBarFragmentActivity;

public class DashboardBasicFragment extends DatabaseFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".DashboardBasicFragment";
	private String customSkinDirectory;
	private boolean useCustomSkin;
	private Button ontableview;
	private Button onflashcard;
	private Button onshowcard;
	private Button ontextcard;
	private ProgressBar progressbar;

	private class SelectAdapter extends SimpleCursorTreeAdapter implements OnChildClickListener
	{
		private final long[] book_ids;

		public SelectAdapter(Cursor cursor, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, int lastChildLayout, String[] childFrom, int[] childTo)
		{
			super(getActivity(), cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, lastChildLayout, childFrom, childTo);
			if(cursor.getCount() == 0)
			{
				book_ids = null;
				return;
			}
			final int size = cursor.getCount();
			book_ids = new long[size];
			for(int i = 0; i < cursor.getCount(); ++i)
			{
				cursor.moveToPosition(i);
				final long book_id = cursor.getLong(cursor.getColumnIndex("_id"));
				book_ids[i] = book_id;
			}
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor)
		{
			Cursor cursor = getDatabaseHelper().getRead().rawQuery("SELECT `chapters`.`_id` AS _id, chapter_volume FROM `chapters` WHERE `chapter_book_id` = ?", new String[] { groupCursor.getString(groupCursor.getColumnIndex("_id")) });
			listCursors.add(cursor);
			return cursor;
		}

		@Override
		public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
		{
			final Cursor chapters = getDatabaseHelper().getRead().query("chapters", new String[] { "_id" }, "chapter_book_id = ?", new String[] { "" + book_ids[groupPosition] }, null, null, " _id ASC");
			if(chapters == null || !chapters.moveToPosition(childPosition)) return false;
			getDatabaseHelper().clearSelection();
			
			ImportSelectionFromChaptersLoader importer = new ImportSelectionFromChaptersLoader(getActivity(), new Integer[] { chapters.getInt(0) });
			importer.setOnFinishListener(new OnFinishListener() {

				@Override
				public void onFinish() {
					state = STATE_TRAINING;
					updateState();
				}
				
			});
			importer.execute();
			
			return true;
		}
		
	}
	
	
	private SelectAdapter adapter;
	private ExpandableListView listView;
	private Cursor listCursor = null;
	private final List<Cursor> listCursors = new LinkedList<Cursor>();
	
	
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		final MenuItem refresh = menu.findItem(R.id.menu_refresh);
		inflater.inflate(R.menu.dashboard, menu);
		if(refresh != null) menu.removeItem(R.id.menu_refresh);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) IncompatibleFunctions.createSearchView(getActivity(), menu, R.id.dashboard_menu_search);

		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	private void onRefresh()
	{
		if(listCursor != null) listCursor.close();
		listCursor = getDatabaseHelper().getBooks();
		adapter = new SelectAdapter(listCursor, R.layout.booklist_group_collapsed, R.layout.booklist_group_expanded, new String[] { "book_name" }, new int[] { R.id.booklist_group }, android.R.layout.simple_list_item_1, R.layout.dashboardbasic_listchild, new String[] { "chapter_volume"}, new int[] { android.R.id.text1 });
		listView.setAdapter(adapter);
		listView.setOnChildClickListener(adapter);
		listView.setVisibility(View.INVISIBLE);
		state = STATE_INIT;
		updateState();
	}
	@Override
	public void onPause()
	{
		super.onPause();
		if(listCursor != null)
		{
			listCursor.close();
			listCursor = null;
		}
		if(!listCursors.isEmpty())
		{
			Iterator<Cursor> it = listCursors.iterator();
			while(it.hasNext())
			{
				it.next().close();
			}
			listCursors.clear();
		}
	}
	
	private View sub_training;
	private View sub_main;
	private TextView caption;
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		useCustomSkin = prefs.getBoolean("custom_skin", false);
		customSkinDirectory = prefs.getString("custom_skin_directory", Environment.getExternalStorageDirectory().toString() + "/skin");

		final View v = inflater.inflate(R.layout.dashboardbasic_fragment, container, false);
		
		v.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.e("TAGG", event.getAction() + " " + keyCode + " - " + event.getRepeatCount()); 
			    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			    	if(state > 0)
			    	{
			    		--state;
			    		updateState();
			    		return true;
			    	}
			    }
				return false;
			}
			
		});
		v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        
		listView = (ExpandableListView) v.findViewById(R.id.dashboardbasic_booklist);
		listView.setClickable(true);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
		
		caption = (TextView) v.findViewById(R.id.dashboardbasic_caption);
		sub_training = v.findViewById(R.id.dashboard_sub_training);
		sub_main = v.findViewById(R.id.dashboardbasic_sub_main);
		final Button button_training = (Button) v.findViewById(R.id.dashboardbasic_button_training);
		button_training.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				state = STATE_SELECTION;
				updateState();
			}
		});
		
		
		
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
				DashboardBasicFragment.this.startActivityForResult(intent, 0);
			}

		});

		onshowcard.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(getActivity(), TrainingActivity.class);
				intent.putExtra("fragment", ShowCardFragment.TAG);
				DashboardBasicFragment.this.startActivityForResult(intent, 0);
			}

		});
		ontextcard.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(getActivity(), TrainingActivity.class);
				intent.putExtra("fragment", TextCardFragment.TAG);
				DashboardBasicFragment.this.startActivityForResult(intent, 0);
			}

		});
		
		final Button button_sync = (Button) v.findViewById(R.id.dashboardbasic_button_sync);
		button_sync.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				final Intent intent = new Intent(getActivity(), SyncActivity.class);
				DashboardBasicFragment.this.startActivityForResult(intent, 0);
			}

		});

		progressbar = (ProgressBar) v.findViewById(R.id.dashboardbasic_progress);
		return v;

	}

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
	
	private final static int STATE_INIT = 0;
	private final static int STATE_SELECTION = 1;
	private final static int STATE_TRAINING = 2;

	private int state;
	private void updateState()
	{
		switch(state)
		{
			case STATE_INIT:
				sub_main.setVisibility(View.VISIBLE);
				sub_training.setVisibility(View.INVISIBLE);
				listView.setVisibility(View.INVISIBLE);
				caption.setText(R.string.dash_basic_mainmenu);
				caption.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				getView().setBackgroundDrawable(null);
				if(useCustomSkin)
					DashboardFragment.setViewBackground(getActivity(), getView(), Color.WHITE, "database");
				else
				getView().setBackgroundColor(Color.WHITE);
			break;
			case STATE_SELECTION:
				sub_main.setVisibility(View.INVISIBLE);
				sub_training.setVisibility(View.INVISIBLE);
				listView.setVisibility(View.VISIBLE);
				caption.setText(R.string.layout_dash_sub_selection);
				caption.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_dash_selection_books), null, null, null);
				if(useCustomSkin)
					DashboardFragment.setViewBackground(getActivity(), getView(), DashboardFragment.BACKGROUND_COLOR_SELECTION, "selection");
					else
					getView().setBackgroundColor(DashboardFragment.BACKGROUND_COLOR_SELECTION);
			break;
			case STATE_TRAINING:
				listView.setVisibility(View.INVISIBLE);
				sub_main.setVisibility(View.INVISIBLE);
				sub_training.setVisibility(View.VISIBLE);
				caption.setText(R.string.layout_dash_sub_training);
				caption.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_dash_training), null, null, null);
				if(useCustomSkin)
					DashboardFragment.setViewBackground(getActivity(), getView(), DashboardFragment.BACKGROUND_COLOR_TRAINING, "training");
				else
				getView().setBackgroundColor(DashboardFragment.BACKGROUND_COLOR_TRAINING);
			break;
			
			
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
		onRefresh();
		progressbar.setVisibility(View.GONE);
		CharacterTranslator.getCharacterTranslator().hookProgressBar(progressbar);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
}


}
