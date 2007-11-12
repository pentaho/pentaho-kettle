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

import java.util.Hashtable;

/**
 * This class contains the counters for Kettle, the transformations, jobs and also the repository.
 * @author Matt
 * @since  17-apr-2005
 * 
 */
public class Counters
{
    private static Counters counters = null; 
    private Hashtable<String,Counter> counterTable = null;
    
    private Counters()
    {
        counterTable = new Hashtable<String,Counter>();
    }
    
    public static final Counters getInstance()
    {
        if (counters!=null) return counters;
        counters = new Counters();
        return counters;
    }
    
    public Counter getCounter(String name)
    {
        return counterTable.get(name);
    }
    
    public void setCounter(String name, Counter counter)
    {
        counterTable.put(name, counter);
    }
    
    public void clearCounter(String name)
    {
        counterTable.remove(name);
    }
    
    public void clear()
    {
        counterTable.clear();
    }
}
