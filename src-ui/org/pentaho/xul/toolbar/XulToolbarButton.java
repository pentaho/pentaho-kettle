package org.pentaho.xul.toolbar;

import org.pentaho.xul.XulItem;

public interface XulToolbarButton extends XulItem {

	public void setImage( Object image );
	
	public void setSelectedImage( Object image );
	
	public void setDisabledImage( Object image );
	
	public void setEnable(boolean enabled);

	public void setText( String text );

	public void setHint(String text);
}
