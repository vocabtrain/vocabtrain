package org.devwork.vocabtrain;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.SimpleXYSeries.ArrayFormat;
import com.example.android.ActionBarFragmentActivity;

public class PlotFragment extends DatabaseFragment
{
	public static final String TAG = Constants.PACKAGE_NAME + ".PlotFragment";
	private int sequence = Sequence.DEFAULT_SEQUENCE;

	@Override
	public void onStart()
	{
		super.onStart();
		if(getActivity() instanceof ActionBarFragmentActivity) ((ActionBarFragmentActivity) getActivity()).getActionBarHelper().onStart();
	}

	private XYPlot plot;

	/*
	 * class Series implements XYSeries {
	 * 
	 * @Override public Number getX(int index) { return index; }
	 * 
	 * @Override public Number getY(int index) { Cursor c = getDatabaseHelper().getRead().query("filing", new String[] { "COUNT(*)" }, "filing_sequence = ? AND filing_interval = ?", new String[] { "" + sequence, "" + index }, "filing_interval", null, null); int ret = c.moveToFirst() ? c.getInt(0) : 0; c.close(); return ret; }
	 * 
	 * @Override public String getTitle() { return "Next Session"; }
	 * 
	 * @Override public int size() { Cursor c = getDatabaseHelper().getRead().query("filing", new String[] { "MAX(filing_interval)" }, "filing_sequence = ?", new String[] { "" + sequence }, null, null, null); c.moveToFirst(); int ret = c.getInt(0); c.close(); return ret + 1; }
	 * 
	 * }
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		sequence = DatabaseFunctions.getSequence(getActivity());
		// setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.plot_fragment, container, false);

		// Initialize our XYPlot reference:
		plot = (XYPlot) v.findViewById(R.id.plot_plot);

		Button button_absinterval = (Button) v.findViewById(R.id.plot_button_absinterval);
		Button button_nextalgo = (Button) v.findViewById(R.id.plot_button_nextalgo);
		Button button_nextalgo_kumul = (Button) v.findViewById(R.id.plot_button_nextalgo_kumul);

		button_absinterval.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{

				final Vector<Integer> vector = new Vector<Integer>();
				Cursor cursor = getDatabaseHelper().getRead().query("filing", new String[] { "filing_interval", "COUNT(*)" }, "filing_sequence = ?", new String[] { "" + sequence }, "filing_interval", null, null);
				int vector_index = 1;
				int max = 0;
				while(cursor.moveToNext())
				{
					int index = cursor.getInt(0);
					if(index < 1) continue;
					while(vector_index < Math.log(index))
					{
						vector.add(0);
						++vector_index;
					}
					final int value = cursor.getInt(1);
					vector.add(value);
					if(max < value) max = value;
					++vector_index;
				}
				cursor.close();
				while(vector.size() < 3)
					vector.add(0);
				plot.setRangeLabel(getString(R.string.plot_cards));
				plot.setDomainLabel(getString(R.string.plot_sessions));
				// plot.setTicksPerRangeLabel(max / 20 + 1);
				// plot.setTicksPerDomainLabel(vector.size() / 20 + 1);
				plot(new SimpleXYSeries(vector, ArrayFormat.Y_VALS_ONLY, getString(R.string.plot_type_interval_desc)), Color.BLUE, getString(R.string.plot_type_interval));

			}
		});
		button_nextalgo_kumul.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				final Vector<Integer> vector = new Vector<Integer>();

				Cursor cursor = getDatabaseHelper().getRead().query("filing", new String[] { "filing_session+filing_interval - (select min(filing_session+filing_interval) from filing where filing_sequence = '" + sequence + "' )", "count(*)" }, "filing_sequence = ?", new String[] { "" + sequence }, "filing_session+filing_interval", null, null, "19");

				vector.add(0);
				int vector_index = 1;
				while(cursor.moveToNext())
				{
					int index = cursor.getInt(0);
					while(vector_index < index)
					{
						vector.add(vector.lastElement());
						++vector_index;
					}
					vector.add(cursor.getInt(1) + vector.lastElement());
					++vector_index;
				}
				cursor.close();
				while(vector.size() < 19)
					vector.add(0);
				vector.remove(0);
				plot.setRangeLabel(getString(R.string.plot_cards));
				plot.setDomainLabel(getString(R.string.plot_sessions));
				// plot.setTicksPerRangeLabel(vector.lastElement() / 20 + 1);
				// plot.setTicksPerDomainLabel(vector.size() / 20 + 1);
				plot(new SimpleXYSeries(vector, ArrayFormat.Y_VALS_ONLY, getString(R.string.plot_type_nextalgo_kumul_desc)), Color.YELLOW, getString(R.string.plot_type_nextalgo_kumul));
			}

		});
		button_nextalgo.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				final Vector<Integer> vector = new Vector<Integer>();

				Cursor cursor = getDatabaseHelper().getRead().query("filing", new String[] { "filing_session+filing_interval - (select min(filing_session+filing_interval) from filing where filing_sequence = '" + sequence + "' )", "count(*)" }, "filing_sequence = ?", new String[] { "" + sequence }, "filing_session+filing_interval", null, null, "19");
				int vector_index = 0;
				int max = 0;
				while(cursor.moveToNext())
				{
					int index = cursor.getInt(0);
					while(vector_index < index)
					{
						vector.add(0);
						++vector_index;
					}
					final int value = cursor.getInt(1);
					vector.add(value);
					if(max < value) max = value;
					++vector_index;
				}
				cursor.close();
				while(vector.size() < 19)
					vector.add(0);

				plot.setRangeLabel(getString(R.string.plot_cards));
				plot.setDomainLabel(getString(R.string.plot_sessions));
				// plot.setTicksPerRangeLabel(max / 20 + 1);
				// plot.setTicksPerDomainLabel(vector.size() / 20 + 1);
				plot(new SimpleXYSeries(vector, ArrayFormat.Y_VALS_ONLY, getString(R.string.plot_type_nextalgo_desc)), Color.RED, getString(R.string.plot_type_nextalgo));
			}

		});
		button_nextalgo.performClick();
		return v;
	}

	private void plot(XYSeries series, int color, String title)
	{

		// setup our line fill paint to be a slightly transparent gradient:
		Paint lineFill = new Paint();
		lineFill.setAlpha(200);
		lineFill.setShader(new LinearGradient(0, 0, 0, 250, Color.WHITE, color, Shader.TileMode.CLAMP));

		StepFormatter stepFormatter = new StepFormatter(Color.rgb(0, 0, 0), color);
		stepFormatter.getLinePaint().setStrokeWidth(1);

		stepFormatter.getLinePaint().setAntiAlias(false);
		stepFormatter.setFillPaint(lineFill);
		plot.clear();
		plot.addSeries(series, stepFormatter);
		// plot.getGraphWidget().setTicksPerRangeLabel(1);
		// plot.getGraphWidget().setTicksPerDomainLabel(1);
		plot.getGraphWidget().setRangeValueFormat(new DecimalFormat("0"));
		plot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
		plot.getGraphWidget().setRangeLabelWidth(25);
		plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
		plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);

		plot.setTitle(title);
		plot.disableAllMarkup();
		plot.setDomainBoundaries(series.getX(0), series.getX(series.size() - 1), BoundaryMode.AUTO);
		touchListener.refresh(series);
	}

	private static enum TouchState
	{
		NONE, ONE_FINGER_DRAG, TWO_FINGERS_DRAG
	}

	private final TouchListener touchListener = new TouchListener();

	private class TouchListener implements OnTouchListener
	{
		private void refresh(XYSeries series)
		{
			// Enact all changes
			plot.redraw();

			// Set of internal variables for keeping track of the boundaries
			plot.calculateMinMaxVals();
			minXY = new PointF(plot.getCalculatedMinX().floatValue(), plot.getCalculatedMinY().floatValue()); // initial minimum data point
			absMinX = minXY.x; // absolute minimum data point
			// absolute minimum value for the domain boundary maximum
			minNoError = Math.round(series.getX(1).floatValue() + 2);
			maxXY = new PointF(plot.getCalculatedMaxX().floatValue(), plot.getCalculatedMaxY().floatValue()); // initial maximum data point

			setTicks();

			absMaxX = maxXY.x; // absolute maximum data point
			// absolute maximum value for the domain boundary minimum
			maxNoError = (float) Math.round(series.getX(series.size() - 1).floatValue()) - 2;

			// Check x data to find the minimum difference between two neighboring domain values
			// Will use to prevent zooming further in than this distance
			double temp1 = series.getX(0).doubleValue();
			double temp2 = series.getX(1).doubleValue();
			double temp3;
			double thisDif;
			minDif = 1000000; // increase if necessary for domain values
			for(int i = 2; i < series.size(); i++)
			{
				temp3 = series.getX(i).doubleValue();
				thisDif = Math.abs(temp1 - temp3);
				if(thisDif < minDif) minDif = thisDif;
				temp1 = temp2;
				temp2 = temp3;
			}
			minDif = minDif + difPadding; // with padding, the minimum difference

			plot.setOnTouchListener(this);
		}

		private TouchState mode = TouchState.NONE;

		private PointF firstFinger;
		private float lastScrolling;
		private float distBetweenFingers;
		private float lastZooming;
		final private double difPadding = 0.1;
		private PointF minXY;
		private PointF maxXY;
		private float absMinX;
		private float absMaxX;
		private float minNoError;
		private float maxNoError;
		private double minDif;

		@Override
		public boolean onTouch(View arg0, MotionEvent event)
		{
			switch(event.getAction() & MotionEvent.ACTION_MASK)
			{
				case MotionEvent.ACTION_DOWN: // Start gesture
					firstFinger = new PointF(event.getX(), event.getY());
					mode = TouchState.ONE_FINGER_DRAG;
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					// When the gesture ends, a thread is created to give inertia to the scrolling and zoom
					final Timer t = new Timer();
					t.schedule(new TimerTask()
					{
						@Override
						public void run()
						{
							if(Math.abs(lastScrolling) > 1f || Math.abs(lastZooming - 1) < 1.10)
							{
								Log.e(TAG, "" + lastScrolling + ", " + lastZooming);
								lastScrolling *= .8; // speed of scrolling damping
								scroll(lastScrolling);
								lastZooming += (1 - lastZooming) * .2; // speed of zooming damping
								zoom(lastZooming);
								checkBoundaries();
								try
								{
									plot.postRedraw();
								}
								catch(final InterruptedException e)
								{
									e.printStackTrace();
								}
								// the thread lives until the scrolling and zooming are imperceptible
							}
						}
					}, 0);

				case MotionEvent.ACTION_POINTER_DOWN: // second finger
					distBetweenFingers = DatabaseFunctions.getDistance(event);
					// the distance check is done to avoid false alarms
					if(distBetweenFingers > 5f) mode = TouchState.TWO_FINGERS_DRAG;
					break;
				case MotionEvent.ACTION_MOVE:
					switch(mode)
					{
						case ONE_FINGER_DRAG:
						{
							final PointF oldFirstFinger = firstFinger;
							firstFinger = new PointF(event.getX(), event.getY());
							lastScrolling = oldFirstFinger.x - firstFinger.x;
							scroll(lastScrolling);
							lastZooming = (firstFinger.y - oldFirstFinger.y) / plot.getHeight();
							if(lastZooming < 0)
								lastZooming = 1 / (1 - lastZooming);
							else
								lastZooming += 1;
							zoom(lastZooming);
							checkBoundaries();
							plot.redraw();

						}
							break;
						case TWO_FINGERS_DRAG:
						{
							final float oldDist = distBetweenFingers;
							distBetweenFingers = DatabaseFunctions.getDistance(event);
							if(distBetweenFingers < 5.0f) return false;
							lastZooming = oldDist / distBetweenFingers;
							zoom(lastZooming);
							checkBoundaries();
							plot.redraw();
						}
							break;
					}
					break;
			}
			return true;
		}

		private void zoom(float scale)
		{
			final float domainSpan = maxXY.x - minXY.x;
			final float domainMidPoint = maxXY.x - domainSpan / 2.0f;
			final float offset = domainSpan * scale / 2.0f;
			minXY.x = domainMidPoint - offset;
			maxXY.x = domainMidPoint + offset;
		}

		private void scroll(float pan)
		{
			final float domainSpan = maxXY.x - minXY.x;
			final float step = domainSpan / plot.getWidth();
			final float offset = pan * step;
			minXY.x += offset;
			maxXY.x += offset;
		}

		private void setTicks()
		{
			plot.setTicksPerRangeLabel((plot.getCalculatedMaxY().intValue() - plot.getCalculatedMinY().intValue()) / 21 + 1);
			plot.setTicksPerDomainLabel((int) (maxXY.x - minXY.x) / 21 + 1);
		}

		private void checkBoundaries()
		{
			// Make sure the proposed domain boundaries will not cause plotting issues
			if(minXY.x < absMinX)
				minXY.x = absMinX;
			else if(minXY.x > maxNoError) minXY.x = maxNoError;
			if(maxXY.x > absMaxX)
				maxXY.x = absMaxX;
			else if(maxXY.x < minNoError) maxXY.x = minNoError;
			if(maxXY.x - minXY.x < minDif) maxXY.x = maxXY.x + (float) (minDif - (maxXY.x - minXY.x));
			plot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.AUTO);
			setTicks();

		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		onRefresh();
	}

	void onRefresh()
	{
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

}