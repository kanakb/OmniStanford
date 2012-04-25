package mobisocial.omnistanford;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import mobisocial.omnistanford.service.LocationService;
import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;

public class OmniStanfordActivity extends Activity {
    public static final String TAG = "OmniStanfordActivity";
    
    private static final String ACTION_CREATE_STANFORD_FEED = "musubi.intent.action.CREATE_STANFORD_FEED";
    private static final String ACTION_OWNED_ID_PICKER = "musubi.intent.action.OWNED_ID_PICKER";
    private static final int REQUEST_CREATE_FEED = 1;
    private static final int REQUEST_PICK_ID = 2;
    private static final String EXTRA_NAME = "mobisocial.omnistanford.json";
    private static final String ACCOUNT_TYPE_STANFORD = "edu.stanford";
    
    private Musubi mMusubi;
    
    private OnClickListener mPickerClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent picker = new Intent(ACTION_OWNED_ID_PICKER);
            startActivityForResult(picker, REQUEST_PICK_ID);
        }
    };
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    one.put("principal", "kanak");
                    one.put("name", "Kanak Biscuitwala");
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
        else if (requestCode == REQUEST_PICK_ID) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> names = data.getStringArrayListExtra("names");
                ArrayList<String> types = data.getStringArrayListExtra("types");
                ArrayList<String> hashes = data.getStringArrayListExtra("principal_hashes");
                Log.d(TAG, "Names: " + names);
                Log.d(TAG, "Types: " + types);
                Log.d(TAG, "Hashes: " + hashes);
                for (int i = 0; i < names.size(); i++) {
                    if (types.get(i).equals(ACCOUNT_TYPE_STANFORD)) {
                        ((TextView)findViewById(R.id.accountPicker)).setText(names.get(i), TextView.BufferType.NORMAL);
                    }
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
            one.put("principal", "kanak");
            one.put("name", "Kanak Biscuitwala");
            arr.put(0, one);
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error", e);
            return;
        }
        
        Log.d(TAG, arr.toString());
        create.putExtra(EXTRA_NAME, arr.toString());
        //startActivityForResult(create, REQUEST_CREATE_FEED);
        
        findViewById(R.id.accountPicker).setOnClickListener(mPickerClickListener);
        
        bindServices();
    }
    
    private void bindServices() {
    	Intent locationService = new Intent(this, LocationService.class);
    	startService(locationService);
    }
}