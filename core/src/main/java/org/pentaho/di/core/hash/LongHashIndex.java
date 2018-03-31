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

package org.pentaho.di.core.hash;

import org.pentaho.di.core.exception.KettleValueException;

public class LongHashIndex {

  private static final int STANDARD_INDEX_SIZE = 512;
  private static final float STANDARD_LOAD_FACTOR = 0.78f;

  private LongHashIndexEntry[] index;
  private int size;
  private int resizeThresHold;

  /**
   * Create a new long/long hash index
   * 
   * @param size
   *          the initial size of the hash index
   */
  public LongHashIndex( int size ) {

    // Find a suitable capacity being a factor of 2:
    int factor2Size = 1;
    while ( factor2Size < size ) {
      factor2Size <<= 1; // Multiply by 2
    }

    this.resizeThresHold = (int) ( factor2Size * STANDARD_LOAD_FACTOR );

    index = new LongHashIndexEntry[factor2Size];
  }

  /**
   * Create a new long/long hash index
   */
  public LongHashIndex() {
    this( STANDARD_INDEX_SIZE );
  }

  public int getSize() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public Long get( long key ) throws KettleValueException {
    int hashCode = generateHashCode( key );

    int indexPointer = indexFor( hashCode, index.length );
    LongHashIndexEntry check = index[indexPointer];

    while ( check != null ) {
      if ( check.hashCode == hashCode && check.equalsKey( key ) ) {
        return check.value;
      }
      check = check.nextEntry;
    }
    return null;
  }

  public void put( long key, Long value ) throws KettleValueException {
    int hashCode = generateHashCode( key );
    int indexPointer = indexFor( hashCode, index.length );

    LongHashIndexEntry check = index[indexPointer];
    LongHashIndexEntry previousCheck = null;

    while ( check != null ) {

      // If there is an identical entry in there, we replace the entry
      // And then we just return...
      //
      if ( check.hashCode == hashCode && check.equalsKey( key ) ) {
        check.value = value;
        return;
      }
      previousCheck = check;
      check = check.nextEntry;
    }

    // Don't forget to link to the previous check entry if there was any...
    //
    if ( previousCheck != null ) {
      previousCheck.nextEntry = new LongHashIndexEntry( hashCode, key, value, null );
    } else {
      index[indexPointer] = new LongHashIndexEntry( hashCode, key, value, null );
    }

    // If required, resize the table...
    //
    resize();
  }

  private final void resize() {
    // Increase the size of the index...
    //
    size++;

    // See if we've reached our resize threshold...
    //
    if ( size >= resizeThresHold ) {

      LongHashIndexEntry[] oldIndex = index;

      // Double the size to keep the size of the index a factor of 2...
      // Allocate the new array...
      //
      int newSize = 2 * index.length;

      LongHashIndexEntry[] newIndex = new LongHashIndexEntry[newSize];

      // Loop over the old index and re-distribute the entries
      // We want to make sure that the calculation
      // entry.hashCode & ( size - 1)
      // ends up in the right location after re-sizing...
      //
      for ( int i = 0; i < oldIndex.length; i++ ) {
        LongHashIndexEntry entry = oldIndex[i];
        if ( entry != null ) {
          oldIndex[i] = null;

          // Make sure we follow all the linked entries...
          // This is a bit of extra work, TODO: see how we can avoid it!
          //
          do {
            LongHashIndexEntry next = entry.nextEntry;
            int indexPointer = indexFor( entry.hashCode, newSize );
            entry.nextEntry = newIndex[indexPointer];
            newIndex[indexPointer] = entry;
            entry = next;
          } while ( entry != null );
        }
      }

      // Replace the old index with the new one we just created...
      //
      index = newIndex;

      // Also change the resize threshold...
      //
      resizeThresHold = (int) ( newSize * STANDARD_LOAD_FACTOR );
    }
  }

  public static int generateHashCode( Long key ) throws KettleValueException {
    return key.hashCode();
  }

  public static int indexFor( int hash, int length ) {
    return hash & ( length - 1 );
  }

  private static final class LongHashIndexEntry {
    private int hashCode;
    private long key;
    private long value;
    private LongHashIndexEntry nextEntry;

    /**
     * @param hashCode
     * @param key
     * @param value
     * @param nextEntry
     */
    public LongHashIndexEntry( int hashCode, Long key, Long value, LongHashIndexEntry nextEntry ) {
      this.hashCode = hashCode;
      this.key = key;
      this.value = value;
      this.nextEntry = nextEntry;
    }

    public boolean equalsKey( long cmpKey ) {
      return key == cmpKey;
    }
  }
}
