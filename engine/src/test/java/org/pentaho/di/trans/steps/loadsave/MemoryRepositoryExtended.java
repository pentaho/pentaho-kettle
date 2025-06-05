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


package org.pentaho.di.trans.steps.loadsave;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryObjectInterface;

import java.util.List;
import java.util.stream.Collectors;
import org.json.simple.parser.ParseException;

/**
 * Used for testing Extended Repository methods
 *
 */
public class MemoryRepositoryExtended extends MemoryRepository implements RepositoryExtended {
  public MemoryRepositoryExtended() {
    super();
  }

  public MemoryRepositoryExtended( String json ) throws ParseException {
    super( json );
  }

  @Override
  @Deprecated
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree( boolean eager ) throws KettleException {
    return null;
  }

  @Override
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree(
      String path,
      String filter,
      int depth,
      boolean showHidden,
      boolean includeEmptyFolder,
      boolean includeAcls )
    throws KettleException {
    return null;
  }

  @Override
  public ObjectId renameRepositoryDirectory( final ObjectId dirId, final RepositoryDirectoryInterface newParent,
                                             final String newName, final boolean renameHomeDirectories )
      throws KettleException {
    return null;
  }

  @Override
  public void deleteRepositoryDirectory( final RepositoryDirectoryInterface dir, final boolean deleteHomeDirectories )
          throws KettleException {
  }

  @Override
  public List<RepositoryObjectInterface> getChildren( String path, String filter ) {
    return null;
  }

  @Override
  public List<DatabaseMeta> getConnections( boolean cached ) throws KettleException {
    return elements.values().stream().filter( e -> e instanceof DatabaseMeta )
      .map( e -> (DatabaseMeta) e ).collect( Collectors.toList() );
  }

  @Override
  public List<SlaveServer> getSlaveServers( boolean cached ) throws KettleException {
    return elements.values().stream().filter( e -> e instanceof SlaveServer )
      .map( e -> (SlaveServer) e ).collect( Collectors.toList() );
  }

  @Override
  public List<PartitionSchema> getPartitions( boolean cached ) throws KettleException {
    return elements.values().stream().filter( e -> e instanceof PartitionSchema )
      .map( e -> (PartitionSchema) e ).collect( Collectors.toList() );
  }

  @Override
  public List<ClusterSchema> getClusters( boolean cached ) throws KettleException {
    return elements.values().stream().filter( e -> e instanceof ClusterSchema )
      .map( e -> (ClusterSchema) e ).collect( Collectors.toList() );
  }


}
