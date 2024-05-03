/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.textfileoutput;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.AliasedFileObject;
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
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.file.BaseFileOutputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * Created on 4-apr-2003
 *
 */
@InjectionSupported( localizationPrefix = "TextFileOutput.Injection.", groups = { "OUTPUT_FIELDS" } )
public class TextFileOutputMeta extends BaseFileOutputMeta implements StepMetaInterface {
  private static Class<?> PKG = TextFileOutputMeta.class; // for i18n purposes, needed by Translator2!!

  // Strings used in XML
  private static final String CONST_STRING_ADD_DATE = "add_date";
  private static final String CONST_STRING_ADD_TIME = "add_time";
  private static final String CONST_STRING_ADD_TO_RESULT_FILENAMES = "add_to_result_filenames";
  private static final String CONST_STRING_APPEND = "append";
  private static final String CONST_STRING_COMPRESSION = "compression";
  private static final String CONST_STRING_CREATE_PARENT_FOLDER = "create_parent_folder";
  private static final String CONST_STRING_CURRENCY = "currency";
  private static final String CONST_STRING_DATE_TIME_FORMAT = "date_time_format";
  private static final String CONST_STRING_DECIMAL = "decimal";
  private static final String CONST_STRING_DO_NOT_OPEN_NEW_FILE_INIT = "do_not_open_new_file_init";
  private static final String CONST_STRING_ENCLOSURE = "enclosure";
  private static final String CONST_STRING_ENCLOSURE_FIX_DISABLED = "enclosure_fix_disabled";
  private static final String CONST_STRING_ENCLOSURE_FORCED = "enclosure_forced";
  private static final String CONST_STRING_ENCODING = "encoding";
  private static final String CONST_STRING_ENDED_LINE = "endedLine";
  private static final String CONST_STRING_EXTENTION = "extention";
  private static final String CONST_STRING_FAST_DUMP = "fast_dump";
  private static final String CONST_STRING_FIELD = "field";
  private static final String CONST_STRING_FIELD_CURRENCY = "field_currency";
  private static final String CONST_STRING_FIELD_DECIMAL = "field_decimal";
  private static final String CONST_STRING_FIELD_FORMAT = "field_format";
  private static final String CONST_STRING_FIELD_GROUP = "field_group";
  private static final String CONST_STRING_FIELD_LENGTH = "field_length";
  private static final String CONST_STRING_FIELD_NAME = "field_name";
  private static final String CONST_STRING_FIELD_NULLIF = "field_nullif";
  private static final String CONST_STRING_FIELD_PRECISION = "field_precision";
  private static final String CONST_STRING_FIELD_TRIM_TYPE = "field_trim_type";
  private static final String CONST_STRING_FIELD_TYPE = "field_type";
  private static final String CONST_STRING_FIELDS = "fields";
  private static final String CONST_STRING_FILE = "file";
  private static final String CONST_STRING_FILE_ADD_DATE = "file_add_date";
  private static final String CONST_STRING_FILE_ADD_PARTNR = "file_add_partnr";
  private static final String CONST_STRING_FILE_ADD_STEPNR = "file_add_stepnr";
  private static final String CONST_STRING_FILE_ADD_TIME = "file_add_time";
  private static final String CONST_STRING_FILE_APPEND = "file_append";
  private static final String CONST_STRING_FILE_EXTENTION = "file_extention";
  private static final String CONST_STRING_FILE_FAST_DUMP = "file_fast_dump";
  private static final String CONST_STRING_FILE_NAME = "file_name";
  private static final String CONST_STRING_FILE_NAME_FIELD = "fileNameField";
  private static final String CONST_STRING_FILE_NAME_IN_FIELD = "fileNameInField";
  private static final String CONST_STRING_FILE_PAD = "file_pad";
  private static final String CONST_STRING_FILE_SERVLET_OUTPUT = "file_servlet_output";
  private static final String CONST_STRING_FILE_SPLIT_ROWS = "file_split_rows";
  private static final String CONST_STRING_FOOTER = "footer";
  private static final String CONST_STRING_FORMAT = "format";
  private static final String CONST_STRING_GROUP = "group";
  private static final String CONST_STRING_HASPARTNO = "haspartno";
  private static final String CONST_STRING_HEADER = "header";
  private static final String CONST_STRING_LENGTH = "length";
  private static final String CONST_STRING_NAME = "name";
  private static final String CONST_STRING_NULLIF = "nullif";
  private static final String CONST_STRING_PAD = "pad";
  private static final String CONST_STRING_PRECISION = "precision";
  private static final String CONST_STRING_SEPARATOR = "separator";
  private static final String CONST_STRING_SERVLET_OUTPUT = "servlet_output";
  private static final String CONST_STRING_SPECIFY_FORMAT = "SpecifyFormat";
  private static final String CONST_STRING_SPLIT = "split";
  private static final String CONST_STRING_SPLITEVERY = "splitevery";
  private static final String CONST_STRING_TRIM_TYPE = "trim_type";
  private static final String CONST_STRING_TYPE = "type";
  private static final String CONST_STRING_ZIPPED = "zipped";

