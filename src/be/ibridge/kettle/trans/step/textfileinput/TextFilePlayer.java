package be.ibridge.kettle.trans.step.textfileinput;

public interface TextFilePlayer {

	boolean isProcessingNeeded(long lineNumberInFile);

}
