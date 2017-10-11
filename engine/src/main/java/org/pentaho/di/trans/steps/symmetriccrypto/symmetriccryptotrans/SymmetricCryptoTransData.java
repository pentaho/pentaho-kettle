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

package org.pentaho.di.trans.steps.symmetriccrypto.symmetriccryptotrans;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCrypto;
import org.pentaho.di.trans.steps.symmetriccrypto.symmetricalgorithm.SymmetricCryptoMeta;

/**
 * Symmetric algorithm Executes a SymmetricCryptoTrans on the values in the input stream. Selected calculated values can
 * then be put on the output stream.
 *
 * @author Samatar
 * @since 5-apr-2003
 *
 */
public class SymmetricCryptoTransData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public int indexOfSecretkeyField;
  public int indexOfMessage;
  public SymmetricCryptoMeta CryptMeta;
  public SymmetricCrypto Crypt;

  public SymmetricCryptoTransData() {
    super();
    indexOfSecretkeyField = -1;
    indexOfMessage = -1;
  }

}
