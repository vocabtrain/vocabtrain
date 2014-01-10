package org.devwork.vocabtrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.TextView;

public class TextViewShrinker implements TextWatcher {

	private final int screenWidth;
	private final TextView tv;
	private final Context context;
	private final int maximum_fontsize;
	private final int minimum_fontsize;
	public TextViewShrinker(Context context, TextView tv, int width)
	{
		this.screenWidth = width;
		this.context = context;
		this.tv = tv;
		tv.addTextChangedListener(this);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		maximum_fontsize = prefs.getInt("maximum_fontsize", Constants.DEFAULT_PREFERRED_FONTSIZE);
		minimum_fontsize = prefs.getInt("minimum_fontsize", Constants.DEFAULT_MINIMUM_FONTSIZE);
	}
	
	@Override
	public void afterTextChanged(Editable e) {
		String s = tv.getText().toString();
		float textsize = maximum_fontsize; //initial_textsize;
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize); 
        float currentWidth = tv.getPaint().measureText(s);
        float phoneDensity = context.getResources().getDisplayMetrics().density;
        final int width = tv.getWidth() == 0 ? screenWidth : tv.getWidth();

        while(currentWidth > (width * phoneDensity)) 
        {         
        	textsize -= 1f;
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize); 
            currentWidth = tv.getPaint().measureText(s);
            if(textsize < minimum_fontsize) break;
        }
	}
	
	public void resize(float scale)
	{
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() + scale ); 
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
	
	
}
