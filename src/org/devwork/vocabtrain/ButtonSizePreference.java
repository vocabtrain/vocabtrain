package org.devwork.vocabtrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ButtonSizePreference extends DialogPreference
{

	static String delimeter = ";";
	boolean hasSecondQuestion;
	public static final int MAX_BUTTONSIZE = 800;
	public static final int MIN_BUTTONSIZE = 100;

	public ButtonSizePreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setDialogLayoutResource(R.layout.buttonsize_preference);
		setDialogTitle(context.getString(R.string.pref_buttonsize));
		defaultValue = attrs.getAttributeIntValue(Constants.ANDROID_NAMESPACE, "defaultValue", defaultValue);
	}

	private int getValue() throws NumberFormatException
	{

		return Integer.parseInt(textedit.getText().toString());
	}

	private void addValue(int add)
	{
		try
		{
			int size = getValue() + add;
			if(size >= MIN_BUTTONSIZE && size <= MAX_BUTTONSIZE) setValue(size);
		}
		catch(NumberFormatException e)
		{
		}
	}

	private int initial_height = 0;

	private void updateTextView(int size)
	{
		if(initial_height == 0) initial_height = buttonView.getHeight();
		buttonView.setHeight(initial_height * size / 100);
	}

	private void setValue(int value)
	{
		seekbar.setProgress(100 * (value - MIN_BUTTONSIZE) / (MAX_BUTTONSIZE - MIN_BUTTONSIZE));
		textedit.setText("" + value);
		updateTextView(value);
	}

	private Integer defaultValue = 100;

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		if(a != null && a.hasValue(index) && a.getString(index) != null)
		{
			try
			{
				int val = Integer.parseInt(a.getString(index));
				if(val > 0 && val < MAX_BUTTONSIZE) defaultValue = val;
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
		buttonplus = (Button) v.findViewById(R.id.buttonsize_button_plus);
		buttonminus = (Button) v.findViewById(R.id.buttonsize_button_minus);
		buttonView = (Button) v.findViewById(R.id.buttonsize_view);
		textedit = (EditText) v.findViewById(R.id.buttonsize_edit);
		seekbar = (SeekBar) v.findViewById(R.id.buttonsize_seekbar);

		buttonplus.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				addValue(1);
			}
		});
		buttonminus.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				addValue(-1);
			}
		});
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if(progress >= 0 && progress <= 100) setValue(progress * (MAX_BUTTONSIZE - MIN_BUTTONSIZE) / 100 + MIN_BUTTONSIZE);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub

			}

		});
		textedit.setOnKeyListener(new OnKeyListener()
		{

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if(event.getAction() == KeyEvent.ACTION_DOWN)
				{
					try
					{
						int value = Integer.parseInt(textedit.getText().toString());
						if(value >= MIN_BUTTONSIZE && value <= MAX_BUTTONSIZE) updateTextView(value);
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
				setValue((i >= MIN_BUTTONSIZE && i <= MAX_BUTTONSIZE) ? i : defaultValue);
			}
			catch(NumberFormatException f)
			{
				setValue(defaultValue);
			}
		}
		return v;
	}

	private EditText textedit;
	private Button buttonView;
	private Button buttonminus;
	private Button buttonplus;
	private SeekBar seekbar;

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if(!positiveResult) return;

		try
		{
			int value = getValue();
			if(value >= MIN_BUTTONSIZE && value <= MAX_BUTTONSIZE)
			{
				SharedPreferences prefs = getSharedPreferences();
				Editor edit = prefs.edit();
				edit.putInt(this.getKey(), Integer.parseInt(textedit.getText().toString()));
				edit.commit();
			}
		}
		catch(NumberFormatException f)
		{
		}
	}
}
