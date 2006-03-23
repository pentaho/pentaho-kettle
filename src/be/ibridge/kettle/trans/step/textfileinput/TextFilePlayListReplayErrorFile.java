package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

public class TextFilePlayListReplayErrorFile extends TextFilePlayListReplayFile {

	private File errorFile;

	public TextFilePlayListReplayErrorFile(File errorFile, File processingFile) {
		super(processingFile);
		this.errorFile = errorFile;
	}

	public boolean isProcessingNeeded(File file, long lineNr)
			throws KettleException {
		return errorFile.exists();
	}

}
