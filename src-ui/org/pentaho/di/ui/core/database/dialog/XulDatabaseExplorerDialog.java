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

import java.util.List;

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
	private List<DatabaseMeta> databases;
	private static Log logger = LogFactory.getLog(XulDatabaseExplorerDialog.class);
	private static final String XUL = "org/pentaho/di/ui/core/database/dialog/database_explorer.xul";

	public XulDatabaseExplorerDialog(Shell aShell, DatabaseMeta aDatabaseMeta, List<DatabaseMeta> aDataBases) {
		this.shell = aShell;
		this.databaseMeta = aDatabaseMeta;
		this.databases = aDataBases;
	}

	public void open() {
		try {

			SwtXulLoader theLoader = new SwtXulLoader();
			theLoader.setOuterContext(this.shell);
			this.container = theLoader.loadXul(XUL);

			this.controller = new XulDatabaseExplorerController(this.shell, this.databaseMeta, this.databases);
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
