package org.pentaho.xul;

public interface XulItem {

	public String getId();

	public void setId(String id);

	public XulItem getParent();

	public void setParent(XulItem parent);

}
