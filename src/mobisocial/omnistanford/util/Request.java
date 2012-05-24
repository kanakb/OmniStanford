package mobisocial.omnistanford.util;

import java.util.HashMap;
import java.util.Map;

import mobisocial.omnistanford.App;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MAccount;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.FeedObserver;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.AppStateObj;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class Request {
	public static final String TAG = "Request Generator";
	HashMap<String, String> mParams = new HashMap<String, String>();
	String mLocPrincipal;
	String mRoute;
	DbFeed mFeed;
	ResponseHandler mResHandler;
	
	public Request(String locPrincipal, String route, ResponseHandler handler) {
		mLocPrincipal = locPrincipal;
		mRoute = route;
		mResHandler = handler;
	}
	
	public Request addParam(String key, String value) {
		mParams.put(key, value);
		return this;
	}
	
	public JSONObject toJSON(Context context) {
		JSONObject req = new JSONObject();
		JSONObject body = new JSONObject();
		try {
			JSONObject from = new JSONObject();
			MAccount account = Util.loadAccount(context);
    		from.put("name", account.name);
    		from.put("hash", account.identifier);
    		from.put("type", account.type);
    		body.put("from", from);
    		
    		if(mParams.size() > 0) {
	    		JSONObject payload = new JSONObject();
	    		for(Map.Entry<String, String> entry : mParams.entrySet()) {
	    			payload.put(entry.getKey(), entry.getValue());
	    		}
	    		body.put("payload", payload);
    		}
    		
    		body.put("route", mRoute);
    		body.put("to", mLocPrincipal);
    		req.put("req", body);
		} catch (JSONException e) {
			Log.e(TAG, "JSON parse error", e);
		}
		
		return req;
	}
	
	public JSONObject send(Context context) {
		synchronized (this) {
			Musubi musubi = Musubi.getInstance(context);
			LocationManager lm = new LocationManager(App.getDatabaseSource(context));
			MLocation loc = lm.getLocation(mLocPrincipal);
			mFeed = musubi.getFeed(loc.feedUri);
			mFeed.insert(new AppStateObj(toJSON(context), null));
			if(mResHandler != null)
				mFeed.registerStateObserver(mObserver);
		}
		
		return null;
	}
	
	public void cancelUpdates() {
	    if (mResHandler != null) {
	        mFeed.unregisterStateObserver(mObserver);
	    }
	}
	
	Observer mObserver = new Observer();
	class Observer implements FeedObserver {
		@Override
		public void onUpdate(DbObj obj) {
			JSONObject json = obj.getJson();
			if(json != null && json.has("res")) {
				Log.i(TAG, "onUpdate: " + obj.getJson().toString());
				mFeed.unregisterStateObserver(mObserver);
				mResHandler.OnResponse(obj);
			}
		}
	}
}
