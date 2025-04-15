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
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringEvaluationResult;
import org.pentaho.di.core.util.StringEvaluator;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;
import org.pentaho.di.trans.steps.file.BaseFileInputAdditionalField;

/**
 * This class is used to process text files of File Type as CSV is selected in Content tab of TextFileInput step
 */
public class TextFileCsvFileTypeImportProcessor extends BaseFileImportProcessor {

  private final TextFileInputMeta meta;

  public TextFileCsvFileTypeImportProcessor( TextFileInputMeta meta, TransMeta transMeta,
                                             BufferedInputStreamReader reader,
                                             int samples,
                                             boolean showSummary
  ) {
    super( meta, transMeta, reader, samples, showSummary );
    this.meta = meta;
    this.inputFields = new BaseFileField[ meta.getInputFields().length ];
  }

  @Override
  protected int getFieldCount() {
    return meta.inputFields.length;
  }

  @Override
  protected String getFieldName( Object field ) {
    return ( (BaseFileField) field ).getName();
  }

  @Override
  protected String getFieldTypeDesc( Object field ) {
    return ( (BaseFileField) field ).getTypeDesc();
  }

  @Override
  protected int getFieldType( Object field ) {
    return ( (BaseFileField) field ).getType();
  }

  @Override
  protected int getFieldLength( Object field ) {
    return ( (BaseFileField) field ).getLength();
  }

  @Override
  protected int getFieldPrecision( Object field ) {
    return ( (BaseFileField) field ).getPrecision();
  }

  @Override
  protected String getFieldFormat( Object field ) {
    return ( (BaseFileField) field ).getFormat();
  }

  @Override
  protected boolean hasHeader() {
    return meta.content.header;
  }

  @Override
  protected int getHeaderLines() {
    return meta.content.nrHeaderLines;
  }

  @Override
  protected String getEnclosure() {
    return meta.content.enclosure;
  }

  @Override
  protected String getEscapeCharacter() {
    return meta.content.escapeCharacter;
  }

  @Override
  protected String getSeparator() {
    return meta.content.separator;
  }

  @Override
  protected int getFileFormatTypeNr() {
    return meta.getFileFormatTypeNr();
  }

  @Override
  protected Object getField( Object metaObj, int index ) {
    return ( (TextFileInputMeta) metaObj ).inputFields[ index ];
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
    BaseFileField f = (BaseFileField) field;
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
    BaseFileField f = (BaseFileField) field;

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
    return TextFileInputUtils.convertLineToRow( log, textFileLine, (TextFileInputMeta) strinfo, null, 0,
      outputRowMeta, convertRowMeta, getFilePath(), rowNumber,
      transMeta.environmentSubstitute( meta.content.separator ),
      transMeta.environmentSubstitute( meta.content.enclosure ),
      transMeta.environmentSubstitute( meta.content.escapeCharacter ),
      null, new BaseFileInputAdditionalField(), null, null, false, null, null, null, null, null, failOnParseError );
  }

  @SuppressWarnings( "java:S1874" )
  // CsvInput uses deprecated method from TextFileInputUtils for reading data from file
  protected String getFilePath() {
    return FileInputList.createFilePathList( transMeta, meta.inputFiles.fileName,
      meta.inputFiles.fileMask, meta.inputFiles.excludeFileMask, meta.inputFiles.fileRequired,
      meta.inputFiles.includeSubFolderBoolean() )[ 0 ];
  }

  @Override
  protected void setAllFieldsToStringType( Object metaObj ) {
    TextFileInputMeta textFileMeta = (TextFileInputMeta) metaObj;
    for ( int i = 0; i < textFileMeta.inputFields.length; i++ ) {
      textFileMeta.inputFields[ i ].setType( ValueMetaInterface.TYPE_STRING );
    }
  }

  @Override protected TextFileInputFieldDTO convertFieldToDto( Object field ) {
    return convertFieldToDto( (BaseFileField) field );
  }

  private TextFileInputFieldDTO convertFieldToDto( BaseFileField field ) {
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
