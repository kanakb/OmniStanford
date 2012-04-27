package mobisocial.omnistanford;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class SelectContactsActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "SelectContactsActivity";
    private static final String[] ITEMS = new String[] {
        "Item 1", "Item 2"
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout wrapper = (LinearLayout)findViewById(R.id.contentArea);
        View view = View.inflate(this, R.layout.contact_list, null);
        wrapper.addView(view,
                new LinearLayout.LayoutParams(wrapper.getWidth(), wrapper.getHeight()));
        ListView lv = (ListView)findViewById(R.id.contactListView);
        lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ITEMS));
        Log.d(TAG, "Showing list");
    }
}
