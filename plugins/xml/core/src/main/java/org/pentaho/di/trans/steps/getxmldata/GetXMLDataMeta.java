/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.getxmldata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.annotations.Step;
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
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
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
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Store run-time data on the getXMLData step.
 */
@Step( id = "getXMLData", image = "GXD.svg", i18nPackageName = "org.pentaho.di.trans.steps.getxmldata",
    name = "GetXMLData.name", description = "GetXMLData.description", categoryDescription = "GetXMLData.category",
    documentationUrl = "http://wiki.pentaho.com/display/EAI/Get+Data+From+XML" )
public class GetXMLDataMeta extends BaseStepMeta implements StepMetaInterface {

  public static final String TAG_INCLUDE = "include";
  public static final String TAG_INCLUDE_FIELD = "include_field";
  public static final String TAG_ROW_NUM = "rownum";
  public static final String TAG_ADD_RESULT_FILE = "addresultfile";
  public static final String TAG_NAME_SPACE_AWARE = "namespaceaware";
  public static final String TAG_IGNORE_COMMENTS = "ignorecomments";
  public static final String TAG_READ_URL = "readurl";
  public static final String TAG_VALIDATING = "validating";
  public static final String TAG_USE_TOKEN = "usetoken";
  public static final String TAG_IS_IGNORE_EMPTY_FILE = "IsIgnoreEmptyFile";
  public static final String TAG_DO_NOT_FAIL_IF_NO_FILE = "doNotFailIfNoFile";
  public static final String TAG_4_SPACES = "    ";
  public static final String TAG_ROW_NUM_FIELD = "rownum_field";
  public static final String TAG_ENCODING = "encoding";
  public static final String TAG_6_SPACES = "      ";
  public static final String TAG_NAME = "name";
  public static final String TAG_FILE_MASK = "filemask";
  public static final String TAG_EXCLUDE_FILE_MASK = "exclude_filemask";
  public static final String TAG_FILE_REQUIRED = "file_required";
  public static final String TAG_INCLUDE_SUBFOLDERS = "include_subfolders";
  public static final String TAG_LIMIT = "limit";
  public static final String TAG_LOOPXPATH = "loopxpath";
  public static final String TAG_IS_IN_FIELDS = "IsInFields";
  public static final String TAG_IS_A_FILE = "IsAFile";
  public static final String TAG_XML_FIELD = "XmlField";
  public static final String TAG_PRUNE_PATH = "prunePath";
  public static final String TAG_SHORT_FILE_FIELD_NAME = "shortFileFieldName";
  public static final String TAG_PATH_FIELD_NAME = "pathFieldName";
  public static final String TAG_HIDDEN_FIELD_NAME = "hiddenFieldName";
  public static final String TAG_LAST_MODIFICATION_TIME_FIELD_NAME = "lastModificationTimeFieldName";
  public static final String TAG_URI_NAME_FIELD_NAME = "uriNameFieldName";
  public static final String TAG_ROOT_URI_NAME_FIELD_NAME = "rootUriNameFieldName";
  public static final String TAG_EXTENSION_FIELD_NAME = "extensionFieldName";
  public static final String TAG_SIZE_FIELD_NAME = "sizeFieldName";
  public static final String TAG_FILE = "file";
  public static final String TAG_FIELDS = "fields";
  public static final String TAG_FIELD = "field";
  public static final String TAG_FILE_NAME = "file_name";
  public static final String TAG_FIELD_NAME = "field_name";
  public static final String TAG_FIELD_XPATH = "field_xpath";
  public static final String TAG_ELEMENT_TYPE = "element_type";
  public static final String TAG_RESULT_TYPE = "result_type";
  public static final String TAG_FIELD_TYPE = "field_type";
  public static final String TAG_FIELD_FORMAT = "field_format";
  public static final String TAG_FIELD_CURRENCY = "field_currency";
  public static final String TAG_FIELD_DECIMAL = "field_decimal";
  public static final String TAG_FIELD_GROUP = "field_group";
  public static final String TAG_FIELD_LENGTH = "field_length";
  public static final String TAG_FIELD_PRECISION = "field_precision";
  public static final String TAG_FIELD_TRIM_TYPE = "field_trim_type";
  public static final String TAG_FIELD_REPEAT = "field_repeat";

