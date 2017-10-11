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

package org.pentaho.di.trans.steps.fileinput.text;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;
import org.pentaho.di.trans.steps.file.IBaseFileInputStepControl;

/**
 * Reader for one text file.
 *
 * @author Alexander Buloichik
 */
public class TextFileInputReader implements IBaseFileInputReader {
  private static final int BUFFER_SIZE_INPUT_STREAM = 8192;

  private final IBaseFileInputStepControl step;
  private final TextFileInputMeta meta;
  private final TextFileInputData data;
  private final LogChannelInterface log;

  private final CompressionInputStream in;

  private final InputStreamReader isr;

  protected long lineInFile;

  private boolean first;

  protected long lineNumberInFile;

  public TextFileInputReader( IBaseFileInputStepControl step, TextFileInputMeta meta, TextFileInputData data,
      FileObject file, LogChannelInterface log ) throws Exception {
    this.step = step;
    this.meta = meta;
    this.data = data;
    this.log = log;

    CompressionProvider provider =
        CompressionProviderFactory.getInstance().getCompressionProviderByName( meta.content.fileCompression );

    if ( log.isDetailed() ) {
      log.logDetailed( "This is a compressed file being handled by the " + provider.getName() + " provider" );
    }

    in = provider.createInputStream( KettleVFS.getInputStream( file ) );

    in.nextEntry();

    BufferedInputStream inStream = new BufferedInputStream( in, BUFFER_SIZE_INPUT_STREAM );
    BOMDetector bom = new BOMDetector( inStream );

    if ( bom.bomExist() ) {
      // if BOM exist, use it instead defined charset
      isr = new InputStreamReader( inStream, bom.getCharset() );
    } else if ( meta.getEncoding() != null && meta.getEncoding().length() > 0 ) {
      isr = new InputStreamReader( inStream, meta.getEncoding() );
    } else {
      isr = new InputStreamReader( inStream );
    }

    String encoding = isr.getEncoding();
    data.encodingType = EncodingType.guessEncodingType( encoding );

    readInitial();
  }

  protected void readInitial() throws Exception {
    data.doneWithHeader = !meta.content.header;
    // /////////////////////////////////////////////////////////////////////////////
    // Read the first lines...

    /*
     * Keep track of the status of the file: are there any lines left to read?
     */
    data.doneReading = false;

    /*
     * OK, read a number of lines in the buffer: The header rows The nr rows in the page : optional The footer rows
     */
    int bufferSize = 1;
    bufferSize += meta.content.header ? meta.content.nrHeaderLines : 0;
    bufferSize +=
        meta.content.layoutPaged ? meta.content.nrLinesPerPage * ( Math.max( 0, meta.content.nrWraps ) + 1 ) : Math.max(
            0, meta.content.nrWraps ); // it helps when we have wrapped input w/o header

    bufferSize += meta.content.footer ? meta.content.nrFooterLines : 0;

    // See if we need to skip the document header lines...
    if ( meta.content.layoutPaged ) {
      for ( int i = 0; i < meta.content.nrLinesDocHeader; i++ ) {
        // Just skip these...
        TextFileInputUtils.getLine( log, isr, data.encodingType, data.fileFormatType, data.lineStringBuilder ); // header
                                                                                                                // and
        // footer: not
        // wrapped
        lineNumberInFile++;
      }
    }

    for ( int i = 0; i < bufferSize && !data.doneReading; i++ ) {
      boolean wasNotFiltered = tryToReadLine( !meta.content.header || i >= meta.content.nrHeaderLines );
      if ( !wasNotFiltered ) {
        // grab another line, this one got filtered
        bufferSize++;
      }
    }

    // Reset counters etc.
    data.headerLinesRead = 0;
    data.footerLinesRead = 0;
    data.pageLinesRead = 0;

  }

