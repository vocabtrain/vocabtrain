package org.devwork.vocabtrain;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;

public class FractalGenerator extends AsyncTask<Void, Bitmap, Void> {
	public static final String TAG = Constants.PACKAGE_NAME + ".FractalGenerator";

	public static final float DEFAULT_POSITION_REAL =  -1.0955354f;
	public static final float DEFAULT_POSITION_IMAG =  0.2408736f;
	public static final float DEFAULT_MAGNIFY = 32000.0f;
	
	private final static int color_size = 255*3;
	
    private enum Direction
    {
    	NONE,
    	LEFT,
    	UP,
    	RIGHT,
    	DOWN
    }
    
    public interface OnProgressListener
    {
    	void onProgress();
    }
    

    private class Complex
    {
    	private final double imag;
    	private final double real;
    	public Complex(double real, double imag)
    	{
    		this.real = real;
    		this.imag = imag;
    	}
    	public boolean equals(Complex obj)
    	{
    		return this.real == obj.real&& this.imag == obj.imag;
    	}
    }
    
    private final int [] colors = new int[color_size];
	
    float position_real =  DEFAULT_POSITION_REAL;
	float position_imag =  DEFAULT_POSITION_IMAG;
	
	float magnify = DEFAULT_MAGNIFY;
	private final int[][] indexmap;
    private final Bitmap image;
    
    private final View view;
    
    private final static int image_width = 64;
    private final static int image_height = 64;
    private final OnProgressListener listener;
    public FractalGenerator(View view, OnProgressListener listener)
    {	
    	this.view = view;
    	this.listener = listener;
    	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
		boolean invert_colors = prefs.getBoolean("invert_colors", false);
		float fpos = prefs.getFloat("fractal_x", 0.0f);
		if(fpos != 0.0) position_real = fpos;
		fpos = prefs.getFloat("fractal_y", 0.0f);
		if(fpos != 0.0) position_imag = fpos;
		fpos = prefs.getFloat("fractal_magnify", 0.0f);
		if(fpos != 0.0) magnify = fpos;
		
    	image = Bitmap.createBitmap(image_width,image_height, Bitmap.Config.ARGB_8888);
    	indexmap = new int[image.getWidth()][image.getHeight()];
    	
		float[] hsv = new float[3];
		
		hsv[2] = 1.0f;
		hsv[1] = 1.0f;
		for(int i = 0; i < colors.length; ++i)
		{
			hsv[0] =  i * 360.0f/ (float) colors.length;
			if(invert_colors)
				hsv[2] = 0.5f - i * 0.5f/ (float) colors.length;
			else 
				hsv[1] = 0.5f - i * 0.5f/ (float) colors.length;
			colors[i] = Color.HSVToColor(hsv);
		}
    }
    
    
    private Direction getDirectionFromRad(double rad)
    {
		if(rad < -Math.PI*3./4.)
			return Direction.DOWN;
		else if(rad < -Math.PI/4.)
			return Direction.RIGHT;
		else if(rad < Math.PI/4)
			return Direction.UP;
		else if(rad < Math.PI*3./4.)
			return Direction.LEFT;
		else return Direction.DOWN;
    }
    
    

