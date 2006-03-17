package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

/**
 * 
 * @author Johnny Vanhentenyk
 * 
 */
public interface TextFileErrorHandler {

	/**
	 * Tells the handler which file is being processed.
	 * 
	 * @param file
	 * @throws KettleException
	 */
	void handleFile(File file) throws KettleException;

	/**
	 * This method handles an error when processing the textFileLine
	 * 
	 * @param textFileLine
	 * @throws KettleException
	 */
	void handleLineError(TextFileLine textFileLine) throws KettleException;

	/**
	 * This method closes the handler;
	 * 
	 */
	void close() throws KettleException;

	/**
	 * This method handles a file that is required, but does not exist.
	 * @param file
	 * @throws KettleException 
	 */
	void handleNonExistantFile(File file) throws KettleException;

	/**
	 * This method handles a file that is required, but is not accessible.
	 * @param file
	 * @throws KettleException 
	 */
	void handleNonAccessibleFile(File file) throws KettleException;
}
