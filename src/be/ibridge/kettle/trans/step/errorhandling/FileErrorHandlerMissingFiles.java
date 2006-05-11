package be.ibridge.kettle.trans.step.errorhandling;

import java.io.File;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.step.BaseStep;

public class FileErrorHandlerMissingFiles extends
		AbstractFileErrorHandler {

	public static final String THIS_FILE_DOES_NOT_EXIST = Messages.getString("FileErrorHandlerMissingFiles.FILE_DOES_NOT_EXIST"); //$NON-NLS-1$

	public static final String THIS_FILE_WAS_NOT_ACCESSIBLE = Messages.getString("FileErrorHandlerMissingFiles.FILE_WAS_NOT_ACCESSIBLE"); //$NON-NLS-1$

	public FileErrorHandlerMissingFiles(Date date,
			String destinationDirectory, String fileExtension, String encoding, BaseStep baseStep) {
		super(date, destinationDirectory, fileExtension, encoding, baseStep);
	}

	public void handleLineError(long lineNr, String filePart) {

	}

	public void handleNonExistantFile(File file) throws KettleException {
		handleFile(file);
		try {
			getWriter(NO_PARTS).write(THIS_FILE_DOES_NOT_EXIST);
			getWriter(NO_PARTS).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException(Messages.getString("FileErrorHandlerMissingFiles.Exception.CouldNotCreateNonExistantFile") //$NON-NLS-1$
					+ file.getPath(), e);
		}
	}

	public void handleNonAccessibleFile(File file ) throws KettleException {
		handleFile(file);
		try {
			getWriter(NO_PARTS).write(THIS_FILE_WAS_NOT_ACCESSIBLE);
			getWriter(NO_PARTS).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException(
					Messages.getString("FileErrorHandlerMissingFiles.Exception.CouldNotCreateNonAccessibleFile") + file.getPath(), //$NON-NLS-1$
					e);
		}
	}

}