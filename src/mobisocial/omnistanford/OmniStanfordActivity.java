package mobisocial.omnistanford;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.Musubi;

public class OmniStanfordActivity extends Activity {
    public static final String TAG = "OmniStanfordActivity";
    
    private static final String ACTION_CREATE_STANFORD_FEED = "musubi.intent.action.CREATE_STANFORD_FEED";
    private static final int REQUEST_CREATE_FEED = 1;
    private static final String EXTRA_NAME = "mobisocial.omnistanford.json";
    
    private Musubi mMusubi;
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CREATE_FEED) {
            if (resultCode == RESULT_OK) {
                Uri feedUri = data.getData();
                if (feedUri == null) {
                    return;
                }
                
                DbFeed feed = mMusubi.getFeed(feedUri);
                
                DbIdentity me = feed.getLocalUser();
                List<DbIdentity> members = feed.getMembers();
                Log.d(TAG, "My ID: " + me.getId() + " Name: " + me.getName());
                for (DbIdentity member : members) {
                    Log.d(TAG, "ID: " + member.getId() + " Name: " + member.getName());
                }
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        mMusubi = Musubi.forIntent(this, getIntent());
        
        if (!Musubi.isMusubiInstalled(this)) {
            return;
        }
        
        Intent create = new Intent(ACTION_CREATE_STANFORD_FEED);
        JSONArray arr = new JSONArray();
        JSONObject one = new JSONObject();
        try {
            one.put("principal", "stfan");
            one.put("name", "Steve Fan");
            arr.put(0, one);
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error", e);
            return;
        }
        
        Log.d(TAG, arr.toString());
        create.putExtra(EXTRA_NAME, arr.toString());
        //startActivityForResult(create, REQUEST_CREATE_FEED);
    }
}