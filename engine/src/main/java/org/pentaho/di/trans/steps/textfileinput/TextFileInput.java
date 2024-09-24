/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.textfileinput;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.AbstractFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.CompositeFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerContentLineNumber;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerMissingFiles;
import org.pentaho.di.trans.steps.fileinput.text.TextFileLine;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 *
 * @author Matt
 * @since 4-apr-2003
 *
 * @deprecated replaced by implementation in the ...steps.fileinput.text package
 */
@Deprecated
public class TextFileInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  private static final int BUFFER_SIZE_INPUT_STREAM = 500;

  private TextFileInputMeta meta;

  private TextFileInputData data;

  private long lineNumberInFile;

  public TextFileInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
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

  @Deprecated
  public static final String[] guessStringsFromLine( LogChannelInterface log, String line, TextFileInputMeta inf,
      String delimiter ) throws KettleException {
    return guessStringsFromLine( new Variables(), log, line, inf, delimiter, StringUtil.substituteHex( inf
        .getEnclosure() ), StringUtil.substituteHex( inf.getEscapeCharacter() ) );
  }

  public static final String[] guessStringsFromLine( VariableSpace space, LogChannelInterface log, String line,
      TextFileInputMeta inf, String delimiter, String enclosure, String escapeCharacter ) throws KettleException {
    List<String> strings = new ArrayList<String>();

    String pol; // piece of line

    try {
      if ( line == null ) {
        return null;
      }

      if ( inf.getFileType().equalsIgnoreCase( "CSV" ) ) {

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
        for ( int i = 0; i < inf.getInputFields().length; i++ ) {
          TextFileInputField field = inf.getInputFields()[i];

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

  public static final String[] convertLineToStrings( LogChannelInterface log, String line, InputFileMetaInterface inf,
      String delimiter, String enclosure, String escapeCharacters ) throws KettleException {
    String[] strings = new String[inf.getInputFields().length];
    int fieldnr;

    String pol; // piece of line

    try {
      if ( line == null ) {
        return null;
      }

      if ( inf.getFileType().equalsIgnoreCase( "CSV" ) ) {
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
                len_encl > 0 && p + len_encl < length
                    && line.substring( p, p + len_encl ).equalsIgnoreCase( enclosure );
            boolean is_escape =
                len_esc > 0 && p + len_esc < length
                    && line.substring( p, p + len_esc ).equalsIgnoreCase( inf.getEscapeCharacter() );

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
                  len_encl > 0 && p + len_encl < length
                      && line.substring( p, p + len_encl ).equals( enclosure );
              is_escape =
                  len_esc > 0 && p + len_esc < length
                      && line.substring( p, p + len_esc ).equals( inf.getEscapeCharacter() );

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

                if ( inf.getEscapeCharacter().equals( before ) ) {
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

          if ( dencl && Utils.isEmpty( inf.getEscapeCharacter() ) ) {
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
            String replace = inf.getEscapeCharacter() + enclosure;
            String replaceWith = enclosure;

            pol = Const.replace( pol, replace, replaceWith );
          }

          // replace the escaped separators with separators...
          if ( contains_escaped_separators ) {
            String replace = inf.getEscapeCharacter() + delimiter;
            String replaceWith = delimiter;

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
        for ( int i = 0; i < inf.getInputFields().length; i++ ) {
          TextFileInputField field = inf.getInputFields()[i];

          int length = line.length();

          if ( field.getPosition() + field.getLength() <= length ) {
            strings[i] = line.substring( field.getPosition(), field.getPosition() + field.getLength() );
          } else {
            if ( field.getPosition() < length ) {
              strings[i] = line.substring( field.getPosition() );
            } else {
              strings[i] = "";
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

  /**
   * @deprecated Use {@link #convertLineToRow(LogChannelInterface, TextFileLine,
   * InputFileMetaInterface, Object[], int, RowMetaInterface,RowMetaInterface,
   * String, long, String, String, String, FileErrorHandler, boolean, boolean,
   * boolean, boolean, boolean, boolean, boolean, boolean, String, String, boolean,
   * Date, String, String, String, long)} instead.
   */
  @Deprecated
  public static final Object[] convertLineToRow( LogChannelInterface log, TextFileLine textFileLine,
      InputFileMetaInterface info, RowMetaInterface outputRowMeta, RowMetaInterface convertRowMeta, String fname,
      long rowNr, String delimiter, FileErrorHandler errorHandler, boolean addShortFilename, boolean addExtension,
      boolean addPath, boolean addSize, boolean addIsHidden, boolean addLastModificationDate, boolean addUri,
      boolean addRootUri, String shortFilename, String path, boolean hidden, Date modificationDateTime, String uri,
      String rooturi, String extension, long size ) throws KettleException {
    return convertLineToRow( log, textFileLine, info, null, 0, outputRowMeta, convertRowMeta, fname, rowNr, delimiter,
        StringUtil.substituteHex( info.getEnclosure() ), StringUtil.substituteHex( info.getEscapeCharacter() ),
        errorHandler, addShortFilename, addExtension, addPath, addSize, addIsHidden, addLastModificationDate, addUri,
        addRootUri, shortFilename, path, hidden, modificationDateTime, uri, rooturi, extension, size );
  }


  public static Object[] convertLineToRow( LogChannelInterface log, TextFileLine textFileLine,
                                           InputFileMetaInterface info, Object[] passThruFields, int nrPassThruFields, RowMetaInterface outputRowMeta,
                                           RowMetaInterface convertRowMeta, String fname, long rowNr, String delimiter, String enclosure,
                                           String escapeCharacter, FileErrorHandler errorHandler, boolean addShortFilename, boolean addExtension,
                                           boolean addPath, boolean addSize, boolean addIsHidden, boolean addLastModificationDate, boolean addUri,
                                           boolean addRootUri, String shortFilename, String path, boolean hidden, Date modificationDateTime, String uri,
                                           String rooturi, String extension, long size ) throws KettleException {
    return convertLineToRow( log, textFileLine, info, passThruFields, nrPassThruFields, outputRowMeta, convertRowMeta,
      fname, rowNr, delimiter, enclosure, escapeCharacter, errorHandler, addShortFilename, addExtension, addPath,
      addSize, addIsHidden, addLastModificationDate, addUri, addRootUri, shortFilename, path, hidden,
      modificationDateTime, uri, rooturi, extension, size, true );
  }

  public static Object[] convertLineToRow( LogChannelInterface log, TextFileLine textFileLine,
      InputFileMetaInterface info, Object[] passThruFields, int nrPassThruFields, RowMetaInterface outputRowMeta,
      RowMetaInterface convertRowMeta, String fname, long rowNr, String delimiter, String enclosure,
      String escapeCharacter, FileErrorHandler errorHandler, boolean addShortFilename, boolean addExtension,
      boolean addPath, boolean addSize, boolean addIsHidden, boolean addLastModificationDate, boolean addUri,
      boolean addRootUri, String shortFilename, String path, boolean hidden, Date modificationDateTime, String uri,
      String rooturi, String extension, long size, final boolean failOnParseError ) throws KettleException {
    if ( textFileLine == null || textFileLine.getLine() == null ) {
      return null;
    }

    Object[] r = RowDataUtil.allocateRowData( outputRowMeta.size() ); // over-allocate a bit in the row producing
                                                                      // steps...

    int nrfields = info.getInputFields().length;
    int fieldnr;

    Long errorCount = null;
    if ( info.isErrorIgnored() && info.getErrorCountField() != null && info.getErrorCountField().length() > 0 ) {
      errorCount = new Long( 0L );
    }
    String errorFields = null;
    if ( info.isErrorIgnored() && info.getErrorFieldsField() != null && info.getErrorFieldsField().length() > 0 ) {
      errorFields = "";
    }
    String errorText = null;
    if ( info.isErrorIgnored() && info.getErrorTextField() != null && info.getErrorTextField().length() > 0 ) {
      errorText = "";
    }

    try {
      // System.out.println("Convertings line to string ["+line+"]");
      String[] strings = convertLineToStrings( log, textFileLine.getLine(), info, delimiter, enclosure, escapeCharacter );
      int shiftFields = ( passThruFields == null ? 0 : nrPassThruFields );
      for ( fieldnr = 0; fieldnr < nrfields; fieldnr++ ) {
        TextFileInputField f = info.getInputFields()[fieldnr];
        int valuenr = shiftFields + fieldnr;
        ValueMetaInterface valueMeta = outputRowMeta.getValueMeta( valuenr );
        ValueMetaInterface convertMeta = convertRowMeta.getValueMeta( valuenr );

        Object value = null;

        String nullif = fieldnr < nrfields ? f.getNullString() : "";
        String ifnull = fieldnr < nrfields ? f.getIfNullValue() : "";
        int trim_type = fieldnr < nrfields ? f.getTrimType() : ValueMetaInterface.TRIM_TYPE_NONE;

        if ( fieldnr < strings.length ) {
          String pol = strings[ fieldnr ];
          try {
            if ( valueMeta.isNull( pol ) ) {
              pol = null;
            }
            value = valueMeta.convertDataFromString( pol, convertMeta, nullif, ifnull, trim_type );
          } catch ( Exception e ) {
            // when getting fields, failOnParseError will be set to false, as we do not want one mis-configured field
            // to prevent us from analyzing other fields, we simply leave the string value as is
            if ( failOnParseError ) {
              // OK, give some feedback!
              String message =
                BaseMessages.getString( PKG, "TextFileInput.Log.CoundNotParseField", valueMeta.toStringMeta(),
                  "" + pol, valueMeta.getConversionMask(), "" + rowNr );

              if ( info.isErrorIgnored() ) {
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
                  errorHandler.handleLineError( textFileLine.getLineNumber(), AbstractFileErrorHandler.NO_PARTS );
                }

                if ( info.isErrorLineSkipped() ) {
                  r = null; // compensates for stmt: r.setIgnore();
                }
              } else {
                throw new KettleException( message, e );
              }
            } else {
              value = pol;
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
          for ( int i = fieldnr; i < info.getInputFields().length; i++ ) {
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
        if ( info.includeFilename() ) {
          r[index] = fname;
          index++;
        }

        // Possibly add a row number...
        if ( info.includeRowNumber() ) {
          r[index] = new Long( rowNr );
          index++;
        }

        // Possibly add short filename...
        if ( addShortFilename ) {
          r[index] = shortFilename;
          index++;
        }
        // Add Extension
        if ( addExtension ) {
          r[index] = extension;
          index++;
        }
        // add path
        if ( addPath ) {
          r[index] = path;
          index++;
        }
        // Add Size
        if ( addSize ) {
          r[index] = new Long( size );
          index++;
        }
        // add Hidden
        if ( addIsHidden ) {
          r[index] = hidden;
          index++;
        }
        // Add modification date
        if ( addLastModificationDate ) {
          r[index] = modificationDateTime;
          index++;
        }
        // Add Uri
        if ( addUri ) {
          r[index] = uri;
          index++;
        }
        // Add RootUri
        if ( addRootUri ) {
          r[index] = rooturi;
          index++;
        }

        if ( passThruFields != null ) {
          // Simply add all fields from source files step
          for ( int i = 0; i < nrPassThruFields; i++ ) {
            r[i] = passThruFields[i];
          }
        }

      } // End if r != null
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "TextFileInput.Log.Error.ErrorConvertingLineText" ), e );
    }

    return r;

  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    data = (TextFileInputData) sdi;
    meta = (TextFileInputMeta) smi;
    Object[] r = null;
    boolean retval = true;
    boolean putrow = false;

    if ( first ) { // we just got started

      first = false;

      data.outputRowMeta = new RowMeta();
      RowMetaInterface[] infoStep = null;

      if ( meta.isAcceptingFilenames() ) {
        // Read the files from the specified input stream...
        //
        data.getFiles().getFiles().clear();

        int idx = -1;
        data.rowSet = findInputRowSet( meta.getAcceptingStepName() );

        Object[] fileRow = getRowFrom( data.rowSet );
        while ( fileRow != null ) {
          RowMetaInterface prevInfoFields = data.rowSet.getRowMeta();
          if ( idx < 0 ) {
            if ( meta.isPassingThruFields() ) {
              data.passThruFields = new HashMap<FileObject, Object[]>();
              infoStep = new RowMetaInterface[] { prevInfoFields };
              data.nrPassThruFields = prevInfoFields.size();
            }
            idx = prevInfoFields.indexOfValue( meta.getAcceptingField() );
            if ( idx < 0 ) {
              logError( BaseMessages.getString( PKG, "TextFileInput.Log.Error.UnableToFindFilenameField", meta
                  .getAcceptingField() ) );
              setErrors( getErrors() + 1 );
              stopAll();
              return false;
            }
          }
          String fileValue = prevInfoFields.getString( fileRow, idx );
          try {
            FileObject fileObject = KettleVFS.getFileObject( fileValue, getTransMeta() );
            data.getFiles().addFile( fileObject );
            if ( meta.isPassingThruFields() ) {
              data.passThruFields.put( fileObject, fileRow );
            }
          } catch ( KettleFileException e ) {
            logError( BaseMessages.getString( PKG, "TextFileInput.Log.Error.UnableToCreateFileObject", fileValue ), e );
          }

          // Grab another row
          fileRow = getRowFrom( data.rowSet );
        }

        if ( data.getFiles().nrOfFiles() == 0 ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "TextFileInput.Log.Error.NoFilesSpecified" ) );
          }
          setOutputDone();
          return false;
        }
      }

      // // get the metadata populated. Simple and easy.
      meta.getFields( data.outputRowMeta, getStepname(), infoStep, null, this, repository, metaStore );
      // Create convert meta-data objects that will contain Date & Number formatters
      //
      data.convertRowMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

      handleMissingFiles();

      // Open the first file & read the required rows in the buffer, stop
      // if it fails and not set to skip bad files...
      if ( !openNextFile() ) {
        if ( failAfterBadFile( null ) ) {
          closeLastFile();
          setOutputDone();
          return false;
        }
      }

      // Count the number of repeat fields...
      for ( int i = 0; i < meta.getInputFields().length; i++ ) {
        if ( meta.getInputFields()[i].isRepeated() ) {
          data.nr_repeats++;
        }
      }
    } else {
      if ( !data.doneReading ) {
        int repeats = 1;
        if ( meta.isLineWrapped() ) {
          repeats = meta.getNrWraps() > 0 ? meta.getNrWraps() : repeats;
        }

        if ( !data.doneWithHeader && data.headerLinesRead == 0 ) {
          // We are just starting to read header lines, read them all
          repeats += meta.getNrHeaderLines() + 1;
        }

        // Read a number of lines...
        for ( int i = 0; i < repeats && !data.doneReading; i++ ) {
          if ( !tryToReadLine( true ) ) {
            repeats++;
          }
        }
      }
    }

    /*
     * If the buffer is empty: open the next file. (if nothing in there, open the next, etc.)
     */
    while ( data.lineBuffer.size() == 0 ) {
      if ( !openNextFile() ) {
        // Open fails: done processing unless set to skip bad files
        if ( failAfterBadFile( null ) ) {
          closeLastFile();
          setOutputDone(); // signal end to receiver(s)
          return false;
        } // else will continue until can open
      }
    }

    /*
     * Take the first line available in the buffer & remove the line from the buffer
     */
    TextFileLine textLine = data.lineBuffer.get( 0 );
    incrementLinesInput();
    lineNumberInFile++;

    data.lineBuffer.remove( 0 );

    if ( meta.isLayoutPaged() ) {
      /*
       * Different rules apply: on each page: a header a number of data lines a footer
       */
      if ( !data.doneWithHeader && data.pageLinesRead == 0 ) {
        // We are reading header lines
        if ( log.isRowLevel() ) {
          logRowlevel( "P-HEADER (" + data.headerLinesRead + ") : " + textLine.getLine() );
        }
        data.headerLinesRead++;
        if ( data.headerLinesRead >= meta.getNrHeaderLines() ) {
          data.doneWithHeader = true;
        }
      } else {
        // data lines or footer on a page

        if ( data.pageLinesRead < meta.getNrLinesPerPage() ) {
          // See if we are dealing with wrapped lines:
          if ( meta.isLineWrapped() ) {
            for ( int i = 0; i < meta.getNrWraps(); i++ ) {
              String extra = "";
              if ( data.lineBuffer.size() > 0 ) {
                extra = data.lineBuffer.get( 0 ).getLine();
                data.lineBuffer.remove( 0 );
              }
              textLine.setLine( textLine.getLine() + extra );
            }
          }

          if ( log.isRowLevel() ) {
            logRowlevel( "P-DATA: " + textLine.getLine() );
          }
          // Read a normal line on a page of data.
          data.pageLinesRead++;
          data.lineInFile++;
          long useNumber = meta.isRowNumberByFile() ? data.lineInFile : getLinesWritten() + 1;
          r =
              convertLineToRow( log, textLine, meta, data.currentPassThruFieldsRow, data.nrPassThruFields,
                  data.outputRowMeta, data.convertRowMeta, data.filename, useNumber, data.separator, data.enclosure,
                  data.escapeCharacter, data.dataErrorLineHandler, data.addShortFilename, data.addExtension,
                  data.addPath, data.addSize, data.addIsHidden, data.addLastModificationDate, data.addUri,
                  data.addRootUri, data.shortFilename, data.path, data.hidden, data.lastModificationDateTime,
                  data.uriName, data.rootUriName, data.extension, data.size );
          if ( r != null ) {
            putrow = true;
          }

          // Possible fix for bug PDI-1121 - paged layout header and line count off by 1
          // We need to reset these BEFORE the next header line is read, so that it
          // is treated as a header ... obviously, only if there is no footer, and we are
          // done reading data.
          if ( !meta.hasFooter() && ( data.pageLinesRead == meta.getNrLinesPerPage() ) ) {
            /*
             * OK, we are done reading the footer lines, start again on 'next page' with the header
             */
            data.doneWithHeader = false;
            data.headerLinesRead = 0;
            data.pageLinesRead = 0;
            data.footerLinesRead = 0;
            if ( log.isRowLevel() ) {
              logRowlevel( "RESTART PAGE" );
            }
          }
        } else {
          // done reading the data lines, skip the footer lines

          if ( meta.hasFooter() && data.footerLinesRead < meta.getNrFooterLines() ) {
            if ( log.isRowLevel() ) {
              logRowlevel( "P-FOOTER: " + textLine.getLine() );
            }
            data.footerLinesRead++;
          }

          if ( !meta.hasFooter() || data.footerLinesRead >= meta.getNrFooterLines() ) {
            /*
             * OK, we are done reading the footer lines, start again on 'next page' with the header
             */
            data.doneWithHeader = false;
            data.headerLinesRead = 0;
            data.pageLinesRead = 0;
            data.footerLinesRead = 0;
            if ( log.isRowLevel() ) {
              logRowlevel( "RESTART PAGE" );
            }
          }
        }
      }
    } else {
      // A normal data line, can also be a header or a footer line

      if ( !data.doneWithHeader ) { // We are reading header lines

        data.headerLinesRead++;
        if ( data.headerLinesRead >= meta.getNrHeaderLines() ) {
          data.doneWithHeader = true;
        }
      } else {
        /*
         * IF we are done reading and we have a footer AND the number of lines in the buffer is smaller then the number
         * of footer lines THEN we can remove the remaining rows from the buffer: they are all footer rows.
         */
        if ( data.doneReading && meta.hasFooter() && data.lineBuffer.size() < meta.getNrFooterLines() ) {
          data.lineBuffer.clear();
        } else {
          // Not yet a footer line: it's a normal data line.

          // See if we are dealing with wrapped lines:
          if ( meta.isLineWrapped() ) {
            for ( int i = 0; i < meta.getNrWraps(); i++ ) {
              String extra = "";
              if ( data.lineBuffer.size() > 0 ) {
                extra = data.lineBuffer.get( 0 ).getLine();
                data.lineBuffer.remove( 0 );
              } else {
                tryToReadLine( true );
                if ( !data.lineBuffer.isEmpty() ) {
                  extra = data.lineBuffer.remove( 0 ).getLine();
                }
              }
              textLine.setLine( textLine.getLine() + extra );
            }
          }
          if ( data.filePlayList.isProcessingNeeded( textLine.getFile(), textLine.getLineNumber(),
              AbstractFileErrorHandler.NO_PARTS ) ) {
            data.lineInFile++;
            long useNumber = meta.isRowNumberByFile() ? data.lineInFile : getLinesWritten() + 1;
            r =
                convertLineToRow( log, textLine, meta, data.currentPassThruFieldsRow, data.nrPassThruFields,
                    data.outputRowMeta, data.convertRowMeta, data.filename, useNumber, data.separator, data.enclosure,
                    data.escapeCharacter, data.dataErrorLineHandler, data.addShortFilename, data.addExtension,
                    data.addPath, data.addSize, data.addIsHidden, data.addLastModificationDate, data.addUri,
                    data.addRootUri, data.shortFilename, data.path, data.hidden, data.lastModificationDateTime,
                    data.uriName, data.rootUriName, data.extension, data.size );
            if ( r != null ) {
              if ( log.isRowLevel() ) {
                logRowlevel( "Found data row: " + data.outputRowMeta.getString( r ) );
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
          for ( int i = 0; i < meta.getInputFields().length; i++ ) {
            if ( meta.getInputFields()[i].isRepeated() ) {
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
        logRowlevel( "Putting row: " + data.outputRowMeta.getString( r ) );
      }
      putRow( data.outputRowMeta, r );

      if ( getLinesInput() >= meta.getRowLimit() && meta.getRowLimit() > 0 ) {
        closeLastFile();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
    }

    if ( checkFeedback( getLinesInput() ) ) {
      if ( log.isBasic() ) {
        logBasic( "linenr " + getLinesInput() );
      }
    }

    return retval;
  }

  /**
   *
   * @param errorMsg
   *          Message to send to rejected row if enabled
   * @return If should stop processing after having problems with a file
   */
  private boolean failAfterBadFile( String errorMsg ) {

    if ( getStepMeta().isDoingErrorHandling() && data.filename != null
        && !data.rejectedFiles.containsKey( data.filename ) ) {
      data.rejectedFiles.put( data.filename, true );
      rejectCurrentFile( errorMsg );
    }

    return !meta.isErrorIgnored() || !meta.isSkipBadFiles() || data.isLastFile;
  }

  /**
   * Send file name and/or error message to error output
   *
   * @param errorMsg
   *          Message to send to rejected row if enabled
   */
  private void rejectCurrentFile( String errorMsg ) {
    if ( StringUtils.isNotBlank( meta.getFileErrorField() )
      || StringUtils.isNotBlank( meta.getFileErrorMessageField() ) ) {
      RowMetaInterface rowMeta = getInputRowMeta();
      if ( rowMeta == null ) {
        rowMeta = new RowMeta();
      }

      int errorFileIndex =
          ( StringUtils.isBlank( meta.getFileErrorField() ) ) ? -1 : addValueMeta( rowMeta, this
              .environmentSubstitute( meta.getFileErrorField() ) );

      int errorMessageIndex =
          StringUtils.isBlank( meta.getFileErrorMessageField() ) ? -1 : addValueMeta( rowMeta, this
              .environmentSubstitute( meta.getFileErrorMessageField() ) );

      try {
        Object[] rowData = getRow();
        if ( rowData == null ) {
          rowData = RowDataUtil.allocateRowData( rowMeta.size() );
        }

        if ( errorFileIndex >= 0 ) {
          rowData[errorFileIndex] = data.filename;
        }
        if ( errorMessageIndex >= 0 ) {
          rowData[errorMessageIndex] = errorMsg;
        }

        putError( rowMeta, rowData, getErrors(), data.filename, null, "ERROR_CODE" );
      } catch ( Exception e ) {
        logError( "Error sending error row", e );
      }
    }
  }

  /**
   * Adds <code>String</code> value meta with given name if not present and returns index
   *
   * @param rowMeta
   * @param fieldName
   * @return Index in row meta of value meta with <code>fieldName</code>
   */
  private int addValueMeta( RowMetaInterface rowMeta, String fieldName ) {
    ValueMetaInterface valueMeta = new ValueMetaString( fieldName );
    valueMeta.setOrigin( getStepname() );
    // add if doesn't exist
    int index = -1;
    if ( !rowMeta.exists( valueMeta ) ) {
      index = rowMeta.size();
      rowMeta.addValueMeta( valueMeta );
    } else {
      index = rowMeta.indexOfValue( fieldName );
    }
    return index;
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
    if ( meta.noEmptyLines() && line.length() == 0 ) {
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

  private void handleMissingFiles() throws KettleException {
    List<FileObject> nonExistantFiles = data.getFiles().getNonExistantFiles();

    if ( nonExistantFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      if ( log.isBasic() ) {
        log.logBasic( "Required files", "WARNING: Missing " + message );
      }
      if ( meta.isErrorIgnored() ) {
        for ( FileObject fileObject : nonExistantFiles ) {
          data.dataErrorLineHandler.handleNonExistantFile( fileObject );
        }
      } else {
        throw new KettleException( "Following required files are missing: " + message );
      }
    }

    List<FileObject> nonAccessibleFiles = data.getFiles().getNonAccessibleFiles();
    if ( nonAccessibleFiles.size() != 0 ) {
      String message = FileInputList.getRequiredFilesDescription( nonAccessibleFiles );
      if ( log.isBasic() ) {
        log.logBasic( "Required files", "WARNING: Not accessible " + message );
      }
      if ( meta.isErrorIgnored() ) {
        for ( FileObject fileObject : nonAccessibleFiles ) {
          data.dataErrorLineHandler.handleNonAccessibleFile( fileObject );
        }
      } else {
        throw new KettleException( "Following required files are not accessible: " + message );
      }
    }
  }

  private boolean closeLastFile() {
    try {
      // Close previous file!
      if ( data.filename != null ) {
        // Increment the lines updated to reflect another file has been finished.
        // This allows us to give a state of progress in the run time metrics
        incrementLinesUpdated();
        /*
         * } else if ( sFileCompression != null && sFileCompression.equals( "Snappy" ) && data.sis != null ) {
         * data.sis.close(); }
         */
        data.in.close();
        data.isr.close();
        data.filename = null; // send it down the next time.
        if ( data.file != null ) {
          data.file.close();
          data.file = null;
        }
      }
      data.dataErrorLineHandler.close();
    } catch ( Exception e ) {
      String errorMsg = "Couldn't close file : " + data.file.getName().getFriendlyURI() + " --> " + e.toString();
      logError( errorMsg );
      if ( failAfterBadFile( errorMsg ) ) { // ( !meta.isSkipBadFiles() || data.isLastFile ){
        stopAll();
      }
      setErrors( getErrors() + 1 );
      return false;
    } // finally {
      // This is for bug #5797 : it tries to assure that the file handle
      // is actually freed/garbarge collected.
      // XXX deinspanjer 2009-07-07: I'm stubbing this out. The bug was ancient and it is worth reevaluating
      // to avoid the performance hit of a System GC on every file close
      // System.gc();
      // }

    return !data.isLastFile;
  }

  private boolean openNextFile() {

    try {
      lineNumberInFile = 0;
      if ( !closeLastFile() && failAfterBadFile( null ) ) {
        return false; // (!meta.isSkipBadFiles() || data.isLastFile) ) return false;
      }

      if ( data.getFiles().nrOfFiles() == 0 ) {
        return false;
      }

      // Is this the last file?
      data.isLastFile = ( data.filenr == data.getFiles().nrOfFiles() - 1 );
      data.file = data.getFiles().getFile( data.filenr );
      data.filename = KettleVFS.getFilename( data.file );

      // Move file pointer ahead!
      data.filenr++;

      // Add additional fields?
      if ( data.addShortFilename ) {
        data.shortFilename = data.file.getName().getBaseName();
      }
      if ( data.addPath ) {
        data.path = KettleVFS.getFilename( data.file.getParent() );
      }
      if ( data.addIsHidden ) {
        data.hidden = data.file.isHidden();
      }
      if ( data.addExtension ) {
        data.extension = data.file.getName().getExtension();
      }
      if ( data.addLastModificationDate ) {
        data.lastModificationDateTime = new Date( data.file.getContent().getLastModifiedTime() );
      }
      if ( data.addUri ) {
        data.uriName = Const.optionallyDecodeUriString( data.file.getName().getURI() );
      }
      if ( data.addRootUri ) {
        data.rootUriName = data.file.getName().getRootURI();
      }
      if ( data.addSize ) {
        data.size = new Long( data.file.getContent().getSize() );
      }
      data.lineInFile = 0;
      if ( meta.isPassingThruFields() ) {
        data.currentPassThruFieldsRow = data.passThruFields.get( data.file );
      }

      // Add this files to the result of this transformation.
      //
      if ( meta.isAddResultFile() ) {
        ResultFile resultFile =
            new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), toString() );
        resultFile.setComment( "File was read by an Text File input step" );
        addResultFile( resultFile );
      }
      if ( log.isBasic() ) {
        logBasic( "Opening file: " + data.file.getName().getFriendlyURI() );
      }

      CompressionProvider provider =
          CompressionProviderFactory.getInstance().getCompressionProviderByName( meta.getFileCompression() );

      data.in = provider.createInputStream( KettleVFS.getInputStream( data.file ) );
      data.dataErrorLineHandler.handleFile( data.file );
      data.in.nextEntry();

      if ( log.isDetailed() ) {
        logDetailed( "This is a compressed file being handled by the " + provider.getName() + " provider" );
      }

      if ( meta.getEncoding() != null && meta.getEncoding().length() > 0 ) {
        data.isr =
            new InputStreamReader( new BufferedInputStream( data.in, BUFFER_SIZE_INPUT_STREAM ), meta.getEncoding() );
      } else {
        data.isr = new InputStreamReader( new BufferedInputStream( data.in, BUFFER_SIZE_INPUT_STREAM ) );
      }

      String encoding = data.isr.getEncoding();
      data.encodingType = EncodingType.guessEncodingType( encoding );

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
      bufferSize += meta.hasHeader() ? meta.getNrHeaderLines() : 0;
      bufferSize += meta.isLayoutPaged()
        ? meta.getNrLinesPerPage() * ( Math.max( 0, meta.getNrWraps() ) + 1 )
        : Math.max( 0, meta.getNrWraps() ); // it helps when we have wrapped input w/o header

      bufferSize += meta.hasFooter() ? meta.getNrFooterLines() : 0;

      // See if we need to skip the document header lines...
      if ( meta.isLayoutPaged() ) {
        for ( int i = 0; i < meta.getNrLinesDocHeader(); i++ ) {
          // Just skip these...
          getLine( log, data.isr, data.encodingType, data.fileFormatType, data.lineStringBuilder ); // header and
                                                                                                    // footer: not
                                                                                                    // wrapped
          lineNumberInFile++;
        }
      }

      for ( int i = 0; i < bufferSize && !data.doneReading; i++ ) {
        boolean wasNotFiltered = tryToReadLine( !meta.hasHeader() || i >= meta.getNrHeaderLines() );
        if ( !wasNotFiltered ) {
          // grab another line, this one got filtered
          bufferSize++;
        }
      }

      // Reset counters etc.
      data.headerLinesRead = 0;
      data.footerLinesRead = 0;
      data.pageLinesRead = 0;

      // Set a flags
      data.doneWithHeader = !meta.hasHeader();
    } catch ( Exception e ) {
      String errorMsg =
          "Couldn't open file #" + data.filenr + " : " + data.file.getName().getFriendlyURI() + " --> " + e.toString();
      logError( errorMsg );
      if ( failAfterBadFile( errorMsg ) ) { // !meta.isSkipBadFiles()) stopAll();
        stopAll();
      }
      setErrors( getErrors() + 1 );
      return false;
    }
    return true;
  }

  private boolean tryToReadLine( boolean applyFilter ) throws KettleFileException {
    String line;
    line = getLine( log, data.isr, data.encodingType, data.fileFormatType, data.lineStringBuilder );
    if ( line != null ) {
      // when there is no header, check the filter for the first line
      if ( applyFilter ) {
        // Filter row?
        boolean isFilterLastLine = false;
        boolean filterOK = checkFilterRow( line, isFilterLastLine );
        if ( filterOK ) {
          data.lineBuffer.add( new TextFileLine( line, lineNumberInFile, data.file ) ); // Store it in the
          // line buffer...
        } else {
          return false;
        }
      } else { // don't checkFilterRow

        if ( !meta.noEmptyLines() || line.length() != 0 ) {
          data.lineBuffer.add( new TextFileLine( line, lineNumberInFile, data.file ) ); // Store it in the line
                                                                                        // buffer...
        }
      }
    } else {
      data.doneReading = true;
    }
    return true;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TextFileInputMeta) smi;
    data = (TextFileInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      initErrorHandling();
      initReplayFactory();

      data.setFiles( meta.getTextFileList( getTransMeta().getBowl(), this ) );
      data.filterProcessor = new TextFileFilterProcessor( meta.getFilter() );

      // If there are missing files,
      // fail if we don't ignore errors
      //
      Result previousResult = getTrans().getPreviousResult();
      Map<String, ResultFile> resultFiles = ( previousResult != null ) ? previousResult.getResultFiles() : null;

      if ( ( previousResult == null || resultFiles == null || resultFiles.size() == 0 )
         && data.getFiles().nrOfMissingFiles() > 0 && !meta.isAcceptingFilenames() && !meta.isErrorIgnored() ) {
        logError( BaseMessages.getString( PKG, "TextFileInput.Log.Error.NoFilesSpecified" ) );
        return false;
      }

      String clusterSize = getVariable( Const.INTERNAL_VARIABLE_CLUSTER_SIZE );
      if ( !Utils.isEmpty( clusterSize ) && Integer.valueOf( clusterSize ) > 1 ) {
        // TODO: add metadata to configure this.
        String nr = getVariable( Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER );
        if ( log.isDetailed() ) {
          logDetailed( "Running on slave server #" + nr
              + " : assuming that each slave reads a dedicated part of the same file(s)." );
        }
      }

      // If no nullif field is supplied, take the default.
      // String null_value = nullif;
      // if (null_value == null)
      // {
      // // value="";
      // }
      // String null_cmp = Const.rightPad(new StringBuilder(null_value), pol.length());

      // calculate the file format type in advance so we can use a switch
      data.fileFormatType = meta.getFileFormatTypeNr();

      // calculate the file type in advance CSV or Fixed?
      data.fileType = meta.getFileTypeNr();

      // Handle the possibility of a variable substitution
      data.separator = environmentSubstitute( meta.getSeparator() );
      data.enclosure = environmentSubstitute( meta.getEnclosure() );
      data.escapeCharacter = environmentSubstitute( meta.getEscapeCharacter() );

      // Add additional fields
      if ( !Utils.isEmpty( meta.getShortFileNameField() ) ) {
        data.addShortFilename = true;
      }
      if ( !Utils.isEmpty( meta.getPathField() ) ) {
        data.addPath = true;
      }
      if ( !Utils.isEmpty( meta.getExtensionField() ) ) {
        data.addExtension = true;
      }
      if ( !Utils.isEmpty( meta.getSizeField() ) ) {
        data.addSize = true;
      }
      if ( !Utils.isEmpty( meta.isHiddenField() ) ) {
        data.addIsHidden = true;
      }
      if ( !Utils.isEmpty( meta.getLastModificationDateField() ) ) {
        data.addLastModificationDate = true;
      }
      if ( !Utils.isEmpty( meta.getUriField() ) ) {
        data.addUri = true;
      }
      if ( !Utils.isEmpty( meta.getRootUriField() ) ) {
        data.addRootUri = true;
      }
      return true;
    }
    return false;
  }

  private void initReplayFactory() {
    Date replayDate = getTrans().getReplayDate();
    if ( replayDate == null ) {
      data.filePlayList = FilePlayListAll.INSTANCE;
    } else {
      data.filePlayList =
          new FilePlayListReplay( replayDate, meta.getLineNumberFilesDestinationDirectory(), meta
              .getLineNumberFilesExtension(), meta.getErrorFilesDestinationDirectory(), meta
              .getErrorLineFilesExtension(), meta.getEncoding() );
    }
  }

  private void initErrorHandling() {
    List<FileErrorHandler> dataErrorLineHandlers = new ArrayList<FileErrorHandler>( 2 );
    if ( meta.getLineNumberFilesDestinationDirectory() != null ) {
      dataErrorLineHandlers
        .add( new FileErrorHandlerContentLineNumber( getTrans().getCurrentDate(), environmentSubstitute( meta
          .getLineNumberFilesDestinationDirectory() ), meta.getLineNumberFilesExtension(), meta.getEncoding(), this ) );
    }
    if ( meta.getErrorFilesDestinationDirectory() != null ) {
      dataErrorLineHandlers.add( new FileErrorHandlerMissingFiles( getTrans().getCurrentDate(), environmentSubstitute(
        meta.getErrorFilesDestinationDirectory() ), meta.getErrorLineFilesExtension(), meta.getEncoding(), this ) );
    }
    data.dataErrorLineHandler = new CompositeFileErrorHandler( dataErrorLineHandlers );
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TextFileInputMeta) smi;
    data = (TextFileInputData) sdi;

    if ( data.file != null ) {
      try {
        data.file.close();
        data.file = null;
      } catch ( Exception e ) {
        log.logError( "Error closing file", e );
      }
    }
    if ( data.in != null ) {
      BaseStep.closeQuietly( data.in );
      data.in = null;
    }
    super.dispose( smi, sdi );
  }

  public boolean isWaitingForData() {
    return true;
  }
}
