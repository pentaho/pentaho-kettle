/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.sort;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Sort the rows in the input-streams based on certain criteria
 *
 * @author Matt
 * @since 29-apr-2003
 */
public class SortRows extends BaseStep implements StepInterface {
  private static Class<?> PKG = SortRows.class; // for i18n

  private SortRowsMeta meta;
  private SortRowsData data;

  public SortRows( StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (SortRowsMeta) getStepMeta().getStepMetaInterface();
    data = (SortRowsData) stepDataInterface;
  }

  void addBuffer( RowMetaInterface rowMeta, Object[] r ) throws KettleException {
    // we need convert some keys?
    if ( data.convertKeysToNative != null ) {
      for ( int i = 0; i < data.convertKeysToNative.length; i++ ) {
        int index = data.convertKeysToNative[i];
        r[index] = rowMeta.getValueMeta( index ).convertBinaryStringToNativeType( (byte[]) r[index] );
      }
    }

    // Save row
    data.buffer.add( r );

    // Check the free memory every 1000 rows...
    //
    data.freeCounter++;
    if ( data.sortSize <= 0 && data.freeCounter >= 1000 ) {
      data.freeMemoryPct = Const.getPercentageFreeMemory();
      data.freeCounter = 0;

      if ( log.isDetailed() ) {
        data.memoryReporting++;
        if ( data.memoryReporting >= 10 ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "SortRows.Detailed.AvailableMemory", data.freeMemoryPct ) );
          }
          data.memoryReporting = 0;
        }
      }
    }

    // Buffer is full: sort & dump to disk
    boolean doSort = data.buffer.size() == data.sortSize;
    doSort |=
        data.freeMemoryPctLimit > 0 && data.freeMemoryPct < data.freeMemoryPctLimit
            && data.buffer.size() >= data.minSortSize;
    if ( log.isDebug() ) {
      this.logDebug( BaseMessages.getString( PKG, "SortRows.Debug.StartDumpToDisk", data.freeMemoryPct, data.buffer
          .size() ) );
    }
    // time to sort the buffer and write the data to disk...
    if ( doSort ) {
      sortExternalRows();
    }
  }

  // dump sorted rows from in-memory buffer to fs file
  // clean current buffer
  void sortExternalRows() throws KettleException {
    // we just recently dump buffer - but there is no new rows came.
    if ( data.buffer.isEmpty() ) {
      return;
    }

    // First sort the rows in buffer[]
    quickSort( data.buffer );

    // Then write them to disk...
    DataOutputStream dos;
    GZIPOutputStream gzos;
    int p;

    try {
      FileObject fileObject =
          KettleVFS.createTempFile( meta.getPrefix(), ".tmp", environmentSubstitute( meta.getDirectory() ),
              getTransMeta() );

      data.files.add( fileObject ); // Remember the files!
      OutputStream outputStream = KettleVFS.getOutputStream( fileObject, false );
      if ( data.compressFiles ) {
        gzos = new GZIPOutputStream( new BufferedOutputStream( outputStream ) );
        dos = new DataOutputStream( gzos );
      } else {
        dos = new DataOutputStream( new BufferedOutputStream( outputStream, 500000 ) );
        gzos = null;
      }

      // Just write the data, nothing else
      List<Integer> duplicates = new ArrayList<Integer>();
      Object[] previousRow = null;
      if ( meta.isOnlyPassingUniqueRows() ) {
        int index = 0;
        while ( index < data.buffer.size() ) {
          Object[] row = data.buffer.get( index );
          if ( previousRow != null ) {
            int result = data.outputRowMeta.compare( row, previousRow, data.fieldnrs );
            if ( result == 0 ) {
              duplicates.add( index );
              if ( log.isRowLevel() ) {
                logRowlevel( BaseMessages.getString( PKG, "SortRows.RowLevel.DuplicateRowRemoved", data.outputRowMeta
                    .getString( row ) ) );
              }
            }
          }
          index++;
          previousRow = row;
        }
      }

      // How many records do we have left?
      data.bufferSizes.add( data.buffer.size() - duplicates.size() );

      int duplicatesIndex = 0;
      for ( p = 0; p < data.buffer.size(); p++ ) {
        boolean skip = false;
        if ( duplicatesIndex < duplicates.size() ) {
          if ( p == duplicates.get( duplicatesIndex ) ) {
            skip = true;
            duplicatesIndex++;
          }
        }
        if ( !skip ) {
          data.outputRowMeta.writeData( dos, data.buffer.get( p ) );
        }
      }

      if ( data.sortSize < 0 ) {
        if ( data.buffer.size() > data.minSortSize ) {
          data.minSortSize = data.buffer.size(); // if we did it once, we can do
                                                 // it again.

          // Memory usage goes up over time, even with garbage collection
          // We need pointers, file handles, etc.
          // As such, we're going to lower the min sort size a bit
          //
          data.minSortSize = (int) Math.round( data.minSortSize * 0.90 );
        }
      }

      // Clear the list
      data.buffer.clear();

      // Close temp-file
      dos.close(); // close data stream
      if ( gzos != null ) {
        gzos.close(); // close gzip stream
      }
      outputStream.close(); // close file stream

      // How much memory do we have left?
      //
      data.freeMemoryPct = Const.getPercentageFreeMemory();
      data.freeCounter = 0;
      if ( data.sortSize <= 0 ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SortRows.Detailed.AvailableMemory", data.freeMemoryPct ) );
        }
      }

    } catch ( Exception e ) {
      throw new KettleException( "Error processing temp-file!", e );
    }

    data.getBufferIndex = 0;
  }

  private DataInputStream getDataInputStream( GZIPInputStream gzipInputStream ) {
    DataInputStream result = new DataInputStream( gzipInputStream );
    data.gzis.add( gzipInputStream );
    return result;
  }

  // get sorted rows from available files in iterative manner.
  // that means call to this method will continue to return rows
  // till all temp files will not be read to the end.
  Object[] getBuffer() throws KettleValueException {
    Object[] retval;

    // Open all files at once and read one row from each file...
    if ( data.files.size() > 0 && ( data.dis.size() == 0 || data.fis.size() == 0 ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "SortRows.Basic.OpeningTempFiles", data.files.size() ) );
      }

      try {
        for ( int f = 0; f < data.files.size() && !isStopped(); f++ ) {
          FileObject fileObject = data.files.get( f );
          String filename = KettleVFS.getFilename( fileObject );
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "SortRows.Detailed.OpeningTempFile", filename ) );
          }
          InputStream fi = KettleVFS.getInputStream( fileObject );
          DataInputStream di;
          data.fis.add( fi );
          if ( data.compressFiles ) {
            di = getDataInputStream( new GZIPInputStream( new BufferedInputStream( fi ) ) );
          } else {
            di = new DataInputStream( new BufferedInputStream( fi, 50000 ) );
          }
          data.dis.add( di );

          // How long is the buffer?
          int buffersize = data.bufferSizes.get( f );

          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "SortRows.Detailed.FromFileExpectingRows",
                filename, buffersize ) );
          }

          if ( buffersize > 0 ) {
            Object[] row = data.outputRowMeta.readData( di );
            data.rowbuffer.add( row ); // new row from input stream
            data.tempRows.add( new RowTempFile( row, f ) );
          }
        }

        // Sort the data row buffer
        Collections.sort( data.tempRows, data.comparator );
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "SortRows.Error.ErrorReadingBackTempFiles" ), e );
      }
    }

    if ( data.files.size() == 0 ) {
      // read from in-memory processing

      if ( data.getBufferIndex < data.buffer.size() ) {
        retval = data.buffer.get( data.getBufferIndex );
        data.getBufferIndex++;
      } else {
        retval = null;
      }
    } else {
      // read from disk processing

      if ( data.rowbuffer.size() == 0 ) {
        retval = null;
      } else {
        // We now have "filenr" rows waiting: which one is the smallest?
        //
        if ( log.isRowLevel() ) {
          for ( int i = 0; i < data.rowbuffer.size() && !isStopped(); i++ ) {
            Object[] b = data.rowbuffer.get( i );
            logRowlevel( BaseMessages
                .getString( PKG, "SortRows.RowLevel.PrintRow", i, data.outputRowMeta.getString( b ) ) );
          }
        }

        RowTempFile rowTempFile = data.tempRows.remove( 0 );
        retval = rowTempFile.row;
        int smallest = rowTempFile.fileNumber;

        // now get another Row for position smallest

        FileObject file = data.files.get( smallest );
        DataInputStream di = data.dis.get( smallest );
        InputStream fi = data.fis.get( smallest );

        try {
          Object[] row2 = data.outputRowMeta.readData( di );
          RowTempFile extra = new RowTempFile( row2, smallest );

          int index = Collections.binarySearch( data.tempRows, extra, data.comparator );
          if ( index < 0 ) {
            data.tempRows.add( index * ( -1 ) - 1, extra );
          } else {
            data.tempRows.add( index, extra );
          }
        } catch ( KettleFileException fe ) { // empty file or EOF mostly
          GZIPInputStream gzfi = ( data.compressFiles ) ? data.gzis.get( smallest ) : null;
          try {
            di.close();
            fi.close();
            if ( gzfi != null ) {
              gzfi.close();
            }
            file.delete();
          } catch ( IOException e ) {
            logError( BaseMessages.getString( PKG, "SortRows.Error.UnableToCloseFile", smallest, file.toString() ) );
            setErrors( 1 );
            stopAll();
            return null;
          }

          data.files.remove( smallest );
          data.dis.remove( smallest );
          data.fis.remove( smallest );

          if ( gzfi != null ) {
            data.gzis.remove( smallest );
          }

          // Also update all file numbers in in data.tempRows if they are larger
          // than smallest.
          //
          for ( RowTempFile rtf : data.tempRows ) {
            if ( rtf.fileNumber > smallest ) {
              rtf.fileNumber--;
            }
          }
        } catch ( SocketTimeoutException e ) {
          throw new KettleValueException( e ); // should never happen on local files
        }
      }
    }
    return retval;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    // wait for first for is available
    Object[] r = getRow();

    List<String> groupFields = null;

    if ( first ) {
      this.first = false;

      // do we have any row at start processing?
      if ( r == null ) {
        // seems that we don't
        this.setOutputDone();
        return false;
      }

      RowMetaInterface inputRowMeta = getInputRowMeta();

      // do we have group numbers?
      if ( meta.isGroupSortEnabled() ) {
        data.newBatch = true;

        // we do set exact list instead of null
        groupFields = meta.getGroupFields();
        data.groupnrs = new int[groupFields.size()];

        for ( int i = 0; i < groupFields.size(); i++ ) {
          data.groupnrs[i] = inputRowMeta.indexOfValue( groupFields.get( i ) );
          if ( data.groupnrs[i] < 0 ) {
            logError( BaseMessages.getString( PKG, "SortRows.Error.PresortedFieldNotFound", groupFields.get( i ) ) );
            setErrors( 1 );
            stopAll();
            return false;
          }
        }
      }

      String[] fieldNames = meta.getFieldName();
      data.fieldnrs = new int[fieldNames.length];
      List<Integer> toConvert = new ArrayList<Integer>();

      // Metadata
      data.outputRowMeta = inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      data.comparator = new RowTemapFileComparator( data.outputRowMeta, data.fieldnrs );

      for ( int i = 0; i < fieldNames.length; i++ ) {
        data.fieldnrs[i] = inputRowMeta.indexOfValue( fieldNames[i] );
        if ( data.fieldnrs[i] < 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "SortRowsMeta.CheckResult.StepFieldNotInInputStream",
              meta.getFieldName()[i], getStepname() ) );
        }
        // do we need binary conversion for this type?
        if ( inputRowMeta.getValueMeta( data.fieldnrs[i] ).isStorageBinaryString() ) {
          toConvert.add( data.fieldnrs[i] );
        }
      }
      data.convertKeysToNative = toConvert.isEmpty() ? null : new int[toConvert.size()];
      int i = 0;
      for ( Integer in : toConvert ) {
        data.convertKeysToNative[i] = in;
        i++;
      }
      data.rowComparator = new RowObjectArrayComparator( data.outputRowMeta, data.fieldnrs );
    } // end if first

    // it is not first row and it is null
    if ( r == null ) {
      // flush result and set output done.
      this.preSortBeforeFlush();
      this.passBuffer();
      this.setOutputDone();
      return false;
    }

    // if Group Sort is not enabled then do the normal sort.
    if ( !meta.isGroupSortEnabled() ) {
      this.addBuffer( getInputRowMeta(), r );
    } else {
      // Otherwise do grouping sort
      if ( data.newBatch ) {
        data.newBatch = false;
        setPrevious( r );
        // this enables Sort stuff to initialize it's state.
        this.addBuffer( getInputRowMeta(), r );
      } else {
        if ( this.sameGroup( data.previous, r ) ) {
          // setPrevious( r ); // we are not need to set it every time

          // this performs SortRows normal row collection functionality.
          this.addBuffer( getInputRowMeta(), r );
        } else {
          this.preSortBeforeFlush();

          // flush sorted block to next step:
          this.passBuffer();

          // new sorted block beginning
          setPrevious( r );
          data.newBatch = true;

          this.addBuffer( getInputRowMeta(), r );
        }
      }
    }

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( "Linenr " + getLinesRead() );
      }
    }

    return true;
  }

  /**
   * This method passes all rows in the buffer to the next steps. Usually call to this method indicates that this
   * particular step finishing processing.
   *
   */
  void passBuffer() throws KettleException {
    // Now we can start the output!
    //
    Object[] r = getBuffer();
    Object[] previousRow = null;

    // log time spent for external merge (expected time consuming operation)
    if ( log.isDebug() && !data.files.isEmpty() ) {
      this.logDebug( BaseMessages.getString( PKG, "SortRows.Debug.ExternalMergeStarted" ) );
    }

    while ( r != null && !isStopped() ) {
      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "SortRows.RowLevel.ReadRow", getInputRowMeta().getString( r ) ) );
      }

      // Do another verification pass for unique rows...
      //
      if ( meta.isOnlyPassingUniqueRows() ) {
        if ( previousRow != null ) {
          // See if this row is the same as the previous one as far as the keys
          // are concerned.
          // If so, we don't put forward this row.
          int result = data.outputRowMeta.compare( r, previousRow, data.fieldnrs );
          if ( result != 0 ) {
            putRow( data.outputRowMeta, r ); // copy row to possible alternate
                                             // rowset(s).
          }
        } else {
          putRow( data.outputRowMeta, r ); // copy row to next steps
        }
        previousRow = r;
      } else {
        putRow( data.outputRowMeta, r ); // copy row to possible alternate
                                         // rowset(s).
      }

      r = getBuffer();
    }

    if ( log.isDebug() && !data.files.isEmpty() ) {
      this.logDebug( BaseMessages.getString( PKG, "SortRows.Debug.ExternalMergeFinished" ) );
    }

    // Clear out the buffer for the next batch
    //
    clearBuffers();
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SortRowsMeta) smi;
    data = (SortRowsData) sdi;

    if ( !super.init( smi, sdi ) ) {
      return false;
    }

    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( getTransMeta().getNamedClusterEmbedManager() != null ) {
      getTransMeta().getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( getTransMeta(), getTransMeta().getEmbeddedMetastoreProviderKey() );
    }

    data.sortSize = Const.toInt( environmentSubstitute( meta.getSortSize() ), -1 );
    data.freeMemoryPctLimit = Const.toInt( meta.getFreeMemoryLimit(), -1 );
    if ( data.sortSize <= 0 && data.freeMemoryPctLimit <= 0 ) {
      // Prefer the memory limit as it should never fail
      //
      data.freeMemoryPctLimit = 25;
    }

    // In memory buffer
    //
    data.buffer = new ArrayList<Object[]>( 5000 );

    // Buffer for reading from disk
    //
    data.rowbuffer = new ArrayList<Object[]>( 5000 );

    data.compressFiles = getBooleanValueOfVariable( meta.getCompressFilesVariable(), meta.getCompressFiles() );

    data.tempRows = new ArrayList<RowTempFile>();

    data.minSortSize = 5000;

    return true;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    clearBuffers();
    super.dispose( smi, sdi );
  }

  private void clearBuffers() {

    // Clean out the sort buffer
    data.buffer.clear();
    data.getBufferIndex = 0;
    data.rowbuffer.clear();

    // close any open DataInputStream objects
    if ( ( data.dis != null ) && ( data.dis.size() > 0 ) ) {
      for ( DataInputStream dis : data.dis ) {
        BaseStep.closeQuietly( dis );
      }
    }
    // close any open InputStream objects
    if ( ( data.fis != null ) && ( data.fis.size() > 0 ) ) {
      for ( InputStream is : data.fis ) {
        BaseStep.closeQuietly( is );
      }
    }
    // remove temp files
    for ( int f = 0; f < data.files.size(); f++ ) {
      FileObject fileToDelete = data.files.get( f );
      try {
        if ( fileToDelete != null && fileToDelete.exists() ) {
          fileToDelete.delete();
        }
      } catch ( FileSystemException e ) {
        logError( e.getLocalizedMessage(), e );
      }
    }
  }

  /**
   * Sort the entire vector, if it is not empty.
   */
  void quickSort( List<Object[]> elements ) throws KettleException {
    if ( elements.size() > 0 ) {
      Collections.sort( elements, data.rowComparator );

      long nrConversions = 0L;
      for ( ValueMetaInterface valueMeta : data.outputRowMeta.getValueMetaList() ) {
        nrConversions += valueMeta.getNumberOfBinaryStringConversions();
        valueMeta.setNumberOfBinaryStringConversions( 0L );
      }
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "SortRows.Detailed.ReportNumberOfBinaryStringConv", nrConversions ) );
      }
    }
  }

  /**
   * Calling this method will alert the step that we finished passing records to the step. Specifically for steps like
   * "Sort Rows" it means that the buffered rows can be sorted and passed on.
   */
  @Override
  public void batchComplete() throws KettleException {
    preSortBeforeFlush();
    passBuffer();
    setOutputDone();
  }

  private void preSortBeforeFlush() throws KettleException {
    if ( data.files.size() > 0 ) {
      // dump to dist and then read from disk
      sortExternalRows();
    } else {
      // sort in memory
      quickSort( data.buffer );
    }
  }

  /*
   * Group Fields Implementation heroic
   */
  // Is the row r of the same group as previous?
  private boolean sameGroup( Object[] previous, Object[] r ) throws KettleValueException {
    if ( r == null ) {
      return false;
    }
    return getInputRowMeta().compare( previous, r, data.groupnrs ) == 0;
  }

  private void setPrevious( Object[] r ) throws KettleException {
    if ( r != null ) {
      this.data.previous = getInputRowMeta().cloneRow( r );
    }
  }

  private class SortRowsComparator {
    protected RowMetaInterface rowMeta;
    protected int[] fieldNrs;

    SortRowsComparator( RowMetaInterface rowMeta, int[] fieldNrs ) {
      this.rowMeta = rowMeta;
      this.fieldNrs = fieldNrs;
    }
  }

  private class RowTemapFileComparator extends SortRowsComparator implements Comparator<RowTempFile> {
    RowTemapFileComparator( RowMetaInterface rowMeta, int[] fieldNrs ) {
      super( rowMeta, fieldNrs );
    }

    @Override
    public int compare( RowTempFile o1, RowTempFile o2 ) {
      try {
        return rowMeta.compare( o1.row, o2.row, fieldNrs );
      } catch ( KettleValueException e ) {
        logError( "Error comparing rows: " + e.toString() );
        return 0;
      }
    }
  }

  private class RowObjectArrayComparator extends SortRowsComparator implements Comparator<Object[]> {
    RowObjectArrayComparator( RowMetaInterface rowMeta, int[] fieldNrs ) {
      super( rowMeta, fieldNrs );
    }

    @Override
    public int compare( Object[] o1, Object[] o2 ) {
      try {
        return rowMeta.compare( o1, o2, fieldNrs );
      } catch ( KettleValueException e ) {
        logError( "Error comparing rows: " + e.toString() );
        return 0;
      }
    }
  }
}
