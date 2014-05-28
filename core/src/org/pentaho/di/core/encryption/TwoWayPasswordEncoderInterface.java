package org.pentaho.di.core.encryption;

import org.pentaho.di.core.exception.KettleException;

public interface TwoWayPasswordEncoderInterface {

  /**
   * Initialize the password encoder by loading key details from the environment (kettle.properties or system settings).
   * @throws KettleException
   */
  public void init() throws KettleException;

  /**
   * Encode the raw password, include a prefix indicating the type of encryption used.
   * @param password The password to encode
   * @return The encoded password string
   */
  public String encode( String password );

  /**
   * Encode a password.
   * @param password The password to encode
   * @param includePrefix True if a prefix needs to be encoded
   * @return The encoded password string
   */
  public String encode( String password, boolean includePrefix );

  /**
   * Decode a password.
   * @param encodedPassword The encoded password with or without a prefix
   * @param optionallyEncrypted Set to true if the password is optionally encrypted (indicated by a prefix).
   * @return The decoded password string
   */
  public String decode( String encodedPassword, boolean optionallyEncrypted );

  /**
   * Decode a password which does NOT have a prefix attached.
   * @param encodedPassword The encoded password without a prefix
   * @return The decoded password string
   */
  public String decode( String encodedPassword );

  /**
   * @return The prefixes to the encoded passwords which this password encoder supports.
   */
  public String[] getPrefixes();
}
