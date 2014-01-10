package org.devwork.vocabtrain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseInitialisizer {
	private final Context context;
	private boolean onRecreate = false; // stop infinite recreation-loop

	
	private DatabaseInitialisizer(Context context)
	{
		this.context = context;
	}
	
	private static DatabaseInitialisizer init = null;
	public static void initialize(Context context)
	{
		if(init == null)
		{
			init = new DatabaseInitialisizer(context);
			init.createDatabase();
		}
	}
	
	
    private void recreateDatabase()
    {
    	if(onRecreate)
    	{
    		//OOPS! TODO something went wrong!
    		return;
    	}
    	onRecreate = true;

    	DatabaseHelper dbh = new DatabaseHelper(context);
    	
    	File dbfile = new File(dbh.getFilename());
    	File tmpfile = DatabaseFunctions.createTempFile(dbfile.getParentFile());

    	dbfile.renameTo(tmpfile);
		
		createDatabase();
		
    	// TODO: Veralterter Code !!!
    	SQLiteDatabase dbw = dbh.getWritableDatabase();
    	
		SQLiteDatabase dbr = SQLiteDatabase.openDatabase(tmpfile.toString(), null, SQLiteDatabase.OPEN_READONLY);
		Cursor c = dbr.rawQuery("SELECT * FROM `filing`", null);
		while(c.moveToNext())
		{
    		ContentValues v = new ContentValues();
    		v.put("filing_card_id", c.getLong(c.getColumnIndex("filing_card_id")));
    		v.put("filing_rank", c.getLong(c.getColumnIndex("filing_rank")));
    		dbw.insert("filing", null, v);
		}
		c.close();
		c = dbr.rawQuery("SELECT * FROM `selection`", null);
		while(c.moveToNext())
		{
    		ContentValues v = new ContentValues();
    		v.put("selection_card_id", c.getLong(c.getColumnIndex("selection_card_id")));
    		dbw.insert("selection", null, v);
		}
		c.close();
		dbw.close();
		dbr.close();
		dbh.close();
		tmpfile.delete();
    }

	private void createDatabase()
	{
				DatabaseHelper dbh = new DatabaseHelper(context);
	    	if(!DatabaseFunctions.hasDatabase(dbh.getFilename()))
	    	{
	    		try 
	    		{
		    		DatabaseFunctions.copyFile(context.getAssets().open(Constants.DATABASE_NAME),  new FileOutputStream(dbh.getFilename()));
				}
	    		catch (IOException e) {
					// TODO Auto-generated catch block
	    			Log.e("ERROR", e.toString());
				}
	    	}
	    	SQLiteDatabase db = dbh.getWritableDatabase();
	    	DatabaseFunctions.createTables(db);
	    	
	    	
	    	Cursor c = db.query("metadata", new String[] { "metadata_value"}, "metadata_key = ?", new String[] { "version" }, null,null,null);
	    	boolean renew = false;
	    	if(c.getCount() != 0) c.moveToFirst();
	    	if(c.getCount() == 0 || c.getInt(0) < Constants.DATABASE_VERSION) renew = true;
	    	c.close();
	    	db.close();
	    	dbh.close();
	    	if(renew)
	    		recreateDatabase();
	    }
	    

    
	
}