	@Override
	protected Void doInBackground(Void... params) {
		Direction dir = Direction.NONE;
		LinkedList<Complex> positions = new LinkedList<Complex>();
		
		try
		{
		while(true)
		{
			final long time = System.currentTimeMillis();
			if(isCancelled()) return null;
			
			
			for(int px = 0; px < image.getWidth(); ++px)
				for(int py = 0; py < image.getHeight(); ++py)
				{
					if(isCancelled()) return null;
					int index = at( (px - image.getWidth()/2.0)/magnify + position_real, (py - image.getHeight()/2.0)/magnify + position_imag );
					image.setPixel(px, py, colors[index]);
					indexmap[px][py] = index;
				}
			
			
			float rad =  (float) Math.atan2(position_imag, position_real);
			
			
			if(dir == Direction.NONE)
				dir = getDirectionFromRad(rad);
			
			int up_black = 0;
			int right_black = 0;
			int left_black = 0;
			int down_black = 0;
			for(int i = 0; i < image.getWidth(); ++i)
			{
				if(indexmap[i][0] == colors.length -1) ++up_black;
				if(indexmap[i][image.getHeight()-1] == colors.length -1) ++down_black;
			}
			for(int i = 0; i < image.getHeight(); ++i)
			{
				if(indexmap[0][i] == colors.length -1) ++left_black;
				if(indexmap[image.getHeight()-1][i] == colors.length -1) ++right_black;
			}

			
			
			Complex currentPosition = new Complex(position_real, position_imag);
			int occurances = 0;
			
			Iterator<Complex> it = positions.iterator();
			while(it.hasNext())				
				if(it.next().equals(currentPosition) ) ++occurances;
			positions.add(currentPosition);	
			
			boolean looping = occurances > 1;
			float dx = 0;
			float dy = 0;
			
			if(looping)
			{
				dir = getDirectionFromRad(rad);
				switch(dir)
				{
					case DOWN: dy = 1;  break;
					case RIGHT: dx = 1; break;
					case UP: dy = -1;  break;
					case LEFT: dx = -1; break;
				}
			}
			else if(left_black == image.getHeight() && right_black == image.getHeight() && up_black == image.getWidth() && down_black == image.getWidth())
			{
				dx = -(float) FloatMath.cos(rad);
				dy = -(float) FloatMath.sin(rad);
			}
			else if(left_black == 0 && right_black == 0 && up_black ==0 && down_black == 0)
			{
				dx = (float) FloatMath.cos(rad);
				dy = (float) FloatMath.sin(rad);
			}
			else if(dir == Direction.DOWN && right_black != 0 && left_black != image.getHeight() && left_black != 0)
			{
				dx = -1; dy = 0;
			}
			else if(dir == Direction.UP && right_black != 0 && right_black != image.getHeight() && left_black != 0)
			{
				dx = +1; dy = 0;
			}
			else if(dir == Direction.LEFT && up_black != 0 && down_black != image.getWidth() && down_black != 0)
			{
				 dx = 0; dy = +1;
			}
			else if(dir == Direction.RIGHT && up_black != 0 && up_black != image.getWidth() && down_black != 0)
			{
				 dx = 0; dy = -1;
			}
			else if(up_black != image.getWidth() && right_black > down_black + left_black)
			{
				dir = Direction.RIGHT;
				dy =-1; dx = 1;
			}
			else // if(left_black != 0 && right_black != 0 && up_black !=0 && down_black != 0)
			{
				dir = getDirectionFromRad(rad);
				switch(dir)
				{
					case DOWN: 
						if(up_black + left_black < right_black)
						{
							dir = Direction.RIGHT;
							dx = +1; dy = -1;
							break;
						}
						if(down_black != 0 && down_black != image.getWidth() && down_black > up_black) dy = 1; 
						if(left_black != 0 && left_black != image.getHeight()) dx = -1; 
						break;
					case RIGHT: 
						if(down_black + left_black < up_black)
						{
							dir = Direction.UP;
							dx = -1; dy = -1;
							break;
						}
						
						if(right_black != 0  && right_black != image.getHeight()  && right_black > left_black) dx = 1; 
						if(up_black != 0 && up_black != image.getWidth() ) dy = -1; 
						break;
					case UP:
						if(down_black + right_black < left_black)
						{
							dir = Direction.LEFT;
							dx = +1; dy = +1;
							break;
						}
						if(up_black != 0  && up_black != image.getWidth()  && up_black > down_black) dy = -1; 
						if(right_black != 0 && right_black != image.getHeight()) dx = 1; 
						break;							
					case LEFT:
						if(up_black + right_black < down_black)
						{
							dir = Direction.DOWN;
							dx = -1; dy = 1;
							break;
						}
						if(left_black != 0 && left_black != image.getHeight() && left_black > right_black) dx = -1; 
						if(down_black != 0 && down_black != image.getWidth() ) dy = 1; 
						break;
				}
			}
			if(dx == 0 && dy == 0)
			{
				dir = getDirectionFromRad(rad);
				switch(dir)
				{
					case DOWN: dy = 1;  break;
					case RIGHT: dx = 1; break;
					case UP: dy = -1;  break;
					case LEFT: dx = -1; break;
				}
			}
			
			System.out.println("dx: " + dx + " dy: " + dy + "\t u:" + up_black + " r:" + right_black + " l:" + left_black + " d:" + down_black + "\t  position_real = " + position_real + "; position_imag = " + position_imag + "; dir: " + dir + " loop: " + looping);
			position_real += dx/magnify;
			position_imag += dy/magnify;
			
			publishProgress(image.copy(Config.ARGB_8888, false));
			
			if(positions.size() > 100) positions.removeFirst();
			
			final long timediff = System.currentTimeMillis() - time;
			if(timediff < 700)
				Thread.sleep(700 - timediff);
		}
		} catch (InterruptedException e) {
			return null;
		}
		
	}
	
	@Override
	protected void onCancelled() 
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
		Editor edit = prefs.edit();
		edit.putFloat("fractal_x", (float) position_real);
		edit.putFloat("fractal_y", (float) position_imag);
		edit.commit();
		Log.e("OnCan", "aoe");
	}
	
	
	@Override
	protected void onProgressUpdate(Bitmap... values)
	{
		BitmapDrawable drawable = new BitmapDrawable(values[0]);
    	view.setBackgroundDrawable(drawable);
		view.invalidate();
		if(listener != null) listener.onProgress();
	}
    
	
	private static int at(double x, double y)
	{
		final double cr = x;
		final double ci = y;
		
		
		double zr = 0;
		double zi = 0;
		
		int i;
		for(i = 0; i < color_size-1; ++i)
		{
			double tzr = zr*zr - zi*zi + cr;
			zi = 2*zr*zi + ci;
			zr = tzr;
			if(zr*zr + zi*zi > (color_size-1)*(color_size-1)) break;
		}
		return i;
	}


	
}
