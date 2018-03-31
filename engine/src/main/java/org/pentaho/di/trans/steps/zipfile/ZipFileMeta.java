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

package org.pentaho.di.trans.steps.zipfile;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Created on 03-Juin-2008
 *
 */

public class ZipFileMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ZipFileMeta.class; // for i18n purposes, needed by Translator2!!

  /** dynamic filename */
  private String sourcefilenamefield;
  private String targetfilenamefield;
  private String baseFolderField;

  private String movetofolderfield;

  private boolean addresultfilenames;
  private boolean overwritezipentry;
  private boolean createparentfolder;

  private boolean keepsourcefolder;

  /** Operations type */
  private int operationType;

  /**
   * The operations description
   */
  public static final String[] operationTypeDesc = {
    BaseMessages.getString( PKG, "ZipFileMeta.operationType.DoNothing" ),
    BaseMessages.getString( PKG, "ZipFileMeta.operationType.Move" ),
    BaseMessages.getString( PKG, "ZipFileMeta.operationType.Delete" ) };

  /**
   * The operations type codes
   */
  public static final String[] operationTypeCode = { "", "move", "delete" };

  public static final int OPERATION_TYPE_NOTHING = 0;

  public static final int OPERATION_TYPE_MOVE = 1;

  public static final int OPERATION_TYPE_DELETE = 2;

  public ZipFileMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the sourcefilenamefield.
   */
  public String getDynamicSourceFileNameField() {
    return sourcefilenamefield;
  }

  /**
   * @param sourcefilenamefield
   *          The sourcefilenamefield to set.
   */
  public void setDynamicSourceFileNameField( String sourcefilenamefield ) {
    this.sourcefilenamefield = sourcefilenamefield;
  }

  /**
   * @return Returns the baseFolderField.
   */
  public String getBaseFolderField() {
    return baseFolderField;
  }

  /**
   * @param baseFolderField
   *          The baseFolderField to set.
   */
  public void setBaseFolderField( String baseFolderField ) {
    this.baseFolderField = baseFolderField;
  }

  /**
   * @return Returns the movetofolderfield.
   */
  public String getMoveToFolderField() {
    return movetofolderfield;
  }

  /**
   * @param movetofolderfield
   *          The movetofolderfield to set.
   */
  public void setMoveToFolderField( String movetofolderfield ) {
    this.movetofolderfield = movetofolderfield;
  }

  /**
   * @return Returns the targetfilenamefield.
   */
  public String getDynamicTargetFileNameField() {
    return targetfilenamefield;
  }

  /**
   * @param targetfilenamefield
   *          The targetfilenamefield to set.
   */
  public void setDynamicTargetFileNameField( String targetfilenamefield ) {
    this.targetfilenamefield = targetfilenamefield;
  }

  public boolean isaddTargetFileNametoResult() {
    return addresultfilenames;
  }

  public boolean isOverwriteZipEntry() {
    return overwritezipentry;
  }

  public boolean isCreateParentFolder() {
    return createparentfolder;
  }

  public boolean isKeepSouceFolder() {
    return keepsourcefolder;
  }

  public void setKeepSouceFolder( boolean value ) {
    keepsourcefolder = value;
  }

  public void setaddTargetFileNametoResult( boolean addresultfilenames ) {
    this.addresultfilenames = addresultfilenames;
  }

  public void setOverwriteZipEntry( boolean overwritezipentry ) {
    this.overwritezipentry = overwritezipentry;
  }

  public void setCreateParentFolder( boolean createparentfolder ) {
    this.createparentfolder = createparentfolder;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    ZipFileMeta retval = (ZipFileMeta) super.clone();

    return retval;
  }

  public void setDefault() {
    addresultfilenames = false;
    overwritezipentry = false;
    createparentfolder = false;
    keepsourcefolder = false;
    operationType = OPERATION_TYPE_NOTHING;
  }

  private static String getOperationTypeCode( int i ) {
    if ( i < 0 || i >= operationTypeCode.length ) {
      return operationTypeCode[0];
    }
    return operationTypeCode[i];
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( "sourcefilenamefield", sourcefilenamefield ) );
    retval.append( "    " + XMLHandler.addTagValue( "targetfilenamefield", targetfilenamefield ) );
    retval.append( "    " + XMLHandler.addTagValue( "baseFolderField", baseFolderField ) );
    retval.append( "    " + XMLHandler.addTagValue( "operation_type", getOperationTypeCode( operationType ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "addresultfilenames", addresultfilenames ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "overwritezipentry", overwritezipentry ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "createparentfolder", createparentfolder ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "keepsourcefolder", keepsourcefolder ) );
    retval.append( "    " + XMLHandler.addTagValue( "movetofolderfield", movetofolderfield ) );
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( sourcefilenamefield );
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( targetfilenamefield );
    parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( baseFolderField );
    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      sourcefilenamefield = XMLHandler.getTagValue( stepnode, "sourcefilenamefield" );
      targetfilenamefield = XMLHandler.getTagValue( stepnode, "targetfilenamefield" );
      baseFolderField = XMLHandler.getTagValue( stepnode, "baseFolderField" );
      operationType =
        getOperationTypeByCode( Const.NVL( XMLHandler.getTagValue( stepnode, "operation_type" ), "" ) );
      addresultfilenames = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addresultfilenames" ) );
      overwritezipentry = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "overwritezipentry" ) );
      createparentfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "createparentfolder" ) );
      keepsourcefolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "keepsourcefolder" ) );
      movetofolderfield = XMLHandler.getTagValue( stepnode, "movetofolderfield" );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "ZipFileMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      sourcefilenamefield = rep.getStepAttributeString( id_step, "sourcefilenamefield" );
      targetfilenamefield = rep.getStepAttributeString( id_step, "targetfilenamefield" );
      baseFolderField = rep.getStepAttributeString( id_step, "baseFolderField" );
      operationType =
        getOperationTypeByCode( Const.NVL( rep.getStepAttributeString( id_step, "operation_type" ), "" ) );
      addresultfilenames = rep.getStepAttributeBoolean( id_step, "addresultfilenames" );
      overwritezipentry = rep.getStepAttributeBoolean( id_step, "overwritezipentry" );
      createparentfolder = rep.getStepAttributeBoolean( id_step, "createparentfolder" );
      keepsourcefolder = rep.getStepAttributeBoolean( id_step, "keepsourcefolder" );
      movetofolderfield = rep.getStepAttributeString( id_step, "movetofolderfield" );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "ZipFileMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "sourcefilenamefield", sourcefilenamefield );
      rep.saveStepAttribute( id_transformation, id_step, "targetfilenamefield", targetfilenamefield );
      rep.saveStepAttribute( id_transformation, id_step, "baseFolderField", baseFolderField );
      rep.saveStepAttribute( id_transformation, id_step, "operation_type", getOperationTypeCode( operationType ) );
      rep.saveStepAttribute( id_transformation, id_step, "addresultfilenames", addresultfilenames );
      rep.saveStepAttribute( id_transformation, id_step, "overwritezipentry", overwritezipentry );
      rep.saveStepAttribute( id_transformation, id_step, "createparentfolder", createparentfolder );
      rep.saveStepAttribute( id_transformation, id_step, "keepsourcefolder", keepsourcefolder );
      rep.saveStepAttribute( id_transformation, id_step, "movetofolderfield", movetofolderfield );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "ZipFileMeta.Exception.UnableToSaveStepInfo" )
        + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;
    String error_message = "";

    // source filename
    if ( Utils.isEmpty( sourcefilenamefield ) ) {
      error_message = BaseMessages.getString( PKG, "ZipFileMeta.CheckResult.SourceFileFieldMissing" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    } else {
      error_message = BaseMessages.getString( PKG, "ZipFileMeta.CheckResult.TargetFileFieldOK" );
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ZipFileMeta.CheckResult.ReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ZipFileMeta.CheckResult.NoInpuReceived" ), stepMeta );
      remarks.add( cr );
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ZipFile( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new ZipFileData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  public int getOperationType() {
    return operationType;
  }

  public static int getOperationTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < operationTypeDesc.length; i++ ) {
      if ( operationTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getOperationTypeByCode( tt );
  }

  public void setOperationType( int operationType ) {
    this.operationType = operationType;
  }

  public static String getOperationTypeDesc( int i ) {
    if ( i < 0 || i >= operationTypeDesc.length ) {
      return operationTypeDesc[0];
    }
    return operationTypeDesc[i];
  }

  private static int getOperationTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < operationTypeCode.length; i++ ) {
      if ( operationTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }
}
