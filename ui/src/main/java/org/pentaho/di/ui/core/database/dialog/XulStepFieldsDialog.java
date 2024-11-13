/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.core.database.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
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
  private static Log logger = LogFactory.getLog( XulStepFieldsDialog.class );
  private static final String XUL = "org/pentaho/di/ui/core/database/dialog/step_fields.xul";

  public XulStepFieldsDialog( Shell aShell, int aStyle, DatabaseMeta aDatabaseMeta, String aTableName,
    RowMetaInterface anInput, String schemaName ) {
    this.shell = aShell;
    this.schemaTableCombo = aDatabaseMeta.getQuotedSchemaTableCombination( schemaName, aTableName );
    this.databaseMeta = aDatabaseMeta;
    this.rowMeta = anInput;
  }

  public void open( boolean isAcceptButtonHidden ) {
    try {
      KettleXulLoader theLoader = new KettleXulLoader();
      theLoader.setOuterContext( this.shell );
      theLoader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      this.container = theLoader.loadXul( XUL );

      this.controller =
        new XulStepFieldsController( this.shell, this.databaseMeta, this.schemaTableCombo, this.rowMeta );
      this.controller.setShowAcceptButton( isAcceptButtonHidden );
      this.container.addEventHandler( this.controller );

      this.runner = new SwtXulRunner();
      this.runner.addContainer( this.container );
      this.runner.initialize();

      XulDialog thePreviewDialog =
        (XulDialog) this.container.getDocumentRoot().getElementById( "stepFieldsDialog" );
      thePreviewDialog.show();
      ( (SwtDialog) thePreviewDialog ).dispose();
    } catch ( Exception e ) {
      logger.info( e );
    }
  }

  public String getSelectedStep() {
    return this.controller.getSelectedStep();
  }
}
