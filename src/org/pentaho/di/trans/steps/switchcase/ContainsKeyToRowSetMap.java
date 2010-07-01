/**
 * 
 */
package org.pentaho.di.trans.steps.switchcase;

import java.util.ArrayList;

import org.pentaho.di.core.RowSet;

public class ContainsKeyToRowSetMap extends KeyToRowSetMap
{
    protected ArrayList<String> list = new ArrayList<String>();
    
    protected ContainsKeyToRowSetMap() { super(); }

    protected RowSet get(Object value)
    {
        String valueStr = (String)value;
        for (String key : list)
        {
            if (valueStr.contains(key))
                return map.get(key);
        }
        return null;
    }
    
    protected void put(Object key, RowSet rowSet)
    {
        super.put(key, rowSet);
        list.add((String)key);
    }
}