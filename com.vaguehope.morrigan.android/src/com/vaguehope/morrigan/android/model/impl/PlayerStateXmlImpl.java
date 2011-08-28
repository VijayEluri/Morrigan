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

package com.vaguehope.morrigan.android.model.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.vaguehope.morrigan.android.model.MlistItem;
import com.vaguehope.morrigan.android.model.PlayState;
import com.vaguehope.morrigan.android.model.PlayerReference;
import com.vaguehope.morrigan.android.model.PlayerState;

public class PlayerStateXmlImpl implements PlayerState, MlistItem, ContentHandler {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static final String PLAYERID = "playerid";
	public static final String PLAYORDER = "playorder";
	public static final String PLAYSTATE = "playstate";
	public static final String PLAYPOSITION = "playposition";
	
	public static final String TRACKLINKNAME = "track"; // Name of link rel attribute.
	public static final String TRACKTITLE = "tracktitle";
	public static final String TRACKFILE = "trackfile";
	public static final String TRACKFILENAME = "trackfilename";
	public static final String TRACKDURATION = "trackduration";
	
	public static final String TRACKHASHCODE = "trackhash";
	public static final String TRACKENABLED = "trackenabled";
	public static final String TRACKMISSING = "trackmissing";
	
	public static final String TRACKSTARTCOUNT = "trackstartcount";
	public static final String TRACKENDCOUNT = "trackendcount";
	
	public static final String LISTID = "listid";
//	public static final String LISTURL = "list"; // Because its a link.
	public static final String LISTTITLE = "listtitle";
	
	public static final String QUEUEDURATION = "queueduration";
	public static final String QUEUELENGTH = "queuelength";
	
	public static final String MONITOR = "monitor";
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private final PlayerReference playerReference;
	
	private int playerId;
	private int playerOrder;
	private PlayState playerState;
	private int playerPosition;
	
	private String trackRelativeUrl;
	private String trackTitle;
	private String trackFile;
	private String trackFileName;
	private int trackDuration;
	private BigInteger trackHashCode;
	private boolean trackEnabled;
	private boolean trackMissing;
	private int trackStartCount;
	private int trackEndCount;
	
	private String listId;
	private String listUrl;
	private String listTitle;
	
	private int queueLength;
	private long queueDuration;
	
