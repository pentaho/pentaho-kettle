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

package org.pentaho.di.trans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.partition.PartitionSchema;
import org.w3c.dom.Node;

/**
 * This class keeps track of which step copy in which transformation is responsible for handling a certain partition nr.
 * This distribution is created BEFORE the slave transformations are sent to the slave servers. As such, it will be easy
 * to link a step copy on a certain slave server uniquely to a certain partition. That is to say, it will be done the
 * same way accross the complete cluster.
 *
 * @author matt
 *
 */
public class SlaveStepCopyPartitionDistribution {

  public class SlaveStepCopy implements Comparable<SlaveStepCopy> {
    private String slaveServerName;
    private String partitionSchemaName;
    private int stepCopyNr;

    /**
     * @param slaveServerName
     * @param partitionSchemaName
     * @param stepCopyNr
     */
    public SlaveStepCopy( String slaveServerName, String partitionSchemaName, int stepCopyNr ) {
      super();
      this.slaveServerName = slaveServerName;
      this.partitionSchemaName = partitionSchemaName;
      this.stepCopyNr = stepCopyNr;
    }

    public String toString() {
      return slaveServerName + "/" + partitionSchemaName + "." + stepCopyNr;
    }

    public boolean equals( Object obj ) {
      SlaveStepCopy copy = (SlaveStepCopy) obj;
      return slaveServerName.equals( copy.slaveServerName )
        && partitionSchemaName.equals( copy.partitionSchemaName ) && stepCopyNr == copy.stepCopyNr;
    }

    public int hashCode() {
      try {
        return slaveServerName.hashCode()
          ^ partitionSchemaName.hashCode() ^ Integer.valueOf( stepCopyNr ).hashCode();
      } catch ( NullPointerException e ) {
        throw new RuntimeException( e );
      }
    }

    public int compareTo( SlaveStepCopy o ) {
      int cmp = slaveServerName.compareTo( o.slaveServerName );
      if ( cmp != 0 ) {
        return cmp;
      }
      cmp = partitionSchemaName.compareTo( o.partitionSchemaName );
      if ( cmp != 0 ) {
        return cmp;
      }
      return stepCopyNr - o.stepCopyNr;
    }

    /**
     * @return the slaveServerName
     */
    public String getSlaveServerName() {
      return slaveServerName;
    }

    /**
     * @param slaveServerName
     *          the slaveServerName to set
     */
    public void setSlaveServerName( String slaveServerName ) {
      this.slaveServerName = slaveServerName;
    }

    /**
     * @return the partition schema name
     */
    public String getPartitionSchemaName() {
      return partitionSchemaName;
    }

    /**
     * @param partitionSchemaName
     *          the partition schema name to set
     */
    public void setStepName( String partitionSchemaName ) {
      this.partitionSchemaName = partitionSchemaName;
    }

    /**
     * @return the stepCopyNr
     */
    public int getStepCopyNr() {
      return stepCopyNr;
    }

    /**
     * @param stepCopyNr
     *          the stepCopyNr to set
     */
    public void setStepCopyNr( int stepCopyNr ) {
      this.stepCopyNr = stepCopyNr;
    }
  }

  public static final String XML_TAG = "slave-step-copy-partition-distribution";

  private Map<SlaveStepCopy, Integer> distribution;
  private List<PartitionSchema> originalPartitionSchemas;

  public SlaveStepCopyPartitionDistribution() {
    distribution = new Hashtable<SlaveStepCopy, Integer>();
  }

  /**
   * Add a partition number to the distribution for re-use at runtime.
   *
   * @param slaveServerName
   * @param partitionSchemaName
   * @param stepCopyNr
   * @param partitionNr
   */
  public void addPartition( String slaveServerName, String partitionSchemaName, int stepCopyNr, int partitionNr ) {
    distribution.put( new SlaveStepCopy( slaveServerName, partitionSchemaName, stepCopyNr ), partitionNr );
  }

  /**
   * Add a partition number to the distribution if it doesn't already exist.
   *
   * @param slaveServerName
   * @param partitionSchemaName
   * @param stepCopyNr
   * @return The found or created partition number
   */
  public int addPartition( String slaveServerName, String partitionSchemaName, int stepCopyNr ) {
    Integer partitionNr = distribution.get( new SlaveStepCopy( slaveServerName, partitionSchemaName, stepCopyNr ) );
    if ( partitionNr == null ) {
      // Not found: add it.
      //
      int nr = 0;
      for ( SlaveStepCopy slaveStepCopy : distribution.keySet() ) {
        if ( slaveStepCopy.partitionSchemaName.equals( partitionSchemaName ) ) {
          nr++;
        }
      }
      partitionNr = Integer.valueOf( nr );
      addPartition( slaveServerName, partitionSchemaName, stepCopyNr, nr );
    }
    return partitionNr.intValue();
  }

