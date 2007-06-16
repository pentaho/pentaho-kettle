package org.pentaho.di.trans.steps.excelinput;

import jxl.Cell;

public class ExcelInputRow {

	public final String sheetName;
	public final int rownr;
	public final Cell[] cells;

	public ExcelInputRow(String sheetName, int rownr, Cell[] cells) {
		this.sheetName = sheetName;
		this.rownr = rownr;
		this.cells = cells;
	}

}
