package mobisocial.omnistanford.service;

import org.json.JSONObject;

import mobisocial.omnistanford.db.DatabaseHelper;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class RequestHandler extends IntentService {
	public static final String TAG = "RequestHandlerService";
	
	private DatabaseHelper mDBHelper;

	public RequestHandler() {
		super("RequestHandlerService");
		
		mDBHelper = new DatabaseHelper(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		synchronized(this) {
			Uri objUri = intent.getParcelableExtra("objUri");
			Musubi musubi = Musubi.forIntent(this, intent);
			DbObj obj = musubi.objForUri(objUri);
			JSONObject request = obj.getJson();
			if(request.has("from") && request.has("route")) {
				String route = request.optString("route");
				if(route.equals("checkin")) {
					JSONObject payload = request.optJSONObject("payload");
					if(payload != null) {
						double lon = payload.optDouble("lon");
						double lat = payload.optDouble("lat");

						LocationManager lm = new LocationManager(mDBHelper);
						MLocation location = new MLocation();
						location.name = "Arrillaga";
						location.type = "Dining Hall";
						location.principal = request.optJSONObject("from").optString("principal");
						location.accountType = "unknown";
						location.maxLatitude = location.minLatitude = (float) lon;
						location.maxLongitude = location.minLongitude = (float) lat;
						lm.insertLocation(location);
						Log.i(TAG, "new location inserted");
					}
				}
			}
		}
	}

}
