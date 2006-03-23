package be.ibridge.kettle.trans.step.playlist;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

public class FilePlayListReplayFile implements FilePlayList {
	private File processingFile;

	public FilePlayListReplayFile(File processingFile) {
		this.processingFile = processingFile;
	}

	File getProcessingFile() {
		return processingFile;
	}

	public boolean isProcessingNeeded(File file, long lineNr)
			throws KettleException {
		return false;
	}
}