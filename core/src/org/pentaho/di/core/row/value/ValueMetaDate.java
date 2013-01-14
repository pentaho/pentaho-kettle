package org.pentaho.di.core.row.value;

import java.util.Date;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaDate extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaDate() {
		this(null);
	}
	
	public ValueMetaDate(String name) {
		super(name, ValueMetaInterface.TYPE_DATE);
	}
	
	@Override
	public Date getDate(Object object) throws KettleValueException {
		return super.getDate(object);
	}
}
