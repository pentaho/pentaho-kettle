package org.pentaho.di.core.row.value;

import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBigNumber extends ValueMetaBase implements ValueMetaInterface {
	
	public ValueMetaBigNumber() {
		this(null);
	}
	
	public ValueMetaBigNumber(String name) {
		super(name, ValueMetaInterface.TYPE_BIGNUMBER);
	}
}
