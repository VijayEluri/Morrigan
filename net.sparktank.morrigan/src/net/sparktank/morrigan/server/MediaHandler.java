package net.sparktank.morrigan.server;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sparktank.morrigan.helpers.ErrorHelper;
import net.sparktank.morrigan.model.tasks.TaskEventListener;
import net.sparktank.morrigan.model.tracks.library.local.LocalLibraryHelper;
import net.sparktank.morrigan.model.tracks.library.local.LocalLibraryUpdateTask;
import net.sparktank.morrigan.model.tracks.library.local.LocalMediaLibrary;
import net.sparktank.morrigan.model.tracks.playlist.MediaPlaylist;
import net.sparktank.morrigan.model.tracks.playlist.PlaylistHelper;
import net.sparktank.morrigan.server.feedwriters.LibrarySrcFeed;
import net.sparktank.morrigan.server.feedwriters.MediaFeed;
import net.sparktank.morrigan.server.feedwriters.MediaListFeed;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class MediaHandler extends AbstractHandler {
	//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		System.err.println("request:t=" + target + ", m=" + request.getMethod());
		
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		PrintWriter out = response.getWriter();
		
		try {
			if (target.equals("/")) {
				new MediaFeed().process(out);
				
			} else {
				String r = target.substring(1);
				Map<?,?> paramMap = request.getParameterMap();
				
				String[] split = r.split("/");
				String type = split[0].toLowerCase();
				
				if (split.length > 1) {
					String id = split[1];
					if (type.equals("library")) {
						String f = LocalLibraryHelper.getFullPathToLib(id);
						LocalMediaLibrary ml = LocalMediaLibrary.FACTORY.manufacture(f);
						
						if (split.length > 2) {
							String param = split[2];
							if (param.equals("src")) {
								if (split.length > 3) {
									String cmd = split[3];
									if (cmd.equals("add")) {
										if (paramMap.containsKey("dir")) {
											String[] v = (String[]) paramMap.get("dir");
											ml.addSource(v[0]);
											out.print("Added src '"+v[0]+"'.");
										} else {
											out.print("To add a src, POST with param 'dir' set.");
										}
									} else if (cmd.equals("remove")) {
										if (paramMap.containsKey("dir")) {
											String[] v = (String[]) paramMap.get("dir");
											ml.removeSource(v[0]);
											out.print("Removed src '"+v[0]+"'.");
										} else {
											out.print("To remove a src, POST with param 'dir' set.");
										}
									}
									
								} else {
									new LibrarySrcFeed(ml).process(out);
								}
								
							} else if (param.equals("scan")) {
								if (scheduleLibScan(ml)) {
									out.print("Scan scheduled.");
								} else {
									out.print("Failed to schedule scan.");
								}
								
							} else {
								String filename = URLDecoder.decode(param, "UTF-8");
								File file = new File(filename);
								if (file.exists()) {
									System.err.println("About to send file '"+file.getAbsolutePath()+"'.");
									returnFile(file, response);
								}
							}
							
						} else {
							MediaListFeed<LocalMediaLibrary> libraryFeed = new MediaListFeed<LocalMediaLibrary>(ml);
							libraryFeed.process(out);
						}
						
					} else if (type.equals("playlist")) {
						String f = PlaylistHelper.getFullPathToPlaylist(id);
						MediaPlaylist ml = MediaPlaylist.FACTORY.manufacture(f);
						MediaListFeed<MediaPlaylist> libraryFeed = new MediaListFeed<MediaPlaylist>(ml);
						libraryFeed.process(out);
					}
					
				} else if (type.equals("newlib")) {
					if (paramMap.containsKey("name")) {
						String[] v = (String[]) paramMap.get("name");
						LocalLibraryHelper.createLib(v[0]);
					} else {
						out.print("To create a library, POST with param 'name' set.");
					}
				}
			} 
			
		} catch (Throwable t) {
			out.print(ErrorHelper.getStackTrace(t));
		}
	}
	
	//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void returnFile (File file, HttpServletResponse response) throws IOException {
		response.reset();
		
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Description", "File Transfer");
		response.addHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
		response.addHeader("Content-Transfer-Encoding", "binary");
		response.addHeader("Expires", "0");
		response.addHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.addHeader("Pragma", "public");
		response.addHeader("Content-Length", String.valueOf(file.length()));
		
		FileInputStream fileInputStream = null;
		ServletOutputStream outputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			outputStream = response.getOutputStream();
			
			BufferedInputStream buf = new BufferedInputStream(fileInputStream);
			// FIXME this could be done better?
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = buf.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			
			response.flushBuffer();
			
		} finally {
			if (fileInputStream != null) fileInputStream.close();
			if (outputStream != null) outputStream.close();
		}
		
	}
	
	//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private boolean scheduleLibScan (final LocalMediaLibrary ml) {
		final LocalLibraryUpdateTask task = LocalLibraryUpdateTask.FACTORY.manufacture(ml);
		if (task != null) {
			
			Thread t = new Thread () {
				@Override
				public void run() {
					task.run(new LibScanMon(ml.getListName()));
				}
			};
			t.start();
			System.err.println("Scan of " + ml.getListId() + " scheduled on thread " + t.getId() + ".");
			return true;
			
		}
		
		System.err.println("Failed to get task object from factory method.");
		return false;
	}
	
	static class LibScanMon implements TaskEventListener {
		
		private final String logPrefix;
		private int totalWork = 0;
		private int workDone = 0;
		private boolean canceled;
		
		public LibScanMon (String logPrefix) {
			this.logPrefix = logPrefix;
		}
		
		@Override
		public void logMsg(String topic, String s) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			sb.append(topic);
			sb.append("] ");
			sb.append(s);
			
			System.out.println(sb.toString());
		}
		
		@Override
		public void logError(String topic, String s, Throwable t) {
			String causeTrace = ErrorHelper.getCauseTrace(t);
			logMsg(topic, s.concat("\n".concat(causeTrace)));
		}
		
		@Override
		public void onStart() {/* UNUSED */}
		
		@Override
		public void beginTask(String name, int totalWork) {
			this.totalWork = totalWork;
			System.out.println("[" + this.logPrefix + "] starting task: " + name + ".");
		}
		
		@Override
		public void done() {
			System.out.println("[" + this.logPrefix + "] done.");
		}
		
		@Override
		public void subTask(String name) {
			System.out.println("[" + this.logPrefix + "] sub task: "+name+".");
		}
		
		@Override
		public boolean isCanceled() {
			return this.canceled;
		}
		
		@Override
		public void worked(int work) {
			this.workDone = this.workDone + work;
			System.out.println("[" + this.logPrefix + "] worked " + this.workDone + " of " + this.totalWork + ".");
		}
		
	}
	
	//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
