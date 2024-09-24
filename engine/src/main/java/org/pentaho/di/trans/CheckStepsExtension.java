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

package org.pentaho.di.trans;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

import java.util.List;

public class CheckStepsExtension {
  private final List<CheckResultInterface> remarks;
  private final VariableSpace variableSpace;
  private final TransMeta transMeta;
  private final StepMeta[] stepMetas;
  private final Repository repository;
  private final IMetaStore metaStore;

  public CheckStepsExtension(
    List<CheckResultInterface> remarks,
    VariableSpace space,
    TransMeta transMeta,
    StepMeta[] stepMetas,
    Repository repository,
    IMetaStore metaStore ) {
    this.remarks = remarks;
    this.variableSpace = space;
    this.transMeta = transMeta;
    this.stepMetas = stepMetas;
    this.repository = repository;
    this.metaStore = metaStore;
  }

  public List<CheckResultInterface> getRemarks() {
    return remarks;
  }

  public VariableSpace getVariableSpace() {
    return variableSpace;
  }

  public TransMeta getTransMeta() {
    return transMeta;
  }

  public StepMeta[] getStepMetas() {
    return stepMetas;
  }

  public Repository getRepository() {
    return repository;
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }
}
