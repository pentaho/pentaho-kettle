package be.ibridge.kettle.trans.step.excelinput;

import be.ibridge.kettle.core.exception.KettleException;

public class ExcelInputRecoverableException extends KettleException{

	/**
     * 
     */
    private static final long serialVersionUID = -4379052886893806698L;

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
