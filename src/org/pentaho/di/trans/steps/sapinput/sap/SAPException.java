package org.pentaho.di.trans.steps.sapinput.sap;

import org.pentaho.di.core.exception.KettleException;

public class SAPException extends KettleException {

	private static final long serialVersionUID = 1L;

	public SAPException(String message) {
		super(message);
	}

	public SAPException(String message, Throwable cause) {
		super(message, cause);
	}

}
