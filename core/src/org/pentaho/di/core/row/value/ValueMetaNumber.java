package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaNumber extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaNumber() {
		this(null);
	}
	
	public ValueMetaNumber(String name) {
		super(name, ValueMetaInterface.TYPE_NUMBER);
	}
	
	@Override
	public Object getNativeDataType(Object object) throws KettleValueException {
	  return getNumber(object);
	}
}
