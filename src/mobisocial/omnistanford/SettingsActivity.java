package mobisocial.omnistanford;

import mobisocial.omnistanford.db.MUserProperty;
import mobisocial.omnistanford.db.PropertiesManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends OmniStanfordBaseActivity {
    public static final String TAG = "SettingsActivity";
    
    private static final String DEPARTMENT = "department";
    private static final String RESIDENCE = "residence";
    private static final String ENABLED = "enabled";
    
    private PropertiesManager mPm;
    
    private OnItemSelectedListener mDepartmentSelector = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                long id) {
            MUserProperty prop = mPm.getProperty(DEPARTMENT);
            if (prop != null && !parent.getItemAtPosition(pos).toString().equals(prop.value)) {
                Toast.makeText(parent.getContext(),
                    "Department successfully set to " + parent.getItemAtPosition(pos).toString(),
                    Toast.LENGTH_SHORT).show();
            }
            mPm.ensureProperty(new MUserProperty(DEPARTMENT,
                    parent.getItemAtPosition(pos).toString()));
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // do nothing
        }
    };
    
    private OnItemSelectedListener mResidenceSelector = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                long id) {
            MUserProperty prop = mPm.getProperty(RESIDENCE);
            if (prop != null && !parent.getItemAtPosition(pos).toString().equals(prop.value)) {
                Toast.makeText(parent.getContext(),
                    "Residence successfully set to " + parent.getItemAtPosition(pos).toString(),
                    Toast.LENGTH_SHORT).show();
            }
            mPm.ensureProperty(new MUserProperty(RESIDENCE,
                    parent.getItemAtPosition(pos).toString()));
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // do nothing
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPm = new PropertiesManager(App.getDatabaseSource(this));
        
        // Wrap the XML
        LinearLayout wrapper = (LinearLayout)findViewById(R.id.contentArea);
        LayoutInflater li = LayoutInflater.from(this);
        li.inflate(R.layout.settings, wrapper);
        
        // Department Selector
        Spinner deptSpinner = (Spinner)findViewById(R.id.selectDept);
        ArrayAdapter<CharSequence> deptAdapter = ArrayAdapter.createFromResource(this,
                R.array.departments_list,
                android.R.layout.simple_spinner_item);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deptSpinner.setAdapter(deptAdapter);
        deptSpinner.setOnItemSelectedListener(mDepartmentSelector);
        MUserProperty currentDept = mPm.getProperty(DEPARTMENT);
        if (currentDept != null) {
            int pos = deptAdapter.getPosition(currentDept.value);
            deptSpinner.setSelection(pos);
        }
        
        // Residence Selector
        Spinner resSpinner = (Spinner)findViewById(R.id.selectDorm);
        ArrayAdapter<CharSequence> resAdapter = ArrayAdapter.createFromResource(this,
                R.array.residences_list,
                android.R.layout.simple_spinner_item);
        resAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resSpinner.setAdapter(resAdapter);
        resSpinner.setOnItemSelectedListener(mResidenceSelector);
        MUserProperty currentRes = mPm.getProperty(RESIDENCE);
        if (currentRes != null) {
            int pos = resAdapter.getPosition(currentRes.value);
            resSpinner.setSelection(pos);
        }
        
        findViewById(R.id.enableSharing).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean checked = ((CheckBox)v.findViewById(R.id.enableSharing)).isChecked();
                String message = (checked) ? "Enabled Location Sharing" : "Disabled Location Sharing";
                Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
                mPm.ensureProperty(new MUserProperty(ENABLED, checked.toString()));
            }
        });
        final CheckBox checkbox = (CheckBox)findViewById(R.id.enableSharing);
        MUserProperty enabledState = mPm.getProperty(ENABLED);
        if (enabledState != null) {
            boolean mode = ("true".equals(enabledState.value)) ? true : false;
            Log.d(TAG, "enabled: " + mode);
            checkbox.setChecked(mode);
        } else {
            mPm.insertProperty(new MUserProperty(ENABLED, new Boolean(true).toString()));
            checkbox.setChecked(true);
        }
    }
}
