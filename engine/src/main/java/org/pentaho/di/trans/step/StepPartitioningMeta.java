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

package org.pentaho.di.trans.step;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PartitionerPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.trans.Partitioner;
import org.w3c.dom.Node;

public class StepPartitioningMeta implements XMLInterface, Cloneable {
  public static final int PARTITIONING_METHOD_NONE = 0;
  public static final int PARTITIONING_METHOD_MIRROR = 1;
  public static final int PARTITIONING_METHOD_SPECIAL = 2;

  public static final String[] methodCodes = new String[] { "none", "Mirror", };
  public static final String[] methodDescriptions = new String[] { "None", "Mirror to all partitions" };

  private int methodType;
  private String method;

  private String partitionSchemaName; // to allow delayed binding...
  private PartitionSchema partitionSchema;

  private Partitioner partitioner;

  private boolean hasChanged = false;

  public StepPartitioningMeta() {
    method = "none";
    methodType = PARTITIONING_METHOD_NONE;
    partitionSchema = new PartitionSchema();
    hasChanged = false;
  }

  /**
   * @param method
   * @param partitionSchema
   */
  public StepPartitioningMeta( String method, PartitionSchema partitionSchema ) throws KettlePluginException {
    setMethod( method );
    this.partitionSchema = partitionSchema;
    hasChanged = false;
  }

  public StepPartitioningMeta clone() {
    try {
      StepPartitioningMeta stepPartitioningMeta =
        new StepPartitioningMeta( method, partitionSchema != null
          ? (PartitionSchema) partitionSchema.clone() : null );
      stepPartitioningMeta.partitionSchemaName = partitionSchemaName;
      stepPartitioningMeta.setMethodType( methodType );
      stepPartitioningMeta.setPartitioner( partitioner == null ? null : partitioner.clone() );
      return stepPartitioningMeta;
    } catch ( KettlePluginException e ) {
      throw new RuntimeException( "Unable to load partitioning plugin", e );
    }
  }

  /**
   * @return true if the partition schema names are the same.
   */
  @Override
  public boolean equals( Object obj ) {
    if ( obj == null ) {
      return false;
    }
    if ( partitionSchemaName == null ) {
      return false;
    }
    StepPartitioningMeta meta = (StepPartitioningMeta) obj;
    if ( meta.partitionSchemaName == null ) {
      return false;
    }
    return partitionSchemaName.equalsIgnoreCase( meta.partitionSchemaName );
  }

  @Override
  public int hashCode() {
    return partitionSchemaName == null ? 0 : partitionSchemaName.toLowerCase().hashCode();
  }

  @Override
  public String toString() {

    String description;

    if ( partitioner != null ) {
      description = partitioner.getDescription();
    } else {
      description = getMethodDescription();
    }
    if ( partitionSchema != null ) {
      description += " / " + partitionSchema.toString();
    }

    return description;
  }

  /**
   * @return the partitioningMethod
   */
  public int getMethodType() {
    return methodType;
  }

  /**
   * @param method
   *          the partitioning method to set
   */
  public void setMethod( String method ) throws KettlePluginException {
    if ( !method.equals( this.method ) ) {
      this.method = method;
      createPartitioner( method );
      hasChanged = true;
    }
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 150 );

    xml.append( "    " ).append( XMLHandler.openTag( "partitioning" ) ).append( Const.CR );
    xml.append( "      " ).append( XMLHandler.addTagValue( "method", getMethodCode() ) );
    xml.append( "      " ).append(
      XMLHandler.addTagValue( "schema_name", partitionSchema != null ? partitionSchema.getName() : "" ) );
    if ( partitioner != null ) {
      xml.append( partitioner.getXML() );
    }
    xml.append( "    " ).append( XMLHandler.closeTag( "partitioning" ) ).append( Const.CR );

