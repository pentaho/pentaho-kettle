 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

 

package org.pentaho.di.core;

import org.pentaho.di.core.row.RowMetaInterface;

import be.ibridge.kettle.trans.step.BaseStep;

/**
 * Contains a buffer of rows.  Getting rows from the buffer or putting rows in the buffer is synchronised to allow concurrent use of multiple Threads.
 * 
 * @author Matt
 * @since 04-04-2003
 *
 */
public class RowSet
{
    public final static int ERR_NO_ERROR        = 0;
    public final static int ERR_ROW_IS_BUSY     = 1;

    private RowMetaInterface rowMeta;
    
    private Object[] queArray; 
    private int   maxSize;
    private int   front;
    private int   rear;
    private int   nItems;
    
    private boolean done;
        
    private  String    originStepName;
    private  int       originStepCopy;
    private  String    destinationStepName;
    private  int       destinationStepCopy;
        
    private BaseStep thread_from;
    private BaseStep thread_to; 
    
    public RowSet(int maxSize)
    {
        this.maxSize = maxSize;
        
        // create new queue with 0 elements. 
        queArray = new Object[maxSize];
        front    = 0;
        rear     = -1;
        nItems   = 0;        
        done     = false;
    }
    
    //
    // We always add rows to the end of the linked queue
    // 
    public synchronized void putRow(Object[] r)
    {
        if (rear == maxSize - 1) // deal with wraparound
            rear = -1;
        queArray[++rear] = r;    // increment rear and insert
        nItems++;                // one more item
    }
    
    //
    // We always read rows from the beginning of the linked queue
    // Once we read the row, we remove it from the queue
    // 
    public synchronized Object[] getRow()
    {
        Object[] temp = (Object[]) queArray[front++]; // get value and incr front
        if (front == maxSize)         // deal with wraparound
            front = 0;
        nItems--;                     // one less item
        return temp;
    }

    public synchronized Object[] lookAtFirst()
    {
        return (Object[]) queArray[front];
    }

    public synchronized boolean isEmpty()
    {
        return (nItems == 0);
    }
    
    // Don't let the buffer get too big, we might run out 
    // of memory if the following steps are working slower
    //
    public synchronized boolean isFull()
    {
        return (nItems == maxSize);
    }
    
    public synchronized void setDone()
    {
        done=true;
    }
    
    public synchronized boolean isDone()
    {
        return done;
    }
    
    /**
     * @return Returns the originStepName.
     */
    public synchronized String getOriginStepName()
    {
        return originStepName;
    }
    
    /**
     * @return Returns the originStepCopy.
     */
    public synchronized int getOriginStepCopy()
    {
        return originStepCopy;
    }
    
    /**
     * @return Returns the destinationStepName.
     */
    public synchronized String getDestinationStepName()
    {
        return destinationStepName;
    }
    
    /**
     * @return Returns the destinationStepCopy.
     */
    public synchronized int getDestinationStepCopy()
    {
        return destinationStepCopy;
    }
    
    
    // Name of a rowset should be unique:
    // Take: name of orinating step
    // Then " - "
    // Then: name of target step
    // Then: " version "
    // Then: the copynr
    
    //public void setName(String n)
    //{
    //  name=n;
    //}
    
    public synchronized String getName()
    {
        return toString();
    }
    
    public synchronized int size()
    {
        return nItems;
    }

    public synchronized void setThreadNameFromToCopy(String from, int from_copy, String to, int to_copy)
    {
        originStepName        = from;
        originStepCopy        = from_copy;
        destinationStepName   = to;
        destinationStepCopy   = to_copy;
    }
    
    public synchronized void setThreadFromTo(BaseStep from, BaseStep to)
    {
        thread_from = from;
        thread_to   = to;
    }
        
    public synchronized boolean setPriorityFrom(int prior)
    {
        if ( thread_from == null ||
             thread_from.getPriority()==prior ||
             !thread_from.isAlive()
           )  return true;
           
        try
        {
            thread_from.setPriority(prior);
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }

    public synchronized boolean setPriorityTo(int prior)
    {
        if ( thread_to == null ||
             thread_to.getPriority()==prior ||
             !thread_to.isAlive()
           )  return true;

        try
        {
            thread_to.setPriority(prior);
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }
        
    public synchronized String toString()
    {
        return originStepName + "." + originStepCopy + 
               " - " +
               destinationStepName + "." + destinationStepCopy
               ; 
    }

    /**
     * @return the rowMeta
     */
    public RowMetaInterface getRowMeta()
    {
        return rowMeta;
    }

    /**
     * @param rowMeta the rowMeta to set
     */
    public void setRowMeta(RowMetaInterface rowMeta)
    {
        this.rowMeta = rowMeta;
    }   
}