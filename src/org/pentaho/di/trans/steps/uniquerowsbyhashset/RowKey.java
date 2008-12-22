package org.pentaho.di.trans.steps.uniquerowsbyhashset;

import java.util.Arrays;

// Package private
class RowKey
{
    // TODO: This field needs to be set by a checkbox in the step dialog.
    private boolean storeValues;
    private int hash;
    private Object[] storedFieldValues;
    
    public RowKey(Object[] row, UniqueRowsByHashSetData sdi)
    {
        Object[] keyFields;
        // If we are keying on the entire row
        if (sdi.fieldnrs.length == 0)
        {
            keyFields = row;
        }
        else
        {
            keyFields = new Object[sdi.fieldnrs.length];
            for (int i = 0; i < sdi.fieldnrs.length; i++)
            {
                keyFields[i] = row[sdi.fieldnrs[i]];
            }
        }
        hash = calculateHashCode(keyFields);
        
        this.storeValues = sdi.storeValues;
        if (storeValues)
        {
            this.storedFieldValues = keyFields;
        }
    }
    
    private int calculateHashCode(Object[] keyFields)
    {
        return Arrays.hashCode(keyFields);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (storeValues)
            return Arrays.equals(storedFieldValues, ((RowKey)obj).storedFieldValues);
        else
            return true;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }
}
