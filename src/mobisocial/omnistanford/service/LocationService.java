package mobisocial.omnistanford.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.omnistanford.App;
import mobisocial.omnistanford.OmniStanfordBaseActivity;
import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.DatabaseHelper;
import mobisocial.omnistanford.db.MAccount;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.util.LocationUpdater;
import mobisocial.omnistanford.util.Request;
import mobisocial.omnistanford.util.Util;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

public class LocationService extends Service {
	public static final String TAG = "LocationService";
	
	private static final String ACTION_CREATE_STANFORD_FEED =
	        "musubi.intent.action.CREATE_STANFORD_FEED";
	private static final String EXTRA_NAME = "mobisocial.omnistanford.json";
	
	private static final long INTERVAL = 1000 * 60 * 15;
	private static final long SHORT_INTERVAL = 1000 * 60 * 5;
	
	private Integer mUpdateCount = 0;
	private static final int MAX_UPDATE_COUNT = 4;
	
	private LocationManager mLocationManager;
	private mobisocial.omnistanford.db.LocationManager mLm =
	        new mobisocial.omnistanford.db.LocationManager(new DatabaseHelper(this));
	
	private final IBinder mBinder = new LocationBinder();
	
	public class LocationBinder extends Binder {
		LocationBinder getService() {
			return LocationBinder.this;
		}
	}
    
    private void setInnerLocationState() {
        Location last = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last != null) {
            double latitude = last.getLatitude();
            double longitude = last.getLongitude();
            
            Log.d(TAG, "Location: (" + latitude + ", " + longitude + ")");
            
            // Use fine locations if at Stanford
            if (latitude < 37.446 && latitude > 37.415 && longitude < -122.148 && longitude > -122.1926) {
                Log.d(TAG, "At Stanford");
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }
        }
    }
    
    private void setOuterLocationState() {
        Location last = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last != null) {
            double latitude = last.getLatitude();
            double longitude = last.getLongitude();
            
            Log.d(TAG, "Location: (" + latitude + ", " + longitude + ")");
            
            // Use fine locations if at Stanford
            if (latitude < 37.446 && latitude > 37.415 && longitude < -122.148 && longitude > -122.1926) {
                Log.d(TAG, "At Stanford");
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            }
        }
    }
	
	@Override
	public void onCreate() {
	    Log.d(TAG, "onCreate called");
	    mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    mLm = new mobisocial.omnistanford.db.LocationManager(new DatabaseHelper(this));
	    AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        long currentElapsedTime = SystemClock.elapsedRealtime();
        
        Intent locationUpdateIntent = new Intent(this, LocationService.class);
        locationUpdateIntent.putExtra("locationUpdate", true);
        PendingIntent locationSender = PendingIntent.getService(this, 0, locationUpdateIntent, 0);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, currentElapsedTime,
                SHORT_INTERVAL, locationSender);
        setInnerLocationState();
        
        Intent periodicLocationUpdateIntent = new Intent(this, LocationService.class);
        locationUpdateIntent.putExtra("periodicLocationUpdate", true);
        PendingIntent periodicLocationSender =
                PendingIntent.getService(this, 0, periodicLocationUpdateIntent, 0);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, currentElapsedTime,
                INTERVAL, periodicLocationSender);
        setOuterLocationState();
	    
	    Intent locationFetchIntent = new Intent(this, LocationService.class);
	    locationFetchIntent.putExtra("locationFetch", true);
	    PendingIntent fetchSender = PendingIntent.getService(this, 0, locationFetchIntent, 0);
	    am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, currentElapsedTime,
	            AlarmManager.INTERVAL_DAY, fetchSender);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        new LocationUpdater(mLm).update();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "received start id " + startId + ": " + intent);
		// Need to update list of known locations periodically
		if (intent != null) {
    		if (intent.hasExtra("locationFetch")) {
    		    new LocationUpdater(mLm).update();
    		} else if (intent.hasExtra("locationUpdate")) {
                setInnerLocationState();
            } else if (intent.hasExtra("periodicLocationUpdate")) {
                setOuterLocationState();
            }
		}
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
	
	public void postToLocationFeed(MLocation location) {
	    if (location.feedUri == null) {
	        Intent create = new Intent(ACTION_CREATE_STANFORD_FEED);
	        JSONObject primary = new JSONObject();
	        JSONArray arr = new JSONArray();
	        JSONObject one = new JSONObject();
	        try {
	            primary.put("visible", false);
	            one.put("hashed", Base64.encodeToString(
	                    OmniStanfordBaseActivity.digestPrincipal("arrillaga.stanford@gmail.com"),
	                    Base64.DEFAULT));
	            one.put("name", "Arrillaga Family Dining Commons");
	            one.put("type", location.accountType);
	            arr.put(0, one);
	            primary.put("members", arr);
	            primary.put("sender", Util.getPickedAccountType(this));
	        } catch (JSONException e) {
	            Log.e(TAG, "JSON parse error", e);
	            return;
	        }

	        Log.d(TAG, arr.toString());
	        create.putExtra(EXTRA_NAME, primary.toString());
	        //startActivityForResult(create, REQUEST_CREATE_FEED);
	    }
	    if (location.feedUri != null) {
    	    Musubi musubi = Musubi.getInstance(this);
    	    @SuppressWarnings("unused")
            DbFeed feed = musubi.getFeed(location.feedUri);
	    }
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
                CheckinManager cm = new CheckinManager(App.getDatabaseSource(LocationService.this));
			    MLocation match = mLm.getLocation(mCurrent.getLatitude(), mCurrent.getLongitude());
			    if (match != null && match.feedUri != null) {
			        Log.d(TAG, "Found " + match.name);
			        MCheckinData data = cm.getRecentCheckin(match.id);
			        if (data == null) {
			            data = new MCheckinData();
			            data.entryTime = System.currentTimeMillis();
			            data.locationId = match.id;
			            MAccount acct = Util.loadAccount(LocationService.this);
			            if (acct != null) {
			                data.accountId = acct.id;
			                
			                // Check in remotely
			                // TODO: send according to privacy settings
//			                Request request = new Request("checkin");
//                            request.addParam("lat", new Double(mCurrent.getLatitude()).toString());
//                            request.addParam("lon", new Double(mCurrent.getLongitude()).toString());
//			                Musubi musubi = Musubi.getInstance(LocationService.this);
//			                DbFeed feed = musubi.getFeed(match.feedUri);
//			                feed.postObj(new MemObj("omnistanford", request.toJSON(LocationService.this)));
//                            
//                            // Check in locally
//                            cm.insertCheckin(data);
			            }
			        }
			    } else {
			        // Exit open checkins
			        List<MCheckinData> checkins = cm.getRecentCheckins();
			        for (MCheckinData data : checkins) {
			            if (data.exitTime == null) {
			                data.exitTime = System.currentTimeMillis();
			                cm.updateCheckin(data);
			            }
			        }
			    }
			}
			synchronized(mUpdateCount) {
    			mUpdateCount++;
    			if (mUpdateCount > MAX_UPDATE_COUNT) {
    			    mUpdateCount = 0;
    			    mLocationManager.removeUpdates(this);
    			}
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.i(TAG, "provider disabled: " + provider);
			if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
//			    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			} else if (provider.equals(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            }
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
