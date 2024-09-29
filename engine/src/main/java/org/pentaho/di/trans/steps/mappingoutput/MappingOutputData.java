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

package org.pentaho.di.trans.steps.mappingoutput;

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MappingOutputData extends BaseStepData implements StepDataInterface {

  public StepInterface[] targetSteps;
  public String[] outputField;
  public String[] outputMapping;
  public RowMetaInterface outputRowMeta;
  public List<MappingValueRename> inputValueRenames;
  public List<MappingValueRename> outputValueRenames;

  public MappingOutputData() {
    super();
  }

}
