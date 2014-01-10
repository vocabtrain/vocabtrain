package org.devwork.vocabtrain;

import org.devwork.vocabtrain.sync.SyncActivity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TourTipsDialog extends DialogFragment {
	public static final String TAG = Constants.PACKAGE_NAME + ".TourTipsDialog";

	
	/*
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		if(!prefs.getBoolean("tourtips", true)) return;
		final int phase = prefs.getInt("tourtips_phase", 0);
		
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(R.layout.tourtip_toast, (ViewGroup) activity.findViewById(R.id.tourtip_layout));
		ImageView image = (ImageView) layout.findViewById(R.id.tourtip_image);
		TextView text = (TextView) layout.findViewById(R.id.tourtip_text);
		final Toast toast = new Toast(activity.getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		
				
		switch(phase)
		{
			case 1:
				image.setImageResource(R.drawable.btn_books);
				text.setText("Use the books icon to load vocabulary from the books database in either your selection or your filing.");
				break;
			case 2:
				image.setImageResource(R.drawable.btn_filing);
				text.setText("Contains all cards stored as filings based on the Leitner-system.");
				break;
			case 3:
				image.setImageResource(R.drawable.btn_algo);
				text.setText("Selects those cards in the filing, that should be reviewed accourding to the algorithm.");
				break;
			default:
			text.setText("Welcome to Vocabtrain. You can use this guide as quick help. You can disable this message in the preferences.");
			
		}

	Editor edit = prefs.edit();
	edit.putInt("tourtips_phase", (phase+1) % 4);
	edit.commit();
	toast.show();

	}
	*/
	
	
	public static void createInstance(String tag, FragmentActivity activity)
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		if(!prefs.getBoolean("tourtips", true)) return;

		TourTipsDialog fragment = new TourTipsDialog();
		Bundle bundle = new Bundle();
		bundle.putString("tag", tag);
		fragment.setArguments(bundle);
		fragment.show(activity.getSupportFragmentManager(), TourTipsDialog.TAG);
	}

    
	private int position;
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
	    //final AlertDialog dialog = new AlertDialog(getActivity());
	    Bundle bundle = getArguments();
	    String tag = bundle.getString("tag");
	    
	    int message_id;
	    int title_id;
	    int icons_id;
	    
	    if(tag.equals(DashboardFragment.TAG))
	    {
	    	message_id = R.array.tip_dashboard;
	    	title_id = R.array.tip_title_dashboard;
	    	icons_id = R.array.tip_icon_dashboard;
	    }
	    else if(tag.equals(DashboardFragment.TAG_DATABASE))
	    {
	    	message_id = R.array.tip_dashboard_database;
	    	title_id = R.array.tip_title_dashboard_database;
	    	icons_id = R.array.tip_icon_dashboard_database;
	    }
	    else if(tag.equals(DashboardFragment.TAG_SELECTION))
	    {
	    	message_id = R.array.tip_dashboard_selection;
	    	title_id = R.array.tip_title_dashboard_selection;
	    	icons_id = R.array.tip_icon_dashboard_selection;
	    }
	    else if(tag.equals(DashboardFragment.TAG_TRAINING))
	    {
	    	message_id = R.array.tip_dashboard_training;
	    	title_id = R.array.tip_title_dashboard_training;
	    	icons_id = R.array.tip_icon_dashboard_training;
	    }
	    else if(tag.equals(BooklistFragment.TAG))
	    {
	    	message_id = R.array.tip_booklist;
	    	title_id = R.array.tip_title_booklist;
	    	icons_id = R.array.tip_icon_booklist;
	    }
	    else if(tag.equals(FilinglistFragment.TAG))
	    {
	    	message_id = R.array.tip_filinglist;
	    	title_id = R.array.tip_title_filinglist;
	    	icons_id = R.array.tip_icon_filinglist;
	    }
	    else if(tag.equals(SyncActivity.TAG))
	    {
	    	message_id = R.array.tip_sync;
	    	title_id = R.array.tip_title_sync;
	    	icons_id = R.array.tip_icon_sync;
	    }
	    else
	    {
	    	message_id = R.array.tip_dashboard;
	    	title_id = R.array.tip_title_dashboard;
	    	icons_id = R.array.tip_icon_dashboard;
	    }
	    
	    final String[] message = getActivity().getResources().getStringArray(message_id);
	    final String[] title = getActivity().getResources().getStringArray(title_id);
	    final TypedArray icons = getActivity().getResources().obtainTypedArray(icons_id);
	    	    
		final Dialog dialog = new Dialog(getActivity());
		dialog.setContentView(R.layout.tourtip_dialog);
		//final ImageView image = (ImageView) dialog.findViewById(R.id.tourtip_image);
		final TextView text = (TextView) dialog.findViewById(R.id.tourtip_text);
		final Button ok = (Button) dialog.findViewById(R.id.tourtip_ok); 
		final Button prev = (Button) dialog.findViewById(R.id.tourtip_prev);
		final Button next = (Button) dialog.findViewById(R.id.tourtip_next);
	
		
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		class ClickListener implements OnClickListener
		{
			private int incr;
			ClickListener(int incr)
			{
				this.incr = incr;
			}
			@Override
			public void onClick(View v) {
				position = (position + title.length + incr) % title.length;
				dialog.setTitle(title[position]);
				text.setText(message[position]);
				text.setCompoundDrawablesWithIntrinsicBounds(icons.getDrawable(position), null, null, null);
				text.setCompoundDrawablePadding(10);
			}
			
		}
		
		new ClickListener(0).onClick(null);
		next.setOnClickListener(new ClickListener(+1));
		prev.setOnClickListener(new ClickListener(-1));
			    	    
	    return dialog;
	}

	
}
