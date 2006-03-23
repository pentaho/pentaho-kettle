package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

public interface TextFilePlayList {

	boolean isProcessingNeeded(File  file, long lineNr) throws KettleException;

}
