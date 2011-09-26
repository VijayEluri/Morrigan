package com.vaguehope.morrigan.model.media.internal.pl;

import java.util.Date;

import com.vaguehope.morrigan.model.helper.EqualHelper;
import com.vaguehope.morrigan.model.media.IMediaItem;
import com.vaguehope.morrigan.model.media.IMediaTrack;
import com.vaguehope.morrigan.model.media.internal.MediaItem;



/**
 * A media track is something that has a temporal dimension;
 * music or video.
 */
public class MediaPlaylistTrack extends MediaItem implements IMediaTrack {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private static final int DURATION_DEFAULT = -1;
	private static final int STARTCOUNT_DEFAULT = 0;
	private static final int ENDCOUNT_DEFAULT = 0;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Constructors - protected so only siblings can create instances.
	
	protected MediaPlaylistTrack (String filePath) {
		super(filePath);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public boolean isPlayable() {
		return true;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Attributes.
	
	private int duration = DURATION_DEFAULT;
	private long startCount = STARTCOUNT_DEFAULT;
	private long endCount = ENDCOUNT_DEFAULT;
	private Date dateLastPlayed = null;
	
	@Override
	public int getDuration() {
		return this.duration;
	}
	@Override
	public boolean setDuration(int duration) {
		if (this.duration != duration) {
			this.duration = duration;
			return true;
		}
		return false;
	}
	
	@Override
	public long getStartCount() {
		return this.startCount;
	}
	@Override
	public boolean setStartCount(long startCount) {
		if (this.startCount != startCount) {
			this.startCount = startCount;
			return true;
		}
		return false;
	}

	@Override
	public long getEndCount() {
		return this.endCount;
	}
	@Override
	public boolean setEndCount(long endCount) {
		if (this.endCount != endCount) {
			this.endCount = endCount;
			return true;
		}
		return false;
	}
	
	@Override
	public Date getDateLastPlayed() {
		return this.dateLastPlayed;
	}
	@Override
	public boolean setDateLastPlayed(Date dateLastPlayed) {
		if (!EqualHelper.areEqual(this.dateLastPlayed, dateLastPlayed)) {
			this.dateLastPlayed = dateLastPlayed;
			return true;
		}
		return false;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Mass setter.
	
	@Override
	public void reset() {
		super.reset();
		
		this.setDuration(DURATION_DEFAULT);
		this.setStartCount(STARTCOUNT_DEFAULT);
		this.setEndCount(ENDCOUNT_DEFAULT);
		this.setDateLastPlayed(null);
	}
	
	@Override
	public boolean setFromMediaItem (IMediaItem mi) {
		boolean setFromMediaItem = super.setFromMediaItem(mi);
		
		if (mi instanceof IMediaTrack) {
			IMediaTrack mt = (IMediaTrack) mi;
			
			boolean b = this.setDuration(mt.getDuration())
				| this.setStartCount(mt.getStartCount())
				| this.setEndCount(mt.getEndCount())
				| this.setDateLastPlayed(mt.getDateLastPlayed());
			
			return b | setFromMediaItem;
		}
		
		return setFromMediaItem;
	}
	
	@Override
	public boolean setFromMediaTrack(IMediaTrack mt) {
		boolean b =
			  this.setFromMediaItem(mt)
			| this.setDuration(mt.getDuration())
			| this.setStartCount(mt.getStartCount())
			| this.setEndCount(mt.getEndCount())
			| this.setDateLastPlayed(mt.getDateLastPlayed());
		return b;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}