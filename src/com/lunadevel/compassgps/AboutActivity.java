package com.lunadevel.compassgps;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public class AboutActivity extends Activity {
	
	private ImageView imgIcon;
	private TextView lblAppName;
	private TextView lblAttributions;
	private boolean isNavigatingUp = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		imgIcon = (ImageView) findViewById(R.id.imgIcon);
		lblAppName = (TextView) findViewById(R.id.lblAppName);
		lblAttributions = (TextView) findViewById(R.id.lblAboutAttributions);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		setAppIconNameVer();
		setAttributions();
	}
	
	private void setAppIconNameVer() {
		try {
			PackageManager pmanager = this.getPackageManager();
			PackageInfo pinfo = pmanager.getPackageInfo(this.getPackageName(), 0);
			
			lblAppName.setText(getString(getApplicationInfo().labelRes) + " " + pinfo.versionName);
		} catch (NameNotFoundException e) {
			lblAppName.setText(getString(getApplicationInfo().labelRes));
		}		
	}

	private void setAttributions() {
		String attributions = new String("");
		
		attributions += "<a href=\"http://thenounproject.com/noun/satellite/#icon-No5350\" target=\"_blank\">Satellite icon</a> " + getString(R.string.about_designedby) + " <a href=\"http://thenounproject.com/fukupixel\" target=\"_blank\">Pedro Ramalho</a> " + getString(R.string.about_from) + " The Noun Project.";
		
		attributions += "<br /><br />";
		
		//attributions += "NumberPickerPreference by <a href=\"mailto:viktorreiser@gmx.de\">viktorreiser@gmx.de</a>";
		
		lblAttributions.setText(Html.fromHtml(attributions));
		lblAttributions.setMovementMethod(LinkMovementMethod.getInstance());
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onNavigateUp() {
		isNavigatingUp = true;
		return super.onNavigateUp();
	}
	
	@Override
	public void finish() {
		super.finish();
		if(isNavigatingUp)
			animateActivityOut();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		animateActivityOut();
	}
	
	private void animateActivityOut() {
		overridePendingTransition(R.anim.fadeinltr, R.anim.fadeoutltr);
	}
}
