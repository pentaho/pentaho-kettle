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


package org.pentaho.di.trans.steps.checksum;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.checksum.CheckSum.FieldToBytesConverter;
import org.pentaho.di.trans.steps.checksum.CheckSum.GenericChecksumCalculator;

/**
 * Data class for the Cheksum class
 *
 * @author Samatar Hassan
 * @since 16-06-2008
 */
public class CheckSumData extends BaseStepData implements StepDataInterface {

  public FieldToBytesConverter[] fieldConverters;

  public RowMetaInterface outputRowMeta;
  public int nrInfields;

  public byte[] fieldSeparatorStringBytes;

  public GenericChecksumCalculator<?> checksumCalculator;

  public CheckSumData() {
    super();
    this.nrInfields = 0;
  }

}
