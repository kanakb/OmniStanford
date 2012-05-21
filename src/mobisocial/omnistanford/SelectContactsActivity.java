package mobisocial.omnistanford;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.ui.PullToRefreshListView;
import mobisocial.omnistanford.ui.PullToRefreshListView.OnRefreshListener;
import mobisocial.omnistanford.util.Request;
import mobisocial.omnistanford.util.ResponseHandler;
import mobisocial.socialkit.musubi.DbObj;
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
    private Request mReq;
    private List<HashMap<String, String>> mList;
    private SimpleAdapter mAdapter;
    private CheckinManager mCm;
    private MCheckinData mCheckin;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Don't bother if there's no data
        if (getIntent() == null || !getIntent().hasExtra("checkin")) {
            finish();
            return;
        }
        
        // Get valid checkin data
        mCm = new CheckinManager(App.getDatabaseSource(this));
        mCheckin = mCm.getCheckin(getIntent().getLongExtra("checkin", -1));
        if (mCheckin == null) {
            finish();
            return;
        }
        
        LinearLayout wrapper = (LinearLayout)findViewById(R.id.contentArea);
        
        String[] from = new String[] { "separator", "title", "subtitle" };
        int[] to = new int[] { R.id.separator, R.id.title, R.id.subtitle };
        
        mList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 10; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("separator", "separator " + i);
            hm.put("title", "title " + i);
            hm.put("subtitle", "subtitle" + i);
            mList.add(hm);
        }
        
        // TODO: should use a CursorAdapter here
        mListView = new PullToRefreshListView(this);
//        final ListView lv = new ListView(this);
        mAdapter = new SimpleAdapter(this, mList, R.layout.list_item, from, to);
        mListView.setAdapter(mAdapter);
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

    class GetDataTask extends AsyncTask<Long, Integer, Void> {
		@Override
		protected Void doInBackground(Long... locationId) {
			mReq = new Request("arrillaga.stanford@gmail.com", "checkin", new MyHandler());
			mReq.addParam("loc_id", "1");
			mReq.send(getApplicationContext());
        	
			return null;
		}
    }
    
    class MyHandler implements ResponseHandler {
		@Override
		public void OnResponse(DbObj obj) {
			JSONObject json;
			try {
				json = obj.getJson().optJSONArray("res").getJSONObject(0);
				Log.i("MyHandler", json.toString());
				HashMap<String, String> entry = mList.get(0);
				entry.put("separator", json.optString("name"));
				entry.put("title", json.optString("type"));
				entry.put("subtitle", json.optString("principal"));
				mAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}
			
        	mListView.onRefreshComplete();
		}
    }
    
}
