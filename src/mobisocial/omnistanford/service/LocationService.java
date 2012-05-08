package mobisocial.omnistanford.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
	public static final String TAG = "LocationService";
	
	@SuppressWarnings("unused")
    private LocationManager mLocationManager;
	private final IBinder mBinder = new LocationBinder();
	
	public class LocationBinder extends Binder {
		LocationBinder getService() {
			return LocationBinder.this;
		}
	}
	
	@Override
	public void onCreate() {
		mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "received start id " + startId + ": " + intent);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "service stopped");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.i(TAG, "received location + " + location.toString());
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.i(TAG, "provider disabled");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.i(TAG, "provider enabled");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.i(TAG, "status changed");
		}
		
	};
}
