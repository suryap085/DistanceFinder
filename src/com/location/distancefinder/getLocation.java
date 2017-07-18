package com.location.distancefinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.location.LocationListener;
import com.location.distancefinder.R;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppAd.AdMode;
import com.startapp.android.publish.video.VideoListener;
import com.startapp.android.publish.StartAppSDK;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class getLocation extends Fragment implements
		android.location.LocationListener {

	private TextView TextViewShowLocation, txtviewAddress;
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	Location location;
	private Button getlocationbtn;
	private TransparentProgressDialog pd;
	private Handler h;
	private Runnable r;
	String bestProvider, str;
	Criteria criteria;
	Geocoder geocoder;
	List<Address> addresses;
	Address returnedAddress;
	StringBuilder strReturnedAddress;
	Context ctx;
	private StartAppAd startAppAd;
	private AdView adview;
	private InterstitialAd interstitialview;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// StartAppSDK.init(getActivity(), "106447526", "206846511");
		startAppAd = new StartAppAd(getActivity());
		View view = inflater.inflate(R.layout.activity_get_location, container,
				false);
		geocoder = new Geocoder(getActivity());
		adview = (AdView) view.findViewById(R.id.adView);
		TextViewShowLocation = (TextView) view.findViewById(R.id.lblLocation);
		txtviewAddress = (TextView) view.findViewById(R.id.lblAddress);
		getlocationbtn = (Button) view.findViewById(R.id.btnShowLocation);
		getlocationbtn.setOnClickListener(showLocation);

		return view;

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		startAppAd.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		h = new Handler();
		pd = new TransparentProgressDialog(getActivity(), R.drawable.spinner);
		pd.show();
		r = new Runnable() {
			@Override
			public void run() {
				if (pd.isShowing()) {
					pd.dismiss();
				}
			}
		};

	}

	private class TransparentProgressDialog extends Dialog {

		private ImageView iv;

		public TransparentProgressDialog(Context context, int resourceIdOfImage) {
			super(context, R.style.TransparentProgressDialog);
			WindowManager.LayoutParams wlmp = getWindow().getAttributes();
			wlmp.gravity = Gravity.CENTER_HORIZONTAL;
			getWindow().setAttributes(wlmp);
			setTitle(null);
			setCancelable(false);
			setOnCancelListener(null);
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			iv = new ImageView(context);
			iv.setImageResource(resourceIdOfImage);
			layout.addView(iv, params);
			addContentView(layout, params);
		}

		@Override
		public void show() {
			super.show();
			RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
					Animation.RELATIVE_TO_SELF, .5f,
					Animation.RELATIVE_TO_SELF, .5f);
			anim.setInterpolator(new LinearInterpolator());
			anim.setRepeatCount(Animation.INFINITE);
			anim.setDuration(3000);
			iv.setAnimation(anim);
			iv.startAnimation(anim);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		AdRequest adRequest = new AdRequest.Builder().build();
		adview.loadAd(adRequest);

		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, true);
		location = locationManager.getLastKnownLocation(bestProvider);
		if (location != null) {
			onLocationChanged(location);

		}
		locationManager.requestLocationUpdates(bestProvider, 20000, 0, this);

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startAppAd.showAd();
		// startAppAd.onResume();
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setCostAllowed(false);
		bestProvider = locationManager.getBestProvider(criteria, true);
		if (!locationManager
				.isProviderEnabled(locationManager.NETWORK_PROVIDER)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Location Services Not Active");
			builder.setMessage("Please enable Location Services and GPS");
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface,
								int i) {
							// Show location settings when the user acknowledges
							// the alert dialog
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					});
			Dialog alertDialog = builder.create();
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
		}
		location = locationManager
				.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
		if (location != null) {
			onLocationChanged(location);

		} else {
			h.postDelayed(r, 5000);
		}

		locationManager.requestLocationUpdates(
				locationManager.NETWORK_PROVIDER, 20000, 0, this);

	}

	@Override
	public void onLocationChanged(Location location) {
		// TextViewShowLocation = (TextView) findViewById(R.id.lblLocation);

		h.postDelayed(r, 5000);

		try {
			List<Address> loc = geocoder.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);
			TextViewShowLocation.setText("Latitude:" + loc.get(0).getLatitude()
					+ ", Longitude:" + loc.get(0).getLongitude());
			getAddressFromLocation(location, getActivity(),
					new GeocoderHandler());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d("Latitude", "disable");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d("Latitude", "enable");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d("Latitude", "status");
	}

	OnClickListener showLocation = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent i = new Intent(getActivity(), ShowLocationInMap.class);
			i.putExtra("address", str);
			startActivity(i);
		}
	};

	public void onDestroyView() {
		h.removeCallbacks(r);
		if (pd.isShowing()) {
			pd.dismiss();
		}
		super.onDestroyView();
	}

	public static void getAddressFromLocation(final Location location,
			final Context context, final Handler handler) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				if (context != null) {
					Geocoder geocoder = new Geocoder(context,
							Locale.getDefault());

					StringBuilder strReturnedAddress = null;
					try {
						List<Address> list = geocoder.getFromLocation(
								location.getLatitude(),
								location.getLongitude(), 1);
						if (list != null && list.size() > 0) {
							Address returnedAddress = list.get(0);
							strReturnedAddress = new StringBuilder();

							for (int i = 0; i < returnedAddress
									.getMaxAddressLineIndex(); i++) {
								strReturnedAddress.append(
										returnedAddress.getAddressLine(i))
										.append("\n");
							}
						}
					} catch (IOException e) {

					} finally {
						Message msg = Message.obtain();
						msg.setTarget(handler);
						if (strReturnedAddress != null) {
							msg.what = 1;
							Bundle bundle = new Bundle();
							bundle.putString("address",
									strReturnedAddress.toString());
							msg.setData(bundle);
						} else
							msg.what = 0;
						msg.sendToTarget();
					}
				}
			}
		};
		thread.start();
	}

	private class GeocoderHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case 1:
				Bundle bundle = message.getData();
				str = bundle.getString("address");
				break;
			default:
				str = null;
			}
			// replace by what you need to do
			if (str != null) {
				txtviewAddress.setText(str);
			} else {
				txtviewAddress.setText("Can't Get Address Info!");
			}
		}
	}

}
