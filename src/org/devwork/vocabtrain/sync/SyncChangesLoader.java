package org.devwork.vocabtrain.sync;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.DatabaseFunctions;
import org.devwork.vocabtrain.DatabaseHelper;
import org.devwork.vocabtrain.OnFinishListener;
import org.devwork.vocabtrain.R;
import org.devwork.vocabtrain.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class SyncChangesLoader extends AsyncTask<Void, String, Void>
{
	private static final String TAG = Constants.PACKAGE_NAME + ".SyncChangesLoader";
	private final Context context;
	private final OnFinishListener listener;
	private final FragmentActivity activity;
	private final UserData user;
	final static int MAX_RETRIES = 10;
	final static private String card_tablenames[] = { "changes_speech", "changes_script", "changes_speech_comment", "changes_script_comment" };
	final static private String card_type_tablename ="changes_type";
	final static private String translation_tablenames[] = { "changes_vernicular", "changes_vernicular_comment" };

	public SyncChangesLoader(final Context context, final UserData user, final OnFinishListener listener)
	{
		this.context = context;
		this.activity = null;
		this.listener = listener;
		this.user = user;
	}

	public SyncChangesLoader(final FragmentActivity activity, final UserData user, final OnFinishListener listener)
	{
		this.context = activity;
		this.activity = activity;
		this.listener = listener;
		this.user = user;
	}

	private void displayError(final String message)
	{
		if(activity != null)
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run()
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage(context.getString(R.string.sync_loader_error, message))
							.setTitle(context.getString(R.string.sync_loader_error_title))
							.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int id)
								{
									dialog.dismiss();
								}
							});
					final AlertDialog alert = builder.create();
					alert.show();
				}

			});

	}

	@Override
	protected Void doInBackground(final Void... arg0)
	{
		if(user == null) return null;
		try
		{

			final JSONObject json = flashDelta();
			Log.e(TAG, "json: " + json);
			if(json != null)
			{
				final HttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout Limit

				final HttpPost post = new HttpPost(Constants.serverUrl(PreferenceManager.getDefaultSharedPreferences(context), Constants.SERVER_CHANGES_UPLOAD));

				post.addHeader("Authorization", "token " + user.getAuthToken());
				final StringEntity str = new StringEntity(StringUtils.unicodeEscape(json.toString()));
				str.setContentType("application/json; charset=utf-8");
				str.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
				post.setEntity(str);

				final HttpResponse response = client.execute(post);
				Log.e(TAG, "response: " + response + " " + (response == null));
				if(isCancelled()) return null;
				if(response != null)
				{
					final InputStream in = response.getEntity().getContent(); // Get the data in the entity
					final BufferedReader input = new BufferedReader(new InputStreamReader(in));
					final StringBuilder entire = new StringBuilder();
					String line;
					while((line = input.readLine()) != null)
					{
						entire.append(line);
						if(line.contains("erfolgreich"))
						{
							final DatabaseHelper dbh = new DatabaseHelper(context);
							final SQLiteDatabase db = dbh.getWritableDatabase();
							DatabaseFunctions.dropChanges(db);
							DatabaseFunctions.createTables(db);
							db.close();
							dbh.close();
							break;
						}
					}
					Log.e(TAG, "entire: " + entire);
				}
			}
		}
		catch(final Throwable e)
		{
			displayError(e.getMessage());
		}

		return null;
	}

	public void doInForeground()
	{
		doInBackground();
		if(listener != null) listener.onFinish();
	}

	private JSONObject flashDelta() throws JSONException
	{
		final JSONObject root = new JSONObject();

		final DatabaseHelper dbh = new DatabaseHelper(context);
		final SQLiteDatabase db = dbh.getRead();
		boolean foundChanges = false;

		for(final String tablename : card_tablenames)
		{
			final JSONArray array = getTableCardDelta(tablename, db);
			if(array != null) foundChanges = true;
			root.put(tablename, array != null ? array : new JSONArray());
		}
		for(final String tablename : translation_tablenames)
		{
			final JSONArray array = getTableTranslationDelta(tablename, db);
			if(array != null) foundChanges = true;
			root.put(tablename, array != null ? array : new JSONArray());
		}
		{
			final Cursor cursor = db.rawQuery("SELECT * from `" + card_type_tablename + "`", null);
			final JSONArray array = new JSONArray();
			if(cursor.getCount() != 0)
			{
				foundChanges = true;
				while(cursor.moveToNext())
				{
					final JSONObject obj = new JSONObject();
					obj.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
					obj.put("changes_value", cursor.getInt(cursor.getColumnIndex("changes_value")));
					array.put(obj);
				}
			}
			cursor.close();
			root.put(card_type_tablename, array);
		}
		db.close();
		dbh.close();
		if(!foundChanges) return null;
		
		return root;
	}

	private JSONArray getTableCardDelta(final String tablename, final SQLiteDatabase db) throws JSONException
	{
		final Cursor cursor = db.rawQuery("SELECT * from `" + tablename + "`", null);
		if(cursor.getCount() != 0)
		{
			final JSONArray array = new JSONArray();
			while(cursor.moveToNext())
			{
				final JSONObject obj = new JSONObject();
				obj.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
				final String changes_value = cursor.getString(cursor.getColumnIndex("changes_value"));
				obj.put("changes_value", changes_value == null ? "" : changes_value);
				array.put(obj);
			}
			cursor.close();
			return array;

		}
		cursor.close();
		return null;
	}

	private JSONArray getTableTranslationDelta(final String tablename, final SQLiteDatabase db) throws JSONException
	{
		final Cursor cursor = db.rawQuery("SELECT * from `" + tablename + "`", null);
		if(cursor.getCount() != 0)
		{
			final JSONArray array = new JSONArray();
			while(cursor.moveToNext())
			{
				final JSONObject obj = new JSONObject();
				obj.put("_id", cursor.getLong(cursor.getColumnIndex("_id")));
				obj.put("changes_card_id", cursor.getLong(cursor.getColumnIndex("changes_card_id")));
				obj.put("changes_language", cursor.getString(cursor.getColumnIndex("changes_language")));
				final String changes_value = cursor.getString(cursor.getColumnIndex("changes_value"));
				obj.put("changes_value", changes_value == null ? "" : changes_value);
				array.put(obj);
			}
			cursor.close();
			return array;

		}
		cursor.close();
		return null;
	}

	@Override
	protected void onPostExecute(final Void result)
	{
		if(listener != null)
			listener.onFinish();

	}

}
