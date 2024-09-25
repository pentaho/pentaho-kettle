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

package org.pentaho.di.trans.steps.fileinput.text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.gui.TextFileInputFieldInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringEvaluationResult;
import org.pentaho.di.core.util.StringEvaluator;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.file.BaseFileInputAdditionalField;
import org.pentaho.di.trans.steps.file.BaseFileInputStep;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;
import org.pentaho.di.trans.steps.util.CsvInputAwareStepUtil;

/**
 * Read all sorts of text files, convert them to rows and writes these to one or more output streams.
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class TextFileInput extends BaseFileInputStep<TextFileInputMeta, TextFileInputData> implements StepInterface, CsvInputAwareStepUtil {
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

  @Override
  public JSONObject doAction( String fieldName, StepMetaInterface stepMetaInterface, TransMeta transMeta,
                              Trans trans, Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      Method actionMethod = TextFileInput.class.getDeclaredMethod( fieldName + "Action", Map.class );
      this.setStepMetaInterface( stepMetaInterface );
      response = (JSONObject) actionMethod.invoke( this, queryParamToValues );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }


  public JSONObject setMinimalWidthAction( Map<String, String> queryParams ) throws JsonProcessingException {
    JSONObject jsonObject = new JSONObject();
    JSONArray textFileFields = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();
    for ( TextFileInputFieldDTO textFileInputFieldDTO : getUpdatedTextFields() ) {
      textFileFields.add( objectMapper.readTree( objectMapper.writeValueAsString( textFileInputFieldDTO ) ) );
    }
    jsonObject.put( "updatedData",textFileFields );
    return jsonObject;
  }

  public List<TextFileInputFieldDTO> getUpdatedTextFields() {
    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();
    List<TextFileInputFieldDTO> textFileFields = new ArrayList<>();

    for ( BaseFileField textFileField : tfii.getInputFields() ) {
      TextFileInputFieldDTO updatedTextFileField = new TextFileInputFieldDTO();
      updatedTextFileField.setName( textFileField.getName() );
      updatedTextFileField.setType( textFileField.getTypeDesc() );

      switch ( textFileField.getType() ) {
        case ValueMetaInterface.TYPE_STRING:
          updatedTextFileField.setFormat( StringUtils.EMPTY );
          break;
        case ValueMetaInterface.TYPE_INTEGER:
          updatedTextFileField.setFormat( "0" );
          break;
        case ValueMetaInterface.TYPE_NUMBER:
          updatedTextFileField.setFormat( "0.#####" );
          break;
        default:
          break;
      }
      updatedTextFileField.setLength( StringUtils.EMPTY );
      updatedTextFileField.setPrecision( StringUtils.EMPTY );
      updatedTextFileField.setCurrency( textFileField.getCurrencySymbol() );
      updatedTextFileField.setDecimal( textFileField.getDecimalSymbol() );
      updatedTextFileField.setGroup( textFileField.getGroupSymbol() );
      updatedTextFileField.setTrimType( "both" );
      updatedTextFileField.setNullif( textFileField.getNullString() );
      updatedTextFileField.setIfnull( textFileField.getIfNullValue() );
      updatedTextFileField.setPosition( String.valueOf( textFileField.getPosition() ) );
      updatedTextFileField.setRepeat( textFileField.isRepeated() ? "N" : "Y" );

      textFileFields.add( updatedTextFileField );
    }
    return textFileFields;
  }

  public JSONObject getFieldsAction( Map<String, String> queryParams ) throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();

    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();

    if ( TextFileInputMeta.FILE_TYPE_CSV == tfii.getFileTypeNr() ) {
      return populateMeta( queryParams );
    } else {
      List<String> rows = this.getFirst( 50, false );
      for ( TextFileInputFieldInterface textFileInputFieldInterface : getFields( tfii, rows ) ) {
        jsonArray.add( objectMapper.readTree( objectMapper.writeValueAsString( textFileInputFieldInterface ) ) );
      }
    }

    response.put( "fields", jsonArray );
    return response;
  }

  public JSONObject populateMeta( Map<String, String> queryParams ) throws KettleException, JsonProcessingException {
    JSONObject response = new JSONObject();
    String isSampleSummary = queryParams.get( "isSampleSummary" );
    int samples = Integer.parseInt( Objects.toString( queryParams.get( "noOfFields" ), "0" ) );
    JSONArray jsonArray = new JSONArray();
    ObjectMapper objectMapper = new ObjectMapper();
    long rownumber = 1L;
    TransMeta transMeta = getTransMeta();
    String line = "";
    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();

    CsvInputAwareMeta csvInputAwareMeta = (CsvInputAwareMeta) getStepMetaInterface();
    final InputStream inputStream = getInputStream( csvInputAwareMeta );
    final BufferedInputStreamReader reader = getBufferedReader( csvInputAwareMeta, inputStream );
    long fileLineNumber = 0;
    EncodingType encodingType = EncodingType.guessEncodingType( reader.getEncoding() );
    boolean failOnParseError = false;

    String[] fieldNames = getFieldNames( csvInputAwareMeta );
    int nrfields = fieldNames.length;

    RowMetaInterface outputRowMeta = new RowMeta();
    tfii.setFields( fieldNames );
    tfii.getFields( outputRowMeta, null, null, null, transMeta, null, null );

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

    DecimalFormatSymbols dfs = new DecimalFormatSymbols();

    tfii.inputFields = new BaseFileField[ nrfields ];

    for ( int i = 0; i < nrfields; i++ ) {
      BaseFileField field = new BaseFileField();

      field.setName( fieldNames[i] );
      field.setType( "0" );
      field.setFormat( "" );
      field.setLength( -1 );
      field.setPrecision( -1 );
      field.setCurrencySymbol( dfs.getCurrencySymbol() );
      field.setDecimalSymbol( "" + dfs.getDecimalSeparator() );
      field.setGroupSymbol( "" + dfs.getGroupingSeparator() );
      field.setNullString( "-" );
      field.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );

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

      tfii.inputFields[i] = field;
    }

    TextFileInputMeta strinfo = (TextFileInputMeta) tfii.clone();
    for ( int i = 0; i < nrfields; i++ ) {
      strinfo.inputFields[i].setType( ValueMetaInterface.TYPE_STRING );
    }

    StringBuilder lineBuffer = new StringBuilder( 256 );
    int fileFormatType = tfii.getFileFormatTypeNr();

    if ( tfii.content.header ) {
      fileLineNumber = TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType, lineBuffer,
              tfii.content.nrHeaderLines, tfii.getEnclosure(), tfii.getEscapeCharacter(), fileLineNumber );
    }
    //Reading the first line of data
    line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineBuffer, tfii.getEnclosure(), tfii.getEscapeCharacter() );
    int linenr = 1;

    List<StringEvaluator> evaluators = new ArrayList<StringEvaluator>();

    // Allocate number and date parsers
    DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance();
    DecimalFormatSymbols dfs2 = new DecimalFormatSymbols();
    SimpleDateFormat daf2 = new SimpleDateFormat();

    boolean errorFound = false;
    while ( !errorFound && line != null && ( linenr <= samples || samples == 0 ) ) {

      RowMetaInterface rowMeta = new RowMeta();
      tfii.getFields( rowMeta, "stepname", null, null, transMeta, null, null );
      // Remove the storage meta-data (don't go for lazy conversion during scan)
      for ( ValueMetaInterface valueMeta : rowMeta.getValueMetaList() ) {
        valueMeta.setStorageMetadata( null );
        valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
      }

      String delimiter = transMeta.environmentSubstitute( tfii.content.separator );
      String enclosure = transMeta.environmentSubstitute( tfii.content.enclosure );
      String escapeCharacter = transMeta.environmentSubstitute( tfii.content.escapeCharacter );
      Object[] r =
              TextFileInputUtils.convertLineToRow( log, new TextFileLine( line, fileLineNumber, null ), strinfo, null, 0,
                      outputRowMeta, convertRowMeta, FileInputList.createFilePathList( transMeta, tfii.inputFiles.fileName,
                              tfii.inputFiles.fileMask, tfii.inputFiles.excludeFileMask, tfii.inputFiles.fileRequired, tfii
                                      .inputFiles.includeSubFolderBoolean() )[0], rownumber, delimiter, enclosure, escapeCharacter, null,
                      new BaseFileInputAdditionalField(), null, null, false, null, null, null, null, null, failOnParseError );

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

    // Show information on items using a dialog box
    //
    StringBuilder message = new StringBuilder();
    if ( Boolean.parseBoolean( isSampleSummary ) ) {
      message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.ResultAfterScanning", ""
              + ( linenr - 1 ) ) );
      message.append( BaseMessages.getString( PKG, "TextFileCSVImportProgressDialog.Info.HorizontalLine" ) );
    }
    if ( nrfields == evaluators.size() ) {
      for ( int i = 0; i < nrfields; i++ ) {
        BaseFileField field = tfii.inputFields[ i ];
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

        if ( Boolean.parseBoolean( isSampleSummary ) ) {
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
        TextFileInputFieldDTO textFileInputFieldDTO = convertFieldToDto( field );
        jsonArray.add( objectMapper.readTree( objectMapper.writeValueAsString( textFileInputFieldDTO ) ) );
      }
    }
    response.put( "fields", jsonArray );
    response.put( "summary", message.toString() );
    return response;
  }

  private TextFileInputFieldDTO convertFieldToDto( BaseFileField field ) {
    TextFileInputFieldDTO textFileInputFieldDTO = new TextFileInputFieldDTO();
    textFileInputFieldDTO.setName( field.getName() );
    textFileInputFieldDTO.setType( field.getTypeDesc() );
    textFileInputFieldDTO.setFormat( field.getFormat() );
    textFileInputFieldDTO.setPosition( field.getPosition() == -1 ? "" : String.valueOf( field.getPosition() ) );
    textFileInputFieldDTO.setLength( field.getLength() == -1 ? "" : String.valueOf( field.getLength() ) );
    textFileInputFieldDTO.setPrecision( field.getPrecision() == -1 ? "" : String.valueOf( field.getPrecision() ) );
    textFileInputFieldDTO.setCurrency( field.getCurrencySymbol() );
    textFileInputFieldDTO.setDecimal( field.getDecimalSymbol() );
    textFileInputFieldDTO.setGroup( field.getGroupSymbol() );
    textFileInputFieldDTO.setNullif( field.getNullString() );
    textFileInputFieldDTO.setIfnull( field.getIfNullValue() );
    textFileInputFieldDTO.setTrimType( field.getTrimTypeDesc() );
    textFileInputFieldDTO.setRepeat( field.isRepeated() ? "Y" : "N" );
    return textFileInputFieldDTO;
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
      if ( ( row.length <= index ) ) {
        if ( failOnParseError ) {
          throw new KettleException( new NullPointerException() );
        }
      }
      string = row.length <= index || row[ index ] == null ? null : row[ index ].toString();
    }

    return string;
  }

  public Vector<TextFileInputFieldInterface> getFields( TextFileInputMeta info, List<String> rows ) {
    Vector<TextFileInputFieldInterface> fields = new Vector<>();

    int maxsize = 0;
    for ( String row : rows ) {
      int len = row.length();
      if ( len > maxsize ) {
        maxsize = len;
      }
    }

    int prevEnd = 0;
    int dummynr = 1;

    for ( int i = 0; i < info.inputFields.length; i++ ) {
      BaseFileField f = info.inputFields[i];

      // See if positions are skipped, if this is the case, add dummy fields...
      if ( f.getPosition() != prevEnd ) { // gap

        BaseFileField field = new BaseFileField( "Dummy" + dummynr, prevEnd, f.getPosition() - prevEnd );
        field.setIgnored( true ); // don't include in result by default.
        fields.add( field );
        dummynr++;
      }

      BaseFileField field = new BaseFileField( f.getName(), f.getPosition(), f.getLength() );
      field.setType( f.getType() );
      field.setIgnored( false );
      field.setFormat( f.getFormat() );
      field.setPrecision( f.getPrecision() );
      field.setTrimType( f.getTrimType() );
      field.setDecimalSymbol( f.getDecimalSymbol() );
      field.setGroupSymbol( f.getGroupSymbol() );
      field.setCurrencySymbol( f.getCurrencySymbol() );
      field.setRepeated( f.isRepeated() );
      field.setNullString( f.getNullString() );

      fields.add( field );

      prevEnd = field.getPosition() + field.getLength();
    }

    if ( info.inputFields.length == 0 ) {
      BaseFileField field = new BaseFileField( "Field1", 0, maxsize );
      fields.add( field );
    } else {
      // Take the last field and see if it reached until the maximum...
      BaseFileField f = info.inputFields[info.inputFields.length - 1];

      int pos = f.getPosition();
      int len = f.getLength();
      if ( pos + len < maxsize ) {
        // If not, add an extra trailing field!
        BaseFileField field = new BaseFileField( "Dummy" + dummynr, pos + len, maxsize - pos - len );
        field.setIgnored( true ); // don't include in result by default.
        fields.add( field );
      }
    }

    Collections.sort( fields );

    return fields;
  }

  public JSONObject getFieldNamesAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    String[] fieldNames = getFieldNames( (CsvInputAwareMeta) getStepMetaInterface() );
    for ( String fieldName : fieldNames ) {
      jsonArray.add( fieldName );
    }
    response.put( "fieldNames", jsonArray );
    return response;
  }

  private JSONObject showFilesAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();

    String filter = queryParams.get( "filter" );
    String isRegex = queryParams.get( "isRegex" );

    TextFileInputMeta tfii = (TextFileInputMeta) getStepMetaInterface();
    String[] files =
            FileInputList.createFilePathList( getTransMeta().getBowl(), getTransMeta(), tfii.inputFiles.fileName,
                    tfii.inputFiles.fileMask, tfii.inputFiles.excludeFileMask, tfii.inputFiles.fileRequired,
                    tfii.inputFiles.includeSubFolderBoolean() );

    JSONArray filteredFiles = new JSONArray();

    if ( files == null || files.length == 0 ) {
      response.put( "message", BaseMessages.getString( PKG, "TextFileInputDialog.NoFilesFound.DialogMessage" ) );
    } else {
      for ( String file : files ) {
        if ( Boolean.TRUE.valueOf( isRegex ) ) {
          Matcher matcher = Pattern.compile( filter ).matcher( file );
          if ( matcher.matches() ) {
            filteredFiles.add( file );
          }
        } else if ( StringUtils.isBlank( filter ) || StringUtils.contains( file.toUpperCase(), filter.toUpperCase() ) ) {
          filteredFiles.add( file );
        }
      }
    }
    response.put( "files", filteredFiles );
    return response;
  }

  public JSONObject showContentAction( Map<String, String> queryParams ) throws KettleException {
    JSONObject response = new JSONObject();
    JSONArray jsonArray = new JSONArray();
    List<String> content = getFirst( Integer.valueOf( Objects.toString( queryParams.get( "nrlines" ), "0" ) ),
            Boolean.valueOf( Objects.toString( queryParams.get( "skipHeaders" ) , "false" ) ) );
    content.forEach( str -> {
      jsonArray.add( str );
    } );
    response.put ( "firstFileContent", jsonArray );
    return response;
  }

  public List<String> getFirst( int nrlines, boolean skipHeaders ) throws KettleException {
    TextFileInputMeta meta = (TextFileInputMeta) getStepMetaInterface();

    FileInputList textFileList = meta.getFileInputList( getTransMeta().getBowl(), getTransMeta() );

    InputStream fi;
    CompressionInputStream f = null;
    StringBuilder lineStringBuilder = new StringBuilder( 256 );
    int fileFormatType = meta.getFileFormatTypeNr();

    List<String> retval = new ArrayList<>();

    if ( textFileList.nrOfFiles() > 0 ) {
      FileObject file = textFileList.getFile( 0 );
      try {
        fi = KettleVFS.getInputStream( file );

        CompressionProvider provider =
                CompressionProviderFactory.getInstance().createCompressionProviderInstance( meta.content.fileCompression );
        f = provider.createInputStream( fi );

        BufferedInputStreamReader reader;
        if ( meta.getEncoding() != null && meta.getEncoding().length() > 0 ) {
          reader = new BufferedInputStreamReader( new InputStreamReader( f, meta.getEncoding() ) );
        } else {
          reader = new BufferedInputStreamReader( new InputStreamReader( f ) );
        }
        EncodingType encodingType = EncodingType.guessEncodingType( reader.getEncoding() );

        int linenr = 0;

        int maxnr = nrlines + ( meta.content.header ? meta.content.nrHeaderLines : 0 );

        if ( skipHeaders ) {
          // Skip the header lines first if more then one, it helps us position
          if ( meta.content.layoutPaged && meta.content.nrLinesDocHeader > 0 ) {
            TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType,
                    lineStringBuilder,  meta.content.nrLinesDocHeader - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }

          // Skip the header lines first if more then one, it helps us position
          if ( meta.content.header && meta.content.nrHeaderLines > 0 ) {
            TextFileInputUtils.skipLines( log, reader, encodingType, fileFormatType,
                    lineStringBuilder,  meta.content.nrHeaderLines - 1, meta.getEnclosure(), meta.getEscapeCharacter(), 0 );
          }
        }

        String line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder, meta.getEnclosure(), meta.getEscapeCharacter() );
        while ( line != null && ( linenr < maxnr || nrlines == 0 ) ) {
          retval.add( line );
          linenr++;
          line = TextFileInputUtils.getLine( log, reader, encodingType, fileFormatType, lineStringBuilder, meta.getEnclosure(), meta.getEscapeCharacter() );
        }
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( PKG, "TextFileInputDialog.Exception.ErrorGettingFirstLines",
                "" + nrlines, file.getName().getURI() ), e );
      } finally {
        try {
          if ( f != null ) {
            f.close();
          }
        } catch ( Exception e ) {
          // Ignore errors
        }
      }
    }

    return retval;
  }

  public boolean isWaitingForData() {
    return true;
  }

  public String massageFieldName( final String fieldName ) {
    // Replace all spaces and hyphens (-) with underscores (_)
    String massagedFieldName = fieldName;
    massagedFieldName = Const.replace( massagedFieldName, " ", "_" );
    massagedFieldName = Const.replace( massagedFieldName, "-", "_" );
    return massagedFieldName;
  }

  public InputStream getInputStream( final CsvInputAwareMeta meta ) {
    InputStream fileInputStream;
    CompressionInputStream inputStream = null;
    try {
      FileObject fileObject = meta.getHeaderFileObject( getTransMeta() );
      fileInputStream = KettleVFS.getInputStream( fileObject );
      CompressionProvider provider = CompressionProviderFactory.getInstance().createCompressionProviderInstance(
              ( (TextFileInputMeta) meta ).content.fileCompression );
      inputStream = provider.createInputStream( fileInputStream );
    } catch ( final Exception e ) {
      logError( BaseMessages.getString( "FileInputDialog.ErrorGettingFileDesc.DialogMessage" ), e );
    }
    return inputStream;
  }

  @Override
  public LogChannelInterface logChannel() {
    return getLogChannel();
  }

}
