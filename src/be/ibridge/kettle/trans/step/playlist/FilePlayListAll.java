package be.ibridge.kettle.trans.step.playlist;

import org.apache.commons.vfs.FileObject;


public class FilePlayListAll implements FilePlayList {

	public static final FilePlayList INSTANCE = new FilePlayListAll();

	public boolean isProcessingNeeded(FileObject file, long lineNr, String filePart) {
		return true;
	}

}
