package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.step.errorhandling.AbstractFileErrorHandler;

public class TextFilePlayListReplay implements TextFilePlayList {

	private final Date replayDate;

	private final String encoding;

	private final String lineNumberDirectory;

	private final String lineNumberExtension;

	private final String errorDirectory;

	private final String errorExtension;

	private TextFilePlayListReplayFile currentLineNumberFile;

	private TextFilePlayListReplayFile currentErrorFile;

	public TextFilePlayListReplay(Date replayDate, String lineNumberDirectory,
			String lineNumberExtension, String errorDirectory,
			String errorExtension, String encoding) {
		this.replayDate = replayDate;
		this.errorDirectory = errorDirectory;
		this.errorExtension = errorExtension;
		this.encoding = encoding;
		this.lineNumberDirectory = lineNumberDirectory;
		this.lineNumberExtension = lineNumberExtension;

	}

	private File getCurrentProcessingFile() {
		File result = null;
		if (currentLineNumberFile != null)
			result = currentLineNumberFile.getProcessingFile();
		return result;
	}

	public boolean isProcessingNeeded(File file, long lineNr)
			throws KettleException {
		initializeCurrentIfNeeded(file);
		return currentLineNumberFile.isProcessingNeeded(file, lineNr)
				|| currentErrorFile.isProcessingNeeded(file, lineNr);
	}

	private void initializeCurrentIfNeeded(File file) throws KettleException {
		if (!file.equals(getCurrentProcessingFile()))
			initializeCurrent(file);
	}

	private void initializeCurrent(File file) throws KettleException {
		File lineFile = AbstractFileErrorHandler.getReplayFilename(
				lineNumberDirectory, file.getName(), replayDate,
				lineNumberExtension, AbstractFileErrorHandler.DUMMY_SOURCE);
		if (lineFile.exists())
			currentLineNumberFile = new TextFilePlayListReplayLineNumberFile(
					lineFile, encoding, file);
		else
			currentLineNumberFile = new TextFilePlayListReplayFile(file);

		File errorFile = AbstractFileErrorHandler.getReplayFilename(
				errorDirectory, file.getName(), replayDate, errorExtension,
				AbstractFileErrorHandler.DUMMY_SOURCE);
		if (errorFile.exists())
			currentErrorFile = new TextFilePlayListReplayErrorFile(errorFile,
					file);
		else
			currentErrorFile = new TextFilePlayListReplayFile(file);
	}
}
