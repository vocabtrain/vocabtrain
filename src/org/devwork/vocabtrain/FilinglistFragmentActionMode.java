package org.devwork.vocabtrain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.os.Build;
import android.util.Log;

public class FilinglistFragmentActionMode {
	public static final String TAG = Constants.PACKAGE_NAME + ".BooklistFragmentActionMode";
	private Object callback;
	

	public FilinglistFragmentActionMode(FilinglistFragment fragment)
	{
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			callback = null;
			return;
		}
		try
		{
			final Class<?> callBackClass = Class.forName("org.devwork.vocabtrain.FilinglistFragmentActionModeCallback");
			final Class<?>[] args = new Class[] { FilinglistFragment.class };
			final Constructor<?> cons = callBackClass.getConstructor(args);
			callback = cons.newInstance(fragment);
		}
		catch(Throwable e)
		{
			Log.e(TAG, e.toString());
		}
		
	}
	
    public void start(int filings_count)
    {
    	if(callback != null)
		try
		{
			final Class<?> callBackClass = Class.forName("org.devwork.vocabtrain.FilinglistFragmentActionModeCallback");
			final Class<?>[] args = new Class[] { int.class };
			final Method start = callBackClass.getMethod("start", args);
			start.invoke(callback, filings_count);
		}
		catch(Throwable e)
		{
			Log.e(TAG, e.toString());
		}
    	
    	
    	/*
    	if(callback != null)
    	((BooklistFragmentActionModeCallback)callback).start(chapter_count);
    	*/
    }
	
}
