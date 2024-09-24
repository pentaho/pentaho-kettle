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

package org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm;

/**
 * Symmetric algorithm
 *
 * @author Samatar
 * @since 01-4-2011
 */
public interface SymmetricCryptoInterface extends Cloneable {

  public static final Class<?>[] implementingClasses = {
    DESSymmetricCrypto.class, TripleDESSymmetricCrypto.class, AESSymmetricCrypto.class, };

  public String getAlgorithm();

  public int getAlgorithmType();

  public String getDefaultScheme();
}