    return xml.toString();
  }

  public StepPartitioningMeta( Node partitioningMethodNode ) throws KettleException {
    this();
    setMethod( getMethod( XMLHandler.getTagValue( partitioningMethodNode, "method" ) ) );
    partitionSchemaName = XMLHandler.getTagValue( partitioningMethodNode, "schema_name" );
    hasChanged = false;
    if ( partitioner != null ) {
      partitioner.loadXML( partitioningMethodNode );
    }
  }

  public String getMethodCode() {
    if ( methodType == PARTITIONING_METHOD_SPECIAL ) {
      if ( partitioner != null ) {
        return partitioner.getId();
      } else {
        return methodCodes[PARTITIONING_METHOD_NONE];
      }
    }
    return methodCodes[methodType];
  }

  public String getMethodDescription() {
    if ( methodType != PARTITIONING_METHOD_SPECIAL ) {
      return methodDescriptions[methodType];
    } else {
      return partitioner.getDescription();
    }
  }

  public String getMethod() {
    return method;
  }

  public static final String getMethod( String name ) {
    if ( Utils.isEmpty( name ) ) {
      return methodCodes[PARTITIONING_METHOD_NONE];
    }

    for ( int i = 0; i < methodDescriptions.length; i++ ) {
      if ( methodDescriptions[i].equalsIgnoreCase( name ) ) {
        return methodCodes[i];
      }
    }

    for ( int i = 0; i < methodCodes.length; i++ ) {
      if ( methodCodes[i].equalsIgnoreCase( name ) ) {
        return methodCodes[i];
      }
    }

    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.findPluginWithName( PartitionerPluginType.class, name );
    if ( plugin != null ) {
      return name;
    }
    plugin = registry.findPluginWithId( PartitionerPluginType.class, name );
    if ( plugin != null ) {
      return name;
    }

    return methodCodes[PARTITIONING_METHOD_NONE];
  }

  public static final int getMethodType( String description ) {
    for ( int i = 0; i < methodDescriptions.length; i++ ) {
      if ( methodDescriptions[i].equalsIgnoreCase( description ) ) {
        return i;
      }
    }

    for ( int i = 0; i < methodCodes.length; i++ ) {
      if ( methodCodes[i].equalsIgnoreCase( description ) ) {
        return i;
      }
    }

    PluginInterface plugin =
      PluginRegistry.getInstance().findPluginWithId( PartitionerPluginType.class, description );
    if ( plugin != null ) {
      return PARTITIONING_METHOD_SPECIAL;
    }
    return PARTITIONING_METHOD_NONE;
  }

  public boolean isPartitioned() {
    return methodType != PARTITIONING_METHOD_NONE;
  }

  public void setPartitionSchemaName( String partitionSchemaName ) {
    this.partitionSchemaName = partitionSchemaName;
  }

  /**
   * @return the partitionSchema
   */
  public PartitionSchema getPartitionSchema() {
    return partitionSchema;
  }

  /**
   * @param partitionSchema
   *          the partitionSchema to set
   */
  public void setPartitionSchema( PartitionSchema partitionSchema ) {
    this.partitionSchema = partitionSchema;
    hasChanged = true;
  }

  /**
   * Set the partitioning schema after loading from XML or repository
   *
   * @param partitionSchemas
   *          the list of partitioning schemas
   */
  public void setPartitionSchemaAfterLoading( List<PartitionSchema> partitionSchemas ) throws KettleException {
    partitionSchema = null; // sorry, not found!

    for ( int i = 0; i < partitionSchemas.size() && partitionSchema == null; i++ ) {
      PartitionSchema schema = partitionSchemas.get( i );
      if ( schema.getName().equalsIgnoreCase( partitionSchemaName ) ) {
        partitionSchema = schema; // found!
      }
    }

    /*
     * if (methodType!=PARTITIONING_METHOD_NONE && partitionSchema==null) { String message =
     * "Unable to set partition schema for name ["+partitionSchemaName+"], method: "+getMethodDescription()+Const.CR;
     * message += "This is the list of available partition schema:"+Const.CR; for (int i=0;i<partitionSchemas.size() &&
     * partitionSchema==null;i++) { PartitionSchema schema = partitionSchemas.get(i);
     * message+="  --> "+schema.getName()+Const.CR; } throw new KettleException(message); }
     */
  }

  public void createPartitioner( String method ) throws KettlePluginException {
    methodType = getMethodType( method );
    switch ( methodType ) {
      case PARTITIONING_METHOD_SPECIAL: {
        PluginRegistry registry = PluginRegistry.getInstance();
        PluginInterface plugin = registry.findPluginWithId( PartitionerPluginType.class, method );
        partitioner = (Partitioner) registry.loadClass( plugin );
        partitioner.setId( plugin.getIds()[0] );
        break;
      }
      case PARTITIONING_METHOD_NONE:
      default:
        partitioner = null;
    }
    if ( partitioner != null ) {
      partitioner.setMeta( this );
    }
  }

  public boolean isMethodMirror() {
    return methodType == PARTITIONING_METHOD_MIRROR;
  }

  public int getPartition( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    if ( partitioner != null ) {
      return partitioner.getPartition( rowMeta, row );
    }
    return 0;
  }

  public Partitioner getPartitioner() {
    return partitioner;
  }

  public void setPartitioner( Partitioner partitioner ) {
    this.partitioner = partitioner;
  }

  public boolean hasChanged() {
    return hasChanged;
  }

  public void hasChanged( boolean hasChanged ) {
    this.hasChanged = hasChanged;
  }

  public void setMethodType( int methodType ) {
    this.methodType = methodType;
  }
}
