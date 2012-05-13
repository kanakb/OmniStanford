package mobisocial.omnistanford;

import mobisocial.omnistanford.db.DatabaseHelper;
import mobisocial.omnistanford.server.db.ServerDatabaseHelper;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class App extends Application {
	public final static String TAG = "OmniStanford";
	
	private SQLiteOpenHelper mDatabaseSource;
	private SQLiteOpenHelper mServerDBSource;
	
	public synchronized SQLiteOpenHelper getDatabaseSource() {
	    if (mDatabaseSource == null) {
            mDatabaseSource = new DatabaseHelper(getApplicationContext());
        }
		return mDatabaseSource;
	}

	public static SQLiteOpenHelper getDatabaseSource(Context c) {
		Context app_as_context = c.getApplicationContext();
		return ((App) app_as_context).getDatabaseSource();
	}
	
	public synchronized SQLiteOpenHelper getServerDatabaseSource() {
	    if (mServerDBSource == null) {
	    	mServerDBSource = new ServerDatabaseHelper(getApplicationContext());
        }
		return mServerDBSource;
	}

	public static SQLiteOpenHelper getServerDatabaseSource(Context c) {
		Context app_as_context = c.getApplicationContext();
		return ((App) app_as_context).getServerDatabaseSource();
	}
}
