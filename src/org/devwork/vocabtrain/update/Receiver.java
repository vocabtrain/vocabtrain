package org.devwork.vocabtrain.update;

import com.tomgibara.android.veecheck.VeecheckReceiver;
import com.tomgibara.android.veecheck.VeecheckSettings;
import com.tomgibara.android.veecheck.VeecheckState;
import com.tomgibara.android.veecheck.util.PrefSettings;
import com.tomgibara.android.veecheck.util.PrefState;

import android.content.Context;
import android.preference.PreferenceManager;

public class Receiver extends VeecheckReceiver {

	@Override
	protected VeecheckSettings createSettings(Context context) {
		return new PrefSettings(PreferenceManager.getDefaultSharedPreferences(context));
	}
	
	@Override
	protected VeecheckState createState(Context context) {
		return new PrefState(context);
	}
	
}
