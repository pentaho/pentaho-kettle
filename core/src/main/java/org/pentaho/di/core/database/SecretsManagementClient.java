package org.pentaho.di.core.database;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.SecretsManagementException;
import org.pentaho.di.core.util.HttpClientManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

class SecretsManagementClient {

  private static final String SECRETS_PATH = "/api/v1/secrets";

  private static final int CONNECT_TIMEOUT_MS = 5_000;
  private static final int SOCKET_TIMEOUT_MS = 10_000;

  private static final SecretsManagementClient INSTANCE =
    new SecretsManagementClient( HttpClientManager.getInstance().createBuilder().ignoreSsl( true ).build() );


  private final CloseableHttpClient httpClient;

  /**
   * Package-private constructor for tests; production code uses {@link #getInstance()}.
   */
  SecretsManagementClient( CloseableHttpClient httpClient ) {
    this.httpClient = httpClient;
  }

  public static SecretsManagementClient getInstance() {
    return INSTANCE;
  }

  private static String stripTrailingSlash( String s ) {
    return s.endsWith( "/" ) ? s.substring( 0, s.length() - 1 ) : s;
  }

  /**
   * Test-only helper to avoid pulling in StandardCharsets from callers.
   */
  static byte[] toBytes( String s ) {
    return s.getBytes( StandardCharsets.UTF_8 );
  }

  public Map<String, String> getSecrets( String secretsRef ) throws SecretsManagementException {
    if ( secretsRef == null || secretsRef.trim().isEmpty() ) {
      throw new IllegalStateException( "secretsRef must be a non-blank reference" );
    }
    String baseUrl = Const.getSecretsManagementUrl();
    if ( baseUrl == null || baseUrl.trim().isEmpty() ) {
      throw new IllegalStateException(
        "SECRETS_MANAGEMENT_URL is not configured — cannot resolve secret '" + secretsRef + "'" );
    }

    String url = stripTrailingSlash( baseUrl ) + SECRETS_PATH + "/" + secretsRef;
    HttpGet request = new HttpGet( url );
    request.setConfig( RequestConfig.custom()
      .setConnectTimeout( CONNECT_TIMEOUT_MS )
      .setSocketTimeout( SOCKET_TIMEOUT_MS )
      .build() );
    request.setHeader( "Accept", "application/json" );

    String bearerToken;
    try {
      bearerToken = CmsTokenProvider.getInstance().getToken();
    } catch ( Exception e ) {
      // Token-endpoint failure: treat as unauthorized so the caller gets the standard message.
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED,
        "Secret unauthorized or expired", e );
    }
    if ( bearerToken != null ) {
      request.setHeader( "Authorization", "Bearer " + bearerToken );
    }

    try ( CloseableHttpResponse response = httpClient.execute( request ) ) {
      int status = response.getStatusLine().getStatusCode();
      if ( status == 200 ) {
        return parseBody( response.getEntity(), secretsRef );
      }
      throw mapStatus( status, secretsRef );
    } catch ( SecretsManagementException e ) {
      throw e;
    } catch ( IOException e ) {
      throw new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
        "Secret store unavailable", e );
    }
  }

  private Map<String, String> parseBody( HttpEntity entity, String secretsRef ) throws SecretsManagementException {
    if ( entity == null ) {
      throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
        "Secret response was empty for '" + secretsRef + "'" );
    }
    try {
      byte[] body = EntityUtils.toByteArray( entity );
      if ( body.length == 0 ) {
        throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
          "Secret response was empty for '" + secretsRef + "'" );
      }
      Map<String, String> parsed = new ObjectMapper().readValue( body,
        new TypeReference<Map<String, String>>() {
        } );
      return parsed == null ? Collections.emptyMap() : parsed;
    } catch ( SecretsManagementException e ) {
      throw e;
    } catch ( IOException e ) {
      // Do not include the body in the message — it may contain plaintext secret material.
      throw new SecretsManagementException( SecretsManagementException.Reason.INVALID_RESPONSE,
        "Secret response could not be parsed for '" + secretsRef + "'", e );
    }
  }

  private SecretsManagementException mapStatus( int status, String secretsRef ) {
    return switch ( status ) {
      case 401, 403 -> new SecretsManagementException( SecretsManagementException.Reason.UNAUTHORIZED,
        "Secret unauthorized or expired" );
      case 404 -> new SecretsManagementException( SecretsManagementException.Reason.NOT_FOUND,
        "Secret not found: " + secretsRef );
      default -> {
        if ( status >= 500 ) {
          yield new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
            "Secret store unavailable" );
        }
        yield new SecretsManagementException( SecretsManagementException.Reason.UNAVAILABLE,
          "Secret store returned unexpected HTTP " + status );
      }
    };
  }
}

