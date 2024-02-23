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

package org.pentaho.di.trans.steps.excelwriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.pentaho.di.core.annotations.Step;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

@Step( id = "TypeExitExcelWriterStep", name = "BaseStep.TypeLongDesc.TypeExitExcelWriterStep",
        description = "BaseStep.TypeTooltipDesc.TypeExitExcelWriterStep",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Output",
        image = "XWS.svg",
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/microsoft-excel-writer",
        i18nPackageName = "org.pentaho.di.trans.steps.excelwriter" )
@InjectionSupported( localizationPrefix = "ExcelWriter.Injection.", groups = "FIELDS" )
public class ExcelWriterStepMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ExcelWriterStepMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String IF_FILE_EXISTS_REUSE = "reuse";
  public static final String IF_FILE_EXISTS_CREATE_NEW = "new";

  public static final String IF_SHEET_EXISTS_REUSE = "reuse";
  public static final String IF_SHEET_EXISTS_CREATE_NEW = "new";

  public static final String ROW_WRITE_OVERWRITE = "overwrite";
  public static final String ROW_WRITE_PUSH_DOWN = "push";

  // FILE GROUP START

  /** The base name of the output file */
  @Injection( name = "FILENAME" )
  private String fileName;

  /** The file extension in case of a generated filename */
  @Injection( name = "EXTENSION" )
  private String extension;

  /** if the parent folders should be created if they don't exist */
  @Injection( name = "CREATE_PARENT_FOLDERS" )
  private boolean createParentFolders;

  /** Do we need to stream data to handle very large files? */
  @Injection( name = "STREAM_XSLX_DATA" )
  private boolean streamingData;

  /** if this value is larger then 0, the text file is split up into parts of this number of lines */
  @Injection( name = "SPLIT_EVERY_DATA_ROWS" )
  private int splitEvery;

  /** Flag: add the stepnr in the filename */
  @Injection( name = "INCLUDE_STEPNR_IN_FILENAME" )
  private boolean stepNrInFilename;

  /** Flag: add the date in the filename */
  @Injection( name = "INCLUDE_DATE_IN_FILENAME" )
  private boolean dateInFilename;

  /** Flag: add the time in the filename */
  @Injection( name = "INCLUDE_TIME_IN_FILENAME" )
  private boolean timeInFilename;

  @Injection( name = "SPECIFY_DATE_TIME_FORMAT" )
  private boolean SpecifyFormat;

  @Injection( name = "DATE_TIME_FORMAT" )
  private String date_time_format;

  /** what to do if file exists **/
  @Injection( name = "IF_OUTPUT_FILE_EXISTS" )
  private String ifFileExists;

  /** Flag : Do not open new file when transformation start */
  @Injection( name = "WAIT_FOR_FIRST_ROW" )
  private boolean doNotOpenNewFileInit;

  /** Flag: add the filenames to result filenames */
  @Injection( name = "ADD_FILENAMES_TO_RESULT" )
  private boolean addToResultFilenames;

  // FILE GROUP END

  // SHEET GROUP START

  /** the excel sheet name */
  @Injection( name = "SHEET_NAME" )
  private String sheetname;

  @Injection( name = "MAKE_SHEET_ACTIVE" )
  private boolean makeSheetActive;

  @Injection( name = "IF_SHEET_EXISTS_IN_OUTPUT" )
  private String ifSheetExists;

  /** Flag: protect the sheet */
  @Injection( name = "PROTECT_SHEET" )
  private boolean protectsheet;

  /** The user/password to protect the sheet */
  @Injection( name = "PROTECTED_BY_USER" )
  private String protectedBy;
  @Injection( name = "PASSWORD" )
  private String password;

  // SHEET GROUP END

  // TEMPLATE GROUP START

  /** Flag: use a template */
  @Injection( name = "USE_TEMPLATE_FOR_NEW_FILES" )
  private boolean templateEnabled;

  /** the excel template */
  @Injection( name = "TEMPLATE_FILE" )
  private String templateFileName;

  @Injection( name = "USE_TEMPLATE_FOR_NEW_SHEETS" )
  private boolean templateSheetEnabled;

  @Injection( name = "TEMPLATE_SHEET" )
  private String templateSheetName;

  @Injection( name = "HIDE_TEMPLATE_SHEET" )
  private boolean templateSheetHidden;

  // TEMPLATE GROUP END

  // CONTENT OPTIONS GROUP START

  /** where to start writing **/
  @Injection( name = "START_WRITING_AT_CELL" )
  private String startingCell;

  /** how to write rows **/
  @Injection( name = "WHEN_WRITING_ROWS" )
  private String rowWritingMethod;

  /** Add a header at the top of the file? */
  @Injection( name = "WRITE_HEADER" )
  private boolean headerEnabled;

  /** Add a footer at the bottom of the file? */
  @Injection( name = "WRITE_FOOTER" )
  private boolean footerEnabled;

  /** Flag : auto size columns? */
  @Injection( name = "AUTO_SIZE_COLUMNS" )
  private boolean autosizecolums;

  @Injection( name = "FORCE_FORMULA_RECALC" )
  private boolean forceFormulaRecalculation = false;

  @Injection( name = "LEAVE_STYLES_UNCHANGED" )
  private boolean leaveExistingStylesUnchanged = false;

  // CONTENT OPTIONS GROUP END

  // WHEN WRITING TO EXISTING SHEET GROUP START

  /** Flag : appendLines lines? */
  @Injection( name = "START_WRITING_AT_SHEET_END" )
  private boolean appendLines;

  /** advanced line append options **/
  @Injection( name = "OFFSET_BY_ROWS" )
  private int appendOffset = 0;

  @Injection( name = "BEGIN_BY_WRITING_EMPTY_LINES" )
  private int appendEmpty = 0;

  @Injection( name = "OMIT_HEADER" )
  private boolean appendOmitHeader = false;

  @Injection( name = "EXTEND_DATA_VALIDATION" )
  private boolean extendDataValidationRanges = false;

  @Injection( name = "RETAIN_NULL_VALUES" )
  private boolean retainNullValues = true;

  // WHEN WRITING TO EXISTING SHEET GROUP END

  /* THE FIELD SPECIFICATIONS ... */

  /** The output fields */
  @InjectionDeep
  private ExcelWriterStepField[] outputFields;

  public ExcelWriterStepMeta() {
    super();
  }

  public int getAppendOffset() {
    return appendOffset;
  }

  public void setAppendOffset( int appendOffset ) {
    this.appendOffset = appendOffset;
  }

  public int getAppendEmpty() {
    return appendEmpty;
  }

  public void setAppendEmpty( int appendEmpty ) {
    this.appendEmpty = appendEmpty >= 0 ? appendEmpty : 0;
  }

  /**
   * @return Returns the dateInFilename.
   */
  public boolean isDateInFilename() {
    return dateInFilename;
  }

  /**
   * @param dateInFilename
   *          The dateInFilename to set.
   */
  public void setDateInFilename( boolean dateInFilename ) {
    this.dateInFilename = dateInFilename;
  }

  public boolean isAppendOmitHeader() {
    return appendOmitHeader;
  }

  public void setAppendOmitHeader( boolean appendOmitHeader ) {
    this.appendOmitHeader = appendOmitHeader;
  }

  public String getStartingCell() {
    return startingCell;
  }

  public void setStartingCell( String startingCell ) {
    this.startingCell = startingCell;
  }

  public String getRowWritingMethod() {
    return rowWritingMethod;
  }

  public void setRowWritingMethod( String rowWritingMethod ) {
    this.rowWritingMethod = rowWritingMethod;
  }

  public String getIfFileExists() {
    return ifFileExists;
  }

  public void setIfFileExists( String ifFileExists ) {
    this.ifFileExists = ifFileExists;
  }

  public String getIfSheetExists() {
    return ifSheetExists;
  }

  public void setIfSheetExists( String ifSheetExists ) {
    this.ifSheetExists = ifSheetExists;
  }

  public String getProtectedBy() {
    return protectedBy;
  }

  public void setProtectedBy( String protectedBy ) {
    this.protectedBy = protectedBy;
  }

  /**
   * @return Returns the extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * @param extension
   *          The extension to set.
   */
  public void setExtension( String extension ) {
    this.extension = extension;
  }

  /**
   * @return Returns the fileName.
   */
  public String getFileName() {
    return fileName;
  }

  public boolean isCreateParentFolders() {
    return createParentFolders;
  }

  public void setCreateParentFolders( boolean value ) {
    createParentFolders = value;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @return Returns the sheet name.
   */
  public String getSheetname() {
    return sheetname;
  }

  /**
   * @param sheetname
   *          The sheet name.
   */
  public void setSheetname( String sheetname ) {
    this.sheetname = sheetname;
  }

  /**
   * @param fileName
   *          The fileName to set.
   */
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  /**
   * @param password
   *          teh passwoed to set.
   */
  public void setPassword( String password ) {
    this.password = password;
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
   * @return Returns the autosizecolums.
   */
  public boolean isAutoSizeColums() {
    return autosizecolums;
  }

  /**
   * @param autosizecolums
   *          The autosizecolums to set.
   */
  public void setAutoSizeColums( boolean autosizecolums ) {
    this.autosizecolums = autosizecolums;
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

  public boolean isSpecifyFormat() {
    return SpecifyFormat;
  }

  public void setSpecifyFormat( boolean SpecifyFormat ) {
    this.SpecifyFormat = SpecifyFormat;
  }

  public String getDateTimeFormat() {
    return date_time_format;
  }

  public void setDateTimeFormat( String date_time_format ) {
    this.date_time_format = date_time_format;
  }

  /**
   * @return Returns the splitEvery.
   */
  public int getSplitEvery() {
    return splitEvery;
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
   * @param splitEvery
   *          The splitEvery to set.
   */
  public void setSplitEvery( int splitEvery ) {
    this.splitEvery = splitEvery >= 0 ? splitEvery : 0;
  }

  /**
   * @return Returns the stepNrInFilename.
   */
  public boolean isStepNrInFilename() {
    return stepNrInFilename;
  }

  /**
   * @param stepNrInFilename
   *          The stepNrInFilename to set.
   */
  public void setStepNrInFilename( boolean stepNrInFilename ) {
    this.stepNrInFilename = stepNrInFilename;
  }

  /**
   * @return Returns the timeInFilename.
   */
  public boolean isTimeInFilename() {
    return timeInFilename;
  }

  /**
   * @return Returns the protectsheet.
   */
  public boolean isSheetProtected() {
    return protectsheet;
  }

  /**
   * @param timeInFilename
   *          The timeInFilename to set.
   */
  public void setTimeInFilename( boolean timeInFilename ) {
    this.timeInFilename = timeInFilename;
  }

  /**
   * @param protectsheet
   *          the value to set.
   */
  public void setProtectSheet( boolean protectsheet ) {
    this.protectsheet = protectsheet;
  }

  /**
   * @return Returns the outputFields.
   */
  public ExcelWriterStepField[] getOutputFields() {
    return outputFields;
  }

  /**
   * @param outputFields
   *          The outputFields to set.
   */
  public void setOutputFields( ExcelWriterStepField[] outputFields ) {
    this.outputFields = outputFields;
  }

  /**
   * @return Returns the template.
   */
  public boolean isTemplateEnabled() {
    return templateEnabled;
  }

  /**
   * @param template
   *          The template to set.
   */
  public void setTemplateEnabled( boolean template ) {
    this.templateEnabled = template;
  }

  public boolean isTemplateSheetEnabled() {
    return templateSheetEnabled;
  }

  public void setTemplateSheetEnabled( boolean templateSheetEnabled ) {
    this.templateSheetEnabled = templateSheetEnabled;
  }

  /**
   * @return Returns the templateFileName.
   */
  public String getTemplateFileName() {
    return templateFileName;
  }

  /**
   * @param templateFileName
   *          The templateFileName to set.
   */
  public void setTemplateFileName( String templateFileName ) {
    this.templateFileName = templateFileName;
  }

  public String getTemplateSheetName() {
    return templateSheetName;
  }

  public void setTemplateSheetName( String templateSheetName ) {
    this.templateSheetName = templateSheetName;
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
   * @return Returns the appendLines.
   */
  public boolean isAppendLines() {
    return appendLines;
  }

  /**
   * @param append
   *          The appendLines to set.
   */
  public void setAppendLines( boolean append ) {
    this.appendLines = append;
  }

  public void setMakeSheetActive( boolean makeSheetActive ) {
    this.makeSheetActive = makeSheetActive;
  }

  public boolean isMakeSheetActive() {
    return makeSheetActive;
  }

  public boolean isForceFormulaRecalculation() {
    return forceFormulaRecalculation;
  }

  public void setForceFormulaRecalculation( boolean forceFormulaRecalculation ) {
    this.forceFormulaRecalculation = forceFormulaRecalculation;
  }

  public boolean isLeaveExistingStylesUnchanged() {
    return leaveExistingStylesUnchanged;
  }

  public void setLeaveExistingStylesUnchanged( boolean leaveExistingStylesUnchanged ) {
    this.leaveExistingStylesUnchanged = leaveExistingStylesUnchanged;
  }

  public boolean isExtendDataValidationRanges() {
    return extendDataValidationRanges;
  }

  public void setExtendDataValidationRanges( boolean value ) {
    extendDataValidationRanges = value;
  }


  public boolean isRetainNullValues() {
    return retainNullValues;
  }

  public void setRetainNullValues( boolean value ) {
    retainNullValues = value;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    outputFields = new ExcelWriterStepField[nrfields];
  }

  @Override
  public Object clone() {
    ExcelWriterStepMeta retval = (ExcelWriterStepMeta) super.clone();
    int nrfields = outputFields.length;

    retval.allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      retval.outputFields[i] = (ExcelWriterStepField) outputFields[i].clone();
    }

    return retval;
  }

  /** node names for xml/repo save/load */
  private static class Tags {
    static final String CREATE_PARENT_FOLDER = "create_parent";
    static final String EXTEND_DATA_VALIDATION = "extend_data_validation";
    static final String RETAIN_NULL_VALUES = "retain_null_values";
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {

      headerEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "header" ) );
      footerEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "footer" ) );
      appendOmitHeader = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "appendOmitHeader" ) );
      appendLines = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "appendLines" ) );
      makeSheetActive = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "makeSheetActive" ) );
      appendOffset = Const.toInt( XMLHandler.getTagValue( stepnode, "appendOffset" ), 0 );
      appendEmpty = Const.toInt( XMLHandler.getTagValue( stepnode, "appendEmpty" ), 0 );

      startingCell = XMLHandler.getTagValue( stepnode, "startingCell" );
      rowWritingMethod = XMLHandler.getTagValue( stepnode, "rowWritingMethod" );
      forceFormulaRecalculation =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "forceFormulaRecalculation" ) );
      leaveExistingStylesUnchanged =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "leaveExistingStylesUnchanged" ) );

      extendDataValidationRanges = getBooleanValue( stepnode, Tags.EXTEND_DATA_VALIDATION, false );

      String addToResult = XMLHandler.getTagValue( stepnode, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResult ) ) {
        addToResultFilenames = true;
      } else {
        addToResultFilenames = "Y".equalsIgnoreCase( addToResult );
      }

      Node fileNode = XMLHandler.getSubNode(stepnode, "file" );

      fileName = XMLHandler.getTagValue( stepnode, "file", "name" );
      extension = XMLHandler.getTagValue( stepnode, "file", "extention" );
      createParentFolders = getBooleanValue( fileNode, Tags.CREATE_PARENT_FOLDER, false );

      doNotOpenNewFileInit =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "do_not_open_newfile_init" ) );
      stepNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "split" ) );
      dateInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_date" ) );
      timeInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_time" ) );
      SpecifyFormat = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "SpecifyFormat" ) );
      date_time_format = XMLHandler.getTagValue( stepnode, "file", "date_time_format" );

      autosizecolums = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "autosizecolums" ) );
      retainNullValues = getBooleanValue( fileNode, Tags.RETAIN_NULL_VALUES, true );
      streamingData = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "stream_data" ) );
      protectsheet = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "protect_sheet" ) );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "file", "password" ) );
      protectedBy = XMLHandler.getTagValue( stepnode, "file", "protected_by" );
      splitEvery = Const.toInt( XMLHandler.getTagValue( stepnode, "file", "splitevery" ), 0 );

      templateEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "template", "enabled" ) );
      templateSheetEnabled =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "template", "sheet_enabled" ) );
      templateSheetHidden =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "template", "hidden" ) );
      templateFileName = XMLHandler.getTagValue( stepnode, "template", "filename" );
      templateSheetName = XMLHandler.getTagValue( stepnode, "template", "sheetname" );
      sheetname = XMLHandler.getTagValue( stepnode, "file", "sheetname" );
      ifFileExists = XMLHandler.getTagValue( stepnode, "file", "if_file_exists" );
      ifSheetExists = XMLHandler.getTagValue( stepnode, "file", "if_sheet_exists" );

      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        outputFields[i] = new ExcelWriterStepField();
        outputFields[i].setName( XMLHandler.getTagValue( fnode, "name" ) );
        outputFields[i].setType( XMLHandler.getTagValue( fnode, "type" ) );
        outputFields[i].setFormat( XMLHandler.getTagValue( fnode, "format" ) );
        outputFields[i].setTitle( XMLHandler.getTagValue( fnode, "title" ) );
        outputFields[i].setTitleStyleCell( XMLHandler.getTagValue( fnode, "titleStyleCell" ) );
        outputFields[i].setStyleCell( XMLHandler.getTagValue( fnode, "styleCell" ) );
        outputFields[i].setCommentField( XMLHandler.getTagValue( fnode, "commentField" ) );
        outputFields[i].setCommentAuthorField( XMLHandler.getTagValue( fnode, "commentAuthorField" ) );
        outputFields[i].setFormula( XMLHandler.getTagValue( fnode, "formula" ) != null
          && XMLHandler.getTagValue( fnode, "formula" ).equalsIgnoreCase( "Y" ) );
        outputFields[i].setHyperlinkField( XMLHandler.getTagValue( fnode, "hyperlinkField" ) );
      }

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
  }

  private boolean getBooleanValue( Node node, String tag, boolean defaultValue ) {
    String value = XMLHandler.getTagValue( node, tag );
    return Optional.ofNullable( value ).filter( StringUtils::isNotBlank ).map( val -> "Y".equals( val ) ).orElse( defaultValue );
  }

  public String getNewLine( String fformat ) {
    String nl = System.getProperty( "line.separator" );

    if ( fformat != null ) {
      if ( fformat.equalsIgnoreCase( "DOS" ) ) {
        nl = "\r\n";
      } else if ( fformat.equalsIgnoreCase( "UNIX" ) ) {
        nl = "\n";
      }
    }

    return nl;
  }

  @Override
  public void setDefault() {

    autosizecolums = false;
    streamingData = false;
    headerEnabled = true;
    footerEnabled = false;
    fileName = "file";
    extension = "xls";
    doNotOpenNewFileInit = false;
    stepNrInFilename = false;
    dateInFilename = false;
    timeInFilename = false;
    date_time_format = null;
    SpecifyFormat = false;
    addToResultFilenames = true;
    protectsheet = false;
    splitEvery = 0;
    templateEnabled = false;
    templateFileName = "template.xls";
    templateSheetHidden = false;
    sheetname = "Sheet1";
    appendLines = false;
    ifFileExists = IF_FILE_EXISTS_CREATE_NEW;
    ifSheetExists = IF_SHEET_EXISTS_CREATE_NEW;
    startingCell = "A1";
    rowWritingMethod = ROW_WRITE_OVERWRITE;
    appendEmpty = 0;
    appendOffset = 0;
    appendOmitHeader = false;
    makeSheetActive = true;
    forceFormulaRecalculation = false;
    retainNullValues = true;

    allocate( 0 );

  }

  public String[] getFiles( VariableSpace space ) {
    int copies = 1;
    int splits = 1;

    if ( stepNrInFilename ) {
      copies = 3;
    }

    if ( splitEvery != 0 ) {
      splits = 4;
    }

    int nr = copies * splits;
    if ( nr > 1 ) {
      nr++;
    }

    String[] retval = new String[nr];

    int i = 0;
    for ( int copy = 0; copy < copies; copy++ ) {
      for ( int split = 0; split < splits; split++ ) {
        retval[i] = buildFilename( space, copy, split );
        i++;
      }
    }
    if ( i < nr ) {
      retval[i] = "...";
    }

    return retval;
  }

  public String buildFilename( VariableSpace space, int stepnr, int splitnr ) {
    SimpleDateFormat daf = new SimpleDateFormat();

    // Replace possible environment variables...
    String retval = space.environmentSubstitute( fileName );
    String realextension = space.environmentSubstitute( extension );

    Date now = new Date();

    if ( SpecifyFormat && !Utils.isEmpty( date_time_format ) ) {
      daf.applyPattern( date_time_format );
      String dt = daf.format( now );
      retval += dt;
    } else {
      if ( dateInFilename ) {
        daf.applyPattern( "yyyMMdd" );
        String d = daf.format( now );
        retval += "_" + d;
      }
      if ( timeInFilename ) {
        daf.applyPattern( "HHmmss" );
        String t = daf.format( now );
        retval += "_" + t;
      }
    }
    if ( stepNrInFilename ) {
      retval += "_" + stepnr;
    }
    if ( splitEvery > 0 ) {
      retval += "_" + splitnr;
    }

    if ( realextension != null && realextension.length() != 0 ) {
      retval += "." + realextension;
    }

    return retval;
  }

  @Override
  public void getFields( RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep,
    VariableSpace space, Repository repository, IMetaStore metaStore ) {

    // No values are added to the row in this type of step
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 800 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "header", headerEnabled ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "footer", footerEnabled ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "makeSheetActive", makeSheetActive ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "startingCell", startingCell ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "appendOmitHeader", appendOmitHeader ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "appendOffset", appendOffset ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "appendEmpty", appendEmpty ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "rowWritingMethod", rowWritingMethod ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "forceFormulaRecalculation", forceFormulaRecalculation ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( "leaveExistingStylesUnchanged", leaveExistingStylesUnchanged ) );
    retval.append( "    " ).append(
      XMLHandler.addTagValue( Tags.EXTEND_DATA_VALIDATION, extendDataValidationRanges ) );
    retval.append( "    " + XMLHandler.addTagValue( "appendLines", appendLines ) );
    retval.append( "    " + XMLHandler.addTagValue( "add_to_result_filenames", addToResultFilenames ) );

    retval.append( "    <file>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "name", fileName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "extention", extension ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( Tags.CREATE_PARENT_FOLDER, createParentFolders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "do_not_open_newfile_init", doNotOpenNewFileInit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "split", stepNrInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_date", dateInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_time", timeInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "SpecifyFormat", SpecifyFormat ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "date_time_format", date_time_format ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sheetname", sheetname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "autosizecolums", autosizecolums ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( Tags.RETAIN_NULL_VALUES, retainNullValues ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "stream_data", streamingData ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "protect_sheet", protectsheet ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "protected_by", protectedBy ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "splitevery", splitEvery ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "if_file_exists", ifFileExists ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "if_sheet_exists", ifSheetExists ) );

    retval.append( "      </file>" ).append( Const.CR );

    retval.append( "    <template>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "enabled", templateEnabled ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sheet_enabled", templateSheetEnabled ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", templateFileName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sheetname", templateSheetName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "hidden", templateSheetHidden ) );
    retval.append( "    </template>" ).append( Const.CR );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < outputFields.length; i++ ) {
      ExcelWriterStepField field = outputFields[i];

      if ( field.getName() != null && field.getName().length() != 0 ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "name", field.getName() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "type", field.getTypeDesc() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "format", field.getFormat() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "title", field.getTitle() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "titleStyleCell", field.getTitleStyleCell() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "styleCell", field.getStyleCell() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "commentField", field.getCommentField() ) );
        retval.append( "        " ).append(
          XMLHandler.addTagValue( "commentAuthorField", field.getCommentAuthorField() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "formula", field.isFormula() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "hyperlinkField", field.getHyperlinkField() ) );
        retval.append( "      </field>" ).append( Const.CR );
      }
    }
    retval.append( "    </fields>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      headerEnabled = rep.getStepAttributeBoolean( id_step, "header" );
      footerEnabled = rep.getStepAttributeBoolean( id_step, "footer" );
      makeSheetActive = rep.getStepAttributeBoolean( id_step, "makeSheetActive" );
      appendOmitHeader = rep.getStepAttributeBoolean( id_step, "appendOmitHeader" );
      startingCell = rep.getStepAttributeString( id_step, "startingCell" );
      appendEmpty = (int) rep.getStepAttributeInteger( id_step, "appendEmpty" );
      appendOffset = (int) rep.getStepAttributeInteger( id_step, "appendOffset" );
      rowWritingMethod = rep.getStepAttributeString( id_step, "rowWritingMethod" );
      appendLines = rep.getStepAttributeBoolean( id_step, "appendLines" );
      forceFormulaRecalculation = rep.getStepAttributeBoolean( id_step, "forceFormulaRecalculation" );
      leaveExistingStylesUnchanged = rep.getStepAttributeBoolean( id_step, "leaveExistingStylesUnchanged" );
      extendDataValidationRanges = rep.getStepAttributeBoolean( id_step, Tags.EXTEND_DATA_VALIDATION );

      String addToResult = rep.getStepAttributeString( id_step, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResult ) ) {
        addToResultFilenames = true;
      } else {
        addToResultFilenames = rep.getStepAttributeBoolean( id_step, "add_to_result_filenames" );
      }

      fileName = rep.getStepAttributeString( id_step, "file_name" );
      extension = rep.getStepAttributeString( id_step, "file_extention" );
      createParentFolders = rep.getStepAttributeBoolean( id_step, Tags.CREATE_PARENT_FOLDER );

      doNotOpenNewFileInit = rep.getStepAttributeBoolean( id_step, "do_not_open_newfile_init" );

      splitEvery = (int) rep.getStepAttributeInteger( id_step, "file_split" );
      stepNrInFilename = rep.getStepAttributeBoolean( id_step, "file_add_stepnr" );
      dateInFilename = rep.getStepAttributeBoolean( id_step, "file_add_date" );
      timeInFilename = rep.getStepAttributeBoolean( id_step, "file_add_time" );
      SpecifyFormat = rep.getStepAttributeBoolean( id_step, "SpecifyFormat" );
      date_time_format = rep.getStepAttributeString( id_step, "date_time_format" );

      autosizecolums = rep.getStepAttributeBoolean( id_step, "autosizecolums" );
      retainNullValues = rep.getStepAttributeBoolean( id_step, 0, Tags.RETAIN_NULL_VALUES, true );
      streamingData = rep.getStepAttributeBoolean( id_step, "stream_data" );
      protectsheet = rep.getStepAttributeBoolean( id_step, "protect_sheet" );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "password" ) );
      protectedBy = rep.getStepAttributeString( id_step, "protected_by" );

      templateEnabled = rep.getStepAttributeBoolean( id_step, "template_enabled" );
      templateFileName = rep.getStepAttributeString( id_step, "template_filename" );
      templateSheetEnabled = rep.getStepAttributeBoolean( id_step, "template_sheet_enabled" );
      templateSheetHidden = rep.getStepAttributeBoolean( id_step, "template_sheet_hidden" );
      templateSheetName = rep.getStepAttributeString( id_step, "template_sheetname" );
      sheetname = rep.getStepAttributeString( id_step, "sheetname" );
      ifFileExists = rep.getStepAttributeString( id_step, "if_file_exists" );
      ifSheetExists = rep.getStepAttributeString( id_step, "if_sheet_exists" );

      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        outputFields[i] = new ExcelWriterStepField();

        outputFields[i].setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        outputFields[i].setType( rep.getStepAttributeString( id_step, i, "field_type" ) );
        outputFields[i].setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
        outputFields[i].setTitle( rep.getStepAttributeString( id_step, i, "field_title" ) );
        outputFields[i].setTitleStyleCell( rep.getStepAttributeString( id_step, i, "field_title_style_cell" ) );
        outputFields[i].setStyleCell( rep.getStepAttributeString( id_step, i, "field_style_cell" ) );
        outputFields[i].setCommentField( rep.getStepAttributeString( id_step, i, "field_comment_field" ) );
        outputFields[i].setCommentAuthorField( rep.getStepAttributeString(
          id_step, i, "field_comment_author_field" ) );
        outputFields[i].setFormula( rep.getStepAttributeBoolean( id_step, i, "field_formula" ) );
        outputFields[i].setHyperlinkField( rep.getStepAttributeString( id_step, i, "field_hyperlink_field" ) );

      }

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "header", headerEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "footer", footerEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "makeSheetActive", makeSheetActive );
      rep.saveStepAttribute( id_transformation, id_step, "startingCell", startingCell );
      rep.saveStepAttribute( id_transformation, id_step, "appendOmitHeader", appendOmitHeader );
      rep.saveStepAttribute( id_transformation, id_step, "appendEmpty", appendEmpty );
      rep.saveStepAttribute( id_transformation, id_step, "appendOffset", appendOffset );
      rep.saveStepAttribute( id_transformation, id_step, "rowWritingMethod", rowWritingMethod );
      rep.saveStepAttribute( id_transformation, id_step, "appendLines", appendLines );
      rep.saveStepAttribute( id_transformation, id_step, "add_to_result_filenames", addToResultFilenames );
      rep.saveStepAttribute( id_transformation, id_step, "file_name", fileName );
      rep.saveStepAttribute( id_transformation, id_step, "do_not_open_newfile_init", doNotOpenNewFileInit );
      rep.saveStepAttribute( id_transformation, id_step, "forceFormulaRecalculation", forceFormulaRecalculation );
      rep.saveStepAttribute(
        id_transformation, id_step, "leaveExistingStylesUnchanged", leaveExistingStylesUnchanged );
      rep.saveStepAttribute( id_transformation, id_step, Tags.EXTEND_DATA_VALIDATION, extendDataValidationRanges );

      rep.saveStepAttribute( id_transformation, id_step, "file_extention", extension );
      rep.saveStepAttribute( id_transformation, id_step, Tags.CREATE_PARENT_FOLDER, createParentFolders );
      rep.saveStepAttribute( id_transformation, id_step, "file_split", splitEvery );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_stepnr", stepNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_date", dateInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_time", timeInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "SpecifyFormat", SpecifyFormat );
      rep.saveStepAttribute( id_transformation, id_step, "date_time_format", date_time_format );

      rep.saveStepAttribute( id_transformation, id_step, "autosizecolums", autosizecolums );
      rep.saveStepAttribute( id_transformation, id_step, Tags.RETAIN_NULL_VALUES, retainNullValues );
      rep.saveStepAttribute( id_transformation, id_step, "stream_data", streamingData );
      rep.saveStepAttribute( id_transformation, id_step, "protect_sheet", protectsheet );
      rep.saveStepAttribute( id_transformation, id_step, "protected_by", protectedBy );
      rep.saveStepAttribute( id_transformation, id_step, "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveStepAttribute( id_transformation, id_step, "template_enabled", templateEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "template_filename", templateFileName );
      rep.saveStepAttribute( id_transformation, id_step, "template_sheet_enabled", templateSheetEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "template_sheet_hidden", templateSheetHidden );
      rep.saveStepAttribute( id_transformation, id_step, "template_sheetname", templateSheetName );
      rep.saveStepAttribute( id_transformation, id_step, "sheetname", sheetname );
      rep.saveStepAttribute( id_transformation, id_step, "if_file_exists", ifFileExists );
      rep.saveStepAttribute( id_transformation, id_step, "if_sheet_exists", ifSheetExists );

      for ( int i = 0; i < outputFields.length; i++ ) {
        ExcelWriterStepField field = outputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_title", field.getTitle() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_title_style_cell", field.getTitleStyleCell() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_style_cell", field.getStyleCell() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_comment_field", field.getCommentField() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_comment_author_field", field
          .getCommentAuthorField() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_formula", field.isFormula() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_hyperlink_field", field.getHyperlinkField() );

      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save step information to the repository for id_step=" + id_step, e );
    }
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
    RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    CheckResult cr;

    // Check output fields
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ExcelWriterStepMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
      remarks.add( cr );

      String error_message = "";
      boolean error_found = false;

      // Starting from selected fields in ...
      for ( int i = 0; i < outputFields.length; i++ ) {
        int idx = prev.indexOfValue( outputFields[i].getName() );
        if ( idx < 0 ) {
          error_message += "\t\t" + outputFields[i].getName() + Const.CR;
          error_found = true;
        }
      }
      if ( error_found ) {
        error_message =
          BaseMessages.getString( PKG, "ExcelWriterStepMeta.CheckResult.FieldsNotFound", error_message );
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ExcelWriterStepMeta.CheckResult.AllFieldsFound" ), stepMeta );
        remarks.add( cr );
      }
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ExcelWriterStepMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ExcelWriterStepMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }

    cr =
      new CheckResult( CheckResultInterface.TYPE_RESULT_COMMENT, BaseMessages.getString(
        PKG, "ExcelWriterStepMeta.CheckResult.FilesNotChecked" ), stepMeta );
    remarks.add( cr );
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
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      if ( !Utils.isEmpty( fileName ) ) {
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( fileName ), space );
        fileName = resourceNamingInterface.nameResource( fileObject, space, true );
      }
      if ( !Utils.isEmpty( templateFileName ) ) {
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( templateFileName ), space );
        templateFileName = resourceNamingInterface.nameResource( fileObject, space, true );
      }

      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  public ExcelWriterStep getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ExcelWriterStep( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public ExcelWriterStepData getStepData() {
    return new ExcelWriterStepData();
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[0];
  }

  /**
   * @return the streamingData
   */
  public boolean isStreamingData() {
    return streamingData;
  }

  /**
   * @param streamingData
   *          the streamingData to set
   */
  public void setStreamingData( boolean streamingData ) {
    this.streamingData = streamingData;
  }

  public boolean isTemplateSheetHidden() {
    return templateSheetHidden;
  }

  public void setTemplateSheetHidden( boolean hide ) {
    this.templateSheetHidden = hide;
  }

}
