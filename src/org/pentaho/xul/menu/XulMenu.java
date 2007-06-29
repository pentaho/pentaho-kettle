package org.pentaho.xul.menu;

import org.pentaho.xul.XulItem;

public interface XulMenu extends XulItem {

	public int getItemCount();
	
	public boolean handleMenuEvent( String id );

	public int indexOf( String id );
	
	public int indexOf( XulMenuItem item );
	
	public void addItem( XulMenuItem item );
	
	public XulMenuItem getItem( int idx );
	
	public void remove( XulMenuItem item );
	
	public void setEnabled(boolean enabled);
	
	public void register( XulMenuItem item, String id, String accessKey );
}
