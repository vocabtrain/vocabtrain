package org.devwork.vocabtrain;

import android.content.Context;

public class RowNotFoundException extends Exception
{
	private static final long serialVersionUID = 8334907470796045783L;
	final int string_id;
	final long row_id;
	public RowNotFoundException(String str)
	{
		super(str);
		string_id = -1;
		row_id = -1;
	}
	public RowNotFoundException(int string_id, long row_id)
	{
		this.string_id = string_id;
		this.row_id = row_id;
	}
	public String toString(Context context)
	{
		if(string_id == -1) return toString();
		return String.format(context.getString(string_id), row_id);
	}
}