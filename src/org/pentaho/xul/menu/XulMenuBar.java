package org.pentaho.xul.menu;

import org.pentaho.xul.XulItem;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuChoice;

public interface XulMenuBar extends XulItem {

	public void handleAccessKey( String key, boolean alt, boolean ctrl );
	
	public void handleAccessKey( String accessKey );

	public boolean handleMenuEvent( String id );

	public void dispose();
	
	public boolean isDisposed();

	public void register( XulMenuItem item, String id, String accessKey );
	
	public String[] getMenuItemIds();
	
	public XulMenuChoice getMenuItemById( String id );
	
	public XulMenu getMenuById( String id );
	
	public XulMenuItem getSeparatorById( String id );
	
	public XulMenuChoice getMenuItemByKey( String accessKey );

	public void addMenuListener( String id, Object listener, Class listenerClass, String methodName );

	public void addMenuListener( String id, Object listener, String methodName );

	public void setEnableById( String id, boolean enabled );
	
	public void setTextById( String id, String text );

	public Object getNativeObject();
	
}
