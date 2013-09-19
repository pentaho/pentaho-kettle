package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBoolean extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaBoolean() {
		this(null);
	}
	
	public ValueMetaBoolean(String name) {
		super(name, ValueMetaInterface.TYPE_BOOLEAN);
	}
	
	@Override
	public Object getNativeDataType(Object object) throws KettleValueException {
	  return getBoolean(object);
	}
}
