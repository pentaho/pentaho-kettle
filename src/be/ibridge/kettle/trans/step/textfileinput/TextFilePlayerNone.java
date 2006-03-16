package be.ibridge.kettle.trans.step.textfileinput;

public class TextFilePlayerNone implements TextFilePlayer {

	public static final TextFilePlayer INSTANCE = new TextFilePlayerNone();

	public boolean isProcessingNeeded(long lineNumberInFile) {
		return false;
	}

}
