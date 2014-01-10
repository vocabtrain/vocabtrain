package org.devwork.vocabtrain;

import org.devwork.vocabtrain.FractalGenerator.OnProgressListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FractalPreference extends DialogPreference implements OnProgressListener 
{
	private FractalGenerator fg = null;

	public FractalPreference(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.fractal_preference);
		setDialogTitle(context.getString(R.string.pref_fractal));
	}
	@Override
	protected View onCreateDialogView()
	{
		View v = super.onCreateDialogView();
		enable = (CheckBox) v.findViewById(R.id.fractal_enable);
		preview = (CheckBox) v.findViewById(R.id.fractal_preview);
		magnify = (EditText) v.findViewById(R.id.fractal_magnify);
		position_real = (EditText) v.findViewById(R.id.fractal_x);
		position_imag = (EditText) v.findViewById(R.id.fractal_y);
		image = (ImageView) v.findViewById(R.id.fractal_image);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		enable.setChecked(prefs.getBoolean("use_fractals", false));
		
		float fpos = prefs.getFloat("fractal_x", 0.0f);
		orig_position_real = fpos == 0.0f ? FractalGenerator.DEFAULT_POSITION_REAL : fpos;
		fpos = prefs.getFloat("fractal_y", 0.0f);
		orig_position_imag = fpos == 0.0f ? FractalGenerator.DEFAULT_POSITION_IMAG : fpos;
		fpos = prefs.getFloat("fractal_magnify", 0.0f);
		orig_magnify = fpos == 0.0f ? FractalGenerator.DEFAULT_MAGNIFY : fpos;
		
		
		
		
		
		position_real.setText("" + orig_position_real);
		position_imag.setText("" + orig_position_imag);
		magnify.setText("" + orig_magnify);
				
		
		preview.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
				{
					if(fg != null) fg.cancel(true);
					fg = new FractalGenerator(image, FractalPreference.this);
					fg.position_imag = 	Float.parseFloat(position_imag.getText().toString());			
					fg.position_real = 	Float.parseFloat(position_real.getText().toString());			
					fg.magnify = 	Float.parseFloat(magnify.getText().toString());
					fg.execute((Void[]) null);
				}
				else
				{
					if(fg != null)
					{
						fg.cancel(true);
						fg = null;
					}
				}
			}
			
		});
		
		
		return v;
	}
	private ImageView image;
	private CheckBox enable;
	private CheckBox preview;
	private EditText magnify;
	private EditText position_real;
	private EditText position_imag;
	
	private  float orig_magnify;
	private  float orig_position_real;
	private  float orig_position_imag;
	
	
	@Override
	protected void onDialogClosed (boolean positiveResult)
	{
		if(fg != null)
		{
			fg.cancel(true);
			fg = null;
		}
		if(!positiveResult)
		{
			SharedPreferences prefs = getSharedPreferences();
			Editor edit = prefs.edit();
			edit.putFloat("fractal_x", orig_position_real);
			edit.putFloat("fractal_y", orig_position_imag);
			edit.putFloat("fractal_magnify", orig_magnify);
			edit.commit();
		}
		else try
		{
			SharedPreferences prefs = getSharedPreferences();
			Editor edit = prefs.edit();
			edit.putBoolean("use_fractals", enable.isChecked());
			edit.putFloat("fractal_x", Float.parseFloat(position_real.getText().toString()));
			edit.putFloat("fractal_y", Float.parseFloat(position_imag.getText().toString()));
			edit.putFloat("fractal_magnify", Float.parseFloat(magnify.getText().toString()));
			edit.commit();
		}
		catch(NumberFormatException f)
		{
		}
	}
	@Override
	public void onProgress() {
		position_imag.setText("" + fg.position_imag);
		position_real.setText("" + fg.position_real);
		magnify.setText("" + fg.magnify);
	}
	
}
