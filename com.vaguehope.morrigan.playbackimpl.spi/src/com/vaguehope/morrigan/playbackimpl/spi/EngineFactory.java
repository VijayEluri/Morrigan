package com.vaguehope.morrigan.playbackimpl.spi;

import com.vaguehope.morrigan.engines.playback.IPlaybackEngine;
import com.vaguehope.morrigan.engines.playback.PlaybackEngineFactory;

public class EngineFactory implements PlaybackEngineFactory {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public IPlaybackEngine getNewPlaybackEngine() {
		return new PlaybackEngine();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}