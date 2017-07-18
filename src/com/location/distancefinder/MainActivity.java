package com.location.distancefinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.location.distancefinder.R;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppAd.AdMode;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	GoogleMap map, placemap;
	ArrayList<LatLng> markerPoints;
	TextView tvDistanceDuration;
	ArrayList<LatLng> points;
	private EditText edtplace;
	private Button btnplace,btndestPlace;
	HashMap<String, String> mMarkerPlaceLink = new HashMap<String, String>();
	private LinearLayout linear, linear1;
	Intent obj;
	private StartAppAd startAppAd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startAppAd=new StartAppAd(MainActivity.this);
		edtplace = (EditText) findViewById(R.id.edplace);
		edtplace.setOnClickListener(typeSearchListener);
		btndestPlace=(Button)findViewById(R.id.btndestPlace);
		btndestPlace.setOnClickListener(destinationbuttonListener);
		btnplace = (Button) findViewById(R.id.btnPlace);
		points = new ArrayList<LatLng>();
		btnplace.setOnClickListener(buttonListener);
		tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);
		linear1 = (LinearLayout) findViewById(R.id.liner1);
		linear = (LinearLayout) findViewById(R.id.liner2);
		obj = getIntent();
		points = obj.getParcelableArrayListExtra("point");
        
		markerPoints = new ArrayList<LatLng>();

		// Getting reference to SupportMapFragment of the activity_main
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		SupportMapFragment fm1 = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.placemap);

		// Getting Map for the SupportMapFragment
		map = fm.getMap();
		placemap = fm1.getMap();
		// Enable MyLocation Button in the Map
		map.setMyLocationEnabled(true);
		map.setTrafficEnabled(true);

		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

