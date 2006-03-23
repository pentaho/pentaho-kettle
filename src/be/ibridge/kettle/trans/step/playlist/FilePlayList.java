package be.ibridge.kettle.trans.step.playlist;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

public interface FilePlayList {

	boolean isProcessingNeeded(File  file, long lineNr, String filePart) throws KettleException;

}
