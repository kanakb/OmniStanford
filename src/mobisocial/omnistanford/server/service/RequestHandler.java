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
import mobisocial.omnistanford.server.db.OmniStanfordContentProvider;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.AppStateObj;
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
	private CheckinManager mCheckinManager;

	public RequestHandler() {
		super("RequestHandlerService");		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mDBHelper = App.getDatabaseSource(this);
		mServerDBHelper = App.getServerDatabaseSource(this);
		
		mLocManager = new LocationManager(mDBHelper);
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
			JSONObject request = obj.getJson().optJSONObject("req");
			long requesterLocalId = feed.getMembers().get(0).getLocalId();
			if(request.has("from") && request.has("route")) {
				String route = request.optString("route");
				
				if(route.equals("checkin")) {
					onCheckin(requesterLocalId, request, feed);
				} else if (route.equals("checkout")) {
					onCheckout(requesterLocalId, request, feed);
				}
			}
		}
	}
	
	
	void onCheckin(long localUserId, JSONObject req, DbFeed feed) {
		JSONObject from = req.optJSONObject("from");
		JSONObject payload = req.optJSONObject("payload");
		if(payload != null) {
			MLocation location = mLocManager.getLocation("arrillaga.stanford@gmail.com");
			
			MCheckinData checkin = new MCheckinData(
					location.id, 
					System.currentTimeMillis(), 
					null,
					from.optString("name"),
					from.optString("type"),
					from.optString("hash"),
					payload.optString("dorm"),
					payload.optString("department"));
			mCheckinManager.checkin(checkin);
			List<MCheckinData> checkins = mCheckinManager.findOpenCheckinForProfile(
					location.id, checkin.userDorm, checkin.userDepartment);
			Log.i(TAG, "new checkin inserted");
			
			JSONObject res = new JSONObject();
			try {
				JSONArray arr = new JSONArray();
				for(MCheckinData c : checkins) {
					JSONObject o = new JSONObject();
					o.put("name", c.userName);
					o.put("principal", c.userType);
					o.put("type", c.userHash);
					o.put("dorm", c.userDorm);
					o.put("department", c.userDepartment);
					arr.put(o);
				}
				res.put("res", arr);
				if (payload.has("id")) {
				    res.put("id", payload.getString("id"));
				}
			} catch(JSONException e) {
				Log.i(TAG, e.toString());
			}
			feed.insert(new AppStateObj(res, null));
			getContentResolver().notifyChange(Uri.withAppendedPath(OmniStanfordContentProvider.CONTENT_URI, "checkins"), null);
		}
	}
	
	void onCheckout(long localUserId, JSONObject req, DbFeed feed) {
		JSONObject from = req.optJSONObject("from");
		if(from != null) {
			MLocation location = mLocManager.getLocation("arrillaga.stanford@gmail.com");
			MCheckinData checkin = mCheckinManager.findOpenCheckinForUser(location.id, 
					from.optString("name"), from.optString("type"), from.optString("hash"));
			if(checkin != null) {
				checkin.exitTime = System.currentTimeMillis();
				mCheckinManager.updateCheckin(checkin);
				Log.i(TAG, "checkout inserted");

				
				JSONObject res = new JSONObject();
				try {
					res.put("res", "true");
				} catch(JSONException e) {
					Log.i(TAG, e.toString());
				}
				feed.insert(new AppStateObj(res, null));
				getContentResolver().notifyChange(Uri.withAppendedPath(OmniStanfordContentProvider.CONTENT_URI, "checkins"), null);
			} else {
				// no checkin found. send false
			}
		}
	}
}
