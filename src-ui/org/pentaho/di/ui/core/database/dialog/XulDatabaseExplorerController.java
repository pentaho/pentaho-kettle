/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 * 
 * Author: Ezequiel Cuellar
 */
package org.pentaho.di.ui.core.database.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.XulDialogCallback;

@SuppressWarnings("unchecked")
public class XulDatabaseExplorerController extends AbstractXulEventHandler {

	private XulDatabaseExplorerModel model;
	private Binding databaseTreeBinding;
	private XulTree databaseTree;
	private XulButton expandCollapseButton;
	private BindingFactory bf;
	private Shell shell;
	private boolean isExpanded = false;

	private static final String DATABASE_IMAGE = "ui/images/folder_connection.png";
	private static final String FOLDER_IMAGE = "ui/images/BOL.png";
	private static final String TABLE_IMAGE = "ui/images/table.png";
	private static final String EXPAND_ALL_IMAGE = "ui/images/ExpandAll.png";
	private static final String COLLAPSE_ALL_IMAGE = "ui/images/CollapseAll.png";

	private static final String STRING_TABLES = Messages.getString("DatabaseExplorerDialog.Tables.Label");
	private static final String STRING_VIEWS = Messages.getString("DatabaseExplorerDialog.Views.Label");

	private static Log logger = LogFactory.getLog(XulDatabaseExplorerController.class);

	public XulDatabaseExplorerController(Shell aShell, DatabaseMeta aMeta) {
		this.model = new XulDatabaseExplorerModel(aMeta);
		this.shell = aShell;
		this.bf = new DefaultBindingFactory();
	}

	public void init() {
		createDatabaseNodes();

		this.bf.setDocument(super.document);
		this.bf.setBindingType(Type.ONE_WAY);

		this.expandCollapseButton = (XulButton) document.getElementById("expandCollapseButton");
		this.databaseTree = (XulTree) document.getElementById("databaseTree");
		this.databaseTreeBinding = bf.createBinding(this.model, "database", this.databaseTree, "elements");

		BindingConvertor<DatabaseExplorerNode, String> theTableNameConvertor = new BindingConvertor<DatabaseExplorerNode, String>() {

			public String sourceToTarget(DatabaseExplorerNode value) {
				return value.getName();
			}

			public DatabaseExplorerNode targetToSource(String value) {
				return null;
			}
		};
		bf.createBinding(this.databaseTree, "selectedItem", this.model, "table", theTableNameConvertor);

		XulMenuList theActionsList = (XulMenuList) this.document.getElementById("actionsList");
		BindingConvertor<DatabaseExplorerNode, Boolean> isDisabledConvertor = new BindingConvertor<DatabaseExplorerNode, Boolean>() {
			public Boolean sourceToTarget(DatabaseExplorerNode value) {
				return !(value != null && value.isTable());
			}

			public DatabaseExplorerNode targetToSource(Boolean value) {
				return null;
			}
		};
		bf.createBinding(this.databaseTree, "selectedItem", "actionsList", "disabled", isDisabledConvertor);
		bf.createBinding(theActionsList, "selectedItem", this, "command");

		fireBindings();
	}

	public void accept() {
		this.cancel();
	}

	public void cancel() {
		XulDialog theDialog = (XulDialog) this.document.getElementById("databaseExplorerDialog");
		theDialog.setVisible(false);
	}

	public void setCommand(Object aCommand) {
		if (aCommand.equals("Preview first 100")) {
			preview(false);
		}

		if (aCommand.equals("Preview x Rows")) {
			preview(true);
		}
		if (aCommand.equals("Row Count")) {
			displayRowCount();
		}
		if (aCommand.equals("Show Layout")) {
			showLayout();
		}

	}

	private void showLayout() {
		XulStepFieldsDialog theStepFieldsDialog = new XulStepFieldsDialog(this.shell, SWT.NONE, this.model.getDatabaseMeta(), this.model.getTable());
		theStepFieldsDialog.open(false);
	}

	private void displayRowCount() {

		try {
			GetTableSizeProgressDialog pd = new GetTableSizeProgressDialog(this.shell, this.model.getDatabaseMeta(), this.model.getTable());
			Long theCount = pd.open();
			if (theCount != null) {
				XulMessageBox theMessageBox = (XulMessageBox) document.createElement("messagebox");
				theMessageBox.setTitle(Messages.getString("DatabaseExplorerDialog.TableSize.Title"));
				theMessageBox.setMessage(Messages.getString("DatabaseExplorerDialog.TableSize.Message", this.model.getTable(), theCount.toString()));
				theMessageBox.open();
			}
		} catch (XulException e) {
			logger.error(e);
		}
	}

