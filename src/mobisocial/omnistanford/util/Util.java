package mobisocial.omnistanford.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Util {
	public static final String PREFS_NAME = "OmniStanfordPrefsFile";
	
	public static String getPickedAccountType(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("account_type", null);
	}
	
	public static String getPickedAccountName(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("account_name", null);
	}
	
	public static String getPickedAccountPrincipalHash(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("account_hash", null);
	}
	
	public static void setPickedAccount(Context context, String name, String type, String hash) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("account_name", name);
		editor.putString("account_type", type);
		editor.putString("account_hash", hash);
		
		editor.commit();
	}
}
