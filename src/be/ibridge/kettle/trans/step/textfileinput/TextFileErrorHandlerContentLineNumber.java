package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class TextFileErrorHandlerContentLineNumber extends
		AbstractTextFileErrorHandler {

	public TextFileErrorHandlerContentLineNumber(Date date,
			String destinationDirectory, String fileExtension, String encoding) {
		super(date, destinationDirectory, fileExtension, encoding);
	}

	public void handleLineError(TextFileLine textFileLine) throws KettleException {
		try {
			getWriter().write(String.valueOf(textFileLine.lineNumber));
			getWriter().write(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create write line:"
					+ textFileLine.line, e);

		}
	}

	public void handleNonExistantFile(File file) {
	}

	public void handleNonAccessibleFile(File file) {
	}

}
