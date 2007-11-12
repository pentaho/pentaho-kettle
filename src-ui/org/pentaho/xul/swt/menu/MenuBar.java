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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.xul.EventHandler;
import org.pentaho.xul.XulObject;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuBar;
import org.pentaho.xul.menu.XulMenuChoice;
import org.pentaho.xul.menu.XulMenuItem;

public class MenuBar extends XulObject implements XulMenuBar {

    private Shell shell;
    private EventHandler handler;
	
    private org.eclipse.swt.widgets.Menu menuBar;

	public MenuBar( Shell shell ) {
		super( null, null );
		this.shell = shell;
		handler = new EventHandler();
		menuBar = new org.eclipse.swt.widgets.Menu(shell, SWT.BAR);
        shell.setMenuBar(menuBar);
	}

	public Object getNativeObject() {
		return menuBar;
	}

	public org.eclipse.swt.widgets.Menu getSwtMenu() {
		return menuBar;
	}
	
	public Shell getSwtShell() {
		return shell;
	}
	
	public boolean handleMenuEvent( String id ) {
		return handler.handleMenuEvent( id );
	}

	public void handleAccessKey( String key, boolean alt, boolean ctrl ) {
		handler.handleAccessKey( key, alt, ctrl );
	}

	public void handleAccessKey( String accessKey ) {
		handler.handleAccessKey( accessKey );
	}

	public void register( XulMenuItem item, String id, String accessKey ) {
		handler.register( item, id, accessKey );
	}
	
	public String[] getMenuItemIds() {
		return handler.getMenuItemIds();
	}
	
	public XulMenuChoice getMenuItemById( String id ) {
		return handler.getMenuItemById( id );
	}
	
	public XulMenu getMenuById( String id ) {
		return handler.getMenuById( id );
	}
	
	public XulMenuItem getSeparatorById( String id ) {
		return handler.getSeparatorById( id );
	}
	
	public XulMenuChoice getMenuItemByKey( String accessKey ) {
		return handler.getMenuItemByKey( accessKey );
	}

	public void addMenuListener( String id, Object listener, Class<?> listenerClass, String methodName ) {
		handler.addMenuListener( id, listener, listenerClass, methodName );
	}

	public void addMenuListener( String id, Object listener, String methodName ) {
		handler.addMenuListener( id, listener, methodName );
	}

	public void setEnableById( String id, boolean enabled ) {
		XulMenuChoice item = getMenuItemById( id );
		if( item != null ) {
			item.setEnabled( enabled );
		}
	}
	
	public void setTextById( String id, String text ) {
		XulMenuChoice item = getMenuItemById( id );
		if( item != null ) {
			item.setText( text );
		}
	}
	
	public void dispose() {
		getSwtMenu().dispose();
	}
	
	public boolean isDisposed() {
		return getSwtMenu().isDisposed();
	}
	
}
