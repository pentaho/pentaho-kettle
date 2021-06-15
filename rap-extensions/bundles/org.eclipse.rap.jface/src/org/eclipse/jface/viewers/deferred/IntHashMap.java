/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents a map of objects onto ints. This is intended for future optimization:
 * using int primitives would allow for an implementation that doesn't require
 * additional object allocations for Integers. However, the current implementation
 * simply delegates to the Java HashMap class. 
 * 
 * @since 1.0
 */
/* package */ class IntHashMap implements Serializable {
    private HashMap map; 
    
    /**
     * @param size
     * @param loadFactor
     */
    public IntHashMap(int size, float loadFactor) {
        map = new HashMap(size, loadFactor);
    }
    
    /**
     * 
     */
    public IntHashMap() {
        map = new HashMap();
    }
    
    /**
     * @param key
     */
    public void remove(Object key) {
        map.remove(key);
    }
    
    /**
     * @param key
     * @param value
     */
    public void put(Object key, int value) {
        map.put(key, new Integer(value));
    }
    
    /**
     * @param key
     * @return the int value at the given key 
     */
    public int get(Object key) {
        return get(key, 0);
    }
    
    /**
     * @param key
     * @param defaultValue
     * @return the int value at the given key, or the default value if this map does not contain the given key
     */
    public int get(Object key, int defaultValue) {
        Integer result = (Integer)map.get(key);
        
        if (result != null) {
            return result.intValue();
        }
        
        return defaultValue;
    }
    
    /**
     * @param key
     * @return <code>true</code> if this map contains the given key, <code>false</code> otherwise
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    /**
     * @return the number of key/value pairs
     */
    public int size() {
    	return map.size();
    }
}
