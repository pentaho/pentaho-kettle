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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtButton;
import org.pentaho.ui.xul.swt.tags.SwtDialog;
import org.pentaho.ui.xul.util.XulDialogCallback;

@SuppressWarnings("unchecked")
public class XulDatabaseExplorerController extends AbstractXulEventHandler {

  private static final Class<?> PKG = XulDatabaseExplorerController.class;
  
	private XulDatabaseExplorerModel model;
	private Binding databaseTreeBinding;
	private Binding selectedTableBinding;
	// private Binding selectedSchemaBinding;
	private XulTree databaseTree;
	private XulButton expandCollapseButton;
	private XulLabel actionLabel;
	private BindingFactory bf;
	private Shell shell;
	private SwtDialog dbExplorerDialog;
	private DBCache dbcache;
	private List<DatabaseMeta> databases;
	private boolean isExpanded;
	private boolean isSplitSchemaAndTable;
	private boolean isJustLook;

	private static final String DATABASE_IMAGE = "ui/images/folder_connection.png";
	private static final String FOLDER_IMAGE = "ui/images/BOL.png";
	private static final String TABLE_IMAGE = "ui/images/table.png";
	private static final String EXPAND_ALL_IMAGE = "ui/images/ExpandAll.png";
	private static final String COLLAPSE_ALL_IMAGE = "ui/images/CollapseAll.png";

	private static final String STRING_TABLES = BaseMessages.getString(PKG, "DatabaseExplorerDialog.Tables.Label");
	private static final String STRING_VIEWS = BaseMessages.getString(PKG, "DatabaseExplorerDialog.Views.Label");

	private static Log logger = LogFactory.getLog(XulDatabaseExplorerController.class);

	public XulDatabaseExplorerController(Shell aShell, DatabaseMeta aMeta, List<DatabaseMeta> aDataBases, boolean aLook) {
		this.model = new XulDatabaseExplorerModel(aMeta);
		this.shell = aShell;
		this.bf = new DefaultBindingFactory();
		this.databases = aDataBases;
		this.dbcache = DBCache.getInstance();
		this.isJustLook = aLook;
	}

	public void init() {

		if (this.isJustLook) {
			SwtButton theAcceptButton = (SwtButton) this.document.getElementById("databaseExplorerDialog_accept");
			theAcceptButton.setVisible(false);
		}

		createDatabaseNodes();
		this.dbExplorerDialog = (SwtDialog) this.document.getElementById("databaseExplorerDialog");
		this.actionLabel = (XulLabel) document.getElementById("action_label");
		this.bf.setDocument(super.document);
		this.bf.setBindingType(Type.ONE_WAY);

		this.expandCollapseButton = (XulButton) document.getElementById("expandCollapseButton");
		this.databaseTree = (XulTree) document.getElementById("databaseTree");
		this.databaseTreeBinding = bf.createBinding(this.model, "database", this.databaseTree, "elements");

		BindingConvertor<DatabaseExplorerNode, String> theTableNameConvertor = new BindingConvertor<DatabaseExplorerNode, String>() {

			public String sourceToTarget(DatabaseExplorerNode aValue) {
				String theTable = null;
				if (aValue != null && aValue.isTable()) {
					theTable = aValue.getName();
				}
				return theTable;
			}

			public DatabaseExplorerNode targetToSource(String aValue) {
				return null;
			}
		};
		this.bf.createBinding(this.databaseTree, "selectedItem", this.model, "table", theTableNameConvertor);

		BindingConvertor<String, List<DatabaseExplorerNode>> theSelectedItemsConvertor = new BindingConvertor<String, List<DatabaseExplorerNode>>() {

			public String targetToSource(List<DatabaseExplorerNode> aValue) {
				return null;
			}

			public List<DatabaseExplorerNode> sourceToTarget(String aValue) {
				DatabaseExplorerNode theNode = XulDatabaseExplorerController.this.model.findBy(aValue);
				List<DatabaseExplorerNode> theResult = new ArrayList<DatabaseExplorerNode>();
				theResult.add(theNode);
				return theResult;
			}
		};
		this.selectedTableBinding = this.bf.createBinding(this.model, "table", this.databaseTree, "selectedItems", theSelectedItemsConvertor);
		// this.selectedSchemaBinding = this.bf.createBinding(this.model, "schema",
		// this.databaseTree, "selectedItems", theSelectedItemsConvertor);

		BindingConvertor<DatabaseExplorerNode, Boolean> isDisabledConvertor = new BindingConvertor<DatabaseExplorerNode, Boolean>() {
			public Boolean sourceToTarget(DatabaseExplorerNode value) {
				return !(value != null && value.isTable());
			}

			public DatabaseExplorerNode targetToSource(Boolean value) {
				return null;
			}
		};
		this.bf.createBinding(this.databaseTree, "selectedItem", "buttonMenuPopUp", "disabled", isDisabledConvertor);
		this.bf.createBinding(this.databaseTree, "selectedItem", actionLabel, "disabled", isDisabledConvertor);
		fireBindings();
	}

