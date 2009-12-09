package org.pentaho.di.ui.core.database.dialog;

import org.pentaho.ui.xul.XulEventSourceAdapter;

public class XulPreviewRowsModel extends XulEventSourceAdapter {

	private int limit;
	private String selectedTable;

	public XulPreviewRowsModel(String aTable, int aLimit) {
		this.limit = aLimit;
		this.selectedTable = aTable;
	}

	public String getSelectedTable() {
		return this.selectedTable;
	}

	public int getLimit() {
		return this.limit;
	}
}