  @Override
  public boolean readRow() throws KettleException {
    Object[] r = null;
    boolean retval = true;
    boolean putrow = false;

    if ( !data.doneReading ) {
      int repeats = 1;
      if ( meta.content.lineWrapped ) {
        repeats = meta.content.nrWraps > 0 ? meta.content.nrWraps : repeats;
      }

      if ( !data.doneWithHeader && data.headerLinesRead == 0 ) {
        // We are just starting to read header lines, read them all
        repeats += meta.content.nrHeaderLines + 1;
      }

      // Read a number of lines...
      for ( int i = 0; i < repeats && !data.doneReading; i++ ) {
        if ( !tryToReadLine( true ) ) {
          repeats++;
        }
      }
    }

    if ( data.lineBuffer.isEmpty() ) {
      return false;
    }

    /*
     * Take the first line available in the buffer & remove the line from the buffer
     */
    TextFileLine textLine = data.lineBuffer.get( 0 );
    step.incrementLinesInput();

    data.lineBuffer.remove( 0 );

    if ( meta.content.layoutPaged ) {
      /*
       * Different rules apply: on each page: a header a number of data lines a footer
       */
      if ( !data.doneWithHeader && data.pageLinesRead == 0 ) { // We are reading header lines
        if ( log.isRowLevel() ) {
          log.logRowlevel( "P-HEADER (" + data.headerLinesRead + ") : " + textLine.line );
        }
        data.headerLinesRead++;
        if ( data.headerLinesRead >= meta.content.nrHeaderLines ) {
          data.doneWithHeader = true;
        }
      } else {
        // data lines or footer on a page

        if ( data.pageLinesRead < meta.content.nrLinesPerPage ) {
          // See if we are dealing with wrapped lines:
          if ( meta.content.lineWrapped ) {
            for ( int i = 0; i < meta.content.nrWraps; i++ ) {
              String extra = "";
              if ( data.lineBuffer.size() > 0 ) {
                extra = data.lineBuffer.get( 0 ).line;
                data.lineBuffer.remove( 0 );
              }
              textLine.line += extra;
            }
          }

          if ( log.isRowLevel() ) {
            log.logRowlevel( "P-DATA: " + textLine.line );
          }
          // Read a normal line on a page of data.
          data.pageLinesRead++;
          lineInFile++;
          long useNumber = meta.content.rowNumberByFile ? lineInFile : step.getLinesWritten() + 1;
          r =
              TextFileInputUtils.convertLineToRow( log, textLine, meta, data.currentPassThruFieldsRow,
                  data.nrPassThruFields, data.outputRowMeta, data.convertRowMeta, data.filename, useNumber,
                  data.separator, data.enclosure, data.escapeCharacter, data.dataErrorLineHandler,
                  meta.additionalOutputFields, data.shortFilename, data.path, data.hidden,
                  data.lastModificationDateTime, data.uriName, data.rootUriName, data.extension, data.size );
          if ( r != null ) {
            putrow = true;
          }

          // Possible fix for bug PDI-1121 - paged layout header and line count off by 1
          // We need to reset these BEFORE the next header line is read, so that it
          // is treated as a header ... obviously, only if there is no footer, and we are
          // done reading data.
          if ( !meta.content.footer && ( data.pageLinesRead == meta.content.nrLinesPerPage ) ) {
            /*
             * OK, we are done reading the footer lines, start again on 'next page' with the header
             */
            data.doneWithHeader = false;
            data.headerLinesRead = 0;
            data.pageLinesRead = 0;
            data.footerLinesRead = 0;
            if ( log.isRowLevel() ) {
              log.logRowlevel( "RESTART PAGE" );
            }
          }
        } else {
          // done reading the data lines, skip the footer lines

          if ( meta.content.footer && data.footerLinesRead < meta.content.nrFooterLines ) {
            if ( log.isRowLevel() ) {
              log.logRowlevel( "P-FOOTER: " + textLine.line );
            }
            data.footerLinesRead++;
          }

          if ( !meta.content.footer || data.footerLinesRead >= meta.content.nrFooterLines ) {
            /*
             * OK, we are done reading the footer lines, start again on 'next page' with the header
             */
            data.doneWithHeader = false;
            data.headerLinesRead = 0;
            data.pageLinesRead = 0;
            data.footerLinesRead = 0;
            if ( log.isRowLevel() ) {
              log.logRowlevel( "RESTART PAGE" );
            }
          }
        }
      }
    } else {
      // A normal data line, can also be a header or a footer line

      if ( !data.doneWithHeader ) { // We are reading header lines

        data.headerLinesRead++;
        if ( data.headerLinesRead >= meta.content.nrHeaderLines ) {
          data.doneWithHeader = true;
        }
      } else {
        /*
         * IF we are done reading and we have a footer AND the number of lines in the buffer is smaller then the number
         * of footer lines THEN we can remove the remaining rows from the buffer: they are all footer rows.
         */
        if ( data.doneReading && meta.content.footer && data.lineBuffer.size() < meta.content.nrFooterLines ) {
          data.lineBuffer.clear();
        } else {
          // Not yet a footer line: it's a normal data line.

          // See if we are dealing with wrapped lines:
          if ( meta.content.lineWrapped ) {
            for ( int i = 0; i < meta.content.nrWraps; i++ ) {
              String extra = "";
              if ( data.lineBuffer.size() > 0 ) {
                extra = data.lineBuffer.get( 0 ).line;
                data.lineBuffer.remove( 0 );
              } else {
                tryToReadLine( true );
                if ( !data.lineBuffer.isEmpty() ) {
                  extra = data.lineBuffer.remove( 0 ).line;
                }
              }
              textLine.line += extra;
            }
          }
          if ( data.filePlayList.isProcessingNeeded( textLine.file, textLine.lineNumber,
              AbstractFileErrorHandler.NO_PARTS ) ) {
            lineInFile++;
            long useNumber = meta.content.rowNumberByFile ? lineInFile : step.getLinesWritten() + 1;
            r =
                TextFileInputUtils.convertLineToRow( log, textLine, meta, data.currentPassThruFieldsRow,
                    data.nrPassThruFields, data.outputRowMeta, data.convertRowMeta, data.filename, useNumber,
                    data.separator, data.enclosure, data.escapeCharacter, data.dataErrorLineHandler,
                    meta.additionalOutputFields, data.shortFilename, data.path, data.hidden,
                    data.lastModificationDateTime, data.uriName, data.rootUriName, data.extension, data.size );
            if ( r != null ) {
              if ( log.isRowLevel() ) {
                log.logRowlevel( "Found data row: " + data.outputRowMeta.getString( r ) );
              }
              putrow = true;
            }
          } else {
            putrow = false;
          }
        }
      }
    }

    if ( putrow && r != null ) {
      // See if the previous values need to be repeated!
      if ( data.nr_repeats > 0 ) {
        if ( data.previous_row == null ) { // First invocation...

          data.previous_row = data.outputRowMeta.cloneRow( r );
        } else {
          // int repnr = 0;
          for ( int i = 0; i < meta.inputFields.length; i++ ) {
            if ( meta.inputFields[i].isRepeated() ) {
              if ( r[i] == null ) {
                // if it is empty: take the previous value!

                r[i] = data.previous_row[i];
              } else {
                // not empty: change the previous_row entry!

                data.previous_row[i] = r[i];
              }
              // repnr++;
            }
          }
        }
      }

      if ( log.isRowLevel() ) {
        log.logRowlevel( "Putting row: " + data.outputRowMeta.getString( r ) );
      }
      step.putRow( data.outputRowMeta, r );

      if ( step.getLinesInput() >= meta.content.rowLimit && meta.content.rowLimit > 0 ) {
        close();
        return false;
      }
    }

    if ( step.checkFeedback( step.getLinesInput() ) ) {
      if ( log.isBasic() ) {
        log.logBasic( "linenr " + step.getLinesInput() );
      }
    }

    return retval;
  }

