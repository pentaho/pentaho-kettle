/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
