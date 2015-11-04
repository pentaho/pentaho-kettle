/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput.text;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.util.EnvUtil;
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
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputStepMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import com.google.common.annotations.VisibleForTesting;

public class TextFileInputMeta extends BaseFileInputStepMeta implements StepMetaInterface {
  private static Class<?> PKG = TextFileInputMeta.class; // for i18n purposes, needed by Translator2!! TODO: check i18n
                                                         // for base

  private static final String STRING_BASE64_PREFIX = "Base64: ";

  public static final int FILE_FORMAT_DOS = 0;
  public static final int FILE_FORMAT_UNIX = 1;
  public static final int FILE_FORMAT_MIXED = 2;

  public static final int FILE_TYPE_CSV = 0;
  public static final int FILE_TYPE_FIXED = 1;

  public Content content = new Content();

  public static class Content implements Cloneable {

    /** Type of file: CSV or fixed */
    public String fileType;

    /** String used to separated field (;) */
    public String separator;

    /** String used to enclose separated fields (") */
    public String enclosure;

    /** Switch to allow breaks (CR/LF) in Enclosures */
    public boolean breakInEnclosureAllowed;

    /** Escape character used to escape the enclosure String (\) */
    public String escapeCharacter;

    /** Flag indicating that the file contains one header line that should be skipped. */
    public boolean header;

    /** The number of header lines, defaults to 1 */
    public int nrHeaderLines;

    /** Flag indicating that the file contains one footer line that should be skipped. */
    public boolean footer;

    /** The number of footer lines, defaults to 1 */
    public int nrFooterLines;

    /** Flag indicating that a single line is wrapped onto one or more lines in the text file. */
    public boolean lineWrapped;

    /** The number of times the line wrapped */
    public int nrWraps;

    /** Flag indicating that the text-file has a paged layout. */
    public boolean layoutPaged;

    /** The number of lines to read per page */
    public int nrLinesPerPage;

    /** The number of lines in the document header */
    public int nrLinesDocHeader;

    /** Type of compression being used */
    public String fileCompression;

    /** Flag indicating that we should skip all empty lines */
    public boolean noEmptyLines;

    /** Flag indicating that we should include the filename in the output */
    public boolean includeFilename;

    /** The name of the field in the output containing the filename */
    public String filenameField;

    /** Flag indicating that a row number field should be included in the output */
    public boolean includeRowNumber;

    /** The name of the field in the output containing the row number */
    public String rowNumberField;

    /** Flag indicating row number is per file */
    public boolean rowNumberByFile;

    /** The file format: DOS or UNIX or mixed */
    public String fileFormat;

    /** The encoding to use for reading: null or empty string means system default encoding */
    public String encoding;

    /** The maximum number or lines to read */
    public long rowLimit;

    /** Indicate whether or not we want to date fields strictly according to the format or lenient */
    public boolean dateFormatLenient;

    /** Specifies the Locale of the Date format, null means the default */
    public Locale dateFormatLocale;

  }

  /** The filters to use... */
  private TextFileFilter[] filter = {};

  /** The name of the field that will contain the number of errors in the row */
  private String errorCountField;

  /** The name of the field that will contain the names of the fields that generated errors, separated by , */
  private String errorFieldsField;

  /** The name of the field that will contain the error texts, separated by CR */
  private String errorTextField;

  /** If error line are skipped, you can replay without introducing doubles. */
  private boolean errorLineSkipped;

  /** The step to accept filenames from */
  private StepMeta acceptingStep;

