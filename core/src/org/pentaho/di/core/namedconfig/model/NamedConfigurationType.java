/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.namedconfig.model;

public enum NamedConfigurationType {

  HADOOP_CLUSTER( "hadoop-cluster", "Hadoop Cluster" );

  private String type;
  private String description;

  private NamedConfigurationType( String type, String description ) {
    this.type = type;
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }

  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }
}
