package com.lunadevel.compassgps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NumberPickerDialogPreference extends DialogPreference {
	
	private static final String LOGCAT = "CompassGPS.NumberPickerDialogPreference";
	
	private static final String NAMESPACE="http://schemas.android.com/apk/res/android";
	
	private final boolean isHoneycombOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

	private RelativeLayout lytContent;
	private final String nbpNumTag = "nbpNum";
	private TextView lblMessage;
    private EditText txtNum;
    private Button btnNumPlus;
    private Button btnNumMinus;

    private String mDialogMessage;
    
    private int mDefault = 5;
    private int mMax, mMin;
    private int mValue = 5;

    public NumberPickerDialogPreference(Context context, AttributeSet attr) { 
        super(context, attr);
        setDialogLayoutResource(R.layout.dialog_numberpicker);
        
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        mDialogMessage = context.getString(attr.getAttributeResourceValue(NAMESPACE, "dialogMessage", -1));
        mDefault = attr.getAttributeIntValue(NAMESPACE,"defaultValue", mDefault);
        mMax = attr.getAttributeIntValue(NAMESPACE,"max", 100);
        //mMin = attr.getAttributeIntValue(NAMESPACE,"min", 1);
        mMin = 1;
        
        setPersistent(true);
    }
    
    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
    	super.onSetInitialValue(restore, defaultValue);
        
        if (restore)
        	mValue = shouldPersist() ? Integer.parseInt(getPersistedString(String.valueOf(mDefault))) : mDefault;
        else
            mValue = (Integer)defaultValue;
        
        setSummary(mValue);
    }
    
    private void setOldInterface(View view) {
        btnNumPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	txtNum.setCursorVisible(false);
            	txtNum.clearFocus();
            	int currentVal = Integer.parseInt(txtNum.getText().toString());
            	if(currentVal < 100)
            		txtNum.setText(String.valueOf(currentVal + 1));
            }
        });
        
        btnNumMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	txtNum.setCursorVisible(false);
            	txtNum.clearFocus();
            	int currentVal = Integer.parseInt(txtNum.getText().toString());
            	if(currentVal > 0)
            		txtNum.setText(String.valueOf(currentVal - 1));
            }
        });
        
        txtNum.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((EditText) v).selectAll();
			}
		});
        txtNum.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				txtNum.setCursorVisible(true);
				return false;
			}
		});
        txtNum.setText(String.valueOf(mValue));
    }
    
    @SuppressLint("NewApi")
	private void setModernInterface(View view) {
    	// Create a real NumberPicker and set values
    	android.widget.NumberPicker nbpNum = new android.widget.NumberPicker(view.getContext());
    	nbpNum.setTag(nbpNumTag);
    	nbpNum.setMaxValue(mMax);
    	nbpNum.setMinValue(mMin);
    	nbpNum.setValue(mValue);
    	
    	// Copy the layout params defined in XML for the old interface EditText 
    	ViewGroup.LayoutParams layoutParams = txtNum.getLayoutParams();
    	
    	// Add the newly created NumberPicker to the layout with the layout params
    	lytContent.addView(nbpNum, layoutParams);
    	
    	// Hide the old interface views
    	btnNumPlus.setVisibility(View.GONE);
    	btnNumMinus.setVisibility(View.GONE);
    	txtNum.setVisibility(View.GONE);
    }

    @Override 
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        lblMessage = (TextView) view.findViewById(R.id.lblNumberPickerDialogMessage);
        lblMessage.setText(mDialogMessage);
        
        lytContent = (RelativeLayout) view.findViewById(R.id.lytNumberPickerContent);
        
        btnNumPlus = (Button) view.findViewById(R.id.btnNumberPickerDialogPlus);
        btnNumMinus = (Button) view.findViewById(R.id.btnNumberPickerDialogMinus);
        txtNum = (EditText) view.findViewById(R.id.txtNumberPickerDialogNum);
        
        if(isHoneycombOrHigher)
        	setModernInterface(view);
        else
	        setOldInterface(view);
        
        setSummary(mValue);
    }
    
    public void onButtonPlusPressed(View view) {
    	int currentVal = Integer.parseInt(txtNum.getText().toString());
    	if(currentVal >= mMax)
    		txtNum.setText(String.valueOf(mMax));
    	else if (currentVal < mMin)
    		txtNum.setText(String.valueOf(mMin));
    	else
    		txtNum.setText(String.valueOf(currentVal + 1));
    }
    
    public void onButtonMinusPressed(View view) {
    	int currentVal = Integer.parseInt(txtNum.getText().toString());
    	if (currentVal > mMax)
    		txtNum.setText(String.valueOf(mMax));
    	else if (currentVal <= mMin)
    		txtNum.setText(String.valueOf(mMin));
    	else
    		txtNum.setText(String.valueOf(currentVal - 1));
    }

    public void setSummary(int value) {
//        CharSequence summary = getSummary();
//        value=getPersistedInt(-1);
//        if (summary == null) {
//        	Log.d(LOGCAT, "CPC1");
//            setSummary(Integer.toString(value));
//        } else {
//        	Log.d(LOGCAT, "CPC2");
//            setSummary(String.format(summary.toString(), value));
//        }
        setSummary(Integer.toString(value));
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
        	mValue = isHoneycombOrHigher ? getModernValue() : Integer.parseInt(txtNum.getText().toString());
        	
        	if(mValue > mMax)
        		mValue = mMax;
        	else if (mValue < mMin)
        		mValue = mMin;
        	
        	setSummary(mValue);
        	persistString(String.valueOf(mValue));
        }
    }
    
    @SuppressLint("NewApi")
	private int getModernValue() {
    	android.widget.NumberPicker nbpNum = (android.widget.NumberPicker) lytContent.findViewWithTag(nbpNumTag);
    	return nbpNum.getValue();
    }

}