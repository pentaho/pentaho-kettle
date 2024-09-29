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
