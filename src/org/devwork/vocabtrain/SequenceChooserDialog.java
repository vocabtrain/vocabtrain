package org.devwork.vocabtrain;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SequenceChooserDialog extends DatabaseDialogFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".SequenceChooserDialog";

	private ListView listview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View v = inflater.inflate(R.layout.sequencechooser_dialog, container, false);
		// listview = (ListView) v.findViewById(R.id.cardviewdialog_listview);
		v.setBackgroundColor(getActivity().getResources().getColor(android.R.color.background_light)); // BUG in Android 2.3
		final Dialog dialog = getDialog();
		dialog.setTitle(getString(R.string.sequencechooser_title));
		listview = (ListView) v.findViewById(R.id.sequencechooser_listview);
		listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listview.setClickable(true);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sequence = prefs.getInt("sequence", Sequence.DEFAULT_SEQUENCE);

		final Button cancel = (Button) v.findViewById(R.id.sequencechooser_cancel);
		cancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dismiss();
			}

		});
		listview = (ListView) v.findViewById(R.id.sequencechooser_listview);
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
		listCursor = db.query("filing", new String[] { "filing_sequence" }, null, null, "filing_sequence", null, null);

		final Cursor cursor = getDatabaseHelper().getRead().query("filing", new String[] { "filing_sequence" }, null, null, "filing_sequence", null, null);

		String[] entries = new String[cursor.getCount()];
		int[] entryvalues = new int[cursor.getCount()];

		int i = 0;
		while(cursor.moveToNext())
		{
			entryvalues[i] = cursor.getInt(cursor.getColumnIndex("filing_sequence"));
			final byte[] array = Sequence.decodeSequence(entryvalues[i]);
			final String[] entrynames = new String[Sequence.data.length];
			for(int j = 0; j < entrynames.length; ++j)
				entrynames[j] = getString(Sequence.getStringId(array[j]));
			entries[i++] = StringUtils.join(entrynames, " \u2192 ");
		}
		cursor.close();

		adapter = new SequenceAdapter(getActivity(), R.layout.sequencechooser_child, R.id.sequencechooser_child, entries, entryvalues);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(adapter);
		for(i = 0; i < entryvalues.length; ++i)
		{
			if(sequence == entryvalues[i]) listview.setItemChecked(i, true);
		}

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

	private Cursor listCursor = null;

	private SequenceAdapter adapter;

	private class SequenceAdapter extends ArrayAdapter<String> implements OnItemClickListener
	{

		private final int[] values;

		public SequenceAdapter(Context context, int resource, int textViewResourceId, String[] objects, int[] values)
		{
			super(context, resource, textViewResourceId, objects);
			this.values = values;
		}

		/*
		 * @Override public View getView(final int position, View convertView, ViewGroup parent) { View view = super.getView(position, convertView, parent); CheckedTextView radio = (CheckedTextView) view.findViewById(R.id.sequencechooser_child); radio.setBackgroundDrawable(SequenceChooserDialog.this.getView().getBackground());
		 * 
		 * Log.e(TAG, " " + sequence + " : " + values[position]); return view; }
		 */
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
			final Editor edit = prefs.edit();
			edit.putInt("sequence", values[position]);
			edit.commit();
			dismiss();
		}

	}
}
