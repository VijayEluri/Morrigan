package com.vaguehope.morrigan.gui.editors.mmdb;


import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.IHandlerService;

import com.vaguehope.morrigan.gui.adaptors.CountsLblProv;
import com.vaguehope.morrigan.gui.adaptors.DateAddedLblProv;
import com.vaguehope.morrigan.gui.adaptors.DateLastModifiedLblProv;
import com.vaguehope.morrigan.gui.adaptors.DateLastPlayerLblProv;
import com.vaguehope.morrigan.gui.adaptors.DimensionsLblProv;
import com.vaguehope.morrigan.gui.adaptors.DurationLblProv;
import com.vaguehope.morrigan.gui.adaptors.FileLblProv;
import com.vaguehope.morrigan.gui.adaptors.HashcodeLblProv;
import com.vaguehope.morrigan.gui.dialogs.MorriganMsgDlg;
import com.vaguehope.morrigan.gui.editors.MediaColumn;
import com.vaguehope.morrigan.gui.editors.MediaItemListEditor;
import com.vaguehope.morrigan.gui.handler.AddToQueue;
import com.vaguehope.morrigan.model.media.IMixedMediaItem;
import com.vaguehope.morrigan.model.media.IMixedMediaList;

public abstract class MixedMediaListEditor<T extends IMixedMediaList<S>, S extends IMixedMediaItem> extends MediaItemListEditor<T,S> {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Column definitions.

	public final MediaColumn
		COL_FILE =       new MediaColumn("file",        new ColumnWeightData(100),            new FileLblProv(this.getImageCache()) );
	public final MediaColumn
		COL_ADDED =      new MediaColumn("added",       new ColumnPixelData(140, true, true), new DateAddedLblProv()           );
	public final MediaColumn
		COL_HASH =       new MediaColumn("hash",        new ColumnPixelData( 90, true, true), new HashcodeLblProv(),           SWT.CENTER);
	public final MediaColumn
		COL_MODIFIED =   new MediaColumn("modified",    new ColumnPixelData(140, true, true), new DateLastModifiedLblProv()    );

    public final MediaColumn
    	COL_COUNTS =     new MediaColumn("counts",      new ColumnPixelData( 70, true, true), new CountsLblProv(),             SWT.CENTER);
    public final MediaColumn
    	COL_LASTPLAYED = new MediaColumn("last played", new ColumnPixelData(140, true, true), new DateLastPlayerLblProv()      );
    public final MediaColumn
    	COL_DURATION =   new MediaColumn("duration",    new ColumnPixelData( 60, true, true), new DurationLblProv(),           SWT.RIGHT);
    public final MediaColumn
    	COL_DIMENSIONS = new MediaColumn("dimensions",  new ColumnPixelData(100, true, true), new DimensionsLblProv(),         SWT.CENTER);

    public final MediaColumn[] COLS_UNKNOWN = new MediaColumn[] {
    		this.COL_FILE,
    		this.COL_ADDED,
    		this.COL_HASH,
    		this.COL_MODIFIED,
    };

    public final MediaColumn[] COLS_TRACKS = new MediaColumn[] {
    		this.COL_FILE,
    		this.COL_COUNTS,
    		this.COL_ADDED,
    		this.COL_MODIFIED,
    		this.COL_LASTPLAYED,
    		this.COL_HASH,
    		this.COL_DURATION
    };

    public final MediaColumn[] COLS_PICTURES = new MediaColumn[] {
    		this.COL_FILE,
    		this.COL_ADDED,
    		this.COL_HASH,
    		this.COL_MODIFIED,
    		this.COL_DIMENSIONS
    };

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Menus and Actions.

	protected IAction addToQueueAction = new Action("Enqueue") {
		@Override
		public void run() {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand(AddToQueue.ID, null);
			}
			catch (ExecutionException e) {
				new MorriganMsgDlg(e).open();
			}
			catch (NotDefinedException e) {
				new MorriganMsgDlg(e).open();
			}
			catch (NotEnabledException e) {
				new MorriganMsgDlg(e).open();
			}
			catch (NotHandledException e) {
				new MorriganMsgDlg(e).open();
			}
		}
	};

//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
