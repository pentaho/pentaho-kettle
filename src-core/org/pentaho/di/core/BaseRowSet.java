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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.pentaho.di.core.row.RowMetaInterface;



/**
 * Contains the base RowSet class to help implement RowSet variants.
 * 
 * @author Matt
 * @since 22-01-2010
 *
 */
abstract class BaseRowSet implements Comparable<RowSet>, RowSet
{
    protected RowMetaInterface rowMeta;
    
    protected AtomicBoolean		    done;
    protected String    			    originStepName;
    protected AtomicInteger		    originStepCopy;
    protected String    			    destinationStepName;
    protected AtomicInteger		    destinationStepCopy;
    
    protected String                  remoteSlaveServerName;
    
    /**
     * Create new non-blocking-queue with maxSize capacity.
     * @param maxSize
     */
    public BaseRowSet()
    {
      // not done putting data into this RowSet
      done = new AtomicBoolean(false);
      
      originStepCopy = new AtomicInteger(0);
      destinationStepCopy = new AtomicInteger(0);
    }
    
    /**
     * Compares using the target steps and copy, not the source.
     * That way, re-partitioning is always done in the same way.
     * 
     */
	public int compareTo(RowSet rowSet) {
		String target = remoteSlaveServerName+"."+destinationStepName+"."+destinationStepCopy.intValue();
		String comp   = rowSet.getRemoteSlaveServerName()+"."+rowSet.getDestinationStepName()+"."+rowSet.getDestinationStepCopy();
		
		return target.compareTo(comp);
	} 
	
	public boolean equals(BaseRowSet rowSet) {
		return compareTo(rowSet) == 0;
	}

    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#putRow(org.pentaho.di.core.row.RowMetaInterface, java.lang.Object[])
	 */
    public abstract boolean putRow(RowMetaInterface rowMeta, Object[] rowData);
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#putRowWait(org.pentaho.di.core.row.RowMetaInterface, java.lang.Object[], long, java.util.concurrent.TimeUnit)
	 */
    public abstract boolean putRowWait(RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu);
    
    // default getRow with wait time = 100ms
    //
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getRow()
	 */
    public abstract Object[] getRow();
    
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getRowImmediate()
	 */       
    public abstract Object[] getRowImmediate();
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getRowWait(long, java.util.concurrent.TimeUnit)
	 */
    public abstract Object[] getRowWait(long timeout, TimeUnit tu);
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#setDone()
	 */
    public void setDone() {
    	done.set(true);
    }
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#isDone()
	 */
    public boolean isDone() {
    	return done.get();
    }
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getOriginStepName()
	 */
    public String getOriginStepName()
    {
    	synchronized(originStepName){
    		return originStepName;
    	}
        
    }
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getOriginStepCopy()
	 */
    public int getOriginStepCopy()
    {
        return originStepCopy.get();
    }
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getDestinationStepName()
	 */
    public String getDestinationStepName()
    {
        return destinationStepName;
    }
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getDestinationStepCopy()
	 */
    public int getDestinationStepCopy()
    {
    	return destinationStepCopy.get();    	
    }

    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getName()
	 */
    public String getName()
    {
        return toString();
    }
    
    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#size()
	 */
    public abstract int size();

    /* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#setThreadNameFromToCopy(java.lang.String, int, java.lang.String, int)
	 */
    public void setThreadNameFromToCopy(String from, int from_copy, String to, int to_copy)
    {
    	if (originStepName == null)
    		originStepName = from;
    	else{
    		synchronized(originStepName){
        		originStepName        = from;
        	}
    	}
    	
    	originStepCopy.set(from_copy);  
    	
    	if (destinationStepName == null)
    		destinationStepName = to;
    	else{
    		synchronized(destinationStepName){
    			destinationStepName   = to;    	
    		}
    	}
    		
    	destinationStepCopy.set(to_copy);
    }
    
    public String toString()
    {
    	StringBuffer str;
    	synchronized(originStepName){
    		str = new StringBuffer(originStepName);
    	}    	
    	str.append(".");
    	synchronized(originStepCopy){
    		str.append(originStepCopy);
    	}
    	str.append(" - ");
    	synchronized(destinationStepName){
    		str.append(destinationStepName);
    	}
    	str.append(".");
    	synchronized(destinationStepCopy){
    		str.append(destinationStepCopy);
    	}
    	if (!Const.isEmpty(remoteSlaveServerName)) {
        	synchronized(remoteSlaveServerName){
        		str.append(" (");
        		str.append(remoteSlaveServerName);
        		str.append(")");
        	}
    	}
        return str.toString();
    }

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getRowMeta()
	 */
	public RowMetaInterface getRowMeta() {
		return rowMeta;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#setRowMeta(org.pentaho.di.core.row.RowMetaInterface)
	 */
	public void setRowMeta(RowMetaInterface rowMeta) {
		this.rowMeta = rowMeta;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#getRemoteSlaveServerName()
	 */
	public String getRemoteSlaveServerName() {
		return remoteSlaveServerName;
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.RowSetInterface#setRemoteSlaveServerName(java.lang.String)
	 */
	public void setRemoteSlaveServerName(String remoteSlaveServerName) {
		this.remoteSlaveServerName = remoteSlaveServerName;
	}

	/**
	 * By default we don't report blocking, only for monitored transformations.
	 * @return true if this row set is blocking on reading or writing.
	 */
    public boolean isBlocking() {
      return false;
    }

}