/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.salesforce;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.salesforce.SalesforceConnection;
import org.pentaho.di.trans.steps.salesforce.SalesforceStepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public abstract class SalesforceStepDialog extends BaseStepDialog implements StepDialogInterface {

  private static final Class<?> PKG = SalesforceStepMeta.class;
  protected static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'.000'Z";
  protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

  private final Class<? extends SalesforceStepMeta> META_CLASS;

  public SalesforceStepDialog( Shell parent, Object in, TransMeta transMeta, String sname ) {
    super( parent, (BaseStepMeta) in, transMeta, sname );
    META_CLASS = ( (SalesforceStepMeta) in ).getClass();
  }

  protected abstract void getInfo( SalesforceStepMeta meta ) throws KettleException;

  protected void test() {

    boolean successConnection = true;
    String msgError = null;
    SalesforceConnection connection = null;
    String realUsername = null;
    try {
      SalesforceStepMeta meta = META_CLASS.newInstance();
      getInfo( meta );

      // get real values
      String realURL = transMeta.environmentSubstitute( meta.getTargetURL() );
      realUsername = transMeta.environmentSubstitute( meta.getUsername() );
      String realPassword = Utils.resolvePassword( transMeta, meta.getPassword() );
      int realTimeOut = Const.toInt( transMeta.environmentSubstitute( meta.getTimeout() ), 0 );

      connection = new SalesforceConnection( log, realURL, realUsername, realPassword );
      connection.setTimeOut( realTimeOut );
      connection.connect();

    } catch ( Exception e ) {
      successConnection = false;
      msgError = e.getMessage();
    } finally {
      if ( connection != null ) {
        try {
          connection.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
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
