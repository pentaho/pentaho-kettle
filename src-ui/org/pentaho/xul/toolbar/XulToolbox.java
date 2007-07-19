package org.pentaho.xul.toolbar;

import org.pentaho.xul.XulItem;

public interface XulToolbox extends XulItem {

	public String[] getToolbarIds();
	
	public XulToolbar getToolbarById( String id );

}
