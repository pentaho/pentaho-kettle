package be.ibridge.kettle.trans.step.excelinput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;

public abstract class AbstractExcelInputErrorHandler implements
		ExcelInputErrorHandler {
	private final LogWriter log = LogWriter.getInstance();

	private final String destinationDirectory;

	private final String fileExtension;

	private final String encoding;

	private String processingFilename;

	private Map writers;

	public AbstractExcelInputErrorHandler(String destinationDirectory,
			String fileExtension, String encoding) {
		this.destinationDirectory = destinationDirectory;
		this.fileExtension = fileExtension;
		this.encoding = encoding;
		this.writers = new HashMap();
	}

	public abstract void handleLine(ExcelInputRow excelInputRow)
			throws KettleException;

	/**
	 * returns the OutputWiter if exists. Otherwhise it will create a new one.
	 * 
	 * @return
	 * @throws KettleException
	 */
	Writer getWriter(String sheetName) throws KettleException {
		Writer outputStreamWriter = (Writer) writers.get(sheetName);
		if (outputStreamWriter != null)
			return outputStreamWriter;
		File directory = new File(Const.replEnv(destinationDirectory));
		String name = null;
		if (fileExtension == null || fileExtension.length() == 0)
			name = processingFilename + "_" + sheetName;
		else
			name = processingFilename + "_" + sheetName + "." + fileExtension;
		File file = new File(directory, name);
		System.out.println("Creating new error handler @ " + file.getAbsolutePath());
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
		writers.put(sheetName, outputStreamWriter);
		return outputStreamWriter;
	}

	public void close() throws KettleException {
		for (Iterator iter = writers.values().iterator(); iter.hasNext();) {
			close((Writer) iter.next());
		}
	}

	private void close(Writer outputStreamWriter) throws KettleException {
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
