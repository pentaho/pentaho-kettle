package org.pentaho.xul.swt.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.xul.XulObject;
import org.pentaho.xul.toolbar.XulToolbar;

public class ToolbarSeparator extends XulObject {

	private ToolItem toolItem;
    
	public ToolbarSeparator( Shell shell, XulToolbar parent ) {
		super( "", parent );

		toolItem = new ToolItem((ToolBar) parent.getNativeObject(), SWT.SEPARATOR);

	}

	public void dispose() {
		toolItem.dispose();
	}

	public boolean isDisposed() {
		return toolItem.isDisposed();
	}

}
