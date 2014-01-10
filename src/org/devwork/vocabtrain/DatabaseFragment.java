package org.devwork.vocabtrain;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class DatabaseFragment extends Fragment {

	private DatabaseHelper database = null;
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(database == null) database = new DatabaseHelper(getActivity());
	}
	@Override
	public void onPause()
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
		if(database == null) database = new DatabaseHelper(getActivity());
		return database;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(database == null) database = new DatabaseHelper(getActivity());
    }
	
	
}
