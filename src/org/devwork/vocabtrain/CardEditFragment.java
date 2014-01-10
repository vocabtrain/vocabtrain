package org.devwork.vocabtrain;

import java.io.IOException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.ActionBarFragmentActivity;

public class CardEditFragment extends DatabaseFragment
{
	static private class KanaTranslator implements OnItemSelectedListener, OnKeyListener
	{
		final EditText edit;
		final TextView view;
		final Spinner type;

		KanaTranslator(final EditText edit, final TextView view, final Spinner type)
		{
			this.edit = edit;
			this.view = view;
			this.type = type;
		}

		private void commit()
		{
			if(CharacterTranslator.getCharacterTranslator() != null && CharacterTranslator.getCharacterTranslator().isReady())
			{
				switch((int) type.getSelectedItemId())
				{
					case 1:
						view.setText(CharacterTranslator.getCharacterTranslator().getHiragana(edit.getText().toString()));
						break;
					case 2:
						view.setText(CharacterTranslator.getCharacterTranslator().getKatakana(edit.getText().toString()));
						break;
				}
			}
		}

		@Override
		public void onItemSelected(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3)
		{
			commit();
		}

		@Override
		public boolean onKey(final View v, final int keyCode, final KeyEvent event)
		{
			commit();
			return false;
		}

		@Override
		public void onNothingSelected(final AdapterView<?> arg0)
		{
		}
	}

	public static final String TAG = Constants.PACKAGE_NAME + ".CardEditFragment";

	public static CardEditFragment createInstance(final long card_id)
	{
		final CardEditFragment fragment = new CardEditFragment();
		final Bundle bundle = new Bundle();
		bundle.putLong("card_id", card_id);
		fragment.setArguments(bundle);
		return fragment;
	}

	private Button apply;
	private EditText script_comment;
	private EditText vernicular_comment;
	private EditText speech_comment;
	private CheckBox isVernicularCommentEmpty;
	private CheckBox isSpeechCommentEmpty;
	private CheckBox isScriptCommentEmpty;
	private String language;
	private Button cancel;
	private Spinner type_basic;
	private Spinner type_first;
	private Spinner type_second;
	private Spinner priority;

	private Spinner speech_comment_translate_type;
	private Spinner speech_translate_type;

	private TextView speech_translated;

	private TextView speech_comment_translated;

	private int sequence = Sequence.DEFAULT_SEQUENCE;

	private View speech_translate_layout;

	private View speech_comment_translate_layout;

	private Card card;
	private EditText script;

	private EditText speech;

	private EditText vernicular;

	private CheckBox isSpeechEmpty;

	private void finish()
	{
		if(getActivity() instanceof CardEditActivity)
		{
			getActivity().finish();
		}
		else
		{
			final FragmentManager manager = getActivity().getSupportFragmentManager();
			manager.popBackStack();
			if(getActivity() instanceof TrainingActivity)
			{
				final String trainingFragmentName = ((TrainingActivity) getActivity()).getTrainingFragmentName();
				final Fragment prev = manager.findFragmentByTag(trainingFragmentName);
				if(prev != null)
					((TrainingFragment) prev).OnRefreshCard();

			}
		}
	}

