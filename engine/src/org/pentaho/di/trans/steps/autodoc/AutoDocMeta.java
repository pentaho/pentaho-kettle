/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.di.trans.steps.autodoc;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaSerializable;
import org.pentaho.di.core.row.value.ValueMetaString;
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
import org.pentaho.di.trans.steps.autodoc.KettleReportBuilder.OutputType;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @since 2009-12-01
 * @author matt
 * @version 4
 */
public class AutoDocMeta extends BaseStepMeta implements StepMetaInterface, AutoDocOptionsInterface {
  private static Class<?> PKG = AutoDocMeta.class; // for i18n purposes, needed by Translator2!!

  private String filenameField;
  private String fileTypeField;

  private String targetFilename;
  private OutputType outputType;
  private boolean includingName;
  private boolean includingDescription;
  private boolean includingExtendedDescription;
  private boolean includingCreated;
  private boolean includingModified;
  private boolean includingImage;
  private boolean includingLoggingConfiguration;
  private boolean includingLastExecutionResult;
  private boolean includingImageAreaList;

  public boolean isIncludingImageAreaList() {
    return includingImageAreaList;
  }

  public void setIncludingImageAreaList( boolean includingImageAreaList ) {
    this.includingImageAreaList = includingImageAreaList;
  }

  public AutoDocMeta() {
    super(); // allocate BaseStepMeta

    outputType = OutputType.PDF;
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
    outputType = OutputType.PDF;
    targetFilename = "${Internal.Entry.Current.Directory}/kettle-autodoc.pdf";
    includingName = true;
    includingDescription = true;
    includingExtendedDescription = true;
    includingCreated = true;
    includingModified = true;
    includingImage = true;
    includingLoggingConfiguration = true;
    includingLastExecutionResult = true;
    includingLastExecutionResult = false;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      filenameField = XMLHandler.getTagValue( stepnode, "filename_field" );
      fileTypeField = XMLHandler.getTagValue( stepnode, "file_type_field" );
      targetFilename = XMLHandler.getTagValue( stepnode, "target_file" );
      includingName = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_name" ) );
      includingDescription = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_description" ) );
      includingExtendedDescription =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_extended_description" ) );
      includingCreated = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_creation" ) );
      includingModified = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_modification" ) );
      includingImage = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_image" ) );
      includingLoggingConfiguration =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_logging_config" ) );
      includingLastExecutionResult =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_last_exec_result" ) );
      includingImageAreaList =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include_image_area_list" ) );

      try {
        outputType = KettleReportBuilder.OutputType.valueOf( XMLHandler.getTagValue( stepnode, "output_type" ) );
      } catch ( Exception e ) {
        outputType = KettleReportBuilder.OutputType.PDF;
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void allocate() {
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "filename_field", filenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "file_type_field", fileTypeField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "target_file", targetFilename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "output_type", outputType.name() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_name", includingName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_description", includingDescription ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "include_extended_description", includingExtendedDescription ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_creation", includingCreated ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_modification", includingModified ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_image", includingImage ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "include_logging_config", includingLoggingConfiguration ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "include_last_exec_result", includingLastExecutionResult ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_image_area_list", includingImageAreaList ) );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      filenameField = rep.getStepAttributeString( id_step, "filename_field" );
      fileTypeField = rep.getStepAttributeString( id_step, "file_type_field" );
      targetFilename = rep.getStepAttributeString( id_step, "target_file" );
      try {
        outputType = KettleReportBuilder.OutputType.valueOf( rep.getStepAttributeString( id_step, "output_type" ) );
      } catch ( Exception e ) {
        outputType = KettleReportBuilder.OutputType.PDF;
      }
      includingName = rep.getStepAttributeBoolean( id_step, "include_name" );
      includingDescription = rep.getStepAttributeBoolean( id_step, "include_description" );
      includingExtendedDescription = rep.getStepAttributeBoolean( id_step, "include_extended_description" );
      includingCreated = rep.getStepAttributeBoolean( id_step, "include_creation" );
      includingModified = rep.getStepAttributeBoolean( id_step, "include_modification" );
      includingImage = rep.getStepAttributeBoolean( id_step, "include_image" );
      includingLoggingConfiguration = rep.getStepAttributeBoolean( id_step, "include_logging_config" );
      includingLastExecutionResult = rep.getStepAttributeBoolean( id_step, "include_last_exec_result" );
      includingImageAreaList = rep.getStepAttributeBoolean( id_step, "include_image_area_list" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "filename_field", filenameField );
      rep.saveStepAttribute( id_transformation, id_step, "file_type_field", fileTypeField );
      rep.saveStepAttribute( id_transformation, id_step, "target_file", targetFilename );
      rep.saveStepAttribute( id_transformation, id_step, "output_type", outputType.name() );
      rep.saveStepAttribute( id_transformation, id_step, "include_name", includingName );
      rep.saveStepAttribute( id_transformation, id_step, "include_description", includingDescription );
      rep.saveStepAttribute(
        id_transformation, id_step, "include_extended_description", includingExtendedDescription );
      rep.saveStepAttribute( id_transformation, id_step, "include_creation", includingCreated );
      rep.saveStepAttribute( id_transformation, id_step, "include_modification", includingModified );
      rep.saveStepAttribute( id_transformation, id_step, "include_image", includingImage );
      rep.saveStepAttribute( id_transformation, id_step, "include_logging_config", includingLoggingConfiguration );
      rep.saveStepAttribute( id_transformation, id_step, "include_last_exec_result", includingLastExecutionResult );
      rep.saveStepAttribute( id_transformation, id_step, "include_image_area_list", includingImageAreaList );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( outputType == OutputType.METADATA ) {

      // Add a bunch of metadata to the output for each input row
      //
      ValueMetaInterface valueMeta = new ValueMetaSerializable( "meta" );
      valueMeta.setOrigin( origin );
      rowMeta.addValueMeta( valueMeta );

      if ( includingName ) {
        valueMeta = new ValueMetaString( "name" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingDescription ) {
        valueMeta = new ValueMetaString( "description" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingExtendedDescription ) {
        valueMeta = new ValueMetaString( "extended_description" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingCreated ) {
        valueMeta = new ValueMetaString( "created" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingModified ) {
        valueMeta = new ValueMetaString( "modified" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingImage ) {
        valueMeta = new ValueMetaBinary( "image" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingLoggingConfiguration ) {
        valueMeta = new ValueMetaString( "logging" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingLastExecutionResult ) {
        valueMeta = new ValueMetaString( "last_result" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
      if ( includingImageAreaList ) {
        valueMeta = new ValueMetaSerializable( "area" );
        valueMeta.setOrigin( origin );
        rowMeta.addValueMeta( valueMeta );
      }
    } else {

      rowMeta.clear(); // Start with a clean slate, eats the input

      // Generate one report in the output...
      //
      ValueMetaInterface valueMeta = new ValueMetaString( "filename" );
      valueMeta.setOrigin( origin );
      rowMeta.addValueMeta( valueMeta );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "AutoDocMeta.CheckResult.NotReceivingFields" ), stepinfo );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "AutoDocMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepinfo );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "AutoDocMeta.CheckResult.StepRecevingData2" ), stepinfo );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "AutoDocMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepinfo );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new AutoDoc( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new AutoDocData();
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
   * @return the targetFilename
   */
  public String getTargetFilename() {
    return targetFilename;
  }

  /**
   * @param targetFilename
   *          the targetFilename to set
   */
  public void setTargetFilename( String targetFilename ) {
    this.targetFilename = targetFilename;
  }

  /**
   * @return the outputType
   */
  @Override
  public OutputType getOutputType() {
    return outputType;
  }

  /**
   * @param outputType
   *          the outputType to set
   */
  public void setOutputType( OutputType outputType ) {
    this.outputType = outputType;
  }

  /**
   * @return the includingDescription
   */
  @Override
  public boolean isIncludingDescription() {
    return includingDescription;
  }

  /**
   * @param includingDescription
   *          the includingDescription to set
   */
  public void setIncludingDescription( boolean includingDescription ) {
    this.includingDescription = includingDescription;
  }

  /**
   * @return the includingCreated
   */
  @Override
  public boolean isIncludingCreated() {
    return includingCreated;
  }

  /**
   * @param includingCreated
   *          the includingCreated to set
   */
  public void setIncludingCreated( boolean includingCreated ) {
    this.includingCreated = includingCreated;
  }

  /**
   * @return the includingModified
   */
  @Override
  public boolean isIncludingModified() {
    return includingModified;
  }

  /**
   * @param includingModified
   *          the includingModified to set
   */
  public void setIncludingModified( boolean includingModified ) {
    this.includingModified = includingModified;
  }

  /**
   * @return the includingImage
   */
  @Override
  public boolean isIncludingImage() {
    return includingImage;
  }

  /**
   * @param includingImage
   *          the includingImage to set
   */
  public void setIncludingImage( boolean includingImage ) {
    this.includingImage = includingImage;
  }

  /**
   * @return the includingLoggingConfiguration
   */
  @Override
  public boolean isIncludingLoggingConfiguration() {
    return includingLoggingConfiguration;
  }

  /**
   * @param includingLoggingConfiguration
   *          the includingLoggingConfiguration to set
   */
  public void setIncludingLoggingConfiguration( boolean includingLoggingConfiguration ) {
    this.includingLoggingConfiguration = includingLoggingConfiguration;
  }

  /**
   * @return the includingLastExecutionResult
   */
  @Override
  public boolean isIncludingLastExecutionResult() {
    return includingLastExecutionResult;
  }

  /**
   * @param includingLastExecutionResult
   *          the includingLastExecutionResult to set
   */
  public void setIncludingLastExecutionResult( boolean includingLastExecutionResult ) {
    this.includingLastExecutionResult = includingLastExecutionResult;
  }

  /**
   * @return the includingExtendedDescription
   */
  @Override
  public boolean isIncludingExtendedDescription() {
    return includingExtendedDescription;
  }

  /**
   * @param includingExtendedDescription
   *          the includingExtendedDescription to set
   */
  public void setIncludingExtendedDescription( boolean includingExtendedDescription ) {
    this.includingExtendedDescription = includingExtendedDescription;
  }

  /**
   * @return the includingName
   */
  @Override
  public boolean isIncludingName() {
    return includingName;
  }

  /**
   * @param includingName
   *          the includingName to set
   */
  public void setIncludingName( boolean includingName ) {
    this.includingName = includingName;
  }

  /**
   * @return the fileTypeField
   */
  public String getFileTypeField() {
    return fileTypeField;
  }

  /**
   * @param fileTypeField
   *          the fileTypeField to set
   */
  public void setFileTypeField( String fileTypeField ) {
    this.fileTypeField = fileTypeField;
  }
}
