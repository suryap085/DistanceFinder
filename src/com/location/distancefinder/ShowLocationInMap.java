package com.location.distancefinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.location.distancefinder.R;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppAd.AdMode;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ShowLocationInMap extends FragmentActivity implements LocationListener {
	
	GoogleMap googleMap;
	private StartAppAd startAppAd;
		@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		startAppAd=new StartAppAd(ShowLocationInMap.this);
		if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.show_location);
       SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.showmap);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20000, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng).title(getIntent().getStringExtra("address")));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15),2000,null);
      
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	 if(getFragmentManager().getBackStackEntryCount() == 0) {
    	        super.onBackPressed();
    	    }
    	    else {
    	        getFragmentManager().popBackStack();
    	    }
    	 
    	 startAppAd.loadAd(AdMode.AUTOMATIC);
    }
}