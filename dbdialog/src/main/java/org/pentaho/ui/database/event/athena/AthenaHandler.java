/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.ui.database.event.athena;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.AthenaDatabaseMeta;
import org.pentaho.di.core.database.AthenaDatabaseMeta.AuthType;
import org.pentaho.ui.database.event.DbInfoHandler;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulRadioGroup;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

/**
 * Data Handler for Athena-specific elements
 */
public class AthenaHandler extends AbstractXulEventHandler implements DbInfoHandler {
  private static final Logger log = LoggerFactory.getLogger( AthenaHandler.class );

  private static final String DEFAULT_AUTH_ID = "authentication-default";
  private static final String PROFILE_NAME_AUTH_ID = "authentication-profile-name";

  private Optional<XulTextbox> workGroupInput = Optional.empty();
  private Optional<XulTextbox> regionInput = Optional.empty();
  private Optional<XulTextbox> catalogInput = Optional.empty();
  private Optional<XulTextbox> outputLocationInput = Optional.empty();

  private Optional<XulRadioGroup> authSelector = Optional.empty();
  private Optional<XulDeck> authDeck = Optional.empty();
  private Optional<XulTextbox> profileNameInput = Optional.empty();

  @Override
  public void setXulDomContainer( XulDomContainer xulDomContainer ) {
    super.setXulDomContainer( xulDomContainer );
    if ( hasValidDocument() ) {
      loadInputs();
    }
  }

  /** if underlying element is null, happens before fragment is attached to main document */
  private boolean hasValidDocument() {
    try {
      return document.getDocument() != null;
    } catch ( Exception e ) {
      return false;
    }
  }

  private void loadInputs() {
    this.document = xulDomContainer.getDocumentRoot();

    workGroupInput = getElementById( XulTextbox.class, "workgroup-text" );
    regionInput = getElementById( XulTextbox.class, "region-text" );
    catalogInput = getElementById( XulTextbox.class, "catalog-text" );
    outputLocationInput =  getElementById( XulTextbox.class, "output-location-text" );

    authSelector = getElementById( XulRadioGroup.class, "auth-radio" );
    authDeck = getElementById( XulDeck.class, "auth-deck" );
    profileNameInput = getElementById( XulTextbox.class, "profile-name-text" );


    selectAuth();
  }

  public DbInfoHandler getInfoHandler() {
    return this;
  }

  private AthenaDatabaseMeta.AuthType getSelectedAuthType() {
    return authDeck.map( deck -> deck.getChildNodes().get( deck.getSelectedIndex() ).getId() )
                   .map( id -> id.equals( PROFILE_NAME_AUTH_ID ) ? AuthType.ProfileCredentials : AuthType.DefaultChain )
                   .orElseGet( AuthType::defaultValue );
  }

  public void selectAuth(){
    log.debug( "selectAuth()" );
    authSelector.ifPresent( radio -> {
      int selectedIdx = getSelectedIndex( radio );
      authDeck.ifPresent( deck -> {
        if ( selectedIdx < 0 || selectedIdx >= deck.getChildNodes().size() ) {
          log.error( "Invalid index: {}", selectedIdx );
          return;
        }
        deck.setSelectedIndex( selectedIdx );
      } );
    } );
  }

  /** meta -> ui */
  @Override
  public void loadConnectionSpecificInfo( DatabaseMeta meta ) {
    loadInputs();
    tryCast( AthenaDatabaseMeta.class, meta.getDatabaseInterface() ).ifPresent( dbiMeta -> {
      log.debug( "Athena meta->ui" );
      AuthType authType = dbiMeta.getAuthType();
      authDeck.ifPresent( deck -> {
        int idx = indexOfAuthType( deck, authType );
        authSelector.ifPresent( radioGroup -> selectIndex( radioGroup, idx ) );
        if ( authType == AuthType.ProfileCredentials ) {
          String profileName = dbiMeta.getProfileName().orElse( "" );
          profileNameInput.ifPresent( input -> input.setValue( profileName ) );
        }
      } );
      selectAuth();
      String workGroup = dbiMeta.getWorkGroup();
      workGroupInput.ifPresent( input -> input.setValue( workGroup ) );

      String region = dbiMeta.getRegion();
      regionInput.ifPresent( input -> input.setValue( region ) );

      String catalog = dbiMeta.getCatalog();
      catalogInput.ifPresent( input -> input.setValue( catalog ) );

      String outputLocation = dbiMeta.getOutputLocation();
      outputLocationInput.ifPresent( input -> input.setValue( outputLocation ) );
    } );
  }

  private int indexOfAuthType( XulDeck deck, AuthType authType ){
    switch ( authType ) {
        case DefaultChain:
          return indexOfId( deck, DEFAULT_AUTH_ID );
        case ProfileCredentials:
          return indexOfId( deck, PROFILE_NAME_AUTH_ID );
        default:
          return 0;
    }
  }

  private int indexOfId( XulDeck deck, String childId ){
    List<XulComponent> children = deck.getChildNodes();
    for ( int i = 0; i < children.size(); i++ ) {
      if ( childId.equals( children.get( i ).getId() ) ) {
        return i;
      }
    }
    return 0;
  }

  /** ui -> meta */
  @Override
  public void saveConnectionSpecificInfo( DatabaseMeta meta ){
    // only set values if filled
    loadInputs();
    tryCast( AthenaDatabaseMeta.class, meta.getDatabaseInterface() ).ifPresent( dbiMeta -> {
      log.debug( "Athena ui->meta" );
      AuthType authType = getSelectedAuthType();
      dbiMeta.setAuthType( authType );
      if ( authType == AuthType.ProfileCredentials ) {
        getTextBoxValue( profileNameInput ).ifPresent( dbiMeta::setProfileName );
      }
      getTextBoxValue( workGroupInput ).ifPresent( dbiMeta::setWorkGroup );
      getTextBoxValue( regionInput ).ifPresent( dbiMeta::setRegion );
      getTextBoxValue( catalogInput ).ifPresent( dbiMeta::setCatalog );
      getTextBoxValue( outputLocationInput ).ifPresent( dbiMeta::setOutputLocation );
    } );
  }

  private <T> Optional<T> getElementById( Class<T> type, String id ) {
    return tryCast( type, document.getElementById( id ) );
  }

  private void selectIndex( XulRadioGroup radioGroup, int idx ) {
    for ( int x = 0; x < radioGroup.getChildNodes().size(); x++ ) {
      XulComponent child = radioGroup.getChildNodes().get( x );
      final int copyx = x;
      tryCast( XulRadio.class, child )
      .ifPresent( radio -> radio.setSelected( copyx == idx ));
    }
  }

  private int getSelectedIndex( XulRadioGroup radioGroup ){
    List<XulComponent> children = radioGroup.getChildNodes();
    for ( int i = 0; i < children.size(); i++ ) {
      if ( tryCast( XulRadio.class, children.get( i )).map( XulRadio::isSelected ).orElse( false )){
        return i;
      }
    }
    return 0;
  }

  private Optional<String> getTextBoxValue( Optional<XulTextbox> tb ) {
    return tb.map( XulTextbox::getValue ).filter( StringUtils::isNotBlank );
  }

  @SuppressWarnings( "unchecked" )
  private static <T> Optional<T> tryCast( Class<T> type, Object obj ){
    if ( obj != null && type.isAssignableFrom( obj.getClass() ) ) {
      return Optional.of( (T) obj );
    } else {
      return Optional.empty();
    }
  }
}
