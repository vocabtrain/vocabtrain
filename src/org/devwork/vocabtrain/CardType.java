package org.devwork.vocabtrain;

import android.content.Context;

public class CardType
{

	public CardType[] getFirstSelectionType()
	{
		return getFirstSelectionType(getBasicType());
	}

	public CardType[] getFirstSelectionType(int basicValue)
	{
		switch(basicValue)
		{
			case VERB:
				return new CardType[] { createType(0, 0), createType(basicValue, A_TRANSITIVE), createType(basicValue, A_INTRANSITIVE), createType(basicValue, A_REFLEXIVE) };
			case ADJECTIVE:
				if(language.equals(LanguageLocale.Language.JAPANESE))
					return new CardType[] { createType(0, 0), createType(basicValue, A_NA_ADJECTIVE), createType(basicValue, A_I_ADJECTIVE) };
				else
					return new CardType[] { createType(0, 0), createType(basicValue, A_COMPARATIVE), createType(basicValue, A_SUPERLATIVE) };
			case ADVERB:
				return new CardType[] { createType(0, 0), createType(basicValue, A_COMPARATIVE), createType(basicValue, A_SUPERLATIVE) };

			case ADPOSITION:
				return new CardType[] { createType(0, 0), createType(basicValue, A_PREPOSITION), createType(basicValue, A_POSTPOSITION), createType(basicValue, A_PARTICLE) };
			case CONJUGATION:
				return null;
			case ABBREVIATION:
				return null;
			case SAW:
				return null;
			case NOUN:
				return new CardType[] { createType(0, 0), createType(basicValue, A_FEMININE), createType(basicValue, A_MASCULINE), createType(basicValue, A_NEUTER), createType(basicValue, A_FEMININE_PLURAL), createType(basicValue, A_MASCULINE_PLURAL), createType(basicValue, A_NEUTER_PLURAL) };
		}
		return null;
	}

	public CardType[] getBasicSelectionType()
	{
		return new CardType[] { createType(0, 0), createType(VERB, 0), createType(ADJECTIVE, 0), createType(ADVERB, 0), createType(ADPOSITION, 0), createType(CONJUGATION, 0), createType(ABBREVIATION, 0), createType(SAW, 0), createType(NOUN, 0) };
	}

	public CardType[] getSecondSelectionType()
	{
		switch(getBasicType())
		{
			case VERB:
				if(language.equals(LanguageLocale.Language.JAPANESE))
					return new CardType[] { createType(0, 0), createType(value, 0, B_GODAN_DOUSHI), createType(value, 0, B_ICHIDAN_DOUSHI), createType(value, 0, B_IRREGULAR_DOUSHI) };
				else
					return null;
		}
		return null;
	}

	final static public int VERB = 1;
	final static public int ADJECTIVE = 2;
	final static public int ADVERB = 3;
	final static public int ADPOSITION = 4;
	final static public int CONJUGATION = 5;
	final static public int ABBREVIATION = 6;
	final static public int SAW = 7;
	final static public int NOUN = 8;

	final static public int A_TRANSITIVE = 1;
	final static public int A_INTRANSITIVE = 2;
	final static public int A_REFLEXIVE = 3;

	/* japanese */
	final static public int B_GODAN_DOUSHI = 1;
	final static public int B_ICHIDAN_DOUSHI = 2;
	final static public int B_IRREGULAR_DOUSHI = 3;

	final static public int A_COMPARATIVE = 1;
	final static public int A_SUPERLATIVE = 2;

	/* japanese */
	final static public int A_NA_ADJECTIVE = 1;
	final static public int A_I_ADJECTIVE = 2;

	final static public int A_PREPOSITION = 1;
	final static public int A_POSTPOSITION = 2;
	final static public int A_PARTICLE = 3;

	final static public int A_FEMININE = 1;
	final static public int A_MASCULINE = 2;
	final static public int A_NEUTER = 3;
	final static public int A_FEMININE_PLURAL = 4;
	final static public int A_MASCULINE_PLURAL = 5;
	final static public int A_NEUTER_PLURAL = 6;

	private final int value;
	private final Context context;
	private final LanguageLocale language;

	public CardType(Context context, LanguageLocale language, int value)
	{
		this.language = language;
		this.context = context;
		this.value = value;
	}

	public boolean isUndefined()
	{
		return value == 0;
	}

	public CardType combineType(CardType firstType, CardType secondType)
	{
		return createType(this.getBasicType(), firstType.getFirstComplexType(), secondType.getSecondComplexType());
	}

