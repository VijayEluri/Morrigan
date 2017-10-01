package com.vaguehope.morrigan.android.playback;

import java.lang.ref.WeakReference;
import java.util.Collection;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.vaguehope.morrigan.android.R;
import com.vaguehope.morrigan.android.helper.LogWrapper;
import com.vaguehope.morrigan.android.helper.Result;
import com.vaguehope.morrigan.android.playback.MediaDb.MediaWatcher;
import com.vaguehope.morrigan.android.playback.MediaDb.SortColumn;
import com.vaguehope.morrigan.android.playback.MediaDb.SortDirection;

public class LibraryFragment extends Fragment {

	private static final LogWrapper LOG = new LogWrapper("LF");

	private static final int MENU_LIBRARY_ID_START = 1100;
	private static final int MENU_LIBRARY_COLUMN_START = 1200;
	private static final int MENU_LIBRARY_DIRECTION_START = 1300;

	// Intent params.
	private int fragmentPosition;

	private MessageHandler messageHandler;

	private Button btnLibrary;
	private ListView mediaList;

	private MediaListCursorAdapter adapter;
	private ScrollState scrollState;

	private Collection<LibraryMetadata> allLibraries;
	private PopupMenu libraryMenu;
	private LibraryMetadata currentLibrary;
	private SortColumn currentSortColumn = SortColumn.PATH;
	private SortDirection currentSortDirection = SortDirection.ASC;

	@Override
	public View onCreateView (final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		this.fragmentPosition = getArguments().getInt(SectionsPagerAdapter.ARG_FRAGMENT_POSITION, -1);
		if (this.fragmentPosition < 0) throw new IllegalArgumentException("Missing fragmentPosition.");

		this.messageHandler = new MessageHandler(this);

		final View rootView = inflater.inflate(R.layout.playback_library, container, false);
		wireGui(rootView, container);
		return rootView;
	}

	@Override
	public void onResume () {
		super.onResume();
		resumeMc();
	}

	@Override
	public void onPause () {
		suspendDb();
		super.onPause();
	}

	@Override
	public void onDestroy () {
		disposeDb();
		super.onDestroy();
	}

	// Service.

	private MediaClient bndMc;

	private void resumeMc () {
		if (this.bndMc == null) {
			LOG.d("Binding service...");
			this.bndMc = new MediaClient(getActivity(), LOG.getPrefix(), new Runnable() {
				@Override
				public void run () {
					/*
					 * this convoluted method is because the service connection
					 * won't finish until this thread processes messages again
					 * (i.e., after it exits this thread). if we try to talk to
					 * the DB service before then, it will NPE.
					 */
					getMediaDb().addMediaWatcher(getMediaWatcher());
					LOG.d("Service bound.");
				}
			});
		}
		else if (getMediaDb() != null) { // because we stop listening in onPause(), we must resume if the user comes back.
			getMediaDb().addMediaWatcher(getMediaWatcher());
			LOG.d("Service rebound.");
		}
		else {
			LOG.w("resumeMc() called while service is half bound.  I do not know what to do.");
		}
	}

	private void suspendDb () {
		// We might be pausing before the callback has come.
		final MediaServices ms = this.bndMc.getService();
		if (ms != null) { // We might be pausing before the callback has come.
			ms.getMediaDb().removeMediaWatcher(getMediaWatcher());
		}
		else { // If we have not even had the callback yet, cancel it.
			this.bndMc.clearReadyListener();
		}
		LOG.d("Service released.");
	}

	private void disposeDb () {
		if (this.adapter != null) this.adapter.dispose();
		if (this.bndMc != null) this.bndMc.dispose();
	}

	protected Playbacker getPlaybacker () {
		final MediaClient d = this.bndMc;
		if (d == null) return null;
		return d.getService().getPlaybacker();
	}

	protected MediaDb getMediaDb () {
		final MediaClient d = this.bndMc;
		if (d == null) return null;
		return d.getService().getMediaDb();
	}

	protected MediaListCursorAdapter getAdapter () {
		return this.adapter;
	}

	// GUI.

