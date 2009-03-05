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
package org.pentaho.di.trans.steps.textfileinput;

public class TextFileFilter implements Cloneable
{
    /** The position of the occurrence of the filter string to check at */
    private int                filterPosition;

    /** The string to filter on */
    private String             filterString;

    /** True if we want to stop when we reach a filter line */
    private boolean            filterLastLine;

    /** True if we want to match only this lines */
    private boolean            filterPositive;

    /**
     * @param filterPosition The position of the occurrence of the filter string to check at
     * @param filterString   The string to filter on
     * @param filterLastLine True if we want to stop when we reach a filter string on the specified position
     *                       False if you just want to skip the line.
     * @param filterPositive True if we want to get only lines that match this string
     *
     */
    public TextFileFilter(int filterPosition, String filterString, boolean filterLastLine, boolean  filterPositive)
    {
        this.filterPosition = filterPosition;
        this.filterString   = filterString;
        this.filterLastLine = filterLastLine;
        this.filterPositive = filterPositive;
    }

    public TextFileFilter()
    {
    }

    public Object clone()
    {
        try
        {
            Object retval = super.clone();
            return retval;
        }
        catch(CloneNotSupportedException e)
        {
            return null;
        }
    }
    
    /**
     * @return Returns the filterLastLine.
     */
    public boolean isFilterLastLine()
    {
        return filterLastLine;
    }

    /**
     * @param filterLastLine The filterLastLine to set.
     */
    public void setFilterLastLine(boolean filterLastLine)
    {
        this.filterLastLine = filterLastLine;
    }

    /**
     * @return Returns the filterPositive.
     */
    public boolean isFilterPositive()
    {
        return filterPositive;
    }

    /**
     * @param filterPositive The filterPositive to set.
     */
    public void setFilterPositive(boolean filterPositive)
    {
        this.filterPositive = filterPositive;
    }

    /**
     * @return Returns the filterPosition.
     */
    public int getFilterPosition()
    {
        return filterPosition;
    }

    /**
     * @param filterPosition The filterPosition to set.
     */
    public void setFilterPosition(int filterPosition)
    {
        this.filterPosition = filterPosition;
    }

    /**
     * @return Returns the filterString.
     */
    public String getFilterString()
    {
        return filterString;
    }

    /**
     * @param filterString The filterString to set.
     */
    public void setFilterString(String filterString)
    {
        this.filterString = filterString;
    }   
}