package org.devwork.vocabtrain;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tatoeba.providers.ProviderInterface;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.example.android.ActionBarFragmentActivity;

public class TatoebaFragment extends DatabaseFragment
{
	private class SelectAdapter extends SimpleCursorTreeAdapter
	{
		private final String[] languages;

		private final String whereClause;
		private boolean noTranslation = false;

		public SelectAdapter(final Cursor cursor, final int collapsedGroupLayout, final int expandedGroupLayout, final String[] groupFrom, final int[] groupTo,
				final int childLayout, final int lastChildLayout, final String[] childFrom, final int[] childTo)
		{
			super(getActivity(), cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, lastChildLayout, childFrom, childTo);
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

			if(prefs.getBoolean("tatoeba_translations_all", false))
			{
				languages = null;
				whereClause = null;
			}
			else
			{
				final List<String> languageList = new LinkedList<String>();
				if(prefs.getBoolean("tatoeba_translations_vernicular", true))
				{
					final Cursor languageCursor = getDatabaseHelper().getRead().query("translations", new String[] { "translation_language" }, null, null,
							"translation_language", null, null);
					while(languageCursor.moveToNext())
						languageList.add(languageCursor.getString(languageCursor.getColumnIndex("translation_language")));
					languageCursor.close();
				}
				if(prefs.getBoolean("tatoeba_translations_study", true))
				{
					final Cursor languageCursor = getDatabaseHelper().getRead().query("books", new String[] { "book_language" }, null, null, "book_language",
							null, null);
					while(languageCursor.moveToNext())
						languageList.add(languageCursor.getString(languageCursor.getColumnIndex("book_language")));
					languageCursor.close();
				}
				if(languageList.isEmpty())
				{
					noTranslation = true;
					languages = null;
					whereClause = null;
				}
				else
				{
					languages = StringUtils.createArray(languageList);
					whereClause = ProviderInterface.LinkTable.LANGUAGE + StringUtils.generateQuestionTokens(languages.length);
				}
			}
		}

		@Override
		protected Cursor getChildrenCursor(final Cursor groupCursor)
		{
			if(noTranslation) return null;
			// Log.e("Hi", "" + groupCursor);
			if(groupCursor == null) return null;
			final Cursor cursor = getActivity().getContentResolver().query(
					ContentUris.withAppendedId(ProviderInterface.LinkTable.CONTENT_URI,
							groupCursor.getLong(groupCursor.getColumnIndex(ProviderInterface.LinkTable._ID))),
					new String[] { ProviderInterface.LinkTable._ID, ProviderInterface.LinkTable.CONTENT, ProviderInterface.LinkTable.LANGUAGE }, whereClause,
					languages, null);
			// Log.e("Hi", "" + cursor);
			listCursors.add(cursor);
			return cursor;

		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, final View convertView, final ViewGroup parent)
		{
			final View view = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
			final Cursor childCursor = getChildrenCursor(getCursor());
			try
			{
				childCursor.moveToPosition(childPosition);
				final String language = childCursor.getString(childCursor.getColumnIndex(ProviderInterface.LinkTable.LANGUAGE));
				if(language.equals("jpn"))
				{
					final long sentence_id = childCursor.getLong(childCursor.getColumnIndex(ProviderInterface.LinkTable._ID));
					view.setOnClickListener(new OnClickListener()
					{

						@Override
						public void onClick(final View v)
						{
							final Cursor cursor = getActivity().getContentResolver().query(
									ContentUris.withAppendedId(ProviderInterface.RubyTable.CONTENT_URI, sentence_id),
									new String[] { ProviderInterface.RubyTable.CONTENT }, null, null, null);
							if(cursor.moveToFirst())
							{
								final String html = cursor.getString(cursor.getColumnIndex(ProviderInterface.RubyTable.CONTENT));
								final RubyDialog dialog = new RubyDialog(getActivity(), html);
								dialog.show();
							}
							cursor.close();
						}

					});
				}
				else view.setClickable(false);
			}
			catch(final android.database.CursorIndexOutOfBoundsException e)
			{
			}
			childCursor.close();
			return view;

		}

