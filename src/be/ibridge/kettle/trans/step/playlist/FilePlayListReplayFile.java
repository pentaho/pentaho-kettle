package be.ibridge.kettle.trans.step.playlist;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

public class FilePlayListReplayFile implements FilePlayList {
	private File processingFile;
	private String processingFilePart;

	public FilePlayListReplayFile(File processingFile, String processingFilePart) {
		this.processingFile = processingFile;
		this.processingFilePart = processingFilePart;
	}

	File getProcessingFile() {
		return processingFile;
	}

	String getProcessingFilePart() {
		return processingFilePart;
	}

	public boolean isProcessingNeeded(File file, long lineNr, String filePart)
			throws KettleException {
		return false;
	}
}