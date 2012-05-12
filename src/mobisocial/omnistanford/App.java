package mobisocial.omnistanford;

import mobisocial.omnistanford.db.DatabaseHelper;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

public class App extends Application {
	public final static String TAG = "OmniStanford";
	
	private SQLiteOpenHelper mDatabaseSource;
	
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
}
