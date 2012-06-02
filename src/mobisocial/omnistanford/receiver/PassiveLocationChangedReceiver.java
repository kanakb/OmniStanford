package mobisocial.omnistanford.receiver;

import mobisocial.omnistanford.service.LocationService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class PassiveLocationChangedReceiver extends BroadcastReceiver {
	public static final String TAG = "PassiveLocationChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String key = LocationManager.KEY_LOCATION_CHANGED;
	    Location location = null;
	    
	    if (intent.hasExtra(key)) {
	    	// This update came from Passive provider, so we can extract the location
	    	// directly.
	    	location = (Location)intent.getExtras().get(key);   
	    	Log.i(TAG, "received broadcast location: " + location.toString());
	    	
	    	Intent serviceIntent = new Intent(context, LocationService.class);
	    	serviceIntent.putExtra("location", location);
	    	context.startService(serviceIntent);
	    }
	}
}
