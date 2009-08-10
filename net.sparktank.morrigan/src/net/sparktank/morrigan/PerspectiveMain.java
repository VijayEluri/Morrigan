package net.sparktank.morrigan;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveMain implements IPerspectiveFactory {

	public static final String ID = "net.sparktank.morrigan.perspectiveMain";
	
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		
//		layout.addStandaloneView(ViewMediaExplorer.ID,  true, IPageLayout.LEFT, 0.3f, editorArea);
		layout.addView(ViewMediaExplorer.ID, IPageLayout.LEFT, 0.3f, editorArea);
	}

}
