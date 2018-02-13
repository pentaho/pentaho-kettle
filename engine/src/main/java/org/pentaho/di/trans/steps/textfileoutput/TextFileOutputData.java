/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import java.util.List;
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

  public class FileStreamsKey implements Comparable<FileStreamsKey> {
    String fileName;
    long index = 0;

    public FileStreamsKey( String fileName, long index ) {
      this.fileName = fileName;
      this.index = index;
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

    @Override
    public int compareTo( FileStreamsKey o ) {
      return fileName.compareTo( o.getFileName() );
    }

    @Override
    public boolean equals( Object obj ) {
      return fileName.equals( obj );
    }
  }

  public class FileStreamsValue {
    OutputStream fileOutputStream;
    CompressionOutputStream compressedOutputStream;
    BufferedOutputStream bufferedOutputStream;
    boolean isDirty;
    boolean isNewFile = false;

    public FileStreamsValue( OutputStream fileOutputStream, CompressionOutputStream compressedOutputStream, BufferedOutputStream bufferedOutputStream ) {
      this.fileOutputStream = fileOutputStream;
      this.compressedOutputStream = compressedOutputStream;
      this.bufferedOutputStream = bufferedOutputStream;
      isDirty = false;
      isNewFile = false;
    }

    public boolean isDirty() {
      return isDirty;
    }

    public void setDirty( boolean dirty ) {
      isDirty = dirty;
    }

    public boolean isNewFile() {
      return isNewFile;
    }

    public void setNewFile( boolean newFile ) {
      isNewFile = newFile;
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

  public class FileStreamsMap extends TreeMap<FileStreamsKey, FileStreamsValue> {
    public void put( String filename, FileStreamsValue fileWriterOutputStream ) {
      long index = 0;
      if ( size() > 0 ) {
        index = lastKey().index + 1;
      }
      put( new FileStreamsKey( filename, index ), fileWriterOutputStream );
    }

    public FileStreamsValue get( String filename ) {
      return get( new FileStreamsKey( filename, -1 ) );
    }

    public FileStreamsValue remove( String filename ) {
      return remove( new FileStreamsKey( filename, -1 ) );
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

  public byte[] binarySeparator;
  public byte[] binaryEnclosure;
  public byte[] binaryNewline;

  public boolean hasEncoding;

  public byte[][] binaryNullValue;

  public boolean oneFileOpened;

  public List<String> previouslyOpenedFiles;

  public int fileNameFieldIndex;

  public ValueMetaInterface fileNameMeta;

  public FileStreamsMap fileWriterMap;

  public long lastFileFlushTime = 0;

  public String fileName;

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

    fileWriterMap = new FileStreamsMap();
  }

}
