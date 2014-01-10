package org.devwork.vocabtrain;

import android.annotation.TargetApi;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

@TargetApi(11)
public class FilinglistFragmentActionModeCallback implements ActionMode.Callback
{
	private ActionMode actionMode;
	private final FilinglistFragment fragment;

	public FilinglistFragmentActionModeCallback(FilinglistFragment fragment)
	{
		this.fragment = fragment;
	}

	@Override
	public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
	{
		actionMode.setTitle(fragment.getActivity().getResources().getQuantityString(R.plurals.filings_select, 0, 0));

		MenuInflater inflater = fragment.getActivity().getMenuInflater();
		inflater.inflate(R.menu.filinglist_selected, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode actionMode, Menu menu)
	{
		return false;
	}

	@Override
	public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem)
	{
		return fragment.onActionItemSelected(menuItem.getItemId(), new OnFinishListener()
		{
			@Override
			public void onFinish()
			{
				actionMode.finish();
			}
		});

	}

	@Override
	public void onDestroyActionMode(ActionMode actionMode)
	{
		this.actionMode = null;
	}

	public void start(int chapter_count)
	{
		if(actionMode == null) actionMode = fragment.getActivity().startActionMode(this);
		actionMode.setTitle(fragment.getActivity().getResources().getQuantityString(R.plurals.filings_select, chapter_count, chapter_count));
	}

};
