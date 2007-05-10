package org.pentaho.di.core.playlist;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.exception.KettleException;

public class FilePlayListReplayFile implements FilePlayList {
	private FileObject processingFile;
	private String processingFilePart;

	public FilePlayListReplayFile(FileObject processingFile, String processingFilePart) {
		this.processingFile = processingFile;
		this.processingFilePart = processingFilePart;
	}

	FileObject getProcessingFile() {
		return processingFile;
	}

	String getProcessingFilePart() {
		return processingFilePart;
	}

	public boolean isProcessingNeeded(FileObject file, long lineNr, String filePart)
			throws KettleException {
		return false;
	}
}