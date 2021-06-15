/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Peter Shipton - original hashtable implementation
 *     Nick Edgar - added element comparer support
 *******************************************************************************/

package org.eclipse.jface.viewers;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * CustomHashtable associates keys with values. Keys and values cannot be null.
 * The size of the Hashtable is the number of key/value pairs it contains.
 * The capacity is the number of key/value pairs the Hashtable can hold.
 * The load factor is a float value which determines how full the Hashtable
 * gets before expanding the capacity. If the load factor of the Hashtable
 * is exceeded, the capacity is doubled.
 * <p>
 * CustomHashtable allows a custom comparator and hash code provider.
 */
/* package */final class CustomHashtable implements Serializable {

    /**
     * HashMapEntry is an internal class which is used to hold the entries of a Hashtable.
     */
    private static class HashMapEntry implements Serializable {
        Object key, value;

        HashMapEntry next;

        HashMapEntry(Object theKey, Object theValue) {
            key = theKey;
            value = theValue;
        }
    }

    private static final class EmptyEnumerator implements Enumeration {
        public boolean hasMoreElements() {
            return false;
        }

        public Object nextElement() {
            throw new NoSuchElementException();
        }
    }

    private class HashEnumerator implements Enumeration {
        boolean key;

        int start;

        HashMapEntry entry;

        HashEnumerator(boolean isKey) {
            key = isKey;
            start = firstSlot;
        }

        public boolean hasMoreElements() {
            if (entry != null) {
				return true;
			}
            while (start <= lastSlot) {
				if (elementData[start++] != null) {
                    entry = elementData[start - 1];
                    return true;
                }
			}
            return false;
        }

        public Object nextElement() {
            if (hasMoreElements()) {
                Object result = key ? entry.key : entry.value;
                entry = entry.next;
                return result;
            } else {
				throw new NoSuchElementException();
			}
        }
    }

    transient int elementCount;

    transient HashMapEntry[] elementData;

    private float loadFactor;

    private int threshold;

    transient int firstSlot = 0;

    transient int lastSlot = -1;

    transient private IElementComparer comparer;

    private static final EmptyEnumerator emptyEnumerator = new EmptyEnumerator();

    /**
     * The default capacity used when not specified in the constructor.
     */
    public static final int DEFAULT_CAPACITY = 13;

    /**
     * Constructs a new Hashtable using the default capacity
     * and load factor.
     */
    public CustomHashtable() {
        this(13);
    }

    /**
     * Constructs a new Hashtable using the specified capacity
     * and the default load factor.
     *
     * @param capacity the initial capacity
     */
    public CustomHashtable(int capacity) {
        this(capacity, null);
    }

    /**
     * Constructs a new hash table with the default capacity and the given
     * element comparer.
     *
     * @param comparer the element comparer to use to compare keys and obtain
     *   hash codes for keys, or <code>null</code>  to use the normal 
     *   <code>equals</code> and <code>hashCode</code> methods
     */
    public CustomHashtable(IElementComparer comparer) {
        this(DEFAULT_CAPACITY, comparer);
    }

    /**
     * Constructs a new hash table with the given capacity and the given
     * element comparer.
     * 
     * @param capacity the maximum number of elements that can be added without
     *   rehashing
     * @param comparer the element comparer to use to compare keys and obtain
     *   hash codes for keys, or <code>null</code>  to use the normal 
     *   <code>equals</code> and <code>hashCode</code> methods
     */
    public CustomHashtable(int capacity, IElementComparer comparer) {
        if (capacity >= 0) {
            elementCount = 0;
            elementData = new HashMapEntry[capacity == 0 ? 1 : capacity];
            firstSlot = elementData.length;
            loadFactor = 0.75f;
            computeMaxSize();
        } else {
			throw new IllegalArgumentException();
		}
        this.comparer = comparer;
    }

    /**
     * Constructs a new hash table with enough capacity to hold all keys in the
     * given hash table, then adds all key/value pairs in the given hash table
     * to the new one, using the given element comparer.
     * @param table the original hash table to copy from
     * 
     * @param comparer the element comparer to use to compare keys and obtain
     *   hash codes for keys, or <code>null</code>  to use the normal 
     *   <code>equals</code> and <code>hashCode</code> methods
     */
    public CustomHashtable(CustomHashtable table, IElementComparer comparer) {
        this(table.size() * 2, comparer);
        for (int i = table.elementData.length; --i >= 0;) {
            HashMapEntry entry = table.elementData[i];
            while (entry != null) {
                put(entry.key, entry.value);
                entry = entry.next;
            }
        }
    }
    
    /**
     * Returns the element comparer used  to compare keys and to obtain
     * hash codes for keys, or <code>null</code> if no comparer has been
     * provided.
     * 
     * @return the element comparer or <code>null</code>
     * 
     * @since 1.0
     */
    public IElementComparer getComparer() {
    	return comparer;
    }

    private void computeMaxSize() {
        threshold = (int) (elementData.length * loadFactor);
    }

    /**
     * Answers if this Hashtable contains the specified object as a key
     * of one of the key/value pairs.
     *
     * @param		key	the object to look for as a key in this Hashtable
     * @return		true if object is a key in this Hashtable, false otherwise
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Answers an Enumeration on the values of this Hashtable. The
     * results of the Enumeration may be affected if the contents
     * of this Hashtable are modified.
     *
     * @return		an Enumeration of the values of this Hashtable
     */
    public Enumeration elements() {
        if (elementCount == 0) {
			return emptyEnumerator;
		}
        return new HashEnumerator(false);
    }

    /**
     * Answers the value associated with the specified key in
     * this Hashtable.
     *
     * @param		key	the key of the value returned
     * @return		the value associated with the specified key, null if the specified key
     *				does not exist
     */
    public Object get(Object key) {
        int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
        HashMapEntry entry = elementData[index];
        while (entry != null) {
            if (keyEquals(key, entry.key)) {
				return entry.value;
			}
            entry = entry.next;
        }
        return null;
    }

    private HashMapEntry getEntry(Object key) {
        int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
        HashMapEntry entry = elementData[index];
        while (entry != null) {
            if (keyEquals(key, entry.key)) {
				return entry;
			}
            entry = entry.next;
        }
        return null;
    }

    /**
     * Answers the hash code for the given key.
     */
    private int hashCode(Object key) {
        if (comparer == null) {
			return key.hashCode();
		} else {
			return comparer.hashCode(key);
		}
    }

    /**
     * Compares two keys for equality.
     */
    private boolean keyEquals(Object a, Object b) {
        if (comparer == null) {
			return a.equals(b);
		} else {
			return comparer.equals(a, b);
		}
    }

    /**
     * Answers an Enumeration on the keys of this Hashtable. The
     * results of the Enumeration may be affected if the contents
     * of this Hashtable are modified.
     *
     * @return		an Enumeration of the keys of this Hashtable
     */
    public Enumeration keys() {
        if (elementCount == 0) {
			return emptyEnumerator;
		}
        return new HashEnumerator(true);
    }

    /**
     * Associate the specified value with the specified key in this Hashtable.
     * If the key already exists, the old value is replaced. The key and value
     * cannot be null.
     *
     * @param		key	the key to add
     * @param		value	the value to add
     * @return		the old value associated with the specified key, null if the key did
     *				not exist
     */
    public Object put(Object key, Object value) {
        if (key != null && value != null) {
            int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
            HashMapEntry entry = elementData[index];
            while (entry != null && !keyEquals(key, entry.key)) {
				entry = entry.next;
			}
            if (entry == null) {
                if (++elementCount > threshold) {
                    rehash();
                    index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
                }
                if (index < firstSlot) {
					firstSlot = index;
				}
                if (index > lastSlot) {
					lastSlot = index;
				}
                entry = new HashMapEntry(key, value);
                entry.next = elementData[index];
                elementData[index] = entry;
                return null;
            }
            Object result = entry.value;
            entry.key = key; // important to avoid hanging onto keys that are equal but "old" -- see bug 30607
            entry.value = value;
            return result;
        } else {
			throw new NullPointerException();
		}
    }

    /**
     * Increases the capacity of this Hashtable. This method is sent when
     * the size of this Hashtable exceeds the load factor.
     */
    private void rehash() {
        int length = elementData.length << 1;
        if (length == 0) {
			length = 1;
		}
        firstSlot = length;
        lastSlot = -1;
        HashMapEntry[] newData = new HashMapEntry[length];
        for (int i = elementData.length; --i >= 0;) {
            HashMapEntry entry = elementData[i];
            while (entry != null) {
                int index = (hashCode(entry.key) & 0x7FFFFFFF) % length;
                if (index < firstSlot) {
					firstSlot = index;
				}
                if (index > lastSlot) {
					lastSlot = index;
				}
                HashMapEntry next = entry.next;
                entry.next = newData[index];
                newData[index] = entry;
                entry = next;
            }
        }
        elementData = newData;
        computeMaxSize();
    }

    /**
     * Remove the key/value pair with the specified key from this Hashtable.
     *
     * @param		key	the key to remove
     * @return		the value associated with the specified key, null if the specified key
     *				did not exist
     */
    public Object remove(Object key) {
        HashMapEntry last = null;
        int index = (hashCode(key) & 0x7FFFFFFF) % elementData.length;
        HashMapEntry entry = elementData[index];
        while (entry != null && !keyEquals(key, entry.key)) {
            last = entry;
            entry = entry.next;
        }
        if (entry != null) {
            if (last == null) {
				elementData[index] = entry.next;
			} else {
				last.next = entry.next;
			}
            elementCount--;
            return entry.value;
        }
        return null;
    }

    /**
     * Answers the number of key/value pairs in this Hashtable.
     *
     * @return		the number of key/value pairs in this Hashtable
     */
    public int size() {
        return elementCount;
    }

    /**
     * Answers the string representation of this Hashtable.
     *
     * @return		the string representation of this Hashtable
     */
    public String toString() {
        if (size() == 0) {
			return "{}"; //$NON-NLS-1$
		}

        StringBuffer buffer = new StringBuffer();
        buffer.append('{');
        for (int i = elementData.length; --i >= 0;) {
            HashMapEntry entry = elementData[i];
            while (entry != null) {
                buffer.append(entry.key);
                buffer.append('=');
                buffer.append(entry.value);
                buffer.append(", "); //$NON-NLS-1$
                entry = entry.next;
            }
        }
        // Remove the last ", "
        if (elementCount > 0) {
			buffer.setLength(buffer.length() - 2);
		}
        buffer.append('}');
        return buffer.toString();
    }
}
