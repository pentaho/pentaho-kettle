package org.pentaho.xul.toolbar;

import org.pentaho.xul.XulItem;

public interface XulToolbar extends XulItem {

	public boolean handleMenuEvent( String id );

	public void setEnableById( String id, boolean enabled );
	
	public void setHintById( String id, String text );
	
	public void setTextById( String id, String text );
	
	public void addMenuListener(String id, Object listener, String methodName);

	public int getMode();
	
	public void setMode(int mode);
	
	public XulToolbarButton getButtonById(String id);

}
