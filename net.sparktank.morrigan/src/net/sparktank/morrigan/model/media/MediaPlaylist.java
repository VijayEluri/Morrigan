package net.sparktank.morrigan.model.media;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import net.sparktank.morrigan.exceptions.MorriganException;

public class MediaPlaylist extends MediaList {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private boolean newPl = false;
	private String filePath = null;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private MediaPlaylist(String title, String filePath) throws MorriganException {
		super(filePath, title);
		this.filePath = filePath;
	}
	
	MediaPlaylist(String title, String filePath, boolean newPl) throws MorriganException {
		super(filePath, title);
		this.filePath = filePath;
		this.newPl = newPl;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public boolean allowDuplicateEntries () {
		return true;
	}
	
	@Override
	public void read () throws MorriganException {
		if (newPl) {
			if (new File(filePath).exists()) {
				throw new MorriganException("Play list already exists.");
			}
			writeToFile();
		} else {
			loadFromFile();
		}
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public String getFilePath () {
		return filePath;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public void loadFromFile () throws MorriganException {
		logger.fine("Reading PlayList from '" + filePath + "'...");
		
		File file = new File(filePath);
        BufferedReader reader = null;
        
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new MorriganException("Failed to open play list file for reading.", e);
		}
		
		// repeat until all lines is read
		String text = null;
		try {
			int n = 0;
			while ((text = reader.readLine()) != null) {
				addTrack(new MediaItem(text));
				n++;
			}
			logger.fine("Read " + n + " lines from '" + filePath + "'.");
			
		} catch (IOException e) {
			throw new MorriganException("Error while reading play list.", e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				throw new MorriganException("Failed to close file handle.", e);
			}
		}
		
		setDirty(false);
	}
	
	public void writeToFile () throws MorriganException {
		logger.fine("Writing PlayList to '" + filePath + "'...");
		
		File file = new File(filePath);
        Writer writer = null;
        
        try {
        	writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			throw new MorriganException("Failed to open file to write to.", e);
		}
        
		try {
			int n = 0;
			for (MediaItem mt : getMediaTracks()) {
				writer.write(mt.getFilepath() + "\n");
				n ++;
			}
			logger.fine("Wrote " + n + " lines to '" + filePath + "'.");
			
		} catch (IOException e) {
			throw new MorriganException("Error while write play list to file.", e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				throw new MorriganException("Failed to close file handle.", e);
			}
		}
		
		setDirty(false);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		MediaPlaylist other = (MediaPlaylist) obj;
		if (filePath == null) {
			if (other.filePath != null) {
				return false;
			}
		} else if (!filePath.equals(other.filePath)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		// Since equals() only uses filePath, we can do this.
		return filePath.hashCode();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
