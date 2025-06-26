/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.configuration.api;

import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;

/**
 * Created by bmorrise on 3/16/17.
 */
public interface RunConfigurationExecutor {
  void execute( RunConfiguration runConfiguration, ExecutionConfiguration configuration, AbstractMeta meta,
                VariableSpace variableSpace, Repository repository ) throws KettleException;
}
