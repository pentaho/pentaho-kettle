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


package org.pentaho.di.trans.steps.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInput;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.fileinput.text.EncodingType;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputFieldDTO;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputUtils;

public interface CsvInputAwareStep {

  /**
   * Retrieves the field names from the CSV input metadata.
   *
   * @param meta the CSV input metadata
   * @return an array of field names
   */
  default String[] getFieldNames( final CsvInputAwareMeta meta ) {
    String[] fieldNames = new String[] {};
    try ( InputStream inputStream = getInputStream( meta ) ) {
      final BufferedInputStreamReader reader = getBufferedReader( meta, inputStream );
      fieldNames = getFieldNamesImpl( reader, meta );
    } catch ( final KettleException | IOException e ) {
      logError( BaseMessages.getString( "Dialog.ErrorGettingFields.Message" ), e );
    }
    return fieldNames;
  }

  /**
   * Retrieves the field names from the CSV input metadata using the provided reader.
   *
   * @param reader the buffered input stream reader for reading the CSV file
   * @param meta   the CSV input metadata
   * @return an array of field names
   * @throws KettleException if an error occurs while retrieving the field names
   */
  default String[] getFieldNamesImpl( final BufferedInputStreamReader reader, final CsvInputAwareMeta meta )
    throws KettleException {

    String[] fieldNames = new String[] {};
    if ( reader == null || meta == null ) {
      logError( BaseMessages.getString( "Dialog.ErrorGettingFields.Message" ) );
      return fieldNames;
    }
    final String delimiter = getTransMeta().environmentSubstitute( meta.getDelimiter() );
    final String enclosure = getTransMeta().environmentSubstitute( meta.getEnclosure() );
    final String escapeCharacter = getTransMeta().environmentSubstitute( meta.getEscapeCharacter() );

    final EncodingType encodingType = EncodingType.guessEncodingType( reader.getEncoding() );

    // Read a line of data to determine the number of rows...
    final String line = TextFileInputUtils.getLine( logChannel(), reader, encodingType, meta.getFileFormatTypeNr(),
      new StringBuilder( 1000 ), enclosure, escapeCharacter );
    if ( !StringUtils.isBlank( line ) ) {
      if ( meta instanceof TextFileInputMeta ) {
        fieldNames = TextFileInputUtils.guessStringsFromLine( getTransMeta().getParentVariableSpace(), logChannel(),
          line, (TextFileInputMeta) meta, delimiter, enclosure, meta.getEscapeCharacter() );
      } else {
        fieldNames = CsvInput.guessStringsFromLine( logChannel(), line, delimiter, enclosure,
          meta.getEscapeCharacter() );
      }
    }
    if ( Utils.isEmpty( fieldNames ) ) {
      logError( BaseMessages.getString( "Dialog.ErrorGettingFields.Message" ) );
      return fieldNames;
    }

    // Massage field names
    for ( int i = 0; i < fieldNames.length; i++ ) {
      fieldNames[ i ] = Const.trim( fieldNames[ i ] );
      if ( !meta.hasHeader() ) {
        final DecimalFormat df = new DecimalFormat( "000" );
        fieldNames[ i ] = "Field_" + df.format( i );
      } else if ( !Utils.isEmpty( meta.getEnclosure() ) && fieldNames[ i ].startsWith( meta.getEnclosure() )
        && fieldNames[ i ].endsWith( meta.getEnclosure() ) && fieldNames[ i ].length() > 1 ) {
        fieldNames[ i ] = fieldNames[ i ].substring( 1, fieldNames[ i ].length() - 1 );
      }
      // trim again, now that the enclosure characters have been removed
      fieldNames[ i ] = Const.trim( fieldNames[ i ] );
      fieldNames[ i ] = massageFieldName( fieldNames[ i ] );
    }
    return fieldNames;
  }

  /**
   * Custom handling of each field can be implemented here.
   */
  default String massageFieldName( final String fieldName ) {
    return fieldName;
  }

