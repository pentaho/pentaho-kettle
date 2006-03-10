package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;

public abstract class AbstractTextFileLineErrorHandler implements
		TextFileLineErrorHandler {
	private final LogWriter log = LogWriter.getInstance();

	private final String destinationDirectory;

	private final String fileExtension;

	private final String encoding;

	private String processingFilename;

	private OutputStreamWriter outputStreamWriter;

	public AbstractTextFileLineErrorHandler(String destinationDirectory,
			String fileExtension, String encoding) {
		this.destinationDirectory = destinationDirectory;
		this.fileExtension = fileExtension;
		this.encoding = encoding;
	}

	public abstract void handleLine(TextFileLine textFileLine)
			throws KettleException;

	/**
	 * returns the OutputWiter if exists. Otherwhise it will create a new one.
	 * 
	 * @return
	 * @throws KettleException
	 */
	Writer getWriter() throws KettleException {
		if (outputStreamWriter != null)
			return outputStreamWriter;
		File directory = new File(destinationDirectory);
		String name = null;
		if (fileExtension == null || fileExtension.length() == 0)
			name = processingFilename;
		else
			name = processingFilename + "." + fileExtension;
		File file = new File(directory, name);
		try {
			if (encoding == null)
				outputStreamWriter = new OutputStreamWriter(
						new FileOutputStream(file));
			else
				outputStreamWriter = new OutputStreamWriter(
						new FileOutputStream(file), encoding);
		} catch (Exception e) {
			throw new KettleException(
					"Could not create TextFileLineErrorHandler for file:"
							+ file.getPath(), e);
		}
		return outputStreamWriter;
	}

	public void close() throws KettleException {
		if (outputStreamWriter != null) {
			try {
				outputStreamWriter.flush();
			} catch (IOException exception) {
				log.logError("Could not flush content to file", exception
						.getLocalizedMessage());
			}
			try {
				outputStreamWriter.close();
			} catch (IOException exception) {
				throw new KettleException("Could not close file", exception);
			} finally {
				outputStreamWriter = null;
			}
		}
	}

	public void handleFile(String filename) throws KettleException {
		close();
		File file = new File(filename);
		this.processingFilename = file.getName();
	}

}
