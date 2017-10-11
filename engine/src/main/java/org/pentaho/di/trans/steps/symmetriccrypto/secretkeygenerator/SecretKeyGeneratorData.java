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
