/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.fileinput.text;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringEvaluationResult;
import org.pentaho.di.core.util.StringEvaluator;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;

/**
 * BaseFileImportProcessor is an abstract class that provides a framework for processing csv file to extract file
 * summary and fields data
 * It defines methods for analyzing file data, converting lines to rows, and managing metadata.
 * Subclasses must implement the abstract methods to provide specific functionality.
 */
public abstract class BaseFileImportProcessor {

  private static final Class<?> PKG = TextFileInputMeta.class;

  protected Object meta;
  protected Object[] inputFields;
  protected int samples;
  protected boolean showSummary;
  protected boolean replaceMeta;
  protected String message;
  protected String debug;
  protected long rowNumber;
  protected BufferedInputStreamReader reader;
  protected TransMeta transMeta;
  protected LogChannelInterface log;
  protected EncodingType encodingType;

  /**
   * Constructor for BaseFileImportProcessor.
   *
   * @param meta        the metadata object
   * @param transMeta   the transformation metadata
   * @param reader      the file reader
   * @param samples     the number of samples to process
   * @param showSummary whether to show a summary of the analysis
   */
  protected BaseFileImportProcessor( Object meta, TransMeta transMeta, BufferedInputStreamReader reader, int samples,
                                     boolean showSummary
  ) {
    this.meta = meta;
    this.reader = reader;
    this.samples = samples;
    this.showSummary = showSummary;
    this.replaceMeta = true;
    this.transMeta = transMeta;
    this.message = null;
    this.debug = "init";
    this.rowNumber = 1L;
    this.log = new LogChannel( transMeta );
    this.encodingType = EncodingType.guessEncodingType( reader.getEncoding() );
  }

  /**
   * Returns the number of fields in the file.
   *
   * @return the field count
   */
  protected abstract int getFieldCount();

  /**
   * Converts a line from the file into a row of data.
   *
   * @param textFileLine     the line of text to convert
   * @param strinfo          metadata information
   * @param outputRowMeta    the output row metadata
   * @param convertRowMeta   the converted row metadata
   * @param failOnParseError whether to fail on parse errors
   * @return the converted row as an object array
   * @throws KettleException if an error occurs during conversion
   */
  protected abstract Object[] convertLineToRow( TextFileLine textFileLine, Object strinfo,
                                                RowMetaInterface outputRowMeta,
                                                RowMetaInterface convertRowMeta,
                                                boolean failOnParseError ) throws KettleException;

  /**
   * Initializes a field with the given decimal format symbols.
   *
   * @param field the field to initialize
   * @param dfs   the decimal format symbols
   */
  protected abstract void initializeField( Object field, DecimalFormatSymbols dfs );

  /**
   * Sets type information for a field based on evaluation results.
   *
   * @param field               the field to set type information for
   * @param evaluator           the string evaluator
   * @param evaluationResults   the list of evaluation results
   * @param strEvaluationResult the advised evaluation result
   */
  protected abstract void setFieldTypeInfo( Object field, StringEvaluator evaluator,
                                            List<StringEvaluationResult> evaluationResults,
                                            StringEvaluationResult strEvaluationResult );

  /**
   * Retrieves the name of a field.
   *
   * @param field the field to retrieve the name for
   * @return the field name
   */
  protected abstract String getFieldName( Object field );

  /**
   * Retrieves the type description of a field.
   *
   * @param field the field to retrieve the type description for
   * @return the field type description
   */
  protected abstract String getFieldTypeDesc( Object field );

  /**
   * Retrieves the type of a field.
   *
   * @param field the field to retrieve the type for
   * @return the field type
   */
  protected abstract int getFieldType( Object field );

  /**
   * Retrieves the length of a field.
   *
   * @param field the field to retrieve the length for
   * @return the field length
   */
  protected abstract int getFieldLength( Object field );

  /**
   * Retrieves the precision of a field.
   *
   * @param field the field to retrieve the precision for
   * @return the field precision
   */
  protected abstract int getFieldPrecision( Object field );

  /**
   * Retrieves the format of a field.
   *
   * @param field the field to retrieve the format for
   * @return the field format
   */
  protected abstract String getFieldFormat( Object field );

  /**
   * Clones the metadata object.
   *
   * @return the cloned metadata object
   */
  protected abstract Object cloneMeta();

