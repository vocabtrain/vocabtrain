package org.devwork.vocabtrain;

import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

public class Playlist
{
	DatabaseHelper db;
	private boolean shuffleEnabled = false;
	private final int sequence;

	public Playlist(Context context, DatabaseHelper db)
	{
		this.db = db;
		SQLiteDatabase sdb = db.getRead();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		final String sorting = prefs.getString("sorting", null);
		sequence = DatabaseFunctions.getSequence(context);
		String sorting_sql = null;
		if(sorting != null)
		{
			if(sorting.equals("1"))
				shuffleEnabled = true;
			else if(sorting.equals("2"))
				sorting_sql = "filing_interval ASC";
			else if(sorting.equals("3")) sorting_sql = "filing_interval DESC";
		}
		else
			shuffleEnabled = true;

		Cursor cursor = sorting_sql == null ? sdb.query("selection", new String[] { "_id" }, null, null, null, null, null, null) : sdb.rawQuery("SELECT selection._id  FROM selection LEFT JOIN filing ON filing_card_id = selection_card_id AND filing_sequence = ? ORDER BY " + sorting_sql, new String[] { "" + sequence });
		assert cursor.getCount() > 0;
		shuffle = new int[cursor.getCount()];
		int i = 0;
		while(cursor.moveToNext())
		{
			shuffle[i++] = cursor.getInt(cursor.getColumnIndex("_id"));
		}
		cursor.close();
		if(shuffle.length == 0) return;
		update();
	}

	private final int[] shuffle;
	private int shufflePosition = 0;
	private int removedCards = 0;
	private int removedPosition = 0;
	private boolean shuffleOnLap = false;
	private Card card = null;

	public class Undo
	{
		private final CardFiling filing;
		private final long id;
		private final int oldShufflePosition;
		private final int oldShuffleValue;
		public final boolean forgotten;

		public long getCardId()
		{
			return id;
		}

		public CardFiling getCardFiling()
		{
			return filing;
		}

		private Undo(Card card)
		{
			forgotten = forgottenCard();
			filing = card.getFiling();
			if(filing != null) Log.e("INTERVAL UNDO",  filing.toString());
			id = card.getId();
			oldShufflePosition = shufflePosition;
			oldShuffleValue = shuffle[shufflePosition];
		}

		public void undo()
		{
			SQLiteDatabase d = db.getWritableDatabase();
			if(filing != null)
			{
				filing.updateDatabase(d);
				Log.e("INTERVAL ONUNDO", filing.toString());

			}
			if(shuffle[oldShufflePosition] == -1)
			{
				ContentValues c = new ContentValues();
				c.put("_id", oldShuffleValue);
				c.put("selection_card_id", id);
				c.put("selection_forgotten", forgotten);
				d.insert("selection", null, c);
				shuffle[oldShufflePosition] = oldShuffleValue;
				--removedCards;
			}
			else
			{
				ContentValues c = new ContentValues();
				c.put("selection_forgotten", forgotten);
				d.update("selection", c, "selection_card_id = ?", new String[] { "" + id });
				removedPosition = (removedPosition + shuffle.length - removedCards - 1) % (shuffle.length - removedCards);
			}
			d.close();
			shufflePosition = oldShufflePosition;

			update();
		}
	}

	public Undo createUndo()
	{
		if(card != null)
			return new Undo(card);
		else
			return null;
		/*
		 * { if(undo == null || undo.oldShufflePosition != shufflePosition) undo = new Undo(card); }
		 * 
		 * return new Undo(getCurrentCard()); private void createUndo() {
		 * 
		 * }
		 */

	}

	public boolean forgottenCard()
	{
		return forgottenCard(card.getId());
	}

	public boolean forgottenCard(long card_id)
	{
		boolean ret = false;
		SQLiteDatabase d = db.getRead();
		Cursor c = d.query("selection", new String[] { "selection_forgotten" }, "selection_card_id = ?", new String[] { "" + card_id }, null, null, null);
		if(c.getCount() > 0 && c.moveToFirst()) ret = c.getInt(0) == 1;
		c.close();
		return ret;
	}

	public boolean isCompleted()
	{
		return(shuffle.length == removedCards || shuffle.length == 0);
	}

	public void shuffleOnLap(boolean shuffle)
	{
		shuffleOnLap = shuffle;
	}

	public void update()
	{
		card = db.getCard(shuffle[shufflePosition]);
		if(card == null && !isCompleted()) 
		{
			discard();
			update();
		}
	}

	public int getPosition()
	{
		return removedPosition;
	}

	public int getCount()
	{
		return shuffle.length - removedCards;
	}

	public void shuffle()
	{
		if(isCompleted()) return;
		if(!shuffleEnabled) return;
		Random random = new Random();
		for(int i = 1; i < shuffle.length; ++i)
		{
			int j = random.nextInt(i + 1);
			int tmp = shuffle[i];
			shuffle[i] = shuffle[j];
			shuffle[j] = tmp;
		}
		shufflePosition = 0;
		update();
	}

	public void discard()
	{
		if(isCompleted()) return;
		SQLiteDatabase d = db.getWritableDatabase();
		d.delete("selection", "_id = ?", new String[] { "" + shuffle[shufflePosition] });

		// db.execSQL("DELETE FROM `selection` WHERE `selection_card_id` = '" + card.getId() + "';");
		d.close();
		shuffle[shufflePosition] = -1;
		++removedCards;
		if(isCompleted()) return;
		next();
		removedPosition = (removedPosition + shuffle.length - removedCards - 1) % (shuffle.length - removedCards);
	}

	public void previous()
	{
		if(isCompleted()) return;
		do
		{
			shufflePosition = (shufflePosition + shuffle.length - 1) % shuffle.length;
		}
		while(shuffle[shufflePosition] == -1);
		update();
		removedPosition = (removedPosition + shuffle.length - removedCards - 1) % (shuffle.length - removedCards);
	}

	public void next()
	{
		if(isCompleted()) return;
		final int prevPosition = shufflePosition;
		do
		{
			shufflePosition = (shufflePosition + 1) % shuffle.length;
		}
		while(shuffle[shufflePosition] == -1);
		if(shuffleOnLap && prevPosition > shufflePosition) shuffle();
		update();
		removedPosition = (removedPosition + 1) % (shuffle.length - removedCards);
	}

	public Card getCurrentCard()
	{
		if(isCompleted()) return null;
		return card;
	}

	public void removeCard()
	{
		SQLiteDatabase d = db.getWritableDatabase();
		d.delete("filing", "filing_card_id = ? AND filing_sequence = ?", new String[] { "" + getCurrentCard().getId(), "" + sequence });
		d.close();
		discard();
	}

}
