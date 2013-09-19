package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaInteger extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaInteger() {
		this(null);
	}
	
	public ValueMetaInteger(String name) {
		super(name, ValueMetaInterface.TYPE_INTEGER);
	}
	
	@Override
	public Object getNativeDataType(Object object) throws KettleValueException {
	  return getInteger(object);
	}
}
