package mobisocial.omnistanford;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import mobisocial.omnistanford.ScheduleListFragment.TimeSlot;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.db.MTag;
import mobisocial.omnistanford.db.TagManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ScheduleActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "ScheduleActivity";
    public static final long DAY = 1000L * 60L * 60L * 24L * 30L;
    public static final String PREFS_NAME = "ScheduleListPrefs";
	public static final String TAG_PREF_KEY = "tags";
    
    private ViewPager mViewPager;
    private SchedulePagerAdapter mPagerAdapter;
	private GridLayout mTagsList;
	private EditText mTagEditText;


 
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);

		 LinearLayout wrapper = (LinearLayout) findViewById(R.id.contentArea);
		 LayoutInflater inflater = LayoutInflater.from(this);
		 LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.schedule, wrapper);
		 mViewPager = (ViewPager) layout.findViewById(R.id.schedulepager);
		 Calendar calendar = Calendar.getInstance();
		 mPagerAdapter = new SchedulePagerAdapter(getSupportFragmentManager(), 
				 calendar.get(Calendar.MONTH));
		 mViewPager.setAdapter(mPagerAdapter);
		 mViewPager.setCurrentItem(calendar.get(Calendar.DATE) - 1);

		 // set up tags area
		 mTagsList = (GridLayout) findViewById(R.id.tagListArea);

		 Set<String> tags = getTagSet(this);
		 if(tags != null) {
			 for(String tagStr : tags) {
				 TagFrameLayout tag = new TagFrameLayout(this, tagStr, true);
				 mTagsList.addView(tag);
			 }
		 } 
		 mTagEditText = (EditText) findViewById(R.id.tagEditText);
		 Button btn = (Button) findViewById(R.id.tagCreateButton);
		 btn.setOnClickListener(mOnCreateButtonClickListener);
	 }
	 
	 private OnClickListener mOnCreateButtonClickListener = new OnClickListener() {
		 @Override
		 public void onClick(View v) {
			 if(mTagsList.getChildCount() < 8) {
				 String tagStr = mTagEditText.getText().toString();
				 Set<String> tags = getTagSet(ScheduleActivity.this);
				 if(!tags.contains(tagStr)) {
					 tags.add(tagStr);
					 TagFrameLayout newTag = new TagFrameLayout(v.getContext(), tagStr, true);
					 mTagEditText.setText("");
					 mTagsList.addView(newTag);
					 
					 saveTagSet(ScheduleActivity.this, tags);
				 }
			 } else {
				 Toast.makeText(v.getContext(), "You can make at most 8 tags", Toast.LENGTH_SHORT).show();
			 }
		 }
	 };
	 
	 protected void onDestroy () {
		
		 
		 super.onDestroy();
	 }

	 
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		 MenuInflater inflater = getSherlock().getMenuInflater();
		 inflater.inflate(R.menu.schedule_menu, menu);
		 return true;
	 }
	 
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
		 switch (item.getItemId()) {
		 case R.id.menu_save_schedule:
			 saveSchedule();
			 Intent intent = new Intent(this, VisualizationActivity.class);
			 Calendar calendar = Calendar.getInstance();
			 calendar.set(Calendar.DATE, mViewPager.getCurrentItem() + 1);
			 intent.putExtra("time", calendar.getTimeInMillis());
			 startActivity(intent);
			 return true;
		 default:
			 return super.onOptionsItemSelected(item);
		 }
	 }
	 
	 private void saveSchedule() {
		 TagManager tm = new TagManager(App.getDatabaseSource(this));
		 LocationManager lm = new LocationManager(App.getDatabaseSource(this));
		 
		 for(int i = 0; i < mPagerAdapter.getCount(); i++) {
			 ScheduleListFragment fragment = (ScheduleListFragment) getSupportFragmentManager() 
			 	.findFragmentByTag("android:switcher:"+R.id.schedulepager+":" + i);
			 if(fragment != null && fragment.mHms != null) {
				 for(HashMap<String, List<Object>> hm : fragment.mHms) {
					 TimeSlot slot = (TimeSlot) ((List<Object>)hm.get("title")).get(0);
					 MLocation loc = lm.getLocation(slot.locationId);
					 
					 long startTime = slot.start;
					 if(slot.end != null && slot.end != 0L) {
						 long total = slot.end - slot.start;
						 List<Object> tags = hm.get("tags");
						 for(Object o : tags) {
							 MTag tag = (MTag) o;
							 tag.locationId = loc.id;
							 tag.startTime = startTime;
							 tag.endTime = startTime + total / tags.size();
							 tm.ensureTag(tag);
						 }
					 }
				 }
			 }
		 }
	 }
	 

	 class SchedulePagerAdapter extends FragmentPagerAdapter {
		 private Calendar mCalendar;
		
		 public SchedulePagerAdapter(FragmentManager fm, int month) {
			 super(fm);
			 mCalendar = Calendar.getInstance();
			 mCalendar.set(Calendar.MONTH, month);
		 }

		 @Override
		 public int getCount() {
			 mCalendar.setTimeInMillis(System.currentTimeMillis());
			 return mCalendar.get(Calendar.DAY_OF_MONTH);
		 }

		 @Override
		 public Fragment getItem(int position) {
			 return ScheduleListFragment.newInstance(mCalendar.get(Calendar.MONTH), position + 1);
		 }

		 @Override
		 public CharSequence getPageTitle (int position) {
			 SimpleDateFormat formatter = new SimpleDateFormat("E, MMM ");
			 mCalendar.set(Calendar.DAY_OF_MONTH, position + 1);
			 return formatter.format(mCalendar.getTime()) + (position + 1);
		 }
		 
		 @Override
		 public void destroyItem(ViewGroup container, int position, Object object) {
			 if (position >= getCount()) {
				 FragmentManager manager = ((Fragment) object).getFragmentManager();
				 FragmentTransaction trans = manager.beginTransaction();
				 trans.remove((Fragment) object);
				 trans.commit();
			 }
		 }
	 }
	 
	 public static Set<String> getTagSet(Context context) {
		 Set<String> tags = new HashSet<String>();
		 SharedPreferences preference = context.getSharedPreferences(PREFS_NAME, 0);
		 tags = preference.getStringSet(TAG_PREF_KEY, null);
		 
		 return tags;
	 }
	 
	 public static void saveTagSet(Context context, Set<String> tags) {
		 SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		 SharedPreferences.Editor editor = settings.edit();
		 editor.putStringSet(TAG_PREF_KEY, tags);
		 editor.commit();
	 }

}
