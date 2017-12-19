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

package org.pentaho.di.repository;

public enum RepositoryObjectType {

  TRANSFORMATION( "transformation", ".ktr" ), JOB( "job", ".kjb" ), DATABASE( "database", ".kdb" ), SLAVE_SERVER(
    "slave server", ".ksl" ), CLUSTER_SCHEMA( "cluster schema", ".kcs" ), PARTITION_SCHEMA(
    "partition schema", ".kps" ), STEP( "step", ".kst" ), JOB_ENTRY( "job entry", ".kje" ), TRANS_DATA_SERVICE(
    "transformation data service", ".das" ), PLUGIN( "plugin", "" ), UNKNOWN( "unknown", "" );

  // non-standard, Kettle database repository only!
  //
  // USER("user", ".usr"),

  private String typeDescription;
  private String extension;

  private RepositoryObjectType( String typeDescription, String extension ) {
    this.typeDescription = typeDescription;
    this.extension = extension;
  }

  @Override
  public String toString() {
    return typeDescription;
  }

  public String getTypeDescription() {
    return typeDescription;
  }

  public String getExtension() {
    return extension;
  }
}
