package be.ibridge.kettle.trans.step.excelinput;

import java.util.Iterator;
import java.util.List;

import be.ibridge.kettle.core.exception.KettleException;

public class CompositeExcelInputErrorHandler implements ExcelInputErrorHandler {
	private List handlers;

	public CompositeExcelInputErrorHandler(List handlers) {
		super();
		this.handlers = handlers;
	}

	public void handleFile(String filename) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			ExcelInputErrorHandler handler = (ExcelInputErrorHandler) iter
					.next();
			handler.handleFile(filename);
		}
	}

	public void handleLine(ExcelInputRow excelInputRow) throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			ExcelInputErrorHandler handler = (ExcelInputErrorHandler) iter
					.next();
			handler.handleLine(excelInputRow);
		}
	}

	public void close() throws KettleException {
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			ExcelInputErrorHandler handler = (ExcelInputErrorHandler) iter
					.next();
			handler.close();
		}
	}
}
