/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Node;

/**
 * Describes the result of the execution of a Transformation or a Job. The information available includes the following:
 * <p>
 * <ul>
 * <li>Number of errors the job or transformation encountered</li>
 * <li>Number of lines input</li>
 * <li>Number of lines output</li>
 * <li>Number of lines updated</li>
 * <li>Number of lines read</li>
 * <li>Number of lines written</li>
 * <li>Number of lines deleted</li>
 * <li>Number of lines rejected</li>
 * <li>Number of files retrieved</li>
 * <li>Boolean result of the execution</li>
 * <li>Exit status value</li>
 * <li>Whether the transformation was stopped</li>
 * <li>Logging information (channel ID and text)</li>
 * </p>
 *
 * After execution of a job or transformation, the Result can be evaluated.
 *
 * @author Matt
 * @since 05-11-2003
 */
@XmlRootElement
public class Result implements Cloneable {

  /** A constant specifying the tag value for the XML node of the result object */
  public static final String XML_TAG = "result";

  /** A constant specifying the tag value for the XML node for result files entry */
  public static final String XML_FILES_TAG = "result-file";

  /** A constant specifying the tag value for the XML node for the result file entry */
  public static final String XML_FILE_TAG = "result-file";

  /** A constant specifying the tag value for the XML node for the result rows entry */
  public static final String XML_ROWS_TAG = "result-rows";

  /** The number of errors during the transformation or job */
  private long nrErrors;

  /** The number of lines input. */
  private long nrLinesInput;

  /** The number of lines output. */
  private long nrLinesOutput;

  /** The number of lines updated. */
  private long nrLinesUpdated;

  /** The number of lines read. */
  private long nrLinesRead;

  /** The number of lines written. */
  private long nrLinesWritten;

  /** The number of lines deleted. */
  private long nrLinesDeleted;

  /** The number of files retrieved. */
  private long nrFilesRetrieved;

  /** The result of the job or transformation, true if successful, false otherwise. */
  private boolean result;

  /** The entry number. */
  private long entryNr;

  /** The exit status. */
  private int exitStatus;

  /** The rows. */
  private List<RowMetaAndData> rows;

  /** The result files. */
  private Map<String, ResultFile> resultFiles;

  /** Whether the job or transformation was stopped. */
  public boolean stopped;

  /** The number of lines rejected. */
  private long nrLinesRejected;

  /** The log channel id. */
  private String logChannelId;

  /** The log text. */
  private StringBuilder logText = new StringBuilder( 10000 );

  /**
   * safe stop.
   */
  private boolean safeStop;

  /**
   * Elapsed time of the ETL execution in milliseconds
   */
  private long elapsedTimeMillis;

  /**
   * Unique identifier of an ETL execution, should one ever care to declare one such
   */
  private String executionId;

  /**
   * Instantiates a new Result object, setting default values for all members
   */
  public Result() {
    nrErrors = 0L;
    nrLinesInput = 0L;
    nrLinesOutput = 0L;
    nrLinesUpdated = 0L;
    nrLinesRead = 0L;
    nrLinesWritten = 0L;
    result = false;

    exitStatus = 0;
    rows = new ArrayList<RowMetaAndData>();
    resultFiles = new ConcurrentHashMap<String, ResultFile>();

    stopped = false;
    entryNr = 0;
  }

  /**
   * Instantiates a new Result object, setting default values for all members and the entry number
   *
   * @param nr
   *          the entry number for the Result
   */
  public Result( int nr ) {
    this();
    this.entryNr = nr;
  }

  /**
   * Performs a semi-deep copy/clone but does not clone the rows from the Result
   *
   * @return An almost-clone of the Result, minus the rows
   */
  public Result lightClone() {
    // This light-weight clone doesn't clone rows
    try {
      Result result = (Result) super.clone();
      result.setRows( null );
      if ( resultFiles != null ) {
        Map<String, ResultFile> clonedFiles = new ConcurrentHashMap<String, ResultFile>();
        Collection<ResultFile> files = resultFiles.values();
        for ( ResultFile file : files ) {
          clonedFiles.put( file.getFile().toString(), file.clone() );
        }
        result.setResultFiles( clonedFiles );
      }
      return result;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }

  }

