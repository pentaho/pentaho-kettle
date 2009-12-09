package org.pentaho.di.ui.core.database.dialog;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtTreeCell;
import org.pentaho.ui.xul.swt.tags.SwtTreeCol;
import org.pentaho.ui.xul.swt.tags.SwtTreeCols;

public class XulPreviewRowsController extends AbstractXulEventHandler {

	private Shell shell;
	private DatabaseMeta databaseMeta;
	private String table;
	private int limit;

	public XulPreviewRowsController(Shell aShell, DatabaseMeta aDatabaseMeta, String aTable, int aLimit) {
		this.shell = aShell;
		this.databaseMeta = aDatabaseMeta;
		this.table = aTable;
		this.limit = aLimit;
	}

	public void init() {

		GetPreviewTableProgressDialog theProgressDialog = new GetPreviewTableProgressDialog(this.shell, this.databaseMeta, this.table, this.limit);
		List<Object[]> thePreviewData = theProgressDialog.open();

		// Adds table rows.
		Object[] theObj = null;
		XulTreeRow theRow = null;
		Object theValue = null;
		SwtTreeCell theCell = null;
		int theRowCount = 0;

		XulTree thePreviewTable = (XulTree) super.document.getElementById("table_data");
		thePreviewTable.getRootChildren().removeAll();
		Iterator<Object[]> theItr = thePreviewData.iterator();
		while (theItr.hasNext()) {
			theObj = theItr.next();
			theRow = thePreviewTable.getRootChildren().addNewRow();
			theRowCount++;
			for (int i = 0; i < theObj.length; i++) {
				theValue = theObj[i];
				if (theValue != null) {
					theCell = new SwtTreeCell(null);
					theCell.setLabel(theValue.toString());
					theRow.addCell(theCell);
				}
			}
		}

		// Adds table columns.
		SwtTreeCol theColumn = null;
		String[] theFieldNames = theProgressDialog.getRowMeta().getFieldNames();
		SwtTreeCols theColumns = new SwtTreeCols(null, thePreviewTable, null, null);
		for (int i = 0; i < theFieldNames.length; i++) {
			theColumn = new SwtTreeCol(null, null, null, null);
			theColumn.setLabel(theFieldNames[i]);
			theColumns.addColumn(theColumn);
		}
		thePreviewTable.setColumns(theColumns);
		thePreviewTable.update();

		XulLabel theCountLabel = (XulLabel) super.document.getElementById("rowCountLabel");
		theCountLabel.setValue("Rows of step: " + this.table + " (" + theRowCount + " rows)");

	}

	public String getName() {
		return "previewRows";
	}
}
