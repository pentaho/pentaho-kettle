/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.file.BaseFileInputAdditionalField;
import org.pentaho.di.trans.steps.file.BaseFileInputFiles;
import org.pentaho.di.trans.steps.file.BaseFileInputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Store run-time data on the JsonInput step.
 */
@Step( id = "JsonInput", image = "JSI.svg", i18nPackageName = "org.pentaho.di.trans.steps.jsoninput",
    documentationUrl = "mk-95pdia003/pdi-transformation-steps/json-input", name = "JsonInput.name",
    description = "JsonInput.description", categoryDescription = "JsonInput.category" )
@InjectionSupported( localizationPrefix = "JsonInput.Injection.", groups = { "FILENAME_LINES", "FIELDS" }, hide = {
  "ACCEPT_FILE_NAMES", "ACCEPT_FILE_STEP", "PASS_THROUGH_FIELDS", "ACCEPT_FILE_FIELD", "ADD_FILES_TO_RESULT",
  "IGNORE_ERRORS", "FILE_ERROR_FIELD", "FILE_ERROR_MESSAGE_FIELD", "SKIP_BAD_FILES", "WARNING_FILES_TARGET_DIR",
  "WARNING_FILES_EXTENTION", "ERROR_FILES_TARGET_DIR", "ERROR_FILES_EXTENTION", "LINE_NR_FILES_TARGET_DIR",
  "LINE_NR_FILES_EXTENTION", "FIELD_NULL_STRING", "FIELD_POSITION", "FIELD_IGNORE", "FIELD_IF_NULL" } )
