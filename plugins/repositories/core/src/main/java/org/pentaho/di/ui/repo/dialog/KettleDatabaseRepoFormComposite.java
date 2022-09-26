/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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
 ***************************************************************************** */

package org.pentaho.di.ui.repo.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;

import java.util.ArrayList;
import java.util.Map;

public class KettleDatabaseRepoFormComposite extends BaseRepoFormComposite {

  private Combo dbListCombo;
  private Runnable dbListRefresh;
  private static final Class<?> PKG = KettleDatabaseRepoFormComposite.class;

  public KettleDatabaseRepoFormComposite( Composite parent, int style ) {
    super( parent, style );
  }

  @SuppressWarnings( "squid:S3776" )
  @Override
  protected Control uiAfterDisplayName() {
    PropsUI props = PropsUI.getInstance();

    Label lLoc = new Label( this, SWT.NONE );
    lLoc.setText( BaseMessages.getString( PKG, "repositories.create.label" ) );
    lLoc.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDisplayName, CONTROL_MARGIN ).result() );
    props.setLook( lLoc );

    dbListCombo = new Combo( this, SWT.READ_ONLY );
    if ( RepositoryConnectController.getInstance().getDatabases().isEmpty() ) {
      // can implement for empty db dialog
    } else {
      ArrayList<String> listData = convertJSONArrayToList( RepositoryConnectController.getInstance().getDatabases() );
      dbListCombo.setItems( listData.toArray( new String[ listData.size() ] ) );
    }

    dbListCombo.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( lLoc, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    dbListCombo.addModifyListener( lsMod );
    props.setLook( dbListCombo );


    Button createDbConBtn = new Button( this, SWT.PUSH );
    createDbConBtn.setText( BaseMessages.getString( PKG, "repositories.create.label" ) );
    createDbConBtn.setLayoutData(
      new FormDataBuilder().left( dbListCombo, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( createDbConBtn );

    Button updateDbConBtn = new Button( this, SWT.PUSH );
    updateDbConBtn.setText( BaseMessages.getString( PKG, "repositories.edit.label" ) );
    updateDbConBtn.setLayoutData(
      new FormDataBuilder().left( createDbConBtn, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( updateDbConBtn );

    updateDbConBtn.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        if ( dbListCombo.getSelectionIndex() > -1 ) {
          if ( !Utils.isEmpty( dbListCombo.getItem( dbListCombo.getSelectionIndex() ) ) ) {
            RepositoryConnectController.getInstance()
              .editDatabaseConnection( ( dbListCombo.getItem( dbListCombo.getSelectionIndex() ) ) );
          } else {
            messageBoxService( " Select a DB connection to edit!" );
          }
        } else {
          messageBoxService( " Select a DB connection to edit!" );
        }
        dbListRefresh.run();
      }
    } );

    Button deleteDbConBtn = new Button( this, SWT.PUSH );
    deleteDbConBtn.setText( BaseMessages.getString( PKG, "repositories.delete.label" ) );
    deleteDbConBtn.setLayoutData(
      new FormDataBuilder().left( updateDbConBtn, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( deleteDbConBtn );

    deleteDbConBtn.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {

        if ( dbListCombo.getSelectionIndex() > -1 ) {
          if ( !Utils.isEmpty( dbListCombo.getItem( dbListCombo.getSelectionIndex() ) ) ) {
            String deleteMessage = String.format( "Are you sure you wish to remove the '%s' DB connection?", dbListCombo.getItem( dbListCombo.getSelectionIndex() ) );
            MessageBox delBox = new MessageBox( getParent().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO );
            delBox.setText( BaseMessages.getString( PKG, "repositories.deldbconn.label" ) );
            delBox.setMessage( deleteMessage );
            if ( SWT.YES == delBox.open() ){
              RepositoryConnectController.getInstance()
                  .deleteDatabaseConnection( dbListCombo.getItem( dbListCombo.getSelectionIndex() ) );
            }
          } else {
            messageBoxService( " Select a DB connection to delete!" );
          }
        } else {
          messageBoxService( " Select a DB connection to delete!" );
        }
        dbListRefresh.run();
      }
    } );

    createDbConBtn.addSelectionListener( new SelectionAdapter() {
                                           @Override public void widgetSelected( SelectionEvent selectionEvent ) {
                                             RepositoryConnectController.getInstance().createConnection();
                                             dbListRefresh.run();
                                           }
                                         }
    );

    dbListRefresh = () -> {
      ArrayList<String> listData = convertJSONArrayToList( RepositoryConnectController.getInstance().getDatabases() );
      dbListCombo.setItems( listData.toArray( new String[ listData.size() ] ) );
    };
    return createDbConBtn;
  }

  private ArrayList<String> convertJSONArrayToList( JSONArray jsonArray ) {
    ArrayList<String> listData = new ArrayList<>();
    if ( !jsonArray.isEmpty() ) {
      for ( int i = 0; i < jsonArray.size(); i++ ) {
        listData.add( ( (JSONObject) jsonArray.get( i ) ).get( "name" ).toString() );
      }
    }
    return listData;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> ret = super.toMap();
    ret.put( BaseRepositoryMeta.ID, "KettleDatabaseRepository" );
    ret.put( KettleDatabaseRepositoryMeta.DATABASE_CONNECTION, dbListCombo.getItem( dbListCombo.getSelectionIndex() ) );
    return ret;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public void populate( JSONObject source ) {
    super.populate( source );
    dbListCombo.setText( (String) source.getOrDefault( KettleDatabaseRepositoryMeta.DATABASE_CONNECTION, "" ) );
  }

  @Override
  protected boolean validateSaveAllowed() {
    return super.validateSaveAllowed() && ( dbListCombo.getSelectionIndex() > -1 ) && !Utils.isEmpty(
      dbListCombo.getSelection().toString() );
  }

  private MessageBox messageBoxService( String msgText ) {
    MessageBox messageBox = new MessageBox( getParent().getShell(), SWT.OK |
      SWT.ICON_ERROR );
    messageBox.setMessage( msgText );
    messageBox.open();
    return messageBox;
  }
}