  /**
   * Returns the {@link InputStreamReader} corresponding to the csv file, or null if the file cannot be read.
   *
   * @return the {@link InputStreamReader} corresponding to the csv file, or null if the file cannot be read.
   */
  default InputStreamReader getReader( final CsvInputAwareMeta meta, final InputStream inputStream ) {
    InputStreamReader reader = null;
    try {
      String realEncoding = getTransMeta().environmentSubstitute( meta.getEncoding() );
      if ( Utils.isEmpty( realEncoding ) ) {
        reader = new InputStreamReader( inputStream );
      } else {
        reader = new InputStreamReader( inputStream, realEncoding );
      }
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( "Dialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return reader;
  }

  /**
   * Converts an array of TextFileInputFieldDTO objects into a JSON array.
   *
   * @param textFileInputFields an array of TextFileInputFieldDTO objects to be converted
   * @return a JSONArray containing the JSON representation of the input fields
   * @throws JsonProcessingException if an error occurs during JSON processing
   */
  default JSONArray convertFieldsToJsonArray( TextFileInputFieldDTO[] textFileInputFields )
    throws JsonProcessingException {
    JSONArray jsonArray = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();
    for ( TextFileInputFieldDTO field : textFileInputFields ) {
      jsonArray.add( objectMapper.readTree( objectMapper.writeValueAsString( field ) ) );
    }

    return jsonArray;
  }

  /**
   * When {@code failOnParseError} is set to {@code false}, returns the {@link String} value from {@link
   * org.pentaho.di.core.row.RowMeta} at the given {@code index}, or directly from the {@code row} object, if there is a
   * problem fetching the value from {@link org.pentaho.di.core.row.RowMeta}. When {@code failOnParseError} is {@code
   * true}, any {@link Exception} thrown by the call to {@link org.pentaho.di.core.row.RowMeta#getString(Object[], int)}
   * is reported back to the caller.
   *
   * @param rowMeta          an instance of {@link RowMetaInterface}
   * @param row              an Object array containing row data
   * @param index            the index representing the column in a row
   * @param failOnParseError when true, Exceptions are reported back to the called, when false, exceptions are ignored
   *                         and a null value is returned
   * @return the row value at the given index
   */
  default String getStringFromRow( final RowMetaInterface rowMeta, final Object[] row, final int index,
                                   final boolean failOnParseError ) throws KettleException {
    String string = null;
    Exception exc = null;
    try {
      string = rowMeta.getString( row, index );
    } catch ( final Exception e ) {
      exc = e;
    }

    // if 'failOnParseError' is true, and we caught an exception, we either re-throw the exception, or wrap its as a
    // KettleException, if it isn't one already
    if ( failOnParseError ) {
      if ( exc instanceof KettleException ) {
        throw (KettleException) exc;
      } else if ( exc != null ) {
        throw new KettleException( exc );
      }
    }

    // if 'failOnParseError' is false, or there is no exception otherwise, we get the string value straight from the row
    // object
    if ( string == null ) {
      if ( ( row.length <= index ) && failOnParseError ) {
        throw new KettleException( new NullPointerException() );
      }
      string = row.length <= index || row[ index ] == null ? null : row[ index ].toString();
    }

    return string;
  }

  /**
   * Creates a buffered input stream reader for the given CSV input metadata and input stream.
   *
   * @param meta        the CSV input metadata
   * @param inputStream the input stream to read from
   * @return a BufferedInputStreamReader for reading the CSV file
   */
  default BufferedInputStreamReader getBufferedReader( final CsvInputAwareMeta meta, final InputStream inputStream ) {
    return new BufferedInputStreamReader( getReader( meta, inputStream ) );
  }

  /**
   * Logs an error message along with an exception.
   *
   * @param message   the error message to log
   * @param exception the exception to log
   */
  default void logError( final String message, final Exception exception ) {
    logChannel().logError( message, exception );
  }

  /**
   * Logs an error message.
   *
   * @param message the error message to log
   */
  default void logError( final String message ) {
    logChannel().logError( message );
  }

  /**
   * Returns the {@link InputStream} corresponding to the csv file, or null if the file cannot be read.
   *
   * @return the {@link InputStream} corresponding to the csv file, or null if the file cannot be read
   */
  InputStream getInputStream( final CsvInputAwareMeta meta );

  LogChannelInterface logChannel();

  TransMeta getTransMeta();

}
