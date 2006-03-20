package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

class TextFilePlayListReplayFile implements TextFilePlayList {
	private File processingFile;

	public TextFilePlayListReplayFile(File processingFile) {
		this.processingFile = processingFile;
	}

	File getProcessingFile() {
		return processingFile;
	}

	public boolean isProcessingNeeded(TextFileLine textFileLine)
			throws KettleException {
		return false;
	}
}