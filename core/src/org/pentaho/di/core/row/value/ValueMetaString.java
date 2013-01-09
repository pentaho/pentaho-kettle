package org.pentaho.di.core.row.value;

import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaString extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaString() {
		this(null);
	}
	
	public ValueMetaString(String name) {
		super(name, ValueMetaInterface.TYPE_STRING);
	}
}
