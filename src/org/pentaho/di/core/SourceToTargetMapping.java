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

public class SourceToTargetMapping
{
    private int sourcePosition;
    private int targetPosition;
    
    /**
     * Creates a new source-to-target mapping
     * @param sourcePosition
     * @param targetPosition
     */
    public SourceToTargetMapping(int sourcePosition, int targetPosition)
    {
        this.sourcePosition = sourcePosition;
        this.targetPosition = targetPosition;
    }
        
    /**
     * @return Returns the sourcePosition.
     */
    public int getSourcePosition()
    {
        return sourcePosition;
    }
    
    /**
     * @param sourcePosition The sourcePosition to set.
     */
    public void setSourcePosition(int sourcePosition)
    {
        this.sourcePosition = sourcePosition;
    }
    /**
     * @return Returns the targetPosition.
     */
    public int getTargetPosition()
    {
        return targetPosition;
    }
    /**
     * @param targetPosition The targetPosition to set.
     */
    public void setTargetPosition(int targetPosition)
    {
        this.targetPosition = targetPosition;
    }
    
    public String getSourceString(String source[])
    {
    	return source[sourcePosition];
    }

    public String getTargetString(String target[])
    {
    	return target[targetPosition];
    }

}
