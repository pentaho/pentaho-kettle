package be.ibridge.kettle.trans.step.playlist;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.step.errorhandling.AbstractFileErrorHandler;

public class FilePlayListReplay implements FilePlayList {

	private final Date replayDate;

	private final String encoding;

	private final String lineNumberDirectory;

	private final String lineNumberExtension;

	private final String errorDirectory;

	private final String errorExtension;

	private FilePlayListReplayFile currentLineNumberFile;

	private FilePlayListReplayFile currentErrorFile;

	public FilePlayListReplay(Date replayDate, String lineNumberDirectory,
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
				lineNumberExtension, AbstractFileErrorHandler.NO_PARTS);
		if (lineFile.exists())
			currentLineNumberFile = new FilePlayListReplayLineNumberFile(
					lineFile, encoding, file);
		else
			currentLineNumberFile = new FilePlayListReplayFile(file);

		File errorFile = AbstractFileErrorHandler.getReplayFilename(
				errorDirectory, file.getName(), replayDate, errorExtension,
				AbstractFileErrorHandler.NO_PARTS);
		if (errorFile.exists())
			currentErrorFile = new FilePlayListReplayErrorFile(errorFile,
					file);
		else
			currentErrorFile = new FilePlayListReplayFile(file);
	}
}
