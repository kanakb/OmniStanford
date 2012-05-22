package mobisocial.omnistanford;

import java.util.HashMap;

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
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SelectContactsActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "SelectContactsActivity";
    private PullToRefreshListView mListView;
    private CursorAdapter mCursorAdapter;
    private CheckinManager mCm;
    private MCheckinData mCheckin;
    private Request mRequest;
    private boolean mLocal;
    private DiscoveryManager mDm;
    private HashMap<Long, Long> mPersonMap;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Don't bother if there's no data
        if (getIntent() == null || !getIntent().hasExtra("checkin")) {
            finish();
            return;
        }
        
        mDm = new DiscoveryManager(App.getDatabaseSource(this));
        mPersonMap = new HashMap<Long, Long>();
        
        // Get valid checkin data
        mCm = new CheckinManager(App.getDatabaseSource(this));
        mCheckin = mCm.getCheckin(getIntent().getLongExtra("checkin", -1));
        if (mCheckin == null) {
            finish();
            return;
        }
        mLocal = (mCheckin.exitTime == null || mCheckin.exitTime == 0L) ? false : true;
        
        LinearLayout wrapper = (LinearLayout)findViewById(R.id.contentArea);
        mCursorAdapter = new ContactsAdapter(this, mDm.getDiscoveriesCursor(mCheckin.id));
        alternateUpdate();
        
        mListView = new PullToRefreshListView(this);
        mListView.setAdapter(mCursorAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                DiscoveryHolder holder = (DiscoveryHolder)view.getTag();
                mPersonMap.put(id, holder.personId);
                CheckedTextView selected = (CheckedTextView) view.findViewById(R.id.title);
                Log.d(TAG, selected.getText().toString());
                Log.d(TAG, position + " " + id);
                for (long selId : mListView.getCheckedItemIds()) {
                    Log.d(TAG, "selected " + selId);
                }
                //parent.getChildAt(position).setBackgroundColor(Color.BLUE);
                //selected.setTextColor(getResources().getColor(android.R.color.darker_gray));
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
    
    private void alternateUpdate() {
        Cursor c = mDm.getDiscoveriesCursor(mCheckin.id);
        if (c != null) {
            startManagingCursor(c);
        }
        mCursorAdapter.changeCursor(c);
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
                    alternateUpdate();
                    mListView.onRefreshComplete();
                }
            });
        }
    };
    
    @Override
    public void onDestroy() {
        if (mRequest != null) {
            mRequest.cancelUpdates();
        }
        super.onDestroy();
    }
    
    private static class DiscoveryHolder {
        public TextView separator;
        public CheckedTextView titleView;
        public TextView subtitleView;
        public Long personId;
    }
    
    private class ContactsAdapter extends CursorAdapter {
        private String mTitle = "";
        private String mPreviousTitle = "";
        private DiscoveredPersonManager mDpm;
        private PropertiesManager mPm;
        
        public ContactsAdapter(Context context, Cursor c) {
            super(context, c);
            mDpm = new DiscoveredPersonManager(
                    App.getDatabaseSource(SelectContactsActivity.this));
            mPm = new PropertiesManager(
                    App.getDatabaseSource(SelectContactsActivity.this));
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final DiscoveryHolder holder = (DiscoveryHolder)view.getTag();
            
            // Get the current separator label
            MDiscovery current = mDm.fillInStandardFields(cursor);
            MUserProperty currentProp = mPm.getProperty(current.connectionType);
            if (currentProp == null) {
                mTitle = current.connectionType;
            } else {
                mTitle = currentProp.value;
            }
            
            // Check if the separator above is the same
            boolean needSeparator = false;
            final int position = cursor.getPosition();
            if (position == 0) {
                needSeparator = true;
            } else {
                cursor.moveToPosition(position - 1);
                MDiscovery previous = mDm.fillInStandardFields(cursor);
                MUserProperty prop = mPm.getProperty(previous.connectionType);
                if (prop == null) {
                    mPreviousTitle = previous.connectionType;
                } else {
                    mPreviousTitle = prop.value;
                }
                if (!mTitle.equals(mPreviousTitle)) {
                    needSeparator = true;
                }
                cursor.moveToPosition(position);
            }
            
            // Show the separator if needed
            if (needSeparator) {
                holder.separator.setText(mTitle);
                holder.separator.setVisibility(View.VISIBLE);
            } else {
                holder.separator.setVisibility(View.GONE);
            }
            
            // Set main title
            MDiscoveredPerson person = mDpm.getPerson(current.personId);
            holder.titleView.setText(person.name);
            holder.titleView.setChecked(true);
            
            // Set subtitle
            holder.subtitleView.setText(person.accountType);
            
            // Save this person
            holder.personId = current.personId;
        }
        
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            DiscoveryHolder dh = new DiscoveryHolder();
            dh.separator = (TextView) v.findViewById(R.id.separator);
            dh.titleView = (CheckedTextView) v.findViewById(R.id.title);
            dh.subtitleView = (TextView) v.findViewById(R.id.subtitle);
            v.setTag(dh);
            return v;
        }
    }
}
