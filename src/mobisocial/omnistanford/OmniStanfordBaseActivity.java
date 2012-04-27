package mobisocial.omnistanford;

import java.util.ArrayList;

import mobisocial.socialkit.musubi.Musubi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class OmniStanfordBaseActivity extends Activity {
    public static final String TAG = "OmniStanfordBaseActivity";
    
    private static final String ACTION_OWNED_ID_PICKER = "musubi.intent.action.OWNED_ID_PICKER";
    private static final int REQUEST_PICK_ID = 2;
    private static final String ACCOUNT_TYPE_STANFORD = "edu.stanford";
    
    private OnClickListener mPickerClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent picker = new Intent(ACTION_OWNED_ID_PICKER);
            startActivityForResult(picker, REQUEST_PICK_ID);
        }
    };
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);if (requestCode == REQUEST_PICK_ID) {
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
        
        if (!Musubi.isMusubiInstalled(this)) {
            return;
        }
        
        findViewById(R.id.accountPicker).setOnClickListener(mPickerClickListener);
    }
}
