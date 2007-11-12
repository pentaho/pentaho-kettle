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

import org.eclipse.swt.widgets.Shell;
import org.pentaho.xul.EventHandler;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuChoice;
import org.pentaho.xul.menu.XulMenuItem;
import org.pentaho.xul.menu.XulPopupMenu;

public class PopupMenu extends Menu implements XulPopupMenu {

    private EventHandler handler;

	public PopupMenu(Shell shell, String id) {
		super(shell, id);
		handler = new EventHandler();
	}

	public void register( XulMenuItem item, String id, String accessKey ) {
	     handler.register( item, id, accessKey );
	}

	public void register( ) {
		handler.register( this, getId(), accessKey );
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


}
