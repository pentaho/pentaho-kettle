package org.pentaho.di.core.row.value;

import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBoolean extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaBoolean() {
		this(null);
	}
	
	public ValueMetaBoolean(String name) {
		super(name, ValueMetaInterface.TYPE_BOOLEAN);
	}
}