  private int getPartition( SlaveStepCopy slaveStepCopy ) {
    Integer integer = distribution.get( slaveStepCopy );
    if ( integer == null ) {
      return -1;
    }
    return integer;
  }

  public int getPartition( String slaveServerName, String partitionSchemaName, int stepCopyNr ) {
    return getPartition( new SlaveStepCopy( slaveServerName, partitionSchemaName, stepCopyNr ) );
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 200 );

    xml.append( "  " ).append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );

    List<SlaveStepCopy> list = new ArrayList<SlaveStepCopy>( distribution.keySet() );
    Collections.sort( list );

    for ( SlaveStepCopy copy : list ) {
      int partition = getPartition( copy );

      xml.append( "    " ).append( XMLHandler.openTag( "entry" ) ).append( Const.CR );
      xml.append( "      " ).append( XMLHandler.addTagValue( "slavename", copy.slaveServerName ) );
      xml
        .append( "      " ).append(
          XMLHandler.addTagValue( "partition_schema_name", copy.partitionSchemaName ) );
      xml.append( "      " ).append( XMLHandler.addTagValue( "stepcopy", copy.stepCopyNr ) );
      xml.append( "      " ).append( XMLHandler.addTagValue( "partition", partition ) );

      xml.append( "    " ).append( XMLHandler.closeTag( "entry" ) ).append( Const.CR );
    }

    if ( originalPartitionSchemas != null ) {
      xml.append( "    " ).append( XMLHandler.openTag( "original-partition-schemas" ) ).append( Const.CR );
      for ( PartitionSchema partitionSchema : originalPartitionSchemas ) {
        xml.append( partitionSchema.getXML() );
      }
      xml.append( "    " ).append( XMLHandler.closeTag( "original-partition-schemas" ) ).append( Const.CR );
    }

    xml.append( "  " ).append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return xml.toString();
  }

  public SlaveStepCopyPartitionDistribution( Node node ) {
    this();

    int nrEntries = XMLHandler.countNodes( node, "entry" );
    for ( int i = 0; i < nrEntries; i++ ) {
      Node entryNode = XMLHandler.getSubNodeByNr( node, "entry", i );
      String slaveServerName = XMLHandler.getTagValue( entryNode, "slavename" );
      String partitionSchemaName = XMLHandler.getTagValue( entryNode, "partition_schema_name" );
      int stepCopyNr = Const.toInt( XMLHandler.getTagValue( entryNode, "stepcopy" ), -1 );
      int partitionNr = Const.toInt( XMLHandler.getTagValue( entryNode, "partition" ), -1 );

      addPartition( slaveServerName, partitionSchemaName, stepCopyNr, partitionNr );
    }

    Node originalPartitionSchemasNode = XMLHandler.getSubNode( node, "original-partition-schemas" );
    if ( originalPartitionSchemasNode != null ) {
      originalPartitionSchemas = new ArrayList<PartitionSchema>();
      int nrSchemas = XMLHandler.countNodes( originalPartitionSchemasNode, PartitionSchema.XML_TAG );
      for ( int i = 0; i < nrSchemas; i++ ) {
        Node schemaNode = XMLHandler.getSubNodeByNr( originalPartitionSchemasNode, PartitionSchema.XML_TAG, i );
        PartitionSchema originalPartitionSchema = new PartitionSchema( schemaNode );
        originalPartitionSchemas.add( originalPartitionSchema );
      }
    }
  }

  public Map<SlaveStepCopy, Integer> getDistribution() {
    return distribution;
  }

  /**
   * @return the originalPartitionSchemas
   */
  public List<PartitionSchema> getOriginalPartitionSchemas() {
    return originalPartitionSchemas;
  }

  /**
   * @param originalPartitionSchemas
   *          the originalPartitionSchemas to set
   */
  public void setOriginalPartitionSchemas( List<PartitionSchema> originalPartitionSchemas ) {
    this.originalPartitionSchemas = originalPartitionSchemas;
  }
}
