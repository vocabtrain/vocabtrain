package org.devwork.vocabtrain;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class CardViewActivity extends FragmentActivity {
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.cardedit_activity);
        final Bundle extras = getIntent().getExtras();
        final long card_id = extras.getLong("card_id");
        FragmentManager manager = getSupportFragmentManager();
        if(manager.findFragmentByTag(CardViewDialog.TAG) == null)
        {
	        FragmentTransaction ft = manager.beginTransaction();
	        Fragment fragment = CardViewDialog.createInstance(card_id);
	        ft.add(R.id.cardedit_activity_layout, fragment, CardViewDialog.TAG);
	        ft.commit();
        }
    }
	
    
    
}
