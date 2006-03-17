package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;

public class TextFileLine {
	String line;

	long lineNumber;

	File file;

	public TextFileLine(String line, long lineNumber, File file) {
		super();
		this.line = line;
		this.lineNumber = lineNumber;
		this.file = file;
	}
}
