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

	public boolean isProcessingNeeded(TextFileLine textFileLine)
			throws KettleException {
		initializeCurrentIfNeeded(textFileLine);
		return currentLineNumberFile.isProcessingNeeded(textFileLine)
				|| currentErrorFile.isProcessingNeeded(textFileLine);
	}

	private void initializeCurrentIfNeeded(TextFileLine textFileLine)
			throws KettleException {
		if (!textFileLine.file.equals(getCurrentProcessingFile()))
			initializeCurrent(textFileLine);
	}

	private void initializeCurrent(TextFileLine textFileLine)
			throws KettleException {
		File lineFile = AbstractFileErrorHandler.getReplayFilename(
				lineNumberDirectory, textFileLine.file.getName(), replayDate,
				lineNumberExtension, AbstractFileErrorHandler.DUMMY_SOURCE);
		if (lineFile.exists())
			currentLineNumberFile = new TextFilePlayListReplayLineNumberFile(
					lineFile, encoding, textFileLine.file);
		else
			currentLineNumberFile = new TextFilePlayListReplayFile(
					textFileLine.file);

		File errorFile = AbstractFileErrorHandler.getReplayFilename(
				errorDirectory, textFileLine.file.getName(), replayDate,
				errorExtension, AbstractFileErrorHandler.DUMMY_SOURCE);
		if (errorFile.exists())
			currentErrorFile = new TextFilePlayListReplayErrorFile(errorFile,
					textFileLine.file);
		else
			currentErrorFile = new TextFilePlayListReplayFile(textFileLine.file);
	}
}