  /**
   * Clones the Result, including rows and files. To perform a clone without rows, use lightClone()
   *
   * @see java.lang.Object#clone()
   * @see Result#lightClone
   *
   * @return A clone of the Result object
   */
  @Override
  public Result clone() {
    try {
      Result result = (Result) super.clone();

      // Clone result rows and files as well...
      if ( rows != null ) {
        List<RowMetaAndData> clonedRows = new ArrayList<RowMetaAndData>();
        for ( int i = 0; i < rows.size(); i++ ) {
          clonedRows.add( ( rows.get( i ) ).clone() );
        }
        result.setRows( clonedRows );
      }

      if ( resultFiles != null ) {
        Map<String, ResultFile> clonedFiles = new ConcurrentHashMap<String, ResultFile>();
        Collection<ResultFile> files = resultFiles.values();
        for ( ResultFile file : files ) {
          clonedFiles.put( file.getFile().toString(), file.clone() );
        }
        result.setResultFiles( clonedFiles );
      }

      return result;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  /**
   * Creates a string containing the read/write throughput. Throughput in this case is defined as two measures, number
   * of lines read or written and number of lines read/written per second.
   *
   * @param seconds
   *          the number of seconds with which to determine the read/write throughput
   * @return a string containing the read write throughput measures with labelling text
   */
  public String getReadWriteThroughput( int seconds ) {
    String throughput = null;
    if ( seconds != 0 ) {
      String readClause = null, writtenClause = null;
      if ( getNrLinesRead() > 0 ) {
        readClause =
          String.format( "lines read: %d ( %d lines/s)", getNrLinesRead(), ( getNrLinesRead() / seconds ) );
      }
      if ( getNrLinesWritten() > 0 ) {
        writtenClause =
          String.format(
            "%slines written: %d ( %d lines/s)", ( getNrLinesRead() > 0 ? "; " : "" ), getNrLinesWritten(),
            ( getNrLinesWritten() / seconds ) );
      }
      if ( readClause != null || writtenClause != null ) {
        throughput =
          String.format(
            "Transformation %s%s", ( getNrLinesRead() > 0 ? readClause : "" ), ( getNrLinesWritten() > 0
              ? writtenClause : "" ) );
      }
    }
    return throughput;
  }

  /**
   * Returns a string representation of the Result object
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "nr="
      + entryNr + ", errors=" + nrErrors + ", exit_status=" + exitStatus
      + ( stopped ? " (Stopped)" : "" + ", result=" + result );
  }

  /**
   * Returns the number of files retrieved during execution of this transformation or job
   *
   * @return the number of files retrieved
   */
  public long getNrFilesRetrieved() {
    return nrFilesRetrieved;
  }

  /**
   * Sets the number of files retrieved to the specified value
   *
   * @param filesRetrieved
   *          The number of files retrieved to set.
   */
  public void setNrFilesRetrieved( long filesRetrieved ) {
    this.nrFilesRetrieved = filesRetrieved;
  }

  /**
   * Returns the entry number
   *
   * @return the entry number
   */
  public long getEntryNr() {
    return entryNr;
  }

  /**
   * Sets the entry number to the specified value
   *
   * @param entryNr
   *          The entry number to set.
   */
  public void setEntryNr( long entryNr ) {
    this.entryNr = entryNr;
  }

  /**
   * Returns the exit status value.
   *
   * @return the exit status.
   */
  public int getExitStatus() {
    return exitStatus;
  }

  /**
   * Sets the exit status value to the specified value
   *
   * @param exitStatus
   *          The exit status to set.
   */
  public void setExitStatus( int exitStatus ) {
    this.exitStatus = exitStatus;
  }

  /**
   * Returns the number of errors that occurred during this transformation or job
   *
   * @return the number of errors
   */
  public long getNrErrors() {
    return nrErrors;
  }

  /**
   * Sets the number of errors that occurred during execution of this transformation or job
   *
   * @param nrErrors
   *          The number of errors to set
   */
  public void setNrErrors( long nrErrors ) {
    this.nrErrors = nrErrors;
  }

  /**
   * Returns the number of lines input during execution of this transformation or job
   *
   * @return the number of lines input
   */
  public long getNrLinesInput() {
    return nrLinesInput;
  }

  /**
   * Sets the number of lines input during execution of this transformation or job
   *
   * @param nrLinesInput
   *          The number of lines input to set.
   */
  public void setNrLinesInput( long nrLinesInput ) {
    this.nrLinesInput = nrLinesInput;
  }

  /**
   * Returns the number of lines output during execution of this transformation or job
   *
   * @return the number of lines output
   */
  public long getNrLinesOutput() {
    return nrLinesOutput;
  }

  /**
   * Sets the number of lines output during execution of this transformation or job
   *
   * @param nrLinesOutput
   *          The number of lines output to set
   */
  public void setNrLinesOutput( long nrLinesOutput ) {
    this.nrLinesOutput = nrLinesOutput;
  }

  /**
   * Returns the number of lines read during execution of this transformation or job
   *
   * @return the number of lines read
   */
  public long getNrLinesRead() {
    return nrLinesRead;
  }

  /**
   * Sets the number of lines read during execution of this transformation or job
   *
   * @param nrLinesRead
   *          The number of lines read to set.
   */
  public void setNrLinesRead( long nrLinesRead ) {
    this.nrLinesRead = nrLinesRead;
  }

  /**
   * Returns the number of lines updated during execution of this transformation or job
   *
   * @return the number of lines updated
   */
  public long getNrLinesUpdated() {
    return nrLinesUpdated;
  }

  /**
   * Sets the number of lines updated during execution of this transformation or job
   *
   * @param nrLinesUpdated
   *          The number of lines updated to set.
   */
  public void setNrLinesUpdated( long nrLinesUpdated ) {
    this.nrLinesUpdated = nrLinesUpdated;
  }

  /**
   * Returns the number of lines written during execution of this transformation or job
   *
   * @return the number of lines written
   */
  public long getNrLinesWritten() {
    return nrLinesWritten;
  }

  /**
   * Sets the number of lines written during execution of this transformation or job
   *
   * @param nrLinesWritten
   *          The number of lines written to set.
   */
  public void setNrLinesWritten( long nrLinesWritten ) {
    this.nrLinesWritten = nrLinesWritten;
  }

  /**
   * Returns the number of lines deleted during execution of this transformation or job
   *
   * @return the number of lines deleted
   */
  public long getNrLinesDeleted() {
    return nrLinesDeleted;
  }

  /**
   * Sets the number of lines deleted during execution of this transformation or job
   *
   * @param nrLinesDeleted
   *          The number of lines deleted to set.
   */
  public void setNrLinesDeleted( long nrLinesDeleted ) {
    this.nrLinesDeleted = nrLinesDeleted;
  }

  /**
   * Returns the boolean result of this transformation or job
   *
   * @return true if the transformation or job was successful, false otherwise
   */
  public boolean getResult() {
    return result;
  }

  /**
   * Sets the result of the transformation or job. A value of true should indicate a successful execution, a value of
   * false should indicate an error condition.
   *
   * @param result
   *          The boolean result to set.
   */
  public void setResult( boolean result ) {
    this.result = result;
  }

  /**
   * Returns the resulting rowset from the job or transformation. For example, Result rows are used in jobs where
   * entries wish to receive the results of previous executions of jobs or transformations. The Result rows can be used
   * to do many kinds of transformation or job post-processing.
   *
   * @return a List of rows associated with the result of execution of a job or transformation
   */
  public List<RowMetaAndData> getRows() {
    return rows;
  }

  /**
   * Sets the resulting rowset from the job or transformation execution
   *
   * @param rows
   *          The List of rows to set.
   */
  public void setRows( List<RowMetaAndData> rows ) {
    this.rows = rows;
  }

  /**
   * Returns whether the transformation or job was stopped before completion
   *
   * @return true if stopped, false otherwise
   */
  public boolean isStopped() {
    return stopped;
  }

  /**
   * Sets whether the transformation or job was stopped before completion
   *
   * @param stopped
   *          true if the transformation or job was stopped, false otherwise
   */
  public void setStopped( boolean stopped ) {
    this.stopped = stopped;
  }

  /**
   * Clears the numbers in this result, setting them all to zero. Also deletes the logging text
   */
  public void clear() {
    nrLinesInput = 0;
    nrLinesOutput = 0;
    nrLinesRead = 0;
    nrLinesWritten = 0;
    nrLinesUpdated = 0;
    nrLinesRejected = 0;
    nrLinesDeleted = 0;
    nrErrors = 0;
    nrFilesRetrieved = 0;
    logText = new StringBuilder( 10000 );
  }

  /**
   * Add the numbers of lines from a different result to this result
   *
   * @param res
   *          The Result object from which to add
   */
  public void add( Result res ) {
    nrLinesInput += res.getNrLinesInput();
    nrLinesOutput += res.getNrLinesOutput();
    nrLinesRead += res.getNrLinesRead();
    nrLinesWritten += res.getNrLinesWritten();
    nrLinesUpdated += res.getNrLinesUpdated();
    nrLinesRejected += res.getNrLinesRejected();
    nrLinesDeleted += res.getNrLinesDeleted();
    nrErrors += res.getNrErrors();
    nrFilesRetrieved += res.getNrFilesRetrieved();
    resultFiles.putAll( res.getResultFiles() );
    logChannelId = res.getLogChannelId();
    logText.append( res.getLogText() );
    rows.addAll( res.getRows() );
  }

  /**
   * Returns a String object with the Result object serialized as XML
   *
   * @return This Result object serialized as XML
   */
  public String getXML() {
    try {

      StringBuilder xml = new StringBuilder();
      xml.append( XMLHandler.openTag( XML_TAG ) );
      setBasicXmlAttrs( xml );

      // Export the result files
      //
      xml.append( XMLHandler.openTag( XML_FILES_TAG ) );
      for ( ResultFile resultFile : resultFiles.values() ) {
        xml.append( resultFile.getXML() );
      }
      xml.append( XMLHandler.closeTag( XML_FILES_TAG ) );

      xml.append( XMLHandler.openTag( XML_ROWS_TAG ) );
      boolean firstRow = true;
      RowMetaInterface rowMeta = null;
      for ( RowMetaAndData row : rows ) {
        if ( firstRow ) {
          firstRow = false;
          rowMeta = row.getRowMeta();
          xml.append( rowMeta.getMetaXML() );
        }
        xml.append( rowMeta.getDataXML( row.getData() ) );
      }
      xml.append( XMLHandler.closeTag( XML_ROWS_TAG ) );

      xml.append( XMLHandler.closeTag( XML_TAG ) );

      return xml.toString();
    } catch ( IOException e ) {
      throw new RuntimeException( "Unexpected error encoding job result as XML", e );
    }
  }

  private StringBuilder setBasicXmlAttrs( StringBuilder xml ) {
    // First the metrics...
    //
    xml.append( XMLHandler.addTagValue( "lines_input", nrLinesInput ) );
    xml.append( XMLHandler.addTagValue( "lines_output", nrLinesOutput ) );
    xml.append( XMLHandler.addTagValue( "lines_read", nrLinesRead ) );
    xml.append( XMLHandler.addTagValue( "lines_written", nrLinesWritten ) );
    xml.append( XMLHandler.addTagValue( "lines_updated", nrLinesUpdated ) );
    xml.append( XMLHandler.addTagValue( "lines_rejected", nrLinesRejected ) );
    xml.append( XMLHandler.addTagValue( "lines_deleted", nrLinesDeleted ) );
    xml.append( XMLHandler.addTagValue( "nr_errors", nrErrors ) );
    xml.append( XMLHandler.addTagValue( "nr_files_retrieved", nrFilesRetrieved ) );
    xml.append( XMLHandler.addTagValue( "entry_nr", entryNr ) );

    // The high level results...
    //
    xml.append( XMLHandler.addTagValue( "result", result ) );
    xml.append( XMLHandler.addTagValue( "exit_status", exitStatus ) );
    xml.append( XMLHandler.addTagValue( "is_stopped", stopped ) );
    xml.append( XMLHandler.addTagValue( "log_channel_id", logChannelId ) );
    xml.append( XMLHandler.addTagValue( "log_text", logText.toString() ) );
    xml.append( XMLHandler.addTagValue( "elapsedTimeMillis", elapsedTimeMillis ) );
    xml.append( XMLHandler.addTagValue( "executionId", executionId ) );

    return xml;
  }

  public String getBasicXml() {
    StringBuilder xml = new StringBuilder();
    xml.append( XMLHandler.openTag( XML_TAG ) );
    setBasicXmlAttrs( xml );
    xml.append( XMLHandler.closeTag( XML_TAG ) );
    return xml.toString();
  }

  /**
   * Instantiates a new Result object from a DOM node
   *
   * @param node
   *          the DOM root node representing the desired Result
   * @throws KettleException
   *           if any errors occur during instantiation
   */
  public Result( Node node ) throws KettleException {
    this();

    // First we read the metrics...
    //
    nrLinesInput = Const.toLong( XMLHandler.getTagValue( node, "lines_input" ), 0L );
    nrLinesOutput = Const.toLong( XMLHandler.getTagValue( node, "lines_output" ), 0L );
    nrLinesRead = Const.toLong( XMLHandler.getTagValue( node, "lines_read" ), 0L );
    nrLinesWritten = Const.toLong( XMLHandler.getTagValue( node, "lines_written" ), 0L );
    nrLinesUpdated = Const.toLong( XMLHandler.getTagValue( node, "lines_updated" ), 0L );
    nrLinesRejected = Const.toLong( XMLHandler.getTagValue( node, "lines_rejected" ), 0L );
    nrLinesDeleted = Const.toLong( XMLHandler.getTagValue( node, "lines_deleted" ), 0L );
    nrErrors = Const.toLong( XMLHandler.getTagValue( node, "nr_errors" ), 0L );
    nrFilesRetrieved = Const.toLong( XMLHandler.getTagValue( node, "nr_files_retrieved" ), 0L );
    entryNr = Const.toLong( XMLHandler.getTagValue( node, "entry_nr" ), 0L );

    // The high level results...
    //
    result = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "result" ) );
    exitStatus = Integer.parseInt( XMLHandler.getTagValue( node, "exit_status" ) );
    stopped = "Y".equalsIgnoreCase( XMLHandler.getTagValue( node, "is_stopped" ) );

    logChannelId = XMLHandler.getTagValue( node, "log_channel_id" );
    String tagText = XMLHandler.getTagValue( node, "log_text" );
    if ( tagText != null ) {
      logText = new StringBuilder( tagText );
    }
    elapsedTimeMillis = Const.toLong( XMLHandler.getTagValue( node, "elapsedTimeMillis" ), 0L );
    executionId = XMLHandler.getTagValue( node, "executionId" );

    // Now read back the result files...
    //
    Node resultFilesNode = XMLHandler.getSubNode( node, XML_FILES_TAG );
    int nrResultFiles = XMLHandler.countNodes( resultFilesNode, XML_FILE_TAG );
    for ( int i = 0; i < nrResultFiles; i++ ) {
      try {
        ResultFile resultFile = new ResultFile( XMLHandler.getSubNodeByNr( resultFilesNode, XML_FILE_TAG, i ) );
        resultFiles.put( resultFile.getFile().toString(), resultFile );
      } catch ( KettleFileException e ) {
        throw new KettleException( "Unexpected error reading back a ResultFile object from XML", e );
      }
    }

    // Let's also read back the result rows...
    //
    Node resultRowsNode = XMLHandler.getSubNode( node, XML_ROWS_TAG );
    List<Node> resultNodes = XMLHandler.getNodes( resultRowsNode, RowMeta.XML_DATA_TAG );
    if ( !resultNodes.isEmpty() ) {
      // OK, get the metadata first...
      //
      RowMeta rowMeta = new RowMeta( XMLHandler.getSubNode( resultRowsNode, RowMeta.XML_META_TAG ) );
      for ( Node resultNode : resultNodes ) {
        Object[] rowData = rowMeta.getRow( resultNode );
        rows.add( new RowMetaAndData( rowMeta, rowData ) );
      }
    }

  }

