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

import java.text.DecimalFormatSymbols;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringEvaluationResult;
import org.pentaho.di.core.util.StringEvaluator;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInput;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInput;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

public class CsvFileImportProcessor extends BaseFileImportProcessor {

  private final CsvInputMeta meta;

  public CsvFileImportProcessor( CsvInputMeta meta, TransMeta transMeta, BufferedInputStreamReader reader,
                                 int samples,
                                 boolean showSummary
  ) {
    super( meta, transMeta, reader, samples, showSummary );
    this.meta = meta;
    this.inputFields = new TextFileInputField[ meta.getInputFields().length ];
  }

  @Override
  protected int getFieldCount() {
    return meta.getInputFields().length;
  }

  @Override
  protected String getFieldName( Object field ) {
    return ( (TextFileInputField) field ).getName();
  }

  @Override
  protected String getFieldTypeDesc( Object field ) {
    return ( (TextFileInputField) field ).getTypeDesc();
  }

  @Override
  protected int getFieldType( Object field ) {
    return ( (TextFileInputField) field ).getType();
  }

  @Override
  protected int getFieldLength( Object field ) {
    return ( (TextFileInputField) field ).getLength();
  }

  @Override
  protected int getFieldPrecision( Object field ) {
    return ( (TextFileInputField) field ).getPrecision();
  }

  @Override
  protected String getFieldFormat( Object field ) {
    return ( (TextFileInputField) field ).getFormat();
  }

  @Override
  protected boolean hasHeader() {
    return meta.hasHeader();
  }

  @Override
  protected int getHeaderLines() {
    return meta.getNrHeaderLines();
  }

  @Override
  protected String getEnclosure() {
    return meta.getEnclosure();
  }

  @Override
  protected String getEscapeCharacter() {
    return meta.getEscapeCharacter();
  }

  @Override
  protected String getSeparator() {
    return meta.getSeparator();
  }

  @Override
  protected int getFileFormatTypeNr() {
    return meta.getFileFormatTypeNr();
  }

  @Override
  protected Object getField( Object metaObj, int index ) {
    return ( (CsvInputMeta) metaObj ).getInputFields()[ index ];
  }

  @Override
  protected void getFields( RowMetaInterface rowMeta ) throws KettleStepException {
    meta.getFields( rowMeta, "stepname", null, null, transMeta, null, null );
  }

  @Override
  protected Object cloneMeta() {
    return meta.clone();
  }


  @Override
  protected void initializeField( Object field, DecimalFormatSymbols dfs ) {
    TextFileInputField f = (TextFileInputField) field;
    f.setName( f.getName() );
    f.setType( f.getType() );
    f.setFormat( "" );
    f.setLength( -1 );
    f.setPrecision( -1 );
    f.setCurrencySymbol( dfs.getCurrencySymbol() );
    f.setDecimalSymbol( "" + dfs.getDecimalSeparator() );
    f.setGroupSymbol( "" + dfs.getGroupingSeparator() );
    f.setNullString( "-" );
    f.setTrimType( ValueMetaInterface.TRIM_TYPE_NONE );
  }

  @Override
  protected void setFieldTypeInfo( Object field, StringEvaluator evaluator,
                                   List<StringEvaluationResult> evaluationResults,
                                   StringEvaluationResult strEvaluationResult ) {
    TextFileInputField f = (TextFileInputField) field;

    if ( evaluationResults.isEmpty() ) {
      f.setType( ValueMetaInterface.TYPE_STRING );
      f.setLength( evaluator.getMaxLength() );
    } else {
      if ( strEvaluationResult != null ) {
        ValueMetaInterface conversionMeta = strEvaluationResult.getConversionMeta();
        f.setType( conversionMeta.getType() );
        f.setTrimType( conversionMeta.getTrimType() );
        f.setFormat( conversionMeta.getConversionMask() );
        f.setDecimalSymbol( conversionMeta.getDecimalSymbol() );
        f.setGroupSymbol( conversionMeta.getGroupingSymbol() );
        f.setLength( conversionMeta.getLength() );
        f.setPrecision( conversionMeta.getPrecision() );
      }
    }
  }

  @Override
  protected Object[] convertLineToRow( TextFileLine textFileLine, Object strinfo, RowMetaInterface outputRowMeta,
                                       RowMetaInterface convertRowMeta, boolean failOnParseError )
    throws KettleException {
    return TextFileInput.convertLineToRow(
      log, textFileLine, (CsvInputMeta) strinfo, null, 0, outputRowMeta,
      convertRowMeta, meta.getFilePaths( transMeta )[ 0 ], rowNumber,
      transMeta.environmentSubstitute( meta.getSeparator() ),
      transMeta.environmentSubstitute( meta.getEnclosure() ),
      transMeta.environmentSubstitute( meta.getEscapeCharacter() ),
      null, false, false, false, false, false, false, false, false, null, null, false, null, null, null,
      null, 0, failOnParseError );
  }

  @Override
  protected void setAllFieldsToStringType( Object metaObj ) {
    CsvInputMeta csvMeta = (CsvInputMeta) metaObj;
    for ( int i = 0; i < csvMeta.getInputFields().length; i++ ) {
      csvMeta.getInputFields()[ i ].setType( ValueMetaInterface.TYPE_STRING );
    }
  }

  @Override
  protected TextFileInputFieldDTO convertFieldToDto( Object field ) {
    return convertFieldToDto( (TextFileInputField) field );
  }

  private TextFileInputFieldDTO convertFieldToDto( TextFileInputField field ) {
    TextFileInputFieldDTO dto = new TextFileInputFieldDTO();
    dto.setName( field.getName() );
    dto.setType( field.getTypeDesc() );
    dto.setFormat( field.getFormat() );
    dto.setPosition( field.getPosition() == -1 ? "" : String.valueOf( field.getPosition() ) );
    dto.setLength( field.getLength() == -1 ? "" : String.valueOf( field.getLength() ) );
    dto.setPrecision( field.getPrecision() == -1 ? "" : String.valueOf( field.getPrecision() ) );
    dto.setCurrency( field.getCurrencySymbol() );
    dto.setDecimal( field.getDecimalSymbol() );
    dto.setGroup( field.getGroupSymbol() );
    dto.setNullif( field.getNullString() );
    dto.setIfnull( field.getIfNullValue() );
    dto.setTrimType( field.getTrimTypeDesc() );
    dto.setRepeat( field.isRepeated() ? "Y" : "N" );
    return dto;
  }

}
