package com.lunadevel.compassgps;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NumberPickerDialogPreference extends DialogPreference {
	
	private static final String NAMESPACE="http://schemas.android.com/apk/res/android";

	private TextView lblMessage;
    private EditText txtNum;
    private Button btnNumPlus;
    private Button btnNumMinus;

    private String mDialogMessage;
    
    private int mDefault;
    private int mMax, mMin;
    private int mValue = 1;

    public NumberPickerDialogPreference(Context context, AttributeSet attr) { 
        super(context, attr);
        setDialogLayoutResource(R.layout.dialog_numberpicker);
        
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        mDialogMessage = context.getString(attr.getAttributeResourceValue(NAMESPACE, "dialogMessage", -1));
        mDefault = attr.getAttributeIntValue(NAMESPACE,"defaultValue", 5);
        mMax = attr.getAttributeIntValue(NAMESPACE,"max", 100);
        //mMin = attr.getAttributeIntValue(NAMESPACE,"min", 0);
        mMin = 0;
        
        setPersistent(true);
    }

    @Override 
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        lblMessage = (TextView) view.findViewById(R.id.lblNumberPickerDialogMessage);
        lblMessage.setText(mDialogMessage);
        
        btnNumPlus = (Button) view.findViewById(R.id.btnNumberPickerDialogPlus);
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
        
        btnNumMinus = (Button) view.findViewById(R.id.btnNumberPickerDialogMinus);
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
        
        txtNum = (EditText) view.findViewById(R.id.txtNumberPickerDialogNum);
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
        
        System.out.println("CPC6");
        setSummary(mValue);
    }
    
    public void onButtonPlusPressed(View view) {
    	int currentVal = Integer.parseInt(txtNum.getText().toString());
    	txtNum.setText(String.valueOf(currentVal + 1));
    }
    
    public void onButtonMinusPressed(View view) {
    	int currentVal = Integer.parseInt(txtNum.getText().toString());
    	txtNum.setText(String.valueOf(currentVal - 1));
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
    	super.onSetInitialValue(restore, defaultValue);
        
        if (restore) {
        	System.out.println("CPC5.1");
            //mValue = shouldPersist() ? getPersistedInt(mDefault) : 5;
        	mValue = shouldPersist() ? Integer.parseInt(getPersistedString(String.valueOf(mDefault))) : 5;
        } else { 
        	System.out.println("CPC5.2");
            mValue = (Integer)defaultValue;
        }
        
        setSummary(mValue);
    }

    public void setSummary(int value) {
//        CharSequence summary = getSummary();
//        value=getPersistedInt(-1);
//        if (summary == null) {
//        	System.out.println("CPC1");
//            setSummary(Integer.toString(value));
//        } else {
//        	System.out.println("CPC2");
//            setSummary(String.format(summary.toString(), value));
//        }
        setSummary(Integer.toString(value));
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
        	mValue = Integer.parseInt(txtNum.getText().toString());
        	
        	if(mValue > mMax)
        		mValue = mMax;
        	else if (mValue < mMin)
        		mValue = mMin;
        	
        	System.out.println("Nuevo value es " + mValue);
        	System.out.println("CPC4");
        	setSummary(mValue);
        	persistString(String.valueOf(mValue));
        }
    }

}