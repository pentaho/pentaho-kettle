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

package org.pentaho.di.trans.steps.parallelgzipcsv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
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
 * Read a simple CSV file Just output Strings found in the file...
 *
 * @author Matt
 * @since 2007-07-05
 */
public class ParGzipCsvInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = ParGzipCsvInputMeta.class; // for i18n purposes, needed by Translator2!!

  private ParGzipCsvInputMeta meta;
  private ParGzipCsvInputData data;

  public ParGzipCsvInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ParGzipCsvInputMeta) smi;
    data = (ParGzipCsvInputData) sdi;

    if ( first ) {
      first = false;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( data.filenames == null ) {
        // We're expecting the list of filenames from the previous step(s)...
        //
        getFilenamesFromPreviousSteps();
      }

      // We only run in parallel if we have at least one file to process
      // AND if we have more than one step copy running...
      //
      data.parallel = meta.isRunningInParallel() && data.totalNumberOfSteps > 1;

      // The conversion logic for when the lazy conversion is turned of is simple:
      // Pretend it's a lazy conversion object anyway and get the native type during conversion.
      //
      data.convertRowMeta = data.outputRowMeta.clone();
      for ( ValueMetaInterface valueMeta : data.convertRowMeta.getValueMetaList() ) {
        valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
      }

      // Calculate the indexes for the filename and row number fields
      //
      data.filenameFieldIndex = -1;
      if ( !Utils.isEmpty( meta.getFilenameField() ) && meta.isIncludingFilename() ) {
        data.filenameFieldIndex = meta.getInputFields().length;
      }

      data.rownumFieldIndex = -1;
      if ( !Utils.isEmpty( meta.getRowNumField() ) ) {
        data.rownumFieldIndex = meta.getInputFields().length;
        if ( data.filenameFieldIndex >= 0 ) {
          data.rownumFieldIndex++;
        }
      }

      // Open the next file...
      //
      boolean opened = false;
      while ( data.filenr < data.filenames.length ) {
        if ( openNextFile() ) {
          opened = true;
          break;
        }
      }

      if ( !opened ) {
        setOutputDone(); // last file, end here
        return false;
      }
    }

    Object[] outputRowData = readOneRow( true ); // get row, set busy!
    if ( outputRowData == null ) { // no more input to be expected...

      if ( skipToNextBlock() ) {
        // If we need to open a new file, make sure we don't stop when we get a false from the openNextFile() algorithm.
        // It can also mean that the file is smaller than the block size
        // In that case, check the file number and retry until we get a valid file position to work with.
        //
        boolean opened = false;
        while ( data.filenr < data.filenames.length ) {
          if ( openNextFile() ) {
            opened = true;
            break;
          }
        }

        if ( opened ) {
          return true; // try again on the next loop in the next file...
        } else {
          incrementLinesUpdated();
          setOutputDone(); // last file, end here
          return false;
        }
      } else {
        return true; // try again on the next loop in the next block...
      }
    } else {
      putRow( data.outputRowMeta, outputRowData ); // copy row to possible alternate rowset(s).
      if ( checkFeedback( getLinesInput() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages
            .getString( PKG, "ParGzipCsvInput.Log.LineNumber", Long.toString( getLinesInput() ) ) );
        }
      }
    }

    return true;
  }

  private boolean skipToNextBlock() throws KettleException {

    if ( data.eofReached ) {
      return true; // next file please!
    }
    // Reset the bytes read in the current block of data
    //
    data.totalBytesRead = 0L;
    data.blockNr++;

    if ( data.parallel ) {

      // So our first act is to skip to the correct position in the compressed stream...
      // The number of bytes to skip is nrOfSteps*BufferSize
      //
      long positionToReach =
        ( data.blockNr * data.blockSize * data.totalNumberOfSteps ) + data.stepNumber * data.blockSize;

      // How many bytes do we need to skip to get where we need to be?
      //
      long bytesToSkip = positionToReach - data.fileReadPosition;

      logBasic( "Skipping "
        + bytesToSkip + " bytes to go to position " + positionToReach + " for step copy " + data.stepNumber );

      // Get into position...
      //
      try {
        long bytesSkipped = 0;
        while ( bytesSkipped < bytesToSkip ) {
          long n = data.gzis.skip( bytesToSkip - bytesSkipped );
          if ( n <= 0 ) {
            // EOF reached...
            //
            data.eofReached = true;
            data.fileReadPosition += bytesSkipped;
            return true; // nothing more to be found in the file, stop right here.
          }
          bytesSkipped += n;
        }

        data.fileReadPosition += bytesSkipped;

        // Now we need to clear the buffer, reset everything...
        //
        clearBuffer();

        // Now get read until the next CR:
        //
        readOneRow( false );

        return false;

      } catch ( IOException e ) {
        throw new KettleException( "Error skipping " + bytesToSkip + " bytes to the next block of data", e );
      }
    } else {
      // this situation should never happen.
      //
      return true; // stop processing the file
    }
  }

  private void getFilenamesFromPreviousSteps() throws KettleException {
    List<String> filenames = new ArrayList<String>();
    boolean firstRow = true;
    int index = -1;
    Object[] row = getRow();
    while ( row != null ) {

      if ( firstRow ) {
        firstRow = false;

        // Get the filename field index...
        //
        String filenameField = environmentSubstitute( meta.getFilenameField() );
        index = getInputRowMeta().indexOfValue( filenameField );
        if ( index < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "ParGzipCsvInput.Exception.FilenameFieldNotFound", filenameField ) );
        }
      }

      String filename = getInputRowMeta().getString( row, index );
      filenames.add( filename ); // add it to the list...

      row = getRow(); // Grab another row...
    }

    data.filenames = filenames.toArray( new String[filenames.size()] );

    logBasic( BaseMessages.getString( PKG, "ParGzipCsvInput.Log.ReadingFromNrFiles", Integer
      .toString( data.filenames.length ) ) );
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    try {
      closeFile(); // close the final file
    } catch ( Exception ignored ) {
      // Exceptions on stream / file closing should be ignored.
    }
    super.dispose( smi, sdi );
  }

  private boolean openNextFile() throws KettleException {
    try {

      // Close the previous file...
      //
      closeFile();

      if ( data.filenr >= data.filenames.length ) {
        return false;
      }

      // Open the next one...
      //
      logBasic( "Opening file #" + data.filenr + " : " + data.filenames[data.filenr] );
      FileObject fileObject = KettleVFS.getFileObject( data.filenames[data.filenr], getTransMeta() );
      data.fis = KettleVFS.getInputStream( fileObject );

      if ( meta.isLazyConversionActive() ) {
        data.binaryFilename = data.filenames[data.filenr].getBytes();
      }

      data.gzis = new GZIPInputStream( data.fis, data.bufferSize );

      clearBuffer();
      data.fileReadPosition = 0L;
      data.blockNr = 0;
      data.eofReached = false;

      // Skip to the next file...
      //
      data.filenr++;

      // If we are running in parallel and we need to skip bytes in the first file, let's do so here.
      //
      if ( data.parallel ) {
        // Calculate the first block of data to read from the file
        // If the buffer size is 500, we read 0-499 for the first file,
        // 500-999 for the second, 1000-1499 for the third, etc.
        //
        // After that we need to get 1500-1999 for the first step again,
        // 2000-2499 for the second, 2500-2999 for the third, etc.
        //
        // This is equivalent :
        //
        // FROM : stepNumber * bufferSize + blockNr*bufferSize*nrOfSteps
        // TO : FROM + bufferSize - 1
        //
        // Example : step 0, block 0, size 500:
        // From: 0*500+0*500*3=0 To: 0+500-1=499
        //
        // Example : step 0, block 1, size 500:
        // From: 0*500+1*500*3=1500 To: 1500+500-1=1999
        //
        // So our first act is to skip to the correct position in the compressed stream...
        //
        data.blockSize = 2 * data.bufferSize; // for now.
        long bytesToSkip = data.stepNumber * data.blockSize;
        if ( bytesToSkip > 0 ) {
          // Get into position for block 0
          //
          logBasic( "Skipping "
            + bytesToSkip + " bytes to go to position " + bytesToSkip + " for step copy " + data.stepNumber );

          long bytesSkipped = 0L;
          while ( bytesSkipped < bytesToSkip ) {
            long n = data.gzis.skip( bytesToSkip - bytesSkipped );
            if ( n <= 0 ) {
              // EOF in this file, can't read a block in this step copy
              data.eofReached = true;
              return false;
            }
            bytesSkipped += n;
          }

          // Keep track of the file pointer!
          //
          data.fileReadPosition += bytesSkipped;

          // Reset the bytes read in the current block of data
          //
          data.totalBytesRead = 0L;

          // Skip the first row until the next CR
          //
          readOneRow( false );
        } else {
          // Reset the bytes read in the current block of data
          //
          data.totalBytesRead = 0L;

          // See if we need to skip a header row...
          //
          if ( meta.isHeaderPresent() ) {
            readOneRow( false );
          }
        }
      } else {
        // Just one block: read it all until we hit an EOF.
        //
        data.blockSize = Long.MAX_VALUE; // 9,223,372,036 GB

        // Also see here if we need to skip a header row...
        //
        if ( meta.isHeaderPresent() ) {
          readOneRow( false );
        }
      }

      // Add filename to result filenames ?
      if ( meta.isAddResultFile() ) {
        ResultFile resultFile =
          new ResultFile( ResultFile.FILE_TYPE_GENERAL, fileObject, getTransMeta().getName(), toString() );
        resultFile.setComment( "File was read by a Csv input step" );
        addResultFile( resultFile );
      }

      // Reset the row number pointer...
      //
      data.rowNumber = 1L;

      return true;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  private void clearBuffer() {
    data.startBuffer = 0;
    data.endBuffer = 0;
    data.maxBuffer = 0;
  }

  /**
   * Check to see if the buffer size is large enough given the data.endBuffer pointer.<br>
   * Resize the buffer if there is not enough room.
   *
   * @return false if everything is OK, true if there is a problem and we should stop.
   * @throws IOException
   *           in case there is a I/O problem (read error)
   */
  private boolean checkBufferSize() throws KettleException {
    if ( data.endBuffer >= data.maxBuffer ) {
      // Oops, we need to read more data...
      // Better resize this before we read other things in it...
      //
      if ( data.eofReached || data.getMoreData() ) {
        // If we didn't manage to read anything, we return true to indicate we're done
        //
        return true;
      }
    }
    return false;
  }

  /**
   * Read a single row of data from the file...
   *
   * @param doConversions
   *          if you want to do conversions, set to false for the header row.
   * @return a row of data...
   * @throws KettleException
   */
  private Object[] readOneRow( boolean doConversions ) throws KettleException {

    // First see if we haven't gone past our block boundary!
    // Not >= because a block can start smack at the beginning of a line.
    // Since we always skip the first row after skipping a block that would mean we drop rows here and there.
    // So keep this > (larger than)
    //
    if ( data.totalBytesRead > data.blockSize ) {
      // skip to the next block or file by returning null
      //
      return null;
    }

    try {
      Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
      int outputIndex = 0;
      boolean newLineFound = false;
      int newLines = 0;

      // The strategy is as follows...
      // We read a block of byte[] from the file.
      // We scan for the separators in the file (NOT for line feeds etc)
      // Then we scan that block of data.
      // We keep a byte[] that we extend if needed..
      // At the end of the block we read another, etc.
      //
      // Let's start by looking where we left off reading.
      //
      while ( !newLineFound && outputIndex < meta.getInputFields().length ) {

        if ( checkBufferSize() ) {
          // Last row was being discarded if the last item is null and
          // there is no end of line delimiter
          if ( outputRowData != null ) {
            // Make certain that at least one record exists before
            // filling the rest of them with null
            if ( outputIndex > 0 ) {
              return ( outputRowData );
            }
          }

          return null; // nothing more to read, call it a day.
        }

        // OK, at this point we should have data in the byteBuffer and we should be able to scan for the next
        // delimiter (;)
        // So let's look for a delimiter.
        // Also skip over the enclosures ("), it is NOT taking into account escaped enclosures.
        // Later we can add an option for having escaped or double enclosures in the file. <sigh>
        //
        boolean delimiterFound = false;
        boolean enclosureFound = false;
        int escapedEnclosureFound = 0;
        while ( !delimiterFound ) {
          // If we find the first char, we might find others as well ;-)
          // Single byte delimiters only for now.
          //
          if ( data.byteBuffer[data.endBuffer] == data.delimiter[0] ) {
            delimiterFound = true;
          } else if ( data.byteBuffer[data.endBuffer] == '\n' || data.byteBuffer[data.endBuffer] == '\r' ) {
            // Perhaps we found a new line?
            // "\n\r".getBytes()
            //
            data.endBuffer++;
            data.totalBytesRead++;
            newLines = 1;

            if ( !checkBufferSize() ) {
              // re-check for double delimiters...
              if ( data.byteBuffer[data.endBuffer] == '\n' || data.byteBuffer[data.endBuffer] == '\r' ) {
                data.endBuffer++;
                data.totalBytesRead++;
                newLines = 2;

                checkBufferSize();
              }
            }

            newLineFound = true;
            delimiterFound = true;
          } else if ( data.enclosure != null && data.byteBuffer[data.endBuffer] == data.enclosure[0] ) {
            // Perhaps we need to skip over an enclosed part?
            // We always expect exactly one enclosure character
            // If we find the enclosure doubled, we consider it escaped.
            // --> "" is converted to " later on.
            //

            enclosureFound = true;
            boolean keepGoing;
            do {
              data.endBuffer++;
              if ( checkBufferSize() ) {
                enclosureFound = false;
                break;
              }

              keepGoing = data.byteBuffer[data.endBuffer] != data.enclosure[0];
              if ( !keepGoing ) {
                // We found an enclosure character.
                // Read another byte...
                //
                data.endBuffer++;
                if ( checkBufferSize() ) {
                  enclosureFound = false;
                  break;
                }

                // If this character is also an enclosure, we can consider the enclosure "escaped".
                // As such, if this is an enclosure, we keep going...
                //
                keepGoing = data.byteBuffer[data.endBuffer] == data.enclosure[0];
                if ( keepGoing ) {
                  escapedEnclosureFound++;
                }
              }
            } while ( keepGoing );

            // Did we reach the end of the buffer?
            //
            if ( data.endBuffer >= data.bufferSize ) {
              newLineFound = true; // consider it a newline to break out of the upper while loop
              newLines += 2; // to remove the enclosures in case of missing newline on last line.
              break;
            }
          } else {

            data.endBuffer++;
            data.totalBytesRead++;

            if ( checkBufferSize() ) {
              if ( data.endBuffer >= data.bufferSize ) {
                newLineFound = true;
                break;
              }
            }
          }
        }

        // If we're still here, we found a delimiter..
        // Since the starting point never changed really, we just can grab range:
        //
        // [startBuffer-endBuffer[
        //
        // This is the part we want.
        //
        int length = data.endBuffer - data.startBuffer;
        if ( newLineFound ) {
          length -= newLines;
          if ( length <= 0 ) {
            length = 0;
          }
        }
        if ( enclosureFound ) {
          data.startBuffer++;
          length -= 2;
          if ( length <= 0 ) {
            length = 0;
          }
        }
        if ( length <= 0 ) {
          length = 0;
        }

        byte[] field = new byte[length];
        System.arraycopy( data.byteBuffer, data.startBuffer, field, 0, length );

        // Did we have any escaped characters in there?
        //
        if ( escapedEnclosureFound > 0 ) {
          if ( log.isRowLevel() ) {
            logRowlevel( "Escaped enclosures found in " + new String( field ) );
          }
          field = data.removeEscapedEnclosures( field, escapedEnclosureFound );
        }

        if ( doConversions ) {
          if ( meta.isLazyConversionActive() ) {
            outputRowData[outputIndex++] = field;
          } else {
            // We're not lazy so we convert the data right here and now.
            // The convert object uses binary storage as such we just have to ask the native type from it.
            // That will do the actual conversion.
            //
            ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta( outputIndex );
            outputRowData[outputIndex++] = sourceValueMeta.convertBinaryStringToNativeType( field );
          }
        } else {
          outputRowData[outputIndex++] = null; // nothing for the header, no conversions here.
        }

        // if (outputRowData[0]!=null && (outputRowData[0] instanceof Long) &&
        // ((Long)outputRowData[0]).longValue()==95174) {
        // System.out.println(outputRowData[0]);
        // }

        // OK, move on to the next field...
        if ( !newLineFound ) {
          data.endBuffer++;
          data.totalBytesRead++;
        }
        data.startBuffer = data.endBuffer;
      }

      // See if we reached the end of the line.
      // If not, we need to skip the remaining items on the line until the next newline...
      //
      if ( !newLineFound && !checkBufferSize() ) {
        do {
          data.endBuffer++;
          data.totalBytesRead++;

          if ( checkBufferSize() ) {
            break; // nothing more to read.
          }

          // TODO: if we're using quoting we might be dealing with a very dirty file with quoted newlines in trailing
          // fields. (imagine that)
          // In that particular case we want to use the same logic we use above (refactored a bit) to skip these fields.

        } while ( data.byteBuffer[data.endBuffer] != '\n' && data.byteBuffer[data.endBuffer] != '\r' );

        if ( !checkBufferSize() ) {
          while ( data.byteBuffer[data.endBuffer] == '\n' || data.byteBuffer[data.endBuffer] == '\r' ) {
            data.endBuffer++;
            data.totalBytesRead++;
            if ( checkBufferSize() ) {
              break; // nothing more to read.
            }
          }
        }

        // Make sure we start at the right position the next time around.
        data.startBuffer = data.endBuffer;
      }

      // Optionally add the current filename to the mix as well...
      //
      if ( meta.isIncludingFilename() && !Utils.isEmpty( meta.getFilenameField() ) ) {
        if ( meta.isLazyConversionActive() ) {
          outputRowData[data.filenameFieldIndex] = data.binaryFilename;
        } else {
          outputRowData[data.filenameFieldIndex] = data.filenames[data.filenr - 1];
        }
      }

      if ( data.isAddingRowNumber ) {
        outputRowData[data.rownumFieldIndex] = new Long( data.rowNumber++ );
      }

      incrementLinesInput();
      return outputRowData;
    } catch ( Exception e ) {
      throw new KettleFileException( "Exception reading line of data", e );
    }

  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ParGzipCsvInputMeta) smi;
    data = (ParGzipCsvInputData) sdi;

    if ( super.init( smi, sdi ) ) {

      data.bufferSize = Integer.parseInt( environmentSubstitute( meta.getBufferSize() ) );
      data.byteBuffer = new byte[] {}; // empty

      // If the step doesn't have any previous steps, we just get the filename.
      // Otherwise, we'll grab the list of filenames later...
      //
      if ( getTransMeta().findNrPrevSteps( getStepMeta() ) == 0 ) {
        String filename = environmentSubstitute( meta.getFilename() );

        if ( Utils.isEmpty( filename ) ) {
          logError( BaseMessages.getString( PKG, "ParGzipCsvInput.MissingFilename.Message" ) );
          return false;
        }

        data.filenames = new String[] { filename, };
      } else {
        data.filenames = null;
        data.filenr = 0;
      }

      data.delimiter = environmentSubstitute( meta.getDelimiter() ).getBytes();

      if ( Utils.isEmpty( meta.getEnclosure() ) ) {
        data.enclosure = null;
      } else {
        data.enclosure = environmentSubstitute( meta.getEnclosure() ).getBytes();
      }

      data.isAddingRowNumber = !Utils.isEmpty( meta.getRowNumField() );

      // Handle parallel reading capabilities...
      //
      if ( meta.isRunningInParallel() ) {
        data.stepNumber = getUniqueStepNrAcrossSlaves();
        data.totalNumberOfSteps = getUniqueStepCountAcrossSlaves();

      }

      return true;

    }
    return false;
  }

  public void closeFile() throws KettleException {

    try {
      if ( data.gzis != null ) {
        data.gzis.close();
      }
      if ( data.fis != null ) {
        incrementLinesUpdated();
        data.fis.close();
      }
    } catch ( IOException e ) {
      throw new KettleException( "Unable to close file '" + data.filenames[data.filenr - 1], e );
    }
  }

}
