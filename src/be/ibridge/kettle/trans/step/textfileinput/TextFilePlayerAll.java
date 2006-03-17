package be.ibridge.kettle.trans.step.textfileinput;

public class TextFilePlayerAll implements TextFilePlayListFactory {

	public static final TextFilePlayListFactory INSTANCE = new TextFilePlayerAll();

	public boolean isProcessingNeeded(TextFileLine textFileLine) {
		return true;
	}

}
