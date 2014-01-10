package org.devwork.vocabtrain;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlashCardFragment extends TrainingFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".FlashCardFragment";

	public FlashCardFragment()
	{
		super(R.layout.flashcard_fragment);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if(v == null) return null;
		button_show = (Button) v.findViewById(R.id.flashcard_showbutton);
		button_show.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				OnNextPhase();
			}
		});
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final int buttonsize_ratio = prefs.getInt("buttonsize", -1);
		button_show.measure(0, 0);
		button_show.setHeight(button_show.getMeasuredHeight() * buttonsize_ratio / 100);

		return v;
	}

	private Button button_show;

	@Override
	protected Card OnReset()
	{
		Card card = super.OnReset();
		if(card == null) return null;
		button_show.setVisibility(View.VISIBLE);
		return card;
	}

	@Override
	protected void OnSolve()
	{
		super.OnSolve();
		button_show.setVisibility(View.GONE);
	}

}
