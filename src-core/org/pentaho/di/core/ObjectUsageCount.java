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
package org.pentaho.di.core;

import java.util.Comparator;

public class ObjectUsageCount implements Comparator<ObjectUsageCount>, Comparable<ObjectUsageCount>
{
    private String objectName;

    private int    nrUses;

    /**
     * @param objectName
     * @param nrUses
     */
    public ObjectUsageCount(String objectName, int nrUses)
    {
        this.objectName = objectName;
        this.nrUses = nrUses;
    }
    
    public String toString()
    {
        return objectName+";"+nrUses;
    }
    
    public static ObjectUsageCount fromString(String string)
    {
        String[] splits = string.split(";");
        if (splits.length>=2) return new ObjectUsageCount(splits[0], Const.toInt(splits[1], 1));
        return new ObjectUsageCount(string, 1);
    }
    
    public int compare(ObjectUsageCount count1, ObjectUsageCount count2)
    {
        return count1.compareTo(count2);
    }
    
    public int compareTo(ObjectUsageCount count)
    {
        return Integer.valueOf(count.getNrUses()).compareTo(Integer.valueOf(getNrUses()));
    }
    
    public void reset()
    {
        nrUses=0;
    }
    
    /**
     * Increment the nr of uses with 1
     * @return the nr of uses
     */
    public int increment()
    {
        nrUses++;
        return nrUses;
    }

    /**
     * @return the nrUses
     */
    public int getNrUses()
    {
        return nrUses;
    }

    /**
     * @param nrUses the nrUses to set
     */
    public void setNrUses(int nrUses)
    {
        this.nrUses = nrUses;
    }

    /**
     * @return the objectName
     */
    public String getObjectName()
    {
        return objectName;
    }

    /**
     * @param objectName the objectName to set
     */
    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

}
