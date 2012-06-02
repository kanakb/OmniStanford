package mobisocial.omnistanford;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

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
import mobisocial.omnistanford.util.Util;
import mobisocial.socialkit.musubi.DbObj;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

public class SelectContactsActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "SelectContactsActivity";
    
    private static final String ACCOUNT_TYPE_STANFORD = "edu.stanford";
    private static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    private static final String ACCOUNT_TYPE_FACEBOOK = "com.facebook.auth.login";
    private static final String ACCOUNT_TYPE_PHONE = "mobisocial.musubi.phone";
    
    private static final String FAMILIAR_STANFORD = "Stanford Account";
    private static final String FAMILIAR_GOOGLE = "Google Account";
    private static final String FAMILIAR_FACEBOOK = "Facebook Account";
    private static final String FAMILIAR_PHONE = "Phone Account";
    
    private PullToRefreshListView mListView;
    private CursorAdapter mCursorAdapter;
    private CheckinManager mCm;
    private MCheckinData mCheckin;
    private Request mRequest;
    private boolean mLocal;
    private DiscoveryManager mDm;
    private HashMap<Long, Long> mPersonMap;
    private HashMap<String, String> mProviderMap;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSherlock().getMenuInflater();
        inflater.inflate(R.menu.contact_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_start_feed:
                Log.d(TAG, "starting feed...");
                initiateNewFeed();
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, OmniStanfordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                Log.d(TAG, "other menu item selected");
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void initiateNewFeed() {
        Intent create = new Intent(ACTION_CREATE_STANFORD_FEED);
        HashSet<Long> addedSet = new HashSet<Long>();
        JSONObject primary = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            primary.put("visible", true);
            DiscoveredPersonManager dpm = new DiscoveredPersonManager(App.getDatabaseSource(this));
            Log.d(TAG, "person map size: " + mPersonMap.size());
            for (Long selId : mListView.getCheckedItemIds()) {
                Long personId = mPersonMap.get(selId);
                MDiscoveredPerson person = dpm.getPerson(personId);
                if (person == null || addedSet.contains(personId)) {
                    continue;
                }
                addedSet.add(personId);
                JSONObject one = new JSONObject();
                one.put("hashed", person.identifier);
                one.put("name", person.name);
                Log.d(TAG, "Adding " + person.name);
                one.put("type", person.accountType);
                arr.put(one);
            }
            primary.put("members", arr);
            primary.put("sender", Util.getPickedAccountType(this));
        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
            return;
        }
        create.putExtra(EXTRA_NAME, primary.toString());
        startActivityForResult(create, REQUEST_CREATE_FEED);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_FEED) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Toast.makeText(this, "Successfully created feed!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_MAIN);
                PackageManager pm = getPackageManager();
                intent = pm.getLaunchIntentForPackage("mobisocial.musubi");
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setData(data.getData());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Could not create feed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mProviderMap = new HashMap<String, String>();
        mProviderMap.put(ACCOUNT_TYPE_STANFORD, FAMILIAR_STANFORD);
        mProviderMap.put(ACCOUNT_TYPE_GOOGLE, FAMILIAR_GOOGLE);
        mProviderMap.put(ACCOUNT_TYPE_FACEBOOK, FAMILIAR_FACEBOOK);
        mProviderMap.put(ACCOUNT_TYPE_PHONE, FAMILIAR_PHONE);
        
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
        Log.d(TAG, "checkinId: " + mCheckin.id);
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
                mCursorAdapter.notifyDataSetChanged();
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
        
        getSupportActionBar().setTitle("Start Something");
    }
    
    @Override
    public void onResume() {
    	alternateUpdate();
    	super.onResume();
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
            Log.d(TAG, json.toString());
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
                                Log.d(TAG, "dorm match for " + checkinId);
                                dm.ensureDiscovery(discovery);
                            }
                            if (myDept != null && myDept.value.equals(match.optString("department"))) {
                                discovery.connectionType = SettingsActivity.DEPARTMENT;
                                Log.d(TAG, "department match for " + checkinId);
                                dm.ensureDiscovery(discovery);
                            }
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
        public void changeCursor(Cursor c) {
            Log.d(TAG, "Changing cursor");
            super.changeCursor(c);
            if (mListView != null && mListView.getCheckedItemPositions() != null) {
                mListView.getCheckedItemPositions().clear();
            }
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final DiscoveryHolder holder = (DiscoveryHolder)view.getTag();
            holder.titleView.setChecked(false);
            
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
            if (mListView != null && mListView.getCheckedItemPositions() != null) {
                holder.titleView.setChecked(mListView.getCheckedItemPositions().get(position + 1));
            }
            
            // Set subtitle
            if (mProviderMap != null && mProviderMap.containsKey(person.accountType)) {
                holder.subtitleView.setText(mProviderMap.get(person.accountType));
            } else {
                holder.subtitleView.setText(person.accountType);
            }
            
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
