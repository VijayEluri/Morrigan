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

import org.xml.sax.SAXException;

import net.sparktank.morrigan.android.helper.XmlParser;
import net.sparktank.morrigan.android.model.PlayState;
import net.sparktank.morrigan.android.model.PlayerState;

public class PlayerStateImpl extends XmlParser implements PlayerState {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private static final String TRACKDURATION = "trackduration";
	private static final String TRACKFILE = "trackfile";
	private static final String PLAYPOSITION = "playposition";
	private static final String TRACKTITLE = "tracktitle";
	private static final String LISTID = "listid";
	private static final String LISTTITLE = "listtitle";
	private static final String QUEUEDURATION = "queueduration";
	private static final String QUEUELENGTH = "queuelength";
	private static final String PLAYORDER = "playorder";
	private static final String PLAYSTATE = "playstate";
	private static final String PLAYERID = "playerid";
	
	public final static String[] nodes = new String[] { 
		PLAYERID,
		PLAYSTATE,
		PLAYORDER,
		QUEUELENGTH,
        QUEUEDURATION,
        LISTTITLE,
        LISTID,
        TRACKTITLE,
        PLAYPOSITION,
        TRACKFILE,
        TRACKDURATION
		};
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public PlayerStateImpl (String xmlString) throws SAXException {
		super(xmlString, nodes);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	@Override
	public int getId() {
		return this.getNodeInt(PLAYERID);
	}
	
	@Override
	public PlayState getPlayState() {
		return PlayState.parseN(this.getNodeInt(PLAYSTATE));
	}
	
	@Override
	public int getPlayOrder() {
		return this.getNodeInt(PLAYORDER);
	}
	
	@Override
	public int getPlayerPosition() {
		return this.getNodeInt(PLAYPOSITION);
	}
	
	@Override
	public String getListTitle() {
		return this.getNode(LISTTITLE);
	}
	
	@Override
	public String getListId() {
		return this.getNode(LISTID);
	}
	
	@Override
	public String getTrackTitle() {
		return this.getNode(TRACKTITLE);
	}
	
	@Override
	public String getTrckFile() {
		return this.getNode(TRACKFILE);
	}
	
	@Override
	public int getTrackDuration() {
		return this.getNodeInt(TRACKDURATION);
	}
	
	@Override
	public int getQueueLength() {
		return this.getNodeInt(QUEUELENGTH);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}