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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;

/**
 *
 * On page one we select the database connection name, the database type and the access type.
 *
 * @author Matt
 * @since 04-apr-2005
 */
public class CreateDatabaseWizardPage1 extends WizardPage {
  private static Class<?> PKG = CreateDatabaseWizard.class; // for i18n purposes, needed by Translator2!!

  private Label wlName;
  private Text wName;
  private FormData fdlName, fdName;

  private Label wlDBType;
  private List wDBType;
  private FormData fdlDBType, fdDBType;

  private Label wlAccType;
  private List wAccType;
  private FormData fdlAccType, fdAccType;

  private PropsUI props;
  private DatabaseMeta databaseMeta;
  private java.util.List<DatabaseMeta> databases;
  private Map<String, String> wDBIDtoNameMap = new HashMap<String, String>();

  public CreateDatabaseWizardPage1( String arg, PropsUI props, DatabaseMeta databaseMeta,
      java.util.List<DatabaseMeta> databases ) {
    super( arg );
    this.props = props;
    this.databaseMeta = databaseMeta;
    this.databases = databases;

    setTitle( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.DialogTitle" ) );
    setDescription( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.DialogMessage" ) );

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

    wlName = new Label( composite, SWT.RIGHT );
    wlName.setText( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.DBName.Label" ) );
    props.setLook( wlName );
    fdlName = new FormData();
    fdlName.left = new FormAttachment( 0, 0 );
    fdlName.top = new FormAttachment( 0, 0 );
    fdlName.right = new FormAttachment( middle, 0 );
    wlName.setLayoutData( fdlName );
    wName = new Text( composite, SWT.SINGLE | SWT.BORDER );
    props.setLook( wName );
    fdName = new FormData();
    fdName.left = new FormAttachment( middle, margin );
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );
    wName.addModifyListener( new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        setPageComplete( false );
      }
    } );

    wlDBType = new Label( composite, SWT.RIGHT );
    wlDBType.setText( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.DBType.Label" ) );
    props.setLook( wlDBType );
    fdlDBType = new FormData();
    fdlDBType.left = new FormAttachment( 0, 0 );
    fdlDBType.top = new FormAttachment( wName, margin );
    fdlDBType.right = new FormAttachment( middle, 0 );
    wlDBType.setLayoutData( fdlDBType );
    wDBType = new List( composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    props.setLook( wDBType );

    PluginRegistry registry = PluginRegistry.getInstance();

    java.util.List<PluginInterface> plugins = registry.getPlugins( DatabasePluginType.class );
    Collections.sort( plugins, new Comparator<PluginInterface>() {
      @Override
      public int compare( PluginInterface o1, PluginInterface o2 ) {
        return o1.getName().toUpperCase().compareTo( o2.getName().toUpperCase() );
      }
    } );

    for ( PluginInterface plugin : plugins ) {
      try {
        wDBType.add( plugin.getName() );
        wDBIDtoNameMap.put( plugin.getIds()[0], plugin.getName() );

      } catch ( Exception e ) {
        throw new RuntimeException( "Error creating class for: " + plugin, e );
      }
    }

    // Select a default: the first
    /*
     * if (databaseMeta.getDatabaseType() <= 0) { wDBType.select(0); } else {
     */
    int idx = wDBType.indexOf( wDBIDtoNameMap.get( databaseMeta.getPluginId() ) );
    if ( idx >= 0 ) {
      wDBType.select( idx );
    } else {
      wDBType.select( 0 );
    }

    // }

    fdDBType = new FormData();
    fdDBType.top = new FormAttachment( wName, margin );
    fdDBType.left = new FormAttachment( middle, margin );
    fdDBType.bottom = new FormAttachment( 80, 0 );
    fdDBType.right = new FormAttachment( 100, 0 );
    wDBType.setLayoutData( fdDBType );
    wDBType.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setAccessTypes();
        setPageComplete( false );
      }
    } );

