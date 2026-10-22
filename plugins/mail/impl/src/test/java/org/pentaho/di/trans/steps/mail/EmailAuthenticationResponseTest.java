/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.trans.steps.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmailAuthenticationResponseTest {

  @Test
  public void testDeserializesOauthResponseWithUnknownProperties() throws Exception {
    String responseBody = "{"
      + "\"token_type\":\"Bearer\","
      + "\"scope\":\"https://outlook.office.com/IMAP.AccessAsUser.All\","
      + "\"expires_in\":3599,"
      + "\"ext_expires_in\":3599,"
      + "\"access_token\":\"access-token\","
      + "\"refresh_token\":\"refresh-token\","
      + "\"id_token\":\"id-token\","
      + "\"expires_on\":\"1735689600\","
      + "\"future_property\":\"ignored\""
      + "}";

    EmailAuthenticationResponse response = new ObjectMapper().readValue( responseBody,
      EmailAuthenticationResponse.class );

    assertEquals( "access-token", response.getAccessToken() );
    assertEquals( "refresh-token", response.getRefreshToken() );
  }
}