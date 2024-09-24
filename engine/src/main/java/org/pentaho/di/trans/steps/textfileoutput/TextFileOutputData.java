/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.textfileoutput;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.pentaho.di.core.compress.CompressionOutputStream;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 22-jan-2005
 */
public class TextFileOutputData extends BaseStepData implements StepDataInterface {

  interface IFileStreamsCollection {
    FileStream getStream( String filename );
    void closeOldestOpenFile( boolean removeFileFromCollection ) throws IOException;
    void flushOpenFiles( boolean closeAfterFlush ) throws IOException;
    String getLastFileName(  );
    FileStream getLastStream(  );
    int getNumOpenFiles( );
    void closeFile( String filename ) throws IOException;
    void closeStream( OutputStream outputStream ) throws IOException;
    int size( );
    void add( String filename, FileStream fileStreams );
  }

  public class FileStreamsCollectionEntry {
    private String fileName;
    private long index = 0;
    private FileStream fileStream;

    public FileStreamsCollectionEntry( String fileName, long index, FileStream fileStream ) {
      this.fileName = fileName;
      this.index = index;
      this.fileStream = fileStream;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName( String fileName ) {
      this.fileName = fileName;
    }

    public long getIndex() {
      return index;
    }

    public void setIndex( int index ) {
      this.index = index;
    }

    public FileStream getFileStream() {
      return fileStream;
    }

    public void setFileStream( FileStream fileStream ) {
      this.fileStream = fileStream;
    }
  }

  public class FileStream {
    OutputStream fileOutputStream;
    CompressionOutputStream compressedOutputStream;
    BufferedOutputStream bufferedOutputStream;
    boolean isDirty;

    public FileStream( OutputStream fileOutputStream, CompressionOutputStream compressedOutputStream, BufferedOutputStream bufferedOutputStream ) {
      this.fileOutputStream = fileOutputStream;
      this.compressedOutputStream = compressedOutputStream;
      this.bufferedOutputStream = bufferedOutputStream;
      isDirty = false;
    }

    public boolean isDirty() {
      return isDirty;
    }

    public void setDirty( boolean dirty ) {
      isDirty = dirty;
    }

    public void flush() throws IOException {
      if ( isDirty ) {
        getBufferedOutputStream().flush();
        isDirty = false;
      }
    }

    public void close() throws IOException {
      setBufferedOutputStream( null );
      getCompressedOutputStream().close();
      setCompressedOutputStream( null );
      getFileOutputStream().close();
      setFileOutputStream( null );
      isDirty = false;
    }

    public boolean isOpen() {
      return ( fileOutputStream != null ) || ( compressedOutputStream != null ) || ( bufferedOutputStream != null );
    }

    public BufferedOutputStream getBufferedOutputStream() {
      return bufferedOutputStream;
    }

    public void setBufferedOutputStream( BufferedOutputStream outputStream ) {
      this.bufferedOutputStream = outputStream;
    }

    public OutputStream getFileOutputStream() {
      return fileOutputStream;
    }

    public void setFileOutputStream( OutputStream fileOutputStream ) {
      this.fileOutputStream = fileOutputStream;
    }

    public CompressionOutputStream getCompressedOutputStream() {
      return compressedOutputStream;
    }

    public void setCompressedOutputStream( CompressionOutputStream compressedOutputStream ) {
      this.compressedOutputStream = compressedOutputStream;
    }
  }

  public class FileStreamsList implements IFileStreamsCollection {
    ArrayList<FileStream> streamsList = new ArrayList<>();
    ArrayList<String> namesList = new ArrayList<>();
    int numOpenFiles = 0;

    @Override
    public FileStream getStream( String filename ) {
      int index = namesList.indexOf( filename );
      return index == -1 ? null : streamsList.get( index );
    }

    @Override
    public void closeOldestOpenFile( boolean removeFileFromCollection ) throws IOException {
      FileStream oldestOpenStream = null;
      int i;
      for ( i = 0; i < streamsList.size(); i++ ) {
        FileStream existingStream = streamsList.get( i );
        if ( existingStream.isOpen() ) {
          oldestOpenStream = existingStream;
          break;
        }
      }
      if ( oldestOpenStream != null ) {
        oldestOpenStream.flush();
        oldestOpenStream.close();
        numOpenFiles--;
        if ( removeFileFromCollection ) {
          streamsList.remove( i );
          namesList.remove( i );
        }
      }
    }

