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
package org.devwork.vocabtrain.sync;

import java.io.IOException;

import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.OnFinishListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.ParseException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.  This sample shows a basic 2-way
 * sync between the client and a sample server.  It also contains an
 * example of how to update the contacts' status messages, which
 * would be useful for a messaging or social networking client.
 */
@TargetApi(5)
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    private static final boolean NOTIFY_AUTH_FAILURE = true;

    private final AccountManager mAccountManager;

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @TargetApi(8)
	private void addPeriodicSync(Account account, String authority,long pollFrequency)
    {
    	ContentResolver.addPeriodicSync(account, authority, new Bundle(), pollFrequency < 1800 ? 1800 : pollFrequency);
    }
    
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
    	try
    	{
    		
	        final String authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, NOTIFY_AUTH_FAILURE);
	        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	        final boolean syncBooks = prefs.getBoolean("sync_books", true);
	        final boolean syncFiling = prefs.getBoolean("sync_filing", true);
	        Log.e(TAG, "" + syncBooks + syncFiling + " auth: " + authToken);
	        long pollFrequency = 3600*24*3;
	        final String pollFrequencyPref = prefs.getString("sync_frequency", null);
	        if(pollFrequencyPref != null)
	        	pollFrequency = Integer.parseInt(pollFrequencyPref);
	        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
	        	addPeriodicSync(account, authority, pollFrequency);
    		if(!syncFiling && !syncBooks) return;
    		
            new TimestampLoader(mContext, authToken, new TimestampLoader.OnFinishListener() {
            	@Override
            	public void onFinish(BookData[] books, LanguageData[] translation_languages, UserData user) {
            		Log.e(TAG, "" + books + " " + translation_languages);
            		if(books == null || translation_languages == null) return;
            		if(user != null)
            		{
            			SyncChangesLoader loader = new SyncChangesLoader(mContext, user, null);
            			loader.doInForeground();
            		}
            		if(syncBooks)
            		{
            			DownloadBooksLoader loader = new DownloadBooksLoader(mContext, books, translation_languages, null);
            			loader.doInForeground();
            		}
            		if(syncFiling && user != null)
            		{
	            		if(user.getOldTimestamp() == user.getTimestamp()) return; 
	            		if(user.getOldTimestamp() < user.getTimestamp())
	            			new DownloadFilingLoader(mContext, user, null).doInForeground();
	            		else
	            			new UploadFilingLoader(mContext, user, null).doInForeground();
            		}
            		
            	}
            }).doInForeground();
	        
	    } catch (final AuthenticatorException e) {
	        Log.e(TAG, "AuthenticatorException", e);
	        syncResult.stats.numParseExceptions++;
	    } catch (final OperationCanceledException e) {
	        Log.e(TAG, "OperationCanceledExcetpion", e);
	    } catch (final IOException e) {
	        Log.e(TAG, "IOException", e);
	        syncResult.stats.numIoExceptions++;
	    } catch (final ParseException e) {
	        Log.e(TAG, "ParseException", e);
	        syncResult.stats.numParseExceptions++;
	    }
        
    }
}
