package be.ibridge.kettle.trans.step.errorhandling;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class FileErrorHandlerContentLineNumber extends AbstractFileErrorHandler {

	public FileErrorHandlerContentLineNumber(Date date,
			String destinationDirectory, String fileExtension, String encoding) {
		super(date, destinationDirectory, fileExtension, encoding);
	}

	public void handleLineError(long lineNr, Object source)
			throws KettleException {
		try {
			getWriter(source).write(String.valueOf(lineNr));
			getWriter(source).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create write line:" + lineNr,
					e);

		}
	}

	public void handleNonExistantFile(File file ) {
	}

	public void handleNonAccessibleFile(File file ) {
	}

}
