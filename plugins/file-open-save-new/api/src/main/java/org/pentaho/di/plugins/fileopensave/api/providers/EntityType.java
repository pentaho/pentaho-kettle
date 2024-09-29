/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.api.providers;

public enum EntityType {
  UNKNOWN( 0, false, false ),
  LOCAL_DIRECTORY( 1, false, true ),
  LOCAL_FILE( 2, true, false ),
  RECENT_FILE( 3, true, false ),
  REPOSITORY_DIRECTORY( 4, false, true ),
  REPOSITORY_FILE( 5, true, false ),
  REPOSITORY_OBJECT( 6, false, false ),
  VFS_DIRECTORY( 7, false, true ),
  VFS_FILE( 8, true, false ),
  VFS_LOCATION( 9, false, true ),
  TREE( 10, true, false ),
  NAMED_CLUSTER_DIRECTORY( 11, false, true ),
  NAMED_CLUSTER_LOCATION( 12, false, true ),
  NAMED_CLUSTER_FILE( 13, true, false ),
  TEST_FILE( 14, true, false ), //Used in tests only
  TEST_DIRECTORY( 15, false, true );  //Used in tests only

  private int val;
  private boolean isDirectory;
  private boolean isFile;

  private static final EntityType[] valueMap = new EntityType[ 16 ];

  static {
    for ( EntityType entityType : values() ) {
      valueMap[ entityType.val ] = entityType;
    }
  }

  EntityType( int val, boolean isFile, boolean isDirectory ) {
    this.val = val;
    this.isDirectory = isDirectory;
    this.isFile = isFile;
  }

  public static EntityType fromValue( int i ) {
    if ( i < 0 || i >= valueMap.length ) {
      throw new IllegalArgumentException( "EntityType integer out of range (0 - " + ( valueMap.length - 1 ) + ")" );
    }
    return valueMap[ i ];
  }

  public int getValue() {
    return val;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public boolean isFile() {
    return isFile;
  }

  public boolean isRepositoryType() {
    return this.name().startsWith( "REPOSITORY" );
  }

  public boolean isVFSType() {
    return this.name().startsWith( "VFS" );
  }

  public boolean isLocalType() { return this.name().startsWith( "LOCAL"); }

  public boolean isNamedClusterType() {
    return this.name().startsWith( "NAMED_CLUSTER" );
  }

  public EntityType getFileTypeAssociatedWithDirType() {
    switch( this ) {
      case TEST_DIRECTORY:
        return EntityType.TEST_FILE;
      case VFS_DIRECTORY:
        return EntityType.VFS_FILE;
      case LOCAL_DIRECTORY:
        return EntityType.LOCAL_FILE;
      case REPOSITORY_DIRECTORY:
        return EntityType.REPOSITORY_FILE;
      case NAMED_CLUSTER_DIRECTORY:
        return EntityType.NAMED_CLUSTER_FILE;
      default:
        throw new IllegalArgumentException( "Incoming type is not a directory" );
    }
  }

}

