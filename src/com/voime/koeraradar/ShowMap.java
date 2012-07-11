package com.voime.koeraradar;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;




public class ShowMap extends MapActivity {
    public static final String PREFS_NAME = "koeraradar";
	public MapController mapController;
	private static MapView mapView;
	private LocationManager locationManager;
	private static MyOverlays myoverlay;
	private MyLocationOverlay myLocationOverlay;
	private static DogOverlay dogoverlay;
	private MyLocationOverlay dogLocationOverlay;	
	
	public static DogOverlay itemizedoverlay;
	public static OverlayItem dog_overlayitem;
	
	public GeoPoint my_position;
	public GeoPoint dog_position;
	public String rihma_nr;
	public String maptype;
	private String sms = "?LOC";
	private String sms_start = "!LOC";
	private String re_sms2 = "!LOC_01/01_NORM_095%_GPS_1_N58.31.53,0_E026.52.31,1_18.07.2011_11:27:11_000KM/H_000DEG_*JAb*JEL*IAA/AAAZn*EF4GAAEABej/AADAA9cIAADAA8DqAAAAA8cIAAAAA8";
	private String re_sms = "!LOC_01/01_NORM_096%_GPS_1_N58.53.29,7_E026.16.22,4_26.02.2011_08:16:05_000KM/H_000DEG_*JAS*JEA*IAA/AAAKP";
	
