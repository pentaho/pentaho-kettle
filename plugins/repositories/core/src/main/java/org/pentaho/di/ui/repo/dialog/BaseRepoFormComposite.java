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
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.BaseRepositoryMeta;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseRepoFormComposite extends Composite {

  protected static final int MEDIUM_WIDTH = 300;
  protected static final int LABEL_CONTROL_MARGIN = 5;
  protected static final int CONTROL_MARGIN = 15;


  // Passed by the outer control
  protected Button btnSave;
  protected Text txtDisplayName;
  protected Text txtDescription;
  protected Button chkDefault;
  protected String originalName;
  protected PropsUI props;
  private static final Class<?> PKG = BaseRepoFormComposite.class;
  protected boolean changed = false;
  protected ModifyListener lsMod = e -> {
    changed = true;
    setSaveButtonEnabled();
  };

  protected BaseRepoFormComposite( Composite parent, int style ) {
    super( parent, style );
    this.props = PropsUI.getInstance();

    props.setLook( this );

    FormLayout layout = new FormLayout();
    setLayout( layout );

    Label lDispName = new Label( this, SWT.NONE );
    lDispName.setText( BaseMessages.getString( PKG, "repositories.displayname.label" ) );
    lDispName.setLayoutData( new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).result() );
    props.setLook( lDispName );

    txtDisplayName = new Text( this, SWT.BORDER );
    props.setLook( txtDisplayName );
    txtDisplayName.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).top( lDispName, LABEL_CONTROL_MARGIN ).width( MEDIUM_WIDTH ).result() );
    txtDisplayName.addModifyListener( lsMod );

    Label lDescription = new Label( this, SWT.None );
    lDescription.setText( BaseMessages.getString( PKG, "repositories.description.label" ) );
    lDescription.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( uiAfterDisplayName(), CONTROL_MARGIN ).result() );
    props.setLook( lDescription );

    txtDescription = new Text( this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL );
    txtDescription.setLayoutData( new FormDataBuilder().left( 0, 0 ).top( lDescription, LABEL_CONTROL_MARGIN )
      .width( MEDIUM_WIDTH ).height( 100 ).result() );
    txtDescription.addModifyListener( lsMod );
    props.setLook( txtDescription );

    chkDefault = new Button( this, SWT.CHECK );
    chkDefault.setText( BaseMessages.getString( PKG, "repositories.launchonstartup.label" ) );
    chkDefault.setLayoutData(
      new FormDataBuilder().left( 0, 0 ).right( 100, 0 ).top( txtDescription, CONTROL_MARGIN ).result() );
    props.setLook( chkDefault );
    chkDefault.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( org.eclipse.swt.events.SelectionEvent e ) {
        lsMod.modifyText( null );
      }
    } );
  }

  protected Control uiAfterDisplayName() {
    return txtDisplayName;
  }

  @SuppressWarnings( "unchecked" )
  public void populate( JSONObject source ) {
    String displayName = (String) source.getOrDefault( BaseRepositoryMeta.DISPLAY_NAME, "" );
    txtDisplayName.setText( displayName );
    originalName = displayName; // Store the originalName (in case this is an edit)

    txtDescription.setText( (String) source.getOrDefault( BaseRepositoryMeta.DESCRIPTION, "" ) );
    chkDefault.setSelection( (Boolean) source.getOrDefault( BaseRepositoryMeta.IS_DEFAULT, false ) );
  }

  public void updateSaveButton( Button btnSave ) {
    this.btnSave = btnSave;
    changed = false;
    setSaveButtonEnabled();
  }

  protected void setSaveButtonEnabled() {
    if ( btnSave != null ) {
      btnSave.setEnabled( changed && validateSaveAllowed() );
    }
  }

  protected boolean validateSaveAllowed() {
    return !Utils.isEmpty( txtDisplayName.getText() );
  }

  public Map<String, Object> toMap() {

    Map<String, Object> res = new HashMap<>();
    if ( !Utils.isEmpty( originalName ) ) {
      res.put( "originalName", originalName );
    }
    res.put( BaseRepositoryMeta.DISPLAY_NAME, txtDisplayName.getText() );
    res.put( BaseRepositoryMeta.DESCRIPTION, txtDescription.getText() );
    res.put( BaseRepositoryMeta.IS_DEFAULT, chkDefault.getSelection() );
    return res;
  }
}
