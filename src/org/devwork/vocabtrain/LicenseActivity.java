package org.devwork.vocabtrain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LicenseActivity extends Activity {

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.license_activity);


        final CheckBox check = (CheckBox) this.findViewById(R.id.license_check);
        final Button cancel = (Button) this.findViewById(R.id.license_decline);
        final Button ok = (Button) this.findViewById(R.id.license_ok);
        final TextView text = (TextView) this.findViewById(R.id.license_text);
        final Button view = (Button) this.findViewById(R.id.license_view);

		try
		{
	        StringBuilder sb = new StringBuilder();
    		BufferedReader bf = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.license_short)));
    		while(true)
    		{
    			String s = bf.readLine();
    			if(s == null) break;
    			sb.append(s);
    			sb.append("\n");
    		}
    		bf.close();
            text.setText(sb.toString());
		}
		catch(IOException io)
		{
		}
        
        ok.setEnabled(false);
        
        check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				ok.setEnabled(isChecked);
			}
        });
        
        ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(check.isChecked())
				{
			        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LicenseActivity.this);
			        Editor edit = prefs.edit();
			        edit.putBoolean("license_accepted", true);
			        edit.commit();
			        setResult(RESULT_OK);
			        finish();
				}
			}
        });
        cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
        });
        view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.gnu.org/copyleft/gpl.html"));
				startActivity(intent);
			}
        });
        
    }
    
    
	
}
