package org.devwork.vocabtrain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;

import com.example.android.ActionBarFragmentActivity;

public class TrainingActivity extends ActionBarFragmentActivity
{

	// private boolean hasCustomTitleFeature;

	boolean clearselection_onDismiss = false;

	private OnActivityTouchListener onActivityTouchListener = null;

	private String fragmentName;

	@Override
	public boolean dispatchTouchEvent(final MotionEvent event)
	{
		if(onActivityTouchListener != null) onActivityTouchListener.onTouch(event);
		return super.dispatchTouchEvent(event);
	}

	public String getTrainingFragmentName()
	{
		return fragmentName;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final boolean useFullscreen = prefs.getBoolean("fullscreen", false);
		final boolean invertColors = prefs.getBoolean("invert_colors", false);
		if(useFullscreen)
		{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		if(invertColors)
		{
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) setTheme(android.R.style.Theme_Holo);
			else setTheme(R.style.ActionBarDarkTheme);
		}
		super.onCreate(savedInstanceState);

		setContentView(R.layout.training_activity);

		final Bundle extras = getIntent().getExtras();
		fragmentName = extras.getString("fragment");
		clearselection_onDismiss = extras.getBoolean("clearselection_onDismiss");
		if(fragmentName == null) return;

		final FragmentManager fragmentManager = getSupportFragmentManager();
		final Fragment prev = fragmentManager.findFragmentByTag(fragmentName);
		if(prev != null)
		{

		}
		else
		{

			try
			{
				final Class<?> classDefinition = Class.forName(fragmentName);
				final TrainingFragment fragment = (TrainingFragment) classDefinition.newInstance();
				final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

				fragmentTransaction.add(R.id.training_activity_layout, fragment, fragmentName);
				fragmentTransaction.commit();
			}
			catch(final ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				Log.e("H:", e.toString());
			}
			catch(final InstantiationException e)
			{
				// TODO Auto-generated catch block
				Log.e("H:", e.toString());

			}
			catch(final IllegalAccessException e)
			{
				// TODO Auto-generated catch block
				Log.e("H:", e.toString());

			}

		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(clearselection_onDismiss && isFinishing())
		{
			final DatabaseHelper dbh = new DatabaseHelper(this);
			dbh.clearSelection();
			dbh.close();
		}
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

	@Override
	public void onStart()
	{
		super.onStart();
		changeOrientation();
	}

	public void setOnActivityTouchListener(final OnActivityTouchListener onActivityTouchListener)
	{
		this.onActivityTouchListener = onActivityTouchListener;
	}

	public void showFragment(final FragmentCreator creator)
	{
		final FragmentManager manager = getSupportFragmentManager();
		final FragmentTransaction ft = manager.beginTransaction();
		final Fragment foundFragment = manager.findFragmentByTag(creator.getTag());
		boolean fragmentExists = false;
		if(foundFragment != null)
		{
			final View view = foundFragment.getView();
			if(view != null && view.getParent() != null) // parents must be equal
			{
				final ViewParent parent = view.getParent();
				if(parent == findViewById(R.id.training_activity_layout)) fragmentExists = true;
			}
		}
		ft.replace(R.id.training_activity_layout, fragmentExists ? creator.update(foundFragment) : creator.create(), creator.getTag());
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.addToBackStack(null);
		ft.commit();
	}

}
