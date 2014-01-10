/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.devwork.vocabtrain;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class IncompatibleFunctions
{
	private static final String TAG = "IncompatibleFunctions";

	public static String obtainAuthToken(final Context context) throws Exception
	{
		final AccountManager accountManager = AccountManager.get(context);
		final Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		if(accounts.length > 0)
		{
			final String authToken = accountManager.blockingGetAuthToken(accounts[0], Constants.AUTHTOKEN_TYPE, true);
			Log.e(TAG, authToken);
			return authToken;
		}
		return null;
	}

	public static void createSearchView(Activity activity, Menu menu, int menu_id)
	{
		// Get the SearchView and set the searchable configuration
		final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
		MenuItem menuitem = menu.findItem(menu_id);
		if(menuitem == null) return;
		final SearchView searchView = (SearchView) menuitem.getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
		searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
