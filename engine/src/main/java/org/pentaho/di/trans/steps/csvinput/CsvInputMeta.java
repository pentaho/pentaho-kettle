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

package org.pentaho.di.trans.steps.csvinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleAttributeInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.InputFileMetaInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @since 2007-07-05
 * @author matt
 * @version 3.0
 */

public class CsvInputMeta extends BaseStepMeta implements StepMetaInterface, InputFileMetaInterface,
  StepMetaInjectionInterface {
  private static Class<?> PKG = CsvInput.class; // for i18n purposes, needed by Translator2!!

  private String filename;

  private String filenameField;

  private boolean includingFilename;

  private String rowNumField;

  private boolean headerPresent;

  private String delimiter;
  private String enclosure;

  private String bufferSize;

  private boolean lazyConversionActive;

  private TextFileInputField[] inputFields;

  private boolean isaddresult;

  private boolean runningInParallel;

  private String encoding;

  private boolean newlinePossibleInFields;

  public CsvInputMeta() {
    super(); // allocate BaseStepMeta
    allocate( 0 );
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  @Override
  public void setDefault() {
    delimiter = ",";
    enclosure = "\"";
    headerPresent = true;
    lazyConversionActive = true;
    isaddresult = false;
    bufferSize = "50000";

    TextFileInputField field = new TextFileInputField();
    field.setName( "field" );
    field.setType( ValueMetaInterface.TYPE_STRING );

    inputFields = new TextFileInputField[] { field, };
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      filename = XMLHandler.getTagValue( stepnode, getXmlCode( "FILENAME" ) );
      filenameField = XMLHandler.getTagValue( stepnode, getXmlCode( "FILENAME_FIELD" ) );
      rowNumField = XMLHandler.getTagValue( stepnode, getXmlCode( "ROW_NUM_FIELD" ) );
      includingFilename =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, getXmlCode( "INCLUDE_FILENAME" ) ) );
      delimiter = XMLHandler.getTagValue( stepnode, getXmlCode( "DELIMITER" ) );
      enclosure = XMLHandler.getTagValue( stepnode, getXmlCode( "ENCLOSURE" ) );
      bufferSize = XMLHandler.getTagValue( stepnode, getXmlCode( "BUFFERSIZE" ) );
      headerPresent = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, getXmlCode( "HEADER_PRESENT" ) ) );
      lazyConversionActive =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, getXmlCode( "LAZY_CONVERSION" ) ) );
      isaddresult = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, getXmlCode( "ADD_FILENAME_RESULT" ) ) );
      runningInParallel = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, getXmlCode( "PARALLEL" ) ) );
      String nlp = XMLHandler.getTagValue( stepnode, getXmlCode( "NEWLINE_POSSIBLE" ) );
      if ( Utils.isEmpty( nlp ) ) {
        if ( runningInParallel ) {
          newlinePossibleInFields = false;
        } else {
          newlinePossibleInFields = true;
        }
      } else {
        newlinePossibleInFields = "Y".equalsIgnoreCase( nlp );
      }
      encoding = XMLHandler.getTagValue( stepnode, getXmlCode( "ENCODING" ) );

      Node fields = XMLHandler.getSubNode( stepnode, getXmlCode( "FIELDS" ) );
      int nrfields = XMLHandler.countNodes( fields, getXmlCode( "FIELD" ) );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        inputFields[i] = new TextFileInputField();

        Node fnode = XMLHandler.getSubNodeByNr( fields, getXmlCode( "FIELD" ), i );

        inputFields[i].setName( XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_NAME" ) ) );
        inputFields[i].setType(
          ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_TYPE" ) ) ) );
        inputFields[i].setFormat( XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_FORMAT" ) ) );
        inputFields[i].setCurrencySymbol( XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_CURRENCY" ) ) );
        inputFields[i].setDecimalSymbol( XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_DECIMAL" ) ) );
        inputFields[i].setGroupSymbol( XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_GROUP" ) ) );
        inputFields[i]
          .setLength( Const.toInt( XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_LENGTH" ) ), -1 ) );
        inputFields[i].setPrecision( Const.toInt(
          XMLHandler.getTagValue( fnode, getXmlCode( "FIELD_PRECISION" ) ), -1 ) );
        inputFields[i].setTrimType( ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue(
          fnode, getXmlCode( "FIELD_TRIM_TYPE" ) ) ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void allocate( int nrFields ) {
    inputFields = new TextFileInputField[nrFields];
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "FILENAME" ), filename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "FILENAME_FIELD" ), filenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "ROW_NUM_FIELD" ), rowNumField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "INCLUDE_FILENAME" ), includingFilename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "DELIMITER" ), delimiter ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "ENCLOSURE" ), enclosure ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "HEADER_PRESENT" ), headerPresent ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "BUFFERSIZE" ), bufferSize ) );
    retval
      .append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "LAZY_CONVERSION" ), lazyConversionActive ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "ADD_FILENAME_RESULT" ), isaddresult ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "PARALLEL" ), runningInParallel ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( getXmlCode( "NEWLINE_POSSIBLE" ), newlinePossibleInFields ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( getXmlCode( "ENCODING" ), encoding ) );

    retval.append( "    " ).append( XMLHandler.openTag( getXmlCode( "FIELDS" ) ) ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      TextFileInputField field = inputFields[i];

      retval.append( "      " ).append( XMLHandler.openTag( getXmlCode( "FIELD" ) ) ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "FIELD_NAME" ), field.getName() ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( getXmlCode( "FIELD_TYPE" ), ValueMetaFactory.getValueMetaName( field.getType() ) ) );
      retval
        .append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "FIELD_FORMAT" ), field.getFormat() ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( getXmlCode( "FIELD_CURRENCY" ), field.getCurrencySymbol() ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( getXmlCode( "FIELD_DECIMAL" ), field.getDecimalSymbol() ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( getXmlCode( "FIELD_GROUP" ), field.getGroupSymbol() ) );
      retval
        .append( "        " ).append( XMLHandler.addTagValue( getXmlCode( "FIELD_LENGTH" ), field.getLength() ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( getXmlCode( "FIELD_PRECISION" ), field.getPrecision() ) );
      retval.append( "        " ).append(
        XMLHandler
          .addTagValue( getXmlCode( "FIELD_TRIM_TYPE" ), ValueMetaString.getTrimTypeCode( field.getTrimType() ) ) );
      retval.append( "      " ).append( XMLHandler.closeTag( getXmlCode( "FIELD" ) ) ).append( Const.CR );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( getXmlCode( "FIELDS" ) ) ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      filename = rep.getStepAttributeString( id_step, getRepCode( "FILENAME" ) );
      filenameField = rep.getStepAttributeString( id_step, getRepCode( "FILENAME_FIELD" ) );
      rowNumField = rep.getStepAttributeString( id_step, getRepCode( "ROW_NUM_FIELD" ) );
      includingFilename = rep.getStepAttributeBoolean( id_step, getRepCode( "INCLUDE_FILENAME" ) );
      delimiter = rep.getStepAttributeString( id_step, getRepCode( "DELIMITER" ) );
      enclosure = rep.getStepAttributeString( id_step, getRepCode( "ENCLOSURE" ) );
      headerPresent = rep.getStepAttributeBoolean( id_step, getRepCode( "HEADER_PRESENT" ) );
      bufferSize = rep.getStepAttributeString( id_step, getRepCode( "BUFFERSIZE" ) );
      lazyConversionActive = rep.getStepAttributeBoolean( id_step, getRepCode( "LAZY_CONVERSION" ) );
      isaddresult = rep.getStepAttributeBoolean( id_step, getRepCode( "ADD_FILENAME_RESULT" ) );
      runningInParallel = rep.getStepAttributeBoolean( id_step, getRepCode( "PARALLEL" ) );
      newlinePossibleInFields =
        rep.getStepAttributeBoolean( id_step, 0, getRepCode( "NEWLINE_POSSIBLE" ), !runningInParallel );
      encoding = rep.getStepAttributeString( id_step, getRepCode( "ENCODING" ) );

      int nrfields = rep.countNrStepAttributes( id_step, getRepCode( "FIELD_NAME" ) );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        inputFields[i] = new TextFileInputField();

        inputFields[i].setName( rep.getStepAttributeString( id_step, i, getRepCode( "FIELD_NAME" ) ) );
        inputFields[i].setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString(
          id_step, i, getRepCode( "FIELD_TYPE" ) ) ) );
        inputFields[i].setFormat( rep.getStepAttributeString( id_step, i, getRepCode( "FIELD_FORMAT" ) ) );
        inputFields[i]
          .setCurrencySymbol( rep.getStepAttributeString( id_step, i, getRepCode( "FIELD_CURRENCY" ) ) );
        inputFields[i].setDecimalSymbol( rep.getStepAttributeString( id_step, i, getRepCode( "FIELD_DECIMAL" ) ) );
        inputFields[i].setGroupSymbol( rep.getStepAttributeString( id_step, i, getRepCode( "FIELD_GROUP" ) ) );
        inputFields[i].setLength( (int) rep.getStepAttributeInteger( id_step, i, getRepCode( "FIELD_LENGTH" ) ) );
        inputFields[i].setPrecision( (int) rep
          .getStepAttributeInteger( id_step, i, getRepCode( "FIELD_PRECISION" ) ) );
        inputFields[i].setTrimType( ValueMetaString.getTrimTypeByCode( rep.getStepAttributeString(
          id_step, i, getRepCode( "FIELD_TRIM_TYPE" ) ) ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "FILENAME" ), filename );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "FILENAME_FIELD" ), filenameField );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "ROW_NUM_FIELD" ), rowNumField );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "INCLUDE_FILENAME" ), includingFilename );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "DELIMITER" ), delimiter );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "ENCLOSURE" ), enclosure );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "BUFFERSIZE" ), bufferSize );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "HEADER_PRESENT" ), headerPresent );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "LAZY_CONVERSION" ), lazyConversionActive );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "ADD_FILENAME_RESULT" ), isaddresult );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "PARALLEL" ), runningInParallel );
      rep
        .saveStepAttribute(
          id_transformation, id_step, getRepCode( "NEWLINE_POSSIBLE" ), newlinePossibleInFields );
      rep.saveStepAttribute( id_transformation, id_step, getRepCode( "ENCODING" ), encoding );

      for ( int i = 0; i < inputFields.length; i++ ) {
        TextFileInputField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_NAME" ), field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_TYPE" ),
          ValueMetaFactory.getValueMetaName( field.getType() ) );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_FORMAT" ), field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_CURRENCY" ), field
          .getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_DECIMAL" ), field
          .getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_GROUP" ), field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_LENGTH" ), field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_PRECISION" ), field
          .getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, getRepCode( "FIELD_TRIM_TYPE" ), ValueMetaString
          .getTrimTypeCode( field.getTrimType() ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    try {
      rowMeta.clear(); // Start with a clean slate, eats the input

      for ( int i = 0; i < inputFields.length; i++ ) {
        TextFileInputField field = inputFields[i];

        ValueMetaInterface valueMeta = ValueMetaFactory.createValueMeta( field.getName(), field.getType() );
        valueMeta.setConversionMask( field.getFormat() );
        valueMeta.setLength( field.getLength() );
        valueMeta.setPrecision( field.getPrecision() );
        valueMeta.setConversionMask( field.getFormat() );
        valueMeta.setDecimalSymbol( field.getDecimalSymbol() );
        valueMeta.setGroupingSymbol( field.getGroupSymbol() );
        valueMeta.setCurrencySymbol( field.getCurrencySymbol() );
        valueMeta.setTrimType( field.getTrimType() );
        if ( lazyConversionActive ) {
          valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
        }
        valueMeta.setStringEncoding( space.environmentSubstitute( encoding ) );

        // In case we want to convert Strings...
        // Using a copy of the valueMeta object means that the inner and outer representation format is the same.
        // Preview will show the data the same way as we read it.
        // This layout is then taken further down the road by the metadata through the transformation.
        //
        ValueMetaInterface storageMetadata =
          ValueMetaFactory.cloneValueMeta( valueMeta, ValueMetaInterface.TYPE_STRING );
        storageMetadata.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
        storageMetadata.setLength( -1, -1 ); // we don't really know the lengths of the strings read in advance.
        valueMeta.setStorageMetadata( storageMetadata );

        valueMeta.setOrigin( origin );

        rowMeta.addValueMeta( valueMeta );
      }

      if ( !Utils.isEmpty( filenameField ) && includingFilename ) {
        ValueMetaInterface filenameMeta = new ValueMetaString( filenameField );
        filenameMeta.setOrigin( origin );
        if ( lazyConversionActive ) {
          filenameMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
          filenameMeta.setStorageMetadata( new ValueMetaString( filenameField ) );
        }
        rowMeta.addValueMeta( filenameMeta );
      }

      if ( !Utils.isEmpty( rowNumField ) ) {
        ValueMetaInterface rowNumMeta = new ValueMetaInteger( rowNumField );
        rowNumMeta.setLength( 10 );
        rowNumMeta.setOrigin( origin );
        rowMeta.addValueMeta( rowNumMeta );
      }
    } catch ( Exception e ) {
      throw new KettleStepException( e );
    }

  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "CsvInputMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "CsvInputMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "CsvInputMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "CsvInputMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new CsvInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new CsvInputData();
  }

  /**
   * @return the delimiter
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * @param delimiter
   *          the delimiter to set
   */
  public void setDelimiter( String delimiter ) {
    this.delimiter = delimiter;
  }

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename
   *          the filename to set
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @return the bufferSize
   */
  public String getBufferSize() {
    return bufferSize;
  }

  /**
   * @param bufferSize
   *          the bufferSize to set
   */
  public void setBufferSize( String bufferSize ) {
    this.bufferSize = bufferSize;
  }

  /**
   * @return true if lazy conversion is turned on: conversions are delayed as long as possible, perhaps to never occur
   *         at all.
   */
  public boolean isLazyConversionActive() {
    return lazyConversionActive;
  }

  /**
   * @param lazyConversionActive
   *          true if lazy conversion is to be turned on: conversions are delayed as long as possible, perhaps to never
   *          occur at all.
   */
  public void setLazyConversionActive( boolean lazyConversionActive ) {
    this.lazyConversionActive = lazyConversionActive;
  }

  /**
   * @return the headerPresent
   */
  public boolean isHeaderPresent() {
    return headerPresent;
  }

  /**
   * @param headerPresent
   *          the headerPresent to set
   */
  public void setHeaderPresent( boolean headerPresent ) {
    this.headerPresent = headerPresent;
  }

  /**
   * @return the enclosure
   */
  @Override
  public String getEnclosure() {
    return enclosure;
  }

  /**
   * @param enclosure
   *          the enclosure to set
   */
  public void setEnclosure( String enclosure ) {
    this.enclosure = enclosure;
  }

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 );

    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );
    if ( !Utils.isEmpty( filename ) ) {
      // Add the filename to the references, including a reference to this
      // step meta data.
      //
      reference.getEntries().add(
        new ResourceEntry( transMeta.environmentSubstitute( filename ), ResourceType.FILE ) );
    }
    return references;
  }

  /**
   * @return the inputFields
   */
  @Override
  public TextFileInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          the inputFields to set
   */
  public void setInputFields( TextFileInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  @Override
  public int getFileFormatTypeNr() {
    return TextFileInputMeta.FILE_FORMAT_MIXED; // TODO: check this
  }

  @Override
  public String[] getFilePaths( VariableSpace space ) {
    return new String[] { space.environmentSubstitute( filename ), };
  }

  @Override
  public int getNrHeaderLines() {
    return 1;
  }

  @Override
  public boolean hasHeader() {
    return isHeaderPresent();
  }

  @Override
  public String getErrorCountField() {
    return null;
  }

  @Override
  public String getErrorFieldsField() {
    return null;
  }

  @Override
  public String getErrorTextField() {
    return null;
  }

  @Override
  public String getEscapeCharacter() {
    return null;
  }

  @Override
  public String getFileType() {
    return "CSV";
  }

  @Override
  public String getSeparator() {
    return delimiter;
  }

  @Override
  public boolean includeFilename() {
    return false;
  }

  @Override
  public boolean includeRowNumber() {
    return false;
  }

  @Override
  public boolean isErrorIgnored() {
    return false;
  }

  @Override
  public boolean isErrorLineSkipped() {
    return false;
  }

  /**
   * @return the filenameField
   */
  public String getFilenameField() {
    return filenameField;
  }

  /**
   * @param filenameField
   *          the filenameField to set
   */
  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  /**
   * @return the includingFilename
   */
  public boolean isIncludingFilename() {
    return includingFilename;
  }

  /**
   * @param includingFilename
   *          the includingFilename to set
   */
  public void setIncludingFilename( boolean includingFilename ) {
    this.includingFilename = includingFilename;
  }

  /**
   * @return the rowNumField
   */
  public String getRowNumField() {
    return rowNumField;
  }

  /**
   * @param rowNumField
   *          the rowNumField to set
   */
  public void setRowNumField( String rowNumField ) {
    this.rowNumField = rowNumField;
  }

  /**
   * @param isaddresult
   *          The isaddresult to set.
   */
  public void setAddResultFile( boolean isaddresult ) {
    this.isaddresult = isaddresult;
  }

  /**
   * @return Returns isaddresult.
   */
  public boolean isAddResultFile() {
    return isaddresult;
  }

  /**
   * @return the runningInParallel
   */
  public boolean isRunningInParallel() {
    return runningInParallel;
  }

  /**
   * @param runningInParallel
   *          the runningInParallel to set
   */
  public void setRunningInParallel( boolean runningInParallel ) {
    this.runningInParallel = runningInParallel;
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @param space
   *          the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   *          The repository to optionally load other resources from (to be converted to XML)
   * @param metaStore
   *          the metaStore in which non-kettle metadata could reside.
   *
   * @return the filename of the exported resource
   */
  @Override
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      // In case the name of the file comes from previous steps, forget about this!
      //
      if ( Utils.isEmpty( filenameField ) && !Utils.isEmpty( filename ) ) {
        // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.csv
        // To : /home/matt/test/files/foo/bar.csv
        //
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( filename ), space );

        // If the file doesn't exist, forget about this effort too!
        //
        if ( fileObject.exists() ) {
          // Convert to an absolute path...
          //
          filename = resourceNamingInterface.nameResource( fileObject, space, true );

          return filename;
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {

    return this;
  }

  @Override
  public void injectStepMetadataEntries( List<StepInjectionMetaEntry> metadata ) {
    for ( StepInjectionMetaEntry entry : metadata ) {
      KettleAttributeInterface attr = findAttribute( entry.getKey() );

      // Set top level attributes...
      //
      if ( entry.getValueType() != ValueMetaInterface.TYPE_NONE ) {
        if ( attr.getKey().equals( "FILENAME" ) ) {
          filename = (String) entry.getValue();
        } else if ( attr.getKey().equals( "FILENAME_FIELD" ) ) {
          filenameField = (String) entry.getValue();
        } else if ( attr.getKey().equals( "ROW_NUM_FIELD" ) ) {
          rowNumField = (String) entry.getValue();
        } else if ( attr.getKey().equals( "HEADER_PRESENT" ) ) {
          headerPresent = (Boolean) entry.getValue();
        } else if ( attr.getKey().equals( "DELIMITER" ) ) {
          delimiter = (String) entry.getValue();
        } else if ( attr.getKey().equals( "ENCLOSURE" ) ) {
          enclosure = (String) entry.getValue();
        } else if ( attr.getKey().equals( "BUFFERSIZE" ) ) {
          bufferSize = (String) entry.getValue();
        } else if ( attr.getKey().equals( "LAZY_CONVERSION" ) ) {
          lazyConversionActive = (Boolean) entry.getValue();
        } else if ( attr.getKey().equals( "PARALLEL" ) ) {
          runningInParallel = (Boolean) entry.getValue();
        } else if ( attr.getKey().equals( "NEWLINE_POSSIBLE" ) ) {
          newlinePossibleInFields = (Boolean) entry.getValue();
        } else if ( attr.getKey().equals( "ADD_FILENAME_RESULT" ) ) {
          isaddresult = (Boolean) entry.getValue();
        } else if ( attr.getKey().equals( "ENCODING" ) ) {
          encoding = (String) entry.getValue();
        } else {
          throw new RuntimeException( "Unhandled metadata injection of attribute: "
            + attr.toString() + " - " + attr.getDescription() );
        }
      } else {
        if ( attr.getKey().equals( "FIELDS" ) ) {
          // This entry contains a list of lists...
          // Each list contains a single CSV input field definition (one line in the dialog)
          //
          List<StepInjectionMetaEntry> inputFieldEntries = entry.getDetails();
          inputFields = new TextFileInputField[inputFieldEntries.size()];
          for ( int row = 0; row < inputFieldEntries.size(); row++ ) {
            StepInjectionMetaEntry inputFieldEntry = inputFieldEntries.get( row );
            TextFileInputField inputField = new TextFileInputField();

            List<StepInjectionMetaEntry> fieldAttributes = inputFieldEntry.getDetails();
            for ( int i = 0; i < fieldAttributes.size(); i++ ) {
              StepInjectionMetaEntry fieldAttribute = fieldAttributes.get( i );
              KettleAttributeInterface fieldAttr = findAttribute( fieldAttribute.getKey() );

              String attributeValue = (String) fieldAttribute.getValue();
              if ( fieldAttr.getKey().equals( "FIELD_NAME" ) ) {
                inputField.setName( attributeValue );
              } else if ( fieldAttr.getKey().equals( "FIELD_TYPE" ) ) {
                inputField.setType( ValueMetaFactory.getIdForValueMeta( attributeValue ) );
              } else if ( fieldAttr.getKey().equals( "FIELD_FORMAT" ) ) {
                inputField.setFormat( attributeValue );
              } else if ( fieldAttr.getKey().equals( "FIELD_LENGTH" ) ) {
                inputField.setLength( attributeValue == null ? -1 : Integer.parseInt( attributeValue ) );
              } else if ( fieldAttr.getKey().equals( "FIELD_PRECISION" ) ) {
                inputField.setPrecision( attributeValue == null ? -1 : Integer.parseInt( attributeValue ) );
              } else if ( fieldAttr.getKey().equals( "FIELD_CURRENCY" ) ) {
                inputField.setCurrencySymbol( attributeValue );
              } else if ( fieldAttr.getKey().equals( "FIELD_DECIMAL" ) ) {
                inputField.setDecimalSymbol( attributeValue );
              } else if ( fieldAttr.getKey().equals( "FIELD_GROUP" ) ) {
                inputField.setGroupSymbol( attributeValue );
              } else if ( fieldAttr.getKey().equals( "FIELD_TRIM_TYPE" ) ) {
                inputField.setTrimType( ValueMetaString.getTrimTypeByCode( attributeValue ) );
              } else {
                throw new RuntimeException( "Unhandled metadata injection of attribute: "
                  + fieldAttr.toString() + " - " + fieldAttr.getDescription() );
              }
            }

            inputFields[row] = inputField;
          }
        }
      }
    }
  }

  @Override
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  /**
   * Describe the metadata attributes that can be injected into this step metadata object.
   *
   * @throws KettleException
   */
  @Override
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    return getStepInjectionMetadataEntries( PKG );
  }

  /**
   * @return the newlinePossibleInFields
   */
  public boolean isNewlinePossibleInFields() {
    return newlinePossibleInFields;
  }

  /**
   * @param newlinePossibleInFields
   *          the newlinePossibleInFields to set
   */
  public void setNewlinePossibleInFields( boolean newlinePossibleInFields ) {
    this.newlinePossibleInFields = newlinePossibleInFields;
  }

}
