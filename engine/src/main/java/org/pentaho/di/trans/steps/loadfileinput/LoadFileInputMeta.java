/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.loadfileinput;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
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
  private static final String FIELD = "field";
  private static final String XML_TAG_INDENT = "    ";
  private static final String XML_TAG_INDENT2 = "      ";

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

  private static final String NO = "N";
  private static final String YES = "Y";

  public static final String[] RequiredFilesDesc = new String[] { BaseMessages.getString( PKG, "System.Combo.No" ),
    BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFilesCode = new String[] { NO, YES };


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
  private String dynamicFilenameField;

  /** Is In fields */
  private boolean fileInField;

  /** Flag: add result filename **/
  private boolean addResultFile;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] fileRequired;

  /** Flag : do we ignore empty file? */
  private boolean isIgnoreEmptyFile;

  /** Flag : do we ignore missing path? */
  private boolean isIgnoreMissingPath;

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
   * @param field The pathFieldName to set.
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
   * @param field The hiddenFieldName to set.
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
   * @see #getExcludeFileMask()
   */
  @Deprecated
  public String[] getExludeFileMask() {
    return getExcludeFileMask();
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
   * @see #getAddResultFile()
   */
  @Deprecated
  public boolean addResultFile() {
    return getAddResultFile();
  }

  /**
   * @return the add result filesname flag
   */
  public boolean getAddResultFile() {
    return addResultFile;
  }

  /**
   * @return the isIgnoreEmptyFile flag
   */
  public boolean isIgnoreEmptyFile() {
    return isIgnoreEmptyFile;
  }

  /**
   * @param isIgnoreEmptyFile the isIgnoreEmptyFile to set
   */
  public void setIgnoreEmptyFile( boolean isIgnoreEmptyFile ) {
    this.isIgnoreEmptyFile = isIgnoreEmptyFile;
  }

  /**
   * @return the isIgnoreMissingPath flag
   */
  public boolean isIgnoreMissingPath() {
    return isIgnoreMissingPath;
  }

  /**
   * @param isIgnoreMissingPath the isIgnoreMissingPath to set
   */
  public void setIgnoreMissingPath( boolean isIgnoreMissingPath ) {
    this.isIgnoreMissingPath = isIgnoreMissingPath;
  }

  public void setAddResultFile( boolean addResultFile ) {
    this.addResultFile = addResultFile;
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

  /* ***********************************
   * get and set FilenameField
   *************************************/
  /**  */
  public String getDynamicFilenameField() {
    return dynamicFilenameField;
  }

  /**  */
  public void setDynamicFilenameField( String dynamicFilenameField ) {
    this.dynamicFilenameField = dynamicFilenameField;
  }

  /* ***********************************
   * get / set fileInFields
   *************************************/
  /**  */
  public boolean getFileInFields() {
    return fileInField;
  }

  /************************************
   * @deprecated doesn't follow standard naming
   * @see #getFileInFields()
   *************************************/
  @Deprecated
  public boolean getIsInFields() {
    return getFileInFields();
  }

  /**
   * @deprecated doesn't follow standard naming
   * @see #setFileInFields(boolean)
   */
  @Deprecated
  public void setIsInFields( boolean fileInField ) {
    setFileInFields( fileInField );
  }

  public void setFileInFields( boolean fileInField ) {
    this.fileInField = fileInField;
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
   * @see #getIncludeFilename()
   */
  @Deprecated
  public boolean includeFilename() {
    return getIncludeFilename();
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
   * @see #getIncludeRowNumber()
   */
  @Deprecated
  public boolean includeRowNumber() {
    return getIncludeRowNumber();
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

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
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

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( INCLUDE, includeFilename ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( INCLUDE_FIELD, filenameField ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( ROWNUM, includeRowNumber ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( ADDRESULTFILE, addResultFile ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( IS_IGNORE_EMPTY_FILE, isIgnoreEmptyFile ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( IS_IGNORE_MISSING_PATH, isIgnoreMissingPath ) );

    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( ROWNUM_FIELD, rowNumberField ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( ENCODING, encoding ) );

    retval.append( XML_TAG_INDENT + "<" + FILE + ">" + Const.CR );
    for ( int i = 0; i < fileName.length; i++ ) {
      retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( NAME, fileName[i] ) );
      retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( FILEMASK, fileMask[i] ) );
      retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( EXCLUDE_FILEMASK, excludeFileMask[i] ) );
      retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( FILE_REQUIRED, fileRequired[i] ) );
      retval.append( XML_TAG_INDENT2 ).append( XMLHandler.addTagValue( INCLUDE_SUBFOLDERS, includeSubFolders[i] ) );
    }
    retval.append( XML_TAG_INDENT2 + "</" + FILE + ">" + Const.CR );

    retval.append( XML_TAG_INDENT + "<" + FIELDS + ">" + Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      LoadFileInputField field = inputFields[i];
      retval.append( field.getXML() );
    }
    retval.append( XML_TAG_INDENT2 + "</" + FIELDS + ">" + Const.CR );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( LIMIT, rowLimit ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( IS_IN_FIELDS, fileInField ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( DYNAMIC_FILENAME_FIELD, dynamicFilenameField ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( SHORT_FILE_FIELD_NAME, shortFileFieldName ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( PATH_FIELD_NAME, pathFieldName ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( HIDDEN_FIELD_NAME, hiddenFieldName ) );
    retval.append( XML_TAG_INDENT ).append(
        XMLHandler.addTagValue( LAST_MODIFICATION_TIME_FIELD_NAME, lastModificationTimeFieldName ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( URI_NAME_FIELD_NAME, uriNameFieldName ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( ROOT_URI_NAME_FIELD_NAME, rootUriNameFieldName ) );
    retval.append( XML_TAG_INDENT ).append( XMLHandler.addTagValue( EXTENSION_FIELD_NAME, extensionFieldName ) );

    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      includeFilename = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, INCLUDE ) );
      filenameField = XMLHandler.getTagValue( stepnode, INCLUDE_FIELD );

      addResultFile = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, ADDRESULTFILE ) );
      isIgnoreEmptyFile = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, IS_IGNORE_EMPTY_FILE ) );
      isIgnoreMissingPath = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, IS_IGNORE_MISSING_PATH ) );

      includeRowNumber = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, ROWNUM ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, ROWNUM_FIELD );
      encoding = XMLHandler.getTagValue( stepnode, ENCODING );

      Node filenode = XMLHandler.getSubNode( stepnode, FILE );
      Node fields = XMLHandler.getSubNode( stepnode, FIELDS );
      int nrFiles = XMLHandler.countNodes( filenode, NAME );
      int nrFields = XMLHandler.countNodes( fields, FIELD );

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
        Node fnode = XMLHandler.getSubNodeByNr( fields, FIELD, i );
        LoadFileInputField field = new LoadFileInputField( fnode );
        inputFields[i] = field;
      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, LIMIT ), 0L );

      fileInField = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, IS_IN_FIELDS ) );

      dynamicFilenameField = XMLHandler.getTagValue( stepnode, DYNAMIC_FILENAME_FIELD );
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

  public void allocate( int nrFiles, int nrFields ) {
    fileName = new String[ nrFiles ];
    fileMask = new String[ nrFiles ];
    excludeFileMask = new String[ nrFiles ];
    fileRequired = new String[ nrFiles ];
    includeSubFolders = new String[ nrFiles ];
    inputFields = new LoadFileInputField[ nrFields ];
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
    isIgnoreEmptyFile = false;
    isIgnoreMissingPath = false;
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    addResultFile = true;

    allocate( 0, 0 );

    rowLimit = 0;

    fileInField = false;
    dynamicFilenameField = null;
  }

  @Override
  public void getFields( Bowl bowl, RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( !getFileInFields() ) {
      r.clear();
    }

    for ( int i = 0; i < inputFields.length; i++ ) {
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

    if ( !Utils.isEmpty( getShortFileNameField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getShortFileNameField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( !Utils.isEmpty( getExtensionField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getExtensionField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( !Utils.isEmpty( getPathField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getPathField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( !Utils.isEmpty( isHiddenField() ) ) {
      ValueMetaInterface v = new ValueMetaBoolean( space.environmentSubstitute( isHiddenField() ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( !Utils.isEmpty( getLastModificationDateField() ) ) {
      ValueMetaInterface v = new ValueMetaDate( space.environmentSubstitute( getLastModificationDateField() ) );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( !Utils.isEmpty( getUriField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( !Utils.isEmpty( getRootUriField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getRootUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idStep, List<DatabaseMeta> databases ) throws KettleException {
    try {
      includeFilename = rep.getStepAttributeBoolean( idStep, INCLUDE );
      filenameField = rep.getStepAttributeString( idStep, INCLUDE_FIELD );

      addResultFile = rep.getStepAttributeBoolean( idStep, ADDRESULTFILE );
      isIgnoreEmptyFile = rep.getStepAttributeBoolean( idStep, IS_IGNORE_EMPTY_FILE );
      isIgnoreMissingPath = rep.getStepAttributeBoolean( idStep, IS_IGNORE_MISSING_PATH );

      includeRowNumber = rep.getStepAttributeBoolean( idStep, ROWNUM );
      rowNumberField = rep.getStepAttributeString( idStep, ROWNUM_FIELD );
      rowLimit = rep.getStepAttributeInteger( idStep, LIMIT );
      encoding = rep.getStepAttributeString( idStep, ENCODING );

      int nrFiles = rep.countNrStepAttributes( idStep, FILE_NAME_REP );
      int nrFields = rep.countNrStepAttributes( idStep, FIELD_NAME_REP );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        fileName[i] = rep.getStepAttributeString( idStep, i, FILE_NAME_REP );
        fileMask[i] = rep.getStepAttributeString( idStep, i, FILE_MASK_REP );
        excludeFileMask[i] = rep.getStepAttributeString( idStep, i, EXCLUDEFILE_MASK_REP );
        fileRequired[i] = rep.getStepAttributeString( idStep, i, FILE_REQUIRED );
        if ( !YES.equalsIgnoreCase( fileRequired[i] ) ) {
          fileRequired[i] = NO;
        }
        includeSubFolders[i] = rep.getStepAttributeString( idStep, i, INCLUDE_SUBFOLDERS );
        if ( !YES.equalsIgnoreCase( includeSubFolders[i] ) ) {
          includeSubFolders[i] = NO;
        }
      }

      for ( int i = 0; i < nrFields; i++ ) {
        LoadFileInputField field = new LoadFileInputField();

        field.setName( rep.getStepAttributeString( idStep, i, FIELD_NAME_REP ) );
        field.setElementType( LoadFileInputField.getElementTypeByCode( rep.getStepAttributeString( idStep, i,
            ELEMENT_TYPE_REP ) ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( idStep, i, FIELD_TYPE_REP ) ) );
        field.setFormat( rep.getStepAttributeString( idStep, i, FIELD_FORMAT_REP ) );
        field.setCurrencySymbol( rep.getStepAttributeString( idStep, i, FIELD_CURRENCY_REP ) );
        field.setDecimalSymbol( rep.getStepAttributeString( idStep, i, FIELD_DECIMAL_REP ) );
        field.setGroupSymbol( rep.getStepAttributeString( idStep, i, FIELD_GROUP_REP ) );
        field.setLength( (int) rep.getStepAttributeInteger( idStep, i, FIELD_LENGTH_REP ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( idStep, i, FIELD_PRECISION_REP ) );
        field.setTrimType( LoadFileInputField.getTrimTypeByCode( rep.getStepAttributeString( idStep, i,
            FIELD_TRIM_TYPE_REP ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( idStep, i, FIELD_REPEAT_REP ) );

        inputFields[i] = field;
      }
      fileInField = rep.getStepAttributeBoolean( idStep, IS_IN_FIELDS );

      dynamicFilenameField = rep.getStepAttributeString( idStep, DYNAMIC_FILENAME_FIELD );
      shortFileFieldName = rep.getStepAttributeString( idStep, SHORT_FILE_FIELD_NAME );
      pathFieldName = rep.getStepAttributeString( idStep, PATH_FIELD_NAME );
      hiddenFieldName = rep.getStepAttributeString( idStep, HIDDEN_FIELD_NAME );
      lastModificationTimeFieldName = rep.getStepAttributeString( idStep, LAST_MODIFICATION_TIME_FIELD_NAME );
      rootUriNameFieldName = rep.getStepAttributeString( idStep, ROOT_URI_NAME_FIELD_NAME );
      uriNameFieldName = rep.getStepAttributeString(  idStep, URI_NAME_FIELD_NAME );
      extensionFieldName = rep.getStepAttributeString( idStep, EXTENSION_FIELD_NAME );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
              "LoadFileInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {
    try {
      rep.saveStepAttribute( idTransformation, idStep, INCLUDE, includeFilename );
      rep.saveStepAttribute( idTransformation, idStep, INCLUDE_FIELD, filenameField );
      rep.saveStepAttribute( idTransformation, idStep, ADDRESULTFILE, addResultFile );
      rep.saveStepAttribute( idTransformation, idStep, IS_IGNORE_EMPTY_FILE, isIgnoreEmptyFile );
      rep.saveStepAttribute( idTransformation, idStep, IS_IGNORE_MISSING_PATH, isIgnoreMissingPath );

      rep.saveStepAttribute( idTransformation, idStep, ROWNUM, includeRowNumber );
      rep.saveStepAttribute( idTransformation, idStep, ROWNUM_FIELD, rowNumberField );
      rep.saveStepAttribute( idTransformation, idStep, LIMIT, rowLimit );
      rep.saveStepAttribute( idTransformation, idStep, ENCODING, encoding );

      for ( int i = 0; i < fileName.length; i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, FILE_NAME_REP, fileName[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, FILE_MASK_REP, fileMask[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, EXCLUDEFILE_MASK_REP, excludeFileMask[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, FILE_REQUIRED, fileRequired[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, INCLUDE_SUBFOLDERS, includeSubFolders[i] );
      }

      for ( int i = 0; i < inputFields.length; i++ ) {
        LoadFileInputField field = inputFields[i];

        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_NAME_REP, field.getName() );
        rep.saveStepAttribute( idTransformation, idStep, i, ELEMENT_TYPE_REP, field.getElementTypeCode() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_TYPE_REP, field.getTypeDesc() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_FORMAT_REP, field.getFormat() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_CURRENCY_REP, field.getCurrencySymbol() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_DECIMAL_REP, field.getDecimalSymbol() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_GROUP_REP, field.getGroupSymbol() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_LENGTH_REP, field.getLength() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_PRECISION_REP, field.getPrecision() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_TRIM_TYPE_REP, field.getTrimTypeCode() );
        rep.saveStepAttribute( idTransformation, idStep, i, FIELD_REPEAT_REP, field.isRepeated() );
      }
      rep.saveStepAttribute( idTransformation, idStep, IS_IN_FIELDS, fileInField );

      rep.saveStepAttribute( idTransformation, idStep, DYNAMIC_FILENAME_FIELD, dynamicFilenameField );
      rep.saveStepAttribute( idTransformation, idStep, SHORT_FILE_FIELD_NAME, shortFileFieldName );
      rep.saveStepAttribute( idTransformation, idStep, PATH_FIELD_NAME, pathFieldName );
      rep.saveStepAttribute( idTransformation, idStep, HIDDEN_FIELD_NAME, hiddenFieldName );
      rep.saveStepAttribute( idTransformation, idStep, LAST_MODIFICATION_TIME_FIELD_NAME,
          lastModificationTimeFieldName );
      rep.saveStepAttribute( idTransformation, idStep, URI_NAME_FIELD_NAME, uriNameFieldName );
      rep.saveStepAttribute( idTransformation, idStep, ROOT_URI_NAME_FIELD_NAME, rootUriNameFieldName );
      rep.saveStepAttribute( idTransformation, idStep, EXTENSION_FIELD_NAME, extensionFieldName );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "LoadFileInputMeta.Exception.ErrorSavingToRepository", ""
          + idStep ), e );
    }
  }

  public FileInputList getFiles( Bowl bowl, VariableSpace space ) {
    return FileInputList.createFileList( bowl, space, fileName, fileMask, excludeFileMask, fileRequired,
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

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    if ( getFileInFields() ) {
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
      FileInputList fileInputList = getFiles( transMeta.getBowl(), transMeta );

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
  @Override
  public String exportResources( Bowl bowl, VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      if ( !fileInField ) {
        for ( int i = 0; i < fileName.length; i++ ) {
          FileObject fileObject = KettleVFS.getInstance( parentStepMeta.getParentTransMeta().getBowl() )
            .getFileObject( space.environmentSubstitute( fileName[i] ), space );
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

  @Override
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

    if ( isIgnoreEmptyFile != that.isIgnoreEmptyFile ) {
      return false;
    }
    if ( isIgnoreMissingPath != that.isIgnoreMissingPath ) {
      return false;
    }
    if ( addResultFile != that.addResultFile ) {
      return false;
    }
    if ( fileInField != that.fileInField ) {
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
    if ( dynamicFilenameField != null ? !dynamicFilenameField.equals( that.dynamicFilenameField )
        : that.dynamicFilenameField != null ) {
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
    result = 31 * result + ( dynamicFilenameField != null ? dynamicFilenameField.hashCode() : 0 );
    result = 31 * result + ( fileInField ? 1 : 0 );
    result = 31 * result + ( addResultFile ? 1 : 0 );
    result = 31 * result + ( fileRequired != null ? Arrays.hashCode( fileRequired ) : 0 );
    result = 31 * result + ( isIgnoreEmptyFile ? 1 : 0 );
    result = 31 * result + ( isIgnoreMissingPath ? 1 : 0 );
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
