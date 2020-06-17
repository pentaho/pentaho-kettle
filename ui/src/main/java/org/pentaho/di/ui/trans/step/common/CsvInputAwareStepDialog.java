/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step.common;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInput;
import org.pentaho.di.trans.steps.fileinput.text.EncodingType;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * A common interface for all step dialogs aware of the csv input format, such as {@link
 * org.pentaho.di.ui.trans.steps.csvinput.CsvInputDialog} and
 * {@link org.pentaho.di.ui.trans.steps.fileinput.text.TextFileInputDialog}
 */
public interface CsvInputAwareStepDialog {

  /**
   * @See {@link GetFieldsCapableStepDialog#getFieldNames(org.pentaho.di.trans.step.BaseStepMeta)}
   */
  default String[] getFieldNames( final CsvInputAwareMeta meta ) {
    String[] fieldNames = new String[] {};
    final InputStream inputStream = getInputStream( meta );
    final InputStreamReader reader = getReader( meta, inputStream );
    try {
      fieldNames = getFieldNamesImpl( reader, meta );
    } catch ( final KettleException e ) {
      logError( BaseMessages.getString( "Dialog.ErrorGettingFields.Message" ), e );
    } finally {
      try {
        inputStream.close();
      } catch ( Exception e ) {
        // Ignore close errors
      }
    }
    return fieldNames;
  }

  default String[] getFieldNamesImpl( final InputStreamReader reader, final CsvInputAwareMeta meta )
    throws KettleException {

    String[] fieldNames = new String[] {};
    if ( reader == null || meta == null ) {
      logError( BaseMessages.getString( "Dialog.ErrorGettingFields.Message" ) );
      return fieldNames;
    }
    final String delimiter = getTransMeta().environmentSubstitute( meta.getDelimiter() );
    final String enclosure = getTransMeta().environmentSubstitute( meta.getEnclosure() );

    final EncodingType encodingType = EncodingType.guessEncodingType( reader.getEncoding() );

    // Read a line of data to determine the number of rows...
    final String line = TextFileInputUtils.getLine( getLogChannel(), reader, encodingType, meta.getFileFormatTypeNr(),
      new StringBuilder( 1000 ), enclosure );
    if ( !StringUtils.isBlank( line ) ) {
      fieldNames = CsvInput.guessStringsFromLine( getLogChannel(), line, delimiter, enclosure,
        meta.getEscapeCharacter() );
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
   * Returns the {@link InputStream} corresponding to the csv file, or null if the file cannot be read.
   *
   * @return the {@link InputStream} corresponding to the csv file, or null if the file cannot be read
   */
  InputStream getInputStream( final CsvInputAwareMeta meta );

  /**
   * Returns the {@link InputStreamReader} corresponding to the csv file, or null if the file cannot be read.
   *
   * @return the {@link InputStreamReader} corresponding to the csv file, or null if the file cannot be read
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
   * @See {@link GetFieldsCapableStepDialog#loadFieldsImpl(BaseStepMeta, int)}
   */
  default String loadFieldsImpl( final CsvInputAwareMeta meta, final int samples ) {

    InputStream inputStream = getInputStream( meta );
    try {
      final InputStreamReader reader = getReader( meta, inputStream );
      final CsvInputAwareImportProgressDialog pd = getCsvImportProgressDialog( meta, samples, reader );
      String message = pd.open( false );
      return message;
    } finally {
      try {
        inputStream.close();
      } catch ( Exception e ) {
        // Ignore close errors
      }
    }
  }

  CsvInputAwareImportProgressDialog getCsvImportProgressDialog(
    final CsvInputAwareMeta meta, final int samples, final InputStreamReader reader );

  default void logError( final String message, final Exception exception ) {
    getLogChannel().logError( message, exception );
  }

  default void logError( final String message ) {
    getLogChannel().logError( message );
  }

  LogChannel getLogChannel();

  TransMeta getTransMeta();
}
