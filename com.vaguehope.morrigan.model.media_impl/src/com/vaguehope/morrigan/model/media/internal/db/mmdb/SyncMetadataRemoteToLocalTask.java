package com.vaguehope.morrigan.model.media.internal.db.mmdb;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaguehope.morrigan.model.exceptions.MorriganException;
import com.vaguehope.morrigan.model.media.ILocalMixedMediaDb;
import com.vaguehope.morrigan.model.media.IMixedMediaItem;
import com.vaguehope.morrigan.model.media.IRemoteMixedMediaDb;
import com.vaguehope.morrigan.model.media.MediaTag;
import com.vaguehope.morrigan.model.media.MediaTagClassification;
import com.vaguehope.morrigan.model.media.impl.MediaFactoryImpl;
import com.vaguehope.morrigan.tasks.IMorriganTask;
import com.vaguehope.morrigan.tasks.TaskEventListener;
import com.vaguehope.morrigan.tasks.TaskResult;
import com.vaguehope.morrigan.tasks.TaskResult.TaskOutcome;
import com.vaguehope.sqlitewrapper.DbException;


public class SyncMetadataRemoteToLocalTask implements IMorriganTask {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private final ILocalMixedMediaDb local;
	private final IRemoteMixedMediaDb remote;

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	public SyncMetadataRemoteToLocalTask (ILocalMixedMediaDb local, IRemoteMixedMediaDb remote) {
		this.local = local;
		this.remote = remote;
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	@Override
	public String getTitle () {
		// TODO make this more sensible.
		return "Sync metadata '"+this.local+"' x '"+this.remote+"' desu~";
	}

	@Override
	public TaskResult run (TaskEventListener taskEventListener) {
		taskEventListener.onStart();
		TaskResult ret;
		try {
			final ILocalMixedMediaDb trans = MediaFactoryImpl.get().getLocalMixedMediaDbTransactional(this.local);
			try {
				trans.read();
				// FIXME add getByHashcode() to local DB.
				// Build list of all hashed local items.
				final Map<BigInteger, IMixedMediaItem> localItems = new HashMap<BigInteger, IMixedMediaItem>();
				for (IMixedMediaItem localItem : trans.getAllDbEntries()) {
					BigInteger hashcode = localItem.getHashcode();
					if (hashcode != null && !BigInteger.ZERO.equals(hashcode)) localItems.put(hashcode, localItem);
				}

				// All remote items.
				final List<IMixedMediaItem> remoteItems = this.remote.getAllDbEntries();

				// Describe what we are doing.
				String taskTitle = "Synchronising metadata from " + this.remote.getListName() + " to " + this.local.getListName() + ".";
				taskEventListener.beginTask(taskTitle, remoteItems.size()); // Work total is number of remote items.

				// For each remote item, see if there is a local item to update.
				for (IMixedMediaItem remoteItem : remoteItems) {
					BigInteger hashcode = remoteItem.getHashcode();
					if (hashcode != null && !BigInteger.ZERO.equals(hashcode)) {
						final IMixedMediaItem localItem = localItems.get(hashcode);
						if (localItem != null) {
							taskEventListener.subTask(localItem.getTitle());
							syncMediaItems(trans, this.remote, remoteItem, localItem);
						}
					}
					taskEventListener.worked(1); // Increment one for each remote item.
					if (taskEventListener.isCanceled()) break;
				}

				trans.commitOrRollback();
				this.local.forceRead(); // TODO replace by using bulk-update methods?  e.g. in MixedMediaDbFeedParser.

				if (taskEventListener.isCanceled()) {
					taskEventListener.logMsg(this.getTitle(), "Sync task was canceled desu~."); // TODO is this quite right?
					ret = new TaskResult(TaskOutcome.CANCELED);
				}
				else {
					ret = new TaskResult(TaskOutcome.SUCCESS);
				}
				taskEventListener.done();
			}
			finally {
				trans.dispose();
			}
		}
		catch (DbException e) {
			ret = new TaskResult(TaskOutcome.FAILED, "Throwable while sync metadata.", e);
		}
		catch (MorriganException e) {
			ret = new TaskResult(TaskOutcome.FAILED, "Throwable while sync metadata.", e);
		}

		return ret;
	}

	private static void syncMediaItems (final ILocalMixedMediaDb ldb, IRemoteMixedMediaDb rdb, IMixedMediaItem remoteItem, IMixedMediaItem localItem) throws MorriganException {
		if (remoteItem.getStartCount() > localItem.getStartCount()) {
			ldb.setTrackStartCnt(localItem, remoteItem.getStartCount());
		}

		if (remoteItem.getEndCount() > localItem.getEndCount()) {
			ldb.setTrackEndCnt(localItem, remoteItem.getEndCount());
		}

		if (remoteItem.getDateAdded().getTime() > 0 && remoteItem.getDateAdded().getTime() < localItem.getDateAdded().getTime()) {
			ldb.setItemDateAdded(localItem, remoteItem.getDateAdded());
		}

		if (
				remoteItem.getDateLastPlayed() != null && remoteItem.getDateLastPlayed().getTime() > 0
				&& (localItem.getDateLastPlayed() == null || remoteItem.getDateLastPlayed().getTime() > localItem.getDateLastPlayed().getTime())
				) {
			ldb.setTrackDateLastPlayed(localItem, remoteItem.getDateLastPlayed());
		}

		if (remoteItem.isEnabled() != localItem.isEnabled()) {
			ldb.setItemEnabled(localItem, remoteItem.isEnabled());
		}

		List<MediaTag> rTags = rdb.getTags(remoteItem);
		if (rTags != null && rTags.size() > 0) {
			for (MediaTag rTag : rTags) {
				MediaTagClassification cls = rTag.getClassification();
				String clsString = cls == null ? null : cls.getClassification();
				ldb.addTag(localItem, rTag.getTag(), rTag.getType(), clsString);
			}
		}
	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
