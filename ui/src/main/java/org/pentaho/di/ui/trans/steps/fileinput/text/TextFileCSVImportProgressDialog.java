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


package org.pentaho.di.ui.trans.steps.fileinput.text;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
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
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputAdditionalField;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.fileinput.text.EncodingType;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputUtils;
import org.pentaho.di.trans.steps.fileinput.text.TextFileLine;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.common.CsvInputAwareImportProgressDialog;

/**
 * Takes care of displaying a dialog that will handle the wait while we're finding out what tables, views etc we can
 * reach in the database.
 *
 * @author Matt
 * @since 07-apr-2005
 */
public class TextFileCSVImportProgressDialog implements CsvInputAwareImportProgressDialog {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;

  private TextFileInputMeta meta;

  private int samples;

  private boolean replaceMeta;

  private String message;

  private String debug;

  private long rownumber;

  private BufferedInputStreamReader reader;

  private TransMeta transMeta;

  private LogChannelInterface log;

  private EncodingType encodingType;


  /**
   * @deprecated construct with BufferedInputStreamReader
   */
  @Deprecated
  public TextFileCSVImportProgressDialog( Shell shell, TextFileInputMeta meta, TransMeta transMeta,
                                          InputStreamReader reader, int samples, boolean replaceMeta ) {
    this( shell, meta, transMeta, new BufferedInputStreamReader( reader ), samples, replaceMeta );
  }

  /**
   * Creates a new dialog that will handle the wait while we're finding out what tables, views etc we can reach in the
   * database.
   */
  public TextFileCSVImportProgressDialog( Shell shell, TextFileInputMeta meta, TransMeta transMeta,
      BufferedInputStreamReader reader, int samples, boolean replaceMeta ) {
    this.shell = shell;
    this.meta = meta;
    this.reader = reader;
    this.samples = samples;
    this.replaceMeta = replaceMeta;
    this.transMeta = transMeta;

    message = null;
    debug = "init";
    rownumber = 1L;

    this.log = new LogChannel( transMeta );

    this.encodingType = EncodingType.guessEncodingType( reader.getEncoding() );

  }

  public String open() {
    return open( true );
  }

  /**
   * @param failOnParseError if set to true, parsing failure on any line will cause parsing to be terminated; when
   *                         set to false, parsing failure on a given line will not prevent remaining lines from
   *                         being parsed - this allows us to analyze fields, even if some field is mis-configured
   *                         and causes a parsing error for the values of that field.
   */
  @Override
  public String open( final boolean failOnParseError ) {
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run( IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException {
        try {
          message = doScan( monitor, failOnParseError );
        } catch ( Exception e ) {
          e.printStackTrace();
          throw new InvocationTargetException( e, BaseMessages.getString( PKG,
              "TextFileCSVImportProgressDialog.Exception.ErrorScanningFile", "" + rownumber, debug, e.toString() ) );
        }
      }
    };

    try {
      ProgressMonitorDialog pmd = new ProgressMonitorDialog( shell );
      pmd.run( true, true, op );
    } catch ( InvocationTargetException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Title" ),
          BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Message" ), e );
    } catch ( InterruptedException e ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Title" ),
          BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.ErrorScanningFile.Message" ), e );
    }

    return message;
  }

  @VisibleForTesting
  String doScan( IProgressMonitor monitor ) throws KettleException {
    return doScan( monitor, true );
  }

