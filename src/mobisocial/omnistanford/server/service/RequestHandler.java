package mobisocial.omnistanford.server.service;

import java.util.List;

import org.json.JSONObject;

import mobisocial.omnistanford.App;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.server.db.CheckinManager;
import mobisocial.omnistanford.server.db.MCheckinData;
import mobisocial.omnistanford.server.db.MUser;
import mobisocial.omnistanford.server.db.UserManager;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class RequestHandler extends IntentService {
	public static final String TAG = "RequestHandlerService";
	
	private SQLiteOpenHelper mDBHelper;
	private SQLiteOpenHelper mServerDBHelper;

	public RequestHandler() {
		super("RequestHandlerService");		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mDBHelper = App.getDatabaseSource(this);
		mServerDBHelper = App.getServerDatabaseSource(this);
		return super.onStartCommand(intent,flags,startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		synchronized(this) {
			Uri objUri = intent.getParcelableExtra("objUri");
			Musubi musubi = Musubi.forIntent(this, intent);
			DbObj obj = musubi.objForUri(objUri);
			JSONObject request = obj.getJson();
			long requesterLocalId = obj.getContainingFeed().getMembers().get(0).getLocalId();
			if(request.has("from") && request.has("route")) {
				String route = request.optString("route");
				
				if(route.equals("checkin")) {
					onCheckin(requesterLocalId, request);
				} else if(route.equals("register")) {
					onRegister(requesterLocalId, request);
				}
			}
		}
	}
	
	void onCheckin(long localUserId, JSONObject req) {
		JSONObject from = req.optJSONObject("from");
		JSONObject payload = req.optJSONObject("payload");
		if(payload != null) {
			double lon = payload.optDouble("lon");
			double lat = payload.optDouble("lat");
			// TODO: find location by longitude and latitude
			
			LocationManager lm = new LocationManager(mDBHelper);
			MLocation location = lm.getLocations("Dining Hall").get(0);
			UserManager um = new UserManager(mServerDBHelper);
			MUser user = um.getAccount(from.optString("name"), 
					from.optString("type"), from.optString("principal"));
			if(user == null) {
				// TODO: user not found, error response
			}
			
			MCheckinData checkin = new MCheckinData();
			checkin.userId = user.id;
			checkin.locationId = location.id;
			checkin.entryTime = System.currentTimeMillis();
			checkin.exitTime = Long.MAX_VALUE;
			CheckinManager cm = new CheckinManager(mServerDBHelper);
			cm.insertCheckin(checkin);
			Log.i(TAG, "new checkin inserted");
		}
	}
	
	void onRegister(long localUserId, JSONObject req) {
		JSONObject from = req.optJSONObject("from");
		String name = from.optString("name");
		String type = from.optString("type");
		String hash = from.optString("principal");
		
		if(!name.equals("") && !type.equals("") && !hash.equals("")) {
			UserManager um = new UserManager(mServerDBHelper);
			MUser newUser = new MUser(localUserId, name, type, hash);
			um.insertAccount(newUser);
			Log.i(TAG, "new user registered: " + name);
		}
	}

}