    wlAccType = new Label( composite, SWT.RIGHT );
    wlAccType.setText( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.DBAccessType.Label" ) );
    props.setLook( wlAccType );
    fdlAccType = new FormData();
    fdlAccType.left = new FormAttachment( 0, 0 );
    fdlAccType.top = new FormAttachment( wDBType, margin );
    fdlAccType.right = new FormAttachment( middle, 0 );
    wlAccType.setLayoutData( fdlAccType );

    wAccType = new List( composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    props.setLook( wAccType );
    fdAccType = new FormData();
    fdAccType.top = new FormAttachment( wDBType, margin );
    fdAccType.left = new FormAttachment( middle, margin );
    fdAccType.bottom = new FormAttachment( 100, 0 );
    fdAccType.right = new FormAttachment( 100, 0 );
    wAccType.setLayoutData( fdAccType );
    wAccType.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        setPageComplete( false );
      }
    } );

    setAccessTypes();

    // set the composite as the control for this page
    setControl( composite );
  }

  public void setAccessTypes() {
    if ( wDBType.getSelectionCount() < 1 ) {
      return;
    }

    int[] acc = DatabaseMeta.getAccessTypeList( wDBType.getSelection()[0] );
    wAccType.removeAll();
    for ( int i = 0; i < acc.length; i++ ) {
      wAccType.add( DatabaseMeta.getAccessTypeDescLong( acc[i] ) );
    }
    // If nothing is selected: select the first item (mostly the native driver)
    if ( wAccType.getSelectionIndex() < 0 ) {
      wAccType.select( 0 );
    }
  }

  public boolean canFlipToNextPage() {
    String name = wName.getText() != null ? wName.getText().length() > 0 ? wName.getText() : null : null;
    String dbType = wDBType.getSelection().length == 1 ? wDBType.getSelection()[0] : null;
    String acType = wAccType.getSelection().length == 1 ? wAccType.getSelection()[0] : null;

    if ( name == null || dbType == null || acType == null ) {
      setErrorMessage( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.ErrorMessage.InvalidInput" ) );
      return false;
    }
    if ( name != null && DatabaseMeta.findDatabase( databases, name ) != null ) {
      setErrorMessage( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.ErrorMessage.DBNameExists",
        name.trim() ) );
      return false;
    } else {
      getDatabaseInfo();
      setErrorMessage( null );
      setMessage( BaseMessages.getString( PKG, "CreateDatabaseWizardPage1.Message.Next" ) );
      return true;
    }
  }

  public DatabaseMeta getDatabaseInfo() {
    if ( wName.getText() != null && wName.getText().length() > 0 ) {
      databaseMeta.setName( wName.getText() );
      databaseMeta.setDisplayName( wName.getText() );
    }

    String[] dbTypeSel = wDBType.getSelection();
    if ( dbTypeSel != null && dbTypeSel.length == 1 ) {
      databaseMeta.setDatabaseType( dbTypeSel[0] );
    }

    String[] accTypeSel = wAccType.getSelection();
    if ( accTypeSel != null && accTypeSel.length == 1 ) {
      databaseMeta.setAccessType( DatabaseMeta.getAccessType( accTypeSel[0] ) );
    }

    // Also, set the default port in case of JDBC:
    databaseMeta.setDBPort( String.valueOf( databaseMeta.getDefaultDatabasePort() ) );

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
    switch ( databaseMeta.getAccessType() ) {
      case DatabaseMeta.TYPE_ACCESS_OCI:
        nextPage = wiz.getPage( "oci" ); // OCI
        break;
      case DatabaseMeta.TYPE_ACCESS_PLUGIN:
        nextPage = wiz.getPage( databaseMeta.getPluginId() ); // e.g. SAPR3
        break;
      default: // Generic or Native
        if ( databaseMeta.getDatabaseInterface() instanceof GenericDatabaseMeta ) { // Generic
          nextPage = wiz.getPage( "generic" ); // generic
        } else { // Native
          nextPage = wiz.getPage( "jdbc" );
          if ( nextPage != null ) {
            // Set the port number...
            ( (CreateDatabaseWizardPageJDBC) nextPage ).setData();
          }
        }
        break;
    }

    return nextPage;
  }

  public boolean canPerformFinish() {
    return false;
  }
}
