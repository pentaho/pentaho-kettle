/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans;

import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.step.StepMeta;

/**
 * A step "meta" that contains a reference to a sub-transformation.
 */
public interface ISubTransAwareMeta {

  ObjectLocationSpecificationMethod getSpecificationMethod();

  String getFileName();

  String getDirectoryPath();

  String getTransName();

  ObjectId getTransObjectId();

  StepMeta getParentStepMeta();

}
