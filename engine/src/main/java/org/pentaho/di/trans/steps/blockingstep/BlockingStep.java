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

package org.pentaho.di.trans.steps.blockingstep;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * A step that blocks throughput until the input ends, then it will either output the last row or the complete input.
 */
public class BlockingStep extends BaseStep implements StepInterface {

  private static Class<?> PKG = BlockingStepMeta.class; // for i18n purposes, needed by Translator2!!

  private BlockingStepMeta meta;
  private BlockingStepData data;
  private Object[] lastRow;

  public BlockingStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private boolean addBuffer( RowMetaInterface rowMeta, Object[] r ) {
    if ( r != null ) {
      data.buffer.add( r ); // Save row
    }

    // Time to write to disk: buffer in core is full!
    if ( data.buffer.size() == meta.getCacheSize() // Buffer is full: dump to disk
      || ( data.files.size() > 0 && r == null && data.buffer.size() > 0 ) // No more records: join from disk
    ) {
      // Then write them to disk...
      DataOutputStream dos;
      GZIPOutputStream gzos;
      int p;

      try {
        FileObject fileObject =
          KettleVFS.createTempFile(
            meta.getPrefix(), ".tmp", environmentSubstitute( meta.getDirectory() ), getTransMeta() );

        data.files.add( fileObject ); // Remember the files!
        OutputStream outputStream = KettleVFS.getOutputStream( fileObject, false );
        if ( meta.getCompress() ) {
          gzos = new GZIPOutputStream( new BufferedOutputStream( outputStream ) );
          dos = new DataOutputStream( gzos );
        } else {
          dos = new DataOutputStream( outputStream );
          gzos = null;
        }

        // How many records do we have?
        dos.writeInt( data.buffer.size() );

        for ( p = 0; p < data.buffer.size(); p++ ) {
          // Just write the data, nothing else
          rowMeta.writeData( dos, data.buffer.get( p ) );
        }
        // Close temp-file
        dos.close(); // close data stream
        if ( gzos != null ) {
          gzos.close(); // close gzip stream
        }
        outputStream.close(); // close file stream
      } catch ( Exception e ) {
        logError( "Error processing tmp-file: " + e.toString() );
        return false;
      }

      data.buffer.clear();
    }

    return true;
  }

  private Object[] getBuffer() {
    Object[] retval;

    // Open all files at once and read one row from each file...
    if ( data.files.size() > 0 && ( data.dis.size() == 0 || data.fis.size() == 0 ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "BlockingStep.Log.Openfiles" ) );
      }

      try {
        FileObject fileObject = data.files.get( 0 );
        String filename = KettleVFS.getFilename( fileObject );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "BlockingStep.Log.Openfilename1" )
            + filename + BaseMessages.getString( PKG, "BlockingStep.Log.Openfilename2" ) );
        }
        InputStream fi = KettleVFS.getInputStream( fileObject );
        DataInputStream di;
        data.fis.add( fi );
        if ( meta.getCompress() ) {
          GZIPInputStream gzfi = new GZIPInputStream( new BufferedInputStream( fi ) );
          di = new DataInputStream( gzfi );
          data.gzis.add( gzfi );
        } else {
          di = new DataInputStream( fi );
        }
        data.dis.add( di );

        // How long is the buffer?
        int buffersize = di.readInt();

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "BlockingStep.Log.BufferSize1" )
            + filename + BaseMessages.getString( PKG, "BlockingStep.Log.BufferSize2" ) + buffersize + " "
            + BaseMessages.getString( PKG, "BlockingStep.Log.BufferSize3" ) );
        }

        if ( buffersize > 0 ) {
          // Read a row from temp-file
          data.rowbuffer.add( data.outputRowMeta.readData( di ) );
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "BlockingStepMeta.ErrorReadingFile" ) + e.toString() );
        logError( Const.getStackTracker( e ) );
      }
    }

    if ( data.files.size() == 0 ) {
      if ( data.buffer.size() > 0 ) {
        retval = data.buffer.get( 0 );
        data.buffer.remove( 0 );
      } else {
        retval = null;
      }
    } else {
      if ( data.rowbuffer.size() == 0 ) {
        retval = null;
      } else {
        retval = data.rowbuffer.get( 0 );

        data.rowbuffer.remove( 0 );

        // now get another
        FileObject file = data.files.get( 0 );
        DataInputStream di = data.dis.get( 0 );
        InputStream fi = data.fis.get( 0 );
        GZIPInputStream gzfi = ( meta.getCompress() ) ? data.gzis.get( 0 ) : null;

        try {
          data.rowbuffer.add( 0, data.outputRowMeta.readData( di ) );
        } catch ( SocketTimeoutException e ) {
          logError( BaseMessages.getString( PKG, "System.Log.UnexpectedError" ) + " : " + e.toString() );
          logError( Const.getStackTracker( e ) );
          setErrors( 1 );
          stopAll();
        } catch ( KettleFileException fe ) {
          // empty file or EOF mostly
          try {
            di.close();
            fi.close();
            if ( gzfi != null ) {
              gzfi.close();
            }
            file.delete();
          } catch ( IOException e ) {
            logError( BaseMessages.getString( PKG, "BlockingStepMeta.UnableDeleteFile" ) + file.toString() );
            setErrors( 1 );
            stopAll();
            return null;
          }

          data.files.remove( 0 );
          data.dis.remove( 0 );
          data.fis.remove( 0 );
          if ( gzfi != null ) {
            data.gzis.remove( 0 );
          }
        }
      }
    }
    return retval;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( ( data.dis != null ) && ( data.dis.size() > 0 ) ) {
      for ( DataInputStream is : data.dis ) {
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
    super.dispose( smi, sdi );
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (BlockingStepMeta) smi;
    data = (BlockingStepData) sdi;

    if ( super.init( smi, sdi ) ) {
      //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
      if ( getTransMeta().getNamedClusterEmbedManager() != null ) {
        getTransMeta().getNamedClusterEmbedManager()
          .passEmbeddedMetastoreKey( getTransMeta(), getTransMeta().getEmbeddedMetastoreProviderKey() );
      }
      // Add init code here.
      return true;
    }
    return false;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    boolean err = true;
    Object[] r = getRow(); // Get row from input rowset & set row busy!

    // initialize
    if ( first && r != null ) {
      first = false;
      data.outputRowMeta = getInputRowMeta().clone();
    }

    if ( !meta.isPassAllRows() ) {
      if ( r == null ) {
        // no more input to be expected...
        if ( lastRow != null ) {
          putRow( data.outputRowMeta, lastRow );
        }
        setOutputDone();
        return false;
      }

      lastRow = r;
      return true;
    } else {
      // The mode in which we pass all rows to the output.
      err = addBuffer( getInputRowMeta(), r );
      if ( !err ) {
        setOutputDone(); // signal receiver we're finished.
        return false;
      }

      if ( r == null ) {
        // no more input to be expected...
        // Now we can start the output!
        r = getBuffer();
        while ( r != null && !isStopped() ) {
          if ( log.isRowLevel() ) {
            logRowlevel( "Read row: " + getInputRowMeta().getString( r ) );
          }

          putRow( data.outputRowMeta, r ); // copy row to possible alternate rowset(s).

          r = getBuffer();
        }

        setOutputDone(); // signal receiver we're finished.
        return false;
      }

      return true;
    }
  }

}
