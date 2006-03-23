package be.ibridge.kettle.trans.step.errorhandling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;

public abstract class AbstractFileErrorHandler implements FileErrorHandler {
	private static final String DD_MMYYYY_HHMMSS = "ddMMyyyy-hhmmss";

	public static final String NO_PARTS = "NO_PARTS";

	private final LogWriter log = LogWriter.getInstance();

	private final String destinationDirectory;

	private final String fileExtension;

	private final String encoding;

	private String processingFilename;

	private Map writers;

	private String dateString;

	public AbstractFileErrorHandler(Date date, String destinationDirectory,
			String fileExtension, String encoding) {
		this.destinationDirectory = destinationDirectory;
		this.fileExtension = fileExtension;
		this.encoding = encoding;
		this.writers = new HashMap();
		initDateFormatter(date);
	}

	private void initDateFormatter(Date date) {
		dateString = createDateFormat().format(date);
	}

	public static DateFormat createDateFormat() {
		return new SimpleDateFormat(DD_MMYYYY_HHMMSS);
	}

	public static File getReplayFilename(String destinationDirectory,
			String processingFilename, String dateString, String extension, Object source) {
		String name = null;
		String sourceAdding = "";
		if (source != NO_PARTS) {
			sourceAdding = "_" + source.toString();
		}
		if (extension == null || extension.length() == 0)
			name = processingFilename + sourceAdding + "." + dateString;
		else
			name = processingFilename + sourceAdding + "." + dateString + "." + extension;
		return new File(Const.replEnv(destinationDirectory), name);
	}

	public static File getReplayFilename(String destinationDirectory,
			String processingFilename, Date date, String extension, Object source) {
		return getReplayFilename(destinationDirectory, processingFilename,
				createDateFormat().format(date), extension, source);
	}

	/**
	 * returns the OutputWiter if exists. Otherwhise it will create a new one.
	 * 
	 * @return
	 * @throws KettleException
	 */
	Writer getWriter(Object source) throws KettleException {
		Writer outputStreamWriter = (Writer) writers.get(source);
		if (outputStreamWriter != null)
			return outputStreamWriter;
		File file = getReplayFilename(destinationDirectory, processingFilename, dateString, fileExtension, source);
		try {
			if (encoding == null)
				outputStreamWriter = new OutputStreamWriter(
						new FileOutputStream(file));
			else
				outputStreamWriter = new OutputStreamWriter(
						new FileOutputStream(file), encoding);
		} catch (Exception e) {
			throw new KettleException(
					"Could not create FileErrorHandler for file:"
							+ file.getPath(), e);
		}
		writers.put(source, outputStreamWriter);
		return outputStreamWriter;
	}

	public void close() throws KettleException {
		for (Iterator iter = writers.values().iterator(); iter.hasNext();) {
			close((Writer) iter.next());
		}
		writers = new HashMap();
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

	public void handleFile(File file) throws KettleException {
		close();
		this.processingFilename = file.getName();
	}

}
