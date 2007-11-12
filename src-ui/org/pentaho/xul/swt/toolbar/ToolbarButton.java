/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
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
	private int mode;
	
	public ToolbarButton( Shell shell, String id, XulToolbar parent ) {
		super( id, parent );
		
		toolItem = new ToolItem((ToolBar) parent.getNativeObject(), SWT.PUSH);
    	toolItem.addSelectionListener( this );
    	mode = parent.getMode();

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
		if( image != null && mode != Toolbar.MODE_TEXT ) {
			setImage( (Image) image );
		}
	}
	
	public void setSelectedImage( Object image ) {
		if( image != null && mode != Toolbar.MODE_TEXT ) {
			setSelectedImage( (Image) image );
		}
	}
	
	public void setDisabledImage( Object image ) {
		if( image != null && mode != Toolbar.MODE_TEXT ) {
			setDisabledImage( (Image) image );
		}
	}
	
	public void setImage( Image image ) {
		if( image != null && mode != Toolbar.MODE_TEXT ) {
			toolItem.setImage( (Image) image);
		}
	}
	
	public void setSelectedImage( Image image ) {
		if( image != null && mode != Toolbar.MODE_TEXT ) {
			toolItem.setHotImage(image);
		}
	}
	
	public void setDisabledImage( Image image ) {
		if( image != null && mode != Toolbar.MODE_TEXT ) {
			toolItem.setDisabledImage(image);
		}
	}
	
	public void setEnable(boolean enabled) {
		toolItem.setEnabled( enabled );
	}

	public void setHint(String text) {
		if( text != null ) {
			toolItem.setToolTipText( text );
		}
	}
	
	public void setText( String text ) {
		if( text != null && mode != Toolbar.MODE_ICONS ) {
			toolItem.setText( text );
		}
	}
	
	public Object getNativeObject() {
		return toolItem;
	}

}
