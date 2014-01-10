package org.devwork.vocabtrain;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class TextCardFragment extends TrainingFragment
{

	public static final String TAG = Constants.PACKAGE_NAME + ".TextCardFragment";
	private TextView hiragana;
	private TextView katakana;
	private TextView firstanswer;
	private EditText edittext;
	private String currentColor;

	public TextCardFragment()
	{
		super(R.layout.textcard_fragment);
		useCompactLayout(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
	{
		if(getActivity().getPackageManager().queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).size() > 0)
			inflater.inflate(R.menu.textcard_option, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View v = super.onCreateView(inflater, container, savedInstanceState);
		if(v == null) return null;
		hiragana = (TextView) v.findViewById(R.id.textcard_hiragana);
		katakana = (TextView) v.findViewById(R.id.textcard_katakana);
		edittext = (EditText) v.findViewById(R.id.textcard_edit);
		firstanswer = (TextView) v.findViewById(R.id.textcard_firstanswer);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final boolean useRomanjiTranslator = prefs.getBoolean("textedit_romaji", true);
		if(!useRomanjiTranslator || getSeq().getFirst().getType() == Sequence.SPEECH
				|| (hasSecondQuestion() && getSeq().getSecond().getType() == Sequence.SPEECH))
		{
			hiragana.setVisibility(View.GONE);
			katakana.setVisibility(View.GONE);
		}

		edittext.setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(final View v, final int keyCode, final KeyEvent event)
			{

				if(CharacterTranslator.getCharacterTranslator() != null && CharacterTranslator.getCharacterTranslator().isReady() && useRomanjiTranslator)
				{
					if((isSecondAnswer() && getSeq().getThird().getType() == Sequence.SPEECH)
							|| (!isSecondAnswer() && getSeq().getSecond().getType() == Sequence.SPEECH))
					{
						hiragana.setText(CharacterTranslator.getCharacterTranslator().getHiragana(edittext.getText().toString()));
						katakana.setText(CharacterTranslator.getCharacterTranslator().getKatakana(edittext.getText().toString()));
					}
				}

				// If the event is a key-down event on the "enter" button
				if(event.getAction() == KeyEvent.ACTION_DOWN)
				{
					if(keyCode == KeyEvent.KEYCODE_ENTER)
					{
						final String answer = getSeq().getThird().get(getCurrentCard());
						final String useranswer = edittext.getText().toString();
						
						boolean correct = areAnswersEqual(answer, useranswer);
						if(!correct)
						{
							final String[] subanswers = answer.split(" *, *");
							for(String subanswer : subanswers)
							{
								correct = areAnswersEqual(subanswer, useranswer);
								if(correct) break;
							}
						}
						
						
						if(correct)
						{
							currentColor = "#00ff00";
						}
						else
						{
							currentColor = "#ff0000";
						}
						OnNextPhase();
						return true;
					}
				}
				return false;
			}
		});
		return v;
	}
	
	private boolean areAnswersEqual(String answer, String useranswer)
	{
		return (answer.equalsIgnoreCase(useranswer) || answer.equals(CharacterTranslator.getCharacterTranslator().getHiragana(useranswer))
				|| answer.equals(CharacterTranslator.getCharacterTranslator().getKatakana(useranswer)));

	}
	

	@Override
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		// Handle item selection
		switch(item.getItemId())
		{
			case R.id.menu_training_speak:
				if(getActivity().getPackageManager().queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0).size() > 0)
				{
					LanguageLocale locale = null;
					final int type = isSecondAnswer() ? getSeq().getSecond().getType() : getSeq().getThird().getType();
					switch(type)
					{
						case Sequence.SCRIPT:
							locale = getCurrentCard().getLanguage();
							break;
						case Sequence.VERNICULAR:
							locale = getSeq().getVernicularLocale();
							break;
					}
					try
					{

						final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
						intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.getLanguage());
						intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
						intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_say));
						startActivityForResult(intent, Constants.REQUEST_VOICE_REC_FOR_TEXTCARD);
					}

					catch(final Throwable e)
					{
						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(getString(R.string.voice_error)).setTitle(getString(R.string.voice_error_title))
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Card OnReset()
	{
		// edittext.setEnabled(true);
		// edittext.setFocusable(true);
		// firstanswer.getBackground().setColorFilter(null);
		edittext.getBackground().setColorFilter(null);
		firstanswer.setBackgroundDrawable(null);
		firstanswer.setText("");
		edittext.setText("");
		hiragana.setText("");
		katakana.setText("");
		edittext.requestFocus();
		return super.OnReset();
	}

	@Override
	protected void OnSolve()
	{
		edittext.getBackground().setColorFilter(Color.parseColor(currentColor), PorterDuff.Mode.DARKEN);
		super.OnSolve();
	}

	@Override
	protected void OnSolveFirst()
	{
		firstanswer.setText(String.format(getString(R.string.training_your_answer), edittext.getText()));
		firstanswer.setTextColor(Color.parseColor(currentColor));
		edittext.setText("");
		edittext.getBackground().setColorFilter(null);
		super.OnSolveFirst();
	}

	@Override
	protected void OnVoiceRequest(final ArrayList<String> matches)
	{
		edittext.setText(matches.get(0));
	}

}
