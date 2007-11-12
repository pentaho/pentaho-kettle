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

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;

/**
 * 
 * @author Johnny Vanhentenyk
 * 
 */
public interface FileErrorHandler {

	/**
	 * Tells the handler which file is being processed.
	 * 
	 * @param file
	 * @throws KettleException
	 */
	void handleFile(FileObject file) throws KettleException;

	/**
	 * This method handles an error when processing the line with corresponding
	 * lineNr.
	 * 
	 * @param lineNr
	 * @param filePart
	 *            allows us to split error according to a filePart
	 * @throws KettleException
	 */
	void handleLineError(long lineNr, String filePart) throws KettleException;

	/**
	 * This method closes the handler;
	 * 
	 */
	void close() throws KettleException;

	/**
	 * This method handles a file that is required, but does not exist.
	 * 
	 * @param file
	 * @throws KettleException
	 */
	void handleNonExistantFile(FileObject file) throws KettleException;

	/**
	 * This method handles a file that is required, but is not accessible.
	 * 
	 * @param file
	 * @throws KettleException
	 */
	void handleNonAccessibleFile(FileObject file) throws KettleException;
}
