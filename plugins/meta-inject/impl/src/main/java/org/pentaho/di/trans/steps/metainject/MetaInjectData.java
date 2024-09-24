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

package org.pentaho.di.trans.steps.metainject;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class MetaInjectData extends BaseStepData implements StepDataInterface {
  public TransMeta transMeta;
  public Map<String, StepMetaInjectionInterface> stepInjectionMap;
  public Map<String, StepMetaInterface> stepInjectionMetasMap;
  public Map<String, List<RowMetaAndData>> rowMap;
  public boolean streaming;
  public String streamingSourceStepname;
  public String streamingTargetStepname;

  public MetaInjectData() {
    super();
  }
}
