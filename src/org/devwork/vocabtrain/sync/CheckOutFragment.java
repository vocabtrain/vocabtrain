package org.devwork.vocabtrain.sync;

import java.util.LinkedList;
import java.util.List;

import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.LanguageLocale;
import org.devwork.vocabtrain.OnFinishListener;
import org.devwork.vocabtrain.R;
import org.devwork.vocabtrain.sync.SyncActivity.DataChangedListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.ActionBarFragmentActivity;

public class CheckOutFragment extends Fragment implements OnFinishListener
{
	public static final String TAG = Constants.PACKAGE_NAME + ".CheckOutFragment";

	@Override
	public void onStart()
	{
		super.onStart();
		((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
	}

	/*
	 * private class UpdateLanguageAdapter extends ArrayAdapter<String> implements OnItemClickListener {
	 * 
	 * private final boolean[] check_states;
	 * 
	 * public UpdateLanguageAdapter(Context context, String[] array) { super(context, R.layout.sync_language_child, R.id.updatelanguage_child_caption, array); check_states = new boolean[array.length];
	 * 
	 * for(int i = 0; i < array.length; ++i) { Cursor c = getDatabaseHelper().getRead().rawQuery("SELECT COUNT(*) FROM translations where translation_language = ?", new String[] { array[i] } ); if( c.getCount() == 1 && c.moveToFirst()) { check_states[i] = c.getLong(0) == 0 ? false : true; } c.close(); }
	 * 
	 * }
	 * 
	 * @Override public View getView(final int position, View convertView, ViewGroup parent) { final View view = super.getView(position, convertView, parent); TextView caption = (TextView) view.findViewById(R.id.sync_language_child_caption); caption.setText( new Locale(caption.getText().toString()).getDisplayLanguage() );
	 * 
	 * final CheckBox checkbox = (CheckBox) view.findViewById(R.id.sync_language_child_check);
	 * 
	 * OnClickListener listener = new OnClickListener(){
	 * 
	 * @Override public void onClick(View v) { boolean state = ! check_states[position]; check_states[position] = state; checkbox.setChecked(state); } }; checkbox.setOnClickListener(listener); checkbox.setChecked(check_states[position]); checkbox.setFocusable(false); return view;
	 * 
	 * }
	 * 
	 * @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) { final CheckBox checkbox = (CheckBox) view.findViewById(R.id.sync_language_child_check); if(checkbox == null) return; boolean state = ! check_states[position]; check_states[position] = state; checkbox.setChecked(state); } }
	 */

	private TextView books_delete_label;
	private TextView books_add_label;
	private TextView books_update_label;
	private TextView lang_add_label;
	private TextView lang_delete_label;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.sync_checkout_fragment, container, false);

		books_delete_label = (TextView) v.findViewById(R.id.sync_checkout_books_delete_label);
		books_add_label = (TextView) v.findViewById(R.id.sync_checkout_books_add_label);
		books_update_label = (TextView) v.findViewById(R.id.sync_checkout_books_update_label);
		lang_add_label = (TextView) v.findViewById(R.id.sync_checkout_language_add_label);
		lang_delete_label = (TextView) v.findViewById(R.id.sync_checkout_language_delete_label);

		books_delete = (ListView) v.findViewById(R.id.sync_checkout_books_delete);
		books_add = (ListView) v.findViewById(R.id.sync_checkout_books_add);
		books_update = (ListView) v.findViewById(R.id.sync_checkout_books_update);
		lang_add = (ListView) v.findViewById(R.id.sync_checkout_language_add);
		lang_delete = (ListView) v.findViewById(R.id.sync_checkout_language_delete);

		filing_layout = v.findViewById(R.id.sync_checkout_filing_layout);
		filing_caption = (TextView) v.findViewById(R.id.sync_checkout_filing_caption);

		filing_upload = (Button) v.findViewById(R.id.sync_checkout_filing_upload);
		filing_download = (Button) v.findViewById(R.id.sync_checkout_filing_download);
		button = (Button) v.findViewById(R.id.sync_checkout_button);

		if(getActivity() instanceof SyncActivity)
		{
			final SyncActivity activity = (SyncActivity) getActivity();
			if(activity.isFinished())
			{
				onFinish();
			}
			else
				activity.addOnFinishListener(this);

			activity.setDataChangedListener(new DataChangedListener()
			{
				@Override
				public void dataChanged()
				{
					if(activity.isFinished()) onFinish();
				}
			});

			if(!activity.isReadyForSync())
			{
				button.setEnabled(false);
				filing_upload.setEnabled(false);
				filing_download.setEnabled(false);
				activity.addOnReadyForSyncListener(new OnFinishListener()
				{
					@Override
					public void onFinish()
					{
						button.setEnabled(true);
						filing_upload.setEnabled(true);
						filing_download.setEnabled(true);
					}
				});
			}

		}

		filing_upload.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final SyncActivity activity = (SyncActivity) getActivity();
				if(!activity.isReadyForSync()) return;
				final UserData user = activity.getUserData();
				if(user == null) return;
				if(user.getTimestamp() > user.getOldTimestamp())
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(getString(R.string.sync_checkout_ask_filing_upload)).setTitle(getString(R.string.sync_checkout_ask_filing_upload_title)).setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener()
					{
						public void onClick(final DialogInterface dialog, final int id)
						{
							dialog.dismiss();
							new UploadFilingLoader(activity, user, null).execute((Void[]) null);
						}
					}).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					final AlertDialog alert = builder.create();
					alert.show();
				}
				else
					new UploadFilingLoader(activity, user, null).execute((Void[]) null);
			}
		});
		filing_download.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final SyncActivity activity = (SyncActivity) getActivity();
				if(!activity.isReadyForSync()) return;
				final UserData user = activity.getUserData();
				if(user == null) return;

				if(user.getTimestamp() < user.getOldTimestamp())
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(getString(R.string.sync_checkout_ask_filing_download)).setTitle(getString(R.string.sync_checkout_ask_filing_download_title)).setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener()
					{
						public void onClick(final DialogInterface dialog, final int id)
						{
							dialog.dismiss();
							new DownloadFilingLoader(activity, user, null).execute((Void[]) null);
						}
					}).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
					{

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					final AlertDialog alert = builder.create();
					alert.show();
				}
				else
					new DownloadFilingLoader(activity, user, null).execute((Void[]) null);
			}
		});

		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final SyncActivity activity = (SyncActivity) getActivity();
				if(!activity.isReadyForSync()) return;
				new DownloadBooksLoader(activity, activity.getBooks(), activity.getTranslationLanguages(), new OnFinishListener()
				{
					@Override
					public void onFinish()
					{
						activity.reset();
						books_add.setAdapter(null);
						books_delete.setAdapter(null);
						books_update.setAdapter(null);
						lang_add.setAdapter(null);
						lang_delete.setAdapter(null);
					}
				}).execute((Void[]) null);
			}
		});

		return v;
	}

	private View filing_layout;

	private TextView filing_caption;
	private Button filing_upload;
	private Button filing_download;
	private Button button;
	private ListView books_delete;
	private ListView books_add;
	private ListView books_update;
	private ListView lang_add;
	private ListView lang_delete;

	@Override
	public void onFinish()
	{
		final List<String> lbooks_add = new LinkedList<String>();
		final List<String> lbooks_delete = new LinkedList<String>();
		final List<String> lbooks_update = new LinkedList<String>();
		final List<String> llang_add = new LinkedList<String>();
		final List<String> llang_delete = new LinkedList<String>();
		final SyncActivity activity = (SyncActivity) getActivity();
		if(activity == null) return;
		final BookData[] books = activity.getBooks();
		for(BookData book : books)
		{
			switch(book.getState())
			{
				case ADD:
					lbooks_add.add(book.toString());
					break;
				case UPDATE:
					lbooks_update.add(book.toString());
					break;
				case REMOVE:
					lbooks_delete.add(book.toString());
					break;
			}
		}
		final LanguageData[] languages = activity.getTranslationLanguages();
		for(LanguageData language : languages)
		{
			switch(language.getState())
			{
				case ADD:
					llang_add.add(new LanguageLocale(getActivity(), language.toString()).getDisplayLanguage());
					break;
				case REMOVE:
					llang_delete.add(new LanguageLocale(getActivity(), language.toString()).getDisplayLanguage());
			}
		}

		if(lbooks_add.isEmpty())
		{
			books_add.setVisibility(View.GONE);
			books_add_label.setVisibility(View.GONE);
		}
		else
		{
			books_add.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.sync_checkout_child, R.id.sync_checkout_child_caption, lbooks_add));
			books_add.setVisibility(View.VISIBLE);
			books_add_label.setVisibility(View.VISIBLE);
		}
		if(lbooks_delete.isEmpty())
		{
			books_delete.setVisibility(View.GONE);
			books_delete_label.setVisibility(View.GONE);
		}
		else
		{
			books_delete.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.sync_checkout_child, R.id.sync_checkout_child_caption, lbooks_delete));
			books_delete.setVisibility(View.VISIBLE);
			books_delete_label.setVisibility(View.VISIBLE);
		}
		if(lbooks_update.isEmpty())
		{
			books_update.setVisibility(View.GONE);
			books_update_label.setVisibility(View.GONE);
		}
		else
		{
			books_update.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.sync_checkout_child, R.id.sync_checkout_child_caption, lbooks_update));
			books_update.setVisibility(View.VISIBLE);
			books_update_label.setVisibility(View.VISIBLE);
		}
		if(llang_add.isEmpty())
		{
			lang_add.setVisibility(View.GONE);
			lang_add_label.setVisibility(View.GONE);
		}
		else
		{
			lang_add.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.sync_checkout_child, R.id.sync_checkout_child_caption, llang_add));
			lang_add.setVisibility(View.VISIBLE);
			lang_add_label.setVisibility(View.VISIBLE);
		}
		if(llang_delete.isEmpty())
		{
			lang_delete.setVisibility(View.GONE);
			lang_delete_label.setVisibility(View.GONE);
		}
		else
		{
			lang_delete.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.sync_checkout_child, R.id.sync_checkout_child_caption, llang_delete));
			lang_delete.setVisibility(View.VISIBLE);
			lang_delete_label.setVisibility(View.VISIBLE);
		}

		final UserData user = activity.getUserData();
		Log.e(TAG, "" + user);
		if(user == null)
		{
			filing_layout.setVisibility(View.GONE);
		}
		else
		{
			Log.e(TAG, "" + user.getTimestamp() + " <-> " + user.getOldTimestamp());

			filing_layout.setVisibility(View.VISIBLE);
			if(user.getTimestamp() == user.getOldTimestamp())
			{
				filing_caption.setText(getString(R.string.sync_checkout_filing_equalnew));
			}
			else if(user.getTimestamp() < user.getOldTimestamp())
			{
				filing_caption.setText(getString(R.string.sync_checkout_filing_localnew));
			}
			else
			{
				filing_caption.setText(getString(R.string.sync_checkout_filing_servernew));
			}
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		final MenuItem refresh = menu.findItem(R.id.menu_refresh);
		inflater.inflate(R.menu.refresh, menu);
		if(refresh != null) menu.removeItem(R.id.menu_refresh);
		super.onCreateOptionsMenu(menu, inflater);
	}

}
