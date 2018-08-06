package org.devwork.vocabtrain.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.DatabaseFunctions;
import org.devwork.vocabtrain.DatabaseHelper;
import org.devwork.vocabtrain.OnFinishListener;
import org.devwork.vocabtrain.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

public class UploadFilingLoader extends AbstractLoader
{
	private static final String TAG = Constants.PACKAGE_NAME + ".UploadFilingLoader";
	private final OnFinishListener listener;
	private final UserData user;

	public UploadFilingLoader(Context context, UserData user, OnFinishListener listener)
	{
		super(context, R.string.sync_loader_filing_upload, R.string.sync_loader_filing_upload_desc, true);
		this.listener = listener;
		this.user = user;

	}

	@Override
	public void doInForeground()
	{
		doInBackground();
		if(listener != null) listener.onFinish();
	}

	@Override
	protected Void doInBackground(Void... arg0)
	{
		if(user == null) return null;
		setSuccess(false);

		final DatabaseHelper dbh = new DatabaseHelper(getContext());
		final File infile = DatabaseFunctions.createTempFile(new File(dbh.getFilename()).getParentFile());
		final File infileZ = new File(infile.toString() + ".gz");
		try
		{
			infile.createNewFile();
			DatabaseFunctions.copyData(dbh.getFilename(), infile.toString());

			{
				FileInputStream in = new FileInputStream(infile);
				GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(infileZ));
				byte[] buffer = new byte[512];
				int count;
				while((count = in.read(buffer)) > 0)
					out.write(buffer, 0, count);
				out.close();
				in.close();
			}

			// publishProgress(activity.getString(R.string.loader_database_sync_upload)); // TODO
			final HttpClient client = new DefaultHttpClient();
			HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout Limit
			client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

			/*
			final HttpPost post = new HttpPost(Constants.SERVER_FILING_UPLOAD);
			final MultipartEntity multipartEntity = new MultipartEntity();
			final ContentBody fileBody = new FileBody(infileZ, "application/gzip-compressed");
			multipartEntity.addPart("database", fileBody);
			multipartEntity.addPart("user_token", new StringBody(user.getAuthToken()));
			post.addHeader(multipartEntity.getContentType());
			post.setEntity(multipartEntity);
			 */
			final HttpPost post = new HttpPost(Constants.serverUrl(PreferenceManager.getDefaultSharedPreferences(getContext()), Constants.SERVER_FILING_UPLOAD));
			post.addHeader("Authorization", "token " + user.getAuthToken());
			post.setEntity(new FileEntity(infileZ, "application/gzip-compressed"));
			
			System.out.println("executing request " + post.getRequestLine());

			/*
			 * final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(); params.add(new BasicNameValuePair("user_token", user.getAuthToken())); final HttpEntity entity = new UrlEncodedFormEntity(params); post.addHeader(entity.getContentType()); post.setEntity(entity); final FileEntity str = new FileEntity(infile, "application/x-sqlite3"); post.setEntity(str);
			 */

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
						setSuccess(true);
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
		catch(Throwable e)
		{
			displayError(e.getMessage());
		}
		finally
		{
			dbh.close();
			if(infile != null && infile.exists()) infile.delete();
			if(infileZ != null && infileZ.exists()) infileZ.delete();
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result)
	{
		if(isSuccessful()) showToast(getContext().getString(R.string.sync_loader_filing_upload_finished));
		if(listener != null) listener.onFinish();
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		if(listener != null) listener.onFinish();
	}
}
