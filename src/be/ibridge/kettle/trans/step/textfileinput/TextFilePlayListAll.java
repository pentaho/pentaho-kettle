package be.ibridge.kettle.trans.step.textfileinput;

public class TextFilePlayListAll implements TextFilePlayList {

	public static final TextFilePlayList INSTANCE = new TextFilePlayListAll();

	public boolean isProcessingNeeded(TextFileLine textFileLine) {
		return true;
	}

}
