/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.exceloutput;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMeta;
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
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;
import org.pentaho.di.core.annotations.Step;

/**
 * Metadata of the Excel Output step.
 *
 * @author Matt
 * @since on 6-sep-2006
 */
@Step( id = "ExcelOutput", name = "BaseStep.TypeLongDesc.ExcelOutput",
        description = "BaseStep.TypeTooltipDesc.ExcelOutput",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Deprecated",
        image = "ui/images/deprecated.svg",
        documentationUrl = "mk-95pdia003/pdi-transformation-steps/microsoft-excel-output",
        i18nPackageName = "org.pentaho.di.trans.steps.exceloutput",
        suggestion = "ExcelOutput.Suggestion" )
@InjectionSupported( localizationPrefix = "ExcelOutput.Injection.", groups = { "FIELDS", "CUSTOM", "CONTENT" } )
public class ExcelOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = ExcelOutputMeta.class; // for i18n purposes, needed by Translator2!!

  public static final int FONT_NAME_ARIAL = 0;
  public static final int FONT_NAME_COURIER = 1;
  public static final int FONT_NAME_TAHOMA = 2;
  public static final int FONT_NAME_TIMES = 3;

  public static final String[] font_name_code = { "arial", "courier", "tahoma", "times" };

  public static final String[] font_name_desc = {
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_name.Arial" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_name.Courier" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_name.Tahoma" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_name.Times" ) };

  public static final int FONT_UNDERLINE_NO = 0;
  public static final int FONT_UNDERLINE_SINGLE = 1;
  public static final int FONT_UNDERLINE_SINGLE_ACCOUNTING = 2;
  public static final int FONT_UNDERLINE_DOUBLE = 3;
  public static final int FONT_UNDERLINE_DOUBLE_ACCOUNTING = 4;

  public static final String[] font_underline_code = {
    "no", "single", "single_accounting", "double", "double_accounting" };

  public static final String[] font_underline_desc = {
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_underline.No" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_underline.Single" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_underline.SingleAccounting" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_underline.Double" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_underline.DoubleAccounting" ) };

  public static final int FONT_ORIENTATION_HORIZONTAL = 0;
  public static final int FONT_ORIENTATION_MINUS_45 = 1;
  public static final int FONT_ORIENTATION_MINUS_90 = 2;
  public static final int FONT_ORIENTATION_PLUS_45 = 3;
  public static final int FONT_ORIENTATION_PLUS_90 = 4;
  public static final int FONT_ORIENTATION_STACKED = 5;
  public static final int FONT_ORIENTATION_VERTICAL = 6;

  public static final String[] font_orientation_code = {
    "horizontal", "minus_45", "minus_90", "plus_45", "plus_90", "stacked", "vertical" };

  public static final String[] font_orientation_desc = {
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_orientation.Horizontal" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_orientation.Minus_45" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_orientation.Minus_90" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_orientation.Plus_45" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_orientation.Plus_90" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_orientation.Stacked" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_orientation.Vertical" ) };

  public static final int FONT_COLOR_NONE = 0;
  public static final int FONT_COLOR_BLACK = 1;
  public static final int FONT_COLOR_WHITE = 2;
  public static final int FONT_COLOR_RED = 3;
  public static final int FONT_COLOR_BRIGHT_GREEN = 4;
  public static final int FONT_COLOR_BLUE = 5;
  public static final int FONT_COLOR_YELLOW = 6;
  public static final int FONT_COLOR_PINK = 7;
  public static final int FONT_COLOR_TURQUOISE = 8;
  public static final int FONT_COLOR_DARK_RED = 9;
  public static final int FONT_COLOR_GREEN = 10;
  public static final int FONT_COLOR_DARK_BLUE = 11;
  public static final int FONT_COLOR_DARK_YELLOW = 12;
  public static final int FONT_COLOR_VIOLET = 13;
  public static final int FONT_COLOR_TEAL = 14;
  public static final int FONT_COLOR_GREY_25pct = 15;
  public static final int FONT_COLOR_GREY_50pct = 16;
  public static final int FONT_COLOR_PERIWINKLEpct = 17;
  public static final int FONT_COLOR_PLUM = 18;
  public static final int FONT_COLOR_IVORY = 19;
  public static final int FONT_COLOR_LIGHT_TURQUOISE = 20;
  public static final int FONT_COLOR_DARK_PURPLE = 21;
  public static final int FONT_COLOR_CORAL = 22;
  public static final int FONT_COLOR_OCEAN_BLUE = 23;
  public static final int FONT_COLOR_ICE_BLUE = 24;
  public static final int FONT_COLOR_TURQOISE = 25;
  public static final int FONT_COLOR_SKY_BLUE = 26;
  public static final int FONT_COLOR_LIGHT_GREEN = 27;
  public static final int FONT_COLOR_VERY_LIGHT_YELLOW = 28;
  public static final int FONT_COLOR_PALE_BLUE = 29;
  public static final int FONT_COLOR_ROSE = 30;
  public static final int FONT_COLOR_LAVENDER = 31;
  public static final int FONT_COLOR_TAN = 32;
  public static final int FONT_COLOR_LIGHT_BLUE = 33;
  public static final int FONT_COLOR_AQUA = 34;
  public static final int FONT_COLOR_LIME = 35;
  public static final int FONT_COLOR_GOLD = 36;
  public static final int FONT_COLOR_LIGHT_ORANGE = 37;
  public static final int FONT_COLOR_ORANGE = 38;
  public static final int FONT_COLOR_BLUE_GREY = 39;
  public static final int FONT_COLOR_GREY_40pct = 40;
  public static final int FONT_COLOR_DARK_TEAL = 41;
  public static final int FONT_COLOR_SEA_GREEN = 42;
  public static final int FONT_COLOR_DARK_GREEN = 43;
  public static final int FONT_COLOR_OLIVE_GREEN = 44;
  public static final int FONT_COLOR_BROWN = 45;
  public static final int FONT_COLOR_GREY_80pct = 46;

  public static final String[] font_color_code = {
    "none", "black", "white", "red", "bright_green", "blue", "yellow", "pink", "turquoise", "dark_red", "green",
    "dark_blue", "dark_yellow", "violet", "teal", "grey_25pct", "grey_50pct", "periwinklepct", "plum", "ivory",
    "light_turquoise", "dark_purple", "coral", "ocean_blue", "ice_blue", "turqoise", "sky_blue", "light_green",
    "very_light_yellow", "pale_blue", "rose", "lavender", "tan", "light_blue", "aqua", "lime", "gold",
    "light_orange", "orange", "blue_grey", "grey_40pct", "dark_teal", "sea_green", "dark_green", "olive_green",
    "brown", "grey_80pct" };

  public static final String[] font_color_desc = {
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.None" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.BLACK" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.WHITE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.RED" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.BRIGHT_GREEN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.BLUE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.YELLOW" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.PINK" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.TURQUOISE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.DARK_RED" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.GREEN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.DARK_BLUE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.DARK_YELLOW" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.VIOLET" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.TEAL" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.GREY_25pct" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.GREY_50pct" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.PERIWINKLEpct" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.PLUM" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.IVORY" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.LIGHT_TURQUOISE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.DARK_PURPLE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.CORAL" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.OCEAN_BLUE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.ICE_BLUE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.TURQOISE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.SKY_BLUE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.LIGHT_GREEN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.VERY_LIGHT_YELLOW" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.PALE_BLUE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.ROSE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.LAVENDER" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.TAN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.LIGHT_BLUE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.AQUA" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.LIME" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.GOLD" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.LIGHT_ORANGE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.ORANGE" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.BLUE_GREY" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.GREY_40pct" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.DARK_TEAL" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.SEA_GREEN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.DARK_GREEN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.OLIVE_GREEN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.BROWN" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_color.GREY_80pct" ) };

  public static final int FONT_ALIGNMENT_LEFT = 0;
  public static final int FONT_ALIGNMENT_RIGHT = 1;
  public static final int FONT_ALIGNMENT_CENTER = 2;
  public static final int FONT_ALIGNMENT_FILL = 3;
  public static final int FONT_ALIGNMENT_GENERAL = 4;
  public static final int FONT_ALIGNMENT_JUSTIFY = 5;

  public static final String[] font_alignment_code = { "left", "right", "center", "fill", "general", "justify" };

  public static final String[] font_alignment_desc = {
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_alignment.Left" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_alignment.Right" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_alignment.Center" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_alignment.Fill" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_alignment.General" ),
    BaseMessages.getString( PKG, "ExcelOutputMeta.font_alignment.Justify" ) };

  public static final int DEFAULT_FONT_SIZE = 10;
  public static final int DEFAULT_ROW_HEIGHT = 255;
  public static final int DEFAULT_ROW_WIDTH = 255;

  private int header_font_name;
  @Injection( name = "HEADER_FONT_SIZE", group = "CUSTOM" )
  private String header_font_size;
  @Injection( name = "HEADER_FONT_BOLD", group = "CUSTOM" )
  private boolean header_font_bold;
  @Injection( name = "HEADER_FONT_ITALIC", group = "CUSTOM" )
  private boolean header_font_italic;
  private int header_font_underline;
  private int header_font_orientation;
  @Injection( name = "HEADER_FONT_COLOR", group = "CUSTOM" )
  private int header_font_color;
  @Injection( name = "HEADER_BACKGROUND_COLOR", group = "CUSTOM" )
  private int header_background_color;
  @Injection( name = "HEADER_ROW_HEIGHT", group = "CUSTOM" )
  private String header_row_height;
  private int header_alignment;
  @Injection( name = "HEADER_IMAGE", group = "CUSTOM" )
  private String header_image;
  // Row font
  private int row_font_name;
  @Injection( name = "ROW_FONT_SIZE", group = "CUSTOM" )
  private String row_font_size;
  @Injection( name = "ROW_FONT_COLOR", group = "CUSTOM" )
  private int row_font_color;
  @Injection( name = "ROW_BACKGROUND_COLOR", group = "CUSTOM" )
  private int row_background_color;

  /** The base name of the output file */
  @Injection( name = "FILENAME" )
  private String fileName;

  /** The file extention in case of a generated filename */
  @Injection( name = "EXTENSION" )
  private String extension;

  /** The password to protect the sheet */
  @Injection( name = "PASSWORD", group = "CONTENT" )
  private String password;

  /** Add a header at the top of the file? */
  @Injection( name = "HEADER_ENABLED", group = "CONTENT" )
  private boolean headerEnabled;

  /** Add a footer at the bottom of the file? */
  @Injection( name = "FOOTER_ENABLED", group = "CONTENT" )
  private boolean footerEnabled;

  /** if this value is larger then 0, the text file is split up into parts of this number of lines */
  @Injection( name = "SPLIT_EVERY", group = "CONTENT" )
  private int splitEvery;

  /** Flag: add the stepnr in the filename */
  @Injection( name = "STEP_NR_IN_FILENAME" )
  private boolean stepNrInFilename;

  /** Flag: add the date in the filename */
  @Injection( name = "DATE_IN_FILENAME" )
  private boolean dateInFilename;

  /** Flag: add the filenames to result filenames */
  @Injection( name = "FILENAME_TO_RESULT" )
  private boolean addToResultFilenames;

  /** Flag: protect the sheet */
  @Injection( name = "PROTECT", group = "CONTENT" )
  private boolean protectsheet;

  /** Flag: add the time in the filename */
  @Injection( name = "TIME_IN_FILENAME" )
  private boolean timeInFilename;

  /** Flag: use a template */
  @Injection( name = "TEMPLATE", group = "CONTENT" )
  private boolean templateEnabled;

  /** the excel template */
  @Injection( name = "TEMPLATE_FILENAME", group = "CONTENT" )
  private String templateFileName;

  /** Flag: append when template */
  @Injection( name = "TEMPLATE_APPEND", group = "CONTENT" )
  private boolean templateAppend;

  /** the excel sheet name */
  @Injection( name = "SHEET_NAME", group = "CONTENT" )
  private String sheetname;

  /** Flag : use temporary files while writing? */
  @Injection( name = "USE_TEMPFILES", group = "CONTENT" )
  private boolean usetempfiles;

  /** Temporary directory **/
  @Injection( name = "TEMPDIR", group = "CONTENT" )
  private String tempdirectory;

  /* THE FIELD SPECIFICATIONS ... */

  /** The output fields */
  @InjectionDeep
  private ExcelField[] outputFields;

  /** The encoding to use for reading: null or empty string means system default encoding */
  @Injection( name = "ENCODING", group = "CONTENT" )
  private String encoding;

  /** Calculated value ... */
  @Injection( name = "NEWLINE", group = "CONTENT" )
  private String newline;

  /** Flag : append workbook? */
  @Injection( name = "APPEND", group = "CONTENT" )
  private boolean append;

  /** Flag : Do not open new file when transformation start */
  @Injection( name = "DONT_OPEN_NEW_FILE" )
  private boolean doNotOpenNewFileInit;

  /** Flag: create parent folder when necessary */
  @Injection( name = "CREATE_PARENT_FOLDER" )
  private boolean createparentfolder;

  @Injection( name = "DATE_FORMAT_SPECIFIED" )
  private boolean SpecifyFormat;

  @Injection( name = "DATE_FORMAT" )
  private String date_time_format;

  /** Flag : auto size columns? */
  @Injection( name = "AUTOSIZE_COLUMNS", group = "CONTENT" )
  private boolean autoSizeColumns;

  /** Flag : write null field values as blank Excel cells? */
  @Injection( name = "NULL_AS_BLANK", group = "CONTENT" )
  private boolean nullIsBlank;

  public ExcelOutputMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the createparentfolder.
   */
  public boolean isCreateParentFolder() {
    return createparentfolder;
  }

  /**
   * @param createparentfolder
   *          The createparentfolder to set.
   */
  public void setCreateParentFolder( boolean createparentfolder ) {
    this.createparentfolder = createparentfolder;
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
   * @return Returns the autosizecolumns.
   */
  public boolean isAutoSizeColumns() {
    return autoSizeColumns;
  }

  /**
   * @param autosizecolumns
   *          The autosizecolumns to set.
   */
  public void setAutoSizeColumns( boolean autosizecolumns ) {
    this.autoSizeColumns = autosizecolumns;
  }

  /**
   * @return Returns the autosizecolums.
   * @deprecated due to typo
   */
  @Deprecated
  public boolean isAutoSizeColums() {
    return autoSizeColumns;
  }

  /**
   * @param autosizecolums
   *          The autosizecolums to set.
   * @deprecated due to typo
   */
  @Deprecated
  public void setAutoSizeColums( boolean autosizecolums ) {
    this.autoSizeColumns = autosizecolums;
  }

  public void setTempDirectory( String directory ) {
    this.tempdirectory = directory;
  }

  public String getTempDirectory() {
    return tempdirectory;
  }

  /**
   * @return Returns whether or not null values are written as blank cells.
   */
  public boolean isNullBlank() {
    return nullIsBlank;
  }

  /**
   * @param nullIsBlank
   *          The boolean indicating whether or not to write null values as blank cells
   */
  public void setNullIsBlank( boolean nullIsBlank ) {
    this.nullIsBlank = nullIsBlank;
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
    this.splitEvery = splitEvery;
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
   * @return Returns the usetempfile.
   */
  public boolean isUseTempFiles() {
    return usetempfiles;
  }

  /**
   * @param usetempfiles
   *          The usetempfiles to set.
   */
  public void setUseTempFiles( boolean usetempfiles ) {
    this.usetempfiles = usetempfiles;
  }

  /**
   * @return Returns the outputFields.
   */
  public ExcelField[] getOutputFields() {
    return outputFields;
  }

  /**
   * @param outputFields
   *          The outputFields to set.
   */
  public void setOutputFields( ExcelField[] outputFields ) {
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

  /**
   * @return Returns the templateAppend.
   */
  public boolean isTemplateAppend() {
    return templateAppend;
  }

  /**
   * @param templateAppend
   *          The templateAppend to set.
   */
  public void setTemplateAppend( boolean templateAppend ) {
    this.templateAppend = templateAppend;
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
   * @return Returns the append.
   */
  public boolean isAppend() {
    return append;
  }

  /**
   * @param append
   *          The append to set.
   */
  public void setAppend( boolean append ) {
    this.append = append;
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  public void allocate( int nrfields ) {
    outputFields = new ExcelField[nrfields];
  }

  @Override
  public Object clone() {
    ExcelOutputMeta retval = (ExcelOutputMeta) super.clone();
    int nrfields = outputFields.length;

    retval.allocate( nrfields );

    for ( int i = 0; i < nrfields; i++ ) {
      retval.outputFields[i] = (ExcelField) outputFields[i].clone();
    }

    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {

      headerEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "header" ) );
      footerEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "footer" ) );
      encoding = XMLHandler.getTagValue( stepnode, "encoding" );
      append = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "append" ) );
      String addToResult = XMLHandler.getTagValue( stepnode, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResult ) ) {
        addToResultFilenames = true;
      } else {
        addToResultFilenames = "Y".equalsIgnoreCase( addToResult );
      }

      fileName = XMLHandler.getTagValue( stepnode, "file", "name" );
      extension = XMLHandler.getTagValue( stepnode, "file", "extention" );

      doNotOpenNewFileInit =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "do_not_open_newfile_init" ) );
      createparentfolder =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "create_parent_folder" ) );

      stepNrInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "split" ) );
      dateInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_date" ) );
      timeInFilename = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_time" ) );
      SpecifyFormat = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "SpecifyFormat" ) );
      date_time_format = XMLHandler.getTagValue( stepnode, "file", "date_time_format" );
      usetempfiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "usetempfiles" ) );

      tempdirectory = XMLHandler.getTagValue( stepnode, "file", "tempdirectory" );
      autoSizeColumns = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "autosizecolums" ) );
      nullIsBlank = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "nullisblank" ) );
      protectsheet = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "protect_sheet" ) );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( stepnode, "file", "password" ) );
      splitEvery = Const.toInt( XMLHandler.getTagValue( stepnode, "file", "splitevery" ), 0 );

      templateEnabled = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "template", "enabled" ) );
      templateAppend = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "template", "append" ) );
      templateFileName = XMLHandler.getTagValue( stepnode, "template", "filename" );
      sheetname = XMLHandler.getTagValue( stepnode, "file", "sheetname" );
      Node fields = XMLHandler.getSubNode( stepnode, "fields" );
      int nrfields = XMLHandler.countNodes( fields, "field" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );

        outputFields[i] = new ExcelField();
        outputFields[i].setName( XMLHandler.getTagValue( fnode, "name" ) );
        outputFields[i].setType( XMLHandler.getTagValue( fnode, "type" ) );
        outputFields[i].setFormat( XMLHandler.getTagValue( fnode, "format" ) );
      }
      Node customnode = XMLHandler.getSubNode( stepnode, "custom" );
      header_font_name =
        getFontNameByCode( Const.NVL( XMLHandler.getTagValue( customnode, "header_font_name" ), "" ) );
      header_font_size =
        Const.NVL( XMLHandler.getTagValue( customnode, "header_font_size" ), "" + DEFAULT_FONT_SIZE );
      header_font_bold = "Y".equalsIgnoreCase( XMLHandler.getTagValue( customnode, "header_font_bold" ) );
      header_font_italic = "Y".equalsIgnoreCase( XMLHandler.getTagValue( customnode, "header_font_italic" ) );
      header_font_underline =
        getFontUnderlineByCode( Const.NVL( XMLHandler.getTagValue( customnode, "header_font_underline" ), "" ) );
      header_font_orientation =
        getFontOrientationByCode( Const
          .NVL( XMLHandler.getTagValue( customnode, "header_font_orientation" ), "" ) );
      header_font_color =
        getFontColorByCode( Const.NVL( XMLHandler.getTagValue( customnode, "header_font_color" ), ""
          + FONT_COLOR_BLACK ) );
      header_background_color =
        getFontColorByCode( Const.NVL( XMLHandler.getTagValue( customnode, "header_background_color" ), ""
          + FONT_COLOR_NONE ) );
      header_row_height = XMLHandler.getTagValue( customnode, "header_row_height" );
      header_alignment =
        getFontAlignmentByCode( Const.NVL( XMLHandler.getTagValue( customnode, "header_alignment" ), "" ) );
      header_image = XMLHandler.getTagValue( customnode, "header_image" );
      // Row font
      row_font_name = getFontNameByCode( Const.NVL( XMLHandler.getTagValue( customnode, "row_font_name" ), "" ) );
      row_font_size = Const.NVL( XMLHandler.getTagValue( customnode, "row_font_size" ), "" + DEFAULT_FONT_SIZE );
      row_font_color =
        getFontColorByCode( Const.NVL( XMLHandler.getTagValue( customnode, "row_font_color" ), ""
          + FONT_COLOR_BLACK ) );
      row_background_color =
        getFontColorByCode( Const.NVL( XMLHandler.getTagValue( customnode, "row_background_color" ), ""
          + FONT_COLOR_NONE ) );

    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to load step info from XML", e );
    }
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
    usetempfiles = false;
    tempdirectory = null;
    header_font_name = FONT_NAME_ARIAL;
    header_font_size = "" + DEFAULT_FONT_SIZE;
    header_font_bold = false;
    header_font_italic = false;
    header_font_underline = FONT_UNDERLINE_NO;
    header_font_orientation = FONT_ORIENTATION_HORIZONTAL;
    header_font_color = FONT_COLOR_BLACK;
    header_background_color = FONT_COLOR_NONE;
    header_row_height = "" + DEFAULT_ROW_HEIGHT;
    header_alignment = FONT_ALIGNMENT_LEFT;
    header_image = null;

    row_font_name = FONT_NAME_ARIAL;
    row_font_size = "" + DEFAULT_FONT_SIZE;
    row_font_color = FONT_COLOR_BLACK;
    row_background_color = FONT_COLOR_NONE;

    autoSizeColumns = false;
    headerEnabled = true;
    footerEnabled = false;
    fileName = "file";
    extension = "xls";
    doNotOpenNewFileInit = false;
    createparentfolder = false;
    stepNrInFilename = false;
    dateInFilename = false;
    timeInFilename = false;
    date_time_format = null;
    SpecifyFormat = false;
    addToResultFilenames = true;
    protectsheet = false;
    splitEvery = 0;
    templateEnabled = false;
    templateAppend = false;
    templateFileName = "template.xls";
    sheetname = "Sheet1";
    append = false;
    nullIsBlank = false;
    int i, nrfields = 0;
    allocate( nrfields );

    for ( i = 0; i < nrfields; i++ ) {
      outputFields[i] = new ExcelField();

      outputFields[i].setName( "field" + i );
      outputFields[i].setType( "Number" );
      outputFields[i].setFormat( " 0,000,000.00;-0,000,000.00" );
    }
  }

  public String[] getFiles( VariableSpace space ) {
    int copies = 1;
    int splits = 1;

    if ( stepNrInFilename ) {
      copies = 3;
    }

    if ( splitEvery != 0 ) {
      splits = 3;
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
    if ( r == null ) {
      r = new RowMeta(); // give back values
    }

    // No values are added to the row in this type of step
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder( 800 );

    retval.append( "    " ).append( XMLHandler.addTagValue( "header", headerEnabled ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "footer", footerEnabled ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "    " + XMLHandler.addTagValue( "append", append ) );
    retval.append( "    " + XMLHandler.addTagValue( "add_to_result_filenames", addToResultFilenames ) );

    retval.append( "    <file>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "name", fileName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "extention", extension ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "do_not_open_newfile_init", doNotOpenNewFileInit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "create_parent_folder", createparentfolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "split", stepNrInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_date", dateInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_time", timeInFilename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "SpecifyFormat", SpecifyFormat ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "date_time_format", date_time_format ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sheetname", sheetname ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "autosizecolums", autoSizeColumns ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nullisblank", nullIsBlank ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "protect_sheet", protectsheet ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "splitevery", splitEvery ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usetempfiles", usetempfiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "tempdirectory", tempdirectory ) );
    retval.append( "      </file>" ).append( Const.CR );

    retval.append( "    <template>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "enabled", templateEnabled ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "append", templateAppend ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filename", templateFileName ) );
    retval.append( "    </template>" ).append( Const.CR );

    retval.append( "    <fields>" ).append( Const.CR );
    for ( int i = 0; i < outputFields.length; i++ ) {
      ExcelField field = outputFields[i];

      if ( field.getName() != null && field.getName().length() != 0 ) {
        retval.append( "      <field>" ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "name", field.getName() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "type", field.getTypeDesc() ) );
        retval.append( "        " ).append( XMLHandler.addTagValue( "format", field.getFormat() ) );
        retval.append( "      </field>" ).append( Const.CR );
      }
    }
    retval.append( "    </fields>" ).append( Const.CR );

    retval.append( "    <custom>" + Const.CR );
    retval.append( "    " + XMLHandler.addTagValue( "header_font_name", getFontNameCode( header_font_name ) ) );
    retval.append( "    " + XMLHandler.addTagValue( "header_font_size", header_font_size ) );
    retval.append( "    " + XMLHandler.addTagValue( "header_font_bold", header_font_bold ) );
    retval.append( "    " + XMLHandler.addTagValue( "header_font_italic", header_font_italic ) );
    retval.append( "    "
      + XMLHandler.addTagValue( "header_font_underline", getFontUnderlineCode( header_font_underline ) ) );
    retval.append( "    "
      + XMLHandler.addTagValue( "header_font_orientation", getFontOrientationCode( header_font_orientation ) ) );
    retval.append( "    " + XMLHandler.addTagValue( "header_font_color", getFontColorCode( header_font_color ) ) );
    retval.append( "    "
      + XMLHandler.addTagValue( "header_background_color", getFontColorCode( header_background_color ) ) );
    retval.append( "    " + XMLHandler.addTagValue( "header_row_height", header_row_height ) );
    retval
      .append( "    " + XMLHandler.addTagValue( "header_alignment", getFontAlignmentCode( header_alignment ) ) );
    retval.append( "    " + XMLHandler.addTagValue( "header_image", header_image ) );
    // row font
    retval.append( "    " + XMLHandler.addTagValue( "row_font_name", getFontNameCode( row_font_name ) ) );
    retval.append( "    " + XMLHandler.addTagValue( "row_font_size", row_font_size ) );
    retval.append( "    " + XMLHandler.addTagValue( "row_font_color", getFontColorCode( row_font_color ) ) );
    retval.append( "    "
      + XMLHandler.addTagValue( "row_background_color", getFontColorCode( row_background_color ) ) );
    retval.append( "      </custom>" + Const.CR );
    return retval.toString();
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    try {
      headerEnabled = rep.getStepAttributeBoolean( id_step, "header" );
      footerEnabled = rep.getStepAttributeBoolean( id_step, "footer" );
      encoding = rep.getStepAttributeString( id_step, "encoding" );
      append = rep.getStepAttributeBoolean( id_step, "append" );

      String addToResult = rep.getStepAttributeString( id_step, "add_to_result_filenames" );
      if ( Utils.isEmpty( addToResult ) ) {
        addToResultFilenames = true;
      } else {
        addToResultFilenames = rep.getStepAttributeBoolean( id_step, "add_to_result_filenames" );
      }

      fileName = rep.getStepAttributeString( id_step, "file_name" );
      extension = rep.getStepAttributeString( id_step, "file_extention" );
      usetempfiles = rep.getStepAttributeBoolean( id_step, "usetempfiles" );
      tempdirectory = rep.getStepAttributeString( id_step, "tempdirectory" );
      doNotOpenNewFileInit = rep.getStepAttributeBoolean( id_step, "do_not_open_newfile_init" );
      createparentfolder = rep.getStepAttributeBoolean( id_step, "create_parent_folder" );
      splitEvery = (int) rep.getStepAttributeInteger( id_step, "file_split" );
      stepNrInFilename = rep.getStepAttributeBoolean( id_step, "file_add_stepnr" );
      dateInFilename = rep.getStepAttributeBoolean( id_step, "file_add_date" );
      timeInFilename = rep.getStepAttributeBoolean( id_step, "file_add_time" );
      SpecifyFormat = rep.getStepAttributeBoolean( id_step, "SpecifyFormat" );
      date_time_format = rep.getStepAttributeString( id_step, "date_time_format" );

      autoSizeColumns = rep.getStepAttributeBoolean( id_step, "autosizecolums" );
      nullIsBlank = rep.getStepAttributeBoolean( id_step, "nullisblank" );
      protectsheet = rep.getStepAttributeBoolean( id_step, "protect_sheet" );
      password = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString( id_step, "password" ) );

      templateEnabled = rep.getStepAttributeBoolean( id_step, "template_enabled" );
      templateAppend = rep.getStepAttributeBoolean( id_step, "template_append" );
      templateFileName = rep.getStepAttributeString( id_step, "template_filename" );
      sheetname = rep.getStepAttributeString( id_step, "sheetname" );
      int nrfields = rep.countNrStepAttributes( id_step, "field_name" );

      allocate( nrfields );

      for ( int i = 0; i < nrfields; i++ ) {
        outputFields[i] = new ExcelField();

        outputFields[i].setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
        outputFields[i].setType( rep.getStepAttributeString( id_step, i, "field_type" ) );
        outputFields[i].setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
      }
      // Header font
      header_font_name =
        getFontNameByCode( Const.NVL( rep.getStepAttributeString( id_step, "header_font_name" ), "" ) );
      header_font_size =
        Const.NVL( rep.getStepAttributeString( id_step, "header_font_size" ), "" + DEFAULT_FONT_SIZE );
      header_font_bold = rep.getStepAttributeBoolean( id_step, "header_font_bold" );
      header_font_italic = rep.getStepAttributeBoolean( id_step, "header_font_italic" );
      header_font_underline =
        getFontUnderlineByCode( Const.NVL( rep.getStepAttributeString( id_step, "header_font_underline" ), "" ) );
      header_font_orientation =
        getFontOrientationByCode( Const.NVL(
          rep.getStepAttributeString( id_step, "header_font_orientation" ), "" ) );
      header_font_color =
        getFontColorByCode( Const.NVL( rep.getStepAttributeString( id_step, "header_font_color" ), ""
          + FONT_COLOR_BLACK ) );
      header_background_color =
        getFontColorByCode( Const.NVL( rep.getStepAttributeString( id_step, "header_background_color" ), ""
          + FONT_COLOR_NONE ) );
      header_row_height = rep.getStepAttributeString( id_step, "header_row_height" );
      header_alignment =
        getFontAlignmentByCode( Const.NVL( rep.getStepAttributeString( id_step, "header_alignment" ), "" ) );
      header_image = rep.getStepAttributeString( id_step, "header_image" );
      // row font
      row_font_name = getFontNameByCode( Const.NVL( rep.getStepAttributeString( id_step, "row_font_name" ), "" ) );
      row_font_size = Const.NVL( rep.getStepAttributeString( id_step, "row_font_size" ), "" + DEFAULT_FONT_SIZE );
      row_font_color =
        getFontColorByCode( Const.NVL( rep.getStepAttributeString( id_step, "row_font_color" ), ""
          + FONT_COLOR_BLACK ) );
      row_background_color =
        getFontColorByCode( Const.NVL( rep.getStepAttributeString( id_step, "row_background_color" ), ""
          + FONT_COLOR_NONE ) );

    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error reading step information from the repository", e );
    }
  }

  private static String getFontNameCode( int i ) {
    if ( i < 0 || i >= font_name_code.length ) {
      return font_name_code[0];
    }
    return font_name_code[i];
  }

  private static String getFontUnderlineCode( int i ) {
    if ( i < 0 || i >= font_underline_code.length ) {
      return font_underline_code[0];
    }
    return font_underline_code[i];
  }

  private static String getFontAlignmentCode( int i ) {
    if ( i < 0 || i >= font_alignment_code.length ) {
      return font_alignment_code[0];
    }
    return font_alignment_code[i];
  }

  private static String getFontOrientationCode( int i ) {
    if ( i < 0 || i >= font_orientation_code.length ) {
      return font_orientation_code[0];
    }
    return font_orientation_code[i];
  }

  private static String getFontColorCode( int i ) {
    if ( i < 0 || i >= font_color_code.length ) {
      return font_color_code[0];
    }
    return font_color_code[i];
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "header", headerEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "footer", footerEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "encoding", encoding );
      rep.saveStepAttribute( id_transformation, id_step, "append", append );
      rep.saveStepAttribute( id_transformation, id_step, "add_to_result_filenames", addToResultFilenames );
      rep.saveStepAttribute( id_transformation, id_step, "file_name", fileName );
      rep.saveStepAttribute( id_transformation, id_step, "do_not_open_newfile_init", doNotOpenNewFileInit );
      rep.saveStepAttribute( id_transformation, id_step, "create_parent_folder", createparentfolder );
      rep.saveStepAttribute( id_transformation, id_step, "file_extention", extension );
      rep.saveStepAttribute( id_transformation, id_step, "file_split", splitEvery );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_stepnr", stepNrInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_date", dateInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "file_add_time", timeInFilename );
      rep.saveStepAttribute( id_transformation, id_step, "SpecifyFormat", SpecifyFormat );
      rep.saveStepAttribute( id_transformation, id_step, "date_time_format", date_time_format );
      rep.saveStepAttribute( id_transformation, id_step, "tempdirectory", tempdirectory );
      rep.saveStepAttribute( id_transformation, id_step, "usetempfiles", usetempfiles );
      rep.saveStepAttribute( id_transformation, id_step, "autosizecolums", autoSizeColumns );
      rep.saveStepAttribute( id_transformation, id_step, "nullisblank", nullIsBlank );
      rep.saveStepAttribute( id_transformation, id_step, "protect_sheet", protectsheet );
      rep.saveStepAttribute( id_transformation, id_step, "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveStepAttribute( id_transformation, id_step, "template_enabled", templateEnabled );
      rep.saveStepAttribute( id_transformation, id_step, "template_append", templateAppend );
      rep.saveStepAttribute( id_transformation, id_step, "template_filename", templateFileName );
      rep.saveStepAttribute( id_transformation, id_step, "sheetname", sheetname );
      for ( int i = 0; i < outputFields.length; i++ ) {
        ExcelField field = outputFields[i];

        rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
        rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
      }
      rep.saveStepAttribute( id_transformation, id_step, "header_font_name", getFontNameCode( header_font_name ) );
      rep.saveStepAttribute( id_transformation, id_step, "header_font_size", header_font_size );
      rep.saveStepAttribute( id_transformation, id_step, "header_font_bold", header_font_bold );
      rep.saveStepAttribute( id_transformation, id_step, "header_font_italic", header_font_italic );
      rep.saveStepAttribute(
        id_transformation, id_step, "header_font_underline", getFontUnderlineCode( header_font_underline ) );
      rep
        .saveStepAttribute(
          id_transformation, id_step, "header_font_orientation",
          getFontOrientationCode( header_font_orientation ) );
      rep
        .saveStepAttribute(
          id_transformation, id_step, "header_font_color", getFontColorCode( header_font_color ) );
      rep.saveStepAttribute(
        id_transformation, id_step, "header_background_color", getFontColorCode( header_background_color ) );
      rep.saveStepAttribute( id_transformation, id_step, "header_row_height", header_row_height );
      rep.saveStepAttribute(
        id_transformation, id_step, "header_alignment", getFontAlignmentCode( header_alignment ) );
      rep.saveStepAttribute( id_transformation, id_step, "header_image", header_image );
      // row font
      rep.saveStepAttribute( id_transformation, id_step, "row_font_name", getFontNameCode( row_font_name ) );
      rep.saveStepAttribute( id_transformation, id_step, "row_font_size", row_font_size );
      rep.saveStepAttribute( id_transformation, id_step, "row_font_color", getFontColorCode( row_font_color ) );
      rep.saveStepAttribute(
        id_transformation, id_step, "row_background_color", getFontColorCode( row_background_color ) );

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
          PKG, "ExcelOutputMeta.CheckResult.FieldsReceived", "" + prev.size() ), stepMeta );
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
        error_message = BaseMessages.getString( PKG, "ExcelOutputMeta.CheckResult.FieldsNotFound", error_message );
        cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "ExcelOutputMeta.CheckResult.AllFieldsFound" ), stepMeta );
        remarks.add( cr );
      }
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "ExcelOutputMeta.CheckResult.ExpectedInputOk" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "ExcelOutputMeta.CheckResult.ExpectedInputError" ), stepMeta );
      remarks.add( cr );
    }

    cr =
      new CheckResult( CheckResultInterface.TYPE_RESULT_COMMENT, BaseMessages.getString(
        PKG, "ExcelOutputMeta.CheckResult.FilesNotChecked" ), stepMeta );
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

      return null;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
    TransMeta transMeta, Trans trans ) {
    return new ExcelOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new ExcelOutputData();
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "jxl.jar", };
  }

  public static String getFontNameDesc( int i ) {
    if ( i < 0 || i >= font_name_desc.length ) {
      return font_name_desc[0];
    }
    return font_name_desc[i];
  }

  public static String getFontUnderlineDesc( int i ) {
    if ( i < 0 || i >= font_underline_desc.length ) {
      return font_underline_desc[0];
    }
    return font_underline_desc[i];
  }

  public static String getFontOrientationDesc( int i ) {
    if ( i < 0 || i >= font_orientation_desc.length ) {
      return font_orientation_desc[0];
    }
    return font_orientation_desc[i];
  }

  public static String getFontColorDesc( int i ) {
    if ( i < 0 || i >= font_color_desc.length ) {
      return font_color_desc[0];
    }
    return font_color_desc[i];
  }

  public static String getFontAlignmentDesc( int i ) {
    if ( i < 0 || i >= font_alignment_desc.length ) {
      return font_alignment_desc[0];
    }
    return font_alignment_desc[i];
  }

  public int getHeaderFontName() {
    return header_font_name;
  }

  public int getRowFontName() {
    return row_font_name;
  }

  public int getHeaderFontUnderline() {
    return header_font_underline;
  }

  public int getHeaderFontOrientation() {
    return header_font_orientation;
  }

  public int getHeaderAlignment() {
    return header_alignment;
  }

  public int getHeaderFontColor() {
    return header_font_color;
  }

  public int getRowFontColor() {
    return row_font_color;
  }

  public int getHeaderBackGroundColor() {
    return header_background_color;
  }

  public int getRowBackGroundColor() {
    return row_background_color;
  }

  public static int getFontNameByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_name_desc.length; i++ ) {
      if ( font_name_desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getFontNameByCode( tt );
  }

  private static int getFontNameByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_name_code.length; i++ ) {
      if ( font_name_code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getFontUnderlineByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_underline_desc.length; i++ ) {
      if ( font_underline_desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getFontUnderlineByCode( tt );
  }

  public static int getFontOrientationByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_orientation_desc.length; i++ ) {
      if ( font_orientation_desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getFontOrientationByCode( tt );
  }

  private static int getFontUnderlineByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_underline_code.length; i++ ) {
      if ( font_underline_code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getFontOrientationByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_orientation_code.length; i++ ) {
      if ( font_orientation_code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public static int getFontColorByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_color_desc.length; i++ ) {
      if ( font_color_desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getFontColorByCode( tt );
  }

  public static int getFontAlignmentByDesc( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_alignment_desc.length; i++ ) {
      if ( font_alignment_desc[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    // If this fails, try to match using the code.
    return getFontAlignmentByCode( tt );
  }

  private static int getFontAlignmentByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_alignment_code.length; i++ ) {
      if ( font_alignment_code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  private static int getFontColorByCode( String tt ) {
    if ( tt == null ) {
      return 0;
    }

    for ( int i = 0; i < font_color_code.length; i++ ) {
      if ( font_color_code[i].equalsIgnoreCase( tt ) ) {
        return i;
      }
    }
    return 0;
  }

  public void setHeaderFontName( int fontname ) {
    this.header_font_name = fontname;
  }

  @Injection( name = "HEADER_FONT_NAME", group = "CUSTOM" )
  public void setHeaderFontName( String fontname ) {
    this.header_font_name = getFontNameByCode( fontname );
  }

  public void setRowFontName( int fontname ) {
    this.row_font_name = fontname;
  }

  @Injection( name = "ROW_FONT_NAME", group = "CUSTOM" )
  public void setRowFontName( String fontname ) {
    this.row_font_name = getFontNameByCode( fontname );
  }

  public void setHeaderFontUnderline( int fontunderline ) {
    this.header_font_underline = fontunderline;
  }

  @Injection( name = "HEADER_FONT_UNDERLINE", group = "CUSTOM" )
  public void setHeaderFontUnderline( String fontunderline ) {
    this.header_font_underline = getFontUnderlineByCode( fontunderline );
  }

  public void setHeaderFontOrientation( int fontorientation ) {
    this.header_font_orientation = fontorientation;
  }

  @Injection( name = "HEADER_FONT_ORIENTATION", group = "CUSTOM" )
  public void setHeaderFontOrientation( String fontorientation ) {
    this.header_font_orientation = getFontOrientationByCode( fontorientation );
  }

  public void setHeaderFontColor( int fontcolor ) {
    this.header_font_color = fontcolor;
  }

  public void setRowFontColor( int fontcolor ) {
    this.row_font_color = fontcolor;
  }

  public void setHeaderBackGroundColor( int fontcolor ) {
    this.header_background_color = fontcolor;
  }

  public void setRowBackGroundColor( int fontcolor ) {
    this.row_background_color = fontcolor;
  }

  public void setHeaderAlignment( int alignment ) {
    this.header_alignment = alignment;
  }

  @Injection( name = "HEADER_ALIGNMENT", group = "CUSTOM" )
  public void setHeaderAlignment( String alignment ) {
    this.header_alignment = getFontAlignmentByCode( alignment );
  }

  public void setHeaderFontSize( String fontsize ) {
    this.header_font_size = fontsize;
  }

  public void setRowFontSize( String fontsize ) {
    this.row_font_size = fontsize;
  }

  public String getHeaderFontSize() {
    return this.header_font_size;
  }

  public String getRowFontSize() {
    return this.row_font_size;
  }

  public void setHeaderImage( String image ) {
    this.header_image = image;
  }

  public String getHeaderImage() {
    return this.header_image;
  }

  public void setHeaderRowHeight( String height ) {
    this.header_row_height = height;
  }

  public String getHeaderRowHeight() {
    return this.header_row_height;
  }

  public boolean isHeaderFontBold() {
    return this.header_font_bold;
  }

  public void setHeaderFontItalic( boolean fontitalic ) {
    this.header_font_italic = fontitalic;
  }

  public boolean isHeaderFontItalic() {
    return this.header_font_italic;
  }

  public void setHeaderFontBold( boolean font_bold ) {
    this.header_font_bold = font_bold;
  }
}
