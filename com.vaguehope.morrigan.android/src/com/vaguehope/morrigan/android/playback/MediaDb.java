package com.vaguehope.morrigan.android.playback;

import java.math.BigInteger;
import java.util.Collection;

import android.database.Cursor;
import android.net.Uri;

public interface MediaDb {

	void addToQueue(Collection<QueueItem> items);
	void removeFromQueue(Collection<QueueItem> items);
	void removeFromQueueById(Collection<Long> itemIds);

	long getQueueSize ();
	Cursor getQueueCursor();
	QueueItem getFirstQueueItem();
	QueueItem getQueueItemById(long rowId);

	void moveQueueItem(long rowId, MoveAction action);
	void moveQueueItemToEnd(long rowId, MoveAction action);

	enum MoveAction {
		UP,
		DOWN;
	}

	LibraryMetadata newLibrary (String name);
	Collection<LibraryMetadata> getLibraries ();
	LibraryMetadata getLibrary (long libraryId);
	void updateLibrary(LibraryMetadata libraryMetadata);
	void deleteLibrary(LibraryMetadata libraryMetadata);

	void addMedia (long libraryId, Collection<MediaItem> items);
	void updateMedia(Collection<MediaItem> items);
	void setFilesExist(Collection<Long> rowId, boolean fileExists);
	void setFileMetadata(long rowId, long fileSize, long fileLastModifiedMillis, BigInteger hash);
	void rmMediaItems (Collection<MediaItem> items);
	void rmMediaItemRows (Collection<Long> rowIds);

	Cursor getAllMediaCursor (long libraryId, SortColumn sortColumn, SortDirection sortDirection);
	Cursor searchMediaCursor (long libraryId, String query, SortColumn sortColumn, SortDirection sortDirection);
	MediaItem getMediaItem(long rowId);
	Presence hasMediaUri(long libraryId, Uri uri);
	long getMediaRowId(long libraryId, Uri uri);

	MediaItem randomMediaItem();

	Cursor findDuplicates(long libraryId);
	void mergeItems(long destRowId, Collection<Long> fromRowIds);

	void addMediaWatcher(MediaWatcher watcher);
	void removeMediaWatcher(MediaWatcher watcher);

	enum Presence {
		/**
		 * File exists.
		 */
		PRESENT,
		/**
		 * File used to exist, now marked as missing.
		 */
		MISSING,
		/**
		 * File is not known.
		 */
		UNKNOWN;
	}

	enum SortColumn {
		PATH,
		DATE_ADDED,
		DATE_LAST_PLAYED,
		START_COUNT,
		END_COUNT,
		DURAITON;
	}

	enum SortDirection {
		ASC,
		DESC;
	}

	interface MediaWatcher {
		void queueChanged();
		void librariesChanged();
	}

}
