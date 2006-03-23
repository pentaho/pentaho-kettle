package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

public class TextFilePlayListAll implements TextFilePlayList {

	public static final TextFilePlayList INSTANCE = new TextFilePlayListAll();

	public boolean isProcessingNeeded(File  file, long lineNr) {
		return true;
	}

}
