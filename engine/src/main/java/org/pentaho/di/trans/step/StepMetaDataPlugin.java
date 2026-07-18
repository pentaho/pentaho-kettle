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



package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;

public interface StepMetaDataPlugin {

  public void saveToRepository( Repository repository, ObjectId transformationId, ObjectId stepId ) throws KettleException;

  public void loadFromRepository( Repository repository, ObjectId transformationId, ObjectId stepId ) throws KettleException;

}
