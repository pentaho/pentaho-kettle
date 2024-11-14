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


package org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm;

/**
 * Symmetric algorithm
 *
 * @author Samatar
 * @since 01-4-2011
 */
public class DESSymmetricCrypto implements SymmetricCryptoInterface {

  private static final String ALGORITHM = "DES";

  private static final String DEFAULT_SCHEME = "DES";

  public DESSymmetricCrypto() {
    super();
  }

  public String getAlgorithm() {
    return ALGORITHM;
  }

  public int getAlgorithmType() {
    return SymmetricCryptoMeta.TYPE_ALGORYTHM_AES;
  }

  public String getDefaultScheme() {
    return DEFAULT_SCHEME;
  }

}
