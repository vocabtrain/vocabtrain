package org.devwork.vocabtrain.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.DatabaseFunctions;
import org.devwork.vocabtrain.DatabaseHelper;
import org.devwork.vocabtrain.OnFinishListener;
import org.devwork.vocabtrain.R;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadFilingLoader extends AbstractLoader {
	private static final String TAG = Constants.PACKAGE_NAME + ".DownloadFilingLoader";
	private final UserData user;

	
	private final OnFinishListener listener;
	public DownloadFilingLoader(Context context, UserData user, OnFinishListener listener)
	{
		super(context, R.string.sync_loader_filing_download, R.string.sync_loader_filing_download_desc, false);
		this.listener = listener;
		this.user = user;
	}
	/*
	private void updateFilingData(SQLiteDatabase db, JSONObject juser, String key) throws JSONException
	{
    	ContentValues v = new ContentValues();
    	v.put("fdata_key", key);
    	v.put("fdata_value", juser.getString("user_filing_" + key));
    	db.insert("filing_data", null, v);
	}
	*/
	@Override
	public void doInForeground()
	{
		doInBackground();
		if(listener != null) listener.onFinish();
		
	}
	@Override
	protected Void doInBackground(Void... arg0) {
		if(user == null) return null;
		DatabaseHelper dbh = new DatabaseHelper(getContext());
		File outfile = null;
		try 
		{
			if(isCancelled()) return null;
			final HttpPost post = new HttpPost(Constants.serverUrl(PreferenceManager.getDefaultSharedPreferences(getContext()), Constants.SERVER_FILING_DOWNLOAD));
			
			post.addHeader("Authorization", "token " + user.getAuthToken());
	        final HttpResponse response = SyncFunctions.getHttpClient().execute(post);
	        final HttpEntity responseEntity = response.getEntity();
            setProgressMax(responseEntity.getContentLength());
            
            outfile = DatabaseFunctions.createTempFile(new File(dbh.getFilename()).getParentFile());
            InputStream input = new GZIPInputStream(responseEntity.getContent());
           // InputStream input = responseEntity.getContent();
            OutputStream output = new FileOutputStream(outfile);
            Log.e("outfile", "" + outfile);
            byte data[] = new byte[1024];
            
            int total = 0;
            int count = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                setProgress(total);
                output.write(data, 0, count);
                if(isCancelled())
                {   
                    output.close();
                    input.close();
                    outfile.delete();
                    return null;
                }
            }
            setCancelable(false);
            output.flush();
            output.close();
            input.close();
            
			DatabaseFunctions.copyData(outfile.toString(), dbh.getFilename());
			outfile.delete();

		}
		catch (Exception e) {
			displayError(e.getMessage());
		}
		finally
		{
			dbh.close();
			if(outfile != null && outfile.exists()) outfile.delete();
		}
		Log.e(TAG, "finished");
		return null;
	}
	
    @Override
    protected void onPostExecute(Void result)
    {
    	if(listener != null) listener.onFinish();
   		super.onPostExecute(result);
   		if(isSuccessful())
   			showToast(getContext().getString(R.string.sync_loader_filing_download_finished));
    }
	
}
