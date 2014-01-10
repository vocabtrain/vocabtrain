package org.devwork.vocabtrain.sync;

import java.util.LinkedList;
import java.util.List;

import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.MainActivity;
import org.devwork.vocabtrain.OnFinishListener;
import org.devwork.vocabtrain.R;
import org.devwork.vocabtrain.TourTipsDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.example.android.ActionBarFragmentActivity;
import com.example.android.TabManager;

public class SyncActivity extends ActionBarFragmentActivity
{
	public interface DataChangedListener
	{
		public void dataChanged();
	}

	public static final String TAG = Constants.PACKAGE_NAME + ".SyncActivity";

	private TabHost tabHost = null;

	List<OnFinishListener> finishListeners = new LinkedList<OnFinishListener>();

	List<OnFinishListener> readyForSyncListeners = new LinkedList<OnFinishListener>();

	private DataChangedListener listener = null;

	private boolean finished = false;

	private boolean readyForSync = false;

	private BookData[] books = null;

	private LanguageData[] translation_languages = null;

	private UserData user = null;

	public void addOnFinishListener(final OnFinishListener listener)
	{
		finishListeners.add(listener);
	}

	public void addOnReadyForSyncListener(final OnFinishListener listener)
	{
		readyForSyncListeners.add(listener);
	}

	public void dataChanged()
	{
		if(listener != null) listener.dataChanged();
	}

	public BookData[] getBooks()
	{
		return books;
	}

	public LanguageData[] getTranslationLanguages()
	{
		return translation_languages;
	}

	public UserData getUserData()
	{
		return user;
	}

	public boolean isFinished()
	{
		return finished;
	}

	public boolean isReadyForSync()
	{
		return readyForSync;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_activity);

		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		if(tabHost != null)
		{
			tabHost.setup();
			final TabManager tabManager = new TabManager(this, tabHost, R.id.realtabcontent);

			tabManager.addTab(
					tabHost.newTabSpec(BookFragment.TAG).setIndicator(getString(R.string.sync_tab_books),
							getResources().getDrawable(R.drawable.ic_sync_tab_books)), BookFragment.class, null);
			tabManager.addTab(
					tabHost.newTabSpec(LanguageFragment.TAG).setIndicator(getString(R.string.sync_tab_languages),
							getResources().getDrawable(R.drawable.ic_sync_tab_language)), LanguageFragment.class, null);
			tabManager.addTab(
					tabHost.newTabSpec(CheckOutFragment.TAG).setIndicator(getString(R.string.sync_tab_checkout),
							getResources().getDrawable(R.drawable.ic_sync_tab_checkout)), CheckOutFragment.class, null);

			if(savedInstanceState != null)
			{
				tabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
			}

			if(Build.VERSION.SDK_INT == Build.VERSION_CODES.DONUT || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH))
			{
				final TabWidget tabwidget = (TabWidget) findViewById(android.R.id.tabs);
				if(tabwidget != null)
					tabwidget.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
			}

		}
		reset();
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
			case R.id.menu_refresh:
				onRefresh(new Runnable() {
					@Override
					public void run() {
						reset();
					}
				});
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(tabHost != null) outState.putString("tab", tabHost.getCurrentTabTag());
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		getActionBarHelper().onStart();
		TourTipsDialog.createInstance(TAG, this);
	}

	public void reset()
	{
		finished = readyForSync = false;

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		new TimestampLoader(this, prefs.getString("account_authToken", null), new TimestampLoader.OnFinishListener()
		{
			@Override
			public void onFinish(final BookData[] books, final LanguageData[] translation_languages, final UserData user)
			{
				Log.i(TAG, "" +  books + ", " + translation_languages + ", " + user);
				if(books == null || translation_languages == null)
				{
				return;
				/*
				 * if(isFinishing()) return; final AlertDialog.Builder builder = new AlertDialog.Builder(SyncActivity.this);
				 * builder.setMessage(getString(R.string.sync_no_internet)).setTitle(getString(R.string.sync_loader_error_title))
				 * .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
				 * 
				 * @Override public void onClick(final DialogInterface dialog, final int id) { finish(); } }); final AlertDialog alert = builder.create();
				 * alert.show(); return;
				 */
				}
				SyncActivity.this.user = user;
				SyncActivity.this.books = books;
				SyncActivity.this.translation_languages = translation_languages;
				for(final OnFinishListener listener : finishListeners)
					listener.onFinish();
				finished = true;
				if(user != null)
				{
					new SyncChangesLoader(SyncActivity.this, user, new OnFinishListener()
					{
						@Override
						public void onFinish()
						{
							for(final OnFinishListener listener : readyForSyncListeners)
								listener.onFinish();
							readyForSync = true;
						}

					}).execute((Void[]) null);
				}
				else
				readyForSync = true;
			}
		}).execute((Void[]) null);
	}

	public void setDataChangedListener(final DataChangedListener listener)
	{
		this.listener = listener;
	}

}