  private static Class<?> PKG = GetXMLDataMeta.class; // for i18n purposes, needed by Translator2!!

  private static final String YES = "Y";

  public static final String[] RequiredFilesDesc = new String[] { BaseMessages.getString( PKG, "System.Combo.No" ),
    BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  public static final String AT = "@";
  public static final String N0DE_SEPARATOR = "/";

  /** Array of filenames */
  private String[] fileName;

  /** Wildcard or filemask (regular expression) */
  private String[] fileMask;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] fileRequired;

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

  /** The XPath location to loop over */
  private String loopxpath;

  /** The fields to import... */
  private GetXMLDataField[] inputFields;

  /** The encoding to use for reading: null or empty string means system default encoding */
  private String encoding;

  /** Is In fields */
  private String xmlField;

  /** Is In fields */
  private boolean inFields;

  /** Is a File */
  private boolean IsAFile;

  /** Flag: add result filename **/
  private boolean addResultFile;

  /** Flag: set Namespace aware **/
  private boolean nameSpaceAware;

  /** Flag: set XML Validating **/
  private boolean validating;

  /** Flag : do we process use tokens? */
  private boolean usetoken;

  /** Flag : do we ignore empty files */
  private boolean IsIgnoreEmptyFile;

  /** Array of boolean values as string, indicating if we need to fetch sub folders. */
  private String[] includeSubFolders;

  /** Flag : do not fail if no file */
  private boolean doNotFailIfNoFile;

  /** Flag : ignore comments */
  private boolean ignorecomments;

  /** Flag : read url as source */
  private boolean readurl;

  // Given this path activates the streaming algorithm to process large files
  private String prunePath;

  /** Additional fields **/
  private String shortFileFieldName;
  private String pathFieldName;
  private String hiddenFieldName;
  private String lastModificationTimeFieldName;
  private String uriNameFieldName;
  private String rootUriNameFieldName;
  private String extensionFieldName;
  private String sizeFieldName;