  private String doScan( IProgressMonitor monitor, final boolean failOnParseError ) throws KettleException {
    if ( samples > 0 ) {
      monitor.beginTask( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Task.ScanningFile" ), samples
          + 1 );
    } else {
      monitor.beginTask( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Task.ScanningFile" ), 2 );
    }

    String line = "";
    long fileLineNumber = 0;

    DecimalFormatSymbols dfs = new DecimalFormatSymbols();

    int nrfields = meta.inputFields.length;

    RowMetaInterface outputRowMeta = new RowMeta();
    meta.getFields( transMeta.getBowl(), outputRowMeta, null, null, null, transMeta, null, null );

    // Remove the storage meta-data (don't go for lazy conversion during scan)
    for ( ValueMetaInterface valueMeta : outputRowMeta.getValueMetaList() ) {
      valueMeta.setStorageMetadata( null );
      valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    }

    RowMetaInterface convertRowMeta = outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );

    // How many null values?
    int[] nrnull = new int[nrfields]; // How many times null value?

    // String info
    String[] minstr = new String[nrfields]; // min string
    String[] maxstr = new String[nrfields]; // max string
    boolean[] firststr = new boolean[nrfields]; // first occ. of string?

    // Date info
    boolean[] isDate = new boolean[nrfields]; // is the field perhaps a Date?
    int[] dateFormatCount = new int[nrfields]; // How many date formats work?
    boolean[][] dateFormat = new boolean[nrfields][Const.getDateFormats().length]; // What are the date formats that
    // work?
    Date[][] minDate = new Date[nrfields][Const.getDateFormats().length]; // min date value
    Date[][] maxDate = new Date[nrfields][Const.getDateFormats().length]; // max date value

    // Number info
    boolean[] isNumber = new boolean[nrfields]; // is the field perhaps a Number?
    int[] numberFormatCount = new int[nrfields]; // How many number formats work?
    boolean[][] numberFormat = new boolean[nrfields][Const.getNumberFormats().length]; // What are the number format
                                                                                       // that work?
    double[][] minValue = new double[nrfields][Const.getDateFormats().length]; // min number value
    double[][] maxValue = new double[nrfields][Const.getDateFormats().length]; // max number value
    int[][] numberPrecision = new int[nrfields][Const.getNumberFormats().length]; // remember the precision?
    int[][] numberLength = new int[nrfields][Const.getNumberFormats().length]; // remember the length?

    for ( int i = 0; i < nrfields; i++ ) {
      BaseFileField field = meta.inputFields[i];

      if ( log.isDebug() ) {
        debug = "init field #" + i;
      }

      if ( replaceMeta ) { // Clear previous info...

        field.setName( meta.inputFields[i].getName() );
        field.setType( meta.inputFields[i].getType() );
        field.setFormat( "" );
        field.setLength( -1 );
        field.setPrecision( -1 );
        field.setCurrencySymbol( dfs.getCurrencySymbol() );
        field.setDecimalSymbol( "" + dfs.getDecimalSeparator() );
        field.setGroupSymbol( "" + dfs.getGroupingSeparator() );
        field.setNullString( "-" );
        field.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
      }

      nrnull[i] = 0;
      minstr[i] = "";
      maxstr[i] = "";
      firststr[i] = true;

      // Init data guess
      isDate[i] = true;
      for ( int j = 0; j < Const.getDateFormats().length; j++ ) {
        dateFormat[i][j] = true;
        minDate[i][j] = Const.MAX_DATE;
        maxDate[i][j] = Const.MIN_DATE;
      }
      dateFormatCount[i] = Const.getDateFormats().length;

      // Init number guess
      isNumber[i] = true;
      for ( int j = 0; j < Const.getNumberFormats().length; j++ ) {
        numberFormat[i][j] = true;
        minValue[i][j] = Double.MAX_VALUE;
        maxValue[i][j] = -Double.MAX_VALUE;
        numberPrecision[i][j] = -1;
        numberLength[i][j] = -1;
      }
      numberFormatCount[i] = Const.getNumberFormats().length;
    }

    TextFileInputMeta strinfo = (TextFileInputMeta) meta.clone();
    for ( int i = 0; i < nrfields; i++ ) {
      strinfo.inputFields[i].setType( ValueMetaInterface.TYPE_STRING );
    }

    // Sample <samples> rows...
    debug = "get first line";

    StringBuilder lineBuffer = new StringBuilder( 256 );
    int fileFormatType = meta.getFileFormatTypeNr();

    if ( meta.content.header ) {
      fileLineNumber = TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType, lineBuffer,
        meta.content.nrHeaderLines, meta.getEnclosure(), meta.getEscapeCharacter(), fileLineNumber );
    }
    //Reading the first line of data
    line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineBuffer, meta.getEnclosure(), meta.getEscapeCharacter() );
    int linenr = 1;

    List<StringEvaluator> evaluators = new ArrayList<StringEvaluator>();

    // Allocate number and date parsers
    DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance();
    DecimalFormatSymbols dfs2 = new DecimalFormatSymbols();
    SimpleDateFormat daf2 = new SimpleDateFormat();

    boolean errorFound = false;
    while ( !errorFound && line != null && ( linenr <= samples || samples == 0 ) && !monitor.isCanceled() ) {
      monitor.subTask( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Task.ScanningLine", ""
          + linenr ) );

      if ( samples > 0 ) {
        monitor.worked( 1 );
      }

      if ( log.isDebug() ) {
        debug = "convert line #" + linenr + " to row";
      }
      RowMetaInterface rowMeta = new RowMeta();
      meta.getFields( transMeta.getBowl(), rowMeta, "stepname", null, null, transMeta, null, null );
      // Remove the storage meta-data (don't go for lazy conversion during scan)
      for ( ValueMetaInterface valueMeta : rowMeta.getValueMetaList() ) {
        valueMeta.setStorageMetadata( null );
        valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
      }

      String delimiter = transMeta.environmentSubstitute( meta.content.separator );
      String enclosure = transMeta.environmentSubstitute( meta.content.enclosure );
      String escapeCharacter = transMeta.environmentSubstitute( meta.content.escapeCharacter );
      Object[] r =
        TextFileInputUtils.convertLineToRow( log, new TextFileLine( line, fileLineNumber, null ), strinfo, null, 0,
              outputRowMeta, convertRowMeta, FileInputList.createFilePathList( transMeta.getBowl(), transMeta,
                  meta.inputFiles.fileName, meta.inputFiles.fileMask, meta.inputFiles.excludeFileMask,
                  meta.inputFiles.fileRequired, meta.inputFiles.includeSubFolderBoolean() )[0],
              rownumber, delimiter, enclosure, escapeCharacter, null, new BaseFileInputAdditionalField(), null, null,
              false, null, null, null, null, null, failOnParseError );

      if ( r == null ) {
        errorFound = true;
        continue;
      }
      rownumber++;
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

      if ( r != null ) {
        linenr++;
      }

      // Grab another line...
      TextFileLine textFileLine = TextFileInputUtils
        .getLine( log, reader, encodingType, fileFormatType, lineBuffer, enclosure, escapeCharacter, fileLineNumber );
      line = textFileLine.getLine();
      fileLineNumber = textFileLine.getLineNumber();
    }

    monitor.worked( 1 );
    monitor.setTaskName( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Task.AnalyzingResults" ) );

    // Show information on items using a dialog box
    //
    StringBuilder message = new StringBuilder();
    message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.ResultAfterScanning", ""
        + ( linenr - 1 ) ) );
    message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.HorizontalLine" ) );
    if ( nrfields == evaluators.size() ) {
      for ( int i = 0; i < nrfields; i++ ) {
        BaseFileField field = meta.inputFields[ i ];
        StringEvaluator evaluator = evaluators.get( i );
        List<StringEvaluationResult> evaluationResults = evaluator.getStringEvaluationResults();

        // If we didn't find any matching result, it's a String...
        //
        if ( evaluationResults.isEmpty() ) {
          field.setType( ValueMetaInterface.TYPE_STRING );
          field.setLength( evaluator.getMaxLength() );
        } else {
          StringEvaluationResult result = evaluator.getAdvicedResult();
          if ( result != null ) {
            // Take the first option we find, list the others below...
            //
            ValueMetaInterface conversionMeta = result.getConversionMeta();
            field.setType( conversionMeta.getType() );
            field.setTrimType( conversionMeta.getTrimType() );
            field.setFormat( conversionMeta.getConversionMask() );
            field.setDecimalSymbol( conversionMeta.getDecimalSymbol() );
            field.setGroupSymbol( conversionMeta.getGroupingSymbol() );
            field.setLength( conversionMeta.getLength() );
            field.setPrecision( conversionMeta.getPrecision() );

            nrnull[ i ] = result.getNrNull();
            minstr[ i ] = result.getMin() == null ? "" : result.getMin().toString();
            maxstr[ i ] = result.getMax() == null ? "" : result.getMax().toString();
          }
        }

        message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.FieldNumber", "" + ( i
          + 1 ) ) );

        message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.FieldName", field
          .getName() ) );
        message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.FieldType", field
          .getTypeDesc() ) );

        switch ( field.getType() ) {
          case ValueMetaInterface.TYPE_NUMBER:
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.EstimatedLength", ( field
              .getLength() < 0 ? "-" : "" + field.getLength() ) ) );
            message
              .append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.EstimatedPrecision", field
                .getPrecision() < 0 ? "-" : "" + field.getPrecision() ) );
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.NumberFormat", field
              .getFormat() ) );

            if ( !evaluationResults.isEmpty() ) {
              if ( evaluationResults.size() > 1 ) {
                message
                  .append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.WarnNumberFormat" ) );
              }

              for ( StringEvaluationResult seResult : evaluationResults ) {
                String mask = seResult.getConversionMeta().getConversionMask();

                message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.NumberFormat2",
                  mask ) );
                message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.TrimType", seResult
                  .getConversionMeta().getTrimType() ) );
                message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.NumberMinValue",
                  seResult.getMin() ) );
                message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.NumberMaxValue",
                  seResult.getMax() ) );

                try {
                  df2.applyPattern( mask );
                  df2.setDecimalFormatSymbols( dfs2 );
                  double mn = df2.parse( seResult.getMin().toString() ).doubleValue();
                  message
                    .append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.NumberExample", mask,
                      seResult.getMin(), Double.toString( mn ) ) );
                } catch ( Exception e ) {
                  if ( log.isDetailed() ) {
                    log.logDetailed( "This is unexpected: parsing [" + seResult.getMin() + "] with format [" + mask
                      + "] did not work." );
                  }
                }
              }
            }
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.NumberNrNullValues", ""
              + nrnull[ i ] ) );
            break;
          case ValueMetaInterface.TYPE_STRING:
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.StringMaxLength", ""
              + field.getLength() ) );
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.StringMinValue",
              minstr[ i ] ) );
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.StringMaxValue",
              maxstr[ i ] ) );
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.StringNrNullValues", ""
              + nrnull[ i ] ) );
            break;
          case ValueMetaInterface.TYPE_DATE:
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.DateMaxLength", field
              .getLength() < 0 ? "-" : "" + field.getLength() ) );
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.DateFormat", field
              .getFormat() ) );
            if ( dateFormatCount[ i ] > 1 ) {
              message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.WarnDateFormat" ) );
            }
            if ( !Utils.isEmpty( minstr[ i ] ) ) {
              for ( int x = 0; x < Const.getDateFormats().length; x++ ) {
                if ( dateFormat[ i ][ x ] ) {
                  message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.DateFormat2", Const
                    .getDateFormats()[ x ] ) );
                  Date mindate = minDate[ i ][ x ];
                  Date maxdate = maxDate[ i ][ x ];
                  message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.DateMinValue",
                    mindate.toString() ) );
                  message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.DateMaxValue",
                    maxdate.toString() ) );

                  daf2.applyPattern( Const.getDateFormats()[ x ] );
                  try {
                    Date md = daf2.parse( minstr[ i ] );
                    message
                      .append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.DateExample", Const
                        .getDateFormats()[ x ], minstr[ i ], md.toString() ) );
                  } catch ( Exception e ) {
                    if ( log.isDetailed() ) {
                      log.logDetailed( "This is unexpected: parsing [" + minstr[ i ] + "] with format [" + Const
                        .getDateFormats()[ x ] + "] did not work." );
                    }
                  }
                }
              }
            }
            message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.DateNrNullValues", ""
              + nrnull[ i ] ) );
            break;
          default:
            break;
        }
        if ( nrnull[ i ] == linenr - 1 ) {
          message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.AllNullValues" ) );
        }
        message.append( Const.CR );

      }
    }

    monitor.worked( 1 );
    monitor.done();

    return message.toString();

  }
}
