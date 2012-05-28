package mobisocial.omnistanford;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.util.LocationUpdater;
import mobisocial.omnistanford.util.Util;
import mobisocial.socialkit.musubi.Musubi;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;

public class OmniStanfordBaseActivity extends SherlockFragmentActivity {
    public static final String TAG = "OmniStanfordBaseActivity";
    
    protected static final String ACTION_CREATE_STANFORD_FEED = "musubi.intent.action.CREATE_STANFORD_FEED";
    protected static final String ACTION_OWNED_ID_PICKER = "musubi.intent.action.OWNED_ID_PICKER";
    protected static final int REQUEST_CREATE_FEED = 1;
    protected static final int REQUEST_PICK_ID = 2;
    protected static final String ACCOUNT_TYPE_STANFORD = "edu.stanford";
    protected static final String EXTRA_NAME = "mobisocial.omnistanford.json";
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_ID) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> names = data.getStringArrayListExtra("names");
                ArrayList<String> types = data.getStringArrayListExtra("types");
                ArrayList<String> hashes = data.getStringArrayListExtra("principal_hashes");
                Log.d(TAG, "Names: " + names);
                Log.d(TAG, "Types: " + types);
                Log.d(TAG, "Hashes: " + hashes);
                // TODO: use the first returned account
                
                int i;
                for (i = 0; i < types.size(); i++) {
                    if (types.get(i).equals(ACCOUNT_TYPE_STANFORD)) {
                        break;
                    }
                }
                if (i == types.size()) {
                    i = 0;
                }

                for (int j = 0; j < names.size(); j++) {
                    Util.saveAccount(this, names.get(j), hashes.get(j), types.get(j));
                }
                
                Util.setPickedAccount(this, names.get(i), types.get(i), hashes.get(i));

                
                Intent create = new Intent(ACTION_CREATE_STANFORD_FEED);
                JSONObject primary = new JSONObject();
                JSONArray arr = new JSONArray();
                JSONObject one = new JSONObject();
                try {
                    primary.put("visible", true);
                    one.put("hashed", Base64.encodeToString(digestPrincipal("arrillaga.stanford@gmail.com"), Base64.DEFAULT));
                    one.put("name", "Steve Fan");
                    one.put("type", "com.google");
                    arr.put(0, one);
                    primary.put("members", arr);
                    primary.put("sender", types.get(0));
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error", e);
                    return;
                }

                Log.d(TAG, arr.toString());
                create.putExtra(EXTRA_NAME, primary.toString());
                //startActivityForResult(create, REQUEST_CREATE_FEED);
                
                new CreateFeedsTask().execute(App.getDatabaseSource(this));
            }
        } else if (requestCode == REQUEST_CREATE_FEED) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "got a feed start result");
                Uri feedUri = data.getData();
                if (feedUri != null) {
                    // Single feed
                    Log.d(TAG, "Feed URI: " + feedUri);
                } else if (data.getStringArrayListExtra("uris") != null 
                        && data.getStringArrayListExtra("principals") != null) {
                    // Multiple feeds
                    ArrayList<String> array = data.getStringArrayListExtra("uris");
                    ArrayList<String> principals = data.getStringArrayListExtra("principals");
                    LocationManager lm = new LocationManager(App.getDatabaseSource(this));
                    for (int i = 0; i < array.size(); i++) {
                        Log.d(TAG, "Principal: " + principals.get(i));
                        MLocation loc = lm.getLocation(principals.get(i));
                        if (loc != null) {
                            loc.feedUri = Uri.parse(array.get(i));
                        	Log.i(TAG, loc.feedUri.toString());
                        	lm.updateLocation(loc);

//                        	Request request = new Request("arrillaga.stanford@gmail.com", "checkin", null);
//                        	request.addParam("dorm", "Off-Campus");
//                        	request.addParam("department", "CS");
//                        	request.send(this);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        LinearLayout variableBox = (LinearLayout)findViewById(R.id.contentArea);
        variableBox.removeAllViewsInLayout();
        
        if (!Musubi.isMusubiInstalled(this)) {
            return;
        }
        
        String currentName = Util.getPickedAccountName(this);
        if(currentName != null) {
        	getSupportActionBar().setTitle(currentName);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        String currentName = Util.getPickedAccountName(this);
        if(currentName != null) {
            getSupportActionBar().setTitle(currentName);
        }
    }
    
    // TODO: remove this
    public static byte[] digestPrincipal(String principal) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(principal.getBytes());
            return md.digest();
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException("Platform doesn't support sha256?!?!", e);
        }
    }
    
  
    
    protected class CreateFeedsTask extends AsyncTask<SQLiteOpenHelper, Void, LocationManager> {
        @Override
        protected LocationManager doInBackground(SQLiteOpenHelper... helpers) {
            // Keeping database update off the main thread
            LocationManager lm = new LocationManager(helpers[0]);
            new LocationUpdater(lm).syncUpdate();
            return lm;
        }
        
        protected void onPostExecute(LocationManager lm) {
            // Start feeds on Musubi
            List<MLocation> locations = lm.getLocations();
            JSONObject toCreate = new JSONObject();
            JSONArray outerArr = new JSONArray();
            ArrayList<String> principals = new ArrayList<String>();
            for (MLocation location : locations) {
                if (location.feedUri == null) {
                    JSONObject primary = new JSONObject();
                    JSONArray arr = new JSONArray();
                    JSONObject one = new JSONObject();
                    try {
                        if (location.principal == null ||
                                Util.getPickedAccountType(OmniStanfordBaseActivity.this) == null ||
                                (location.feedUri != null && !location.feedUri.equals(""))) {
                            continue;
                        }
                        primary.put("visible", false);
                        one.put("hashed", Base64.encodeToString(
                                OmniStanfordBaseActivity.digestPrincipal(location.principal),
                                Base64.DEFAULT));
                        one.put("name", location.name);
                        one.put("type", location.accountType);
                        principals.add(location.principal);
                        arr.put(0, one);
                        primary.put("members", arr);
                        primary.put("sender", Util.getPickedAccountType(OmniStanfordBaseActivity.this));
                        outerArr.put(primary);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parse error", e);
                        return;
                    }
                }
            }
            try {
                toCreate.put("array", outerArr);
                if (outerArr.length() > 0) {
                    Intent intent = new Intent(ACTION_CREATE_STANFORD_FEED);
                    intent.putExtra(EXTRA_NAME, toCreate.toString());
                    Log.d(TAG, "starting...");
                    Log.d(TAG, toCreate.toString());
                    intent.putStringArrayListExtra("principals", principals);
                    Log.d(TAG, "sent principals: " + principals);
                    startActivityForResult(intent, REQUEST_CREATE_FEED);
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON error", e);
            }
        }
    }
}
