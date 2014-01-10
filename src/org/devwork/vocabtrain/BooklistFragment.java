package org.devwork.vocabtrain;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
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
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView.OnChildClickListener;

import com.example.android.ActionBarFragmentActivity;

public class BooklistFragment extends DatabaseFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".BooklistFragment";
	private int sequence = Sequence.DEFAULT_SEQUENCE;

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
		TourTipsDialog.createInstance(TAG, getActivity());
	}

	private class SelectAdapter extends SimpleCursorTreeAdapter implements OnChildClickListener
	{

		private final boolean[][] check_states;
		private final long[] book_ids;

		public SelectAdapter(Cursor cursor, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, int lastChildLayout, String[] childFrom, int[] childTo)
		{
			super(getActivity(), cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, lastChildLayout, childFrom, childTo);
			if(cursor.getCount() == 0)
			{
				check_states = null;
				book_ids = null;
				return;
			}
			final int size = cursor.getCount();
			check_states = new boolean[size][];
			book_ids = new long[size];
			for(int i = 0; i < cursor.getCount(); ++i)
			{
				cursor.moveToPosition(i);
				final long book_id = cursor.getLong(cursor.getColumnIndex("_id"));
				check_states[i] = new boolean[getDatabaseHelper().getChaptersCount(book_id)];
				book_ids[i] = book_id;
			}
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor)
		{
			Cursor cursor = getDatabaseHelper().getRead().rawQuery("SELECT `chapters`.`_id` AS _id, chapter_volume, COUNT(`content_card_id`) AS chapter_cards_count, COUNT(`filing_card_id`) AS chapter_filing_count, COUNT(`selection_card_id`) AS chapter_selection_count FROM `chapters` JOIN `content` ON `content_chapter_id` = `chapters`.`_id` LEFT JOIN `filing` ON `filing_card_id` = `content_card_id` AND `filing_sequence` = ? LEFT JOIN `selection` ON `selection_card_id` = `content_card_id` WHERE `chapter_book_id` = ? GROUP BY `chapters`.`_id`", new String[] { "" + sequence, groupCursor.getString(groupCursor.getColumnIndex("_id")) });
			listCursors.add(cursor);
			return cursor;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{
			View view = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);

			TextView tcaption = (TextView) view.findViewById(R.id.booklist_child_caption);
			final String tcaption_s = tcaption.getText().toString();
			tcaption.setText(tcaption_s.length() > 5 ? tcaption_s :
					String.format(getActivity().getString(R.string.layout_booklist_caption), tcaption_s));

			TextView tcards = (TextView) view.findViewById(R.id.booklist_child_cards_count);
			int icards = Integer.parseInt(tcards.getText().toString());
			tcards.setText(getActivity().getResources().getQuantityString(R.plurals.layout_booklist_cards, icards, icards));

			{
				TextView tfiling = (TextView) view.findViewById(R.id.booklist_child_filing_count);
				int ifiling = Integer.parseInt(tfiling.getText().toString());
				tfiling.setText(getActivity().getResources().getQuantityString(R.plurals.layout_booklist_filing, ifiling, ifiling));
				ProgressBar pfiling = (ProgressBar) view.findViewById(R.id.booklist_progress_filing);
				pfiling.setMax(icards);
				pfiling.setProgress(ifiling);
			}
			{
				TextView tselection = (TextView) view.findViewById(R.id.booklist_child_selection_count);
				int iselection = Integer.parseInt(tselection.getText().toString());
				tselection.setText(getActivity().getResources().getQuantityString(R.plurals.layout_booklist_selection, iselection, iselection));
				ProgressBar pselection = (ProgressBar) view.findViewById(R.id.booklist_progress_selection);
				pselection.setMax(icards);
				pselection.setProgress(iselection);
			}

			final CheckBox checkbox = (CheckBox) view.findViewById(R.id.booklist_child_check);
			OnClickListener listener = new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{

					boolean state = !check_states[groupPosition][childPosition];
					check_states[groupPosition][childPosition] = state;
					checkbox.setChecked(state);
					actionMode.start(getSelectedChaptersCount());
				}
			};

			// view.setOnClickListener(listener);
			checkbox.setOnClickListener(listener);
			checkbox.setChecked(check_states[groupPosition][childPosition]);
			checkbox.setFocusable(false);
			return view;
		}

		private int getSelectedChaptersCount()
		{
			int count = 0;
			for(boolean[] row : check_states)
				for(boolean el : row)
					if(el) ++count;
			return count;
		}

		@Override
		public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id)
		{
			final CheckBox checkbox = (CheckBox) view.findViewById(R.id.booklist_child_check);
			if(checkbox == null) return false;
			boolean state = !check_states[groupPosition][childPosition];
			check_states[groupPosition][childPosition] = state;
			checkbox.setChecked(state);
			actionMode.start(getSelectedChaptersCount());
			return true;
		}

	}

	private final BooklistFragmentActionMode actionMode = new BooklistFragmentActionMode(this);
	private SelectAdapter adapter;
	private ExpandableListView listView;

	static class OnFinishResume implements OnFinishListener
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
			Fragment foundFragment = manager.findFragmentByTag(BooklistFragment.TAG);
			if(foundFragment != null) ((BooklistFragment) foundFragment).onRefresh();
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{

		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.booklist_fragment, container, false);

		listView = (ExpandableListView) v.findViewById(R.id.booklist_listview);
		listView.setClickable(true);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);

		sequence = DatabaseFunctions.getSequence(getActivity());

		listView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
			{
				final boolean isSelectionEmpty = getDatabaseHelper().getSelectionLength() == 0;
				if(!nothingSelected())
				{
					ChapterBatchLoader imp = new ImportSelectionFromChaptersLoader(getActivity(), StringUtils.listToArray(getChaptersSelected()));
					imp.setOnFinishListener(new OnFinishListener()
					{
						@Override
						public void onFinish()
						{
							if(getActivity() == null) return;
							FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
							TrainingChooserDialog dialog = TrainingChooserDialog.createInstance(isSelectionEmpty);
							dialog.show(fragmentManager, TrainingChooserDialog.TAG);
						}
					});
					imp.execute();
				}
				return true;
			}
		});

		return v;
	}

	private Cursor listCursor = null;
	private final List<Cursor> listCursors = new LinkedList<Cursor>();

	@Override
	public void onResume()
	{
		super.onResume();
		onRefresh();
	}

	private void onRefresh()
	{
		if(listCursor != null) listCursor.close();
		sequence = DatabaseFunctions.getSequence(getActivity());
		listCursor = getDatabaseHelper().getBooks();
		adapter = new SelectAdapter(listCursor, R.layout.booklist_group_collapsed, R.layout.booklist_group_expanded, new String[] { "book_name" }, new int[] { R.id.booklist_group }, R.layout.booklist_child, R.layout.booklist_child, new String[] { "chapter_volume", "chapter_cards_count", "chapter_filing_count", "chapter_selection_count" }, new int[] { R.id.booklist_child_caption, R.id.booklist_child_cards_count, R.id.booklist_child_filing_count, R.id.booklist_child_selection_count });
		listView.setAdapter(adapter);
		listView.setOnChildClickListener(adapter);
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

	private boolean nothingSelected()
	{
		for(int i = 0; i < adapter.check_states.length; ++i)
			for(int j = 0; j < adapter.check_states[i].length; ++j)
				if(adapter.check_states[i][j]) return false;
		return true;
	}

	private LinkedList<Integer> getChaptersSelected()
	{
		if(adapter == null) return new LinkedList<Integer>();
		final SQLiteDatabase db = getDatabaseHelper().getRead();
		final LinkedList<Integer> list = new LinkedList<Integer>();
		for(int i = 0; i < adapter.check_states.length; ++i)
		{
			final long book_id = adapter.book_ids[i];
			final Cursor chapters = db.query("chapters", new String[] { "_id" }, "chapter_book_id = ?", new String[] { "" + book_id }, null, null, " _id ASC");
			for(int j = 0; j < adapter.check_states[i].length; ++j)
			{
				if(adapter.check_states[i][j] && chapters.moveToPosition(j))
				{
					list.add(chapters.getInt(0));
				}
			}
			chapters.close();
		}
		return list;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{

		final MenuItem refresh = menu.findItem(R.id.menu_refresh);
		inflater.inflate(R.menu.refresh, menu);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) inflater.inflate(R.menu.booklist, menu);
		if(refresh != null) menu.removeItem(R.id.menu_refresh);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public boolean onActionItemSelected(int menu_id, final OnFinishListener listener)
	{
		switch(menu_id)
		{
			case R.id.menu_booklist_remove_from_selection:
			{
				RemoveSelectionLoader importer = new RemoveSelectionLoader(getActivity(), StringUtils.listToArray(getChaptersSelected()));
				importer.setOnFinishListener(new OnFinishListener()
				{
					@Override
					public void onFinish()
					{
						if(listener != null) listener.onFinish();
					}
				});
				importer.execute();

			}

				return true;
			case R.id.menu_booklist_remove_from_filing:
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
						RemoveFilingLoader importer = new RemoveFilingLoader(getActivity(), StringUtils.listToArray(getChaptersSelected()));
						importer.setOnFinishListener(new OnFinishResume(BooklistFragment.this));
						importer.execute();
						if(listener != null) listener.onFinish();
					}
				});
				final AlertDialog alert = builder.create();
				alert.show();
			}
				return true;
			case R.id.menu_booklist_toselection:
			{
				ImportSelectionFromChaptersLoader importer = new ImportSelectionFromChaptersLoader(getActivity(), StringUtils.listToArray(getChaptersSelected()));
				importer.setOnFinishListener(new OnFinishResume(BooklistFragment.this));
				importer.execute();
			}
				if(listener != null) listener.onFinish();
				return true;
			case R.id.menu_booklist_tofiling:
			{
				ImportFilingLoader importer = new ImportFilingLoader(getActivity(), StringUtils.listToArray(getChaptersSelected()));
				importer.setOnFinishListener(new OnFinishResume(BooklistFragment.this));
				importer.execute();

			}
				if(listener != null) listener.onFinish();
				return true;
			case R.id.menu_booklist_toboth:
			{
				final Integer[] lesson_ids = StringUtils.listToArray(getChaptersSelected());
				if(lesson_ids.length > 0)
				{
					final ChapterBatchLoader importer = new ImportFilingLoader(getActivity(), lesson_ids);
					final ChapterBatchLoader simporter = new ImportSelectionFromChaptersLoader(importer);

					simporter.setOnFinishListener(new OnFinishListener()
					{
						@Override
						public void onFinish()
						{
							// if(getActivity() == null) return;
							importer.setOnFinishListener(new OnFinishResume(BooklistFragment.this));
							importer.execute();
							if(listener != null) listener.onFinish();
						}
					});
					simporter.execute();
				}

			}

				return true;
			case R.id.menu_booklist_priority:
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(getString(R.string.select_priority));

				builder.setItems(R.array.priorities, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int item)
					{
						String[] values = getResources().getStringArray(R.array.priority_values);
						ChangePriorityLoader importer = new ChangePriorityLoader(getActivity(), StringUtils.listToArray(getChaptersSelected()), CardFiling.Priority.get(Integer.parseInt(values[item])));
						importer.setOnFinishListener(new OnFinishListener()
						{
							@Override
							public void onFinish()
							{
								if(listener != null) listener.onFinish();
							}
						});
						importer.execute();

					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
				return true;
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
