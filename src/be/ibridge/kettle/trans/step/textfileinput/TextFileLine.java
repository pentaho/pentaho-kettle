package be.ibridge.kettle.trans.step.textfileinput;

public class TextFileLine {
	String line;

	long lineNumber;

	public TextFileLine(String line, long lineNumber) {
		super();
		this.line = line;
		this.lineNumber = lineNumber;
	}
}
