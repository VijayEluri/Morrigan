package com.vaguehope.morrigan.android.playback;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.vaguehope.morrigan.android.R;
import com.vaguehope.morrigan.android.helper.ChecksumHelper;
import com.vaguehope.morrigan.android.helper.ExceptionHelper;
import com.vaguehope.morrigan.android.helper.IoHelper;
import com.vaguehope.morrigan.android.helper.LogWrapper;
import com.vaguehope.morrigan.android.playback.MediaDb.Presence;
import com.vaguehope.morrigan.android.playback.MediaDb.SortColumn;
import com.vaguehope.morrigan.android.playback.MediaDb.SortDirection;

public class RescanLibrariesService extends MediaBindingAwakeService {

	protected static final LogWrapper LOG = new LogWrapper("RDS");

	private NotificationManager notifMgr;

	public RescanLibrariesService () {
		super("RescanLibrariesService", LOG);
	}

	@Override
	public void onCreate () {
		super.onCreate();
		this.notifMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	protected void doWork (final Intent i) {
		final int notificationId = (int) (System.currentTimeMillis()); // Probably unique.
		final Builder notif = makeNotif();
		updateNotif(notificationId, notif);

		String result = "Unknown result.";
		try {
			waitForDbReady();
			doScans(notificationId, notif);
			result = "Finished.";
		}
		catch (final Exception e) {
			LOG.e("Scan failed.", e);
			result = ExceptionHelper.veryShortMessage(e);
		}
		finally {
			updateNotifResult(notificationId, notif, result);
		}
	}

	private Builder makeNotif () {
		final String title = "Updating Libraries";
		final String subTitle = "Starting...";
		return new Notification.Builder(this)
				.setSmallIcon(R.drawable.search)
				.setContentTitle(title)
				.setContentText(subTitle)
				.setTicker(subTitle)
				.setOngoing(true);
	}

	private void updateNotifTitle (final int notificationId, final Builder notif, final String msg) {
		updateNotif(notificationId, notif.setContentTitle(msg));
	}

	private void updateNotifProgress (final int notificationId, final Builder notif, final String msg) {
		updateNotif(notificationId, notif.setContentText(msg));
	}

	protected void updateNotifProgress (final int notificationId, final Builder notif, final String msg, final int max, final int progress) {
		updateNotif(notificationId, notif.setContentText(msg).setProgress(max, progress, false));
	}

	private void updateNotifResult (final int notificationId, final Builder notif, final String msg) {
		updateNotif(notificationId, notif.setOngoing(false)
				.setProgress(0, 0, false)
				.setContentText(msg));
	}

	private void updateNotif (final int notificationId, final Builder notif) {
		this.notifMgr.notify(notificationId, notif.getNotification());
	}

	private void doScans (final int notificationId, final Builder notif) throws IOException {
		final Collection<LibraryMetadata> libraries = getMediaDb().getLibraries();
		for (final LibraryMetadata library : libraries) {
			// TODO check if cancelled.
			scanForNewMedia(library, notificationId, notif);
			updateMetadataForKnowItems(library, notificationId, notif);
		}
	}

	private void scanForNewMedia (final LibraryMetadata library, final int notificationId, final Builder notif) {
		updateNotifTitle(notificationId, notif, "Updating Library: " + library.getName());
		for (final Uri source : library.getSources()) {
			// TODO check if cancelled.
			scanSourceForNewMedia(source, library, notificationId, notif);
		}
	}

	private void scanSourceForNewMedia (final Uri source, final LibraryMetadata library, final int notificationId, final Builder notif) {
		LOG.i("Scanning source: %s", source);
		updateNotifProgress(notificationId, notif, "Scanning for new files...");

		final MediaDb mediaDb = getMediaDb();

		final List<MediaItem> toAdd = new ArrayList<MediaItem>();
		final List<Long> toMarkAsFound = new ArrayList<Long>();

		final Queue<DocumentFile> dirs = new LinkedList<DocumentFile>();
		final DocumentFile root = DocumentFile.fromTreeUri(this, source);
		dirs.add(root);

		while (!dirs.isEmpty()) {
			// TODO check if cancelled.

			final DocumentFile dir = dirs.poll();
			final DocumentFile[] files = dir.listFiles();
			if (files == null) {
				LOG.w("Can not list files: %s", dir.getUri());
				continue;
			}

			for (final DocumentFile file : files) {
				// TODO check if cancelled.

				if (file.isDirectory()) {
					dirs.add(file);
				}
				else if (file.isFile()) {
					if (!isValidMediaFile(file)) continue;

					final Presence presence = mediaDb.hasMediaUri(library.getId(), file.getUri());
					if (presence == Presence.UNKNOWN) {
						LOG.i("New: %s", file.getUri());
						toAdd.add(makeMediaItem(file));
					}
					else if (presence == Presence.MISSING) {
						LOG.i("Restored: %s", file.getUri());
						toMarkAsFound.add(mediaDb.getMediaRowId(library.getId(), file.getUri()));
					}

					updateNotifProgress(notificationId, notif,
							String.format("Found %s new items, %s restored items...",
									toAdd.size(), toMarkAsFound.size()));
				}
				else {
					LOG.w("Do not know how to read: %s", file.getUri());
				}
			}
		}

		addMediaToLibrary(mediaDb, library, toAdd, toMarkAsFound);
	}

	private static void addMediaToLibrary (final MediaDb mediaDb, final LibraryMetadata library, final List<MediaItem> toAdd, final List<Long> toMarkAsFound) {
		LOG.i("Adding %s items to library %s...", toAdd.size(), library.getId());
		mediaDb.addMedia(library.getId(), toAdd);

		LOG.i("Marking %s items as found...", toMarkAsFound.size());
		mediaDb.setFilesExist(toMarkAsFound, true);
	}

	private static final Set<String> SUPPORTED_TYPES = new HashSet<String>();
	static {
		SUPPORTED_TYPES.add("application/ogg");
	}

	private static boolean isValidMediaFile (final DocumentFile file) {
		final String type = file.getType();
		if (!SUPPORTED_TYPES.contains(type) && !type.startsWith("audio")) {
			LOG.i("Not audio: %s %s", file.getUri(), type);
			return false;
		}
		if (!file.exists()) {
			LOG.i("Not exists: %s", file.getUri());
			return false;
		}
		if (!file.canRead()) {
			LOG.i("Not readable: %s", file.getUri());
			return false;
		}
		return true;
	}

	private static MediaItem makeMediaItem (final DocumentFile file) {
		return new MediaItem(file.getUri(), file.getName(), file.length(), file.lastModified(), System.currentTimeMillis());
	}

	private void updateMetadataForKnowItems (final LibraryMetadata library, final int notificationId, final Builder notif) throws IOException {
		updateNotifProgress(notificationId, notif, "Checking for expired metadata...");

		final MediaDb mediaDb = getMediaDb();
		final List<ItemToUpdate> itemsToHash = new ArrayList<ItemToUpdate>();
		final List<Long> toMarkAsMissing = new ArrayList<Long>();

		final Cursor c = mediaDb.getAllMediaCursor(library.getId(), SortColumn.PATH, SortDirection.ASC);
		try {
			if (c != null && c.moveToFirst()) {
				final MediaCursorReader reader = new MediaCursorReader();
				do {
					// TODO check if cancelled.

					final long id = reader.readId(c);
					final Uri uri = reader.readUri(c);
					final DocumentFile file = DocumentFile.fromSingleUri(this, uri);
					if (file.exists()) {
						final BigInteger libFileHash = reader.readFileHash(c);
						final long libSizeBytes = reader.readSizeBytes(c);
						final long libLastModified = reader.readFileLastModified(c);
						if (libFileHash == null || libSizeBytes != file.length() || libLastModified != file.lastModified()) {
							itemsToHash.add(new ItemToUpdate(id, uri));
						}
					}
					else {
						toMarkAsMissing.add(id);
					}
				}
				while (c.moveToNext());
			}
		}
		finally {
			IoHelper.closeQuietly(c);
		}

		LOG.i("Marking files missing: %s", toMarkAsMissing.size());
		mediaDb.setFilesExist(toMarkAsMissing, false);

		LOG.i("Checksums to calculate: %s", itemsToHash.size());
		updateNotifProgress(notificationId, notif, "Calculating checksums...");
		int i = 0;
		final byte[] buffer = ChecksumHelper.createBuffer();
		for (final ItemToUpdate item : itemsToHash) {
			// TODO check if cancelled.

			updateFileHash(item.id, item.uri, buffer);

			i += 1;
			updateNotifProgress(notificationId, notif,
					"Calculated " + i + " of " + itemsToHash.size() + " checksums...",
					itemsToHash.size(), i);
		}
	}

	private static final class ItemToUpdate {
		final long id;
		final Uri uri;

		public ItemToUpdate (final long id, final Uri uri) {
			this.id = id;
			this.uri = uri;
		}
	}

	private void updateFileHash (final long id, final Uri uri, final byte[] buffer) throws IOException {
		final InputStream is = getContentResolver().openInputStream(uri);
		try {
			final BigInteger hash = ChecksumHelper.generateMd5Checksum(is, buffer);
			LOG.i("%s %s", hash.toString(16), uri);
			final DocumentFile file = DocumentFile.fromSingleUri(this, uri);
			getMediaDb().setFileMetadata(id, file.length(), file.lastModified(), hash);
		}
		finally {
			IoHelper.closeQuietly(is);
		}
	}

}
