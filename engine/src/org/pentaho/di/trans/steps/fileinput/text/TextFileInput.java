/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputStep;
import org.pentaho.di.trans.steps.fileinput.IBaseFileInputReader;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileInput extends BaseFileInputStep<TextFileInputMeta, TextFileInputData> implements StepInterface {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  public TextFileInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  protected IBaseFileInputReader createReader( TextFileInputMeta meta, TextFileInputData data, FileObject file )
    throws Exception {
    return new TextFileInputReader( this, meta, data, file, log );
  }

  @Override
  public boolean init() {
    Date replayDate = getTrans().getReplayDate();
    if ( replayDate == null ) {
      data.filePlayList = FilePlayListAll.INSTANCE;
    } else {
      data.filePlayList =
          new FilePlayListReplay( replayDate, meta.errorHandling.lineNumberFilesDestinationDirectory,
              meta.errorHandling.lineNumberFilesExtension, meta.errorHandling.errorFilesDestinationDirectory,
              meta.errorHandling.errorFilesExtension, meta.content.encoding );
    }

    data.filterProcessor = new TextFileFilterProcessor( meta.getFilter(), this );

    // calculate the file format type in advance so we can use a switch
    data.fileFormatType = meta.getFileFormatTypeNr();

    // calculate the file type in advance CSV or Fixed?
    data.fileType = meta.getFileTypeNr();

    // Handle the possibility of a variable substitution
    data.separator = environmentSubstitute( meta.content.separator );
    data.enclosure = environmentSubstitute( meta.content.enclosure );
    data.escapeCharacter = environmentSubstitute( meta.content.escapeCharacter );
    // CSV without separator defined
    if ( meta.content.fileType.equalsIgnoreCase( "CSV" ) && ( meta.content.separator == null || meta.content.separator
        .isEmpty() ) ) {
      logError( BaseMessages.getString( PKG, "TextFileInput.Exception.NoSeparator" ) );
      return false;
    }

    return true;
  }

  public boolean isWaitingForData() {
    return true;
  }

  public static final String[] guessStringsFromLine( VariableSpace space, LogChannelInterface log, String line,
      TextFileInputMeta inf, String delimiter, String enclosure, String escapeCharacter ) throws
      KettleException {
    List<String> strings = new ArrayList<String>();

    String pol; // piece of line

    try {
      if ( line == null ) {
        return null;
      }

      if ( inf.content.fileType.equalsIgnoreCase( "CSV" ) ) {

        // Split string in pieces, only for CSV!

        int pos = 0;
        int length = line.length();
        boolean dencl = false;

        int len_encl = ( enclosure == null ? 0 : enclosure.length() );
        int len_esc = ( escapeCharacter == null ? 0 : escapeCharacter.length() );

        while ( pos < length ) {
          int from = pos;
          int next;

          boolean encl_found;
          boolean contains_escaped_enclosures = false;
          boolean contains_escaped_separators = false;

          // Is the field beginning with an enclosure?
          // "aa;aa";123;"aaa-aaa";000;...
          if ( len_encl > 0 && line.substring( from, from + len_encl ).equalsIgnoreCase( enclosure ) ) {
            if ( log.isRowLevel() ) {
              log.logRowlevel( BaseMessages.getString( PKG, "TextFileInput.Log.ConvertLineToRowTitle" ), BaseMessages
                  .getString( PKG, "TextFileInput.Log.ConvertLineToRow", line.substring( from, from + len_encl ) ) );
            }
            encl_found = true;
            int p = from + len_encl;

            boolean is_enclosure =
                len_encl > 0 && p + len_encl < length && line.substring( p, p + len_encl )
                    .equalsIgnoreCase( enclosure );
            boolean is_escape =
                len_esc > 0 && p + len_esc < length
                    && line.substring( p, p + len_esc ).equalsIgnoreCase( escapeCharacter );

            boolean enclosure_after = false;

            // Is it really an enclosure? See if it's not repeated twice or escaped!
            if ( ( is_enclosure || is_escape ) && p < length - 1 ) {
              String strnext = line.substring( p + len_encl, p + 2 * len_encl );
              if ( strnext.equalsIgnoreCase( enclosure ) ) {
                p++;
                enclosure_after = true;
                dencl = true;

                // Remember to replace them later on!
                if ( is_escape ) {
                  contains_escaped_enclosures = true;
                }
              }
            }

            // Look for a closing enclosure!
            while ( ( !is_enclosure || enclosure_after ) && p < line.length() ) {
              p++;
              enclosure_after = false;
              is_enclosure =
                  len_encl > 0 && p + len_encl < length && line.substring( p, p + len_encl ).equals( enclosure );
              is_escape =
                  len_esc > 0 && p + len_esc < length && line.substring( p, p + len_esc ).equals( escapeCharacter );

              // Is it really an enclosure? See if it's not repeated twice or escaped!
              if ( ( is_enclosure || is_escape ) && p < length - 1 ) {

                String strnext = line.substring( p + len_encl, p + 2 * len_encl );
                if ( strnext.equals( enclosure ) ) {
                  p++;
                  enclosure_after = true;
                  dencl = true;

                  // Remember to replace them later on!
                  if ( is_escape ) {
                    contains_escaped_enclosures = true; // remember
                  }
                }
              }
            }

            if ( p >= length ) {
              next = p;
            } else {
              next = p + len_encl;
            }

            if ( log.isRowLevel() ) {
              log.logRowlevel( BaseMessages.getString( PKG, "TextFileInput.Log.ConvertLineToRowTitle" ), BaseMessages
                  .getString( PKG, "TextFileInput.Log.EndOfEnclosure", "" + p ) );
            }
          } else {
            encl_found = false;
            boolean found = false;
            int startpoint = from;
            // int tries = 1;
            do {
              next = line.indexOf( delimiter, startpoint );

              // See if this position is preceded by an escape character.
              if ( len_esc > 0 && next - len_esc > 0 ) {
                String before = line.substring( next - len_esc, next );

                if ( escapeCharacter.equals( before ) ) {
                  // take the next separator, this one is escaped...
                  startpoint = next + 1;
                  // tries++;
                  contains_escaped_separators = true;
                } else {
                  found = true;
                }
              } else {
                found = true;
              }
            } while ( !found && next >= 0 );
          }
          if ( next == -1 ) {
            next = length;
          }

          if ( encl_found ) {
            pol = line.substring( from + len_encl, next - len_encl );
            if ( log.isRowLevel() ) {
              log.logRowlevel( BaseMessages.getString( PKG, "TextFileInput.Log.ConvertLineToRowTitle" ), BaseMessages
                  .getString( PKG, "TextFileInput.Log.EnclosureFieldFound", "" + pol ) );
            }
          } else {
            pol = line.substring( from, next );
            if ( log.isRowLevel() ) {
              log.logRowlevel( BaseMessages.getString( PKG, "TextFileInput.Log.ConvertLineToRowTitle" ), BaseMessages
                  .getString( PKG, "TextFileInput.Log.NormalFieldFound", "" + pol ) );
            }
          }

          if ( dencl ) {
            StringBuilder sbpol = new StringBuilder( pol );
            int idx = sbpol.indexOf( enclosure + enclosure );
            while ( idx >= 0 ) {
              sbpol.delete( idx, idx + enclosure.length() );
              idx = sbpol.indexOf( enclosure + enclosure );
            }
            pol = sbpol.toString();
          }

          // replace the escaped enclosures with enclosures...
          if ( contains_escaped_enclosures ) {
            String replace = escapeCharacter + enclosure;
            String replaceWith = enclosure;

            pol = Const.replace( pol, replace, replaceWith );
          }

          // replace the escaped separators with separators...
          if ( contains_escaped_separators ) {
            String replace = escapeCharacter + delimiter;
            String replaceWith = delimiter;

            pol = Const.replace( pol, replace, replaceWith );
          }

          // Now add pol to the strings found!
          strings.add( pol );

          pos = next + delimiter.length();
        }
        if ( pos == length ) {
          if ( log.isRowLevel() ) {
            log.logRowlevel( BaseMessages.getString( PKG, "TextFileInput.Log.ConvertLineToRowTitle" ), BaseMessages
                .getString( PKG, "TextFileInput.Log.EndOfEmptyLineFound" ) );
          }
          strings.add( "" );
        }
      } else {
        // Fixed file format: Simply get the strings at the required positions...
        for ( int i = 0; i < inf.inputFiles.inputFields.length; i++ ) {
          BaseFileInputField field = inf.inputFiles.inputFields[i];

          int length = line.length();

          if ( field.getPosition() + field.getLength() <= length ) {
            strings.add( line.substring( field.getPosition(), field.getPosition() + field.getLength() ) );
          } else {
            if ( field.getPosition() < length ) {
              strings.add( line.substring( field.getPosition() ) );
            } else {
              strings.add( "" );
            }
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "TextFileInput.Log.Error.ErrorConvertingLine", e
          .toString() ), e );
    }

    return strings.toArray( new String[strings.size()] );
  }

  public static final String getLine( LogChannelInterface log, InputStreamReader reader, int formatNr,
      StringBuilder line ) throws KettleFileException {
    EncodingType type = EncodingType.guessEncodingType( reader.getEncoding() );
    return getLine( log, reader, type, formatNr, line );
  }

  public static final String getLine( LogChannelInterface log, InputStreamReader reader, EncodingType encodingType,
      int formatNr, StringBuilder line ) throws KettleFileException {
    int c = 0;
    line.setLength( 0 );
    try {
      switch ( formatNr ) {
        case TextFileInputMeta.FILE_FORMAT_DOS:
          while ( c >= 0 ) {
            c = reader.read();

            if ( encodingType.isReturn( c ) || encodingType.isLinefeed( c ) ) {
              c = reader.read(); // skip \n and \r
              if ( !encodingType.isReturn( c ) && !encodingType.isLinefeed( c ) ) {
                // make sure its really a linefeed or cariage return
                // raise an error this is not a DOS file
                // so we have pulled a character from the next line
                throw new KettleFileException( BaseMessages.getString( PKG, "TextFileInput.Log.SingleLineFound" ) );
              }
              return line.toString();
            }
            if ( c >= 0 ) {
              line.append( (char) c );
            }
          }
          break;
        case TextFileInputMeta.FILE_FORMAT_UNIX:
          while ( c >= 0 ) {
            c = reader.read();

            if ( encodingType.isLinefeed( c ) || encodingType.isReturn( c ) ) {
              return line.toString();
            }
            if ( c >= 0 ) {
              line.append( (char) c );
            }
          }
          break;
        case TextFileInputMeta.FILE_FORMAT_MIXED:
          // in mixed mode we suppose the LF is the last char and CR is ignored
          // not for MAC OS 9 but works for Mac OS X. Mac OS 9 can use UNIX-Format
          while ( c >= 0 ) {
            c = reader.read();

            if ( encodingType.isLinefeed( c ) ) {
              return line.toString();
            } else if ( !encodingType.isReturn( c ) ) {
              if ( c >= 0 ) {
                line.append( (char) c );
              }
            }
          }
          break;
        default:
          break;
      }
    } catch ( KettleFileException e ) {
      throw e;
    } catch ( Exception e ) {
      if ( line.length() == 0 ) {
        throw new KettleFileException( BaseMessages.getString( PKG, "TextFileInput.Log.Error.ExceptionReadingLine", e
            .toString() ), e );
      }
      return line.toString();
    }
    if ( line.length() > 0 ) {
      return line.toString();
    }

    return null;
  }
}
