// CHECKSTYLE:FileLength:OFF
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

package org.pentaho.di.trans.steps.textfileinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
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
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
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

import com.google.common.annotations.VisibleForTesting;

/**
 * @deprecated replaced by implementation in the ...steps.fileinput.text package
 */
@Deprecated
public class TextFileInputMeta extends BaseStepMeta implements StepMetaInterface, InputFileMetaInterface {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String[] RequiredFilesDesc = new String[] {
    BaseMessages.getString( PKG, "System.Combo.No" ), BaseMessages.getString( PKG, "System.Combo.Yes" ) };
  public static final String[] RequiredFilesCode = new String[] { "N", "Y" };

  private static final String NO = "N";

  private static final String YES = "Y";

  private static final String STRING_BASE64_PREFIX = "Base64: ";

  public static final int FILE_FORMAT_DOS = 0;
  public static final int FILE_FORMAT_UNIX = 1;
  public static final int FILE_FORMAT_MIXED = 2;

  public static final int FILE_TYPE_CSV = 0;
  public static final int FILE_TYPE_FIXED = 1;

  /** Array of filenames */
  private String[] fileName;

  /** Wildcard or filemask (regular expression) */
  private String[] fileMask;

  /** Wildcard or filemask to exclude (regular expression) */
  private String[] excludeFileMask;

  /** Array of boolean values as string, indicating if a file is required. */
  private String[] fileRequired;

  /** Type of file: CSV or fixed */
  private String fileType;

  /** String used to separated field (;) */
  private String separator;

  /** String used to enclose separated fields (") */
  private String enclosure;

  /** Escape character used to escape the enclosure String (\) */
  private String escapeCharacter;

  /** Switch to allow breaks (CR/LF) in Enclosures */
  private boolean breakInEnclosureAllowed;

  /** Flag indicating that the file contains one header line that should be skipped. */
  private boolean header;

  /** The number of header lines, defaults to 1 */
  private int nrHeaderLines;

  /** Flag indicating that the file contains one footer line that should be skipped. */
  private boolean footer;

  /** The number of footer lines, defaults to 1 */
  private int nrFooterLines;

  /** Flag indicating that a single line is wrapped onto one or more lines in the text file. */
  private boolean lineWrapped;

  /** The number of times the line wrapped */
  private int nrWraps;

  /** Flag indicating that the text-file has a paged layout. */
  private boolean layoutPaged;

  /** The number of lines in the document header */
  private int nrLinesDocHeader;

  /** The number of lines to read per page */
  private int nrLinesPerPage;

  /** Type of compression being used */
  private String fileCompression;

  /** Flag indicating that we should skip all empty lines */
  private boolean noEmptyLines;

  /** Flag indicating that we should include the filename in the output */
  private boolean includeFilename;

  /** The name of the field in the output containing the filename */
  private String filenameField;

  /** Flag indicating that a row number field should be included in the output */
  private boolean includeRowNumber;

  /** Flag indicating row number is per file */
  private boolean rowNumberByFile;

  /** The name of the field in the output containing the row number */
  private String rowNumberField;

  /** The file format: DOS or UNIX or mixed */
  private String fileFormat;

  /** The maximum number or lines to read */
  private long rowLimit;

  /** The fields to import... */
  private TextFileInputField[] inputFields;

  /** Array of boolean values as string, indicating if we need to fetch sub folders. */
  private String[] includeSubFolders;

  /** The filters to use... */
  private TextFileFilter[] filter;

  /** The encoding to use for reading: null or empty string means system default encoding */
  private String encoding;

  /** Ignore error : turn into warnings */
  private boolean errorIgnored;

  /** The name of the field that will contain the number of errors in the row */
  private String errorCountField;

  /** The name of the field that will contain the names of the fields that generated errors, separated by , */
  private String errorFieldsField;

  /** The name of the field that will contain the error texts, separated by CR */
  private String errorTextField;

  /** The directory that will contain warning files */
  private String warningFilesDestinationDirectory;

  /** The extension of warning files */
  private String warningFilesExtension;

  /** The directory that will contain error files */
  private String errorFilesDestinationDirectory;

  /** The extension of error files */
  private String errorFilesExtension;

  /** The directory that will contain line number files */
  private String lineNumberFilesDestinationDirectory;

  /** The extension of line number files */
  private String lineNumberFilesExtension;

  /** Indicate whether or not we want to date fields strictly according to the format or lenient */
  private boolean dateFormatLenient;

  /** Specifies the Locale of the Date format, null means the default */
  private Locale dateFormatLocale;

  /** If error line are skipped, you can replay without introducing doubles. */
  private boolean errorLineSkipped;

  /** Are we accepting filenames in input rows? */
  private boolean acceptingFilenames;

  /** If receiving input rows, should we pass through existing fields? */
  private boolean passingThruFields;

  /** The field in which the filename is placed */
  private String acceptingField;

  /** The stepname to accept filenames from */
  private String acceptingStepName;

  /** The step to accept filenames from */
  private StepMeta acceptingStep;

  /** The add filenames to result filenames flag */
  private boolean isaddresult;

  /** Additional fields **/
  private String shortFileFieldName;
  private String pathFieldName;
  private String hiddenFieldName;
  private String lastModificationTimeFieldName;
  private String uriNameFieldName;
  private String rootUriNameFieldName;
  private String extensionFieldName;
  private String sizeFieldName;

  private boolean skipBadFiles;
  private String fileErrorField;
  private String fileErrorMessageField;

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
   *
   * @return If should continue processing after failing to open a file
   */
  public boolean isSkipBadFiles() {
    return skipBadFiles;
  }

  /**
   *
   * @param value
   *          If should continue processing after failing to open a file
   */
  public void setSkipBadFiles( boolean value ) {
    skipBadFiles = value;
  }

  public String getFileErrorField() {
    return fileErrorField;
  }

  public void setFileErrorField( String field ) {
    fileErrorField = field;
  }

  public String getFileErrorMessageField() {
    return fileErrorMessageField;
  }

  public void setFileErrorMessageField( String field ) {
    fileErrorMessageField = field;
  }

  /**
   * @return Returns the encoding.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding
   *          The encoding to set.
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  public TextFileInputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the input fields.
   */
  @Override
  public TextFileInputField[] getInputFields() {
    return inputFields;
  }

  /**
   * @param inputFields
   *          The input fields to set.
   */
  public void setInputFields( TextFileInputField[] inputFields ) {
    this.inputFields = inputFields;
  }

