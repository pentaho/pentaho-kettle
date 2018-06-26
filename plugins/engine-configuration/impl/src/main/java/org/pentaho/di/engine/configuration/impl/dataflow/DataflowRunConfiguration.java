/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */
package org.pentaho.di.engine.configuration.impl.dataflow;

import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

/**
 * Created by ccaspanello on 6/13/18.
 */

@MetaStoreElementType(
  name = "Dataflow Run Configuration",
  description = "Defines an Dataflow run configuration" )
public class DataflowRunConfiguration implements RunConfiguration {

  public static String TYPE = "Dataflow";

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description;

  @MetaStoreAttribute
  private String applicationJar = "";

  @MetaStoreAttribute
  private String runner = "";

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getType() {
    return TYPE;
  }

  public String getApplicationJar() {
    return applicationJar;
  }

  public void setApplicationJar( String applicationJar ) {
    this.applicationJar = applicationJar;
  }

  public String getRunner() {
    return runner;
  }

  public void setRunner( String runner ) {
    this.runner = runner;
  }

  @Override public boolean isReadOnly() {
    return false;
  }

  @Override public RunConfigurationUI getUI() {
    return new DataflowRunConfigurationUI( this );
  }
}
