package mobisocial.omnistanford;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONObject;

import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.ui.PullToRefreshListView;
import mobisocial.omnistanford.ui.PullToRefreshListView.OnRefreshListener;
import mobisocial.omnistanford.util.Request;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.FeedObserver;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SelectContactsActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "SelectContactsActivity";
    private PullToRefreshListView mListView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout wrapper = (LinearLayout)findViewById(R.id.contentArea);
        
        String[] from = new String[] { "separator", "title", "subtitle" };
        int[] to = new int[] { R.id.separator, R.id.title, R.id.subtitle };
        
        List<HashMap<String, String>> hms = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("separator", "separator " + i);
            hm.put("title", "title " + i);
            hm.put("subtitle", "subtitle" + i);
            hms.add(hm);
        }
        
        // TODO: should use a CursorAdapter here
        mListView = new PullToRefreshListView(this);
//        final ListView lv = new ListView(this);
        mListView.setAdapter(new SimpleAdapter(this, hms, R.layout.list_item, from, to));
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                TextView selected = (TextView) arg1.findViewById(R.id.title);
                Log.d(TAG, selected.getText().toString());
                Log.d(TAG, arg2 + " " + arg3);
                for (long id : mListView.getCheckedItemIds()) {
                    Log.d(TAG, "selected" + id);
                }
                //selected.setTextColor(getResources().getColor(android.R.color.darker_gray));
                //selected.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
        });
        
        mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new GetDataTask().execute();
			}
        });
        
        wrapper.addView(mListView);
        Log.d(TAG, "Showing list");
    }

    // TODO: the async nature of feed makes it hard to get response in place.
    // need to place a progress bar on screen before we get response
    private class GetDataTask extends AsyncTask<Long, Integer, Void> {
        @Override
        protected void onPostExecute(Void r) {
//            mListItems.addFirst("Added after refresh...");
            // Call onRefreshComplete when the list has been refreshed.
        	mListView.onRefreshComplete();
            super.onPostExecute(r);
        }

		@Override
		protected Void doInBackground(Long... locationId) {
			Context context = getApplicationContext();
			Musubi musubi = Musubi.getInstance(context);
			LocationManager lm = new LocationManager(App.getDatabaseSource(context));
			MLocation loc = lm.getLocation("arrillaga.stanford@gmail.com");
			DbFeed feed = musubi.getFeed(loc.feedUri);
	    	Request req = new Request("checkin");
	    	req.addParam("lon", "1").addParam("lat", "2");
	    	feed.insert(new MemObj("omnistanford", req.toJSON(context)));
	    	Log.d(TAG, feed.getLatestObj().getJson().toString());
			return null;
		}
    }
    
}