  @Override
  public void close() {
    try {
      // Close previous file!
      if ( data.filename != null ) {
        // Increment the lines updated to reflect another file has been finished.
        // This allows us to give a state of progress in the run time metrics
        step.incrementLinesUpdated();
        /*
         * } else if ( sFileCompression != null && sFileCompression.equals( "Snappy" ) && data.sis != null ) {
         * data.sis.close(); }
         */
        if ( in != null ) {
          BaseStep.closeQuietly( in );
        }
        isr.close();
        data.filename = null; // send it down the next time.
        if ( data.file != null ) {
          try {
            data.file.close();
            data.file = null;
          } catch ( Exception e ) {
            log.logError( "Error closing file", e );
          }
          data.file = null;
        }
      }
      data.dataErrorLineHandler.close();
    } catch ( Exception e ) {
      String errorMsg = "Couldn't close file : " + data.file.getName().getFriendlyURI() + " --> " + e.toString();
      log.logError( errorMsg );
      if ( step.failAfterBadFile( errorMsg ) ) { // ( !meta.isSkipBadFiles() || data.isLastFile ){
        step.stopAll();
      }
      step.setErrors( step.getErrors() + 1 );
    } // finally {
      // This is for bug #5797 : it tries to assure that the file handle
      // is actually freed/garbarge collected.
      // XXX deinspanjer 2009-07-07: I'm stubbing this out. The bug was ancient and it is worth reevaluating
      // to avoid the performance hit of a System GC on every file close
      // System.gc();
      // }
  }

  protected boolean tryToReadLine( boolean applyFilter ) throws KettleFileException {
    String line;
    line = TextFileInputUtils.getLine( log, isr, data.encodingType, data.fileFormatType, data.lineStringBuilder );
    if ( line != null ) {
      // when there is no header, check the filter for the first line
      if ( applyFilter ) {
        // Filter row?
        boolean isFilterLastLine = false;
        boolean filterOK = checkFilterRow( line, isFilterLastLine );
        if ( filterOK ) {
          data.lineBuffer.add( new TextFileLine( line, lineNumberInFile++, data.file ) ); // Store it in the
          // line buffer...
        } else {
          return false;
        }
      } else { // don't checkFilterRow

        if ( !meta.content.noEmptyLines || line.length() != 0 ) {
          data.lineBuffer.add( new TextFileLine( line, lineNumberInFile++, data.file ) ); // Store it in the line
                                                                                        // buffer...
        }
      }
    } else {
      data.doneReading = true;
    }
    return true;
  }

  /**
   * Check if the line should be taken.
   *
   * @param line
   * @param isFilterLastLine
   *          (dummy input param, only set when return value is false)
   * @return true when the line should be taken (when false, isFilterLastLine will be set)
   */
  private boolean checkFilterRow( String line, boolean isFilterLastLine ) {
    boolean filterOK = true;

    // check for noEmptyLines
    if ( meta.content.noEmptyLines && line.length() == 0 ) {
      filterOK = false;
    } else {
      // check the filters
      filterOK = data.filterProcessor.doFilters( line );
      if ( !filterOK ) {
        if ( data.filterProcessor.isStopProcessing() ) {
          data.doneReading = true;
        }
      }
    }

    return filterOK;
  }

}