  private static final String CONST_STRING_CR = "CR";
  private static final String CONST_STRING_DOS = "DOS";
  private static final String CONST_STRING_FILE_SPLIT = "file_split";
  private static final String CONST_STRING_NONE = "None";
  private static final String CONST_STRING_TXT = "txt";
  private static final String CONST_STRING_UNIX = "UNIX";
  private static final String CONST_STRING_ZIP = "Zip";

  private static final String NEW_LINE_CR_FORMAT = "\r";
  private static final String NEW_LINE_DOS_FORMAT = "\r\n";
  private static final String NEW_LINE_UNIX_FORMAT = "\n";

  protected static final int FILE_COMPRESSION_TYPE_NONE = 0;
  protected static final int FILE_COMPRESSION_TYPE_ZIP = 1;

  protected static final String[] fileCompressionTypeCodes = new String[] { CONST_STRING_NONE, CONST_STRING_ZIP };

  public static final String[] formatMapperLineTerminator = new String[] { CONST_STRING_DOS, CONST_STRING_UNIX,
    CONST_STRING_CR, CONST_STRING_NONE };

  /** Whether to push the output into the output of a servlet with the executeTrans Carte/DI-Server servlet */
  @Injection( name = "PASS_TO_SERVLET" )
  private boolean servletOutput;

  /** Flag: create parent folder, default to true */
  @Injection( name = "CREATE_PARENT_FOLDER" )
  private boolean createparentfolder = true;

  /** The separator to choose for the CSV file */
  @Injection( name = "SEPARATOR" )
  private String separator;

  /** The enclosure to use in case the separator is part of a field's value */
  @Injection( name = "ENCLOSURE" )
  private String enclosure;

  /** Setting to allow the enclosure to be always surrounding a String value, even when there is no separator inside */
  @Injection( name = "FORCE_ENCLOSURE" )
  private boolean enclosureForced;

  /**
   * Setting to allow for backwards compatibility where the enclosure did not show up at all if Force Enclosure was not
   * checked
   */
  @Injection( name = "DISABLE_ENCLOSURE_FIX" )
  private boolean disableEnclosureFix;

  /** Add a header at the top of the file? */
  @Injection( name = "HEADER" )
  private boolean headerEnabled;

  /** Add a footer at the bottom of the file? */
  @Injection( name = "FOOTER" )
  private boolean footerEnabled;

  /**
   * The file format: DOS or Unix
   * It could be injected using the key "FORMAT"
   * see the setter {@link TextFileOutputMeta#setFileFormat(java.lang.String)}.
   */
  private String fileFormat;

  /** if this value is larger then 0, the text file is split up into parts of this number of lines */
  @Injection( name = "SPLIT_EVERY" )
  private String splitEveryRows;

  /** Flag to indicate the we want to append to the end of an existing file (if it exists) */
  @Injection( name = "APPEND" )
  private boolean fileAppended;

  /** Flag: pad fields to their specified length */
  @Injection( name = "RIGHT_PAD_FIELDS" )
  private boolean padded;

  /** Flag: Fast dump data without field formatting */
  @Injection( name = "FAST_DATA_DUMP" )
  private boolean fastDump;

  /* THE FIELD SPECIFICATIONS ... */

  /** The output fields */
  @InjectionDeep
  private TextFileField[] outputFields;

  /** The encoding to use for reading: null or empty string means system default encoding */
  @Injection( name = "ENCODING" )
  private String encoding;

  /** The string to use for append to end line of the whole file: null or empty string means no line needed */
  @Injection( name = "ADD_ENDING_LINE" )
  private String endedLine;

  /* Specification if file name is in field */

  @Injection( name = "FILENAME_IN_FIELD" )
  private boolean fileNameInField;

  @Injection( name = "FILENAME_FIELD" )
  private String fileNameField;

  /** Calculated value ... */
  @Injection( name = "NEW_LINE", converter = NewLineCharacterConverter.class )
  private String newline;

  /** Flag: add the filenames to result filenames */
  @Injection( name = "ADD_TO_RESULT" )
  private boolean addToResultFilenames;

  /** Flag : Do not open new file when transformation start */
  @Injection( name = "DO_NOT_CREATE_FILE_AT_STARTUP" )
  private boolean doNotOpenNewFileInit;

  protected ValueMetaInterface[] metaWithFieldOptions = null;

  public TextFileOutputMeta() {
    super(); // allocate BaseStepMeta
  }

  public boolean isServletOutput() {
    return servletOutput;
  }

  public void setServletOutput( boolean servletOutput ) {
    this.servletOutput = servletOutput;
  }

  /**
   * @param createparentfolder
   *          The createparentfolder to set.
   */
  public void setCreateParentFolder( boolean createparentfolder ) {
    this.createparentfolder = createparentfolder;
  }

  /**
   * @return Returns the createparentfolder.
   */
  public boolean isCreateParentFolder() {
    return createparentfolder;
  }