	private CardType createType(int basic, int complex)
	{
		return new CardType(context, language, basic + complex * 100);
	}

	public CardType createType(int basic, int firstComplex, int secondComplex)
	{
		return new CardType(context, language, basic + firstComplex * 100 + secondComplex * 1000);
	}

	int getBasicType()
	{
		return value % 100;
	}

	int getFirstComplexType()
	{
		return value / 100 % 10;
	}

	int getSecondComplexType()
	{
		return value / 1000 % 10;
	}

	public int getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		if(getSecondComplexType() != 0)
		{
			switch(getSecondComplexType())
			{
				case B_GODAN_DOUSHI:
					return context.getString(R.string.type_special_godan_doushi);
				case B_ICHIDAN_DOUSHI:
					return context.getString(R.string.type_special_ichidan_doushi);
				case B_IRREGULAR_DOUSHI:
					return context.getString(R.string.type_special_irregular_doushi);
				default:
					return null;
			}
		}
		else if(getFirstComplexType() != 0)
		{
			switch(getBasicType())
			{
				case VERB:
					switch(getFirstComplexType())
					{
						case A_TRANSITIVE:
							return context.getString(R.string.type_special_transitive);

						case A_INTRANSITIVE:
							return context.getString(R.string.type_special_intransitive);

						case A_REFLEXIVE:
							return context.getString(R.string.type_special_reflexive);

						default:
							return null;
					}
				case ADJECTIVE:
					if(language.equals(LanguageLocale.Language.JAPANESE))
						switch(getFirstComplexType())
						{
							case A_NA_ADJECTIVE:
								return context.getString(R.string.type_special_adjective_na);
							case A_I_ADJECTIVE:
								return context.getString(R.string.type_special_adjective_i);
							default:
								return null;

						}
					else
						switch(getFirstComplexType())
						{
							case A_COMPARATIVE:
								return context.getString(R.string.type_special_comparative);
							case A_SUPERLATIVE:
								return context.getString(R.string.type_special_superlative);
							default:
								return null;
						}
				case ADVERB:
					switch(getFirstComplexType())
					{
						case A_COMPARATIVE:
							return context.getString(R.string.type_special_comparative);
						case A_SUPERLATIVE:
							return context.getString(R.string.type_special_superlative);
						default:
							return null;
					}
				case ADPOSITION:
					switch(getFirstComplexType())
					{
						case A_PREPOSITION:
							return context.getString(R.string.type_adposition_preposition);
						case A_POSTPOSITION:
							return context.getString(R.string.type_adposition_postposition);
						case A_PARTICLE:
							return context.getString(R.string.type_adposition_particle);
						default:
							return null;
					}
				case NOUN:
					switch(getFirstComplexType())
					{
						case A_FEMININE:
							return context.getString(R.string.type_noun_feminine);
						case A_MASCULINE:
							return context.getString(R.string.type_noun_maskuline);
						case A_NEUTER:
							return context.getString(R.string.type_noun_neuter);
						case A_FEMININE_PLURAL:
							return context.getString(R.string.type_noun_feminine_plural);
						case A_MASCULINE_PLURAL:
							return context.getString(R.string.type_noun_maskuline_plural);
						case A_NEUTER_PLURAL:
							return context.getString(R.string.type_noun_neuter_plural);
						default:
							return context.getString(R.string.type_noun);
					}
			}
		}
		else
		{
			switch(getBasicType())
			{
				case VERB:
					return context.getString(R.string.type_verb);

				case ADJECTIVE:
					return context.getString(R.string.type_adjective);

				case ADVERB:
					return context.getString(R.string.type_adverb);
				case ADPOSITION:
					return context.getString(R.string.type_adposition);
				case CONJUGATION:
					return context.getString(R.string.type_conjugation);
				case ABBREVIATION:
					return context.getString(R.string.type_abbreviation);
				case SAW:
					return context.getString(R.string.type_saw);
				case NOUN:
					return context.getString(R.string.type_noun);
			}
		}
		return context.getString(R.string.type_unknown);
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof CardType)
		{
			CardType c = (CardType) o;
			if(c.getBasicType() != this.getBasicType()) return false;
			if(c.getSecondComplexType() == 0)
			{
				if(c.getFirstComplexType() == 0) return true;
				if(c.getFirstComplexType() == this.getFirstComplexType()) return true;
			}
			if(c.getFirstComplexType() == 0)
			{
				if(c.getSecondComplexType() == this.getSecondComplexType()) return true;
			}
			return c.value == this.value;
		}
		if(o instanceof Integer)
		{
			return (((Integer) o).intValue()) == value;
		}
		return false;
	}

	String toDetailedString()
	{
		switch(getBasicType())
		{
			case VERB:
				switch(getFirstComplexType())
				{
					case A_TRANSITIVE:
						switch(getSecondComplexType())
						{
							case B_GODAN_DOUSHI:
								return context.getString(R.string.type_verb_transitive_godan_doushi);
							case B_ICHIDAN_DOUSHI:
								return context.getString(R.string.type_verb_transitive_ichidan_doushi);
							case B_IRREGULAR_DOUSHI:
								return context.getString(R.string.type_verb_transitive_irregular_doushi);
							default:
								return context.getString(R.string.type_verb_transitive);
						}
					case A_INTRANSITIVE:
						switch(getSecondComplexType())
						{
							case B_GODAN_DOUSHI:
								return context.getString(R.string.type_verb_intransitive_godan_doushi);
							case B_ICHIDAN_DOUSHI:
								return context.getString(R.string.type_verb_intransitive_ichidan_doushi);
							case B_IRREGULAR_DOUSHI:
								return context.getString(R.string.type_verb_intransitive_irregular_doushi);
							default:
								return context.getString(R.string.type_verb_intransitive);
						}
					case A_REFLEXIVE:
						switch(getSecondComplexType())
						{
							case B_GODAN_DOUSHI:
								return context.getString(R.string.type_verb_reflexive_godan_doushi);
							case B_ICHIDAN_DOUSHI:
								return context.getString(R.string.type_verb_reflexive_ichidan_doushi);
							case B_IRREGULAR_DOUSHI:
								return context.getString(R.string.type_verb_reflexive_irregular_doushi);
							default:
								return context.getString(R.string.type_verb_reflexive);
						}
					default:
						switch(getSecondComplexType())
						{
							case B_GODAN_DOUSHI:
								return context.getString(R.string.type_verb_godan_doushi);
							case B_ICHIDAN_DOUSHI:
								return context.getString(R.string.type_verb_ichidan_doushi);
							case B_IRREGULAR_DOUSHI:
								return context.getString(R.string.type_verb_irregular_doushi);
							default:
								return context.getString(R.string.type_verb);
						}
				}
			case ADJECTIVE:
				if(language.equals(LanguageLocale.Language.JAPANESE))
					switch(getFirstComplexType())
					{
						case A_NA_ADJECTIVE:
							return context.getString(R.string.type_adjective_na);
						case A_I_ADJECTIVE:
							return context.getString(R.string.type_adjective_i);
						default:
							return context.getString(R.string.type_adjective);
					}
				else
					switch(getFirstComplexType())
					{
						case A_COMPARATIVE:
							return context.getString(R.string.type_adjective_comparative);
						case A_SUPERLATIVE:
							return context.getString(R.string.type_adjective_superlative);
						default:
							return context.getString(R.string.type_adjective);
					}
			case ADVERB:
				switch(getFirstComplexType())
				{
					case A_COMPARATIVE:
						return context.getString(R.string.type_adverb_comparative);
					case A_SUPERLATIVE:
						return context.getString(R.string.type_adverb_superlative);
					default:
						return context.getString(R.string.type_adverb);
				}
			case ADPOSITION:
				switch(getFirstComplexType())
				{
					case A_PREPOSITION:
						return context.getString(R.string.type_adposition_preposition);
					case A_POSTPOSITION:
						return context.getString(R.string.type_adposition_postposition);
					case A_PARTICLE:
						return context.getString(R.string.type_adposition_particle);
					default:
						return context.getString(R.string.type_adposition);
				}
			case CONJUGATION:
				return context.getString(R.string.type_conjugation);
			case ABBREVIATION:
				return context.getString(R.string.type_abbreviation);
			case SAW:
				return context.getString(R.string.type_saw);
			case NOUN:
				switch(getFirstComplexType())
				{
					case A_FEMININE:
						return context.getString(R.string.type_noun_feminine);
					case A_MASCULINE:
						return context.getString(R.string.type_noun_maskuline);
					case A_NEUTER:
						return context.getString(R.string.type_noun_neuter);
					case A_FEMININE_PLURAL:
						return context.getString(R.string.type_noun_feminine_plural);
					case A_MASCULINE_PLURAL:
						return context.getString(R.string.type_noun_maskuline_plural);
					case A_NEUTER_PLURAL:
						return context.getString(R.string.type_noun_neuter_plural);
					default:
						return context.getString(R.string.type_noun);
				}
		}
		return context.getString(R.string.type_unknown);
	}
}
