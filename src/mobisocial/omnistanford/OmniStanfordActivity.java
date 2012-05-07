package mobisocial.omnistanford;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import mobisocial.omnistanford.service.LocationService;
import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;

public class OmniStanfordActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "OmniStanfordActivity";
    
    private Musubi mMusubi;
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CREATE_FEED) {
            if (resultCode == RESULT_OK) {
                Uri feedUri = data.getData();
                if (feedUri == null) {
                    return;
                }
                Log.d(TAG, "Feed URI: " + feedUri);
                
                DbFeed feed = mMusubi.getFeed(feedUri);
                
                DbIdentity me = feed.getLocalUser();
                List<DbIdentity> members = feed.getMembers();
                Log.d(TAG, "My ID: " + me.getId() + " Name: " + me.getName());
                for (DbIdentity member : members) {
                    Log.d(TAG, "ID: " + member.getId() + " Name: " + member.getName());
                }
                
                JSONObject one = new JSONObject();
                try {
                    one.put("principal", "stfan");
                    one.put("name", "Steve Fan");
                    //one.put(Obj.FIELD_RENDER_TYPE, Obj.RENDER_LATEST);
                    one.put(Obj.FIELD_HTML, "<html>hi</html>");
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error", e);
                    return;
                }
                
                feed.insert(new MemObj("omnistanford", one));
                Log.d(TAG, feed.getLatestObj().getJson().toString());
                
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mMusubi = Musubi.forIntent(this, getIntent());
        
        if (!Musubi.isMusubiInstalled(this)) {
            return;
        }
        Intent create = new Intent(ACTION_CREATE_STANFORD_FEED);
        JSONObject primary = new JSONObject();
        JSONArray arr = new JSONArray();
        JSONObject one = new JSONObject();
        try {
            primary.put("visible", true);
            one.put("hashed", Base64.encodeToString(digestPrincipal("kanak"), Base64.DEFAULT));
            one.put("name", "Kanak Biscuitwala");
            arr.put(0, one);
            primary.put("members", arr);
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error", e);
            return;
        }
        
        Log.d(TAG, arr.toString());
        create.putExtra(EXTRA_NAME, primary.toString());
        //startActivityForResult(create, REQUEST_CREATE_FEED);
        bindServices();
    }
    
    private void bindServices() {
    	Intent locationService = new Intent(this, LocationService.class);
    	startService(locationService);
    }
}