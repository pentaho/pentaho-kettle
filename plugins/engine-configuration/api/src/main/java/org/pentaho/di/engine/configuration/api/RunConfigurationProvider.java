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

/**
 * Created by bmorrise on 3/16/17.
 */
public interface RunConfigurationProvider extends RunConfigurationFactory {
  RunConfiguration getConfiguration();
  RunConfigurationExecutor getExecutor();
  String getType();
  boolean isSupported( String type );
}
