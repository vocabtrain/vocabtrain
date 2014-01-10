package org.devwork.vocabtrain;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;

public class SearchIntents
{
	public static final String COLORDICT_ACTION = "colordict.intent.action.SEARCH";

	public final static String[] LEO_LANGUAGES = { "en", "es", "fr", "it", "ru", "zh" };

	public final static String[] REVERSO_LANGUAGES = { "de", "en", "es", "fr" };

	private final static Intent AARDDICT_INTENT = new Intent(Intent.ACTION_SEARCH).setComponent(new ComponentName("aarddict.android",
			"aarddict.android.Article"));

	public static final String QUICKDIC_ACTION = "com.hughes.action.ACTION_SEARCH_DICT";
	public static final String QUICKDIC_FROM_LANGUAGE = "from";
	public static final String QUICKDIC_TO_LANGUAGE = "to";
	
	public static final String AEDICT_ACTION = "sk.baka.aedict.action.ACTION_SEARCH_EDICT";
	public static final String AEDICT_STRING = "kanjis";

	public static final String SIMEJI_ACTION = "com.adamrocker.android.simeji.ACTION_INTERCEPT";

	public static final String SIMEJI_CATEGORY = "com.adamrocker.android.simeji.REPLACE";
	public static final String SIMEJI_STRING = "replace_key";

