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
