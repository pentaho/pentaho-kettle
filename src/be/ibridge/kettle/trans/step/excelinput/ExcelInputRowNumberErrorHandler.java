package be.ibridge.kettle.trans.step.excelinput;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class ExcelInputRowNumberErrorHandler extends AbstractExcelInputErrorHandler {

	public ExcelInputRowNumberErrorHandler(String destinationDirectory, String fileExtension, String encoding) {
		super(destinationDirectory, fileExtension, encoding);
	}

	public void handleLine(ExcelInputRow excelInputRow) throws KettleException {
		try {
			getWriter(excelInputRow.sheetName).write(String.valueOf(excelInputRow.rownr));
			getWriter(excelInputRow.sheetName).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create row number for: " + excelInputRow, e);

		}
	}

	
}
