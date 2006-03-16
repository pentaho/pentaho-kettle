package be.ibridge.kettle.trans.step.excelinput;

public class ExcelInputRowValueException extends ExcelInputRecoverableException {

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