	private void wireGui (final View rootView, final ViewGroup container) {
		this.adapter = new MediaListCursorAdapter(container.getContext());

		this.btnLibrary = (Button) rootView.findViewById(R.id.btnLibrary);
		this.btnLibrary.setOnClickListener(this.btnLibraryOnClickListener);

		this.mediaList = (ListView) rootView.findViewById(R.id.mediaList);
		this.mediaList.setAdapter(this.adapter);
		this.mediaList.setOnItemClickListener(this.mediaListOnItemClickListener);
	}

	protected MediaWatcher getMediaWatcher () {
		return this.mediaWatcher;
	}

	private final MediaWatcher mediaWatcher = new MediaWatcherAdapter() {
		@Override
		public void librariesChanged () {
			LibraryFragment.this.messageHandler.sendEmptyMessage(Msgs.LIBRARIES_CHANGED.ordinal());
		}
	};

	protected enum Msgs {
		LIBRARIES_CHANGED,
		LIBRARY_CHANGED;
		public static final Msgs values[] = values(); // Optimisation to avoid new array every time.
	}

	private static class MessageHandler extends Handler {

		private final WeakReference<LibraryFragment> parentRef;

		public MessageHandler (final LibraryFragment libraryFragment) {
			this.parentRef = new WeakReference<LibraryFragment>(libraryFragment);
		}

		@Override
		public void handleMessage (final Message msg) {
			final LibraryFragment parent = this.parentRef.get();
			if (parent != null) parent.msgOnUiThread(msg);
		}
	}

	protected void msgOnUiThread (final Message msg) {
		final Msgs m = Msgs.values[msg.what];
		switch (m) {
			case LIBRARIES_CHANGED:
				onLibrariesChanged();
				break;
			case LIBRARY_CHANGED:
				// TODO check is the selected library that changed?
				reloadLibrary();
				break;
			default:
		}
	}

	private final OnClickListener btnLibraryOnClickListener = new OnClickListener() {
		@Override
		public void onClick (final View v) {
			final PopupMenu menu = LibraryFragment.this.libraryMenu;
			if (menu != null) menu.show();
		}
	};

