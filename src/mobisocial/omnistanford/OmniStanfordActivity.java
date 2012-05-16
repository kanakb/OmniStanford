package mobisocial.omnistanford;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.service.LocationService;
import mobisocial.omnistanford.util.Request;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;

public class OmniStanfordActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "OmniStanfordActivity";
    
    private static final long MONTH = 1000 * 60 * 60 * 24 * 30;
    
    private Musubi mMusubi;
    private View mSettingsView;
    private LinearLayout mButtonView;
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CREATE_FEED) {
            if (resultCode == RESULT_OK) {
                Uri feedUri = data.getData();
                if (feedUri != null) {
                    // Single feed
                    Log.d(TAG, "Feed URI: " + feedUri);
                    
                    DbFeed feed = mMusubi.getFeed(feedUri);
                    mMusubi.setFeed(feed);
                }
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mButtonView = new LinearLayout(this);
        mButtonView.setOrientation(LinearLayout.VERTICAL);
        Button registerButton = new Button(this);
        registerButton.setText("Register");
        registerButton.setOnClickListener(mRegisterClickListener);
        mButtonView.addView(registerButton);
        
        Button checkinButton = new Button(this);
        checkinButton.setText("Checkin");
        checkinButton.setOnClickListener(mCheckinClickListener);
        mButtonView.addView(checkinButton);
        
//        findViewById(R.id.settingsButton)
//        	.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View arg0) {
////					flipit();
//				}
//        	});
        ((LinearLayout)findViewById(R.id.contentArea)).addView(mButtonView);
        
        if (!Musubi.isMusubiInstalled(this)) {
            return;
        }
        
        mMusubi = Musubi.getInstance(this);

        TextView tv = new TextView(this);
        tv.setTextSize(20.0f);
        tv.setTextColor(Color.WHITE);
        tv.setText("Recent Locations");
        CheckinManager cm = new CheckinManager(App.getDatabaseSource(this));
        List<MCheckinData> checkins = cm.getRecentCheckins();
        for (MCheckinData data : checkins) {
            if (data.exitTime == null) {
                LocationManager lm = new LocationManager(App.getDatabaseSource(this));
                MLocation loc = lm.getLocation(data.locationId);
                if (loc != null) {
                    //tv.setText("Checked in at " + loc.name);
                }
                break;
            }
        }
        mButtonView.addView(tv);
        
        showRecent(mButtonView);
        
//        LayoutInflater li = LayoutInflater.from(this);
//        mSettingsView = li.inflate(R.layout.settings, null);
//        mSettingsView.setVisibility(View.GONE);
//        ((LinearLayout)findViewById(R.id.contentArea)).addView(mSettingsView);
        
        
        
        // Do some location updates
        new CreateFeedsTask().execute(App.getDatabaseSource(this));
        
        bindServices();
    }
    
    private void showRecent(LinearLayout wrapper) {
        String[] from = new String[] { "title", "subtitle" };
        int[] to = new int[] { R.id.plainTitle, R.id.plainSubtitle };
        List<HashMap<String, String>> hms = new ArrayList<HashMap<String, String>>();
        CheckinManager cm = new CheckinManager(App.getDatabaseSource(this));
        LocationManager lm = new LocationManager(App.getDatabaseSource(this));
        List<MCheckinData> checkins = cm.getRecentCheckins(MONTH);
        for (MCheckinData checkin : checkins) {
            HashMap<String, String> hm = new HashMap<String, String>();
            MLocation loc = lm.getLocation(checkin.locationId);
            hm.put("title", loc.name);
            hm.put("subtitle", new Date(checkin.entryTime).toString());
            hms.add(hm);
        }
        /*HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("title", "Gates Building");
        hm.put("subtitle", new Date(0L).toString());
        hms.add(hm);*/
        
        // TODO: this should be pull to refresh
        ListView listView = new ListView(this);
        listView.setAdapter(new SimpleAdapter(this, hms, R.layout.plain_list_item, from, to));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                //do something
            }
        });
        
        wrapper.addView(listView);
    }
    
    private OnClickListener mCheckinClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			LocationManager lm = new LocationManager(App.getDatabaseSource(v.getContext()));
			MLocation loc = lm.getLocation("arrillaga.stanford@gmail.com");
			DbFeed feed = mMusubi.getFeed(loc.feedUri);
	    	Request req = new Request("checkin");
	    	req.addParam("lon", "1").addParam("lat", "2");
	    	feed.insert(new MemObj("omnistanford", req.toJSON(v.getContext())));
	    	Log.d(TAG, feed.getLatestObj().getJson().toString());
		}
    };
    
    private OnClickListener mRegisterClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			LocationManager lm = new LocationManager(App.getDatabaseSource(v.getContext()));
			MLocation loc = lm.getLocation("arrillaga.stanford@gmail.com");
			DbFeed feed = mMusubi.getFeed(loc.feedUri);
	    	Request req = new Request("register");
	    	req.addParam("dorm", "McFarland")
	    		.addParam("department", "Computer Science");
	    	feed.insert(new MemObj("omnistanford", req.toJSON(v.getContext())));
	    	Log.d(TAG, feed.getLatestObj().getJson().toString());
		}
    };
    
    private void bindServices() {
    	Intent locationService = new Intent(this, LocationService.class);
    	startService(locationService);
    }
    
//    private Interpolator accelerator = new AccelerateInterpolator();
//    private Interpolator decelerator = new DecelerateInterpolator();
//    private void flipit() {
//    	final View visibleList;
//        final View invisibleList;
//        if(mButtonView.getVisibility() == View.GONE) {
//        	visibleList = mSettingsView;
//        	invisibleList = mButtonView;
//        } else {
//        	invisibleList = mSettingsView;
//        	visibleList = mButtonView;
//        }
//        ObjectAnimator visToInvis = ObjectAnimator.ofFloat(visibleList, "rotationY", 0f, 90f);
//        visToInvis.setDuration(500);
//        visToInvis.setInterpolator(accelerator);
//        final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(invisibleList, "rotationY",
//                -90f, 0f);
//        invisToVis.setDuration(500);
//        invisToVis.setInterpolator(decelerator);
//        visToInvis.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator anim) {
//            	visibleList.setVisibility(View.GONE);
//                invisToVis.start();
//                invisibleList.setVisibility(View.VISIBLE);
//            }
//        });
//        visToInvis.start();
//    }
}