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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.pentaho.di.core.row.RowMetaInterface;



/**
 * Contains a buffer of rows.  Getting rows from the buffer or putting rows in the buffer is synchronised to allow concurrent use of multiple Threads.
 * 
 * @author Matt
 * @since 04-04-2003
 *
 */
public class RowSet implements Comparable<RowSet>
{
	public final static int ERR_NO_ERROR        = 0;
    public final static int ERR_ROW_IS_BUSY     = 1;

    private RowMetaInterface rowMeta;
    
    private BlockingQueue<Object[]> queArray;
    private AtomicBoolean		    done;
    private String    			    originStepName;
    private AtomicInteger		    originStepCopy;
    private String    			    destinationStepName;
    private AtomicInteger		    destinationStepCopy;
    
    private String                  remoteSlaveServerName;
    
    /**
     * Create new non-blocking-queue with maxSize capacity.
     * @param maxSize
     */
    public RowSet(int maxSize)
    {
      // create an empty queue 
      queArray = new ArrayBlockingQueue<Object[]>(maxSize, false);
      
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
		String comp   = rowSet.remoteSlaveServerName+"."+rowSet.destinationStepName+"."+rowSet.destinationStepCopy.intValue();
		
		return target.compareTo(comp);
	} 
	
	public boolean equals(RowSet rowSet) {
		return compareTo(rowSet) == 0;
	}

    public boolean putRow(RowMetaInterface rowMeta, Object[] rowData)
    {
    	return putRowWait(rowMeta, rowData, Const.TIMEOUT_PUT_MILLIS, TimeUnit.MILLISECONDS);
    }
    
    public boolean putRowImmediate(RowMetaInterface rowMeta, Object[] rowData){
    	this.rowMeta = rowMeta;
    	try {             		
    		return queArray.offer(rowData);             
    	}
    	catch (NullPointerException e)
	    {
    		return false;
	    }    	
    }
    
    public boolean putRowWait(RowMetaInterface rowMeta, Object[] rowData, long time, TimeUnit tu) {
    	this.rowMeta = rowMeta;
    	try{
    		
    		return queArray.offer(rowData, time, tu);
    	}
    	catch (InterruptedException e)
	    {
    		return false;
	    }
    	catch (NullPointerException e)
	    {
    		return false;
	    }    	
    	
    }
    
    // default getRow with wait time = 100ms
    //
    public Object[] getRow(){
    	return getRowWait(Const.TIMEOUT_GET_MILLIS, TimeUnit.MILLISECONDS);
    }
    
    
    /**
     * get the first row in the list immediately.
     * @return Row or null
     */       
    public Object[] getRowImmediate(){

    	return queArray.poll();	    	
    }
    
    /**
     * get the first row in the list immediately
     * if it is available or wait until timeout
     * @return Row or null
     */
    public Object[] getRowWait(long timeout, TimeUnit tu){

    	try{
    		return queArray.poll(timeout, tu);
    	}
    	catch(InterruptedException e){
    		return null;
    	}
    }

    /**
     * Wait forever until successfully receive a row.
     * This method should be use only at the beginning of
     * transformation. All the step in the beginning can wait
     * until other steps finish producing data.
     * This method can block your thread forever.
     */
    public Object[] getRowUnitlSuccess(){
    	
    	try{
    		return queArray.take();
    	}
    	catch(InterruptedException e){
    		return null;
    	}
    }
    
    /**
     * @return Set indication that there is no more input
     */
    public void setDone()
    {
    	done.set(true);
    	    	
    }
    
    /**
     * @return Returns true if there is no more input and vice versa
     */
    public boolean isDone()
    {
    	return done.get();
    }
    
    /**
     * @return Returns the originStepName.
     */
    public String getOriginStepName()
    {
    	synchronized(originStepName){
    		return originStepName;
    	}
        
    }
    
    /**
     * @return Returns the originStepCopy.
     */
    public int getOriginStepCopy()
    {
        return originStepCopy.get();
    }
    
    /**
     * @return Returns the destinationStepName.
     */
    public String getDestinationStepName()
    {
        return destinationStepName;
    }
    
    /**
     * @return Returns the destinationStepCopy.
     */
    public int getDestinationStepCopy()
    {
    	return destinationStepCopy.get();    	
    }

    
    public String getName()
    {
        return toString();
    }
    
    /**
     * 
     * @return Return the size (or max capacity) of the RowSet
     */
    public int size()
    {
    	return queArray.size();
    }

    /**
     * This method is used only in Trans.java 
     * when created RowSet at line 333. Don't need
     * any synchronization on this method
     *     
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

	/**
	 * @return the rowMeta
	 */
	public RowMetaInterface getRowMeta() {
		return rowMeta;
	}

	/**
	 * @param rowMeta the rowMeta to set
	 */
	public void setRowMeta(RowMetaInterface rowMeta) {
		this.rowMeta = rowMeta;
	}

	/**
	 * @return the targetSlaveServer
	 */
	public String getRemoteSlaveServerName() {
		return remoteSlaveServerName;
	}

	/**
	 * @param remoteSlaveServerName the remote slave server to set
	 */
	public void setRemoteSlaveServerName(String remoteSlaveServerName) {
		this.remoteSlaveServerName = remoteSlaveServerName;
	}
}