  /**
   * @return Returns the fileName.
   */
  public String[] getFileName() {
    return inputFiles.fileName;
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName( String[] fileName ) {
    inputFiles.fileName = fileName;
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

  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    try {
      inputFiles.acceptingFilenames = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "accept_filenames" ) );
      inputFiles.passingThruFields =
          YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "passing_through_fields" ) );
      inputFiles.acceptingField = XMLHandler.getTagValue( stepnode, "accept_field" );
      inputFiles.acceptingStepName = XMLHandler.getTagValue( stepnode, "accept_stepname" );

      content.separator = XMLHandler.getTagValue( stepnode, "separator" );
      content.enclosure = XMLHandler.getTagValue( stepnode, "enclosure" );
      content.breakInEnclosureAllowed = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "enclosure_breaks" ) );
      content.escapeCharacter = XMLHandler.getTagValue( stepnode, "escapechar" );
      content.header = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "header" ) );
      content.nrHeaderLines = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_headerlines" ), 1 );
      content.footer = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "footer" ) );
      content.nrFooterLines = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_footerlines" ), 1 );
      content.lineWrapped = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "line_wrapped" ) );
      content.nrWraps = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_wraps" ), 1 );
      content.layoutPaged = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "layout_paged" ) );
      content.nrLinesPerPage = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_lines_per_page" ), 1 );
      content.nrLinesDocHeader = Const.toInt( XMLHandler.getTagValue( stepnode, "nr_lines_doc_header" ), 1 );
      String addToResult = XMLHandler.getTagValue( stepnode, "add_to_result_filenames" );
      if ( Const.isEmpty( addToResult ) ) {
        inputFiles.isaddresult = true;
      } else {
        inputFiles.isaddresult = "Y".equalsIgnoreCase( addToResult );
      }

      String nempty = XMLHandler.getTagValue( stepnode, "noempty" );
      content.noEmptyLines = YES.equalsIgnoreCase( nempty ) || nempty == null;
      content.includeFilename = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "include" ) );
      content.filenameField = XMLHandler.getTagValue( stepnode, "include_field" );
      content.includeRowNumber = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      content.rowNumberByFile = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownumByFile" ) );
      content.rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      content.fileFormat = XMLHandler.getTagValue( stepnode, "format" );
      content.encoding = XMLHandler.getTagValue( stepnode, "encoding" );

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
        inputFiles.fileName[i] = loadSource( filenode, filenamenode, i );
        inputFiles.fileMask[i] = XMLHandler.getNodeValue( filemasknode );
        inputFiles.excludeFileMask[i] = XMLHandler.getNodeValue( excludefilemasknode );
        inputFiles.fileRequired[i] = XMLHandler.getNodeValue( fileRequirednode );
        inputFiles.includeSubFolders[i] = XMLHandler.getNodeValue( includeSubFoldersnode );
      }

      content.fileType = XMLHandler.getTagValue( stepnode, "file", "type" );
      content.fileCompression = XMLHandler.getTagValue( stepnode, "file", "compression" );
      if ( content.fileCompression == null ) {
        content.fileCompression = "None";
        if ( YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "zipped" ) ) ) {
          content.fileCompression = "Zip";
        }
      }

      // Backward compatibility : just one filter
      if ( XMLHandler.getTagValue( stepnode, "filter" ) != null ) {
        filter = new TextFileFilter[1];
        filter[0] = new TextFileFilter();

        filter[0].setFilterPosition( Const.toInt( XMLHandler.getTagValue( stepnode, "filter_position" ), -1 ) );
        filter[0].setFilterString( XMLHandler.getTagValue( stepnode, "filter_string" ) );
        filter[0].setFilterLastLine( YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode,
            "filter_is_last_line" ) ) );
        filter[0].setFilterPositive( YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "filter_is_positive" ) ) );
      } else {
        for ( int i = 0; i < nrfilters; i++ ) {
          Node fnode = XMLHandler.getSubNodeByNr( filtersNode, "filter", i );
          filter[i] = new TextFileFilter();

          filter[i].setFilterPosition( Const.toInt( XMLHandler.getTagValue( fnode, "filter_position" ), -1 ) );

          String filterString = XMLHandler.getTagValue( fnode, "filter_string" );
          if ( filterString != null && filterString.startsWith( STRING_BASE64_PREFIX ) ) {
            filter[i].setFilterString( new String( Base64.decodeBase64( filterString.substring( STRING_BASE64_PREFIX
                .length() ).getBytes() ) ) );
          } else {
            filter[i].setFilterString( filterString );
          }

          filter[i].setFilterLastLine( YES.equalsIgnoreCase( XMLHandler.getTagValue( fnode, "filter_is_last_line" ) ) );
          filter[i].setFilterPositive( YES.equalsIgnoreCase( XMLHandler.getTagValue( fnode, "filter_is_positive" ) ) );
        }
      }

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
        BaseFileInputField field = new BaseFileInputField();

        field.setName( XMLHandler.getTagValue( fnode, "name" ) );
        field.setType( ValueMeta.getType( XMLHandler.getTagValue( fnode, "type" ) ) );
        field.setFormat( XMLHandler.getTagValue( fnode, "format" ) );
        field.setCurrencySymbol( XMLHandler.getTagValue( fnode, "currency" ) );
        field.setDecimalSymbol( XMLHandler.getTagValue( fnode, "decimal" ) );
        field.setGroupSymbol( XMLHandler.getTagValue( fnode, "group" ) );
        field.setNullString( XMLHandler.getTagValue( fnode, "nullif" ) );
        field.setIfNullValue( XMLHandler.getTagValue( fnode, "ifnull" ) );
        field.setPosition( Const.toInt( XMLHandler.getTagValue( fnode, "position" ), -1 ) );
        field.setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
        field.setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
        field.setTrimType( ValueMeta.getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );
        field.setRepeated( YES.equalsIgnoreCase( XMLHandler.getTagValue( fnode, "repeat" ) ) );

        inputFiles.inputFields[i] = field;
      }

      // Is there a limit on the number of rows we process?
      content.rowLimit = Const.toLong( XMLHandler.getTagValue( stepnode, "limit" ), 0L );

      errorHandling.errorIgnored = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "error_ignored" ) );
      errorHandling.skipBadFiles = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "skip_bad_files" ) );
      errorHandling.fileErrorField = XMLHandler.getTagValue( stepnode, "file_error_field" );
      errorHandling.fileErrorMessageField = XMLHandler.getTagValue( stepnode, "file_error_message_field" );
      errorLineSkipped = YES.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "error_line_skipped" ) );
      errorCountField = XMLHandler.getTagValue( stepnode, "error_count_field" );
      errorFieldsField = XMLHandler.getTagValue( stepnode, "error_fields_field" );
      errorTextField = XMLHandler.getTagValue( stepnode, "error_text_field" );
      errorHandling.warningFilesDestinationDirectory =
          XMLHandler.getTagValue( stepnode, "bad_line_files_destination_directory" );
      errorHandling.warningFilesExtension = XMLHandler.getTagValue( stepnode, "bad_line_files_extension" );
      errorHandling.errorFilesDestinationDirectory =
          XMLHandler.getTagValue( stepnode, "error_line_files_destination_directory" );
      errorHandling.errorFilesExtension = XMLHandler.getTagValue( stepnode, "error_line_files_extension" );
      errorHandling.lineNumberFilesDestinationDirectory =
          XMLHandler.getTagValue( stepnode, "line_number_files_destination_directory" );
      errorHandling.lineNumberFilesExtension = XMLHandler.getTagValue( stepnode, "line_number_files_extension" );
      // Backward compatible

      content.dateFormatLenient = !NO.equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "date_format_lenient" ) );
      String dateLocale = XMLHandler.getTagValue( stepnode, "date_format_locale" );
      if ( dateLocale != null ) {
        content.dateFormatLocale = EnvUtil.createLocale( dateLocale );
      } else {
        content.dateFormatLocale = Locale.getDefault();
      }

      additionalOutputFields.shortFilenameField = XMLHandler.getTagValue( stepnode, "shortFileFieldName" );
      additionalOutputFields.pathField = XMLHandler.getTagValue( stepnode, "pathFieldName" );
      additionalOutputFields.hiddenField = XMLHandler.getTagValue( stepnode, "hiddenFieldName" );
      additionalOutputFields.lastModificationField =
          XMLHandler.getTagValue( stepnode, "lastModificationTimeFieldName" );
      additionalOutputFields.uriField = XMLHandler.getTagValue( stepnode, "uriNameFieldName" );
      additionalOutputFields.rootUriField = XMLHandler.getTagValue( stepnode, "rootUriNameFieldName" );
      additionalOutputFields.extensionField = XMLHandler.getTagValue( stepnode, "extensionFieldName" );
      additionalOutputFields.sizeField = XMLHandler.getTagValue( stepnode, "sizeFieldName" );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public Object clone() {
    TextFileInputMeta retval = (TextFileInputMeta) super.clone();

    int nrfiles = inputFiles.fileName.length;
    int nrfields = inputFiles.inputFields.length;
    int nrfilters = filter.length;

    retval.allocate( nrfiles, nrfields, nrfilters );

    for ( int i = 0; i < nrfiles; i++ ) {
      retval.inputFiles.fileName[i] = inputFiles.fileName[i];
      retval.inputFiles.fileMask[i] = inputFiles.fileMask[i];
      retval.inputFiles.excludeFileMask[i] = inputFiles.excludeFileMask[i];
      retval.inputFiles.fileRequired[i] = inputFiles.fileRequired[i];
      retval.inputFiles.includeSubFolders[i] = inputFiles.includeSubFolders[i];
    }

    for ( int i = 0; i < nrfields; i++ ) {
      retval.inputFiles.inputFields[i] = (BaseFileInputField) inputFiles.inputFields[i].clone();
    }

    for ( int i = 0; i < nrfilters; i++ ) {
      retval.filter[i] = (TextFileFilter) filter[i].clone();
    }

    retval.content.dateFormatLocale = (Locale) content.dateFormatLocale.clone();
    retval.content.fileCompression = content.fileCompression;

    return retval;
  }

  public void allocate( int nrfiles, int nrfields, int nrfilters ) {
    allocateFiles( nrfiles );

    inputFiles.inputFields = new BaseFileInputField[nrfields];
    filter = new TextFileFilter[nrfilters];
  }

  public void allocateFiles( int nrFiles ) {
    inputFiles.fileName = new String[nrFiles];
    inputFiles.fileMask = new String[nrFiles];
    inputFiles.excludeFileMask = new String[nrFiles];
    inputFiles.fileRequired = new String[nrFiles];
    inputFiles.includeSubFolders = new String[nrFiles];
  }

  public void setDefault() {
    additionalOutputFields.shortFilenameField = null;
    additionalOutputFields.pathField = null;
    additionalOutputFields.hiddenField = null;
    additionalOutputFields.lastModificationField = null;
    additionalOutputFields.uriField = null;
    additionalOutputFields.rootUriField = null;
    additionalOutputFields.extensionField = null;
    additionalOutputFields.sizeField = null;

    inputFiles.isaddresult = true;

    content.separator = ";";
    content.enclosure = "\"";
    content.breakInEnclosureAllowed = false;
    content.header = true;
    content.nrHeaderLines = 1;
    content.footer = false;
    content.nrFooterLines = 1;
    content.lineWrapped = false;
    content.nrWraps = 1;
    content.layoutPaged = false;
    content.nrLinesPerPage = 80;
    content.nrLinesDocHeader = 0;
    content.fileCompression = "None";
    content.noEmptyLines = true;
    content.fileFormat = "DOS";
    content.fileType = "CSV";
    content.includeFilename = false;
    content.filenameField = "";
    content.includeRowNumber = false;
    content.rowNumberField = "";
    content.dateFormatLenient = true;
    content.rowNumberByFile = false;

    errorHandling.errorIgnored = false;
    errorHandling.skipBadFiles = false;
    errorLineSkipped = false;
    errorHandling.warningFilesDestinationDirectory = null;
    errorHandling.warningFilesExtension = "warning";
    errorHandling.errorFilesDestinationDirectory = null;
    errorHandling.errorFilesExtension = "error";
    errorHandling.lineNumberFilesDestinationDirectory = null;
    errorHandling.lineNumberFilesExtension = "line";

    int nrfiles = 0;
    int nrfields = 0;
    int nrfilters = 0;

    allocate( nrfiles, nrfields, nrfilters );

    for ( int i = 0; i < nrfiles; i++ ) {
      inputFiles.fileName[i] = "filename" + ( i + 1 );
      inputFiles.fileMask[i] = "";
      inputFiles.excludeFileMask[i] = "";
      inputFiles.fileRequired[i] = NO;
      inputFiles.includeSubFolders[i] = NO;
    }

    for ( int i = 0; i < nrfields; i++ ) {
      inputFiles.inputFields[i] = new BaseFileInputField( "field" + ( i + 1 ), 1, -1 );
    }

    content.dateFormatLocale = Locale.getDefault();

    content.rowLimit = 0L;
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    if ( !inputFiles.passingThruFields ) {
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

    for ( int i = 0; i < inputFiles.inputFields.length; i++ ) {
      BaseFileInputField field = inputFiles.inputFields[i];

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
        v.setDateFormatLenient( content.dateFormatLenient );
        v.setDateFormatLocale( content.dateFormatLocale );
        v.setTrimType( field.getTrimType() );

        row.addValueMeta( v );
      } catch ( Exception e ) {
        throw new KettleStepException( e );
      }
    }
    if ( errorHandling.errorIgnored ) {
      if ( errorCountField != null && errorCountField.length() > 0 ) {
        ValueMetaInterface v = new ValueMeta( errorCountField, ValueMetaInterface.TYPE_INTEGER );
        v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
        v.setOrigin( name );
        row.addValueMeta( v );
      }
      if ( errorFieldsField != null && errorFieldsField.length() > 0 ) {
        ValueMetaInterface v = new ValueMeta( errorFieldsField, ValueMetaInterface.TYPE_STRING );
        v.setOrigin( name );
        row.addValueMeta( v );
      }
      if ( errorTextField != null && errorTextField.length() > 0 ) {
        ValueMetaInterface v = new ValueMeta( errorTextField, ValueMetaInterface.TYPE_STRING );
        v.setOrigin( name );
        row.addValueMeta( v );
      }
    }
    if ( content.includeFilename ) {
      ValueMetaInterface v = new ValueMeta( content.filenameField, ValueMetaInterface.TYPE_STRING );
      v.setLength( 100 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( content.includeRowNumber ) {
      ValueMetaInterface v = new ValueMeta( content.rowNumberField, ValueMetaInterface.TYPE_INTEGER );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    // Add additional fields

    if ( StringUtils.isNotBlank( additionalOutputFields.shortFilenameField ) ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( additionalOutputFields.shortFilenameField ),
              ValueMetaInterface.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( StringUtils.isNotBlank( additionalOutputFields.extensionField ) ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( additionalOutputFields.extensionField ),
              ValueMetaInterface.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( StringUtils.isNotBlank( additionalOutputFields.pathField ) ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( additionalOutputFields.pathField ),
              ValueMetaInterface.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( StringUtils.isNotBlank( additionalOutputFields.sizeField ) ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( additionalOutputFields.sizeField ),
              ValueMetaInterface.TYPE_INTEGER );
      v.setOrigin( name );
      v.setLength( 9 );
      row.addValueMeta( v );
    }
    if ( StringUtils.isNotBlank( additionalOutputFields.hiddenField ) ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( additionalOutputFields.hiddenField ),
              ValueMetaInterface.TYPE_BOOLEAN );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    if ( StringUtils.isNotBlank( additionalOutputFields.lastModificationField ) ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( additionalOutputFields.lastModificationField ),
              ValueMetaInterface.TYPE_DATE );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
    if ( StringUtils.isNotBlank( additionalOutputFields.uriField ) ) {
      ValueMetaInterface v =
          new ValueMeta( space.environmentSubstitute( additionalOutputFields.uriField ),
              ValueMetaInterface.TYPE_STRING );
      v.setLength( 100, -1 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }

    if ( StringUtils.isNotBlank( additionalOutputFields.rootUriField ) ) {
      ValueMetaInterface v = new ValueMeta( additionalOutputFields.rootUriField, ValueMetaInterface.TYPE_STRING );
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

  public String getXML() {
    StringBuffer retval = new StringBuffer( 1500 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "accept_filenames", inputFiles.acceptingFilenames ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "passing_through_fields", inputFiles.passingThruFields ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "accept_field", inputFiles.acceptingField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "accept_stepname", ( acceptingStep != null ? acceptingStep
        .getName() : "" ) ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "separator", content.separator ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "enclosure", content.enclosure ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "enclosure_breaks", content.breakInEnclosureAllowed ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "escapechar", content.escapeCharacter ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "header", content.header ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_headerlines", content.nrHeaderLines ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "footer", content.footer ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_footerlines", content.nrFooterLines ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "line_wrapped", content.lineWrapped ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_wraps", content.nrWraps ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "layout_paged", content.layoutPaged ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_lines_per_page", content.nrLinesPerPage ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "nr_lines_doc_header", content.nrLinesDocHeader ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "noempty", content.noEmptyLines ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include", content.includeFilename ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "include_field", content.filenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum", content.includeRowNumber ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownumByFile", content.rowNumberByFile ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rownum_field", content.rowNumberField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "format", content.fileFormat ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", content.encoding ) );
    retval.append( "    " + XMLHandler.addTagValue( "add_to_result_filenames", inputFiles.isaddresult ) );

    retval.append( "    <file>" ).append( Const.CR );
    for ( int i = 0; i < inputFiles.fileName.length; i++ ) {
      saveSource( retval, inputFiles.fileName[i] );
      retval.append( "      " ).append( XMLHandler.addTagValue( "filemask", inputFiles.fileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "exclude_filemask", inputFiles.excludeFileMask[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "file_required", inputFiles.fileRequired[i] ) );
      retval.append( "      " ).append( XMLHandler.addTagValue( "include_subfolders",
          inputFiles.includeSubFolders[i] ) );
    }
    retval.append( "      " ).append( XMLHandler.addTagValue( "type", content.fileType ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "compression", ( content.fileCompression == null )
        ? "None" : content.fileCompression ) );
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
      retval.append( "        " ).append( XMLHandler.addTagValue( "filter_position", filter[i].getFilterPosition(),
          false ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "filter_is_last_line", filter[i].isFilterLastLine(),
          false ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "filter_is_positive", filter[i].isFilterPositive(),
          false ) );
      retval.append( "      </filter>" ).append( Const.CR );
    }
    retval.append( "    </filters>" ).append( Const.CR );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < inputFiles.inputFields.length; i++ ) {
      BaseFileInputField field = inputFiles.inputFields[i];

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
    retval.append( "    " ).append( XMLHandler.addTagValue( "limit", content.rowLimit ) );

    // ERROR HANDLING
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_ignored", errorHandling.errorIgnored ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "skip_bad_files", errorHandling.skipBadFiles ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "file_error_field", errorHandling.fileErrorField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "file_error_message_field",
        errorHandling.fileErrorMessageField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_line_skipped", errorLineSkipped ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_count_field", errorCountField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_fields_field", errorFieldsField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_text_field", errorTextField ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "bad_line_files_destination_directory",
        errorHandling.warningFilesDestinationDirectory ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "bad_line_files_extension",
        errorHandling.warningFilesExtension ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_line_files_destination_directory",
        errorHandling.errorFilesDestinationDirectory ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "error_line_files_extension",
        errorHandling.errorFilesExtension ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "line_number_files_destination_directory",
        errorHandling.lineNumberFilesDestinationDirectory ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "line_number_files_extension",
        errorHandling.lineNumberFilesExtension ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "date_format_lenient", content.dateFormatLenient ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "date_format_locale", content.dateFormatLocale != null
        ? content.dateFormatLocale.toString() : null ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "shortFileFieldName",
        additionalOutputFields.shortFilenameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "pathFieldName", additionalOutputFields.pathField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "hiddenFieldName", additionalOutputFields.hiddenField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "lastModificationTimeFieldName",
        additionalOutputFields.lastModificationField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "uriNameFieldName", additionalOutputFields.uriField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rootUriNameFieldName",
        additionalOutputFields.rootUriField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "extensionFieldName",
        additionalOutputFields.extensionField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sizeFieldName", additionalOutputFields.sizeField ) );

    return retval.toString();
  }

  public String getLookupStepname() {
    if ( inputFiles.acceptingFilenames && acceptingStep != null && !Const.isEmpty( acceptingStep.getName() ) ) {
      return acceptingStep.getName();
    }
    return null;
  }

  /**
   * @param steps
   *          optionally search the info step in a list of steps
   */
  public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
    acceptingStep = StepMeta.findStep( steps, inputFiles.acceptingStepName );
  }

  public String[] getInfoSteps() {
    if ( inputFiles.acceptingFilenames && acceptingStep != null ) {
      return new String[] { acceptingStep.getName() };
    }
    return null;
  }

  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      inputFiles.acceptingFilenames = rep.getStepAttributeBoolean( id_step, "accept_filenames" );
      inputFiles.passingThruFields = rep.getStepAttributeBoolean( id_step, "passing_through_fields" );
      inputFiles.acceptingField = rep.getStepAttributeString( id_step, "accept_field" );
      inputFiles.acceptingStepName = rep.getStepAttributeString( id_step, "accept_stepname" );

      content.separator = rep.getStepAttributeString( id_step, "separator" );
      content.enclosure = rep.getStepAttributeString( id_step, "enclosure" );
      content.breakInEnclosureAllowed = rep.getStepAttributeBoolean( id_step, "enclosure_breaks" );
      content.escapeCharacter = rep.getStepAttributeString( id_step, "escapechar" );
      content.header = rep.getStepAttributeBoolean( id_step, "header" );
      content.nrHeaderLines = (int) rep.getStepAttributeInteger( id_step, "nr_headerlines" );
      content.footer = rep.getStepAttributeBoolean( id_step, "footer" );
      content.nrFooterLines = (int) rep.getStepAttributeInteger( id_step, "nr_footerlines" );
      content.lineWrapped = rep.getStepAttributeBoolean( id_step, "line_wrapped" );
      content.nrWraps = (int) rep.getStepAttributeInteger( id_step, "nr_wraps" );
      content.layoutPaged = rep.getStepAttributeBoolean( id_step, "layout_paged" );
      content.nrLinesPerPage = (int) rep.getStepAttributeInteger( id_step, "nr_lines_per_page" );
      content.nrLinesDocHeader = (int) rep.getStepAttributeInteger( id_step, "nr_lines_doc_header" );
      content.noEmptyLines = rep.getStepAttributeBoolean( id_step, "noempty" );

      content.includeFilename = rep.getStepAttributeBoolean( id_step, "include" );
      content.filenameField = rep.getStepAttributeString( id_step, "include_field" );
      content.includeRowNumber = rep.getStepAttributeBoolean( id_step, "rownum" );
      content.rowNumberByFile = rep.getStepAttributeBoolean( id_step, "rownumByFile" );
      content.rowNumberField = rep.getStepAttributeString( id_step, "rownum_field" );

      content.fileFormat = rep.getStepAttributeString( id_step, "format" );
      content.encoding = rep.getStepAttributeString( id_step, "encoding" );
      String addToResult = rep.getStepAttributeString( id_step, "add_to_result_filenames" );
      if ( Const.isEmpty( addToResult ) ) {
        inputFiles.isaddresult = true;
      } else {
        inputFiles.isaddresult = rep.getStepAttributeBoolean( id_step, "add_to_result_filenames" );
      }

      content.rowLimit = rep.getStepAttributeInteger( id_step, "limit" );

      int nrfiles = rep.countNrStepAttributes( id_step, "file_name" );
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );
      int nrfilters = rep.countNrStepAttributes( id_step, "filter_string" );

      allocate( nrfiles, nrfields, nrfilters );

      for ( int i = 0; i < nrfiles; i++ ) {
        inputFiles.fileName[i] = loadSourceRep( rep, id_step, i );
        inputFiles.fileMask[i] = rep.getStepAttributeString( id_step, i, "file_mask" );
        inputFiles.excludeFileMask[i] = rep.getStepAttributeString( id_step, i, "exclude_file_mask" );
        inputFiles.fileRequired[i] = rep.getStepAttributeString( id_step, i, "file_required" );
        if ( !YES.equalsIgnoreCase( inputFiles.fileRequired[i] ) ) {
          inputFiles.fileRequired[i] = NO;
        }
        inputFiles.includeSubFolders[i] = rep.getStepAttributeString( id_step, i, "include_subfolders" );
        if ( !YES.equalsIgnoreCase( inputFiles.includeSubFolders[i] ) ) {
          inputFiles.includeSubFolders[i] = NO;
        }
      }
      content.fileType = rep.getStepAttributeString( id_step, "file_type" );
      content.fileCompression = rep.getStepAttributeString( id_step, "compression" );
      if ( content.fileCompression == null ) {
        content.fileCompression = "None";
        if ( rep.getStepAttributeBoolean( id_step, "file_zipped" ) ) {
          content.fileCompression = "Zip";
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
        BaseFileInputField field = new BaseFileInputField();

        field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        field.setType( ValueMeta.getType( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
        field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
        field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
        field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
        field.setNullString( rep.getStepAttributeString( id_step, i, "field_nullif" ) );
        field.setIfNullValue( rep.getStepAttributeString( id_step, i, "field_ifnull" ) );
        field.setPosition( (int) rep.getStepAttributeInteger( id_step, i, "field_position" ) );
        field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
        field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
        field.setTrimType( ValueMeta.getTrimTypeByCode( rep.getStepAttributeString( id_step, i, "field_trim_type" ) ) );
        field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );

        inputFiles.inputFields[i] = field;
      }

      errorHandling.errorIgnored = rep.getStepAttributeBoolean( id_step, "error_ignored" );
      errorHandling.skipBadFiles = rep.getStepAttributeBoolean( id_step, "skip_bad_files" );
      errorHandling.fileErrorField = rep.getStepAttributeString( id_step, "file_error_field" );
      errorHandling.fileErrorMessageField = rep.getStepAttributeString( id_step, "file_error_message_field" );

      errorLineSkipped = rep.getStepAttributeBoolean( id_step, "error_line_skipped" );
      errorCountField = rep.getStepAttributeString( id_step, "error_count_field" );
      errorFieldsField = rep.getStepAttributeString( id_step, "error_fields_field" );
      errorTextField = rep.getStepAttributeString( id_step, "error_text_field" );

      errorHandling.warningFilesDestinationDirectory = rep.getStepAttributeString( id_step, "bad_line_files_dest_dir" );
      errorHandling.warningFilesExtension = rep.getStepAttributeString( id_step, "bad_line_files_ext" );
      errorHandling.errorFilesDestinationDirectory = rep.getStepAttributeString( id_step, "error_line_files_dest_dir" );
      errorHandling.errorFilesExtension = rep.getStepAttributeString( id_step, "error_line_files_ext" );
      errorHandling.lineNumberFilesDestinationDirectory =
          rep.getStepAttributeString( id_step, "line_number_files_dest_dir" );
      errorHandling.lineNumberFilesExtension = rep.getStepAttributeString( id_step, "line_number_files_ext" );

      content.dateFormatLenient = rep.getStepAttributeBoolean( id_step, 0, "date_format_lenient", true );

      String dateLocale = rep.getStepAttributeString( id_step, 0, "date_format_locale" );
      if ( dateLocale != null ) {
        content.dateFormatLocale = EnvUtil.createLocale( dateLocale );
      } else {
        content.dateFormatLocale = Locale.getDefault();
      }
      additionalOutputFields.shortFilenameField = rep.getStepAttributeString( id_step, "shortFileFieldName" );
      additionalOutputFields.pathField = rep.getStepAttributeString( id_step, "pathFieldName" );
      additionalOutputFields.hiddenField = rep.getStepAttributeString( id_step, "hiddenFieldName" );
      additionalOutputFields.lastModificationField =
          rep.getStepAttributeString( id_step, "lastModificationTimeFieldName" );
      additionalOutputFields.uriField = rep.getStepAttributeString( id_step, "uriNameFieldName" );
      additionalOutputFields.rootUriField = rep.getStepAttributeString( id_step, "rootUriNameFieldName" );
      additionalOutputFields.extensionField = rep.getStepAttributeString( id_step, "extensionFieldName" );
      additionalOutputFields.sizeField = rep.getStepAttributeString( id_step, "sizeFieldName" );
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "accept_filenames", inputFiles.acceptingFilenames );
      rep.saveStepAttribute( id_transformation, id_step, "passing_through_fields", inputFiles.passingThruFields );
      rep.saveStepAttribute( id_transformation, id_step, "accept_field", inputFiles.acceptingField );
      rep.saveStepAttribute( id_transformation, id_step, "accept_stepname", ( acceptingStep != null ? acceptingStep
          .getName() : "" ) );

      rep.saveStepAttribute( id_transformation, id_step, "separator", content.separator );
      rep.saveStepAttribute( id_transformation, id_step, "enclosure", content.enclosure );
      rep.saveStepAttribute( id_transformation, id_step, "enclosure_breaks", content.breakInEnclosureAllowed );
      rep.saveStepAttribute( id_transformation, id_step, "escapechar", content.escapeCharacter );
      rep.saveStepAttribute( id_transformation, id_step, "header", content.header );
      rep.saveStepAttribute( id_transformation, id_step, "nr_headerlines", content.nrHeaderLines );
      rep.saveStepAttribute( id_transformation, id_step, "footer", content.footer );
      rep.saveStepAttribute( id_transformation, id_step, "nr_footerlines", content.nrFooterLines );
      rep.saveStepAttribute( id_transformation, id_step, "line_wrapped", content.lineWrapped );
      rep.saveStepAttribute( id_transformation, id_step, "nr_wraps", content.nrWraps );
      rep.saveStepAttribute( id_transformation, id_step, "layout_paged", content.layoutPaged );
      rep.saveStepAttribute( id_transformation, id_step, "nr_lines_per_page", content.nrLinesPerPage );
      rep.saveStepAttribute( id_transformation, id_step, "nr_lines_doc_header", content.nrLinesDocHeader );

      rep.saveStepAttribute( id_transformation, id_step, "noempty", content.noEmptyLines );

      rep.saveStepAttribute( id_transformation, id_step, "include", content.includeFilename );
      rep.saveStepAttribute( id_transformation, id_step, "include_field", content.filenameField );
      rep.saveStepAttribute( id_transformation, id_step, "rownum", content.includeRowNumber );
      rep.saveStepAttribute( id_transformation, id_step, "rownumByFile", content.rowNumberByFile );
      rep.saveStepAttribute( id_transformation, id_step, "rownum_field", content.rowNumberField );

      rep.saveStepAttribute( id_transformation, id_step, "format", content.fileFormat );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", content.encoding );
      rep.saveStepAttribute( id_transformation, id_step, "add_to_result_filenames", inputFiles.isaddresult );

      rep.saveStepAttribute( id_transformation, id_step, "limit", content.rowLimit );

      for ( int i = 0; i < inputFiles.fileName.length; i++ ) {
        saveSourceRep( rep, id_transformation, id_step, i, inputFiles.fileName[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_mask", inputFiles.fileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "exclude_file_mask", inputFiles.excludeFileMask[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "file_required", inputFiles.fileRequired[i] );
        rep.saveStepAttribute( id_transformation, id_step, i, "include_subfolders", inputFiles.includeSubFolders[i] );
      }
      rep.saveStepAttribute( id_transformation, id_step, "file_type", content.fileType );
      rep.saveStepAttribute( id_transformation, id_step, "compression", ( content.fileCompression == null ) ? "None"
          : content.fileCompression );

      for ( int i = 0; i < filter.length; i++ ) {
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_position", filter[i].getFilterPosition() );
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_string", filter[i].getFilterString() );
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_is_last_line", filter[i].isFilterLastLine() );
        rep.saveStepAttribute( id_transformation, id_step, i, "filter_is_positive", filter[i].isFilterPositive() );
      }

      for ( int i = 0; i < inputFiles.inputFields.length; i++ ) {
        BaseFileInputField field = inputFiles.inputFields[i];

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

      rep.saveStepAttribute( id_transformation, id_step, "error_ignored", errorHandling.errorIgnored );
      rep.saveStepAttribute( id_transformation, id_step, "skip_bad_files", errorHandling.skipBadFiles );
      rep.saveStepAttribute( id_transformation, id_step, "file_error_field", errorHandling.fileErrorField );
      rep.saveStepAttribute( id_transformation, id_step, "file_error_message_field",
          errorHandling.fileErrorMessageField );
      rep.saveStepAttribute( id_transformation, id_step, "error_line_skipped", errorLineSkipped );
      rep.saveStepAttribute( id_transformation, id_step, "error_count_field", errorCountField );
      rep.saveStepAttribute( id_transformation, id_step, "error_fields_field", errorFieldsField );
      rep.saveStepAttribute( id_transformation, id_step, "error_text_field", errorTextField );

      rep.saveStepAttribute( id_transformation, id_step, "bad_line_files_dest_dir",
          errorHandling.warningFilesDestinationDirectory );
      rep.saveStepAttribute( id_transformation, id_step, "bad_line_files_ext", errorHandling.warningFilesExtension );
      rep.saveStepAttribute( id_transformation, id_step, "error_line_files_dest_dir",
          errorHandling.errorFilesDestinationDirectory );
      rep.saveStepAttribute( id_transformation, id_step, "error_line_files_ext", errorHandling.errorFilesExtension );
      rep.saveStepAttribute( id_transformation, id_step, "line_number_files_dest_dir",
          errorHandling.lineNumberFilesDestinationDirectory );
      rep.saveStepAttribute( id_transformation, id_step, "line_number_files_ext",
          errorHandling.lineNumberFilesExtension );

      rep.saveStepAttribute( id_transformation, id_step, "date_format_lenient", content.dateFormatLenient );
      rep.saveStepAttribute( id_transformation, id_step, "date_format_locale", content.dateFormatLocale != null
          ? content.dateFormatLocale.toString() : null );

      rep.saveStepAttribute( id_transformation, id_step, "shortFileFieldName",
          additionalOutputFields.shortFilenameField );
      rep.saveStepAttribute( id_transformation, id_step, "pathFieldName", additionalOutputFields.pathField );
      rep.saveStepAttribute( id_transformation, id_step, "hiddenFieldName", additionalOutputFields.hiddenField );
      rep.saveStepAttribute( id_transformation, id_step, "lastModificationTimeFieldName",
          additionalOutputFields.lastModificationField );
      rep.saveStepAttribute( id_transformation, id_step, "uriNameFieldName", additionalOutputFields.uriField );
      rep.saveStepAttribute( id_transformation, id_step, "rootUriNameFieldName", additionalOutputFields.rootUriField );
      rep.saveStepAttribute( id_transformation, id_step, "extensionFieldName", additionalOutputFields.extensionField );
      rep.saveStepAttribute( id_transformation, id_step, "sizeFieldName", additionalOutputFields.sizeField );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    // See if we get input...
    if ( input.length > 0 ) {
      if ( !inputFiles.acceptingFilenames ) {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "TextFileInputMeta.CheckResult.NoInputError" ), stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "TextFileInputMeta.CheckResult.AcceptFilenamesOk" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "TextFileInputMeta.CheckResult.NoInputOk" ), stepMeta );
      remarks.add( cr );
    }

    FileInputList textFileList = getFileInputList( transMeta );
    if ( textFileList.nrOfFiles() == 0 ) {
      if ( !inputFiles.acceptingFilenames ) {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                "TextFileInputMeta.CheckResult.ExpectedFilesError" ), stepMeta );
        remarks.add( cr );
      }
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "TextFileInputMeta.CheckResult.ExpectedFilesOk", "" + textFileList.nrOfFiles() ), stepMeta );
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new TextFileInput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new TextFileInputData();
  }

  public String getErrorCountField() {
    return errorCountField;
  }

  public void setErrorCountField( String errorCountField ) {
    this.errorCountField = errorCountField;
  }

  public String getErrorFieldsField() {
    return errorFieldsField;
  }

  public void setErrorFieldsField( String errorFieldsField ) {
    this.errorFieldsField = errorFieldsField;
  }

  public String getErrorTextField() {
    return errorTextField;
  }

  public void setErrorTextField( String errorTextField ) {
    this.errorTextField = errorTextField;
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

  public boolean isErrorLineSkipped() {
    return errorLineSkipped;
  }

  public void setErrorLineSkipped( boolean errorLineSkipped ) {
    this.errorLineSkipped = errorLineSkipped;
  }

  /**
   * @param acceptingStep
   *          The acceptingStep to set.
   */
  public void setAcceptingStep( StepMeta acceptingStep ) {
    this.acceptingStep = acceptingStep;
  }

  public int getFileFormatTypeNr() {
    // calculate the file format type in advance so we can use a switch
    if ( content.fileFormat.equalsIgnoreCase( "DOS" ) ) {
      return FILE_FORMAT_DOS;
    } else if ( content.fileFormat.equalsIgnoreCase( "unix" ) ) {
      return TextFileInputMeta.FILE_FORMAT_UNIX;
    } else {
      return TextFileInputMeta.FILE_FORMAT_MIXED;
    }
  }

  public int getFileTypeNr() {
    // calculate the file type in advance CSV or Fixed?
    if ( content.fileType.equalsIgnoreCase( "CSV" ) ) {
      return TextFileInputMeta.FILE_TYPE_CSV;
    } else {
      return TextFileInputMeta.FILE_TYPE_FIXED;
    }
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
      if ( !inputFiles.acceptingFilenames ) {

        // Replace the filename ONLY (folder or filename)
        //
        for ( int i = 0; i < inputFiles.fileName.length; i++ ) {
          FileObject fileObject =
              KettleVFS.getFileObject( space.environmentSubstitute( inputFiles.fileName[i] ), space );
          inputFiles.fileName[i] =
              resourceNamingInterface.nameResource( fileObject, space, Const.isEmpty( inputFiles.fileMask[i] ) );
        }
      }
      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  public boolean supportsErrorHandling() {
    return errorHandling.errorIgnored && errorHandling.skipBadFiles;
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

  protected String loadSource( Node filenode, Node filenamenode, int i ) {
    return XMLHandler.getNodeValue( filenamenode );
  }

  protected void saveSource( StringBuffer retVal, String source ) {
    retVal.append( "      " ).append( XMLHandler.addTagValue( "name", source ) );
  }

  protected String loadSourceRep( Repository rep, ObjectId id_step, int i ) throws KettleException {
    return rep.getStepAttributeString( id_step, i, "file_name" );
  }

  protected void saveSourceRep( Repository rep, ObjectId id_transformation, ObjectId id_step, int i, String fileName )
    throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, i, "file_name", fileName ); // this should be in subclass
  }

  @Override
  public String getEncoding() {
    return content.encoding;
  }

  /**
   * Required for the Data Lineage.
   */
  public boolean isAcceptingFilenames() {
    return inputFiles.acceptingFilenames;
  }

  /**
   * Required for the Data Lineage.
   */
  public String getAcceptingStepName() {
    return inputFiles.acceptingStepName;
  }

  /**
   * Required for the Data Lineage.
   */
  public StepMeta getAcceptingStep() {
    return acceptingStep;
  }

  /**
   * Required for the Data Lineage.
   */
  public String getAcceptingField() {
    return inputFiles.acceptingField;
  }
}
