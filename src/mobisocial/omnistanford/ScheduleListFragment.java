package mobisocial.omnistanford;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import mobisocial.omnistanford.db.CheckinManager;
import mobisocial.omnistanford.db.LocationManager;
import mobisocial.omnistanford.db.MCheckinData;
import mobisocial.omnistanford.db.MLocation;
import mobisocial.omnistanford.db.MTag;
import mobisocial.omnistanford.db.TagManager;
import android.content.ClipData;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnDragListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/*
 *  Fragment display daily schedule and tags
 */
public class ScheduleListFragment extends Fragment {
	 private int mMonth;
	 private int mDay;
	 private int mWidth;
	 private int mHeight;
	 private ListView mListView;
	 private SimpleAdapter mAdapter;
	 private LinearLayout mContainer;

	 
	 public boolean mIsUpdated = false;
	 public List<HashMap<String, List<Object>>> mHms;
	 

	 static ScheduleListFragment newInstance(int month, int day) {
		 ScheduleListFragment f = new ScheduleListFragment();

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
		 mListView = (ListView) mContainer.findViewById(R.id.scheduleList);
		 
		 return mContainer;
	 }
	 
	

	 @Override
	 public void onActivityCreated(Bundle savedInstanceState) {
		 super.onActivityCreated(savedInstanceState);
		 mWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		 mHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
		 
		 // fill schedule list adapter
		 String[] from = new String[] { "title", "subtitle", "tags" };
		 int[] to = new int[] { R.id.scheduleTitle, R.id.scheduleSubtitle, R.id.scheduleContent };
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
			 List<Object> tagsCasted = new ArrayList<Object>();
			 for(MTag t : tags) {
				 tagsCasted.add(t);
			 }
			 hm.put("title", title);
			 hm.put("subtitle", title);
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
					 return true;
				 case DragEvent.ACTION_DRAG_ENTERED:
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
			 if(view.getId() == R.id.scheduleContent) {
				 @SuppressWarnings("unchecked")
				 List<Object> tags = (List<Object>) data;
				 if(tags.size() > 0) {
					 LinearLayout layout = (LinearLayout) view;
					 int childCount = layout.getChildCount();
					 int width = mWidth / tags.size();
					 int height = mHeight / 10;
					 for(int i = 0; i < tags.size(); i++) {
						 if(i < childCount) {
							 TagFrameLayout child = (TagFrameLayout)layout.getChildAt(i);
							 child.setLayoutParams(new LinearLayout.LayoutParams(width, height));
						 } else {
							 TagFrameLayout newTag = new TagFrameLayout(view.getContext(), ((MTag) tags.get(i)).name, false);
							 newTag.setLayoutParams(new LinearLayout.LayoutParams(width, height));
							 layout.addView(newTag);
						 }
					 }
				 }
				 return true;
			 } else if(view.getId() == R.id.scheduleTitle) {
				 StringBuilder text = new StringBuilder();
				 @SuppressWarnings("unchecked")
				 TimeSlot slot = (TimeSlot) ((List<Object>) data).get(0);
				 text.append(slot.locationName).append("\n");
				 ((TextView) view).setText(text.toString());
				 
				 return true;
			 } else if(view.getId() == R.id.scheduleSubtitle) {
				 StringBuilder text = new StringBuilder();
				 @SuppressWarnings("unchecked")
				 TimeSlot slot = (TimeSlot) ((List<Object>) data).get(0);
				 Calendar start = Calendar.getInstance();
				 start.setTimeInMillis(slot.start);
				 SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, h:mm a");
				 text.append(formatter.format(start.getTime())).append(" - ");
				 if(slot.end != null && slot.end != 0L) {
					 Calendar end = Calendar.getInstance();
					 end.setTimeInMillis(slot.end);
					 text.append(formatter.format(end.getTime()));
				 } else {
					 text.append("now");
				 }
				 ((TextView) view).setText(text.toString());
				 
				 return true;
			 }

			 return false;
		 }

	 }
	 
	 public static class TimeSlot {
		 public Long start;
		 public Long end;
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


