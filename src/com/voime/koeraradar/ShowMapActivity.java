package com.voime.koeraradar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class ShowMapActivity extends MapActivity {

	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private MyOverlays itemizedoverlay;
	private MyLocationOverlay myLocationOverlay;
	private String maptype;
	private String rihma_nr;
	private GeoPoint my_position;
	private GeoPoint dog_position;
	
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main); // bind the layout to the activity

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		maptype = prefs.getString("maptype", null);
		rihma_nr = prefs.getString("number", null);
		
		Toast.makeText(getApplicationContext(), "Number on  " + rihma_nr, Toast.LENGTH_LONG).show();
		// Configure the Map
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		if (maptype.equals("2")){
			mapView.setSatellite(true);
		}else{
			mapView.setSatellite(false);			
		}

		mapController = mapView.getController();
		mapController.setZoom(14); // Zoon 1 is world view
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());

		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);

		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().animateTo(myLocationOverlay.getMyLocation());
			}
		});

		Drawable drawable = this.getResources().getDrawable(R.drawable.point);
		itemizedoverlay = new MyOverlays(this, drawable);
		createMarker();


	}
	@Override
	protected void onDestroy() {
	    super.onDestroy();

	    /*
	     * Notify the system to finalize and collect all objects of the
	     * application on exit so that the process running the application can
	     * be killed by the system without causing issues. NOTE: If this is set
	     * to true then the process will not be killed until all of its threads
	     * have closed.
	     */
	    System.runFinalizersOnExit(true);

	    /*
	     * Force the system to close the application down completely instead of
	     * retaining it in the background. The process that runs the application
	     * will be killed. The application will be completely created as a new
	     * application in a new process if the user starts the application
	     * again.
	     */
	    System.exit(0);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.enableCompass();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
		myLocationOverlay.disableCompass();
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
			createMarker();
			mapController.animateTo(point); // mapController.setCenter(point);
			my_position = point;
			Toast.makeText(getApplicationContext(), "liikus: " + point, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "Disable new provider " + provider,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "Enable provider " + provider,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Toast.makeText(getApplicationContext(), "StatusChanged", Toast.LENGTH_LONG).show();
		}
	}

	private void createMarker() {
		GeoPoint p = mapView.getMapCenter();
		OverlayItem overlayitem = new OverlayItem(p, "", "");
		itemizedoverlay.addOverlay(overlayitem);
		if (itemizedoverlay.size() > 0) {
			mapView.getOverlays().add(itemizedoverlay);
		}
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
			Toast.makeText(getApplicationContext(), "Seaded aktiveeruvad peale restarti!", Toast.LENGTH_LONG).show();
			openSettings();
			return true;
		case R.id.mypos:
			Toast.makeText(getApplicationContext(), "Minu positsioon: " + my_position, Toast.LENGTH_LONG).show();
			mapController.animateTo(my_position);
			return true;
		case R.id.dogpos:
			Toast.makeText(getApplicationContext(), "Koera positsioon: " + dog_position, Toast.LENGTH_LONG).show();
			//mapController.animateTo(my_position);
			return true;
		case R.id.send:
			Toast.makeText(getApplicationContext(), "Saadan sõnumi", Toast.LENGTH_LONG).show();
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
    
}