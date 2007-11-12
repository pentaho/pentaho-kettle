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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuBar;
import org.pentaho.xul.menu.XulMenuItem;

public class Menu extends MenuItem implements XulMenu {

    private org.eclipse.swt.widgets.Menu menu;
    private XulMenuBar menuBar;
    private Menu parentMenu;
    protected String accessKey;
    private List<XulMenuItem> items = new ArrayList<XulMenuItem>();

	public Menu( XulMenuBar parent, String name, String id, String accessKey ) {
		super( parent, id );
		this.menuBar = parent;
		this.parentMenu = null;
		this.accessKey = accessKey;

		org.eclipse.swt.widgets.MenuItem menuItem = new org.eclipse.swt.widgets.MenuItem((org.eclipse.swt.widgets.Menu)parent.getNativeObject(), SWT.CASCADE); 
		setSwtMenuItem( menuItem );
        menuItem.setText( name );

        menu = new org.eclipse.swt.widgets.Menu(((MenuBar)parent).getSwtShell(), SWT.DROP_DOWN);
        menuItem.setMenu( menu);

        register();
	}

	public Menu( Menu parent, String name, String id, String accessKey ) {
		super( parent, id );
		this.menuBar = parent.getMenuBar();
		this.parentMenu = parent;
		this.accessKey = accessKey;

		org.eclipse.swt.widgets.MenuItem menuItem = new org.eclipse.swt.widgets.MenuItem(parent.getSwtMenu(), SWT.CASCADE); 
		setSwtMenuItem( menuItem );
        menuItem.setText( name );

        menu = new org.eclipse.swt.widgets.Menu(parent.getSwtMenu().getShell(), SWT.DROP_DOWN);
        menuItem.setMenu( menu);

        parent.addItem( this );
        register();
	}

	public Menu( Shell shell, String id ) {
		super( (Menu) null, id );

        menu = new org.eclipse.swt.widgets.Menu(shell, SWT.POP_UP);

	}

	public int indexOf( String id ) {
		
		MenuChoice item = (MenuChoice) menuBar.getMenuItemById( id );
		if( item == null ) {
			return -1;
		}
		org.eclipse.swt.widgets.MenuItem swtItem = item.getSwtMenuItem();
		return (menu).indexOf( swtItem );
	}
	
	public int indexOf( XulMenuItem item ) {
		
		org.eclipse.swt.widgets.MenuItem swtItem = (org.eclipse.swt.widgets.MenuItem)item.getNativeObject();
		return (menu).indexOf( swtItem );
	}
	
	public void register( XulMenuItem item, String id, String accessKey ) {
		if( getMenuBar() != null ) {
	        getMenuBar().register( item, id, accessKey );
		}
		if( parentMenu != null ) {
			parentMenu.register( item, id, accessKey );
		}
	}

	public void register( ) {
		if( getMenuBar() != null ) {
	        getMenuBar().register( this, getId(), accessKey );
		}
		if( parentMenu != null ) {
			parentMenu.register( this, getId(), accessKey );
		}
	}

	public XulMenuBar getMenuBar() {
		return menuBar;
	}

	public boolean handleMenuEvent( String id ) {
		if( parentMenu != null ) {
			if( parentMenu.handleMenuEvent( id ) ) {
				return true;
			}
		}
		if( getMenuBar() != null ) {
			if( getMenuBar().handleMenuEvent( id ) ) {
				return true;
			}
		}
		return false;
	}

	public void addSeparator() {
		
	}
	
	public int getItemCount() {
		return menu.getItemCount();
	}
	
	public void addItem( XulMenuItem item ) {
		items.add( item );
	}
	
	public XulMenuItem getItem( int idx ) {
		return items.get( idx );
	}
	
	public void remove( XulMenuItem item ) {
		items.remove( item );
		item.dispose();
	}
	
	public Object getNativeObject() {
		return menu;
	}
	
	public org.eclipse.swt.widgets.Menu getSwtMenu() {
		return menu;
	}
	
	public void dispose() {
		getSwtMenuItem().dispose();
		menu.dispose();
	}


}
