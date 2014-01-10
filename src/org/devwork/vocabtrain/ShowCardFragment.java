package org.devwork.vocabtrain;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

// TODO: Speech empty -> visibiltiy = gone

public class ShowCardFragment extends TrainingFragment
{
	enum Gesture
	{
		NONE, PINCH
	}

	public static final String TAG = Constants.PACKAGE_NAME + ".ShowCardFragment";

	public ShowCardFragment()
	{
		super(R.layout.showcard_fragment);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
	{
		final View v = super.onCreateView(inflater, container, savedInstanceState);
		if(v == null) return null;

		setGestureDetector(new GestureDetector(getActivity(), new GestureDetector.OnGestureListener()
		{

			@Override
			public boolean onDown(final MotionEvent e)
			{
				return false;
			}

			@Override
			public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY)
			{

				final int dx = (int) (e2.getX() - e1.getX());
				if(Math.abs(dx) > v.getWidth() / 5 && Math.abs(velocityX) > Math.abs(velocityY))
				{
					if(velocityX < 0)
						OnNextButton();
					else
						OnPreviousButton();
					return false;
				}
				return false;
			}

			@Override
			public void onLongPress(final MotionEvent e)
			{
			}

			@Override
			public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY)
			{
				return false;
			}

			@Override
			public void onShowPress(final MotionEvent e)
			{
			}

			@Override
			public boolean onSingleTapUp(final MotionEvent event)
			{
				final int height = v.getHeight();
				if(event.getY() < height / 4 || event.getY() > height * 3 / 4) return false;
				final int width = v.getWidth();
				if(event.getX() < 50)
					OnPreviousButton();
				else if(event.getX() > width - 50)
					OnNextButton();
				else
					return false;
				return false;
			}
		}));

		return v;
	}

	@Override
	protected Card OnReset()
	{
		final Card c = super.OnReset();
		OnSolve();
		return c;
	}

}
