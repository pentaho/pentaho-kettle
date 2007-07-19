package org.pentaho.xul.swt.menu;

import org.eclipse.swt.SWT;

public class MenuItemSeparator extends MenuItem {

	public MenuItemSeparator(Menu menu, String id ) {
		super( menu, id );

        // create the menu item
		org.eclipse.swt.widgets.MenuItem menuItem = new org.eclipse.swt.widgets.MenuItem( menu.getSwtMenu(), SWT.SEPARATOR); 
		setSwtMenuItem( menuItem );
		register();
	}

}
