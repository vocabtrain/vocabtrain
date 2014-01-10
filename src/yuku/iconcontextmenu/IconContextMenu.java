/*
 * Copyright 2011 yukuku <yukuku@gmail.com>
 * Copyright 2012 Dominik Köppl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package yuku.iconcontextmenu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.android.MenuBuilder;

public class IconContextMenu
{
	public interface IconContextItemSelectedListener
	{
		void onIconContextItemSelected(MenuItem item);
	}

	private final Menu menu;

	private IconContextItemSelectedListener iconContextItemSelectedListener;
	private final Context context;

	public IconContextMenu(Context context)
	{
		menu = new MenuBuilder(context);
		this.context = context;

	}

	public void show()
	{
		final IconContextMenuAdapter adapter = new IconContextMenuAdapter(context, menu);
		AlertDialog dlg = new AlertDialog.Builder(context).setAdapter(adapter, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if(iconContextItemSelectedListener != null)
				{
					iconContextItemSelectedListener.onIconContextItemSelected(adapter.getItem(which));
				}
			}
		}).setInverseBackgroundForced(true).create();
		dlg.show();
	}

	public static Menu newMenu(Context context, int menuId)
	{
		Menu menu = new MenuBuilder(context);
		new MenuInflater(context).inflate(menuId, menu);
		return menu;
	}

	public Menu getMenu()
	{
		return menu;
	}

	public void setOnIconContextItemSelectedListener(IconContextItemSelectedListener iconContextItemSelectedListener)
	{
		this.iconContextItemSelectedListener = iconContextItemSelectedListener;
	}

}
