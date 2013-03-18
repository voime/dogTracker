package com.voime.koeraradar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;




public class ShowMap extends MapActivity {
    
	public static final String PREFS_NAME = "koeraradar";
	public MapController mapController;
	MapView mapView = null;
	private LocationManager locationManager;
	private MyLocationOverlay myLocationOverlay;
	private static DogOverlay dogoverlay;
	
	public static DogOverlay itemizedoverlay;
	public static OverlayItem dog_overlayitem;
	
	public GeoPoint my_position;
	public GeoPoint dog_position;
	public String rihma_nr;
	public String maptype;
	private String sms_start = "!LOC";
	public boolean follow_me = true;
	
	IntentFilter intentFilter;
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
		        @Override
		        public void onReceive(Context context, Intent intent) {
		        	updateDogs();
		        	Toast.makeText(getApplicationContext(), "Saabus sõnum ja uuendan asukohta!", Toast.LENGTH_LONG).show();
		        }
	};
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main); // bind the layout to the activity
        //---intent to filter for SMS messages received---
		Drawable dogdrawable;
		intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RE");

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String dogicon = prefs.getString("dogicon", "1");
		rihma_nr = prefs.getString("number", null);
		if (rihma_nr == null){
			Toast.makeText(getApplicationContext(), "Määra seadete alt rihma number", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(getApplicationContext(), "Number on  " + rihma_nr, Toast.LENGTH_LONG).show();
		}
		// Configure the Map

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
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

		// üritan sama asja teha koera punkti panekuks
		if (dogicon.equals("3")){
			dogdrawable = this.getResources().getDrawable(R.drawable.ic_launcher);
		}else if (dogicon.equals("2")){
			dogdrawable = this.getResources().getDrawable(R.drawable.point);			
		}else{
			dogdrawable = this.getResources().getDrawable(R.drawable.dog);
		}
		//Toast.makeText(getApplicationContext(), "Ikoon  " + dogicon, Toast.LENGTH_LONG).show();
		dogoverlay = new DogOverlay(dogdrawable,this);		
		registerReceiver(intentReceiver, intentFilter);
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
			if (follow_me) {
				mapController.animateTo(point); // mapController.setCenter(point);	
			}
			my_position = point;
			//Toast.makeText(getApplicationContext(), "liikus: " + point, Toast.LENGTH_LONG).show();
			TextView txt = (TextView)findViewById(R.id.MyTextView);			
			if (dog_position != null) {
				CharSequence sisu = "Kaugus koerani " + getDistance(my_position, dog_position) + " meetrit";
				txt.setText(sisu);
			}else{
				CharSequence sisu = "Kaugust ei saa arvutada";
				txt.setText(sisu);
			}
			
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

	private void dogMarker(GeoPoint point, String title, String snippet) {
		OverlayItem dogoverlayitem = new OverlayItem(point, title, snippet);
		dogoverlay.addOverlay(dogoverlayitem);
		if (dogoverlay.size() > 0) {
			mapView.getOverlays().add(dogoverlay);
		}
	}
	@Override
	protected void onResume() {
		 //---register the receiver---
       // registerReceiver(intentReceiver, intentFilter);
        super.onResume();
        myLocationOverlay.enableMyLocation();
		
        SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String maptype = prefs.getString("maptype", "1");
		Boolean compass = prefs.getBoolean("compass", true);
		if (compass) {
			myLocationOverlay.enableCompass();
		}
		
		if (maptype.equals("2")){
			mapView.setSatellite(true);
		}else{
			mapView.setSatellite(false);			
		}
		
		updateDogs();
		
	}
	@Override
    protected void onStop(){
       super.onStop();
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putInt("zoom", mapView.getZoomLevel());
      editor.commit();
    }
	@Override
	protected void onPause() {
        //---unregister the receiver---
     //   unregisterReceiver(intentReceiver);
		super.onPause();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableCompass();
	}
	@Override
	protected void onDestroy() {
		unregisterReceiver(intentReceiver);
		super.onDestroy();
	    System.runFinalizersOnExit(true);
	    System.exit(0);
	}
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
			follow_me = true;
			return true;
		case R.id.dogpos:
			if (dog_position == null) {
				Toast.makeText(getApplicationContext(), "Ei ole teada, vajuta enne SMS nuppu!", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(getApplicationContext(), "Koera asukoht: " + dog_position, Toast.LENGTH_LONG).show();
				mapController.animateTo(dog_position);				
			}
			follow_me = false;
			return true;
		case R.id.send:
			openSMS();
			//sendSms();
			return true;
		case R.id.about:
			openAbout();
			//this.finish();
			//this.moveTaskToBack(true);
			return true;
		case R.id.refresh:
			updateDogs();
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
	private void openAbout() {
        Intent aboutActivity = new Intent(getBaseContext(),
                        About.class);
        startActivity(aboutActivity);
	}
	private void openSMS() {
        Intent SMSActivity = new Intent(getBaseContext(),
        		Sendsms.class);
        startActivity(SMSActivity);
	}
	public void updateDogs(){
		mapView.getOverlays().remove(dogoverlay);
		//mapView.invalidate();
		readDogsSMS();
	}
	public void readDogsSMS(){
		boolean is_point = true;
		String title = null;
		String snippet = null;
		int sms_jrk = 0;
		GeoPoint old_position = null;
		
		//mapView.getOverlays().remove();
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Boolean track_line = prefs.getBoolean("track_line", false);
		// sõnumite lugemine postkastist
		Uri uri = Uri.parse("content://sms/inbox");
		Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
		while (cursor.moveToNext()) {
		  // Retrieve sms
		  // see column "address" for comparing
			String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
			Long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
			//String read = cursor.getString(cursor.getColumnIndexOrThrow("read"));
			// read = 0-lugemata 1-loetud
			boolean sisu=body.startsWith(sms_start);
			boolean sisu1=body.startsWith("!TRC"); // näitab ka mitmiksõnumit
			if (sisu || sisu1){
				DateFormat formatter = new SimpleDateFormat("dd.MM.yy hh:mm:ss");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(date);
				String kuup = formatter.format(calendar.getTime());
				GeoPoint tmp_position = extractCoords(body);
				int lat = tmp_position.getLatitudeE6();
				int lng = tmp_position.getLongitudeE6(); 
				snippet = kuup + "\n" + "lat " + lat + " lng " + lng;
				if (my_position != null) {						
					int distance = getDistance(tmp_position,my_position);
					snippet += "\nKaugus on " + distance  + " meetrit";
				}else{
					snippet += "\nKaugus puudub";	
				}
				// joonistan esimese punkti koera
				if (is_point) {
				    is_point = false;
					title = "Koera asukoht";
				    dog_position=tmp_position;
					dogMarker(dog_position,title,snippet);
				    mapController.animateTo(dog_position);
				    old_position=tmp_position;
				}else{
				// kui veel siis joonistan ka jooned
					sms_jrk++;
					title = "Koer jrk: " + sms_jrk;
					if (track_line){
						// joonista joon, aga praegu ei oska ja panen lihtsalt koera markeri
						mapView.getOverlays().add(new DirectionPathOverlay(old_position, tmp_position));
						old_position=tmp_position;

					}
				}
			}	
			
			
			//Toast.makeText(getApplicationContext(), address + rihma_nr, Toast.LENGTH_LONG).show();
		  // Then update the sms and set the column "read" to 1
		}

	}
	public int getDistance(GeoPoint start, GeoPoint end){
		int lat = start.getLatitudeE6();
		int lng = start.getLongitudeE6(); 		
		int lat1 = end.getLatitudeE6();
		int lng1 = end.getLongitudeE6(); 
		Location locationA = new Location("point A");
		locationA.setLatitude(Double.valueOf(lat)/1e6);
		locationA.setLongitude(Double.valueOf(lng)/1e6);
		Location locationB = new Location("point B");
		locationB.setLatitude(Double.valueOf(lat1)/1e6);
		locationB.setLongitude(Double.valueOf(lng1)/1e6);
		float distance = locationA.distanceTo(locationB);
		return (int) distance;
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