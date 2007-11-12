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
package org.pentaho.xul.swt.menu;

import org.eclipse.swt.graphics.Image;
import org.pentaho.xul.XulObject;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuBar;
import org.pentaho.xul.menu.XulMenuItem;

public class MenuItem extends XulObject implements XulMenuItem {

    private org.eclipse.swt.widgets.MenuItem menuItem;
    private boolean enabled;
    private boolean checked;
    
	public MenuItem(XulMenu parent, String id ) {
		super( id, parent );
	}

	public MenuItem(XulMenuBar parent, String id ) {
		super( id, parent );
	}

	public XulMenu getMenu() {
		return (XulMenu) getParent();
	}

	public void register() {
		getMenu().addItem( this );
		getMenu().register( this, getId(), null );
	}

	public org.eclipse.swt.widgets.MenuItem getSwtMenuItem() {
		return menuItem;
	}

	public Object getNativeObject() {
		return menuItem;
	}

	public void setSwtMenuItem(org.eclipse.swt.widgets.MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public void dispose() {
		getSwtMenuItem().dispose();
	}

	public void setImage( Image image ) {
		getSwtMenuItem().setImage( image );
	}
	
	public void setText( String text ) {
		getSwtMenuItem().setText(text);
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		getSwtMenuItem().setSelection( checked );
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (!getSwtMenuItem().isDisposed()) getSwtMenuItem().setEnabled( enabled );
	}

}
