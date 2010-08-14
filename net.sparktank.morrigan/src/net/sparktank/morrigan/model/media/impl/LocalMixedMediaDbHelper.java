package net.sparktank.morrigan.model.media.impl;

import java.io.File;
import java.util.ArrayList;

import net.sparktank.morrigan.config.Config;
import net.sparktank.morrigan.model.explorer.MediaExplorerItem;
import net.sparktank.sqlitewrapper.DbException;

public class LocalMixedMediaDbHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static String getFullPathToMmdb (String fileName) {
		File dir = Config.getMmdbDir();
		String file = dir.getPath() + File.separator + fileName;
		
		if (!file.toLowerCase().endsWith(Config.MMDB_LOCAL_FILE_EXT)) {
			file = file.concat(Config.MMDB_LOCAL_FILE_EXT);
		}
		
		return file;
	}
	
	public static LocalMixedMediaDb createMmdb (String name) throws DbException {
		String file = getFullPathToMmdb(name);
		LocalMixedMediaDb l = MixedMediaListFactory.LOCAL_MMDB_FACTORY.manufacture(file);
		return l;
	}
	
	public static boolean isMmdbFile (String filePath) {
		return (filePath.toLowerCase().endsWith(Config.MMDB_LOCAL_FILE_EXT));
	}
	
	public static ArrayList<MediaExplorerItem> getAllMmdb () {
		ArrayList<MediaExplorerItem> ret = new ArrayList<MediaExplorerItem>();
		
		File dir = Config.getMmdbDir();
		File [] files = dir.listFiles();
		
		// empty dir?
		if (files == null || files.length < 1 ) return ret;
		
		for (File file : files) {
			if (isMmdbFile(file.getAbsolutePath())) {
				MediaExplorerItem newItem = new MediaExplorerItem(MediaExplorerItem.ItemType.LOCALMMDB);
				newItem.identifier = file.getAbsolutePath();
				newItem.title = getMmdbTitle(newItem.identifier);
				ret.add(newItem);
			}
		}
		
		return ret;
	}
	
	public static String getMmdbTitle (String filePath) {
		String ret = filePath;
		int x;
		
		x = ret.lastIndexOf(File.separator);
		if (x > 0) {
			ret = ret.substring(x+1);
		}
		
		x = ret.lastIndexOf(Config.MMDB_LOCAL_FILE_EXT);
		if (x > 0) {
			ret = ret.substring(0, x);
		}
		
		return ret;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
