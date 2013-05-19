package com.lunadevel.compassgps;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ResetAllDialogsDialogPreference extends DialogPreference {
	
	public ResetAllDialogsDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if(positiveResult) {
			Editor editor = getEditor();
			Context context = getContext();
			
			//Write here all the values to be resetted
			editor.putBoolean(context.getString(R.string.pref_key_showenablegpssettings), Boolean.parseBoolean(context.getString(R.string.pref_key_showenablegpssettings_default)));
			
			//Important
			editor.commit();
		}
	}
}
