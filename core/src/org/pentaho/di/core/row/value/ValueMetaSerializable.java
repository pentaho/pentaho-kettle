package org.pentaho.di.core.row.value;

import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaSerializable extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaSerializable() {
		this(null);
	}
	
	public ValueMetaSerializable(String name) {
		super(name, ValueMetaInterface.TYPE_SERIALIZABLE);
	}
}
