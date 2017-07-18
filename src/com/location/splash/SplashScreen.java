package com.location.splash;

import com.location.distancefinder.MainPage;
import com.location.distancefinder.getLocation;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

public class SplashScreen extends Activity {
	// Splash screen timer
	private static int SPLASH_TIME_OUT = 5000;
	public static final String PREFS_NAME = "MyPrefsFile";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkLogin();
		StartAppSDK.init(this, "106447526", "206846511");
		StartAppAd.showSplash(this, savedInstanceState);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("hasLoggedIn", true);
				editor.commit();
				Intent i = new Intent(SplashScreen.this, MainPage.class);
				startActivity(i);
				finish();
			}
		}, SPLASH_TIME_OUT);
	}
	
	public void checkLogin()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		//Get "hasLoggedIn" value. If the value doesn't exist yet false is returned
		boolean hasLoggedIn = settings.getBoolean("hasLoggedIn", false);

		if(hasLoggedIn)
		{
			Intent intent = new Intent(SplashScreen.this,
					MainPage.class);
			startActivity(intent);
			finish();
		}
	}

}
