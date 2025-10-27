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


package org.pentaho.di.ui.trans.steps.salesforce;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceStep;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepHelper;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.ArrayList;

public abstract class SalesforceStepDialog extends BaseStepDialog implements StepDialogInterface {

  private static final Class<?> PKG = SalesforceStepMeta.class;
  protected static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.000'Z";
  protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

  private final Class<? extends SalesforceStepMeta> META_CLASS;
  protected SalesforceStepMeta meta;

  public SalesforceStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    META_CLASS = ( (SalesforceStepMeta) in ).getClass();
    this.meta = (SalesforceStepMeta) in;
  }

  protected abstract void getInfo( SalesforceStepMeta meta ) throws KettleException;

  protected void test() {

    boolean successConnection = true;
    String msgError = null;
    String realUsername = null;
    try {
      Trans trans = new Trans( transMeta, null );
      trans.rowsets = new ArrayList<>();

      getInfo( meta );
      SalesforceStep step = (SalesforceStep) meta.getStep( stepMeta, meta.getStepData(), 0, transMeta, trans );
      step.setStepMetaInterface( meta );
      realUsername = transMeta.environmentSubstitute( meta.getUsername() );
      SalesforceStepHelper salesforceStepHelper = (SalesforceStepHelper) meta.getStepHelperInterface();
      successConnection = salesforceStepHelper.testConnection( transMeta );
    } catch ( Exception e ) {
      successConnection = false;
      msgError = e.getMessage();
    }

    if ( successConnection ) {

      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "SalesforceDialog.Connected.OK", realUsername )
        + Const.CR );
      mb.setText( BaseMessages.getString( PKG, "SalesforceDialog.Connected.Title.Ok" ) );
      mb.open();
    } else {
      new ErrorDialog(
        shell,
        BaseMessages.getString( PKG, "SalesforceDialog.Connected.Title.Error" ),
        BaseMessages.getString( PKG, "SalesforceDialog.Connected.NOK", realUsername ),
        new Exception( msgError ) );
    }
  }
}
