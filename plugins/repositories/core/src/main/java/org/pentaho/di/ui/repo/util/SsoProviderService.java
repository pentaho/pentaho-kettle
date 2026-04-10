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

package org.pentaho.di.ui.repo.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SsoProviderService {

  private static final int CONNECT_TIMEOUT_MS = 5000;
  private static final int READ_TIMEOUT_MS = 5000;

  public List<SsoProvider> fetchProviders( String serverUrl ) throws IOException {
    String providersUrl = buildProvidersUrl( serverUrl );
    HttpURLConnection connection = null;

    try {
      connection = (HttpURLConnection) new URL( providersUrl ).openConnection();
      connection.setRequestMethod( "GET" );
      connection.setConnectTimeout( CONNECT_TIMEOUT_MS );
      connection.setReadTimeout( READ_TIMEOUT_MS );
      connection.setRequestProperty( "Accept", "application/json" );

      int responseCode = connection.getResponseCode();
      if ( responseCode == HttpURLConnection.HTTP_NOT_FOUND ) {
        // The SSO providers endpoint does not exist on this server — SSO is not configured.
        return Collections.emptyList();
      }
      if ( responseCode < 200 || responseCode >= 300 ) {
        throw new IOException( "Provider lookup failed with HTTP status " + responseCode );
      }

      try ( InputStreamReader reader =
              new InputStreamReader( connection.getInputStream(), StandardCharsets.UTF_8 ) ) {
        Object parsed = new JSONParser().parse( reader );
        if ( !( parsed instanceof JSONArray providersArray ) ) {
          return Collections.emptyList();
        }

        List<SsoProvider> providers = new ArrayList<>();
        for ( Object item : providersArray ) {
          if ( item instanceof JSONObject providerObject ) {
            boolean enabled = getBoolean( providerObject.get( "enabled" ) );
            String clientName = stringValue( providerObject.get( "clientName" ) );
            String authorizationUri = stringValue( providerObject.get( "authorizationUri" ) );
            String registrationId = stringValue( providerObject.get( "registrationId" ) );
            if ( enabled && !isBlank( clientName ) && !isBlank( authorizationUri ) ) {
              providers.add( new SsoProvider( clientName, authorizationUri, registrationId ) );
            }
          }
        }
        return providers;
      } catch ( Exception e ) {
        throw new IOException( "Failed to parse SSO provider response", e );
      }
    } finally {
      if ( connection != null ) {
        connection.disconnect();
      }
    }
  }

  public String buildProvidersUrl( String serverUrl ) {
    return normalizeBaseUrl( serverUrl ) + "/plugin/login/api/v0/oauth-providers";
  }

  private String normalizeBaseUrl( String serverUrl ) {
    if ( isBlank( serverUrl ) ) {
      throw new IllegalArgumentException( "Server URL is required" );
    }

    String trimmed = serverUrl.trim();
    return trimmed.endsWith( "/" ) ? trimmed.substring( 0, trimmed.length() - 1 ) : trimmed;
  }

  private boolean getBoolean( Object value ) {
    if ( value instanceof Boolean booleanValue ) {
      return booleanValue;
    }
    if ( value instanceof String stringValue ) {
      return Boolean.parseBoolean( stringValue );
    }
    return false;
  }

  private String stringValue( Object value ) {
    return value == null ? null : value.toString();
  }

  private boolean isBlank( String value ) {
    return value == null || value.trim().isEmpty();
  }

  public record SsoProvider(String clientName, String authorizationUri, String registrationId) {

    @Override
      public String toString() {
        return clientName;
      }
    }
}