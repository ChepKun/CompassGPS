package com.lunadevel.compassgps;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

public class EnableGPSSettingsDialogFragment extends DialogFragment {
	
	/***
	 * Classes implementing this interface should implement these methods in order to receive Positive and Negative clicks:
	 * <ul><li><code>onDialogPositiveClick(DialogFragment, boolean)</code></li>
	 * <li><code>onDialogNegativeClick(DialogFragment, boolean)</code></li></ul>
	 * being the <code>DialogFragment</code> the dialog itself and <code>boolean</code> the request to show it again or not.
	 */
	public interface EnableGPSSettingsDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog, boolean showNextTime);
		public void onDialogNegativeClick(DialogFragment dialog, boolean showNextTime);
	}
	
	EnableGPSSettingsDialogListener listener;
	private CheckBox chkDontShowAgain;
	
	public EnableGPSSettingsDialogFragment() {
		// Empty constructor required for DialogFragment
	}
	
	private boolean getShowNextTime() {
		return !chkDontShowAgain.isChecked();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View view = inflater.inflate(R.layout.dialog_enablegps, null); 
		
		chkDontShowAgain = (CheckBox) view.findViewById(R.id.chkEnableGPSDialogDontShow);
		
		builder.setView(view)
			.setMessage(R.string.dialog_text)
			.setTitle(R.string.dialog_title)
			.setPositiveButton(R.string.title_activity_settings, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Send the positive button event back to the host activity
					listener.onDialogPositiveClick(EnableGPSSettingsDialogFragment.this, getShowNextTime());
				}
			})
			.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Send the negative button event back to the host activity
					listener.onDialogNegativeClick(EnableGPSSettingsDialogFragment.this, getShowNextTime());
				}
			});
		// Create the AlertDialog object and return it
		return builder.create();
	}
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			listener = (EnableGPSSettingsDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString() + " must implement EnableGPSSettingsDialogListener");
		}
	}
	
}