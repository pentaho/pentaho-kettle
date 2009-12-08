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

public class XulPreviewRowsDialog {

	private Shell shell;
	private int limit;
	private String table;
	private XulDomContainer container;
	private XulRunner runner;
	private XulPreviewRowsController controller;
	private DatabaseMeta databaseMeta;
	private static Log logger = LogFactory.getLog(XulPreviewRowsDialog.class);
	private static final String XUL = "org/pentaho/di/ui/core/database/dialog/preview_rows.xul";

	public XulPreviewRowsDialog(Shell aShell, int aStyle, DatabaseMeta aDatabaseMeta, String aTableName, int aLimit) {
		this.shell = aShell;
		this.limit = aLimit;
		this.table = aTableName;
		this.databaseMeta = aDatabaseMeta;
	}

	public void open() {
		try {
			SwtXulLoader theLoader = new SwtXulLoader();
			theLoader.setOuterContext(this.shell);
			this.container = theLoader.loadXul(XUL);

			this.controller = new XulPreviewRowsController(this.shell, this.databaseMeta, this.table, this.limit);
			this.container.addEventHandler(this.controller);

			this.runner = new SwtXulRunner();
			this.runner.addContainer(this.container);
			this.runner.initialize();

			XulDialog thePreviewDialog = (XulDialog) this.container.getDocumentRoot().getElementById("previewRowsDialog");
			thePreviewDialog.show();

		} catch (Exception e) {
			logger.info(e);
		}
	}
}
