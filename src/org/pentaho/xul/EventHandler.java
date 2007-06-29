package org.pentaho.xul;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuChoice;
import org.pentaho.xul.menu.XulMenuItem;
import org.pentaho.xul.toolbar.XulToolbarButton;

public class EventHandler {

    private Map<String,Object> idMap = new HashMap<String,Object>();
    private Map<String,Object> keyMap = new HashMap<String,Object>();
    private Map<String,Method> functionMap = new HashMap<String,Method>();
    private Map<String,Object> objectMap = new HashMap<String,Object>();

	public EventHandler() {
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
			XulMenuChoice menuItem = (XulMenuChoice) keyMap.get( accessKey );
			if( menuItem != null ) {
				handleMenuEvent( menuItem.getId() );
			}
		} catch ( Throwable t ) {
			// TODO use error logging
			t.printStackTrace();
		}
	}

	public void register( XulMenuItem item, String id, String accessKey ) {
		if( id != null ) {
			idMap.put( id, item );
		}
		if( accessKey != null ) {
			keyMap.put( accessKey, item );
		}
	}
	
	public void register( XulToolbarButton item, String id, String accessKey ) {
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
	
	public XulMenuChoice getMenuItemById( String id ) {
		return (XulMenuChoice) idMap.get( id );
	}
	
	public XulMenu getMenuById( String id ) {
		return (XulMenu) idMap.get( id );
	}
	
	public XulMenuItem getSeparatorById( String id ) {
		return (XulMenuItem) idMap.get( id );
	}
	
	public XulMenuChoice getMenuItemByKey( String accessKey ) {
		return (XulMenuChoice) keyMap.get( accessKey );
	}
	
	public void addMenuListener( String id, Object listener, String methodName ) {
		Class listenerClass = listener.getClass();
		addMenuListener(id, listener, listenerClass, methodName);
	}	
	
	public void addMenuListener( String id, Object listener, Class<?> listenerClass, String methodName ) {
		Method method = null;
		while(listenerClass != null ) {
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
			
			listenerClass = listenerClass.getSuperclass();
		}
		System.err.println( "No valid listener for menu item: "+id );
	}
}
