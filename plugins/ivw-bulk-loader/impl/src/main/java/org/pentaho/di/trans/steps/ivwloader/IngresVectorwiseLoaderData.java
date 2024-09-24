/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.ivwloader;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.StreamLogger;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.ivwloader.IngresVectorwiseLoader.FifoOpener;

/**
 * Stores data for the VectorWise bulk load step.
 *
 * @author Matt
 * @since 14-apr-2009
 */
public class IngresVectorwiseLoaderData extends BaseStepData implements StepDataInterface {
  public int[] keynrs; // nr of keylookup -value in row...

  public StreamLogger errorLogger;

  public StreamLogger outputLogger;

  public byte[] separator;
  public byte[] newline;

  public String schemaTable;

  public String fifoFilename;

  public FileChannel fileChannel;

  public IngresVectorwiseLoader.SqlRunner sqlRunner;

  public byte[] quote;

  public Process sqlProcess;

  public OutputStream sqlOutputStream;

  public FifoOpener fifoOpener;

  public boolean isEncoding;

  public String encoding;

  public ByteBuffer byteBuffer;

  public int bufferSize;

  public byte[] semicolon;

  public byte[] doubleQuote;

  public RowMetaInterface bulkRowMeta;

  /**
   * Default constructor.
   */
  public IngresVectorwiseLoaderData() {
    super();
  }

  public byte[] getBytes( String str ) {
    if ( isEncoding ) {
      try {
        return str.getBytes( encoding );
      } catch ( UnsupportedEncodingException e ) {
        throw new RuntimeException( e );
      }
    } else {
      return str.getBytes();
    }
  }
}
