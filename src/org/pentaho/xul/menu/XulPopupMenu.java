package org.pentaho.xul.menu;

public interface XulPopupMenu extends XulMenu {

	public void handleAccessKey( String key, boolean alt, boolean ctrl );
	
	public void handleAccessKey( String accessKey );

	public String[] getMenuItemIds();
	
	public XulMenuChoice getMenuItemById( String id );
	
	public XulMenu getMenuById( String id );
	
	public XulMenuItem getSeparatorById( String id );
	
	public XulMenuChoice getMenuItemByKey( String accessKey );

	public void addMenuListener( String id, Object listener, Class<?> listenerClass, String methodName );

	public void addMenuListener( String id, Object listener, String methodName );

}
