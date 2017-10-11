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

package org.pentaho.di.trans.steps.filestoresult;

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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

/**
 *
 * @author matt
 * @since 26-may-2006
 *
 */

public class FilesToResultMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = FilesToResultMeta.class; // for i18n purposes, needed by Translator2!!

  private String filenameField;

  private int fileType;

  /**
   * @return Returns the fieldname that contains the filename.
   */
  public String getFilenameField() {
    return filenameField;
  }

  /**
   * @param filenameField
   *          set the fieldname that contains the filename.
   */
  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  /**
   * @return Returns the fileType.
   * @see ResultFile
   */
  public int getFileType() {
    return fileType;
  }

  /**
   * @param fileType
   *          The fileType to set.
   * @see ResultFile
   */
  public void setFileType( int fileType ) {
    this.fileType = fileType;
  }

  public FilesToResultMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder();

    xml.append( XMLHandler.addTagValue( "filename_field", filenameField ) );
    xml.append( XMLHandler.addTagValue( "file_type", ResultFile.getTypeCode( fileType ) ) );

    return xml.toString();
  }

  private void readData( Node stepnode ) {
    filenameField = XMLHandler.getTagValue( stepnode, "filename_field" );
    fileType = ResultFile.getType( XMLHandler.getTagValue( stepnode, "file_type" ) );
  }

  public void setDefault() {
    filenameField = null;
    fileType = ResultFile.FILE_TYPE_GENERAL;
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    filenameField = rep.getStepAttributeString( id_step, "filename_field" );
    fileType = ResultFile.getType( rep.getStepAttributeString( id_step, "file_type" ) );
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "filename_field", filenameField );
    rep.saveStepAttribute( id_transformation, id_step, "file_type", ResultFile.getTypeCode( fileType ) );
  }

  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "FilesToResultMeta.CheckResult.StepReceivingInfoFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    } else {
      CheckResult cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "FilesToResultMeta.CheckResult.NoInputReceivedError" ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new FilesToResult( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new FilesToResultData();
  }

}
