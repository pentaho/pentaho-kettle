package org.pentaho.xul.swt.menu;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MenuHandler {

    private Map<String,Object> idMap = new HashMap<String,Object>();
    private Map<String,Object> keyMap = new HashMap<String,Object>();
    private Map<String,Method> functionMap = new HashMap<String,Method>();
    private Map<String,Object> objectMap = new HashMap<String,Object>();

	public MenuHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean handleMenuEvent( String id ) {
		try {
			Method method = functionMap.get( id );
			Object object = objectMap.get( id );
			if( method != null && object != null ) {
				if( method.getParameterTypes() == null || method.getParameterTypes().length == 0 ) {
					method.invoke( object);
				} else {
					method.invoke( object, new Object[] { id }  );
				}
				return true;
			}
		} catch ( Throwable t ) {
			// TODO use error logging
			t.printStackTrace();
		}
		return false;
	}

	public void handleAccessKey( String key, boolean alt, boolean ctrl ) {
		String accessKey = ( (ctrl) ? "ctrl-" : "" ) + ( (alt) ? "alt-" : "" ) + key; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		handleAccessKey( accessKey );
	}

	public void handleAccessKey( String accessKey ) {
		try {
			MenuChoice menuItem = (MenuChoice) keyMap.get( accessKey );
			if( menuItem != null ) {
				handleMenuEvent( menuItem.getId() );
			}
		} catch ( Throwable t ) {
			// TODO use error logging
			t.printStackTrace();
		}
	}

	public void register( Object item, String id, String accessKey ) {
		if( id != null ) {
			idMap.put( id, item );
		}
		if( accessKey != null ) {
			keyMap.put( accessKey, item );
		}
	}
	
	public String[] getMenuItemIds() {
		String ids[] = new String[ idMap.keySet().size() ];
		return idMap.keySet().toArray( ids );
	}
	
	public MenuChoice getMenuItemById( String id ) {
		return (MenuChoice) idMap.get( id );
	}
	
	public Menu getMenuById( String id ) {
		return (Menu) idMap.get( id );
	}
	
	public MenuItemSeparator getSeparatorById( String id ) {
		return (MenuItemSeparator) idMap.get( id );
	}
	
	public MenuChoice getMenuItemByKey( String accessKey ) {
		return (MenuChoice) keyMap.get( accessKey );
	}
	
	public void addMenuListener( String id, Object listener, String methodName ) {
		Class listenerClass = listener.getClass();
		addMenuListener(id, listener, listenerClass, methodName);
	}	
	
	public void addMenuListener( String id, Object listener, Class<?> listenerClass, String methodName ) {
		Method method = null;
		try {
			method = listenerClass.getDeclaredMethod( methodName, new Class[0] );
			functionMap.put( id, method );
			objectMap.put( id, listener );
			return;
		} catch (NoSuchMethodException e) {
		}
		try {
			method = listenerClass.getDeclaredMethod( methodName, new Class[] {String.class} );
			functionMap.put( id, method );
			objectMap.put( id, listener );
			return;
		} catch (NoSuchMethodException e) {
		}
		System.err.println( "No valid listener for menu item: "+id );
	}
}