  /**
   * @return Returns the enclosure.
   */
  @Override
  public String getEnclosure() {
    return enclosure;
  }

  /**
   * @param enclosure
   *          The enclosure to set.
   */
  public void setEnclosure( String enclosure ) {
    this.enclosure = enclosure;
  }

  /**
   * @return Returns the breakInEnclosureAllowed.
   */
  public boolean isBreakInEnclosureAllowed() {
    return breakInEnclosureAllowed;
  }

  /**
   * @param breakInEnclosureAllowed
   *          The breakInEnclosureAllowed to set.
   */
  public void setBreakInEnclosureAllowed( boolean breakInEnclosureAllowed ) {
    this.breakInEnclosureAllowed = breakInEnclosureAllowed;
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
   * @return Returns the fileFormat.
   */
  public String getFileFormat() {
    return fileFormat;
  }

  /**
   * @param fileFormat
   *          The fileFormat to set.
   */
  public void setFileFormat( String fileFormat ) {
    this.fileFormat = fileFormat;
  }

  /**
   * @return Returns the fileMask.
   */
  public String[] getFileMask() {
    return fileMask;
  }

  /**
   * @return Returns the fileRequired.
   */
  public String[] getFileRequired() {
    return fileRequired;
  }

  /**
   * @param fileMask
   *          The fileMask to set.
   */
  public void setFileMask( String[] fileMask ) {
    this.fileMask = fileMask;
  }

  /**
   * @param fileRequired
   *          The fileRequired to set.
   */
  public void setFileRequired( String[] fileRequiredin ) {
    for ( int i = 0; i < fileRequiredin.length; i++ ) {
      this.fileRequired[i] = getRequiredFilesCode( fileRequiredin[i] );
    }
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
   * @return Returns the fileType.
   */
  @Override
  public String getFileType() {
    return fileType;
  }

  /**
   * @param fileType
   *          The fileType to set.
   */
  public void setFileType( String fileType ) {
    this.fileType = fileType;
  }

  /**
   * @return The array of filters for the metadata of this text file input step.
   */
  public TextFileFilter[] getFilter() {
    return filter;
  }

  /**
   * @param filter
   *          The array of filters to use
   */
  public void setFilter( TextFileFilter[] filter ) {
    this.filter = filter;
  }

  /**
   * @return Returns the footer.
   */
  public boolean hasFooter() {
    return footer;
  }

  /**
   * @param footer
   *          The footer to set.
   */
  public void setFooter( boolean footer ) {
    this.footer = footer;
  }

  /**
   * @return Returns the header.
   */
  @Override
  public boolean hasHeader() {
    return header;
  }

  /**
   * @param header
   *          The header to set.
   */
  public void setHeader( boolean header ) {
    this.header = header;
  }

  /**
   * @return Returns the includeFilename.
   */
  @Override
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
  @Override
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
   * true if row number reset for each file
   *
   * @return rowNumberByFile
   */
  public boolean isRowNumberByFile() {
    return rowNumberByFile;
  }

  /**
   * @param rowNumberByFile
   *          True if row number field is reset for each file
   */
  public void setRowNumberByFile( boolean rowNumberByFile ) {
    this.rowNumberByFile = rowNumberByFile;
  }

  /**
   * @return Returns the noEmptyLines.
   */
  public boolean noEmptyLines() {
    return noEmptyLines;
  }

  /**
   * @param noEmptyLines
   *          The noEmptyLines to set.
   */
  public void setNoEmptyLines( boolean noEmptyLines ) {
    this.noEmptyLines = noEmptyLines;
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
   * @return Returns the separator.
   */
  @Override
  public String getSeparator() {
    return separator;
  }

  /**
   * @param separator
   *          The separator to set.
   */
  public void setSeparator( String separator ) {
    this.separator = separator;
  }

  /**
   * @return Returns the type of compression used
   */
  public String getFileCompression() {
    return fileCompression;
  }

  /**
   * @param fileCompression
   *          Sets the compression type
   */
  public void setFileCompression( String fileCompression ) {
    this.fileCompression = fileCompression;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      acceptingFilenames = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "accept_filenames" ) );
      passingThruFields = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "passing_through_fields" ) );
      acceptingField = XMLHandler.getTagValue( stepnode, "accept_field" );
      acceptingStepName = XMLHandler.getTagValue( stepnode, "accept_stepname" );

