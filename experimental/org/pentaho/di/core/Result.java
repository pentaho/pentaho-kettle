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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

	private int exitStatus;
	private ArrayList rows;
	private Map resultFiles;
	
	public boolean stopped;
    private int nrLinesRejected;
	
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
		rows=new ArrayList();
		resultFiles = new Hashtable();
		
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
			
			// Clone result rows and files as well...
			if (rows!=null)
			{
				ArrayList clonedRows = new ArrayList();
				for (int i=0;i<rows.size();i++)
				{
					clonedRows.add( ((RowMetaAndData)rows.get(i)).clone() );
				}
				result.setRows(clonedRows);
			}

			if (resultFiles!=null)
			{
				Map clonedFiles = new Hashtable();
                Collection files = resultFiles.values();
                for (Iterator iter = files.iterator(); iter.hasNext();)
                {
                    ResultFile file = (ResultFile) iter.next();
                    clonedFiles.put(file.getFile().toString(), file.clone());
                }
				result.setResultFiles(clonedFiles);
			}

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
     * Clear the numbers in this result, set them all to 0 
     *
     */
    public void clear()
    {
        nrLinesInput=0;
        nrLinesOutput=0;
        nrLinesRead=0;
        nrLinesWritten=0;
        nrLinesUpdated=0;
        nrLinesRejected=0;
        nrLinesDeleted=0;
        nrErrors=0;
        nrFilesRetrieved=0;
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
        nrLinesRejected+=res.getNrLinesRejected();
        nrLinesDeleted+=res.getNrLinesDeleted();
        nrErrors+=res.getNrErrors();
        nrFilesRetrieved+=res.getNrFilesRetrieved();
        resultFiles.putAll(res.getResultFiles());
        rows.addAll(res.getRows());
    }

    /**
     * @return Returns the result files.  This is a Map with String as key and ResultFile as value.
     */
    public Map getResultFiles()
    {
        return resultFiles;
    }

    /**
     * @return Returns the result files.  This is a list of type ResultFile
     */
    public List getResultFilesList()
    {
        return new ArrayList(resultFiles.values());
    }
	/**
	 * @param usedFiles The list of result files to set. This is a list of type ResultFile
	 */
	public void setResultFiles(Map usedFiles)
	{
		this.resultFiles = usedFiles;
	}

    /**
     * @return the nrLinesRejected
     */
    public int getNrLinesRejected()
    {
        return nrLinesRejected;
    }

    /**
     * @param nrLinesRejected the nrLinesRejected to set
     */
    public void setNrLinesRejected(int nrLinesRejected)
    {
        this.nrLinesRejected = nrLinesRejected;
    }
}
