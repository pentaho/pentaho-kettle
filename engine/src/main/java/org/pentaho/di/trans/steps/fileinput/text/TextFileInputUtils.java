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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputAdditionalField;

/**
 * Some common methods for text file parsing.
 *
 * @author Alexander Buloichik
 */
public class TextFileInputUtils {
  private static Class<?> PKG = TextFileInputUtils.class; // for i18n purposes, needed by Translator2!!

  public static final String[] guessStringsFromLine( VariableSpace space, LogChannelInterface log, String line,
      TextFileInputMeta inf, String delimiter, String enclosure, String escapeCharacter ) throws KettleException {
    List<String> strings = new ArrayList<>();

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
          boolean contains_escaped_escape = false;

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
                len_encl > 0 && p + len_encl < length && line.substring( p, p + len_encl ).equalsIgnoreCase(
                    enclosure );
            boolean is_escape =
                len_esc > 0 && p + len_esc < length && line.substring( p, p + len_esc ).equalsIgnoreCase(
                    escapeCharacter );

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
              } else if ( strnext.equals( escapeCharacter ) ) {
                p++;
                // Remember to replace them later on!
                if ( is_escape ) {
                  contains_escaped_escape = true; // remember
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
                } else if ( strnext.equals( escapeCharacter ) ) {
                  p++;
                  // Remember to replace them later on!
                  if ( is_escape ) {
                    contains_escaped_escape = true; // remember
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

          // replace the escaped escape with escape...
          if ( contains_escaped_escape ) {
            String replace = escapeCharacter + escapeCharacter;
            String replaceWith = escapeCharacter;

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
        for ( int i = 0; i < inf.inputFields.length; i++ ) {
          BaseFileField field = inf.inputFields[i];

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

  public static final Object[] convertLineToRow( LogChannelInterface log, TextFileLine textFileLine,
      TextFileInputMeta info, Object[] passThruFields, int nrPassThruFields, RowMetaInterface outputRowMeta,
      RowMetaInterface convertRowMeta, String fname, long rowNr, String delimiter, String enclosure,
      String escapeCharacter, FileErrorHandler errorHandler,
      BaseFileInputAdditionalField additionalOutputFields, String shortFilename, String path,
      boolean hidden, Date modificationDateTime, String uri, String rooturi, String extension, Long size )
        throws KettleException {
    if ( textFileLine == null || textFileLine.line == null ) {
      return null;
    }

    Object[] r = RowDataUtil.allocateRowData( outputRowMeta.size() ); // over-allocate a bit in the row producing
                                                                      // steps...

    int nrfields = info.inputFields.length;
    int fieldnr;

    Long errorCount = null;
    if ( info.errorHandling.errorIgnored && info.getErrorCountField() != null && info.getErrorCountField()
        .length() > 0 ) {
      errorCount = new Long( 0L );
    }
    String errorFields = null;
    if ( info.errorHandling.errorIgnored && info.getErrorFieldsField() != null && info.getErrorFieldsField()
        .length() > 0 ) {
      errorFields = "";
    }
    String errorText = null;
    if ( info.errorHandling.errorIgnored && info.getErrorTextField() != null && info.getErrorTextField()
        .length() > 0 ) {
      errorText = "";
    }

    try {
      // System.out.println("Convertings line to string ["+line+"]");
      String[] strings = convertLineToStrings( log, textFileLine.line, info, delimiter, enclosure, escapeCharacter );
      int shiftFields = ( passThruFields == null ? 0 : nrPassThruFields );
      for ( fieldnr = 0; fieldnr < nrfields; fieldnr++ ) {
        BaseFileField f = info.inputFields[fieldnr];
        int valuenr = shiftFields + fieldnr;
        ValueMetaInterface valueMeta = outputRowMeta.getValueMeta( valuenr );
        ValueMetaInterface convertMeta = convertRowMeta.getValueMeta( valuenr );

        Object value;

        String nullif = fieldnr < nrfields ? f.getNullString() : "";
        String ifnull = fieldnr < nrfields ? f.getIfNullValue() : "";
        int trim_type = fieldnr < nrfields ? f.getTrimType() : ValueMetaInterface.TRIM_TYPE_NONE;

        if ( fieldnr < strings.length ) {
          String pol = strings[ fieldnr ];
          try {
            if ( valueMeta.isNull( pol ) || !Utils.isEmpty( nullif ) && nullif.equals( pol ) ) {
              pol = null;
            }
            value = valueMeta.convertDataFromString( pol, convertMeta, nullif, ifnull, trim_type );
          } catch ( Exception e ) {
            // OK, give some feedback!
            String message =
                BaseMessages.getString( PKG, "TextFileInput.Log.CoundNotParseField", valueMeta.toStringMeta(), "" + pol,
                    valueMeta.getConversionMask(), "" + rowNr );

            if ( info.errorHandling.errorIgnored ) {
              log.logDetailed( fname, BaseMessages.getString( PKG, "TextFileInput.Log.Warning" ) + ": " + message
                  + " : " + e.getMessage() );

              value = null;

              if ( errorCount != null ) {
                errorCount = new Long( errorCount.longValue() + 1L );
              }
              if ( errorFields != null ) {
                StringBuilder sb = new StringBuilder( errorFields );
                if ( sb.length() > 0 ) {
                  sb.append( "\t" ); // TODO document this change
                }
                sb.append( valueMeta.getName() );
                errorFields = sb.toString();
              }
              if ( errorText != null ) {
                StringBuilder sb = new StringBuilder( errorText );
                if ( sb.length() > 0 ) {
                  sb.append( Const.CR );
                }
                sb.append( message );
                errorText = sb.toString();
              }
              if ( errorHandler != null ) {
                errorHandler.handleLineError( textFileLine.lineNumber, AbstractFileErrorHandler.NO_PARTS );
              }

              if ( info.isErrorLineSkipped() ) {
                r = null; // compensates for stmt: r.setIgnore();
              }
            } else {
              throw new KettleException( message, e );
            }
          }
        } else {
          // No data found: TRAILING NULLCOLS: add null value...
          value = null;
        }

        // Now add value to the row (if we're not skipping the row)
        if ( r != null ) {
          r[valuenr] = value;
        }
      }

      // none of this applies if we're skipping the row
      if ( r != null ) {
        // Support for trailing nullcols!
        // Should be OK at allocation time, but it doesn't hurt :-)
        if ( fieldnr < nrfields ) {
          for ( int i = fieldnr; i < info.inputFields.length; i++ ) {
            r[shiftFields + i] = null;
          }
        }

        // Add the error handling fields...
        int index = shiftFields + nrfields;
        if ( errorCount != null ) {
          r[index] = errorCount;
          index++;
        }
        if ( errorFields != null ) {
          r[index] = errorFields;
          index++;
        }
        if ( errorText != null ) {
          r[index] = errorText;
          index++;
        }

        // Possibly add a filename...
        if ( info.content.includeFilename ) {
          r[index] = fname;
          index++;
        }

        // Possibly add a row number...
        if ( info.content.includeRowNumber ) {
          r[index] = new Long( rowNr );
          index++;
        }

        // Possibly add short filename...
        if ( additionalOutputFields.shortFilenameField != null ) {
          r[index] = shortFilename;
          index++;
        }
        // Add Extension
        if ( additionalOutputFields.extensionField != null ) {
          r[index] = extension;
          index++;
        }
        // add path
        if ( additionalOutputFields.pathField != null ) {
          r[index] = path;
          index++;
        }
        // Add Size
        if ( additionalOutputFields.sizeField != null ) {
          r[index] = size;
          index++;
        }
        // add Hidden
        if ( additionalOutputFields.hiddenField != null ) {
          r[index] = hidden;
          index++;
        }
        // Add modification date
        if ( additionalOutputFields.lastModificationField != null ) {
          r[index] = modificationDateTime;
          index++;
        }
        // Add Uri
        if ( additionalOutputFields.uriField != null ) {
          r[index] = uri;
          index++;
        }
        // Add RootUri
        if ( additionalOutputFields.rootUriField != null ) {
          r[index] = rooturi;
          index++;
        }
      } // End if r != null
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "TextFileInput.Log.Error.ErrorConvertingLineText" ), e );
    }

    if ( r != null && passThruFields != null ) {
      // Simply add all fields from source files step
      for ( int i = 0; i < nrPassThruFields; i++ ) {
        r[i] = passThruFields[i];
      }
    }

    return r;
  }

  public static final String[] convertLineToStrings( LogChannelInterface log, String line, TextFileInputMeta inf,
      String delimiter, String enclosure, String escapeCharacters ) throws KettleException {
    String[] strings = new String[inf.inputFields.length];
    int fieldnr;

    String pol; // piece of line

    try {
      if ( line == null ) {
        return null;
      }

      if ( inf.content.fileType.equalsIgnoreCase( "CSV" ) ) {
        // Split string in pieces, only for CSV!

        fieldnr = 0;
        int pos = 0;
        int length = line.length();
        boolean dencl = false;

        int len_encl = ( enclosure == null ? 0 : enclosure.length() );
        int len_esc = ( escapeCharacters == null ? 0 : escapeCharacters.length() );

        while ( pos < length ) {
          int from = pos;
          int next;

          boolean encl_found;
          boolean contains_escaped_enclosures = false;
          boolean contains_escaped_separators = false;
          boolean contains_escaped_escape = false;

          // Is the field beginning with an enclosure?
          // "aa;aa";123;"aaa-aaa";000;...
          if ( len_encl > 0 && line.substring( from, from + len_encl ).equalsIgnoreCase( enclosure ) ) {
            if ( log.isRowLevel() ) {
              log.logRowlevel( BaseMessages.getString( PKG, "TextFileInput.Log.ConvertLineToRowTitle" ), BaseMessages
                  .getString( PKG, "TextFileInput.Log.Encloruse", line.substring( from, from + len_encl ) ) );
            }
            encl_found = true;
            int p = from + len_encl;

            boolean is_enclosure =
                len_encl > 0 && p + len_encl < length && line.substring( p, p + len_encl ).equalsIgnoreCase(
                    enclosure );
            boolean is_escape =
                len_esc > 0 && p + len_esc < length && line.substring( p, p + len_esc ).equalsIgnoreCase(
                    inf.content.escapeCharacter );

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
              } else if ( strnext.equals( inf.content.escapeCharacter ) ) {
                p++;
                // Remember to replace them later on!
                if ( is_escape ) {
                  contains_escaped_escape = true; // remember
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
                  len_esc > 0 && p + len_esc < length && line.substring( p, p + len_esc ).equals(
                      inf.content.escapeCharacter );

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
                } else if ( strnext.equals( inf.content.escapeCharacter ) ) {
                  p++;
                  // Remember to replace them later on!
                  if ( is_escape ) {
                    contains_escaped_escape = true; // remember
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

                if ( inf.content.escapeCharacter.equals( before ) ) {
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

          if ( encl_found && ( ( from + len_encl ) <= ( next - len_encl ) ) ) {
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

          if ( dencl && Utils.isEmpty( inf.content.escapeCharacter ) ) {
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
            String replace = inf.content.escapeCharacter + enclosure;
            String replaceWith = enclosure;

            pol = Const.replace( pol, replace, replaceWith );
          }

          // replace the escaped separators with separators...
          if ( contains_escaped_separators ) {
            String replace = inf.content.escapeCharacter + delimiter;
            String replaceWith = delimiter;

            pol = Const.replace( pol, replace, replaceWith );
          }

          // replace the escaped escape with escape...
          if ( contains_escaped_escape ) {
            String replace = inf.content.escapeCharacter + inf.content.escapeCharacter;
            String replaceWith = inf.content.escapeCharacter;

            pol = Const.replace( pol, replace, replaceWith );
          }

          // Now add pol to the strings found!
          try {
            strings[fieldnr] = pol;
          } catch ( ArrayIndexOutOfBoundsException e ) {
            // In case we didn't allocate enough space.
            // This happens when you have less header values specified than there are actual values in the rows.
            // As this is "the exception" we catch and resize here.
            //
            String[] newStrings = new String[strings.length];
            for ( int x = 0; x < strings.length; x++ ) {
              newStrings[x] = strings[x];
            }
            strings = newStrings;
          }

          pos = next + delimiter.length();
          fieldnr++;
        }
        if ( pos == length ) {
          if ( log.isRowLevel() ) {
            log.logRowlevel( BaseMessages.getString( PKG, "TextFileInput.Log.ConvertLineToRowTitle" ), BaseMessages
                .getString( PKG, "TextFileInput.Log.EndOfEmptyLineFound" ) );
          }
          if ( fieldnr < strings.length ) {
            strings[fieldnr] = Const.EMPTY_STRING;
          }
          fieldnr++;
        }
      } else {
        // Fixed file format: Simply get the strings at the required positions...
        // Note - charBased is the old default behavior. If this is an old transformation, content.length will be null
        // and should be processed as before. If the content.length is equal to "Characters" or there is no specified encoding,
        // it will still use the old behavior. The *only* way to get the new behavior is if content.length = "Bytes" and
        // the encoding is specified.
        boolean charBased = ( inf.content.length == null || inf.content.length.equalsIgnoreCase( "Characters" ) || inf.getEncoding() == null ); // Default to classic behavior
        for ( int i = 0; i < inf.inputFields.length; i++ ) {
          BaseFileField field = inf.inputFields[i];

          int length;
          int fPos = field.getPosition();
          int fLength = field.getLength();
          int fPl = fPos + fLength;
          if ( charBased ) {
            length = line.length();
            if ( fPl <= length ) {
              strings[i] = line.substring( fPos, fPl );
            } else {
              if ( fPos < length ) {
                strings[i] = line.substring( fPos );
              } else {
                strings[i] = "";
              }
            }
          } else {
            byte[] b = null;
            String enc = inf.getEncoding();
            b = line.getBytes( enc );
            length = b.length;
            if ( fPl <= length ) {
              strings[i] = new String( Arrays.copyOfRange( b, fPos, fPl ), enc );
            } else {
              if ( fPos < length ) {
                strings[i] = new String( Arrays.copyOfRange( b, fPos, length - 1 ), enc );
              } else {
                strings[i] = "";
              }
            }
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "TextFileInput.Log.Error.ErrorConvertingLine", e
          .toString() ), e );
    }

    return strings;
  }
}
