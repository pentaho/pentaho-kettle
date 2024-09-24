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
