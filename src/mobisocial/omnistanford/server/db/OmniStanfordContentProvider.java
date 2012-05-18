package mobisocial.omnistanford.server.db;

import mobisocial.omnistanford.App;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorJoiner.Result;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class OmniStanfordContentProvider extends ContentProvider {
	public static final String TAG = "OmniStanfordContentProvider";
	
	public static final int CHECKINS = 0;
	public static final int CHECKIN_ID = 1;
	
	public static final String AUTHORITY = "mobisocial.omnistanford.db";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	private SQLiteOpenHelper mHelper;
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sUriMatcher.addURI(AUTHORITY, "checkins", CHECKINS);
		sUriMatcher.addURI(AUTHORITY, "checkins/#", CHECKIN_ID);
	}
	
	@Override
	public boolean onCreate() {
		Log.i(TAG, "OmniStanford content provider created");
		mHelper = App.getServerDatabaseSource(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)) {
			case CHECKINS:
				return "vnd.android.cursor.dir/vnd.omnistanford.checkin";
			case CHECKIN_ID:
				return "vnd.android.cursor.item/vnd.omnistanford.checkin";
			default:
                throw new IllegalStateException("Unmatched-but-known content type");
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.i(TAG, "query " + uri);
		Cursor result = null;
		switch (sUriMatcher.match(uri)) {
			case CHECKINS:
				result = mHelper.getReadableDatabase().query(
						MCheckinData.TABLE, projection, selection, selectionArgs, null, null, sortOrder);
				break;
		}
		
		
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
