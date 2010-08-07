package net.sparktank.morrigan.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sparktank.morrigan.exceptions.MorriganException;
import net.sparktank.morrigan.helpers.FileHelper;

public abstract class MediaItemList<T extends MediaItem> implements IMediaItemList<T> {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Constructors and parameters.
	
	private final String listId;
	private final String listName;
	private final List<T> mediaTracks = new ArrayList<T>();
	
	/**
	 * listId must be unique.  It will be used to identify
	 * the matching editor.
	 * @param listId a unique ID.
	 * @param listName a human-readable title for this list.
	 */
	protected MediaItemList (String listId, String listName) {
		if (listId == null) throw new IllegalArgumentException("listId can not be null.");
		if (listName == null) throw new IllegalArgumentException("listName can not be null.");
		
		this.listId = listId;
		this.listName = listName;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * A unique identifier.
	 */
	@Override
	public String getListId () {
		return this.listId;
	}
	
	/**
	 * A human readable name for the GUI.
	 * @return
	 */
	@Override
	public String getListName () {
		return this.listName;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	abstract public String getType ();
	
	@Override
	abstract public String getSerial ();
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Dirty state and event listeners.
	
	private DirtyState dirtyState = DirtyState.CLEAN;
	private ArrayList<Runnable> dirtyChangeEvents = new ArrayList<Runnable>();
	private ArrayList<Runnable> changeEvents = new ArrayList<Runnable>();
	
	abstract public boolean isCanBeDirty (); 
	
	public void setDirtyState (DirtyState state) {
		if (isCanBeDirty()) {
			// Changed?  Priority order - don't drop back down.
			boolean changed = false;
			if (state!=this.dirtyState) {
				if (this.dirtyState==DirtyState.DIRTY && state==DirtyState.METADATA) {
					// Its too late to figure this out the other way round.
				} else {
					changed = true;
				}
			}
			
			if (changed) {
				this.dirtyState = state;
				
				for (Runnable r : this.dirtyChangeEvents) {
					r.run();
				}
			}
		}
		
		for (Runnable r : this.changeEvents) {
			r.run();
		}
	}
	
	@Override
	public DirtyState getDirtyState () {
		return this.dirtyState;
	}
	
	@Override
	public void addDirtyChangeEvent (Runnable r) {
		this.dirtyChangeEvents.add(r);
	}
	
	@Override
	public void removeDirtyChangeEvent (Runnable r) {
		this.dirtyChangeEvents.remove(r);
	}
	
	@Override
	public void addChangeEvent (Runnable r) {
		this.changeEvents.add(r);
	}
	
	@Override
	public void removeChangeEvent (Runnable r) {
		this.changeEvents.remove(r);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	abstract public boolean allowDuplicateEntries ();
	
	@Override
	abstract public void read () throws MorriganException;
	
	@Override
	public int getCount () {
		return this.mediaTracks.size();
	}
	
	/**
	 * Returns an unmodifiable list of the playlist items.
	 * @return
	 */
	@Override
	public List<T> getMediaTracks() {
		return Collections.unmodifiableList(this.mediaTracks);
	}
	
	protected void setMediaTracks (List<T> newMediaTracks) {
		synchronized (this.mediaTracks) {
			this.mediaTracks.clear();
			this.mediaTracks.addAll(newMediaTracks);
		}
		setDirtyState(DirtyState.DIRTY);
	}
	
	/**
	 * 
	 * @param newTracks
	 * @return items that are removed.
	 */
	protected List<T> replaceList (List<T> newTracks) {
		List<T> ret = updateList(this.mediaTracks, newTracks);
		setDirtyState(DirtyState.DIRTY);
		return ret;
	}
	
	/**
	 * Use this variant when you are about to to re-query the DB anyway
	 * and don't want to do two successive updates. 
	 * @param newTracks
	 * @return items that are removed.
	 */
	protected List<T> replaceListWithoutSetDirty (List<T> newTracks) {
		return updateList(this.mediaTracks, newTracks, false);
	}
	
	@Override
	public void addTrack (T track) {
		if (allowDuplicateEntries() || !this.mediaTracks.contains(track)) {
			this.mediaTracks.add(track);
			setDirtyState(DirtyState.DIRTY);
		}
	}
	
	/**
	 * @throws MorriganException  
	 */
	@Override
	public void removeMediaTrack (T track) throws MorriganException {
		this.mediaTracks.remove(track);
		setDirtyState(DirtyState.DIRTY);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Update methods.  Use these for data that is to be persisted.
//	These methods are sub-classed where persistence is needed.
	
	/**
	 * @throws MorriganException  
	 */
	@Override
	public void setDateAdded (T track, Date date) throws MorriganException {
		track.setDateAdded(date);
		setDirtyState(DirtyState.METADATA);
	}
	
	/**
	 * @throws MorriganException  
	 */
	@Override
	public void setTrackHashCode (T track, long hashcode) throws MorriganException {
		track.setHashcode(hashcode);
		setDirtyState(DirtyState.METADATA);
	}
	
	/**
	 * @throws MorriganException  
	 */
	@Override
	public void setTrackDateLastModified (T track, Date date) throws MorriganException {
		track.setDateLastModified(date);
		setDirtyState(DirtyState.METADATA);
	}
	
	/**
	 * @throws MorriganException  
	 */
	@Override
	public void setTrackEnabled (T track, boolean value) throws MorriganException {
		track.setEnabled(value);
		setDirtyState(DirtyState.METADATA);
	}
	
	/**
	 * @throws MorriganException  
	 */
	@Override
	public void setTrackMissing (T track, boolean value) throws MorriganException {
		track.setMissing(value);
		setDirtyState(DirtyState.METADATA);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Actions.
	
	@Override
	public void copyMediaItemFile (T mi, File targetDirectory) throws MorriganException {
		if (!targetDirectory.isDirectory()) {
			throw new IllegalArgumentException("targetDirectory must be a directory.");
		}
		
		File targetFile = new File(targetDirectory.getAbsolutePath() + File.separatorChar
				+ mi.getFilepath().substring(mi.getFilepath().lastIndexOf(File.separatorChar) + 1));
		
		if (!targetFile.exists()) {
			System.err.println("Copying '"+mi.getFilepath()+"' to '"+targetFile.getAbsolutePath()+"'...");
			try {
				FileHelper.copyFile(new File(mi.getFilepath()), targetFile);
			} catch (IOException e) {
				throw new MorriganException(e);
			}
		}
		else {
			System.err.println("Skipping '"+targetFile.getAbsolutePath()+"' as it already exists.");
		}
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public String toString () {
		return this.listName + " ("+this.mediaTracks.size()+" items)";
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Typed helper methods.
	
	public List<T> updateList (List<T> keepList, List<T> freshList) {
		return updateList(keepList, freshList, true);
	}
	
	/**
	 * Update keepList without replacing any equivalent
	 * objects.  Instead copy the data from the new
	 * object the old one.  This is to work around how
	 * a GUI list uses a data provider.
	 * 
	 * @param keepList
	 * @param freshList
	 * @return items that were removed from keepList.
	 */
	public List<T> updateList (List<T> keepList, List<T> freshList, boolean UpdateKeepList) {
		List<T> finalList = new ArrayList<T>();
		
		synchronized (keepList) {
			synchronized (freshList) {
				
				// This block takes no time.
				Map<String,T> keepMap = new HashMap<String,T>(keepList.size());
				for (T e : keepList) {
					keepMap.put(e.getFilepath(), e);
				}
				
				// This block is very quick.
				for (T newItem : freshList) {
					T oldItem = keepMap.get(newItem.getFilepath());
					if (oldItem != null) {
						oldItem.setFromMediaItem(newItem);
						finalList.add(oldItem);
					} else {
						finalList.add(newItem);
					}
				}
				
				System.err.println("Replacing " + keepList.size() + " items with " + finalList.size() + " items.");
				
				/* Create a new list and populate it with the
				 * items removed.
				 */
				List<T> removedItems = new ArrayList<T>();
				keepMap = new HashMap<String,T>(keepList.size());
				for (T e : finalList) {
					keepMap.put(e.getFilepath(), e);
				}
				for (T e : keepList) {
					if (!keepMap.containsKey(e.getFilepath())) {
						removedItems.add(e);
					}
				}
				
				System.err.println("Removed " + removedItems.size() + " items.");
				
				/* Update the keep list.  We need to modify
				 * the passed in list, not return a new one.
				 * This block takes no time.
				 */
				if (UpdateKeepList) {
					keepList.clear();
					keepList.addAll(finalList);
				}
				
				return removedItems;
			}
		}
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
