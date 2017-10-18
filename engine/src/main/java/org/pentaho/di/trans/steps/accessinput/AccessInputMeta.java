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

package org.pentaho.di.trans.steps.accessinput;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNone;
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
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;

public class AccessInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = AccessInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  /** Array of filenames */
  private String[] fileName;

  /** Wildcard or filemask (regular expression) */
  private String[] fileMask;

  /** Wildcard or filemask to exclude (regular expression) */
  private String[] excludeFileMask;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] fileRequired;

  /** Array of boolean values as string, indicating if we need to fetch sub folders. */
  private String[] includeSubFolders;

  /** Flag indicating that we should include the filename in the output */
  private boolean includeFilename;

  /** Flag indicating that we should include the tablename in the output */
  private boolean includeTablename;

  /** Flag indicating that we should reset RowNum for each file */
  private boolean resetRowNumber;

  /** The name of the field in the output containing the table name */
  private String tablenameField;

  /** The name of the field in the output containing the filename */
  private String filenameField;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The name of the table of the database */
  private String TableName;

  /** The maximum number or lines to read */
  private long rowLimit;

  /** The fields to import... */
  private AccessInputField[] inputFields;

  /** file name from previous fields **/
  private boolean filefield;

  private boolean isaddresult;

  private String dynamicFilenameField;

  private static final String YES = "Y";

  public static final String[] type_trim_code = { "none", "left", "right", "both" };

  /** Prefix that flags system tables */
  public static final String PREFIX_SYSTEM = "MSys";

  /** Additional fields **/
  private String shortFileFieldName;
  private String pathFieldName;
  private String hiddenFieldName;
  private String lastModificationTimeFieldName;
  private String uriNameFieldName;
  private String rootUriNameFieldName;
  private String extensionFieldName;
  private String sizeFieldName;

  public AccessInputMeta() {
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
  @Deprecated
  public void setIsHiddenField( String field ) {
    setHiddenField( field );
  }

  /**
   * @param field
   *          The hiddenFieldName to set.
   */
  public void setHiddenField( String field ) {
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
   * @return Returns the input fields.
   */
  public AccessInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( AccessInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the excludeFileMask.
   */
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
   * @return Returns the fileMask.
   */
  public String[] getFileMask() {
    return fileMask;
  }

  public String[] getFileRequired() {
    return fileRequired;
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
   * @param fileMask
   *          The fileMask to set.
   */
  public void setFileMask( String[] fileMask ) {
    this.fileMask = fileMask;
  }

  public void setFileRequired( String[] fileRequiredin ) {
    if ( fileRequiredin == null ) {
      this.fileRequired = new String[0];
      return;
    }

    this.fileRequired = new String[fileRequiredin.length];
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      this.fileRequired[i] = getRequiredFilesCode( fileRequiredin[i] );
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

  public String[] getIncludeSubFolders() {
    return includeSubFolders;
  }

  public void setIncludeSubFolders( String[] includeSubFoldersin ) {
    if ( includeSubFoldersin == null ) {
      this.includeSubFolders = new String[0];
      return;
    }

    this.includeSubFolders = new String[includeSubFoldersin.length];
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
   * @return Returns the dynamic filename field (from previous steps)
   */
  public String getDynamicFilenameField() {
    return dynamicFilenameField;
  }

  /**
   * @param dynamicFilenameField
   *          The dynamic filename field to set.
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
  @Deprecated
  public boolean includeFilename() {
    return isIncludeFilename();
  }

  /**
   * @return Returns the includeFilename.
   */
  public boolean isIncludeFilename() {
    return includeFilename;
  }

  /**
   * @return Returns the includeTablename.
   */
  @Deprecated
  public boolean includeTablename() {
    return isIncludeTablename();
  }

  /**
   * @return Returns the includeTablename.
   */
  public boolean isIncludeTablename() {
    return includeTablename;
  }

  /**
   * @param includeFilename
   *          The includeFilename to set.
   */
  public void setIncludeFilename( boolean includeFilename ) {
    this.includeFilename = includeFilename;
  }

  /**
   * @param includeTablename
   *          The includeTablename to set.
   */
  public void setIncludeTablename( boolean includeTablename ) {
    this.includeTablename = includeTablename;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  @Deprecated
  public boolean includeRowNumber() {
    return isIncludeRowNumber();
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean isIncludeRowNumber() {
    return includeRowNumber;
  }

  /**
   * @return Returns the File field.
   */
  public boolean isFileField() {
    return filefield;
  }

  /**
   * @param filefield
   *          The filefield to set.
   */
  public void setFileField( boolean filefield ) {
    this.filefield = filefield;
  }

  /**
   * @return Returns the resetRowNumber.
   */
  @Deprecated
  public boolean resetRowNumber() {
    return isResetRowNumber();
  }

  /**
   * @return Returns the resetRowNumber.
   */
  public boolean isResetRowNumber() {
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
   * @param isaddresult
   *          The isaddresult to set.
   */
  public void setAddResultFile( boolean isaddresult ) {
    this.isaddresult = isaddresult;
  }

  /**
   * @return Returns isaddresult.
   */
  public boolean isAddResultFile() {
    return isaddresult;
  }

  /**
   * @param resetRowNumber
   *          The resetRowNumber to set.
   */
  public void setResetRowNumber( boolean resetRowNumber ) {
    this.resetRowNumber = resetRowNumber;
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
   * @return Returns the tablenameField.
   */
  @Deprecated
  public String gettablenameField() {
    return getTablenameField();
  }

  /**
   * @return Returns the tablenameField.
   */
  public String getTablenameField() {
    return tablenameField;
  }

  /**
   * @return Returns the TableName.
   */
  public String getTableName() {
    return TableName;
  }

  /**
   * @param rowNumberField
   *          The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  /**
   * @param tablenameField
   *          The tablenameField to set.
   */
  public void setTablenameField( String tablenameField ) {
    this.tablenameField = tablenameField;
  }

  /**
   * @param TableName
   *          The table name to set.
   */
  public void setTableName( String TableName ) {
    this.TableName = TableName;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public Object clone() {
    AccessInputMeta retval = (AccessInputMeta) super.clone();
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
        retval.inputFields[i] = (AccessInputField) inputFields[i].clone();
      }
    }

    return retval;
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 500 );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include", includeFilename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_field", filenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "tablename", includeTablename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "filename_Field", dynamicFilenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "tablename_field", tablenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "isaddresult", isaddresult ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "filefield", filefield ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "resetrownumber", resetRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "table_name", TableName ) );
    retval.append( "    <file>" ).append( Const.CR );
    for ( int i = 0; i < fileName.length; i++ ) {
      retval.append( "      " ).append( XMLHandler.addTagValue( "name", fileName[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "filemask", fileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "exclude_filemask", excludeFileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "file_required", fileRequired[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", includeSubFolders[i] ) );
    }
    retval.append( "    </file>" ).append( Const.CR );

    /*
     * Describe the fields to read
     */
    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", inputFields[i].getName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "attribut", inputFields[i].getColumn() ) );
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
      includeFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include" ) );
      filenameField = XMLHandler.getTagValue( stepnode, "include_field" );
      includeTablename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "tablename" ) );
      tablenameField = XMLHandler.getTagValue( stepnode, "tablename_field" );
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );

      String addresult = XMLHandler.getTagValue( stepnode, "isaddresult" );
      if ( Utils.isEmpty( addresult ) ) {
        isaddresult = true;
      } else {
        isaddresult = "Y".equalsIgnoreCase( addresult );
      }

      filefield = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "filefield" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      TableName = XMLHandler.getTagValue( stepnode, "table_name" );
      dynamicFilenameField = XMLHandler.getTagValue( stepnode, "filename_Field" );
      resetRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "resetrownumber" ) );

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
        inputFields[i] = new AccessInputField();

        inputFields[i].setName( XMLHandler.getTagValue( fnode, "name" ) );
        inputFields[i].setColumn( XMLHandler.getTagValue( fnode, "attribut" ) );
        inputFields[i].setType( ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) ) );
        inputFields[i].setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
        inputFields[i].setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
        String srepeat = XMLHandler.getTagValue( fnode, "repeat" );
        inputFields[i].setTrimType( getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );

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

  public void allocate( int nrfiles, int nrfields ) {
    allocateFiles( nrfiles );
    allocateFields( nrfields );
  }

  public void allocateFiles( int nrfiles ) {
    fileName = new String[nrfiles];
    fileMask = new String[nrfiles];
    excludeFileMask = new String[nrfiles];
    fileRequired = new String[nrfiles];
    includeSubFolders = new String[nrfiles];
  }

  public void allocateFields( int nrfields ) {
    inputFields = new AccessInputField[nrfields];
  }

  @Override
  public void setDefault() {
    shortFileFieldName = null;
    pathFieldName = null;
    hiddenFieldName = null;
    lastModificationTimeFieldName = null;
    uriNameFieldName = null;
    rootUriNameFieldName = null;
    extensionFieldName = null;
    sizeFieldName = null;

    isaddresult = true;
    filefield = false;
    includeFilename = false;
    filenameField = "";
    includeTablename = false;
    tablenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    TableName = "";
    dynamicFilenameField = "";

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
      inputFields[i] = new AccessInputField( "field" + ( i + 1 ) );
    }

    rowLimit = 0;
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

    int i;
    int nr = inputFields == null ? 0 : inputFields.length;
    for ( i = 0; i < nr; i++ ) {
      AccessInputField field = inputFields[i];

      int type = field.getType();
      if ( type == ValueMetaInterface.TYPE_NONE ) {
        type = ValueMetaInterface.TYPE_STRING;
      }
      ValueMetaInterface v;
      try {
        v = ValueMetaFactory.createValueMeta( space.environmentSubstitute( field.getName() ), type );
      } catch ( KettlePluginException e ) {
        v = new ValueMetaNone( space.environmentSubstitute( field.getName() ) );
      }
      v.setLength( field.getLength() );
      v.setPrecision( field.getPrecision() );
      v.setOrigin( name );
      v.setConversionMask( field.getFormat() );
      v.setDecimalSymbol( field.getDecimalSymbol() );
      v.setGroupingSymbol( field.getGroupSymbol() );
      v.setCurrencySymbol( field.getCurrencySymbol() );
      v.setTrimType( field.getTrimType() );
      r.addValueMeta( v );

    }

    if ( includeFilename ) {
      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( filenameField ) );
      v.setLength( 250 );
      v.setPrecision( -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }

    if ( includeTablename ) {

      ValueMetaInterface v = new ValueMetaString( space.environmentSubstitute( tablenameField ) );
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
      ValueMetaInterface v =
        new ValueMetaString( space.environmentSubstitute( getExtensionField() ) );
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
    if ( getSizeField() != null && getSizeField().length() > 0 ) {
      ValueMetaInterface v = new ValueMetaInteger( space.environmentSubstitute( getSizeField() ) );
      v.setOrigin( name );
      v.setLength( 9 );
      r.addValueMeta( v );
    }
    if ( isHiddenField() != null && isHiddenField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaBoolean( space.environmentSubstitute( isHiddenField() ) );
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
      ValueMetaInterface v =
        new ValueMetaString( space.environmentSubstitute( getRootUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      r.addValueMeta( v );
    }
  }

  public static final int getTrimTypeByCode( String tt ) {
    if ( tt != null ) {
      for ( int i = 0; i < type_trim_code.length; i++ ) {
        if ( type_trim_code[i].equalsIgnoreCase( tt ) ) {
          return i;
        }
      }
    }
    return 0;
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {

    try {
      includeFilename = rep.getStepAttributeBoolean( id_step, "include" );
      filenameField = rep.getStepAttributeString( id_step, "include_field" );
      TableName = rep.getStepAttributeString( id_step, "table_name" );
      includeTablename = rep.getStepAttributeBoolean( id_step, "tablename" );
      dynamicFilenameField = rep.getStepAttributeString( id_step, "filename_Field" );
      tablenameField = rep.getStepAttributeString( id_step, "tablename_field" );
      includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );

      String addresult = rep.getStepAttributeString( id_step, "isaddresult" );
      if ( Utils.isEmpty( addresult ) ) {
        isaddresult = true;
      } else {
        isaddresult = rep.getStepAttributeBoolean( id_step, "isaddresult" );
      }

      filefield = rep.getStepAttributeBoolean( id_step, "filefield" );
      rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );
      resetRowNumber = rep.getStepAttributeBoolean( id_step, "reset_rownumber" );
      rowLimit = rep.getStepAttributeInteger( id_step, "limit" );
      int nrFiles = rep.countNrStepAttributes( id_step, "file_name" );
      int nrFields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrFiles, nrFields );

      for ( int i = 0; i < nrFiles; i++ ) {
        fileName[i] = rep.getStepAttributeString( id_step, i, "file_name" );
        fileMask[i] = rep.getStepAttributeString( id_step, i, "file_mask" );
        excludeFileMask[i] = rep.getStepAttributeString( id_step, i, "exclude_file_mask" );
        fileRequired[i] = rep.getStepAttributeString( id_step, i, "file_required" );
        if ( !YES.equalsIgnoreCase( fileRequired[i] ) ) {
          fileRequired[i] = RequiredFilesCode[0];
        }
        includeSubFolders[i] = rep.getStepAttributeString( id_step, i, "include_subfolders" );
        if ( !YES.equalsIgnoreCase( includeSubFolders[i] ) ) {
          includeSubFolders[i] = RequiredFilesCode[0];
        }
      }

      for ( int i = 0; i < nrFields; i++ ) {
        AccessInputField field = new AccessInputField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setColumn( rep.getStepAttributeString( id_step, i, "field_attribut" ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        field.setTrimType( AccessInputField.getTrimTypeByCode( rep.getStepAttributeString(
          id_step, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );

        inputFields[i] = field;
      }
      shortFileFieldName = rep.getStepAttributeString( id_step, "shortFileFieldName" );
      pathFieldName = rep.getStepAttributeString( id_step, "pathFieldName" );
      hiddenFieldName = rep.getStepAttributeString( id_step, "hiddenFieldName" );
      lastModificationTimeFieldName = rep.getStepAttributeString( id_step, "lastModificationTimeFieldName" );
      uriNameFieldName = rep.getStepAttributeString( id_step, "uriNameFieldName" );
      rootUriNameFieldName = rep.getStepAttributeString( id_step, "rootUriNameFieldName" );
      extensionFieldName = rep.getStepAttributeString( id_step, "extensionFieldName" );
      sizeFieldName = rep.getStepAttributeString( id_step, "sizeFieldName" );
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "AccessInputMeta.Exception.ErrorReadingRepository" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "include", includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, "include_field", filenameField );
      rep.saveStepAttribute( id_transformation, id_step, "tablename", includeTablename );
      rep.saveStepAttribute( id_transformation, id_step, "tablename_field", tablenameField );
      rep.saveStepAttribute( id_transformation, id_step, "rownum", includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "isaddresult", isaddresult );
      rep.saveStepAttribute( id_transformation, id_step, "filefield", filefield );
      rep.saveStepAttribute( id_transformation, id_step, "filename_Field", dynamicFilenameField );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", rowNumberField );
      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );
      rep.saveStepAttribute( id_transformation, id_step, "table_name", TableName );
      rep.saveStepAttribute( id_transformation, id_step, "reset_rownumber", resetRowNumber );

      for ( int i = 0; i < fileName.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "file_name", fileName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_mask", fileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "exclude_file_mask", excludeFileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_required", fileRequired[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "include_subfolders", includeSubFolders[i] );
      }

      for ( int i = 0; i < inputFields.length; i++ ) {
        AccessInputField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_attribut", field.getColumn() );
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
      rep.saveStepAttribute( id_transformation, id_step, "shortFileFieldName", shortFileFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "pathFieldName", pathFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "hiddenFieldName", hiddenFieldName );
      rep.saveStepAttribute(
        id_transformation, id_step, "lastModificationTimeFieldName", lastModificationTimeFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "uriNameFieldName", uriNameFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "rootUriNameFieldName", rootUriNameFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "extensionFieldName", extensionFieldName );
      rep.saveStepAttribute( id_transformation, id_step, "sizeFieldName", sizeFieldName );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "AccessInputMeta.Exception.ErrorSavingToRepository", "" + id_step ), e );
    }
  }

  public FileInputList getFiles( VariableSpace space ) {
    return FileInputList.createFileList(
      space, fileName, fileMask, excludeFileMask, fileRequired, includeSubFolderBoolean() );
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
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // See if we get input...
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "AccessInputMeta.CheckResult.NoInputExpected" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "AccessInputMeta.CheckResult.NoInput" ), stepMeta );
      remarks.add( cr );
    }

    FileInputList fileInputList = getFiles( transMeta );
    // String files[] = getFiles();
    if ( fileInputList == null || fileInputList.getFiles().size() == 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "AccessInputMeta.CheckResult.NoFiles" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "AccessInputMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ), stepMeta );
      remarks.add( cr );
    }

    // Check table
    if ( Utils.isEmpty( getTableName() ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "AccessInputMeta.CheckResult.NoFiles" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "AccessInputMeta.CheckResult.FilesOk", "" + fileInputList.getFiles().size() ), stepMeta );
      remarks.add( cr );
    }

  }

  public static String getFilename( FileObject fileObject ) {
    FileName fileName = fileObject.getName();
    String root = fileName.getRootURI();
    if ( !root.startsWith( "file:" ) ) {
      return fileName.getURI();
    }
    if ( root.endsWith( ":/" ) ) {
      root = root.substring( 8, 10 );
    } else {
      root = root.substring( 7, root.length() - 1 );
    }
    String fileString = root + fileName.getPath();
    if ( !"/".equals( Const.FILE_SEPARATOR ) ) {
      fileString = Const.replace( fileString, "/", Const.FILE_SEPARATOR );
    }
    return fileString;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
    Trans trans ) {
    return new AccessInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new AccessInputData();
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
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
      // Replace the filename ONLY (folder or filename)
      //
      for ( int i = 0; i < fileName.length; i++ ) {
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( fileName[i] ), space );
        fileName[i] = resourceNamingInterface.nameResource( fileObject, space, Utils.isEmpty( fileMask[i] ) );
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Returns kettle type from Microsoft Access database
   *
   * @param : MS Access column
   * @return valuemeta
   */
  public static ValueMetaInterface getValueMeta( Column c ) {
    // get value
    ValueMetaAndData vmd = getValueMetaAndData( c, null, null );
    if ( vmd != null ) {
      // returns meta
      return vmd.getValueMeta();
    }
    return null;
  }

  /**
   * Returns kettle type from Microsoft Access database also convert data to prepare kettle value
   *
   * @param : MS Access column
   * @param : destination field name
   * @param : MS Access column value
   * @return valuemeta and data
   */
  public static ValueMetaAndData getValueMetaAndData( Column c, String name, Object data ) {

    ValueMetaAndData valueMetaData = new ValueMetaAndData();
    // get data
    Object o = data;

    // Get column type
    DataType type = c.getType();

    int sourceValueType = ValueMetaInterface.TYPE_STRING;

    // Find corresponding Kettle type for each MS Access type
    // We have to take of Meta AND data
    switch ( type ) {
      case BINARY:
        sourceValueType = ValueMetaInterface.TYPE_BINARY;
        break;
      case BOOLEAN:
        sourceValueType = ValueMetaInterface.TYPE_BOOLEAN;
        if ( o != null ) {
          o = Boolean.valueOf( o.toString() );
        }

        break;
      case DOUBLE:
        sourceValueType = ValueMetaInterface.TYPE_NUMBER;

        break;
      case FLOAT:
        sourceValueType = ValueMetaInterface.TYPE_BIGNUMBER;
        if ( o != null ) {
          o = new BigDecimal( Float.toString( (Float) o ) );
        }

        break;
      case INT:
        sourceValueType = ValueMetaInterface.TYPE_NUMBER;
        if ( o != null ) {
          o = Double.parseDouble( o.toString() );
        }
        break;
      case BYTE:
        sourceValueType = ValueMetaInterface.TYPE_NUMBER;
        if ( o != null ) {
          o = Double.parseDouble( o.toString() );
        }
        break;
      case LONG:
        sourceValueType = ValueMetaInterface.TYPE_INTEGER;
        if ( o != null ) {
          Integer i = (Integer) o;
          o = i.longValue();
        }

        break;
      case MEMO:
        // Should be considered as String

        break;
      case MONEY:
        sourceValueType = ValueMetaInterface.TYPE_BIGNUMBER;

        break;
      case NUMERIC:
        sourceValueType = ValueMetaInterface.TYPE_BIGNUMBER;

        break;
      case SHORT_DATE_TIME:
        sourceValueType = ValueMetaInterface.TYPE_DATE;

        break;
      default:
        // Default it's string
        if ( o != null ) {
          o = o.toString();
        }
        break;
    }

    ValueMetaInterface sourceValueMeta;
    try {
      sourceValueMeta = ValueMetaFactory.createValueMeta( name == null ? c.getName() : name, sourceValueType );
    } catch ( KettlePluginException e ) {
      sourceValueMeta = new ValueMetaNone( name == null ? c.getName() : name );
    }
    sourceValueMeta.setLength( c.getLength(), c.getPrecision() );

    // set value meta data and return it
    valueMetaData.setValueMeta( sourceValueMeta );
    if ( o != null ) {
      valueMetaData.setValueData( o );
    }

    return valueMetaData;
  }

  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new AccessInputMetaInjection( this );
  }
}
