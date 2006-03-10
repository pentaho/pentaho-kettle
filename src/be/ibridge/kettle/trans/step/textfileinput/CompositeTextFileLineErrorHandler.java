package be.ibridge.kettle.trans.step.textfileinput;

import java.util.Iterator;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleException;

public class CompositeTextFileLineErrorHandler implements
		TextFileLineErrorHandler {
	private List handlers;

	public CompositeTextFileLineErrorHandler(List handlers) {
		super();
		this.handlers = handlers;
	}

	public void handleFile(String filename) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileLineErrorHandler handler = (TextFileLineErrorHandler) iter
					.next();
			handler.handleFile(filename);
		}
	}

	public void handleLine(TextFileLine textFileLine) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileLineErrorHandler handler = (TextFileLineErrorHandler) iter
					.next();
			handler.handleLine(textFileLine);
		}
	}

	public void close() throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			TextFileLineErrorHandler handler = (TextFileLineErrorHandler) iter
					.next();
			handler.close();
		}
	}
}
