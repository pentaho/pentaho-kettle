package org.pentaho.di.core.playlist;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;


public class FilePlayListReplayErrorFile extends FilePlayListReplayFile {

	private FileObject errorFile;

	public FilePlayListReplayErrorFile(FileObject errorFile, FileObject processingFile) {
		super(processingFile, AbstractFileErrorHandler.NO_PARTS);
		this.errorFile = errorFile;
	}

	public boolean isProcessingNeeded(FileObject file, long lineNr, String filePart)
			throws KettleException {
        try
        {
            return errorFile.exists();
        }
        catch(IOException e)
        {
            throw new KettleException(e);
        }
	}

}
