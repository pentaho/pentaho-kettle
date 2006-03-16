package be.ibridge.kettle.trans.step.excelinput;

import be.ibridge.kettle.core.exception.KettleException;

public class ExcelInputRecoverableException extends KettleException{

	public ExcelInputRecoverableException() {
		super();
	}

	public ExcelInputRecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExcelInputRecoverableException(String message) {
		super(message);
	}

	public ExcelInputRecoverableException(Throwable cause) {
		super(cause);
	}

}
