package org.devwork.vocabtrain.sync;

import org.devwork.vocabtrain.R;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LanguageData {

	public enum State {
		ADD(R.drawable.sync_add),
		INSTALLED(R.drawable.sync_installed),
		REMOVE(R.drawable.sync_remove),
		NONE(R.drawable.sync_none);
		
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
	
	
	
	private final String name;
	private State state;
	
	public LanguageData(String language, SQLiteDatabase db) throws JSONException
	{
		name = language; //o.getString("translation_language");
		
		Cursor c = db.rawQuery("SELECT COUNT(*) FROM translations WHERE translation_language = ?", new String[] { name });
		if(c.moveToFirst() && c.getCount() != 0)
			state = (c.getLong(0) != 0) ? State.INSTALLED : State.NONE;
		else state = State.NONE;
		c.close();

	}
	
	
	public String getLanguage() {
		return name;
	}
	@Override
	public String toString() {
		return name;
	}
	
	public void switchState() {
		switch(state)
		{
			case ADD: 
				state = State.NONE;
				break;
			case NONE:
				state = State.ADD;
				break;
			case INSTALLED:
				state = State.REMOVE;
				break;
			case REMOVE:
				state = State.INSTALLED;
				break;
		}
	}
	public State getState() {
		return state;
	}
}
