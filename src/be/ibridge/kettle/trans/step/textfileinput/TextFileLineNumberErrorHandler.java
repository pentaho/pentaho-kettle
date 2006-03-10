package be.ibridge.kettle.trans.step.textfileinput;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class TextFileLineNumberErrorHandler extends AbstractTextFileLineErrorHandler {

	public TextFileLineNumberErrorHandler(String destinationDirectory, String fileExtension, String encoding) {
		super(destinationDirectory, fileExtension, encoding);
	}

	public void handleLine(TextFileLine textFileLine) throws KettleException {
		try {
			getWriter().write(String.valueOf(textFileLine.lineNumber));
			getWriter().append(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create write line:" + textFileLine.line, e);

		}
	}
}
