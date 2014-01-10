package org.devwork.vocabtrain.update;

import org.devwork.vocabtrain.R;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;

import com.tomgibara.android.veecheck.VeecheckActivity;
import com.tomgibara.android.veecheck.VeecheckState;
import com.tomgibara.android.veecheck.util.PrefState;

public class ConfirmActivity extends VeecheckActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.updateconfirm_activity);
	}
	
	@Override
	protected VeecheckState createState() {
		return new PrefState(this);
	}
	
	@Override
	protected View getNoButton() {
		return findViewById(R.id.update_confirm_no);
	}
	
	@Override
	protected View getYesButton() {
		return findViewById(R.id.update_confirm_yes);
	}

	@Override
	protected Checkable getStopCheckBox() {
		return (CheckBox) findViewById(R.id.update_confirm_stop);
	}
}
