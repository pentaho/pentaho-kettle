package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class TextFileErrorHandlerMissingFiles extends
		AbstractTextFileErrorHandler {

	public static final String THIS_FILE_DOES_NOT_EXIST = "This file does not exist";

	public static final String THIS_FILE_WAS_NOT_ACCESSIBLE = "This file was not accessible";

	public TextFileErrorHandlerMissingFiles(Date date,
			String destinationDirectory, String fileExtension, String encoding) {
		super(date, destinationDirectory, fileExtension, encoding);
	}

	public void handleLineError(TextFileLine textFileLine) {

	}

	public void handleNonExistantFile(File file) throws KettleException {
		handleFile(file);
		try {
			getWriter().write(THIS_FILE_DOES_NOT_EXIST);
			getWriter().write(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create NonExistantFile for :"
					+ file.getPath(), e);
		}
	}

	public void handleNonAccessibleFile(File file) throws KettleException {
		handleFile(file);
		try {
			getWriter().write(THIS_FILE_WAS_NOT_ACCESSIBLE);
			getWriter().write(Const.CR);
		} catch (Exception e) {
			throw new KettleException(
					"Could not create NonAccessibleFile for :" + file.getPath(),
					e);
		}
	}

}