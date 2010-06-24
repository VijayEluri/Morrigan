package net.sparktank.morrigan.gui.views;

import java.util.ArrayList;
import java.util.List;

import net.sparktank.morrigan.gui.dialogs.MorriganMsgDlg;
import net.sparktank.morrigan.gui.dialogs.RunnableDialog;
import net.sparktank.morrigan.gui.editors.AbstractLibraryEditor;
import net.sparktank.morrigan.gui.helpers.ImageCache;
import net.sparktank.morrigan.model.library.AbstractMediaLibrary;
import net.sparktank.morrigan.model.library.DbException;
import net.sparktank.morrigan.model.library.MediaLibraryTrack;
import net.sparktank.morrigan.model.tags.MediaTag;
import net.sparktank.morrigan.model.tags.MediaTagType;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class ViewTagEditor extends ViewPart {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static final String ID = "net.sparktank.morrigan.gui.views.ViewTagEditor";
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private ImageCache imageCache = new ImageCache();
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public void createPartControl(Composite parent) {
		createLayout(parent);
		initSelectionListener();
	}
	
	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}
	
	@Override
	public void dispose() {
		removeSelectionListener();
		imageCache.clearCache();
		super.dispose();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	External events.
	
	private void initSelectionListener () {
		ISelectionService service = getSite().getWorkbenchWindow().getSelectionService();
		service.addSelectionListener(selectionListener);
	}
	
	private void removeSelectionListener () {
		ISelectionService service = getSite().getWorkbenchWindow().getSelectionService();
		service.removeSelectionListener(selectionListener);
	}
	
	private ISelectionListener selectionListener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection==null) return;
			if (selection.isEmpty()) return;
			
//			System.err.println("selectionChanged("+part.getTitle()+","+selection.toString()+").");
			
			if (part instanceof AbstractLibraryEditor<?>) {
				@SuppressWarnings("unchecked") // FIXME avoid need for this?
				AbstractLibraryEditor<? extends AbstractMediaLibrary> libEditor = (AbstractLibraryEditor<? extends AbstractMediaLibrary>) part;
				AbstractMediaLibrary mediaList = libEditor.getMediaList();
				
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection iSel = (IStructuredSelection) selection;
					ArrayList<MediaLibraryTrack> sel = new ArrayList<MediaLibraryTrack>();
					for (Object selectedObject : iSel.toList()) {
						if (selectedObject != null) {
							if (selectedObject instanceof MediaLibraryTrack) {
								MediaLibraryTrack track = (MediaLibraryTrack) selectedObject;
								sel.add(track);
							}
						}
					}
					
					inputSelectionChanged(mediaList, sel);
				}
			}
		}
	};
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	Data links.
	
	private AbstractMediaLibrary editedMediaList = null;
	private MediaLibraryTrack editedMediaItem = null;
	
	private void inputSelectionChanged (AbstractMediaLibrary editedMediaList, List<MediaLibraryTrack> selection) {
		this.editedMediaList = editedMediaList;
		
		if (selection.size() == 1) {
			setContentDescription(selection.get(0).getTitle());
			editedMediaItem = selection.get(0);
		}
		else if (selection.size() > 1) {
			setContentDescription(selection.size() + " items selected.");
			editedMediaItem = null;
		}
		else {
			setContentDescription("No items selected.");
			editedMediaItem = null;
		}
		
		tableViewer.refresh();
	}
	
	private IStructuredContentProvider sourcesProvider = new IStructuredContentProvider() {
		
		@Override
		public Object[] getElements(Object inputElement) {
			if (editedMediaList != null && editedMediaItem != null) {
				try {
					List<MediaTag> tags = editedMediaList.getTags(editedMediaItem);
					return tags.toArray();
				}
				catch (DbException e) {
					getSite().getShell().getDisplay().asyncExec(new RunnableDialog(e));
					// TODO disable stuff 'cos we are broken?
					return new String[]{};
				}
			}
			else {
				return new String[]{};
			}
		}
		
		@Override
		public void dispose() {}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		
	};
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//	GUI stuff.
	
	protected final int sep = 3;
	
	private Text txtFilter;
	private Button btnAddTag;
	private Button btnRemoveTag;
	private TableViewer tableViewer;
	
	private void createLayout (Composite parent) {
		FormData formData;
		
		parent.setLayout(new FormLayout());
		
		Composite tbCom = new Composite(parent, SWT.NONE);
		txtFilter = new Text(tbCom, SWT.SINGLE | SWT.BORDER);
		btnAddTag = new Button(tbCom, SWT.PUSH);
		btnRemoveTag = new Button(tbCom, SWT.PUSH);
		
		tbCom.setLayout(new FormLayout());
		formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		tbCom.setLayoutData(formData);
		
		txtFilter.setMessage("New tag");
		formData = new FormData();
		formData.left = new FormAttachment(0, sep);
		formData.right = new FormAttachment(btnAddTag, -sep);
		formData.top = new FormAttachment(0, sep);
		formData.bottom = new FormAttachment(100, -sep);
		txtFilter.setLayoutData(formData);
		
		btnAddTag.setImage(imageCache.readImage("icons/plus.gif"));
		formData = new FormData();
		formData.right = new FormAttachment(btnRemoveTag, -sep);
		formData.top = new FormAttachment(0, sep);
		formData.bottom = new FormAttachment(100, -sep);
		btnAddTag.setLayoutData(formData);
		
		btnRemoveTag.setImage(imageCache.readImage("icons/minus.gif"));
		formData = new FormData();
		formData.right = new FormAttachment(100, -sep);
		formData.top = new FormAttachment(0, sep);
		formData.bottom = new FormAttachment(100, -sep);
		btnRemoveTag.setLayoutData(formData);
		
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL);
		tableViewer.setContentProvider(sourcesProvider);
		tableViewer.setInput(getViewSite()); // use content provider.
		getSite().setSelectionProvider(tableViewer);
		formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.top = new FormAttachment(tbCom, 0);
		formData.bottom = new FormAttachment(100, -sep);
		tableViewer.getTable().setLayoutData(formData);
		
		btnAddTag.addSelectionListener(btnAddTagListener);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private SelectionListener btnAddTagListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (editedMediaList != null && editedMediaItem != null) {
				String text = txtFilter.getText();
				if (text.length() > 0) {
					try {
						editedMediaList.addTag(editedMediaItem, text, MediaTagType.MANUAL, null);
						tableViewer.refresh();
					}
					catch (DbException e) {
						getSite().getShell().getDisplay().asyncExec(new RunnableDialog(e));
					}
				}
				
			}
			else {
				new MorriganMsgDlg("No item selected to add tag to.").open();
			}
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}
	};
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
