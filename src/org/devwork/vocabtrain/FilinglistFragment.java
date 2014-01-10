package org.devwork.vocabtrain;

import java.util.LinkedList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.android.ActionBarFragmentActivity;

public class FilinglistFragment extends DatabaseFragment
{

	public static final String TAG = Constants.PACKAGE_NAME + ".FilinglistFragment";

	@Override
	public void onStart()
	{
		super.onStart();
		((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
		TourTipsDialog.createInstance(TAG, getActivity());
	}

	private class filinglistAdapter extends SimpleCursorAdapter implements OnItemClickListener
	{

		private final boolean[] check_states;
		private final long[] ranks;

		public filinglistAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
		{
			super(context, layout, c, from, to);
			check_states = new boolean[c.getCount()];
			ranks = new long[c.getCount()];
			if(c.moveToFirst())
			{
				for(int i = 0; i < c.getCount(); ++i)
				{
					ranks[i] = c.getLong(c.getColumnIndex("filing_rank"));
					c.moveToNext();
				}
			}
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			View view = super.getView(position, convertView, parent);

			TextView tcaption = (TextView) view.findViewById(R.id.filinglist_child_caption);
			tcaption.setText(String.format(getActivity().getString(R.string.layout_filing_caption), tcaption.getText().toString()));

			TextView tcards = (TextView) view.findViewById(R.id.filinglist_child_count);
			int icards = Integer.parseInt(tcards.getText().toString());
			tcards.setText(getActivity().getResources().getQuantityString(R.plurals.layout_booklist_cards, icards, icards));

			TextView tselection = (TextView) view.findViewById(R.id.filinglist_child_selection_count);
			int iselection = Integer.parseInt(tselection.getText().toString());
			tselection.setText(getActivity().getResources().getQuantityString(R.plurals.layout_booklist_selection, iselection, iselection));

			ProgressBar pselection = (ProgressBar) view.findViewById(R.id.filinglist_child_progress_selection);
			pselection.setMax(icards);
			pselection.setProgress(iselection);

			final CheckBox checkbox = (CheckBox) view.findViewById(R.id.filinglist_child_check);

			OnClickListener listener = new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					boolean state = !check_states[position];
					check_states[position] = state;
					checkbox.setChecked(state);
					actionMode.start(getSelectedFilingsCount());
				}
			};
			// view.setOnClickListener(listener);
			checkbox.setOnClickListener(listener);
			checkbox.setChecked(check_states[position]);
			checkbox.setFocusable(false);
			return view;

		}

		private int getSelectedFilingsCount()
		{
			int count = 0;
			for(boolean el : check_states)
				if(el) ++count;
			return count;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			final CheckBox checkbox = (CheckBox) view.findViewById(R.id.filinglist_child_check);
			if(checkbox == null) return;
			boolean state = !check_states[position];
			check_states[position] = state;
			checkbox.setChecked(state);
			actionMode.start(getSelectedFilingsCount());
		}

	}

	private final FilinglistFragmentActionMode actionMode = new FilinglistFragmentActionMode(this);
	private Cursor listCursor = null;

	private static class OnFinishResume implements OnFinishListener
	{

		private final FragmentActivity activity;

		public OnFinishResume(Fragment fragment)
		{
			activity = fragment.getActivity();
		}

		@Override
		public void onFinish()
		{
			if(activity == null) return;
			FragmentManager manager = activity.getSupportFragmentManager();
			Fragment foundFragment = manager.findFragmentByTag(FilinglistFragment.TAG);
			if(foundFragment != null) ((FilinglistFragment) foundFragment).onRefresh();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		final View v = inflater.inflate(R.layout.filinglist_fragment, container, false);
		final TextView session = (TextView) v.findViewById(R.id.filinglist_session);

		listview = (ListView) v.findViewById(R.id.filinglist_listview);
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listview.setClickable(true);
		sequence = DatabaseFunctions.getSequence(getActivity());

		final Cursor c = getDatabaseHelper().getRead().query("filing_data", new String[] { "filing_session" }, "filing_sequence = ?", new String[] { "" + sequence }, null, null, null);
		if(c.getCount() == 1 && c.moveToFirst())
		{
			session.setText(getResources().getQuantityString(R.plurals.filing_session, c.getInt(0) + 1, c.getInt(0) + 1));
		}
		c.close();
		return v;

	}

	private int sequence = Sequence.DEFAULT_SEQUENCE;

	@Override
	public void onResume()
	{
		super.onResume();
		onRefresh();
	}

