package mobisocial.omnistanford;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.omnistanford.db.AccountManager;
import mobisocial.omnistanford.db.MAccount;
import mobisocial.omnistanford.util.Util;
import mobisocial.socialkit.musubi.Musubi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OmniStanfordBaseActivity extends Activity {
    public static final String TAG = "OmniStanfordBaseActivity";
    
    protected static final String ACTION_CREATE_STANFORD_FEED = "musubi.intent.action.CREATE_STANFORD_FEED";
    protected static final String ACTION_OWNED_ID_PICKER = "musubi.intent.action.OWNED_ID_PICKER";
    protected static final int REQUEST_CREATE_FEED = 1;
    protected static final int REQUEST_PICK_ID = 2;
    protected static final String ACCOUNT_TYPE_STANFORD = "edu.stanford";
    protected static final String EXTRA_NAME = "mobisocial.omnistanford.json";
    
    private OnClickListener mPickerClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent picker = new Intent(ACTION_OWNED_ID_PICKER);
            startActivityForResult(picker, REQUEST_PICK_ID);
        }
    };
    
    private OnClickListener mHomeClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent home = new Intent(OmniStanfordBaseActivity.this, OmniStanfordActivity.class);
            startActivity(home);
        }
    };
    
    private OnClickListener mSettingsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO: this isn't the right activity
            Intent create = new Intent(OmniStanfordBaseActivity.this, SelectContactsActivity.class);
            startActivity(create);
        }
    };
    
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
                ((TextView)findViewById(R.id.accountPicker)).setText(names.get(0), TextView.BufferType.NORMAL);
                
                // save picked account
                Util.saveAccount(this, names.get(0), hashes.get(0), types.get(0));
                
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
                startActivityForResult(create, REQUEST_CREATE_FEED);
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        LinearLayout variableBox = (LinearLayout)findViewById(R.id.contentArea);
        variableBox.removeAllViewsInLayout();
        
        if (!Musubi.isMusubiInstalled(this)) {
            return;
        }
        
        findViewById(R.id.accountPicker).setOnClickListener(mPickerClickListener);
        findViewById(R.id.homeButton).setOnClickListener(mHomeClickListener);
        findViewById(R.id.settingsButton).setOnClickListener(mSettingsClickListener);
        
        MAccount ac = Util.loadAccount(this);
        if(ac != null) {
            ((TextView)findViewById(R.id.accountPicker)).setText(ac.name, TextView.BufferType.NORMAL);
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
}
