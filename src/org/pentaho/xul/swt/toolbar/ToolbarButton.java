package org.pentaho.xul.swt.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.xul.XulObject;
import org.pentaho.xul.toolbar.XulToolbar;
import org.pentaho.xul.toolbar.XulToolbarButton;

public class ToolbarButton extends XulObject implements XulToolbarButton, SelectionListener {

	private ToolItem toolItem;
	
	public ToolbarButton( Shell shell, String id, XulToolbar parent ) {
		super( id, parent );
		
		toolItem = new ToolItem((ToolBar) parent.getNativeObject(), SWT.PUSH);
        toolItem.addSelectionListener( this );

	}
	
	public void dispose() {
		toolItem.dispose();
	}

	public boolean isDisposed() {
		return toolItem.isDisposed();
	}
	
	public XulToolbar getToolbar() {
		return (XulToolbar) getParent();
	}
	
	public void widgetSelected(SelectionEvent e) { 
		getToolbar().handleMenuEvent( getId() );
	}
	
	public void widgetDefaultSelected(SelectionEvent e) { 
		getToolbar().handleMenuEvent( getId() );
	}
	
	public void setImage( Object image ) {
		setImage( (Image) image );
	}
	
	public void setSelectedImage( Object image ) {
		setSelectedImage( (Image) image );
	}
	
	public void setDisabledImage( Object image ) {
		setDisabledImage( (Image) image );
	}
	
	public void setImage( Image image ) {
		toolItem.setImage( (Image) image);
	}
	
	public void setSelectedImage( Image image ) {
		toolItem.setHotImage(image);
	}
	
	public void setDisabledImage( Image image ) {
		toolItem.setDisabledImage(image);
	}
	
	public void setEnable(boolean enabled) {
		toolItem.setEnabled( enabled );
	}

	public void setHint(String text) {
		toolItem.setToolTipText( text );
	}

}
