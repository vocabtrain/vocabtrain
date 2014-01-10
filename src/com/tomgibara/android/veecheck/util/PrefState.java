/*
 * Copyright 2008 Tom Gibara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomgibara.android.veecheck.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tomgibara.android.veecheck.VeecheckState;

/**
 * A convenient implementation of {@link VeecheckState} that stores its state
 * in a {@link SharedPreferences}.
 * 
 * @author Tom Gibara
 *
 */

public class PrefState implements VeecheckState {

	public static final String SHARED_PREFS_NAME = VeecheckState.class.getPackage().getName()+".STATE";

	public static final String KEY_LAST_CHECK         = "last_check";
	public static final String KEY_INGNORED_INTENT    = "ignored_intent";

	public static final long   DEFAULT_LAST_CHECK     = -1L;
	public static final String DEFAULT_IGNORED_INTENT = null;

	private static String intentToString(Intent intent) {
		//TODO temporary implementation - needs to order categories
		return intent.getClass()+"|"+intent.getAction()+"|"+intent.getDataString()+"|"+intent.getType()+"|"+intent.getCategories();
	}

	/**
	 * @param context the invoking context
	 * @return the preferences into which this state is persisted 
	 */
	
	public static SharedPreferences getSharedPrefs(Context context) {
		return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	private final SharedPreferences prefs;

	public PrefState(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public PrefState(Context context) {
		this(getSharedPrefs(context));
	}
	
	@Override
	public long getLastCheck() {
		return prefs.getLong(KEY_LAST_CHECK, DEFAULT_LAST_CHECK);
	}

	@Override
	public void setLastCheckNow(long lastCheck) {
		prefs.edit().putLong(KEY_LAST_CHECK, lastCheck).commit();
	}
	
	@Override
	public void setIgnoredIntent(Intent intent) {
		prefs.edit().putString(KEY_INGNORED_INTENT, intentToString(intent)).commit();
		
	}

	@Override
	public boolean isIgnoredIntent(Intent intent) {
		return intentToString(intent).equals(prefs.getString(KEY_INGNORED_INTENT, DEFAULT_IGNORED_INTENT));
	}
	
}
