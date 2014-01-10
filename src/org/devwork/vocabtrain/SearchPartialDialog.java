package org.devwork.vocabtrain;

import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchPartialDialog extends Dialog
{

	final Card card;

	protected SearchPartialDialog(FragmentActivity activity, String search, Card card)
	{
		super(activity, true, null);
		this.card = card;
		this.activity = activity;
		setContentView(R.layout.searchpartial_dialog);
		setTitle(activity.getString(R.string.search_partial));

		edit = (EditText) this.findViewById(R.id.searchpartial_edit);
		Button button = (Button) this.findViewById(R.id.searchpartial_button);
		edit.setText(search);

		button.setOnClickListener(new android.view.View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				openIconContextMenu();
			}

		});
		edit.setOnKeyListener(new EditText.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
				{
					openIconContextMenu();
					return true;
				}
				return false;
			}
		});

	}

	private final FragmentActivity activity;
	private final EditText edit;

	private void openIconContextMenu()
	{
		IconContextMenu cm = new IconContextMenu(activity);
		onCreateOptionsMenu(cm.getMenu());
		cm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener()
		{
			@Override
			public void onIconContextItemSelected(MenuItem item)
			{
				onMenuItemSelected(0, item);
			}
		});
		cm.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		final MenuInflater inflater = new MenuInflater(getContext());
		inflater.inflate(R.menu.searchpartial_options, menu);
		inflater.inflate(R.menu.dicts, menu);
		SearchIntents.disableMissingMenuEntries(menu, activity, card);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item)
	{
		if(SearchIntents.search(item.getItemId(), edit.getText().toString(), card, activity))
		{
			dismiss();
			return true;
		}
		else
		{
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
