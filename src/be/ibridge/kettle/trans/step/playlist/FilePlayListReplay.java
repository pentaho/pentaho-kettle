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

	private String getCurrentProcessingFilePart() {
		String result = null;
		if (currentLineNumberFile != null)
			result = currentLineNumberFile.getProcessingFilePart();
		return result;
	}

	public boolean isProcessingNeeded(File file, long lineNr, String filePart)
			throws KettleException {
		initializeCurrentIfNeeded(file, filePart);
		return currentLineNumberFile.isProcessingNeeded(file, lineNr, filePart)
				|| currentErrorFile.isProcessingNeeded(file, lineNr, filePart);
	}

	private void initializeCurrentIfNeeded(File file, String filePart) throws KettleException {
		if (!(file.equals(getCurrentProcessingFile()) && filePart.equals(getCurrentProcessingFilePart())))
			initializeCurrent(file, filePart);
	}

	private void initializeCurrent(File file, String filePart) throws KettleException {
		File lineFile = AbstractFileErrorHandler.getReplayFilename(
				lineNumberDirectory, file.getName(), replayDate,
				lineNumberExtension, filePart);
		if (lineFile.exists())
			currentLineNumberFile = new FilePlayListReplayLineNumberFile(
					lineFile, encoding, file, filePart);
		else
			currentLineNumberFile = new FilePlayListReplayFile(file, filePart);

		File errorFile = AbstractFileErrorHandler.getReplayFilename(
				errorDirectory, file.getName(), replayDate, errorExtension,
				AbstractFileErrorHandler.NO_PARTS);
		if (errorFile.exists())
			currentErrorFile = new FilePlayListReplayErrorFile(errorFile, file);
		else
			currentErrorFile = new FilePlayListReplayFile(file, AbstractFileErrorHandler.NO_PARTS);
	}
}