  /**
   * Sets all fields in the metadata to string type.
   *
   * @param meta the metadata object
   */
  protected abstract void setAllFieldsToStringType( Object meta );

  /**
   * Retrieves a field from the metadata at the specified index.
   *
   * @param meta  the metadata object
   * @param index the index of the field
   * @return the field object
   */
  protected abstract Object getField( Object meta, int index );

  /**
   * Checks if the file has a header.
   *
   * @return true if the file has a header, false otherwise
   */
  protected abstract boolean hasHeader();

  /**
   * Retrieves the number of header lines in the file.
   *
   * @return the number of header lines
   */
  protected abstract int getHeaderLines();

  /**
   * Retrieves the enclosure character used in the file.
   *
   * @return the enclosure character
   */
  protected abstract String getEnclosure();

  /**
   * Retrieves the escape character used in the file.
   *
   * @return the escape character
   */
  protected abstract String getEscapeCharacter();

  /**
   * Retrieves the file format type number.
   *
   * @return the file format type number
   */
  protected abstract int getFileFormatTypeNr();

  /**
   * Populates the row metadata with field information.
   *
   * @param rowMeta the row metadata to populate
   * @throws KettleStepException if an error occurs while populating the metadata
   */
  protected abstract void getFields( RowMetaInterface rowMeta ) throws KettleStepException;

  /**
   * Converts a field to a Data Transfer Object (DTO).
   *
   * @param field the field to convert
   * @return the field DTO
   */
  protected abstract TextFileInputFieldDTO convertFieldToDto( Object field );

  /**
   * Analyzes the file and generates a summary message.
   *
   * @param failOnParseError whether to fail on parse errors
   * @return the summary message
   * @throws KettleException if an error occurs during analysis
   */
  public String analyzeFile( boolean failOnParseError ) throws KettleException {
    long fileLineNumber = 0;
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    int nrfields = getFieldCount();

    RowMetaInterface outputRowMeta = new RowMeta();
    getFields( outputRowMeta );

    // Remove the storage meta-data (don't go for lazy conversion during scan)
    for ( ValueMetaInterface valueMeta : outputRowMeta.getValueMetaList() ) {
      valueMeta.setStorageMetadata( null );
      valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    }

    RowMetaInterface convertRowMeta = outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
    FieldAnalysis[] fieldAnalyses = initializeFieldAnalyses( nrfields, dfs );

    Object strInfo = cloneMeta();
    setAllFieldsToStringType( strInfo );

    StringBuilder lineBuffer = new StringBuilder( 256 );
    int fileFormatType = getFileFormatTypeNr();

    if ( hasHeader() ) {
      fileLineNumber = TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType, lineBuffer,
        getHeaderLines(), getEnclosure(), getEscapeCharacter(), fileLineNumber );
    }

