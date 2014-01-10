package org.devwork.vocabtrain;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class StringUtils {
	public static String join(final String[] string)
	{
		return join(string, ",");
	}
	public static String join(final String[] string, final String delimeter)
	{
		if(string.length == 0) return null;
		StringBuilder sb = new StringBuilder(string[0]);
		for(int i = 1; i < string.length; ++i)
		{
			sb.append(delimeter).append(string[i]);
		}
		return sb.toString();
	}
	public static String join(final int[] array, final String delimeter)
	{
		if(array.length == 0) return null;
		StringBuilder sb = new StringBuilder("" + array[0]);
		for(int i = 1; i < array.length; ++i)
		{
			sb.append(delimeter).append("" + array[i]);
		}
		return sb.toString();
	}
	public static int[] splitInts(final String string, final String delimeter)
	{
		try
		{
		String[] array = string.split(delimeter);
		int[] iarray = new int[array.length];
		for(int i = 0; i < array.length; ++i)
		{
			iarray[i] = Integer.parseInt(array[i]);
		}
		return iarray;
		}
		catch(NumberFormatException e)
		{
			return null;
		}
	}
	
	public static String generateQuestionTokens(final int length)
	{
		if(length == 0) return null;
		StringBuilder sb = new StringBuilder(" IN ( ?");
		for(int i = 1; i < length; ++i)
			sb.append(",?");
		sb.append(")");
		return sb.toString();
	}
	public static <T> String[] createArray(Collection<T> c)
	{
		String[] a = new String[c.size()];
		Iterator<T> it = c.iterator();
		int i = 0;
		while(it.hasNext())
			a[i++] = it.next().toString();
		return a;
	}
	
    private static final char[] hexChar = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

	//from http://www.xinotes.org/notes/note/812/
    public static String unicodeEscape(String s) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < s.length(); i++) {
    	    char c = s.charAt(i);
    	    if ((c >> 7) > 0) {
    		sb.append("\\u");
    		sb.append(hexChar[(c >> 12) & 0xF]); // append the hex character for the left-most 4-bits
    		sb.append(hexChar[(c >> 8) & 0xF]);  // hex for the second group of 4-bits from the left
    		sb.append(hexChar[(c >> 4) & 0xF]);  // hex for the third group
    		sb.append(hexChar[c & 0xF]);         // hex for the last group, e.g., the right most 4-bits
    	    }
    	    else {
    		sb.append(c);
    	    }
    	}
    	return sb.toString();
        }
    public static <T> int linearSearch(T[] array, T search) {
    	for(int i = 0; i < array.length; ++i)
    	{
    		if(array[i].equals(search))
    			return i;
    	}
    	return -1;
    }
    
    public static String[] listToArray(List<String> list) {
    	String[] array = new String[list.size()];
    	list.toArray(array);
    	return array;
    }
    public static Integer[] listToArray(List<Integer> list) {
    	Integer[] array = new Integer[list.size()];
    	list.toArray(array);
    	return array;
    }
	public static String[] createStringArray(Long[] src) {
		String [] a = new String[src.length];
		for(int i = 0; i < a.length; ++i)
			a[i] = "" + src[i];
		return null;
	}

	
}
