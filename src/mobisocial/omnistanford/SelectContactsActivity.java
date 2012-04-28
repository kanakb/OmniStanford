package mobisocial.omnistanford;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        final ListView lv = new ListView(this);
        lv.setAdapter(new SimpleAdapter(this, hms, R.layout.list_item, from, to));
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                TextView selected = (TextView) arg1.findViewById(R.id.title);
                Log.d(TAG, selected.getText().toString());
                Log.d(TAG, arg2 + " " + arg3);
                for (long id : lv.getCheckedItemIds()) {
                    Log.d(TAG, "selected" + id);
                }
                //selected.setTextColor(getResources().getColor(android.R.color.darker_gray));
                //selected.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
        });
        wrapper.addView(lv);
        Log.d(TAG, "Showing list");
    }
}
