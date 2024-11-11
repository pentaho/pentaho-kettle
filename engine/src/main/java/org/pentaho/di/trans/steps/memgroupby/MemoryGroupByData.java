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


package org.pentaho.di.trans.steps.memgroupby;

import java.util.HashMap;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class MemoryGroupByData extends BaseStepData implements StepDataInterface {
  public class HashEntry {
    private Object[] groupData;

    public HashEntry( Object[] groupData ) {
      this.groupData = groupData;
    }

    public Object[] getGroupData() {
      return groupData;
    }

    public boolean equals( Object obj ) {
      HashEntry entry = (HashEntry) obj;

      try {
        return groupMeta.compare( groupData, entry.groupData ) == 0;
      } catch ( KettleValueException e ) {
        throw new RuntimeException( e );
      }
    }

    public int hashCode() {
      try {
        return groupMeta.hashCode( getHashValue() );
      } catch ( KettleValueException e ) {
        throw new RuntimeException( e );
      }
    }

    private Object[] getHashValue() throws KettleValueException {
      Object[] groupDataHash = new Object[groupMeta.size()];
      for ( int i = 0; i < groupMeta.size(); i++ ) {
        ValueMetaInterface valueMeta = groupMeta.getValueMeta( i );
        groupDataHash[i] = valueMeta.convertToNormalStorageType( groupData[i] );
      }
      return groupDataHash;
    }
  }

  public HashMap<HashEntry, Aggregate> map;

  public RowMetaInterface aggMeta;
  public RowMetaInterface groupMeta;
  public RowMetaInterface entryMeta;

  public RowMetaInterface groupAggMeta; // for speed: groupMeta+aggMeta
  public int[] groupnrs;
  public int[] subjectnrs;

  public boolean firstRead;

  public Object[] groupResult;

  public boolean hasOutput;

  public RowMetaInterface inputRowMeta;
  public RowMetaInterface outputRowMeta;

  public ValueMetaInterface valueMetaInteger;
  public ValueMetaInterface valueMetaNumber;

  public boolean newBatch;

  public MemoryGroupByData() {
    super();

  }

  public HashEntry getHashEntry( Object[] groupData ) {
    return new HashEntry( groupData );
  }

  /**
   * Method responsible for clearing out memory hogs
   */
  public void clear() {
    map = new HashMap<MemoryGroupByData.HashEntry, Aggregate>();
  }
}
