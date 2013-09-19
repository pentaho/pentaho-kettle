package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBigNumber extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaBigNumber() {
		this(null);
	}
	
	public ValueMetaBigNumber(String name) {
		super(name, ValueMetaInterface.TYPE_BIGNUMBER);
	}
	
	@Override
	public Object getNativeDataType(Object object) throws KettleValueException {
	  return getBigNumber(object);
	}
}
