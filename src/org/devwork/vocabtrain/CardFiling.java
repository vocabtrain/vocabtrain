package org.devwork.vocabtrain;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;

class CardFiling
{
	public static final int VALUE_HIGH = 5;
	public static final int VALUE_MEDIUM = 4;
	public static final int VALUE_LOW = 3;
	public static enum Priority
	{
		HIGH(VALUE_HIGH) {
			@Override
			public String toString() { return "H"; }
		},
		MEDIUM(VALUE_MEDIUM) {
			@Override
			public String toString() { return "M"; }
		},
		LOW(VALUE_LOW) {
			@Override
			public String toString() { return "L"; }
		};
		
		private int value;
		private Priority(int value)
		{
			this.value = value;
		}
		
		public int getStringId()
		{
			switch(value)
			{
			case VALUE_HIGH: return R.string.priority_high;
			case VALUE_MEDIUM: return R.string.priority_medium;
			case VALUE_LOW: return R.string.priority_low;
			default: return R.string.priority_medium;
			}
		}
		public static Priority get(String str, Context context)
		{
			Resources r = context.getResources();
			if(str.equals(r.getString(R.string.priority_high))) return HIGH;
			else if(str.equals(r.getString(R.string.priority_medium))) return MEDIUM;
			else if(str.equals(r.getString(R.string.priority_low))) return LOW;
			else return MEDIUM;
			
		}
		
		public static Priority get(int value)
		{
			switch(value)
			{
				case VALUE_HIGH: return HIGH;
				case VALUE_MEDIUM: return MEDIUM;
				case VALUE_LOW: return LOW;
				default: return MEDIUM;
			}
		}
		public int get()
		{
			return value;
		}
		
	}
	
	
	private final long card_id;
	private final long filing_id;
	final int rank;
	final int grades;
	final int count;
	final long session;
	final long interval;
	final float difficulty;
	final Priority priority;
	final int sequence;
	
	CardFiling(long card_id, int sequence, SQLiteDatabase db) throws RowNotFoundException
	{
		this.sequence = sequence;
		this.card_id = card_id;
		Cursor c = db.query("filing", new String[] { "_id", "filing_rank", "filing_grades", "filing_count", "filing_session", "filing_interval", "filing_difficulty", "filing_priority" }, "filing_card_id = ? AND filing_sequence = ?", new String[] { "" + card_id, "" + sequence }, null, null, null);
		if(c.getCount() == 0 || !c.moveToFirst()) 
		{
			c.close();
			throw new RowNotFoundException(R.string.filing_card_id_not_found, card_id);

		}
		filing_id = c.getLong(c.getColumnIndex("_id"));
		rank = c.getInt(c.getColumnIndex("filing_rank"));
		grades = c.getInt(c.getColumnIndex("filing_grades"));
		count = c.getInt(c.getColumnIndex("filing_count"));
		session = c.getLong(c.getColumnIndex("filing_session"));
		interval = c.getLong(c.getColumnIndex("filing_interval"));
		difficulty = c.getFloat(c.getColumnIndex("filing_difficulty"));
		priority = Priority.get(c.getInt(c.getColumnIndex("filing_priority")));
		c.close();

	}

	public CardFiling(CardFiling o) {
		this.card_id = o.card_id;
		filing_id = o.filing_id;
		rank = o.rank;
		grades = o.grades;
		count = o.count;
		session =o.session;
		interval =o.interval;
		difficulty = o.difficulty;
		priority =o.priority;
		sequence = o.sequence;
	}

	@Override
	public String toString()
	{
		return "(card_id = " + card_id + ", filing_id = " + filing_id + ", rank = " + rank + ", grades = " + grades + ", count = " + count + 
				", session = " + session + ", interval = " + interval + ", difficulty = " + difficulty + ", priority = " + priority +
				", sequence = " + sequence + ")";
	}

	public CardFiling(Parcel source) {
		card_id = source.readLong();
		filing_id = source.readLong();
		rank = source.readInt();
		grades = source.readInt();
		count = source.readInt();
		session = source.readLong();
		interval = source.readLong();
		difficulty = source.readFloat();
		priority = Priority.get(source.readInt());
		sequence = source.readInt();
	}

	public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(card_id);
			dest.writeLong(filing_id);
			dest.writeInt(rank);
			dest.writeInt(grades);
			dest.writeInt(count);
			dest.writeLong(session);
			dest.writeLong(interval);
			dest.writeFloat(difficulty);
			dest.writeInt(priority.get());
			dest.writeInt(sequence);
	}
	
	public void updateDatabase(SQLiteDatabase db) {
		final ContentValues c = new ContentValues();
		c.put("_id", filing_id);
		c.put("filing_card_id", card_id);
		c.put("filing_rank", rank);
		c.put("filing_grades", grades);
		c.put("filing_count", count);
		c.put("filing_session", session);
		c.put("filing_interval", interval);
		c.put("filing_difficulty", difficulty);
		c.put("filing_priority", priority.get());
		c.put("filing_sequence", sequence);
		db.replace("filing", null, c);
		/*if(db.update("filing", c, "_id = ?", new String[] { "" + filing_id }) == 0)
		{
			c.put("_id", filing_id);
			c.put("filing_card_id", card_id);
			db.insert("filing", null, c);
		}
		*/
	}
	
	
}


