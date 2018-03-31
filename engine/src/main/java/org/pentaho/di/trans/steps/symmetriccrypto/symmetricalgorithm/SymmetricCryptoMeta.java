/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Symmetric algorithm
 *
 * @author Samatar
 * @since 01-4-2011
 */
public class SymmetricCryptoMeta {

  private static Class<?> PKG = SymmetricCryptoMeta.class; // for i18n purposes, needed by Translator2!!

  private SymmetricCryptoInterface cryptographyInterface;
  private static SymmetricCryptoInterface[] allSymmetricCryptoInterface;

  public static final String[] TYPE_ALGORYTHM_CODE = new String[] { "DES", "DESede", "AES" };

  /**
   * Indicates that the algorithm doesn't point to a type of algorithm yet.
   */
  public static final int TYPE_ALGORYTHM_NONE = 0;
  public static final int TYPE_ALGORYTHM_DES = 1;
  public static final int TYPE_ALGORYTHM_TRIPLE_DES = 2;
  public static final int TYPE_ALGORYTHM_AES = 3;

  public static final String ALGORYTHM_DES = "DES";
  public static final String ALGORYTHM_TRIPLE_DES = "DESede";
  public static final String ALGORYTHM_AES = "AES";

  /**
   * Construct a new Database Connection
   *
   * @param inf
   *          The Database Connection Info to construct the connection with.
   */
  public SymmetricCryptoMeta( String algorythm ) throws CryptoException {
    cryptographyInterface = getSymmetricCryptoInterface( algorythm );
  }

  /**
   * Search for the right type of DatabaseInterface object and clone it.
   *
   * @param databaseType
   *          the type of DatabaseInterface to look for (description)
   * @return The requested DatabaseInterface
   *
   * @throws CryptoException
   *           when the type could not be found or referenced.
   */
  public static final SymmetricCryptoInterface getSymmetricCryptoInterface( String cryptoname ) throws CryptoException {
    return findSymmetricCryptoInterface( cryptoname );

  }

  /**
   * Search for the right type of DatabaseInterface object and return it.
   *
   * @param databaseType
   *          the type of DatabaseInterface to look for (description)
   * @return The requested DatabaseInterface
   *
   * @throws CryptoException
   *           when the type could not be found or referenced.
   */
  private static final synchronized SymmetricCryptoInterface findSymmetricCryptoInterface( String cryptograhname ) throws CryptoException {
    SymmetricCryptoInterface[] di = getSymmetricCryptoInterfaces();
    for ( int i = 0; i < di.length; i++ ) {
      if ( di[i].getAlgorithm().equalsIgnoreCase( cryptograhname ) ) {
        return di[i];
      }
    }

    throw new CryptoException( BaseMessages.getString(
      PKG, "SymmetricCryptoMeta.CouldNotFoundAlgorithm", cryptograhname ) );
  }

  public static final synchronized SymmetricCryptoInterface[] getSymmetricCryptoInterfaces() {
    if ( allSymmetricCryptoInterface != null ) {
      return allSymmetricCryptoInterface;
    }

    Class<?>[] ic = SymmetricCryptoInterface.implementingClasses;
    allSymmetricCryptoInterface = new SymmetricCryptoInterface[ic.length];
    for ( int i = 0; i < ic.length; i++ ) {
      try {
        Class.forName( ic[i].getName() );
        allSymmetricCryptoInterface[i] = (SymmetricCryptoInterface) ic[i].newInstance();
      } catch ( Exception e ) {
        throw new RuntimeException( "Error creating class for : " + ic[i].getName(), e );
      }
    }
    return allSymmetricCryptoInterface;
  }

  public String getAlgorithm() {
    return cryptographyInterface.getAlgorithm();
  }

  public int getAlgorithmType() {
    return cryptographyInterface.getAlgorithmType();
  }

  public String getDefaultScheme() {
    return cryptographyInterface.getDefaultScheme();
  }

  public static int getAlgorithmTypeFromCode( String code ) {
    if ( !Utils.isEmpty( code ) ) {
      int nr = TYPE_ALGORYTHM_CODE.length;
      for ( int i = 0; i < nr; i++ ) {
        if ( TYPE_ALGORYTHM_CODE[i].equals( code ) ) {
          return i + 1;
        }
      }
    }
    return TYPE_ALGORYTHM_NONE;
  }

}
