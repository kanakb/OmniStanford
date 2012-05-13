package mobisocial.omnistanford.server.service;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.omnistanford.App;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.server.db.CheckinManager;
import mobisocial.omnistanford.server.db.MCheckinData;
import mobisocial.omnistanford.server.db.MProfile;
import mobisocial.omnistanford.server.db.MUser;
import mobisocial.omnistanford.server.db.ProfileManager;
import mobisocial.omnistanford.server.db.UserManager;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;
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
			DbFeed feed = obj.getContainingFeed();
			JSONObject request = obj.getJson();
			long requesterLocalId = feed.getMembers().get(0).getLocalId();
			if(request.has("from") && request.has("route")) {
				String route = request.optString("route");
				
				if(route.equals("checkin")) {
					onCheckin(requesterLocalId, request, feed);
				} else if(route.equals("register")) {
					onRegister(requesterLocalId, request);
				}
			}
		}
	}
	
	void onCheckin(long localUserId, JSONObject req, DbFeed feed) {
		JSONObject from = req.optJSONObject("from");
		JSONObject payload = req.optJSONObject("payload");
		if(payload != null) {
			double lon = payload.optDouble("lon");
			double lat = payload.optDouble("lat");
			// TODO: find location by longitude and latitude
			
			LocationManager lm = new LocationManager(mDBHelper);
			MLocation location = lm.getLocations("Dining Hall").get(0);
			UserManager um = new UserManager(mServerDBHelper);
			MUser user = um.getUser(from.optString("name"), 
					from.optString("type"), from.optString("principal"));
			if(user == null) {
				// TODO: user not found, error response
				return;
			}
			
			MCheckinData checkin = new MCheckinData(user.id, 
					location.id, System.currentTimeMillis(), null);
			CheckinManager cm = new CheckinManager(mServerDBHelper);
			cm.insertCheckin(checkin);
			Log.i(TAG, "new checkin inserted");
			
			List<MCheckinData> checkins = cm.findOpenCheckinAt(location.id);
			JSONObject res = new JSONObject();
			try {
				JSONArray arr = new JSONArray();
				for(MCheckinData c : checkins) {
					JSONObject o = new JSONObject();
					o.put("location_id", c.locationId);
					o.put("user_id", c.userId);
					arr.put(o);
				}
				res.put("res", arr);
			} catch(JSONException e) {
				Log.i(TAG, e.toString());
			}
			feed.insert(new MemObj("omnistanford", res));
		}
	}
	
	
	void onRegister(long localUserId, JSONObject req) {
		JSONObject from = req.optJSONObject("from");
		JSONObject payload = req.optJSONObject("payload");
		String name = from.optString("name");
		String type = from.optString("type");
		String hash = from.optString("principal");
		
		if(!name.equals("") && !type.equals("") && !hash.equals("")) {
			UserManager um = new UserManager(mServerDBHelper);
			MUser newUser = new MUser(localUserId, name, type, hash);
			// TODO: ensure only one user
			um.insertUser(newUser);
			Log.i(TAG, "new user registered: " + name);
			
			MProfile profile = new MProfile();
			profile.userId = newUser.id;
			profile.dorm = payload.optString("dorm");
			profile.department = payload.optString("department");
			ProfileManager pm = new ProfileManager(mServerDBHelper);
			pm.insertProfile(profile);
			Log.i(TAG, "new profile inserted "
					+ profile.dorm + " " + profile.department);
		}
	}

}
