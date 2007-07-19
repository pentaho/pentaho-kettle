package org.pentaho.xul.swt.tab;

public interface TabListener {

	public void tabSelected( TabItem item );
	
	public void tabDeselected( TabItem item );
	
	public boolean tabClose( TabItem item );
	
}