    //Reading the first line of data
    String line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineBuffer,
      getEnclosure(), getEscapeCharacter() );
    int linenr = 1;

    List<StringEvaluator> evaluators = new ArrayList<>();
    boolean errorFound = false;

    while ( !errorFound && line != null && ( linenr <= samples || samples == 0 ) ) {
      RowMetaInterface rowMeta = new RowMeta();
      getFields( rowMeta );

      for ( ValueMetaInterface valueMeta : rowMeta.getValueMetaList() ) {
        valueMeta.setStorageMetadata( null );
        valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
      }

      String enclosure = transMeta.environmentSubstitute( getEnclosure() );
      String escapeCharacter = transMeta.environmentSubstitute( getEscapeCharacter() );

      Object[] r = convertLineToRow( new TextFileLine( line, fileLineNumber, null ), strInfo, outputRowMeta,
        convertRowMeta, failOnParseError );

      if ( r == null ) {
        errorFound = true;
        continue;
      }

      rowNumber++;
      processRowData( nrfields, evaluators, rowMeta, r, failOnParseError );
      linenr++;

      TextFileLine textFileLine = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType,
        lineBuffer, enclosure, escapeCharacter, fileLineNumber );
      line = textFileLine.getLine();
      fileLineNumber = textFileLine.getLineNumber();
    }

    message = generateFieldAnalysisMessage( nrfields, evaluators, linenr, fieldAnalyses );

    return message;
  }

  /**
   * Initializes an array of `FieldAnalysis` objects for the specified number of fields.
   * <p>
   * This method creates and initializes `FieldAnalysis` objects for each field in the metadata.
   * It also initializes the fields with the provided decimal format symbols if `replaceMeta` is true.
   * The initialized fields are stored in the `inputFields` array.
   * </p>
   *
   * @param nrfields the number of fields to initialize
   * @param dfs      the decimal format symbols to use for field initialization
   * @return an array of `FieldAnalysis` objects, one for each field
   */
  private FieldAnalysis[] initializeFieldAnalyses( int nrfields, DecimalFormatSymbols dfs ) {
    FieldAnalysis[] fieldAnalyses = new FieldAnalysis[ nrfields ];

    for ( int i = 0; i < nrfields; i++ ) {
      Object field = getField( meta, i );

      if ( replaceMeta ) {
        initializeField( field, dfs );
      }

      fieldAnalyses[ i ] = new FieldAnalysis();
      inputFields[ i ] = field;
    }
    return fieldAnalyses;
  }

  /**
   * Processes a row of data by evaluating each field in the row.
   * <p>
   * This method iterates through the fields in the row, evaluates their string values,
   * and updates the corresponding `StringEvaluator` objects. If a field's value cannot
   * be retrieved and `failOnParseError` is true, an exception is thrown.
   * </p>
   *
   * @param nrfields         the number of fields in the row
   * @param evaluators       a list of `StringEvaluator` objects used to evaluate field data
   * @param rowMeta          the metadata describing the structure of the row
   * @param r                the row data as an array of objects
   * @param failOnParseError whether to throw an exception on parsing errors
   * @throws KettleException if a parsing error occurs and `failOnParseError` is true
   */
  private void processRowData( int nrfields, List<StringEvaluator> evaluators,
                               RowMetaInterface rowMeta, Object[] r, boolean failOnParseError ) throws KettleException {
    for ( int i = 0; i < nrfields && i < r.length; i++ ) {
      StringEvaluator evaluator;
      if ( i >= evaluators.size() ) {
        evaluator = new StringEvaluator( true );
        evaluators.add( evaluator );
      } else {
        evaluator = evaluators.get( i );
      }

      String string = getStringFromRow( rowMeta, r, i, failOnParseError );
      evaluator.evaluateString( string );
    }
  }

  /**
   * Evaluates fields and generates a summary message based on the analysis of the file's fields.
   * <p>
   * This method processes the results of field evaluations and constructs a detailed summary
   * message. The summary includes information about each field, such as its name, type, length,
   * precision, format, and analysis results (e.g., null values, min/max values). It also handles
   * specific field types (e.g., number, string, date) and appends additional details based on the type.
   * </p>
   *
   * @param nrfields      the number of fields in the file
   * @param evaluators    a list of `StringEvaluator` objects used to evaluate field data
   * @param linenr        the total number of lines processed
   * @param fieldAnalyses an array of `FieldAnalysis` objects containing analysis results for each field
   * @return a `String` containing the generated summary message
   */
  private String generateFieldAnalysisMessage( int nrfields, List<StringEvaluator> evaluators,
                                               int linenr, FieldAnalysis[] fieldAnalyses ) {
    StringBuilder summary = new StringBuilder();
    if ( showSummary ) {
      summary.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.ResultAfterScanning",
        String.valueOf( linenr - 1 ) ) );
      summary.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.HorizontalLine" ) );
    }

    if ( nrfields == evaluators.size() ) {
      for ( int i = 0; i < nrfields; i++ ) {
        Object field = getField( meta, i );
        StringEvaluator evaluator = evaluators.get( i );
        List<StringEvaluationResult> evaluationResults = evaluator.getStringEvaluationResults();
        StringEvaluationResult strEvalResult = evaluator.getAdvicedResult();
        setFieldTypeInfo( field, evaluator, evaluationResults, strEvalResult );

        if ( strEvalResult != null ) {
          fieldAnalyses[ i ].nrnull = strEvalResult.getNrNull();
          fieldAnalyses[ i ].minstr = Objects.toString( strEvalResult.getMin(), Const.EMPTY_STRING );
          fieldAnalyses[ i ].maxstr = Objects.toString( strEvalResult.getMax(), Const.EMPTY_STRING );
        }
        if ( showSummary ) {
          appendFieldInfoToMessage( summary, i, field, evaluationResults, fieldAnalyses[ i ], linenr );
        }
      }
    }
    return summary.toString();
  }

  /**
   * Appends detailed information about a field to the provided message.
   * <p>
   * This method generates a summary of the field's metadata and analysis results,
   * including its name, type, length, precision, format, and null value statistics.
   * It also handles specific field types (e.g., number, string, date) and appends
   * additional details based on the type.
   * </p>
   *
   * @param message           the `StringBuilder` to append the field information to
   * @param fieldIndex        the index of the field being processed
   * @param field             the field object containing metadata about the field
   * @param evaluationResults the list of evaluation results for the field
   * @param fieldAnalysis     the analysis results for the field, including null count, min, and max values
   * @param linenr            the total number of lines processed
   */
  private void appendFieldInfoToMessage( StringBuilder message, int fieldIndex, Object field,
                                         List<StringEvaluationResult> evaluationResults,
                                         FieldAnalysis fieldAnalysis, int linenr ) {
    message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.FieldNumber",
      String.valueOf( fieldIndex + 1 ) ) );
    message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.FieldName",
      getFieldName( field ) ) );
    message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.FieldType",
      getFieldTypeDesc( field ) ) );

    switch ( getFieldType( field ) ) {
      case ValueMetaInterface.TYPE_NUMBER:
        appendNumberFieldInfo( message, field, evaluationResults, fieldAnalysis );
        break;
      case ValueMetaInterface.TYPE_STRING:
        appendStringFieldInfo( message, field, fieldAnalysis );
        break;
      case ValueMetaInterface.TYPE_DATE:
        appendDateFieldInfo( message, field, fieldAnalysis );
        break;
      default:
        break;
    }

    if ( fieldAnalysis.nrnull == linenr - 1 ) {
      message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.AllNullValues" ) );
    }
    message.append( Const.CR );
  }

  /**
   * Appends information about a numeric field to the provided message.
   * <p>
   * This method generates details about the numeric field, including its estimated length, precision,
   * format, minimum and maximum values, and the number of null values. It also evaluates the field
   * using various number formats and appends the results to the provided `StringBuilder` object.
   * </p>
   *
   * @param message           the `StringBuilder` to append the field information to
   * @param field             the field object containing metadata about the numeric field
   * @param evaluationResults the list of evaluation results for the field
   * @param fieldAnalysis     the analysis results for the field, including null count, min, and max values
   */
  private void appendNumberFieldInfo( StringBuilder message, Object field,
                                      List<StringEvaluationResult> evaluationResults, FieldAnalysis fieldAnalysis ) {
    DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance();
    DecimalFormatSymbols dfs2 = new DecimalFormatSymbols();

    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.EstimatedLength",
      ( getFieldLength( field ) < 0 ? "-" : String.valueOf( getFieldLength( field ) ) ) ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.EstimatedPrecision",
      getFieldPrecision( field ) < 0 ? "-" : String.valueOf( getFieldPrecision( field ) ) ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.NumberFormat", getFieldFormat( field ) ) );

    if ( !evaluationResults.isEmpty() ) {
      if ( evaluationResults.size() > 1 ) {
        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.WarnNumberFormat" ) );
      }

      for ( StringEvaluationResult seResult : evaluationResults ) {
        String mask = seResult.getConversionMeta().getConversionMask();

        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.NumberFormat2", mask ) );
        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.TrimType",
          seResult.getConversionMeta().getTrimType() ) );
        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.NumberMinValue", seResult.getMin() ) );
        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.NumberMaxValue", seResult.getMax() ) );

        try {
          df2.applyPattern( mask );
          df2.setDecimalFormatSymbols( dfs2 );
          double mn = df2.parse( seResult.getMin().toString() ).doubleValue();
          message.append( BaseMessages.getString(
            PKG, "TextFileCSVImportProgressDialog.Info.NumberExample",
            mask, seResult.getMin(), Double.toString( mn ) ) );
        } catch ( Exception e ) {
          if ( log.isDetailed() ) {
            log.logDetailed( "This is unexpected: parsing [" + seResult.getMin() +
              "] with format [" + mask + "] did not work." );
          }
        }
      }
    }
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.NumberNrNullValues", String.valueOf( fieldAnalysis.nrnull ) ) );
  }

  /**
   * Appends information about a string field to the provided message.
   * <p>
   * This method generates details about the string field, including its maximum length,
   * minimum and maximum values, and the number of null values. The information is appended
   * to the provided `StringBuilder` object.
   * </p>
   *
   * @param message       the `StringBuilder` to append the field information to
   * @param field         the field object containing metadata about the string field
   * @param fieldAnalysis the analysis results for the field, including null count, min, and max values
   */
  private void appendStringFieldInfo( StringBuilder message, Object field, FieldAnalysis fieldAnalysis ) {
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringMaxLength", String.valueOf( getFieldLength( field ) ) ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringMinValue", fieldAnalysis.minstr ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringMaxValue", fieldAnalysis.maxstr ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringNrNullValues", String.valueOf( fieldAnalysis.nrnull ) ) );
  }

  /**
   * Appends information about a date field to the provided message.
   * <p>
   * This method generates details about the date field, including its maximum length, format,
   * minimum and maximum values, and the number of null values. It also attempts to parse
   * the minimum value using various date formats to provide examples.
   * </p>
   *
   * @param message       the `StringBuilder` to append the field information to
   * @param field         the field object containing metadata about the date field
   * @param fieldAnalysis the analysis results for the field, including null count, min, and max values
   */
  private void appendDateFieldInfo( StringBuilder message, Object field, FieldAnalysis fieldAnalysis ) {
    SimpleDateFormat daf2 = new SimpleDateFormat();

    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.DateMaxLength",
      getFieldLength( field ) < 0 ? "-" : String.valueOf( getFieldLength( field ) ) ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.DateFormat", getFieldFormat( field ) ) );

    if ( !Utils.isEmpty( fieldAnalysis.minstr ) ) {
      for ( int x = 0; x < Const.getDateFormats().length; x++ ) {
        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.DateFormat2", Const.getDateFormats()[ x ] ) );
        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.DateMinValue", fieldAnalysis.minstr ) );
        message.append( BaseMessages.getString(
          PKG, "TextFileCSVImportProgressDialog.Info.DateMaxValue", fieldAnalysis.maxstr ) );

        daf2.applyPattern( Const.getDateFormats()[ x ] );
        try {
          Date md = daf2.parse( fieldAnalysis.minstr );
          message.append( BaseMessages.getString(
            PKG, "TextFileCSVImportProgressDialog.Info.DateExample",
            Const.getDateFormats()[ x ], fieldAnalysis.minstr, md.toString() ) );
        } catch ( Exception e ) {
          if ( log.isDetailed() ) {
            log.logDetailed( "This is unexpected: parsing [" + fieldAnalysis.minstr +
              "] with format [" + Const.getDateFormats()[ x ] + "] did not work." );
          }
        }
      }
    }
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.DateNrNullValues", String.valueOf( fieldAnalysis.nrnull ) ) );
  }

  /**
   * Retrieves a string value from a row at the specified index.
   * <p>
   * This method attempts to extract a string value from the given row using the provided row metadata.
   * If an exception occurs during the extraction and `failOnParseError` is true, the exception is thrown.
   * If `failOnParseError` is false, the method will attempt to retrieve the string value directly from the row object.
   *
   * @param rowMeta          the metadata describing the structure of the row
   * @param row              the row data as an array of objects
   * @param index            the index of the field to retrieve
   * @param failOnParseError whether to throw an exception on parsing errors
   * @return the string value of the field at the specified index, or null if the value cannot be retrieved
   * @throws KettleException if a parsing error occurs and `failOnParseError` is true
   */
  private String getStringFromRow( final RowMetaInterface rowMeta, final Object[] row, final int index,
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

    // if 'failOnParseError' is false, or there is no exceptionotherwise, we get the string value straight from the row
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
   * Retrieves the generated summary message.
   *
   * @return the summary message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Retrieves the input fields as an array of objects.
   *
   * @return the input fields
   */
  public Object[] getInputFields() {
    return inputFields;
  }

  /**
   * Retrieves the input fields as an array of Data Transfer Objects (DTOs).
   *
   * @return the input field DTOs
   */
  public TextFileInputFieldDTO[] getInputFieldsDto() {
    TextFileInputFieldDTO[] inputFieldDtos = new TextFileInputFieldDTO[ getInputFields().length ];
    for ( int i = 0; i < getInputFields().length; i++ ) {
      inputFieldDtos[ i ] = convertFieldToDto( getInputFields()[ i ] );
    }
    return inputFieldDtos;
  }

  static class FieldAnalysis {
    int nrnull;
    String minstr;
    String maxstr;
  }

}
