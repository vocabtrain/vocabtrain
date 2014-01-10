package org.devwork.vocabtrain;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class TrainingChooserDialog extends DialogFragment {
	public static final String TAG = Constants.PACKAGE_NAME + ".TrainingChooserDialog";

	
	public static TrainingChooserDialog createInstance(boolean isSelectionEmpty)
	{
		TrainingChooserDialog dialog = new TrainingChooserDialog();
		Bundle bundle = new Bundle();
		bundle.putBoolean("isSelectionEmpty", isSelectionEmpty);
		dialog.setArguments(bundle);
		return dialog;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.trainingchooser_select_type));
		builder.setCancelable(true);
		
		//getDatabaseHelper().createSelection(list);
		final String [] types = getResources().getStringArray(R.array.training_types);
		builder.setItems(types, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				final boolean isSelectionEmpty = getArguments() == null ? false : getArguments().getBoolean("isSelectionEmpty"); 
				if(item == 0)
				{
					((MainActivity)getActivity()).showFragment(new FragmentCreator() {
						@Override
						public Fragment create() {
							return SelectionTableViewFragment.createInstance(isSelectionEmpty);
						}
						@Override
						public String getTag() {
							return SelectionTableViewFragment.TAG;
						}
						@Override
						public Fragment update(Fragment fragment) {
							((SelectionTableViewFragment)fragment).onRefresh();
							return fragment;
						}
						@Override
						public boolean equals(Fragment fragment) {
							return (fragment instanceof SelectionTableViewFragment);
						}
						
					});
                    return;
				}
				
				Intent intent = new Intent(getActivity(), TrainingActivity.class);
				intent.putExtra("clearselection_onDismiss", isSelectionEmpty);
				switch(item)
				{
				case 1:
					intent.putExtra("fragment", ShowCardFragment.TAG);
					break;
				
				case 2:
					intent.putExtra("fragment", FlashCardFragment.TAG);
					break;
				case 3:
					intent.putExtra("fragment", TextCardFragment.TAG);
					break;
					
				}
				startActivity(intent);
				dismiss();
			}
		});
		return builder.create();
    }
}
