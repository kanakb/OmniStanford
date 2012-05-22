package mobisocial.omnistanford;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.DiscoveredPersonManager;
import mobisocial.omnistanford.db.DiscoveryManager;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.db.MDiscoveredPerson;
import mobisocial.omnistanford.db.MDiscovery;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.db.MUserProperty;
import mobisocial.omnistanford.db.PropertiesManager;
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
    private List<HashMap<String, String>> mList;
    private SimpleAdapter mAdapter;
    private CheckinManager mCm;
    private MCheckinData mCheckin;
    private Request mRequest;
    private boolean mLocal;
    
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
        mLocal = (mCheckin.exitTime == null || mCheckin.exitTime == 0L) ? false : true;
        
        LinearLayout wrapper = (LinearLayout)findViewById(R.id.contentArea);
        
        String[] from = new String[] { "separator", "title", "subtitle" };
        int[] to = new int[] { R.id.separator, R.id.title, R.id.subtitle };
        
        mList = new ArrayList<HashMap<String, String>>();
        updateLocal();
        
        mListView = new PullToRefreshListView(this);
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
    
    private void updateLocal() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.clear();
                DiscoveryManager dm = new DiscoveryManager(
                        App.getDatabaseSource(SelectContactsActivity.this));
                DiscoveredPersonManager dpm = new DiscoveredPersonManager(
                        App.getDatabaseSource(SelectContactsActivity.this));
                PropertiesManager pm = new PropertiesManager(
                        App.getDatabaseSource(SelectContactsActivity.this));
                List<MDiscovery> discoveries = dm.getDiscoveries(mCheckin.id);
                for (MDiscovery disc : discoveries) {
                    MDiscoveredPerson person = dpm.getPerson(disc.personId);
                    HashMap<String, String> hm = new HashMap<String, String>();
                    MUserProperty prop = pm.getProperty(disc.connectionType);
                    if (prop != null) {
                        hm.put("separator", prop.value);
                    } else {
                        hm.put("separator", disc.connectionType);
                    }
                    hm.put("title", person.name);
                    hm.put("subtitle", person.accountType);
                    mList.add(hm);
                }
            }
        });
    }

    class GetDataTask extends AsyncTask<Long, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Long... locationId) {
		    // If the checkin is still active, ask the server for an update
		    if (!mLocal) {
		        LocationManager lm = new LocationManager(
		                App.getDatabaseSource(SelectContactsActivity.this));
		        PropertiesManager pm = new PropertiesManager(
		                App.getDatabaseSource(SelectContactsActivity.this));
		        MLocation match = lm.getLocation(mCheckin.locationId);
		        if (match == null) {
		            return true;
		        }
                mRequest = new Request(match.principal, "checkin", mResponseHandler);
                mRequest.addParam("id", new Long(mCheckin.id).toString());
                MUserProperty dorm = pm.getProperty(SettingsActivity.RESIDENCE);
                if (dorm != null) {
                    mRequest.addParam(SettingsActivity.RESIDENCE, dorm.value);
                }
                MUserProperty department = pm.getProperty(SettingsActivity.DEPARTMENT);
                if (department != null) {
                    mRequest.addParam(SettingsActivity.DEPARTMENT, department.value);
                }
                MUserProperty enabled = pm.getProperty(SettingsActivity.ENABLED);
                if (enabled != null) {
                    boolean shouldSend = "true".equals(enabled.value) ? true : false;
                    if (shouldSend) {
                        mRequest.send(SelectContactsActivity.this);
                    }
                }
		    }
        	
			return mLocal;
		}
		
		@Override
		protected void onPostExecute(Boolean status) {
		    if (status) {
		        mListView.onRefreshComplete();
		    }
		}
    }
    
    private ResponseHandler mResponseHandler = new ResponseHandler() {
        @Override
        public void OnResponse(DbObj obj) {
            Log.d(TAG, "got a response");
            JSONObject json = obj.getJson();
            if (!json.optString("id").equals("")) {
                long checkinId = Long.parseLong(json.optString("id"));
                JSONArray arr = json.optJSONArray("res");
                if (arr != null) {
                    DiscoveredPersonManager dpm = new DiscoveredPersonManager(
                            App.getDatabaseSource(SelectContactsActivity.this));
                    DiscoveryManager dm = new DiscoveryManager(
                            App.getDatabaseSource(SelectContactsActivity.this));
                    PropertiesManager pm = new PropertiesManager(
                            App.getDatabaseSource(SelectContactsActivity.this));
                    MUserProperty myDorm = pm.getProperty(SettingsActivity.RESIDENCE);
                    MUserProperty myDept = pm.getProperty(SettingsActivity.DEPARTMENT);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject match = arr.optJSONObject(i);
                        if (match != null) {
                            MDiscoveredPerson person = new MDiscoveredPerson();
                            person.name = match.optString("name");
                            person.identifier = match.optString("principal");
                            person.accountType = match.optString("type");
                            dpm.ensurePerson(person);
                            MDiscovery discovery = new MDiscovery();
                            discovery.checkinId = checkinId;
                            discovery.personId = person.id;
                            if (myDorm != null && myDorm.value.equals(match.optString("dorm"))) {
                                discovery.connectionType = SettingsActivity.RESIDENCE;
                            } else if (myDept != null && myDorm.value.equals(match.optString("department"))) {
                                discovery.connectionType = SettingsActivity.DEPARTMENT;
                            } else {
                                continue;
                            }
                            dm.insertDiscovery(discovery);
                        }
                    }
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateLocal();
                    mListView.onRefreshComplete();
                }
            });
        }
    };
    
    @Override
    public void onDestroy() {
        mRequest.cancelUpdates();
        super.onDestroy();
    }
}
