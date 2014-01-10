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
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.android.ActionBarFragmentActivity;

public class LanguageFragment extends Fragment implements OnFinishListener
{
	public static final String TAG = Constants.PACKAGE_NAME + ".UpdatelanguageFragment";

	@Override
	public void onStart()
	{
		super.onStart();
		((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
	}

	private class UpdateLanguageAdapter extends ArrayAdapter<LanguageData> implements OnItemClickListener
	{

		public UpdateLanguageAdapter(Context context, LanguageData[] array)
		{
			super(context, R.layout.sync_language_child, R.id.sync_language_child_caption, array);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			final View view = super.getView(position, convertView, parent);
			TextView caption = (TextView) view.findViewById(R.id.sync_language_child_caption);
			caption.setText(new LanguageLocale(getActivity(), caption.getText().toString()).getDisplayLanguage());
			final LanguageData current = (LanguageData) getItem(position);

			//final ImageView checkfield = (ImageView) view.findViewById(R.id.sync_language_child_check);
			//checkfield.setImageDrawable(getResources().getDrawable(current.getState().getDrawableId()));
			caption.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(current.getState().getDrawableId()), null, null, null);

			return view;

		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			TextView caption = (TextView) view.findViewById(R.id.sync_language_child_caption);
			//final ImageView checkfield = (ImageView) view.findViewById(R.id.sync_language_child_check);
			if(caption == null) return;
			final LanguageData current = (LanguageData) getItem(position);
			current.switchState();
		//	checkfield.setImageDrawable(getResources().getDrawable(current.getState().getDrawableId()));
			caption.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(current.getState().getDrawableId()), null, null, null);

			
			final SyncActivity activity = (SyncActivity) getActivity();
			activity.dataChanged();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.sync_language_fragment, container, false);
		listview = (ListView) v.findViewById(R.id.sync_language_listview);
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

	@Override
	public void onFinish()
	{
		UpdateLanguageAdapter adapter = new UpdateLanguageAdapter(getActivity(), ((SyncActivity) getActivity()).getTranslationLanguages());
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
