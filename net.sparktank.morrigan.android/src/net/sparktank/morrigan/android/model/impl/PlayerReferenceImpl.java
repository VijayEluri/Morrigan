/*
 * Copyright 2010 Fae Hutter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.sparktank.morrigan.android.model.impl;

import net.sparktank.morrigan.android.Constants;
import net.sparktank.morrigan.android.model.PlayerReference;
import net.sparktank.morrigan.android.model.ServerReference;

public class PlayerReferenceImpl implements PlayerReference {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private final int playerId;
	private final String baseUrl;
	private final ServerReference serverReference;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public PlayerReferenceImpl (ServerReference serverReference, int playerId) {
		if (serverReference == null) throw new IllegalArgumentException();
		
		this.playerId = playerId;
		this.baseUrl = serverReference.getBaseUrl() + Constants.CONTEXT_PLAYERS + "/" + playerId;
		this.serverReference = serverReference;
	}
	
//	public PlayerReferenceImpl (String baseUrl, ServerReference serverReference) {
//		if (baseUrl == null) throw new IllegalArgumentException();
//		if (serverReference == null) throw new IllegalArgumentException();
//		
//		this.baseUrl = baseUrl;
//		this.serverReference = serverReference;
//	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public int getPlayerId() {
		return this.playerId;
	}
	
	@Override
	public String getBaseUrl() {
		return this.baseUrl;
	}
	
	@Override
	public ServerReference getServerReference() {
		return this.serverReference;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
