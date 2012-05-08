package mobisocial.omnistanford;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {
	public static final String TAG = "MessageReceiver";
	
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri objUri = intent.getParcelableExtra("objUri");
        if (objUri == null) {
            Log.i("WordPlayNotification", "No object found");
            return;
        }

        Musubi musubi = Musubi.forIntent(context, intent);
        @SuppressWarnings("unused")
        DbObj obj = musubi.objForUri(objUri);
//        if (obj.getSender().isOwned()) {
//            return;
//        }
        
        Log.i(TAG, "received message " + intent);

        // Dont notify in Musubi
        Bundle b = new Bundle();
        b.putInt("notification", 0);
        setResult(Activity.RESULT_OK, null, b);
    }
}
