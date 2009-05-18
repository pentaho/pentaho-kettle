/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.step.errorhandling;

import java.util.Date;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStep;

public class FileErrorHandlerMissingFiles extends
		AbstractFileErrorHandler {

	private static Class<?> PKG = FileErrorHandlerMissingFiles.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String THIS_FILE_DOES_NOT_EXIST = BaseMessages.getString(PKG, "FileErrorHandlerMissingFiles.FILE_DOES_NOT_EXIST"); //$NON-NLS-1$

	public static final String THIS_FILE_WAS_NOT_ACCESSIBLE = BaseMessages.getString(PKG, "FileErrorHandlerMissingFiles.FILE_WAS_NOT_ACCESSIBLE"); //$NON-NLS-1$

	public FileErrorHandlerMissingFiles(Date date,
			String destinationDirectory, String fileExtension, String encoding, BaseStep baseStep) {
		super(date, destinationDirectory, fileExtension, encoding, baseStep);
	}

	public void handleLineError(long lineNr, String filePart) {

	}

	public void handleNonExistantFile(FileObject file) throws KettleException {
		handleFile(file);
		try {
			getWriter(NO_PARTS).write(THIS_FILE_DOES_NOT_EXIST);
			getWriter(NO_PARTS).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "FileErrorHandlerMissingFiles.Exception.CouldNotCreateNonExistantFile") //$NON-NLS-1$
					+ file.getName().getURI(), e);
		}
	}

	public void handleNonAccessibleFile(FileObject file ) throws KettleException {
		handleFile(file);
		try {
			getWriter(NO_PARTS).write(THIS_FILE_WAS_NOT_ACCESSIBLE);
			getWriter(NO_PARTS).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException(
					BaseMessages.getString(PKG, "FileErrorHandlerMissingFiles.Exception.CouldNotCreateNonAccessibleFile") + file.getName().getURI(), //$NON-NLS-1$
					e);
		}
	}

}