package org.pentaho.xul.swt.menu;

import org.eclipse.swt.widgets.Shell;

public class PopupMenu extends Menu {

    private MenuHandler handler;

	public PopupMenu(Shell shell, String id) {
		super(shell, id);
		handler = new MenuHandler();
	}

	public void register( MenuItem item, String id, String accessKey ) {
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


}