	public static void disableMissingMenuEntries(final Menu menu, final Activity activity, final Card card)
	{
		if(card == null)
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(activity.getString(R.string.database_error_title)).setMessage(activity.getString(R.string.database_synced_error))
					.setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(final DialogInterface dialog, final int id)
						{
							dialog.dismiss();
						}
					});
			final AlertDialog alert = builder.create();
			alert.show();
			return;
		}
		final DatabaseHelper dbh = new DatabaseHelper(activity);
		final LanguageLocale vernicular = DatabaseFunctions.getVernicular(activity, dbh.getRead());

		if(card.getLanguage().equals(LanguageLocale.Language.JAPANESE))
		{
			if(activity.getPackageManager().queryIntentActivities(new Intent(AEDICT_ACTION), 0).size() == 0) menu.removeItem(R.id.menu_intent_aedict);
			if(activity.getPackageManager().queryIntentActivities(new Intent(SIMEJI_ACTION), 0).size() == 0) menu.removeItem(R.id.menu_intent_simeji);
		}
		else
		{
			menu.removeItem(R.id.menu_intent_aedict);
			menu.removeItem(R.id.menu_intent_simeji);
		}

		if(card.getLanguage().equals(LanguageLocale.Language.GERMAN) || vernicular.equals(LanguageLocale.Language.GERMAN))
		{
			final LanguageLocale language = card.getLanguage().equals(LanguageLocale.Language.GERMAN) ? vernicular : card.getLanguage();
			if(!language.isInISOLanguageArray(LEO_LANGUAGES)) menu.removeItem(R.id.menu_intent_leo);
		}
		else
		{
			menu.removeItem(R.id.menu_intent_leo);
		}
		if(!((card.getLanguage().equals(LanguageLocale.Language.GERMAN) || vernicular.equals(LanguageLocale.Language.GERMAN)) && (card.getLanguage().equals(
				LanguageLocale.Language.ENGLISH) || vernicular.equals(LanguageLocale.Language.ENGLISH)))) menu.removeItem(R.id.menu_intent_dictcc);

		if(!card.getLanguage().isInISOLanguageArray(REVERSO_LANGUAGES) || !vernicular.isInISOLanguageArray(REVERSO_LANGUAGES))
			menu.removeItem(R.id.menu_intent_reverso);

		if(activity.getPackageManager().queryIntentActivities(new Intent(COLORDICT_ACTION), 0).size() == 0) menu.removeItem(R.id.menu_intent_colordict);

		if(activity.getPackageManager().queryIntentActivities(new Intent(QUICKDIC_ACTION), 0).size() == 0) menu.removeItem(R.id.menu_intent_quickdic);

		if(activity.getPackageManager().queryIntentActivities(AARDDICT_INTENT, 0).size() == 0) menu.removeItem(R.id.menu_intent_aarddict);

		if(!(card.getLanguage().equals(LanguageLocale.Language.FRENCH) && card.getType().getBasicType() == CardType.VERB))
			menu.removeItem(R.id.menu_intent_laconjugaison);

		dbh.close();
	}

	private static String removeAccents(final String accented)
	{
		return accented.toLowerCase().replace("æ", "ae").replace('ñ', 'n').replace('ç', 'c').replace('à', 'a').replace('á', 'a').replace('â', 'a')
				.replace('ù', 'u').replace('ú', 'u').replace('û', 'u').replace('ò', 'o').replace('ó', 'o').replace('ô', 'o').replace('ï', 'i')
				.replace('è', 'e').replace('é', 'e').replace('ê', 'e').replace('ë', 'e');
	}

	/*
	 * private static String cutPhrase(String phrase) { if(phrase.contains(',' }
	 */
	public static boolean search(final int menu_id, final String phrase, final Card card, final FragmentActivity activity)
	{
		class IntentNotFound extends Throwable
		{
			private static final long serialVersionUID = -747067227785195478L;
			public final String msg;

			IntentNotFound(final String msg)
			{
				this.msg = msg;
			}
		}
		final String text = phrase.split(",")[0];

		try
		{
			String conjugated = WordInflector.masu2dict(text, card.getType());
			if(conjugated == null) conjugated = text;
			switch(menu_id)
			{
				case R.id.menu_intent_partial:
					if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
					{
						final SearchPartialDialog dialog = new SearchPartialDialog(activity, text, card);
						dialog.show();

					}
					else
					{
						final FragmentCreator creator = new FragmentCreator()
						{
							@Override
							public Fragment create()
							{
								return SearchPartialDialogFragment.createInstance(text, card.getId());
							}

							@Override
							public boolean equals(final Fragment fragment)
							{
								if(!(fragment instanceof SearchPartialDialogFragment)) return false;
								return ((SearchPartialDialogFragment) fragment).getSearch().equals(text);
							}

							@Override
							public String getTag()
							{
								return SearchPartialDialogFragment.TAG;
							}

							@Override
							public Fragment update(final Fragment fragment)
							{
								((SearchPartialDialogFragment) fragment).setSearch(text);
								return fragment;
							}

						};
						if(activity instanceof TrainingActivity) ((TrainingActivity) activity).showFragment(creator);
						else
						{
							final FragmentManager manager = activity.getSupportFragmentManager();
							final FragmentTransaction ft = manager.beginTransaction();
							((DialogFragment) creator.create()).show(ft, CardViewDialog.TAG);
						}

					}
					return true;
				case R.id.menu_intent_clipboard:
				{
					final ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
					clipboard.setText(text);
				}
					return true;
				case R.id.menu_intent_aedict:
					try
					{
						final Intent intent = new Intent(AEDICT_ACTION);
						intent.putExtra(AEDICT_STRING, conjugated);
						activity.startActivity(intent);
					}
					catch(final ActivityNotFoundException e)
					{
						throw new IntentNotFound(activity.getString(R.string.missing_app, "aedict"));
					}

					return true;
				case R.id.menu_intent_quickdic:
					try
					{
						final Intent intent = new Intent(QUICKDIC_ACTION);
						intent.addCategory("android.intent.category.DEFAULT");
						intent.putExtra(SearchManager.QUERY, conjugated);
						intent.putExtra(QUICKDIC_FROM_LANGUAGE, card.getLanguage().getLanguage());
						final DatabaseHelper dbh = new DatabaseHelper(activity);
						intent.putExtra(QUICKDIC_TO_LANGUAGE, DatabaseFunctions.getVernicular(activity, dbh.getRead()).getLanguage() );
						dbh.close();
						activity.startActivity(intent);
					}
					catch(final ActivityNotFoundException e)
					{
						throw new IntentNotFound(activity.getString(R.string.missing_app, "quickdic"));
					}
					return true;
				case R.id.menu_intent_simeji:
					try
					{
						final Intent intent = new Intent(SIMEJI_ACTION);
						intent.addCategory(SIMEJI_CATEGORY);
						intent.addCategory("android.intent.category.DEFAULT");
						intent.putExtra(SIMEJI_STRING, conjugated);
						activity.startActivity(intent);
					}
					catch(final ActivityNotFoundException e)
					{
						throw new IntentNotFound(activity.getString(R.string.missing_app, "simeji"));
					}
					return true;
				case R.id.menu_intent_leo:
					try
					{
						final Intent intent = new Intent("android.intent.action.SEND");
						intent.setComponent(new ComponentName("org.leo.android.dict", "org.leo.android.dict.LeoDict"));
						final DatabaseHelper dbh = new DatabaseHelper(activity);
						final LanguageLocale language = card.getLanguage().equals(LanguageLocale.Language.GERMAN) ? DatabaseFunctions.getVernicular(activity,
								dbh.getRead()) : card.getLanguage();
						dbh.close();
						if(activity.getPackageManager().queryIntentActivities(intent, 0).size() != 0)
						{
							intent.putExtra("org.leo.android.dict.DICTIONARY", language.getLanguage() + "de");
							intent.putExtra(Intent.EXTRA_TEXT, text);
							activity.startActivity(intent);
						}
						else
						{
							final Intent webintent = new Intent("android.intent.action.VIEW", Uri.parse("http://pda.leo.org/?lp=" + language.getLanguage()
									+ "de&search=" + text));
							activity.startActivity(webintent);
						}
					}
					catch(final ActivityNotFoundException e)
					{
						throw new IntentNotFound(activity.getString(R.string.missing_app, "leo"));
					}
					return true;
				case R.id.menu_intent_colordict:
					try
					{
						final Intent intent = new Intent(COLORDICT_ACTION);
						intent.putExtra("EXTRA_QUERY", text);
						activity.startActivity(intent);
					}
					catch(final ActivityNotFoundException e)
					{
						throw new IntentNotFound(activity.getString(R.string.missing_app, "ColorDict"));
					}
					return true;
				case R.id.menu_intent_aarddict:
					try
					{
						final Intent intent = new Intent(AARDDICT_INTENT);
						intent.putExtra(SearchManager.QUERY, text);
						activity.startActivity(intent);
					}
					catch(final ActivityNotFoundException e)
					{
						throw new IntentNotFound(activity.getString(R.string.missing_app, "AardDict"));
					}
					return true;

				case R.id.menu_intent_search:
					try
					{
						final Intent intent = new Intent(Intent.ACTION_SEARCH);
						intent.addCategory("android.intent.category.DEFAULT");
						intent.putExtra(SearchManager.QUERY, text);
						activity.startActivity(intent);
					}
					catch(final ActivityNotFoundException e)
					{
						throw new IntentNotFound(activity.getString(R.string.missing_search));
					}

					return true;
				case R.id.menu_intent_reverso:
				{
					final DatabaseHelper dbh = new DatabaseHelper(activity);
					final LanguageLocale vernicular = DatabaseFunctions.getVernicular(activity, dbh.getRead());
					dbh.close();
					Log.e("Reverso", "http://mobile-dictionary.reverso.net/" + removeAccents(card.getLanguage().getDisplayLanguage()) + '-'
							+ removeAccents(vernicular.getDisplayLanguage()) + '/' + text);
					final Intent webintent = new Intent("android.intent.action.VIEW", Uri.parse("http://mobile-dictionary.reverso.net/"
							+ removeAccents(card.getLanguage().getDisplayLanguage()) + '-' + removeAccents(vernicular.getDisplayLanguage()) + '/' + text));
					activity.startActivity(webintent);
				}
					return true;
				case R.id.menu_intent_laconjugaison:
				{
					final Intent webintent = new Intent("android.intent.action.VIEW", Uri.parse("http://mobile.la-conjugaison.fr/du/verbe/"
							+ removeAccents(text) + ".php"));
					activity.startActivity(webintent);
				}
					return true;
				case R.id.menu_intent_dictcc:
				{
					final Intent webintent = new Intent("android.intent.action.VIEW", Uri.parse("http://pocket.dict.cc/?s=" + text));
					activity.startActivity(webintent);
				}
					return true;
				default:
					return false;
			}
		}
		catch(final IntentNotFound e)
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(e.msg).setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener()
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
		return false;
	}

	public static boolean view(final int id, final Card card, final Fragment fragment)
	{
		switch(id)
		{
			case R.id.menu_intent_edit:
			{
				final FragmentActivity activity = fragment.getActivity();
				if(activity instanceof MainActivity || activity instanceof TrainingActivity)
				{

					final FragmentCreator creator = new FragmentCreator()
					{
						@Override
						public Fragment create()
						{
							return CardEditFragment.createInstance(card.getId());
						}

						@Override
						public boolean equals(final Fragment fragment)
						{
							if(!(fragment instanceof CardEditFragment)) return false;
							return ((CardEditFragment) fragment).getCardId() == card.getId();
						}

						@Override
						public String getTag()
						{
							return CardEditFragment.TAG;
						}

						@Override
						public Fragment update(final Fragment fragment)
						{
							((CardEditFragment) fragment).setCardId(card.getId());
							return fragment;
						}
					};
					if(activity instanceof MainActivity) ((MainActivity) activity).showFragment(creator);
					else ((TrainingActivity) activity).showFragment(creator);
				}
				else
				{
					final Intent intent = new Intent(fragment.getActivity(), CardEditActivity.class);
					intent.putExtra("card_id", card.getId());
					fragment.startActivityForResult(intent, Constants.REQUEST_CARD_EDIT);
				}
			}
				return true;

			case R.id.menu_intent_view:
			{
				final FragmentActivity activity = fragment.getActivity();
				if(activity instanceof MainActivity || activity instanceof TrainingActivity)
				{

					final FragmentCreator creator = new FragmentCreator()
					{
						@Override
						public Fragment create()
						{
							return CardViewDialog.createInstance(card.getId());
						}

						@Override
						public boolean equals(final Fragment fragment)
						{
							if(!(fragment instanceof CardViewDialog)) return false;
							return ((CardViewDialog) fragment).getCardId() == card.getId();
						}

						@Override
						public String getTag()
						{
							return CardViewDialog.TAG;
						}

						@Override
						public Fragment update(final Fragment fragment)
						{
							((CardViewDialog) fragment).setCardId(card.getId());
							return fragment;
						}

					};
					if(activity instanceof MainActivity) ((MainActivity) activity).showFragment(creator);
					else
					{
						final FragmentManager manager = activity.getSupportFragmentManager();
						final FragmentTransaction ft = manager.beginTransaction();
						((CardViewDialog) creator.create()).show(ft, CardViewDialog.TAG);
					}

				}
				else
				{
					final Intent intent = new Intent(fragment.getActivity(), CardViewActivity.class);
					intent.putExtra("card_id", card.getId());
					fragment.startActivityForResult(intent, Constants.REQUEST_CARD_VIEW);
				}
			}
				return true;
		}
		return false;
	}

}
