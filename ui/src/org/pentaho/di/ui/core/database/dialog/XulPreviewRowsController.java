/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.database.dialog;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtTreeCell;
import org.pentaho.ui.xul.swt.tags.SwtTreeCol;
import org.pentaho.ui.xul.swt.tags.SwtTreeCols;

public class XulPreviewRowsController extends AbstractXulEventHandler {

	private Shell shell;
	private DatabaseMeta databaseMeta;
	private String schema;
	private String table;
	private int limit;
	private BindingFactory bf;
	private Binding rowCountBinding;
	private String rowCount;

	private static Log logger = LogFactory.getLog(XulStepFieldsController.class);

	public XulPreviewRowsController(Shell aShell, DatabaseMeta aDatabaseMeta, String aSchema, String aTable, int aLimit) {
		this.shell = aShell;
		this.databaseMeta = aDatabaseMeta;
		this.schema = aSchema;
		this.table = aTable;
		this.limit = aLimit;
		this.bf = new DefaultBindingFactory();
	}

	public void init() {
		createPreviewRows();

		this.bf.setDocument(super.document);
		this.bf.setBindingType(Type.ONE_WAY);
		this.rowCountBinding = this.bf.createBinding(this, "rowCount", "rowCountLabel", "value");
		fireBindings();
	}

	private void fireBindings() {
		try {
			this.rowCountBinding.fireSourceChanged();
		} catch (Exception e) {
			logger.info(e);
		}
	}

	private void createPreviewRows() {
		GetPreviewTableProgressDialog theProgressDialog = new GetPreviewTableProgressDialog(this.shell, this.databaseMeta, this.schema, this.table, this.limit);
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
				theCell = new SwtTreeCell(null);
				theCell.setLabel(theValue == null ? "" : theValue.toString());					
				theRow.addCell(theCell);
			}
		}

		// Adds table columns.
		SwtTreeCol theColumn = null;
		String[] theFieldNames = theProgressDialog.getRowMeta().getFieldNames();
		SwtTreeCols theColumns = new SwtTreeCols(null, thePreviewTable, null, null);
		for (int i = 0; i < theFieldNames.length; i++) {
			theColumn = new SwtTreeCol(null, null, null, null);
			theColumn.setWidth(100);
			theColumn.setLabel(theFieldNames[i]);
			theColumns.addColumn(theColumn);
		}
		thePreviewTable.setColumns(theColumns);
		thePreviewTable.update();

		setRowCount("Rows of step: " + this.table + " (" + theRowCount + " rows)");
	}

	public void accept() {
		XulDialog theDialog = (XulDialog) super.document.getElementById("previewRowsDialog");
		theDialog.setVisible(false);
	}

	public void setRowCount(String aRowCount) {
		this.rowCount = aRowCount;
	}

	public String getRowCount() {
		return this.rowCount;
	}

	public String getName() {
		return "previewRows";
	}
}
