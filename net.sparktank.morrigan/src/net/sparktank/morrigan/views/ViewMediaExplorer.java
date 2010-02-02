package net.sparktank.morrigan.views;

import java.util.ArrayList;

import net.sparktank.morrigan.actions.NewPlaylistAction;
import net.sparktank.morrigan.dialogs.MorriganMsgDlg;
import net.sparktank.morrigan.handler.CallMediaListEditor;
import net.sparktank.morrigan.helpers.PlaylistHelper;
import net.sparktank.morrigan.model.ui.MediaExplorerItem;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

public class ViewMediaExplorer extends ViewPart {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static final String ID = "net.sparktank.morrigan.views.ViewMediaExplorer";
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private TableViewer viewer;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * This is a callback that will allow us to create the viewer and initialise it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(contentProvider);
		viewer.setInput(getViewSite()); // use content provider.
		getSite().setSelectionProvider(viewer);
		viewer.addDoubleClickListener(doubleClickListener);
		
		addToolbar();
		addMenu();
	}
	
	IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
		@Override
		public Object[] getElements(Object inputElement) {
			ArrayList<MediaExplorerItem> items = new ArrayList<MediaExplorerItem>();
			
			items.add(new MediaExplorerItem("dis", "Display", MediaExplorerItem.ItemType.DISPLAY));
			items.add(new MediaExplorerItem("lib", "Library", MediaExplorerItem.ItemType.LIBRARY));
			items.addAll(PlaylistHelper.instance.getAllPlaylists());
			
			return items.toArray();
		}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		@Override
		public void dispose() {}
	};
	
	IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
		public void doubleClick(DoubleClickEvent event) {
			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
			try {
				handlerService.executeCommand(CallMediaListEditor.ID, null);
			} catch (CommandException e) {
				new MorriganMsgDlg(e).open();
			}
		}
	};
	
	private void addToolbar () {
		getViewSite().getActionBars().getToolBarManager().add(new NewPlaylistAction(getViewSite().getWorkbenchWindow()));
	}
	
	private void addMenu () {
		getViewSite().getActionBars().getMenuManager().add(new NewPlaylistAction(getViewSite().getWorkbenchWindow()));
		getViewSite().getActionBars().getMenuManager().add(new Separator());
		getViewSite().getActionBars().getMenuManager().add(new NewPlaylistAction(getViewSite().getWorkbenchWindow()));
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public void refresh () {
		viewer.refresh();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}