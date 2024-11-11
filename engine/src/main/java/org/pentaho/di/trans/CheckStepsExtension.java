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
