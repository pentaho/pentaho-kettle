package be.ibridge.kettle.trans.step.textfileinput;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleException;

public class CompositeTextFileErrorHandler implements
		TextFileErrorHandler {
	private List handlers;

	public CompositeTextFileErrorHandler(List handlers) {
		super();
		this.handlers = handlers;
	}

	public void handleFile(File file) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileErrorHandler handler = (TextFileErrorHandler) iter
					.next();
			handler.handleFile(file);
		}
	}

	public void handleLineError(TextFileLine textFileLine) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileErrorHandler handler = (TextFileErrorHandler) iter
					.next();
			handler.handleLineError(textFileLine);
		}
	}

	public void close() throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileErrorHandler handler = (TextFileErrorHandler) iter
					.next();
			handler.close();
		}
	}

	public void handleNonExistantFile(File file) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileErrorHandler handler = (TextFileErrorHandler) iter
					.next();
			handler.handleNonExistantFile(file);
		}
	}

	public void handleNonAccessibleFile(File file) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileErrorHandler handler = (TextFileErrorHandler) iter
					.next();
			handler.handleNonAccessibleFile(file);
		}
	}
}
