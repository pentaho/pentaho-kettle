package org.pentaho.di.ui.core.database.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.XulDialogCallback;

@SuppressWarnings("unchecked")
public class XulDatabaseExplorerController extends AbstractXulEventHandler {

	private XulDatabaseExplorerModel model;
	private Binding databaseTreeBinding;
	private XulTree databaseTree;
	private BindingFactory bf;
	private Shell shell;

	private static Log logger = LogFactory.getLog(XulDatabaseExplorerController.class);

	public XulDatabaseExplorerController(Shell aShell, DatabaseMeta aMeta) {
		this.model = new XulDatabaseExplorerModel(aMeta);
		this.shell = aShell;
		this.bf = new DefaultBindingFactory();
	}

	public void init() {
		bf.setDocument(super.document);
		bf.setBindingType(Type.ONE_WAY);

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

		XulMenuList theActionsList = (XulMenuList) document.getElementById("actionsList");
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

	public void setCommand(Object aCommand) {

		if (aCommand.equals("Preview first 100")) {
			preview(false);
		}

		if (aCommand.equals("Preview x Rows")) {
			preview(true);
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
