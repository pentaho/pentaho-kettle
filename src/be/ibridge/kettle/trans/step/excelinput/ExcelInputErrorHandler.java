package be.ibridge.kettle.trans.step.excelinput;

import be.ibridge.kettle.core.exception.KettleException;

/**
 * 
 * @author Johnny Vanhentenyk
 * 
 */
public interface ExcelInputErrorHandler {
	
	/**
	 * Tells the handler which file is being processed.
	 * @param filename
	 * @throws KettleException 
	 */
	void handleFile(String filename) throws KettleException;

	/**
	 * This method handles an error when processing the excelInputRow
	 * @param excelInputRow
	 * @throws KettleException
	 */
	void handleLine(ExcelInputRow excelInputRow) throws KettleException;

	/**
	 * This method closes the handler;
	 * 
	 */
	void close() throws KettleException;
}
