/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.database.wizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.InformixDatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;

/**
 *
 * On page one we select the database connection JDBC settings 1) The servername 2) The port 3) The database name
 *
 * @author Matt
 * @since 04-apr-2005
 */
public class CreateDatabaseWizardPageJDBC extends WizardPage {
  private static final String DATA_SERVICES_PLUGIN_ID = "KettleThin";
  private static final String DEFAULT_WEB_APPLICATION_NAME = "pentaho";

  private static Class<?> PKG = CreateDatabaseWizard.class; // for i18n purposes, needed by Translator2!!

  private Label wlHostname;
  private Text wHostname;
  private FormData fdlHostname, fdHostname;

  private Label wlPort;
  private Text wPort;
  private FormData fdlPort, fdPort;

  private Label wlDBName;
  private Text wDBName;
  private FormData fdlDBName, fdDBName;

  private PropsUI props;
  private DatabaseMeta databaseMeta;

  private boolean defaultWebAppNameSet = false;

  public CreateDatabaseWizardPageJDBC( String arg, PropsUI props, DatabaseMeta info ) {
    super( arg );
    this.props = props;
    this.databaseMeta = info;

    setTitle( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.DialogTitle" ) );
    setDescription( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.DialogMessage" ) );

    setPageComplete( false );
  }

  public void createControl( Composite parent ) {
    int margin = Const.MARGIN;
    int middle = props.getMiddlePct();

    // create the composite to hold the widgets
    Composite composite = new Composite( parent, SWT.NONE );
    props.setLook( composite );

    FormLayout compLayout = new FormLayout();
    compLayout.marginHeight = Const.FORM_MARGIN;
    compLayout.marginWidth = Const.FORM_MARGIN;
    composite.setLayout( compLayout );

    // HOSTNAME
    wlHostname = new Label( composite, SWT.RIGHT );
    wlHostname.setText( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.Hostname.Label" ) );
    props.setLook( wlHostname );
    fdlHostname = new FormData();
    fdlHostname.top = new FormAttachment( 0, 0 );
    fdlHostname.left = new FormAttachment( 0, 0 );
    fdlHostname.right = new FormAttachment( middle, 0 );
    wlHostname.setLayoutData( fdlHostname );
    wHostname = new Text( composite, SWT.SINGLE | SWT.BORDER );
    props.setLook( wHostname );
    fdHostname = new FormData();
    fdHostname.top = new FormAttachment( 0, 0 );
    fdHostname.left = new FormAttachment( middle, margin );
    fdHostname.right = new FormAttachment( 100, 0 );
    wHostname.setLayoutData( fdHostname );
    wHostname.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        setPageComplete( false );
      }
    } );

    // PORT
    wlPort = new Label( composite, SWT.RIGHT );
    wlPort.setText( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.Port.Label" ) );
    props.setLook( wlPort );
    fdlPort = new FormData();
    fdlPort.top = new FormAttachment( wHostname, margin );
    fdlPort.left = new FormAttachment( 0, 0 );
    fdlPort.right = new FormAttachment( middle, 0 );
    wlPort.setLayoutData( fdlPort );
    wPort = new Text( composite, SWT.SINGLE | SWT.BORDER );
    props.setLook( wPort );
    wPort.setText( databaseMeta.getDatabasePortNumberString() );
    fdPort = new FormData();
    fdPort.top = new FormAttachment( wHostname, margin );
    fdPort.left = new FormAttachment( middle, margin );
    fdPort.right = new FormAttachment( 100, 0 );
    wPort.setLayoutData( fdPort );
    wPort.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        setPageComplete( false );
      }
    } );

    // DATABASE NAME
    wlDBName = new Label( composite, SWT.RIGHT );
    props.setLook( wlDBName );
    fdlDBName = new FormData();
    fdlDBName.top = new FormAttachment( wPort, margin );
    fdlDBName.left = new FormAttachment( 0, 0 );
    fdlDBName.right = new FormAttachment( middle, 0 );
    wlDBName.setLayoutData( fdlDBName );
    wDBName = new Text( composite, SWT.SINGLE | SWT.BORDER );
    props.setLook( wDBName );
    fdDBName = new FormData();
    fdDBName.top = new FormAttachment( wPort, margin );
    fdDBName.left = new FormAttachment( middle, margin );
    fdDBName.right = new FormAttachment( 100, 0 );
    wDBName.setLayoutData( fdDBName );
    wDBName.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent arg0 ) {
        setPageComplete( false );
      }
    } );

    // set the composite as the control for this page
    setControl( composite );
  }

  public void setData() {
    wHostname.setText( Const.NVL( databaseMeta.getHostname(), "" ) );
    wPort.setText( Const.NVL( databaseMeta.getDatabasePortNumberString(), "" ) );

    if ( !defaultWebAppNameSet && isDataServiceConnection() ) {
      wDBName.setText( DEFAULT_WEB_APPLICATION_NAME );
      defaultWebAppNameSet = true;
    } else {
      wDBName.setText( Const.NVL( databaseMeta.getDatabaseName(), "" ) );
    }

    if ( isDataServiceConnection() ) {
      wlDBName.setText( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.WebAppName.Label" ) );
    } else {
      wlDBName.setText( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.DBName.Label" ) );
    }
  }

  public boolean canFlipToNextPage() {
    String server = wHostname.getText().length() > 0 ? wHostname.getText() : null;
    String port = wPort.getText().length() > 0 ? wPort.getText() : null;
    String dbname = wDBName.getText().length() > 0 ? wDBName.getText() : null;

    if ( ( server == null || port == null || dbname == null ) && !isDataServiceConnection() ) {
      setErrorMessage( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.ErrorMessage.InvalidInput" ) );

      return false;
    }

    if ( ( server == null || port == null ) && isDataServiceConnection() ) {
      setErrorMessage( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.PDSHostPort.ErrorMessage" ) );

      return false;
    }

    getDatabaseInfo();
    setErrorMessage( null );
    setMessage( BaseMessages.getString( PKG, "CreateDatabaseWizardPageJDBC.Message.Input" ) );

    return true;
  }

  public DatabaseMeta getDatabaseInfo() {
    databaseMeta.setHostname( wHostname.getText() );
    databaseMeta.setDBPort( wPort.getText() );
    databaseMeta.setDBName( wDBName.getText() );

    return databaseMeta;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
   */
  public IWizardPage getNextPage() {
    IWizard wiz = getWizard();

    IWizardPage nextPage;
    if ( databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
      nextPage = wiz.getPage( "oracle" ); // Oracle
    } else if ( databaseMeta.getDatabaseInterface() instanceof InformixDatabaseMeta ) {
      nextPage = wiz.getPage( "ifx" ); // Informix
    } else {
      nextPage = wiz.getPage( "2" ); // page 2
    }

    return nextPage;
  }

  private boolean isDataServiceConnection() {
    return DATA_SERVICES_PLUGIN_ID.equals( databaseMeta.getPluginId() );
  }
}
