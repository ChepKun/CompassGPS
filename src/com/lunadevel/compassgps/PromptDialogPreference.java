package com.lunadevel.compassgps;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class PromptDialogPreference extends DialogPreference {

	public PromptDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PromptDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if(positiveResult) {
			//TODO: Resetear el estado del aviso
		}
	}

}