  /**
   * Returns the result files as a Map with the filename as key and the ResultFile object as value
   *
   * @see org.pentaho.di.core.ResultFile
   *
   * @return a Map with String as key and ResultFile as value.
   */
  public Map<String, ResultFile> getResultFiles() {
    return resultFiles;
  }

  /**
   * Returns the result files as a List of type ResultFile
   *
   * @see org.pentaho.di.core.ResultFile
   * @return a list of type ResultFile containing this Result's ResultFile objects
   */
  public List<ResultFile> getResultFilesList() {
    return new ArrayList<ResultFile>( resultFiles.values() );
  }

  /**
   * Sets the result files for this Result to the specified Map of ResultFile objects
   *
   * @see org.pentaho.di.core.ResultFile
   *
   * @param usedFiles
   *          The Map of result files to set. This is a Map with the filename as key and ResultFile object as value
   */
  public void setResultFiles( Map<String, ResultFile> usedFiles ) {
    this.resultFiles = usedFiles;
  }

  /**
   * Returns the number of lines rejected during execution of this transformation or job
   *
   * @return the number of lines rejected
   */
  public long getNrLinesRejected() {
    return nrLinesRejected;
  }

  /**
   * Sets the number of lines rejected during execution of this transformation or job
   *
   * @param nrLinesRejected
   *          the number of lines rejected to set
   */
  public void setNrLinesRejected( long nrLinesRejected ) {
    this.nrLinesRejected = nrLinesRejected;
  }

