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

package org.pentaho.di.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TableView;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class ConfigurationDialog extends Dialog {

  protected AbstractMeta abstractMeta;
  protected ExecutionConfiguration configuration;
  protected TableView wArguments;
  protected TableView wVariables;
  protected TableView wParams;
  protected boolean retval;
  protected Shell shell;
  protected PropsUI props;
  protected Display display;
  protected Shell parent;
  protected Button wOK;
  protected Button wCancel;
  protected Group gLocal;
  protected Button wExecLocal;
  protected Button wExecRemote;
  protected Button wGatherMetrics;
  protected Label wlReplayDate;
  protected Label wlLogLevel;
  protected Group gDetails;
  protected CCombo wLogLevel;
  protected Button wSafeMode;
  protected Button wClearLog;
  protected Text wReplayDate;
  protected Label wlRemoteHost;
  protected CCombo wRemoteHost;
  protected Button wPassExport;
  protected Label wlArguments;
  protected Label wlParams;
  protected Label wlVariables;
  protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );

  public ConfigurationDialog( Shell parent, ExecutionConfiguration configuration, AbstractMeta meta ) {
    super( parent );
    this.parent = parent;
    this.configuration = configuration;
    this.abstractMeta = meta;
  }

  protected void getInfoArguments() {
    Map<String, String> map = new HashMap<String, String>();
    int nrNonEmptyArguments = wArguments.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyArguments; i++ ) {
      TableItem tableItem = wArguments.getNonEmpty( i );
      String varName = tableItem.getText( 1 );
      String varValue = tableItem.getText( 2 );

      if ( !Const.isEmpty( varName ) ) {
        map.put( varName, varValue );
      }
    }
    configuration.setArguments( map );
  }

  protected void getInfoVariables() {
    Map<String, String> map = new HashMap<String, String>();
    int nrNonEmptyVariables = wVariables.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyVariables; i++ ) {
      TableItem tableItem = wVariables.getNonEmpty( i );
      String varName = tableItem.getText( 1 );
      String varValue = tableItem.getText( 2 );

      if ( !Const.isEmpty( varName ) ) {
        map.put( varName, varValue );
      }
    }
    configuration.setVariables( map );
  }

  /**
   * Get the parameters from the dialog.
   */
  protected void getInfoParameters() {
    Map<String, String> map = new HashMap<String, String>();
    int nrNonEmptyVariables = wParams.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyVariables; i++ ) {
      TableItem tableItem = wParams.getNonEmpty( i );
      String paramName = tableItem.getText( 1 );
      String paramValue = tableItem.getText( 2 );
      String defaultValue = tableItem.getText( 3 );

      if ( Const.isEmpty( paramValue ) ) {
        paramValue = Const.NVL( defaultValue, "" );
      }

      map.put( paramName, paramValue );
    }
    configuration.setParams( map );
  }

  protected void ok() {
    if ( Const.isOSX() ) {
      // OSX bug workaround.
      //
      wVariables.applyOSXChanges();
      wParams.applyOSXChanges();
      wArguments.applyOSXChanges();
    }
    getInfo();
    retval = true;
    dispose();
  }

  private void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  protected void cancel() {
    dispose();
  }

  public abstract void getInfo();

  protected void getParamsData() {
    wParams.clearAll( false );
    ArrayList<String> paramNames = new ArrayList<String>( configuration.getParams().keySet() );
    Collections.sort( paramNames );

    for ( int i = 0; i < paramNames.size(); i++ ) {
      String paramName = paramNames.get( i );
      String paramValue = configuration.getParams().get( paramName );
      String defaultValue;
      try {
        defaultValue = abstractMeta.getParameterDefault( paramName );
      } catch ( UnknownParamException e ) {
        defaultValue = "";
      }

      TableItem tableItem = new TableItem( wParams.table, SWT.NONE );
      tableItem.setText( 1, paramName );
      tableItem.setText( 2, Const.NVL( paramValue, "" ) );
      tableItem.setText( 3, Const.NVL( defaultValue, "" ) );
    }
    wParams.removeEmptyRows();
    wParams.setRowNums();
    wParams.optWidth( true );
  }

  /**
   * @param configuration the configuration to set
   */
  public void setConfiguration( ExecutionConfiguration configuration ) {
    this.configuration = configuration;
  }
}
