package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;

public abstract class AbstractTextFileErrorHandler implements
		TextFileErrorHandler {
	private static final String DD_MMYYYY_HHMMSS = "ddMMyyyy-hhmmss";

	private final LogWriter log = LogWriter.getInstance();

	private final String destinationDirectory;

	private final String fileExtension;

	private final String encoding;

	private String processingFilename;

	private OutputStreamWriter outputStreamWriter;

	private String dateString;

	public AbstractTextFileErrorHandler(Date date,
			String destinationDirectory, String fileExtension, String encoding) {
		this.destinationDirectory = destinationDirectory;
		this.fileExtension = fileExtension;
		this.encoding = encoding;
		initDateFormatter(date);
	}

	private void initDateFormatter(Date date) {
		dateString = createDateFormat().format(date);
	}

	static DateFormat createDateFormat() {
		return new SimpleDateFormat(DD_MMYYYY_HHMMSS);
	}

	public abstract void handleLineError(TextFileLine textFileLine)
			throws KettleException;
	
	public static File getReplayFilename(String destinationDirectory, String processingFilename, String dateString, String extension )
	{
		String name = null;
		if (extension == null || extension.length() == 0)
			name = processingFilename + "." + dateString;
		else
			name = processingFilename + "." + dateString + "." + extension;
		return new File(Const.replEnv(destinationDirectory), name);
	}
	
	public static File getReplayFilename(String destinationDirectory, String processingFilename, Date date, String extension )
	{
		return getReplayFilename(destinationDirectory, processingFilename, createDateFormat().format(date), extension);
	}

	/**
	 * returns the OutputWiter if exists. Otherwhise it will create a new one.
	 * 
	 * @return
	 * @throws KettleException
	 */
	Writer getWriter() throws KettleException {
		if (outputStreamWriter != null)
			return outputStreamWriter;
		File file = getReplayFilename(destinationDirectory, processingFilename, dateString, fileExtension);
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

	public void handleFile(File file) throws KettleException {
		close();
		this.processingFilename = file.getName();
	}

}
