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

public abstract class BaseFileImportProcessor {

  private static Class<?> PKG = TextFileInputMeta.class;

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

  protected abstract int getFieldCount();

  protected abstract Object[] convertLineToRow( TextFileLine textFileLine, Object strinfo,
                                                RowMetaInterface outputRowMeta,
                                                RowMetaInterface convertRowMeta,
                                                boolean failOnParseError ) throws KettleException;

  protected abstract void initializeField( Object field, DecimalFormatSymbols dfs );

  protected abstract void setFieldTypeInfo( Object field, StringEvaluator evaluator,
                                            List<StringEvaluationResult> evaluationResults,
                                            StringEvaluationResult strEvaluationResult );

  protected abstract String getFieldName( Object field );

  protected abstract String getFieldTypeDesc( Object field );

  protected abstract int getFieldType( Object field );

  protected abstract int getFieldLength( Object field );

  protected abstract int getFieldPrecision( Object field );

  protected abstract String getFieldFormat( Object field );

  protected abstract Object cloneMeta();

  protected abstract void setAllFieldsToStringType( Object meta );

  protected abstract Object getField( Object meta, int index );

  protected abstract boolean hasHeader();

  protected abstract int getHeaderLines();

  protected abstract String getEnclosure();

  protected abstract String getEscapeCharacter();

  protected abstract String getSeparator();

  protected abstract int getFileFormatTypeNr();

  protected abstract void getFields( RowMetaInterface rowMeta ) throws KettleStepException;

  protected abstract TextFileInputFieldDTO convertFieldToDto( Object field );

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

    if ( showSummary ) {
      message = generateResultsMessage( nrfields, evaluators, linenr, fieldAnalyses );
    }
    return message;
  }

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

  private String generateResultsMessage( int nrfields, List<StringEvaluator> evaluators,
                                         int linenr, FieldAnalysis[] fieldAnalyses ) {
    StringBuilder summary = new StringBuilder();
    summary.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.ResultAfterScanning",
      "" + ( linenr - 1 ) ) );
    summary.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.HorizontalLine" ) );

    if ( nrfields == evaluators.size() ) {
      for ( int i = 0; i < nrfields; i++ ) {
        Object field = getField( meta, i );
        StringEvaluator evaluator = evaluators.get( i );
        List<StringEvaluationResult> evaluationResults = evaluator.getStringEvaluationResults();
        StringEvaluationResult strEvalResult = evaluator.getAdvicedResult();
        setFieldTypeInfo( field, evaluator, evaluationResults, strEvalResult );

        if ( strEvalResult != null ) {
          fieldAnalyses[ i ].nrnull = strEvalResult.getNrNull();
          fieldAnalyses[ i ].minstr = strEvalResult.getMin() == null ? "" : strEvalResult.getMin().toString();
          fieldAnalyses[ i ].maxstr = strEvalResult.getMax() == null ? "" : strEvalResult.getMax().toString();
        }
        appendFieldInfoToMessage( summary, i, field, evaluationResults, fieldAnalyses[ i ], linenr );
      }
    }
    return summary.toString();
  }

  private void appendFieldInfoToMessage( StringBuilder message, int fieldIndex, Object field,
                                         List<StringEvaluationResult> evaluationResults,
                                         FieldAnalysis fieldAnalysis, int linenr ) {
    message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.FieldNumber",
      "" + ( fieldIndex + 1 ) ) );
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

  private void appendNumberFieldInfo( StringBuilder message, Object field,
                                      List<StringEvaluationResult> evaluationResults, FieldAnalysis fieldAnalysis ) {
    DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance();
    DecimalFormatSymbols dfs2 = new DecimalFormatSymbols();

    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.EstimatedLength",
      ( getFieldLength( field ) < 0 ? "-" : "" + getFieldLength( field ) ) ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.EstimatedPrecision",
      getFieldPrecision( field ) < 0 ? "-" : "" + getFieldPrecision( field ) ) );
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
      PKG, "TextFileCSVImportProgressDialog.Info.NumberNrNullValues", "" + fieldAnalysis.nrnull ) );
  }

  private void appendStringFieldInfo( StringBuilder message, Object field, FieldAnalysis fieldAnalysis ) {
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringMaxLength", "" + getFieldLength( field ) ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringMinValue", fieldAnalysis.minstr ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringMaxValue", fieldAnalysis.maxstr ) );
    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.StringNrNullValues", "" + fieldAnalysis.nrnull ) );
  }

  private void appendDateFieldInfo( StringBuilder message, Object field, FieldAnalysis fieldAnalysis ) {
    SimpleDateFormat daf2 = new SimpleDateFormat();

    message.append( BaseMessages.getString(
      PKG, "TextFileCSVImportProgressDialog.Info.DateMaxLength",
      getFieldLength( field ) < 0 ? "-" : "" + getFieldLength( field ) ) );
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
      PKG, "TextFileCSVImportProgressDialog.Info.DateNrNullValues", "" + fieldAnalysis.nrnull ) );
  }

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

  public String getMessage() {
    return message;
  }

  public Object[] getInputFields() {
    return inputFields;
  }

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
