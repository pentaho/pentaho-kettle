package org.pentaho.di.ui.core.database.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class XulDatabaseExplorerDialog {

	private Shell shell;
	private XulDomContainer container;
	private XulRunner runner;
	private XulDatabaseExplorerController controller;
	private DatabaseMeta databaseMeta;
	private static Log logger = LogFactory.getLog(XulDatabaseExplorerDialog.class);
	private static final String XUL = "org/pentaho/di/ui/core/database/dialog/database_explorer.xul";

	public XulDatabaseExplorerDialog(Shell aShell, DatabaseMeta aDatabaseMeta) {
		this.shell = aShell;
		this.databaseMeta = aDatabaseMeta;
	}

	public void open() {
		try {

			SwtXulLoader theLoader = new SwtXulLoader();
			theLoader.setOuterContext(this.shell);
			this.container = theLoader.loadXul(XUL);

			this.controller = new XulDatabaseExplorerController(this.shell, this.databaseMeta);
			this.container.addEventHandler(this.controller);

			this.runner = new SwtXulRunner();
			this.runner.addContainer(this.container);
			this.runner.initialize();

			XulDialog theExplorerDialog = (XulDialog) this.container.getDocumentRoot().getElementById("databaseExplorerDialog");
			theExplorerDialog.show();

		} catch (Exception e) {
			logger.info(e);
		}
	}
}
