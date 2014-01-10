package org.devwork.vocabtrain;


public class Card // implements Parcelable
{

	private final long id;
	private final String script;
	private final String speech;
	private final String vernicular;
	private final String script_comment;
	private final String speech_comment;
	private final String vernicular_comment;
	private final CardType type;
	private final CardFiling filing;
	private final LanguageLocale language;

	Card(long id, String script, String speech, String vernicular, String script_comment, String speech_comment, String vernicular_comment, CardType type, LanguageLocale language, CardFiling filing)
	{
		this.id = id;
		this.script = script;
		this.speech = speech;
		this.vernicular = vernicular;
		this.script_comment = script_comment;
		this.speech_comment = speech_comment;
		this.vernicular_comment = vernicular_comment;
		this.type = type;
		this.language = language;
		this.filing = filing;
	}
/*
	public Card(Card c)
	{
		this.id = c.id;
		this.script = c.script;
		this.speech = c.speech;
		this.vernicular = c.vernicular;
		this.script_comment = c.script_comment;
		this.speech_comment = c.speech_comment;
		this.vernicular_comment = c.vernicular_comment;
		this.type = c.type;
		this.language = c.language;
		this.filing = new CardFiling(c.filing);
	}
*/
	/*
	 * public Card(Parcel source) { this.id = source.readLong(); this.script = source.readString(); this.speech = source.readString(); this.vernicular = source.readString(); this.script_comment = source.readString(); this.speech_comment = source.readString(); this.vernicular_comment = source.readString(); this.language = new LanguageLocale(source); this.type = new CardType(source.readInt(); this.filing = new CardFiling(source); }
	 */
	public LanguageLocale getLanguage()
	{
		return language;
	}

	public String getVernicular()
	{
		return vernicular;
	}

	public String getScript()
	{
		return script;
	}

	public String getSpeech()
	{
		return speech;
	}

	public long getId()
	{
		return id;
	}

	public CardFiling getFiling()
	{
		return filing;
	}

	public String getVernicularComment()
	{
		return vernicular_comment;
	}

	public String getScriptComment()
	{
		return script_comment;
	}

	public String getSpeechComment()
	{
		return speech_comment;
	}

	public CardType getType()
	{
		return type;
	}

	public int getFlagId()
	{
		return language.getFlagId();
	}

}
