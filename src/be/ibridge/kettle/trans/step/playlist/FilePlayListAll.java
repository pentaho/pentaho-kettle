package be.ibridge.kettle.trans.step.playlist;

import java.io.File;


public class FilePlayListAll implements FilePlayList {

	public static final FilePlayList INSTANCE = new FilePlayListAll();

	public boolean isProcessingNeeded(File  file, long lineNr, String filePart) {
		return true;
	}

}
