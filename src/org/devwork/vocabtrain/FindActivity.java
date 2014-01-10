package org.devwork.vocabtrain;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.example.android.ActionBarFragmentActivity;

public class FindActivity extends ActionBarFragmentActivity
{

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_activity);
		final Bundle extras = getIntent().getExtras();
		final String search = extras.getString(SearchManager.QUERY);
		if(search == null) return;
		final FragmentManager manager = getSupportFragmentManager();
		FindFragment findFragment = (FindFragment) manager.findFragmentByTag(FindFragment.TAG);
		if(findFragment == null)
		{
			final FragmentTransaction ft = manager.beginTransaction();
			findFragment = FindFragment.createInstance(search);
			ft.add(R.id.searchactivity_layout, findFragment, FindFragment.TAG);
			ft.commit();
		}
		else
			findFragment.setSearchString(search);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				final Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}