    @Override
    public void flushOpenFiles( boolean closeAfterFlush ) throws IOException {
      for ( FileStream outputStream : streamsList ) {
        if ( outputStream.isDirty() ) {
          try {
            outputStream.flush();
            if ( closeAfterFlush && outputStream.isOpen() ) {
              outputStream.close();
              numOpenFiles--;
            }
          } catch ( IOException e ) {
            e.printStackTrace();
          }
        }
      }
    }

    @Override
    public String getLastFileName() {
      return namesList.isEmpty() ? null : namesList.get( namesList.size() - 1 );
    }

    @Override
    public FileStream getLastStream() {
      return streamsList.isEmpty() ? null : streamsList.get( streamsList.size() - 1 );
    }

    @Override
    public int getNumOpenFiles() {
      return numOpenFiles;
    }

    @Override
    public void closeFile( String filename ) throws IOException {
      int index = namesList.indexOf( filename );
      if ( index >= 0 ) {
        FileStream existingStream = streamsList.get( index );
        if ( existingStream.isOpen() ) {
          existingStream.flush();
          existingStream.close();
          numOpenFiles--;
        }
      }
    }

    @Override
    public void closeStream( OutputStream outputStream ) throws IOException {
      for ( int i = 0; i < streamsList.size(); i++ ) {
        FileStream fileStream = streamsList.get( i );
        if ( ( fileStream.getBufferedOutputStream() == outputStream ) || ( fileStream.getCompressedOutputStream() == outputStream ) || ( fileStream.getFileOutputStream() == outputStream ) ) {
          closeFile( namesList.get( i ) );
        }
      }
    }

    @Override
    public int size() {
      return streamsList.size();
    }

    @Override
    public void add( String filename, FileStream fileStreams ) {
      namesList.add( filename );
      streamsList.add( fileStreams );
      if ( fileStreams.isOpen() ) {
        numOpenFiles++;
      }
    }
  }

  public class FileStreamsMap implements IFileStreamsCollection  {
    private int numOpenFiles = 0;
    private TreeMap<String, FileStreamsCollectionEntry> fileNameMap = new TreeMap<>();
    private TreeMap<Long, FileStreamsCollectionEntry> indexMap = new TreeMap<>();

    @Override
    public int size() {
      return fileNameMap.size();
    }

    @Override
    public void add( String fileName, FileStream fileWriterOutputStream ) {
      long index = 0;
      if ( size() > 0 ) {
        index = indexMap.lastKey() + 1;
      }
      FileStreamsCollectionEntry newEntry = new FileStreamsCollectionEntry( fileName, index, fileWriterOutputStream );
      fileNameMap.put( fileName, newEntry );
      indexMap.put( index, newEntry );
      if ( fileWriterOutputStream.isOpen() ) {
        numOpenFiles++;
      }

    }

    @Override
    public FileStream getStream( String filename ) {
      if ( fileNameMap.containsKey( filename ) ) {
        return fileNameMap.get( filename ).getFileStream();
      } else {
        return null;
      }
    }

    @Override
    public String getLastFileName() {
      String filename = null;
      if ( indexMap.size() > 0 ) {
        filename = indexMap.lastEntry().getValue().getFileName();
      }
      return filename;
    }

    @Override
    public FileStream getLastStream() {
      FileStream lastStream = null;
      if ( indexMap.size() > 0 ) {
        lastStream = indexMap.lastEntry().getValue().getFileStream();
      }
      return lastStream;
    }

    @Override
    public int getNumOpenFiles() {
      return numOpenFiles;
    }

