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


public class FileErrorHandlerContentLineNumber extends AbstractFileErrorHandler {
	private static Class<?> PKG = FileErrorHandlerContentLineNumber.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public FileErrorHandlerContentLineNumber(Date date,
			String destinationDirectory, String fileExtension, String encoding, BaseStep baseStep) {
		super(date, destinationDirectory, fileExtension, encoding, baseStep);
	}

	public void handleLineError(long lineNr, String filePart)
			throws KettleException {
		try {
			getWriter(filePart).write(String.valueOf(lineNr));
			getWriter(filePart).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "FileErrorHandlerContentLineNumber.Exception.CouldNotCreateWriteLine") + lineNr, //$NON-NLS-1$
					e);

		}
	}

	public void handleNonExistantFile(FileObject file ) {
	}

	public void handleNonAccessibleFile(FileObject file ) {
	}

}
