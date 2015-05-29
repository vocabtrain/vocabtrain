package org.devwork.vocabtrain.sync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
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
import org.devwork.vocabtrain.sync.BookData.State;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadBooksLoader extends AbstractLoader
{
	private static final String TAG = Constants.PACKAGE_NAME + ".DownloadBooksLoader";
	private final BookData[] books;
	private final LanguageData[] languages;

	private final OnFinishListener listener;

	public DownloadBooksLoader(final Context context, final BookData[] books, final LanguageData[] languages, final OnFinishListener listener)
	{
		super(context, R.string.sync_loader_book_download, R.string.sync_loader_book_download_desc, false);
		this.books = books;
		this.languages = languages;
		this.listener = listener;
	}

	@Override
	protected Void doInBackground(final Void... arg0)
	{

		final List<String> changeBook_ids = new LinkedList<String>();
		final List<String> addBook_ids = new LinkedList<String>(changeBook_ids);

		for(final LanguageData language : languages)
		{
			if(language.getState() == LanguageData.State.ADD || language.getState() == LanguageData.State.REMOVE)
			{
				for(final BookData book : books)
				{
					if(book.getState() == State.INSTALLED && Arrays.binarySearch(book.getTranslationLanguages(), language.getLanguage()) > 0)
					{
						changeBook_ids.add("" + book.getId());
						if(language.getState() == LanguageData.State.ADD)
							addBook_ids.add("" + book.getId());
					}
				}
			}

		}
		for(final BookData book : books)
		{
			if(book.getState() == State.UPDATE || book.getState() == State.ADD)
			{
				changeBook_ids.add("" + book.getId());
				addBook_ids.add("" + book.getId());
			}
			else if(book.getState() == State.REMOVE)
				changeBook_ids.add("" + book.getId());

		}
		Log.e(TAG, "changesBook_ids: " + changeBook_ids);
		if(changeBook_ids.isEmpty()) return null;
		File outfile = null;
		final DatabaseHelper dbh = new DatabaseHelper(getContext());
		try
		{
			if(isCancelled()) return null;
			if(!addBook_ids.isEmpty())
			{
				final JSONObject json = getQuery(addBook_ids);
				Log.e(TAG, "json: " + json);
				final HttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout Limit
				 
				
				final HttpPost post = new HttpPost(Constants.serverUrl(PreferenceManager.getDefaultSharedPreferences(getContext()), Constants.SERVER_BOOKS_DOWNLOAD));

				final StringEntity str = new StringEntity(StringUtils.unicodeEscape(json.toString()));
				str.setContentType("application/json; charset=utf-8");
				str.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
				post.setEntity(str);

				final HttpResponse response = client.execute(post);
				Log.e(TAG, "response: " + response + " " + (response == null));
				if(isCancelled()) return null;
				if(response == null) return null;

				final HttpEntity responseEntity = response.getEntity();
				setProgressMax(responseEntity.getContentLength());

				outfile = DatabaseFunctions.createTempFile(new File(dbh.getFilename()).getParentFile());
				final InputStream input = new GZIPInputStream(responseEntity.getContent());
				//final InputStream input = responseEntity.getContent();
				final OutputStream output = new FileOutputStream(outfile);

				final byte data[] = new byte[1024];

				int total = 0;
				int count = 0;
				while((count = input.read(data)) != -1)
				{
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
				output.flush();
				output.close();
				input.close();
				setCancelable(false);
				final SQLiteDatabase db = dbh.getWritableDatabase();

				db.execSQL("ATTACH '" + outfile + "' AS i"); // We cannot use BindArgs as usual due to this bug appeared in Honeycomb ::
																// http://code.google.com/p/android/issues/detail?id=15499

				setProgress(0);
				setProgressMax(5);
				setProgressMessage(getContext().getResources().getString(R.string.sync_loader_book_download_insert));
				db.execSQL("INSERT OR REPLACE INTO books SELECT * FROM i.books");
				incrementProgress();
				db.execSQL("INSERT OR REPLACE INTO chapters SELECT * FROM i.chapters");
				incrementProgress();
				db.execSQL("INSERT OR REPLACE INTO content SELECT * FROM i.content");
				incrementProgress();
				db.execSQL("INSERT OR REPLACE INTO cards SELECT * FROM i.cards");
				incrementProgress();
				db.execSQL("INSERT OR REPLACE INTO translations SELECT translation_card_id, translation_language, translation_content, translation_comment FROM i.translations");
				incrementProgress();
				db.execSQL("DETACH DATABASE i");
				db.close();
				outfile.delete();
			}
			final SQLiteDatabase db = dbh.getWritableDatabase();
			setProgress(0);
			setProgressMax(5);
			setProgressMessage(getContext().getResources().getString(R.string.sync_loader_book_download_cleanup));

			// Remove
			for(final BookData book : books)
			{
				if(book.getState() == State.REMOVE)
				{
					db.delete("books", "_id = ?", new String[] { "" + book.getId() });
					db.delete("chapters", "chapter_book_id = ?", new String[] { "" + book.getId() });
				}
			}
			incrementProgress();
			for(final LanguageData language : languages)
				if(language.getState() == LanguageData.State.REMOVE)
				{
					Log.e("DELETING", language.getLanguage());
					db.execSQL(
							"DELETE FROM translations WHERE translation_language = ? AND translation_card_id IN ("
									+
									"SELECT t.translation_card_id from translations AS t left join translations AS o ON o.translation_card_id = t.translation_card_id AND o.translation_language != ? WHERE t.translation_language = ? AND o.translation_card_id != 0 "
									+
									")",
							new String[] { language.getLanguage(), language.getLanguage(), language.getLanguage() });
				}

			incrementProgress();

			// TODO: Remove Target Languages !!!
			// Clean-Up
			db.execSQL("DELETE FROM content where content._id IN (select content._id from content left join chapters on content_chapter_id = chapters._id where chapters._id is null)");
			incrementProgress();
			db.execSQL("DELETE FROM cards where cards._id IN (select cards._id from cards left join content on content_card_id = cards._id where content._id is null)");
			incrementProgress();
			db.execSQL("DELETE FROM translations WHERE translation_card_id IN (select translation_card_id from translations left join cards on translation_card_id = cards._id where cards._id is null)");
			incrementProgress();
			DatabaseFunctions.cleanOrphans(db);
			incrementProgress();

			db.close();

			Log.e(TAG, "FINISHED");
		}
		catch(final Exception e)
		{
			e.printStackTrace();
			displayError(e.getMessage());
		}
		finally
		{
			if(outfile != null && outfile.exists()) outfile.delete();
			dbh.close();
		}
		return null;
	}

	@Override
	public void doInForeground()
	{
		doInBackground();
		if(listener != null) listener.onFinish();
	}

	private JSONObject getQuery(final List<String> changeBook_ids) throws JSONException
	{
		final JSONArray jbooks = new JSONArray();
		for(final String book_id : changeBook_ids)
			jbooks.put(Long.parseLong(book_id));
		final JSONArray jlanguages = new JSONArray();
		for(final LanguageData language : languages)
		{
			if(language.getState() == LanguageData.State.ADD || language.getState() == LanguageData.State.INSTALLED)
				jlanguages.put(language.getLanguage());
		}
		final JSONObject jroot = new JSONObject();
		jroot.put("books", jbooks);
		jroot.put("languages", jlanguages);

		if(jroot.length() == 0) return null;
		return jroot;
	}

	@Override
	protected void onCancelled()
	{
		if(listener != null) listener.onFinish();
	}

	@Override
	protected void onPostExecute(final Void result)
	{
		if(listener != null) listener.onFinish();
		super.onPostExecute(result);
		if(isSuccessful())
			showToast(getContext().getString(R.string.sync_loader_book_download_finished));
	}

}
