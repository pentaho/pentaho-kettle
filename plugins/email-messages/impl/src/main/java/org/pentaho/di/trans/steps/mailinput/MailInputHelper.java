/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.mailinput;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import jakarta.mail.Folder;

import java.util.Map;

/**
 * Helper class for the MailInput step providing UI-triggered action support.
 * <p>
 * Handles the {@code testConnection} and {@code checkIMAPFolder} actions that were
 * previously implemented inline in {@code MailInputDialog}. Both the Spoon dialog and
 * the web client delegate to this helper so the connection logic lives in one place.
 */
public class MailInputHelper extends BaseStepHelper {

  private static final String TEST_CONNECTION = "testConnection";
  private static final String CHECK_IMAP_FOLDER = "checkIMAPFolder";
  private static final String SELECT_IMAP_FOLDER = "selectIMAPFolder";
  private static final String SELECT_FOLDER = "selectFolder";
  private static final String FOLDER = "folder";
  private static final String FOLDERS = "folders";
  private static final String ERROR_MESSAGE = "errorMessage";

  private final MailInputMeta meta;

  public MailInputHelper( MailInputMeta meta ) {
    this.meta = meta;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    switch ( method ) {
      case TEST_CONNECTION:
        return testConnectionAction( transMeta );
      case CHECK_IMAP_FOLDER:
        return checkIMAPFolderAction( transMeta );
      case SELECT_IMAP_FOLDER, SELECT_FOLDER:
        return selectIMAPFolderAction( transMeta );
      default:
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
        return response;
    }
  }

  /**
   * Attempts to connect to the configured mail server.
   *
   * @param transMeta the transformation metadata used for variable substitution
   * @return a JSON object indicating whether the connection succeeded
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject testConnectionAction( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    MailConnection mailConn = null;
    try {
      mailConn = connect( transMeta );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR_MESSAGE, e.getMessage() );
    } finally {
      disconnect( mailConn );
    }
    return response;
  }

  /**
   * Checks whether the configured IMAP folder exists on the mail server.
   *
   * @param transMeta the transformation metadata used for variable substitution
   * @return a JSON object indicating whether the folder exists
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject checkIMAPFolderAction( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    MailConnection mailConn = null;
    try {
      String folderName = transMeta.environmentSubstitute( meta.getIMAPFolder() );
      mailConn = connect( transMeta );
      boolean exists = !Utils.isEmpty( folderName ) && mailConn.folderExists( folderName );
      response.put( ACTION_STATUS, exists ? SUCCESS_RESPONSE : FAILURE_RESPONSE );
      response.put( FOLDER, folderName );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR_MESSAGE, e.getMessage() );
    } finally {
      disconnect( mailConn );
    }
    return response;
  }

  /**
   * Fetches available IMAP folders from the connected mail store.
   * Returns both the complete folder list and a default folder value.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject selectIMAPFolderAction( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    MailConnection mailConn = null;
    try {
      mailConn = connect( transMeta );
      Folder defaultFolder = mailConn.getStore().getDefaultFolder();
      JSONArray folderNames = new JSONArray();

      if ( defaultFolder != null ) {
        collectFolderNames( defaultFolder, folderNames );
      }

      response.put( FOLDERS, folderNames );

      String imapFolder = transMeta.environmentSubstitute( meta.getIMAPFolder() );
      if ( Utils.isEmpty( imapFolder ) ) {
        imapFolder = "INBOX";
      }
      response.put( FOLDER, imapFolder );
      response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
      response.put( ERROR_MESSAGE, e.getMessage() );
    } finally {
      disconnect( mailConn );
    }
    return response;
  }

  @SuppressWarnings( "unchecked" )
  private void collectFolderNames( Folder folder, JSONArray folderNames ) {
    if ( folder == null ) {
      return;
    }

    try {
      Folder[] children = folder.list();
      if ( children == null || children.length == 0 ) {
        String fullName = folder.getFullName();
        if ( !Utils.isEmpty( fullName ) ) {
          folderNames.add( fullName );
        }
        return;
      }

      for ( Folder child : children ) {
        // collectFolderNames handles leaf and branch nodes, so add through recursion only once
        collectFolderNames( child, folderNames );
      }
    } catch ( Exception e ) {
      log.logDebug( e.getMessage() );
    }
  }

  private MailConnection connect( TransMeta transMeta ) throws KettleException {
    String realserver = transMeta.environmentSubstitute( meta.getServerName() );
    String realuser = transMeta.environmentSubstitute( meta.getUserName() );
    // Handle both encrypted passwords (from saved KTR) and plain text (from form test connection)
    String rawpass = transMeta.environmentSubstitute( meta.getPassword() );
    String realpass = Encr.decryptPasswordOptionallyEncrypted( rawpass );
    String realProxyUsername = transMeta.environmentSubstitute( meta.getProxyUsername() );
    int realport = Const.toInt( transMeta.environmentSubstitute( meta.getPort() ), -1 );

    if ( MailInputMeta.AUTENTICATION_OAUTH.equals( meta.isUsingAuthentication() ) ) {
      String tokenUrl = transMeta.environmentSubstitute( meta.getTokenUrl() );
      String scope = transMeta.environmentSubstitute( meta.getScope() );
      String clientId = transMeta.environmentSubstitute( meta.getClientId() );
      String secretKey = transMeta.environmentSubstitute( meta.getSecretKey() );
      String grantType = transMeta.environmentSubstitute( meta.getGrant_type() );
      String refreshToken = transMeta.environmentSubstitute( meta.getRefresh_token() );
      String authorizationCode = transMeta.environmentSubstitute( meta.getAuthorization_code() );
      String redirectUri = transMeta.environmentSubstitute( meta.getRedirectUri() );
      realpass = "Bearer " + meta.getOauthToken( tokenUrl, scope, clientId, secretKey, grantType, refreshToken,
        authorizationCode, redirectUri ).getAccessToken();
    }

    MailConnection mailConn = new MailConnection( transMeta.getBowl(), LogChannel.UI,
      MailConnectionMeta.getProtocolFromString( meta.getProtocol(), MailConnectionMeta.PROTOCOL_IMAP ),
      realserver, realport, realuser, realpass, meta.isUseSSL(), meta.isUseProxy(), realProxyUsername );
    mailConn.connect();
    return mailConn;
  }

  private void disconnect( MailConnection mailConn ) {
    if ( mailConn != null ) {
      try {
        mailConn.disconnect();
      } catch ( Exception e ) {
        log.logDebug( e.getMessage() );
      }
    }
  }
}
