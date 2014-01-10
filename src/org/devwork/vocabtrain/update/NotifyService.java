package org.devwork.vocabtrain.update;

import org.devwork.vocabtrain.R;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;

import com.tomgibara.android.veecheck.VeecheckNotifier;
import com.tomgibara.android.veecheck.VeecheckService;
import com.tomgibara.android.veecheck.VeecheckState;
import com.tomgibara.android.veecheck.util.DefaultNotifier;
import com.tomgibara.android.veecheck.util.PrefState;

public class NotifyService extends VeecheckService
{

	public static final int NOTIFICATION_ID = 1;

	@Override
	protected VeecheckNotifier createNotifier()
	{
		// it's good practice to set up filters to help guard against malicious intents
		IntentFilter[] filters = new IntentFilter[1];
		try
		{
			IntentFilter filter = new IntentFilter(Intent.ACTION_VIEW);
			filter.addDataType("text/html");
			filter.addDataScheme("http");
			filters[0] = filter;
		}
		catch(MalformedMimeTypeException e)
		{
			Log.e("veechecksample", "Invalid data type for filter.", e);
		}

		// return a default notifier implementation
		return new DefaultNotifier(this, NOTIFICATION_ID, filters, new Intent(this, ConfirmActivity.class), R.drawable.ic_home, R.string.update_notify_ticker, R.string.update_notify_title, R.string.update_notify_message);
	}

	@Override
	protected VeecheckState createState()
	{
		return new PrefState(this);
	}

}
