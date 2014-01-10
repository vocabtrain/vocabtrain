package org.devwork.vocabtrain.sync;

import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.LanguageLocale;
import org.devwork.vocabtrain.OnFinishListener;
import org.devwork.vocabtrain.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.android.ActionBarFragmentActivity;

public class BookFragment extends Fragment implements OnFinishListener
{
	public static final String TAG = Constants.PACKAGE_NAME + ".UpdateBookFragment";

	@Override
	public void onStart()
	{
		super.onStart();
		((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
	}

	private class UpdateBookAdapter extends ArrayAdapter<BookData> implements OnItemClickListener
	{

		public UpdateBookAdapter(Context context, BookData[] array)
		{
			super(context, R.layout.sync_book_child, R.id.sync_book_child_caption, array);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			final View view = super.getView(position, convertView, parent);
			final BookData book = ((BookData) getItem(position));
			// TextView caption = (TextView) v.findViewById(R.id.sync_book_child_caption);
			// caption.setText(current.getString("book_title") );
			final ImageView language = (ImageView) view.findViewById(R.id.sync_book_child_image_language);
			language.setImageDrawable(getResources().getDrawable(new LanguageLocale(getActivity(), book.getLanguage()).getFlagId()));
			final MultiFlagView multiflag = (MultiFlagView) view.findViewById(R.id.sync_book_child_image_translated_languages);
			multiflag.setLanguages(book);
			multiflag.setImageDrawable(getResources().getDrawable(new LanguageLocale(getActivity(), book.getLanguage()).getFlagId()));
			// ImageView update = (ImageView) view.findViewById(R.id.sync_book_child_image_update);

			/*
			 * if(book.getOldTimestamp() != -1 && !book.isSelected()) { update.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_input_delete) ); } else if(book.isUpdateable()) { update.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_input_add) ); } else update.setImageDrawable(null);
			 */

			final ImageView checkfield = (ImageView) view.findViewById(R.id.sync_book_child_check);
			checkfield.setImageDrawable(getResources().getDrawable(book.getState().getDrawableId()));

			return view;

		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			final ImageView checkfield = (ImageView) view.findViewById(R.id.sync_book_child_check);
			if(checkfield == null) return;
			final BookData book = ((BookData) getItem(position));
			book.switchState();
			checkfield.setImageDrawable(getResources().getDrawable(book.getState().getDrawableId()));

			final SyncActivity activity = (SyncActivity) getActivity();
			activity.dataChanged();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.sync_book_fragment, container, false);
		listview = (ListView) v.findViewById(R.id.sync_book_listview);
		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listview.setClickable(true);
		if(getActivity() instanceof SyncActivity)
		{
			final SyncActivity activity = (SyncActivity) getActivity();
			if(activity.isFinished())
				onFinish();
			else
				activity.addOnFinishListener(this);
		}
		return v;
	}

	private UpdateBookAdapter adapter = null;

	@Override
	public void onFinish()
	{
		final SyncActivity activity = (SyncActivity) getActivity();
		if(activity.getBooks() == null) return;
		adapter = new UpdateBookAdapter(activity, activity.getBooks());
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(adapter);
	}

	private ListView listview;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		final MenuItem refresh = menu.findItem(R.id.menu_refresh);
		inflater.inflate(R.menu.refresh, menu);
		if(refresh != null) menu.removeItem(R.id.menu_refresh);
		super.onCreateOptionsMenu(menu, inflater);
	}

}