	private void fireBindings() {
		try {
			this.databaseTreeBinding.fireSourceChanged();
		} catch (Exception e) {
			logger.info(e);
		}
	}

	public String getName() {
		return "dbexplorer";
	}

	public void preview(boolean askLimit) {
		try {
			PromptCallback theCallback = new PromptCallback();
			boolean execute = true;
			if (askLimit) {
				XulPromptBox thePromptBox = (XulPromptBox) document.createElement("promptbox");
				thePromptBox.setTitle("Enter Max Rows");
				thePromptBox.setMessage("Max Rows:");
				thePromptBox.addDialogCallback(theCallback);
				thePromptBox.open();
				execute = theCallback.getLimit() != -1;
			}

			if (execute) {
				XulPreviewRowsDialog thePreviewRowsDialog = new XulPreviewRowsDialog(this.shell, SWT.NONE, this.model.getDatabaseMeta(), this.model.getTable(), theCallback.getLimit());
				thePreviewRowsDialog.open();
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void refresh() {
		collapse();
		this.model.getDatabase().clear();
		createDatabaseNodes();
		fireBindings();
	}

	private void createDatabaseNodes() {
		try {
			Database theDatabase = new Database(this.model.getDatabaseMeta());
			theDatabase.connect();

			// Adds the main database node.
			DatabaseExplorerNode theDatabaseNode = new DatabaseExplorerNode();
			theDatabaseNode.setName(this.model.getDatabaseMeta().getName());
			theDatabaseNode.setImage(DATABASE_IMAGE);
			this.model.getDatabase().add(theDatabaseNode);

			// Adds the Tables database node.
			DatabaseExplorerNode theTablesNode = new DatabaseExplorerNode();
			theTablesNode.setName(STRING_TABLES);
			theTablesNode.setImage(FOLDER_IMAGE);
			theDatabaseNode.addChild(theTablesNode);

			// Adds the Views database node.
			DatabaseExplorerNode theViewsNode = new DatabaseExplorerNode();
			theViewsNode.setName(STRING_VIEWS);
			theViewsNode.setImage(FOLDER_IMAGE);
			theDatabaseNode.addChild(theViewsNode);

			// Adds the database tables.
			String[] theTableNames = theDatabase.getTablenames();
			DatabaseExplorerNode theTableNode = null;
			for (int i = 0; i < theTableNames.length; i++) {
				theTableNode = new DatabaseExplorerNode();
				theTableNode.setIsTable(true);
				theTableNode.setName(theTableNames[i]);
				theTableNode.setImage(TABLE_IMAGE);
				theTablesNode.addChild(theTableNode);
			}

			// Adds the database views.
			String[] theViewNames = theDatabase.getViews();
			DatabaseExplorerNode theViewNode = null;
			for (int i = 0; i < theViewNames.length; i++) {
				theViewNode = new DatabaseExplorerNode();
				theViewNode.setIsTable(true);
				theViewNode.setName(theViewNames[i]);
				theViewNode.setImage(TABLE_IMAGE);
				theViewsNode.addChild(theViewNode);
			}
		} catch (Exception e) {
			logger.info(e);
		}
	}

	public void expandCollapse() {
		if (isExpanded) {
			collapse();
		} else {
			expand();
		}
	}

	private void expand() {
		this.databaseTree.expandAll();
		this.isExpanded = true;
		this.expandCollapseButton.setImage(COLLAPSE_ALL_IMAGE);
	}

	private void collapse() {
		this.databaseTree.collapseAll();
		this.isExpanded = false;
		this.expandCollapseButton.setImage(EXPAND_ALL_IMAGE);
	}

	class PromptCallback implements XulDialogCallback {

		private int limit = -1;

		public void onClose(XulComponent aSender, Status aReturnCode, Object aRetVal) {
			if (aReturnCode == Status.ACCEPT) {
				try {
					this.limit = Integer.parseInt(aRetVal.toString());
				} catch (NumberFormatException e) {
					logger.equals(e);
				}
			}
		}

		public void onError(XulComponent aSenter, Throwable aThrowable) {
		}

		public int getLimit() {
			return this.limit;
		}
	}
}
