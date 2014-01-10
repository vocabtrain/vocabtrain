package org.devwork.vocabtrain;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.android.ActionBarFragmentActivity;

public abstract class TableViewFragment extends DatabaseFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".TableViewFragment";

	private Cursor listCursor = null;
	private ListView listview;
	private EditText filter;

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		onRefresh();
	}
	
	class TableAdapter extends SimpleCursorAdapter
	{

		public TableAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			

		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = super.getView(position, convertView, parent);
			TextView script = (TextView) view.findViewById(R.id.table_child_script);
			TextView vernicular = (TextView) view.findViewById(R.id.table_child_vernicular);
			TextView script_comment = (TextView) view.findViewById(R.id.table_child_script_comment);
			TextView vernicular_comment = (TextView) view.findViewById(R.id.table_child_vernicular_comment);

			TextView speech = (TextView) view.findViewById(R.id.table_child_speech);
			TextView speech_comment = (TextView) view.findViewById(R.id.table_child_speech_comment);
			try 
			{
				Typeface vernicularTypeface = DatabaseFunctions.getTypefaceFromLocale(getActivity(), DatabaseFunctions.getVernicular(getActivity()));
				vernicular.setTypeface(vernicularTypeface);
				vernicular_comment.setTypeface(vernicularTypeface);
				if(target_language != null)
				{
					Typeface foreignTypeface = DatabaseFunctions.getTypefaceFromLocale(getActivity(), target_language);
					script.setTypeface(foreignTypeface);
					script_comment.setTypeface(foreignTypeface);
					if(speech != null) speech.setTypeface(foreignTypeface);
					if(speech_comment != null) speech_comment.setTypeface(foreignTypeface);

				}
			} 
			catch (IOException e) {}
			
			
			return view;
		}
	}
	

	public void onRefresh()
	{
		if(listCursor != null) listCursor.close();
		final SimpleCursorAdapter adapter;
		if(!hasSpeech)
		{
			listCursor = getCursor(null);
			adapter = new TableAdapter(getActivity(), R.layout.table_narrow_child, listCursor, new String[] { "card_script", "translation_content", "card_script_comment", "translation_comment" }, new int[] { R.id.table_child_script, R.id.table_child_vernicular, R.id.table_child_script_comment, R.id.table_child_vernicular_comment });

		}
		else
		{
			listCursor = getCursor(null);
			adapter = new TableAdapter(getActivity(), R.layout.table_child, listCursor, new String[] { "card_script", "card_speech", "translation_content", "card_script_comment", "card_speech_comment", "translation_comment" }, new int[] { R.id.table_child_script, R.id.table_child_speech, R.id.table_child_vernicular, R.id.table_child_script_comment, R.id.table_child_speech_comment, R.id.table_child_vernicular_comment });
		}
		adapter.setFilterQueryProvider(new FilterQueryProvider()
		{
			@Override
			public Cursor runQuery(CharSequence c)
			{
				Cursor cursor = getCursor(c.toString());
				listCursors.add(cursor);
				return cursor;
			}
		});
		filter.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void afterTextChanged(Editable s)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				adapter.getFilter().filter(s);
			}
		});
		listview.setAdapter(adapter);
		listview.setTextFilterEnabled(true);
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

	private List<Cursor> listCursors = new LinkedList<Cursor>();
	private boolean hasSpeech = true;

	protected abstract Cursor getCursor(String search);

	protected abstract boolean hasSpeechColumn();

	protected abstract LanguageLocale getTargetLanguage();

	protected boolean hasSpeech()
	{
		return hasSpeech;
	}

	protected abstract int getLayoutId();
	LanguageLocale target_language;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		hasSpeech = hasSpeechColumn();
		target_language = getTargetLanguage();
		View v = inflater.inflate(getLayoutId(), container, false);
		filter = (EditText) v.findViewById(R.id.table_filter);
		if(!hasSpeech)
		{
			v.findViewById(R.id.table_title_speech).setVisibility(View.GONE);
			TextView script = (TextView) v.findViewById(R.id.table_title_script);
			if(target_language != null) script.setText(target_language.getDisplayLanguage());
		}
		TextView vernicular = (TextView) v.findViewById(R.id.table_title_vernicular);
		vernicular.setText(DatabaseFunctions.getVernicular(getActivity(), getDatabaseHelper().getRead()).getDisplayLanguage());

		listview = (ListView) v.findViewById(R.id.tableactivity_listview);
		registerForContextMenu(listview);
		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if(view != null && parent != null) listview.showContextMenuForChild(view);
			}
		});

		return v;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.tableview_context, menu);
		inflater.inflate(R.menu.dicts, menu);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Card card = getCardFromRow(info.id);
		SearchIntents.disableMissingMenuEntries(menu, getActivity(), card);
	}

	protected abstract Card getCardFromRow(long row);

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		Card card = getCardFromRow(info.id);
		if(card != null)
		{
			if(SearchIntents.search(item.getItemId(), card.getScript(), card, getActivity())) return true;
			if(SearchIntents.view(item.getItemId(), card, this)) return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		final MenuItem refresh = menu.findItem(R.id.menu_refresh);
		inflater.inflate(R.menu.tableview_option, menu);
		if(refresh != null) menu.removeItem(R.id.menu_refresh);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
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

			case R.id.menu_filter:
				switch(filter.getVisibility())
				{
					case View.GONE:
						filter.setVisibility(View.VISIBLE);
						break;
					case View.VISIBLE:
						filter.setText("");
						filter.setVisibility(View.GONE);
						break;

				}

				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