		@Override
		public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent)
		{
			final View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			final TextView text = (TextView) view.findViewById(R.id.tatoeba_group_text);
			Log.e(TAG, "Language: " + target_language);
			if(target_language.equals("jpn"))
			{
				text.setOnClickListener(new OnClickListener()
				{

					@Override
					public void onClick(final View v)
					{
						final Cursor groupCursor = getCursor();
						groupCursor.moveToPosition(groupPosition);
						final long sentence_id = groupCursor.getLong(groupCursor.getColumnIndex(ProviderInterface.LinkTable._ID));
						final Cursor cursor = getActivity().getContentResolver().query(
								ContentUris.withAppendedId(ProviderInterface.RubyTable.CONTENT_URI, sentence_id),
								new String[] { ProviderInterface.RubyTable.CONTENT }, null, null, null);
						Log.e(TAG, "Cursor: " + cursor.getCount());
						if(cursor.moveToFirst())
						{
							final String html = cursor.getString(cursor.getColumnIndex(ProviderInterface.RubyTable.CONTENT));
							final RubyDialog dialog = new RubyDialog(getActivity(), html);
							dialog.show();
						}
						cursor.close();
					}
				});
			}
			else text.setClickable(false);

			return view;
		}

	}

	public static final String TAG = Constants.PACKAGE_NAME + ".TatoebaFragment";

	private final List<Cursor> listCursors = new LinkedList<Cursor>();
	private Cursor listCursor;
	private final LanguageLocale target_language;

	private ExpandableListView listview;

	private SelectAdapter adapter;

	private TextView caption;

	public TatoebaFragment()
	{
		this.listCursor = null;
		this.target_language = null;
	}

	public TatoebaFragment(final Cursor listCursor, final LanguageLocale target_language)
	{
		this.listCursor = listCursor;
		this.target_language = target_language;
	}

	public void changeCursor(final Cursor listCursor)
	{
		// this.listCursor.close();
		this.listCursor = listCursor;
		adapter.changeCursor(this.listCursor);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		setHasOptionsMenu(false);
		final View v = inflater.inflate(R.layout.tatoeba_fragment, container, false);
		listview = (ExpandableListView) v.findViewById(R.id.tatoeba_list);
		caption = (TextView) v.findViewById(R.id.tatoeba_caption);

		/*
		 * for(int i = 0; i < count; i++) listview.expandGroup(i);
		 */
		return v;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		/*if(listCursor != null)
		{
			listCursor.close();
			listCursor = null;
		}
		*/
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if(!listCursors.isEmpty())
		{
			try
			{
				final Iterator<Cursor> it = listCursors.iterator();
				while(it.hasNext())
				{
					it.next().close();
				}
			}
			catch(final ConcurrentModificationException e)
			{
			}
			listCursors.clear();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(listCursor == null)
		{
			remove();
			return;
		}
		final String[] groupFrom = new String[] { ProviderInterface.LinkTable.CONTENT };
		final int[] groupTo = new int[] { R.id.tatoeba_group_text };
		final String[] childFrom = new String[] { ProviderInterface.LinkTable.CONTENT };
		final int[] childTo = new int[] { R.id.tatoeba_child_text };
		adapter = new SelectAdapter(listCursor, R.layout.tatoeba_group, R.layout.tatoeba_group, groupFrom, groupTo, R.layout.tatoeba_child,
				R.layout.tatoeba_child, childFrom, childTo);
		listview.setAdapter(adapter);
		final int count = adapter.getGroupCount();
		caption.setText(getResources().getQuantityString(R.plurals.tatoeba_count, count, count));

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if(prefs.getBoolean("tatoeba_show_translations", true)) for(int i = 0; i < count; i++)
			listview.expandGroup(i);

	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
	}

	private void remove()
	{
		final FragmentManager manager = getActivity().getSupportFragmentManager();
		final FragmentTransaction ft = manager.beginTransaction();
		ft.remove(this);
		ft.commit();
		manager.popBackStack();
	}

}
