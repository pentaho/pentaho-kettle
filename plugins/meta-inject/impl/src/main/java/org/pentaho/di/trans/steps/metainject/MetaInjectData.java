/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
