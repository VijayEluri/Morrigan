package net.sparktank.morrigan.server.feedreader;

import java.io.IOException;
import java.util.Date;

import net.sparktank.morrigan.exceptions.MorriganException;
import net.sparktank.morrigan.model.library.MediaLibraryTrack;
import net.sparktank.morrigan.model.library.remote.RemoteMediaLibrary;
import net.sparktank.morrigan.model.tasks.TaskEventListener;
import net.sparktank.morrigan.server.HttpClient;
import net.sparktank.morrigan.server.feedwriters.XmlHelper;
import net.sparktank.sqlitewrapper.DbException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Deprecated
public class MediaListFeedParser {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static void parseFeed (RemoteMediaLibrary library, TaskEventListener taskEventListener) throws MorriganException, DbException {
		String xmlString;
		try {
			xmlString = HttpClient.getHttpClient().doHttpRequest(library.getUrl()).getBody();
		} catch (IOException e) {
			throw new MorriganException(e);
		}
		
		boolean thereWereErrors = true;
		try {
			library.setAutoCommit(false);
			library.beginBulkUpdate();
			FeedParser.parseFeed(xmlString, taskEventListener, new EntryHandler(library));
			thereWereErrors = false;
		} finally {
			try {
				library.completeBulkUpdate(thereWereErrors);
			} finally {
				try {
					library.commit();
				} finally {
					library.setAutoCommit(true);
				}
			}
		}
		
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private static class EntryHandler implements IEntryHandler {
		
		private final RemoteMediaLibrary library;

		public EntryHandler (RemoteMediaLibrary library) {
			this.library = library;
		}
		
		@Override
		public void parseEntry(Node entry) throws FeedParseException, DbException {
			NodeList childNodes = entry.getChildNodes();
			if (childNodes.getLength() < 1) {
				throw new FeedParseException("Entry contains no elements.");
			}
			
			MediaLibraryTrack mi = new MediaLibraryTrack();
			
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node item = childNodes.item(i);
				
				if (item.getNodeName().equals("link")) {
					NamedNodeMap att = item.getAttributes();
					
					Node relNode = att.getNamedItem("rel");
					if (relNode != null) {
						String relVal = relNode.getNodeValue();
						if (relVal != null && relVal.equals("self")) {
							Node hrefNode = att.getNamedItem("href");
							if (hrefNode != null) {
								String hrefVal = hrefNode.getNodeValue();
								if (hrefVal != null) {
									mi.setRemoteLocation(hrefVal);
								}
							} else {
								throw new FeedParseException("Link missing 'href' att.");
							}
						}
					} else {
						throw new FeedParseException("Link missing 'rel' att.");
					}
					
				} else if (item.getNodeName().equals("title")) {
					mi.setFilepath(item.getTextContent());
					
				} else if (item.getNodeName().equals("duration")) {
					int v = Integer.parseInt(item.getTextContent());
					mi.setDuration(v);
					
				} else if (item.getNodeName().equals("hash")) {
					long v = Long.parseLong(item.getTextContent());
					mi.setHashcode(v);
					
				} else if (item.getNodeName().equals("startcount")) {
					long v = Long.parseLong(item.getTextContent());
					mi.setStartCount(v);
					
				} else if (item.getNodeName().equals("endcount")) {
					long v = Long.parseLong(item.getTextContent());
					mi.setEndCount(v);
					
				} else if (item.getNodeName().equals("dateadded")) {
					try {
						Date d = XmlHelper.getIso8601UtcDateFormatter().parse(item.getTextContent());
						mi.setDateAdded(d);
						
					} catch (Exception e) {
						throw new FeedParseException("Exception parsing date '"+item.getTextContent()+"'.", e);
					}
					
				} else if (item.getNodeName().equals("datelastmodified")) {
					try {
						Date d = XmlHelper.getIso8601UtcDateFormatter().parse(item.getTextContent());
						mi.setDateLastModified(d);
						
					} catch (Exception e) {
						throw new FeedParseException("Exception parsing date '"+item.getTextContent()+"'.", e);
					}
					
				} else if (item.getNodeName().equals("datelastplayed")) {
					try {
						Date d = XmlHelper.getIso8601UtcDateFormatter().parse(item.getTextContent());
						mi.setDateLastPlayed(d);
						
					} catch (Exception e) {
						throw new FeedParseException("Exception parsing date '"+item.getTextContent()+"'.", e);
					}
				}
				
			}
			
			try {
				library.updateItem(mi);
			} catch (MorriganException e) {
				throw new FeedParseException(e);
			}
		}
		
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
