package org.devwork.vocabtrain.sync;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserData
{
	private final long timestamp;
	private final long oldTimestamp;
	private final String authToken;

	public UserData(String authToken, long timestamp, long oldTimestamp)
	{
		this.authToken = authToken;
		this.timestamp = timestamp;
		this.oldTimestamp = oldTimestamp;
	}

	public UserData(JSONObject o, SQLiteDatabase db, String authToken) throws JSONException
	{
		this.timestamp = o.getLong("timestamp");
		this.authToken = authToken;

		final Cursor c = db.rawQuery("SELECT MAX(filing_timestamp) FROM filing_data", null);
		oldTimestamp = (c.getCount() == 1 && c.moveToFirst()) ? c.getLong(0) : 0;
		c.close();
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public long getOldTimestamp()
	{
		return oldTimestamp;
	}

	public String getAuthToken()
	{
		return authToken;
	}

}
