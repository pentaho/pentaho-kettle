/*! ******************************************************************************
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

package org.pentaho.di.trans.steps.s3csvinput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read a simple CSV file
 * Just output Strings found in the file...
 *
 * @author Matt
 * @since 2007-07-05
 */
public class S3CsvInput extends BaseStep implements StepInterface {
  private S3CsvInputMeta meta;
  private S3CsvInputData data;

  public S3CsvInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (S3CsvInputMeta) smi;
    data = (S3CsvInputData) sdi;

    if ( first ) {
      first = false;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this );

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

      // Now handle the parallel reading aspect: determine total of all the file sizes
      // Then skip to the appropriate file and location in the file to start reading...
      // Also skip to right after the first newline
      //
      if ( data.parallel ) {
        prepareToRunInParallel();
      }

      // Open the next file...
      //
      if ( !openNextFile() ) {
        setOutputDone();
        return false; // nothing to see here, move along...
      }
    }

    // If we are running in parallel, make sure we don't read too much in this step copy...
    //
    if ( data.parallel ) {
      if ( data.totalBytesRead > data.blockToRead ) {
        setOutputDone(); // stop reading
        return false;
      }
    }

    Object[] outputRowData = readOneRow( true );    // get row, set busy!
    if ( outputRowData == null ) { // no more input to be expected...
      if ( openNextFile() ) {
        return true; // try again on the next loop...
      } else {
        setOutputDone(); // last file, end here
        return false;
      }
    } else {
      putRow( data.outputRowMeta, outputRowData );     // copy row to possible alternate rowset(s).
      if ( checkFeedback( getLinesInput() ) ) {
        if ( log.isBasic() ) {
          logBasic( Messages.getString( "S3CsvInput.Log.LineNumber", Long.toString( getLinesInput() ) ) ); //$NON-NLS-1$
        }
      }
    }

    return true;
  }


  private void prepareToRunInParallel() throws KettleException {
    try {
      // At this point it doesn't matter if we have 1 or more files.
      // We'll use the same algorithm...
      //
      for ( String filename : data.filenames ) {
        long size = new S3ObjectsProvider( data.s3Service ).getS3ObjectContentLenght( data.s3bucket, filename );
        data.fileSizes.add( size );
        data.totalFileSize += size;
      }

      // Now we can determine the range to read.
      //
      // For example, the total file size is 50000, spread over 5 files of 10000
      // Suppose we have 2 step copies running (clustered or not)
      // That means step 0 has to read 0-24999 and step 1 has to read 25000-49999
      //
      // The size of the block to read (25000 in the example) :
      //
      data.blockToRead = Math.round( (double) data.totalFileSize / (double) data.totalNumberOfSteps );

      // Now we calculate the position to read (0 and 25000 in our sample) :
      //
      data.startPosition = data.blockToRead * data.stepNumber;
      data.endPosition = data.startPosition + data.blockToRead;

      // Determine the start file number (0 or 2 in our sample) :
      // >0<,1000,>2000<,3000,4000
      //
      long totalFileSize = 0L;
      for ( int i = 0; i < data.fileSizes.size(); i++ ) {
        long size = data.fileSizes.get( i );

        // Start of file range: totalFileSize
        // End of file range: totalFileSize+size

        if ( data.startPosition >= totalFileSize && data.startPosition < totalFileSize + size ) {
          // This is the file number to start reading from...
          //
          data.filenr = i;

          // remember where we started to read to allow us to know that we have to skip the header row in the next files (if any)
          //
          data.startFilenr = i;


          // How many bytes do we skip in that first file?
          //
          if ( data.startPosition == 0 ) {
            data.bytesToSkipInFirstFile = 0L;
          } else {
            data.bytesToSkipInFirstFile = data.startPosition - totalFileSize;
          }

          break;
        }
        totalFileSize += size;
      }

      logBasic( Messages.getString( "S3CsvInput.Log.ParallelFileNrAndPositionFeedback", data.filenames[data.filenr], Long.toString( data.fileSizes.get( data.filenr ) ), Long.toString( data.bytesToSkipInFirstFile ), Long.toString( data.blockToRead ) ) );
    } catch ( Exception e ) {
      throw new KettleException( Messages.getString( "S3CsvInput.Exception.ErrorPreparingParallelRun" ), e );
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
          throw new KettleException( Messages.getString( "S3CsvInput.Exception.FilenameFieldNotFound", filenameField ) );
        }
      }

      String filename = getInputRowMeta().getString( row, index );
      filenames.add( filename );  // add it to the list...

      row = getRow(); // Grab another row...
    }

    data.filenames = filenames.toArray( new String[filenames.size()] );

    logBasic( Messages.getString( "S3CsvInput.Log.ReadingFromNrFiles", Integer.toString( data.filenames.length ) ) );
  }

  private boolean openNextFile() throws KettleException {
    try {

      // Close the previous file...
      //
      if ( data.fis != null ) {
        data.fis.close();
      }

      if ( data.filenr >= data.filenames.length ) {
        return false;
      }

      data.s3Object = null;

      // If we are running in parallel we only want to grab a part of the content, not everything.
      //
      if ( data.parallel ) {

        data.s3Object = new S3ObjectsProvider( data.s3Service ).getS3Object( data.s3bucket, data.filenames[data.filenr], data.bytesToSkipInFirstFile, data.bytesToSkipInFirstFile + data.blockToRead + data.maxLineSize * 2 );

      } else {
        data.s3Object = new S3ObjectsProvider( data.s3Service ).getS3Object( data.s3bucket, data.filenames[data.filenr] );
      }

      if ( meta.isLazyConversionActive() ) {
        data.binaryFilename = data.filenames[data.filenr].getBytes();
      }

      data.fis = data.s3Object.getDataInputStream();

      if ( data.parallel ) {
        if ( data.bytesToSkipInFirstFile > 0 ) {
          // Now, we need to skip the first row, until the first CR that is.
          //
          readOneRow( false );
        }
      }

      // See if we need to skip the header row...
      //
      if ( ( meta.isHeaderPresent() && !data.parallel ) || // Standard flat file : skip header
          ( data.parallel && data.filenr == data.startFilenr && data.bytesToSkipInFirstFile <= 0 ) || // parallel processing : first file : nothing to skip
          ( data.parallel && data.filenr > data.startFilenr && data.bytesToSkipInFirstFile <= 0 ) ) {   // parallel processing : start of next file, nothing to skip
        readOneRow( false ); // skip this row.
        logBasic( Messages.getString( "S3CsvInput.Log.HeaderRowSkipped", data.filenames[data.filenr] ) );
      }

      // Move to the next filename
      //
      data.filenr++;

      // Reset the row number pointer...
      //
      data.rowNumber = 1L;

      // Don't skip again in the next file...
      //
      data.bytesToSkipInFirstFile = -1L;


      return true;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /** Read a single row of data from the file...
   *
   * @param doConversions if you want to do conversions, set to false for the header row.
   * @return a row of data...
   * @throws KettleException
   */
  private Object[] readOneRow( boolean doConversions ) throws KettleException {

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
      while ( !newLineFound && outputIndex < data.convertRowMeta.size() ) {

        if ( data.endBuffer >= data.bufferSize ) {
          // Oops, we need to read more data...
          // Better resize this before we read other things in it...
          //
          data.resizeByteBuffer();

          // Also read another chunk of data, now that we have the space for it...
          if ( !data.readBufferFromFile() ) {
            // TODO handle EOF properly for EOF in the middle of the row, etc.
            return null;
          }
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
            //
            //
            data.endBuffer++;
            data.totalBytesRead++;
            newLines = 1;

            if ( data.endBuffer >= data.bufferSize ) {
              // Oops, we need to read more data...
              // Better resize this before we read other things in it...
              //
              data.resizeByteBuffer();

              // Also read another chunk of data, now that we have the space for it...
              // Ignore EOF, there might be other stuff in the buffer.
              //
              data.readBufferFromFile();
            }

            // re-check for double delimiters...
            if ( data.byteBuffer[data.endBuffer] == '\n' || data.byteBuffer[data.endBuffer] == '\r' ) {
              data.endBuffer++;
              data.totalBytesRead++;
              newLines = 2;
              if ( data.endBuffer >= data.bufferSize ) {
                // Oops, we need to read more data...
                // Better resize this before we read other things in it...
                //
                data.resizeByteBuffer();

                // Also read another chunk of data, now that we have the space for it...
                // Ignore EOF, there might be other stuff in the buffer.
                //
                data.readBufferFromFile();
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
              if ( data.increaseEndBuffer() ) {
                enclosureFound = false;
                break;
              }
              keepGoing = data.byteBuffer[data.endBuffer] != data.enclosure[0];
              if ( !keepGoing ) {
                // We found an enclosure character.
                // Read another byte...
                if ( data.increaseEndBuffer() ) {
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

            if ( data.endBuffer >= data.bufferSize ) {
              // Oops, we need to read more data...
              // Better resize this before we read other things in it...
              //
              data.resizeByteBuffer();

              // Also read another chunk of data, now that we have the space for it...
              if ( !data.readBufferFromFile() ) {
                // Break out of the loop if we don't have enough buffer space to continue...
                //
                if ( data.endBuffer >= data.bufferSize ) {
                  newLineFound = true; // consider it a newline to break out of the upper while loop
                  break;
                }
              }
            }
          }
        }

        // If we're still here, we found a delimiter..
        // Since the starting point never changed really, we just can grab range:
        //
        //    [startBuffer-endBuffer[
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

        // OK, move on to the next field...
        if ( !newLineFound ) {
          data.endBuffer++;
          data.totalBytesRead++;
        }
        data.startBuffer = data.endBuffer;
      }

      // Optionally add the current filename to the mix as well...
      //
      if ( meta.isIncludingFilename() && !Utils.isEmpty( meta.getFilenameField() ) ) {
        if ( meta.isLazyConversionActive() ) {
          outputRowData[outputIndex++] = data.binaryFilename;
        } else {
          outputRowData[outputIndex++] = data.filenames[data.filenr - 1];
        }
      }

      if ( data.isAddingRowNumber ) {
        outputRowData[outputIndex++] = new Long( data.rowNumber++ );
      }

      incrementLinesInput();
      return outputRowData;
    } catch ( Exception e ) {
      throw new KettleFileException( "Exception reading line using NIO", e );
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (S3CsvInputMeta) smi;
    data = (S3CsvInputData) sdi;

    if ( super.init( smi, sdi ) ) {

      data.preferredBufferSize = 500000; // Fixed size

      try {
        //Get the specified bucket
        String bucketname = environmentSubstitute( meta.getBucket() );
        data.s3Service = meta.getS3Service( this );
        data.s3bucket = new S3ObjectsProvider( data.s3Service ).getBucket( bucketname );
        if ( data.s3bucket == null ) {
          logError( Messages.getString( "S3CsvInput.Log.UnableToFindBucket.Message", bucketname ) );
          return false;
        }

        data.maxLineSize = Integer.parseInt( environmentSubstitute( meta.getMaxLineSize() ) );

        // If the step doesn't have any previous steps, we just get the filename.
        // Otherwise, we'll grab the list of filenames later...
        //
        if ( getTransMeta().findNrPrevSteps( getStepMeta() ) == 0 ) {
          String filename = environmentSubstitute( meta.getFilename() );

          if ( Utils.isEmpty( filename ) ) {
            logError( Messages.getString( "S3CsvInput.MissingFilename.Message" ) );
            return false;
          }

          data.filenames = new String[] { filename, };
        } else {
          data.filenames = null;
          data.filenr = 0;
        }

        data.totalBytesRead = 0L;

        data.delimiter = environmentSubstitute( meta.getDelimiter() ).getBytes();

        if ( Utils.isEmpty( meta.getEnclosure() ) ) {
          data.enclosure = null;
        } else {
          data.enclosure = environmentSubstitute( meta.getEnclosure() ).getBytes();
        }

        data.isAddingRowNumber = !Utils.isEmpty( meta.getRowNumField() );

        // Handle parallel reading capabilities...
        //
        data.stopReading = false;

        if ( meta.isRunningInParallel() ) {
          data.stepNumber = getUniqueStepNrAcrossSlaves();
          data.totalNumberOfSteps = getUniqueStepCountAcrossSlaves();

          // We are not handling a single file, but possibly a list of files...
          // As such, the fair thing to do is calculate the total size of the files
          // Then read the required block.
          //

          data.fileSizes = new ArrayList<Long>();
          data.totalFileSize = 0L;
        }

        return true;
      } catch ( Exception e ) {
        logError( "Unexpected error trying to verify S3 settings : ", e );
      }

    }
    return false;
  }

  public void closeFile() throws KettleException {
    try {
      if ( data.fis != null ) {
        data.fis.close();
      }
    } catch ( IOException e ) {
      throw new KettleException( "Unable to close file channel for file '" + data.filenames[data.filenr - 1], e );
    }
  }
}
