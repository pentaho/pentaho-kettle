package org.pentaho.xul.swt.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import org.pentaho.xul.Messages;
import org.pentaho.xul.menu.XulMenu;
import org.pentaho.xul.menu.XulMenuBar;
import org.pentaho.xul.menu.XulMenuChoice;

public class MenuChoice extends MenuItem implements Listener, XulMenuChoice {

	public static int TYPE_PLAIN = 1;
	public static int TYPE_CHECKBOX = 2;
	public static int TYPE_RADIO = 3;
    private XulMenu menu;
    private String id;
    private String accessKey;

	public MenuChoice( XulMenuBar menu, String text, String id, String accessText, String accessKey, Messages messages) {
		super( menu, id );
	}

    public MenuChoice( XulMenu menu, String text, String id, String accessText, String accessKey, int type, Messages messages) {
		super( menu, id );

		this.menu = menu;
		this.id = id;
		this.accessKey = accessKey;
		// create the text for the menu item
        if( text.charAt( 0 ) == '&' && text.charAt( text.length()-1 ) == ';') {
        		text = messages.getString( text.substring( 1, text.length()-2 ) );
        }
        if( accessText != null ) {
        		text += " \t" + accessText; //$NON-NLS-1$
        }

        int flags = SWT.CASCADE;
        if( type == TYPE_CHECKBOX ) {
        		flags |= SWT.CHECK;
        }
        // create the menu item
        org.eclipse.swt.widgets.MenuItem menuItem = new org.eclipse.swt.widgets.MenuItem( (Menu) menu.getNativeObject(), flags); 
		setSwtMenuItem( menuItem );

		setText(text);
		setEnabled( true );

        // create the callback
        menuItem.addListener (SWT.Selection, this );

        register();
	}

	public void register() {
		menu.addItem( this );
        menu.register( this, id, accessKey );
	}
	
	public void handleEvent(Event e) {
		handleMenuEvent(  );
	}
	
	public void handleMenuEvent( ) {
		menu.handleMenuEvent( id );
	}
	
}
