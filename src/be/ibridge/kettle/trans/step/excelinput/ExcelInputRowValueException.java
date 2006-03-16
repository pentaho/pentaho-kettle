package be.ibridge.kettle.trans.step.excelinput;

public class ExcelInputRowValueException extends ExcelInputRecoverableException {

	/**
     * 
     */
    private static final long serialVersionUID = -6588939491162445000L;
    
    ExcelInputRow excelInputRow;

	public ExcelInputRowValueException() {
		super();
	}

	public ExcelInputRowValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExcelInputRowValueException(String message) {
		super(message);
	}

	public ExcelInputRowValueException(Throwable cause) {
		super(cause);
	}

}
