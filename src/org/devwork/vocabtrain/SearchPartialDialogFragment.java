package org.devwork.vocabtrain;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SearchPartialDialogFragment extends DialogFragment {
	public static final String TAG = Constants.PACKAGE_NAME + ".SearchPartialDialogFragment";

	public static SearchPartialDialogFragment createInstance(String search, long card_id) {
	SearchPartialDialogFragment dialog = new SearchPartialDialogFragment();
       Bundle args = new Bundle();
       args.putString("search", search);
       args.putLong("card_id", card_id);
       dialog.setArguments(args);
       return dialog;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.searchpartial_dialog, container, false);
        this.setHasOptionsMenu(true);
        final Dialog dialog = getDialog();
        if(dialog != null)
        {
        	dialog.setTitle(getActivity().getString(R.string.search_partial));
        }
        
        edit =  (EditText) v.findViewById(R.id.searchpartial_edit);
		Button button =  (Button) v.findViewById(R.id.searchpartial_button);
		
		edit.setText(getArguments().getString("search"));
		
		button.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(dialog != null) dialog.openOptionsMenu();
				actionMode.start();
			}

			
		});
		edit.setOnKeyListener(new EditText.OnKeyListener() 
        {
        	@Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                  if(dialog != null) dialog.openOptionsMenu();
                  actionMode.start();
                  return true;
                }
                return false;
            }
        });
    	DatabaseHelper dbh = new DatabaseHelper(getActivity());
    	card = dbh.getCardById(getArguments().getLong("card_id"));
    	dbh.close();
        
	    return v;

    }
    private Card card; 
    
    public String getSearch()
    {
    	return getArguments().getString("search");
    }
    
    public void setSearch(String search)
    {
        getArguments().putString("search", search);
        edit.setText(search);
    }
	private EditText edit;
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(card == null) return false;
		if(SearchIntents.search(item.getItemId(), edit.getText().toString(), card, getActivity()))
		{
			dismiss();
			return true;
		}
        return super.onOptionsItemSelected(item);
    }
    private final ActionModeCallback actionMode = new ActionModeCallback();
    
    @TargetApi(11)
	private class ActionModeCallback implements ActionMode.Callback 
    {
        private ActionMode actionMode;
        
    	@Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
    		if(card == null) return false;
            actionMode.setTitle(getActivity().getResources().getQuantityString(R.plurals.chapters_select, 0, 0));

            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.searchpartial_options, menu);
            inflater.inflate(R.menu.dicts, menu);
            SearchIntents.disableMissingMenuEntries(menu, getActivity(), card);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        	if(card == null) return false;
    		if(SearchIntents.search(menuItem.getItemId(), edit.getText().toString(), card, getActivity()))
    		{
				final FragmentManager manager = getActivity().getSupportFragmentManager();
				final FragmentTransaction ft = manager.beginTransaction();
				ft.remove(SearchPartialDialogFragment.this);
				ft.commit();
				manager.popBackStack();
				dismiss();
				
				actionMode.finish();
    			return true;
    		}
    		return false;
        }
        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
        	this.actionMode = null;
        }
        
        
        public void start()
        {
            if (actionMode == null) 
            	actionMode = getActivity().startActionMode(this);
            actionMode.setTitle(R.string.send_text_to);
        }
        
        
    };
	
}
