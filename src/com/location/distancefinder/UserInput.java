package com.location.distancefinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.model.LatLng;
import com.location.adapterclasses.GooglePlacesAutocompleteAdapter;
import com.startapp.android.publish.StartAppAd;

public class UserInput extends Fragment {
	private AutoCompleteTextView first, second;
	private Button submit;
	ArrayList<LatLng> point;
	private StartAppAd startAppAd;
	private AdView adview;
	private InterstitialAd interstitialview;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.address_activity, container,
				false);
		startAppAd = new StartAppAd(getActivity());
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		adview = (AdView) view.findViewById(R.id.adView);
		first = (AutoCompleteTextView) view.findViewById(R.id.firstaddress);
		second = (AutoCompleteTextView) view.findViewById(R.id.secondaddress);
		first.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(),
				R.layout.list_item));
		second.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(),
				R.layout.list_item));
		submit = (Button) view.findViewById(R.id.submit);
		submit.setOnClickListener(submitlistener);
		second.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				InputMethodManager inputManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);

				inputManager.hideSoftInputFromWindow(getActivity()
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});
		point = new ArrayList<LatLng>();
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();

		getView().setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						Fragment fragment = new getLocation();
						FragmentManager fragmentManager = getFragmentManager();
						fragmentManager.beginTransaction()
								.replace(R.id.frame_container, fragment)
								.commit();
						return true;
					}
				}
				return false;
			}
		});
		// startAppAd.showAd();
		// startAppAd.showSlider(getActivity());

		// Request for Ads
		AdRequest adRequest = new AdRequest.Builder().build();
		adview.loadAd(adRequest);

	}

	OnClickListener submitlistener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			if (isInternetAvailable()) {
				try {
					point.add(getLatLng(first.getText().toString()));
					point.add(getLatLng(second.getText().toString()));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Intent i = new Intent(getActivity(), MainActivity.class);
				i.putExtra("point", point);
				i.putExtra("source", first.getText().toString());
				i.putExtra("destination", second.getText().toString());

				startActivity(i);
			} else {
				Toast.makeText(getActivity(),
						"Internet Not Available Please Check Network",
						Toast.LENGTH_LONG).show();
			}
			return;
		}
	};

	public boolean isInternetAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public LatLng getLatLng(String address) throws UnsupportedEncodingException {
		double lat, lng;
		String uri = "http://maps.google.com/maps/api/geocode/json?address="
				+ URLEncoder.encode(address, "UTF-8") + "&sensor=false";
		HttpGet httpGet = new HttpGet(uri);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(stringBuilder.toString());

			lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lng");

			lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
					.getJSONObject("geometry").getJSONObject("location")
					.getDouble("lat");
			LatLng point = new LatLng(lat, lng);
			Log.e("address is-------------------------------------------------",
					point.toString());

			return point;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

	}

}
