package org.pentaho.di.core.playlist;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;

public interface FilePlayList {

	boolean isProcessingNeeded(FileObject file, long lineNr, String filePart) throws KettleException;

}
