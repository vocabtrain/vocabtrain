package org.devwork.vocabtrain;

import android.support.v4.app.Fragment;

public interface FragmentCreator {
	public Fragment create();
	public Fragment update(Fragment fragment); // can be deleted!
	public String getTag();
	public boolean equals(Fragment fragment);
}