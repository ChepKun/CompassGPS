package com.lunadevel.compassgps;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ProgressBar;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import java.util.Date;

public class CompassGPSActivity extends FragmentActivity
	implements SensorEventListener, LocationListener, EnableGPSSettingsDialogFragment.EnableGPSSettingsDialogListener, OnSharedPreferenceChangeListener {
	
	private static final String LOGCAT = "CompassGPS";
	
	private boolean wasGPSChecked = false;
	private static final String KEY_WASGPSCHECKED = "wasGPSChecked";
	
	private SensorManager snmSensorManager;
	private Sensor snsCompass;
	private LocationManager lnmLocationManager;
	
	private TextView lblCompDegs;
	private TextView lblCompGeo;
	private ImageView imgCompass;
	private ImageView imgUp;
	private float declination = 0;
	private boolean showTrueNorth = true;
	
	private boolean isGPSEnabled = false;
	private boolean showGPSSettingsDialog = true;	
	
	private TextView lblGPSStatus;
	private static final int COLOR_GREY = Color.GRAY;
	private static final int COLOR_RED = Color.parseColor("#FF4444");
	private static final int COLOR_YELLOW = Color.parseColor("#FFBB33");
	private static final int COLOR_GREEN = Color.parseColor("#99CC00");
	
	private ProgressBar pgbGPSSignal;
	private ToggleButton tgbToggleGPS;
	private TextView lblLatTitle;
	private TextView lblLatNS;
	private TextView lblLatContent;
	private TextView lblLonTitle;
	private TextView lblLonWE;
	private TextView lblLonContent;
	
	private double lat = 0.0;
	private double lon = 0.0;
	
	private int GPS_REFRESH_TIME = 5; //seconds
    private int GPS_REFRESH_DISTANCE = 5; //meters
    
    private static final int GPS_DEGMINSEC = 1;
    private static final int GPS_DEGMIN = 2;
    private static final int GPS_DEGONLY = 3;
    private int GPS_format = GPS_DEGMINSEC;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		snmSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    snsCompass = snmSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    
	    lnmLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	    imgUp = (ImageView) findViewById(R.id.imgUp);
	    imgCompass = (ImageView) findViewById(R.id.imgCompass);
	    lblCompDegs = (TextView) findViewById(R.id.lblCompassDegrees);
	    lblCompGeo = (TextView) findViewById(R.id.lblCompassGeo);
	    
	    //Depending on target SDK version, UI is set up accordingly
	    boolean isHoneycombOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	    imgCompass.setVisibility(isHoneycombOrHigher ? View.VISIBLE : View.GONE);
	    imgUp.setVisibility(isHoneycombOrHigher ? View.GONE : View.VISIBLE);
	    
	    lblGPSStatus = (TextView) findViewById(R.id.lblGPSStatus);
	    pgbGPSSignal = (ProgressBar) findViewById(R.id.pgbLoading);
	    tgbToggleGPS = (ToggleButton) findViewById(R.id.tgbGPSToggle);
	    lblLatTitle = (TextView) findViewById(R.id.lblGPSLatLabel);
	    lblLatNS = (TextView) findViewById(R.id.lblGPSLatNS);
	    lblLatContent = (TextView) findViewById(R.id.lblGPSLatContent);
	    lblLonTitle = (TextView) findViewById(R.id.lblGPSLonLabel);
	    lblLonWE = (TextView) findViewById(R.id.lblGPSLonWE);
	    lblLonContent = (TextView) findViewById(R.id.lblGPSLonContent);
		
		lblCompDegs.setText("0°");
		lblCompGeo.setText("N");
		lblLatContent.setText("…");
		lblLonContent.setText("…");
		
		readPreferences();
		
		//forceOverflowMenuOfActionBar();
	}
	
	/**
	 * Forces the Overflow Menu on the ActionBar
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void forceOverflowMenuOfActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			try {
		        ViewConfiguration config = ViewConfiguration.get(this);
		        java.lang.reflect.Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
		        if(menuKeyField != null) {
		            menuKeyField.setAccessible(true);
		            menuKeyField.setBoolean(config, false);
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		}
	}
	
	private void readPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		readPrefCompassShowTrueNorth(prefs.getBoolean(getString(R.string.pref_key_compassshowmagneticnorth), Boolean.parseBoolean(getString(R.string.pref_key_compassshowmagneticnorth_default))));
		
		readPrefGPSDegreesFormat(prefs.getString(getString(R.string.pref_key_gpsdegreesformat), getString(R.string.pref_key_gpsdegreesformat_default)));
		
		readPrefGPSUpdateDistance(prefs.getString(getString(R.string.pref_key_gpsupdatedistance), getString(R.string.pref_key_gpsupdatedistance_default)));
		//readPrefGPSUpdateDistance(prefs.getInt(getString(R.string.pref_key_gpsupdatedistance), Integer.parseInt(getString(R.string.pref_key_gpsupdatedistance_default))));
		
		readPrefGPSUpdateTime(prefs.getString(getString(R.string.pref_key_gpsupdatetime), getString(R.string.pref_key_gpsupdatetime_default)));
		//readPrefGPSUpdateTime(prefs.getInt(getString(R.string.pref_key_gpsupdatetime), Integer.parseInt(getString(R.string.pref_key_gpsupdatetime_default))));
		
		readPrefShowEnableGPSSettingsDialog(prefs.getBoolean(getString(R.string.pref_key_showenablegpssettings), Boolean.parseBoolean(getString(R.string.pref_key_showenablegpssettings_default))));
		
		readPrefEnableGPSOnStart(prefs.getBoolean(getString(R.string.pref_key_gpsstartenabled), Boolean.parseBoolean(getString(R.string.pref_key_gpsstartenabled_default))));
	}
	
	@Override
	protected void onStart() {
		Log.d(LOGCAT, "onStart");
		super.onStart();
		
		snmSensorManager.registerListener(this, snsCompass, SensorManager.SENSOR_DELAY_NORMAL);

		declination = getDeclination(lnmLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		
		isGPSEnabled = lnmLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(!isGPSEnabled && showGPSSettingsDialog)
	    	showGPSDialog();
		
		setUIDependingOnGPSProvider();
	    
	    if(isGPSEnabled && tgbToggleGPS.isChecked())
	    	registerListenerLocationManager(true);
	}
	
	@Override
	protected void onResume() {
		Log.d(LOGCAT, "onResume");
		super.onResume();
		
		readPreferences();
	    
	    isGPSEnabled = lnmLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    
//	    if(isGPSEnabled && tgbToggleGPS.isChecked())
//	    	updateLatLon();
	}
	
	@Override
	protected void onPause() {
		Log.d(LOGCAT, "onPause");
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		Log.d(LOGCAT, "onStop");
		super.onStop();
	    
	    snmSensorManager.unregisterListener(this);
	    registerListenerLocationManager(false);
	    lat = 0.0;
	    lon = 0.0;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(LOGCAT, "onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putBoolean(KEY_WASGPSCHECKED, tgbToggleGPS.isChecked());
		
		
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(LOGCAT, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);

		showGPSSettingsDialog = false;
		wasGPSChecked = savedInstanceState.getBoolean(KEY_WASGPSCHECKED);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void toast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
	    switch(item.getItemId()){
	    	case R.id.mniGPSViewInMap:
	    		viewGPSOnMapActivity();
	    		return true;
	    		
	    	case R.id.mniGPSShare:
	    		shareLocationActivity();
	    		return true;
	    
	    	case R.id.mniAbout:
	    		Log.d(LOGCAT, "Lanzando Actividad About");
		        intent = new Intent(this, AboutActivity.class);
		        startActivity(intent);
		        animateUponLeaving();
		        return true;
		    
		    case R.id.mniSettings:
		    	Log.d(LOGCAT, "Lanzando Actividad Settings");
		    	intent = new Intent(this, SettingsActivity.class);
		        startActivity(intent);
		        animateUponLeaving();
		        return true;
		        
	        default:
	        	return super.onOptionsItemSelected(item);
	    }
	}
	
	private void animateUponLeaving() {
		overridePendingTransition(R.anim.fadeinrtl, R.anim.fadeoutrtl);
	}
	
	/***
	 * Calculates the declination (difference in degrees east from Magnetic North to True North) for a given <code>location</code>
	 * @param current <code>Location</code> to calculate declination for
	 * @return Declination as <code>float</code>, in degrees east of Magnetic North, for the given <code>location</code> 
	 */
	private float getDeclination(Location current) {
		float dec = 0;
		
		if(showTrueNorth && current != null) {
			Date now = new Date();
			GeomagneticField geoPos = new GeomagneticField(new Float(current.getLatitude()), new Float(current.getLongitude()), new Float(current.getAltitude()), now.getTime());
			dec = geoPos.getDeclination();
		}
		
		return dec;
	}
	
	private void showGPSDialog() {
		EnableGPSSettingsDialogFragment dialog = new EnableGPSSettingsDialogFragment();
    	dialog.show(getSupportFragmentManager(), "enablegpssettings");
	}

	public void toggleGPS(View view) {
		boolean isChecked = tgbToggleGPS.isChecked();
		boolean isGPSEnabled = lnmLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		if(isChecked)
		{
			wasGPSChecked = true;
			
			if(isGPSEnabled) {
				setUIGPSTempDetails(true);
				toggleGPSfields(true);
				registerListenerLocationManager(true);
			}
			else {
				pgbGPSSignal.setVisibility(View.VISIBLE);
				toast(getString(R.string.dialog_text));
				launchGPSSettingsActivity();
				
			}
		}
		else
		{
			toggleGPSfields(false);
			registerListenerLocationManager(false);
			wasGPSChecked = false;
			setUIGPSTempDetails(false);
		}
	}
	
	/***
	 * Toggles state of GPS-related fields
	 * @param isEnabled State to set to fields
	 */
	private void toggleGPSfields(boolean isEnabled) {
		lblLatTitle.setEnabled(isEnabled);
		lblLatNS.setEnabled(isEnabled);
		lblLatContent.setEnabled(isEnabled);
		if(!isEnabled) {
			lblLatNS.setText("");
			lblLatContent.setText("…");
		}
		lblLonTitle.setEnabled(isEnabled);
		lblLonWE.setEnabled(isEnabled);
		lblLonContent.setEnabled(isEnabled);
		if(!isEnabled) {
			lblLonWE.setText("");
			lblLonContent.setText("…");
		}
	}
	
	// The following method is required by the SensorEventListener interface;
	public void onAccuracyChanged(Sensor sensor, int accuracy) {    
	}

	// The following method is required by the SensorEventListener interface;
	// Hook this event to process updates;
	public void onSensorChanged(SensorEvent event) {
	    int azimuth = Math.round(event.values[0]);
	    // The other values provided by event.values[] are: 
	    //  float pitch = event.values[1];
	    //  float roll = event.values[2];
	    
	    rotateCompass(azimuth);
	    //lblCompDecl.setText(new GeomagneticField)
	}
	
	private void rotateCompass(int azimuth) {
		lblCompGeo.setText(getDegToGeo(azimuth+declination));
		lblCompDegs.setText(String.valueOf((int) (azimuth+declination)) + "°");
		
	    rotateCompassPicture(azimuth+declination);
	}
	
	/***
	 * Returns orientation depending on degrees
	 * @param azimuth Value between 0 and 359
	 * @return A 1- or 2-character String with "N", "NE", "E", "SE", "S", "SW", "W" or "NW" if input is in between 0 and 359, and "" otherwise.
	 */
	private String getDegToGeo(float azimuth) {
		if(azimuth < 23 || azimuth > 337)
			return "N";
		else if (azimuth > 22 && azimuth < 68)
			return "NE";
		else if (azimuth > 67 && azimuth < 113)
			return "E";
		else if (azimuth > 112 && azimuth < 158)
			return "SE";
		else if (azimuth > 157 && azimuth < 203)
			return "S";
		else if (azimuth > 202 && azimuth < 248)
			return "SW";
		else if (azimuth > 247 && azimuth < 293)
			return "W";
		else if (azimuth > 292 && azimuth < 338)
			return "NW";
		else
			return "";
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void rotateCompassPicture(float azimuth) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			imgCompass.setRotation(360 - azimuth);
	}
	
	//The following method is required by the LocationListener interface;
	public void onProviderDisabled(String provider) {
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			//setUIDependingOnGPSProvider();
			isGPSEnabled=false;
			tgbToggleGPS.setChecked(false);
			toggleGPSfields(false);
			setUIGPSTempDetails(false);
		}
	}

	//The following method is required by the LocationListener interface;	
	public void onProviderEnabled (String provider) {
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			//setUIDependingOnGPSProvider();
			isGPSEnabled=true;
		}
	}
	
	private void setUIDependingOnGPSProvider() {
		boolean isEnabled = isGPSEnabled ? wasGPSChecked : false;
		tgbToggleGPS.setChecked(isEnabled);
		toggleGPSfields(isEnabled);
		pgbGPSSignal.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
		lblGPSStatus.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
		lblGPSStatus.setTextColor(COLOR_YELLOW);
	}
	
	private void setUIGPSTempDetails(boolean areEnabled) {
		pgbGPSSignal.setVisibility(areEnabled ? View.VISIBLE : View.INVISIBLE);
		lblGPSStatus.setVisibility(areEnabled ? View.VISIBLE : View.INVISIBLE);
		lblGPSStatus.setTextColor(COLOR_YELLOW);
	}
	
	//The following method is required by the LocationListener interface;
	public void onStatusChanged (String provider, int status, Bundle extras) {
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			String txtGPSStatus = getString(R.string.gpsstatus);
			
			if(status == LocationProvider.AVAILABLE) {
				txtGPSStatus += ": " + getString(R.string.gpsstatus_ok);
				pgbGPSSignal.setVisibility(View.INVISIBLE);
				lblGPSStatus.setTextColor(COLOR_GREEN);
			} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
				txtGPSStatus += ": " + getString(R.string.gpsstatus_temp);
				pgbGPSSignal.setVisibility(View.VISIBLE);
				lblGPSStatus.setTextColor(COLOR_YELLOW);
			} else if (status == LocationProvider.OUT_OF_SERVICE) {
				txtGPSStatus += ": " + getString(R.string.gpsstatus_out);
				pgbGPSSignal.setVisibility(View.VISIBLE);
				lblGPSStatus.setTextColor(COLOR_RED);
			}
			
			//toast(txtGPSStatus);
			Log.i(LOGCAT, txtGPSStatus);
		}
	}
	
	//The following method is required by the LocationListener interface;
	public void onLocationChanged (Location location) {
		if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			pgbGPSSignal.setVisibility(View.INVISIBLE);
			lat = location.getLatitude();
			lon = location.getLongitude();
			updateLatLon();
			Log.d(LOGCAT, "Localización actualizada");
			declination = getDeclination(location);
		} else {
			toast("onLocationChanged, provider=NETWORK_PROVIDER");
			declination = getDeclination(location);
		}
	}
	
	/***
	 * Rounds a double to the desired number of decimal places
	 * @param unrounded Number to round
	 * @param precision Number of decimal places
	 * @return A <code>double</code> with <code>precision</code> decimal places
	 */
	public static double roundToPrecision(double unrounded, int precision)
	{
		java.math.BigDecimal bd = new java.math.BigDecimal(unrounded);
		java.math.BigDecimal rounded = bd.setScale(precision, java.math.BigDecimal.ROUND_HALF_UP);
		return rounded.doubleValue();
	}
	
	private void updateLatLon() {
		String[] latvalues, lonvalues;

		lblLatNS.setText(lat>0 ? "N" : "S");
		lblLonWE.setText(lon>0 ? "E" : "W");
		
		switch (GPS_format) {
			case GPS_DEGMINSEC:
				latvalues = decToDegMinSec(lat, GPS_DEGMINSEC);
				lblLatContent.setText(latvalues[0] + "° " + latvalues[1] + "\' " + latvalues[2] + "\"");
				lonvalues = decToDegMinSec(lon, GPS_DEGMINSEC);
				lblLonContent.setText(lonvalues[0] + "° " + lonvalues[1] + "\' " + lonvalues[2] + "\"");
				break;
			case GPS_DEGMIN:
				latvalues = decToDegMinSec(lat, GPS_DEGMIN);
				lblLatContent.setText(latvalues[0] + "° " + latvalues[1] + "\'");
				lonvalues = decToDegMinSec(lon, GPS_DEGMIN);
				lblLonContent.setText(lonvalues[0] + "° " + lonvalues[1] + "\'");
				break;
			case GPS_DEGONLY:
			default:
				latvalues = decToDegMinSec(lat, GPS_DEGONLY);
				lblLatContent.setText(latvalues[0] + "°");
				lonvalues = decToDegMinSec(lon, GPS_DEGONLY);
				lblLonContent.setText(lonvalues[0] + "°");
				break;
		}
		
		lblGPSStatus.setTextColor(COLOR_GREEN);
	}
	
	/***
	 * Converts decimal degrees (<code>dd.ddddd°</code>) to degrees, minutes and seconds (<code>dd° mm' ss.ss"</code>)
	 * @deprecated Use <code>decToDegMinSec (double dec, int degMode)</code> instead
	 * @param dec Decimal degrees
	 * @return A <code>String[]</code> of 3 positions:<ul><li>[0]: degrees</li><li>[1]: minutes</li><li>[2]: seconds</li></ul>
	 */
	private String[] decimalToDegMinSec(double dec) {
		int d=0, m=0;
		double t1=0.0, s=0.0;
		
		d = (int)dec;  // Truncate the decimals
		t1 = (dec - d) * 60;
		m = (int)t1;
		s = (t1 - m) * 60;
		
		s = roundToPrecision(s, 2);
		
		String[] res = {String.valueOf(d), String.valueOf(m), String.valueOf(s)};
		return res;
	}
	
	/***
	 * Converts decimal degrees (<code>dd.ddddd°</code>) to the format specified by <code>degMode</code>,
	 * being it either degrees, minutes and seconds (<code>dd° mm' ss.ss"</code>) or degrees and minutes (<code>dd° mm.mmmm'</code>) 
	 * @param dec Decimal degrees
	 * @param degMode an Integer value from the constants <code>GPS_DEGMINSEC</code>, <code>GPS_DEGMIN</code> or <code>GPS_DEGONLY</code>
	 * @return  A <code>String[]</code> of 3 positions:<ul><li>[0]: degrees (in all cases)</li><li>[1]: minutes (if <code>degMode = GPS_DEGMINSEC</code> or <code>degMode = GPS_DEGMIN</code>)</li><li>[2]: seconds (if <code>degMode = GPS_DEGMINSEC</code>)</li></ul>
	 */
	private String[] decToDegMinSec(double dec, int degMode) {
		String[] res = {"0", "0", "0"};
		final int secDecimals = 2;
		final int minDecimals = 4;
		final int degDecimals = 8;
		
		switch (degMode) {
			case GPS_DEGMINSEC:
				int d1=0, m1=0;
				double t1=0.0, s1=0.0;
				
				d1 = (int)dec;  // Truncate the decimals
				t1 = (dec - d1) * 60;
				m1 = (int)t1;
				s1 = (t1 - m1) * 60;
				
				s1 = roundToPrecision(s1, secDecimals);
				
				res[0] = String.valueOf(d1);
				res[1] = String.valueOf(m1);
				res[2] = String.valueOf(s1);
				break;
				
			case GPS_DEGMIN:
				int d2=0;
				double m2=0.0;
				
				d2 = (int)dec;  // Truncate the decimals
				m2 = (dec - d2) * 60;
				
				m2 = roundToPrecision(m2, minDecimals);
				
				res[0] = String.valueOf(d2);
				res[1] = String.valueOf(m2);
				break;
				
			case GPS_DEGONLY:
			default:
				res[0] = String.valueOf(roundToPrecision(dec, degDecimals));
				break;					
		}
		
	return res;
	}
	
	/***
	 * Converts decimal degrees (<code>dd.dddd°</code>) to degrees and minutes (<code>dd° mm.mm'</code>)
	 * @deprecated Use <code>decToDegMinSec (double dec, int degMode)</code> instead
	 * @param dec Decimal degrees
	 * @return A <code>String[]</code> of 2 positions:<ul><li>[0]: degrees</li><li>[1]: minutes</li></ul>
	 */
	private String[] decimalToDegMin(double dec) {
		int d=0;
		double m=0.0;
		
		d = (int)dec;  // Truncate the decimals
		m = (dec - d) * 60;
		
		m = roundToPrecision(m, 4);
		
		String[] res = {String.valueOf(d), String.valueOf(m)};
		return res;
	}
	
	private void registerListenerLocationManager(boolean enable) {
		if(enable){
			Log.d(LOGCAT, "Registrado proveedor de GPS");
			lnmLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_REFRESH_TIME * 1000, GPS_REFRESH_DISTANCE, this);
		}
		else {
			Log.d(LOGCAT, "Liberado proveedor de GPS");
			lnmLocationManager.removeUpdates(this);
		}
	}

	// Launch System Location settings
	private void launchGPSSettingsActivity() {
		Log.d(LOGCAT, "Lanzando actividad de Location Settings");
		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	private void dismissEnableGPSDialog(DialogFragment dialog, boolean showNextTime) {
		showGPSSettingsDialog=false;
		dialog.dismiss();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefs.edit().putBoolean(getString(R.string.pref_key_showenablegpssettings), showNextTime).commit();
	}
	
	// The following method is required by the EnableGPSSettingsDialogListener interface;
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, boolean showDialogNextTime) {
		launchGPSSettingsActivity();
		dismissEnableGPSDialog(dialog, showDialogNextTime);
	}
	
	// The following method is required by the EnableGPSSettingsDialogListener interface;
	@Override
	public void onDialogNegativeClick(DialogFragment dialog, boolean showDialogNextTime) {
		dismissEnableGPSDialog(dialog, showDialogNextTime);
		
	}
	
	private void viewGPSOnMapActivity() {
		if(lat != 0.0 && lon != 0.0) {
			String uri = String.format(java.util.Locale.ENGLISH, "geo:%f,%f", lat, lon);
			Intent viewIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
			try {
				startActivity(Intent.createChooser(viewIntent, getString(R.string.viewusing)));
			} catch (android.content.ActivityNotFoundException ANFe) {
				Toast.makeText(getApplicationContext(), ANFe.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			}
		} else
			toast(getString(R.string.waitingacquire));
	}
	
	private void shareLocationActivity() {
		if(lat != 0.0 && lon != 0.0) {
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_imat) + " " + getShareableLocation());
			shareIntent.setType("text/plain");
			
			try {
				startActivity(Intent.createChooser(shareIntent, getString(R.string.sharelocation)));
			} catch (android.content.ActivityNotFoundException ANFe) {
				toast(ANFe.getLocalizedMessage());
			}
		} else
			toast(getString(R.string.waitingacquire));
	}
	
	// Returns the current location in a human-readable format
	private String getShareableLocation() {
		String res = "";
		String[] latitude = decToDegMinSec(lat, GPS_format);
		String[] longitude = decToDegMinSec(lon, GPS_format);
		
		res += (lat>0) ? "N " : "S ";
		
		switch (GPS_format) {
			case GPS_DEGMINSEC:
				res += latitude[0] + "° " + latitude[1] + "\' " + latitude[2] + "\"";
				break;
			case GPS_DEGMIN:
				res += latitude[0] + "° " + latitude[1] + "\'";
				break;
			case GPS_DEGONLY:
			default:
				res += latitude[0] + "°";
				break;
		}
		
		res += ", ";
		
		res += (lon>0) ? "E " : "W ";
		
		switch (GPS_format) {
			case GPS_DEGMINSEC:
				res += longitude[0] + "° " + longitude[1] + "\' " + longitude[2] + "\"";
				break;
			case GPS_DEGMIN:
				res += longitude[0] + "° " + longitude[1] + "\'";
				break;
			case GPS_DEGONLY:
			default:
				res += longitude[0] + "°";
				break;
		}
		
		return res;
	}
	
	private void readPrefCompassShowTrueNorth (boolean showMagneticNorth) {
		showTrueNorth = !showMagneticNorth;
	}
	
	private void readPrefGPSDegreesFormat (String degFormatStr) {
		int degFormat = Integer.parseInt(degFormatStr);
		switch (degFormat) {
			case GPS_DEGONLY:
			case GPS_DEGMIN:
			case GPS_DEGMINSEC:
				GPS_format = degFormat;
				break;
			default:
				GPS_format = Integer.valueOf(getString(R.string.pref_key_gpsdegreesformat_default));
		}
	}
	
	private void readPrefGPSUpdateDistance (String updateDistanceStr) {
		int maxDistance = Integer.valueOf(getString(R.string.pref_key_gpsupdatedistance_max));
		int minDistance = Integer.valueOf(getString(R.string.pref_key_gpsupdatedistance_min));
		int defDistance = Integer.valueOf(getString(R.string.pref_key_gpsupdatedistance_default));
		
		try {
			int updateDistance = Integer.valueOf(updateDistanceStr);
			if (updateDistance <= maxDistance && updateDistance >= minDistance)
				GPS_REFRESH_DISTANCE = updateDistance;
			else
				GPS_REFRESH_DISTANCE = defDistance;
		} catch (NumberFormatException NFe) {
			GPS_REFRESH_DISTANCE = defDistance;
			throw new NumberFormatException("Could not parse stored value for GPS refresh distance in readPrefGPSUpdateDistance(String). Exception message is:\n" + NFe.getMessage());
		} finally {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			prefs.edit().putString(getString(R.string.pref_key_gpsupdatedistance), String.valueOf(GPS_REFRESH_DISTANCE)).commit();
		}
	}
	
	private void readPrefGPSUpdateDistance (int updateDistance) {
		int maxDistance = Integer.valueOf(getString(R.string.pref_key_gpsupdatedistance_max));
		int minDistance = Integer.valueOf(getString(R.string.pref_key_gpsupdatedistance_min));
		int defDistance = Integer.valueOf(getString(R.string.pref_key_gpsupdatedistance_default));

		if (updateDistance <= maxDistance && updateDistance >= minDistance)
			GPS_REFRESH_DISTANCE = updateDistance;
		else
			GPS_REFRESH_DISTANCE = defDistance;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefs.edit().putInt(getString(R.string.pref_key_gpsupdatedistance), GPS_REFRESH_DISTANCE).commit();
	}
	
	private void readPrefGPSUpdateTime (String updateTimeStr) {
		int maxTime = Integer.valueOf(getString(R.string.pref_key_gpsupdatetime_max));
		int minTime = Integer.valueOf(getString(R.string.pref_key_gpsupdatetime_min));
		int defTime = Integer.valueOf(getString(R.string.pref_key_gpsupdatetime_default));
		
		try {
			int updateTime = Integer.valueOf(updateTimeStr);
			if(updateTime <= maxTime && updateTime >= minTime)
				GPS_REFRESH_TIME = updateTime;
			else
				GPS_REFRESH_TIME = defTime;
		} catch (NumberFormatException NFe) {
			GPS_REFRESH_TIME = defTime;
			throw new NumberFormatException("Could not parse stored value for GPS refresh time in readPrefGPSUpdateTime(String). Exception message is:\n" + NFe.getMessage());
		} finally {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			prefs.edit().putString(getString(R.string.pref_key_gpsupdatetime), String.valueOf(GPS_REFRESH_TIME)).commit();
		}
	}
	
	private void readPrefGPSUpdateTime (int updateTime) {
		int maxTime = Integer.valueOf(getString(R.string.pref_key_gpsupdatetime_max));
		int minTime = Integer.valueOf(getString(R.string.pref_key_gpsupdatetime_min));
		int defTime = Integer.valueOf(getString(R.string.pref_key_gpsupdatetime_default));
		
		if(updateTime <= maxTime && updateTime >= minTime)
			GPS_REFRESH_TIME = updateTime;
		else
			GPS_REFRESH_TIME = defTime;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefs.edit().putInt(getString(R.string.pref_key_gpsupdatetime), GPS_REFRESH_TIME).commit();
	}
	
	private void readPrefShowEnableGPSSettingsDialog (boolean showDialog) {
		showGPSSettingsDialog = showDialog;
	}
	
	private void readPrefEnableGPSOnStart (boolean enableGPS) {
		wasGPSChecked = enableGPS;
	}
	
	/***
	 * Management of preferences which alter something is changed in runtime.
	 * Some preferences do not need to be managed here as they do not alter anything in runtime
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		
		if(key.equals(getString(R.string.pref_key_compassshowmagneticnorth))) {
			readPrefCompassShowTrueNorth(prefs.getBoolean(key, Boolean.parseBoolean(getString(R.string.pref_key_compassshowmagneticnorth_default))));
			
		} else if(key.equals(getString(R.string.pref_key_gpsdegreesformat))) {
			readPrefGPSDegreesFormat(prefs.getString(key, getString(R.string.pref_key_gpsdegreesformat_default)));
			updateLatLon();
			
		} else if(key.equals(getString(R.string.pref_key_gpsupdatedistance))) {
			readPrefGPSUpdateDistance(prefs.getString(key, getString(R.string.pref_key_gpsupdatedistance_default)));
			//readPrefGPSUpdateDistance(prefs.getInt(key, Integer.parseInt(getString(R.string.pref_key_gpsupdatedistance_default))));
			
		} else if(key.equals(getString(R.string.pref_key_gpsupdatetime))) {
			readPrefGPSUpdateTime(prefs.getString(key, getString(R.string.pref_key_gpsupdatetime_default)));
			//readPrefGPSUpdateTime(prefs.getInt(key, Integer.parseInt(getString(R.string.pref_key_gpsupdatetime_default))));
			
		} else if(key.equals(getString(R.string.pref_key_showenablegpssettings))) {
			readPrefShowEnableGPSSettingsDialog(prefs.getBoolean(key, Boolean.parseBoolean(getString(R.string.pref_key_showenablegpssettings_default))));
			
		}
	}
}
