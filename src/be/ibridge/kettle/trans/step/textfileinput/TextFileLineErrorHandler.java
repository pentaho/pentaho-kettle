package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

/**
 * 
 * @author Johnny Vanhentenyk
 * 
 */
public interface TextFileLineErrorHandler {

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
	void handleLine(TextFileLine textFileLine) throws KettleException;

	/**
	 * This method closes the handler;
	 * 
	 */
	void close() throws KettleException;
}
