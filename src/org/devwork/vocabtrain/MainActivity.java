package org.devwork.vocabtrain;

import org.apachecommons.codec.binary.Base64;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;

import com.example.android.ActionBarFragmentActivity;
import com.tomgibara.android.veecheck.Veecheck;
import com.tomgibara.android.veecheck.util.PrefSettings;

public class MainActivity extends ActionBarFragmentActivity
{
	public static final String TAG = Constants.PACKAGE_NAME + ".MainActivity";

	private FragmentHelper helper;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		final FragmentManager manager = getSupportFragmentManager();
		if(manager.findFragmentByTag(FragmentHelper.TAG) == null)
		{
			final FragmentTransaction ft = manager.beginTransaction();
			helper = new FragmentHelper();
			ft.add(helper, FragmentHelper.TAG);
			ft.commit();
		}
		if(helper == null) helper = (FragmentHelper) manager.findFragmentByTag(FragmentHelper.TAG);

	}

	@Override
	public void onStart()
	{
		super.onStart();
		changeOrientation();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getString(PrefSettings.KEY_CHECK_URI, null) == null)
		{
			final Editor editor = prefs.edit();
			editor.putBoolean(PrefSettings.KEY_ENABLED, false);
			editor.putString(PrefSettings.KEY_CHECK_URI, Constants.serverUrl(prefs, Constants.SERVER_VEECHECK) );
			editor.commit();
		}
		if(prefs.getBoolean(PrefSettings.KEY_ENABLED, false))
		{
			Intent intent = new Intent(Veecheck.getRescheduleAction(this));
			Log.e(TAG, "update send broadcast " + Veecheck.getRescheduleAction(this));
			sendBroadcast(intent);
		}
		if(!prefs.getBoolean("license_accepted", false))
		{
			Intent intent = new Intent(MainActivity.this, LicenseActivity.class);
			startActivityForResult(intent, Constants.REQUEST_LICENSE_ACCEPTING);
		}
		final FragmentManager manager = getSupportFragmentManager();

		
		if(prefs.getBoolean("basic_mode", true))
		{
			if(manager.findFragmentByTag(DashboardBasicFragment.TAG) == null)
			{
				final FragmentTransaction ft = manager.beginTransaction();
				final Fragment fragment = new DashboardBasicFragment();
				ft.add(R.id.main_layout, fragment, DashboardBasicFragment.TAG);
				ft.commit();
				final Fragment oldFragment = manager.findFragmentByTag(DashboardFragment.TAG);
				if(oldFragment != null)
					ft.remove(oldFragment);
			}
		}
		else
		if(manager.findFragmentByTag(DashboardFragment.TAG) == null)
		{
			final FragmentTransaction ft = manager.beginTransaction();
			final Fragment fragment = new DashboardFragment();
			ft.add(R.id.main_layout, fragment, DashboardFragment.TAG);
			ft.commit();
			final Fragment oldFragment = manager.findFragmentByTag(DashboardBasicFragment.TAG);
			if(oldFragment != null)
				ft.remove(oldFragment);
		}

		/*
		 * if(manager.findFragmentByTag(PlotFragment.TAG) == null) { final FragmentTransaction ft = manager.beginTransaction(); final Fragment fragment = new PlotFragment(); ft.add(R.id.main_layout, fragment, PlotFragment.TAG); ft.commit(); }
		 */
		if(helper != null) helper.repopulate();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				if(helper != null) helper.clear();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		switch(requestCode)
		{
			case Constants.REQUEST_LICENSE_ACCEPTING:
				if(resultCode != RESULT_OK) finish();
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void showFragment(FragmentCreator creator)
	{
		final FragmentManager manager = getSupportFragmentManager();
		final FragmentTransaction ft = manager.beginTransaction();
		final Fragment foundFragment = manager.findFragmentByTag(creator.getTag());
		int fragment_layout = -1;

		int layout_id = findViewById(R.id.main_layout_second) == null ? R.id.main_layout : R.id.main_layout_second;
		if(findViewById(R.id.main_layout_second) != null && !helper.isEmpty()) layout_id = (helper.getLastLayoutId() == R.id.main_layout_second) ? R.id.main_layout : R.id.main_layout_second;
		if(foundFragment != null)
		{
			View view = foundFragment.getView();
			if(view != null && view.getParent() != null) // parents must be equal
			{
				ViewParent parent = view.getParent();
				if(parent == findViewById(R.id.main_layout))
					fragment_layout = R.id.main_layout;
				else if(parent == findViewById(R.id.main_layout_second)) fragment_layout = R.id.main_layout_second;
			}
		}
		if(fragment_layout != -1) layout_id = fragment_layout;
		// final int layout_id = findViewById(R.id.main_layout_second) == null ? R.id.main_layout : (position == 0 ? R.id.main_layout : R.id.main_layout_second);
		final Fragment newFragment = fragment_layout != -1 ? creator.update(foundFragment) : creator.create();
		ft.replace(layout_id, newFragment, creator.getTag());
		ft.setBreadCrumbTitle(creator.getTag());
		helper.add(creator, layout_id);

		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.addToBackStack(null);
		ft.commit();

	}

	public void showPorter(boolean export)
	{
		FragmentManager manager = getSupportFragmentManager();
		PorterDialog dialog = PorterDialog.createInstance(export);
		dialog.show(manager, PorterDialog.TAG);
	}

}
