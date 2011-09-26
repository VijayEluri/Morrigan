package com.vaguehope.morrigan.playbackimpl.gs;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.gstreamer.State;
import org.gstreamer.elements.PlayBin;

public class GStreamerHelper {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private GStreamerHelper () { /* Unused */ }
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	static public int readFileDuration (final String fpath) {
		PlayBin playb = new PlayBin("Metadata");
		playb.setVideoSink(null);
		playb.setInputFile(new File(fpath));
		playb.setState(State.PAUSED);
		
		long queryDuration = -1;
		long startTime = System.currentTimeMillis();
		while (true) {
			queryDuration = playb.queryDuration(TimeUnit.MILLISECONDS);
			if (queryDuration > 0 || System.currentTimeMillis() - startTime > Constants.FILE_READ_DURATION_TIMEOUT) {
				break;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) { /* UNUSED */ }
		}
		playb.setState(State.NULL);
		playb.dispose();
		
		int retDuration = -1;
		if (queryDuration > 0) {
			retDuration = (int) (queryDuration / 1000);
			if (retDuration < 1) retDuration = 1;
		}
		
		return retDuration;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}