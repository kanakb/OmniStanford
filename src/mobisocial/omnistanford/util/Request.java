package mobisocial.omnistanford.util;

import java.util.HashMap;
import java.util.Map;

import mobisocial.omnistanford.db.MAccount;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class Request {
	public static final String TAG = "Request Generator";
	HashMap<String, String> mParams = new HashMap<String, String>();
	String mRoute;
	
	public Request(String route) {
		mRoute = route;
	}
	
	public Request addParam(String key, String value) {
		mParams.put(key, value);
		return this;
	}
	
	public JSONObject toJSON(Context context) {
		JSONObject req = new JSONObject();
		try {
			JSONObject from = new JSONObject();
			MAccount account = Util.loadAccount(context);
    		from.put("name", account.name);
    		from.put("principal", account.identifier);
    		from.put("type", account.type);
    		req.put("from", from);
    		
    		if(mParams.size() > 0) {
	    		JSONObject payload = new JSONObject();
	    		for(Map.Entry<String, String> entry : mParams.entrySet()) {
	    			payload.put(entry.getKey(), entry.getValue());
	    		}
	    		req.put("payload", payload);
    		}
    		
    		req.put("route", mRoute);
		} catch (JSONException e) {
			Log.e(TAG, "JSON parse error", e);
		}
		
		return req;
	}
}
