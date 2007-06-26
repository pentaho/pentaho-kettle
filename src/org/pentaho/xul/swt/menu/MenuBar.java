package org.pentaho.xul.swt.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class MenuBar extends MenuObject {

    private Shell shell;
    private MenuHandler handler;
	
    private org.eclipse.swt.widgets.Menu menuBar;

	public MenuBar( Shell shell ) {
		super( null, null );
		this.shell = shell;
		handler = new MenuHandler();
		menuBar = new org.eclipse.swt.widgets.Menu(shell, SWT.BAR);
        shell.setMenuBar(menuBar);
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

	public void register( Object item, String id, String accessKey ) {
		handler.register( item, id, accessKey );
	}
	
	public String[] getMenuItemIds() {
		return handler.getMenuItemIds();
	}
	
	public MenuChoice getMenuItemById( String id ) {
		return handler.getMenuItemById( id );
	}
	
	public Menu getMenuById( String id ) {
		return handler.getMenuById( id );
	}
	
	public MenuItemSeparator getSeparatorById( String id ) {
		return handler.getSeparatorById( id );
	}
	
	public MenuChoice getMenuItemByKey( String accessKey ) {
		return handler.getMenuItemByKey( accessKey );
	}

	public void addMenuListener( String id, Object listener, Class listenerClass, String methodName ) {
		handler.addMenuListener( id, listener, listenerClass, methodName );
	}

	public void addMenuListener( String id, Object listener, String methodName ) {
		handler.addMenuListener( id, listener, methodName );
	}

	public void setEnableById( String id, boolean enabled ) {
		MenuChoice item = getMenuItemById( id );
		if( item != null ) {
			item.setEnabled( enabled );
		}
	}
	
	public void setTextById( String id, String text ) {
		MenuChoice item = getMenuItemById( id );
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
