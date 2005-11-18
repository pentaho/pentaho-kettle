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
import java.util.ArrayList;

/**
 * Result describes the result of the execution of a Transformation or a Job.
 * Using this, the Result can be evaluated after execution.
 * 
 * @author Matt
 * @since 05-11-2003
 */
public class Result implements Cloneable
{
	private long nrErrors;
	private long nrLinesInput;
	private long nrLinesOutput;
	private long nrLinesUpdated;
	private long nrLinesRead;
	private long nrLinesWritten;
    private long nrLinesDeleted;
    
	private long nrFilesRetrieved;
	
	private boolean result;
	private long entryNr;

	public int exitStatus;
	public ArrayList rows;
	
	public boolean stopped;
	
	public Result()
	{
		nrErrors=0L;
		nrLinesInput=0L;
		nrLinesOutput=0L;
		nrLinesUpdated=0L;
		nrLinesRead=0L;
		nrLinesWritten=0L;
		result=false;
		
		exitStatus=0;
		rows=null;
		
		stopped=false;
		entryNr=0;
	}

	public Result(int nr)
	{
		this();
		this.entryNr=nr;
	}
	
	public Object clone()
	{
		try
		{
			Result result = (Result)super.clone();
			return result;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public String toString()
	{
		return "nr="+entryNr+", errors="+nrErrors+", exit_status="+exitStatus+(stopped?" (Stopped)":""+", result="+result); 
	}
	
	/**
	 * @return Returns the number of files retrieved.
	 */
	public long getNrFilesRetrieved()
	{
		return nrFilesRetrieved;
	}
	
	/**
	 * @param filesRetrieved The number of files retrieved to set.
	 */
	public void setNrFilesRetrieved(long filesRetrieved)
	{
		this.nrFilesRetrieved = filesRetrieved;
	}
	
	
	
	/**
	 * @return Returns the entryNr.
	 */
	public long getEntryNr()
	{
		return entryNr;
	}
	
	/**
	 * @param entryNr The entryNr to set.
	 */
	public void setEntryNr(long entryNr)
	{
		this.entryNr = entryNr;
	}
	
	/**
	 * @return Returns the exitStatus.
	 */
	public int getExitStatus()
	{
		return exitStatus;
	}
	
	/**
	 * @param exitStatus The exitStatus to set.
	 */
	public void setExitStatus(int exitStatus)
	{
		this.exitStatus = exitStatus;
	}
	
	/**
	 * @return Returns the nrErrors.
	 */
	public long getNrErrors()
	{
		return nrErrors;
	}
	
	/**
	 * @param nrErrors The nrErrors to set.
	 */
	public void setNrErrors(long nrErrors)
	{
		this.nrErrors = nrErrors;
	}
	
	/**
	 * @return Returns the nrLinesInput.
	 */
	public long getNrLinesInput()
	{
		return nrLinesInput;
	}
	
	/**
	 * @param nrLinesInput The nrLinesInput to set.
	 */
	public void setNrLinesInput(long nrLinesInput)
	{
		this.nrLinesInput = nrLinesInput;
	}
	
	/**
	 * @return Returns the nrLinesOutput.
	 */
	public long getNrLinesOutput()
	{
		return nrLinesOutput;
	}
	
	/**
	 * @param nrLinesOutput The nrLinesOutput to set.
	 */
	public void setNrLinesOutput(long nrLinesOutput)
	{
		this.nrLinesOutput = nrLinesOutput;
	}
	
	/**
	 * @return Returns the nrLinesRead.
	 */
	public long getNrLinesRead()
	{
		return nrLinesRead;
	}
	
	/**
	 * @param nrLinesRead The nrLinesRead to set.
	 */
	public void setNrLinesRead(long nrLinesRead)
	{
		this.nrLinesRead = nrLinesRead;
	}
	
	/**
	 * @return Returns the nrLinesUpdated.
	 */
	public long getNrLinesUpdated()
	{
		return nrLinesUpdated;
	}
	
	/**
	 * @param nrLinesUpdated The nrLinesUpdated to set.
	 */
	public void setNrLinesUpdated(long nrLinesUpdated)
	{
		this.nrLinesUpdated = nrLinesUpdated;
	}
	
	/**
	 * @return Returns the nrLinesWritten.
	 */
	public long getNrLinesWritten()
	{
		return nrLinesWritten;
	}
	
	/**
	 * @param nrLinesWritten The nrLinesWritten to set.
	 */
	public void setNrLinesWritten(long nrLinesWritten)
	{
		this.nrLinesWritten = nrLinesWritten;
	}
	
	/**
     * @return Returns the nrLinesDeleted.
     */
    public long getNrLinesDeleted()
    {
        return nrLinesDeleted;
    }

    /**
     * @param nrLinesDeleted The nrLinesDeleted to set.
     */
    public void setNrLinesDeleted(long nrLinesDeleted)
    {
        this.nrLinesDeleted = nrLinesDeleted;
    }

    /**
	 * @return Returns the result.
	 */
	public boolean getResult()
	{
		return result;
	}
	
	/**
	 * @param result The result to set.
	 */
	public void setResult(boolean result)
	{
		this.result = result;
	}
	
	/**
	 * @return Returns the rows.
	 */
	public ArrayList getRows()
	{
		return rows;
	}
	
	/**
	 * @param rows The rows to set.
	 */
	public void setRows(ArrayList rows)
	{
		this.rows = rows;
	}
	
	/**
	 * @return Returns the stopped.
	 */
	public boolean isStopped()
	{
		return stopped;
	}
	
	/**
	 * @param stopped The stopped to set.
	 */
	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}

    /**
     * Add the NrOfLines from a different result to this result 
     * @param res The result to add
     */
    public void add(Result res)
    {
        nrLinesInput+=res.getNrLinesInput();
        nrLinesOutput+=res.getNrLinesOutput();
        nrLinesRead+=res.getNrLinesRead();
        nrLinesWritten+=res.getNrLinesWritten();
        nrLinesUpdated+=res.getNrLinesUpdated();
        nrLinesDeleted+=res.getNrLinesDeleted();
        nrErrors+=res.getNrErrors();
        nrFilesRetrieved+=res.getNrFilesRetrieved();
    }

}
