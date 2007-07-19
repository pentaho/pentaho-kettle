package org.pentaho.xul;


public class XulObject implements XulItem {

    private String id;
    private XulItem parent;

	public XulObject( ) {
		this( null, null );
	}

	public XulObject( String id, XulItem parent ) {
		super();
		this.id = id;
		this.parent = parent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public XulItem getParent() {
		return parent;
	}

	public void setParent(XulItem parent) {
		this.parent = parent;
	}
	
	public Object getNativeObject() {
		return null;
	}
}
