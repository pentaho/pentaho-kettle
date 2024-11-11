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


package org.pentaho.di.trans.steps.mapping;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.SingleThreadedTransExecutor;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingData extends BaseStepData implements StepDataInterface {
  public Trans mappingTrans;
  public MappingInput mappingInput;
  public MappingOutput mappingOutput;
  public List<Integer> renameFieldIndexes;
  public List<String> renameFieldNames;
  public boolean wasStarted;
  public TransMeta mappingTransMeta;
  public RowMetaInterface outputRowMeta;
  public List<MappingValueRename> inputRenameList;
  protected int linesReadStepNr = -1;
  protected int linesInputStepNr = -1;
  protected int linesWrittenStepNr = -1;
  protected int linesOutputStepNr = -1;
  protected int linesUpdatedStepNr = -1;
  protected int linesRejectedStepNr = -1;
  public SingleThreadedTransExecutor singleThreadedTransExcecutor;

  public MappingData() {
    super();
    mappingTrans = null;
    wasStarted = false;
    inputRenameList = new ArrayList<MappingValueRename>();
  }

  public Trans getMappingTrans() {
    return mappingTrans;
  }

  public void setMappingTrans( Trans mappingTrans ) {
    this.mappingTrans = mappingTrans;
  }
}
