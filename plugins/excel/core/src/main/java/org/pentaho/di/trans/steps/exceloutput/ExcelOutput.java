/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import jxl.Cell;
import jxl.Sheet;

import jxl.read.biff.BiffException;
import jxl.write.DateFormat;
import jxl.write.DateFormats;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.DateTime;
import jxl.write.NumberFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import org.apache.commons.vfs2.FileObject;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.StringHelper;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.WritableFont.FontName;
import org.pentaho.di.trans.steps.utils.CommonExcelUtils;

/**
 * Converts input rows to excel cells and then writes this information to one or more files.
 *
 * @author Matt
 * @since 7-sep-2006
 */
public class ExcelOutput extends BaseStep implements StepInterface {

  private static Class<?> PKG = ExcelOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private ExcelOutputMeta meta;
  private ExcelOutputData data;

  public ExcelOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    CommonExcelUtils.setZipBombConfiguration();
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ExcelOutputMeta) smi;
    data = (ExcelOutputData) sdi;

    Object[] r = getRow(); // This also waits for a row to be finished.
    if ( first && r != null ) {
      // get the RowMeta, rowMeta is only set when a row is read
      data.previousMeta = getInputRowMeta().clone();
      // do not set first=false, below is another part that uses first

      if ( meta.isAutoSizeColumns() ) {
        if ( meta.getOutputFields() != null && meta.getOutputFields().length > 0 ) {
          data.fieldsWidth = new int[meta.getOutputFields().length];
        } else {
          data.fieldsWidth = new int[data.previousMeta.size()];
        }
      }

      if ( meta.isDoNotOpenNewFileInit() && !openNewFile() ) {
        logError( "Couldn't open file " + buildFilename() );
        return false;
      }

      data.oneFileOpened = true;
      // If we need to write a header, do so...
      if ( meta.isHeaderEnabled() && !data.headerWrote ) {
        writeHeader();
        data.headerWrote = true;
      }
    }

    // If we split the data stream in small XLS files, we need to do this here...
    //
    boolean splitFile = meta.getSplitEvery() > 0 && ( ( getLinesOutput() ) % meta.getSplitEvery() ) == 0;
    if ( getLinesOutput() > 0 && splitFile ) {
      // Not finished: open another file...
      if ( r != null ) {
        if ( data.oneFileOpened ) {
          closeFile();
        }
        if ( !openNewFile() ) {
          logError( "Unable to open new file (split #" + data.splitnr + "..." );
          setErrors( 1 );
          return false;
        }
        // If we need to write a header, do so...
        //
        if ( meta.isHeaderEnabled() && !data.headerWrote ) {
          writeHeader();
          data.headerWrote = true;
        }
      }
    }

    if ( r == null ) {
      // no more input to be expected...
      setOutputDone();
      return false;
    }

    boolean result = writeRowToFile( r );
    if ( !result ) {
      setErrors( 1 );
      stopAll();
      return false;
    }

    putRow( data.previousMeta, r ); // in case we want it to go further...

    if ( checkFeedback( getLinesOutput() ) ) {
      if ( log.isBasic() ) {
        logBasic( "linenr " + getLinesOutput() );
      }
    }

    return result;
  }

  private boolean writeRowToFile( Object[] r ) {
    Object v;

    try {
      if ( first ) {
        first = false;

        data.fieldnrs = new int[meta.getOutputFields().length];
        for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
          data.fieldnrs[i] = data.previousMeta.indexOfValue( meta.getOutputFields()[i].getName() );
          if ( data.fieldnrs[i] < 0 ) {
            logError( "Field [" + meta.getOutputFields()[i].getName() + "] couldn't be found in the input stream!" );
            setErrors( 1 );
            stopAll();
            return false;
          }
        }
      }

      if ( meta.getOutputFields() == null || meta.getOutputFields().length == 0 ) {
        /*
         * Write all values in stream to text file.
         */
        for ( int i = 0; i < data.previousMeta.size(); i++ ) {
          v = r[i];
          if ( !writeField( v, data.previousMeta.getValueMeta( i ), null, i ) ) {
            return false;
          }

        }
        // go to the next line
        data.positionX = 0;
        data.positionY++;

      } else {
        /*
         * Only write the fields specified!
         */
        for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
          v = r[data.fieldnrs[i]];

          if ( !writeField( v, data.previousMeta.getValueMeta( data.fieldnrs[i] ), meta.getOutputFields()[i], i ) ) {
            return false;
          }
        }

        // go to the next line
        data.positionX = 0;
        data.positionY++;
      }
    } catch ( Exception e ) {
      logError( "Error writing line :" + e.toString() );
      return false;
    }

    incrementLinesOutput();

    return true;
  }

  private boolean createParentFolder( FileObject file ) {
    boolean retval = true;
    // Check for parent folder
    FileObject parentfolder = null;
    try {
      // Get parent folder
      parentfolder = file.getParent();
      if ( parentfolder.exists() ) {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "ExcelOutput.Log.ParentFolderExist", parentfolder
            .getName().toString() ) );
        }
      } else {
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "ExcelOutput.Log.ParentFolderNotExist", parentfolder
            .getName().toString() ) );
        }
        if ( meta.isCreateParentFolder() ) {
          parentfolder.createFolder();
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "ExcelOutput.Log.ParentFolderCreated", parentfolder
              .getName().toString() ) );
          }
        } else {
          retval = false;
          logError( BaseMessages.getString( PKG, "ExcelOutput.Error.CanNotFoundParentFolder", parentfolder
            .getName().toString(), file.getName().toString() ) );
        }
      }
    } catch ( Exception e ) {
      retval = false;
      logError( BaseMessages.getString( PKG, "ExcelOutput.Log.CouldNotCreateParentFolder", parentfolder
        .getName().toString() ) );
    } finally {
      if ( parentfolder != null ) {
        try {
          parentfolder.close();
        } catch ( Exception ex ) {
          // Ignore
        }
      }
    }
    return retval;
  }

  /**
   * Write a value to Excel, increasing data.positionX with one afterwards.
   *
   * @param v
   *          The value to write
   * @param vMeta
   *          The valueMeta to write
   * @param excelField
   *          the field information (if any, otherwise : null)
   * @param column
   *          the excel column for getting the template format
   * @return <code>true</code> if write succeeded
   */
  private boolean writeField( Object v, ValueMetaInterface vMeta, ExcelField excelField, int column ) {
    return writeField( v, vMeta, excelField, column, false );
  }

  /**
   * Write a value to Excel, increasing data.positionX with one afterwards.
   *
   * @param v
   *          The value to write
   * @param vMeta
   *          The valueMeta to write
   * @param excelField
   *          the field information (if any, otherwise : null)
   * @param column
   *          the excel column for getting the template format
   * @param isHeader
   *          true if this is part of the header/footer
   * @return <code>true</code> if write succeeded
   */
  private boolean writeField( Object v, ValueMetaInterface vMeta, ExcelField excelField, int column,
    boolean isHeader ) {
    try {
      String hashName = vMeta.getName();
      if ( isHeader ) {
        hashName = "____header_field____"; // all strings, can map to the same format.
      }

      WritableCellFormat cellFormat = data.formats.get( hashName );

      // when template is used, take over the column format
      if ( cellFormat == null && meta.isTemplateEnabled() && !isHeader ) {
        try {
          if ( column < data.templateColumns ) {
            CellFormat format = data.sheet.getColumnView( column ).getFormat();
            if ( format != null ) {
              cellFormat = new WritableCellFormat( format );
              data.formats.put( hashName, cellFormat ); // save for next time around...
            }
          }
        } catch ( RuntimeException e ) {
          // ignore if the column is not found, format as usual
        }
      }
      if ( meta.isAutoSizeColumns() ) {
        // prepare auto size columns
        int vlen = vMeta.getName().length();
        if ( !isHeader && v != null ) {
          vlen = v.toString().trim().length();
        }
        if ( vlen > 0 && vlen > data.fieldsWidth[column] ) {
          data.fieldsWidth[column] = vlen + 1;
        }
      }

      // Do we need to use a specific format to header?
      if ( isHeader ) {
        // Set font for header and footer+
        // row to write in
        int rowNumber = data.sheet.getColumn( data.positionX ).length;
        data.sheet.addCell( new Label( data.positionX, rowNumber, vMeta.getName(), data.headerCellFormat ) );
        if ( cellFormat == null ) {
          data.formats.put( hashName, data.headerCellFormat ); // save for next time around...
        }
      } else {
        // Will write new row after existing ones in current column
        data.positionY = data.sheet.getColumn( data.positionX ).length;
        switch ( vMeta.getType() ) {
          case ValueMetaInterface.TYPE_DATE: {
            if ( v != null && vMeta.getDate( v ) != null ) {
              if ( cellFormat == null ) {
                if ( excelField != null && excelField.getFormat() != null ) {
                  DateFormat dateFormat = new DateFormat( excelField.getFormat() );

                  if ( data.writableFont != null ) {
                    cellFormat = new WritableCellFormat( data.writableFont, dateFormat );
                    if ( data.rowFontBackgoundColour != null ) {
                      cellFormat.setBackground( data.rowFontBackgoundColour );
                    }
                  } else {
                    cellFormat = new WritableCellFormat( dateFormat );
                  }
                } else {
                  if ( data.writableFont != null ) {
                    cellFormat = new WritableCellFormat( data.writableFont, DateFormats.FORMAT9 );
                    if ( data.rowFontBackgoundColour != null ) {
                      cellFormat.setBackground( data.rowFontBackgoundColour );
                    }
                  } else {
                    cellFormat = new WritableCellFormat( DateFormats.FORMAT9 );
                  }
                }
                data.formats.put( hashName, cellFormat ); // save for next time around...
              }
              DateTime dateTime = new DateTime( data.positionX, data.positionY, vMeta.getDate( v ), cellFormat );
              data.sheet.addCell( dateTime );
            } else if ( !meta.isNullBlank() ) {
              data.sheet.addCell( new Label( data.positionX, data.positionY, "" ) );
            }
            break;
          }
          default:
            // fallthrough
            // Output the data value as a string
          case ValueMetaInterface.TYPE_STRING:
          case ValueMetaInterface.TYPE_BOOLEAN:
          case ValueMetaInterface.TYPE_BINARY: {
            if ( cellFormat == null ) {
              cellFormat = new WritableCellFormat( data.writableFont );
              if ( data.rowFontBackgoundColour != null ) {
                cellFormat.setBackground( data.rowFontBackgoundColour );
              }
              data.formats.put( hashName, cellFormat );
            }
            if ( v != null ) {
              Label label = new Label( data.positionX, data.positionY, vMeta.getString( v ), cellFormat );
              data.sheet.addCell( label );
            } else if ( !meta.isNullBlank() ) {
              data.sheet.addCell( new Label( data.positionX, data.positionY, "" ) );
            }
            break;
          }
          case ValueMetaInterface.TYPE_NUMBER:
          case ValueMetaInterface.TYPE_BIGNUMBER:
          case ValueMetaInterface.TYPE_INTEGER: {
            if ( v != null ) {
              if ( cellFormat == null ) {
                String format;
                if ( excelField != null && excelField.getFormat() != null ) {
                  format = excelField.getFormat();
                } else {
                  format = "###,###.00";
                }
                NumberFormat numberFormat = new NumberFormat( format );

                if ( data.writableFont != null ) {
                  cellFormat = new WritableCellFormat( data.writableFont, numberFormat );
                  if ( data.rowFontBackgoundColour != null ) {
                    cellFormat.setBackground( data.rowFontBackgoundColour );
                  }
                } else {
                  cellFormat = new WritableCellFormat( numberFormat );
                }

                data.formats.put( vMeta.getName(), cellFormat ); // save for next time around...
              }
              jxl.write.Number number =
                new jxl.write.Number( data.positionX, data.positionY, vMeta.getNumber( v ), cellFormat );
              data.sheet.addCell( number );
            } else if ( !meta.isNullBlank() ) {
              data.sheet.addCell( new Label( data.positionX, data.positionY, "" ) );
            }
            break;
          }
        }
      }
    } catch ( Exception e ) {
      logError( "Error writing field (" + data.positionX + "," + data.positionY + ") : " + e.toString() );
      logError( Const.getStackTracker( e ) );
      return false;
    } finally {
      data.positionX++; // always advance :-)
    }
    return true;
  }

  private boolean writeHeader() {
    boolean retval = false;

    try {
      // If we have fields specified: list them in this order!
      if ( meta.getOutputFields() != null && meta.getOutputFields().length > 0 ) {
        for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
          String fieldName = meta.getOutputFields()[i].getName();
          ValueMetaInterface vMeta = new ValueMetaString( fieldName );
          writeField( fieldName, vMeta, null, i, true );
        }
      } else {
        if ( data.previousMeta != null ) { // Just put all field names in the header/footer
          for ( int i = 0; i < data.previousMeta.size(); i++ ) {
            String fieldName = data.previousMeta.getFieldNames()[i];
            ValueMetaInterface vMeta = new ValueMetaString( fieldName );
            writeField( fieldName, vMeta, null, i, true );
          }
        }
      }
    } catch ( Exception e ) {
      logError( "Error writing header line: " + e.toString() );
      logError( Const.getStackTracker( e ) );
      retval = true;
    } finally {
      data.positionX = 0;
      data.positionY++;
    }

    return retval;
  }

  public String buildFilename() {
    return meta.buildFilename( this, getCopy(), data.splitnr );
  }

  public boolean openNewFile() {
    boolean retval = false;

    try {
      // Static filename
      data.realFilename = buildFilename();
      data.file = KettleVFS.getFileObject( data.realFilename, getTransMeta() );
      if ( meta.isCreateParentFolder() ) {
        if ( !createParentFolder( data.file ) ) {
          return retval;
        }
      }
      data.realFilename = KettleVFS.getFilename( data.file );
      addFilenameToResult();
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "ExcelOutput.Log.OpeningFile", data.realFilename ) );
      }

      // The Sheet name to be used
      String sheetName = "Sheet1";
      if ( !Utils.isEmpty( data.realSheetname ) ) {
        sheetName = data.realSheetname;
      }

      // Create the workbook
      File targetFile = new File( KettleVFS.getFilename( data.file ) );
      if ( !meta.isTemplateEnabled() ) {
        if ( meta.isAppend() && targetFile.exists() ) {
          Workbook targetFileWorkbook = Workbook.getWorkbook( targetFile );
          data.workbook = Workbook.createWorkbook( targetFile, targetFileWorkbook );

          // Do not rewrite header
          meta.setHeaderEnabled( false );
        } else {
          // Create a new Workbook
          data.outputStream = KettleVFS.getOutputStream( data.file, false );
          data.workbook = Workbook.createWorkbook( data.outputStream, data.ws );
        }

        // Check if the sheet already exists
        data.sheet = data.workbook.getSheet( sheetName );
        if ( null == data.sheet ) {
          // It does not, so create it at the end of the workbook
          data.sheet = data.workbook.createSheet( sheetName, data.workbook.getNumberOfSheets() );
          // It's a new sheet, so let's add the header
          meta.setHeaderEnabled( true );
        }
      } else {
        String templateFilename = environmentSubstitute( meta.getTemplateFileName() );
        try ( FileObject templateFile = KettleVFS.getFileObject( templateFilename, getTransMeta() ) ) {
          // create the openFile from the template
          Workbook templateWorkbook = Workbook.getWorkbook( KettleVFS.getInputStream( templateFile ), data.ws );

          if ( meta.isAppend() && targetFile.exists() && isTemplateContained( templateWorkbook, targetFile ) ) {
            Workbook targetFileWorkbook = Workbook.getWorkbook( targetFile );
            data.workbook = Workbook.createWorkbook( targetFile, targetFileWorkbook );

            // Do not rewrite header
            meta.setHeaderEnabled( false );
          } else {
            data.outputStream = KettleVFS.getOutputStream( data.file, false );
            data.workbook = Workbook.createWorkbook( data.outputStream, templateWorkbook );
          }

          // Check if the sheet already exists
          data.sheet = data.workbook.getSheet( sheetName );
          if ( null == data.sheet ) {
            logError( "WorkSheet Name is null or different from that of Template WorkSheet Name" );
            // Create the sheet at the end of the workbook using the first sheet as template
            data.workbook.copySheet( 0, sheetName, data.workbook.getNumberOfSheets() );
            data.sheet = data.workbook.getSheet( sheetName );
            // It's a new sheet, so let's add the header
            meta.setHeaderEnabled( true );
          }
        }
      }

      if ( meta.isSheetProtected() ) {
        // Protect Sheet by setting password
        data.sheet.getSettings().setProtected( true );
        String realPassword = Utils.resolvePassword( variables, meta.getPassword() );
        data.sheet.getSettings().setPassword( realPassword );
      }

      // Set the initial position...
      data.positionX = 0;
      if ( meta.isTemplateEnabled() && meta.isTemplateAppend() ) {
        data.positionY = data.sheet.getColumn( data.positionX ).length;
      } else {
        data.positionY = 0;
      }

      if ( data.headerImage != null ) {
        // Put an image (LEFT TOP Corner)
        data.sheet.addImage( data.headerImage );
        data.positionY += Math.round( data.headerImageHeight );
      }

      // Sets the height of the specified row, as well as its collapse status
      // height the row height in characters
      if ( data.Headerrowheight > 0 && data.Headerrowheight != ExcelOutputMeta.DEFAULT_ROW_HEIGHT ) {
        data.sheet.setRowView( data.positionY, data.Headerrowheight );
      }

      try {
        setFonts();
      } catch ( Exception we ) {
        logError( "Error preparing fonts, colors for header and rows: " + we.toString() );
        return retval;
      }

      data.headerWrote = false;
      data.splitnr++;
      data.oneFileOpened = true;
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "ExcelOutput.Log.FileOpened", data.file.toString() ) );
      }
      retval = true;
    } catch ( Exception e ) {
      logError( "Error opening new file", e );
      setErrors( 1 );
    }

    return retval;
  }

  private boolean isTemplateContained( Workbook templateWorkbook, File targetFile )
       throws IOException, BiffException {
    Workbook targetFileWorkbook = Workbook.getWorkbook( targetFile );
    int templateWorkbookNumberOfSheets = templateWorkbook.getNumberOfSheets();
    int targetWorkbookNumberOfSheets = targetFileWorkbook.getNumberOfSheets();
    if ( templateWorkbookNumberOfSheets > targetWorkbookNumberOfSheets ) {
      return false;
    }

    for ( int worksheetNumber = 0; worksheetNumber < templateWorkbookNumberOfSheets; worksheetNumber++ ) {
      Sheet templateWorkbookSheet = templateWorkbook.getSheet( worksheetNumber );
      Sheet targetWorkbookSheet = targetFileWorkbook.getSheet( worksheetNumber );
      int templateWorkbookSheetColumns = templateWorkbookSheet.getColumns();
      int targetWorkbookSheetColumns = targetWorkbookSheet.getColumns();
      if ( templateWorkbookSheetColumns > targetWorkbookSheetColumns ) {
        return false;
      }
      int templateWorkbookSheetRows = templateWorkbookSheet.getRows();
      int targetWorkbookSheetRows = targetWorkbookSheet.getRows();
      if ( templateWorkbookSheetRows > targetWorkbookSheetRows ) {
        return false;
      }
      for ( int currentRowNumber = 0; currentRowNumber < templateWorkbookSheetRows; currentRowNumber++ ) {
        Cell[] templateWorkbookSheetRow = templateWorkbookSheet.getRow( currentRowNumber );
        Cell[] targetWorkbookSheetRow = targetWorkbookSheet.getRow( currentRowNumber );
        if ( templateWorkbookSheetRow.length > targetWorkbookSheetRow.length ) {
          return false;
        }
        for ( int currentCellNumber = 0; currentCellNumber < templateWorkbookSheetRow.length; currentCellNumber++ ) {
          Cell templateWorksheetCell = templateWorkbookSheetRow[currentCellNumber];
          Cell targetWorksheetCell = targetWorkbookSheetRow[currentCellNumber];
          if ( !templateWorksheetCell.getContents().equals( targetWorksheetCell.getContents() ) ) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean closeFile() {
    boolean retval = false;
    String filename = null;
    try {
      if ( meta.isFooterEnabled() ) {
        writeHeader();
      }
      if ( data.workbook != null ) {
        if ( data.fieldsWidth != null ) {
          if ( meta.isAutoSizeColumns() ) {
            // auto resize columns
            int nrfields = data.fieldsWidth.length;
            for ( int i = 0; i < nrfields; i++ ) {
              data.sheet.setColumnView( i, data.fieldsWidth[i] );
            }
          }
          data.fieldsWidth = null;
        }
        data.ws.setWriteAccess( reEncodeWriteAccessIfNecessary( data.ws.getWriteAccess() ) );
        data.workbook.write();
        data.workbook.close();
        data.workbook = null;
        if ( data.outputStream != null ) {
          data.outputStream.close();
          data.outputStream = null;
        }
        if ( data.sheet != null ) {
          data.sheet = null;
        }
        if ( data.file != null ) {
          filename = data.file.toString();
          data.file.close();
          data.file = null;
        }
      }
      data.formats.clear();
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "ExcelOutput.Log.FileClosed", filename ) );
      }

      retval = true;
      data.oneFileOpened = false;
    } catch ( Exception e ) {
      logError( "Unable to close openFile file : " + data.file.toString(), e );
      setErrors( 1 );
    }
    return retval;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelOutputMeta) smi;
    data = (ExcelOutputData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.splitnr = 0;
      data.realSheetname = environmentSubstitute( meta.getSheetname() );

      data.ws = new WorkbookSettings();
      if ( meta.isUseTempFiles() ) {
        data.ws.setUseTemporaryFileDuringWrite( true );
        String realdir = environmentSubstitute( meta.getTempDirectory() );
        if ( !Utils.isEmpty( realdir ) ) {
          File file = new File( realdir );
          if ( !file.exists() ) {
            logError( BaseMessages.getString( PKG, "ExcelInputLog.TempDirectoryNotExist", realdir ) );
            return false;
          }
          data.ws.setTemporaryFileDuringWriteDirectory( file );
        }
      }
      data.ws.setLocale( Locale.getDefault() );
      data.Headerrowheight = Const.toInt( environmentSubstitute( meta.getHeaderRowHeight() ), -1 );
      data.realHeaderImage = environmentSubstitute( meta.getHeaderImage() );
      if ( !Utils.isEmpty( meta.getEncoding() ) ) {
        data.ws.setEncoding( meta.getEncoding() );
      }

      if ( !meta.isDoNotOpenNewFileInit() ) {
        data.oneFileOpened = true;
        if ( openNewFile() ) {
          return true;
        } else {
          logError( "Couldn't open file " + meta.getFileName() );
          setErrors( 1L );
          stopAll();
        }
      } else {
        return true;
      }
    }
    return false;
  }

  private void addFilenameToResult() throws KettleException {
    try {
      if ( meta.isAddToResultFiles() ) {
        // Add this to the result file names...
        ResultFile resultFile =  new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname() );
        resultFile.setComment( "This file was created with an Excel output step by Pentaho Data Integration" );
        addResultFile( resultFile );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to add filename to the result", e );
    }
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelOutputMeta) smi;
    data = (ExcelOutputData) sdi;

    if ( data.oneFileOpened ) {
      closeFile();
    }
    if ( data.file != null ) {
      try {
        data.file.close();
        data.file = null;
      } catch ( Exception e ) {
        logDetailed( BaseMessages.getString( PKG, "ExcelOutput.Log.UnableToCloseFileDuringDispose", data.file.getPublicURIString() ), e );
      }
    }
    data.fieldsWidth = null;
    data.headerImage = null;
    data.writableFont = null;
    data.ws = null;
    super.dispose( smi, sdi );
  }

  private void setFonts() throws Exception {
    // --- Set Header font
    int headerFontSize = Const.toInt( environmentSubstitute( meta.getHeaderFontSize() ), ExcelOutputMeta.DEFAULT_FONT_SIZE );
    // Set font name
    FontName headerFontName = ExcelFontMap.getFontName( meta.getHeaderFontName() );
    // Set UnderlineStyle
    UnderlineStyle underline = ExcelFontMap.getUnderlineStyle( meta.getHeaderFontUnderline() );

    WritableFont writableHeaderFont = null;
    if ( meta.isHeaderFontBold() ) {
      writableHeaderFont = new WritableFont( headerFontName, headerFontSize, WritableFont.BOLD, meta.isHeaderFontItalic(), underline );
    } else {
      writableHeaderFont = new WritableFont( headerFontName, headerFontSize, WritableFont.NO_BOLD, meta.isHeaderFontItalic(), underline );
    }

    // Header font color
    Colour fontHeaderColour = ExcelFontMap.getColour( meta.getHeaderFontColor(), Colour.BLACK );
    if ( !fontHeaderColour.equals( Colour.BLACK ) ) {
      writableHeaderFont.setColour( fontHeaderColour );
    }
    data.headerCellFormat = new WritableCellFormat( writableHeaderFont );

    // Header background color
    if ( meta.getHeaderBackGroundColor() != ExcelOutputMeta.FONT_COLOR_NONE ) {
      data.headerCellFormat.setBackground( ExcelFontMap.getColour( meta.getHeaderBackGroundColor(), null ) );
    }

    // Set alignment
    data.headerCellFormat = ExcelFontMap.getAlignment( meta.getHeaderAlignment(), data.headerCellFormat );
    data.headerCellFormat = ExcelFontMap.getOrientation( meta.getHeaderFontOrientation(), data.headerCellFormat );

    // Do we need to put a image on the header
    if ( !Utils.isEmpty( data.realHeaderImage ) ) {
      InputStream imageStream = null;
      try ( FileObject imageFile = KettleVFS.getFileObject( data.realHeaderImage ) ) {
        if ( !imageFile.exists() ) {
          throw new KettleException( BaseMessages.getString( PKG, "ExcelInputLog.ImageFileNotExists", data.realHeaderImage ) );
        }
        data.realHeaderImage = KettleVFS.getFilename( imageFile );
        // Put an image
        Dimension m = ExcelFontMap.getImageDimension( data.realHeaderImage );
        data.headerImageWidth = m.getWidth() * 0.016;
        data.headerImageHeight = m.getHeight() * 0.0625;

        byte[] imageData = new byte[(int) imageFile.getContent().getSize()];
        imageStream = KettleVFS.getInputStream( imageFile );
        imageStream.read( imageData );

        data.headerImage = new WritableImage( 0, 0, data.headerImageWidth, data.headerImageHeight, imageData );
      } catch ( Exception e ) {
        throw new KettleException( e );
      }
    }

    // --- Set rows font
    // Set font size
    int rowFontSize = Const.toInt( environmentSubstitute( meta.getRowFontSize() ), ExcelOutputMeta.DEFAULT_FONT_SIZE );
    // Set font name
    FontName rowFontName = ExcelFontMap.getFontName( meta.getRowFontName() );

    data.writableFont = new WritableFont( rowFontName, rowFontSize, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE );

    // Row font color
    Colour rowFontColour = ExcelFontMap.getColour( meta.getRowFontColor(), Colour.BLACK );
    if ( !fontHeaderColour.equals( Colour.BLACK ) ) {
      data.writableFont.setColour( rowFontColour );
    }

    // Set rows background color if needed
    if ( meta.getRowBackGroundColor() != ExcelOutputMeta.FONT_COLOR_NONE ) {
      data.rowFontBackgoundColour = ExcelFontMap.getColour( meta.getRowBackGroundColor(), null );
    }
  }
  /*
   * Returns the writeAccess, re-encoded if necessary
   * fixes http://jira.pentaho.com/browse/PDI-14022
   **/
  private String reEncodeWriteAccessIfNecessary( String writeAccess ) {

    if ( writeAccess == null || writeAccess.length() == 0 ) {
      return writeAccess;
    }
    byte[] data = new byte[112];
    try {
      // jxl reads writeAccess with "UnicodeLittle" encoding, but will try to write later with "file.encoding"
      // this throws an ArrayIndexOutOfBoundsException in *nix systems
      StringHelper.getBytes( writeAccess, data, 0 );
    } catch ( ArrayIndexOutOfBoundsException e ) {
      try {
        // properly re-encoding string from UnicodeLittle, removing BOM characters
        return new String( writeAccess.getBytes( "UnicodeLittle" ), System.getProperty( "file.encoding" ) ).substring( 2 );
      } catch ( UnsupportedEncodingException e1 ) {
        logError( Const.getStackTracker( e ) );
      }
    }
    return writeAccess;
  }
}
