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

package org.pentaho.di.repository.pur;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

public class PartitionDelegate extends AbstractDelegate implements ITransformer,
    SharedObjectAssembler<PartitionSchema>, java.io.Serializable {

  private static final long serialVersionUID = -6069812592810099251L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final String NODE_ROOT = "partitionSchema"; //$NON-NLS-1$

  private static final String PROP_DYNAMIC_DEFINITION = "DYNAMIC_DEFINITION"; //$NON-NLS-1$

  private static final String PROP_PARTITIONS_PER_SLAVE = "PARTITIONS_PER_SLAVE"; //$NON-NLS-1$

  private static final String NODE_ATTRIBUTES = "attributes"; //$NON-NLS-1$

  private static final String PROP_NB_PARTITION_SCHEMA = "NB_PARTITION_SCHEMA"; //$NON-NLS-1$
  // ~ Instance fields =================================================================================================

  private PurRepository repo;

  // ~ Constructors ====================================================================================================

  public PartitionDelegate( final PurRepository repo ) {
    super();
    this.repo = repo;
  }

  // ~ Methods =========================================================================================================

  public RepositoryElementInterface dataNodeToElement( DataNode rootNode ) throws KettleException {
    PartitionSchema partitionSchema = new PartitionSchema();
    dataNodeToElement( rootNode, partitionSchema );
    return partitionSchema;
  }

  public void dataNodeToElement( DataNode rootNode, RepositoryElementInterface element ) throws KettleException {
    PartitionSchema partitionSchema = (PartitionSchema) element;
    partitionSchema.setDynamicallyDefined( rootNode.getProperty( PROP_DYNAMIC_DEFINITION ).getBoolean() );
    partitionSchema.setNumberOfPartitionsPerSlave( getString( rootNode, PROP_PARTITIONS_PER_SLAVE ) );
    // Also, load all the properties we can find...

    DataNode attrNode = rootNode.getNode( NODE_ATTRIBUTES );
    long partitionSchemaSize = attrNode.getProperty( PROP_NB_PARTITION_SCHEMA ).getLong();

    for ( int i = 0; i < partitionSchemaSize; i++ ) {
      DataProperty property = attrNode.getProperty( String.valueOf( i ) );
      partitionSchema.getPartitionIDs().add( Const.NVL( property.getString(), "" ) );
    }

  }

  public DataNode elementToDataNode( RepositoryElementInterface element ) throws KettleException {
    PartitionSchema partitionSchema = (PartitionSchema) element;
    DataNode rootNode = new DataNode( NODE_ROOT );

    rootNode.setProperty( PROP_DYNAMIC_DEFINITION, partitionSchema.isDynamicallyDefined() );
    rootNode.setProperty( PROP_PARTITIONS_PER_SLAVE, partitionSchema.getNumberOfPartitionsPerSlave() );

    // Save the cluster-partition relationships
    DataNode attrNode = rootNode.addNode( NODE_ATTRIBUTES );
    attrNode.setProperty( PROP_NB_PARTITION_SCHEMA, partitionSchema.getPartitionIDs().size() );
    for ( int i = 0; i < partitionSchema.getPartitionIDs().size(); i++ ) {
      attrNode.setProperty( String.valueOf( i ), partitionSchema.getPartitionIDs().get( i ) );
    }
    return rootNode;
  }

  public PartitionSchema assemble( RepositoryFile file, NodeRepositoryFileData data, VersionSummary version )
    throws KettleException {
    PartitionSchema partitionSchema = (PartitionSchema) dataNodeToElement( data.getNode() );
    partitionSchema.setName( file.getTitle() );
    partitionSchema.setObjectId( new StringObjectId( file.getId().toString() ) );
    partitionSchema.setObjectRevision( repo.createObjectRevision( version ) );
    partitionSchema.clearChanged();
    return partitionSchema;
  }
}