  public GetXMLDataMeta() {
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

  /**
   * @return Returns the sizeFieldName.
   */
  public String getSizeField() {
    return sizeFieldName;
  }

  /**
   * @param field
   *          The sizeFieldName to set.
   */
  public void setSizeField( String field ) {
    sizeFieldName = field;
  }

  /**
   * @return the add result filesname flag
   */
  public boolean addResultFile() {
    return addResultFile;
  }

  /**
   * @return the validating flag
   */
  public boolean isValidating() {
    return validating;
  }

  /**
   * @param validating
   *          the validating flag to set
   */
  public void setValidating( boolean validating ) {
    this.validating = validating;
  }

  /**
   * @return the readurl flag
   */
  public boolean isReadUrl() {
    return readurl;
  }

  /**
   * @param readurl
   *          the readurl flag to set
   */
  public void setReadUrl( boolean readurl ) {
    this.readurl = readurl;
  }

  public void setAddResultFile( boolean addResultFile ) {
    this.addResultFile = addResultFile;
  }

  /**
   * @return Returns the input fields.
   */
  public GetXMLDataField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( GetXMLDataField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the excludeFileMask.
   */
  public String[] getExludeFileMask() {
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
   * Get XML field.
   */
  public String getXMLField() {
    return xmlField;
  }

  /**
   * Set XML field.
   */
  public void setXMLField( String xmlField ) {
    this.xmlField = xmlField;
  }

  /**
   * Get the IsInFields.
   */
  public boolean isInFields() {
    return inFields;
  }

  /**
   * @param inFields
   *          set the inFields.
   */
  public void setInFields( boolean inFields ) {
    this.inFields = inFields;
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

  public String[] getFileRequired() {
    return fileRequired;
  }

  public void setFileRequired( String[] fileRequiredin ) {
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      this.fileRequired[i] = getRequiredFilesCode( fileRequiredin[i] );
    }
  }

  public void setIncludeSubFolders( String[] includeSubFoldersin ) {
    for ( int i = 0; i < includeSubFoldersin.length; i++ ) {
      this.includeSubFolders[i] = getRequiredFilesCode( includeSubFoldersin[i] );
    }
  }

  /**
   * @return Returns the fileName.
   */
  public String[] getFileName() {
    return fileName;
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
   * @return Returns the LoopXPath
   */
  public String getLoopXPath() {
    return loopxpath;
  }

  /**
   * @param loopxpath
   *          The loopxpath to set.
   */
  public void setLoopXPath( String loopxpath ) {
    this.loopxpath = loopxpath;
  }

  /**
   * @param usetoken
   *          the "use token" flag to set
   */
  public void setuseToken( boolean usetoken ) {
    this.usetoken = usetoken;
  }

  /**
   * @return the use token flag
   */
  public boolean isuseToken() {
    return usetoken;
  }

  /**
   * @return the IsIgnoreEmptyFile flag
   */
  public boolean isIgnoreEmptyFile() {
    return IsIgnoreEmptyFile;
  }

  /**
   * @param IsIgnoreEmptyFile
   *          the IsIgnoreEmptyFile to set
   */
  public void setIgnoreEmptyFile( boolean IsIgnoreEmptyFile ) {
    this.IsIgnoreEmptyFile = IsIgnoreEmptyFile;
  }

  /**
   * @return the doNotFailIfNoFile flag
   */
  public boolean isdoNotFailIfNoFile() {
    return doNotFailIfNoFile;
  }

  /**
   * @param doNotFailIfNoFile
   *          the doNotFailIfNoFile to set
   */
  public void setdoNotFailIfNoFile( boolean doNotFailIfNoFile ) {
    this.doNotFailIfNoFile = doNotFailIfNoFile;
  }

  /**
   * @return the ignorecomments flag
   */
  public boolean isIgnoreComments() {
    return ignorecomments;
  }

  /**
   * @param ignorecomments
   *          the ignorecomments to set
   */
  public void setIgnoreComments( boolean ignorecomments ) {
    this.ignorecomments = ignorecomments;
  }

  /**
   * @param nameSpaceAware
   *          the name space aware flag to set
   */
  public void setNamespaceAware( boolean nameSpaceAware ) {
    this.nameSpaceAware = nameSpaceAware;
  }

  /**
   * @return the name space aware flag
   */
  public boolean isNamespaceAware() {
    return nameSpaceAware;
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

  public boolean getIsAFile() {
    return IsAFile;
  }

  public void setIsAFile( boolean IsAFile ) {
    this.IsAFile = IsAFile;
  }

  /**
   * @return the prunePath
   */
  public String getPrunePath() {
    return prunePath;
  }

  public String[] getIncludeSubFolders() {
    return includeSubFolders;
  }

  /**
   * @param prunePath
   *          the prunePath to set
   */
  public void setPrunePath( String prunePath ) {
    this.prunePath = prunePath;
  }

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public Object clone() {
    GetXMLDataMeta retval = (GetXMLDataMeta) super.clone();

    int nrFiles = fileName.length;
    int nrFields = inputFields.length;

    retval.allocate( nrFiles, nrFields );

    for ( int i = 0; i < nrFiles; i++ ) {
      retval.fileName[i] = fileName[i];
      retval.fileMask[i] = fileMask[i];
      retval.excludeFileMask[i] = excludeFileMask[i];
      retval.fileRequired[i] = fileRequired[i];
      retval.includeSubFolders[i] = includeSubFolders[i];
    }

    for ( int i = 0; i < nrFields; i++ ) {
      if ( inputFields[i] != null ) {
        retval.inputFields[i] = (GetXMLDataField) inputFields[i].clone();
      }
    }
    return retval;
  }

  public String getXML() {
    StringBuffer retval = new StringBuffer( 400 );

    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE, includeFilename ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_FIELD, filenameField ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_ROW_NUM, includeRowNumber ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_ADD_RESULT_FILE, addResultFile ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_NAME_SPACE_AWARE, nameSpaceAware ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_IGNORE_COMMENTS, ignorecomments ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_READ_URL, readurl ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_VALIDATING, validating ) );
    retval.append( TAG_4_SPACES + XMLHandler.addTagValue( TAG_USE_TOKEN, usetoken ) );
    retval.append( TAG_4_SPACES + XMLHandler.addTagValue( TAG_IS_IGNORE_EMPTY_FILE, IsIgnoreEmptyFile ) );
    retval.append( TAG_4_SPACES + XMLHandler.addTagValue( TAG_DO_NOT_FAIL_IF_NO_FILE, doNotFailIfNoFile ) );

    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_ROW_NUM_FIELD, rowNumberField ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_ENCODING, encoding ) );

    retval.append( TAG_4_SPACES + "<file>" ).append( Const.CR );
    for ( int i = 0; i < fileName.length; i++ ) {
      retval.append( TAG_6_SPACES ).append( XMLHandler.addTagValue( TAG_NAME, fileName[i] ) );
      retval.append( TAG_6_SPACES ).append( XMLHandler.addTagValue( TAG_FILE_MASK, fileMask[i] ) );
      retval.append( TAG_6_SPACES ).append( XMLHandler.addTagValue( TAG_EXCLUDE_FILE_MASK, excludeFileMask[i] ) );
      retval.append( TAG_6_SPACES ).append( XMLHandler.addTagValue( TAG_FILE_REQUIRED, fileRequired[i] ) );
      retval.append( TAG_6_SPACES ).append( XMLHandler.addTagValue( TAG_INCLUDE_SUBFOLDERS, includeSubFolders[i] ) );

    }
    retval.append( TAG_4_SPACES + "</file>" ).append( Const.CR );

    retval.append( TAG_4_SPACES + "<fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      GetXMLDataField field = inputFields[i];
      retval.append( field.getXML() );
    }
    retval.append( TAG_4_SPACES + "</fields>" ).append( Const.CR );

    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_LIMIT, rowLimit ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_LOOPXPATH, loopxpath ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_IS_IN_FIELDS, inFields ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_IS_A_FILE, IsAFile ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_XML_FIELD, xmlField ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_PRUNE_PATH, prunePath ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_SHORT_FILE_FIELD_NAME, shortFileFieldName ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_PATH_FIELD_NAME, pathFieldName ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_HIDDEN_FIELD_NAME, hiddenFieldName ) );
    retval.append( TAG_4_SPACES ).append(
        XMLHandler.addTagValue( TAG_LAST_MODIFICATION_TIME_FIELD_NAME, lastModificationTimeFieldName ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_URI_NAME_FIELD_NAME, uriNameFieldName ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_ROOT_URI_NAME_FIELD_NAME, rootUriNameFieldName ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_EXTENSION_FIELD_NAME, extensionFieldName ) );
    retval.append( TAG_4_SPACES ).append( XMLHandler.addTagValue( TAG_SIZE_FIELD_NAME, sizeFieldName ) );
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

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      includeFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_INCLUDE ) );
      filenameField = XMLHandler.getTagValue( stepnode, TAG_INCLUDE_FIELD );

      addResultFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_ADD_RESULT_FILE ) );
      nameSpaceAware = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_NAME_SPACE_AWARE ) );
      ignorecomments = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_IGNORE_COMMENTS ) );

      readurl = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_READ_URL ) );
      validating = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_VALIDATING ) );
      usetoken = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_USE_TOKEN ) );
      IsIgnoreEmptyFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_IS_IGNORE_EMPTY_FILE ) );
      doNotFailIfNoFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_DO_NOT_FAIL_IF_NO_FILE ) );

      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_ROW_NUM ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, TAG_ROW_NUM_FIELD );
      encoding = XMLHandler.getTagValue( stepnode, TAG_ENCODING );

      Node filenode = XMLHandler.getSubNode( stepnode, TAG_FILE );
      Node fields = XMLHandler.getSubNode( stepnode, TAG_FIELDS );
      int nrFiles = XMLHandler.countNodes( filenode, TAG_NAME );
      int nrFields = XMLHandler.countNodes( fields, TAG_FIELD );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        Node filenamenode = XMLHandler.getSubNodeByNr( filenode, TAG_NAME, i );
        Node filemasknode = XMLHandler.getSubNodeByNr( filenode, TAG_FILE_MASK, i );
        Node excludefilemasknode = XMLHandler.getSubNodeByNr( filenode, TAG_EXCLUDE_FILE_MASK, i );
        Node fileRequirednode = XMLHandler.getSubNodeByNr( filenode, TAG_FILE_REQUIRED, i );
        Node includeSubFoldersnode = XMLHandler.getSubNodeByNr( filenode, TAG_INCLUDE_SUBFOLDERS, i );
        fileName[i] = XMLHandler.getNodeValue( filenamenode );
        fileMask[i] = XMLHandler.getNodeValue( filemasknode );
        excludeFileMask[i] = XMLHandler.getNodeValue( excludefilemasknode );
        fileRequired[i] = XMLHandler.getNodeValue( fileRequirednode );
        includeSubFolders[i] = XMLHandler.getNodeValue( includeSubFoldersnode );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, TAG_FIELD, i );
        GetXMLDataField field = new GetXMLDataField( fnode );
        inputFields[i] = field;
      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, TAG_LIMIT ), 0L );
      // Do we skip rows before starting to read
      loopxpath = XMLHandler.getTagValue( stepnode, TAG_LOOPXPATH );

      inFields = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_IS_IN_FIELDS ) );
      IsAFile = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, TAG_IS_A_FILE ) );

      xmlField = XMLHandler.getTagValue( stepnode, TAG_XML_FIELD );
      prunePath = XMLHandler.getTagValue( stepnode, TAG_PRUNE_PATH );

      shortFileFieldName = XMLHandler.getTagValue( stepnode, TAG_SHORT_FILE_FIELD_NAME );
      pathFieldName = XMLHandler.getTagValue( stepnode, TAG_PATH_FIELD_NAME );
      hiddenFieldName = XMLHandler.getTagValue( stepnode, TAG_HIDDEN_FIELD_NAME );
      lastModificationTimeFieldName = XMLHandler.getTagValue( stepnode, TAG_LAST_MODIFICATION_TIME_FIELD_NAME );
      uriNameFieldName = XMLHandler.getTagValue( stepnode, TAG_URI_NAME_FIELD_NAME );
      rootUriNameFieldName = XMLHandler.getTagValue( stepnode, TAG_ROOT_URI_NAME_FIELD_NAME );
      extensionFieldName = XMLHandler.getTagValue( stepnode, TAG_EXTENSION_FIELD_NAME );
      sizeFieldName = XMLHandler.getTagValue( stepnode, TAG_SIZE_FIELD_NAME );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "GetXMLDataMeta.Exception.ErrorLoadingXML", e
          .toString() ) );
    }
  }

  public void allocate( int nrfiles, int nrfields ) {
    allocateFiles( nrfiles );
    inputFields = new GetXMLDataField[nrfields];
  }

  public void allocateFiles( int nrfiles ) {
    fileName = new String[nrfiles];
    fileMask = new String[nrfiles];
    excludeFileMask = new String[nrfiles];
    fileRequired = new String[nrfiles];
    includeSubFolders = new String[nrfiles];
  }

  public void setDefault() {
    shortFileFieldName = null;
    pathFieldName = null;
    hiddenFieldName = null;
    lastModificationTimeFieldName = null;
    uriNameFieldName = null;
    rootUriNameFieldName = null;
    extensionFieldName = null;
    sizeFieldName = null;

    usetoken = false;
    IsIgnoreEmptyFile = false;
    doNotFailIfNoFile = true;
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    IsAFile = false;
    addResultFile = false;
    nameSpaceAware = false;
    ignorecomments = false;
    readurl = false;
    validating = false;

    int nrFiles = 0;
    int nrFields = 0;
    loopxpath = "";

    allocate( nrFiles, nrFields );

    for ( int i = 0; i < nrFiles; i++ ) {
      fileName[i] = "filename" + ( i + 1 );
      fileMask[i] = "";
      excludeFileMask[i] = "";
      fileRequired[i] = RequiredFilesCode[0];
      includeSubFolders[i] = RequiredFilesCode[0];
    }

    for ( int i = 0; i < nrFields; i++ ) {
      inputFields[i] = new GetXMLDataField( TAG_FIELD + ( i + 1 ) );
    }

    rowLimit = 0;

    inFields = false;
    xmlField = "";
    prunePath = "";
  }

  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    int i;
    for ( i = 0; i < inputFields.length; i++ ) {
      GetXMLDataField field = inputFields[i];

      int type = field.getType();
      if ( type == ValueMeta.TYPE_NONE ) {
        type = ValueMeta.TYPE_STRING;
      }
      try {
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( space.environmentSubstitute( field.getName() ), type );
        v.setLength( field.getLength() );
        v.setPrecision( field.getPrecision() );
        v.setOrigin( name );
        v.setConversionMask( field.getFormat() );
        v.setDecimalSymbol( field.getDecimalSymbol() );
        v.setGroupingSymbol( field.getGroupSymbol() );
        v.setCurrencySymbol( field.getCurrencySymbol() );
        r.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }

    if ( includeFilename ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( filenameField ), ValueMeta.TYPE_STRING );
      v.setLength( 250 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( includeRowNumber ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( rowNumberField ), ValueMeta.TYPE_INTEGER );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    // Add additional fields

    if ( getShortFileNameField() != null && getShortFileNameField().length() > 0 ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( getShortFileNameField() ), ValueMeta.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getExtensionField() != null && getExtensionField().length() > 0 ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( getExtensionField() ), ValueMeta.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getPathField() != null && getPathField().length() > 0 ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( getPathField() ), ValueMeta.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getSizeField() != null && getSizeField().length() > 0 ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( getSizeField() ), ValueMeta.TYPE_INTEGER );
      v.setOrigin( name );
      v.setLength( 9 );
      r.addValueMeta( v );
    }
    if ( isHiddenField() != null && isHiddenField().length() > 0 ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( isHiddenField() ), ValueMeta.TYPE_BOOLEAN );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( getLastModificationDateField() != null && getLastModificationDateField().length() > 0 ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( getLastModificationDateField() ), ValueMeta.TYPE_DATE );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( getUriField() != null && getUriField().length() > 0 ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( getUriField() ), ValueMeta.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( getRootUriField() != null && getRootUriField().length() > 0 ) {
      ValueMetaInterface v = new ValueMeta( space.environmentSubstitute( getRootUriField() ), ValueMeta.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {

    try {
      includeFilename = rep.getStepAttributeBoolean( id_step, TAG_INCLUDE );
      filenameField = rep.getStepAttributeString( id_step, TAG_INCLUDE_FIELD );

      addResultFile = rep.getStepAttributeBoolean( id_step, TAG_ADD_RESULT_FILE );
      nameSpaceAware = rep.getStepAttributeBoolean( id_step, TAG_NAME_SPACE_AWARE );
      ignorecomments = rep.getStepAttributeBoolean( id_step, TAG_IGNORE_COMMENTS );
      readurl = rep.getStepAttributeBoolean( id_step, TAG_READ_URL );

      validating = rep.getStepAttributeBoolean( id_step, TAG_VALIDATING );
      usetoken = rep.getStepAttributeBoolean( id_step, TAG_USE_TOKEN );
      IsIgnoreEmptyFile = rep.getStepAttributeBoolean( id_step, TAG_IS_IGNORE_EMPTY_FILE );
      doNotFailIfNoFile = rep.getStepAttributeBoolean( id_step, TAG_DO_NOT_FAIL_IF_NO_FILE );

      includeRowNumber = rep.getStepAttributeBoolean( id_step, TAG_ROW_NUM );
      rowNumberField = rep.getStepAttributeString( id_step, TAG_ROW_NUM_FIELD );
      rowLimit = rep.getStepAttributeInteger( id_step, TAG_LIMIT );
      loopxpath = rep.getStepAttributeString( id_step, TAG_LOOPXPATH );
      encoding = rep.getStepAttributeString( id_step, TAG_ENCODING );

      int nrFiles = rep.countNrStepAttributes( id_step, TAG_FILE_NAME );
      int nrFields = rep.countNrStepAttributes( id_step, TAG_FIELD_NAME );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        fileName[i] = rep.getStepAttributeString( id_step, i, TAG_FILE_NAME );
        fileMask[i] = rep.getStepAttributeString( id_step, i, TAG_FILE_MASK );
        excludeFileMask[i] = rep.getStepAttributeString( id_step, i, TAG_EXCLUDE_FILE_MASK );
        fileRequired[i] = rep.getStepAttributeString( id_step, i, TAG_FILE_REQUIRED );
        includeSubFolders[i] = rep.getStepAttributeString( id_step, i, TAG_INCLUDE_SUBFOLDERS );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        GetXMLDataField field = new GetXMLDataField();

        field.setName( rep.getStepAttributeString( id_step, i, TAG_FIELD_NAME ) );
        field.setXPath( rep.getStepAttributeString( id_step, i, TAG_FIELD_XPATH ) );
        field.setElementType( GetXMLDataField.getElementTypeByCode( rep.getStepAttributeString( id_step, i,
          TAG_ELEMENT_TYPE ) ) );
        field.setResultType( GetXMLDataField.getResultTypeByCode( rep
            .getStepAttributeString( id_step, i, TAG_RESULT_TYPE ) ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, TAG_FIELD_TYPE ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, TAG_FIELD_FORMAT ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, TAG_FIELD_CURRENCY ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, TAG_FIELD_DECIMAL ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, TAG_FIELD_GROUP ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, TAG_FIELD_LENGTH ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, TAG_FIELD_PRECISION ) );
        field.setTrimType( GetXMLDataField.getTrimTypeByCode( rep
            .getStepAttributeString( id_step, i, TAG_FIELD_TRIM_TYPE ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, TAG_FIELD_REPEAT ) );

        inputFields[i] = field;
      }
      inFields = rep.getStepAttributeBoolean( id_step, TAG_IS_IN_FIELDS );
      IsAFile = rep.getStepAttributeBoolean( id_step, TAG_IS_A_FILE );

      xmlField = rep.getStepAttributeString( id_step, TAG_XML_FIELD );
      prunePath = rep.getStepAttributeString( id_step, TAG_PRUNE_PATH );

      shortFileFieldName = rep.getStepAttributeString( id_step, TAG_SHORT_FILE_FIELD_NAME );
      extensionFieldName = rep.getStepAttributeString( id_step, TAG_EXTENSION_FIELD_NAME );
      pathFieldName = rep.getStepAttributeString( id_step, TAG_PATH_FIELD_NAME );
      sizeFieldName = rep.getStepAttributeString( id_step, TAG_SIZE_FIELD_NAME );
      hiddenFieldName = rep.getStepAttributeString( id_step, TAG_HIDDEN_FIELD_NAME );
      lastModificationTimeFieldName = rep.getStepAttributeString( id_step, TAG_LAST_MODIFICATION_TIME_FIELD_NAME );
      uriNameFieldName = rep.getStepAttributeString( id_step, TAG_URI_NAME_FIELD_NAME );
      rootUriNameFieldName = rep.getStepAttributeString( id_step, TAG_ROOT_URI_NAME_FIELD_NAME );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetXMLDataMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, TAG_INCLUDE, includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, TAG_INCLUDE_FIELD, filenameField );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ADD_RESULT_FILE, addResultFile );
      rep.saveStepAttribute( id_transformation, id_step, TAG_NAME_SPACE_AWARE, nameSpaceAware );
      rep.saveStepAttribute( id_transformation, id_step, TAG_IGNORE_COMMENTS, ignorecomments );
      rep.saveStepAttribute( id_transformation, id_step, TAG_READ_URL, readurl );

      rep.saveStepAttribute( id_transformation, id_step, TAG_VALIDATING, validating );
      rep.saveStepAttribute( id_transformation, id_step, TAG_USE_TOKEN, usetoken );
      rep.saveStepAttribute( id_transformation, id_step, TAG_IS_IGNORE_EMPTY_FILE, IsIgnoreEmptyFile );
      rep.saveStepAttribute( id_transformation, id_step, TAG_DO_NOT_FAIL_IF_NO_FILE, doNotFailIfNoFile );

      rep.saveStepAttribute( id_transformation, id_step, TAG_ROW_NUM, includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ROW_NUM_FIELD, rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, TAG_LIMIT, rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, TAG_LOOPXPATH, loopxpath );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ENCODING, encoding );

      for ( int i = 0; i < fileName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FILE_NAME, fileName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FILE_MASK, fileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_EXCLUDE_FILE_MASK, excludeFileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FILE_REQUIRED, fileRequired[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_INCLUDE_SUBFOLDERS, includeSubFolders[i] );
      }

      for ( int i = 0; i < inputFields.length; i++ ) {
        GetXMLDataField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_NAME, field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_XPATH, field.getXPath() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_ELEMENT_TYPE, field.getElementTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_RESULT_TYPE, field.getResultTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_TYPE, field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_FORMAT, field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_CURRENCY, field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_DECIMAL, field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_GROUP, field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_LENGTH, field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_PRECISION, field.getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_TRIM_TYPE, field.getTrimTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, TAG_FIELD_REPEAT, field.isRepeated() );
      }
      rep.saveStepAttribute( id_transformation, id_step, TAG_IS_IN_FIELDS, inFields );
      rep.saveStepAttribute( id_transformation, id_step, TAG_IS_A_FILE, IsAFile );

      rep.saveStepAttribute( id_transformation, id_step, TAG_XML_FIELD, xmlField );
      rep.saveStepAttribute( id_transformation, id_step, TAG_PRUNE_PATH, prunePath );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SHORT_FILE_FIELD_NAME, shortFileFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_EXTENSION_FIELD_NAME, extensionFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_PATH_FIELD_NAME, pathFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_SIZE_FIELD_NAME, sizeFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_HIDDEN_FIELD_NAME, hiddenFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_LAST_MODIFICATION_TIME_FIELD_NAME, lastModificationTimeFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_URI_NAME_FIELD_NAME, uriNameFieldName );
      rep.saveStepAttribute( id_transformation, id_step, TAG_ROOT_URI_NAME_FIELD_NAME, rootUriNameFieldName );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "GetXMLDataMeta.Exception.ErrorSavingToRepository", ""
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

    // See if we get input...
    if ( input.length <= 0 ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "GetXMLDataMeta.CheckResult.NoInputExpected" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "GetXMLDataMeta.CheckResult.NoInput" ), stepMeta );
      remarks.add( cr );
    }

    // control Xpath
    if ( getLoopXPath() == null || Utils.isEmpty( getLoopXPath() ) ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "GetXMLDataMeta.CheckResult.NoLoopXpath" ), stepMeta );
      remarks.add( cr );
    }
    if ( getInputFields().length <= 0 ) {
      cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "GetXMLDataMeta.CheckResult.NoInputField" ), stepMeta );
      remarks.add( cr );
    }

    if ( isInFields() ) {
      if ( Utils.isEmpty( getXMLField() ) ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "GetXMLDataMeta.CheckResult.NoField" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "GetXMLDataMeta.CheckResult.FieldOk" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      FileInputList fileInputList = getFiles( transMeta );
      // String files[] = getFiles();
      if ( fileInputList == null || fileInputList.getFiles().size() == 0 ) {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "GetXMLDataMeta.CheckResult.NoFiles" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "GetXMLDataMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ), stepMeta );
        remarks.add( cr );
      }
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans ) {
    return new GetXMLData( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new GetXMLDataData();
  }

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
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore )
    throws KettleException {
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
          fileName = newFilenames.toArray( new String[newFilenames.size()] );
          fileMask = new String[newFilenames.size()]; // all null since converted to absolute path.
          fileRequired = new String[newFilenames.size()]; // all null, turn to "Y" :
          for ( int i = 0; i < newFilenames.size(); i++ ) {
            fileRequired[i] = "Y";
          }
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new GetXMLDataMetaInjection( this );
  }

  @Override
  public TransMeta.TransformationType[] getSupportedTransformationTypes() {
    return new TransMeta.TransformationType[] { TransMeta.TransformationType.Normal };
  }
}
