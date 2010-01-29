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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

/**
 * Result describes the result of the execution of a Transformation or a Job.
 * Using this, the Result can be evaluated after execution.
 * 
 * @author Matt
 * @since 05-11-2003
 */
public class Result implements Cloneable
{
	public static final String XML_TAG = "result";
	public static final String XML_FILES_TAG = "result-file";
	public static final String XML_FILE_TAG = "result-file";
	public static final String XML_ROWS_TAG = "result-rows";
	
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
	private List<RowMetaAndData> rows;
	private Map<String, ResultFile> resultFiles;
	
	public boolean stopped;
    private long nrLinesRejected;
	
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
		rows=new ArrayList<RowMetaAndData>();
		resultFiles = new ConcurrentHashMap<String, ResultFile>();
		
		stopped=false;
		entryNr=0;
	}

	public Result(int nr)
	{
		this();
		this.entryNr=nr;
	}
	
	public Result clone()
	{
		try
		{
			Result result = (Result)super.clone();
			
			// Clone result rows and files as well...
			if (rows!=null)
			{
				List<RowMetaAndData> clonedRows = new ArrayList<RowMetaAndData>();
				for (int i=0;i<rows.size();i++)
				{
					clonedRows.add( (rows.get(i)).clone() );
				}
				result.setRows(clonedRows);
			}

			if (resultFiles!=null)
			{
				Map<String, ResultFile> clonedFiles = new ConcurrentHashMap<String, ResultFile>();
                Collection<ResultFile> files = resultFiles.values();
                for (ResultFile file : files)
                {
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
	
	public String getReadWriteThroughput(int seconds)
	{
		String throughput = null;
		if (seconds != 0)
		{
			String readClause = null, writtenClause = null;
			if (getNrLinesRead() > 0) {
				readClause = String.format("lines read: %d ( %d lines/s)", getNrLinesRead(), (getNrLinesRead()/seconds));
			}
			if (getNrLinesWritten() > 0) {
				writtenClause = String.format("%slines written: %d ( %d lines/s)", (getNrLinesRead() > 0 ? "; " : ""), getNrLinesWritten(), (getNrLinesWritten()/seconds));
			}
			if (readClause != null || writtenClause != null) {
				throughput = String.format("Transformation %s%s", (getNrLinesRead() > 0 ? readClause : ""), (getNrLinesWritten() > 0 ? writtenClause : ""));
			}
		}
		return throughput;
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
	public List<RowMetaAndData> getRows()
	{
		return rows;
	}
	
	/**
	 * @param rows The rows to set.
	 */
	public void setRows(List<RowMetaAndData> rows)
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
     * @return This Result object serialized as XML
     * @throws IOException 
     */
    public String getXML() throws IOException
    {
    	StringBuffer xml = new StringBuffer();
    	
        xml.append(XMLHandler.openTag(XML_TAG));
        
        // First the metrics...
        //
        xml.append(XMLHandler.addTagValue("lines_input", nrLinesInput));
        xml.append(XMLHandler.addTagValue("lines_output", nrLinesOutput));
        xml.append(XMLHandler.addTagValue("lines_read", nrLinesRead));
        xml.append(XMLHandler.addTagValue("lines_written", nrLinesWritten));
        xml.append(XMLHandler.addTagValue("lines_updated", nrLinesUpdated));
        xml.append(XMLHandler.addTagValue("lines_rejected", nrLinesRejected));
        xml.append(XMLHandler.addTagValue("lines_deleted", nrLinesDeleted));
        xml.append(XMLHandler.addTagValue("nr_errors", nrErrors));
        xml.append(XMLHandler.addTagValue("nr_files_retrieved", nrFilesRetrieved));
        xml.append(XMLHandler.addTagValue("entry_nr", entryNr));
        
        // The high level results...
        //
        xml.append(XMLHandler.addTagValue("result", result));;
        xml.append(XMLHandler.addTagValue("exit_status", exitStatus));
        xml.append(XMLHandler.addTagValue("is_stopped", stopped));
                
        // Export the result files
        //
        xml.append(XMLHandler.openTag(XML_FILES_TAG));
        for (ResultFile resultFile: resultFiles.values())
        {
        	xml.append(resultFile.getXML());
        }
        xml.append(XMLHandler.closeTag(XML_FILES_TAG));
        
        xml.append(XMLHandler.openTag(XML_ROWS_TAG));
        boolean firstRow=true;
        RowMetaInterface rowMeta = null;
        for (RowMetaAndData row : rows)
        {
        	if (firstRow)
        	{
        		firstRow=false;
        		rowMeta = row.getRowMeta();
            	xml.append(rowMeta.getMetaXML());
        	}
        	xml.append(rowMeta.getDataXML(row.getData()));
        }
        xml.append(XMLHandler.closeTag(XML_ROWS_TAG));
        
        xml.append(XMLHandler.closeTag(XML_TAG));
        
        return xml.toString();
    }
    
    public Result(Node node) throws IOException
    {
    	this();
    	
    	// First we read the metrics...
    	//
    	nrLinesInput     = Const.toLong(XMLHandler.getTagValue(node, "lines_input"), 0L);
    	nrLinesOutput    = Const.toLong(XMLHandler.getTagValue(node, "lines_output"), 0L);
    	nrLinesRead      = Const.toLong(XMLHandler.getTagValue(node, "lines_read"), 0L);
    	nrLinesWritten   = Const.toLong(XMLHandler.getTagValue(node, "lines_written"), 0L);
    	nrLinesUpdated   = Const.toLong(XMLHandler.getTagValue(node, "lines_updated"), 0L);
    	nrLinesRejected  = Const.toLong(XMLHandler.getTagValue(node, "lines_rejected"), 0L);
    	nrLinesDeleted   = Const.toLong(XMLHandler.getTagValue(node, "lines_deleted"), 0L);
    	nrErrors         = Const.toLong(XMLHandler.getTagValue(node, "nr_errors"), 0L);
    	nrFilesRetrieved = Const.toLong(XMLHandler.getTagValue(node, "nr_files_retrieved"), 0L);
    	entryNr          = Const.toLong(XMLHandler.getTagValue(node, "entry_nr"), 0L);
    	
        // The high level results...
        //
        result  = "Y".equalsIgnoreCase( XMLHandler.getTagValue(node, "result"));
        exitStatus = Integer.parseInt( XMLHandler.getTagValue(node, "exit_status") );
        stopped = "Y".equalsIgnoreCase( XMLHandler.getTagValue(node, "is_stopped" ));
        
    	// Now read back the result files...
    	//
    	Node resultFilesNode = XMLHandler.getSubNode(node, XML_FILES_TAG);
    	int nrResultFiles = XMLHandler.countNodes(resultFilesNode, XML_FILE_TAG);
    	for (int i=0;i<nrResultFiles;i++)
    	{
    		try {
				ResultFile resultFile = new ResultFile(XMLHandler.getSubNodeByNr(resultFilesNode, XML_FILE_TAG, i));
				resultFiles.put(resultFile.getFile().toString(), resultFile);
			} catch (IOException e) {
				
				LogWriter.getInstance().logError("Execution result", "Unexpected error reading back a ResultFile object from XML", e);
			} 
    	}

    	// Let's also read back the result rows...
    	//
    	Node resultRowsNode = XMLHandler.getSubNode(node, XML_ROWS_TAG);
    	int nrResultRows = XMLHandler.countNodes(resultFilesNode, RowMeta.XML_DATA_TAG);
    	if (nrResultRows>0)
    	{
    		// OK, get the metadata first...
    		//
    		RowMeta rowMeta = new RowMeta( XMLHandler.getSubNode(resultRowsNode, RowMeta.XML_META_TAG) );
    		for (int i=0;i<nrResultRows;i++)
    		{
    			Object[] rowData = rowMeta.getRow(XMLHandler.getSubNodeByNr(resultRowsNode, RowMeta.XML_META_TAG, i));
    			rows.add(new RowMetaAndData(rowMeta, rowData));
    		}
    	}

    }

    /**
     * @return Returns the result files.  This is a Map with String as key and ResultFile as value.
     */
    public Map<String, ResultFile> getResultFiles()
    {
        return resultFiles;
    }

    /**
     * @return Returns the result files.  This is a list of type ResultFile
     */
    public List<ResultFile> getResultFilesList()
    {
        return new ArrayList<ResultFile>(resultFiles.values());
    }
	/**
	 * @param usedFiles The list of result files to set. This is a list of type ResultFile
	 */
	public void setResultFiles(Map<String, ResultFile> usedFiles)
	{
		this.resultFiles = usedFiles;
	}

    /**
     * @return the nrLinesRejected
     */
    public long getNrLinesRejected()
    {
        return nrLinesRejected;
    }

    /**
     * @param nrLinesRejected the nrLinesRejected to set
     */
    public void setNrLinesRejected(long nrLinesRejected)
    {
        this.nrLinesRejected = nrLinesRejected;
    }
}
