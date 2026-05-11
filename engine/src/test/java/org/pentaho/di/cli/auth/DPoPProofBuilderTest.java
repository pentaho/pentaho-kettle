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

package org.pentaho.di.cli.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DPoPProofBuilderTest {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Base64.Decoder B64URL = Base64.getUrlDecoder();
  private static final Base64.Encoder B64URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

  @Test
  public void buildProofCreatesSignedJwtWithExpectedClaims()
    throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    KeyPair keyPair = generateKeyPair();
    DPoPProofBuilder builder = new DPoPProofBuilder( keyPair );

    String proof = builder.buildProof( "POST", "https://server.example/pentaho/api/oauth/auth-start" );

    String[] parts = proof.split( "\\." );
    assertEquals( 3, parts.length );

    JsonNode header = decodeJson( parts[ 0 ] );
    JsonNode payload = decodeJson( parts[ 1 ] );

    assertEquals( "dpop+jwt", header.path( "typ" ).asText() );
    assertEquals( "RS256", header.path( "alg" ).asText() );
    assertEquals( "RSA", header.path( "jwk" ).path( "kty" ).asText() );
    assertNotNull( header.path( "jwk" ).path( "n" ).textValue() );
    assertNotNull( header.path( "jwk" ).path( "e" ).textValue() );

    assertEquals( "POST", payload.path( "htm" ).asText() );
    assertEquals( "https://server.example/pentaho/api/oauth/auth-start", payload.path( "htu" ).asText() );
    assertTrue( payload.hasNonNull( "jti" ) );
    assertTrue( payload.path( "iat" ).asLong() > 0 );
    assertFalse( payload.has( "ath" ) );

    assertEquals( expectedThumbprint( header.path( "jwk" ) ), builder.getJwkThumbprint() );
    assertTrue( verifySignature( parts, keyPair ) );
  }

  @Test
  public void buildProofBindsAccessTokenThroughAthClaim()
    throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    KeyPair keyPair = generateKeyPair();
    DPoPProofBuilder builder = new DPoPProofBuilder( keyPair );

    String targetUri = "https://server.example/pentaho/api/oauth/token-refresh?note=\"quoted\"&path=a\\b";
    String accessToken = "access-token-value";
    String proof = builder.buildProof( "GET", targetUri, accessToken );

    String[] parts = proof.split( "\\." );
    JsonNode payload = decodeJson( parts[ 1 ] );

    assertEquals( "GET", payload.path( "htm" ).asText() );
    assertEquals( targetUri, payload.path( "htu" ).asText() );
    assertEquals( expectedAth( accessToken ), payload.path( "ath" ).asText() );
    assertTrue( verifySignature( parts, keyPair ) );
  }

  @Test
  public void buildProofOmitsAthWhenAccessTokenIsBlank()
    throws IOException, NoSuchAlgorithmException {
    KeyPair keyPair = generateKeyPair();
    DPoPProofBuilder builder = new DPoPProofBuilder( keyPair );

    String proof = builder.buildProof( "POST", "https://server.example/pentaho/api/oauth/auth-start", "   " );
    JsonNode payload = decodeJson( proof.split( "\\." )[ 1 ] );

    assertFalse( payload.has( "ath" ) );
  }

  @Test
  public void buildProofUsesEmptyHtuWhenTargetUriIsNull()
    throws IOException, NoSuchAlgorithmException {
    KeyPair keyPair = generateKeyPair();
    DPoPProofBuilder builder = new DPoPProofBuilder( keyPair );

    String proof = builder.buildProof( "POST", null );
    JsonNode payload = decodeJson( proof.split( "\\." )[ 1 ] );

    assertEquals( "", payload.path( "htu" ).asText() );
  }

  @Test
  public void getJwkThumbprintRemainsStableAcrossMultipleProofs() throws NoSuchAlgorithmException {
    KeyPair keyPair = generateKeyPair();
    DPoPProofBuilder builder = new DPoPProofBuilder( keyPair );

    String thumbprint = builder.getJwkThumbprint();
    String firstProof = builder.buildProof( "POST", "https://server.example/one" );
    String secondProof = builder.buildProof( "POST", "https://server.example/two" );

    assertEquals( thumbprint, builder.getJwkThumbprint() );
    assertNotEquals( firstProof, secondProof );
  }

  @Test
  public void defaultConstructorBuildsProofAndThumbprint() throws IOException {
    DPoPProofBuilder builder = new DPoPProofBuilder();

    String proof = builder.buildProof( "POST", "https://server.example/default" );
    String[] parts = proof.split( "\\." );
    JsonNode header = decodeJson( parts[ 0 ] );

    assertEquals( 3, parts.length );
    assertEquals( "dpop+jwt", header.path( "typ" ).asText() );
    assertTrue( builder.getJwkThumbprint().length() > 10 );
  }

  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA" );
    generator.initialize( 2048 );
    return generator.generateKeyPair();
  }

  private JsonNode decodeJson( String part ) throws IOException {
    return OBJECT_MAPPER.readTree( new String( B64URL.decode( part ), StandardCharsets.UTF_8 ) );
  }

  private boolean verifySignature( String[] parts, KeyPair keyPair )
    throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signature = Signature.getInstance( "SHA256withRSA" );
    signature.initVerify( keyPair.getPublic() );
    signature.update( ( parts[ 0 ] + "." + parts[ 1 ] ).getBytes( StandardCharsets.UTF_8 ) );
    return signature.verify( B64URL.decode( parts[ 2 ] ) );
  }

  private String expectedAth( String accessToken ) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
    return B64URL_ENCODER.encodeToString( digest.digest( accessToken.getBytes( StandardCharsets.UTF_8 ) ) );
  }

  private String expectedThumbprint( JsonNode jwk ) throws NoSuchAlgorithmException {
    String canonical = "{\"e\":\"" + jwk.path( "e" ).asText() + "\",\"kty\":\"RSA\",\"n\":\""
      + jwk.path( "n" ).asText() + "\"}";
    MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
    return B64URL_ENCODER.encodeToString( digest.digest( canonical.getBytes( StandardCharsets.UTF_8 ) ) );
  }
}
