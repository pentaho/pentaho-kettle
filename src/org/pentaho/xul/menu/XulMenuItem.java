package org.pentaho.xul.menu;

import org.eclipse.swt.graphics.Image;
import org.pentaho.xul.XulItem;

public interface XulMenuItem extends XulItem {

	
	public void dispose();

	public void setImage( Image image );
	
	public void setText( String text );
	
	public boolean isChecked();

	public void setChecked(boolean checked);

	public boolean isEnabled();

	public void setEnabled(boolean enabled);
	
	public XulMenu getMenu();
}
