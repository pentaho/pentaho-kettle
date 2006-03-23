package be.ibridge.kettle.trans.step.playlist;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.step.errorhandling.AbstractFileErrorHandler;

public class FilePlayListReplayErrorFile extends FilePlayListReplayFile {

	private File errorFile;

	public FilePlayListReplayErrorFile(File errorFile, File processingFile) {
		super(processingFile, AbstractFileErrorHandler.NO_PARTS);
		this.errorFile = errorFile;
	}

	public boolean isProcessingNeeded(File file, long lineNr, String filePart)
			throws KettleException {
		return errorFile.exists();
	}

}
