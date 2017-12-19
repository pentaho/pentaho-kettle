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

package org.pentaho.di.trans.steps.streamlookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.hash.ByteArrayHashIndex;
import org.pentaho.di.core.hash.LongHashIndex;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class StreamLookupData extends BaseStepData implements StepDataInterface {
  /** used to store values in used to look up things */
  public Map<RowMetaAndData, Object[]> look;

  public List<KeyValue> list;

  /** nrs of keys-values in row. */
  public int[] keynrs;

  /** The metadata we send out */
  public RowMetaInterface outputRowMeta;

  /** default string converted to values... */
  public Object[] nullIf;

  /** Flag to indicate that we have to read lookup values from the info step */
  public boolean readLookupValues;

  /**
   * Stores the first row of the lookup-values to later determine if the types are the same as the input row lookup
   * values.
   */
  public RowMetaInterface keyTypes;

  public RowMetaInterface cacheKeyMeta;

  public RowMetaInterface cacheValueMeta;

  public Comparator<KeyValue> comparator;

  public ByteArrayHashIndex hashIndex;
  public LongHashIndex longIndex;

  public RowMetaInterface lookupMeta;

  public RowMetaInterface infoMeta;

  public int[] lookupColumnIndex;

  public boolean metadataVerifiedIntegerPair;

  /** See if we need to convert the keys to a native data type */
  public boolean[] convertKeysToNative;

  // Did we read rows from the lookup hop.
  public boolean hasLookupRows;

  public StreamInterface infoStream;

  public StreamLookupData() {
    super();
    look = new HashMap<RowMetaAndData, Object[]>();
    hashIndex = null;
    longIndex = new LongHashIndex();
    list = new ArrayList<KeyValue>();
    metadataVerifiedIntegerPair = false;
    hasLookupRows = false;

    comparator = new Comparator<KeyValue>() {
      public int compare( KeyValue k1, KeyValue k2 ) {
        try {
          return cacheKeyMeta.compare( k1.getKey(), k2.getKey() );
        } catch ( KettleValueException e ) {
          throw new RuntimeException( "Stream Lookup comparator error", e );
        }
      }
    };
  }

}
