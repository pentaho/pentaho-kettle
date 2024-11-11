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


package org.pentaho.di.trans.steps.pgpencryptstream;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.job.entries.pgpencryptfiles.GPG;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class PGPEncryptStreamData extends BaseStepData implements StepDataInterface {
  public int indexOfField;
  public RowMetaInterface previousRowMeta;
  public RowMetaInterface outputRowMeta;
  public int NrPrevFields;

  public GPG gpg;
  public String keyName;
  public int indexOfKeyName;

  public PGPEncryptStreamData() {
    super();
    this.indexOfField = -1;
    this.gpg = null;
    this.keyName = null;
    this.indexOfKeyName = -1;
  }

}
