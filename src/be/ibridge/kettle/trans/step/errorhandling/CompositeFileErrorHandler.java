package be.ibridge.kettle.trans.step.errorhandling;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleException;

public class CompositeFileErrorHandler implements FileErrorHandler
{
	private List handlers;

	public CompositeFileErrorHandler(List handlers)
	{
		super();
		this.handlers = handlers;
	}

	public void handleFile(File file) throws KettleException
	{
		for (Iterator iter = handlers.iterator(); iter.hasNext();)
		{
			FileErrorHandler handler = (FileErrorHandler) iter.next();
			handler.handleFile(file);
		}
	}

	public void handleLineError(long lineNr, String filePart) throws KettleException
	{
		for (Iterator iter = handlers.iterator(); iter.hasNext();)
		{
			FileErrorHandler handler = (FileErrorHandler) iter.next();
			handler.handleLineError(lineNr, filePart);
		}
	}

	public void close() throws KettleException
	{
		for (Iterator iter = handlers.iterator(); iter.hasNext();)
		{
			FileErrorHandler handler = (FileErrorHandler) iter.next();
			handler.close();
		}
	}

	public void handleNonExistantFile(File file) throws KettleException
	{
		for (Iterator iter = handlers.iterator(); iter.hasNext();)
		{
			FileErrorHandler handler = (FileErrorHandler) iter.next();
			handler.handleNonExistantFile(file);
		}
	}

	public void handleNonAccessibleFile(File file) throws KettleException
	{
		for (Iterator iter = handlers.iterator(); iter.hasNext();)
		{
			FileErrorHandler handler = (FileErrorHandler) iter.next();
			handler.handleNonAccessibleFile(file);
		}
	}
}
