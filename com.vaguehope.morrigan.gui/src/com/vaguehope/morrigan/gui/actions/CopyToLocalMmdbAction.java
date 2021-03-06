package com.vaguehope.morrigan.gui.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;

import com.vaguehope.morrigan.gui.Activator;
import com.vaguehope.morrigan.gui.editors.MediaItemListEditor;
import com.vaguehope.morrigan.gui.editors.mmdb.LocalMixedMediaDbEditor;
import com.vaguehope.morrigan.gui.jobs.TaskJob;
import com.vaguehope.morrigan.model.media.ILocalMixedMediaDb;
import com.vaguehope.morrigan.model.media.IMediaItem;
import com.vaguehope.morrigan.model.media.IMediaItemList;
import com.vaguehope.morrigan.tasks.MorriganTask;

/**
 *
 * @param <T> the type of the source list.
 */
public class CopyToLocalMmdbAction<T extends IMediaItem> extends Action {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private final MediaItemListEditor<?,T> fromEd;
	private final IEditorReference toEd;

	public CopyToLocalMmdbAction (MediaItemListEditor<?,T> fromEd, IEditorReference toEd) {
		super(toEd.getName(), Activator.getImageDescriptor("icons/db.gif"));
		this.fromEd = fromEd;
		this.toEd = toEd;
	}

	@Override
	public void run() {
		super.run();

		IWorkbenchPart toPart = this.toEd.getPart(true);

		if (this.fromEd != null && toPart != null && toPart instanceof LocalMixedMediaDbEditor) {
			IMediaItemList<T> fromList = this.fromEd.getMediaList();
			LocalMixedMediaDbEditor toMmdbEd = (LocalMixedMediaDbEditor) toPart;
			ILocalMixedMediaDb toMmdb = toMmdbEd.getMediaList();

			MorriganTask task = Activator.getMediaFactory().getNewCopyToLocalMmdbTask(fromList, this.fromEd.getSelectedItems(), toMmdb);
			TaskJob job = new TaskJob(task);
			job.schedule();
		}
		else {
			throw new IllegalArgumentException("part is null or is not LocalMixedMediaDbEditor.");
		}

	}

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}