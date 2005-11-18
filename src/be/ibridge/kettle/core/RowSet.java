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

 

package be.ibridge.kettle.core;
import java.util.LinkedList;

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
	
	private LinkedList set;
	private boolean done;
	public int read_pointer;
	public int write_pointer;
	
	public int size_rowset;
	
	public int empty_calls;
	public int full_calls;
	
	private  String    originStepName;
	private  int       originStepCopy;
	private  String    destinationStepName;
	private  int       destinationStepCopy;
		
	private BaseStep thread_from;
	private BaseStep thread_to;
	
	private int maxsize;
	
	public RowSet(int maxsize)
	{
		set = new LinkedList(); // create new linked list with 0 elements.
		
		done=false;
		
		size_rowset=0;
		empty_calls=0;
		full_calls=0;
		
		this.maxsize = maxsize;
	}
	
	//
	// We always add rows to the end of the linked list
	// 
	public synchronized void putRow(Row r)
	{
		set.addLast(r);
		size_rowset++;
	}
	
	//
	// We always read rows from the beginning of the linked list
	// Once we read the row, we remove it from the list
	// 
	public synchronized Row getRow()
	{
		Row r = (Row)set.getFirst();
		set.removeFirst();
		size_rowset--;
		
		return r;
	}
	
	public synchronized boolean isEmpty()
	{
		empty_calls++;
		return (size_rowset==0);
	}
	
	// Don't let the buffer get too big, we might run out of memory
	// If the following steps are working slower!
	//
	public synchronized boolean isFull()
	{
		full_calls++;
		return (size_rowset>=maxsize);
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
    public String getOriginStepName()
    {
        return originStepName;
    }
    
    /**
     * @return Returns the originStepCopy.
     */
    public int getOriginStepCopy()
    {
        return originStepCopy;
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
	//	name=n;
	//}
	
	public String getName()
	{
		return toString();
	}
	
	public synchronized int size()
	{
		return set.size();
	}

	public void setThreadNameFromToCopy(String from, int from_copy, String to, int to_copy)
	{
		originStepName      = from;
		originStepCopy = from_copy;
		destinationStepName        = to;
		destinationStepCopy   = to_copy;
	}
	
	public void setThreadFromTo(BaseStep from, BaseStep to)
	{
		thread_from = from;
		thread_to   = to;
	}
	
	public boolean waitFrom()
	{
		try
		{
			thread_from.waiting = true;
			thread_from.wait();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

	public boolean waitTo()
	{
		try
		{
			thread_to.waiting=true;
			thread_to.wait();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

	public boolean notifyFrom()
	{
		if (!thread_from.waiting) return true;
		try
		{
			thread_from.notifyAll();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

	public boolean notifyTo()
	{
		if (!thread_to.waiting) return true;
		try
		{
			thread_to.notifyAll();
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}

	public boolean setPriorityFrom(int prior)
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

	public boolean setPriorityTo(int prior)
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
	
	public boolean waitingThreadFrom()
	{
		return thread_from.waiting;
	}

	public boolean waitingThreadTo()
	{
		return thread_to.waiting;
	}
	
	public String toString()
	{
		return originStepName + "." + originStepCopy + 
               " - " +
		       destinationStepName + "." + destinationStepCopy
		       ; 
	}
	
}