	public long getCardId()
	{
		return card == null ? -1 : card.getId();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View v = inflater.inflate(R.layout.cardedit_fragment, container, false);
		final long card_id = getArguments().getLong("card_id");

		language = DatabaseFunctions.getVernicular(getActivity(), getDatabaseHelper().getRead()).toString();
		sequence = DatabaseFunctions.getSequence(getActivity());

		script = (EditText) v.findViewById(R.id.cardedit_script);
		speech = (EditText) v.findViewById(R.id.cardedit_speech);
		vernicular = (EditText) v.findViewById(R.id.cardedit_vernicular);
		script_comment = (EditText) v.findViewById(R.id.cardedit_script_comment);
		speech_comment = (EditText) v.findViewById(R.id.cardedit_speech_comment);
		vernicular_comment = (EditText) v.findViewById(R.id.cardedit_vernicular_comment);
		apply = (Button) v.findViewById(R.id.cardedit_apply);
		cancel = (Button) v.findViewById(R.id.cardedit_cancel);

		isSpeechEmpty = (CheckBox) v.findViewById(R.id.cardedit_speech_check);
		isSpeechCommentEmpty = (CheckBox) v.findViewById(R.id.cardedit_speech_comment_check);
		isScriptCommentEmpty = (CheckBox) v.findViewById(R.id.cardedit_script_comment_check);
		isVernicularCommentEmpty = (CheckBox) v.findViewById(R.id.cardedit_vernicular_comment_check);

		type_basic = (Spinner) v.findViewById(R.id.cardedit_type_basic);
		type_first = (Spinner) v.findViewById(R.id.cardedit_type_first);
		type_second = (Spinner) v.findViewById(R.id.cardedit_type_second);
		priority = (Spinner) v.findViewById(R.id.cardedit_priority);
		speech_comment_translate_type = (Spinner) v.findViewById(R.id.cardedit_speech_comment_translate_type);
		speech_translate_type = (Spinner) v.findViewById(R.id.cardedit_speech_translate_type);

		speech_translated = (TextView) v.findViewById(R.id.cardedit_speech_translated);
		speech_comment_translated = (TextView) v.findViewById(R.id.cardedit_speech_comment_translated);
		speech_translate_layout = v.findViewById(R.id.cardedit_speech_translate_layout);
		speech_comment_translate_layout = v.findViewById(R.id.cardedit_speech_comment_translate_layout);
		
		final TextView speech_label = (TextView) v.findViewById(R.id.cardedit_speech_label);
		speech_label.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				isSpeechEmpty.setChecked(!isSpeechEmpty.isChecked());
			}
		});
		final TextView speech_comment_label = (TextView) v.findViewById(R.id.cardedit_speech_comment_label);
		speech_comment_label.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				isSpeechCommentEmpty.setChecked(!isSpeechCommentEmpty.isChecked());
			}
		});
		final TextView script_comment_label = (TextView) v.findViewById(R.id.cardedit_script_comment_label);
		script_comment_label.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				isScriptCommentEmpty.setChecked(!isScriptCommentEmpty.isChecked());
			}
		});
		final TextView vernicular_comment_label = (TextView) v.findViewById(R.id.cardedit_vernicular_comment_label);
		vernicular_comment_label.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				isVernicularCommentEmpty.setChecked(!isVernicularCommentEmpty.isChecked());
			}
		});

		isSpeechEmpty.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final boolean isDisabled = !isSpeechEmpty.isChecked();
				speech.setEnabled(isDisabled);
				speech.setVisibility(isDisabled ? View.VISIBLE : View.GONE);
				speech_translate_layout.setVisibility(isDisabled ? View.VISIBLE : View.GONE);
			}
		});
		isSpeechCommentEmpty.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final boolean isDisabled = !isSpeechCommentEmpty.isChecked();
				speech_comment.setEnabled(isDisabled);
				speech_comment.setVisibility(isDisabled ? View.VISIBLE : View.GONE);
				speech_comment_translate_layout.setVisibility(isDisabled ? View.VISIBLE : View.GONE);
			}
		});
		isScriptCommentEmpty.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final boolean isDisabled = !isScriptCommentEmpty.isChecked();
				script_comment.setEnabled(isDisabled);
				script_comment.setVisibility(isDisabled ? View.VISIBLE : View.GONE);
			}
		});
		isVernicularCommentEmpty.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				final boolean isDisabled = !isVernicularCommentEmpty.isChecked();
				vernicular_comment.setEnabled(isDisabled);
				vernicular_comment.setVisibility(isDisabled ? View.VISIBLE : View.GONE);
			}
		});
		Log.e("Card_id", "" + card_id);

		if(vernicular != null)
		{
			final LanguageLocale vernicular_locale = DatabaseFunctions.getVernicular(getActivity(), getDatabaseHelper().getRead());
			final TextView vernicular_label = (TextView) v.findViewById(R.id.cardedit_vernicular_label);

			vernicular_label.setText(vernicular_locale.getDisplayLanguage() + ":");
			vernicular_comment_label.setText(String.format(getActivity().getString(R.string.layout_card_vernicular_comment_gap),
					vernicular_locale.getDisplayLanguage()));
		}
		final KanaTranslator speech_translator = new KanaTranslator(speech, speech_translated, speech_translate_type);
		final KanaTranslator speech_comment_translator = new KanaTranslator(speech_comment, speech_comment_translated, speech_comment_translate_type);

		speech.setOnKeyListener(speech_translator);
		speech_translate_type.setOnItemSelectedListener(speech_translator);
		speech_comment.setOnKeyListener(speech_comment_translator);
		speech_comment_translate_type.setOnItemSelectedListener(speech_comment_translator);
		/*
		 * speech.setOnKeyListener(new OnKeyListener() {
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) { if(CharacterTranslator.getCharacterTranslator() != null &&
		 * CharacterTranslator.getCharacterTranslator().isReady()) { switch((int) speech_translate_type.getSelectedItemId()) { case 1:
		 * speech_translated.setText(CharacterTranslator.getCharacterTranslator().getHiragana(speech.getText().toString())); break; case 2:
		 * speech_translated.setText(CharacterTranslator.getCharacterTranslator().getKatakana(speech.getText().toString())); break; } } return false; }
		 * 
		 * }); speech_translate_type.setOnItemSelectedListener(new OnItemSelectedListener() {
		 * 
		 * @Override public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) { }
		 * 
		 * @Override public void onNothingSelected(AdapterView<?> arg0) { }
		 * 
		 * });
		 * 
		 * 
		 * speech_comment.setOnKeyListener(new OnKeyListener() {
		 * 
		 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) { if(CharacterTranslator.getCharacterTranslator() != null &&
		 * CharacterTranslator.getCharacterTranslator().isReady()) { switch((int) speech_comment_translate_type.getSelectedItemId()) { case 1:
		 * speech_comment_translated.setText(CharacterTranslator.getCharacterTranslator().getHiragana(speech_comment.getText().toString())); break; case 2:
		 * speech_comment_translated.setText(CharacterTranslator.getCharacterTranslator().getKatakana(speech_comment.getText().toString())); break; } } return
		 * false; }
		 * 
		 * });
		 */
		cancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				finish();

			}
		});

		apply.setOnClickListener(new OnClickListener()
		{

			private CardType getType()
			{
				final CardType basic = (CardType) type_basic.getSelectedItem();
				if(type_second.isEnabled())
				{
					final CardType second = (CardType) type_second.getSelectedItem();
					if(type_first.isEnabled())
					return basic.combineType(((CardType) type_first.getSelectedItem()), second);
					else
					return second.isUndefined() ? basic : second;
				}
				else if(type_first.isEnabled())
				{
					final CardType first = (CardType) type_first.getSelectedItem();
					return first.isUndefined() ? basic : first;
				}
				else
				return basic;
			}

			@Override
			public void onClick(final View v)
			{
				// if(hasChanged())
				{
					final SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
					final String sscript = script.getText().toString();
					final String svernicular = vernicular.getText().toString();
					final String sspeech = isSpeechEmpty.isChecked() ? null : (speech_translate_type.getSelectedItemId() == 0 ? speech : speech_translated)
							.getText().toString();

					final String sspeech_comment = isSpeechCommentEmpty.isChecked() ? null
							: (speech_comment_translate_type.getSelectedItemId() == 0 ? speech_comment : speech_comment_translated).getText().toString();
					final String sscript_comment = isScriptCommentEmpty.isChecked() ? null : script_comment.getText().toString();
					final String svernicular_comment = isVernicularCommentEmpty.isChecked() ? null : vernicular_comment.getText().toString();

					final int stype = getType().getValue();
					Log.e(TAG, "" + stype);

					final ContentValues cvCardUpdate = new ContentValues();
					final ContentValues cvTranslationUpdate = new ContentValues();

					if(stype != card.getType().getValue())
					{
						db.execSQL("INSERT OR IGNORE INTO `changes_type` (`_id`) VALUES ( '" + card.getId() + "' )");
						final ContentValues c = new ContentValues();
						c.put("changes_value", stype);
						db.update("changes_type", c, "_id = ?", new String[] { "" + card.getId() });

						cvCardUpdate.put("card_type", stype);
					}

					if(!card.getScript().equals(sscript))
					{
						db.execSQL("INSERT OR IGNORE INTO `changes_script` (`_id`) VALUES ( '" + card.getId() + "' )");
						final ContentValues c = new ContentValues();
						c.put("changes_value", sscript);
						db.update("changes_script", c, "_id = ?", new String[] { "" + card.getId() });

						cvCardUpdate.put("card_script", sscript);
					}
					if(!card.getVernicular().equals(svernicular))
					{
						db.execSQL("INSERT OR IGNORE INTO `changes_vernicular` (`changes_card_id`, `changes_language`) VALUES ( '" + card.getId() + "','"
								+ language + "' )");
						final ContentValues c = new ContentValues();
						c.put("changes_value", svernicular);
						db.update("changes_vernicular", c, "changes_card_id = ? AND changes_language = ?", new String[] { "" + card.getId(), "" + language });

						cvTranslationUpdate.put("translation_content", svernicular);
					}
					if(((card.getVernicularComment() == null) != (svernicular_comment == null))
							|| (card.getVernicularComment() != null && !card.getVernicularComment().equals(svernicular_comment)))
					{
						db.execSQL("INSERT OR IGNORE INTO `changes_vernicular_comment` (`changes_card_id`, `changes_language`) VALUES ( '" + card.getId()
								+ "','" + language + "' )");
						final ContentValues c = new ContentValues();
						c.put("changes_value", svernicular_comment);
						db.update("changes_vernicular_comment", c, "changes_card_id = ? AND changes_language = ?", new String[] { "" + card.getId(),
								"" + language });

						cvTranslationUpdate.put("translation_comment", svernicular_comment);
					}

					if(((card.getScriptComment() == null) != (sscript_comment == null))
							|| (card.getScriptComment() != null && !card.getScriptComment().equals(sscript_comment)))
					{
						db.execSQL("INSERT OR IGNORE INTO `changes_script_comment` (`_id`) VALUES ( '" + card.getId() + "' )");
						final ContentValues c = new ContentValues();
						c.put("changes_value", sscript_comment);
						db.update("changes_script_comment", c, "_id = ?", new String[] { "" + card.getId() });

						cvCardUpdate.put("card_script_comment", sscript_comment);
					}
					if(((card.getSpeechComment() == null) != (sspeech_comment == null))
							|| (card.getSpeechComment() != null && !card.getSpeechComment().equals(sspeech_comment)))
					{
						db.execSQL("INSERT OR IGNORE INTO `changes_speech_comment` (`_id`) VALUES ( '" + card.getId() + "' )");
						final ContentValues c = new ContentValues();
						c.put("changes_value", sspeech_comment);
						db.update("changes_speech_comment", c, "_id = ?", new String[] { "" + card.getId() });

						cvCardUpdate.put("card_speech_comment", sspeech_comment);
					}
					if(((card.getSpeech() == null) != (sspeech == null)) || (card.getSpeech() != null && !card.getSpeech().equals(sspeech)))
					{
						db.execSQL("INSERT OR IGNORE INTO `changes_speech` (`_id`) VALUES ( '" + card.getId() + "' )");
						final ContentValues c = new ContentValues();
						c.put("changes_value", sspeech);
						db.update("changes_speech", c, "_id = ?", new String[] { "" + card.getId() });

						cvCardUpdate.put("card_speech", sspeech);
					}

					if(cvCardUpdate.size() > 0) db.update("cards", cvCardUpdate, "_id = ?", new String[] { "" + card.getId() });

					if(cvTranslationUpdate.size() > 0)
						db.update("translations", cvTranslationUpdate, "translation_card_id = ? AND translation_language = ?", new String[] {
								"" + card.getId(), "" + language });

					if(card.getFiling() != null)
					{
						final CardFiling filing = card.getFiling();
						final String[] priorities = getResources().getStringArray(R.array.priorities);
						final CardFiling.Priority spriority = CardFiling.Priority.get(priorities[(int) priority.getSelectedItemId()], getActivity());

						if(spriority != filing.priority)
						{
							final ContentValues c = new ContentValues();
							c.put("filing_priority", spriority.get());
							db.update("filing", c, "filing_card_id = ? AND filing_priority = ?", new String[] { "" + card.getId(), "" + sequence });
						}
					}

					db.close();

				}
				finish();
			}

		});
		
		setCardId(card_id);
		return v;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
	}

	public void setCardId(final long card_id)
	{
		card = getDatabaseHelper().getCardById(card_id);
		if(card == null) return;
		script.setText(card.getScript());
		vernicular.setText(card.getVernicular());
		if(card.getSpeech() == null)
		{
			isSpeechEmpty.setChecked(true);
			speech.setEnabled(false);
			speech.setVisibility(View.GONE);
			speech_translate_layout.setVisibility(View.GONE);
		}
		else
		{
			speech.setText(card.getSpeech());
			speech.setVisibility(View.VISIBLE);
		}
		if(card.getSpeechComment() == null)
		{
			isSpeechCommentEmpty.setChecked(true);
			speech_comment.setEnabled(false);
			speech_comment.setVisibility(View.GONE);
			speech_comment_translate_layout.setVisibility(View.GONE);
		}
		else speech_comment.setText(card.getSpeechComment());
		if(card.getScriptComment() == null)
		{
			isScriptCommentEmpty.setChecked(true);
			script_comment.setEnabled(false);
			script_comment.setVisibility(View.GONE);

		}
		else script_comment.setText(card.getScriptComment());
		if(card.getVernicularComment() == null)
		{
			isVernicularCommentEmpty.setChecked(true);
			vernicular_comment.setEnabled(false);
			vernicular_comment.setVisibility(View.GONE);
		}
		else vernicular_comment.setText(card.getVernicularComment());

		{
			final ArrayAdapter<CardType> adapter = new ArrayAdapter<CardType>(getActivity(), android.R.layout.simple_spinner_item, card.getType()
					.getBasicSelectionType());
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			type_basic.setAdapter(adapter);
			type_basic.setOnItemSelectedListener(new OnItemSelectedListener()
			{
				@Override
				public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id)
				{
					final CardType type = (CardType) parent.getSelectedItem();
					final CardType[] a = type.getFirstSelectionType();
					final CardType[] b = type.getSecondSelectionType();
					if(a != null)
					{
						final ArrayAdapter<CardType> adapter = new ArrayAdapter<CardType>(getActivity(), android.R.layout.simple_spinner_item, a);
						adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						type_first.setAdapter(adapter);
						final int pos = adapter.getPosition(card.getType());
						type_first.setSelection(pos, true);
						type_first.setVisibility(View.VISIBLE);
						type_first.setEnabled(true);
					}
					else
					{
						type_first.setVisibility(View.GONE);
						type_first.setAdapter(null);
						type_first.setEnabled(false);
					}
					if(b != null)
					{
						final ArrayAdapter<CardType> adapter = new ArrayAdapter<CardType>(getActivity(), android.R.layout.simple_spinner_item, b);
						adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						type_second.setAdapter(adapter);
						final int pos = adapter.getPosition(card.getType());
						type_second.setSelection(pos, true);
						type_second.setVisibility(View.VISIBLE);
						type_second.setEnabled(true);
					}
					else
					{
						type_second.setVisibility(View.GONE);
						type_second.setAdapter(null);
						type_second.setEnabled(false);
					}
				}

				@Override
				public void onNothingSelected(final AdapterView<?> arg0)
				{
					// TODO Auto-generated method stub

				}

			});
			final int pos = adapter.getPosition(card.getType());
			type_basic.setSelection(pos, true);
		}
		if(card.getFiling() != null)
		{
			final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.priorities, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			priority.setAdapter(adapter);
			final int pos = adapter.getPosition(getResources().getString(card.getFiling().priority.getStringId()));
			priority.setSelection(pos, true);
		}
		else
		{
			priority.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,
					new String[] { getString(R.string.missing_filing) }));
			priority.setEnabled(false);
		}

		{
			final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.speech_types,
					android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			speech_comment_translate_type.setAdapter(adapter);
			speech_comment_translate_type.setSelection(0);
			speech_translate_type.setAdapter(adapter);
			speech_translate_type.setSelection(0);
		}
		
		try 
		{
			Typeface foreignTypeface = DatabaseFunctions.getTypefaceFromLocale(getActivity(), card.getLanguage());
			Typeface vernicularTypeface = DatabaseFunctions.getTypefaceFromLocale(getActivity(), DatabaseFunctions.getVernicular(getActivity()));
			
			speech_comment.setTypeface(foreignTypeface);
			script.setTypeface(foreignTypeface);
			script_comment.setTypeface(foreignTypeface);
			speech.setTypeface(foreignTypeface);
			vernicular.setTypeface(vernicularTypeface);
			vernicular_comment.setTypeface(vernicularTypeface);
		} 
		catch (IOException e) {}

	}

}
