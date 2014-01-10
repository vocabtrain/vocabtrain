package org.devwork.vocabtrain;

import android.util.Log;

public class Sequence {
	public static final String TAG = Constants.PACKAGE_NAME + ".Sequence";

	public static final String[] data = { "script", "speech", "vernicular" };

	public final static int DEFAULT_SEQUENCE = 12;

	public static int getStringId(byte type)
	{
		switch(type)
		{
		case SCRIPT:
			return R.string.script;
		case SPEECH:
			return R.string.speech;
		case VERNICULAR:
			return R.string.vernicular;
		}
		return R.string.vernicular;
	}


	public static final byte SCRIPT = 0;
	public static final byte SPEECH = 1;
	public static final byte VERNICULAR = 2;
	
	public enum Order
	{
		SCRIPT(0), SPEECH(1), VERNICULAR(2);
		private Order(int i)
		{
			value = i;
		}
		public String toString() {
			return data[value];
		}
		private final int value;
	}
	
	public static byte[] decodeSequence(int value)
	{
		final byte[] array = new byte[data.length];
		for(int i = data.length-1; i > -1; --i, value /= 10)
		{
			array[i] = (byte) ( (value % 10) % data.length);
		}
		Log.e(TAG, "decode: " + value + " " + array[0] + "," + array[1] + "," + array[2]);
		//return new byte[] { 0, 1, 2};
		return array;
	}
	
	public static int powerOfTen(int power)
	{
		int value = 1;
		for(int i = 0; i < power; ++i)
			value *= 10;
		return value;
	}
	
	public static int encodeSequence(byte[] array)
	{
		int value = 0;
		for(int i = 0; i < array.length; ++i)
		{
			value += array[i] * powerOfTen(array.length-i-1);
		}
		Log.e(TAG, "encode: " + value + " " + array[0] + "," + array[1] + "," + array[2]);
		return value;
	}
	
	
}
