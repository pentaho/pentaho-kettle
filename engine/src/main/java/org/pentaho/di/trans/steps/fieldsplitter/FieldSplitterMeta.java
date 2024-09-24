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

package org.pentaho.di.trans.steps.fieldsplitter;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.AfterInjection;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/*
 * Created on 31-okt-2003
 *
 */

/**
 * <CODE>
  Example1:<p>
  -------------<p>
  DATUM;VALUES<p>
  20031031;500,300,200,100<p>
<p>
        ||<t>        delimiter     = ,<p>
       \||/<t>       field[]       = SALES1, SALES2, SALES3, SALES4<p>
        \/<t>        id[]          = <empty><p>
          <t>        idrem[]       = no, no, no, no<p>
           <t>       type[]        = Number, Number, Number, Number<p>
            <t>      format[]      = ###.##, ###.##, ###.##, ###.##<p>
            <t>      group[]       = <empty><p>
            <t>      decimal[]     = .<p>
            <t>      currency[]    = <empty><p>
            <t>      length[]      = 3, 3, 3, 3<p>
            <t>      precision[]   = 0, 0, 0, 0<p>
  <p>
  DATUM;SALES1;SALES2;SALES3;SALES4<p>
  20031031;500;300;200;100<p>
<p>
  Example2:<p>
  -----------<p>
<p>
  20031031;Sales2=310.50, Sales4=150.23<p>
<p>
        ||        delimiter     = ,<p>
       \||/       field[]       = SALES1, SALES2, SALES3, SALES4<p>
        \/        id[]          = Sales1, Sales2, Sales3, Sales4<p>
                  idrem[]       = yes, yes, yes, yes (remove ID's from split field)<p>
                  type[]        = Number, Number, Number, Number<p>
                  format[]      = ###.##, ###.##, ###.##, ###.##<p>
                  group[]       = <empty><p>
                  decimal[]     = .<p>
                  currency[]    = <empty><p>
                  length[]      = 3, 3, 3, 3<p>
                  precision[]   = 0, 0, 0, 0<p>
<p>
  DATUM;SALES1;SALES2;SALES3;SALES4<p>
  20031031;310,50;150,23<p>
<p>

</CODE>
 **/
