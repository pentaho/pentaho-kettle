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

package org.pentaho.di.partition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.resource.ResourceHolderInterface;
import org.pentaho.di.shared.SharedObjectInterface;
import org.w3c.dom.Node;

/**
 * A partition schema allow you to partition a step according into a number of partitions that run independendly. It
 * allows us to "map"
 *
 * @author Matt
 */
public class PartitionSchema extends ChangedFlag implements Cloneable, SharedObjectInterface,
  ResourceHolderInterface, RepositoryElementInterface, XMLInterface {
  public static final String XML_TAG = "partitionschema";

  public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.PARTITION_SCHEMA;

  private String name;

  private List<String> partitionIDs;
  private boolean shared;

  private ObjectId id;

  private boolean dynamicallyDefined;
  private String numberOfPartitionsPerSlave;

  private ObjectRevision objectRevision;

  private Date changedDate;

  public PartitionSchema() {
    this.partitionIDs = new ArrayList<String>();
    this.changedDate = new Date();
  }

  /**
   * @param name
   * @param partitionIDs
   */
  public PartitionSchema( String name, List<String> partitionIDs ) {
    this.name = name;
    this.partitionIDs = partitionIDs;
    this.changedDate = new Date();
  }

  public Object clone() {
    PartitionSchema partitionSchema = new PartitionSchema();
    partitionSchema.replaceMeta( this );
    partitionSchema.setObjectId( null );
    return partitionSchema;
  }

  public void replaceMeta( PartitionSchema partitionSchema ) {
    this.name = partitionSchema.name;
    this.partitionIDs = new ArrayList<String>();
    this.partitionIDs.addAll( partitionSchema.partitionIDs );

    this.dynamicallyDefined = partitionSchema.dynamicallyDefined;
    this.numberOfPartitionsPerSlave = partitionSchema.numberOfPartitionsPerSlave;

    // this.shared = partitionSchema.shared;
    this.setObjectId( partitionSchema.id );
    this.setChanged( true );
  }

  public String toString() {
    return name;
  }

  public boolean equals( Object obj ) {
    if ( obj == null || name == null ) {
      return false;
    }
    return name.equals( ( (PartitionSchema) obj ).name );
  }

  public int hashCode() {
    return name.hashCode();
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the partitionIDs
   */
  public List<String> getPartitionIDs() {
    return partitionIDs;
  }

  /**
   * @param partitionIDs
   *          the partitionIDs to set
   */
  public void setPartitionIDs( List<String> partitionIDs ) {
    this.partitionIDs = partitionIDs;
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 200 );

    xml.append( "      " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );
    xml.append( "        " ).append( XMLHandler.addTagValue( "name", name ) );
    for ( int i = 0; i < partitionIDs.size(); i++ ) {
      xml.append( "        " ).append( XMLHandler.openTag( "partition" ) ).append( Const.CR );
      xml.append( "          " ).append( XMLHandler.addTagValue( "id", partitionIDs.get( i ) ) );
      xml.append( "        " ).append( XMLHandler.closeTag( "partition" ) ).append( Const.CR );
    }

    xml.append( "        " ).append( XMLHandler.addTagValue( "dynamic", dynamicallyDefined ) );
    xml
      .append( "        " ).append(
        XMLHandler.addTagValue( "partitions_per_slave", numberOfPartitionsPerSlave ) );

    xml.append( "      " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );
    return xml.toString();
  }

  public PartitionSchema( Node partitionSchemaNode ) {
    changedDate = new Date();
    name = XMLHandler.getTagValue( partitionSchemaNode, "name" );

    int nrIDs = XMLHandler.countNodes( partitionSchemaNode, "partition" );
    partitionIDs = new ArrayList<String>();
    for ( int i = 0; i < nrIDs; i++ ) {
      Node partitionNode = XMLHandler.getSubNodeByNr( partitionSchemaNode, "partition", i );
      partitionIDs.add( XMLHandler.getTagValue( partitionNode, "id" ) );
    }

    dynamicallyDefined = "Y".equalsIgnoreCase( XMLHandler.getTagValue( partitionSchemaNode, "dynamic" ) );
    numberOfPartitionsPerSlave = XMLHandler.getTagValue( partitionSchemaNode, "partitions_per_slave" );
  }

  /**
   * @return the shared
   */
  public boolean isShared() {
    return shared;
  }

  /**
   * @param shared
   *          the shared to set
   */
  public void setShared( boolean shared ) {
    this.shared = shared;
  }

  /**
   * @return the id
   */
  public ObjectId getObjectId() {
    return id;
  }

  public String getDescription() {
    return null;
  }

  public String getHolderType() {
    return "PARTITION_SCHEMA";
  }

  public String getTypeId() {
    return null;
  }

  /**
   * @return the dynamicallyDefined
   */
  public boolean isDynamicallyDefined() {
    return dynamicallyDefined;
  }

  /**
   * @param dynamicallyDefined
   *          the dynamicallyDefined to set
   */
  public void setDynamicallyDefined( boolean dynamicallyDefined ) {
    this.dynamicallyDefined = dynamicallyDefined;
  }

  /**
   * @return the numberOfStepCopiesPerSlave
   */
  public String getNumberOfPartitionsPerSlave() {
    return numberOfPartitionsPerSlave;
  }

  /**
   * @param numberOfPartitionsPerSlave
   *          the number of partitions per slave to set...
   */
  public void setNumberOfPartitionsPerSlave( String numberOfPartitionsPerSlave ) {
    this.numberOfPartitionsPerSlave = numberOfPartitionsPerSlave;
  }

  public void expandPartitionsDynamically( int nrSlaves, VariableSpace space ) {
    // Let's change the partition list...
    //
    partitionIDs.clear();

    // What's the number of partitions to create per slave server?
    // --> defaults to 1
    //
    int nrPartitionsPerSlave = Const.toInt( space.environmentSubstitute( numberOfPartitionsPerSlave ), 1 );
    int totalNumberOfPartitions = nrSlaves * nrPartitionsPerSlave;
    for ( int partitionNumber = 0; partitionNumber < totalNumberOfPartitions; partitionNumber++ ) {
      partitionIDs.add( "PDyn" + partitionNumber );
    }

    dynamicallyDefined = false;
    numberOfPartitionsPerSlave = null;
  }

  /**
   * Slaves don't need ALL the partitions, they just need a few.<br>
   * So we should only retain those partitions that are of interest to the slave server.<br>
   * Divide the number of partitions (6) through the number of slaves (2)<br>
   * That gives you 0, 1, 2, 3, 4, 5<br>
   * Slave 0 : 0, 2, 4<br>
   * Slave 1 : 1, 3, 5<br>
   * --> slaveNumber == partitionNr % slaveCount<br>
   *
   * @param slaveCount
   * @param slaveNumber
   */
  public void retainPartitionsForSlaveServer( int slaveCount, int slaveNumber ) {
    List<String> ids = new ArrayList<String>();
    int partitionCount = partitionIDs.size();

    for ( int i = 0; i < partitionCount; i++ ) {
      if ( slaveNumber == ( i % slaveCount ) ) {
        ids.add( partitionIDs.get( i ) );
      }
    }
    partitionIDs.clear();
    partitionIDs.addAll( ids );
  }

  /**
   * Not supported for Partition schema, return the root.
   */
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return new RepositoryDirectory();
  }

  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {
  }

  public RepositoryObjectType getRepositoryElementType() {
    return REPOSITORY_ELEMENT_TYPE;
  }

  public void setObjectId( ObjectId id ) {
    this.id = id;
  }

  public ObjectRevision getObjectRevision() {
    return objectRevision;
  }

  public void setObjectRevision( ObjectRevision objectRevision ) {
    this.objectRevision = objectRevision;
  }

  public void setDescription( String description ) {
    // NOT USED
  }

  /**
   * @return the changedDate
   */
  public Date getChangedDate() {
    return changedDate;
  }

  /**
   * @param changedDate
   *          the changedDate to set
   */
  public void setChangedDate( Date changedDate ) {
    this.changedDate = changedDate;
  }
}
