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
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
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
	    private static final int TWO_MINUTES = 1000 * 60 * 2;
	    Location mCurrent = null;

	    /** Determines whether one Location reading is better than the current Location fix
	      * @param location  The new Location that you want to evaluate
	      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	      * Thanks, Android documentation.
	      */
	    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	        if (currentBestLocation == null) {
	            // A new location is always better than no location
	            return true;
	        }

	        // Check whether the new location fix is newer or older
	        long timeDelta = location.getTime() - currentBestLocation.getTime();
	        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	        boolean isNewer = timeDelta > 0;

	        // If it's been more than two minutes since the current location, use the new location
	        // because the user has likely moved
	        if (isSignificantlyNewer) {
	            return true;
	        // If the new location is more than two minutes older, it must be worse
	        } else if (isSignificantlyOlder) {
	            return false;
	        }

	        // Check whether the new location fix is more or less accurate
	        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	        boolean isLessAccurate = accuracyDelta > 0;
	        boolean isMoreAccurate = accuracyDelta < 0;
	        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	        // Check if the old and new location are from the same provider
	        boolean isFromSameProvider = isSameProvider(location.getProvider(),
	                currentBestLocation.getProvider());

	        // Determine location quality using a combination of timeliness and accuracy
	        if (isMoreAccurate) {
	            return true;
	        } else if (isNewer && !isLessAccurate) {
	            return true;
	        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	            return true;
	        }
	        return false;
	    }

	    /** Checks whether two providers are the same */
	    private boolean isSameProvider(String provider1, String provider2) {
	        if (provider1 == null) {
	          return provider2 == null;
	        }
	        return provider1.equals(provider2);
	    }
	    
		@Override
		public void onLocationChanged(Location location) {
			Log.i(TAG, "received location + " + location.toString());
			if (isBetterLocation(location, mCurrent)) {
			    mCurrent = location;
			}
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
