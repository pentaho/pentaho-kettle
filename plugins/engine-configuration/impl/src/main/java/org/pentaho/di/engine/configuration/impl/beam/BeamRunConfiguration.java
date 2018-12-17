/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.engine.configuration.impl.beam;

import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.api.RunConfigurationUI;
import org.pentaho.metastore.persist.MetaStoreAttribute;
import org.pentaho.metastore.persist.MetaStoreElementType;

/**
 * Created by bmorrise on 3/15/17.
 */
@MetaStoreElementType(
  name = "Beam Run Configuration",
  description = "Defines an Apache Beam Run configuration" )
public class BeamRunConfiguration implements RunConfiguration {

  public static String TYPE = "Beam";

  @MetaStoreAttribute
  private String name;

  @MetaStoreAttribute
  private String description;

  @MetaStoreAttribute
  private String beamJobConfig;

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

  public String getBeamJobConfig() {
    return beamJobConfig;
  }

  public void setBeamJobConfig( String beamJobConfig ) {
    this.beamJobConfig = beamJobConfig;
  }

  public String getType() {
    return TYPE;
  }

  @Override public boolean isReadOnly() {
    return false;
  }

  @Override public RunConfigurationUI getUI() {
    return new BeamRunConfigurationUI( this );
  }
}