//		CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(
//				points.get(0), 5);
//		map.animateCamera(yourLocation);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 10));
		map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

		placemap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker arg0) {
				Intent intent = new Intent(getBaseContext(),
						PlaceDetailsActivity.class);
				String reference = mMarkerPlaceLink.get(arg0.getId());
				intent.putExtra("reference", reference);

				// Starting the Place Details Activity
				startActivity(intent);
			}
		});

		// Setting onclick event listener for the map

		for (int i = 0; i < points.size(); i++) {
			// Already two locations
			if (markerPoints.size() > 1) {
				markerPoints.clear();
				map.clear();
			}
			// Adding new item to the ArrayList
			markerPoints.add(points.get(i));

			// Creating MarkerOptions
			MarkerOptions options = new MarkerOptions();

			// Setting the position of the marker
			options.position(points.get(i));

			/**
			 * For the start location, the color of marker is GREEN and for the
			 * end location, the color of marker is RED.
			 */
			if (markerPoints.size() == 1) {
				options.icon(
						BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
						.title(obj.getStringExtra("source"));
			} else if (markerPoints.size() == 2) {
				options.icon(
						BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_RED))
						.title(obj.getStringExtra("destination"));
			}

			// Add new marker to the Google Map Android API V2
			if (options.getPosition() != null) {
				map.addMarker(options);
			} else {
				return;
			}

			// Checks, whether start and end locations are captured
			if (markerPoints.size() >= 2) {
				LatLng origin = markerPoints.get(0);
				LatLng dest = markerPoints.get(1);

				// Getting URL to the Google Directions API
				String url = getDirectionsUrl(origin, dest);

				DownloadTask downloadTask = new DownloadTask();

				// Start downloading json data from Google Directions API
				downloadTask.execute(url);
			}
		}

	}
	
	OnClickListener typeSearchListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			searchCitiesList();
		}
	};
	
	public void searchCitiesList() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.type_list_view);
        dialog.setTitle("Search Types");
        final ListView listView = (ListView) dialog.findViewById(R.id.list);
        dialog.show();
       listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                 String itemValue = (String) listView
                        .getItemAtPosition(position);

                edtplace.setText(itemValue);
                dialog.cancel();

            }

        });
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(internetReciever, filter);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unregisterReceiver(internetReciever);
	}

	BroadcastReceiver internetReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getExtras() != null) {
				NetworkInfo ni = (NetworkInfo) intent.getExtras().get(
						ConnectivityManager.EXTRA_NETWORK_INFO);
				if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
					// Toast.makeText(getApplicationContext(),
					// "Now Connected with with Internet",
					// Toast.LENGTH_LONG).show();
				} else
					Toast.makeText(getApplicationContext(),
							"Internet Not Available Please Check Network",
							Toast.LENGTH_LONG).show();

			}

		}
	};

	private String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	private String downloadplaceUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();

				// Starts parsing data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		// Executes in UI thread, after the parsing process
		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;
			MarkerOptions markerOptions = new MarkerOptions();
			String distance = "";
			String duration = "";

			if (result.size() < 1) {
				Toast.makeText(getBaseContext(),
						"Can't get Points please try with Proper Search Place",
						Toast.LENGTH_LONG).show();

			}

			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();

				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					if (j == 0) { // Get distance from the list
						distance = (String) point.get("distance");
						continue;
					} else if (j == 1) { // Get duration from the list
						duration = (String) point.get("duration");
						continue;
					}

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				// Adding all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(15);
				lineOptions.color(Color.RED);
			}

			tvDistanceDuration.setText("Distance:" + distance + ", Duration:"
					+ duration);

			// Drawing polyline in the Google Map for the i-th route
			map.addPolyline(lineOptions);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

	OnClickListener buttonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			linear.setVisibility(View.GONE);
			linear1.setVisibility(View.VISIBLE);
			placemap.clear();
			placemap.setMyLocationEnabled(true);
			placemap.setTrafficEnabled(true);
            placemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			placemap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 10));
			placemap.animateCamera(CameraUpdateFactory.zoomTo(10));
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

			inputManager.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			String type = edtplace.getText().toString();
			LatLng latlng = points.get(0);
			Log.e("cordinates---------------------------------------------",
					latlng.latitude + "," + latlng.longitude);
			StringBuilder sb = new StringBuilder(
					"https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
			sb.append("location=" + latlng.latitude + "," + latlng.longitude);
			sb.append("&radius=3000");
			try {
				sb.append("&types=" + URLEncoder.encode(type, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append("&key=AIzaSyDn83I9K7jIASD8KHGvWY0OnGKJSwp6gzA");
			sb.append("&sensor=true");

			// Creating a new non-ui thread task to download Google place json
			// data
			PlacesTask placesTask = new PlacesTask();

			// Invokes the "doInBackground()" method of the class PlaceTask
			placesTask.execute(sb.toString());
		}
	};
	
	OnClickListener destinationbuttonListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			linear.setVisibility(View.GONE);
			linear1.setVisibility(View.VISIBLE);
			placemap.clear();
			placemap.setMyLocationEnabled(true);
			placemap.setTrafficEnabled(true);
            placemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			placemap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(1), 10));
			placemap.animateCamera(CameraUpdateFactory.zoomTo(10));
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

			inputManager.hideSoftInputFromWindow(getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			String type = edtplace.getText().toString();
			LatLng latlng = points.get(1);
			Log.e("cordinates---------------------------------------------",
					latlng.latitude + "," + latlng.longitude);
			StringBuilder sb = new StringBuilder(
					"https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
			sb.append("location=" + latlng.latitude + "," + latlng.longitude);
			sb.append("&radius=3000");
			try {
				sb.append("&types=" + URLEncoder.encode(type, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append("&key=AIzaSyDn83I9K7jIASD8KHGvWY0OnGKJSwp6gzA");
			sb.append("&sensor=true");

			// Creating a new non-ui thread task to download Google place json
			// data
			PlacesTask placesTask = new PlacesTask();

			// Invokes the "doInBackground()" method of the class PlaceTask
			placesTask.execute(sb.toString());
		}
	};

	private class PlacesTask extends AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadplaceUrl(url[0]);
				Log.d("data is---------------------------------", data);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {
			plaecParserTask placeparserTask = new plaecParserTask();

			placeparserTask.execute(result);
		}
	}

	private class plaecParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a List construct */
				places = placeJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return places;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {

			// Clears all the existing markers

			placemap.clear();
		   Log.e("hello-----------------------------------------",
					"placeparsetask");
			Log.e("hello-----------------------------------------",
					"" + list.size());
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
       
					// Creating a marker
					MarkerOptions markerOptions = new MarkerOptions();

					// Getting a place from the places list
					HashMap<String, String> hmPlace = list.get(i);

					// Getting latitude of the place
					double lat = Double.parseDouble(hmPlace.get("lat"));

					// Getting longitude of the place
					double lng = Double.parseDouble(hmPlace.get("lng"));
					Log.e("latlng is----------------------------------------------------",
							lat + "," + lng);
					// Getting name
					String name = hmPlace.get("place_name");

					// Getting vicinity
					String vicinity = hmPlace.get("vicinity");

					LatLng latLng = new LatLng(lat, lng);

					// Setting the position for the marker
					markerOptions.position(latLng);

					// Setting the title for the marker.
					// This will be displayed on taping the marker
					markerOptions.title(name + " : " + vicinity);

					// Placing a marker on the touched position
					Marker m = placemap.addMarker(markerOptions);

					// Linking Marker id and place reference
					mMarkerPlaceLink.put(m.getId(), hmPlace.get("reference"));
				}
			} else {
				Toast.makeText(MainActivity.this,
						"No Data Found Please Try Another String",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
