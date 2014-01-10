package org.devwork.vocabtrain;

import java.util.Iterator;
import java.util.LinkedList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.util.Log;

public class FragmentHelper extends Fragment  {
	
	private static class Element
	{
		public final FragmentCreator creator;
		public final int layout_id;
		public Element(FragmentCreator creator, int layout_id)
		{
			this.creator = creator;
			this.layout_id = layout_id;
		}
	}
	
	public static final String TAG = Constants.PACKAGE_NAME + ".FragmentHelper";

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}	
	private final LinkedList<Element> list = new LinkedList<Element>();
	
	
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
	public int getLastLayoutId()
	{
		commitBackStackChanges();
		return list.isEmpty() ? R.id.main_layout : list.getLast().layout_id;
	}
	
	public void add(FragmentCreator creator, int layout_id)
	{
		list.add(new Element(creator, layout_id));
		Log.e(TAG, "add: " + this);
	}
	
	private int hadSecondLayout = -1;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Element c : list)
		{
			sb.append(c.creator.getTag() + ", ");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public void repopulate()
	{
		Log.e(TAG, "repopulate: " + this);
		
		if(hadSecondLayout == -1) hadSecondLayout = getActivity().findViewById(R.id.main_layout_second) != null ? 1 : 0;
		if(list.isEmpty()) return;
		commitBackStackChanges();
		final FragmentManager manager = getActivity().getSupportFragmentManager();
		if(hadSecondLayout == 1 && getActivity().findViewById(R.id.main_layout_second) == null)
		{
			clearFragmentManager();
			for(final Element element : list)
			{
				final FragmentCreator creator = element.creator;
				final FragmentTransaction ft = manager.beginTransaction();
				ft.replace(R.id.main_layout, creator.create(), creator.getTag());
	        	ft.setBreadCrumbTitle(creator.getTag());
	        	ft.addToBackStack(null);
	        	ft.commit();
			}
		}
		else if(hadSecondLayout == 0 && getActivity().findViewById(R.id.main_layout_second) != null)
		{
			clearFragmentManager();
			int c = 0;
			for(final Element element : list)
			{
				final FragmentCreator creator = element.creator;
				final FragmentTransaction ft = manager.beginTransaction();
				ft.replace( (++c % 2 == 0) ? R.id.main_layout : R.id.main_layout_second, creator.create(), creator.getTag());
				ft.setBreadCrumbTitle(creator.getTag());
	        	ft.addToBackStack(null);
	        	ft.commit();
			}
		}
		hadSecondLayout = getActivity().findViewById(R.id.main_layout_second) != null ? 1 : 0;
		/*
		if(helper.list.size() > 0)
    		if(getActivity().findViewById(R.id.main_layout_second) == null)
        	{
        		final FragmentTransaction ft = manager.beginTransaction();
        		final FragmentCreator creator = list.getLast().creator;
        		
        		Fragment dFragment = manager.findFragmentByTag(creator.getTag());
        		if(dFragment != null) ft.remove(dFragment);
	        	ft.replace(R.id.main_layout, creator.create(), creator.getTag());
	        	ft.addToBackStack(null);
	        	ft.commit();
        	}
    		else
    		{
    			final FrameLayout first = (FrameLayout) findViewById(R.id.main_layout);
    			final FrameLayout second = (FrameLayout) findViewById(R.id.main_layout_second);
    			if(second.getChildCount() == 0)
    			{
    				final FragmentTransaction ft = manager.beginTransaction();
	        		FragmentCreator creator = helper.list.get(helper.list.size()-1);
	        		Fragment dFragment = manager.findFragmentByTag(creator.getTag());
	        		if(dFragment != null) ft.remove(dFragment);
		        	ft.replace(R.id.main_layout_second, creator.create(), creator.getTag());
		        	ft.addToBackStack(null);
		        	ft.commit();
    			}
    			if(first.getChildCount() == 1)
    			{
    				if(helper.list.size() > 1)
    				{
        				final FragmentTransaction ft = manager.beginTransaction();
    	        		FragmentCreator creator = helper.list.get(helper.list.size()-2);
    	        		Fragment dFragment = manager.findFragmentByTag(creator.getTag());
    	        		if(dFragment != null) ft.remove(dFragment);
    		        	ft.replace(R.id.main_layout, creator.create(), creator.getTag());
    		        	ft.addToBackStack(null);
    		        	ft.commit();
    				}
    				else
    				{
    					Log.e("DASHB", "A");
    					Fragment dashboard = manager.findFragmentByTag(DashboardFragment.TAG);
    					if(dashboard != null)
    					{
    						Log.e("DASHB", "B");
            				final FragmentTransaction ft = manager.beginTransaction();
            				//ft.replace(R.id.main_layout, dashboard, DashboardFragment.TAG);
            				
            				ft.attach(dashboard);
            				ft.show(dashboard);
            				ft.commit();
            				
    					}
    				}
    			}
    			
    				
    		}
    	Log.e(TAG, "" + helper.list.toString());
    	*/
	}
	
	private void clearFragmentManager()
	{
    	final FragmentManager manager = getActivity().getSupportFragmentManager();
    	if(manager.getBackStackEntryCount() == 0) return;
    	BackStackEntry entry = manager.getBackStackEntryAt(0);
    	if(entry == null) return;
    	int id = entry.getId();
    	manager.popBackStack(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}
	
	public void clear()
	{
		list.clear();
		clearFragmentManager();
	}
	
	private void commitBackStackChanges() {
		final FragmentManager manager = getActivity().getSupportFragmentManager();
		final int fragments_count = manager.getBackStackEntryCount();
		final Iterator<Element> it = list.iterator();
		Log.e(TAG, "onBackStackChanged Count : " + fragments_count);
		for(int i = 0; i < fragments_count && it.hasNext(); )
		{
			final Element element = it.next();
			final FragmentCreator creator = element.creator;
			final BackStackEntry entry = manager.getBackStackEntryAt(i);
			Log.e(TAG, "onBackStackChanged i : " + i + " " + entry + " " + creator.getTag());
			if(entry.getBreadCrumbTitle() == null)
			{
				++i; continue;
			}
			if(!entry.getBreadCrumbTitle().equals( creator.getTag()))
			{
					it.remove();
					continue;
			}
			final Fragment fragment =  manager.findFragmentByTag( creator.getTag());
			if(fragment == null || !creator.equals(fragment)) { it.remove(); continue; }
			++i;
		}
		while(it.hasNext())
		{
			it.next();
			it.remove();
		}
	}
	
	
	
	
}