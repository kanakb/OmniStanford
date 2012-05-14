package mobisocial.omnistanford.server.service;

import java.util.ArrayList;
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
	
	private LocationManager mLocManager;
	private UserManager mUserManager;
	private ProfileManager mProfileManager;
	private CheckinManager mCheckinManager;

	public RequestHandler() {
		super("RequestHandlerService");		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mDBHelper = App.getDatabaseSource(this);
		mServerDBHelper = App.getServerDatabaseSource(this);
		
		mLocManager = new LocationManager(mDBHelper);
		mUserManager = new UserManager(mServerDBHelper);
		mProfileManager = new ProfileManager(mServerDBHelper);
		mCheckinManager = new CheckinManager(mServerDBHelper);
		
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
			MLocation location = mLocManager.getLocations("Dining Hall").get(0);
			MUser user = mUserManager.getUser(from.optString("name"), 
					from.optString("type"), from.optString("principal"));
			if(user == null) {
				// TODO: user not found, error response
				return;
			}
			
			MCheckinData checkin = new MCheckinData(user.id, 
					location.id, System.currentTimeMillis(), null);
			mCheckinManager.insertCheckin(checkin);
			Log.i(TAG, "new checkin inserted");
			
			List<MUser> users = findSimilarUsersAt(user, location.id);
			JSONObject res = new JSONObject();
			try {
				JSONArray arr = new JSONArray();
				for(MUser u : users) {
					JSONObject o = new JSONObject();
					o.put("name", u.name);
					o.put("principal", u.identifier);
					o.put("type", u.type);
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
			MUser newUser = new MUser(localUserId, name, type, hash);
			// TODO: ensure only one user
			mUserManager.ensureUser(newUser);
			Log.i(TAG, "new user registered: " + name);
			
			MProfile profile = new MProfile(newUser.id, 
					payload.optString("dorm"), payload.optString("department"));
			mProfileManager.ensureProfile(profile);
			Log.i(TAG, "new profile inserted "
					+ profile.dorm + " " + profile.department);
		}
	}
	
	List<MUser> findSimilarUsersAt(MUser user, Long locationId) {
		MProfile profile = mProfileManager.getProfile(user.id);
		
		// TODO: this is a naive way to find similar users.
		List<Long> checkinUserIds = mCheckinManager.findOpenCheckinAt(locationId);
		
		List<Long> matchedUserIds = mProfileManager.getMatchingUserIds(checkinUserIds, 
				profile.dorm, profile.department);
		
		return mUserManager.getUsers(matchedUserIds);
	}

}