	public void setSplitSchemaAndTable(boolean aSplit) {
		this.isSplitSchemaAndTable = aSplit;
	}

	public boolean getSplitSchemaAndTable() {
		return this.isSplitSchemaAndTable;
	}

	public void setSelectedTable(String aTable) {
		this.model.setTable(aTable);
	}

	public String getSelectedTable() {
		return this.model.getTable();
	}

	public DatabaseMeta getDatabaseMeta() {
		return this.model.getDatabaseMeta();
	}

	public void setSelectedSchema(String aSchema) {
		this.model.setSchema(aSchema);
	}

	public String getSelectedSchema() {
		return this.model.getSchema();
	}

	public void accept() {
		this.dbExplorerDialog.setVisible(false);
	}

	public void cancel() {
		this.model.setTable(null);
		this.dbExplorerDialog.setVisible(false);
	}

	public void truncate() {
		SQLEditor theSqlEditor = new SQLEditor(this.dbExplorerDialog.getShell(), SWT.NONE, this.model.getDatabaseMeta(), this.dbcache, "-- TRUNCATE TABLE " + this.model.getTable());
		theSqlEditor.open();
	}

	public void viewSql() {
		SQLEditor theSqlEditor = new SQLEditor(this.dbExplorerDialog.getShell(), SWT.NONE, this.model.getDatabaseMeta(), this.dbcache, "SELECT * FROM " + this.model.getTable());
		theSqlEditor.open();
	}

	public void showLayout() {
		XulStepFieldsDialog theStepFieldsDialog = new XulStepFieldsDialog(this.shell, SWT.NONE, this.model.getDatabaseMeta(), this.model.getTable(), null);
		theStepFieldsDialog.open(false);
	}

	public void displayRowCount() {

		try {
			GetTableSizeProgressDialog pd = new GetTableSizeProgressDialog(this.shell, this.model.getDatabaseMeta(), this.model.getTable());
			Long theCount = pd.open();
			if (theCount != null) {
				XulMessageBox theMessageBox = (XulMessageBox) document.createElement("messagebox");
				theMessageBox.setModalParent(this.dbExplorerDialog.getShell());
				theMessageBox.setTitle(BaseMessages.getString(PKG,"DatabaseExplorerDialog.TableSize.Title"));
				theMessageBox.setMessage(BaseMessages.getString(PKG,"DatabaseExplorerDialog.TableSize.Message", this.model.getTable(), theCount.toString()));
				theMessageBox.open();
			}
		} catch (XulException e) {
			logger.error(e);
		}
	}