      separator = XMLHandler.getTagValue( stepnode, "separator" );
      enclosure = XMLHandler.getTagValue( stepnode, "enclosure" );
      breakInEnclosureAllowed = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "enclosure_breaks" ) );
      escapeCharacter = XMLHandler.getTagValue( stepnode, "escapechar" );
      header = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "header" ) );
      nrHeaderLines = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_headerlines" ), 1 );
      footer = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "footer" ) );
      nrFooterLines = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_footerlines" ), 1 );
      lineWrapped = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "line_wrapped" ) );
      nrWraps = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_wraps" ), 1 );
      layoutPaged = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "layout_paged" ) );
      nrLinesPerPage = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_lines_per_page" ), 1 );
      nrLinesDocHeader = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_lines_doc_header" ), 1 );
      String addToResult = XMLHandler.getTagValue( stepnode, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResult ) ) {
        isaddresult = true;
      } else {
        isaddresult = "Y".equalsIgnoreCase( addToResult );
      }

      String nempty = XMLHandler.getTagValue( stepnode, "noempty" );
      noEmptyLines = YES.equalsIgnoreCase( nempty ) || nempty == null;
      includeFilename = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include" ) );
      filenameField = XMLHandler.getTagValue( stepnode, "include_field" );
      includeRowNumber = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      rowNumberByFile = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownumByFile" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      fileFormat = XMLHandler.getTagValue( stepnode, "format" );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );

      Node filenode = XMLHandler.getSubNode( stepnode, "file" );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      Node filtersNode = XMLHandler.getSubNode( stepnode, "filters" );
      int nrfiles = XMLHandler.countNodes( filenode, "name" );
      int nrfields = XMLHandler.countNodes( fields, "field" );
      int nrfilters = XMLHandler.countNodes( filtersNode, "filter" );

      allocate( nrfiles, nrfields, nrfilters );

      for ( int i = 0; i < nrfiles; i++ ) {
        Node filenamenode = XMLHandler.getSubNodeByNr( filenode, "name", i );
        Node filemasknode = XMLHandler.getSubNodeByNr( filenode, "filemask", i );
        Node excludefilemasknode = XMLHandler.getSubNodeByNr( filenode, "exclude_filemask", i );
        Node fileRequirednode = XMLHandler.getSubNodeByNr( filenode, "file_required", i );
        Node includeSubFoldersnode = XMLHandler.getSubNodeByNr( filenode, "include_subfolders", i );
        fileName[i] = loadSource( filenode, filenamenode, i, metaStore );
        fileMask[i] = XMLHandler.getNodeValue( filemasknode );
        excludeFileMask[i] = XMLHandler.getNodeValue( excludefilemasknode );
        fileRequired[i] = XMLHandler.getNodeValue( fileRequirednode );
        includeSubFolders[i] = XMLHandler.getNodeValue( includeSubFoldersnode );
      }

      fileType = XMLHandler.getTagValue( stepnode, "file", "type" );
      fileCompression = XMLHandler.getTagValue( stepnode, "file", "compression" );
      if ( fileCompression == null ) {
        fileCompression = "None";
        if ( YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "zipped" ) ) ) {
          fileCompression = "Zip";
        }
      }

      // Backward compatibility : just one filter
      if ( XMLHandler.getTagValue( stepnode, "filter" ) != null ) {
        filter = new TextFileFilter[1];
        filter[0] = new TextFileFilter();

        filter[0].setFilterPosition( Const.toInt( XMLHandler.getTagValue( stepnode, "filter_position" ), -1 ) );
        filter[0].setFilterString( XMLHandler.getTagValue( stepnode, "filter_string" ) );
        filter[0].setFilterLastLine( YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "filter_is_last_line" ) ) );
        filter[0].setFilterPositive( YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "filter_is_positive" ) ) );
      } else {
        for ( int i = 0; i < nrfilters; i++ ) {
          Node fnode = XMLHandler.getSubNodeByNr( filtersNode, "filter", i );
          filter[i] = new TextFileFilter();

          filter[i].setFilterPosition( Const.toInt( XMLHandler.getTagValue( fnode, "filter_position" ), -1 ) );

          String filterString = XMLHandler.getTagValue( fnode, "filter_string" );
          if ( filterString != null && filterString.startsWith( STRING_BASE64_PREFIX ) ) {
            filter[i].setFilterString( new String( Base64.decodeBase64( filterString.substring(
                STRING_BASE64_PREFIX.length() ).getBytes() ) ) );
          } else {
            filter[i].setFilterString( filterString );
          }

          filter[i].setFilterLastLine( YES.equalsIgnoreCase( XMLHandler.getTagValue( fnode, "filter_is_last_line" ) ) );
          filter[i].setFilterPositive( YES.equalsIgnoreCase( XMLHandler.getTagValue( fnode, "filter_is_positive" ) ) );
        }
      }

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        TextFileInputField field = new TextFileInputField();

        field.setName( XMLHandler.getTagValue( fnode, "name" ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) ) );
        field.setFormat( XMLHandler.getTagValue( fnode, "format" ) );
        field.setCurrencySymbol( XMLHandler.getTagValue( fnode, "currency" ) );
        field.setDecimalSymbol( XMLHandler.getTagValue( fnode, "decimal" ) );
        field.setGroupSymbol( XMLHandler.getTagValue( fnode, "group" ) );
        field.setNullString( XMLHandler.getTagValue( fnode, "nullif" ) );
        field.setIfNullValue( XMLHandler.getTagValue( fnode, "ifnull" ) );
        field.setPosition( Const.toInt( XMLHandler.getTagValue( fnode, "position" ), -1 ) );
        field.setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
        field.setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
        field.setTrimType( ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );
        field.setRepeated( YES.equalsIgnoreCase( XMLHandler.getTagValue( fnode, "repeat" ) ) );

        inputFields[i] = field;
      }

      // Is there a limit on the number of rows we process?
      rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, "limit" ), 0L );

      errorIgnored = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "error_ignored" ) );
      skipBadFiles = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "skip_bad_files" ) );
      fileErrorField = XMLHandler.getTagValue( stepnode, "file_error_field" );
      fileErrorMessageField = XMLHandler.getTagValue( stepnode, "file_error_message_field" );
      errorLineSkipped = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "error_line_skipped" ) );
      errorCountField = XMLHandler.getTagValue( stepnode, "error_count_field" );
      errorFieldsField = XMLHandler.getTagValue( stepnode, "error_fields_field" );
      errorTextField = XMLHandler.getTagValue( stepnode, "error_text_field" );
      warningFilesDestinationDirectory = XMLHandler.getTagValue( stepnode, "bad_line_files_destination_directory" );
      warningFilesExtension = XMLHandler.getTagValue( stepnode, "bad_line_files_extension" );
      errorFilesDestinationDirectory = XMLHandler.getTagValue( stepnode, "error_line_files_destination_directory" );
      errorFilesExtension = XMLHandler.getTagValue( stepnode, "error_line_files_extension" );
      lineNumberFilesDestinationDirectory =
          XMLHandler.getTagValue( stepnode, "line_number_files_destination_directory" );
      lineNumberFilesExtension = XMLHandler.getTagValue( stepnode, "line_number_files_extension" );
      // Backward compatible

      dateFormatLenient = !NO.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "date_format_lenient" ) );
      String dateLocale = XMLHandler.getTagValue( stepnode, "date_format_locale" );
      if ( dateLocale != null ) {
        dateFormatLocale = EnvUtil.createLocale( dateLocale );
      } else {
        dateFormatLocale = Locale.getDefault();
      }

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

  @Override
  public Object clone() {
    TextFileInputMeta retval = (TextFileInputMeta) super.clone();

    int nrFiles = fileName.length;
    int nrfields = inputFields.length;
    int nrfilters = filter.length;

    retval.allocate( nrFiles, nrfields, nrfilters );

    System.arraycopy( fileName, 0, retval.fileName, 0, nrFiles );
    System.arraycopy( fileMask, 0, retval.fileMask, 0, nrFiles );
    System.arraycopy( excludeFileMask, 0, retval.excludeFileMask, 0, nrFiles );
    System.arraycopy( fileRequired, 0, retval.fileRequired, 0, nrFiles );
    System.arraycopy( includeSubFolders, 0, retval.includeSubFolders, 0, nrFiles );

    for ( int i = 0; i < nrfields; i++ ) {
      retval.inputFields[i] = (TextFileInputField) inputFields[i].clone();
    }

    for ( int i = 0; i < nrfilters; i++ ) {
      retval.filter[i] = (TextFileFilter) filter[i].clone();
    }

    retval.dateFormatLocale = (Locale) dateFormatLocale.clone();
    retval.fileCompression = fileCompression;

    return retval;
  }

  public void allocate( int nrfiles, int nrfields, int nrfilters ) {
    allocateFiles( nrfiles );

    inputFields = new TextFileInputField[nrfields];
    filter = new TextFileFilter[nrfilters];
  }

  public void allocateFiles( int nrFiles ) {
    fileName = new String[nrFiles];
    fileMask = new String[nrFiles];
    excludeFileMask = new String[nrFiles];
    fileRequired = new String[nrFiles];
    includeSubFolders = new String[nrFiles];
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
    separator = ";";
    enclosure = "\"";
    breakInEnclosureAllowed = false;
    header = true;
    nrHeaderLines = 1;
    footer = false;
    nrFooterLines = 1;
    lineWrapped = false;
    nrWraps = 1;
    layoutPaged = false;
    nrLinesPerPage = 80;
    nrLinesDocHeader = 0;
    fileCompression = "None";
    noEmptyLines = true;
    fileFormat = "DOS";
    fileType = "CSV";
    includeFilename = false;
    filenameField = "";
    includeRowNumber = false;
    rowNumberField = "";
    errorIgnored = false;
    skipBadFiles = false;
    errorLineSkipped = false;
    warningFilesDestinationDirectory = null;
    warningFilesExtension = "warning";
    errorFilesDestinationDirectory = null;
    errorFilesExtension = "error";
    lineNumberFilesDestinationDirectory = null;
    lineNumberFilesExtension = "line";
    dateFormatLenient = true;
    rowNumberByFile = false;

    int nrfiles = 0;
    int nrfields = 0;
    int nrfilters = 0;

    allocate( nrfiles, nrfields, nrfilters );

    for ( int i = 0; i < nrfiles; i++ ) {
      fileName[i] = "filename" + ( i + 1 );
      fileMask[i] = "";
      excludeFileMask[i] = "";
      fileRequired[i] = NO;
      includeSubFolders[i] = NO;
    }

    for ( int i = 0; i < nrfields; i++ ) {
      inputFields[i] = new TextFileInputField( "field" + ( i + 1 ), 1, -1 );
    }

    dateFormatLocale = Locale.getDefault();

    rowLimit = 0L;
  }

  @Override
  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( !isPassingThruFields() ) {
      // all incoming fields are not transmitted !
      row.clear();
    } else {
      if ( info != null ) {
        boolean found = false;
        for ( int i = 0; i < info.length && !found; i++ ) {
          if ( info[i] != null ) {
            row.mergeRowMeta( info[i] );
            found = true;
          }
        }
      }
    }

    for ( int i = 0; i < inputFields.length; i++ ) {
      TextFileInputField field = inputFields[i];

      int type = field.getType();
      if ( type == ValueMetaInterface.TYPE_NONE ) {
        type = ValueMetaInterface.TYPE_STRING;
      }

      try {
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( field.getName(), type );
        v.setLength( field.getLength() );
        v.setPrecision( field.getPrecision() );
        v.setOrigin( name );
        v.setConversionMask( field.getFormat() );
        v.setDecimalSymbol( field.getDecimalSymbol() );
        v.setGroupingSymbol( field.getGroupSymbol() );
        v.setCurrencySymbol( field.getCurrencySymbol() );
        v.setDateFormatLenient( dateFormatLenient );
        v.setDateFormatLocale( dateFormatLocale );
        v.setTrimType( field.getTrimType() );

        row.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
    if ( errorIgnored ) {
      if ( errorCountField != null && errorCountField.length() > 0 ) {
        ValueMetaInterface v = new ValueMetaInteger( errorCountField );
        v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
        v.setOrigin( name );
        row.addValueMeta( v );
      }
      if ( errorFieldsField != null && errorFieldsField.length() > 0 ) {
        ValueMetaInterface v = new ValueMetaString( errorFieldsField );
        v.setOrigin( name );
        row.addValueMeta( v );
      }
      if ( errorTextField != null && errorTextField.length() > 0 ) {
        ValueMetaInterface v = new ValueMetaString( errorTextField );
        v.setOrigin( name );
        row.addValueMeta( v );
      }
    }
    if ( includeFilename ) {
      ValueMetaInterface v = new ValueMetaString( filenameField );
      v.setLength( 100 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( includeRowNumber ) {
      ValueMetaInterface v = new ValueMetaInteger( rowNumberField );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    // Add additional fields

    if ( getShortFileNameField() != null && getShortFileNameField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaString( space.environmentSubstitute( getShortFileNameField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( getExtensionField() != null && getExtensionField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaString( space.environmentSubstitute( getExtensionField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( getPathField() != null && getPathField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaString( space.environmentSubstitute( getPathField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( getSizeField() != null && getSizeField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaInteger( space.environmentSubstitute( getSizeField() ) );
      v.setOrigin( name );
      v.setLength( 9 );
      row.addValueMeta( v );
    }
    if ( isHiddenField() != null && isHiddenField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaBoolean( space.environmentSubstitute( isHiddenField() ) );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    if ( getLastModificationDateField() != null && getLastModificationDateField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaDate(
          space.environmentSubstitute( getLastModificationDateField() ) );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( getUriField() != null && getUriField().length() > 0 ) {
      ValueMetaInterface v =
        new ValueMetaString( space.environmentSubstitute( getUriField() ) );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    if ( getRootUriField() != null && getRootUriField().length() > 0 ) {
      ValueMetaInterface v = new ValueMetaString( getRootUriField() );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

  }

  @Override
  @Deprecated
  public void getFields( RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space ) throws KettleStepException {
    getFields( inputRowMeta, name, info, nextStep, space, null, null );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 1500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "accept_filenames", acceptingFilenames ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "passing_through_fields", passingThruFields ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "accept_field", acceptingField ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "accept_stepname", ( acceptingStep != null ? acceptingStep.getName() : "" ) ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "separator", separator ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "enclosure", enclosure ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "enclosure_breaks", breakInEnclosureAllowed ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "escapechar", escapeCharacter ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "header", header ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_headerlines", nrHeaderLines ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "footer", footer ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_footerlines", nrFooterLines ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "line_wrapped", lineWrapped ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_wraps", nrWraps ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "layout_paged", layoutPaged ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_lines_per_page", nrLinesPerPage ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_lines_doc_header", nrLinesDocHeader ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "noempty", noEmptyLines ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include", includeFilename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_field", filenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownumByFile", rowNumberByFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "format", fileFormat ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " + XMLHandler.addTagValue( "add_to_result_filenames", isaddresult ) );

    retval.append( "    <file>" ).append( Const.CR );
    for ( int i = 0; i < fileName.length; i++ ) {
      saveSource( retval, fileName[i] );
      retval.append( "      " ).append( XMLHandler.addTagValue( "filemask", fileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "exclude_filemask", excludeFileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "file_required", fileRequired[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders", includeSubFolders[i] ) );
    }
    retval.append( "      " ).append( XMLHandler.addTagValue( "type", fileType ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compression",
      ( fileCompression == null ) ? "None" : fileCompression ) );
    retval.append( "    </file>" ).append( Const.CR );

    retval.append( "    <filters>" ).append( Const.CR );
    for ( int i = 0; i < filter.length; i++ ) {
      String filterString = filter[i].getFilterString();
      byte[] filterBytes = new byte[] {};
      String filterPrefix = "";
      if ( filterString != null ) {
        filterBytes = filterString.getBytes();
        filterPrefix = STRING_BASE64_PREFIX;
      }
      String filterEncoded = filterPrefix + new String( Base64.encodeBase64( filterBytes ) );

      retval.append( "      <filter>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "filter_string", filterEncoded, false ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "filter_position", filter[i].getFilterPosition(), false ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "filter_is_last_line", filter[i].isFilterLastLine(), false ) );
      retval.append( "        " ).append(
        XMLHandler.addTagValue( "filter_is_positive", filter[i].isFilterPositive(), false ) );
      retval.append( "      </filter>" ).append( Const.CR );
    }
    retval.append( "    </filters>" ).append( Const.CR );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFields.length; i++ ) {
      TextFileInputField field = inputFields[i];

      retval.append( "      <field>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", field.getName() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "type", field.getTypeDesc() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "format", field.getFormat() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "currency", field.getCurrencySymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "decimal", field.getDecimalSymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "group", field.getGroupSymbol() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "nullif", field.getNullString() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "ifnull", field.getIfNullValue() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "position", field.getPosition() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "length", field.getLength() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "precision", field.getPrecision() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "trim_type", field.getTrimTypeCode() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "repeat", field.isRepeated() ) );
      retval.append( "      </field>" ).append( Const.CR );
    }
    retval.append( "    </fields>" ).append( Const.CR );
    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", rowLimit ) );

    // ERROR HANDLING
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_ignored", errorIgnored ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "skip_bad_files", skipBadFiles ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "file_error_field", fileErrorField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "file_error_message_field", fileErrorMessageField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_line_skipped", errorLineSkipped ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_count_field", errorCountField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_fields_field", errorFieldsField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_text_field", errorTextField ) );

    retval.append( "    " ).append(
      XMLHandler.addTagValue( "bad_line_files_destination_directory", warningFilesDestinationDirectory ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "bad_line_files_extension", warningFilesExtension ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "error_line_files_destination_directory", errorFilesDestinationDirectory ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_line_files_extension", errorFilesExtension ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "line_number_files_destination_directory", lineNumberFilesDestinationDirectory ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "line_number_files_extension", lineNumberFilesExtension ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "date_format_lenient", dateFormatLenient ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "date_format_locale", dateFormatLocale != null
      ? dateFormatLocale.toString() : Locale.getDefault().toString() ) );

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

  public String getLookupStepname() {
    if ( acceptingFilenames && acceptingStep != null && !Utils.isEmpty( acceptingStep.getName() ) ) {
      return acceptingStep.getName();
    }
    return null;
  }

  /**
   * @param steps
   *          optionally search the info step in a list of steps
   */
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

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      acceptingFilenames = rep.getStepAttributeBoolean( id_step, "accept_filenames" );
      passingThruFields = rep.getStepAttributeBoolean( id_step, "passing_through_fields" );
      acceptingField = rep.getStepAttributeString( id_step, "accept_field" );
      acceptingStepName = rep.getStepAttributeString( id_step, "accept_stepname" );

      separator = rep.getStepAttributeString( id_step, "separator" );
      enclosure = rep.getStepAttributeString( id_step, "enclosure" );
      breakInEnclosureAllowed = rep.getStepAttributeBoolean( id_step, "enclosure_breaks" );
      escapeCharacter = rep.getStepAttributeString( id_step, "escapechar" );
      header = rep.getStepAttributeBoolean( id_step, "header" );
      nrHeaderLines = (int) rep.getStepAttributeInteger( id_step, "nr_headerlines" );
      footer = rep.getStepAttributeBoolean( id_step, "footer" );
      nrFooterLines = (int) rep.getStepAttributeInteger( id_step, "nr_footerlines" );
      lineWrapped = rep.getStepAttributeBoolean( id_step, "line_wrapped" );
      nrWraps = (int) rep.getStepAttributeInteger( id_step, "nr_wraps" );
      layoutPaged = rep.getStepAttributeBoolean( id_step, "layout_paged" );
      nrLinesPerPage = (int) rep.getStepAttributeInteger( id_step, "nr_lines_per_page" );
      nrLinesDocHeader = (int) rep.getStepAttributeInteger( id_step, "nr_lines_doc_header" );
      noEmptyLines = rep.getStepAttributeBoolean( id_step, "noempty" );

      includeFilename = rep.getStepAttributeBoolean( id_step, "include" );
      filenameField = rep.getStepAttributeString( id_step, "include_field" );
      includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );
      rowNumberByFile = rep.getStepAttributeBoolean( id_step, "rownumByFile" );
      rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );

      fileFormat = rep.getStepAttributeString( id_step, "format" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      String addToResult = rep.getStepAttributeString( id_step, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResult ) ) {
        isaddresult = true;
      } else {
        isaddresult = rep.getStepAttributeBoolean( id_step, "add_to_result_filenames" );
      }

      rowLimit = rep.getStepAttributeInteger( id_step, "limit" );

      int nrfiles = rep.countNrStepAttributes( id_step, "file_name" );
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );
      int nrfilters = rep.countNrStepAttributes( id_step, "filter_string" );

      allocate( nrfiles, nrfields, nrfilters );

      for ( int i = 0; i < nrfiles; i++ ) {
        fileName[i] = loadSourceRep( rep, id_step, i );
        fileMask[i] = rep.getStepAttributeString( id_step, i, "file_mask" );
        excludeFileMask[i] = rep.getStepAttributeString( id_step, i, "exclude_file_mask" );
        fileRequired[i] = rep.getStepAttributeString( id_step, i, "file_required" );
        if ( !YES.equalsIgnoreCase( fileRequired[i] ) ) {
          fileRequired[i] = NO;
        }
        includeSubFolders[i] = rep.getStepAttributeString( id_step, i, "include_subfolders" );
        if ( !YES.equalsIgnoreCase( includeSubFolders[i] ) ) {
          includeSubFolders[i] = NO;
        }
      }
      fileType = rep.getStepAttributeString( id_step, "file_type" );
      fileCompression = rep.getStepAttributeString( id_step, "compression" );
      if ( fileCompression == null ) {
        fileCompression = "None";
        if ( rep.getStepAttributeBoolean( id_step, "file_zipped" ) ) {
          fileCompression = "Zip";
        }
      }

      for ( int i = 0; i < nrfilters; i++ ) {
        filter[i] = new TextFileFilter();
        filter[i].setFilterPosition( (int) rep.getStepAttributeInteger( id_step, i, "filter_position" ) );
        filter[i].setFilterString( rep.getStepAttributeString( id_step, i, "filter_string" ) );
        filter[i].setFilterLastLine( rep.getStepAttributeBoolean( id_step, i, "filter_is_last_line" ) );
        filter[i].setFilterPositive( rep.getStepAttributeBoolean( id_step, i, "filter_is_positive" ) );
      }

      for ( int i = 0; i < nrfields; i++ ) {
        TextFileInputField field = new TextFileInputField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
        field.setNullString( rep.getStepAttributeString( id_step, i, "field_nullif" ) );
        field.setIfNullValue( rep.getStepAttributeString( id_step, i, "field_ifnull" ) );
        field.setPosition( (int) rep.getStepAttributeInteger( id_step, i, "field_position" ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        field.setTrimType( ValueMetaString
          .getTrimTypeByCode( rep.getStepAttributeString( id_step, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );

        inputFields[i] = field;
      }

      errorIgnored = rep.getStepAttributeBoolean( id_step, "error_ignored" );
      skipBadFiles = rep.getStepAttributeBoolean( id_step, "skip_bad_files" );
      fileErrorField = rep.getStepAttributeString( id_step, "file_error_field" );
      fileErrorMessageField = rep.getStepAttributeString( id_step, "file_error_message_field" );

      errorLineSkipped = rep.getStepAttributeBoolean( id_step, "error_line_skipped" );
      errorCountField = rep.getStepAttributeString( id_step, "error_count_field" );
      errorFieldsField = rep.getStepAttributeString( id_step, "error_fields_field" );
      errorTextField = rep.getStepAttributeString( id_step, "error_text_field" );

      warningFilesDestinationDirectory = rep.getStepAttributeString( id_step, "bad_line_files_dest_dir" );
      warningFilesExtension = rep.getStepAttributeString( id_step, "bad_line_files_ext" );
      errorFilesDestinationDirectory = rep.getStepAttributeString( id_step, "error_line_files_dest_dir" );
      errorFilesExtension = rep.getStepAttributeString( id_step, "error_line_files_ext" );
      lineNumberFilesDestinationDirectory = rep.getStepAttributeString( id_step, "line_number_files_dest_dir" );
      lineNumberFilesExtension = rep.getStepAttributeString( id_step, "line_number_files_ext" );

      dateFormatLenient = rep.getStepAttributeBoolean( id_step, 0, "date_format_lenient", true );

      String dateLocale = rep.getStepAttributeString( id_step, 0, "date_format_locale" );
      if ( dateLocale != null ) {
        dateFormatLocale = EnvUtil.createLocale( dateLocale );
      } else {
        dateFormatLocale = Locale.getDefault();
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
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "accept_filenames", acceptingFilenames );
      rep.saveStepAttribute( id_transformation, id_step, "passing_through_fields", passingThruFields );
      rep.saveStepAttribute( id_transformation, id_step, "accept_field", acceptingField );
      rep.saveStepAttribute( id_transformation, id_step, "accept_stepname", ( acceptingStep != null
        ? acceptingStep.getName() : "" ) );

      rep.saveStepAttribute( id_transformation, id_step, "separator", separator );
      rep.saveStepAttribute( id_transformation, id_step, "enclosure", enclosure );
      rep.saveStepAttribute( id_transformation, id_step, "enclosure_breaks", breakInEnclosureAllowed );
      rep.saveStepAttribute( id_transformation, id_step, "escapechar", escapeCharacter );
      rep.saveStepAttribute( id_transformation, id_step, "header", header );
      rep.saveStepAttribute( id_transformation, id_step, "nr_headerlines", nrHeaderLines );
      rep.saveStepAttribute( id_transformation, id_step, "footer", footer );
      rep.saveStepAttribute( id_transformation, id_step, "nr_footerlines", nrFooterLines );
      rep.saveStepAttribute( id_transformation, id_step, "line_wrapped", lineWrapped );
      rep.saveStepAttribute( id_transformation, id_step, "nr_wraps", nrWraps );
      rep.saveStepAttribute( id_transformation, id_step, "layout_paged", layoutPaged );
      rep.saveStepAttribute( id_transformation, id_step, "nr_lines_per_page", nrLinesPerPage );
      rep.saveStepAttribute( id_transformation, id_step, "nr_lines_doc_header", nrLinesDocHeader );

      rep.saveStepAttribute( id_transformation, id_step, "noempty", noEmptyLines );

      rep.saveStepAttribute( id_transformation, id_step, "include", includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, "include_field", filenameField );
      rep.saveStepAttribute( id_transformation, id_step, "rownum", includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "rownumByFile", rowNumberByFile );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", rowNumberField );

      rep.saveStepAttribute( id_transformation, id_step, "format", fileFormat );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "add_to_result_filenames", isaddresult );

      rep.saveStepAttribute( id_transformation, id_step, "limit", rowLimit );

      for ( int i = 0; i < fileName.length; i++ ) {
        saveSourceRep( rep, id_transformation, id_step, i, fileName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_mask", fileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "exclude_file_mask", excludeFileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_required", fileRequired[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "include_subfolders", includeSubFolders[i] );
      }
      rep.saveStepAttribute( id_transformation, id_step, "file_type", fileType );
      rep.saveStepAttribute( id_transformation, id_step, "compression",
        ( fileCompression == null ) ? "None" : fileCompression );

      for ( int i = 0; i < filter.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_position", filter[i].getFilterPosition() );
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_string", filter[i].getFilterString() );
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_is_last_line", filter[i].isFilterLastLine() );
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_is_positive", filter[i].isFilterPositive() );
      }

      for ( int i = 0; i < inputFields.length; i++ ) {
        TextFileInputField field = inputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_group", field.getGroupSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_nullif", field.getNullString() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_ifnull", field.getIfNullValue() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_position", field.getPosition() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_length", field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", field.getPrecision() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_trim_type", field.getTrimTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_repeat", field.isRepeated() );
      }

      rep.saveStepAttribute( id_transformation, id_step, "error_ignored", errorIgnored );
      rep.saveStepAttribute( id_transformation, id_step, "skip_bad_files", skipBadFiles );
      rep.saveStepAttribute( id_transformation, id_step, "file_error_field", fileErrorField );
      rep.saveStepAttribute( id_transformation, id_step, "file_error_message_field", fileErrorMessageField );
      rep.saveStepAttribute( id_transformation, id_step, "error_line_skipped", errorLineSkipped );
      rep.saveStepAttribute( id_transformation, id_step, "error_count_field", errorCountField );
      rep.saveStepAttribute( id_transformation, id_step, "error_fields_field", errorFieldsField );
      rep.saveStepAttribute( id_transformation, id_step, "error_text_field", errorTextField );

      rep.saveStepAttribute(
        id_transformation, id_step, "bad_line_files_dest_dir", warningFilesDestinationDirectory );
      rep.saveStepAttribute( id_transformation, id_step, "bad_line_files_ext", warningFilesExtension );
      rep.saveStepAttribute(
        id_transformation, id_step, "error_line_files_dest_dir", errorFilesDestinationDirectory );
      rep.saveStepAttribute( id_transformation, id_step, "error_line_files_ext", errorFilesExtension );
      rep.saveStepAttribute(
        id_transformation, id_step, "line_number_files_dest_dir", lineNumberFilesDestinationDirectory );
      rep.saveStepAttribute( id_transformation, id_step, "line_number_files_ext", lineNumberFilesExtension );

      rep.saveStepAttribute( id_transformation, id_step, "date_format_lenient", dateFormatLenient );
      rep.saveStepAttribute( id_transformation, id_step, "date_format_locale", dateFormatLocale.toString() );

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
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public String[] getFilePaths( VariableSpace space ) {
    return FileInputList.createFilePathList(
      space, fileName, fileMask, excludeFileMask, fileRequired, includeSubFolderBoolean() );
  }

  public FileInputList getTextFileList( VariableSpace space ) {
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
      if ( !isAcceptingFilenames() ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "TextFileInputMeta.CheckResult.NoInputError" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "TextFileInputMeta.CheckResult.AcceptFilenamesOk" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "TextFileInputMeta.CheckResult.NoInputOk" ), stepMeta );
      remarks.add( cr );
    }

    FileInputList textFileList = getTextFileList( transMeta );
    if ( textFileList.nrOfFiles() == 0 ) {
      if ( !isAcceptingFilenames() ) {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "TextFileInputMeta.CheckResult.ExpectedFilesError" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "TextFileInputMeta.CheckResult.ExpectedFilesOk", "" + textFileList.nrOfFiles() ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new TextFileInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new TextFileInputData();
  }

  /**
   * @return Returns the escapeCharacter.
   */
  @Override
  public String getEscapeCharacter() {
    return escapeCharacter;
  }

  /**
   * @param escapeCharacter
   *          The escapeCharacter to set.
   */
  public void setEscapeCharacter( String escapeCharacter ) {
    this.escapeCharacter = escapeCharacter;
  }

  @Override
  public String getErrorCountField() {
    return errorCountField;
  }

  public void setErrorCountField( String errorCountField ) {
    this.errorCountField = errorCountField;
  }

  @Override
  public String getErrorFieldsField() {
    return errorFieldsField;
  }

  public void setErrorFieldsField( String errorFieldsField ) {
    this.errorFieldsField = errorFieldsField;
  }

  @Override
  public boolean isErrorIgnored() {
    return errorIgnored;
  }

  public void setErrorIgnored( boolean errorIgnored ) {
    this.errorIgnored = errorIgnored;
  }

  @Override
  public String getErrorTextField() {
    return errorTextField;
  }

  public void setErrorTextField( String errorTextField ) {
    this.errorTextField = errorTextField;
  }

  /**
   * @return Returns the lineWrapped.
   */
  public boolean isLineWrapped() {
    return lineWrapped;
  }

  /**
   * @param lineWrapped
   *          The lineWrapped to set.
   */
  public void setLineWrapped( boolean lineWrapped ) {
    this.lineWrapped = lineWrapped;
  }

  /**
   * @return Returns the nrFooterLines.
   */
  public int getNrFooterLines() {
    return nrFooterLines;
  }

  /**
   * @param nrFooterLines
   *          The nrFooterLines to set.
   */
  public void setNrFooterLines( int nrFooterLines ) {
    this.nrFooterLines = nrFooterLines;
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
   * @return Returns the nrHeaderLines.
   */
  @Override
  public int getNrHeaderLines() {
    return nrHeaderLines;
  }

  /**
   * @param nrHeaderLines
   *          The nrHeaderLines to set.
   */
  public void setNrHeaderLines( int nrHeaderLines ) {
    this.nrHeaderLines = nrHeaderLines;
  }

  /**
   * @return Returns the nrWraps.
   */
  public int getNrWraps() {
    return nrWraps;
  }

  /**
   * @param nrWraps
   *          The nrWraps to set.
   */
  public void setNrWraps( int nrWraps ) {
    this.nrWraps = nrWraps;
  }

  /**
   * @return Returns the layoutPaged.
   */
  public boolean isLayoutPaged() {
    return layoutPaged;
  }

  /**
   * @param layoutPaged
   *          The layoutPaged to set.
   */
  public void setLayoutPaged( boolean layoutPaged ) {
    this.layoutPaged = layoutPaged;
  }

  /**
   * @return Returns the nrLinesPerPage.
   */
  public int getNrLinesPerPage() {
    return nrLinesPerPage;
  }

  /**
   * @param nrLinesPerPage
   *          The nrLinesPerPage to set.
   */
  public void setNrLinesPerPage( int nrLinesPerPage ) {
    this.nrLinesPerPage = nrLinesPerPage;
  }

  /**
   * @return Returns the nrLinesDocHeader.
   */
  public int getNrLinesDocHeader() {
    return nrLinesDocHeader;
  }

  /**
   * @param nrLinesDocHeader
   *          The nrLinesDocHeader to set.
   */
  public void setNrLinesDocHeader( int nrLinesDocHeader ) {
    this.nrLinesDocHeader = nrLinesDocHeader;
  }

  public String getWarningFilesDestinationDirectory() {
    return warningFilesDestinationDirectory;
  }

  public void setWarningFilesDestinationDirectory( String warningFilesDestinationDirectory ) {
    this.warningFilesDestinationDirectory = warningFilesDestinationDirectory;
  }

  public String getWarningFilesExtension() {
    return warningFilesExtension;
  }

  public void setWarningFilesExtension( String warningFilesExtension ) {
    this.warningFilesExtension = warningFilesExtension;
  }

  public String getLineNumberFilesDestinationDirectory() {
    return lineNumberFilesDestinationDirectory;
  }

  public void setLineNumberFilesDestinationDirectory( String lineNumberFilesDestinationDirectory ) {
    this.lineNumberFilesDestinationDirectory = lineNumberFilesDestinationDirectory;
  }

  public String getLineNumberFilesExtension() {
    return lineNumberFilesExtension;
  }

  public void setLineNumberFilesExtension( String lineNumberFilesExtension ) {
    this.lineNumberFilesExtension = lineNumberFilesExtension;
  }

  public String getErrorFilesDestinationDirectory() {
    return errorFilesDestinationDirectory;
  }

  public void setErrorFilesDestinationDirectory( String errorFilesDestinationDirectory ) {
    this.errorFilesDestinationDirectory = errorFilesDestinationDirectory;
  }

  public String getErrorLineFilesExtension() {
    return errorFilesExtension;
  }

  public void setErrorLineFilesExtension( String errorLineFilesExtension ) {
    this.errorFilesExtension = errorLineFilesExtension;
  }

  public boolean isDateFormatLenient() {
    return dateFormatLenient;
  }

  public void setDateFormatLenient( boolean dateFormatLenient ) {
    this.dateFormatLenient = dateFormatLenient;
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

  @Override
  public boolean isErrorLineSkipped() {
    return errorLineSkipped;
  }

  public void setErrorLineSkipped( boolean errorLineSkipped ) {
    this.errorLineSkipped = errorLineSkipped;
  }

  /**
   * @return Returns the dateFormatLocale.
   */
  public Locale getDateFormatLocale() {
    return dateFormatLocale;
  }

  /**
   * @param dateFormatLocale
   *          The dateFormatLocale to set.
   */
  public void setDateFormatLocale( Locale dateFormatLocale ) {
    this.dateFormatLocale = dateFormatLocale;
  }

  public boolean isAcceptingFilenames() {
    return acceptingFilenames;
  }

  public void setAcceptingFilenames( boolean getFileFromJob ) {
    this.acceptingFilenames = getFileFromJob;
  }

  public boolean isPassingThruFields() {
    return passingThruFields;
  }

  public void setPassingThruFields( boolean passingThruFields ) {
    this.passingThruFields = passingThruFields;
  }

  /**
   * @return Returns the fileNameField.
   */
  public String getAcceptingField() {
    return acceptingField;
  }

  /**
   * @param fileNameField
   *          The fileNameField to set.
   */
  public void setAcceptingField( String fileNameField ) {
    this.acceptingField = fileNameField;
  }

  /**
   * @return Returns the acceptingStep.
   */
  public String getAcceptingStepName() {
    return acceptingStepName;
  }

  /**
   * @param acceptingStep
   *          The acceptingStep to set.
   */
  public void setAcceptingStepName( String acceptingStep ) {
    this.acceptingStepName = acceptingStep;
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

  @Override
  public int getFileFormatTypeNr() {
    // calculate the file format type in advance so we can use a switch
    if ( getFileFormat().equalsIgnoreCase( "DOS" ) ) {
      return FILE_FORMAT_DOS;
    } else if ( getFileFormat().equalsIgnoreCase( "unix" ) ) {
      return TextFileInputMeta.FILE_FORMAT_UNIX;
    } else {
      return TextFileInputMeta.FILE_FORMAT_MIXED;
    }
  }

  public int getFileTypeNr() {
    // calculate the file type in advance CSV or Fixed?
    if ( getFileType().equalsIgnoreCase( "CSV" ) ) {
      return TextFileInputMeta.FILE_TYPE_CSV;
    } else {
      return TextFileInputMeta.FILE_TYPE_FIXED;
    }
  }

  @Override
  public List<ResourceReference> getResourceDependencies( TransMeta transMeta, StepMeta stepInfo ) {
    List<ResourceReference> references = new ArrayList<ResourceReference>( 5 );
    ResourceReference reference = new ResourceReference( stepInfo );
    references.add( reference );

    String[] textFiles = getFilePaths( transMeta );
    if ( textFiles != null ) {
      for ( int i = 0; i < textFiles.length; i++ ) {
        reference.getEntries().add( new ResourceEntry( textFiles[i], ResourceType.FILE ) );
      }
    }
    return references;
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

        // Replace the filename ONLY (folder or filename)
        //
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

  @Override
  public boolean supportsErrorHandling() {
    return isErrorIgnored() && isSkipBadFiles();
  }

  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new TextFileInputMetaInjection( this );
  }


  @VisibleForTesting
  public void setFileNameForTest( String[] fileName ) {
    allocateFiles( fileName.length );
    setFileName( fileName );
  }

  protected String loadSource( Node filenode, Node filenamenode, int i, IMetaStore metaStore ) {
    return XMLHandler.getNodeValue( filenamenode );
  }

  protected void saveSource( StringBuilder retVal, String source ) {
    retVal.append( "      " ).append( XMLHandler.addTagValue( "name", source ) );
  }

  protected String loadSourceRep( Repository rep, ObjectId id_step, int i ) throws KettleException {
    return rep.getStepAttributeString( id_step, i, "file_name" );
  }

  protected void saveSourceRep( Repository rep, ObjectId id_transformation, ObjectId id_step, int i, String fileName )
    throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, i, "file_name", fileName ); //this should be in subclass
  }
}