  /**
   * @param dateInFilename
   *          The dateInFilename to set.
   */
  public void setDateInFilename( boolean dateInFilename ) {
    this.dateInFilename = dateInFilename;
  }

  /**
   * @return Returns the enclosure.
   */
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
   * @return Returns the enclosureForced.
   */
  public boolean isEnclosureForced() {
    return enclosureForced;
  }

  /**
   * @param enclosureForced
   *          The enclosureForced to set.
   */
  public void setEnclosureForced( boolean enclosureForced ) {
    this.enclosureForced = enclosureForced;
  }

  /**
   * @return Returns the enclosureFixDisabled.
   */
  public boolean isEnclosureFixDisabled() {
    return disableEnclosureFix;
  }

  /**
   * @param disableEnclosureFix
   *          The enclosureFixDisabled to set.
   */
  public void setEnclosureFixDisabled( boolean disableEnclosureFix ) {
    this.disableEnclosureFix = disableEnclosureFix;
  }

  /**
   * @return Returns the add to result filesname.
   */
  public boolean isAddToResultFiles() {
    return addToResultFilenames;
  }

  /**
   * @param addtoresultfilenamesin
   *          The addtoresultfilenames to set.
   */
  public void setAddToResultFiles( boolean addtoresultfilenamesin ) {
    this.addToResultFilenames = addtoresultfilenamesin;
  }

  /**
   * @return Returns the fileAppended.
   */
  public boolean isFileAppended() {
    return fileAppended;
  }

  /**
   * @param fileAppended
   *          The fileAppended to set.
   */
  public void setFileAppended( boolean fileAppended ) {
    this.fileAppended = fileAppended;
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
  @Injection( name = "FORMAT" )
  public void setFileFormat( String fileFormat ) {
    this.fileFormat = fileFormat;
    this.newline = getNewLine( fileFormat );
  }

  /**
   * @return Returns the footer.
   */
  public boolean isFooterEnabled() {
    return footerEnabled;
  }

  /**
   * @param footer
   *          The footer to set.
   */
  public void setFooterEnabled( boolean footer ) {
    this.footerEnabled = footer;
  }

  /**
   * @return Returns the header.
   */
  public boolean isHeaderEnabled() {
    return headerEnabled;
  }

  /**
   * @param header
   *          The header to set.
   */
  public void setHeaderEnabled( boolean header ) {
    this.headerEnabled = header;
  }

  /**
   * @return Returns the newline.
   */
  public String getNewline() {
    return newline;
  }

  /**
   * @param newline
   *          The newline to set.
   */
  public void setNewline( String newline ) {
    this.newline = newline;
  }

  /**
   * @return Returns the padded.
   */
  public boolean isPadded() {
    return padded;
  }

  /**
   * @param padded
   *          The padded to set.
   */
  public void setPadded( boolean padded ) {
    this.padded = padded;
  }

  /**
   * @return Returns the fastDump.
   */
  public boolean isFastDump() {
    return fastDump;
  }

  /**
   * @param fastDump
   *          The fastDump to set.
   */
  public void setFastDump( boolean fastDump ) {
    this.fastDump = fastDump;
  }

  /**
   * @return Returns the separator.
   */
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
   * @return Returns the "do not open new file at init" flag.
   */
  public boolean isDoNotOpenNewFileInit() {
    return doNotOpenNewFileInit;
  }

  /**
   * @param doNotOpenNewFileInit
   *          The "do not open new file at init" flag to set.
   */
  public void setDoNotOpenNewFileInit( boolean doNotOpenNewFileInit ) {
    this.doNotOpenNewFileInit = doNotOpenNewFileInit;
  }

  /**
   * @deprecated use {@link #getSplitEvery(VariableSpace)} or {@link #getSplitEveryRows()}
   * @return Returns the splitEvery.
   */
  @Override
  public int getSplitEvery() {
    return Const.toInt( splitEveryRows, 0 );
  }

  /**
   * @param varSpace for variable substitution
   * @return At how many rows to split into another file.
   */
  @Override
  public int getSplitEvery( VariableSpace varSpace ) {
    return Const.toInt( varSpace == null ? splitEveryRows : varSpace.environmentSubstitute( splitEveryRows ), 0 );
  }

  /**
   * @return At how many rows to split into a new file.
   */
  public String getSplitEveryRows() {
    return splitEveryRows;
  }

  /**
   * @param value At how many rows to split into a new file.
   */
  public void setSplitEveryRows( String value ) {
    splitEveryRows = value;
  }

  /**
   * @return <tt>1</tt> if <tt>isFooterEnabled()</tt> and <tt>0</tt> otherwise
   */
  public int getFooterShift() {
    return isFooterEnabled() ? 1 : 0;
  }

  /**
   * @deprecated use {@link #setSplitEveryRows(String)}
   * @param splitEvery
   *          The splitEvery to set.
   */
  public void setSplitEvery( int splitEvery ) {
    splitEveryRows = Integer.toString( splitEvery );
  }

  /**
   * @param stepNrInFilename
   *          The stepNrInFilename to set.
   */
  public void setStepNrInFilename( boolean stepNrInFilename ) {
    this.stepNrInFilename = stepNrInFilename;
  }

  /**
   * @param partNrInFilename
   *          The partNrInFilename to set.
   */
  public void setPartNrInFilename( boolean partNrInFilename ) {
    this.partNrInFilename = partNrInFilename;
  }

  /**
   * @param timeInFilename
   *          The timeInFilename to set.
   */
  public void setTimeInFilename( boolean timeInFilename ) {
    this.timeInFilename = timeInFilename;
  }

  /**
   * @return Returns the outputFields.
   */
  public TextFileField[] getOutputFields() {
    return outputFields;
  }

  /**
   * @param outputFields
   *          The outputFields to set.
   */
  public void setOutputFields( TextFileField[] outputFields ) {
    this.outputFields = outputFields;
  }

  /**
   * @return The desired encoding of output file, null or empty if the default system encoding needs to be used.
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @param encoding
   *          The desired encoding of output file, null or empty if the default system encoding needs to be used.
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @return The desired last line in the output file, null or empty if nothing has to be added.
   */
  public String getEndedLine() {
    return endedLine;
  }

  /**
   * @param endedLine
   *          The desired last line in the output file, null or empty if nothing has to be added.
   */
  public void setEndedLine( String endedLine ) {
    this.endedLine = endedLine;
  }

  /**
   * @return Is the file name coded in a field?
   */
  public boolean isFileNameInField() {
    return fileNameInField;
  }

  /**
   * @param fileNameInField
   *          Is the file name coded in a field?
   */
  public void setFileNameInField( boolean fileNameInField ) {
    this.fileNameInField = fileNameInField;
  }

  /**
   * @return The field name that contains the output file name.
   */
  public String getFileNameField() {
    return fileNameField;
  }

  /**
   * @param fileNameField
   *          Name of the field that contains the file name
   */
  public void setFileNameField( String fileNameField ) {
    this.fileNameField = fileNameField;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode, metaStore );
  }

