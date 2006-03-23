package be.ibridge.kettle.trans.step.errorhandling;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

/**
 * 
 * @author Johnny Vanhentenyk
 * 
 */
public interface FileErrorHandler {

	/**
	 * Tells the handler which file is being processed.
	 * 
	 * @param file
	 * @throws KettleException
	 */
	void handleFile(File file) throws KettleException;

	/**
	 * This method handles an error when processing the line with corresponding
	 * lineNr.
	 * 
	 * @param lineNr
	 * @param filePart
	 *            allows us to split error according to a filePart
	 * @throws KettleException
	 */
	void handleLineError(long lineNr, String filePart) throws KettleException;

	/**
	 * This method closes the handler;
	 * 
	 */
	void close() throws KettleException;

	/**
	 * This method handles a file that is required, but does not exist.
	 * 
	 * @param file
	 * @throws KettleException
	 */
	void handleNonExistantFile(File file) throws KettleException;

	/**
	 * This method handles a file that is required, but is not accessible.
	 * 
	 * @param file
	 * @throws KettleException
	 */
	void handleNonAccessibleFile(File file)
			throws KettleException;
}
