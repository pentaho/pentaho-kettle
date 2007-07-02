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