  public void allocate( int nrfields ) {
    outputFields = new TextFileField[nrfields];
  }

  @Override
  public Object clone() {
    TextFileOutputMeta retval = (TextFileOutputMeta) super.clone();
    int nrFields = outputFields.length;

    retval.allocate( nrFields );

    for ( int i = 0; i < nrFields; i++ ) {
      retval.outputFields[ i ] = (TextFileField) outputFields[ i ].clone();
    }

    if ( null != metaWithFieldOptions ) {
      int nrMetas = metaWithFieldOptions.length;

      retval.metaWithFieldOptions = new ValueMetaInterface[ nrMetas ];
      for ( int i = 0; i < nrMetas; i++ ) {
        retval.metaWithFieldOptions[ i ] = metaWithFieldOptions[ i ].clone();
      }
    } else {
      retval.metaWithFieldOptions = null;
    }

    return retval;
  }

  protected void readData( Node stepnode, IMetaStore metastore ) throws KettleXMLException {
    try {
      separator = Const.NVL( XMLHandler.getTagValue( stepnode, CONST_STRING_SEPARATOR ), Const.EMPTY_STRING );

      enclosure = Const.NVL( XMLHandler.getTagValue( stepnode, CONST_STRING_ENCLOSURE ), Const.EMPTY_STRING );

      enclosureForced = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_ENCLOSURE_FORCED ) );

      String sDisableEnclosureFix = XMLHandler.getTagValue( stepnode, CONST_STRING_ENCLOSURE_FIX_DISABLED );
      // Default this value to true for backwards compatibility
      disableEnclosureFix = ( sDisableEnclosureFix == null ) || "Y".equalsIgnoreCase( sDisableEnclosureFix );

      // Default createparentfolder to true if the tag is missing
      String createParentFolderTagValue = XMLHandler.getTagValue( stepnode, CONST_STRING_CREATE_PARENT_FOLDER );
      createparentfolder = ( createParentFolderTagValue == null ) || "Y".equalsIgnoreCase( createParentFolderTagValue );

      headerEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_HEADER ) );
      footerEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FOOTER ) );
      fileFormat = XMLHandler.getTagValue( stepnode, CONST_STRING_FORMAT );
      setFileCompression( XMLHandler.getTagValue( stepnode, CONST_STRING_COMPRESSION ) );
      if ( getFileCompression() == null ) {
        if ( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_ZIPPED ) ) ) {
          setFileCompression(  fileCompressionTypeCodes[FILE_COMPRESSION_TYPE_ZIP] );
        } else {
          setFileCompression( fileCompressionTypeCodes[FILE_COMPRESSION_TYPE_NONE] );
        }
      }

      encoding = XMLHandler.getTagValue( stepnode, CONST_STRING_ENCODING );

      endedLine = Const.NVL( XMLHandler.getTagValue( stepnode, CONST_STRING_ENDED_LINE ), Const.EMPTY_STRING );

      fileName = loadSource( stepnode, metastore );
      servletOutput = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE,
        CONST_STRING_SERVLET_OUTPUT ) );
      doNotOpenNewFileInit =
          "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_DO_NOT_OPEN_NEW_FILE_INIT ) );
      extension = XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_EXTENTION );
      fileAppended = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_APPEND ) );
      stepNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_SPLIT ) );
      partNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_HASPARTNO ) );
      dateInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_ADD_DATE ) );
      timeInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_ADD_TIME ) );
      setSpecifyingFormat( "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_SPECIFY_FORMAT ) ) );
      setDateTimeFormat( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_DATE_TIME_FORMAT ) );

      String addToResultFiles = XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_ADD_TO_RESULT_FILENAMES );
      addToResultFilenames = Utils.isEmpty( addToResultFiles ) || "Y".equalsIgnoreCase( addToResultFiles );

      padded = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_PAD ) );
      fastDump = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_FAST_DUMP ) );
      splitEveryRows = XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_SPLITEVERY );

      newline = getNewLine( fileFormat );

      fileNameInField = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, CONST_STRING_FILE_NAME_IN_FIELD ) );
      fileNameField = XMLHandler.getTagValue( stepnode, CONST_STRING_FILE_NAME_FIELD );

      Node fields = XMLHandler.getSubNode( stepnode, CONST_STRING_FIELDS );
      int nrfields = XMLHandler.countNodes( fields, CONST_STRING_FIELD );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, CONST_STRING_FIELD, i );

        outputFields[i] = new TextFileField();
        outputFields[i].setName( XMLHandler.getTagValue( fnode, CONST_STRING_NAME ) );
        outputFields[i].setType( XMLHandler.getTagValue( fnode, CONST_STRING_TYPE ) );
        outputFields[i].setFormat( XMLHandler.getTagValue( fnode, CONST_STRING_FORMAT ) );
        outputFields[i].setCurrencySymbol( XMLHandler.getTagValue( fnode, CONST_STRING_CURRENCY ) );
        outputFields[i].setDecimalSymbol( XMLHandler.getTagValue( fnode, CONST_STRING_DECIMAL ) );
        outputFields[i].setGroupingSymbol( XMLHandler.getTagValue( fnode, CONST_STRING_GROUP ) );
        outputFields[i].setTrimType( ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue( fnode,
          CONST_STRING_TRIM_TYPE ) ) );
        outputFields[i].setNullString( XMLHandler.getTagValue( fnode, CONST_STRING_NULLIF ) );
        outputFields[i].setLength( Const.toInt( XMLHandler.getTagValue( fnode, CONST_STRING_LENGTH ), -1 ) );
        outputFields[i].setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, CONST_STRING_PRECISION ), -1 ) );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  public void readData( Node stepnode ) throws KettleXMLException {
    readData( stepnode, null );
  }

  public String getNewLine( String fformat ) {
    String nl = Const.CR;

    if ( fformat != null ) {
      if ( fformat.equalsIgnoreCase( CONST_STRING_DOS ) ) {
        nl = NEW_LINE_DOS_FORMAT;
      } else if ( fformat.equalsIgnoreCase( CONST_STRING_UNIX ) ) {
        nl = NEW_LINE_UNIX_FORMAT;
      } else if ( fformat.equalsIgnoreCase( CONST_STRING_CR ) ) {
        nl = NEW_LINE_CR_FORMAT;
      } else if ( fformat.equalsIgnoreCase( CONST_STRING_NONE ) ) {
        nl = Const.EMPTY_STRING;
      }
    }

    return nl;
  }

  @Override
  public void setDefault() {
    createparentfolder = true; // Default createparentfolder to true
    separator = ";";
    enclosure = "\"";
    setSpecifyingFormat( false );
    setDateTimeFormat( null );
    enclosureForced = false;
    disableEnclosureFix = false;
    headerEnabled = true;
    footerEnabled = false;
    fileFormat = CONST_STRING_DOS;
    setFileCompression( fileCompressionTypeCodes[FILE_COMPRESSION_TYPE_NONE] );
    fileName = CONST_STRING_FILE;
    servletOutput = false;
    doNotOpenNewFileInit = false;
    extension = CONST_STRING_TXT;
    stepNrInFilename = false;
    partNrInFilename = false;
    dateInFilename = false;
    timeInFilename = false;
    padded = false;
    fastDump = false;
    addToResultFilenames = true;
    fileAppended = false;

    newline = getNewLine( fileFormat );

    allocate( 0 );
    metaWithFieldOptions = null;
  }

  public String buildFilename( VariableSpace space, int stepnr, String partnr, int splitnr, boolean ziparchive ) {
    return super.buildFilename( space, Integer.toString( stepnr ), partnr, Integer.toString( splitnr ), ziparchive );
  }

  public String buildFilename( String filename, String extension, VariableSpace space, int stepnr, String partnr,
                               int splitnr, boolean ziparchive, TextFileOutputMeta meta ) {

    final String realFileName = space.environmentSubstitute( filename );
    final String realExtension = space.environmentSubstitute( extension );
    return super.buildFilename( space, realFileName, realExtension, Integer.toString( stepnr ), partnr, Integer
      .toString( splitnr ), new Date(), ziparchive, true, meta );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 800 );

    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_SEPARATOR, separator ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_ENCLOSURE, enclosure ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_ENCLOSURE_FORCED, enclosureForced ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_ENCLOSURE_FIX_DISABLED, disableEnclosureFix ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_HEADER, headerEnabled ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_FOOTER, footerEnabled ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_FORMAT, fileFormat ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_COMPRESSION, getFileCompression() ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_ENCODING, encoding ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_ENDED_LINE, endedLine ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_FILE_NAME_IN_FIELD, fileNameInField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_FILE_NAME_FIELD, fileNameField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( CONST_STRING_CREATE_PARENT_FOLDER, createparentfolder ) );
    retval.append( "    <file>" ).append( Const.CR );
    saveFileOptions( retval );
    retval.append( "    </file>" ).append( Const.CR );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( TextFileField field : outputFields ) {
      if ( !Utils.isEmpty( field.getName() ) ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_NAME, field.getName() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_TYPE, field.getTypeDesc() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_FORMAT, field.getFormat() ) );
        retval.append( "        " )
          .append( XMLHandler.addTagValue( CONST_STRING_CURRENCY, field.getCurrencySymbol() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_DECIMAL, field.getDecimalSymbol() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_GROUP, field.getGroupingSymbol() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_NULLIF, field.getNullString() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_TRIM_TYPE, field.getTrimTypeCode() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_LENGTH, field.getLength() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( CONST_STRING_PRECISION, field.getPrecision() ) );
        retval.append( "      </field>" ).append( Const.CR );
      }
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  protected void saveFileOptions( StringBuilder retval ) {
    if ( parentStepMeta != null && parentStepMeta.getParentTransMeta() != null ) {
      parentStepMeta.getParentTransMeta().getNamedClusterEmbedManager().registerUrl( fileName );
    }
    saveSource( retval, fileName );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_SERVLET_OUTPUT, servletOutput ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_DO_NOT_OPEN_NEW_FILE_INIT, doNotOpenNewFileInit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_EXTENTION, extension ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_APPEND, fileAppended ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_SPLIT, stepNrInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_HASPARTNO, partNrInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_ADD_DATE, dateInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_ADD_TIME, timeInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_SPECIFY_FORMAT, isSpecifyingFormat() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_DATE_TIME_FORMAT, getDateTimeFormat() ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_ADD_TO_RESULT_FILENAMES, addToResultFilenames ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_PAD, padded ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_FAST_DUMP, fastDump ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_SPLITEVERY, splitEveryRows ) );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      separator = rep.getStepAttributeString( id_step, CONST_STRING_SEPARATOR );
      enclosure = rep.getStepAttributeString( id_step, CONST_STRING_ENCLOSURE );
      enclosureForced = rep.getStepAttributeBoolean( id_step, CONST_STRING_ENCLOSURE_FORCED );
      disableEnclosureFix = rep.getStepAttributeBoolean( id_step, 0, CONST_STRING_ENCLOSURE_FIX_DISABLED, true );
      createparentfolder = rep.getStepAttributeBoolean( id_step, CONST_STRING_CREATE_PARENT_FOLDER );
      headerEnabled = rep.getStepAttributeBoolean( id_step, CONST_STRING_HEADER );
      footerEnabled = rep.getStepAttributeBoolean( id_step, CONST_STRING_FOOTER );
      fileFormat = rep.getStepAttributeString( id_step, CONST_STRING_FORMAT );
      setFileCompression( rep.getStepAttributeString( id_step, CONST_STRING_COMPRESSION ) );
      fileNameInField = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_NAME_IN_FIELD );
      fileNameField = rep.getStepAttributeString( id_step, CONST_STRING_FILE_NAME_FIELD );
      if ( getFileCompression() == null ) {
        if ( rep.getStepAttributeBoolean( id_step, CONST_STRING_ZIPPED ) ) {
          setFileCompression( fileCompressionTypeCodes[FILE_COMPRESSION_TYPE_ZIP] );
        } else {
          setFileCompression( fileCompressionTypeCodes[FILE_COMPRESSION_TYPE_NONE] );
        }
      }
      encoding = rep.getStepAttributeString( id_step, CONST_STRING_ENCODING );

      fileName = loadSourceRep( rep, id_step, metaStore );
      servletOutput = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_SERVLET_OUTPUT );
      doNotOpenNewFileInit = rep.getStepAttributeBoolean( id_step, CONST_STRING_DO_NOT_OPEN_NEW_FILE_INIT );
      extension = rep.getStepAttributeString( id_step, CONST_STRING_FILE_EXTENTION );
      fileAppended = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_APPEND );

      splitEveryRows = rep.getStepAttributeString( id_step, CONST_STRING_FILE_SPLIT_ROWS );
      if ( Utils.isEmpty( splitEveryRows ) ) {
        // test for legacy
        long splitEvery = rep.getStepAttributeInteger( id_step, CONST_STRING_FILE_SPLIT );
        if ( splitEvery > 0 ) {
          splitEveryRows = Long.toString( splitEvery );
        }
      }

      stepNrInFilename = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_ADD_STEPNR );
      partNrInFilename = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_ADD_PARTNR );
      dateInFilename = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_ADD_DATE );
      timeInFilename = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_ADD_TIME );
      setSpecifyingFormat( rep.getStepAttributeBoolean( id_step, CONST_STRING_SPECIFY_FORMAT ) );
      setDateTimeFormat( rep.getStepAttributeString( id_step, CONST_STRING_DATE_TIME_FORMAT ) );

      String addToResultFiles = rep.getStepAttributeString( id_step, CONST_STRING_ADD_TO_RESULT_FILENAMES );
      addToResultFilenames = Utils.isEmpty( addToResultFiles ) || rep.getStepAttributeBoolean( id_step,
        CONST_STRING_ADD_TO_RESULT_FILENAMES );

      padded = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_PAD );
      fastDump = rep.getStepAttributeBoolean( id_step, CONST_STRING_FILE_FAST_DUMP );

      newline = getNewLine( fileFormat );

      int nrfields = rep.countNrStepAttributes( id_step, CONST_STRING_FIELD_NAME );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        outputFields[i] = new TextFileField();

        outputFields[i].setName( rep.getStepAttributeString( id_step, i, CONST_STRING_FIELD_NAME ) );
        outputFields[i].setType( rep.getStepAttributeString( id_step, i, CONST_STRING_FIELD_TYPE ) );
        outputFields[i].setFormat( rep.getStepAttributeString( id_step, i, CONST_STRING_FIELD_FORMAT ) );
        outputFields[i].setCurrencySymbol( rep.getStepAttributeString( id_step, i, CONST_STRING_FIELD_CURRENCY ) );
        outputFields[i].setDecimalSymbol( rep.getStepAttributeString( id_step, i, CONST_STRING_FIELD_DECIMAL ) );
        outputFields[i].setGroupingSymbol( rep.getStepAttributeString( id_step, i, CONST_STRING_FIELD_GROUP ) );
        outputFields[i].setTrimType( ValueMetaString.getTrimTypeByCode( rep.getStepAttributeString( id_step, i,
          CONST_STRING_FIELD_TRIM_TYPE ) ) );
        outputFields[i].setNullString( rep.getStepAttributeString( id_step, i, CONST_STRING_FIELD_NULLIF ) );
        outputFields[i].setLength( (int) rep.getStepAttributeInteger( id_step, i, CONST_STRING_FIELD_LENGTH ) );
        outputFields[i].setPrecision( (int) rep.getStepAttributeInteger( id_step, i, CONST_STRING_FIELD_PRECISION ) );
      }
      endedLine = rep.getStepAttributeString( id_step, CONST_STRING_ENDED_LINE );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_SEPARATOR, separator );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_ENCLOSURE, enclosure );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_ENCLOSURE_FORCED, enclosureForced );
      rep.saveStepAttribute( id_transformation, id_step, 0, CONST_STRING_ENCLOSURE_FIX_DISABLED, disableEnclosureFix );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_HEADER, headerEnabled );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FOOTER, footerEnabled );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FORMAT, fileFormat );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_COMPRESSION, getFileCompression() );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_ENCODING, encoding );
      saveSourceRep( rep, id_transformation, id_step, fileName );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_SERVLET_OUTPUT, servletOutput );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_DO_NOT_OPEN_NEW_FILE_INIT, doNotOpenNewFileInit );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_EXTENTION, extension );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_APPEND, fileAppended );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_SPLIT_ROWS, splitEveryRows );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_ADD_STEPNR, stepNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_ADD_PARTNR, partNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_ADD_DATE, dateInFilename );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_DATE_TIME_FORMAT, getDateTimeFormat() );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_CREATE_PARENT_FOLDER, createparentfolder );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_SPECIFY_FORMAT, isSpecifyingFormat() );

      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_ADD_TO_RESULT_FILENAMES, addToResultFilenames );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_ADD_TIME, timeInFilename );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_PAD, padded );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_FAST_DUMP, fastDump );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_NAME_IN_FIELD, fileNameInField );
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_NAME_FIELD, fileNameField );

      for ( int i = 0; i < outputFields.length; i++ ) {
        TextFileField field = outputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_NAME, field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_TYPE, field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_FORMAT, field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_CURRENCY, field.getCurrencySymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_DECIMAL, field.getDecimalSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_GROUP, field.getGroupingSymbol() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_TRIM_TYPE, field.getTrimTypeCode() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_NULLIF, field.getNullString() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_LENGTH, field.getLength() );
        rep.saveStepAttribute( id_transformation, id_step, i, CONST_STRING_FIELD_PRECISION, field.getPrecision() );
      }
      rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_ENDED_LINE, endedLine );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;

    // Check output fields
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
          "TextFileOutputMeta.CheckResult.FieldsReceived", Const.EMPTY_STRING + prev.size() ), stepMeta );
      remarks.add( cr );

      StringBuilder missingFields = new StringBuilder();
      boolean errorFound = false;

      // Starting from selected fields in ...
      for ( TextFileField outputField : outputFields ) {
        int idx = prev.indexOfValue( outputField.getName() );
        if ( idx < 0 ) {
          missingFields.append( "\t\t" ).append( outputField.getName() ).append( Const.CR );
          errorFound = true;
        }
      }
      if ( errorFound ) {
        String errorMessage = BaseMessages.getString( PKG, "TextFileOutputMeta.CheckResult.FieldsNotFound",
          missingFields );
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta );
        remarks.add( cr );
      } else {
        cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
                "TextFileOutputMeta.CheckResult.AllFieldsFound" ), stepMeta );
        remarks.add( cr );
      }
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "TextFileOutputMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "TextFileOutputMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }

    cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_COMMENT, BaseMessages.getString( PKG,
            "TextFileOutputMeta.CheckResult.FilesNotChecked" ), stepMeta );
    remarks.add( cr );
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans ) {
    return new TextFileOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new TextFileOutputData();
  }

  /**
   * Since the exported transformation that runs this will reside in a ZIP file, we can't reference files relatively. So
   * what this does is turn the name of the base path into an absolute path.
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
      ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore )
    throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      // In case the name of the file comes from previous steps, forget about this!
      //
      if ( !fileNameInField ) {

        if ( !Utils.isEmpty( fileName ) ) {
          FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( fileName ), space );
          fileName = resourceNamingInterface.nameResource( fileObject, space, true );
        }
      }

      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public void setFilename( String fileName ) {
    this.fileName = fileName;
  }

  @Override
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return getStepMetaInjectionInterface().extractStepMetadataEntries();
  }

  protected String loadSource( Node stepnode, IMetaStore metastore ) {
    return XMLHandler.getTagValue( stepnode, CONST_STRING_FILE, CONST_STRING_NAME );
  }

  protected void saveSource( StringBuilder retVal, String value ) {
    retVal.append( "      " ).append( XMLHandler.addTagValue( CONST_STRING_NAME, fileName ) );
  }

  protected String loadSourceRep( Repository rep, ObjectId id_step, IMetaStore metaStore  ) throws KettleException {
    return rep.getStepAttributeString( id_step, CONST_STRING_FILE_NAME );
  }

  protected void saveSourceRep( Repository rep, ObjectId id_transformation, ObjectId id_step, String value )
    throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, CONST_STRING_FILE_NAME, fileName );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean passDataToServletOutput() {
    return servletOutput;
  }

  /**
   * <p>Creates a copy of the meta information of the output fields, so that we don't make any changes to the
   * original meta information.</p>
   * <p>It is based on the original meta information and augmented with the information the user configured.</p>
   *
   * @param data
   */
  protected synchronized void calcMetaWithFieldOptions( TextFileOutputData data ) {
    if ( !Utils.isEmpty( getOutputFields() ) ) {
      metaWithFieldOptions = new ValueMetaInterface[ getOutputFields().length ];

      for ( int i = 0; i < getOutputFields().length; ++i ) {
        ValueMetaInterface v = data.outputRowMeta.getValueMeta( data.fieldnrs[ i ] );

        if ( v != null ) {
          metaWithFieldOptions[ i ] = v.clone();

          TextFileField field = getOutputFields()[ i ];
          metaWithFieldOptions[ i ].setLength( field.getLength() );
          metaWithFieldOptions[ i ].setPrecision( field.getPrecision() );
          if ( !Utils.isEmpty( field.getFormat() ) ) {
            metaWithFieldOptions[ i ].setConversionMask( field.getFormat() );
          }
          metaWithFieldOptions[ i ].setDecimalSymbol( field.getDecimalSymbol() );
          metaWithFieldOptions[ i ].setGroupingSymbol( field.getGroupingSymbol() );
          metaWithFieldOptions[ i ].setCurrencySymbol( field.getCurrencySymbol() );
          metaWithFieldOptions[ i ].setTrimType( field.getTrimType() );
          if ( !Utils.isEmpty( getEncoding() ) ) {
            metaWithFieldOptions[ i ].setStringEncoding( getEncoding() );
          }

          // enable output padding by default to be compatible with v2.5.x
          //
          metaWithFieldOptions[ i ].setOutputPaddingEnabled( true );
        }
      }
    } else {
      metaWithFieldOptions = null;
    }
  }

  protected synchronized ValueMetaInterface[] getMetaWithFieldOptions() {
    if ( null == metaWithFieldOptions ) {
      metaWithFieldOptions = new ValueMetaInterface[ 0 ];
    }

    return metaWithFieldOptions;
  }
}
