package org.devwork.vocabtrain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import org.devwork.vocabtrain.sync.SyncChangesLoader;
import org.devwork.vocabtrain.sync.UploadFilingLoader;
import org.devwork.vocabtrain.sync.UserData;
import org.tatoeba.providers.ProviderInterface;

import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ActionBarFragmentActivity;

public class TrainingFragment extends DatabaseFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		if(prefs.getBoolean("tatoeba", true)) try
		{
			getActivity().getPackageManager().getPackageInfo("org.tatoeba", PackageManager.GET_ACTIVITIES);
			LoaderManager.enableDebugLogging(true);
			Bundle bundle = new Bundle();
			bundle.putLong("id", playlist.getCurrentCard().getId());
			tatoeba_cursor_init = true;
			getActivity().getSupportLoaderManager().initLoader(CURSORLOADER_TATOEBA, bundle, TrainingFragment.this);
		}
		catch(final NameNotFoundException e)
		{
		}
		
	}


	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
	{
		Log.e("TatoebaButton", "Creating " + id);
		if(id != CURSORLOADER_TATOEBA) return null;
		if(playlist == null) return null;
		final Card card = playlist.getCurrentCard();
		if(card == null) return null;
		tatoeba_button.setText(getString(R.string.button_tatoeba) + " ...");
		tatoeba_button.setEnabled(false);
		tatoeba_button_drawable.setOneShot(false);
		tatoeba_button_drawable.start();
		
		tatoeba_language = card.getLanguage();
		String search = card.getScript();
		if(card.getLanguage().equals(LanguageLocale.Language.JAPANESE) && card.getSpeech() != null && card.getSpeech().length() > 0)
			search += " " + card.getSpeech();
		Log.e("TatoebaButton", "Created " + id);
		return new CursorLoader(getActivity(), ProviderInterface.SearchTable.CONTENT_URI, new String[] { ProviderInterface.LinkTable._ID,
				ProviderInterface.LinkTable.CONTENT }, null, new String[] { search, card.getLanguage().toString() }, "" + tatoeba_length);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader)
	{
		Log.e("TatoebaButton", "Reset");
		// adapter.changeCursor(null);
		
	}
	boolean tatoeba_cursor_init;
	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
	{
		Log.e("TatoebaButton", "Finished");
		tatoeba_button_drawable.stop();
		if(tatoeba_cursor_init) 
		{ 
			tatoeba_cursor_init = false; 
			tatoeba_button.setText(getString(R.string.button_tatoeba));
			tatoeba_button.setOnClickListener(tatoeba_clicklistener);
			tatoeba_button.setEnabled(true);
			return; 
		}
		// tatoeba_button.setCompoundDrawables(null, null, null, null);
		tatoeba_button.setText(getString(R.string.button_tatoeba) + " (" + (data == null ? 0 : data.getCount()) + ")");
		// Log.e("DATA", "" + data);
		tatoeba_button.setEnabled(true);
		if(data == null || data.getCount() == 0) return;

		tatoeba_button.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(final View v)
			{
				((TrainingActivity) getActivity()).showFragment(new FragmentCreator()
				{
					@Override
					public Fragment create()
					{
						return new TatoebaFragment(data, tatoeba_language);
					}

					@Override
					public boolean equals(final Fragment fragment)
					{
						return (fragment instanceof TatoebaFragment);
					}

					@Override
					public String getTag()
					{
						return TatoebaFragment.TAG;
					}

					@Override
					public Fragment update(final Fragment fragment)
					{
						((TatoebaFragment) fragment).changeCursor(data);
						return fragment;
					}

				});
				tatoeba_button.setText(getString(R.string.button_tatoeba));
				tatoeba_button.setOnClickListener(tatoeba_clicklistener);
			}

		});

		Log.e("DATACOLS", "" + data.getCount());
	}
	public final static int CURSORLOADER_TATOEBA = 1;
	private int tatoeba_length;

	private Button tatoeba_button;

	private LanguageLocale tatoeba_language;

	private final OnClickListener tatoeba_clicklistener = new OnClickListener()
	{
		@Override
		public void onClick(final View v)
		{
			Log.e("TatoebaButton", "Clicked");
			Bundle bundle = new Bundle();
			bundle.putLong("id", playlist.getCurrentCard().getId());
			getActivity().getSupportLoaderManager().restartLoader(CURSORLOADER_TATOEBA, bundle, TrainingFragment.this);
			
		}
	};

	private AnimationDrawable tatoeba_button_drawable;

	
	
	
	
	private class ActivityTouchListener implements OnActivityTouchListener
	{

		private GestureDetector gestureDetector = null;
		private float distance;
		private GestureMode gesture = GestureMode.NONE;
		final static float DISTANCE_GAP = 13.0f;

		@Override
		public boolean onTouch(final MotionEvent event)
		{
			switch(event.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_POINTER_DOWN:
					if(gesture != GestureMode.PINCH)
					{
						distance = DatabaseFunctions.getDistance(event);
						if(distance > DISTANCE_GAP)
						{
							gesture = GestureMode.PINCH;
							return true;

						}
						Log.d(TAG, "PointerDown : " + distance + " - " + gesture);
					}
					break;
				case MotionEvent.ACTION_POINTER_UP:
					gesture = GestureMode.NONE;
					Log.d(TAG, "PointerUp");
					break;
				case MotionEvent.ACTION_MOVE:
					if(gesture == GestureMode.PINCH)
					{
						final float new_distance = DatabaseFunctions.getDistance(event);

						if(new_distance > DISTANCE_GAP)
						{
							final float scale = new_distance / distance;
							Log.e(TAG, "Scale: " + scale);
							scaleTextView(first, scale);
							scaleTextView(second, scale);
							scaleTextView(third, scale);
							scaleTextView(first_comment, scale);
							scaleTextView(second_comment, scale);
							scaleTextView(third_comment, scale);

							distance = new_distance;
							return true;
						}
						Log.d(TAG, "Move : " + distance + " -> " + new_distance);

					}

					break;
			}

			return (gestureDetector == null) ? false : gestureDetector.onTouchEvent(event);
		}

		private void scaleTextView(final TextView tv, final float scale)
		{
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() * scale);
		}

	}

	private class CardMarker extends AsyncTask<Void, Void, Void>
	{
		final private int grade;
		final long card_id;

		private int days_diff = 0;

		int interval;

		public CardMarker(final long card_id, final int grade)
		{
			this.card_id = card_id;
			this.grade = grade;
		}

		@Override
		protected Void doInBackground(final Void... params)
		{
			final DatabaseHelper helper = new DatabaseHelper(getActivity());
			final SQLiteDatabase db = helper.getWritableDatabase();
			try
			{
				final Cursor cursor = db.query("filing", new String[] { "filing_session", "filing_interval", "filing_grades", "filing_priority",
						"filing_count", "filing_difficulty" }, "filing_card_id = ? AND filing_sequence = ?", new String[] { "" + card_id, "" + sequence },
						null, null, null);
				if(cursor.getCount() == 0 || !cursor.moveToFirst())
				{
					cursor.close();
					return null;
				}
				// int session = cursor.getInt(cursor.getColumnIndex("filing_session"));
				interval = cursor.getInt(cursor.getColumnIndex("filing_interval"));
				int grades = cursor.getInt(cursor.getColumnIndex("filing_grades"));
				final int priority = cursor.getInt(cursor.getColumnIndex("filing_priority"));
				int count = cursor.getInt(cursor.getColumnIndex("filing_count"));
				cursor.close();
				if(resetForgotten && grade == 0 && grades > 0) // HAVOC -> reset
				{
					grades = 0;
					count = 0;
				}
				
				final float average_grade = (float) grades / (float) count;

				final double ideal_interval = Math.pow(count, 2.5) * Math.exp(5 - priority);
				final float difficulty = (float) Math.log((ideal_interval + 1.0f) / (interval + 1.0));
				int ssrf_min = (int) Math.floor(Math.pow(count, average_grade / 2.) * Math.exp(grade - 1. - priority)) + 1; // +1 so that the worst interval is
																															// 1, not 0 -> in each session only
																															// ONCE a card is revwied
				Log.e("INTDATA", "grades: " + grades + " priority: " + priority + ", count " + count + ", avggrade " + average_grade + ", ideal_int = " 
						+ ideal_interval + ", difficulty " + difficulty);
				
				int ssrf_max = (int) Math.ceil(Math.pow(count, average_grade / 2.) * Math.exp(grade - priority)) + 1;

				Log.e("SSRF", "" + ssrf_min + " - " + ssrf_max);
				
				final int[] workloads = new int[ssrf_max - ssrf_min];
				final float[] difficulties = new float[ssrf_max - ssrf_min];
				final double[] load_diffs = new double[ssrf_max - ssrf_min];
				int new_interval = ssrf_min;
				if(ssrf_max > ssrf_min)
				{
					if(ssrf_days_lookup > 0 && ssrf_max - ssrf_min > ssrf_days_lookup + 1)
					{
						final int diff = (ssrf_max - ssrf_min - ssrf_days_lookup - 1) / 2;
						ssrf_min += diff;
						ssrf_max -= diff;
					}
					final int[] new_workloads = new int[ssrf_max - ssrf_min];
					final float[] new_difficulties = new float[ssrf_max - ssrf_min];

					for(int proposed_interval = ssrf_min; proposed_interval < ssrf_max; ++proposed_interval)
					{
						final Cursor c = db
								.rawQuery(
										"SELECT COUNT(*), SUM(`filing_difficulty`) FROM `filing` WHERE `filing_card_id` != ? AND `filing_interval`+`filing_session` = ? AND filing_sequence = ?",
										new String[] { "" + card_id, "" + (session + proposed_interval), "" + sequence });
						if(c.getCount() == 0 || !c.moveToFirst())
						{
							c.close();
							continue;
						}
						workloads[proposed_interval - ssrf_min] = c.getInt(0);
						difficulties[proposed_interval - ssrf_min] = c.getFloat(1) / workloads[proposed_interval - ssrf_min];
						new_workloads[proposed_interval - ssrf_min] = c.getInt(0) + 1;
						new_difficulties[proposed_interval - ssrf_min] = (c.getFloat(1) + difficulty) / workloads[proposed_interval - ssrf_min];
						c.close();
					}

					final int minimal_workload = findMinimum(workloads);
					final float minimal_difficulty = findMinimum(difficulties);
					final int minimal_new_workload = findMinimum(new_workloads);
					final float minimal_new_difficulty = findMinimum(new_difficulties);

					for(int proposed_interval = ssrf_min; proposed_interval < ssrf_max; ++proposed_interval)
					{
						final int i = proposed_interval - ssrf_min;
						final float w = workloads[i] == 0 ? 0f : minimal_workload / workloads[i];
						final float d = difficulties[i] == 0 ? 0f : minimal_difficulty / difficulties[i];
						final double load = (Math.pow(w, 2) + Math.pow(d, 2)) / 2.;

						final float new_w = new_workloads[i] == 0 ? 0 : minimal_new_workload / new_workloads[i];
						final float new_d = new_difficulties[i] == 0 ? 0 : minimal_new_difficulty / new_difficulties[i];
						final double new_load = (Math.pow(new_w, 2) + Math.pow(new_d, 2)) / 2.;
						load_diffs[proposed_interval - ssrf_min] = load / new_load - 1.0;

					}
					new_interval = findMinimumPosition(load_diffs) + ssrf_min;
					//Log.e("NEWINTERVAL", "" + new_interval+ ", " + helper.getCardById(card_id).getFiling());
				}

				{

					final ContentValues c = new ContentValues();
					c.put("filing_session", session);
					c.put("filing_interval", new_interval);
					c.put("filing_grades", grades + grade);
					c.put("filing_count", count + 1);
					c.put("filing_difficulty", difficulty);
					db.update("filing", c, "filing_card_id = ? AND filing_sequence = ?", new String[] { "" + card_id, "" + sequence });
					Log.e(TAG, "session: " + session);
				}
				days_diff = new_interval - interval;
				switch(grade)
				{
					case 0:
						db.execSQL("UPDATE `filing` SET `filing_rank` = '0' WHERE `filing_card_id` = ? AND filing_sequence = ?", new String[] { "" + card_id,
								"" + sequence });
						break;
					case 1:
						db.execSQL("UPDATE `filing` SET `filing_rank` = max(0,`filing_rank`-'2') WHERE `filing_card_id` = ? AND filing_sequence = ?",
								new String[] { "" + card_id, "" + sequence });
						break;
					case 2:
						db.execSQL("UPDATE `filing` SET `filing_rank` = max(0,`filing_rank`-'1') WHERE `filing_card_id` = ? AND filing_sequence = ?",
								new String[] { "" + card_id, "" + sequence });
						break;
					case 3:
						break;
					case 4:
						db.execSQL("UPDATE `filing` SET `filing_rank` = `filing_rank`+'1' WHERE `filing_card_id` = ? AND filing_sequence = ?", new String[] {
								"" + card_id, "" + sequence });
						break;
					case 5:
						db.execSQL("UPDATE `filing` SET `filing_rank` = `filing_rank`+'2' WHERE `filing_card_id` = ? AND filing_sequence = ?", new String[] {
								"" + card_id, "" + sequence });
						break;
				}
				markCardAsForgotten(db, grade, card_id);

				db.execSQL("UPDATE filing_data SET `filing_timestamp` = strftime('%s','now') WHERE filing_sequence = ?", new String[] { "" + sequence });
			}
			catch(final SQLiteException e)
			{

				if(!getActivity().isFinishing())
				{
					getActivity().runOnUiThread(new Runnable()
					{

						@Override
						public void run()
						{
							final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setMessage(getString(R.string.database_error, e.getMessage())).setTitle(getString(R.string.database_error_title))
									.setPositiveButton(getString(R.string.button_retry), new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(final DialogInterface dialog, final int id)
										{
											cancel(true);
											marker = new CardMarker(card_id, grade);
											marker.execute();
										}
									}).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
									{
										@Override
										public void onClick(final DialogInterface dialog, final int id)
										{
											dialog.dismiss();
											OnUndo();
										}
									});
							final AlertDialog alert = builder.create();
							alert.show();
						}
					});
				}
			}
			finally
			{
				db.close();
				helper.close();
			}
			return null;
		}

		private Button getButtonFromGrade()
		{
			switch(grade)
			{
				case 0:
					return button_havoc;
				case 1:
					return button_bad;
				case 2:
					return button_tricky;
				case 3:
					return button_good;
				case 4:
					return button_easy;
				case 5:
					return button_trivial;
			}
			return button_good;
		}

		@Override
		protected void onPostExecute(final Void result)
		{
			if(isCancelled()) return;
			if(frame_caption != null) frame_caption.setText("");
			marker = null;
			if(frame_days_diff != null)
				frame_days_diff.setText("" + interval + (days_diff > 0 ? "+" + days_diff : (days_diff == 0 ? "\u00B10" : ("" + days_diff))));
		}

		@Override
		protected void onPreExecute()
		{
			if(frame_caption != null) frame_caption.setText(getButtonFromGrade().getText() + "...");
		}

	}

	private static enum CardStatus
	{
		BADSTATE, OLDSTATE, NEWSTATE, NONE
	}

	private enum GestureMode
	{
		NONE, PINCH
	}


	public static final String TAG = "TrainingActivity";

	private static float findMinimum(final float[] array)
	{
		float min = array[0];
		for(final float a : array)
			if(a < min) min = a;
		return min;
	}

	private static int findMinimum(final int[] array)
	{
		int min = array[0];
		for(final int a : array)
			if(a < min) min = a;
		return min;
	}

	private static int findMinimumPosition(final double[] array)
	{
		int position = 0;
		for(int i = 1; i < array.length; ++i)
			if(array[i] < array[position]) position = i;
		return position;
	}

	private static void markCardAsForgotten(final SQLiteDatabase db, final int grade, final long card_id)
	{
		if(grade < 3)
		{
			final ContentValues c = new ContentValues();
			c.put("selection_forgotten", true);
			db.update("selection", c, "selection_card_id = ?", new String[] { "" + card_id });
		}
	}

	private static void setNormalNumber(final TextView tv, final int value)
	{
		tv.setText("" + value);
	}

	private static void setUnderlinedNumber(final TextView tv, final int value)
	{
		final SpannableString content = new SpannableString("" + value);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		tv.setText(content);
	}

	private Playlist playlist = null;

	private SequenceManager seq;

	private boolean hasTitleBar;

	private int ssrf_days_lookup = 1;
	private boolean hasSecondQuestion;
	private boolean inSecondAnswer;
	private boolean resetForgotten;
	private boolean useFractals;
	private boolean firstIsEmpty = false;
	private final int layoutResID;
	private TextView first;
	private TextView second;

	private TextView third;

	private TextView first_comment;
	private TextView second_comment;
	private TextView third_comment;
	private View button_layout = null;
	private View button_layout_forgotten = null;
	private View button_layout_fresh = null;
	private ImageView frame_flag;
	private int cards_count;
	private int new_cards_count;
	private int old_cards_count;
	private int bad_cards_count;
	private TextView frame_caption;
	private TextView frame_card_type;

	private TextView frame_cards_count;
	private TextView frame_new_cards_count;

	private TextView frame_days_diff;
	private TextView frame_old_cards_count;

	private TextView frame_bad_cards_count;
	private ProgressBar frame_progressbar;
	private int session = 0;

	private boolean hasNoSpeech;

	private final LinkedList<Playlist.Undo> undolist = new LinkedList<Playlist.Undo>();
	private int undolist_size = 50; // TODO: Testvalue!

	private boolean readWithTTS;
	private int sequence = Sequence.DEFAULT_SEQUENCE;
	private Button button_easy;

	private Button button_good;
	private Button button_trivial;
	private Button button_tricky;

	private Button button_bad;
	private Button button_havoc;

	private View contentView;

	private FractalGenerator fractalgenerator = null;

	private volatile CardMarker marker = null;

	private TextToSpeech tts = null;

	/*
	 * @Override public Object onRetainNonConfigurationInstance() { return playlist; }
	 */// TODO !!!
	private boolean useCompactLayout = false;
	private LanguageLocale previous_language = null;
	private float commentTextSize = -1f;

	private MenuItem undoMenuItem = null;

	private TextView onContextMenu;

	private final ActivityTouchListener activityTouchListener = new ActivityTouchListener();

	private boolean useTTS;

	private boolean customSkin;
	private String customSkinDirectory;

	public TrainingFragment(final int layoutResID)
	{
		this.layoutResID = layoutResID;
	}
	private final static String PREFIX_SKINFILE = "training_"; 

	public void changeSkin(final int grade)
	{
		if(useFractals) return;
		String filename;
		switch(grade)
		{
			case -1:
				filename = PREFIX_SKINFILE + "recalled";
				break;
			case 0:
				filename = PREFIX_SKINFILE + "havoc";
				break;
			case 1:
				filename = PREFIX_SKINFILE + "bad";
				break;
			case 2:
				filename = PREFIX_SKINFILE + "tricky";
				break;
			case 3:
				filename = PREFIX_SKINFILE + "good";
				break;
			case 4:
				filename = PREFIX_SKINFILE + "easy";
				break;
			case 5:
				filename = PREFIX_SKINFILE + "trivial";
				break;
			default:
				return;
		}
		File file = new File(customSkinDirectory + File.separator + filename + ".jpg");
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			final File portrait = new File(customSkinDirectory + File.separator + filename + "_land.jpg");
			if(portrait.isFile()) file = portrait;
		}
		if(!file.isFile()) return;
		final Bitmap bitmap = BitmapFactory.decodeFile(file.toString());
		if(bitmap == null) return;
		final BitmapDrawable drawable = new BitmapDrawable(getActivity().getResources(), bitmap);
		if(drawable != null)
		{
			drawable.setAlpha(50);
			getView().setBackgroundDrawable(drawable);
		}
	}

	protected Card getCurrentCard()
	{
		return playlist.getCurrentCard();
	}

	protected SequenceManager getSeq()
	{
		return seq;
	}

	public boolean hasSecondQuestion()
	{
		return hasSecondQuestion;
	}

	public boolean isSecondAnswer()
	{
		return inSecondAnswer;
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data)
	{
		switch(requestCode)
		{
			case Constants.REQUEST_VOICE_REC_FOR_TEXTCARD:
				if(resultCode == Activity.RESULT_OK)
				{
					final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					OnVoiceRequest(matches);
				}
				break;
			case Constants.REQUEST_CARD_EDIT:
				playlist.update();
				OnReset();
				break;
			case Constants.REQUEST_TTS:
			{
				if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
				{
					tts = new TextToSpeech(getActivity(), new OnInitListener()
					{
						@Override
						public void onInit(final int status)
						{
							if(status == TextToSpeech.ERROR)
							{
								tts = null;
								return;
							}

							switch(tts.isLanguageAvailable(seq.getVernicularLocale().getLocale()))
							{

								case TextToSpeech.LANG_NOT_SUPPORTED:
								{
									final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
									builder.setMessage(getString(R.string.tts_language_not_supported, seq.getVernicularLocale().getDisplayLanguage()))
											.setTitle(getString(R.string.tts_error_title))
											.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
											{
												@Override
												public void onClick(final DialogInterface dialog, final int id)
												{
													dialog.dismiss();
												}
											});
									final AlertDialog alert = builder.create();
									alert.show();
								}
									return;
								case TextToSpeech.LANG_MISSING_DATA:
								{
									final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
									builder.setMessage(
											String.format(getString(R.string.tts_language_missing_data), seq.getVernicularLocale().getDisplayLanguage()))
											.setTitle(getString(R.string.tts_error_title))
											.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
											{
												@Override
												public void onClick(final DialogInterface dialog, final int id)
												{
													dialog.dismiss();
												}
											});
									final AlertDialog alert = builder.create();
									alert.show();
								}
									return;
							}
							final Cursor c = getDatabaseHelper()
									.getRead()
									.rawQuery(
											"select book_language from books join chapters on chapter_book_id = books._id join content on content_chapter_id = chapters._id join selection on content_card_id = selection_card_id group by book_language",
											null);
							while(c.moveToNext())
							{

								final Locale locale = new Locale(c.getString(0));
								switch(tts.isLanguageAvailable(locale))
								{
									case TextToSpeech.LANG_NOT_SUPPORTED:
									{
										final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
										builder.setMessage(String.format(getString(R.string.tts_language_not_supported), locale.getDisplayName()))
												.setTitle(getString(R.string.tts_error_title))
												.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
												{
													@Override
													public void onClick(final DialogInterface dialog, final int id)
													{
														dialog.dismiss();
													}
												});
										final AlertDialog alert = builder.create();
										alert.show();
									}
										break;
									case TextToSpeech.LANG_MISSING_DATA:
									{
										final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
										builder.setMessage(String.format(getString(R.string.tts_language_missing_data), locale.getDisplayName()))
												.setTitle(getString(R.string.tts_error_title))
												.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
												{
													@Override
													public void onClick(final DialogInterface dialog, final int id)
													{
														dialog.dismiss();
													}
												});
										final AlertDialog alert = builder.create();
										alert.show();
									}
										break;
								}
							}
							c.close();

						}
					});
				}

				else
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(getString(R.string.tts_missing)).setTitle(getString(R.string.tts_error_title))
							.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
									dialog.dismiss();
								}
							}).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
									dialog.dismiss();
									final Intent installIntent = new Intent();
									installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
									startActivity(installIntent);
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();

				}
			}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		sequence = DatabaseFunctions.getSequence(getActivity());
		seq = new SequenceManager(getActivity(), getDatabaseHelper());
		{
			final SQLiteDatabase db = getDatabaseHelper().getRead();
			final Cursor sessionCursor = db.query("filing_data", new String[] { "filing_session" }, "filing_sequence = ?", new String[] { "" + sequence },
					null, null, null);
			if(sessionCursor.getCount() == 1 && sessionCursor.moveToFirst()) session = sessionCursor.getInt(0);
			else 
			{
				final SQLiteDatabase dbw = getDatabaseHelper().getWritableDatabase();
				final ContentValues c = new ContentValues();
				c.put("filing_sequence", sequence);
				dbw.insert("filing_data", null,c);
				dbw.close();
			}
			Log.e(TAG, "session: " + session);
			sessionCursor.close();
			final Cursor hasNoSpeechCursor = db.rawQuery(
					"SELECT COUNT(*) FROM `selection` JOIN `cards` ON `cards`.`_id` = `selection_card_id` WHERE `card_speech` is not null", null);
			if(hasNoSpeechCursor.getCount() == 1 && hasNoSpeechCursor.moveToFirst()) hasNoSpeech = hasNoSpeechCursor.getInt(0) == 0;
			hasNoSpeechCursor.close();

			Log.e(TAG, "NoSpeech: " + hasNoSpeech + " " + seq.getThird());
			if(!hasNoSpeech && seq.getThird().getType() == Sequence.SPEECH) // speech may be empty -> discard
			{
				Log.e(TAG, "NoSpeech");
				final SQLiteDatabase dbw = getDatabaseHelper().getWritableDatabase();
				dbw.execSQL("delete from selection where selection._id in ( SELECT `selection`.`_id` FROM `selection` JOIN `cards` ON `selection_card_id` = `cards`.`_id` WHERE `card_speech` is null ) ");
				dbw.close();
			}

		}

		playlist = new Playlist(getActivity(), getDatabaseHelper());
		playlist.shuffle();
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v instanceof TextView)
		{
			if(((TextView) v).getText().length() == 0) return;
			final IconContextMenu cm = new IconContextMenu(getActivity());
			final MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.training_context, cm.getMenu());
			inflater.inflate(R.menu.dicts, cm.getMenu());
			cm.setOnIconContextItemSelectedListener(new IconContextItemSelectedListener()
			{

				@Override
				public void onIconContextItemSelected(final MenuItem item)
				{
					final TextView tv = onContextMenu;
					if(tv == null) return;
					final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
					final boolean useRomanji = prefs.getBoolean("romaji", false);

					String text = tv.getText().toString();
					if(useRomanji)
					{
						final String[] texts = text.split("\n");
						if(texts.length == 2)
						{
							final CharacterTranslator c = CharacterTranslator.getCharacterTranslator();
							if((!c.fromHiragana(texts[0]).equals(texts[0]) || !c.fromKatakana(texts[0]).equals(texts[0]))
									&& c.fromHiragana(texts[1]).equals(c.fromKatakana(texts[1]))) text = texts[0];
						}
					}
					SearchIntents.search(item.getItemId(), text, playlist.getCurrentCard(), getActivity());
				}
			});
			onContextMenu = (TextView) v;
			SearchIntents.disableMissingMenuEntries(cm.getMenu(), getActivity(), playlist.getCurrentCard());
			cm.show();
		}
	}
	/*
	 * @Override public void onCreateContextMenu3(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
	 * 
	 * super.onCreateContextMenu(menu, v, menuInfo); if(v instanceof TextView) { final MenuInflater inflater = getActivity().getMenuInflater();
	 * inflater.inflate(R.menu.training_context, menu); inflater.inflate(R.menu.dicts, menu); onContextMenu = (TextView) v;
	 * SearchIntents.disableMissingMenuEntries(menu, getActivity(), playlist.getCurrentCard()); } }
	 * 
	 * @Override public boolean onContextItemSelected(final MenuItem item) { final TextView tv = onContextMenu; if(tv == null) return
	 * super.onContextItemSelected(item); final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()); final boolean useRomanji
	 * = prefs.getBoolean("romaji", false);
	 * 
	 * String text = tv.getText().toString(); if(useRomanji) { final String[] texts = text.split("\n"); if(texts.length == 2) { final CharacterTranslator c =
	 * CharacterTranslator.getCharacterTranslator(); if((!c.fromHiragana(texts[0]).equals(texts[0]) || !c.fromKatakana(texts[0]).equals(texts[0])) &&
	 * c.fromHiragana(texts[1]).equals(c.fromKatakana(texts[1]))) text = texts[0]; } } if(SearchIntents.search(item.getItemId(), text,
	 * playlist.getCurrentCard(), getActivity())) return true; return super.onContextItemSelected(item); }
	 */
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		inflater.inflate(R.menu.training_option, menu);

		undoMenuItem = menu.findItem(R.id.menu_training_undo);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) IncompatibleFunctions.createSearchView(getActivity(), menu, R.id.menu_training_search);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		setHasOptionsMenu(true);
		setRetainInstance(true);

		if(getActivity() instanceof TrainingActivity) ((TrainingActivity) getActivity()).setOnActivityTouchListener(activityTouchListener);

		final View v = inflater.inflate(layoutResID, container, false);
		hasTitleBar = v.findViewById(R.id.training_titlebar_layout) != null;

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		resetForgotten = prefs.getBoolean("reset_forgotten", false);
		useFractals = prefs.getBoolean("use_fractals", false);
		customSkin = prefs.getBoolean("custom_skin", false);
		customSkinDirectory = prefs.getString("custom_skin_directory", Environment.getExternalStorageDirectory().toString() + "/skin");
		tatoeba_length = prefs.getInt("tatoeba_length", 20);
		hasSecondQuestion = prefs.getBoolean("second_question", true);
		final int buttonsize_ratio = prefs.getInt("buttonsize", -1);
		undolist_size = prefs.getInt("undo_listsize", undolist_size);
		useTTS = prefs.getBoolean("tts", false);
		readWithTTS = useTTS ? prefs.getBoolean("tts_auto", false) : false;
		ssrf_days_lookup = prefs.getInt("ssrf_days_lookup", 1);
		inSecondAnswer = false;

		first = (TextView) v.findViewById(R.id.training_first);
		second = (TextView) v.findViewById(R.id.training_second);
		third = (TextView) v.findViewById(R.id.training_third);
		first_comment = (TextView) v.findViewById(R.id.training_first_comment);
		second_comment = (TextView) v.findViewById(R.id.training_second_comment);
		third_comment = (TextView) v.findViewById(R.id.training_third_comment);
		tatoeba_button = (Button) v.findViewById(R.id.training_tatoeba);
		tatoeba_button_drawable = (AnimationDrawable) getActivity().getResources().getDrawable(R.drawable.btn_tatoeba);
		tatoeba_button.setCompoundDrawablesWithIntrinsicBounds(tatoeba_button_drawable, null, null, null);

		if(prefs.getBoolean("tatoeba", true)) try
		{
			getActivity().getPackageManager().getPackageInfo("org.tatoeba", PackageManager.GET_ACTIVITIES);
			tatoeba_button.setOnClickListener(tatoeba_clicklistener);
			tatoeba_button.setEnabled(true);
			tatoeba_button.setVisibility(View.VISIBLE);
		}
		catch(final NameNotFoundException e)
		{
		}

		registerForContextMenu(first);
		registerForContextMenu(second);
		registerForContextMenu(third);
		registerForContextMenu(first_comment);
		registerForContextMenu(second_comment);
		registerForContextMenu(third_comment);

		final int width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		new TextViewShrinker(getActivity(), first, width);
		new TextViewShrinker(getActivity(), second, width);
		new TextViewShrinker(getActivity(), third, width);

		final Button button_forgotten = (Button) v.findViewById(R.id.training_button_forgotten);
		final Button button_recall = (Button) v.findViewById(R.id.training_button_recall);

		button_easy = (Button) v.findViewById(R.id.training_button_easy);
		button_good = (Button) v.findViewById(R.id.training_button_good);
		button_trivial = (Button) v.findViewById(R.id.training_button_trivial);

		button_tricky = (Button) v.findViewById(R.id.training_button_tricky);
		button_bad = (Button) v.findViewById(R.id.training_button_bad);
		button_havoc = (Button) v.findViewById(R.id.training_button_havoc);

		if(hasTitleBar)
		{
			final SQLiteDatabase db = getDatabaseHelper().getRead(); // will be closed under certain conditions, thus fetch a possible new one
			frame_caption = (TextView) v.findViewById(R.id.training_titlebar_caption);
			frame_cards_count = (TextView) v.findViewById(R.id.training_titlebar_count);
			frame_card_type = (TextView) v.findViewById(R.id.training_titlebar_card_type);
			frame_flag = (ImageView) v.findViewById(R.id.training_titlebar_image);
			frame_progressbar = (ProgressBar) v.findViewById(R.id.training_titlebar_progress);
			cards_count = playlist.getCount();
			frame_progressbar.setMax(cards_count);
			frame_progressbar.setVisibility(ProgressBar.VISIBLE);
			frame_caption.setText(""); // TODO

			Cursor c = db
					.rawQuery(
							"SELECT COUNT(*) FROM `selection` JOIN `filing` ON `filing_card_id` = `selection_card_id` WHERE `filing_rank` = '0' AND `filing_sequence` = ? AND `selection_forgotten` == '0'",
							new String[] { "" + sequence });
			c.moveToFirst();
			new_cards_count = c.getInt(0);
			c.close();
			c = db.rawQuery(
					"SELECT COUNT(*) FROM `selection` JOIN `filing` ON `filing_card_id` = `selection_card_id` WHERE `filing_rank` != '0' AND `filing_sequence` = ? AND `selection_forgotten` == '0'",
					new String[] { "" + sequence });
			c.moveToFirst();
			old_cards_count = c.getInt(0);
			c.close();
			c = db.rawQuery("SELECT COUNT(*) FROM `selection` WHERE `selection_forgotten` == '1'", null);
			c.moveToFirst();
			bad_cards_count = c.getInt(0);
			c.close();
			frame_days_diff = (TextView) v.findViewById(R.id.training_titlebar_days_diff);
			frame_new_cards_count = (TextView) v.findViewById(R.id.training_titlebar_new_cards_count);
			frame_old_cards_count = (TextView) v.findViewById(R.id.training_titlebar_old_cards_count);
			frame_bad_cards_count = (TextView) v.findViewById(R.id.training_titlebar_bad_cards_count);
			frame_days_diff.setText("");
			
		}

		button_layout = v.findViewById(R.id.training_buttonbar_layout);
		if(button_layout != null)
		{
			if(buttonsize_ratio > 0)
			{
				button_trivial.measure(0, 0);
				final int height = button_trivial.getMeasuredHeight() * buttonsize_ratio / 100;
				button_trivial.setHeight(height);
				button_easy.setHeight(height);
				button_good.setHeight(height);
				button_tricky.setHeight(height);
				button_bad.setHeight(height);
				button_havoc.setHeight(height);
				button_recall.setHeight(height);
				button_forgotten.setHeight(height);
			}
			button_layout_forgotten = v.findViewById(R.id.training_buttonbar_forgotten_layout);
			button_layout_fresh = v.findViewById(R.id.training_buttonbar_fresh_layout);

			button_recall.getBackground().setColorFilter(0xffA0FFFF, PorterDuff.Mode.MULTIPLY);
			button_forgotten.getBackground().setColorFilter(0xffFF00FF, PorterDuff.Mode.MULTIPLY);

			button_trivial.getBackground().setColorFilter(0xffA0FFFF, PorterDuff.Mode.MULTIPLY);
			button_easy.getBackground().setColorFilter(0xff70FFFF, PorterDuff.Mode.MULTIPLY);
			button_good.getBackground().setColorFilter(0xff50FFFF, PorterDuff.Mode.MULTIPLY);
			button_tricky.getBackground().setColorFilter(0xff507FFF, PorterDuff.Mode.MULTIPLY);
			button_bad.getBackground().setColorFilter(0xff7F00FF, PorterDuff.Mode.MULTIPLY);
			button_havoc.getBackground().setColorFilter(0xffFF00FF, PorterDuff.Mode.MULTIPLY);

			class GradeClickListener implements OnClickListener
			{
				final int grade;

				GradeClickListener(final int grade)
				{
					this.grade = grade;
				}

				@Override
				public void onClick(final View v)
				{
					frame_days_diff.setText("");
					synchronized(TrainingFragment.this)
					{
						if(marker != null) return;
						pushUndoList();
						if(grade < 3 && hasTitleBar)
						{
							final Card card = playlist.getCurrentCard();
							if(card != null && !playlist.forgottenCard())
							{
								final SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
								markCardAsForgotten(db, grade, card.getId());
								db.close();
								++bad_cards_count;
								if(card.getFiling() != null)
								{
									if(card.getFiling().rank == 0) --new_cards_count;
									else --old_cards_count;
								}
							}
						}
						marker = new CardMarker(playlist.getCurrentCard().getId(), grade);
						marker.execute();
						if(grade < 3)
						{
							playlist.next();
							OnReset();
						}
						else OnDiscard();
						if(customSkin) changeSkin(grade);
					}
				}
			}
			;
			button_trivial.setOnClickListener(new GradeClickListener(5));
			button_easy.setOnClickListener(new GradeClickListener(4));
			button_good.setOnClickListener(new GradeClickListener(3));
			button_tricky.setOnClickListener(new GradeClickListener(2));
			button_bad.setOnClickListener(new GradeClickListener(1));
			button_havoc.setOnClickListener(new GradeClickListener(0));

			button_forgotten.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(final View v)
				{
					frame_days_diff.setText("");
					synchronized(TrainingFragment.this)
					{
						if(marker != null) return;
						pushUndoList();
						marker = new CardMarker(playlist.getCurrentCard().getId(), 0);
						marker.execute();
						playlist.next();
						OnReset();
						if(customSkin) changeSkin(0);
					}
				}
			});
			button_recall.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					frame_days_diff.setText("");
					pushUndoList();
					OnDiscard();
					if(customSkin) changeSkin(-1);
				}
			});

		}
		if(useTTS)
		{
			first.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					if(tts != null) seq.getFirst().say(playlist.getCurrentCard(), tts, TextToSpeech.QUEUE_FLUSH);
				}
			});
			second.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					if(tts != null) seq.getSecond().say(playlist.getCurrentCard(), tts, TextToSpeech.QUEUE_FLUSH);
				}
			});
			third.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(final View v)
				{
					if(tts != null) seq.getThird().say(playlist.getCurrentCard(), tts, TextToSpeech.QUEUE_FLUSH);
				}
			});

		}

		contentView = v;
		return v;

	}

	@Override
	public void onDestroy()
	{
		if(tts != null)
		{
			tts.stop();
			tts.shutdown();
			tts = null;
		}
		super.onDestroy();
		if(tatoeba_button.getVisibility() == View.VISIBLE)
			getActivity().getSupportLoaderManager().destroyLoader(CURSORLOADER_TATOEBA);
	}

	@Override
	public void onDetach()
	{
		super.onDetach();
		previous_language = null;
	}

	protected void OnDiscard()
	{
		if(playlist.isCompleted() || playlist.getCurrentCard() == null) return;
		if(undolist.size() == 0) return;
		final Playlist.Undo undo = undolist.getLast();
		final CardFiling filing = undo.getCardFiling();
		if(hasTitleBar)
		{
			if(undo.forgotten) --bad_cards_count;
			else if(filing != null)
			{
				if(filing.rank == 0) --new_cards_count;
				else --old_cards_count;
			}
		}
		playlist.discard();
		OnReset();
	}

	protected void OnFinish()
	{

		final Toast toast = Toast.makeText(getActivity(), getString(R.string.training_finished), Toast.LENGTH_SHORT);
		toast.show();

		final SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();

		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM `filing` WHERE filing_sequence = ?", new String[] { "" + sequence });
		final boolean hasFiling = cursor.getCount() != 0 && cursor.moveToFirst() && cursor.getLong(0) != 0;
		cursor.close();
		if(!hasFiling)
		{
			db.close();
			return;
		}

		cursor = db.rawQuery("SELECT COUNT(*) FROM `filing` WHERE filing_sequence = ? AND (`filing_session`+`filing_interval`) <= " + session,
				new String[] { "" + sequence });
		final boolean sessionFinished = cursor.getCount() != 0 && cursor.moveToFirst() && cursor.getLong(0) == 0;
		cursor.close();
		if(!sessionFinished)
		{
			db.close();
			return;
		}

		final ContentValues c = new ContentValues();
		c.put("filing_session", session + 1);
		if(db.update("filing_data", c, "filing_sequence = ?", new String[] { "" + sequence }) == 0)
		{
			c.put("filing_sequence", sequence);
			db.insert("filing_data", null, c);
		}
		db.close();

	}


	protected void OnNextButton()
	{
		playlist.next();
		OnReset();
	}

	protected void OnNextPhase()
	{
		if(hasSecondQuestion || inSecondAnswer) OnSolve();

		else OnSolveFirst();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		// Handle item selection
		switch(item.getItemId())
		{
			case R.id.menu_training_skip:
				pushUndoList();
				OnDiscard();
				return true;
			case R.id.menu_refresh:
				OnRefreshCard();
				return true;
			case R.id.menu_training_search:
				getActivity().onSearchRequested();
				return true;
			case R.id.menu_training_discard:
			{
				if(playlist.getCurrentCard().getFiling() == null) OnDiscard();
				else
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage(getString(R.string.ask_remove_a_card_from_filing)).setTitle(getString(R.string.ask_remove_a_card_from_filing_title))
							.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
								}
							}).setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
									final SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
									db.delete("filing", "filing_card_id = ? AND filing_sequence = ?", new String[] { "" + playlist.getCurrentCard().getId(),
											"" + sequence });
									OnDiscard();
									db.close();
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();
				}
			}
				return true;
			case R.id.menu_training_undo:
				OnUndo();
				return true;
			default:
				if(SearchIntents.view(item.getItemId(), playlist.getCurrentCard(), this)) return true;
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if(fractalgenerator != null)
		{
			fractalgenerator.cancel(true);
			fractalgenerator = null;
		}
		if(tts != null)
		{
			tts.stop();
			tts.shutdown();
			tts = null;
		}
	}

	protected void OnPreviousButton()
	{
		playlist.previous();
		OnReset();
	}

	public void OnRefreshCard()
	{
		playlist.update();
		OnReset();
	}

	protected Card OnReset()
	{
		if(playlist.isCompleted() || playlist.getCurrentCard() == null)
		{
			OnFinish();
			String authToken = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("account_authToken", null);
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR && authToken == null)
			{
				getActivity().finish();
				return null;
			}

			try
			{
				if(authToken == null) authToken = IncompatibleFunctions.obtainAuthToken(getActivity());
				if(authToken == null) throw new NullPointerException();
				final String finalAuthToken = authToken;
				final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(getString(R.string.sync_after_training)).setTitle(getString(R.string.sync_after_training_title))
						.setNegativeButton(getString(R.string.button_later), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								dialog.dismiss();
								if(!getActivity().isFinishing()) getActivity().finish();
							}
						}).setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								final UserData user = new UserData(finalAuthToken, 0, 0);
								new SyncChangesLoader(getActivity(), user, null).execute();
								new UploadFilingLoader(getActivity(), user, new OnFinishListener()
								{

									@Override
									public void onFinish()
									{
										if(!getActivity().isFinishing())
										{
											dialog.dismiss();
											getActivity().finish();
										}
									}
								}).execute();
								/*
								 * 
								 * Log.e(TAG, "clicked"); new TimestampLoader(getActivity(), authToken, new TimestampLoader.OnFinishListener() {
								 * 
								 * @Override public void onFinish(BookData[] array, LanguageData[] languages, UserData user) { if(user == null) {
								 * if(!getActivity().isFinishing()) { dialog.dismiss(); getActivity().finish(); } return; } new SyncChangesLoader(getActivity(),
								 * user, null).execute(); new UploadFilingLoader(getActivity(), user, new OnFinishListener() {
								 * 
								 * @Override public void onFinish() { if(!getActivity().isFinishing()) { dialog.dismiss(); getActivity().finish(); } }
								 * }).execute();
								 * 
								 * } }).execute();
								 */
							}

						}).setOnCancelListener(new OnCancelListener()
						{

							@Override
							public void onCancel(final DialogInterface arg0)
							{
								if(!getActivity().isFinishing()) getActivity().finish();
							}

						});
				final AlertDialog alert = builder.create();
				alert.show();
			}
			catch(final Exception e)
			{
				getActivity().finish();
				return null;
			}

			return null;
		}
		if(button_layout != null) button_layout.setVisibility(View.INVISIBLE);

		final Card card = playlist.getCurrentCard();
		final CardFiling filing = card.getFiling();
		final boolean forgotten = playlist.forgottenCard();
		first.setVisibility(View.VISIBLE);
		if(seq.getFirst().getType() == Sequence.SPEECH && hasNoSpeech)
		{
			first.setVisibility(View.GONE);
			first_comment.setVisibility(View.GONE);
		}
		if(seq.getSecond().getType() == Sequence.SPEECH && hasNoSpeech) second_comment.setVisibility(View.GONE);
		third.setVisibility(useCompactLayout ? View.GONE : View.INVISIBLE);
		final String firstString = seq.getFirst().get(card);
		first.setText(firstString);
		second.setText(seq.getSecond().get(card));
		third.setText(seq.getThird().get(card));
		first_comment.setText(seq.getFirst().getComment(card));
		second_comment.setText(seq.getSecond().getComment(card));
		third_comment.setText(seq.getThird().getComment(card));
		if(useCompactLayout)
		{
			first_comment.setVisibility(first_comment.getText().length() == 0 ? View.GONE : View.VISIBLE);
			second_comment.setVisibility(second_comment.getText().length() == 0 ? View.GONE : View.VISIBLE);
			third_comment.setVisibility(third_comment.getText().length() == 0 ? View.GONE : View.VISIBLE);
		}
		Log.e(TAG, "seq: " + seq + " card: " + card);

		firstIsEmpty = firstString == null ? true : (firstString.length() == 0 ? true : false);
		if(button_layout != null)
		{
			if(forgotten)
			{
				button_layout_forgotten.setVisibility(View.VISIBLE);
				button_layout_fresh.setVisibility(View.GONE);
			}
			else
			{
				button_layout_forgotten.setVisibility(View.GONE);
				button_layout_fresh.setVisibility(View.VISIBLE);
			}
		}
		inSecondAnswer = false;
		Log.e(TAG, "inSecond: " + inSecondAnswer + "hasSecondQ: " + hasSecondQuestion + " firstEmpty: " + firstIsEmpty + "NoSpeech: " + hasNoSpeech);
		if(!hasSecondQuestion)
		{
			if(firstIsEmpty)
			{
				first.setVisibility(View.GONE);
				inSecondAnswer = true;
				second.setVisibility(View.VISIBLE);
			}
			else second.setVisibility(useCompactLayout ? View.GONE : View.INVISIBLE);
		}
		if(hasTitleBar)
		{
			frame_cards_count.setText("" + (playlist.getPosition() + 1) + '/' + playlist.getCount());
			frame_progressbar.setProgress(frame_progressbar.getMax() - playlist.getCount());
			frame_card_type.setText(card.getType().isUndefined() ? "" : card.getType().toDetailedString());
			frame_flag.setImageResource(card.getFlagId());
			if(forgotten) updateStatusbar(CardStatus.BADSTATE);
			else if(filing != null)
			{
				if(filing.rank == 0) updateStatusbar(CardStatus.NEWSTATE);
				else updateStatusbar(CardStatus.OLDSTATE);
			}
			else updateStatusbar(CardStatus.NONE);

		}
		if(undoMenuItem != null) undoMenuItem.setEnabled(!undolist.isEmpty());
		if(tts != null && readWithTTS)
		{
			seq.getFirst().say(card, tts, TextToSpeech.QUEUE_FLUSH);
			if(hasSecondQuestion) seq.getSecond().say(card, tts, TextToSpeech.QUEUE_ADD);
		}
		if(previous_language == null || !card.getLanguage().equals(previous_language))
		{
			try
			{
				final Typeface first_tf = seq.getFirst().getTypeface(card);
				final Typeface second_tf = seq.getSecond().getTypeface(card);
				final Typeface third_tf = seq.getThird().getTypeface(card);
				first.setTypeface(first_tf);
				second.setTypeface(second_tf);
				third.setTypeface(third_tf);
				first_comment.setTypeface(first_tf);
				second_comment.setTypeface(second_tf);
				third_comment.setTypeface(third_tf);
				previous_language = card.getLanguage();
			}
			catch(final IOException e)
			{
				final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(e.toString()).setTitle(getString(R.string.typeface_missing_title))
						.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(final DialogInterface dialog, final int id)
							{
								dialog.dismiss();
							}
						});
				final AlertDialog alert = builder.create();
				alert.show();
			}
		}

		if(commentTextSize == -1f)
		{
			commentTextSize = first_comment.getTextSize();
		}
		else
		{
			first_comment.setTextSize(TypedValue.COMPLEX_UNIT_PX, commentTextSize);
			second_comment.setTextSize(TypedValue.COMPLEX_UNIT_PX, commentTextSize);
			third_comment.setTextSize(TypedValue.COMPLEX_UNIT_PX, commentTextSize);
		}
		return card;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(useFractals && fractalgenerator == null)
		{
			fractalgenerator = new FractalGenerator(contentView, null);
			fractalgenerator.execute((Void[]) null);
		}
		if(useTTS && tts == null)
		{
			final Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, Constants.REQUEST_TTS);
		}
		previous_language = null;
	}

	protected void OnShuffle()
	{
		playlist.shuffle();
		OnReset();
	}

	protected void OnSolve()
	{
		if(button_layout != null) button_layout.setVisibility(View.VISIBLE);
		second.setVisibility((hasNoSpeech && seq.getSecond().getType() == Sequence.SPEECH) ? View.GONE : View.VISIBLE);
		third.setVisibility(View.VISIBLE);
		inSecondAnswer = false;
		if(tts != null && readWithTTS) seq.getThird().say(playlist.getCurrentCard(), tts, TextToSpeech.QUEUE_ADD);
	}

	protected void OnSolveFirst()
	{
		if(hasNoSpeech && seq.getSecond().getType() == Sequence.SPEECH)
		{
			OnSolve();
			return;
		}
		second.setVisibility(View.VISIBLE);
		if(tts != null && readWithTTS) seq.getSecond().say(playlist.getCurrentCard(), tts, TextToSpeech.QUEUE_ADD);
		inSecondAnswer = true;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
		OnReset();

	}

	protected void OnUndo()
	{
		if(marker != null) return;
		if(!undolist.isEmpty())
		{
			final int oldcount = playlist.getCount();
			final Playlist.Undo undo = undolist.removeLast();
			final boolean previousForgotten = playlist.forgottenCard(undo.getCardId());
			undo.undo();

			final int newcount = playlist.getCount();
			final CardFiling filing = playlist.getCurrentCard().getFiling();

			if(filing == null)
			{
				if(oldcount < newcount && playlist.forgottenCard()) ++bad_cards_count;
				else if(!playlist.forgottenCard() && previousForgotten) --bad_cards_count;
				OnReset();
				return;
			}
			if(oldcount < newcount)
			{
				if(playlist.forgottenCard()) ++bad_cards_count;
				else if(filing.rank == 0) ++new_cards_count;
				else ++old_cards_count;
			}
			else if(!playlist.forgottenCard())
			{

				if(filing.rank == 0)
				{
					++new_cards_count;
					--bad_cards_count;
				}
				else
				{
					++old_cards_count;
					--bad_cards_count;
				}
			}
			OnReset();
		}
	}

	protected void OnVoiceRequest(final ArrayList<String> matches) // dummy method, is overriden by TextCardFragment
	{
	}

	protected void pushUndoList()
	{
		undolist.addLast(playlist.createUndo());
		if(undolist.size() > undolist_size) undolist.removeFirst();
	}

	protected void setGestureDetector(final GestureDetector gestureDetector)
	{
		activityTouchListener.gestureDetector = gestureDetector;
	}

	private void updateStatusbar(final CardStatus state)
	{
		switch(state)
		{
			case BADSTATE:
				setUnderlinedNumber(frame_bad_cards_count, bad_cards_count);
				setNormalNumber(frame_old_cards_count, old_cards_count);
				setNormalNumber(frame_new_cards_count, new_cards_count);
				break;
			case OLDSTATE:
				setUnderlinedNumber(frame_old_cards_count, old_cards_count);
				setNormalNumber(frame_bad_cards_count, bad_cards_count);
				setNormalNumber(frame_new_cards_count, new_cards_count);
				break;
			case NEWSTATE:
				setUnderlinedNumber(frame_new_cards_count, new_cards_count);
				setNormalNumber(frame_bad_cards_count, bad_cards_count);
				setNormalNumber(frame_old_cards_count, old_cards_count);
				break;
			default:
				setNormalNumber(frame_new_cards_count, new_cards_count);
				setNormalNumber(frame_bad_cards_count, bad_cards_count);
				setNormalNumber(frame_old_cards_count, old_cards_count);
				break;
		}

	}

	protected void useCompactLayout(final boolean enable)
	{
		useCompactLayout = enable;
	}

}
