package be.ibridge.kettle.trans.step.textfileinput;

import be.ibridge.kettle.core.exception.KettleException;

public interface TextFilePlayList {

	boolean isProcessingNeeded(TextFileLine textFileLine) throws KettleException;

}
