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

package org.pentaho.di.trans.steps.getsubfolders;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
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

/**
 * @author Samatar
 * @since 18-July-2008
 */

public class GetSubFoldersMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = GetSubFoldersMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFoldersDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFoldersCode = new String[] { "N", "Y" };

  public static final String NO = "N";

  /** Array of filenames */
  private String[] folderName;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] folderRequired;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The name of the field in the output containing the foldername */
  private String dynamicFoldernameField;

  /** folder name from previous fields **/
  private boolean isFoldernameDynamic;

  /** The maximum number or lines to read */
  private long rowLimit;

  public GetSubFoldersMeta() {
    super(); // allocate BaseStepMeta
  }

  public String getRequiredFilesDesc( String tt ) {
    if ( Utils.isEmpty( tt ) ) {
      return RequiredFoldersDesc[0];
    }
    if ( tt.equalsIgnoreCase( RequiredFoldersCode[1] ) ) {
      return RequiredFoldersDesc[1];
    } else {
      return RequiredFoldersDesc[0];
    }
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @param dynamicFoldernameField
   *          The dynamic foldername field to set.
   */
  public void setDynamicFoldernameField( String dynamicFoldernameField ) {
    this.dynamicFoldernameField = dynamicFoldernameField;
  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  /**
   * @return Returns the dynamic folder field (from previous steps)
   */
  public String getDynamicFoldernameField() {
    return dynamicFoldernameField;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @return Returns the dynamic foldername flag.
   */
  public boolean isFoldernameDynamic() {
    return isFoldernameDynamic;
  }

  /**
   * @param isFoldernameDynamic
   *          The isFoldernameDynamic to set.
   */
  public void setFolderField( boolean isFoldernameDynamic ) {
    this.isFoldernameDynamic = isFoldernameDynamic;
  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @return Returns the folderRequired.
   */
  public String[] getFolderRequired() {
    return folderRequired;
  }

  public String getRequiredFoldersCode( String tt ) {
    if ( tt == null ) {
      return RequiredFoldersCode[0];
    }
    if ( tt.equals( RequiredFoldersDesc[1] ) ) {
      return RequiredFoldersCode[1];
    } else {
      return RequiredFoldersCode[0];
    }
  }

  /**
   * @param folderRequired
   *          The folderRequired to set.
   */

  public void setFolderRequired( String[] folderRequiredin ) {
    this.folderRequired = new String[folderRequiredin.length];
    for ( int i = 0; i < folderRequiredin.length; i++ ) {
      this.folderRequired[i] = getRequiredFoldersCode( folderRequiredin[i] );
    }
  }

  /**
   * @return Returns the folderName.
   */
  public String[] getFolderName() {
    return folderName;
  }

  /**
   * @param folderName
   *          The folderName to set.
   */
  public void setFolderName( String[] folderName ) {
    this.folderName = folderName;
  }

  /**
   * @return Returns the rowLimit.
   */
  public long getRowLimit() {
    return rowLimit;
  }

  /**
   * @param rowLimit
   *          The rowLimit to set.
   */
  public void setRowLimit( long rowLimit ) {
    this.rowLimit = rowLimit;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    GetSubFoldersMeta retval = (GetSubFoldersMeta) super.clone();

    int nrfiles = folderName.length;

    retval.allocate( nrfiles );

    System.arraycopy( folderName, 0, retval.folderName, 0, nrfiles );
    System.arraycopy( folderRequired, 0, retval.folderRequired, 0, nrfiles );

    return retval;
  }

  public void allocate( int nrfiles ) {
    folderName = new String[nrfiles];
    folderRequired = new String[nrfiles];
  }

  public void setDefault() {
    int nrfiles = 0;
    isFoldernameDynamic = false;
    includeRowNumber = false;
    rowNumberField = "";
    dynamicFoldernameField = "";

    allocate( nrfiles );

    for ( int i = 0; i < nrfiles; i++ ) {
      folderName[i] = "folderName" + ( i + 1 );
      folderRequired[i] = NO;
    }
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    // the folderName
    ValueMetaInterface folderName = new ValueMetaString( "folderName" );
    folderName.setLength( 500 );
    folderName.setPrecision( -1 );
    folderName.setOrigin( name );
    row.addValueMeta( folderName );

    // the short folderName
    ValueMetaInterface short_folderName = new ValueMetaString( "short_folderName" );
    short_folderName.setLength( 500 );
    short_folderName.setPrecision( -1 );
    short_folderName.setOrigin( name );
    row.addValueMeta( short_folderName );

    // the path
    ValueMetaInterface path = new ValueMetaString( "path" );
    path.setLength( 500 );
    path.setPrecision( -1 );
    path.setOrigin( name );
    row.addValueMeta( path );

    // the ishidden
    ValueMetaInterface ishidden = new ValueMetaBoolean( "ishidden" );
    ishidden.setOrigin( name );
    row.addValueMeta( ishidden );

    // the isreadable
    ValueMetaInterface isreadable = new ValueMetaBoolean( "isreadable" );
    isreadable.setOrigin( name );
    row.addValueMeta( isreadable );

    // the iswriteable
    ValueMetaInterface iswriteable = new ValueMetaBoolean( "iswriteable" );
    iswriteable.setOrigin( name );
    row.addValueMeta( iswriteable );

    // the lastmodifiedtime
    ValueMetaInterface lastmodifiedtime = new ValueMetaDate( "lastmodifiedtime" );
    lastmodifiedtime.setOrigin( name );
    row.addValueMeta( lastmodifiedtime );

    // the uri
    ValueMetaInterface uri = new ValueMetaString( "uri" );
    uri.setOrigin( name );
    row.addValueMeta( uri );

    // the rooturi
    ValueMetaInterface rooturi = new ValueMetaString( "rooturi" );
    rooturi.setOrigin( name );
    row.addValueMeta( rooturi );

    // childrens
    ValueMetaInterface childrens =
      new ValueMetaInteger( space.environmentSubstitute( "childrens" ) );
    childrens.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
    childrens.setOrigin( name );
    row.addValueMeta( childrens );

    if ( includeRowNumber ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "foldername_dynamic", isFoldernameDynamic ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "foldername_field", dynamicFoldernameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", rowLimit ) );
    retval.append( "    <file>" ).append( Const.CR );

    for ( int i = 0; i < folderName.length; i++ ) {
      retval.append( "      " ).append( XMLHandler.addTagValue( "name", folderName[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "file_required", folderRequired[i] ) );
      parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( folderName[i] );
    }
    retval.append( "    </file>" ).append( Const.CR );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      isFoldernameDynamic = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "foldername_dynamic" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      dynamicFoldernameField = XMLHandler.getTagValue( stepnode, "foldername_field" );

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, "limit" ), 0L );

      Node filenode = XMLHandler.getSubNode( stepnode, "file" );
      int nrfiles = XMLHandler.countNodes( filenode, "name" );

      allocate( nrfiles );

      for ( int i = 0; i < nrfiles; i++ ) {
        Node folderNamenode = XMLHandler.getSubNodeByNr( filenode, "name", i );
        Node folderRequirednode = XMLHandler.getSubNodeByNr( filenode, "file_required", i );
        folderName[i] = XMLHandler.getNodeValue( folderNamenode );
        folderRequired[i] = XMLHandler.getNodeValue( folderRequirednode );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      int nrfiles = rep.countNrStepAttributes( id_step, "file_name" );

      dynamicFoldernameField = rep.getStepAttributeString( id_step, "foldername_field" );

      includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );
      isFoldernameDynamic = rep.getStepAttributeBoolean( id_step, "foldername_dynamic" );
      rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );
      rowLimit = rep.getStepAttributeInteger( id_step, "limit" );

      allocate( nrfiles );

      for ( int i = 0; i < nrfiles; i++ ) {
        folderName[i] = rep.getStepAttributeString( id_step, i, "file_name" );
        folderRequired[i] = rep.getStepAttributeString( id_step, i, "file_required" );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {

      rep.saveStepAttribute( id_transformation, id_step, "rownum", includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "foldername_dynamic", isFoldernameDynamic );
      rep.saveStepAttribute( id_transformation, id_step, "foldername_field", dynamicFoldernameField );

      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );

      for ( int i = 0; i < folderName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "file_name", folderName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_required", folderRequired[i] );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public FileInputList getFolderList( VariableSpace space ) {
    return FileInputList.createFolderList( space, folderName, folderRequired );
  }

  public FileInputList getDynamicFolderList( VariableSpace space, String[] folderName, String[] folderRequired ) {
    return FileInputList.createFolderList( space, folderName, folderRequired );
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // See if we get input...
    if ( isFoldernameDynamic ) {
      if ( input.length > 0 ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.InputOk" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.InputErrorKo" ), stepMeta );
      }
      remarks.add( cr );

      if ( Utils.isEmpty( dynamicFoldernameField ) ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.FolderFieldnameMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.FolderFieldnameOk" ), stepMeta );
      }

      remarks.add( cr );
    } else {
      if ( input.length > 0 ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.NoInputError" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.NoInputOk" ), stepMeta );
      }
      remarks.add( cr );
      // check specified folder names
      FileInputList fileList = getFolderList( transMeta );
      if ( fileList.nrOfFiles() == 0 ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.ExpectedFoldersError" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "GetSubFoldersMeta.CheckResult.ExpectedFilesOk", "" + fileList.nrOfFiles() ), stepMeta );
        remarks.add( cr );
      }
    }

  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new GetSubFolders( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new GetSubFoldersData();
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
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      // In case the name of the file comes from previous steps, forget about this!
      //
      if ( !isFoldernameDynamic ) {
        for ( int i = 0; i < folderName.length; i++ ) {
          FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( folderName[i] ), space );
          folderName[i] = resourceNamingInterface.nameResource( fileObject, space, true );
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

}