	Map<Integer, String> monitors;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public PlayerStateXmlImpl (InputStream dataIs, PlayerReference playerReference) throws SAXException {
		this.playerReference = playerReference;
		this.monitors = new LinkedHashMap<Integer, String>();
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp;
		try {
			sp = spf.newSAXParser();
			XMLReader xmlReader = sp.getXMLReader();
			xmlReader.setContentHandler(this);
			try {
				xmlReader.parse(new InputSource(dataIs));
			}
			catch (IOException e) {
				throw new SAXException(e);
			}
		}
		catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public PlayerReference getPlayerReference() {
		return this.playerReference;
	}
	
	@Override
	public String getTitle() {
		return getTrackTitle();
	}
	
	@Override
	public int getImageResource() {
		return PlayerStateBasicImpl.getImageResource(getPlayState());
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public int getId() {
		return this.playerId;
	}
	
	@Override
	public PlayState getPlayState() {
		return this.playerState;
	}
	
	@Override
	public int getPlayOrder() {
		return this.playerOrder;
	}
	
	@Override
	public int getPlayerPosition() {
		return this.playerPosition;
	}
	
	@Override
	public String getListTitle() {
		return this.listTitle;
	}
	
	@Override
	public String getListId() {
		return this.listId;
	}
	
	@Override
	public String getListUrl() {
		return this.listUrl;
	}
	
	@Override
	public String getTrackRelativeUrl () {
		return this.trackRelativeUrl;
	}
	
	@Override
	public String getTrackTitle() {
		return this.trackTitle;
	}
	
	@Override
	public String getTrackFile() {
		return this.trackFile;
	}
	
	@Override
	public String getTrackFileName() {
		return this.trackFileName;
	}
	
	@Override
	public int getTrackDuration() {
		return this.trackDuration;
	}
	
	@Override
	public BigInteger getTrackHashCode () {
		return this.trackHashCode;
	}
	
	@Override
	public boolean getTrackEnabled() {
		return this.trackEnabled;
	}
	
	@Override
	public boolean getTrackMissing() {
		return this.trackMissing;
	}
	
	@Override
	public int getTrackStartCount () {
		return this.trackStartCount;
	}
	
	@Override
	public int getTrackEndCount () {
		return this.trackEndCount;
	}
	
	@Override
	public MlistItem getItem () {
		return this;
	}
	
	@Override
	public int getQueueLength() {
		return this.queueLength;
	}
	
	@Override
	public long getQueueDuration() {
		return this.queueDuration;
	}
	
	@Override
	public Map<Integer, String> getMonitors() {
		return Collections.unmodifiableMap(this.monitors);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	MlistItem methods.
	
	@Override
	public int getType () {
		return -1;
	}
	
	@Override
	public String getRelativeUrl () {
		return getTrackRelativeUrl();
	}
	
	@Override
	public String getFileName () {
		return getTrackFileName();
	}
	
	@Override
	public BigInteger getHashCode () {
		return getTrackHashCode();
	}
	
	@Override
	public boolean isEnabled() {
		return getTrackEnabled();
	}
	
	@Override
	public boolean isMissing() {
		return getTrackMissing();
	}
	
	@Override
	public int getDuration () {
		return getTrackDuration();
	}
	
	@Override
	public int getStartCount () {
		return getTrackStartCount();
	}
	
	@Override
	public int getEndCount () {
		return getTrackEndCount();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private final Stack<String> stack = new Stack<String>();
	private StringBuilder currentText;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		this.stack.push(localName);
		
		if (this.stack.size() == 2 && localName.equals("link")) {
			String relVal = attributes.getValue("rel");
			if (relVal != null && relVal.equals("list")) {
				String hrefVal = attributes.getValue("href");
				if (hrefVal != null && hrefVal.length() > 0) {
					this.listUrl = this.playerReference.getServerReference().getBaseUrl() + hrefVal;
				}
			}
			else if (relVal != null && relVal.equals(TRACKLINKNAME)) {
				String hrefVal = attributes.getValue("href");
				if (hrefVal != null && hrefVal.length() > 0) {
					this.trackRelativeUrl = hrefVal;
				}
			}
		}
		
		// If we need a new StringBuilder, make one.
		if (this.currentText == null || this.currentText.length() > 0) {
			this.currentText = new StringBuilder();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (this.stack.size() == 2) {
			if (localName.equals(PLAYERID)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.playerId = v;
			}
			else if (localName.equals(PLAYORDER)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.playerOrder = v;
			}
			else if (localName.equals(PLAYSTATE)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.playerState = PlayState.parseN(v);
			}
			else if (localName.equals(PLAYPOSITION)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.playerPosition = v;
			}
			else if (localName.equals(TRACKTITLE)) {
				this.trackTitle = this.currentText.toString();
			}
			else if (localName.equals(TRACKFILE)) {
				this.trackFile = this.currentText.toString();
			}
			else if (localName.equals(TRACKFILENAME)) {
				this.trackFileName = this.currentText.toString();
			}
			else if (localName.equals(TRACKDURATION)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.trackDuration = v;
			}
			else if (localName.equals(TRACKHASHCODE)) {
				BigInteger v = new BigInteger(this.currentText.toString(), 16);
				this.trackHashCode = v;
			}
			else if (localName.equals(TRACKENABLED)) {
				boolean v = Boolean.parseBoolean(this.currentText.toString());
				this.trackEnabled = v;
			}
			else if (localName.equals(TRACKMISSING)) {
				boolean v = Boolean.parseBoolean(this.currentText.toString());
				this.trackMissing = v;
			}
			else if (localName.equals(TRACKSTARTCOUNT)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.trackStartCount = v;
			}
			else if (localName.equals(TRACKENDCOUNT)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.trackEndCount = v;
			}
			else if (localName.equals(LISTID)) {
				this.listId = this.currentText.toString();
			}
			else if (localName.equals(LISTTITLE)) {
				this.listTitle = this.currentText.toString();
			}
			else if (localName.equals(QUEUEDURATION)) {
				long v = Long.parseLong(this.currentText.toString());
				this.queueDuration = v;
			}
			else if (localName.equals(QUEUELENGTH)) {
				int v = Integer.parseInt(this.currentText.toString());
				this.queueLength = v;
			}
			else if (localName.equals(MONITOR)) {
				String data = this.currentText.toString();
				int indexOf = data.indexOf(":");
				String idString = data.substring(0, indexOf);
				String desString = data.substring(indexOf + 1);
				Integer id = Integer.valueOf(idString);
				this.monitors.put(id, desString);
			}
		}
		
		this.stack.pop();
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
        this.currentText.append( ch, start, length );
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public void endDocument() throws SAXException { /* UNUSED */ }
	@Override
	public void endPrefixMapping(String prefix) throws SAXException { /* UNUSED */ }
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException { /* UNUSED */ }
	@Override
	public void processingInstruction(String target, String data) throws SAXException { /* UNUSED */ }
	@Override
	public void setDocumentLocator(Locator locator) { /* UNUSED */ }
	@Override
	public void skippedEntity(String name) throws SAXException { /* UNUSED */ }
	@Override
	public void startDocument() throws SAXException { /* UNUSED */ }
	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException { /* UNUSED */ }
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
	