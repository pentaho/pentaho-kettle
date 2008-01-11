package org.pentaho.di.trans.steps.validator;

import org.pentaho.di.core.exception.KettleValueException;

public class KettleValidatorException extends KettleValueException {

	public static final int ERROR_NULL_VALUE_NOT_ALLOWED      =  1;
	public static final int ERROR_LONGER_THAN_MAXIMUM_LENGTH  =  2;
	public static final int ERROR_SHORTER_THAN_MINIMUM_LENGTH =  3;
	public static final int ERROR_UNEXPECTED_DATA_TYPE        =  4;
	public static final int ERROR_LOWER_THAN_ALLOWED_MINIMUM  =  5;
	public static final int ERROR_HIGHER_THAN_ALLOWED_MAXIMUM =  6;
	public static final int ERROR_VALUE_NOT_IN_LIST           =  7;
	
	private static final String errorCode[] = new String[] { "KVD-001", "KVD-002", "KVD-003", "KVD-004", "KVD-005", "KVD-006", "KVD-007", };
	
	private int code;
	private String fieldname;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -212228277329271284L;

	/**
	 * Constructs a new throwable with the specified detail message.
	 * @param code - the error code, see the static members of this class.
	 * @param message - the detail message. The detail message is saved for later retrieval by the getMessage() method.
	 */
	public KettleValidatorException(int code, String message, String fieldname)
	{
		super(message);
		this.code = code;
		this.fieldname = fieldname;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/**
	 * @return the code in string format
	 */
	public String getCodeDesc() { 
		return errorCode[code];
	}

	/**
	 * @return the fieldname
	 */
	public String getFieldname() {
		return fieldname;
	}

	/**
	 * @param fieldname the fieldname to set
	 */
	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}
}
