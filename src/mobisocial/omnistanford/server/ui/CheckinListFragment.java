package mobisocial.omnistanford.server.ui;

import mobisocial.omnistanford.R;
import mobisocial.omnistanford.server.db.MCheckinData;
import mobisocial.omnistanford.server.db.OmniStanfordContentProvider;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;

public class CheckinListFragment extends ListFragment 
		implements LoaderCallbacks<Cursor> {
	
	private SimpleCursorAdapter mAdapter;
	private ContentObserver mObserver;
	
	 @Override 
	 public void onActivityCreated(Bundle savedInstanceState) {
		 super.onActivityCreated(savedInstanceState);

		 setEmptyText("No checkins");

		 mAdapter = new SimpleCursorAdapter(getActivity(),
				 R.layout.server_checkin_list_item, null,
				 new String[] { MCheckinData.COL_USER_NAME, MCheckinData.COL_USER_DEPARTMENT },
				 new int[] { R.id.checkin_title, R.id.checkin_subtitle }, 0);
		 setListAdapter(mAdapter);
		 getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		 
		 getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				FragmentTransaction ft = getFragmentManager().beginTransaction();

			    // Create and show the dialog.
			    DialogFragment newFragment = MenuDialogFragment.newInstance("");
			    newFragment.show(ft, "dialog");
			}
		 });

		 setListShown(false);
		 getLoaderManager().initLoader(0, null, this);
		 
		 mObserver = new ContentObserver(new Handler()) {
			 @Override
			 public void onChange(boolean selfChange) {
				 getLoaderManager().restartLoader(0, null, CheckinListFragment.this);
			 }
		 };
		 getActivity().getContentResolver().registerContentObserver(
				 Uri.withAppendedPath(OmniStanfordContentProvider.CONTENT_URI, Uri.encode("checkins")), 
						 true, mObserver);
	 }
	 
	 @Override
	 public void onDestroy() {
		 getActivity().getContentResolver().unregisterContentObserver(mObserver);
		 super.onDestroy();
	 }
	 
	static final String[] CHECKIN_PROJECTION = new String[] {
		MCheckinData.COL_ID,
		MCheckinData.COL_USER_NAME,
		MCheckinData.COL_USER_DORM,
		MCheckinData.COL_USER_DEPARTMENT
	};

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri baseUri = Uri.withAppendedPath(OmniStanfordContentProvider.CONTENT_URI, 
				Uri.encode("checkins"));

		String select = MCheckinData.COL_EXIT_TIME + " IS NULL";
		return new CursorLoader(getActivity(), baseUri, CHECKIN_PROJECTION, select, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);

		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}

class MenuDialogFragment extends DialogFragment {
    String mName;

    static MenuDialogFragment newInstance(String name) {
    	MenuDialogFragment f = new MenuDialogFragment();

        Bundle args = new Bundle();
        args.putString("name", name);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mName = getArguments().getString("name");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dining_menu_dialog, container, false);
        RadioGroup rg = (RadioGroup) v.findViewById(R.id.radio_group);
        rg.check(R.id.lunch_radio_button);
        
        getDialog().setTitle("Select dining type:");

        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.cancel_button);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}