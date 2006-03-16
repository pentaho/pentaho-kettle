package be.ibridge.kettle.trans.step.excelinput;

import jxl.Cell;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleException;

public class ExcelInputRowErrorHandler extends AbstractExcelInputErrorHandler {

	public ExcelInputRowErrorHandler(String destinationDirectory,
			String fileExtension, String encoding) {
		super(destinationDirectory, fileExtension, encoding);
	}

	public void handleLine(ExcelInputRow excelInputRow) throws KettleException {
		try {
			getWriter(excelInputRow.sheetName).write(
					toString(excelInputRow.cells));
			getWriter(excelInputRow.sheetName).write(Const.CR);
		} catch (Exception e) {
			throw new KettleException("Could not create row error for: "
					+ excelInputRow, e);

		}
	}

	private String toString(Cell[] cells) {
		StringBuffer buffer = new StringBuffer();
		if(cells != null && cells.length != 0)
		{
			buffer.append(cells[0].getContents());
			for (int i = 1; i < cells.length; i++) {
				buffer.append('\t');
				buffer.append(cells[i].getContents());
			}
		}
		return buffer.toString();
	}
}
