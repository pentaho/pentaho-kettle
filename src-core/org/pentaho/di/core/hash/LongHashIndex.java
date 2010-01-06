/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.hash;

import org.pentaho.di.core.exception.KettleValueException;

public class LongHashIndex {
	
	private static final int   STANDARD_INDEX_SIZE =  512;
	private static final float STANDARD_LOAD_FACTOR = 0.78f;
	
	private LongHashIndexEntry[] index;
	private int size;
	private int resizeThresHold;
	
	/**
	 * Create a new long/long hash index
	 * @param size the initial size of the hash index
	 */
	public LongHashIndex(int size) {
		
		// Find a suitable capacity being a factor of 2:
		int factor2Size = 1;
		while (factor2Size<size) factor2Size<<=1; // Multiply by 2
		
		this.size = factor2Size;
		this.resizeThresHold = (int)(factor2Size*STANDARD_LOAD_FACTOR);
		
		index = new LongHashIndexEntry[factor2Size];
	}
	
	/**
	 * Create a new long/long hash index
	 */
	public LongHashIndex() {
		this(STANDARD_INDEX_SIZE);
	}
	
	public int getSize() {
		return size;
	}
	
	public boolean isEmpty() {
		return size==0;
	}
	
	 public Long get(long key) throws KettleValueException {
		int hashCode = generateHashCode(key);

		int indexPointer = hashCode & (index.length - 1);
		LongHashIndexEntry check = index[indexPointer];
		
		while (check!=null) {
			if (check.hashCode == hashCode && check.equalsKey(key)) {
				return check.value;
			}
			check = check.nextEntry;
		}
		return null;
	}
	 
    public void put(long key, Long value) throws KettleValueException {
		int hashCode = generateHashCode(key);
		int indexPointer = hashCode & (index.length - 1);
		
		// First see if there is an entry on that pointer...
		//
		boolean searchEmptySpot = false;
		
		LongHashIndexEntry check = index[indexPointer];
		LongHashIndexEntry previousCheck = null;

		while (check!=null) {
			searchEmptySpot = true;
			
			// If there is an identical entry in there, we replace the entry
			// And then we just return...
			//
			if (check.hashCode == hashCode && check.equalsKey(key)) {
				check.value = value;
				return;
			}
			previousCheck = check;
			check = check.nextEntry;
		}
		
		// If we are still here, that means that we are ready to put the value down...
		// Where do we need to search for an empty spot in the index?
		//
		while (searchEmptySpot) {
			indexPointer++;
			if (indexPointer>=size) indexPointer=0;
			if (index[indexPointer]==null) {
				searchEmptySpot=false;
			}
		}

		// OK, now that we know where to put the entry, insert it...
		//
		index[indexPointer] = new LongHashIndexEntry(hashCode, key, value, index[indexPointer]);
		
		// Don't forget to link to the previous check entry if there was any...
		//
		if (previousCheck!=null) {
			previousCheck.nextEntry = index[indexPointer];
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
		if (size >= resizeThresHold) {
			
			LongHashIndexEntry[] oldIndex = index;
			
			// Double the size to keep the size of the index a factor of 2...
			// Allocate the new array...
			//
			int newSize = 2 * index.length;

			LongHashIndexEntry[] newIndex = new LongHashIndexEntry[newSize];

			// Loop over the old index and re-distribute the entries
			// We want to make sure that the calculation 
			//     entry.hashCode & ( size - 1) 
			// ends up in the right location after re-sizing...
			//
			for (int i = 0; i < oldIndex.length; i++) {
				LongHashIndexEntry entry = oldIndex[i];
				if (entry != null) {
					oldIndex[i] = null;
					
					// Make sure we follow all the linked entries...
					// This is a bit of extra work, TODO: see how we can avoid it!
					// 
					do {
						LongHashIndexEntry next = entry.nextEntry;
						int indexPointer = entry.hashCode & (newSize - 1);
						entry.nextEntry = newIndex[indexPointer];
						newIndex[indexPointer] = entry;
						entry = next;
					} 
					while (entry != null); 
				}
			}
			
			// Replace the old index with the new one we just created...
			//
			index = newIndex;
			
			// Also change the resize threshold...
			//
			resizeThresHold = (int) (newSize * STANDARD_LOAD_FACTOR);
		}
	}
	 
    public static int generateHashCode(Long key) throws KettleValueException
    {
    	return key.hashCode();
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
		public LongHashIndexEntry(int hashCode, Long key, Long value, LongHashIndexEntry nextEntry) {
			this.hashCode = hashCode;
			this.key = key;
			this.value = value;
			this.nextEntry = nextEntry;
		}
		
        public boolean equalsKey(long cmpKey)
        {
            return key == cmpKey;
        }        
	}
}