	private void fireBindings() {
		try {
			this.databaseTreeBinding.fireSourceChanged();
			if (this.getSelectedTable() != null) {
				this.selectedTableBinding.fireSourceChanged();
			}
			// if (this.getSelectedSchema() != null) {
			// this.selectedSchemaBinding.fireSourceChanged();
			// }
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
			@SuppressWarnings("unused")
      boolean execute = true;
			if (askLimit) {
				XulPromptBox thePromptBox = (XulPromptBox) this.document.createElement("promptbox");
				thePromptBox.setModalParent(this.dbExplorerDialog.getShell());
				thePromptBox.setTitle("Enter Max Rows");
				thePromptBox.setMessage("Max Rows:");
				thePromptBox.addDialogCallback(theCallback);
				thePromptBox.open();
				execute = theCallback.getLimit() != -1;
			}

//			if (execute) {
//				XulPreviewRowsDialog thePreviewRowsDialog = new XulPreviewRowsDialog(this.shell, SWT.NONE, this.model.getDatabaseMeta(), this.model.getTable(), theCallback.getLimit());
//				thePreviewRowsDialog.open();
//			}
			
			GetPreviewTableProgressDialog pd = new GetPreviewTableProgressDialog(shell, this.model.getDatabaseMeta(), this.model.getTable(), theCallback.getLimit());
      List<Object[]> rows = pd.open();
      if (rows!=null) // otherwise an already shown error...
      {
      if (rows.size()>0)
      {
        PreviewRowsDialog prd = new PreviewRowsDialog(shell, this.model.getDatabaseMeta(), SWT.None, this.model.getTable(), pd.getRowMeta(), rows);
        prd.open();
      }
      else
      {
        MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        mb.setMessage(BaseMessages.getString(PKG,"DatabaseExplorerDialog.NoRows.Message"));
        mb.setText(BaseMessages.getString(PKG,"DatabaseExplorerDialog.NoRows.Title"));
        mb.open();
      }
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
			Database theDatabase = new Database(null, this.model.getDatabaseMeta());
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
	
	public void close() {
		this.dbExplorerDialog.setVisible(false);
	}

	public void expandCollapse() {
		if (this.isExpanded) {
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

	public void getDDL() {
		Database db = new Database(null, this.model.getDatabaseMeta());
		try {
			db.connect();
			RowMetaInterface r = db.getTableFields(this.model.getTable());
			String sql = db.getCreateTableStatement(this.model.getTable(), r, null, false, null, true);
			SQLEditor se = new SQLEditor(this.dbExplorerDialog.getShell(), SWT.NONE, this.model.getDatabaseMeta(), this.dbcache, sql);
			se.open();
		} catch (KettleDatabaseException dbe) {
			new ErrorDialog(this.dbExplorerDialog.getShell(), BaseMessages.getString(PKG, "Dialog.Error.Header"), BaseMessages.getString(PKG,  "DatabaseExplorerDialog.Error.RetrieveLayout"), dbe);
		} finally {
			db.disconnect();
		}
	}

	public void getDDLForOther() {

		if (databases != null) {
			Database db = new Database(null, this.model.getDatabaseMeta());
			try {
				db.connect();

				RowMetaInterface r = db.getTableFields(this.model.getTable());

				// Now select the other connection...

				// Only take non-SAP R/3 connections....
				List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>();
				for (int i = 0; i < databases.size(); i++) {
					if (((databases.get(i)).getDatabaseInterface().isExplorable())) {
						dbs.add(databases.get(i));
					}
				}

				String conn[] = new String[dbs.size()];
				for (int i = 0; i < conn.length; i++)
					conn[i] = (dbs.get(i)).getName();

				EnterSelectionDialog esd = new EnterSelectionDialog(this.dbExplorerDialog.getShell(), conn, BaseMessages.getString(PKG,  "DatabaseExplorerDialog.TargetDatabase.Title"), BaseMessages.getString(PKG,  "DatabaseExplorerDialog.TargetDatabase.Message"));
				String target = esd.open();
				if (target != null) {
					DatabaseMeta targetdbi = DatabaseMeta.findDatabase(dbs, target);
					Database targetdb = new Database(null, targetdbi);

					String sql = targetdb.getCreateTableStatement(this.model.getTable(), r, null, false, null, true);
					SQLEditor se = new SQLEditor(this.dbExplorerDialog.getShell(), SWT.NONE, this.model.getDatabaseMeta(), this.dbcache, sql);
					se.open();
				}
			} catch (KettleDatabaseException dbe) {
				new ErrorDialog(this.dbExplorerDialog.getShell(), BaseMessages.getString(PKG, "Dialog.Error.Header"), BaseMessages.getString(PKG, "DatabaseExplorerDialog.Error.GenDDL"), dbe);
			} finally {
				db.disconnect();
			}
		} else {
			MessageBox mb = new MessageBox(this.dbExplorerDialog.getShell(), SWT.NONE | SWT.ICON_INFORMATION);
			mb.setMessage(BaseMessages.getString(PKG, "DatabaseExplorerDialog.NoConnectionsKnown.Message"));
			mb.setText(BaseMessages.getString(PKG, "DatabaseExplorerDialog.NoConnectionsKnown.Title"));
			mb.open();
		}
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

	class SelectedItemsConvertor extends BindingConvertor<String, List<DatabaseExplorerNode>> {

		public String targetToSource(List<DatabaseExplorerNode> aValue) {
			return null;
		}

		public List<DatabaseExplorerNode> sourceToTarget(String aValue) {
			DatabaseExplorerNode theNode = XulDatabaseExplorerController.this.model.findBy(aValue);
			List<DatabaseExplorerNode> theResult = new ArrayList<DatabaseExplorerNode>();
			theResult.add(theNode);
			return theResult;
		}
	}
}
