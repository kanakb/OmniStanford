package mobisocial.omnistanford.server.ui;

import mobisocial.omnistanford.R;
import mobisocial.omnistanford.server.db.MCheckinData;
import mobisocial.omnistanford.server.db.OmniStanfordContentProvider;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.ListView;

public class CheckinListFragment extends ListFragment 
		implements LoaderCallbacks<Cursor> {
	
	private SimpleCursorAdapter mAdapter;
	private ContentObserver mObserver;
	
	 @Override 
	 public void onActivityCreated(Bundle savedInstanceState) {
		 super.onActivityCreated(savedInstanceState);

		 setEmptyText("No checkins");

		 mAdapter = new SimpleCursorAdapter(getActivity(),
				 R.layout.list_item, null,
				 new String[] { MCheckinData.COL_USER_DORM, MCheckinData.COL_USER_NAME, MCheckinData.COL_USER_DEPARTMENT },
				 new int[] { R.id.separator, R.id.title, R.id.subtitle }, 0);
		 setListAdapter(mAdapter);
		 getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		 
		 getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				CheckedTextView title = (CheckedTextView) view.findViewById(R.id.title);
				title.toggle();
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
