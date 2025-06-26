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

import java.util.List;

/**
 * Created by bmorrise on 3/17/17.
 */
public interface RunConfigurationFactory {
  List<RunConfiguration> load();
  RunConfiguration load( String name );
  boolean save( RunConfiguration runConfiguration );
  boolean delete( String name );
  void deleteAll();
  List<String> getNames();
  List<String> getNames( String type );
}
