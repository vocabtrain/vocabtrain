package org.devwork.vocabtrain.sync;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.devwork.vocabtrain.Constants;
import org.devwork.vocabtrain.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class BookData {

	public enum State {
		ADD(R.drawable.sync_add),
		INSTALLED(R.drawable.sync_installed),
		REMOVE(R.drawable.sync_remove),
		NONE(R.drawable.sync_none),
		DONTUPDATE(R.drawable.sync_dontupdate),
		UPDATE(R.drawable.sync_update);
		
		private final int drawable_res;
		private State(int drawable_res)
		{
			this.drawable_res = drawable_res;
		}
		public int getDrawableId()
		{
			return drawable_res;
		}
	}
	
	
	private final long id;
	private final long timestamp; 
	private final long old_timestamp;
	private final String name;
	private final String language;
	private final String[] translation_languages;
	//private boolean selected;
	private State state;

	public BookData(JSONObject o, SQLiteDatabase db) throws JSONException, ParseException
	{
		timestamp = Constants.jsonDateFormat.parse(o.getString("book_timestamp")).getTime();
		id = o.getLong("_id");
		name = o.getString("book_name");
		language = o.getString("book_language");
		final JSONArray jtranslation_languages = o.getJSONArray("book_translations");
		translation_languages = new String[jtranslation_languages.length()];
		for(int i = 0; i < jtranslation_languages.length(); ++i)
			translation_languages[i] = jtranslation_languages.getString(i);
		Arrays.sort(translation_languages);
		final Cursor c = db.query("books", new String[] { "book_timestamp" } , "_id = ?", new String[] { "" + id }, null, null, null);
		if( c.getCount() == 1 && c.moveToFirst())
		{
			long tryoldtimestamp;
			try
			{
				tryoldtimestamp = Constants.sqliteDateFormat.parse(c.getString(0).replaceAll("\\.([0-9]{3})([0-9]{3}) UTC$", "\\.$1 +0000")).getTime();
			}
			catch(NullPointerException e)
			{
				tryoldtimestamp = c.getLong(0);
			}
			catch(ParseException e)
			{
				tryoldtimestamp = c.getLong(0);
			}
			old_timestamp = tryoldtimestamp;
			Log.i("timestamp", ""+ old_timestamp);
			//old_timestamp = c.getLong(0);
			state = isUpdateable() ? State.UPDATE : State.INSTALLED;
		}
		else 
		{
			old_timestamp = -1;
			state = State.NONE;
		}
		c.close();

	}

	public String[] getTranslationLanguages()
	{
		return translation_languages;
	}
	
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getTimestamp() {
		return timestamp;
	}
	@Override
	public String toString() {
		return name;
	}

	public String getLanguage() {
		return language;
	}

	public long getOldTimestamp() {
		return old_timestamp;
	}
	public boolean isInstalled()
	{
		return old_timestamp != -1;
	}

	public boolean isUpdateable()
	{
		return old_timestamp+120 < timestamp;
	}
	public void switchState() {
		switch(state)
		{
			// not yet installed -- States: ADD - NONE
			case ADD: 
				state = State.NONE;
				break;
			case NONE:
				state = State.ADD;
				break;
			
			// already installed, no update available -- States: installed, remove
			case INSTALLED:
				state = State.REMOVE;
				break;
				
			// already installed, update available -- States: don't update, update, remove
			case UPDATE:
				state = State.DONTUPDATE;
				break;
			case DONTUPDATE:
				state = State.REMOVE;
				break;
			// remove -> (update, installed)
			case REMOVE:
				state = isUpdateable() ? State.UPDATE : State.INSTALLED;
				break;
		}
	}
	public State getState() {
		return state;
	}
}
