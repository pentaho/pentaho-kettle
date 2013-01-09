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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swt.SwtXulLoader;
import org.pentaho.ui.xul.swt.SwtXulRunner;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class XulStepFieldsDialog {

	private Shell shell;
	private String schemaTableCombo;
	private XulDomContainer container;
	private XulRunner runner;
	private XulStepFieldsController controller;
	private DatabaseMeta databaseMeta;
	private RowMetaInterface rowMeta;
	private static Log logger = LogFactory.getLog(XulStepFieldsDialog.class);
	private static final String XUL = "org/pentaho/di/ui/core/database/dialog/step_fields.xul";

    public XulStepFieldsDialog(Shell aShell, int aStyle, DatabaseMeta aDatabaseMeta, String aTableName, RowMetaInterface anInput, String schemaName) {
      this.shell = aShell;
      this.schemaTableCombo = aDatabaseMeta.getQuotedSchemaTableCombination(schemaName, aTableName);
      this.databaseMeta = aDatabaseMeta;
      this.rowMeta = anInput;
    }

	public void open(boolean isAcceptButtonHidden) {
		try {
			SwtXulLoader theLoader = new SwtXulLoader();
			theLoader.setOuterContext(this.shell);
			theLoader.setSettingsManager(XulSpoonSettingsManager.getInstance());
			this.container = theLoader.loadXul(XUL);

			this.controller = new XulStepFieldsController(this.shell, this.databaseMeta, this.schemaTableCombo, this.rowMeta);
			this.controller.setShowAcceptButton(isAcceptButtonHidden);
			this.container.addEventHandler(this.controller);

			this.runner = new SwtXulRunner();
			this.runner.addContainer(this.container);
			this.runner.initialize();

			XulDialog thePreviewDialog = (XulDialog) this.container.getDocumentRoot().getElementById("stepFieldsDialog");
			thePreviewDialog.show();
      ((SwtDialog)thePreviewDialog).dispose();
		} catch (Exception e) {
			logger.info(e);
		}
	}

	public String getSelectedStep() {
		return this.controller.getSelectedStep();
	}
}
