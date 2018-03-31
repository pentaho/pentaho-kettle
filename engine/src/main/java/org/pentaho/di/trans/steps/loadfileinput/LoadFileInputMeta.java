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

package org.pentaho.di.trans.steps.loadfileinput;

import java.util.Arrays;
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

public class LoadFileInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static final String INCLUDE = "include";
  private static final String INCLUDE_FIELD = "include_field";
  private static final String ROWNUM = "rownum";
  private static final String ADDRESULTFILE = "addresultfile";
  private static final String IS_IGNORE_EMPTY_FILE = "IsIgnoreEmptyFile";
  private static final String IS_IGNORE_MISSING_PATH = "IsIgnoreMissingPath";
  private static final String ROWNUM_FIELD = "rownum_field";
  private static final String ENCODING = "encoding";
  private static final String NAME = "name";
  private static final String FILEMASK = "filemask";
  private static final String EXCLUDE_FILEMASK = "exclude_filemask";
  private static final String FILE_REQUIRED = "file_required";
  private static final String INCLUDE_SUBFOLDERS = "include_subfolders";
  private static final String LIMIT = "limit";
  private static final String IS_IN_FIELDS = "IsInFields";
  private static final String DYNAMIC_FILENAME_FIELD = "DynamicFilenameField";
  private static final String SHORT_FILE_FIELD_NAME = "shortFileFieldName";
  private static final String PATH_FIELD_NAME = "pathFieldName";
  private static final String HIDDEN_FIELD_NAME = "hiddenFieldName";
  private static final String LAST_MODIFICATION_TIME_FIELD_NAME = "lastModificationTimeFieldName";
  private static final String URI_NAME_FIELD_NAME = "uriNameFieldName";
  private static final String ROOT_URI_NAME_FIELD_NAME = "rootUriNameFieldName";
  private static final String EXTENSION_FIELD_NAME = "extensionFieldName";
  private static final String FILE = "file";
  private static final String FIELDS = "fields";

  // Repository constant not sync with xml just to backward compatibility
  private static final String FILE_NAME_REP = "file_name";
  private static final String FILE_MASK_REP = "file_mask";
  private static final String EXCLUDEFILE_MASK_REP = "excludefile_mask";
  private static final String FIELD_NAME_REP = "field_name";
  private static final String ELEMENT_TYPE_REP = "element_type";
  private static final String FIELD_TYPE_REP = "field_type";
  private static final String FIELD_FORMAT_REP = "field_format";
  private static final String FIELD_CURRENCY_REP = "field_currency";
  private static final String FIELD_DECIMAL_REP = "field_decimal";
  private static final String FIELD_GROUP_REP = "field_group";
  private static final String FIELD_LENGTH_REP = "field_length";
  private static final String FIELD_PRECISION_REP = "field_precision";
  private static final String FIELD_TRIM_TYPE_REP = "field_trim_type";
  private static final String FIELD_REPEAT_REP = "field_repeat";
  private static Class<?> PKG = LoadFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesDesc = new String[] { BaseMessages.getString( PKG, "System.Combo.No" ),
    BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  private static final String NO = "N";
  private static final String YES = "Y";

  /** Array of filenames */
  private String[] fileName;

  /** Wildcard or filemask (regular expression) */
  private String[] fileMask;

  /** Wildcard or filemask to exclude (regular expression) */
  private String[] excludeFileMask;

  /** Flag indicating that we should include the filename in the output */
  private boolean includeFilename;

  /** The name of the field in the output containing the filename */
  private String filenameField;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The maximum number or lines to read */
  private long rowLimit;

  /** The fields to import... */
  private LoadFileInputField[] inputFields;

  /** The encoding to use for reading: null or empty string means system default encoding */
  private String encoding;

  /** Dynamic FilenameField */
  private String DynamicFilenameField;

  /** Is In fields */
  private boolean fileinfield;

  /** Flag: add result filename **/
  private boolean addresultfile;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] fileRequired;

  /** Flag : do we ignore empty file? */
  private boolean IsIgnoreEmptyFile;

  /** Flag : do we ignore missing path? */
  private boolean IsIgnoreMissingPath;

  /** Array of boolean values as string, indicating if we need to fetch sub folders. */
  private String[] includeSubFolders;

  /** Additional fields **/
  private String shortFileFieldName;
  private String pathFieldName;
  private String hiddenFieldName;
  private String lastModificationTimeFieldName;
  private String uriNameFieldName;
  private String rootUriNameFieldName;
  private String extensionFieldName;

  public LoadFileInputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the shortFileFieldName.
   */
  public String getShortFileNameField() {
    return shortFileFieldName;
  }

  /**
   * @param field
   *          The shortFileFieldName to set.
   */
  public void setShortFileNameField( String field ) {
    shortFileFieldName = field;
  }

  /**
   * @return Returns the pathFieldName.
   */
  public String getPathField() {
    return pathFieldName;
  }

  /**
   * @param field
   *          The pathFieldName to set.
   */
  public void setPathField( String field ) {
    this.pathFieldName = field;
  }

  /**
   * @return Returns the hiddenFieldName.
   */
  public String isHiddenField() {
    return hiddenFieldName;
  }

  /**
   * @param field
   *          The hiddenFieldName to set.
   */
  public void setIsHiddenField( String field ) {
    hiddenFieldName = field;
  }

  /**
   * @return Returns the lastModificationTimeFieldName.
   */
  public String getLastModificationDateField() {
    return lastModificationTimeFieldName;
  }

  /**
   * @param field
   *          The lastModificationTimeFieldName to set.
   */
  public void setLastModificationDateField( String field ) {
    lastModificationTimeFieldName = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getUriField() {
    return uriNameFieldName;
  }

  /**
   * @param field
   *          The uriNameFieldName to set.
   */
  public void setUriField( String field ) {
    uriNameFieldName = field;
  }

  /**
   * @return Returns the uriNameFieldName.
   */
  public String getRootUriField() {
    return rootUriNameFieldName;
  }

  /**
   * @param field
   *          The rootUriNameFieldName to set.
   */
  public void setRootUriField( String field ) {
    rootUriNameFieldName = field;
  }

  /**
   * @return Returns the extensionFieldName.
   */
  public String getExtensionField() {
    return extensionFieldName;
  }

  /**
   * @param field
   *          The extensionFieldName to set.
   */
  public void setExtensionField( String field ) {
    extensionFieldName = field;
  }

  public String[] getFileRequired() {
    return fileRequired;
  }

  public void setFileRequired( String[] fileRequired ) {
    this.fileRequired = fileRequired;
  }

  /**
   * @deprecated typo in method name
   */
  @Deprecated
  public String[] getExludeFileMask() {
    return excludeFileMask;
  }

  /**
   * @return Returns the excludeFileMask.
   */
  public String[] getExcludeFileMask() {
    return excludeFileMask;
  }

  /**
   * @param excludeFileMask
   *          The excludeFileMask to set.
   */
  public void setExcludeFileMask( String[] excludeFileMask ) {
    this.excludeFileMask = excludeFileMask;
  }

  /**
   * @deprecated doesn't following naming standards
   */
  @Deprecated
  public boolean addResultFile() {
    return addresultfile;
  }

  /**
   * @return the add result filesname flag
   */
  public boolean getAddResultFile() {
    return addresultfile;
  }

  /**
   * @return the IsIgnoreEmptyFile flag
   */
  public boolean isIgnoreEmptyFile() {
    return IsIgnoreEmptyFile;
  }

  /**
   * @param the
   *          IsIgnoreEmptyFile to set
   */
  public void setIgnoreEmptyFile( boolean IsIgnoreEmptyFile ) {
    this.IsIgnoreEmptyFile = IsIgnoreEmptyFile;
  }

  /**
   * @return the IsIgnoreMissingPath flag
   */
  public boolean isIgnoreMissingPath() {
    return IsIgnoreMissingPath;
  }

  /**
   * @param the
   *          IsIgnoreMissingPath to set
   */
  public void setIgnoreMissingPath( boolean IsIgnoreMissingPath ) {
    this.IsIgnoreMissingPath = IsIgnoreMissingPath;
  }

  public void setAddResultFile( boolean addresultfile ) {
    this.addresultfile = addresultfile;
  }

  /**
   * @return Returns the input fields.
   */
  public LoadFileInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( LoadFileInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /************************************
   * get and set FilenameField
   *************************************/
  /**  */
  public String getDynamicFilenameField() {
    return DynamicFilenameField;
  }

  /**  */
  public void setDynamicFilenameField( String DynamicFilenameField ) {
    this.DynamicFilenameField = DynamicFilenameField;
  }

  /************************************
   * get / set fileInFields
   *************************************/
  /**  */
  public boolean getFileInFields() {
    return fileinfield;
  }

  /************************************
   * @deprecated doesn't follow standard naming
   *************************************/
  @Deprecated
  public boolean getIsInFields() {
    return fileinfield;
  }

  /**
   * @deprecated doesn't follow standard naming 
   */
  @Deprecated
  public void setIsInFields( boolean IsInFields ) {
    this.fileinfield = IsInFields;
  }

  public void setFileInFields( boolean IsInFields ) {
    this.fileinfield = IsInFields;
  }

  /**
   * @return Returns the fileMask.
   */
  public String[] getFileMask() {
    return fileMask;
  }

  /**
   * @param fileMask
   *          The fileMask to set.
   */
  public void setFileMask( String[] fileMask ) {
    this.fileMask = fileMask;
  }

  /**
   * @return Returns the fileName.
   */
  public String[] getFileName() {
    return fileName;
  }

  public String[] getIncludeSubFolders() {
    return includeSubFolders;
  }

  public void setIncludeSubFolders( String[] includeSubFoldersin ) {
    for ( int i = 0; i < includeSubFoldersin.length; i++ ) {
      this.includeSubFolders[i] = getRequiredFilesCode( includeSubFoldersin[i] );
    }
  }

  public String getRequiredFilesCode( String tt ) {
    if ( tt == null ) {
      return RequiredFilesCode[0];
    }
    if ( tt.equals( RequiredFilesDesc[1] ) ) {
      return RequiredFilesCode[1];
    } else {
      return RequiredFilesCode[0];
    }
  }

  public String getRequiredFilesDesc( String tt ) {
    if ( tt == null ) {
      return RequiredFilesDesc[0];
    }
    if ( tt.equals( RequiredFilesCode[1] ) ) {
      return RequiredFilesDesc[1];
    } else {
      return RequiredFilesDesc[0];
    }
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName( String[] fileName ) {
    this.fileName = fileName;
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
   * @deprecated doesn't follow standard naming
   */
  @Deprecated
  public boolean includeFilename() {
    return includeFilename;
  }

  /**
   * @return Returns the includeFilename.
   * 
   */
  public boolean getIncludeFilename() {
    return includeFilename;
  }

  /**
   * @param includeFilename
   *          The includeFilename to set.
   */
  public void setIncludeFilename( boolean includeFilename ) {
    this.includeFilename = includeFilename;
  }

  /**
   * @return Returns the includeRowNumber.
   * @deprecated doesn't follow standard naming
   */
  @Deprecated
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean getIncludeRowNumber() {
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
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
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

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    LoadFileInputMeta retval = (LoadFileInputMeta) super.clone();

    int nrFiles = fileName.length;
    int nrFields = inputFields.length;

    retval.allocate( nrFiles, nrFields );
    System.arraycopy( fileName, 0, retval.fileName, 0, nrFiles );
    System.arraycopy( fileMask, 0, retval.fileMask, 0, nrFiles );
    System.arraycopy( excludeFileMask, 0, retval.excludeFileMask, 0, nrFiles );
    System.arraycopy( fileRequired, 0, retval.fileRequired, 0, nrFiles );
    System.arraycopy( includeSubFolders, 0, retval.includeSubFolders, 0, nrFiles );

    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[i] != null ) {
        retval.inputFields[i] = (LoadFileInputField) inputFields[i].clone();
      }
    }
    return retval;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " + XMLHandler.addTagValue( INCLUDE, includeFilename ) );
    retval.append( "    " + XMLHandler.addTagValue( INCLUDE_FIELD, filenameField ) );
    retval.append( "    " + XMLHandler.addTagValue( ROWNUM, includeRowNumber ) );
    retval.append( "    " + XMLHandler.addTagValue( ADDRESULTFILE, addresultfile ) );
    retval.append( "    " + XMLHandler.addTagValue( IS_IGNORE_EMPTY_FILE, IsIgnoreEmptyFile ) );
    retval.append( "    " + XMLHandler.addTagValue( IS_IGNORE_MISSING_PATH, IsIgnoreMissingPath ) );

    retval.append( "    " + XMLHandler.addTagValue( ROWNUM_FIELD, rowNumberField ) );
    retval.append( "    " + XMLHandler.addTagValue( ENCODING, encoding ) );

    retval.append( "    <" + FILE + ">" + Const.CR );
    for ( int i = 0; i < fileName.length; i++ ) {
      retval.append( "      " + XMLHandler.addTagValue( NAME, fileName[i] ) );
      retval.append( "      " + XMLHandler.addTagValue( FILEMASK, fileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( EXCLUDE_FILEMASK, excludeFileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( FILE_REQUIRED, fileRequired[i] ) );
      retval.append( "      " + XMLHandler.addTagValue( INCLUDE_SUBFOLDERS, includeSubFolders[i] ) );
    }
    retval.append( "      </" + FILE + ">" + Const.CR );

    retval.append( "    <" + FIELDS + ">" + Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      LoadFileInputField field = inputFields[i];
      retval.append( field.getXML() );
    }
    retval.append( "      </" + FIELDS + ">" + Const.CR );
    retval.append( "    " + XMLHandler.addTagValue( LIMIT, rowLimit ) );
    retval.append( "    " + XMLHandler.addTagValue( IS_IN_FIELDS, fileinfield ) );
    retval.append( "    " + XMLHandler.addTagValue( DYNAMIC_FILENAME_FIELD, DynamicFilenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( SHORT_FILE_FIELD_NAME, shortFileFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( PATH_FIELD_NAME, pathFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( HIDDEN_FIELD_NAME, hiddenFieldName ) );
    retval.append( "    " ).append(
        XMLHandler.addTagValue( LAST_MODIFICATION_TIME_FIELD_NAME, lastModificationTimeFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( URI_NAME_FIELD_NAME, uriNameFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( ROOT_URI_NAME_FIELD_NAME, rootUriNameFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( EXTENSION_FIELD_NAME, extensionFieldName ) );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      includeFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, INCLUDE ) );
      filenameField = XMLHandler.getTagValue( stepnode, INCLUDE_FIELD );

      addresultfile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, ADDRESULTFILE ) );
      IsIgnoreEmptyFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, IS_IGNORE_EMPTY_FILE ) );
      IsIgnoreMissingPath = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, IS_IGNORE_MISSING_PATH ) );

      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, ROWNUM ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, ROWNUM_FIELD );
      encoding = XMLHandler.getTagValue( stepnode, ENCODING );

      Node filenode = XMLHandler.getSubNode( stepnode, FILE );
      Node fields = XMLHandler.getSubNode( stepnode, FIELDS );
      int nrFiles = XMLHandler.countNodes( filenode, NAME );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        Node filenamenode = XMLHandler.getSubNodeByNr( filenode, NAME, i );
        Node filemasknode = XMLHandler.getSubNodeByNr( filenode, FILEMASK, i );
        Node excludefilemasknode = XMLHandler.getSubNodeByNr( filenode, EXCLUDE_FILEMASK, i );
        Node fileRequirednode = XMLHandler.getSubNodeByNr( filenode, FILE_REQUIRED, i );
        Node includeSubFoldersnode = XMLHandler.getSubNodeByNr( filenode, INCLUDE_SUBFOLDERS, i );
        fileName[i] = XMLHandler.getNodeValue( filenamenode );
        fileMask[i] = XMLHandler.getNodeValue( filemasknode );
        excludeFileMask[i] = XMLHandler.getNodeValue( excludefilemasknode );
        fileRequired[i] = XMLHandler.getNodeValue( fileRequirednode );
        includeSubFolders[i] = XMLHandler.getNodeValue( includeSubFoldersnode );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        LoadFileInputField field = new LoadFileInputField( fnode );
        inputFields[i] = field;
      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, LIMIT ), 0L );

      fileinfield = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, IS_IN_FIELDS ) );

      DynamicFilenameField = XMLHandler.getTagValue( stepnode, DYNAMIC_FILENAME_FIELD );
      shortFileFieldName = XMLHandler.getTagValue( stepnode, SHORT_FILE_FIELD_NAME );
      pathFieldName = XMLHandler.getTagValue( stepnode, PATH_FIELD_NAME );
      hiddenFieldName = XMLHandler.getTagValue( stepnode, HIDDEN_FIELD_NAME );
      lastModificationTimeFieldName = XMLHandler.getTagValue( stepnode, LAST_MODIFICATION_TIME_FIELD_NAME );
      uriNameFieldName = XMLHandler.getTagValue( stepnode, URI_NAME_FIELD_NAME );
      rootUriNameFieldName = XMLHandler.getTagValue( stepnode, ROOT_URI_NAME_FIELD_NAME );
      extensionFieldName = XMLHandler.getTagValue( stepnode, EXTENSION_FIELD_NAME );

    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "LoadFileInputMeta.Exception.ErrorLoadingXML", e
          .toString() ) );
    }
  }

  public void allocate( int nrfiles, int nrfields ) {
    fileName = new String[nrfiles];
    fileMask = new String[nrfiles];
    excludeFileMask = new String[nrfiles];
    fileRequired = new String[nrfiles];
    includeSubFolders = new String[nrfiles];
    inputFields = new LoadFileInputField[nrfields];

  }

  public void setDefault() {
    shortFileFieldName = null;
    pathFieldName = null;
    hiddenFieldName = null;
    lastModificationTimeFieldName = null;
    uriNameFieldName = null;
    rootUriNameFieldName = null;
    extensionFieldName = null;

    encoding = "";
    IsIgnoreEmptyFile = false;
    IsIgnoreMissingPath = false;
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    addresultfile = true;

    int nrFiles = 0;
    int nrFields = 0;

    allocate( nrFiles, nrFields );

    for ( int i = 0; i < nrFiles; i++ ) {
      fileName[i] = "filename" + ( i + 1 );
      fileMask[i] = "";
      excludeFileMask[i] = "";
      fileRequired[i] = RequiredFilesCode[0];
      includeSubFolders[i] = RequiredFilesCode[0];
    }

    for ( int i = 0; i < nrFields; i++ ) {
      inputFields[i] = new LoadFileInputField( "field" + ( i + 1 ) );
    }

    rowLimit = 0;

    fileinfield = false;
    DynamicFilenameField = null;
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( !getIsInFields() ) {
      r.clear();
    }
    int i;
    for ( i = 0; i < inputFields.length; i++ ) {
      LoadFileInputField field = inputFields[i];
      int type = field.getType();

      switch ( field.getElementType() ) {
        case LoadFileInputField.ELEMENT_TYPE_FILECONTENT:
          if ( type == ValueMetaInterface.TYPE_NONE ) {
            type = ValueMetaInterface.TYPE_STRING;
          }
          break;
        case LoadFileInputField.ELEMENT_TYPE_FILESIZE:
          if ( type == ValueMetaInterface.TYPE_NONE ) {
            type = ValueMetaInterface.TYPE_INTEGER;
          }
          break;
        default:
          break;
      }

      try {
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( space.environmentSubstitute( field.getName() ), type );
        v.setLength( field.getLength() );
        v.setPrecision( field.getPrecision() );
        v.setConversionMask( field.getFormat() );
        v.setCurrencySymbol( field.getCurrencySymbol() );
        v.setDecimalSymbol( field.getDecimalSymbol() );
        v.setGroupingSymbol( field.getGroupSymbol() );
        v.setTrimType( field.getTrimType() );
        v.setOrigin( name );
        r.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
    if ( includeFilename ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( filenameField ) );
      v.setLength( 250 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( includeRowNumber ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    // Add additional fields

    if ( getShortFileNameField() != null && getShortFileNameField().length() > 0 ) {
      ValueMetaInterface v =
          new ValueMetaString( space.environmentSubstitute( getShortFileNameField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getExtensionField() != null && getExtensionField().length() > 0 ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getExtensionField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getPathField() != null && getPathField().length() > 0 ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getPathField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( isHiddenField() != null && isHiddenField().length() > 0 ) {
      ValueMetaInterface v = new ValueMetaBoolean( space.environmentSubstitute( isHiddenField() ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( getLastModificationDateField() != null && getLastModificationDateField().length() > 0 ) {
      ValueMetaInterface v =
          new ValueMetaDate( space.environmentSubstitute( getLastModificationDateField() ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getUriField() != null && getUriField().length() > 0 ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( getRootUriField() != null && getRootUriField().length() > 0 ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getRootUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      includeFilename = rep.getStepAttributeBoolean( id_step, INCLUDE );
      filenameField = rep.getStepAttributeString( id_step, INCLUDE_FIELD );

      addresultfile = rep.getStepAttributeBoolean( id_step, ADDRESULTFILE );
      IsIgnoreEmptyFile = rep.getStepAttributeBoolean( id_step, IS_IGNORE_EMPTY_FILE );
      IsIgnoreMissingPath = rep.getStepAttributeBoolean( id_step, IS_IGNORE_MISSING_PATH );

      includeRowNumber = rep.getStepAttributeBoolean( id_step, ROWNUM );
      rowNumberField = rep.getStepAttributeString( id_step, ROWNUM_FIELD );
      rowLimit = rep.getStepAttributeInteger( id_step, LIMIT );
      encoding = rep.getStepAttributeString( id_step, ENCODING );

      int nrFiles = rep.countNrStepAttributes( id_step, FILE_NAME_REP );
      int nrFields = rep.countNrStepAttributes( id_step, FIELD_NAME_REP );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        fileName[i] = rep.getStepAttributeString( id_step, i, FILE_NAME_REP );
        fileMask[i] = rep.getStepAttributeString( id_step, i, FILE_MASK_REP );
        excludeFileMask[i] = rep.getStepAttributeString( id_step, i, EXCLUDEFILE_MASK_REP );
        fileRequired[i] = rep.getStepAttributeString( id_step, i, FILE_REQUIRED );
        if ( !YES.equalsIgnoreCase( fileRequired[i] ) ) {
          fileRequired[i] = NO;
        }
        includeSubFolders[i] = rep.getStepAttributeString( id_step, i, INCLUDE_SUBFOLDERS );
        if ( !YES.equalsIgnoreCase( includeSubFolders[i] ) ) {
          includeSubFolders[i] = NO;
        }
      }

      for ( int i = 0; i < nrFields; i++ ) {
        LoadFileInputField field = new LoadFileInputField();

        field.setName( rep.getStepAttributeString( id_step, i, FIELD_NAME_REP ) );
        field.setElementType( LoadFileInputField.getElementTypeByCode( rep.getStepAttributeString( id_step, i,
            ELEMENT_TYPE_REP ) ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, FIELD_TYPE_REP ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, FIELD_FORMAT_REP ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, FIELD_CURRENCY_REP ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, FIELD_DECIMAL_REP ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, FIELD_GROUP_REP ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, FIELD_LENGTH_REP ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, FIELD_PRECISION_REP ) );
        field.setTrimType( LoadFileInputField.getTrimTypeByCode( rep.getStepAttributeString( id_step, i,
            FIELD_TRIM_TYPE_REP ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, FIELD_REPEAT_REP ) );

        inputFields[i] = field;
      }
      fileinfield = rep.getStepAttributeBoolean( id_step, IS_IN_FIELDS );

      DynamicFilenameField = rep.getStepAttributeString( id_step, DYNAMIC_FILENAME_FIELD );
      shortFileFieldName = rep.getStepAttributeString( id_step, SHORT_FILE_FIELD_NAME );
      pathFieldName = rep.getStepAttributeString( id_step, PATH_FIELD_NAME );
      hiddenFieldName = rep.getStepAttributeString( id_step, HIDDEN_FIELD_NAME );
      lastModificationTimeFieldName = rep.getStepAttributeString( id_step, LAST_MODIFICATION_TIME_FIELD_NAME );
      rootUriNameFieldName = rep.getStepAttributeString( id_step, ROOT_URI_NAME_FIELD_NAME );
      uriNameFieldName = rep.getStepAttributeString(  id_step, URI_NAME_FIELD_NAME );
      extensionFieldName = rep.getStepAttributeString( id_step, EXTENSION_FIELD_NAME );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
              "LoadFileInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, INCLUDE, includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, INCLUDE_FIELD, filenameField );
      rep.saveStepAttribute( id_transformation, id_step, ADDRESULTFILE, addresultfile );
      rep.saveStepAttribute( id_transformation, id_step, IS_IGNORE_EMPTY_FILE, IsIgnoreEmptyFile );
      rep.saveStepAttribute( id_transformation, id_step, IS_IGNORE_MISSING_PATH, IsIgnoreMissingPath );

      rep.saveStepAttribute( id_transformation, id_step, ROWNUM, includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, ROWNUM_FIELD, rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, LIMIT, rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, ENCODING, encoding );

      for ( int i = 0; i < fileName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, FILE_NAME_REP, fileName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, FILE_MASK_REP, fileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, EXCLUDEFILE_MASK_REP, excludeFileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, FILE_REQUIRED, fileRequired[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, INCLUDE_SUBFOLDERS, includeSubFolders[i] );
      }

      for ( int i = 0; i < inputFields.length; i++ ) {
        LoadFileInputField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_NAME_REP, field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, ELEMENT_TYPE_REP, field.getElementTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_TYPE_REP, field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_FORMAT_REP, field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_CURRENCY_REP, field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_DECIMAL_REP, field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_GROUP_REP, field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_LENGTH_REP, field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_PRECISION_REP, field.getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_TRIM_TYPE_REP, field.getTrimTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, FIELD_REPEAT_REP, field.isRepeated() );
      }
      rep.saveStepAttribute( id_transformation, id_step, IS_IN_FIELDS, fileinfield );

      rep.saveStepAttribute( id_transformation, id_step, DYNAMIC_FILENAME_FIELD, DynamicFilenameField );
      rep.saveStepAttribute( id_transformation, id_step, SHORT_FILE_FIELD_NAME, shortFileFieldName );
      rep.saveStepAttribute( id_transformation, id_step, PATH_FIELD_NAME, pathFieldName );
      rep.saveStepAttribute( id_transformation, id_step, HIDDEN_FIELD_NAME, hiddenFieldName );
      rep.saveStepAttribute( id_transformation, id_step, LAST_MODIFICATION_TIME_FIELD_NAME,
          lastModificationTimeFieldName );
      rep.saveStepAttribute( id_transformation, id_step, URI_NAME_FIELD_NAME, uriNameFieldName );
      rep.saveStepAttribute( id_transformation, id_step, ROOT_URI_NAME_FIELD_NAME, rootUriNameFieldName );
      rep.saveStepAttribute( id_transformation, id_step, EXTENSION_FIELD_NAME, extensionFieldName );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "LoadFileInputMeta.Exception.ErrorSavingToRepository", ""
          + id_step ), e );
    }
  }

  public FileInputList getFiles( VariableSpace space ) {
    return FileInputList.createFileList( space, fileName, fileMask, excludeFileMask, fileRequired,
        includeSubFolderBoolean() );
  }

  private boolean[] includeSubFolderBoolean() {
    int len = fileName.length;
    boolean[] includeSubFolderBoolean = new boolean[len];
    for ( int i = 0; i < len; i++ ) {
      includeSubFolderBoolean[i] = YES.equalsIgnoreCase( includeSubFolders[i] );
    }
    return includeSubFolderBoolean;
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    if ( getIsInFields() ) {
      // See if we get input...
      if ( input.length == 0 ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "LoadFileInputMeta.CheckResult.NoInputExpected" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "LoadFileInputMeta.CheckResult.NoInput" ), stepMeta );
        remarks.add( cr );
      }

      if ( Utils.isEmpty( getDynamicFilenameField() ) ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "LoadFileInputMeta.CheckResult.NoField" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "LoadFileInputMeta.CheckResult.FieldOk" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      FileInputList fileInputList = getFiles( transMeta );

      if ( fileInputList == null || fileInputList.getFiles().size() == 0 ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "LoadFileInputMeta.CheckResult.NoFiles" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "LoadFileInputMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ), stepMeta );
        remarks.add( cr );
      }
    }
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
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      if ( !fileinfield ) {
        for ( int i = 0; i < fileName.length; i++ ) {
          FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( fileName[i] ), space );
          fileName[i] = resourceNamingInterface.nameResource( fileObject, space, Utils.isEmpty( fileMask[i] ) );
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new LoadFileInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new LoadFileInputData();
  }

  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof LoadFileInputMeta ) ) {
      return false;
    }
    LoadFileInputMeta that = (LoadFileInputMeta) o;

    if ( IsIgnoreEmptyFile != that.IsIgnoreEmptyFile ) {
      return false;
    }
    if ( IsIgnoreMissingPath != that.IsIgnoreMissingPath ) {
      return false;
    }
    if ( addresultfile != that.addresultfile ) {
      return false;
    }
    if ( fileinfield != that.fileinfield ) {
      return false;
    }
    if ( includeFilename != that.includeFilename ) {
      return false;
    }
    if ( includeRowNumber != that.includeRowNumber ) {
      return false;
    }
    if ( rowLimit != that.rowLimit ) {
      return false;
    }
    if ( DynamicFilenameField != null ? !DynamicFilenameField.equals( that.DynamicFilenameField )
        : that.DynamicFilenameField != null ) {
      return false;
    }
    if ( encoding != null ? !encoding.equals( that.encoding ) : that.encoding != null ) {
      return false;
    }
    if ( !Arrays.equals( excludeFileMask, that.excludeFileMask ) ) {
      return false;
    }
    if ( extensionFieldName != null ? !extensionFieldName.equals( that.extensionFieldName )
        : that.extensionFieldName != null ) {
      return false;
    }
    if ( !Arrays.equals( fileMask, that.fileMask ) ) {
      return false;
    }
    if ( !Arrays.equals( fileName, that.fileName ) ) {
      return false;
    }
    if ( !Arrays.equals( fileRequired, that.fileRequired ) ) {
      return false;
    }
    if ( filenameField != null ? !filenameField.equals( that.filenameField ) : that.filenameField != null ) {
      return false;
    }
    if ( hiddenFieldName != null ? !hiddenFieldName.equals( that.hiddenFieldName ) : that.hiddenFieldName != null ) {
      return false;
    }
    if ( !Arrays.equals( includeSubFolders, that.includeSubFolders ) ) {
      return false;
    }
    if ( !Arrays.equals( inputFields, that.inputFields ) ) {
      return false;
    }
    if ( lastModificationTimeFieldName != null ? !lastModificationTimeFieldName
        .equals( that.lastModificationTimeFieldName ) : that.lastModificationTimeFieldName != null ) {
      return false;
    }
    if ( pathFieldName != null ? !pathFieldName.equals( that.pathFieldName ) : that.pathFieldName != null ) {
      return false;
    }
    if ( rootUriNameFieldName != null ? !rootUriNameFieldName.equals( that.rootUriNameFieldName )
        : that.rootUriNameFieldName != null ) {
      return false;
    }
    if ( rowNumberField != null ? !rowNumberField.equals( that.rowNumberField ) : that.rowNumberField != null ) {
      return false;
    }
    if ( shortFileFieldName != null ? !shortFileFieldName.equals( that.shortFileFieldName )
        : that.shortFileFieldName != null ) {
      return false;
    }
    return !( uriNameFieldName != null ? !uriNameFieldName.equals( that.uriNameFieldName )
        : that.uriNameFieldName != null );

  }

  @Override
  public int hashCode() {
    int result = fileName != null ? Arrays.hashCode( fileName ) : 0;
    result = 31 * result + ( fileMask != null ? Arrays.hashCode( fileMask ) : 0 );
    result = 31 * result + ( excludeFileMask != null ? Arrays.hashCode( excludeFileMask ) : 0 );
    result = 31 * result + ( includeFilename ? 1 : 0 );
    result = 31 * result + ( filenameField != null ? filenameField.hashCode() : 0 );
    result = 31 * result + ( includeRowNumber ? 1 : 0 );
    result = 31 * result + ( rowNumberField != null ? rowNumberField.hashCode() : 0 );
    result = 31 * result + (int) ( rowLimit ^ ( rowLimit >>> 32 ) );
    result = 31 * result + ( inputFields != null ? Arrays.hashCode( inputFields ) : 0 );
    result = 31 * result + ( encoding != null ? encoding.hashCode() : 0 );
    result = 31 * result + ( DynamicFilenameField != null ? DynamicFilenameField.hashCode() : 0 );
    result = 31 * result + ( fileinfield ? 1 : 0 );
    result = 31 * result + ( addresultfile ? 1 : 0 );
    result = 31 * result + ( fileRequired != null ? Arrays.hashCode( fileRequired ) : 0 );
    result = 31 * result + ( IsIgnoreEmptyFile ? 1 : 0 );
    result = 31 * result + ( IsIgnoreMissingPath ? 1 : 0 );
    result = 31 * result + ( includeSubFolders != null ? Arrays.hashCode( includeSubFolders ) : 0 );
    result = 31 * result + ( shortFileFieldName != null ? shortFileFieldName.hashCode() : 0 );
    result = 31 * result + ( pathFieldName != null ? pathFieldName.hashCode() : 0 );
    result = 31 * result + ( hiddenFieldName != null ? hiddenFieldName.hashCode() : 0 );
    result = 31 * result + ( lastModificationTimeFieldName != null ? lastModificationTimeFieldName.hashCode() : 0 );
    result = 31 * result + ( uriNameFieldName != null ? uriNameFieldName.hashCode() : 0 );
    result = 31 * result + ( rootUriNameFieldName != null ? rootUriNameFieldName.hashCode() : 0 );
    result = 31 * result + ( extensionFieldName != null ? extensionFieldName.hashCode() : 0 );
    return result;
  }
}
