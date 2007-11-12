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

import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.exception.KettleException;

public class CompositeFileErrorHandler implements FileErrorHandler
{
	private List<FileErrorHandler> handlers;

	public CompositeFileErrorHandler(List<FileErrorHandler> handlers)
	{
		super();
		this.handlers = handlers;
	}

	public void handleFile(FileObject file) throws KettleException
	{
		for (FileErrorHandler handler : handlers)
		{
			handler.handleFile(file);
		}
	}

	public void handleLineError(long lineNr, String filePart) throws KettleException
	{
		for (FileErrorHandler handler : handlers)
		{
			handler.handleLineError(lineNr, filePart);
		}
	}

	public void close() throws KettleException
	{
		for (FileErrorHandler handler : handlers)
		{
			handler.close();
		}
	}

	public void handleNonExistantFile(FileObject file) throws KettleException
	{
		for (FileErrorHandler handler : handlers)
		{
			handler.handleNonExistantFile(file);
		}
	}

	public void handleNonAccessibleFile(FileObject file) throws KettleException
	{
		for (FileErrorHandler handler : handlers)
		{
			handler.handleNonAccessibleFile(file);
		}
	}
}
