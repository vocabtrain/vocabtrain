package org.devwork.vocabtrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SequencePreference extends DialogPreference
{

	public final static String delimeter = ";";
	private boolean hasSecondQuestion;

	private class SequenceAdapter extends ArrayAdapter<String>
	{

		final byte[] permut = new byte[Sequence.data.length];

		public SequenceAdapter(Context context)
		{
			super(context, R.layout.sequence_child, R.id.sequence_preference_text);
			for(byte i = 0; i < permut.length; ++i)
				permut[i] = i;

			final SharedPreferences prefs = SequencePreference.this.getSharedPreferences();
			final int value = prefs.getInt(getKey(), Sequence.DEFAULT_SEQUENCE);
			hasSecondQuestion = prefs.getBoolean("second_question", true);
			final byte[] a = Sequence.decodeSequence(value);
			if(a != null)
			{
				for(int i = 0; i < permut.length && i < a.length; ++i)
					permut[i] = a[i];
			}
			updateArray();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			final View v = super.getView(position, convertView, parent);
			if(position == 0 || position == 2) v.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					final byte tmp = permut[position];
					permut[position] = permut[1];
					permut[1] = tmp;
					updateArray();
				}

			});
			final TextView caption = (TextView) v.findViewById(R.id.sequence_preference_caption);
			if(permut[position] == Sequence.VERNICULAR)
			{
				final TextView text = (TextView) v.findViewById(R.id.sequence_preference_text);
				text.setText(DatabaseFunctions.getVernicular(getContext(), helper.getRead()).getDisplayLanguage());
			}

			if(caption != null)
			{
				switch(position)
				{
					case 0:
						caption.setText(getContext().getString(hasSecondQuestion ? R.string.seq_first_question : R.string.seq_question));
						break;
					case 1:
						caption.setText(getContext().getString(hasSecondQuestion ? R.string.seq_second_question : R.string.seq_first_answer));
						break;
					case 2:
						caption.setText(getContext().getString(hasSecondQuestion ? R.string.seq_answer : R.string.seq_second_answer));
						break;
				}
			}
			return v;
		}

		private void updateArray()
		{
			clear();
			final String[] seq = getContext().getResources().getStringArray(R.array.training_sequence);
			for(int i : permut)
				add(seq[i]);
			notifyDataSetChanged();
		}

	}

	private SequenceAdapter adapter;
	final DatabaseHelper helper;

	public SequencePreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.sequence_preference);
		// setNegativeButtonText(R.id.sequence_preference_cancel);
		setDialogTitle(getContext().getString(R.string.pref_sequence));
		helper = new DatabaseHelper(getContext());
	}

	@Override
	protected View onCreateDialogView()
	{
		final View v = super.onCreateDialogView();
		adapter = new SequenceAdapter(getContext());
		final ListView listview = (ListView) v.findViewById(R.id.sequence_preference_listview);
		listview.setAdapter(adapter);
		return v;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		helper.close();
		if(!positiveResult) return;
		final SharedPreferences prefs = getSharedPreferences();
		final Editor edit = prefs.edit();
		edit.putInt(this.getKey(), Sequence.encodeSequence(adapter.permut));
		edit.commit();
	}
}
