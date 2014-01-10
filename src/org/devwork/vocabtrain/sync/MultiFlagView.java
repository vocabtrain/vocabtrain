package org.devwork.vocabtrain.sync;

import org.devwork.vocabtrain.LanguageLocale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class MultiFlagView extends ImageView
{

	public MultiFlagView(Context context)
	{
		super(context);
	}

	public MultiFlagView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public MultiFlagView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	private BookData book;

	public void setLanguages(BookData book)
	{
		this.book = book;
		invalidate();
	}

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas)
	{
		final String[] langs = book.getTranslationLanguages();
		if(langs == null || 	langs.length == 0) return; // bug?
		final int cols = (int) Math.ceil(Math.sqrt(langs.length));
		final int rows = (int) Math.ceil(langs.length / (double) cols);
		final int width = getWidth();
		final int height = getHeight();
		final int panel_width = width / cols;
		final int panel_height = height / rows;

		final Rect rect = new Rect(0, 0, panel_width, panel_height);
		final int rectColumns = (langs.length < cols * rows) ? cols - 1 : cols;

		for(int x = 0; x < rectColumns; ++x)
			for(int y = 0; y < rows; ++y)
			{
				final Drawable d = getContext().getResources().getDrawable(new LanguageLocale(getContext(), langs[x * rows + y]).getFlagId());
				rect.offsetTo(x * panel_width, y * panel_height);
				d.setBounds(rect);
				d.draw(canvas);
			}
		if(rectColumns != cols)
		{
			final int remaining = cols * rows - langs.length;
			final int remaining_panel_height = height / remaining;
			final Rect remaining_rect = new Rect(0, 0, panel_width, remaining_panel_height);
			for(int y = 0; y < remaining; ++y)
			{
				final Drawable d = getContext().getResources().getDrawable(new LanguageLocale(getContext(), langs[rectColumns * rows + y]).getFlagId());
				remaining_rect.offsetTo(rectColumns * panel_width, y * remaining_panel_height);
				d.setBounds(remaining_rect);
				d.draw(canvas);
			}
		}

	}
}
