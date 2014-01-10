package org.devwork.vocabtrain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Scanner;
import java.util.regex.Pattern;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ProgressBar;

public class CharacterTranslator
{
	private final static String hiragana_file = "hiragana.txt";
	private final static String katakana_file = "katakana.txt";
	private final SpeechTranslator hiragana;
	private final SpeechTranslator katakana;
	private final SpeechTranslatorCreator task;

	private CharacterTranslator(final AssetManager assetManager) throws IOException
	{
		hiragana = new SpeechTranslator();
		katakana = new SpeechTranslator();
		task = new SpeechTranslatorCreator();
		task.execute(assetManager.open(hiragana_file), assetManager.open(katakana_file));
	}

	public String getHiragana(String romaji)
	{
		return hiragana.getSpeech(romaji);
	}

	public String getKatakana(String romaji)
	{
		return katakana.getSpeech(romaji);
	}

	public String fromKatakana(String speech)
	{
		return katakana.getRomanji(speech);
	}

	public String fromHiragana(String speech)
	{
		return hiragana.getRomanji(speech);
	}

	public boolean isReady()
	{
		return isready;
	}

	private boolean isready = false;

	public void hookProgressBar(ProgressBar progressbar)
	{
		if(!isready) task.hookProgressBar(progressbar);
	}

	private static CharacterTranslator charactertranslator = null;

	public static CharacterTranslator createCharacterTranslator(final AssetManager assetManager) throws IOException
	{
		if(charactertranslator == null) charactertranslator = new CharacterTranslator(assetManager);
		return charactertranslator;
	}

	public static CharacterTranslator getCharacterTranslator()
	{
		return charactertranslator;
	}

	private static class SpeechTranslator
	{

		private final TrieMap romaji2speech = new TrieMap();
		private final TrieMap speech2romaji = new TrieMap();

		public String getSpeech(String romaji)
		{
			return romaji2speech.getTokenized(romaji);
		}

		public String getRomanji(String speech)
		{
			return speech2romaji.getTokenized(speech);
		}

		public void readStream(BufferedReader speechReader, SpeechTranslatorCreator task) throws IOException
		{
			final Pattern delimiter = Pattern.compile(":");
			final Pattern filecomment = Pattern.compile("\\W?[\\p{Cntrl}\\s　]*[%#].*");

			while(true)
			{
				String line = speechReader.readLine();
				task.OnProgressUpdate();
				if(line == null) break;
				if(line.length() == 0 || filecomment.matcher(line).matches()) continue;

				Scanner s = new Scanner(line);
				s.useDelimiter(delimiter);
				String speech, romaji;
				if(!s.hasNext()) continue;
				romaji = s.next();
				if(!s.hasNext()) continue;
				speech = s.next();
				romaji2speech.put(romaji, speech);
				speech2romaji.put(speech, romaji);
			}
		}

	}

	private class SpeechTranslatorCreator extends AsyncTask<InputStream, Integer, Void>
	{
		private ProgressBar progressbar = null;

		public void hookProgressBar(ProgressBar progressbar)
		{
			this.progressbar = progressbar;
			if(lines != 0) progressbar.setMax(lines);
			progressbar.setIndeterminate(false);
			progressbar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			if(progressbar != null) progressbar.setProgress(values[0]);
		}

		private int lines;
		private int progress;

		public void OnProgressUpdate()
		{
			publishProgress(++progress);
		}

		@Override
		protected Void doInBackground(InputStream... params)
		{
			try
			{

				LineNumberReader lnr = new LineNumberReader(new InputStreamReader(params[0]));
				lnr.skip(Long.MAX_VALUE);
				lines = lnr.getLineNumber();
				params[0].reset();
				lnr = new LineNumberReader(new InputStreamReader(params[1]));
				lnr.skip(Long.MAX_VALUE);
				lines += lnr.getLineNumber();
				params[1].reset();
				if(progressbar != null) progressbar.setMax(lines);

				hiragana.readStream(new BufferedReader(new InputStreamReader(params[0])), this);
				katakana.readStream(new BufferedReader(new InputStreamReader(params[1])), this);

			}
			catch(IOException e)
			{
				// TODO
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			if(progressbar != null)
			{
				AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
				animation.setDuration(1000);
				animation.setStartOffset(0);
				animation.setAnimationListener(new AnimationListener()
				{

					@Override
					public void onAnimationEnd(Animation arg0)
					{
						progressbar.setVisibility(View.GONE);
					}

					@Override
					public void onAnimationRepeat(Animation arg0)
					{
					}

					@Override
					public void onAnimationStart(Animation arg0)
					{
						progressbar.setProgress(progressbar.getMax());
					}
				});
				progressbar.startAnimation(animation);
			}
			isready = true;
		}
	}

}