package org.pentaho.di.core.database;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.SecretsManagementException;
import org.pentaho.di.core.util.HttpClientManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.pentaho.di.core.Const;
import java.io.IOException;
import java.util.Map;

/**
 * Http client for the connections from the connections-management-service.
 */
public class CmsConnectionProvider {

  private static final CmsConnectionProvider INSTANCE = new CmsConnectionProvider();

  private CmsConnectionProvider() {
    // Prevents instantiation.
  }

  /**
   * Returns the singleton instance.
   */
  public static CmsConnectionProvider getInstance() {
    return INSTANCE;
  }

  private static String stripTrailingSlash( String s ) {
    return s.endsWith( "/" ) ? s.substring( 0, s.length() - 1 ) : s;
  }

  public DatabaseInterface fetchConnectionDetails( String connectionId ) {
    verifyConfigured();

    try ( var client = getHttpClient() ) {
      var request = prepareRequest( connectionId );
      var response = client.execute( request );
      var connectionDetails = verifyResponse( response );

      return parseConnectionResponse( connectionDetails );
    } catch ( IOException ex ) {
      throw new RuntimeException( "Failed to fetch connection details for connection Id " + connectionId, ex );
    }
  }

  private void verifyConfigured() {
    if ( Const.getCmsConnectionsUrl() == null ) {
      throw new IllegalStateException(
        "CMS connection provider is not configured. Please set CMS_CONNECTIONS_URL, CMS_CLIENT_ID, and "
          + "CMS_CLIENT_SECRET." );
    }
  }

  private HttpGet prepareRequest( String connectionId ) {
    var requestUrl = stripTrailingSlash( Const.getCmsConnectionsUrl() ) + "/" + connectionId;
    var request = new HttpGet( requestUrl );

    request.addHeader( "Content-Type", "application/json" );

    try {
      var token = CmsTokenProvider.getInstance().getToken();

      if ( StringUtils.isNotBlank( token ) ) {
        request.addHeader( "Authorization", "Bearer " + token );
      }
    } catch ( KettleDatabaseException e ) {
      throw new RuntimeException( e );
    }

    return request;
  }

  private JsonNode verifyResponse( HttpResponse response ) throws IOException {
    var statusCode = response.getStatusLine().getStatusCode();

    if ( statusCode != 200 ) {
      throw new RuntimeException(
        "Failed to fetch connection details from the connection management service. Status code: " + statusCode );
    }

    var content = new String( response.getEntity().getContent().readAllBytes() );
    var mapper = new ObjectMapper();

    return mapper.readTree( content );
  }

  private DatabaseInterface parseConnectionResponse( JsonNode connectionNode ) {
    var databaseConnection = connectionNode.path( "databaseConnection" );
    var detail = databaseConnection.path( "detail" );
    var type = detail.path( "databaseType" ).path( "shortName" ).asText();
    var secretsRef = databaseConnection.path( "credentials" ).path( "secretReference" ).asText();

    try {
      var db = DatabaseMeta.getDatabaseInterface( type );

      db.setConnectionId( databaseConnection.path( "id" ).asText() );
      db.setName( databaseConnection.path( "name" ).asText() );
      db.setHostname( detail.path( "hostname" ).asText() );
      db.setDatabaseName( detail.path( "databaseName" ).asText() );
      db.setDatabasePortNumberString( detail.path( "databasePort" ).asText() );

      var credentials = fetchCredentials( secretsRef );
      db.setUsername( credentials.get( "username" ) );
      db.setPassword( credentials.get( "password" ) );

      db.setDataTablespace( detail.path( "dataTablespace" ).asText() );
      db.setIndexTablespace( detail.path( "indexTablespace" ).asText() );

      return db;
    } catch ( KettleDatabaseException e ) {
      throw new RuntimeException( e );
    }
  }

  private Map<String, String> fetchCredentials( String connectionId ) {
    try {
      var secrets = SecretsManagementClient.getInstance().getSecrets( connectionId );
      var user = secrets.get( "username" );
      var pass = secrets.get( "password" );

      if ( user == null && pass == null ) {
        throw new SecretsManagementException(
          SecretsManagementException.Reason.INVALID_RESPONSE,
          "Secret bundle for connection '" + connectionId + "' did not contain 'username' or 'password'" );
      }

      return secrets;
    } catch ( SecretsManagementException ex ) {
      throw new RuntimeException( ex );
    }
  }

  private CloseableHttpClient getHttpClient() {
    return HttpClientManager.getInstance()
      .createBuilder()
      .build();
  }
}
