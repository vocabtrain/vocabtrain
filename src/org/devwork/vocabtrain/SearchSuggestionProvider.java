package org.devwork.vocabtrain;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider
{
	public final static String AUTHORITY = "org.devwork.vocabtrain.SearchSuggestionProvider";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public SearchSuggestionProvider()
	{
		setupSuggestions(AUTHORITY, MODE);
	}
}