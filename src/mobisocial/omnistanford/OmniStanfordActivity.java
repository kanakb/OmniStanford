package mobisocial.omnistanford;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MAccount;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.db.SimpleCursorLoader;
import mobisocial.omnistanford.server.ui.CheckinListFragment;
import mobisocial.omnistanford.service.LocationService;
import mobisocial.omnistanford.ui.PullToRefreshListView;
import mobisocial.omnistanford.ui.PullToRefreshListView.OnRefreshListener;
import mobisocial.omnistanford.util.Util;
import mobisocial.socialkit.musubi.DbFeed;
import mobisocial.socialkit.musubi.Musubi;

public class OmniStanfordActivity extends OmniStanfordBaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "OmniStanfordActivity";
    
    public static final long MONTH = 1000L * 60L * 60L * 24L * 30L;
    
    private static final int CHECKIN_CURSOR_LOADER = 0x01;
    
    private Musubi mMusubi;
    private LinearLayout mButtonView;
    private HashMap<Long, Long> mIdMap;
    private PullToRefreshListView mListView;
    private CursorAdapter mCursorAdapter;
    private CheckinManager mCm;
    private Loader<Cursor> mLoader;
    private HashMap<Long, Bitmap> mBitmapCache;
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CREATE_FEED) {
            if (resultCode == RESULT_OK) {
                Uri feedUri = data.getData();
                if (feedUri != null) {
                    // Single feed
                    Log.d(TAG, "Feed URI: " + feedUri);
                    
                    if (mMusubi != null) {
                        DbFeed feed = mMusubi.getFeed(feedUri);
                        mMusubi.setFeed(feed);
                    }
                }
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Sherlock_ForceOverflow);
        
        if(setServerMode()) {
        	FragmentManager fragmentManager = getSupportFragmentManager();
        	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        	CheckinListFragment fragment = new CheckinListFragment();
        	fragmentTransaction.add(R.id.contentArea, fragment);
        	fragmentTransaction.commit();
        } else {
            if (Util.isFirstTime(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("In a minute, " +
                        "OmniStanford will be able to find locations for you automatically.")
                        .setTitle("Welcome to OmniStanford!")
                        .setCancelable(false)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Util.setFirstTime(OmniStanfordActivity.this);
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
            
            mButtonView = new LinearLayout(this);
            mButtonView.setOrientation(LinearLayout.VERTICAL);
            
            mIdMap = new HashMap<Long, Long>();
            mBitmapCache = new HashMap<Long, Bitmap>();
            
            mCm = new CheckinManager(App.getDatabaseSource(this));
            
            getSupportLoaderManager().initLoader(CHECKIN_CURSOR_LOADER, null, this);
            new Thread() {
                @Override
                public void run() {
                    constructMap(mCm.getRecentCheckins(MONTH));
                }
            };
            mCursorAdapter = new CheckinsAdapter(
                    this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            
//            Button checkinButton = new Button(this);
//            checkinButton.setText("Checkin");
//            checkinButton.setOnClickListener(mCheckinClickListener);
//            mButtonView.addView(checkinButton);
//            
//            Button checkoutButton = new Button(this);
//            checkoutButton.setText("Checkout");
//            checkoutButton.setOnClickListener(mCheckoutClickListener);
//            mButtonView.addView(checkoutButton);
            
            ((LinearLayout)findViewById(R.id.contentArea)).addView(mButtonView);
            
            if (Musubi.isMusubiInstalled(this)) {
                mMusubi = Musubi.getInstance(this);
            }

            TextView tv = new TextView(this);
            tv.setTextSize(20.0f);
            tv.setTextColor(Color.WHITE);
            tv.setText("Recent Locations");
            List<MCheckinData> checkins = mCm.getRecentCheckins();
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
            //mButtonView.addView(tv);
            
            showRecent(mButtonView);
            
            // Do some location updates
            if (Musubi.isMusubiInstalled(this)) {
                new CreateFeedsTask().execute(App.getDatabaseSource(this));
            }
            
            bindServices(null);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (Musubi.isMusubiInstalled(this)) {
            new CreateFeedsTask().execute(App.getDatabaseSource(this));
        }
        new RefreshListTask().execute(0L);
    }
    
    private void constructMap(List<MCheckinData> checkins) {
        Log.d(TAG, "refreshing map");
        synchronized(mIdMap) {
            mIdMap.clear();
            for (MCheckinData checkin : checkins) {
                mIdMap.put(new Long(mIdMap.size() + 1), checkin.id);
            }
        }
    }
    
    private void showRecent(LinearLayout wrapper) {
        mListView = new PullToRefreshListView(this);
        mListView.setAdapter(mCursorAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                //do something
                if (!Musubi.isMusubiInstalled(OmniStanfordActivity.this)) {
                    Toast.makeText(
                            OmniStanfordActivity.this, "Musubi not installed!", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                Log.d(TAG, "Selected " + position);
                Intent intent = new Intent(OmniStanfordActivity.this, SelectContactsActivity.class);
                if (mIdMap.containsKey(new Long(position))) {
                    intent.putExtra("checkin", mIdMap.get(new Long(position)).longValue());
                }
                startActivity(intent);
            }
        });
        mListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshListTask().execute(1L);
            }
        });
        
        wrapper.addView(mListView);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSherlock().getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
            	Intent create = new Intent(this, SettingsActivity.class);
            	startActivity(create);
            	return true;
            case R.id.menu_add_account:
                if (!Musubi.isMusubiInstalled(this)) {
                    Toast.makeText(this, "Musubi not installed!", Toast.LENGTH_SHORT).show();
                    return true;
                }
            	Intent picker = new Intent(ACTION_OWNED_ID_PICKER);
            	try {
            	    startActivityForResult(picker, REQUEST_PICK_ID);
            	} catch (ActivityNotFoundException e) {
            	    Log.w(TAG, "activity not found", e);
            	    Toast.makeText(
            	            this,
            	            "The installed version of Musubi does not support discovery features.",
            	            Toast.LENGTH_SHORT).show();
            	}
                return true;
            case R.id.menu_schedule:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                	Intent intent = new Intent(OmniStanfordActivity.this, ScheduleActivity.class);
    				startActivity(intent);
                } else {
                    Toast.makeText(this, "Android 3.0+ required!", Toast.LENGTH_SHORT).show();
                }
				return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
//    private OnClickListener mCheckinClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			Request req = new Request("gates.stanford@gmail.com", "checkin", null);
//			req.addParam("loc_id", "1")
//				.addParam("dorm", "McFarland")
//				.addParam("department", "CS");
//			req.send(v.getContext());
//			bindServices("locationUpdate");
//		}
//    };
    
//    private OnClickListener mCheckoutClickListener = new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//		    Request req = new Request("gates.stanford@gmail.com", "checkout", null);
//            req.send(v.getContext());
//            
//		    Log.d(TAG, "Checkout");
//            // Exit open checkins
//		    CheckinManager cm = new CheckinManager(
//		            App.getDatabaseSource(OmniStanfordActivity.this));
//		    LocationManager lm = new LocationManager(
//		            App.getDatabaseSource(OmniStanfordActivity.this));
//            List<MCheckinData> checkins = cm.getRecentOpenCheckins(MONTH);
//            for (MCheckinData data : checkins) {
//                if (data.exitTime == null || data.exitTime == 0) {
//                    data.exitTime = System.currentTimeMillis();
//                    cm.updateCheckin(data);
//                    MLocation loc = lm.getLocation(data.locationId);
//                    Log.d(TAG, "Checking out from " + loc.name + " for: " + data.id);
//                    Request request = new Request(loc.principal, "checkout", null);
//                    request.send(v.getContext());
//                } else {
//                    Log.d(TAG, "exit time: " + data.exitTime + " for: " + data.id);
//                }
//            }
//		}
//    };
    
    private void bindServices(String extra) {
    	Intent locationService = new Intent(this, LocationService.class);
    	if (extra != null) {
    	    locationService.putExtra(extra, true);
    	}
    	startService(locationService);
    }
    
    private boolean setServerMode() {
        LocationManager lm = new LocationManager(App.getDatabaseSource(this));
        List<MLocation> locations = lm.getLocations();
        MAccount account = Util.loadAccount(this);
        if(account != null) {
        	for(MLocation loc : locations) {
        		String locHash = Base64.encodeToString(
        				digestPrincipal(loc.principal), Base64.NO_WRAP);
        		if(locHash.equals(account.identifier)) {
        			((App)getApplicationContext()).setServerMode(true);
        			return true;
        		}
        	}
        }
        
        return false;
    }
    
    class RefreshListTask extends AsyncTask<Long, Void, Long> {
        @Override
        protected Long doInBackground(Long... params) {
            if (mCm != null) {
                constructMap(mCm.getRecentCheckins(MONTH));
            }
            return params[0];
        }
        
        @Override
        protected void onPostExecute(Long data) {
            updateCursor();
            
            if (data == 1L) {
                mListView.onRefreshComplete();
            }
        }
    }
    
    private void updateCursor() {
        if (mLoader != null && mLoader.isStarted()) {
            Log.d(TAG, "updating cursor");
            mLoader.forceLoad();
        }
    }
    
    private static class CheckinHolder {
        public TextView titleView;
        public TextView subtitleView;
        public TextView subtitle2View;
        public ImageView imageView;
        public TextView separatorView;
    }
    
    private class CheckinsAdapter extends CursorAdapter {
        private LocationManager mLm;
        private String mTitle = "";
        private String mPreviousTitle = "";
        
        public CheckinsAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mLm = new LocationManager(App.getDatabaseSource(OmniStanfordActivity.this));
        }
        
        @Override
        public void changeCursor(Cursor c) {
            Log.d(TAG, "Changing cursor");
            super.changeCursor(c);
        }
        
        @Override
        public void bindView(View view, Context context, Cursor c) {
            final CheckinHolder holder = (CheckinHolder)view.getTag();
            
            MCheckinData checkin = mCm.fillInStandardFields(c);
            MLocation where = mLm.getLocation(checkin.locationId);
            mTitle = getTitleForTime(checkin);
            
            // Show separator if needed
            boolean needSeparator = false;
            final int position = c.getPosition();
            if (position == 0) {
                needSeparator = true;
            } else {
                c.moveToPosition(position - 1);
                MCheckinData previous = mCm.fillInStandardFields(c);
                mPreviousTitle = getTitleForTime(previous);
                if (!mTitle.equals(mPreviousTitle)) {
                    needSeparator = true;
                }
                c.moveToPosition(position);
            }
            if (needSeparator) {
                holder.separatorView.setText(mTitle);
                holder.separatorView.setVisibility(View.VISIBLE);
            } else {
                holder.separatorView.setVisibility(View.GONE);
            }
            
            // Title is name of the location
            holder.titleView.setText(where.name);
            
            // Set subtitles based on current state
            Format fullFormatter = new SimpleDateFormat("M/d/yyyy h:mm a");
            if (checkin.exitTime == null || checkin.exitTime == 0L) {
                holder.subtitleView.setText("You are currently here");
                holder.subtitle2View.setText(
                        "Entered at " + fullFormatter.format(new Date(checkin.entryTime)));
            } else {
                holder.subtitleView.setText(
                        "Entered at " + fullFormatter.format(new Date(checkin.entryTime)));
                holder.subtitle2View.setText(
                        "Left at " + fullFormatter.format(new Date(checkin.exitTime)));
            }
            
            if (where.image != null && where.image.length > 0) {
                if (mBitmapCache.containsKey(where.id)) {
                    holder.imageView.setImageBitmap(mBitmapCache.get(where.id));
                } else {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(where.image, 0, where.image.length);
                    mBitmapCache.put(where.id, bitmap);
                    holder.imageView.setImageBitmap(bitmap);
                }
            } else {
                holder.imageView.setImageResource(R.drawable.stanford);
            }
        }
        
        @Override
        public View newView(Context context, Cursor c, ViewGroup parent) {
            View v = LayoutInflater.from(context).inflate(R.layout.plain_list_item, parent, false);
            CheckinHolder holder = new CheckinHolder();
            holder.titleView = (TextView)v.findViewById(R.id.plainTitle);
            holder.subtitleView = (TextView)v.findViewById(R.id.plainSubtitle);
            holder.subtitle2View = (TextView)v.findViewById(R.id.plainSubtitle2);
            holder.imageView = (ImageView)v.findViewById(R.id.locationImage);
            holder.separatorView = (TextView)v.findViewById(R.id.plainSeparator);
            v.setTag(holder);
            return v;
        }
        
        private String getTitleForTime(MCheckinData data) {
            // Get the checkin time
            long checkinTime = data.entryTime;
            if (data.exitTime != null && data.exitTime != 0L) {
                checkinTime = data.exitTime;
            }
            Calendar checkinCal = Calendar.getInstance();
            checkinCal.setTimeInMillis(checkinTime);
            
            // Get the current time
            long now = System.currentTimeMillis();
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(now);
            
            if (isSameDay(checkinCal, nowCal)) {
                return "Today";
            } else if (isSameDay(checkinCal, nowCal, 1)) {
                return "Yesterday";
            } else if (isSameWeek(checkinCal, nowCal)) {
                return "This Week";
            } else if (isSameWeek(checkinCal, nowCal, 1)) {
                return "Last Week";
            } else if (isSameMonth(checkinCal, nowCal)) {
                return "This Month";
            }
            return "Older Locations";
        }
        
        private boolean isSameDay(Calendar prev, Calendar curr) {
            return prev.get(Calendar.YEAR) == curr.get(Calendar.YEAR) &&
                    prev.get(Calendar.DAY_OF_YEAR) == curr.get(Calendar.DAY_OF_YEAR);
        }
        
        private boolean isSameDay(Calendar prev, Calendar curr, int offset) {
            Calendar newPrev = Calendar.getInstance();
            newPrev.setTimeInMillis(prev.getTimeInMillis());
            newPrev.add(Calendar.DATE, offset);
            return isSameDay(newPrev, curr);
        }
        
        private boolean isSameWeek(Calendar prev, Calendar curr) {
            return prev.get(Calendar.YEAR) == curr.get(Calendar.YEAR) &&
                    prev.get(Calendar.WEEK_OF_YEAR) == curr.get(Calendar.WEEK_OF_YEAR);
        }
        
        private boolean isSameWeek(Calendar prev, Calendar curr, int offset) {
            Calendar newPrev = Calendar.getInstance();
            newPrev.setTimeInMillis(prev.getTimeInMillis());
            newPrev.add(Calendar.WEEK_OF_YEAR, offset);
            return isSameDay(newPrev, curr);
        }
        
        private boolean isSameMonth(Calendar prev, Calendar curr) {
            return prev.get(Calendar.YEAR) == curr.get(Calendar.YEAR) &&
                    prev.get(Calendar.MONTH) == curr.get(Calendar.MONTH);
        }
    }
    
    private static class CheckinCursorLoader extends SimpleCursorLoader {
        private long mDuration;
        private CheckinManager mCm;

        public CheckinCursorLoader(Context context, CheckinManager cm, long duration) {
            super(context);
            mDuration = duration;
            mCm = cm;
        }

        @Override
        public Cursor loadInBackground() {
            return mCm.getRecentCheckinsCursor(mDuration);
        }
        
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        mLoader = new CheckinCursorLoader(this, mCm, MONTH);
        mBitmapCache.clear();
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished");
        Cursor oldCursor = mCursorAdapter.swapCursor(cursor);
        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            Log.d(TAG, "replaced cursor");
            oldCursor.close();
        }
        mBitmapCache.clear();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        Cursor oldCursor = mCursorAdapter.swapCursor(null);
        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }
        mBitmapCache.clear();
    }
}