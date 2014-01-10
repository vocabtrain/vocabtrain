package org.devwork.vocabtrain;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class IntegerPreference extends EditTextPreference {


    public IntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.valueOf(value));
    }
    
    
}
