package com.vaguehope.morrigan.model.media;

import java.util.Date;

public interface IMediaTrack extends IMediaItem {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	boolean isPlayable ();

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	int getDuration();
	boolean setDuration(int duration);

	long getStartCount();
	boolean setStartCount(long startCount);

	long getEndCount();
	boolean setEndCount(long endCount);

	Date getDateLastPlayed();
	boolean setDateLastPlayed(Date dateLastPlayed);

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	boolean setFromMediaTrack (IMediaTrack mt);

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