	private final OnItemClickListener mediaListOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick (final AdapterView<?> parent, final View view, final int position, final long id) {
			MediaItem mediaItem = getMediaDb().getMediaItem(id);
			if (mediaItem != null) {
				addMediaItemToQueue(mediaItem);
			}
			else {
				LOG.w("Item %s not found in DB.", id);
			}
		}
	};

	private void addMediaItemToQueue (final MediaItem mediaItem) {
		final Playbacker pb = getPlaybacker();
		if (pb != null) {
			QueueItem item = new QueueItem(getActivity(), mediaItem.getUri());
			pb.getQueue().add(item);
			pb.notifyQueueChanged();
			LOG.i("Added to queue: %s", item);
		}
	}

	private void onLibrariesChanged () {
		this.allLibraries = getMediaDb().getLibraries();

		final PopupMenu newMenu = new PopupMenu(getActivity(), LibraryFragment.this.btnLibrary);
		for (final LibraryMetadata library : this.allLibraries) {
			final MenuItem item = newMenu.getMenu().add(Menu.NONE, MENU_LIBRARY_ID_START + (int) library.getId(), Menu.NONE, library.getName());
			item.setCheckable(true);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick (final MenuItem item) {
					setCurrentLibrary(library);
					return true;
				}
			});
		}
		for (final SortColumn s : SortColumn.values()) {
			final MenuItem item = newMenu.getMenu().add(Menu.NONE, MENU_LIBRARY_COLUMN_START + s.ordinal(), Menu.NONE, s.toString());
			item.setCheckable(true);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick (final MenuItem item) {
					setSortColumn(s);
					return true;
				}
			});
		}
		for (final SortDirection s : SortDirection.values()) {
			final MenuItem item = newMenu.getMenu().add(Menu.NONE, MENU_LIBRARY_DIRECTION_START + s.ordinal(), Menu.NONE, s.toString());
			item.setCheckable(true);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick (final MenuItem item) {
					setSortDirection(s);
					return true;
				}
			});
		}
		this.libraryMenu = newMenu;

		if (this.currentLibrary == null) {
			setCurrentLibrary(this.allLibraries.iterator().next());
		}
	}

	private void setCurrentLibrary (final LibraryMetadata library) {
		setCurrentLibrary(library, this.currentSortColumn, this.currentSortDirection);
	}

	protected void setSortColumn (final SortColumn sortColumn) {
		setCurrentLibrary(this.currentLibrary, sortColumn, this.currentSortDirection);
	}

	protected void setSortDirection (final SortDirection sortDirection) {
		setCurrentLibrary(this.currentLibrary, this.currentSortColumn, sortDirection);
	}

	private void setCurrentLibrary (final LibraryMetadata library, final SortColumn sortColumn, final SortDirection sortDirection) {
		this.currentLibrary = library;
		this.currentSortColumn = sortColumn;
		this.currentSortDirection = sortDirection;

		reloadLibrary();

		((PlaybackActivity) getActivity()).getSectionsPagerAdapter().setPageTitle(this.fragmentPosition, library.getName());
		this.btnLibrary.setText(library.getName());

		final PopupMenu menu = this.libraryMenu;
		if (menu != null) {
			for (int i = 0; i < menu.getMenu().size(); i++) {
				final MenuItem item = menu.getMenu().getItem(i);
				item.setChecked(item.getItemId() == MENU_LIBRARY_ID_START + library.getId()
						|| item.getItemId() == MENU_LIBRARY_COLUMN_START + sortColumn.ordinal()
						|| item.getItemId() == MENU_LIBRARY_DIRECTION_START + sortDirection.ordinal());
			}
		}
	}

	private void reloadLibrary () {
		new LoadLibrary(this, this.currentLibrary, this.currentSortColumn, this.currentSortDirection).execute(); // TODO OnExecutor?
	}

	private static class LoadLibrary extends AsyncTask<Void, Void, Result<Cursor>> {

		private final LibraryFragment host;
		private final LibraryMetadata library;
		private final SortColumn sortColumn;
		private final SortDirection sortDirection;

		public LoadLibrary (final LibraryFragment host, final LibraryMetadata library, final SortColumn sortColumn, final SortDirection sortDirection) {
			this.host = host;
			this.library = library;
			this.sortColumn = sortColumn;
			this.sortDirection = sortDirection;
		}

		@Override
		protected void onPreExecute () {
			// TODO show progress indicator.
		}

		@Override
		protected Result<Cursor> doInBackground (final Void... params) {
			try {
				final MediaDb db = this.host.getMediaDb();
				if (db != null) {
					final Cursor cursor = db.getAllMediaCursor(this.library.getId(), this.sortColumn, this.sortDirection);
					return new Result<Cursor>(cursor);
				}
				return new Result<Cursor>(new IllegalStateException("Failed to refresh column as DB was not bound."));
			}
			catch (final Exception e) { // NOSONAR needed to report errors.
				return new Result<Cursor>(e);
			}
		}

		@Override
		protected void onPostExecute (final Result<Cursor> result) {
			if (result.isSuccess()) {
				this.host.saveScrollIfNotSaved();
				this.host.getAdapter().changeCursor(result.getData());
				LOG.d("Refreshed library cursor.");
				this.host.restoreScroll();
			}
			else {
				LOG.w("Failed to refresh column.", result.getE());
			}
			// TODO hide progress indicator.
		}

	}

	// Scrolling.

	private ScrollState getCurrentScroll () {
		return ScrollState.from(this.mediaList);
	}

	private void saveScroll () {
		final ScrollState newState = getCurrentScroll();
		if (newState != null) {
			this.scrollState = newState;
			LOG.d("Saved scroll: %s", this.scrollState);
		}
	}

	private void saveScrollIfNotSaved () {
		if (this.scrollState != null) return;
		saveScroll();
	}

	private void restoreScroll () {
		if (this.scrollState == null) return;
		this.scrollState.applyTo(this.mediaList);
		LOG.d("Restored scroll: %s", this.scrollState);
		this.scrollState = null;
	}

}