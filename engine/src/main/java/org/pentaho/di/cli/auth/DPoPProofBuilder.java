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

import org.pentaho.di.i18n.BaseMessages;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Lightweight DPoP proof JWT builder for CLI tools (RFC 9449).
 * <p>
 * Uses only JDK APIs — no Nimbus JOSE dependency required. Generates a
 * per-session RSA-2048 keypair and produces DPoP proof JWTs signed with
 * {@code RS256}. The server validates these using {@code DPoPVerifier} and
 * binds the auth handle to this key's JWK thumbprint.
 * <p>
 * Thread-safe: the keypair is generated once and the build method is
 * stateless aside from reading the immutable key.
 */
public class DPoPProofBuilder {

  private static final Base64.Encoder B64URL = Base64.getUrlEncoder().withoutPadding();

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( DPoPProofBuilder.class, key, tokens );
  }

  private final KeyPair keyPair;
  private final String jwkJson;
  private final String jwkThumbprint;

  public DPoPProofBuilder() {
    try {
      KeyPairGenerator gen = KeyPairGenerator.getInstance( "RSA" );
      gen.initialize( 2048 );
      this.keyPair = gen.generateKeyPair();
      this.jwkJson = buildJwkJson( (RSAPublicKey) keyPair.getPublic() );
      this.jwkThumbprint = computeJwkThumbprint( (RSAPublicKey) keyPair.getPublic() );
    } catch ( NoSuchAlgorithmException e ) {
      throw new IllegalStateException( message( "DPoPProofBuilder.RsaUnavailable" ), e );
    }
  }

  /**
   * For testing — supply a pre-generated keypair.
   */
  DPoPProofBuilder( KeyPair keyPair ) {
    this.keyPair = keyPair;
    this.jwkJson = buildJwkJson( (RSAPublicKey) keyPair.getPublic() );
    this.jwkThumbprint = computeJwkThumbprint( (RSAPublicKey) keyPair.getPublic() );
  }

  /**
   * Returns the JWK thumbprint (SHA-256, base64url) for this key.
   */
  public String getJwkThumbprint() {
    return jwkThumbprint;
  }

  /**
   * Builds a DPoP proof JWT for the given HTTP method and target URI.
   * Does not include the {@code ath} claim — use
   * {@link #buildProof(String, String, String)} when an access token is
   * available.
   *
   * @param httpMethod e.g. "POST" or "GET"
   * @param targetUri  the full request URI (scheme + authority + path)
   * @return the compact-serialised JWT string (header.payload.signature)
   */
  public String buildProof( String httpMethod, String targetUri ) {
    return buildProof( httpMethod, targetUri, null );
  }

  /**
   * Builds a DPoP proof JWT for the given HTTP method, target URI, and
   * access token. When {@code accessToken} is non-null, the {@code ath} claim
   * (SHA-256 of the token, base64url) is included — required per RFC 9449 §4.2
   * when the proof accompanies a resource request with a Bearer token.
   *
   * @param httpMethod  e.g. "POST"
   * @param targetUri   the full request URI (scheme + authority + path)
   * @param accessToken the Bearer access token to bind via {@code ath}, or null
   * @return the compact-serialised JWT string (header.payload.signature)
   */
  public String buildProof( String httpMethod, String targetUri, String accessToken ) {
    String header = "{\"typ\":\"dpop+jwt\",\"alg\":\"RS256\",\"jwk\":" + jwkJson + "}";
    StringBuilder payloadBuilder = new StringBuilder();
    payloadBuilder.append( "{\"jti\":\"" ).append( UUID.randomUUID() ).append( "\"" )
      .append( ",\"htm\":\"" ).append( httpMethod ).append( "\"" )
      .append( ",\"htu\":\"" ).append( escapeJsonString( targetUri ) ).append( "\"" )
      .append( ",\"iat\":" ).append( Instant.now().getEpochSecond() );
    if ( accessToken != null && !accessToken.isBlank() ) {
      payloadBuilder.append( ",\"ath\":\"" ).append( computeAth( accessToken ) ).append( "\"" );
    }
    payloadBuilder.append( "}" );
    String payload = payloadBuilder.toString();

    String encodedHeader = B64URL.encodeToString( header.getBytes( StandardCharsets.UTF_8 ) );
    String encodedPayload = B64URL.encodeToString( payload.getBytes( StandardCharsets.UTF_8 ) );
    String signingInput = encodedHeader + "." + encodedPayload;

    try {
      Signature sig = Signature.getInstance( "SHA256withRSA" );
      sig.initSign( keyPair.getPrivate() );
      sig.update( signingInput.getBytes( StandardCharsets.UTF_8 ) );
      String encodedSignature = B64URL.encodeToString( sig.sign() );
      return signingInput + "." + encodedSignature;
    } catch ( Exception e ) {
      throw new IllegalStateException( message( "DPoPProofBuilder.SignFailed", e.getMessage() ), e );
    }
  }

  /**
   * Computes the {@code ath} claim value: SHA-256 of the token bytes,
   * base64url-encoded (RFC 9449 §4.2).
   */
  private static String computeAth( String accessToken ) {
    try {
      MessageDigest md = MessageDigest.getInstance( "SHA-256" );
      byte[] hash = md.digest( accessToken.getBytes( StandardCharsets.UTF_8 ) );
      return B64URL.encodeToString( hash );
    } catch ( NoSuchAlgorithmException e ) {
      throw new IllegalStateException( message( "DPoPProofBuilder.Sha256Unavailable" ), e );
    }
  }

  private static String buildJwkJson( RSAPublicKey pub ) {
    String n = B64URL.encodeToString( toUnsignedBytes( pub.getModulus() ) );
    String e = B64URL.encodeToString( toUnsignedBytes( pub.getPublicExponent() ) );
    return "{\"kty\":\"RSA\",\"n\":\"" + n + "\",\"e\":\"" + e + "\"}";
  }

  /**
   * Computes the JWK thumbprint per RFC 7638 §3.
   * For RSA: SHA-256 of the canonical JSON
   * {@code {"e":"...","kty":"RSA","n":"..."}}.
   */
  private static String computeJwkThumbprint( RSAPublicKey pub ) {
    String n = B64URL.encodeToString( toUnsignedBytes( pub.getModulus() ) );
    String e = B64URL.encodeToString( toUnsignedBytes( pub.getPublicExponent() ) );
    // RFC 7638: members sorted alphabetically, no whitespace
    String canonical = "{\"e\":\"" + e + "\",\"kty\":\"RSA\",\"n\":\"" + n + "\"}";
    try {
      MessageDigest md = MessageDigest.getInstance( "SHA-256" );
      byte[] hash = md.digest( canonical.getBytes( StandardCharsets.UTF_8 ) );
      return B64URL.encodeToString( hash );
    } catch ( NoSuchAlgorithmException ex ) {
      throw new IllegalStateException( message( "DPoPProofBuilder.Sha256Unavailable" ), ex );
    }
  }

  /**
   * Converts a BigInteger to an unsigned byte array (strips leading zero byte
   * if present, as required by JWK base64url encoding).
   */
  private static byte[] toUnsignedBytes( java.math.BigInteger bigInt ) {
    byte[] bytes = bigInt.toByteArray();
    if ( bytes.length > 1 && bytes[ 0 ] == 0 ) {
      byte[] trimmed = new byte[ bytes.length - 1 ];
      System.arraycopy( bytes, 1, trimmed, 0, trimmed.length );
      return trimmed;
    }
    return bytes;
  }

  private static String escapeJsonString( String s ) {
    if ( s == null ) {
      return "";
    }
    return s.replace( "\\", "\\\\" ).replace( "\"", "\\\"" );
  }
}
