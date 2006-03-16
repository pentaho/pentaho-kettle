package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

public class TextFilePlayerAll implements TextFileReplayFactory,
		TextFilePlayer {

	public static final TextFileReplayFactory INSTANCE = new TextFilePlayerAll();

	public TextFilePlayer createPlayer(File file) {
		return this;
	}

	public boolean isProcessingNeeded(long lineNumberInFile) {
		return true;
	}

}
