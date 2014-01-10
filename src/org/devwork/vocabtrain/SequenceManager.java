package org.devwork.vocabtrain;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

public class SequenceManager
{

	public interface StringGetter
	{

		public String get(Card card);

		public String getComment(Card card);

		public void say(Card card, TextToSpeech tts, int mode);

		public int getType();

		public Typeface getTypeface(Card card) throws IOException;
	}

	private final StringGetter[] getter = new StringGetter[Sequence.data.length];
	private final int[] types = new int[getter.length];
	// private CharacterTranslator charactertranslator = null;
	private final boolean useRomanji;
	private final LanguageLocale vernicularLocale;


	public LanguageLocale getVernicularLocale()
	{
		return vernicularLocale;
	}

	private final Context context;

	public SequenceManager(Context context, DatabaseHelper dbh)
	{
		this.context = context;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		useRomanji = prefs.getBoolean("romaji", false);
		vernicularLocale = DatabaseFunctions.getVernicular(context, dbh.getRead());

		final byte[] a = Sequence.decodeSequence(prefs.getInt("sequence", Sequence.DEFAULT_SEQUENCE));
		if(a != null)
		{
			for(int i = 0; i < getter.length && i < a.length; ++i)
			{
				getter[i] = createGetter(a[i]);
				types[i] = a[i];
			}
		}
		else
		{
			createDefaultGetter();
		}

	}

	private void createDefaultGetter()
	{
		for(int i = 0; i < getter.length; ++i)
		{
			getter[i] = createGetter(i);
			types[i] = i;
		}
	}

	private StringGetter createGetter(int value)
	{
		switch(value)
		{
			case Sequence.SPEECH:
				abstract class SpeechGetter implements StringGetter
				{

					private final WordInflector conjugation;

					protected SpeechGetter()
					{
						conjugation = new WordInflector(context);
					}

					@Override
					public Typeface getTypeface(Card card) throws IOException
					{
						return DatabaseFunctions.getTypefaceFromLocale(context, card.getLanguage());
					}

					@Override
					public int getType()
					{
						return Sequence.SPEECH;
					}

					@Override
					public String getComment(Card card)
					{
						return card.getSpeechComment();
					}

					@Override
					public void say(Card card, TextToSpeech tts, int mode)
					{
						final int result = tts.setLanguage(card.getLanguage().getLocale() == null ? Locale.JAPANESE : card.getLanguage().getLocale());
						if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
						{
							if(CharacterTranslator.getCharacterTranslator() == null) return;
							final String message = getRomanji(getSpeech(card));
							if(message == null) return;
							tts.setLanguage(Locale.GERMAN); // best language which speaks text as it is
							tts.speak(message, mode, null);
						}
						else
						{
							final String message = getSpeech(card);
							if(message == null) return;
							tts.speak(message, mode, null);
						}
					}

					protected String getRomanji(String speech)
					{
						if(!CharacterTranslator.getCharacterTranslator().isReady()) return speech;
						CharacterTranslator trans = CharacterTranslator.getCharacterTranslator();
						return trans.fromKatakana(trans.fromHiragana(speech));
					}

					protected String getSpeech(Card card)
					{
						return conjugation.conjugate(card, card.getSpeech());
					}

				}
				if(useRomanji) return new SpeechGetter()
				{
					@Override
					public String get(Card card)
					{
						String speech = getSpeech(card);
						if(speech != null)
						{
							String romaji = getRomanji(speech);
							if(speech.equals(romaji))
								return speech;
							else
								return speech + "\n" + getRomanji(speech);
						}
						else
						{
							String romaji = getRomanji(card.getScript());
							return romaji.equals(card.getScript()) ? null : romaji;
						}
					}
				};
				return new SpeechGetter()
				{
					@Override
					public String get(Card card)
					{
						return getSpeech(card);
					}
				};
			case Sequence.SCRIPT:
				return new StringGetter()
				{
					private final WordInflector conjugation = new WordInflector(context);

					@Override
					public Typeface getTypeface(Card card) throws IOException
					{
						return DatabaseFunctions.getTypefaceFromLocale(context, card.getLanguage());
					}

					@Override
					public int getType()
					{
						return Sequence.SCRIPT;
					}

					@Override
					public String get(Card card)
					{
						return conjugation.conjugate(card, card.getScript());
					}

					@Override
					public String getComment(Card card)
					{
						return card.getScriptComment();
					}

					@Override
					public void say(Card card, TextToSpeech tts, int mode)
					{
						final int result = tts.setLanguage(card.getLanguage().getLocale() == null ? Locale.JAPANESE : card.getLanguage().getLocale());

						// TODO: Say TTS Script Prefs Option?
						if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) return;
						final String message = get(card);
						if(message == null) return;
						tts.speak(message, mode, null);

						/*
						 * final int result = tts.setLanguage(Locale.JAPANESE); if(result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) { final String message = get(card); if(message != null) tts.speak(message, mode, null); }
						 */
						return;
					}
				};
			case Sequence.VERNICULAR:
			{
				return new StringGetter()
				{
					private Typeface typeface = null;

					@Override
					public int getType()
					{
						return Sequence.VERNICULAR;
					}

					@Override
					public String get(Card card)
					{
						return card.getVernicular();
					}

					@Override
					public String getComment(Card card)
					{
						return card.getVernicularComment();
					}

					@Override
					public void say(Card card, TextToSpeech tts, int mode)
					{
						final int result = tts.setLanguage(vernicularLocale.getLocale());
						if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) return;
						tts.speak(get(card), mode, null);
					}

					@Override
					public Typeface getTypeface(Card card) throws IOException
					{
						if(typeface == null) typeface = DatabaseFunctions.getTypefaceFromLocale(context, getVernicularLocale());
						return typeface;
					}
				};
			}
		}
		return null;
	}

	public StringGetter getFirst()
	{
		return getter[0];
	}

	public StringGetter getSecond()
	{
		return getter[1];
	}

	public StringGetter getThird()
	{
		return getter[2];
	}

}
