/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.encryption;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.util.Utils;

import com.google.common.annotations.VisibleForTesting;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.support.encryption.PasswordEncoderException;
import org.pentaho.support.utils.StringUtil;

/**
 * This class handles basic encryption of passwords in Kettle. Note that it's not really encryption, it's more
 * obfuscation. Passwords are <b>difficult</b> to read, not impossible.
 *
 * @author Matt
 * @since 17-12-2003
 *
 */
public class KettleTwoWayPasswordEncoder implements TwoWayPasswordEncoderInterface {
  private static final KettleTwoWayPasswordEncoder instance = new KettleTwoWayPasswordEncoder();
  private static final int RADIX = 16;
  private String Seed;
  /**
   * The word that is put before a password to indicate an encrypted form. If this word is not present, the password is
   * considered to be NOT encrypted
   */
  @SuppressWarnings( "squid:S2068" ) public static final String PASSWORD_ENCRYPTED_PREFIX = "Encrypted ";

  public KettleTwoWayPasswordEncoder() {
    String envSeed = Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_TWO_WAY_PASSWORD_ENCODER_SEED ), "0933910847463829827159347601486730416058" ); // Solve for PDI-16512
    Seed = envSeed;
  }

  private static KettleTwoWayPasswordEncoder getInstance() {
    return instance;
  }

  @Override
  public void init() throws PasswordEncoderException {
    // Nothing to do here.
  }

  @Override
  public String encode( String rawPassword ) {
    return encode( rawPassword, true );
  }

  @Override
  public String encode( String rawPassword, boolean includePrefix ) {
    if ( includePrefix ) {
      return encryptPasswordIfNotUsingVariablesInternal( rawPassword );
    } else {
      return encryptPasswordInternal( rawPassword );
    }
  }

  @Override
  public String decode( String encodedPassword ) {

    if ( encodedPassword != null && encodedPassword.startsWith( PASSWORD_ENCRYPTED_PREFIX ) ) {
      encodedPassword = encodedPassword.substring( PASSWORD_ENCRYPTED_PREFIX.length() );
    }

    return decryptPasswordInternal( encodedPassword );
  }

  @Override
  public String decode( String encodedPassword, boolean optionallyEncrypted ) {

    if ( encodedPassword == null ) {
      return null;
    }

    if ( optionallyEncrypted ) {

      if ( encodedPassword.startsWith( PASSWORD_ENCRYPTED_PREFIX ) ) {
        encodedPassword = encodedPassword.substring( PASSWORD_ENCRYPTED_PREFIX.length() );
        return decryptPasswordInternal( encodedPassword );
      } else {
        return encodedPassword;
      }
    } else {
      return decryptPasswordInternal( encodedPassword );
    }
  }

  @VisibleForTesting
  protected String encryptPasswordInternal( String password ) {
    if ( password == null ) {
      return "";
    }
    if ( password.length() == 0 ) {
      return "";
    }

    BigInteger bi_passwd = new BigInteger( password.getBytes() );

    BigInteger bi_r0 = new BigInteger( getSeed() );
    BigInteger bi_r1 = bi_r0.xor( bi_passwd );

    return bi_r1.toString( RADIX );
  }

  @VisibleForTesting
  protected String decryptPasswordInternal( String encrypted ) {
    if ( encrypted == null ) {
      return "";
    }
    if ( encrypted.length() == 0 ) {
      return "";
    }

    BigInteger bi_confuse = new BigInteger( getSeed() );

    try {
      BigInteger bi_r1 = new BigInteger( encrypted, RADIX );
      BigInteger bi_r0 = bi_r1.xor( bi_confuse );

      return new String( bi_r0.toByteArray() );
    } catch ( Exception e ) {
      return "";
    }
  }


  @VisibleForTesting
  protected String getSeed() {
    return this.Seed;
  }

  @Override
  public String[] getPrefixes() {
    return new String[] { PASSWORD_ENCRYPTED_PREFIX };
  }


  /**
   * Encrypt the password, but only if the password doesn't contain any variables.
   *
   * @param password
   *          The password to encrypt
   * @return The encrypted password or the
   */
  protected final String encryptPasswordIfNotUsingVariablesInternal( String password ) {
    String encrPassword = "";
    List<String> varList = new ArrayList<>();
    StringUtil.getUsedVariables( password, varList, true );
    if ( varList.isEmpty() ) {
      encrPassword = PASSWORD_ENCRYPTED_PREFIX + KettleTwoWayPasswordEncoder.encryptPassword( password );
    } else {
      encrPassword = password;
    }

    return encrPassword;
  }


  /**
   * Decrypts a password if it contains the prefix "Encrypted "
   *
   * @param password
   *          The encrypted password
   * @return The decrypted password or the original value if the password doesn't start with "Encrypted "
   */
  protected final String decryptPasswordOptionallyEncryptedInternal( String password ) {
    if ( !Utils.isEmpty( password ) && password.startsWith( PASSWORD_ENCRYPTED_PREFIX ) ) {
      return KettleTwoWayPasswordEncoder.decryptPassword( password.substring( PASSWORD_ENCRYPTED_PREFIX.length() ) );
    }
    return password;
  }

  // Old Static Methods - should be deprecated


  /**
   * Encrypt the password, but only if the password doesn't contain any variables.
   *
   * @param password
   *          The password to encrypt
   * @return The encrypted password or the
   * @deprecated - Use the instance method through Encr instead of this directly
   */
  public static final String encryptPasswordIfNotUsingVariables( String password ) {
    return getInstance().encryptPasswordIfNotUsingVariablesInternal( password );
  }

  /**
   * Decrypts a password if it contains the prefix "Encrypted "
   *
   * @param password
   *          The encrypted password
   * @return The decrypted password or the original value if the password doesn't start with "Encrypted "
   * @deprecated - Use the instance method through Encr instead of this directly
   */
  public static final String decryptPasswordOptionallyEncrypted( String password ) {
    return getInstance().decryptPasswordOptionallyEncryptedInternal( password );
  }

  /**
   * Deprecared - use the instance method instead
   * @param password
   * @return encrypted password
   * @deprecated - use the instance method through Encr instead of this directly
   */
  public static final String encryptPassword( String password ) {
    return getInstance().encryptPasswordInternal( password );
  }

  /**
   * Deprecated - use Encr instead of this directly
   * @param encrypted
   * @return decrypted password
   * @deprecated
   */
  public static final String decryptPassword( String encrypted ) {
    return getInstance().decryptPasswordInternal( encrypted );
  }



}
