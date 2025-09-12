/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.propertyinput;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.bowl.Bowl;
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
import org.pentaho.di.core.util.Utils;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PropertyInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = PropertyInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };

  private static final String YES = "Y";
  private static final String NO = "N";
  public static final String[] RequiredFilesCode = new String[] { NO, YES };

  public static final String DEFAULT_ENCODING = "UTF-8";

  public static final String[] type_trim_code = { "none", "left", "right", "both" };

  public static final String[] column_code = { "key", "value" };

  public static final String[] fileTypeDesc = new String[] {
    BaseMessages.getString( PKG, "PropertyInputMeta.FileType.Property" ),
    BaseMessages.getString( PKG, "PropertyInputMeta.FileType.Ini" ) };
  public static final String[] fileTypeCode = new String[] { "property", "ini" };
  public static final int FILE_TYPE_PROPERTY = 0;
  public static final int FILE_TYPE_INI = 1;

  private String encoding;

  private String fileType;

  /** Array of filenames */
  private String[] fileName;

  /** Wildcard or filemask (regular expression) */
  private String[] fileMask;

  /** Wildcard or filemask to exclude (regular expression) */
  private String[] excludeFileMask;

  /** Flag indicating that we should include the filename in the output */
  private boolean includeFilename;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] fileRequired;

  /** Array of boolean values as string, indicating if we need to fetch sub folders. */
  private String[] includeSubFolders;

  /** Flag indicating that we should reset RowNum for each file */
  private boolean resetRowNumber;

  /** Flag do variable substitution for value */
  private boolean resolveValueVariable;

  /** The name of the field in the output containing the filename */
  private String filenameField;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The maximum number of lines to read */
  private long rowLimit;

  /** The fields to import... */
  private PropertyInputField[] inputFields;

  /** file name from previous fields **/
  private boolean fileField;

  private boolean isAddResult;

  private String dynamicFilenameField;

  /** Flag indicating that a INI file section field should be included in the output */
  private boolean includeIniSection;

  /** The name of the field in the output containing the INI file section */
  private String iniSectionField;

  private String section;

  /** Additional fields **/
  private String shortFileFieldName;
  private String pathFieldName;
  private String hiddenFieldName;
  private String lastModificationTimeFieldName;
  private String uriNameFieldName;
  private String rootUriNameFieldName;
  private String extensionFieldName;
  private String sizeFieldName;

  public PropertyInputMeta() {
    super(); // allocate BaseStepMeta
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
   * @return Returns the rootUriNameFieldName.
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
   * @return Returns the input fields.
   */
  public PropertyInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( PropertyInputField[] inputFields ) {
    this.inputFields = inputFields;
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

  /**
   * @return Returns the excludeFileMask.
   * @deprecated due to typo
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

  public String[] getFileRequired() {
    return this.fileRequired;
  }

  public void setFileRequired( String[] fileRequiredIn ) {
    for ( int i = 0; i < fileRequiredIn.length; i++ ) {
      this.fileRequired[i] = getRequiredFilesCode( fileRequiredIn[i] );
    }
  }

  public String[] getIncludeSubFolders() {
    return includeSubFolders;
  }

  public void setIncludeSubFolders( String[] includeSubFoldersIn ) {
    for ( int i = 0; i < includeSubFoldersIn.length; i++ ) {
      this.includeSubFolders[i] = getRequiredFilesCode( includeSubFoldersIn[i] );
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
   * @return Returns the dynamically defined filename field (to read from previous steps).
   */
  public String getDynamicFilenameField() {
    return dynamicFilenameField;
  }

  /**
   * @param dynamicFilenameField
   *          the dynamically defined filename field (to read from previous steps)
   */
  public void setDynamicFilenameField( String dynamicFilenameField ) {
    this.dynamicFilenameField = dynamicFilenameField;
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

  public static String getFileTypeCode( int i ) {
    if ( i < 0 || i >= fileTypeCode.length ) {
      return fileTypeCode[0];
    }
    return fileTypeCode[i];
  }

  public static int getFileTypeByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < fileTypeDesc.length; i++ ) {
      if ( fileTypeDesc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getFileTypeByCode( tt );
  }

  public static int getFileTypeByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < fileTypeCode.length; i++ ) {
      if ( fileTypeCode[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static String getFileTypeDesc( int i ) {
    if ( i < 0 || i >= fileTypeDesc.length ) {
      return fileTypeDesc[0];
    }
    return fileTypeDesc[i];
  }

  public void setFileType( String filetype ) {
    this.fileType = filetype;
  }

  public String getFileType() {
    return fileType;
  }

  /**
   * @param includeIniSection
   *          The includeIniSection to set.
   */
  public void setIncludeIniSection( boolean includeIniSection ) {
    this.includeIniSection = includeIniSection;
  }

  /**
   * @return Returns the includeIniSection.
   */
  public boolean includeIniSection() {
    return includeIniSection;
  }

  /**
   * @param encoding
   *          The encoding to set.
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @return Returns encoding.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param iniSectionField
   *          The iniSectionField to set.
   */
  public void setINISectionField( String iniSectionField ) {
    this.iniSectionField = iniSectionField;
  }

  /**
   * @return Returns the iniSectionField.
   */
  public String getINISectionField() {
    return iniSectionField;
  }

  /**
   * @param section
   *          The section to set.
   */
  public void setSection( String section ) {
    this.section = section;
  }

  /**
   * @return Returns the section.
   */
  public String getSection() {
    return section;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @return Returns the File field.
   */
  public boolean isFileField() {
    return fileField;
  }

  /**
   * @param fileField
   *          The fileField to set.
   */
  public void setFileField( boolean fileField ) {
    this.fileField = fileField;
  }

  /**
   * @return Returns the resetRowNumber.
   */
  public boolean resetRowNumber() {
    return resetRowNumber;
  }

  /**
   * @param includeRowNumber
   *          The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @param isAddResult
   *          The isAddResult to set.
   */
  public void setAddResultFile( boolean isAddResult ) {
    this.isAddResult = isAddResult;
  }

  /**
   * @return Returns isAddResult.
   */
  public boolean isAddResultFile() {
    return isAddResult;
  }

  /**
   * @param resetRowNumber
   *          The resetRowNumber to set.
   */
  public void setResetRowNumber( boolean resetRowNumber ) {
    this.resetRowNumber = resetRowNumber;
  }

  /**
   * @param resolveValueVariable
   *          The resolveValueVariable to set.
   */
  public void setResolveValueVariable( boolean resolveValueVariable ) {
    this.resolveValueVariable = resolveValueVariable;
  }

  /**
   * @return Returns resolveValueVariable.
   */
  public boolean isResolveValueVariable() {
    return resolveValueVariable;
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

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    PropertyInputMeta retval = (PropertyInputMeta) super.clone();
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
        retval.inputFields[i] = (PropertyInputField) inputFields[i].clone();
      }
    }

    return retval;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );
    retval.append( "    " ).append( XMLHandler.addTagValue( "file_type", fileType ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include", includeFilename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_field", filenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "filename_Field", dynamicFilenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "isaddresult", isAddResult ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "filefield", fileField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "resetrownumber", resetRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "resolvevaluevariable", resolveValueVariable ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "ini_section", includeIniSection ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "ini_section_field", iniSectionField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "section", section ) );
    retval.append( "    <file>" ).append( Const.CR );
    for ( int i = 0; i < fileName.length; i++ ) {
      retval.append( "      " ).append( XMLHandler.addTagValue( "name", fileName[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "exclude_filemask", excludeFileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "filemask", fileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "file_required", fileRequired[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", includeSubFolders[i] ) );
      parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( fileName[i] );
    }
    retval.append( "    </file>" ).append( Const.CR );

    /*
     * Describe the fields to read
     */
    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", inputFields[i].getName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "column", inputFields[i].getColumnCode() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "type", inputFields[i].getTypeDesc() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "format", inputFields[i].getFormat() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "length", inputFields[i].getLength() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "precision", inputFields[i].getPrecision() ) );
      retval
        .append( "        " ).append( XMLHandler.addTagValue( "currency", inputFields[i].getCurrencySymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", inputFields[i].getDecimalSymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "group", inputFields[i].getGroupSymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "trim_type", inputFields[i].getTrimTypeCode() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "repeat", inputFields[i].isRepeated() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", rowLimit ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "shortFileFieldName", shortFileFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "pathFieldName", pathFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "hiddenFieldName", hiddenFieldName ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "lastModificationTimeFieldName", lastModificationTimeFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "uriNameFieldName", uriNameFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rootUriNameFieldName", rootUriNameFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "extensionFieldName", extensionFieldName ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sizeFieldName", sizeFieldName ) );
    return retval.toString();
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      fileType = XMLHandler.getTagValue( stepnode, "file_type" );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      includeFilename = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include" ) );
      filenameField = XMLHandler.getTagValue( stepnode, "include_field" );
      includeRowNumber = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );

      String addResult = XMLHandler.getTagValue( stepnode, "isaddresult" );
      if ( Utils.isEmpty( addResult ) ) {
        isAddResult = true;
      } else {
        isAddResult = YES.equalsIgnoreCase( addResult );
      }
      section = XMLHandler.getTagValue( stepnode, "section" );
      iniSectionField = XMLHandler.getTagValue( stepnode, "ini_section_field" );
      includeIniSection = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "ini_section" ) );
      fileField = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "filefield" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      dynamicFilenameField = XMLHandler.getTagValue( stepnode, "filename_Field" );
      resetRowNumber = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "resetrownumber" ) );
      resolveValueVariable = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "resolvevaluevariable" ) );
      Node filenode = XMLHandler.getSubNode( stepnode, "file" );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrFiles = XMLHandler.countNodes( filenode, "name" );
      int nrFields = XMLHandler.countNodes( fields, "field" );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        Node filenamenode = XMLHandler.getSubNodeByNr( filenode, "name", i );
        Node filemasknode = XMLHandler.getSubNodeByNr( filenode, "filemask", i );
        Node excludefilemasknode = XMLHandler.getSubNodeByNr( filenode, "exclude_filemask", i );
        Node fileRequirednode = XMLHandler.getSubNodeByNr( filenode, "file_required", i );
        Node includeSubFoldersnode = XMLHandler.getSubNodeByNr( filenode, "include_subfolders", i );
        fileName[i] = XMLHandler.getNodeValue( filenamenode );
        fileMask[i] = XMLHandler.getNodeValue( filemasknode );
        excludeFileMask[i] = XMLHandler.getNodeValue( excludefilemasknode );
        fileRequired[i] = XMLHandler.getNodeValue( fileRequirednode );
        includeSubFolders[i] = XMLHandler.getNodeValue( includeSubFoldersnode );
      }

      for ( int i = 0; i < nrFields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        inputFields[i] = new PropertyInputField();

        inputFields[i].setName( XMLHandler.getTagValue( fnode, "name" ) );
        inputFields[i].setColumn( getColumnByCode( XMLHandler.getTagValue( fnode, "column" ) ) );
        inputFields[i].setType( ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) ) );
        inputFields[i].setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
        inputFields[i].setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
        inputFields[i].setTrimType( getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );

        String srepeat = XMLHandler.getTagValue( fnode, "repeat" );
        if ( srepeat != null ) {
          inputFields[i].setRepeated( YES.equalsIgnoreCase( srepeat ) );
        } else {
          inputFields[i].setRepeated( false );
        }

        inputFields[i].setFormat( XMLHandler.getTagValue( fnode, "format" ) );
        inputFields[i].setCurrencySymbol( XMLHandler.getTagValue( fnode, "currency" ) );
        inputFields[i].setDecimalSymbol( XMLHandler.getTagValue( fnode, "decimal" ) );
        inputFields[i].setGroupSymbol( XMLHandler.getTagValue( fnode, "group" ) );
      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, "limit" ), 0L );
      shortFileFieldName = XMLHandler.getTagValue( stepnode, "shortFileFieldName" );
      pathFieldName = XMLHandler.getTagValue( stepnode, "pathFieldName" );
      hiddenFieldName = XMLHandler.getTagValue( stepnode, "hiddenFieldName" );
      lastModificationTimeFieldName = XMLHandler.getTagValue( stepnode, "lastModificationTimeFieldName" );
      uriNameFieldName = XMLHandler.getTagValue( stepnode, "uriNameFieldName" );
      rootUriNameFieldName = XMLHandler.getTagValue( stepnode, "rootUriNameFieldName" );
      extensionFieldName = XMLHandler.getTagValue( stepnode, "extensionFieldName" );
      sizeFieldName = XMLHandler.getTagValue( stepnode, "sizeFieldName" );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void allocate( int nrFiles, int nrFields ) {
    fileName = new String[ nrFiles ];
    fileMask = new String[ nrFiles ];
    excludeFileMask = new String[ nrFiles ];
    fileRequired = new String[ nrFiles ];
    includeSubFolders = new String[ nrFiles ];
    inputFields = new PropertyInputField[ nrFields ];
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

    fileType = fileTypeCode[0];
    section = "";
    encoding = DEFAULT_ENCODING;
    includeIniSection = false;
    iniSectionField = "";
    resolveValueVariable = false;
    isAddResult = true;
    fileField = false;
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    dynamicFilenameField = "";

    allocate( 0, 0 );

    rowLimit = 0;
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    for ( PropertyInputField field : inputFields ) {
      int type = field.getType();
      if ( type == ValueMetaInterface.TYPE_NONE ) {
        type = ValueMetaInterface.TYPE_STRING;
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
      String realFilenameField = space.environmentSubstitute( filenameField );
      if ( !Utils.isEmpty( realFilenameField ) ) {
        ValueMetaInterface v = new ValueMetaString( realFilenameField, 500, -1 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
    }

    if ( includeRowNumber ) {
      String realRowNumberField = space.environmentSubstitute( rowNumberField );
      if ( !Utils.isEmpty( realRowNumberField ) ) {
        ValueMetaInterface v = new ValueMetaInteger( realRowNumberField, ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
    }

    if ( includeIniSection ) {
      String realSectionField = space.environmentSubstitute( iniSectionField );
      if ( !Utils.isEmpty( realSectionField ) ) {
        ValueMetaInterface v = new ValueMetaString( realSectionField, 500, -1 );
        v.setOrigin( name );
        r.addValueMeta( v );
      }
    }

    getAdditionalFields( r, name, space );
  }

  private void getAdditionalFields( RowMetaInterface r, String name, VariableSpace space ) {
    if ( !Utils.isEmpty( getShortFileNameField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getShortFileNameField() ), 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( !Utils.isEmpty( getExtensionField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getExtensionField() ), 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( !Utils.isEmpty( getPathField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getPathField() ), 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
    if ( !Utils.isEmpty( getSizeField() ) ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( getSizeField() ), 9, -1 );
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
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getUriField() ), 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( !Utils.isEmpty( getRootUriField() ) ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( getRootUriField() ), 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public static int getTrimTypeByCode( String tt ) {
    if ( tt != null ) {
      for ( int i = 0; i < type_trim_code.length; i++ ) {
        if ( type_trim_code[ i ].equalsIgnoreCase( tt ) ) {
          return i;
        }
      }
    }
    return 0;
  }

  public static int getColumnByCode( String tt ) {
    if ( tt != null ) {
      for ( int i = 0; i < column_code.length; i++ ) {
        if ( column_code[ i ].equalsIgnoreCase( tt ) ) {
          return i;
        }
      }
    }
    return 0;
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId idSstep, List<DatabaseMeta> databases ) throws KettleException {
    try {
      fileType = rep.getStepAttributeString( idSstep, "file_type" );
      section = rep.getStepAttributeString( idSstep, "section" );
      encoding = rep.getStepAttributeString( idSstep, "encoding" );
      includeIniSection = rep.getStepAttributeBoolean( idSstep, "ini_section" );
      iniSectionField = rep.getStepAttributeString( idSstep, "ini_section_field" );
      includeFilename = rep.getStepAttributeBoolean( idSstep, "include" );
      filenameField = rep.getStepAttributeString( idSstep, "include_field" );
      dynamicFilenameField = rep.getStepAttributeString( idSstep, "filename_Field" );
      includeRowNumber = rep.getStepAttributeBoolean( idSstep, "rownum" );

      String addresult = rep.getStepAttributeString( idSstep, "isaddresult" );
      if ( Utils.isEmpty( addresult ) ) {
        isAddResult = true;
      } else {
        isAddResult = rep.getStepAttributeBoolean( idSstep, "isaddresult" );
      }

      fileField = rep.getStepAttributeBoolean( idSstep, "filefield" );
      rowNumberField = rep.getStepAttributeString( idSstep, "rownum_field" );
      resetRowNumber = rep.getStepAttributeBoolean( idSstep, "reset_rownumber" );
      resolveValueVariable = rep.getStepAttributeBoolean( idSstep, "resolve_value_variable" );

      rowLimit = rep.getStepAttributeInteger( idSstep, "limit" );
      int nrFiles = rep.countNrStepAttributes( idSstep, "file_name" );
      int nrFields = rep.countNrStepAttributes( idSstep, "field_name" );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        fileName[i] = rep.getStepAttributeString( idSstep, i, "file_name" );
        fileMask[i] = rep.getStepAttributeString( idSstep, i, "file_mask" );
        excludeFileMask[i] = rep.getStepAttributeString( idSstep, i, "exclude_file_mask" );
        fileRequired[i] = rep.getStepAttributeString( idSstep, i, "file_required" );
        if ( !YES.equalsIgnoreCase( fileRequired[i] ) ) {
          fileRequired[i] = RequiredFilesCode[0];
        }
        includeSubFolders[i] = rep.getStepAttributeString( idSstep, i, "include_subfolders" );
        if ( !YES.equalsIgnoreCase( includeSubFolders[i] ) ) {
          includeSubFolders[i] = RequiredFilesCode[0];
        }
      }

      for ( int i = 0; i < nrFields; i++ ) {
        PropertyInputField field = new PropertyInputField();

        field.setName( rep.getStepAttributeString( idSstep, i, "field_name" ) );
        field.setColumn( PropertyInputField.getColumnByCode( rep.getStepAttributeString(
          idSstep, i, "field_column" ) ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( idSstep, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( idSstep, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( idSstep, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( idSstep, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( idSstep, i, "field_group" ) );
        field.setLength( (int) rep.getStepAttributeInteger( idSstep, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( idSstep, i, "field_precision" ) );
        field.setTrimType(
          PropertyInputField.getTrimTypeByCode( rep.getStepAttributeString( idSstep, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( idSstep, i, "field_repeat" ) );

        inputFields[i] = field;
      }
      shortFileFieldName = rep.getStepAttributeString( idSstep, "shortFileFieldName" );
      pathFieldName = rep.getStepAttributeString( idSstep, "pathFieldName" );
      hiddenFieldName = rep.getStepAttributeString( idSstep, "hiddenFieldName" );
      lastModificationTimeFieldName = rep.getStepAttributeString( idSstep, "lastModificationTimeFieldName" );
      uriNameFieldName = rep.getStepAttributeString( idSstep, "uriNameFieldName" );
      rootUriNameFieldName = rep.getStepAttributeString( idSstep, "rootUriNameFieldName" );
      extensionFieldName = rep.getStepAttributeString( idSstep, "extensionFieldName" );
      sizeFieldName = rep.getStepAttributeString( idSstep, "sizeFieldName" );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages
        .getString( PKG, "PropertyInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId idTransformation, ObjectId idStep ) throws KettleException {
    try {
      rep.saveStepAttribute( idTransformation, idStep, "file_type", fileType );
      rep.saveStepAttribute( idTransformation, idStep, "section", section );
      rep.saveStepAttribute( idTransformation, idStep, "encoding", encoding );
      rep.saveStepAttribute( idTransformation, idStep, "ini_section", includeIniSection );
      rep.saveStepAttribute( idTransformation, idStep, "ini_section_field", iniSectionField );
      rep.saveStepAttribute( idTransformation, idStep, "include", includeFilename );
      rep.saveStepAttribute( idTransformation, idStep, "include_field", filenameField );
      rep.saveStepAttribute( idTransformation, idStep, "rownum", includeRowNumber );
      rep.saveStepAttribute( idTransformation, idStep, "isaddresult", isAddResult );
      rep.saveStepAttribute( idTransformation, idStep, "filefield", fileField );
      rep.saveStepAttribute( idTransformation, idStep, "filename_Field", dynamicFilenameField );
      rep.saveStepAttribute( idTransformation, idStep, "rownum_field", rowNumberField );
      rep.saveStepAttribute( idTransformation, idStep, "limit", rowLimit );
      rep.saveStepAttribute( idTransformation, idStep, "reset_rownumber", resetRowNumber );
      rep.saveStepAttribute( idTransformation, idStep, "resolve_value_variable", resolveValueVariable );
      rep.saveStepAttribute( idTransformation, idStep, "sizeFieldName", sizeFieldName );

      for ( int i = 0; i < fileName.length; i++ ) {
        rep.saveStepAttribute( idTransformation, idStep, i, "file_name", fileName[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, "file_mask", fileMask[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, "exclude_file_mask", excludeFileMask[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, "file_required", fileRequired[i] );
        rep.saveStepAttribute( idTransformation, idStep, i, "include_subfolders", includeSubFolders[i] );
      }

      for ( int i = 0; i < inputFields.length; i++ ) {
        PropertyInputField field = inputFields[i];

        rep.saveStepAttribute( idTransformation, idStep, i, "field_name", field.getName() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_column", field.getColumnCode() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_format", field.getFormat() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_currency", field.getCurrencySymbol() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_decimal", field.getDecimalSymbol() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_group", field.getGroupSymbol() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_length", field.getLength() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_precision", field.getPrecision() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_trim_type", field.getTrimTypeCode() );
        rep.saveStepAttribute( idTransformation, idStep, i, "field_repeat", field.isRepeated() );
      }

      rep.saveStepAttribute( idTransformation, idStep, "shortFileFieldName", shortFileFieldName );
      rep.saveStepAttribute( idTransformation, idStep, "pathFieldName", pathFieldName );
      rep.saveStepAttribute( idTransformation, idStep, "hiddenFieldName", hiddenFieldName );
      rep.saveStepAttribute(
        idTransformation, idStep, "lastModificationTimeFieldName", lastModificationTimeFieldName );
      rep.saveStepAttribute( idTransformation, idStep, "uriNameFieldName", uriNameFieldName );
      rep.saveStepAttribute( idTransformation, idStep, "rootUriNameFieldName", rootUriNameFieldName );
      rep.saveStepAttribute( idTransformation, idStep, "extensionFieldName", extensionFieldName );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "PropertyInputMeta.Exception.ErrorSavingToRepository", "" + idStep ), e );
    }
  }

  public FileInputList getFiles( VariableSpace space ) {
    String[] required = new String[ fileName.length ];
    Arrays.fill( required, YES );
    boolean[] subDirs = new boolean[ fileName.length ]; // boolean arrays are defaulted to false.

    return FileInputList.createFileList( space, fileName, fileMask, excludeFileMask, required, subDirs );
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    // See if we get input...
    if ( input.length > 0 ) {
      remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR,
        BaseMessages.getString( PKG, "PropertyInputMeta.CheckResult.NoInputExpected" ), stepMeta ) );
    } else {
      remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_OK,
        BaseMessages.getString( PKG, "PropertyInputMeta.CheckResult.NoInput" ), stepMeta ) );
    }

    FileInputList fileInputList = getFiles( transMeta );

    if ( fileInputList == null || fileInputList.getFiles().isEmpty() ) {
      remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR,
        BaseMessages.getString( PKG, "PropertyInputMeta.CheckResult.NoFiles" ), stepMeta ) );
    } else {
      remarks.add( new CheckResult( CheckResultInterface.TYPE_RESULT_OK,
        BaseMessages.getString( PKG, "PropertyInputMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ),
        stepMeta ) );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
                                Trans trans ) {
    return new PropertyInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  public StepDataInterface getStepData() {
    return new PropertyInputData();
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
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      // In case the name of the file comes from previous steps, forget about this!
      //
      if ( !fileField ) {
        for ( int i = 0; i < fileName.length; i++ ) {
          FileObject fileObject =
            KettleVFS.getFileObject( space.environmentSubstitute( fileName[ i ] ), space );
          fileName[ i ] = resourceNamingInterface.nameResource( fileObject, space, Utils.isEmpty( fileMask[ i ] ) );
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }
}
