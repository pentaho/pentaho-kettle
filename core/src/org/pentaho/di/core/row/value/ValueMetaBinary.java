package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBinary extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaBinary() {
		this(null);
	}
	
	public ValueMetaBinary(String name) {
		super(name, ValueMetaInterface.TYPE_BINARY);
	}
	
	@Override
	public Object getNativeDataType(Object object) throws KettleValueException {
	  return getBinary(object);
	}
}
