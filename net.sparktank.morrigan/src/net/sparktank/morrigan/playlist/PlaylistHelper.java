package net.sparktank.morrigan.playlist;

import java.io.File;
import java.util.ArrayList;

import net.sparktank.morrigan.config.Config;
import net.sparktank.morrigan.exceptions.MorriganException;
import net.sparktank.morrigan.model.media.MediaListFactory;
import net.sparktank.morrigan.model.media.MediaPlaylist;
import net.sparktank.morrigan.model.ui.MediaExplorerItem;

public class PlaylistHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public static final PlaylistHelper INSTANCE = new PlaylistHelper();
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private String getPathForNewPlaylist (String plName) {
		File plDir = Config.getPlDir();
		String plFile = plDir.getPath() + File.separator + plName + Config.PL_FILE_EXT;
		return plFile;
	}
	
	public MediaPlaylist createPl (String plName) throws MorriganException {
		String plFile = getPathForNewPlaylist(plName);
		MediaPlaylist pl = MediaListFactory.makeMediaPlaylist(plFile, true);
		pl.read();
		return pl;
	}
	
	private boolean isPlFile (String filePath) {
		return (filePath.toLowerCase().endsWith(Config.PL_FILE_EXT));
	}
	
	public ArrayList<MediaExplorerItem> getAllPlaylists () {
		ArrayList<MediaExplorerItem> ret = new ArrayList<MediaExplorerItem>();
		
		File plDir = Config.getPlDir();
		File [] plFiles = plDir.listFiles();
		
		// empty dir?
		if (plFiles == null || plFiles.length < 1 ) return ret;
		
		for (File file : plFiles) {
			if (isPlFile(file.getAbsolutePath())) {
				MediaExplorerItem newItem = new MediaExplorerItem(MediaExplorerItem.ItemType.PLAYLIST);
				newItem.identifier = file.getAbsolutePath();
				
				int x = newItem.identifier.lastIndexOf(File.separator);
				if (x > 0) {
					newItem.title = newItem.identifier.substring(x+1);
				} else {
					newItem.title = newItem.identifier;
				}
				
				ret.add(newItem);
			}
		}
		
		return ret;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