@InjectionSupported( localizationPrefix = "FieldSplitter.Injection.", groups = { "FIELDS" } )
public class FieldSplitterMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = FieldSplitterMeta.class; // for i18n purposes, needed by Translator2!!

  /** Field to split */
  @Injection( name = "FIELD_TO_SPLIT" )
  private String splitField;

  /** Split fields based upon this delimiter. */
  @Injection( name = "DELIMITER" )
  private String delimiter;

  /** Ignore delimiter inside pairs of the enclosure string */
  @Injection( name = "ENCLOSURE" )
  private String enclosure;

  /** new field names */
  @Injection( name = "NAME", group = "FIELDS" )
  private String[] fieldName;

  /** Field ID's to scan for */
  @Injection( name = "ID", group = "FIELDS" )
  private String[] fieldID;

  /** flag: remove ID */
  @Injection( name = "REMOVE_ID", group = "FIELDS" )
  private boolean[] fieldRemoveID;

  /** type of new field */
  @Injection( name = "DATA_TYPE", group = "FIELDS", converter = DataTypeConverter.class )
  private int[] fieldType;

  /** formatting mask to convert value */
  @Injection( name = "FORMAT", group = "FIELDS" )
  private String[] fieldFormat;

  /** Grouping symbol */
  @Injection( name = "GROUPING", group = "FIELDS" )
  private String[] fieldGroup;

  /** Decimal point . or , */
  @Injection( name = "DECIMAL", group = "FIELDS" )
  private String[] fieldDecimal;

  /** Currency symbol */
  @Injection( name = "CURRENCY", group = "FIELDS" )
  private String[] fieldCurrency;

  /** Length of field */
  @Injection( name = "LENGTH", group = "FIELDS" )
  private int[] fieldLength;

  /** Precision of field */
  @Injection( name = "PRECISION", group = "FIELDS" )
  private int[] fieldPrecision;

  /** Replace this value with a null */
  @Injection( name = "NULL_IF", group = "FIELDS" )
  private String[] fieldNullIf;

  /** Default value in case no value was found (ID option) */
  @Injection( name = "DEFAULT", group = "FIELDS" )
  private String[] fieldIfNull;

  /** Perform trimming of this type on the fieldName during lookup and storage */
  @Injection( name = "TRIM_TYPE", group = "FIELDS", converter = TrimTypeConverter.class )
  private int[] fieldTrimType;

  public FieldSplitterMeta() {
    super(); // allocate BaseStepMeta
  }

  public String getSplitField() {
    return splitField;
  }

  public void setSplitField( final String splitField ) {
    this.splitField = splitField;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter( final String delimiter ) {
    this.delimiter = delimiter;
  }

  public String getEnclosure() {
    return enclosure;
  }

  public void setEnclosure( final String enclosure ) {
    this.enclosure = enclosure;
  }

  public String[] getFieldName() {
    return fieldName;
  }

  public void setFieldName( final String[] fieldName ) {
    this.fieldName = fieldName;
  }

  public String[] getFieldID() {
    return fieldID;
  }

  public void setFieldID( final String[] fieldID ) {
    this.fieldID = fieldID;
  }

  public boolean[] getFieldRemoveID() {
    return fieldRemoveID;
  }

  public void setFieldRemoveID( final boolean[] fieldRemoveID ) {
    this.fieldRemoveID = fieldRemoveID;
  }

  public int[] getFieldType() {
    return fieldType;
  }

  public void setFieldType( final int[] fieldType ) {
    this.fieldType = fieldType;
  }

  public String[] getFieldFormat() {
    return fieldFormat;
  }

  public void setFieldFormat( final String[] fieldFormat ) {
    this.fieldFormat = fieldFormat;
  }

  public String[] getFieldGroup() {
    return fieldGroup;
  }

  public void setFieldGroup( final String[] fieldGroup ) {
    this.fieldGroup = fieldGroup;
  }

  public String[] getFieldDecimal() {
    return fieldDecimal;
  }

  public void setFieldDecimal( final String[] fieldDecimal ) {
    this.fieldDecimal = fieldDecimal;
  }

  public String[] getFieldCurrency() {
    return fieldCurrency;
  }

  public void setFieldCurrency( final String[] fieldCurrency ) {
    this.fieldCurrency = fieldCurrency;
  }

  public int[] getFieldLength() {
    return fieldLength;
  }

  public void setFieldLength( final int[] fieldLength ) {
    this.fieldLength = fieldLength;
  }

  public int[] getFieldPrecision() {
    return fieldPrecision;
  }

  public void setFieldPrecision( final int[] fieldPrecision ) {
    this.fieldPrecision = fieldPrecision;
  }

  public String[] getFieldNullIf() {
    return fieldNullIf;
  }

  public void setFieldNullIf( final String[] fieldNullIf ) {
    this.fieldNullIf = fieldNullIf;
  }

  public String[] getFieldIfNull() {
    return fieldIfNull;
  }

  public void setFieldIfNull( final String[] fieldIfNull ) {
    this.fieldIfNull = fieldIfNull;
  }

  public int[] getFieldTrimType() {
    return fieldTrimType;
  }

  public void setFieldTrimType( final int[] fieldTrimType ) {
    this.fieldTrimType = fieldTrimType;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    fieldName = new String[nrfields];
    fieldID = new String[nrfields];
    fieldRemoveID = new boolean[nrfields];
    fieldType = new int[nrfields];
    fieldFormat = new String[nrfields];
    fieldGroup = new String[nrfields];
    fieldDecimal = new String[nrfields];
    fieldCurrency = new String[nrfields];
    fieldLength = new int[nrfields];
    fieldPrecision = new int[nrfields];
    fieldNullIf = new String[nrfields];
    fieldIfNull = new String[nrfields];
    fieldTrimType = new int[nrfields];
  }

  public Object clone() {
    FieldSplitterMeta retval = (FieldSplitterMeta) super.clone();

    final int nrfields = fieldName.length;

    retval.allocate( nrfields );

    System.arraycopy( fieldName, 0, retval.fieldName, 0, nrfields );
    System.arraycopy( fieldID, 0, retval.fieldID, 0, nrfields );
    System.arraycopy( fieldRemoveID, 0, retval.fieldRemoveID, 0, nrfields );
    System.arraycopy( fieldType, 0, retval.fieldType, 0, nrfields );
    System.arraycopy( fieldLength, 0, retval.fieldLength, 0, nrfields );
    System.arraycopy( fieldPrecision, 0, retval.fieldPrecision, 0, nrfields );
    System.arraycopy( fieldFormat, 0, retval.fieldFormat, 0, nrfields );
    System.arraycopy( fieldGroup, 0, retval.fieldGroup, 0, nrfields );
    System.arraycopy( fieldDecimal, 0, retval.fieldDecimal, 0, nrfields );
    System.arraycopy( fieldCurrency, 0, retval.fieldCurrency, 0, nrfields );
    System.arraycopy( fieldNullIf, 0, retval.fieldNullIf, 0, nrfields );
    System.arraycopy( fieldIfNull, 0, retval.fieldIfNull, 0, nrfields );
    System.arraycopy( fieldTrimType, 0, retval.fieldTrimType, 0, nrfields );

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      splitField = XMLHandler.getTagValue( stepnode, "splitfield" );
      delimiter = XMLHandler.getTagValue( stepnode, "delimiter" );
      enclosure = XMLHandler.getTagValue( stepnode, "enclosure" );

      final Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      final int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        final Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        fieldName[i] = XMLHandler.getTagValue( fnode, "name" );
        fieldID[i] = XMLHandler.getTagValue( fnode, "id" );
        final String sidrem = XMLHandler.getTagValue( fnode, "idrem" );
        final String stype = XMLHandler.getTagValue( fnode, "type" );
        fieldFormat[i] = XMLHandler.getTagValue( fnode, "format" );
        fieldGroup[i] = XMLHandler.getTagValue( fnode, "group" );
        fieldDecimal[i] = XMLHandler.getTagValue( fnode, "decimal" );
        fieldCurrency[i] = XMLHandler.getTagValue( fnode, "currency" );
        final String slen = XMLHandler.getTagValue( fnode, "length" );
        final String sprc = XMLHandler.getTagValue( fnode, "precision" );
        fieldNullIf[i] = XMLHandler.getTagValue( fnode, "nullif" );
        fieldIfNull[i] = XMLHandler.getTagValue( fnode, "ifnull" );
        final String trim = XMLHandler.getTagValue( fnode, "trimtype" );

        fieldRemoveID[i] = "Y".equalsIgnoreCase( sidrem );
        fieldType[i] = ValueMetaFactory.getIdForValueMeta( stype );
        fieldLength[i] = Const.toInt( slen, -1 );
        fieldPrecision[i] = Const.toInt( sprc, -1 );
        fieldTrimType[i] = ValueMetaString.getTrimTypeByCode( trim );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "FieldSplitterMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    splitField = "";
    delimiter = ",";
    enclosure = null;
    allocate( 0 );
  }

  public int getFieldsCount() {
    int count = Math.min( getFieldName().length, getFieldType().length );
    count = Math.min( count, getFieldLength().length );
    count = Math.min( count, getFieldPrecision().length );
    count = Math.min( count, getFieldFormat().length );
    count = Math.min( count, getFieldDecimal().length );
    count = Math.min( count, getFieldGroup().length );
    count = Math.min( count, getFieldCurrency().length );
    count = Math.min( count, getFieldTrimType().length );
    return count;
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Remove the field to split
    int idx = r.indexOfValue( getSplitField() );
    if ( idx < 0 ) { // not found
      throw new RuntimeException( BaseMessages.getString(
        PKG, "FieldSplitter.Log.CouldNotFindFieldToSplit", getSplitField() ) );
    }

    // Add the new fields at the place of the index --> replace!
    int count = getFieldsCount();
    for ( int i = 0; i < count; i++ ) {
      try {
        final ValueMetaInterface v = ValueMetaFactory.createValueMeta( getFieldName()[i], getFieldType()[i] );
        v.setLength( getFieldLength()[i], getFieldPrecision()[i] );
        v.setOrigin( name );
        v.setConversionMask( getFieldFormat()[i] );
        v.setDecimalSymbol( getFieldDecimal()[i] );
        v.setGroupingSymbol( getFieldGroup()[i] );
        v.setCurrencySymbol( getFieldCurrency()[i] );
        v.setTrimType( getFieldTrimType()[i] );
        // TODO when implemented in UI
        // v.setDateFormatLenient(dateFormatLenient);
        // TODO when implemented in UI
        // v.setDateFormatLocale(dateFormatLocale);
        if ( i == 0 && idx >= 0 ) {
          // the first valueMeta (splitField) will be replaced
          r.setValueMeta( idx, v );
        } else {
          // other valueMeta will be added
          if ( idx >= r.size() ) {
            r.addValueMeta( v );
          }
          r.addValueMeta( idx + i, v );
        }
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
  }

  public String getXML() {
    final StringBuilder retval = new StringBuilder( 500 );

    retval
      .append( "   " ).append( XMLHandler.addTagValue( "splitfield", splitField ) )
      .append( "   " ).append( XMLHandler.addTagValue( "delimiter", delimiter ) )
      .append( "   " ).append( XMLHandler.addTagValue( "enclosure", enclosure ) );

    retval.append( "   " ).append( "<fields>" );
    for ( int i = 0; i < fieldName.length; i++ ) {
      retval
        .append( "      " ).append( "<field>" )
        .append( "        " ).append( XMLHandler.addTagValue( "name", fieldName[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "id", ArrayUtils.isEmpty( fieldID ) ? null : fieldID[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "idrem", ArrayUtils.isEmpty( fieldRemoveID ) ? false : fieldRemoveID[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "type",
          ValueMetaFactory.getValueMetaName( ArrayUtils.isEmpty( fieldType ) ? 0 : fieldType[i] ) ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "format", ArrayUtils.isEmpty( fieldFormat ) ? null : fieldFormat[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "group", ArrayUtils.isEmpty( fieldGroup ) ? null : fieldGroup[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "decimal", ArrayUtils.isEmpty( fieldDecimal ) ? null : fieldDecimal[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "currency", ArrayUtils.isEmpty( fieldCurrency ) ? null : fieldCurrency[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "length",  ArrayUtils.isEmpty( fieldLength ) ? -1 : fieldLength[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "precision", ArrayUtils.isEmpty( fieldPrecision ) ? -1 : fieldPrecision[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "nullif", ArrayUtils.isEmpty( fieldNullIf ) ? null : fieldNullIf[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "ifnull", ArrayUtils.isEmpty( fieldIfNull ) ? null : fieldIfNull[i] ) )
        .append( "        " )
        .append( XMLHandler.addTagValue( "trimtype",
          ValueMetaString.getTrimTypeCode( ArrayUtils.isEmpty( fieldTrimType ) ? 0 : fieldTrimType[i] ) ) )
        .append( "      " ).append( "</field>" );
    }
    retval.append( "    " ).append( "</fields>" );

    return retval.toString();
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      splitField = rep.getStepAttributeString( id_step, "splitfield" );
      delimiter = rep.getStepAttributeString( id_step, "delimiter" );
      enclosure = rep.getStepAttributeString( id_step, "enclosure" );

      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        fieldName[i] = rep.getStepAttributeString( id_step, i, "field_name" );
        fieldID[i] = rep.getStepAttributeString( id_step, i, "field_id" );
        fieldRemoveID[i] = rep.getStepAttributeBoolean( id_step, i, "field_idrem" );
        fieldType[i] = ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) );
        fieldFormat[i] = rep.getStepAttributeString( id_step, i, "field_format" );
        fieldGroup[i] = rep.getStepAttributeString( id_step, i, "field_group" );
        fieldDecimal[i] = rep.getStepAttributeString( id_step, i, "field_decimal" );
        fieldCurrency[i] = rep.getStepAttributeString( id_step, i, "field_currency" );
        fieldLength[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_length" );
        fieldPrecision[i] = (int) rep.getStepAttributeInteger( id_step, i, "field_precision" );
        fieldNullIf[i] = rep.getStepAttributeString( id_step, i, "field_nullif" );
        fieldIfNull[i] = rep.getStepAttributeString( id_step, i, "field_ifnull" );
        fieldTrimType[i] =
          ValueMetaString.getTrimTypeByCode( rep.getStepAttributeString( id_step, i, "field_trimtype" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "FieldSplitterMeta.Exception.UnexpectedErrorInReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "splitfield", splitField );
      rep.saveStepAttribute( id_transformation, id_step, "delimiter", delimiter );
      rep.saveStepAttribute( id_transformation, id_step, "enclosure", enclosure );

      for ( int i = 0; i < fieldName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", fieldName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_id", fieldID[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_idrem", fieldRemoveID[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type",
          ValueMetaFactory.getValueMetaName( fieldType[i] ) );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", fieldFormat[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_group", fieldGroup[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", fieldDecimal[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", fieldCurrency[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", fieldLength[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", fieldPrecision[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_nullif", fieldNullIf[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_ifnull", fieldIfNull[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_trimtype", ValueMetaString
          .getTrimTypeCode( fieldTrimType[i] ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "FieldSplitterMeta.Exception.UnalbeToSaveStepInfoToRepository" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    String error_message = "";
    CheckResult cr;

    // Look up fields in the input stream <prev>
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FieldSplitterMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      error_message = "";

      int i = prev.indexOfValue( splitField );
      if ( i < 0 ) {
        error_message =
          BaseMessages.getString(
            PKG, "FieldSplitterMeta.CheckResult.SplitedFieldNotPresentInInputStream", splitField );
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "FieldSplitterMeta.CheckResult.SplitedFieldFoundInInputStream", splitField ), stepMeta );
        remarks.add( cr );
      }
    } else {
      error_message =
        BaseMessages.getString( PKG, "FieldSplitterMeta.CheckResult.CouldNotReadFieldsFromPreviousStep" )
          + Const.CR;
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FieldSplitterMeta.CheckResult.StepReceivingInfoFromOtherStep" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "FieldSplitterMeta.CheckResult.NoInputReceivedFromOtherStep" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new FieldSplitter( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new FieldSplitterData();
  }

  /**
   * If we use injection we can have different arrays lengths.
   * We need synchronize them for consistency behavior with UI
   */
  @AfterInjection
  public void afterInjectionSynchronization() {
    int nrFields = ( fieldName == null ? -1 : fieldName.length );
    if ( nrFields <= 0 ) {
      return;
    }

    String[][] normalizedStringArrays = Utils.normalizeArrays( nrFields, fieldID, fieldFormat, fieldGroup, fieldDecimal, fieldCurrency, fieldNullIf, fieldIfNull );
    fieldID = normalizedStringArrays[ 0 ];
    fieldFormat = normalizedStringArrays[ 1 ];
    fieldGroup = normalizedStringArrays[ 2 ];
    fieldDecimal = normalizedStringArrays[ 3 ];
    fieldCurrency = normalizedStringArrays[ 4 ];
    fieldNullIf = normalizedStringArrays[ 5 ];
    fieldIfNull = normalizedStringArrays[ 6 ];

    boolean[][] normalizedBooleanArrays = Utils.normalizeArrays( nrFields, fieldRemoveID );
    fieldRemoveID = normalizedBooleanArrays[ 0 ];

    int[][] normalizedIntArrays = Utils.normalizeArrays( nrFields, fieldType, fieldLength, fieldPrecision, fieldTrimType );
    fieldType = normalizedIntArrays[ 0 ];
    fieldLength = normalizedIntArrays[ 1 ];
    fieldPrecision = normalizedIntArrays[ 2 ];
    fieldTrimType = normalizedIntArrays[ 3 ];

  }
}
