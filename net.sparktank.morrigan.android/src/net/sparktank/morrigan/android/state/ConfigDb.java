package net.sparktank.morrigan.android.state;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sparktank.morrigan.android.model.ServerReference;
import net.sparktank.morrigan.android.model.impl.ServerReferenceImpl;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ConfigDb extends SQLiteOpenHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private static final String DB_NAME = "config";
	private static final int DB_VERSION = 1;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static final String TBL_HOSTS = "hosts";
	private static final String TBL_HOSTS_ID = "_id";
	private static final String TBL_HOSTS_URL = "url";
	
	private static final String TBL_HOSTS_CREATE = "CREATE TABLE " + TBL_HOSTS + " ("
    	+ TBL_HOSTS_ID + " integer primary key autoincrement,"
    	+ TBL_HOSTS_URL + " text"
    	+ ");";
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
    public ConfigDb (Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TBL_HOSTS_CREATE);
    }
    
    @Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	db.execSQL("DROP TABLE IF EXISTS " + TBL_HOSTS);
		onCreate(db);
	}
    
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    
    public List<ServerReference> getServers () {
    	SQLiteDatabase db = null;
		try {
			db = this.getReadableDatabase();
			db.beginTransaction();
			try {
				Cursor c = null;
				try {
					List<ServerReference> ret = new LinkedList<ServerReference>();
					
					c = db.query(true, TBL_HOSTS,
							new String[] {TBL_HOSTS_URL},
							null, null, null, null,
							TBL_HOSTS_URL + " DESC", null);
					
					if (c.moveToFirst()) {
						int col_url = c.getColumnIndex(TBL_HOSTS_URL);
						
						do {
							ServerReferenceImpl i = new ServerReferenceImpl(c.getString(col_url));
							ret.add(i);
						}
						while (c.moveToNext());
					}
					
					return Collections.unmodifiableList(ret);
				}
				finally {
					if (c != null) c.close();
				}
			}
			finally {
				db.endTransaction();
			}
		}
		finally {
			if (db != null) db.close();
		}
    }
    
    public void addServer (ServerReference sr) {
    	ContentValues initialValues = new ContentValues();
		initialValues.put(TBL_HOSTS_URL, sr.getBaseUrl());
		
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			db.beginTransaction();
			try {
				db.insert(TBL_HOSTS, null, initialValues);
				db.setTransactionSuccessful();
			}
			finally {
				db.endTransaction();
			}
		}
		finally {
			if (db != null) db.close();
		}
    }
    
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}