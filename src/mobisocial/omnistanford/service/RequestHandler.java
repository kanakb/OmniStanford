package mobisocial.omnistanford.service;

import java.util.List;

import org.json.JSONObject;

import mobisocial.omnistanford.App;
import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.DatabaseHelper;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class RequestHandler extends IntentService {
	public static final String TAG = "RequestHandlerService";
	
	private SQLiteOpenHelper mDBHelper;

	public RequestHandler() {
		super("RequestHandlerService");		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mDBHelper = App.getDatabaseSource(this);
		return super.onStartCommand(intent,flags,startId);
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
						// TODO: find location by longitude and latitude
						
						LocationManager lm = new LocationManager(mDBHelper);
						MLocation location = lm.getLocations("Dining Hall").get(0);
						List<DbIdentity> members = obj.getContainingFeed().getMembers();
						Log.i(TAG, "checkin user " + members.get(1).getName());
						
						MCheckinData checkin = new MCheckinData();
						checkin.accountId = Long.valueOf(members.get(0).getLocalId());
						checkin.locationId = location.id;
						checkin.entryTime = System.currentTimeMillis();
						checkin.exitTime = Long.MAX_VALUE;
						CheckinManager cm = new CheckinManager(mDBHelper);
						cm.insertCheckin(checkin);
						Log.i(TAG, "new checkin inserted");
					}
				}
			}
		}
	}

}
