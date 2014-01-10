package org.devwork.vocabtrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FontSizePreference extends DialogPreference 
{
	
	static String delimeter = ";";
	boolean hasSecondQuestion;
	public static final int MAX_FONTSIZE = 100;
	
	
	public FontSizePreference(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.fontsize_preference);
		setDialogTitle(context.getString(R.string.pref_fontsize));
		defaultValue = attrs.getAttributeIntValue(Constants.ANDROID_NAMESPACE, "defaultValue", defaultValue);
	}

	private void addValue(int add)
	{
		try
		{
			int size = Integer.parseInt(textedit.getText().toString()) + add;
			textedit.setText("" + size);
			updateTextView(size);
		}
		catch(NumberFormatException e)
		{}
	}
	private void updateTextView(int size)
	{
		textview.setTextSize(size);
	}
	private void setValue(int value)
	{
		textedit.setText("" + value);
		updateTextView(value);
	}
	
	private Integer defaultValue = 10;
	
	@Override
	protected Object onGetDefaultValue (TypedArray a, int index) 
	{
		if(a != null && a.hasValue(index) && a.getString(index) != null)
		{
			try
			{
				int val = Integer.parseInt(a.getString(index));
				if(val > 0 && val < MAX_FONTSIZE) defaultValue = val;
			}
        	catch(NumberFormatException e)
        	{
        	}
		}
		return defaultValue;
	}
	
	
	@Override
	protected View onCreateDialogView()
	{
		View v = super.onCreateDialogView();
		buttonplus = (Button) v.findViewById(R.id.fontsize_button_plus);
		buttonminus = (Button) v.findViewById(R.id.fontsize_button_minus);
		textview = (TextView) v.findViewById(R.id.fontsize_view);
		textedit = (EditText) v.findViewById(R.id.fontsize_edit);
		
		if(Build.BOARD.equals("zoom2") && Build.BRAND.equals("nook") && Build.DEVICE.equals("zoom2")) // nook touch renders default white text on white
			// background
		{
			textview.setTextColor(Color.BLACK);
		}
		
		buttonplus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				addValue(1);
			}
		});
		buttonminus.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				addValue(-1);
			}
		});
		textedit.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) 
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                	try
                	{
                		int value = Integer.parseInt(textedit.getText().toString());
                		if(value > 0 && value < MAX_FONTSIZE)
                			textview.setTextSize(value);
                	}
                	catch(NumberFormatException e)
                	{
                	}
                }
                return false;
            }
		});
		
		SharedPreferences prefs = getSharedPreferences();
		try
    	{
			int i = prefs.getInt(getKey(), defaultValue);
			setValue(i > 0 ? i : defaultValue);
    	}
		catch(ClassCastException e)
		{
			try
			{
				int i = Integer.parseInt(prefs.getString(this.getKey(), "" + defaultValue));
				setValue( (i > 0  && i < MAX_FONTSIZE) ? i : defaultValue);
			}
			catch(NumberFormatException f)
			{
				setValue(defaultValue);
			}
		}
		return v;
	}
	private EditText textedit;
	private TextView textview;
	private Button buttonminus;
	private Button buttonplus;
	
	@Override
	protected void onDialogClosed (boolean positiveResult)
	{
		if(!positiveResult) return;
		try
		{
			SharedPreferences prefs = getSharedPreferences();
			Editor edit = prefs.edit();
			edit.putInt(this.getKey(), Integer.parseInt(textedit.getText().toString()));
			edit.commit();
		}
		catch(NumberFormatException f)
		{
		}
	}
}
