/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/
package org.pentaho.di.job.entries.getpop;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailAuthenticationResponse implements IEmailAuthenticationResponse {
  @JsonProperty( "access_token" )
  private String accessToken;

  @JsonProperty( "token_type" )
  private String tokenType;

  @JsonProperty( "expires_in" )
  private Integer expiresIn;

  @JsonProperty( "ext_expires_in" )
  private Integer extExpiresIn;

  @JsonProperty( "refresh_token" )
  private String refreshToken;

  @JsonProperty( "scope" )
  private String scope;

  @JsonProperty( "id_token" )
  private String idToken;

  public String getAccessToken() {
    return accessToken == null ? "" : accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

}
