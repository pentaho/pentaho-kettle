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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;

/**
 * Implements common functionality needed by partitioner plugins.
 */
public abstract class BasePartitioner implements Partitioner {

  protected StepPartitioningMeta meta;
  protected int nrPartitions = -1;
  protected String id;
  protected String description;

  /**
   * Instantiates a new base partitioner.
   */
  public BasePartitioner() {
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#clone()
   */
  public Partitioner clone() {
    Partitioner partitioner = getInstance();
    partitioner.setId( id );
    partitioner.setDescription( description );
    partitioner.setMeta( meta );
    return partitioner;
  }

  /**
   * Gets the nr partitions.
   *
   * @return the nr partitions
   */
  public int getNrPartitions() {
    return nrPartitions;
  }

  /**
   * Sets the nr partitions.
   *
   * @param nrPartitions
   *          the new nr partitions
   */
  public void setNrPartitions( int nrPartitions ) {
    this.nrPartitions = nrPartitions;
  }

  /**
   * Inits the.
   *
   * @param rowMeta
   *          the row meta
   * @throws KettleException
   *           the kettle exception
   */
  public void init( RowMetaInterface rowMeta ) throws KettleException {

    if ( nrPartitions < 0 ) {
      nrPartitions = meta.getPartitionSchema().getPartitionIDs().size();
    }

  }

  /**
   * Gets the meta.
   *
   * @return the meta
   */
  public StepPartitioningMeta getMeta() {
    return meta;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.Partitioner#setMeta(org.pentaho.di.trans.step.StepPartitioningMeta)
   */
  public void setMeta( StepPartitioningMeta meta ) {
    this.meta = meta;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.Partitioner#getInstance()
   */
  public abstract Partitioner getInstance();

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.Partitioner#getDescription()
   */
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.Partitioner#setDescription(java.lang.String)
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.Partitioner#getId()
   */
  public String getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.Partitioner#setId(java.lang.String)
   */
  public void setId( String id ) {
    this.id = id;
  }

}
