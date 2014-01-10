package org.devwork.vocabtrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class WordInflector
{
	private final static int VERB_CONJUGATION_DISABLED = 0;
	private final static int VERB_CONJUGATION_MASU = 1;
	private final static int VERB_CONJUGATION_DICT = 2;
	private final static int NOUN_ARTICLE_DISABLED = 0;
	private final static int NOUN_ARTICLE_INDEFINITE = 1;
	private final static int NOUN_ARTICLE_DEFINITE_UNIQUE = 2;
	private final static int NOUN_ARTICLE_DEFINITE = 3;

	private static String addArticleToNoun(final Card card, final String noun, final int noun_article)
	{
		if(noun == null) return null;
		if(card.getType().getBasicType() != CardType.NOUN) return null;
		if(card.getLanguage().equals(LanguageLocale.Language.ENGLISH))
		{
			switch(noun_article)
			{
				case NOUN_ARTICLE_INDEFINITE:
					return "a " + noun;
				case NOUN_ARTICLE_DEFINITE_UNIQUE:
				case NOUN_ARTICLE_DEFINITE:
					return "the " + noun;
			}
		}
		else if(card.getLanguage().equals(LanguageLocale.Language.FRENCH))
		{
			switch(noun_article)
			{
				case NOUN_ARTICLE_INDEFINITE:
					switch(card.getType().getFirstComplexType())
					{
						case CardType.A_FEMININE:
							return "une " + noun;
						case CardType.A_MASCULINE:
							return "un " + noun;
						case CardType.A_FEMININE_PLURAL:
						case CardType.A_MASCULINE_PLURAL:
							return "des " + noun;
						default:
							return null;
					}
				case NOUN_ARTICLE_DEFINITE_UNIQUE:
				{
					final char c = Character.toLowerCase(noun.charAt(0));
					if(c == 'a' || c == 'o' || c == 'i' || c == 'u' || c == 'e' || c == 'á' || c == 'é' || c == 'h') return addArticleToNoun(card, noun,
							NOUN_ARTICLE_INDEFINITE);
					else return addArticleToNoun(card, noun, NOUN_ARTICLE_DEFINITE);
				}
				case NOUN_ARTICLE_DEFINITE:
				{
					final char c = Character.toLowerCase(noun.charAt(0));
					if(c == 'a' || c == 'o' || c == 'i' || c == 'u' || c == 'e' || c == 'á' || c == 'é' || c == 'h') switch(card.getType()
							.getFirstComplexType())
					{
						case CardType.A_FEMININE:
						case CardType.A_MASCULINE:
							return "l'" + noun;
						case CardType.A_FEMININE_PLURAL:
						case CardType.A_MASCULINE_PLURAL:
							return "les " + noun;
						default:
							return null;
					}
					else switch(card.getType().getFirstComplexType())
					{
						case CardType.A_FEMININE:
							return "la " + noun;
						case CardType.A_MASCULINE:
							return "le " + noun;
						case CardType.A_FEMININE_PLURAL:
						case CardType.A_MASCULINE_PLURAL:
							return "les " + noun;
						default:
							return null;
					}
				}
			}
		}

		return null;
	}

	public static String dict2masu(final String s, final CardType type)
	{
		if(s == null) return null;
		if(type.getBasicType() != CardType.VERB) return null;
		if(s.endsWith("ます")) return null;
		switch(type.getSecondComplexType())
		{
			case CardType.B_ICHIDAN_DOUSHI:
				return s.replaceFirst("る", "ます$");
			case CardType.B_IRREGULAR_DOUSHI:
				switch(s.charAt(s.length() - 2))
				{
					case 'す':
						return s.replaceFirst("する", "します$");
					case 'く':
						return s.replaceFirst("くる", "きます$");
					case '来':
						return s.replaceFirst("来る", "来ます$");
					default:
						return null;
				}
			case CardType.B_GODAN_DOUSHI:
				switch(s.charAt(s.length() - 1))
				{
					case 'す':
						return s.replaceFirst("す", "します$");
					case 'く':
						return s.replaceFirst("く", "きます$");
					case 'う':
						return s.replaceFirst("う", "います$");
					case 'つ':
						return s.replaceFirst("つ", "ちます$");
					case 'ぬ':
						return s.replaceFirst("ぬ", "にます$");
					case 'ふ':
						return s.replaceFirst("ふ", "ひます$");
					case 'む':
						return s.replaceFirst("む", "みます$");
					case 'る':
						return s.replaceFirst("る", "ります$");
					case 'ぐ':
						return s.replaceFirst("ぐ", "ぎます$");
					case 'ず':
						return s.replaceFirst("ず", "じます$");
					case 'ぶ':
						return s.replaceFirst("ぶ", "びます$");
					case 'ぷ':
						return s.replaceFirst("ぷ", "ぴます$");
					case 'づ':
						return s.replaceFirst("づ", "ぢます$");
					default:
						return null;
				}
			default:
				return null;
		}
	}

	public static String masu2dict(final String s, final CardType type)
	{
		if(s == null) return null;
		if(!s.endsWith("ます")) return null;
		if(type.getBasicType() != CardType.VERB) return null;
		switch(type.getSecondComplexType())
		{
			case CardType.B_ICHIDAN_DOUSHI:
				return s.replaceFirst("ます$", "る");
			case CardType.B_IRREGULAR_DOUSHI:
				switch(s.charAt(s.length() - 3))
				{
					case 'し':
						return s.replaceFirst("します$", "する");
					case 'き':
						return s.replaceFirst("きます$", "くる");
					case '来':
						return s.replaceFirst("来ます$", "来る");
					default:
						return null;
				}
			case CardType.B_GODAN_DOUSHI:
				switch(s.charAt(s.length() - 3))
				{
					case 'し':
						return s.replaceFirst("します$", "す");
					case 'き':
						return s.replaceFirst("きます$", "く");
					case 'い':
						return s.replaceFirst("います$", "う");
					case 'ち':
						return s.replaceFirst("ちます$", "つ");
					case 'に':
						return s.replaceFirst("にます$", "ぬ");
					case 'ひ':
						return s.replaceFirst("ひます$", "ふ");
					case 'み':
						return s.replaceFirst("みます$", "む");
					case 'り':
						return s.replaceFirst("ります$", "る");
					case 'ぎ':
						return s.replaceFirst("ぎます$", "ぐ");
					case 'じ':
						return s.replaceFirst("じます$", "ず");
					case 'び':
						return s.replaceFirst("びます$", "ぶ");
					case 'ぴ':
						return s.replaceFirst("ぴます$", "ぷ");
					case 'ぢ':
						return s.replaceFirst("ぢます$", "づ");
					default:
						return null;
				}
			default:
				return null;
		}
	}

	private final int verb_conjugation;

	private final int noun_article;

	WordInflector(final Context context)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		verb_conjugation = Integer.parseInt(prefs.getString("verb_conjugation", "0"));
		noun_article = Integer.parseInt(prefs.getString("noun_article", "0"));
	}

	public String conjugate(final Card card, final String s)
	{
		if(s == null) return null;
		String conjugated = null;
		switch(card.getType().getBasicType())
		{
			case CardType.VERB:
				switch(verb_conjugation)
				{
					case VERB_CONJUGATION_MASU:
						conjugated = WordInflector.dict2masu(s, card.getType());
						break;
					case VERB_CONJUGATION_DICT:
						conjugated = WordInflector.masu2dict(s, card.getType());
						break;
				}
				break;
			case CardType.NOUN:
				conjugated = addArticleToNoun(card, s, noun_article);
				break;
		}
		return conjugated == null ? s : conjugated;
	}

}
