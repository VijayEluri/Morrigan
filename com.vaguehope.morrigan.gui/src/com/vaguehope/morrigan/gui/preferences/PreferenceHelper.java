package com.vaguehope.morrigan.gui.preferences;

import com.vaguehope.morrigan.gui.Activator;

public class PreferenceHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static final String PREF_LASTJUMPTODLGQUERY = "PREF_LASTJUMPTODLGQUERY";
	
	static public String getLastJumpToDlgQuery () {
		String s = Activator.getDefault().getPreferenceStore().getString(PREF_LASTJUMPTODLGQUERY);
		return s;
	}
	
	static public void setLastJumpToDlgQuery (String q) {
		Activator.getDefault().getPreferenceStore().setValue(PREF_LASTJUMPTODLGQUERY, q);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}