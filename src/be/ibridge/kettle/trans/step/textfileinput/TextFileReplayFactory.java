package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

import be.ibridge.kettle.core.exception.KettleException;

public interface TextFileReplayFactory {

	TextFilePlayer createPlayer(File file) throws KettleException;

}
