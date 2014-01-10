package org.devwork.vocabtrain.sync;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.DatabaseHelper;
import org.devwork.vocabtrain.IncompatibleFunctions;
import org.devwork.vocabtrain.R;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class TimestampLoader extends AbstractLoader
{
	public interface OnFinishListener
	{
		public void onFinish(BookData[] array, LanguageData[] languages, UserData user);
	}

	private static final String TAG = Constants.PACKAGE_NAME + ".TimestampLoader";

	private final OnFinishListener listener;

	private BookData[] books = null;

	private LanguageData[] languages = null;

	private UserData user = null;
	private String authToken = null;

	public TimestampLoader(final Context context, final OnFinishListener listener)
	{
		super(context, R.string.sync_loader_timestamp, R.string.sync_loader_timestamp_desc, false);
		this.listener = listener;
	}

	public TimestampLoader(final Context context, final String authToken, final OnFinishListener onFinishListener)
	{
		this(context, onFinishListener);
		this.authToken = authToken;
	}

	@Override
	protected Void doInBackground(final Void... arg0)
	{

		final DatabaseHelper dbh = new DatabaseHelper(getContext());
		try
		{
			Log.e(TAG, "Starting");
			if(isCancelled()) return null;
			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			final HttpPost post = new HttpPost(Constants.SERVER_QUERY);

			if(authToken == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) authToken = IncompatibleFunctions.obtainAuthToken(getContext());
			if(authToken != null) post.addHeader("Authorization", "token " + authToken);
			final HttpEntity entity = new UrlEncodedFormEntity(params);
			post.addHeader(entity.getContentType());
			
			post.addHeader("Accept", "application/json");
			post.addHeader("Accept-Encoding", "gzip");
			post.setEntity(entity);

			final HttpResponse response = SyncFunctions.getHttpClient().execute(post);
			final HttpEntity responseEntity = response.getEntity();
			setProgressMax(responseEntity.getContentLength());
			//TODO final BufferedReader input = new BufferedReader(new InputStreamReader(new GZIPInputStream(responseEntity.getContent())));
			final BufferedReader input = new BufferedReader(new InputStreamReader((responseEntity.getContent())));

			final StringBuilder content = new StringBuilder();
			String line;
			while((line = input.readLine()) != null)
			{
				content.append(line);
				addProgress(line.codePointCount(0, line.length()));
				if(isCancelled()) return null;
			}

			Log.e(TAG, "Len: " + response.getEntity().getContentLength());
			Log.e(TAG, content.toString());
			setCancelable(false);

			final JSONObject jroot = new JSONObject(content.toString());
			final JSONArray jbooks = jroot.getJSONArray("books");
			final JSONArray jlanguages = jroot.getJSONArray("translation_languages");

			final JSONObject juser = jroot.has("userdata") ? jroot.getJSONObject("userdata") : null;

			books = new BookData[jbooks.length()];
			for(int i = 0; i < jbooks.length(); ++i)
			{
				books[i] = new BookData(jbooks.getJSONObject(i), dbh.getRead());
			}
			languages = new LanguageData[jlanguages.length()];
			for(int i = 0; i < jlanguages.length(); ++i)
			{
				languages[i] = new LanguageData(jlanguages.getString(i), dbh.getRead());
			}
			if(juser != null)
			{
				user = new UserData(juser, dbh.getRead(), authToken);
			}

		}
		catch(final Throwable e)
		{
			displayError(e.getLocalizedMessage());
		}
		finally
		{
			dbh.close();
		}
		Log.e(TAG, "finished");
		return null;
	}

	@Override
	public void doInForeground()
	{
		doInBackground();
		if(listener != null) listener.onFinish(books, languages, user);
	}

	@Override
	protected void onCancelled()
	{
		Log.e(TAG, "Cancelled");
		if(listener != null) listener.onFinish(null, null, null);
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(final Void result)
	{
		if(listener != null) listener.onFinish(books, languages, user);
		super.onPostExecute(result);
	}
}