	private void onRefresh()
	{
		if(listCursor != null) listCursor.close();
		final SQLiteDatabase db = getDatabaseHelper().getRead();
		sequence = DatabaseFunctions.getSequence(getActivity());
		listCursor = db.query("filing left join selection on selection_card_id = filing_card_id", new String[] { "filing._id AS _id", "filing_rank", "COUNT(*) AS count", "COUNT(selection_card_id) AS selecttion_count" }, "filing_sequence = ?", new String[] { "" + sequence }, "filing_rank", null, "filing_rank");

		adapter = new filinglistAdapter(getActivity(), R.layout.filinglist_child, listCursor, new String[] { "filing_rank", "count", "selecttion_count" }, new int[] { R.id.filinglist_child_caption, R.id.filinglist_child_count, R.id.filinglist_child_selection_count });
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(adapter);

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
	}

	private filinglistAdapter adapter;
	private ListView listview;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		final MenuItem refresh = menu.findItem(R.id.menu_refresh);
		inflater.inflate(R.menu.refresh, menu);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) inflater.inflate(R.menu.filinglist_selected, menu);
		inflater.inflate(R.menu.filinglist, menu);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) menu.removeItem(R.id.menu_filinglist_chart);
		if(refresh != null) menu.removeItem(R.id.menu_refresh);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public boolean onActionItemSelected(int menu_id, final OnFinishListener listener)
	{
		switch(menu_id)
		{
			case R.id.menu_filinglist_remove_from_filing:
			{
				final LinkedList<String> filings = new LinkedList<String>();
				for(int i = 0; i < adapter.check_states.length; ++i)
				{
					if(adapter.check_states[i]) filings.add("" + adapter.ranks[i]);
				}

				if(filings.size() > 0)
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(getString(R.string.ask_remove_from_filing)).setTitle(getString(R.string.ask_remove_from_filing_title)).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
						}
					}).setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int id)
						{
							filings.addFirst("" + sequence);
							final String[] where_clause = StringUtils.listToArray(filings);
							final SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
							db.delete("filing", "filing_sequence = ? AND filing_rank " + StringUtils.generateQuestionTokens(where_clause.length), where_clause);
							db.close();
							onResume();
							if(listener != null) listener.onFinish();
						}
					});
					final AlertDialog alert = builder.create();
					alert.show();
				}
			}
				return true;
			case R.id.menu_filinglist_remove_from_selection:
			{
				final LinkedList<String> filings = new LinkedList<String>();
				// String[] filing_ranks = new String[adapter.check_states.length];
				for(int i = 0; i < adapter.check_states.length; ++i)
				{
					if(adapter.check_states[i]) filings.add("" + adapter.ranks[i]);
				}
				if(filings.size() > 0)
				{
					filings.addFirst("" + sequence);
					final String[] where_clause = StringUtils.listToArray(filings);
					final SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
					db.execSQL("DELETE from selection where selection_card_id IN (SELECT filing_card_id FROM filing WHERE filing_sequence = ? AND filing_rank " + StringUtils.generateQuestionTokens(where_clause.length) + ")", where_clause);
					db.close();
					onResume();
					if(listener != null) listener.onFinish();
				}
			}
				return true;

			case R.id.menu_filinglist_toselection:
			{
				final LinkedList<Long> list = new LinkedList<Long>();
				for(int i = 0; i < adapter.check_states.length; ++i)
				{
					if(adapter.check_states[i]) list.add(adapter.ranks[i]);
				}

				if(list.isEmpty()) return false;
				final ImportSelectionFromFilingLoader loader = new ImportSelectionFromFilingLoader(getActivity(), list, sequence);
				loader.setOnFinishListener(new OnFinishResume(this));
				loader.execute();
				if(listener != null) listener.onFinish();
			}
				return true;

			case R.id.menu_filinglist_chart:
				((MainActivity) getActivity()).showFragment(new FragmentCreator()
				{
					@Override
					public Fragment create()
					{
						return new PlotFragment();
					}

					@Override
					public boolean equals(final Fragment fragment)
					{
						return(fragment instanceof PlotFragment);
					}

					@Override
					public String getTag()
					{
						return PlotFragment.TAG;
					}

					@Override
					public Fragment update(final Fragment fragment)
					{
						((PlotFragment) fragment).onRefresh();
						return fragment;
					}

				});

		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch(item.getItemId())
		{
			case R.id.menu_refresh:
				final Runnable run = new Runnable()
				{
					@Override
					public void run()
					{
						onRefresh();
					}
				};
				if(getActivity() instanceof ActionBarFragmentActivity)
				{
					final ActionBarFragmentActivity activity = (ActionBarFragmentActivity) getActivity();
					activity.onRefresh(run);
				}
				else
					run.run();
				return false;
			default:
				if(onActionItemSelected(item.getItemId(), null)) return true;
				return super.onOptionsItemSelected(item);
		}
	}

}
