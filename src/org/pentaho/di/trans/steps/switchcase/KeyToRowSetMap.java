/**
 * 
 */
package org.pentaho.di.trans.steps.switchcase;

import java.util.HashMap;

import org.pentaho.di.core.RowSet;

public class KeyToRowSetMap
{
    protected KeyToRowSetMap() {}
    
    protected HashMap<Object, RowSet> map = new HashMap<Object, RowSet>();
    
    protected RowSet get(Object key)
    {
        return map.get(key);
    }
    protected void put(Object key, RowSet rowSet)
    {
        map.put(key, rowSet);
    }
}