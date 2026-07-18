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



package org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm;

/**
 * Symmetric algorithm
 *
 * @author Samatar
 * @since 01-4-2011
 */
public class AESSymmetricCrypto implements SymmetricCryptoInterface {

  private static final String ALGORITHM = "AES";

  private static final String DEFAULT_SCHEME = "AES";

  public AESSymmetricCrypto() {
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
