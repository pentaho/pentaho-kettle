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

package org.pentaho.test.ui.database;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.ui.database.DatabaseConnectionDialog;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulRoot;
import org.pentaho.ui.xul.containers.XulWindow;

public class DatabaseDialogHarness {

  DatabaseMeta database = null;

  public static void main( String[] args ) {

    try {
      KettleEnvironment.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
      System.exit( 1 );
    }
    DatabaseDialogHarness harness = new DatabaseDialogHarness();
    harness.showDialog();
  }

  private void showDialog() {

    XulDomContainer container = null;
    try {
      DatabaseConnectionDialog dcDialog = new DatabaseConnectionDialog();
      container = dcDialog.getSwtInstance( new Shell( SWT.NONE ) );
      if ( database != null ) {
        container.getEventHandler( "dataHandler" ).setData( database );
      }
    } catch ( XulException e ) {
      e.printStackTrace();
    }

    XulRoot root = (XulRoot) container.getDocumentRoot().getRootElement();
    if ( root instanceof XulDialog ) {
      ( (XulDialog) root ).show();
    }
    if ( root instanceof XulWindow ) {
      ( (XulWindow) root ).open();
    }

    try {
      database = (DatabaseMeta) container.getEventHandler( "dataHandler" ).getData();
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    String message = DatabaseDialogHarness.setMessage( database );
    Shell shell = new Shell( SWT.DIALOG_TRIM );
    shell.setLayout( new RowLayout() );
    Label label = new Label( shell, SWT.NONE );
    label.setText( message );
    Button button = new Button( shell, SWT.NONE );
    button.setText( "Edit Database ..." );

    button.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent event ) {
        try {
          showDialog();
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }
    } );
    shell.pack();
    shell.open();

    while ( !shell.isDisposed() ) {
      if ( !shell.getDisplay().readAndDispatch() ) {
        shell.getDisplay().sleep();
      }
    }
  }

  private static String setMessage( DatabaseMeta database ) {
    String message = "";
    if ( database != null ) {
      String carriageReturn = System.getProperty( "line.separator" );
      try {
        message =
          "Name: "
            .concat( database.getName() ).concat( carriageReturn ).concat( "Database Name: " ).concat(
              database.getDatabaseName() ).concat( carriageReturn ).concat( "Host Name: " ).concat(
              database.getHostname() ).concat( carriageReturn ).concat( "Port Number: " ).concat(
              database.getDatabasePortNumberString() ).concat( carriageReturn ).concat( "User Name: " )
            .concat( database.getUsername() ).concat( carriageReturn ).concat( "Password: " ).concat(
              database.getPassword() ).concat( carriageReturn ).concat( "Driver Class: " ).concat(
              database.getDriverClass() ).concat( carriageReturn ).concat( "URL: " ).concat(
              database.getURL() ).concat( carriageReturn );

        Iterator<String> keys = database.getExtraOptions().keySet().iterator();
        message = message.concat( carriageReturn ).concat( "Option Parameters:" ).concat( carriageReturn );
        while ( keys.hasNext() ) {
          String parameter = keys.next();
          String value = database.getExtraOptions().get( parameter );
          message =
            message.concat( carriageReturn ).concat( parameter ).concat( ": " ).concat( value ).concat(
              carriageReturn );
        }

        message =
          message
            .concat( carriageReturn ).concat( "SQL: " ).concat(
              database.getConnectSQL() != null ? database.getConnectSQL() : "" ).concat( carriageReturn )
            .concat( "Quote Identifiers: " ).concat( Boolean.toString( database.isQuoteAllFields() ) ).concat(
              carriageReturn ).concat( "Upper Case Identifiers: " ).concat(
              Boolean.toString( database.isForcingIdentifiersToUpperCase() ) ).concat( carriageReturn )
            .concat( "Lower Case Identifiers: " ).concat(
              Boolean.toString( database.isForcingIdentifiersToLowerCase() ) ).concat( carriageReturn );

        message =
          message.concat( carriageReturn ).concat( "Is Partitioned: " ).concat(
            Boolean.toString( database.isPartitioned() ) ).concat( carriageReturn );

        if ( database.isPartitioned() ) {
          PartitionDatabaseMeta[] partitions = database.getPartitioningInformation();
          if ( partitions != null ) {
            for ( int i = 0; i < partitions.length; i++ ) {
              PartitionDatabaseMeta pdm = partitions[i];
              message =
                message
                  .concat( carriageReturn ).concat( Integer.toString( i ) ).concat( ". ID: " ).concat(
                    pdm.getPartitionId() ).concat( ", Host: " ).concat( pdm.getHostname() )
                  .concat( ", Db: " ).concat( pdm.getDatabaseName() ).concat( ", Port: " ).concat(
                    pdm.getPort() ).concat( ", User: " ).concat( pdm.getUsername() ).concat( ", Pass: " )
                  .concat( pdm.getPassword() ).concat( carriageReturn );
            }
          }
        }
        Iterator<Object> poolKeys = database.getConnectionPoolingProperties().keySet().iterator();
        message = message.concat( carriageReturn ).concat( "Pooling Parameters:" ).concat( carriageReturn );
        while ( poolKeys.hasNext() ) {
          String parameter = (String) poolKeys.next();
          String value = database.getConnectionPoolingProperties().getProperty( parameter );
          message =
            message.concat( carriageReturn ).concat( parameter ).concat( ": " ).concat( value ).concat(
              carriageReturn );
        }

      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }
    return message;

  }

}
