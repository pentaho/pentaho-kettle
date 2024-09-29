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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;

import java.util.Map;

public class KettleFileRepoFormComposite extends BaseRepoFormComposite {

  private Text txtLocation;
  private Button showHidden;
  private Button doNotModify;
  private static final Class<?> PKG = KettleFileRepoFormComposite.class;


  public KettleFileRepoFormComposite( Composite parent, int style ) {
    super( parent, style );
  }

  @Override
  protected Control uiAfterDisplayName() {
    PropsUI props = PropsUI.getInstance();

    Label lLoc = new Label( this, SWT.NONE );
    lLoc.setText( BaseMessages.getString( PKG, "repositories.location.label" ) );
    lLoc.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDisplayName, CONTROL_MARGIN ).result() );
    props.setLook( lLoc );

    txtLocation = new Text( this, SWT.BORDER );
    txtLocation.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( lLoc, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    txtLocation.addModifyListener( lsMod );
    props.setLook( txtLocation );

    Button browseBtn = new Button( this, SWT.PUSH );
    browseBtn.setText( BaseMessages.getString( PKG, "repositories.browse.label" ) );
    browseBtn.setLayoutData(
      new FormDataBuilder().left( txtLocation, LABEL_CONTROL_MARGIN ).top( lLoc, LABEL_CONTROL_MARGIN ).result() );
    props.setLook( browseBtn );


    browseBtn.addSelectionListener( new SelectionAdapter() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {

        DirectoryDialog dialog = new DirectoryDialog( getShell() );
        dialog.setFilterPath( "c:\\" );
        String selectedDir = dialog.open();
        if ( !Utils.isEmpty( selectedDir ) ) {
          txtLocation.setText( selectedDir );
        } else {
          messageBoxService( BaseMessages.getString( PKG, "repositories.selectadirectory.label" ) );
        }
      }
    } );
    doNotModify = new Button( this, SWT.CHECK );
    doNotModify.setText( BaseMessages.getString( PKG, "repositories.donotmodify.label" ) );
    doNotModify.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( txtLocation, CONTROL_MARGIN ).result() );
    props.setLook( doNotModify );
    doNotModify.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
        lsMod.modifyText( null );
      }
    } );

    showHidden = new Button( this, SWT.CHECK );
    showHidden.setText( BaseMessages.getString( PKG, "repositories.showhidden.label" ) );
    showHidden.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( doNotModify, CONTROL_MARGIN ).result() );
    props.setLook( showHidden );
    showHidden.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
        lsMod.modifyText( null );
      }
    } );

    return showHidden;
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> ret = super.toMap();

    ret.put( BaseRepositoryMeta.ID, "KettleFileRepository" );
    ret.put( KettleFileRepositoryMeta.LOCATION, txtLocation.getText() );
    ret.put( KettleFileRepositoryMeta.SHOW_HIDDEN_FOLDERS, showHidden.getSelection() );
    ret.put( KettleFileRepositoryMeta.DO_NOT_MODIFY, doNotModify.getSelection() );

    return ret;
  }


  @SuppressWarnings( "unchecked" )
  @Override
  public void populate( JSONObject source ) {
    super.populate( source );
    txtLocation.setText( (String) source.getOrDefault( KettleFileRepositoryMeta.LOCATION, "" ) );
    showHidden.setSelection( (Boolean) source.getOrDefault( KettleFileRepositoryMeta.SHOW_HIDDEN_FOLDERS, false ) );
    doNotModify.setSelection( (Boolean) source.getOrDefault( KettleFileRepositoryMeta.DO_NOT_MODIFY, false ) );
  }

  @Override
  protected boolean validateSaveAllowed() {
    return super.validateSaveAllowed() && !Utils.isEmpty( txtLocation.getText() );
  }

  private void messageBoxService( String msgText ) {
    MessageBox messageBox = new MessageBox( getParent().getShell(), SWT.OK |
      SWT.ICON_ERROR );
    messageBox.setMessage( msgText );
    messageBox.open();
  }
}
