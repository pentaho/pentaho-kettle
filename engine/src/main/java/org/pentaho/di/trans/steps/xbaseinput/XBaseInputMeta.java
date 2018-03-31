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

package org.pentaho.di.trans.steps.xbaseinput;

import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
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
 * Created on 2-jun-2003
 *
 */

public class XBaseInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = XBaseInputMeta.class; // for i18n purposes, needed by Translator2!!

  private String dbfFileName;
  private int rowLimit;
  private boolean rowNrAdded;
  private String rowNrField;

  /** Are we accepting filenames in input rows? */
  private boolean acceptingFilenames;

  /** The field in which the filename is placed */
  private String acceptingField;

  /** The stepname to accept filenames from */
  private String acceptingStepName;

  /** The step to accept filenames from */
  private StepMeta acceptingStep;

  /** Flag indicating that we should include the filename in the output */
  private boolean includeFilename;

  /** The name of the field in the output containing the filename */
  private String filenameField;

  /** The character set / encoding used in the string or memo fields */
  private String charactersetName;

  public XBaseInputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the dbfFileName.
   */
  public String getDbfFileName() {
    return dbfFileName;
  }

  /**
   * @param dbfFileName
   *          The dbfFileName to set.
   */
  public void setDbfFileName( String dbfFileName ) {
    this.dbfFileName = dbfFileName;
  }

  /**
   * @return Returns the rowLimit.
   */
  public int getRowLimit() {
    return rowLimit;
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  public void setRowLimit( int rowLimit ) {
    this.rowLimit = rowLimit;
  }

  /**
   * @return Returns the rowNrField.
   */
  public String getRowNrField() {
    return rowNrField;
  }

  /**
   * @param rowNrField
   *          The rowNrField to set.
   */
  public void setRowNrField( String rowNrField ) {
    this.rowNrField = rowNrField;
  }

  /**
   * @return Returns the rowNrAdded.
   */
  public boolean isRowNrAdded() {
    return rowNrAdded;
  }

  /**
   * @param rowNrAdded
   *          The rowNrAdded to set.
   */
  public void setRowNrAdded( boolean rowNrAdded ) {
    this.rowNrAdded = rowNrAdded;
  }

  /**
   * @return Returns the acceptingField.
   */
  public String getAcceptingField() {
    return acceptingField;
  }

  /**
   * @param acceptingField
   *          The acceptingField to set.
   */
  public void setAcceptingField( String acceptingField ) {
    this.acceptingField = acceptingField;
  }

  /**
   * @return Returns the acceptingFilenames.
   */
  public boolean isAcceptingFilenames() {
    return acceptingFilenames;
  }

  /**
   * @param acceptingFilenames
   *          The acceptingFilenames to set.
   */
  public void setAcceptingFilenames( boolean acceptingFilenames ) {
    this.acceptingFilenames = acceptingFilenames;
  }

  /**
   * @return Returns the acceptingStep.
   */
  public StepMeta getAcceptingStep() {
    return acceptingStep;
  }

  /**
   * @param acceptingStep
   *          The acceptingStep to set.
   */
  public void setAcceptingStep( StepMeta acceptingStep ) {
    this.acceptingStep = acceptingStep;
  }

  /**
   * @return Returns the acceptingStepName.
   */
  public String getAcceptingStepName() {
    return acceptingStepName;
  }

  /**
   * @param acceptingStepName
   *          The acceptingStepName to set.
   */
  public void setAcceptingStepName( String acceptingStepName ) {
    this.acceptingStepName = acceptingStepName;
  }

  /**
   * @return Returns the filenameField.
   */
  public String getFilenameField() {
    return filenameField;
  }

  /**
   * @param filenameField
   *          The filenameField to set.
   */
  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  /**
   * @return Returns the includeFilename.
   */
  public boolean includeFilename() {
    return includeFilename;
  }

  /**
   * @param includeFilename
   *          The includeFilename to set.
   */
  public void setIncludeFilename( boolean includeFilename ) {
    this.includeFilename = includeFilename;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    XBaseInputMeta retval = (XBaseInputMeta) super.clone();
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      dbfFileName = XMLHandler.getTagValue( stepnode, "file_dbf" );
      rowLimit = Const.toInt( XMLHandler.getTagValue( stepnode, "limit" ), 0 );
      rowNrAdded = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "add_rownr" ) );
      rowNrField = XMLHandler.getTagValue( stepnode, "field_rownr" );

      includeFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include" ) );
      filenameField = XMLHandler.getTagValue( stepnode, "include_field" );
      charactersetName = XMLHandler.getTagValue( stepnode, "charset_name" );

      acceptingFilenames = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "accept_filenames" ) );
      acceptingField = XMLHandler.getTagValue( stepnode, "accept_field" );
      acceptingStepName = XMLHandler.getTagValue( stepnode, "accept_stepname" );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "XBaseInputMeta.Exception.UnableToReadStepInformationFromXML" ), e );
    }
  }

  @Override
  public void setDefault() {
    dbfFileName = null;
    rowLimit = 0;
    rowNrAdded = false;
    rowNrField = null;
  }

  public String getLookupStepname() {
    if ( acceptingFilenames && acceptingStep != null && !Utils.isEmpty( acceptingStep.getName() ) ) {
      return acceptingStep.getName();
    }
    return null;
  }

  @Override
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    acceptingStep = StepMeta.findStep( steps, acceptingStepName );
  }

  public String[] getInfoSteps() {
    if ( acceptingFilenames && acceptingStep != null ) {
      return new String[] { acceptingStep.getName() };
    }
    return null;
  }

  public RowMetaInterface getOutputFields( FileInputList files, String name ) throws KettleStepException {
    RowMetaInterface rowMeta = new RowMeta();

    // Take the first file to determine what the layout is...
    //
    XBase xbi = null;
    try {
      xbi = new XBase( getLog(), KettleVFS.getInputStream( files.getFile( 0 ) ) );
      xbi.setDbfFile( files.getFile( 0 ).getName().getURI() );
      xbi.open();
      RowMetaInterface add = xbi.getFields();
      for ( int i = 0; i < add.size(); i++ ) {
        ValueMetaInterface v = add.getValueMeta( i );
        v.setOrigin( name );
      }
      rowMeta.addRowMeta( add );
    } catch ( Exception ke ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "XBaseInputMeta.Exception.UnableToReadMetaDataFromXBaseFile" ), ke );
    } finally {
      if ( xbi != null ) {
        xbi.close();
      }
    }

    if ( rowNrAdded && rowNrField != null && rowNrField.length() > 0 ) {
      ValueMetaInterface rnr = new ValueMetaInteger( rowNrField );
      rnr.setOrigin( name );
      rowMeta.addValueMeta( rnr );
    }

    if ( includeFilename ) {
      ValueMetaInterface v = new ValueMetaString( filenameField );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      rowMeta.addValueMeta( v );
    }
    return rowMeta;
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    FileInputList fileList = getTextFileList( space );
    if ( fileList.nrOfFiles() == 0 ) {
      throw new KettleStepException( BaseMessages
        .getString( PKG, "XBaseInputMeta.Exception.NoFilesFoundToProcess" ) );
    }

    row.addRowMeta( getOutputFields( fileList, name ) );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( "file_dbf", dbfFileName ) );
    retval.append( "    " + XMLHandler.addTagValue( "limit", rowLimit ) );
    retval.append( "    " + XMLHandler.addTagValue( "add_rownr", rowNrAdded ) );
    retval.append( "    " + XMLHandler.addTagValue( "field_rownr", rowNrField ) );

    retval.append( "    " + XMLHandler.addTagValue( "include", includeFilename ) );
    retval.append( "    " + XMLHandler.addTagValue( "include_field", filenameField ) );
    retval.append( "    " + XMLHandler.addTagValue( "charset_name", charactersetName ) );

    retval.append( "    " + XMLHandler.addTagValue( "accept_filenames", acceptingFilenames ) );
    retval.append( "    " + XMLHandler.addTagValue( "accept_field", acceptingField ) );
    if ( ( acceptingStepName == null ) && ( acceptingStep != null ) ) {
      acceptingStepName = acceptingStep.getName();
    }
    retval.append( "    "
      + XMLHandler.addTagValue( "accept_stepname", acceptingStepName ) );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      dbfFileName = rep.getStepAttributeString( id_step, "file_dbf" );
      rowLimit = (int) rep.getStepAttributeInteger( id_step, "limit" );
      rowNrAdded = rep.getStepAttributeBoolean( id_step, "add_rownr" );
      rowNrField = rep.getStepAttributeString( id_step, "field_rownr" );

      includeFilename = rep.getStepAttributeBoolean( id_step, "include" );
      filenameField = rep.getStepAttributeString( id_step, "include_field" );
      charactersetName = rep.getStepAttributeString( id_step, "charset_name" );

      acceptingFilenames = rep.getStepAttributeBoolean( id_step, "accept_filenames" );
      acceptingField = rep.getStepAttributeString( id_step, "accept_field" );
      acceptingStepName = rep.getStepAttributeString( id_step, "accept_stepname" );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "XBaseInputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "file_dbf", dbfFileName );
      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, "add_rownr", rowNrAdded );
      rep.saveStepAttribute( id_transformation, id_step, "field_rownr", rowNrField );

      rep.saveStepAttribute( id_transformation, id_step, "include", includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, "include_field", filenameField );
      rep.saveStepAttribute( id_transformation, id_step, "charset_name", charactersetName );

      rep.saveStepAttribute( id_transformation, id_step, "accept_filenames", acceptingFilenames );
      rep.saveStepAttribute( id_transformation, id_step, "accept_field", acceptingField );
      if ( ( acceptingStepName == null ) && ( acceptingStep != null ) ) {
        acceptingStepName = acceptingStep.getName();
      }
      rep.saveStepAttribute( id_transformation, id_step, "accept_stepname", acceptingStepName );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "XBaseInputMeta.Exception.UnableToSaveMetaDataToRepository" )
        + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    CheckResult cr;

    if ( dbfFileName == null ) {
      if ( isAcceptingFilenames() ) {
        if ( Utils.isEmpty( getAcceptingStepName() ) ) {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "XBaseInput.Log.Error.InvalidAcceptingStepName" ), stepMeta );
          remarks.add( cr );
        }

        if ( Utils.isEmpty( getAcceptingField() ) ) {
          cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
              PKG, "XBaseInput.Log.Error.InvalidAcceptingFieldName" ), stepMeta );
          remarks.add( cr );
        }
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "XBaseInputMeta.Remark.PleaseSelectFileToUse" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "XBaseInputMeta.Remark.FileToUseIsSpecified" ), stepMeta );
      remarks.add( cr );

      XBase xbi = new XBase( getLog(), transMeta.environmentSubstitute( dbfFileName ) );
      try {
        xbi.open();
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "XBaseInputMeta.Remark.FileExistsAndCanBeOpened" ), stepMeta );
        remarks.add( cr );

        RowMetaInterface r = xbi.getFields();

        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, r.size()
            + BaseMessages.getString( PKG, "XBaseInputMeta.Remark.OutputFieldsCouldBeDetermined" ), stepMeta );
        remarks.add( cr );
      } catch ( KettleException ke ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "XBaseInputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError" )
            + Const.CR + ke.getMessage(), stepMeta );
        remarks.add( cr );
      } finally {
        xbi.close();
      }
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new XBaseInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new XBaseInputData();
  }

  public String[] getFilePaths( VariableSpace space ) {
    return FileInputList.createFilePathList(
      space, new String[] { dbfFileName }, new String[] { null }, new String[] { null }, new String[] { "N" } );
  }

  public FileInputList getTextFileList( VariableSpace space ) {
    return FileInputList.createFileList(
      space, new String[] { dbfFileName }, new String[] { null }, new String[] { null }, new String[] { "N" } );
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "javadbf.jar", };
  }

  /**
   * @return the charactersetName
   */
  public String getCharactersetName() {
    return charactersetName;
  }

  /**
   * @param charactersetName
   *          the charactersetName to set
   */
  public void setCharactersetName( String charactersetName ) {
    this.charactersetName = charactersetName;
  }

  /**
   * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively. So
   * what this does is turn the name of files into absolute paths OR it simply includes the resource in the ZIP file.
   * For now, we'll simply turn it into an absolute path and pray that the file is on a shared drive or something like
   * that.
   *
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
      if ( !acceptingFilenames ) {
        // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.dbf
        // To : /home/matt/test/files/foo/bar.dbf
        //
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( dbfFileName ), space );

        // If the file doesn't exist, forget about this effort too!
        //
        if ( fileObject.exists() ) {
          // Convert to an absolute path...
          //
          dbfFileName = resourceNamingInterface.nameResource( fileObject, space, true );

          return dbfFileName;
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

}
