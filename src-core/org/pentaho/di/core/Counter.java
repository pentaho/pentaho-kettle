 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 

package org.pentaho.di.core;

/**
 * Is used to keep the state of sequences / counters throughout a single session of a Transformation, but across Steps.
 * 
 * @author Matt
 * @since 13-05-2003
 *
 */
public class Counter 
{
	private long   counter;
	private long   start;
	private long   increment;
	private long   maximum;
	private boolean loop;

	public Counter()
	{
		start=1L;
		increment=1L;
		maximum=0L;
		loop=false;
		counter=start;
	}
	
	public Counter(long start)
	{
		this();
		this.start=start;
		counter=start;
	}

	public Counter(long start, long increment)
	{
		this(start);
		this.increment=increment;
	}

	public Counter(long start, long increment, long maximum)
	{
		this(start, increment);
		this.loop=true;
		this.maximum=maximum;
	}
	
	/**
     * @return Returns the counter.
     */
    public long getCounter()
    {
        return counter;
    }
    
    /**
     * @return Returns the increment.
     */
    public long getIncrement()
    {
        return increment;
    }
    
    /**
     * @return Returns the maximum.
     */
    public long getMaximum()
    {
        return maximum;
    }
    
    /**
     * @return Returns the start.
     */
    public long getStart()
    {
        return start;
    }
    
    /**
     * @return Returns the loop.
     */
    public boolean isLoop()
    {
        return loop;
    }
    
    /**
     * @param counter The counter to set.
     */
    public void setCounter(long counter)
    {
        this.counter = counter;
    }

    /**
     * @param increment The increment to set.
     */
    public void setIncrement(long increment)
    {
        this.increment = increment;
    }
    
    /**
     * @param loop The loop to set.
     */
    public void setLoop(boolean loop)
    {
        this.loop = loop;
    }
    
    /**
     * @param maximum The maximum to set.
     */
    public void setMaximum(long maximum)
    {
        this.maximum = maximum;
    }
	
	public long next()
	{
		long retval=counter;
		
		counter+=increment;
		if (loop && counter>maximum) counter=start;
		
		return retval;
	}
}
