package mobisocial.omnistanford;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbIdentity;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MessageReceiver extends BroadcastReceiver {
	public static final String TAG = "MessageReceiver";
	
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "received message " + intent);

        Uri objUri = intent.getParcelableExtra("objUri");
        if (objUri == null) {
            Log.i("WordPlayNotification", "No object found");
            return;
        }

        Musubi musubi = Musubi.forIntent(context, intent);
        DbObj obj = musubi.objForUri(objUri);
        if (obj.getSender().isOwned()) {
            return;
        }
        
        // generate an auto response
//        DbFeed feed = obj.getContainingFeed();
//        DbIdentity me = feed.getLocalUser();
//        List<DbIdentity> members = feed.getMembers();
//        Log.d(TAG, "My ID: " + me.getId() + " Name: " + me.getName());
//        for (DbIdentity member : members) {
//            Log.d(TAG, "ID: " + member.getId() + " Name: " + member.getName());
//        }
//        
//        JSONObject one = new JSONObject();
//        try {
//            one.put("principal", "stfan");
//            one.put("name", "Steve Fan");
//            //one.put(Obj.FIELD_RENDER_TYPE, Obj.RENDER_LATEST);
//            one.put(Obj.FIELD_HTML, "<html>pong</html>");
//        } catch (JSONException e) {
//            Log.e(TAG, "JSON parse error", e);
//            return;
//        }
//        
//        feed.insert(new MemObj("omnistanford", one));
//        Log.d(TAG, feed.getLatestObj().getJson().toString());

        // Dont notify in Musubi
        Bundle b = new Bundle();
        b.putInt("notification", 0);
        setResult(Activity.RESULT_OK, null, b);
    }
}
