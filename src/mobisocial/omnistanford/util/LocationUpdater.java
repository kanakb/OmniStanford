package mobisocial.omnistanford.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;

public class LocationUpdater {
    public static final String TAG = "LocationUpdater";
    
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
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON object");
            }
        }
        MLocation temp = new MLocation();
        temp.name = "Kanak's Neighborhood";
        temp.principal = "kinselofant@gmail.com";
        temp.accountType = "com.google";
        temp.type = "Home";
        temp.minLatitude = 37.34459;
        temp.maxLatitude = 37.34925;
        temp.minLongitude = -122.02596;
        temp.maxLongitude = -122.01925;
        mLm.ensureLocation(temp);
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
