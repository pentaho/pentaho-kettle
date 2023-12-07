package org.pentaho.ui.database.event.databricks;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabricksDatabaseMeta;
import org.pentaho.di.core.database.DatabricksDatabaseMeta.AuthMethod;
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
 * Data Handler for Databricks-specific elements
 */
public class DatabricksHandler extends AbstractXulEventHandler implements DbInfoHandler {
  private static final Logger log = LoggerFactory.getLogger( DatabricksHandler.class );

  private static final String TOKEN_AUTH_ID = "authentication-token";
  private static final String CREDS_AUTH_ID = "authentication-user-pass";

  private Optional<XulTextbox> accessTokenInput = Optional.empty();
  private Optional<XulTextbox> httpPathInput = Optional.empty();
  private Optional<XulRadioGroup> authSelector = Optional.empty();
  private Optional<XulDeck> authDeck = Optional.empty();

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
    accessTokenInput = getElementById( XulTextbox.class, "token-text" );
    httpPathInput = getElementById( XulTextbox.class, "http-path-text" );
    authSelector = getElementById( XulRadioGroup.class, "auth-radio" );
    authDeck = getElementById( XulDeck.class, "auth-deck" );
    selectAuth();
  }

  public DbInfoHandler getInfoHandler() {
    return this;
  }

  private DatabricksDatabaseMeta.AuthMethod getSelectedAuthMethod() {
    return authDeck.map( deck -> {
      return deck.getChildNodes().get( deck.getSelectedIndex() ).getId();
    } ).map( id -> id.equals( CREDS_AUTH_ID ) ? AuthMethod.Credentials : AuthMethod.Token )
        .orElseGet( AuthMethod::defaultValue );
  }

  public void selectAuth() {
    log.debug( "selectAuth()" );
    authSelector.ifPresent( radio -> {
      int selectedIdx = getSelectedIndex( radio );
      authDeck.ifPresent( deck -> {
        if ( selectedIdx < 0 || selectedIdx >= deck.getChildNodes().size() ) {
          log.error( "Invalid index", selectedIdx );
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
    tryCast( DatabricksDatabaseMeta.class, meta.getDatabaseInterface() ).ifPresent( dbiMeta -> {
      log.debug( "Datbricks meta->ui" );
      // Properties attributes = meta.getAttributes();
      AuthMethod authMtd = dbiMeta.getAuthMethod();
      authDeck.ifPresent( deck -> {
        int idx = indexOfAuthMethod( deck, authMtd );
        authSelector.ifPresent( radioGroup -> selectIndex( radioGroup, idx ) );
        if ( authMtd == AuthMethod.Token ) {
          String token = dbiMeta.getToken().orElse( "" );
          accessTokenInput.ifPresent( input -> input.setValue( token ) );
        }
      } );
      selectAuth();
      String httpPath = dbiMeta.getHttpPath().orElse( "" );
      httpPathInput.ifPresent( input -> input.setValue( httpPath ) );

    } );
  }

  private int indexOfAuthMethod( XulDeck deck, AuthMethod authMtd ) {
    switch ( authMtd ) {
      case Credentials:
        return indexOfId( deck, CREDS_AUTH_ID );
      case Token:
        return indexOfId( deck, TOKEN_AUTH_ID );
      default:
        return 0;
    }
  }

  private int indexOfId( XulDeck deck, String childId ) {
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
  public void saveConnectionSpecificInfo( DatabaseMeta meta ) {
    // only set values if filled
    loadInputs();
    tryCast( DatabricksDatabaseMeta.class, meta.getDatabaseInterface() ).ifPresent( dbiMeta -> {
      log.debug( "Datbricks ui->meta" );
      AuthMethod authMethod = getSelectedAuthMethod();
      dbiMeta.setAuthMethod( authMethod );
      // credentials case is handled by main handler
      if ( authMethod == AuthMethod.Token ) {
        getTextBoxValue( accessTokenInput ).ifPresent( dbiMeta::setToken );
      }
      getTextBoxValue( httpPathInput ).ifPresent( dbiMeta::setHttpPath );
    } );
  }

  private <T> Optional<T> getElementById( Class<T> type, String id ) {
    return tryCast( type, document.getElementById( id ) );
  }

  private void selectIndex( XulRadioGroup radioGroup, int idx ) {
    tryCast( XulRadio.class, radioGroup.getChildNodes().get( idx ) ).ifPresent( radio -> radio.setSelected( true ) );
  }

  private int getSelectedIndex( XulRadioGroup radioGroup ) {
    List<XulComponent> children = radioGroup.getChildNodes();
    for ( int i = 0; i < children.size(); i++ ) {
      if ( tryCast( XulRadio.class, children.get( i ) ).map( XulRadio::isSelected ).orElse( false ) ) {
        return i;
      }
    }
    return 0;
  }

  private Optional<String> getTextBoxValue( Optional<XulTextbox> tb ) {
    return tb.map( XulTextbox::getValue ).filter( StringUtils::isNotBlank );
  }

  @SuppressWarnings( "unchecked" )
  private static <T> Optional<T> tryCast( Class<T> type, Object obj ) {
    if ( obj != null && type.isAssignableFrom( obj.getClass() ) ) {
      return Optional.of( (T) obj );
    } else {
      return Optional.empty();
    }
  }
}
