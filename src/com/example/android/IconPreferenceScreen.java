/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android;

import org.devwork.vocabtrain.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class IconPreferenceScreen extends PreferenceGroup
{

	private Drawable mIcon;

	public IconPreferenceScreen(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public IconPreferenceScreen(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.preference_icon);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconPreferenceScreen, defStyle, 0);
		mIcon = a.getDrawable(R.styleable.IconPreferenceScreen_icon);

	}

	@Override
	public void onBindView(View view)
	{
		super.onBindView(view);
		if(Build.BOARD.equals("zoom2") && Build.BRAND.equals("nook") && Build.DEVICE.equals("zoom2")) // nook touch renders default white text on white background
		{
			((TextView) view.findViewById(android.R.id.summary)).setTextColor(Color.BLACK);
			((TextView) view.findViewById(android.R.id.title)).setTextColor(Color.BLACK);
		}

		// v.setBackgroundColor(getActivity().getResources().getColor(android.R.color.background_light)); // BUG in Android 2.3
		ImageView imageView = (ImageView) view.findViewById(R.id.icon);
		if(imageView != null && mIcon != null)
		{
			imageView.setImageDrawable(mIcon);
		}
	}

	/**
	 * Sets the icon for this Preference with a Drawable.
	 * 
	 * @param icon
	 *            The icon for this Preference
	 */
	public void setIcon(Drawable icon)
	{
		if((icon == null && mIcon != null) || (icon != null && !icon.equals(mIcon)))
		{
			mIcon = icon;
			notifyChanged();
		}
	}

	/**
	 * Returns the icon of this Preference.
	 * 
	 * @return The icon.
	 * @see #setIcon(Drawable)
	 */
	public Drawable getIcon()
	{
		return mIcon;
	}
}
