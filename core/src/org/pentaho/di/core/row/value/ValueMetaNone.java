package org.pentaho.di.core.row.value;

import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaNone extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaNone() {
		this(null);
	}
	
	public ValueMetaNone(String name) {
		super(name, ValueMetaInterface.TYPE_NONE);
	}
}
