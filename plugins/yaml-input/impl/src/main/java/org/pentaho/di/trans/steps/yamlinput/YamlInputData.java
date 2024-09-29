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


package org.pentaho.di.trans.steps.yamlinput;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 21-06-2007
 */
public class YamlInputData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;

  public int nrInputFields;
  public Object[] readrow;
  public int totalPreviousFields;
  public int totalOutFields;
  public int totalOutStreamFields;

  /**
   * The YAML files to read
   */
  public FileInputList files;
  public FileObject file;
  public int filenr;

  public long rownr;
  public int indexOfYamlField;

  public YamlReader yaml;

  public RowMetaInterface rowMeta;

  public YamlInputData() {
    super();

    this.filenr = 0;
    this.indexOfYamlField = -1;
    this.nrInputFields = -1;
    this.readrow = null;
    this.totalPreviousFields = 0;
    this.file = null;
    this.totalOutFields = 0;
    this.totalOutStreamFields = 0;
  }
}
