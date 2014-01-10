package org.devwork.vocabtrain;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class DatabaseActivity extends FragmentActivity {

	private DatabaseHelper database = null;
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if(database == null) database = new DatabaseHelper(this);
	}
	@Override
	protected void onPause()
	{
		super.onPause();
		if(database != null) 
		{
			database.close();
			database = null;
		}
	}
	
	protected DatabaseHelper getDatabaseHelper()
	{
		if(database == null) database = new DatabaseHelper(this);
		return database;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(database == null) database = new DatabaseHelper(this);
    }
	
	
}