  /**
   * Returns the log channel id of the object that was executed (trans, job, job entry, etc)
   *
   * @return the log channel id
   */
  public String getLogChannelId() {
    return logChannelId;
  }

  /**
   * Sets the log channel id of the object that was executed (trans, job, job entry, etc)
   *
   * @param logChannelId
   *          the logChannelId to set
   */
  public void setLogChannelId( String logChannelId ) {
    this.logChannelId = logChannelId;
  }

  /**
   * Increases the number of lines read by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseLinesRead( long incr ) {
    nrLinesRead += incr;
  }

  /**
   * Increases the number of lines written by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseLinesWritten( long incr ) {
    nrLinesWritten += incr;
  }

  /**
   * Increases the number of lines input by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseLinesInput( long incr ) {
    nrLinesInput += incr;
  }

  /**
   * Increases the number of lines output by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseLinesOutput( long incr ) {
    nrLinesOutput += incr;
  }

  /**
   * Increases the number of lines updated by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseLinesUpdated( long incr ) {
    nrLinesUpdated += incr;
  }

  /**
   * Increases the number of lines deleted by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseLinesDeleted( long incr ) {
    nrLinesDeleted += incr;
  }

  /**
   * Increases the number of lines rejected by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseLinesRejected( long incr ) {
    nrLinesRejected += incr;
  }

  /**
   * Increases the number of errors by the specified value
   *
   * @param incr
   *          the amount to increment
   */
  public void increaseErrors( long incr ) {
    nrErrors += incr;
  }