public class JsonInputMeta extends
    BaseFileInputMeta<JsonInputMeta.AdditionalFileOutputFields, JsonInputMeta.InputFiles, JsonInputField> implements
    StepMetaInterface {
  private static Class<?> PKG = JsonInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };

  private static final String INCLUDE_NULLS = "includeNulls";

  // TextFileInputMeta.Content.includeFilename
  /** Flag indicating that we should include the filename in the output */
  @Injection( name = "FILE_NAME_OUTPUT" )
  private boolean includeFilename; // InputFiles.isaddresult?..

  // TextFileInputMeta.Content.filenameField
  /** The name of the field in the output containing the filename */
  @Injection( name = "FILE_NAME_FIELDNAME" )
  private String filenameField;

  /** Flag indicating that a row number field should be included in the output */
  @Injection( name = "ROW_NUMBER_OUTPUT" )
  private boolean includeRowNumber;

  // TextFileInputMeta.Content.rowNumberField
  /** The name of the field in the output containing the row number */
  @Injection( name = "ROW_NUMBER_FIELDNAME" )
  private String rowNumberField;

  // TextFileInputMeta.Content.rowLimit
  /** The maximum number or lines to read */
  @Injection( name = "ROW_LIMIT" )
  private long rowLimit;

  protected void setInputFiles( InputFiles inputFiles ) {
    this.inputFiles = inputFiles;
  }

  protected InputFiles getInputFiles() {
    return this.inputFiles;
  }

  public static class InputFiles extends BaseFileInputFiles {
    public void allocate( int nrFiles ) {
      fileName = new String[nrFiles];
      fileMask = new String[nrFiles];
      excludeFileMask = new String[nrFiles];
      fileRequired = new String[nrFiles];
      includeSubFolders = new String[nrFiles];
      Arrays.fill( fileName, "" );
      Arrays.fill( fileMask, "" );
      Arrays.fill( excludeFileMask, "" );
      Arrays.fill( fileRequired, NO );
      Arrays.fill( includeSubFolders, NO );
    }
    @Override
    public InputFiles clone() {
      InputFiles clone = (InputFiles) super.clone();
      clone.allocate( this.fileName.length );
      return clone;
    }
  }

  public static class AdditionalFileOutputFields extends BaseFileInputAdditionalField {

    public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info,
        VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
      // TextFileInput is the same, this can be refactored further
      if ( shortFilenameField != null ) {
        ValueMetaInterface v =
          new ValueMetaString( space.environmentSubstitute( shortFilenameField ) );
        v.setLength( 100, -1 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
      if ( extensionField != null ) {
        ValueMetaInterface v =
          new ValueMetaString( space.environmentSubstitute( extensionField ) );
        v.setLength( 100, -1 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
      if ( pathField != null ) {
        ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( pathField ) );
        v.setLength( 100, -1 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
      if ( sizeField != null ) {
        ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( sizeField ) );
        v.setOrigin( name );
        v.setLength( 9 );
        r.addValueMeta( v );
      }
      if ( hiddenField != null ) {
        ValueMetaInterface v =
          new ValueMetaBoolean( space.environmentSubstitute( hiddenField ) );
        v.setOrigin( name );
        r.addValueMeta( v );
      }

      if ( lastModificationField != null ) {
        ValueMetaInterface v =
          new ValueMetaDate( space.environmentSubstitute( lastModificationField ) );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
      if ( uriField != null ) {
        ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( uriField ) );
        v.setLength( 100, -1 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }

      if ( rootUriField != null ) {
        ValueMetaInterface v =
          new ValueMetaString( space.environmentSubstitute( rootUriField ) );
        v.setLength( 100, -1 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
    }
  }

  /** Is In fields */
  @Injection( name = "SOURCE_FIELD_NAME" )
  private String valueField;

  /** Is In fields */
  @Injection( name = "SOURCE_IN_FIELD" )
  private boolean inFields;

  /** Is a File */
  @Injection( name = "SOURCE_FIELD_IS_FILENAME" )
  private boolean isAFile;

  /** Flag: add result filename **/
  @Injection( name = "ADD_RESULT_FILE" )
  private boolean addResultFile;

  /** Flag : do we ignore empty files */
  @Injection( name = "IGNORE_EMPTY_FILE" )
  private boolean isIgnoreEmptyFile;

  /** Flag : do not fail if no file */
  @Injection( name = "DO_NOT_FAIL_IF_NO_FILE" )
  private boolean doNotFailIfNoFile;

  @Injection( name = "IGNORE_MISSING_PATH" )
  private boolean ignoreMissingPath;

  /** Flag : read url as source */
  @Injection( name = "READ_SOURCE_AS_URL" )
  private boolean readurl;

  @Injection( name = "REMOVE_SOURCE_FIELDS" )
  private boolean removeSourceField;

  private boolean defaultPathLeafToNull;

  private boolean includeNulls;

  public JsonInputMeta() {
    additionalOutputFields = new JsonInputMeta.AdditionalFileOutputFields();
    inputFiles = new JsonInputMeta.InputFiles();
    inputFields = new JsonInputField[0];
  }

  /**Returns the defaultPathLeafToNull.
   * @return defaultPathLeafToNull
   */
  public boolean isDefaultPathLeafToNull() {
    return defaultPathLeafToNull;
  }

  /**Set the defaultPathLeafToNull
   * @param defaultPathLeafToNull the defaultPathLeafToNull to set.
   */
  public void setDefaultPathLeafToNull( boolean defaultPathLeafToNull ) {
    this.defaultPathLeafToNull = defaultPathLeafToNull;
  }

  /** Returns the includeNulls boolean
   * @return includeNulls
   */
  public boolean isIncludeNulls() {
    return includeNulls;
  }

  /** Sets the includeNulls boolean
   * @param includeNulls the includeNulls to set
   */
  public void setIncludeNulls( boolean includeNulls ) {
    this.includeNulls = includeNulls;
  }

  /**
   * @return Returns the shortFileFieldName.
   */
  public String getShortFileNameField() {
    return additionalOutputFields.shortFilenameField;
  }

  /**
   * @param field
   *          The shortFileFieldName to set.
   */
  public void setShortFileNameField( String field ) {
    additionalOutputFields.shortFilenameField = field;
  }

  /**
   * @return Returns the pathFieldName.
   */
  public String getPathField() {
    return additionalOutputFields.pathField;
  }

  /**
   * @param field
   *          The pathFieldName to set.
   */
  public void setPathField( String field ) {
    additionalOutputFields.pathField = field;
  }

  /**
   * @return Returns the hiddenFieldName.
   */
  public String isHiddenField() { //name..
    return additionalOutputFields.hiddenField;
  }

  /**
   * @param field
   *          The hiddenFieldName to set.
   */
  public void setIsHiddenField( String field ) { //name..
    additionalOutputFields.hiddenField = field;
  }

  /**
   * @return Returns the lastModificationTimeFieldName.
   */
  public String getLastModificationDateField() {
    return additionalOutputFields.lastModificationField;
  }

  /**
   * @param field
   *          The lastModificationTimeFieldName to set.
   */
  public void setLastModificationDateField( String field ) {
    additionalOutputFields.lastModificationField = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getUriField() {
    return additionalOutputFields.uriField;
  }

  /**
   * @param field
   *          The uriNameFieldName to set.
   */
  public void setUriField( String field ) {
    additionalOutputFields.uriField = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getRootUriField() {
    return additionalOutputFields.rootUriField;
  }

  /**
   * @param field
   *          The rootUriNameFieldName to set.
   */
  public void setRootUriField( String field ) {
    additionalOutputFields.rootUriField = field;
  }

  /**
   * @return Returns the extensionFieldName.
   */
  public String getExtensionField() {
    return additionalOutputFields.extensionField;
  }

  /**
   * @param field
   *          The extensionFieldName to set.
   */
  public void setExtensionField( String field ) {
    additionalOutputFields.extensionField = field;
  }

  /**
   * @return Returns the sizeFieldName.
   */
  public String getSizeField() {
    return additionalOutputFields.sizeField;
  }

  /**
   * @param field
   *          The sizeFieldName to set.
   */
  public void setSizeField( String field ) {
    additionalOutputFields.sizeField = field;
  }

  /**
   * @return the add result filesname flag
   */
  public boolean addResultFile() {
    return addResultFile;
  }

  public boolean isReadUrl() {
    return readurl;
  }

  public void setReadUrl( boolean readurl ) {
    this.readurl = readurl;
  }

  public boolean isRemoveSourceField() {
    return removeSourceField;
  }

  public void setRemoveSourceField( boolean removeSourceField ) {
    this.removeSourceField = removeSourceField;
  }

  public void setAddResultFile( boolean addResultFile ) {
    this.addResultFile = addResultFile;
  }

  @Override
  public JsonInputField[] getInputFields() {
    return super.getInputFields();
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( JsonInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @deprecated use {@link#getExcludeFileMask()}
   */
  @Deprecated
  public String[] getExludeFileMask() {
    return getExcludeFileMask();
  }

  public String[] getExcludeFileMask() {
    return inputFiles.excludeFileMask;
  }

  public void setExcludeFileMask( String[] excludeFileMask ) {
    inputFiles.excludeFileMask = excludeFileMask;
  }

  /**
   * Get field value.
   */
  public String getFieldValue() {
    return valueField;
  }

  public void setFieldValue( String value ) {
    this.valueField = value;
    inputFiles.acceptingField = value; //TODO
  }

  public boolean isInFields() {
    return inFields;
  }

  public void setInFields( boolean inFields ) {
    this.inFields = inFields;
    inputFiles.acceptingFilenames = inFields;
  }

  public String[] getFileMask() {
    return inputFiles.fileMask;
  }

  public void setFileMask( String[] fileMask ) {
    inputFiles.fileMask = fileMask;
  }

  public String[] getFileRequired() {
    return inputFiles.fileRequired;
  }

  public void setFileRequired( String[] fileRequiredin ) {
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      this.inputFiles.fileRequired[i] = getRequiredFilesCode( fileRequiredin[i] );
    }
  }

  public void setIncludeSubFolders( String[] includeSubFoldersin ) {
    for ( int i = 0; i < includeSubFoldersin.length; i++ ) {
      this.inputFiles.includeSubFolders[i] = getRequiredFilesCode( includeSubFoldersin[i] );
    }
  }

  public String[] getFileName() {
    return inputFiles.fileName;
  }

  public void setFileName( String[] fileName ) {
    this.inputFiles.fileName = fileName;
  }

  public String getFilenameField() {
    return filenameField;
  }

  public void setFilenameField( String filenameField ) {
    this.filenameField = filenameField;
  }

  public boolean includeFilename() {
    return includeFilename;
  }

  public void setIncludeFilename( boolean includeFilename ) {
    this.includeFilename = includeFilename;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
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

  /**
   * @return the IsIgnoreEmptyFile flag
   */
  public boolean isIgnoreEmptyFile() {
    return isIgnoreEmptyFile;
  }

  /**
   * @param isIgnoreEmptyFile
   *          the IsIgnoreEmptyFile to set
   */
  public void setIgnoreEmptyFile( boolean isIgnoreEmptyFile ) {
    this.isIgnoreEmptyFile = isIgnoreEmptyFile;
  }

  @Deprecated
  public boolean isdoNotFailIfNoFile() {
    return isDoNotFailIfNoFile();
  }
  @Deprecated
  public void setdoNotFailIfNoFile( boolean doNotFailIfNoFile ) {
    setDoNotFailIfNoFile( doNotFailIfNoFile );
  }

  public boolean isDoNotFailIfNoFile() {
    return doNotFailIfNoFile;
  }

  public void setDoNotFailIfNoFile( boolean doNotFailIfNoFile ) {
    this.doNotFailIfNoFile = doNotFailIfNoFile;
  }

  public boolean isIgnoreMissingPath() {
    return ignoreMissingPath;
  }

  public void setIgnoreMissingPath( boolean ignoreMissingPath ) {
    this.ignoreMissingPath = ignoreMissingPath;
  }

  public String getRowNumberField() {
    return rowNumberField;
  }

  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  public boolean getIsAFile() {
    return isAFile;
  }

  public void setIsAFile( boolean isAFile ) {
    this.isAFile = isAFile;
  }

  public String[] getIncludeSubFolders() {
    return inputFiles.includeSubFolders;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public JsonInputMeta clone() {
    JsonInputMeta clone = (JsonInputMeta) super.clone();
    clone.setFileName( getFileName() );
    clone.setFileMask( getFileMask() );
    clone.setExcludeFileMask( getExcludeFileMask() );
    for ( int i = 0; i < inputFields.length; i++ ) {
      clone.inputFields[i] = inputFields[i].clone();
    }
    return clone;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 400 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "include", includeFilename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_field", filenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "addresultfile", addResultFile ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "readurl", readurl ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "removeSourceField", removeSourceField ) );

    retval.append( "    " + XMLHandler.addTagValue( "IsIgnoreEmptyFile", isIgnoreEmptyFile ) );
    retval.append( "    " + XMLHandler.addTagValue( "doNotFailIfNoFile", doNotFailIfNoFile ) );
    retval.append( "    " + XMLHandler.addTagValue( "ignoreMissingPath", ignoreMissingPath ) );
    retval.append( "    " + XMLHandler.addTagValue( "defaultPathLeafToNull", defaultPathLeafToNull ) );
    retval.append( "    " + XMLHandler.addTagValue( INCLUDE_NULLS, includeNulls ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", rowNumberField ) );

    retval.append( "    <file>" ).append( Const.CR );
    for ( int i = 0; i < getFileName().length; i++ ) {
      retval.append( "      " ).append( XMLHandler.addTagValue( "name", getFileName()[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "filemask", getFileMask()[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "exclude_filemask", getExcludeFileMask()[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "file_required", getFileRequired()[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", getIncludeSubFolders()[i] ) );

    }
    retval.append( "    </file>" ).append( Const.CR );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < getInputFields().length; i++ ) {
      JsonInputField field = getInputFields()[i];
      retval.append( field.getXML() );
    }
    retval.append( "    </fields>" ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", rowLimit ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "IsInFields", inFields ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "IsAFile", isAFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "valueField", valueField ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "shortFileFieldName", getShortFileNameField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "pathFieldName", getPathField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "hiddenFieldName", isHiddenField() ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "lastModificationTimeFieldName", getLastModificationDateField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "uriNameFieldName", getUriField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rootUriNameFieldName", getUriField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "extensionFieldName", getExtensionField() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sizeFieldName", getSizeField() ) );
    return retval.toString();
  }

  public String getRequiredFilesDesc( String tt ) {
    if ( Utils.isEmpty( tt ) ) {
      return RequiredFilesDesc[0];
    }
    if ( tt.equalsIgnoreCase( RequiredFilesCode[1] ) ) {
      return RequiredFilesDesc[1];
    } else {
      return RequiredFilesDesc[0];
    }
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      includeFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include" ) );
      filenameField = XMLHandler.getTagValue( stepnode, "include_field" );
      addResultFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "addresultfile" ) );
      readurl = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "readurl" ) );
      removeSourceField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "removeSourceField" ) );
      isIgnoreEmptyFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "IsIgnoreEmptyFile" ) );
      ignoreMissingPath = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "ignoreMissingPath" ) );
      defaultPathLeafToNull = getDefaultPathLeafToNull( stepnode );
      includeNulls = getincludeNulls( stepnode );
      doNotFailIfNoFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "doNotFailIfNoFile" ) );
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );

      Node filenode = XMLHandler.getSubNode( stepnode, "file" );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFiles = XMLHandler.countNodes( filenode, "name" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      initArrayFields( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        Node filenamenode = XMLHandler.getSubNodeByNr( filenode, "name", i );
        Node filemasknode = XMLHandler.getSubNodeByNr( filenode, "filemask", i );
        Node excludefilemasknode = XMLHandler.getSubNodeByNr( filenode, "exclude_filemask", i );
        Node fileRequirednode = XMLHandler.getSubNodeByNr( filenode, "file_required", i );
        Node includeSubFoldersnode = XMLHandler.getSubNodeByNr( filenode, "include_subfolders", i );
        getFileName()[i] = XMLHandler.getNodeValue( filenamenode );
        getFileMask()[i] = XMLHandler.getNodeValue( filemasknode );
        getExcludeFileMask()[i] = XMLHandler.getNodeValue( excludefilemasknode );
        getFileRequired()[i] = XMLHandler.getNodeValue( fileRequirednode );
        getIncludeSubFolders()[i] = XMLHandler.getNodeValue( includeSubFoldersnode );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        JsonInputField field = new JsonInputField( fnode );
        getInputFields()[i] = field;
      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, "limit" ), 0L );

      setInFields( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "IsInFields" ) ) );
      isAFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "IsAFile" ) );
      setFieldValue( XMLHandler.getTagValue( stepnode, "valueField" ) );
      setShortFileNameField( XMLHandler.getTagValue( stepnode, "shortFileFieldName" ) );
      setPathField( XMLHandler.getTagValue( stepnode, "pathFieldName" ) );
      setIsHiddenField( XMLHandler.getTagValue( stepnode, "hiddenFieldName" ) );
      setLastModificationDateField( XMLHandler.getTagValue( stepnode, "lastModificationTimeFieldName" ) );
      setUriField( XMLHandler.getTagValue( stepnode, "uriNameFieldName" ) );
      setRootUriField( XMLHandler.getTagValue( stepnode, "rootUriNameFieldName" ) );
      setExtensionField( XMLHandler.getTagValue( stepnode, "extensionFieldName" ) );
      setSizeField( XMLHandler.getTagValue( stepnode, "sizeFieldName" ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JsonInputMeta.Exception.ErrorLoadingXML", e
          .toString() ) );
    }
  }

  // For backward compatibility: if "defaultPathLeafToNull" tag is absent in the step node at all, then we set
  // defaultPathLeafToNull as default true.
  private static boolean getDefaultPathLeafToNull( Node stepnode ) {
    boolean result = true;
    List<Node> nodes = XMLHandler.getNodes( stepnode, "defaultPathLeafToNull" );
    if ( nodes != null && nodes.size() > 0 ) {
      result = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "defaultPathLeafToNull" ) );
    }
    return result;
  }

  // For backward compatibility: if "includeNulls" tag is absent in the step node at all, then we set
  // includeNulls default if KETTLE_JSON_INPUT_INCLUDE_NULLS is set to "Y" in kettle.properties
  // as seen in PDI-19138
  private static boolean getincludeNulls( Node stepnode ) {
    boolean result;
    List<Node> nodes = XMLHandler.getNodes( stepnode, INCLUDE_NULLS );
    if ( nodes != null && !nodes.isEmpty() ) {
      result = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, INCLUDE_NULLS ) );
    } else {
      result = getIncludeNullsProperty();
    }
    return result;
  }

  @Deprecated //?needs to be public?
  public void allocate( int nrFiles, int nrFields ) {
    initArrayFields( nrFiles, nrFields );
  }

  private void initArrayFields( int nrfiles, int nrfields ) {
    setInputFields( new JsonInputField[nrfields] );
    inputFiles.allocate( nrfiles );
    inputFields = new JsonInputField[nrfields];
  }

  @Override
  public void setDefault() {
    additionalOutputFields = new AdditionalFileOutputFields();

    isIgnoreEmptyFile = false;
    ignoreMissingPath = true;
    defaultPathLeafToNull = true;
    includeNulls = getIncludeNullsProperty();
    doNotFailIfNoFile = true;
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    isAFile = false;
    addResultFile = false;

    readurl = false;

    removeSourceField = false;

    int nrFiles = 0;
    int nrFields = 0;

    initArrayFields( nrFiles, nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      getInputFields()[i] = new JsonInputField( "field" + ( i + 1 ) );
    }

    rowLimit = 0;

    inFields = false;
    valueField = "";

  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    if ( inFields && removeSourceField && !Utils.isEmpty( valueField ) ) {
      int index = rowMeta.indexOfValue( valueField );
      if ( index != -1 ) {
        rowMeta.removeValueMeta( index );
      }
    }

    for ( JsonInputField field : getInputFields() ) {
      try {
        rowMeta.addValueMeta( field.toValueMeta( name, space ) );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }

    if ( includeFilename ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( filenameField ) );
      v.setLength( 250 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      rowMeta.addValueMeta( v );
    }

    if ( includeRowNumber ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      rowMeta.addValueMeta( v );
    }
    // Add additional fields
    additionalOutputFields.normalize();
    additionalOutputFields.getFields( rowMeta, name, info, space, repository, metaStore );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {

    try {
      includeFilename = rep.getStepAttributeBoolean( id_step, "include" );
      filenameField = rep.getStepAttributeString( id_step, "include_field" );

      addResultFile = rep.getStepAttributeBoolean( id_step, "addresultfile" );

      readurl = rep.getStepAttributeBoolean( id_step, "readurl" );

      removeSourceField = rep.getStepAttributeBoolean( id_step, "removeSourceField" );

      isIgnoreEmptyFile = rep.getStepAttributeBoolean( id_step, "IsIgnoreEmptyFile" );
      ignoreMissingPath = rep.getStepAttributeBoolean( id_step, "ignoreMissingPath" );
      defaultPathLeafToNull = rep.getStepAttributeBoolean( id_step, 0, "defaultPathLeafToNull", true );
      includeNulls = rep.getStepAttributeBoolean( id_step, 0, INCLUDE_NULLS, getIncludeNullsProperty() );

      doNotFailIfNoFile = rep.getStepAttributeBoolean( id_step, "doNotFailIfNoFile" );

      includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );
      rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );
      rowLimit = rep.getStepAttributeInteger( id_step, "limit" );

      int nrFiles = rep.countNrStepAttributes( id_step, "file_name" );
      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );

      initArrayFields( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        getFileName()[i] = rep.getStepAttributeString( id_step, i, "file_name" );
        getFileMask()[i] = rep.getStepAttributeString( id_step, i, "file_mask" );
        getExcludeFileMask()[i] = rep.getStepAttributeString( id_step, i, "exclude_file_mask" );
        getFileRequired()[i] = rep.getStepAttributeString( id_step, i, "file_required" );
        getIncludeSubFolders()[i] = rep.getStepAttributeString( id_step, i, "include_subfolders" );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        JsonInputField field = new JsonInputField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setPath( rep.getStepAttributeString( id_step, i, "field_path" ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        field.setTrimType( ValueMetaBase.getTrimTypeByCode( rep.getStepAttributeString(
          id_step, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );

        getInputFields()[i] = field;
      }
      setInFields( rep.getStepAttributeBoolean( id_step, "IsInFields" ) );
      isAFile = rep.getStepAttributeBoolean( id_step, "IsAFile" );

      valueField = rep.getStepAttributeString( id_step, "valueField" );

      setShortFileNameField( rep.getStepAttributeString( id_step, "shortFileFieldName" ) );
      setPathField( rep.getStepAttributeString( id_step, "pathFieldName" ) );
      setIsHiddenField( rep.getStepAttributeString( id_step, "hiddenFieldName" ) );
      setLastModificationDateField( rep.getStepAttributeString( id_step, "lastModificationTimeFieldName" ) );
      setUriField( rep.getStepAttributeString( id_step, "uriNameFieldName" ) );
      setRootUriField( rep.getStepAttributeString( id_step, "rootUriNameFieldName" ) );
      setExtensionField( rep.getStepAttributeString( id_step, "extensionFieldName" ) );
      setSizeField( rep.getStepAttributeString( id_step, "sizeFieldName" ) );
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "JsonInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "include", includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, "include_field", filenameField );
      rep.saveStepAttribute( id_transformation, id_step, "addresultfile", addResultFile );
      rep.saveStepAttribute( id_transformation, id_step, "readurl", readurl );

      rep.saveStepAttribute( id_transformation, id_step, "removeSourceField", removeSourceField );

      rep.saveStepAttribute( id_transformation, id_step, "IsIgnoreEmptyFile", isIgnoreEmptyFile );
      rep.saveStepAttribute( id_transformation, id_step, "ignoreMissingPath", ignoreMissingPath );
      rep.saveStepAttribute( id_transformation, id_step, "defaultPathLeafToNull", defaultPathLeafToNull );
      rep.saveStepAttribute( id_transformation, id_step, INCLUDE_NULLS, includeNulls );

      rep.saveStepAttribute( id_transformation, id_step, "doNotFailIfNoFile", doNotFailIfNoFile );

      rep.saveStepAttribute( id_transformation, id_step, "rownum", includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );

      for ( int i = 0; i < getFileName().length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "file_name", getFileName()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_mask", getFileMask()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "exclude_file_mask", getExcludeFileMask()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_required", getFileRequired()[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "include_subfolders", getIncludeSubFolders()[i] );
      }

      for ( int i = 0; i < getInputFields().length; i++ ) {
        JsonInputField field = getInputFields()[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_path", field.getPath() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_group", field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", field.getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_trim_type", field.getTrimTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_repeat", field.isRepeated() );
      }
      rep.saveStepAttribute( id_transformation, id_step, "IsInFields", inFields );
      rep.saveStepAttribute( id_transformation, id_step, "IsAFile", isAFile );

      rep.saveStepAttribute( id_transformation, id_step, "valueField", valueField );
      rep.saveStepAttribute( id_transformation, id_step, "shortFileFieldName", getShortFileNameField() );
      rep.saveStepAttribute( id_transformation, id_step, "pathFieldName", getPathField() );
      rep.saveStepAttribute( id_transformation, id_step, "hiddenFieldName", isHiddenField() );
      rep.saveStepAttribute(
        id_transformation, id_step, "lastModificationTimeFieldName", getLastModificationDateField() );
      rep.saveStepAttribute( id_transformation, id_step, "uriNameFieldName", getUriField() );
      rep.saveStepAttribute( id_transformation, id_step, "rootUriNameFieldName", getRootUriField() );
      rep.saveStepAttribute( id_transformation, id_step, "extensionFieldName", getExtensionField() );
      rep.saveStepAttribute( id_transformation, id_step, "sizeFieldName", getSizeField() );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JsonInputMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
    }
  }

  public FileInputList getFiles( VariableSpace space ) {
    return FileInputList.createFileList(
      space, getFileName(), getFileMask(), getExcludeFileMask(), getFileRequired(), inputFiles.includeSubFolderBoolean() );
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    if ( !isInFields() ) {
      // See if we get input...
      if ( input.length <= 0 ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "JsonInputMeta.CheckResult.NoInputExpected" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "JsonInputMeta.CheckResult.NoInput" ), stepMeta );
        remarks.add( cr );
      }
    }

    if ( getInputFields().length <= 0 ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "JsonInputMeta.CheckResult.NoInputField" ), stepMeta );
      remarks.add( cr );
    }

    if ( isInFields() ) {
      if ( Utils.isEmpty( getFieldValue() ) ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "JsonInputMeta.CheckResult.NoField" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "JsonInputMeta.CheckResult.FieldOk" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      FileInputList fileInputList = getFiles( transMeta );
      // String files[] = getFiles();
      if ( fileInputList == null || fileInputList.getFiles().size() == 0 ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "JsonInputMeta.CheckResult.NoFiles" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "JsonInputMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ), stepMeta );
        remarks.add( cr );
      }
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans ) {
    return new JsonInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new JsonInputData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
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
      List<String> newFilenames = new ArrayList<String>();

      if ( !isInFields() ) {
        FileInputList fileList = getFiles( space );
        if ( fileList.getFiles().size() > 0 ) {
          for ( FileObject fileObject : fileList.getFiles() ) {
            // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.xml
            // To : /home/matt/test/files/foo/bar.xml
            //
            // If the file doesn't exist, forget about this effort too!
            //
            if ( fileObject.exists() ) {
              // Convert to an absolute path and add it to the list.
              //
              newFilenames.add( fileObject.getName().getPath() );
            }
          }

          // Still here: set a new list of absolute filenames!
          //
          setFileName( newFilenames.toArray( new String[newFilenames.size()] ) );
          setFileMask( new String[newFilenames.size()] ); // all null since converted to absolute path.
          setFileRequired( new String[newFilenames.size()] ); // all null, turn to "Y" :
          Arrays.fill( getFileRequired(), YES );
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  public String getEncoding() {
    return "UTF-8";
  }

  // Needed for PDI-19138 purposes
  public static boolean getIncludeNullsProperty() {
    return "Y".equalsIgnoreCase( System.getProperty( Const.KETTLE_JSON_INPUT_INCLUDE_NULLS, "N" ) );
  }

}