    @Override
    public void closeOldestOpenFile( boolean removeFileFromCollection ) throws IOException {
      FileStream oldestOpenStream = null;
      String oldestOpenFileName = null;
      Long oldestOpenFileIndex = null;
      for ( Map.Entry<Long, FileStreamsCollectionEntry> mapEntry : indexMap.entrySet() ) {
        FileStreamsCollectionEntry existingStream = mapEntry.getValue();
        if ( existingStream.getFileStream().isOpen() ) {
          oldestOpenStream = existingStream.getFileStream();
          oldestOpenFileName = existingStream.getFileName();
          oldestOpenFileIndex = existingStream.getIndex();
          break;
        }
      }
      if ( oldestOpenStream != null ) {
        oldestOpenStream.flush();
        oldestOpenStream.close();
        numOpenFiles--;
        if ( removeFileFromCollection ) {
          fileNameMap.remove( oldestOpenFileName );
          indexMap.remove( oldestOpenFileIndex );
        }
      }
    }

    @Override
    public void flushOpenFiles( boolean closeAfterFlush ) {
      for ( FileStreamsCollectionEntry collectionEntry : indexMap.values() ) {
        if ( collectionEntry.getFileStream().isDirty() ) {
          try {
            collectionEntry.getFileStream().flush();
            if ( closeAfterFlush ) {
              collectionEntry.getFileStream().close();
            }
          } catch ( IOException e ) {
            e.printStackTrace();
          }
        }
      }
    }

    @Override
    public void closeFile( String filename ) throws IOException {
      FileStream outputStreams = getStream( filename );
      if ( ( outputStreams != null ) && ( outputStreams.isOpen() ) ) {
        outputStreams.flush();
        outputStreams.close();
        numOpenFiles--;
      }
    }

    @Override
    public void closeStream( OutputStream outputStream ) throws IOException {
      for ( Map.Entry<Long, FileStreamsCollectionEntry> mapEntry : indexMap.entrySet() ) {
        FileStream fileStream = mapEntry.getValue().getFileStream();
        if ( ( fileStream.getBufferedOutputStream() == outputStream ) || ( fileStream.getCompressedOutputStream() == outputStream ) || ( fileStream.getFileOutputStream() == outputStream ) ) {
          closeFile( mapEntry.getValue().getFileName() );
        }
      }
    }
  }

  public int splitnr;

  public int[] fieldnrs;

  public NumberFormat nf;
  public DecimalFormat df;
  public DecimalFormatSymbols dfs;

  public SimpleDateFormat daf;
  public DateFormatSymbols dafs;

  public CompressionOutputStream out;

  public OutputStream writer;

  public DecimalFormat defaultDecimalFormat;
  public DecimalFormatSymbols defaultDecimalFormatSymbols;

  public SimpleDateFormat defaultDateFormat;
  public DateFormatSymbols defaultDateFormatSymbols;

  public Process cmdProc;

  public OutputStream fos;

  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;

  public byte[] binarySeparator;
  public byte[] binaryEnclosure;
  public byte[] binaryNewline;

  public boolean hasEncoding;

  public byte[][] binaryNullValue;

  public boolean oneFileOpened;

  public int fileNameFieldIndex;

  public ValueMetaInterface fileNameMeta;

  public IFileStreamsCollection fileStreamsCollection;

  public long lastFileFlushTime = 0;

  public String fileName;

  public int splitEvery;

  public TextFileOutputData() {
    super();

    nf = NumberFormat.getInstance();
    df = (DecimalFormat) nf;
    dfs = new DecimalFormatSymbols();

    daf = new SimpleDateFormat();
    dafs = new DateFormatSymbols();

    defaultDecimalFormat = (DecimalFormat) NumberFormat.getInstance();
    defaultDecimalFormatSymbols = new DecimalFormatSymbols();

    defaultDateFormat = new SimpleDateFormat();
    defaultDateFormatSymbols = new DateFormatSymbols();

    fileNameFieldIndex = -1;

    cmdProc = null;
    oneFileOpened = false;
  }

  public IFileStreamsCollection getFileStreamsCollection() {
    if ( fileStreamsCollection == null ) {
      if ( splitnr > 0 ) {
        fileStreamsCollection = new FileStreamsList();
      } else {
        fileStreamsCollection = new FileStreamsMap();
      }
    }
    return fileStreamsCollection;
  }
}
