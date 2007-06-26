package org.pentaho.xul.swt.menu;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class Menu extends MenuItem {

    private org.eclipse.swt.widgets.Menu menu;
    private MenuBar menuBar;
    private Menu parentMenu;
    protected String accessKey;
    private List<MenuItem> items = new ArrayList<MenuItem>();

	public Menu( MenuBar parent, String name, String id, String accessKey ) {
		super( parent, id );
		this.menuBar = parent;
		this.parentMenu = null;
		this.accessKey = accessKey;

		org.eclipse.swt.widgets.MenuItem menuItem = new org.eclipse.swt.widgets.MenuItem(parent.getSwtMenu(), SWT.CASCADE); 
		setSwtMenuItem( menuItem );
        menuItem.setText( name );

        menu = new org.eclipse.swt.widgets.Menu(parent.getSwtShell(), SWT.DROP_DOWN);
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
		
		MenuChoice item = menuBar.getMenuItemById( id );
		if( item == null ) {
			return -1;
		}
		org.eclipse.swt.widgets.MenuItem swtItem = item.getSwtMenuItem();
		return (menu).indexOf( swtItem );
	}
	
	public int indexOf( MenuItem item ) {
		
		org.eclipse.swt.widgets.MenuItem swtItem = item.getSwtMenuItem();
		return (menu).indexOf( swtItem );
	}
	
	public void register( MenuItem item, String id, String accessKey ) {
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

	public MenuBar getMenuBar() {
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
	
	public void addItem( MenuItem item ) {
		items.add( item );
	}
	
	public MenuItem getItem( int idx ) {
		return items.get( idx );
	}
	
	public void remove( MenuItem item ) {
		items.remove( item );
		item.dispose();
	}
	
	public org.eclipse.swt.widgets.Menu getSwtMenu() {
		return menu;
	}
	
	public void dispose() {
		getSwtMenuItem().dispose();
		menu.dispose();
	}


}
