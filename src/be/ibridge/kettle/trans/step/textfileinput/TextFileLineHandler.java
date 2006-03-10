package be.ibridge.kettle.trans.step.textfileinput;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class TextFileLineHandler extends AbstractTextFileLineErrorHandler {

	public TextFileLineHandler(String destinationDirectory,
			String fileExtension, String encoding) {
		super(destinationDirectory, fileExtension, encoding);
	}

	public void handleLine(TextFileLine textFileLine) throws KettleException {
		try {
			getWriter().write(textFileLine.line);
			getWriter().append(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create write line:"
					+ textFileLine.line, e);
		}
	}
}
