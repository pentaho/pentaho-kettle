package be.ibridge.kettle.trans.step.textfileinput;

import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class TextFileLineHandler extends AbstractTextFileLineErrorHandler {

	public TextFileLineHandler(Date date, String destinationDirectory,
			String fileExtension, String encoding) {
		super(date, destinationDirectory, fileExtension, encoding);
	}

	public void handleLine(TextFileLine textFileLine) throws KettleException {
		try {
			getWriter().write(textFileLine.line);
			getWriter().write(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create write line:"
					+ textFileLine.line, e);
		}
	}
}
