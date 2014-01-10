package org.devwork.vocabtrain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;

import android.content.Context;
import android.os.Parcel;

public class LanguageLocale
{

	enum Language implements Serializable
	{
		UNKNOWN
		{
			// TODO
			@Override
			public int toStringId()
			{
				return R.string.language_unknown;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_unknown;
			}

			@Override
			public String toString()
			{
				return "unk";
			}

			@Override
			public String getLanguage()
			{
				return "";
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}
		},
		GERMAN
		{
			@Override
			public int toStringId()
			{
				return -1;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_deu;
			}

			@Override
			public String toString()
			{
				return "deu";
			}

			@Override
			public String getLanguage()
			{
				return "de";
			}

			@Override
			public Locale getLocale()
			{
				return Locale.GERMAN;
			}
		},
		ITALIAN
		{
			@Override
			public int toStringId()
			{
				return -1;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_ita;
			}

			@Override
			public String toString()
			{
				return "ita";
			}

			@Override
			public String getLanguage()
			{
				return "it";
			}

			@Override
			public Locale getLocale()
			{
				return Locale.ITALIAN;
			}
		},
		FRENCH
		{
			@Override
			public int toStringId()
			{
				return -1;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_fra;
			}

			@Override
			public String toString()
			{
				return "fra";
			}

			@Override
			public String getLanguage()
			{
				return "fr";
			}

			@Override
			public Locale getLocale()
			{
				return Locale.FRENCH;
			}

		},
		ENGLISH
		{
			@Override
			public int toStringId()
			{
				return -1;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_eng;
			}

			@Override
			public String toString()
			{
				return "eng";
			}

			@Override
			public String getLanguage()
			{
				return "en";
			}

			@Override
			public Locale getLocale()
			{
				return Locale.ENGLISH;
			}
		},
		LATIN
		{
			@Override
			public String toString()
			{
				return "lat";
			}

			@Override
			public int toStringId()
			{
				return R.string.language_latin;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_lat;
			}

			@Override
			public String getLanguage()
			{
				return "la";
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}
		},
		CHINESE
		{
			@Override
			public String toString()
			{
				return "zho";
			}

			@Override
			public int toStringId()
			{
				return -1;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_zho;
			}

			@Override
			public String getLanguage()
			{
				return "zh";
			}

			@Override
			public Locale getLocale()
			{
				return Locale.CHINESE;
			}
		},
		SPANISH
		{
			@Override
			public String toString()
			{
				return "spa";
			}

			@Override
			public int toStringId()
			{
				return R.string.language_spanish;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_spa;
			}

			@Override
			public String getLanguage()
			{
				return "sp";
			}

			@Override
			public Locale getLocale()
			{
				return null;
			}
		},
		JAPANESE
		{
			@Override
			public String toString()
			{
				return "jpn";
			}

			@Override
			public int toStringId()
			{
				return -1;
			}

			@Override
			public int getFlagId()
			{
				return R.drawable.ic_flags_jpn;
			}

			@Override
			public String getLanguage()
			{
				return "ja";
			}

			@Override
			public Locale getLocale()
			{
				return Locale.JAPANESE;
			}
		};
		public abstract int toStringId();

		public abstract Locale getLocale();

		public String getDisplayLanguage()
		{
			if(getLocale() != null) return getLocale().getDisplayLanguage();
			return null;
		}

		public abstract String getLanguage();

		public abstract int getFlagId();
	}

	private final Language language;
	private final String languageName;

	public LanguageLocale(Parcel source)
	{
		language = (Language) source.readSerializable();
		languageName = source.readString();
	}

	public LanguageLocale(Context context, String id)
	{
		if(id == null)
			language = Language.UNKNOWN;
		else if(id.equals("ja") || id.equals("jpn"))
			language = Language.JAPANESE;
		else if(id.equals("en") || id.equals("eng"))
			language = Language.ENGLISH;
		else if(id.equals("de") || id.equals("deu") || id.equals("ger"))
			language = Language.GERMAN;
		else if(id.equals("fr") || id.equals("fre") || id.equals("fra"))
			language = Language.FRENCH;
		else if(id.equals("it") || id.equals("ita"))
			language = Language.ITALIAN;
		else if(id.equals("la") || id.equals("lat"))
			language = Language.LATIN;
		else if(id.equals("zh") || id.equals("zho"))
			language = Language.CHINESE;
		else if(id.equals("sp") || id.equals("spa"))
			language = Language.SPANISH;
		else
			language = Language.UNKNOWN;
		int stringId = language.toStringId();
		languageName = (stringId == -1 ? language.getDisplayLanguage() : context.getString(language.toStringId()));
	}

	@Override
	public boolean equals(Object o)
	{
		if(o instanceof LanguageLocale)
			return ((LanguageLocale) o).language == this.language;
		else if(o instanceof Language)
			return o == this.language;
		else if(o instanceof String) return this.toString().equals(o);
		return false;
	}

	public int getFlagId()
	{
		return language.getFlagId();
	}

	@Override
	public String toString()
	{
		return language.toString();
	}

	public String getDisplayLanguage()
	{
		return languageName;
	}

	public String getLanguage()
	{
		return language.getLanguage();
	}

	public boolean isInISOLanguageArray(String[] array)
	{
		Arrays.sort(array);
		return Arrays.binarySearch(array, getLanguage()) >= 0;
	}

	public Locale getLocale()
	{
		return language.getLocale();
	}

}
