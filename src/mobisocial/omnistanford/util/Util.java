package mobisocial.omnistanford.util;

import java.util.List;

import mobisocial.omnistanford.App;
import mobisocial.omnistanford.db.AccountManager;
import mobisocial.omnistanford.db.MAccount;
import android.content.Context;
import android.content.SharedPreferences;

public class Util {
	public static final String PREFS_NAME = "OmniStanfordPrefsFile";
	
	public static String getPickedAccountType(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("account_type", "");
	}
	
	public static String getPickedAccountName(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("account_name", "");
	}
	
	public static String getPickedAccountPrincipalHash(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("account_hash", "");
	}
	
	public static void setPickedAccount(Context context, String name, String type, String hash) {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("account_name", name);
		editor.putString("account_type", type);
		editor.putString("account_hash", hash);
		
		editor.commit();
	}
	
	public static void saveAccount(Context context, 
			String name, String hash, String type) {
		AccountManager am = new AccountManager(App.getDatabaseSource(context));
		if(am.getAccounts(type).size() == 0) {
			MAccount account = new MAccount();
			account.name = name;
			account.identifier = hash;
			account.type = type;
			am.insertAccount(account);
		}
	}
	
	public static MAccount loadAccount(Context context) {
		AccountManager am = new AccountManager(App.getDatabaseSource(context));
		List<MAccount> acs = am.getAccounts(null);
		if(acs.size() != 0) {
			return acs.get(0);
		} else {
			return null;
		}
	}
}
