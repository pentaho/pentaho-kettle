package be.ibridge.kettle.trans.step.playlist;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

public class FilePlayListReplayErrorFile extends FilePlayListReplayFile {

	private File errorFile;

	public FilePlayListReplayErrorFile(File errorFile, File processingFile) {
		super(processingFile);
		this.errorFile = errorFile;
	}

	public boolean isProcessingNeeded(File file, long lineNr)
			throws KettleException {
		return errorFile.exists();
	}

}
