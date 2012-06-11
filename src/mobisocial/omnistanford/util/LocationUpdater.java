package mobisocial.omnistanford.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;

public class LocationUpdater {
    public static final String TAG = "LocationUpdater";
    
    private static final int CHUNK_SIZE = 512;
    
    private static final String ENDPOINT =
            "https://musulogin.appspot.com/static/stanford_locations.json";
    
    private final LocationManager mLm;
    
    public LocationUpdater(LocationManager lm) {
        mLm = lm;
    }
    
    public void update() {
        new Thread() {
            @Override
            public void run() {
                syncUpdate();
            }
        }.start();
    }
    
    // Don't call this from the main thread
    public void syncUpdate() {
        JSONArray locations = sendRequest();
        if (locations == null) {
            return;
        }
        for (int i = 0; i < locations.length(); i++) {
            try {
                JSONObject obj = locations.getJSONObject(i);
                MLocation loc = new MLocation();
                loc.name = obj.getString("name");
                loc.principal = obj.getString("principal");
                loc.accountType = obj.getString("accountType");
                loc.type = obj.getString("type");
                loc.minLatitude = obj.getDouble("minLatitude");
                loc.maxLatitude = obj.getDouble("maxLatitude");
                loc.minLongitude = obj.getDouble("minLongitude");
                loc.maxLongitude = obj.getDouble("maxLongitude");
                if (loc.principal != null && !loc.principal.equals("null")) {
                    Log.d(TAG, "principal: " + loc.principal);
                    mLm.ensureLocation(loc);
                    if (obj.has("image")) {
                        // do an image update if needed
                        updateImage(loc, obj.getString("image"));
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON object");
            }
        }
    }
    
    private void updateImage(MLocation loc, String imageUrl) {
        // No need to update if the image URL hasn't changed
        if (loc.imageUrl != null && imageUrl.equals(loc.imageUrl)) {
            return;
        }
        
        loc.imageUrl = imageUrl;
        Log.d(TAG, "getting image for " + loc.name + " at " + loc.imageUrl);
        
        byte[] imageData = imageDataRequest(imageUrl);
        if (imageData != null) {
            Log.d(TAG, "image not null, size " + imageData.length + " adding to location " + loc.id);
            loc.image = imageData;
            mLm.updateLocation(loc);
        }
    }
    
    private byte[] imageDataRequest(String remoteUrl) {
        try {
            // Grab the content
            URL url = new URL(remoteUrl);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            
            // Read the content chunk by chunk
            BufferedInputStream bis = new BufferedInputStream(is, 8192);
            ByteArrayBuffer baf = new ByteArrayBuffer(0);
            byte[] chunk = new byte[CHUNK_SIZE];
            int current = bis.read(chunk);
            while (current != -1) {
                baf.append(chunk, 0, current);
                current = bis.read(chunk);
            }
            return baf.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "HTTP error", e);
        }
        return null;
    }
    
    private static JSONArray sendRequest() {
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(ENDPOINT);
        try {
            HttpResponse response = client.execute(httpGet);
            // Read the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String responseStr = "";
            String line = "";
            while ((line = rd.readLine()) != null) {
                responseStr += line;
            }
            return new JSONArray(responseStr);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON array", e);
        } catch (IOException e) {
            Log.e(TAG, "Request error", e);
        }
        return null;
    }
}
