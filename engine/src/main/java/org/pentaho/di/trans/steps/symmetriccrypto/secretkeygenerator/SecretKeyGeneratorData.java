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


package org.pentaho.di.trans.steps.symmetriccrypto.secretkeygenerator;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCrypto;

/**
 * Generate secret key. for symmetric algorithms
 *
 * @author Samatar
 * @since 01-4-2011
 */
public class SecretKeyGeneratorData extends BaseStepData implements StepDataInterface {

  public int[] algorithm;
  public String[] scheme;
  public int[] secretKeyCount;
  public int[] secretKeyLen;
  public int nr;

  public RowMetaInterface outputRowMeta;

  public boolean addAlgorithmOutput;
  public boolean addSecretKeyLengthOutput;

  public SymmetricCrypto[] cryptoTrans;

  public boolean readsRows;
  public int prevNrField;

  public SecretKeyGeneratorData() {
    super();
    addAlgorithmOutput = false;
    addSecretKeyLengthOutput = false;
  }
}
