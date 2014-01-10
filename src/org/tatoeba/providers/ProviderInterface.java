package org.tatoeba.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public interface ProviderInterface
{
	public static final String AUTHORITY = "org.tatoeba.providers.TranslationProvider";
	public static final String SEARCH_TABLE = "search";
	public static final String LINK_TABLE = "links";
	public static final String RUBY_TABLE = "ruby";

	public static final class SearchTable implements BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + ProviderInterface.AUTHORITY + "/" + SEARCH_TABLE);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.tatoeba.search";
		public final static String LANGUAGE = "lang";
	}

	public static final class LinkTable implements BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + ProviderInterface.AUTHORITY + "/" + LINK_TABLE);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.org.tatoeba.link";
		public final static String LANGUAGE = "lang";
		public final static String CONTENT = "text";
	}

	public static final class RubyTable implements BaseColumns
	{
		public static final Uri CONTENT_URI = Uri.parse("content://" + ProviderInterface.AUTHORITY + "/" + RUBY_TABLE);
		public final static String CONTENT = "text";
	}
}
