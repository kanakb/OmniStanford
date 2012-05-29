package mobisocial.omnistanford.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.omnistanford.App;
import mobisocial.omnistanford.OmniStanfordActivity;
import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.db.MTag;
import mobisocial.omnistanford.db.TagManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class JavaScriptInterface {
	public static final String TAG = "JavaScriptInterface";
	Context mContext;

    /** Instantiate the interface and set the context */
    public JavaScriptInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
    
    public String getDailyData() {
    	TagManager tm = new TagManager(App.getDatabaseSource(mContext));
    	List<MTag> tags = tm.getDailyTags(new Date(System.currentTimeMillis()));
    	return convertToJSONString(tags, "daily");
    }
    
    public String getWeeklyData() {
    	TagManager tm = new TagManager(App.getDatabaseSource(mContext));
    	List<MTag> tags = tm.getWeeklyTags(new Date(System.currentTimeMillis()));
    	return convertToJSONString(tags, "weekly");
    }
    
    public String getMonthlyData() {
    	TagManager tm = new TagManager(App.getDatabaseSource(mContext));
    	List<MTag> tags = tm.getMonthlyTags(new Date(System.currentTimeMillis()));
    	return convertToJSONString(tags, "monthly");
    }
    
    private String convertToJSONString(List<MTag> tags, String type) {
    	HashMap<Long, List<MTag>> locToTag = new HashMap<Long, List<MTag>>();
    	for(MTag t : tags) {
    		if(locToTag.containsKey(t.locationId)) {
    			List<MTag> tagList = locToTag.get(t.locationId);
    			tagList.add(t);
    		} else {
    			List<MTag> tagList = new ArrayList<MTag>();
    			tagList.add(t);
    			locToTag.put(t.locationId, tagList);
    		}
    	}
    	

    	LocationManager lm = new LocationManager(App.getDatabaseSource(mContext));
    	JSONObject data = new JSONObject();
    	try {
    		JSONArray children = new JSONArray();
    		for(Entry<Long, List<MTag>> entry : locToTag.entrySet()) {
    			JSONObject locationData = new JSONObject();
    			MLocation location = lm.getLocation(entry.getKey());
    			locationData.put("name", location.name);
    			locationData.put("type", type);
    			JSONArray locationTags = new JSONArray();
    			for(MTag t : entry.getValue()) {
    				JSONObject tagData = new JSONObject();
    				tagData.put("name", t.name);
    				tagData.put("type", type);
    				tagData.put("size", t.endTime - t.startTime);
    				locationTags.put(tagData);
    			}
    			locationData.put("children", locationTags);
    			children.put(locationData);
    		}
    		data.put("name", "tags");
    		data.put("type", type);
    		data.put("children", children);
    	} catch (JSONException e) {
    		Log.e(TAG, e.toString());
    	}
    	
    	Log.i(TAG, data.toString());
    	return data.toString();
    }
}