  /**
   * Returns all the text from any logging performed by the transformation or job
   *
   * @return the logging text as a string
   */
  public String getLogText() {
    return logText.toString();
  }

  /**
   * Sets the logging text to the specified String
   *
   * @param logText
   *          the logText to set
   */
  public void setLogText( String logText ) {
    if ( logText == null ) {
      this.logText = new StringBuilder( 10000 );
    } else {
      this.logText = new StringBuilder( logText );
    }
  }

  public void appendLogText( String logTextStr ) {
    logText.append( logTextStr );
  }

  /**
   * Sets flag for safe stopping a transformation
   *
   * @return the safe stop flag
   */
  public boolean isSafeStop() {
    return safeStop;
  }

  /**
   * Returns the flag for safe stopping a transformation
   *
   * @param safeStop the safe stop flag
   */
  public void setSafeStop( boolean safeStop ) {
    this.safeStop = safeStop;
  }

  /**
   * Returns the elapsed time of the ETL execution in milliseconds
   *
   * @returns elapsed time of the ETL execution in milliseconds
   */
  public long getElapsedTimeMillis() {
    return elapsedTimeMillis;
  }

  /**
   * Sets the elapsed time of the ETL execution in milliseconds
   *
   * @param elapsedTimeMillis elapsed time of the ETL execution in milliseconds
   */
  public void setElapsedTimeMillis( long elapsedTimeMillis ) {
    this.elapsedTimeMillis = elapsedTimeMillis;
  }

  /**
   * Returns the unique identifier of an ETL execution, should one ever care to declare one such
   *
   * @return unique identifier of an ETL execution, should one ever care to declare one such
   */
  public String getExecutionId() {
    return executionId;
  }

  /**
   * Sets a unique identifier of an ETL execution, should one ever care to declare one such
   *
   * @param executionId unique identifier of an ETL execution, should one ever care to declare one such
   */
  public void setExecutionId( String executionId ) {
    this.executionId = executionId;
  }
}