	private int sms_jrk;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main); // bind the layout to the activity
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		//maptype = prefs.getString("maptype", null);
		rihma_nr = prefs.getString("number", null);
		Toast.makeText(getApplicationContext(), "Number on  " + rihma_nr, Toast.LENGTH_LONG).show();
		// Configure the Map

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		// see on tõstetud onresume alla
		/*
		if (maptype.equals("2")){
			mapView.setSatellite(true);
		}else{
			mapView.setSatellite(false);			
		}
		*/
		
		mapController = mapView.getController();
		
		// Restore preferences
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    int zoom = settings.getInt("zoom", 14);
		mapController.setZoom(zoom); // Zoon 1 is world view
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(
						myLocationOverlay.getMyLocation());
			}
		});
		
		my_position = myLocationOverlay.getMyLocation();
		Drawable mydrawable = this.getResources().getDrawable(R.drawable.point);
		myoverlay = new MyOverlays(this, mydrawable);
		createMarker();

		// üritan sama asja teha koera punkti panekuks
		Drawable dogdrawable = this.getResources().getDrawable(R.drawable.dog);
		dogoverlay = new DogOverlay(dogdrawable,this);
		
		// loen sõnumitest koerad peale
		readDogsSMS();
		
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public class GeoUpdateHandler implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			//createMarker();
			//mapController.animateTo(point); // mapController.setCenter(point);
			my_position = point;
			//Toast.makeText(getApplicationContext(), "liikus: " + point, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	private void createMarker() {
		GeoPoint p = mapView.getMapCenter();
		OverlayItem overlayitem = new OverlayItem(p, "", "");
		myoverlay.addOverlay(overlayitem);
		if (myoverlay.size() > 0) {
			mapView.getOverlays().add(myoverlay);
		}
	}
	private void dogMarker(GeoPoint point, String title, String snippet) {
		OverlayItem dogoverlayitem = new OverlayItem(point, title, snippet);
		dogoverlay.addOverlay(dogoverlayitem);
		if (dogoverlay.size() > 0) {
			mapView.getOverlays().add(dogoverlay);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String maptype = prefs.getString("maptype", null);
		Boolean compass = prefs.getBoolean("compass", true);
		if (compass) {
			myLocationOverlay.enableCompass();
		}
		//Toast.makeText(getApplicationContext(), "onResume " + maptype, Toast.LENGTH_LONG).show();
		// Configure the Map
		
		if (maptype.equals("2")){
			mapView.setSatellite(true);
		}else{
			mapView.setSatellite(false);			
		}
		
	}
	@Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putInt("zoom", mapView.getZoomLevel());
      // Commit the edits!
      editor.commit();
    }
	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableCompass();
	}
	/*
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    System.runFinalizersOnExit(true);
	    System.exit(0);
	}
	*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			openSettings();
			return true;
		case R.id.mypos:
			if (my_position == null) {
				Toast.makeText(getApplicationContext(), "Ma ei tea oma asukohta, lülita GPS sisse!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(getApplicationContext(), "Mina asun: " + my_position, Toast.LENGTH_LONG).show();
				mapController.animateTo(my_position);
			}
			return true;
		case R.id.dogpos:
			if (dog_position == null) {
				Toast.makeText(getApplicationContext(), "Ei ole teada, vajuta enne saada nuppu!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(getApplicationContext(), "Koera asukoht: " + dog_position, Toast.LENGTH_LONG).show();
				mapController.animateTo(dog_position);				
			}
			return true;
		case R.id.send:
			sendSms();
			//Toast.makeText(getApplicationContext(), "Saadan sõnumi", Toast.LENGTH_LONG).show();
			return true;
		case R.id.exit:
			this.finish();
			this.moveTaskToBack(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	private void openSettings() {
            Intent settingsActivity = new Intent(getBaseContext(),
                            Preferences.class);
            startActivity(settingsActivity);
    }
	private void sendSms() {
		CharSequence text;
		if (rihma_nr == null) {			
			rihma_nr = "+37253268656";
			text = "Number on määramata, kasutan vaikimisi numbrit " + rihma_nr;
		} else {
			text = "Saadan sõnumi " + rihma_nr;
		}
		// popupi näitamine
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
		
		// sõnumi saatmine
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(rihma_nr, null, sms, null, null);
		//Toast.makeText(getApplicationContext(), "Saabund sõnum: " + re_sms, Toast.LENGTH_LONG).show();
	    
		
	    //dog_position = extractCoords(re_sms);
		/*
		int lat = (int) ((58+Math.random()) * 1E6);
		int lng = (int) ((26+Math.random()) * 1E6);
		dog_position = new GeoPoint(lat,lng);
		sms_jrk++;
		String title = "Koer jrk:" + sms_jrk;
		String snippet = "lat " + lat + " lng " + lng;
	    dogMarker(dog_position,title,snippet);
		mapController.animateTo(dog_position);
	    
		/*
		String teade = "Koer jrk:" + sms_jrk;
		sms_jrk++;
		setDog(re_sms,teade,re_sms);
		*/
		
		
		
	}
	 public List<String> getSms() {
	        Uri mSmsQueryUri = Uri.parse("content://sms/inbox");
	        List<String> messages = new ArrayList<String>();
	        Cursor cursor = null;
	        try {
	            cursor = getApplicationContext().getContentResolver().query(mSmsQueryUri, null, null, null, null);
	            if (cursor == null) {
	                return messages;
	            }

	            for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {
	                final String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
	                messages.add(body);
	            }
	        } catch (Exception e) {

	        } finally {
	            cursor.close();
	        }
	        return messages;
	}
	
	public void readDogsSMS(){
		// sõnumite lugemine postkastist
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
		while (cursor.moveToNext()) {
		  // Retrieve sms
		  // see column "address" for comparing
			String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
			String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
			String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
			String read = cursor.getString(cursor.getColumnIndexOrThrow("read"));
			boolean sisu=body.startsWith(sms_start);
			if (sisu){
				dog_position = extractCoords(body);
				int lat = dog_position.getLatitudeE6();
				int lng = dog_position.getLongitudeE6(); 
				sms_jrk++;
				String title = "Koer" + date + ":" + read + ":" + status + ":" + sms_jrk;
				String snippet = "lat " + lat + " lng " + lng;
			    dogMarker(dog_position,title,snippet);
				mapController.animateTo(dog_position);
			}	
			//Toast.makeText(getApplicationContext(), address + rihma_nr, Toast.LENGTH_LONG).show();
		  // Then update the sms and set the column "read" to 1
		}
	}
	 
	public void setDog(String sms, String title, String snippet){
		dog_position = extractCoords(sms);
	    dogMarker(dog_position,title,snippet);
		mapController.animateTo(dog_position);
	}
	private int getMicroDegrees(String coord) {
		int m = 1000000;

		int firstPoint = coord.indexOf(".");
		int secondPoint = coord.indexOf(".", firstPoint + 1);
		int comma = coord.indexOf(",");

		String sdegrees = coord.substring(0, firstPoint);
		String sminutes = coord.substring(firstPoint + 1, secondPoint);
		String sseconds1 = coord.substring(secondPoint + 1, comma);
		String sseconds2 = coord.substring(comma + 1, comma + 2);
		int degrees = Integer.valueOf(sdegrees) * m;
		int minutes = Integer.valueOf(sminutes) * m / 60;
		int seconds = (int) (Double.valueOf(sseconds1 + "." + sseconds2) * m / (60 * 60));

		int microDegrees = degrees + minutes + seconds;

		return microDegrees;
	}
	private GeoPoint extractCoords(String loc) {
		int latitudeE6;
		int longitudeE6;

		int startLat = loc.indexOf("GPS_1_N") + 7;
		int endLat = startLat + 10;
		int startLon = loc.indexOf("_E") + 2;
		int endLon = startLon + 11;


		String sLat = loc.substring(startLat, endLat);
		String sLon = loc.substring(startLon, endLon);

		latitudeE6 = getMicroDegrees(sLat);
		longitudeE6 = getMicroDegrees(sLon);

		GeoPoint point = new GeoPoint(latitudeE6, longitudeE6);
		return point;
	}
}