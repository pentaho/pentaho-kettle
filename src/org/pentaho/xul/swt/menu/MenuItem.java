package org.pentaho.xul.swt.menu;

import org.eclipse.swt.graphics.Image;

public class MenuItem extends MenuObject {

    private org.eclipse.swt.widgets.MenuItem menuItem;
    private boolean enabled;
    private boolean checked;
    
	public MenuItem(Menu parent, String id ) {
		super( id, parent );
	}

	public MenuItem(MenuBar parent, String id ) {
		super( id, parent );
	}

	public Menu getMenu() {
		return (Menu) getParent();
	}

	public void register() {
		getMenu().addItem( this );
		getMenu().register( this, getId(), null );
	}

	public org.eclipse.swt.widgets.MenuItem getSwtMenuItem() {
		return menuItem;
	}

	public void setSwtMenuItem(org.eclipse.swt.widgets.MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public void dispose() {
		getSwtMenuItem().dispose();
	}

	public void setImage( Image image ) {
		getSwtMenuItem().setImage( image );
	}
	
	public void setText( String text ) {
		getSwtMenuItem().setText(text);
	}
	
	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		getSwtMenuItem().setSelection( checked );
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		getSwtMenuItem().setEnabled( enabled );
	}

}
