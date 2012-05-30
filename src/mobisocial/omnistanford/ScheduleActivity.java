package mobisocial.omnistanford;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.db.MTag;
import mobisocial.omnistanford.db.TagManager;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "ScheduleActivity";
    public static final long DAY = 1000L * 60L * 60L * 24L * 30L;
    
    private ViewPager mViewPager;
    private SchedulePagerAdapter mPagerAdapter;

 
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
			 DailyScheduleFragment fragment = (DailyScheduleFragment) getSupportFragmentManager() 
			 	.findFragmentByTag("android:switcher:"+R.id.schedulepager+":" + i);
			 if(fragment != null && fragment.mHms != null) {
				 for(HashMap<String, List<Object>> hm : fragment.mHms) {
					 TimeSlot slot = (TimeSlot) ((List<Object>)hm.get("title")).get(0);
					 MLocation loc = lm.getLocation(slot.locationId);
					 
					 long startTime = slot.start;
					 long total = slot.end - slot.start;
					 List<Object> tags = hm.get("tags");
					 for(Object o : tags) {
						 MTag tag = (MTag) o;
						 tag.locationId = loc.id;
						 tag.startTime = startTime;
						 tag.endTime = startTime + total / tags.size();
						 startTime = tag.endTime;
						 tm.ensureTag(tag);
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
			 int count = 31;
			 switch (mCalendar.get(Calendar.MONTH)) {
			 	case Calendar.APRIL:
			 	case Calendar.JUNE:
			 	case Calendar.SEPTEMBER:
			 	case Calendar.NOVEMBER:
			 		count = 30;
			 		break;
			 	case Calendar.FEBRUARY:
			 		// TODO: leap year returns 29
			 		count = 28;
			 		break;
			 }
			 
			 return count;
		 }

		 @Override
		 public Fragment getItem(int position) {
			 return DailyScheduleFragment.newInstance(mCalendar.get(Calendar.MONTH), position + 1);
		 }

		 @Override
		 public CharSequence getPageTitle (int position) {
			 return mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
			 	+ " " + (position + 1);
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
	 
	 /*
	  *  Fragment display daily schedule and tags
	  */
	 public static class DailyScheduleFragment extends Fragment {
		 private int mMonth;
		 private int mDay;
		 private int mWidth;
		 private ListView mListView;
		 private SimpleAdapter mAdapter;
		 private EditText mTagEditText;
		 private LinearLayout mContainer;
		 
		 public boolean mIsUpdated = false;
		 public List<HashMap<String, List<Object>>> mHms;
		 

		 static DailyScheduleFragment newInstance(int month, int day) {
			 DailyScheduleFragment f = new DailyScheduleFragment();

			 // Supply num input as an argument.
			 Bundle args = new Bundle();
			 args.putInt("month", month);
			 args.putInt("day", day);
			 f.setArguments(args);

			 return f;
		 }

		 @Override
		 public void onCreate(Bundle savedInstanceState) {
			 super.onCreate(savedInstanceState);
			 mMonth = getArguments().getInt("month");
			 mDay = getArguments().getInt("day");
		 }

		 @Override
		 public View onCreateView(LayoutInflater inflater, ViewGroup container,
				 Bundle savedInstanceState) {
			 mContainer = (LinearLayout)inflater.inflate(R.layout.schedule_page, container, false);
			 
			 // set up schedule listview
			 mListView = new ListView(this.getActivity());
			 ((LinearLayout) mContainer.findViewById(R.id.scheduleArea)).addView(mListView);
			 
			 // set up tags area
			 final GridLayout tagsList = (GridLayout) mContainer.findViewById(R.id.tagListArea);
			 TagTextView tag = new TagTextView(this.getActivity(), "Coding");
			 tagsList.addView(tag);
			 
			 mTagEditText = (EditText) mContainer.findViewById(R.id.tagEditText);
			 Button btn = (Button) mContainer.findViewById(R.id.tagCreateButton);
			 btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(tagsList.getChildCount() < 8) {
						TagTextView newTag = new TagTextView(v.getContext(), mTagEditText.getText().toString());
						mTagEditText.setText("");
						tagsList.addView(newTag);
					} else {
						Toast.makeText(v.getContext(), "You can make at most 8 tags", Toast.LENGTH_SHORT).show();
					}
				}
				 
			 });
			 
			 return mContainer;
		 }

		 @Override
		 public void onActivityCreated(Bundle savedInstanceState) {
			 super.onActivityCreated(savedInstanceState);
			 mWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
			 
			 // fill schedule list adapter
			 String[] from = new String[] { "title", "tags" };
			 int[] to = new int[] { R.id.scheduleTitle, R.id.scheduleSubtitle };
			 if (mHms == null) {
				 mHms = new ArrayList<HashMap<String, List<Object>>>();
			 }
			 constructMap();
			 mAdapter = new SimpleAdapter(this.getActivity(), mHms, R.layout.schedule_list_item, from, to);
			 mAdapter.setViewBinder(new TagViewBinder());
			 mListView.setAdapter(mAdapter);
			 mListView.setOnDragListener(new myDragEventListener());
			 mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		 }
		 
		 @Override
		 public void onStart() {
			 super.onStart();
			
		 }

		 private void constructMap() {
			 mHms.clear();
			 LocationManager lm = new LocationManager(App.getDatabaseSource(this.getActivity()));
			 CheckinManager cm = new CheckinManager(App.getDatabaseSource(this.getActivity()));
			 TagManager tm = new TagManager(App.getDatabaseSource(this.getActivity()));

			 Calendar start = Calendar.getInstance();
			 start.set(Calendar.MONTH, mMonth);
			 start.set(Calendar.DAY_OF_MONTH, mDay);
			 start.set(Calendar.HOUR_OF_DAY, 0);
			 
			 List<MCheckinData> checkins = cm.getDailyCheckins(start.getTimeInMillis());
			 List<TimeSlot> slots = new ArrayList<TimeSlot>();
			 if (checkins.size() == 0) {
				 // test data
				 Calendar calendar = Calendar.getInstance();
				 calendar.set(Calendar.MONTH, mMonth);
				 calendar.set(Calendar.DATE, mDay);
				 Long startTime, endTime;
				 MLocation loc;
				 
				 calendar.set(Calendar.HOUR_OF_DAY, 9);
				 startTime = calendar.getTimeInMillis();
				 calendar.set(Calendar.HOUR_OF_DAY, 10);
				 endTime = calendar.getTimeInMillis();
				 loc = lm.getLocation(1L);
				 slots.add(new TimeSlot(startTime, endTime, loc.name, loc.id, mMonth*32*32 + mDay*32 + 1));
				 
				 calendar.set(Calendar.HOUR_OF_DAY, 12);
				 startTime = calendar.getTimeInMillis();
				 calendar.set(Calendar.HOUR_OF_DAY, 14);
				 endTime = calendar.getTimeInMillis();
				 loc = lm.getLocation(2L);
				 slots.add(new TimeSlot(startTime, endTime, loc.name, loc.id, mMonth*32*32 + mDay*32 + 2));
				 
				 calendar.set(Calendar.HOUR_OF_DAY, 18);
				 startTime = calendar.getTimeInMillis();
				 calendar.set(Calendar.HOUR_OF_DAY, 19);
				 endTime = calendar.getTimeInMillis();
				 loc = lm.getLocation(3L);
				 slots.add(new TimeSlot(startTime, endTime, loc.name, loc.id, mMonth*32*32 + mDay*32 + 3));
			 } else {
				 for (MCheckinData checkin : checkins) {
					 MLocation loc = lm.getLocation(checkin.locationId);
					 slots.add(new TimeSlot(checkin.entryTime, checkin.exitTime, loc.name, loc.id, checkin.id));
				 }				 
			 }
			 
			 for(TimeSlot entry : slots) {
				 HashMap<String, List<Object>> hm = new HashMap<String, List<Object>>();
				 List<Object> title = new ArrayList<Object>();
				 title.add(entry);
				 List<MTag> tags = tm.getTags(entry.checkinId);
				 Log.i(TAG, entry.checkinId + " month:" + mMonth + " day:" + mDay);
				 List<Object> tagsCasted = new ArrayList<Object>();
				 for(MTag t : tags) {
					 tagsCasted.add(t);
				 }
				 hm.put("title", title);
				 hm.put("tags", tagsCasted);
				 mHms.add(hm);
			 }
		 }
		 
		 protected class myDragEventListener implements OnDragListener {
			 int mCheckedItem = -1;

			 // This is the method that the system calls when it dispatches a drag event to the
			 // listener.
			 public boolean onDrag(View v, DragEvent event) {

				 // Defines a variable to store the action type for the incoming event
				 final int action = event.getAction();

				 // Handles each of the expected events
				 switch(action) {
					 case DragEvent.ACTION_DRAG_STARTED:
						 Log.i(TAG, "drag started");
						 return true;
					 case DragEvent.ACTION_DRAG_ENTERED:
						 Log.i(TAG, "drag entered");
						 return true;
					 case DragEvent.ACTION_DRAG_LOCATION: {
						 int itemId = mListView.pointToPosition((int)event.getX(), (int)event.getY());
						 if(itemId != AdapterView.INVALID_POSITION && itemId != mCheckedItem) {
							 mListView.setItemChecked(itemId, true);
							 mListView.setItemChecked(mCheckedItem, false);
							 mCheckedItem = itemId;
						 }
						 return true ;
					 }
					 case DragEvent.ACTION_DRAG_EXITED:
						 reset();
						 return true;
					 case DragEvent.ACTION_DROP:
						 int itemId = mListView.pointToPosition((int)event.getX(), (int)event.getY());
						 if(itemId != ListView.INVALID_POSITION) {
							 ClipData.Item item = event.getClipData().getItemAt(0);
							 HashMap<String, List<Object>> hm = mHms.get(itemId);
							 TimeSlot slot = (TimeSlot) ((List<Object>)hm.get("title")).get(0);
							 List<Object> tags = hm.get("tags");
							 String tagName = item.getText().toString();
							 for(Object t : tags) {
								 // don't add duplicate tag
								 if(((MTag) t).name.equals(tagName)) {
									 return true;
								 }
							 }
							 MTag tag = new MTag();
							 tag.locationId = slot.locationId;
							 tag.checkinId = slot.checkinId;
							 tag.name = tagName;
							 tags.add(tag);
							 hm.put("tags", tags);
							 mIsUpdated = true;
							 mAdapter.notifyDataSetChanged();
						 }
						 return true;
					 case DragEvent.ACTION_DRAG_ENDED:
						 reset();
						 return true;
					 default:
						 Log.e("DragDrop Example","Unknown action type received by OnDragListener.");
						 return true;
				 }
			 }
			 
			 void reset() {
				 mListView.setItemChecked(mCheckedItem, false);
				 mCheckedItem = -1;
			 }
		 }
		 
		 class TagViewBinder implements SimpleAdapter.ViewBinder {

			 @Override
			 public boolean setViewValue(View view, Object data,
					 String textRepresentation) {
				 if(view.getId() == R.id.scheduleSubtitle) {
					 @SuppressWarnings("unchecked")
					List<Object> tags = (List<Object>) data;
					 if(tags.size() > 0) {
						 LinearLayout layout = (LinearLayout) view;
						 int childCount = layout.getChildCount();
						 if(tags.size() != childCount) {
							 int width = mWidth / tags.size();
							 for(int i = 0; i < tags.size(); i++) {
								 if(i < childCount) {
									 TagTextView child = (TagTextView)layout.getChildAt(i);
									 child.setWidth(width);
								 } else {
									 TagTextView newTag = new TagTextView(view.getContext(), ((MTag) tags.get(i)).name);
									 newTag.setWidth(width);
									 layout.addView(newTag);
								 }
							 }
						 }
					 }
					 return true;
				 } else if(view.getId() == R.id.scheduleTitle) {
					 @SuppressWarnings("unchecked")
					 TimeSlot slot = (TimeSlot) ((List<Object>) data).get(0);
					 Calendar start = Calendar.getInstance();
					 start.setTimeInMillis(slot.start);
					 Calendar end = Calendar.getInstance();
					 end.setTimeInMillis(slot.end);

					 ((TextView) view).setText(slot.locationName + " " + start.get(Calendar.HOUR_OF_DAY) + " - "
							 + end.get(Calendar.HOUR_OF_DAY));
					 return true;
				 }

				 return false;
			 }

		 }
	 }
	 
	 public static class TimeSlot {
		 public long start;
		 public long end;
		 public String locationName;
		 public long locationId;
		 public long checkinId;
		 
		 public TimeSlot(long start, long end, String locationName, long locationId, long checkinId) {
			 this.start = start; this.end = end; this.locationId = locationId;
			 this.locationName = locationName;
			 this.checkinId = checkinId;
		 }
	 }
	 

}
