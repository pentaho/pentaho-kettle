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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
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
import org.pentaho.di.trans.steps.utils.CommonExcelUtils;
import org.pentaho.di.workarounds.BufferedOutputStreamWithCloseDetection;

public class ExcelWriterStep extends BaseStep implements StepInterface {

  public static final String STREAMER_FORCE_RECALC_PROP_NAME = "KETTLE_EXCEL_WRITER_STREAMER_FORCE_RECALCULATE";
  public static final String XLSX = "xlsx";
  private static final int STREAMING_WINDOW_SIZE = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;

  private ExcelWriterStepData data;
  private ExcelWriterStepMeta meta;

  private static Class<?> PKG = ExcelWriterStepMeta.class; // for i18n

  public ExcelWriterStep( StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
    super( s, stepDataInterface, c, t, dis );
    CommonExcelUtils.setZipBombConfiguration();
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (ExcelWriterStepMeta) smi;
    data = (ExcelWriterStepData) sdi;

    // get next row
    Object[] r = getRow();

    // first row initialization
    if ( first ) {

      first = false;
      if ( r == null ) {
        data.outputRowMeta = new RowMeta();
        data.inputRowMeta = new RowMeta();
      } else {
        data.outputRowMeta = getInputRowMeta().clone();
        data.inputRowMeta = getInputRowMeta().clone();
      }

      // if we are supposed to init the file up front, here we go
      if ( !meta.isDoNotOpenNewFileInit() ) {
        data.firstFileOpened = true;

        try {
          prepareNextOutputFile();
        } catch ( KettleException e ) {
          logError( BaseMessages.getString( PKG, "ExcelWriterStep.Exception.CouldNotPrepareFile",
            environmentSubstitute( meta.getFileName() ) ) );
          setErrors( 1L );
          stopAll();
          return false;
        }
      }

      if ( r != null ) {
        // if we are supposed to init the file delayed, here we go
        if ( meta.isDoNotOpenNewFileInit() ) {
          data.firstFileOpened = true;
          prepareNextOutputFile();
        }

        // Let's remember where the fields are in the input row
        int outputFieldsCount = meta.getOutputFields().length;
        data.commentauthorfieldnrs = new int[outputFieldsCount];
        data.commentfieldnrs = new int[outputFieldsCount];
        data.linkfieldnrs = new int[outputFieldsCount];
        data.fieldnrs = new int[outputFieldsCount];

        int i = 0;
        for ( ExcelWriterStepField outputField : meta.getOutputFields() ) {
          // Output Fields
          String outputFieldName = outputField.getName();
          data.fieldnrs[i] = data.inputRowMeta.indexOfValue( outputFieldName );
          if ( data.fieldnrs[i] < 0 ) {
            logError( "Field [" + outputFieldName + "] couldn't be found in the input stream!" );
            setErrors( 1 );
            stopAll();
            return false;
          }

          // Comment Fields
          String commentField = outputField.getCommentField();
          data.commentfieldnrs[i] = data.inputRowMeta.indexOfValue( commentField );
          if ( data.commentfieldnrs[i] < 0 && !Utils.isEmpty( commentField ) ) {
            logError( "Comment Field [" + commentField + "] couldn't be found in the input stream!" );
            setErrors( 1 );
            stopAll();
            return false;
          }

          // Comment Author Fields
          String commentAuthorField = outputField.getCommentAuthorField();
          data.commentauthorfieldnrs[i] = data.inputRowMeta.indexOfValue( commentAuthorField );
          if ( data.commentauthorfieldnrs[i] < 0 && !Utils.isEmpty( commentAuthorField ) ) {
            logError( "Comment Author Field [" + commentAuthorField + "] couldn't be found in the input stream!" );
            setErrors( 1 );
            stopAll();
            return false;
          }

          // Link Fields
          String hyperlinkField = outputField.getHyperlinkField();
          data.linkfieldnrs[i] = data.inputRowMeta.indexOfValue( hyperlinkField );
          if ( data.linkfieldnrs[i] < 0 && !Utils.isEmpty( hyperlinkField ) ) {
            logError( "Link Field [" + hyperlinkField + "] couldn't be found in the input stream!" );
            setErrors( 1 );
            stopAll();
            return false;
          }

          // Increase counter
          ++i;
        }
      }
    }

    if ( r != null ) {
      // File Splitting Feature, is it time to create a new file?
      if ( !meta.isAppendLines() && meta.getSplitEvery() > 0 && data.datalines > 0
          && data.datalines % meta.getSplitEvery() == 0 ) {
        closeOutputFile();
        prepareNextOutputFile();
      }

      writeNextLine( r );
      incrementLinesOutput();

      data.datalines++;

      // pass on the row unchanged
      putRow( data.outputRowMeta, r );

      // Some basic logging
      if ( checkFeedback( getLinesOutput() ) ) {
        if ( log.isBasic() ) {
          logBasic( "Linenr " + getLinesOutput() );
        }
      }
      return true;
    } else {
      setOutputDone();
      return false;
    }
  }

  // clears all memory that POI may hold
  private void clearWorkbookMem() {
    data.file = null;
    data.sheet = null;
    data.innerSheet = Optional.empty();
    IOUtils.closeQuietly( data.wb );
    data.wb = null;
    data.clearStyleCache( 0 );
  }

  @Override
  public boolean afterFinishProcessing( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( data.wb != null ) {
      try {
        closeOutputFile();
      } catch ( KettleException e ) {
        logDetailed( e.getMessage() );
        return false;
      }
    }
    setOutputDone();
    clearWorkbookMem();
    return true;
  }

  private void closeOutputFile() throws KettleException {
    try ( BufferedOutputStreamWithCloseDetection out =
        new BufferedOutputStreamWithCloseDetection( KettleVFS.getOutputStream( data.file, false ) ) ) {
      // may have to write a footer here
      if ( meta.isFooterEnabled() ) {
        writeHeader();
      }
      // handle auto size for columns
      if ( meta.isAutoSizeColums() ) {

        // track all columns for autosizing if using streaming worksheet
        if ( data.sheet instanceof SXSSFSheet ) {
          ( (SXSSFSheet) data.sheet ).trackAllColumnsForAutoSizing();
        }

        if ( meta.getOutputFields() == null || meta.getOutputFields().length == 0 ) {
          for ( int i = 0; i < data.inputRowMeta.size(); i++ ) {
            data.sheet.autoSizeColumn( i + data.startingCol );
          }
        } else {
          for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
            data.sheet.autoSizeColumn( i + data.startingCol );
          }
        }
      }

      if ( meta.isExtendDataValidationRanges() ) {
        extendDataValidationRanges();
      }

      // force recalculation of formulas if requested
      if ( meta.isForceFormulaRecalculation() ) {
        recalculateAllWorkbookFormulas();
      }
      data.wb.write( out );
    } catch ( IOException e ) {
      throw new KettleException( e );
    } finally {
      IOUtils.closeQuietly( data.wb );
      data.wb = null;
    }
  }

  private void extendDataValidationRanges() {
    DataValidationHelper helper = data.sheet.getDataValidationHelper();
    for ( DataValidation validation : data.sheet.getDataValidations() ) {
      CellRangeAddressList rangeList = validation.getRegions();
      // extend ranges that include data rows
      boolean updated = false;
      for ( CellRangeAddress range : rangeList.getCellRangeAddresses() ) {
        // extend any ranges that apply to the first data row but not the last
        if ( range.containsRow( data.startingRow ) && !range.containsRow( data.posY ) ) {
          range.setLastRow( data.posY );
          updated = true;
        }
      }
      if ( updated ) {
        // modified ranges won't get applied, we need to add new validations.
        DataValidation newValidation = helper.createValidation( validation.getValidationConstraint(), validation.getRegions() );
        data.sheet.addValidationData( newValidation );
      }
    }
  }

  // recalculates all formula fields for the entire workbook
  // package-local visibility for testing purposes
  void recalculateAllWorkbookFormulas() {
    if ( data.wb instanceof XSSFWorkbook ) {
      // XLSX needs full reevaluation
      FormulaEvaluator evaluator = data.wb.getCreationHelper().createFormulaEvaluator();
      for ( int sheetNum = 0; sheetNum < data.wb.getNumberOfSheets(); sheetNum++ ) {
        Sheet sheet = data.wb.getSheetAt( sheetNum );
        for ( Row r : sheet ) {
          for ( Cell c : r ) {
            if ( c.getCellType() == CellType.FORMULA ) {
              evaluator.evaluateFormulaCell( c );
            }
          }
        }
      }
    } else if ( data.wb instanceof HSSFWorkbook ) {
      // XLS supports a "dirty" flag to have excel recalculate everything when a sheet is opened
      for ( int sheetNum = 0; sheetNum < data.wb.getNumberOfSheets(); sheetNum++ ) {
        HSSFSheet sheet = ( (HSSFWorkbook) data.wb ).getSheetAt( sheetNum );
        sheet.setForceFormulaRecalculation( true );
      }
    } else {
      String forceRecalc = getVariable( STREAMER_FORCE_RECALC_PROP_NAME, "N" );
      if ( "Y".equals( forceRecalc ) ) {
        data.wb.setForceFormulaRecalculation( true );
      }
    }
  }

  public void writeNextLine( Object[] r ) throws KettleException {
    try {
      openLine();
      Row xlsRow = getOrCreateRow( data.posY );
      if ( meta.getOutputFields() == null || meta.getOutputFields().length == 0 ) {
        // Write all values in stream to text file.
        int nr = data.inputRowMeta.size();
        data.clearStyleCache( nr );
        data.linkfieldnrs = new int[nr];
        data.commentfieldnrs = new int[nr];
        for ( int i = 0; i < nr; i++ ) {
          writeField( r[i], data.inputRowMeta.getValueMeta( i ), null, xlsRow, data.posX++, r, i, false );
        }
      } else {
        /*
         * Only write the fields specified!
         */
        for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
          Object v = r[data.fieldnrs[i]];
          writeField( v, data.inputRowMeta.getValueMeta( data.fieldnrs[i] ), meta.getOutputFields()[i], xlsRow,
            data.posX++, r, i, false );
        }

      }
      // go to the next line
      data.posX = data.startingCol;
      data.posY++;
    } catch ( Exception e ) {
      logError( "Error writing line :" + e.toString() );
      throw new KettleException( e );
    }
  }

  private Comment createCellComment( String author, String comment ) {
    // comments only supported for XLSX
    if ( data.sheet instanceof XSSFSheet ) {
      CreationHelper factory = data.wb.getCreationHelper();
      Drawing<?> drawing = data.sheet.createDrawingPatriarch();

      ClientAnchor anchor = factory.createClientAnchor();
      Comment cmt = drawing.createCellComment( anchor );
      RichTextString str = factory.createRichTextString( comment );
      cmt.setString( str );
      cmt.setAuthor( author );
      return cmt;
    }
    return null;
  }

  /**
   * @param reference
   * @return the cell the reference points to
   */
  private Cell getCellFromReference( String reference ) {

    CellReference cellRef = new CellReference( reference );
    String sheetName = cellRef.getSheetName();

    Sheet sheet = data.sheet;
    if ( !Utils.isEmpty( sheetName ) ) {
      sheet = data.wb.getSheet( sheetName );
    }
    if ( sheet == null ) {
      return null;
    }
    // reference is assumed to be absolute
    Row xlsRow = sheet.getRow( cellRef.getRow() );
    if ( xlsRow == null ) {
      return null;
    }
    Cell styleCell = xlsRow.getCell( cellRef.getCol() );
    return styleCell;
  }

  // VisibleForTesting
  void writeField( Object v, ValueMetaInterface vMeta, ExcelWriterStepField excelField, Row xlsRow, int posX,
      Object[] row, int fieldNr, boolean isTitle )
    throws KettleException {
    try {
      boolean cellExisted = true;
      // get the cell
      Cell cell = xlsRow.getCell( posX );
      if ( cell == null ) {
        cellExisted = false;
        cell = xlsRow.createCell( posX );
      }

      // if cell existed and existing cell's styles should not be changed, don't
      if ( !( cellExisted && meta.isLeaveExistingStylesUnchanged() ) ) {

        // if the style of this field is cached, reuse it
        if ( !isTitle && data.getCachedStyle( fieldNr ) != null ) {
          cell.setCellStyle( data.getCachedStyle( fieldNr ) );
        } else {
          // apply style if requested
          if ( excelField != null ) {

            // determine correct cell for title or data rows
            String styleRef = null;
            if ( !isTitle && !Utils.isEmpty( excelField.getStyleCell() ) ) {
              styleRef = excelField.getStyleCell();
            } else if ( isTitle && !Utils.isEmpty( excelField.getTitleStyleCell() ) ) {
              styleRef = excelField.getTitleStyleCell();
            }

            if ( styleRef != null ) {
              Cell styleCell = getCellFromReference( styleRef );
              if ( styleCell != null && cell != styleCell ) {
                cell.setCellStyle( styleCell.getCellStyle() );
              }
            }
          }

          // set cell format as specified, specific format overrides cell specification
          if ( !isTitle && excelField != null && !Utils.isEmpty( excelField.getFormat() )
              && !excelField.getFormat().startsWith( "Image" ) ) {
            setDataFormat( excelField.getFormat(), cell );
          }
          // cache it for later runs
          if ( !isTitle ) {
            data.cacheStyle( fieldNr, cell.getCellStyle() );
          }
        }
      }

      // create link on cell if requested
      if ( !isTitle && excelField != null && data.linkfieldnrs[fieldNr] >= 0 ) {
        String link =
            data.inputRowMeta.getValueMeta( data.linkfieldnrs[fieldNr] ).getString( row[data.linkfieldnrs[fieldNr]] );
        if ( !Utils.isEmpty( link ) ) {
          CreationHelper ch = data.wb.getCreationHelper();
          // set the link on the cell depending on link type
          Hyperlink hyperLink = null;
          if ( link.startsWith( "http:" ) || link.startsWith( "https:" ) || link.startsWith( "ftp:" ) ) {
            hyperLink = ch.createHyperlink( HyperlinkType.URL );
            hyperLink.setLabel( "URL Link" );
          } else if ( link.startsWith( "mailto:" ) ) {
            hyperLink = ch.createHyperlink( HyperlinkType.EMAIL );
            hyperLink.setLabel( "Email Link" );
          } else if ( link.startsWith( "'" ) ) {
            hyperLink = ch.createHyperlink( HyperlinkType.DOCUMENT );
            hyperLink.setLabel( "Link within this document" );
          } else {
            hyperLink = ch.createHyperlink( HyperlinkType.FILE );
            hyperLink.setLabel( "Link to a file" );
          }

          hyperLink.setAddress( link );
          cell.setHyperlink( hyperLink );

          // if cell existed and existing cell's styles should not be changed, don't
          if ( !( cellExisted && meta.isLeaveExistingStylesUnchanged() ) ) {

            if ( data.getCachedLinkStyle( fieldNr ) != null ) {
              cell.setCellStyle( data.getCachedLinkStyle( fieldNr ) );
            } else {
              Font origFont = data.wb.getFontAt( cell.getCellStyle().getFontIndexAsInt() );
              Font hlinkFont = data.wb.createFont();
              // reproduce original font characteristics

              hlinkFont.setBold( origFont.getBold() );
              hlinkFont.setCharSet( origFont.getCharSet() );
              hlinkFont.setFontHeight( origFont.getFontHeight() );
              hlinkFont.setFontName( origFont.getFontName() );
              hlinkFont.setItalic( origFont.getItalic() );
              hlinkFont.setStrikeout( origFont.getStrikeout() );
              hlinkFont.setTypeOffset( origFont.getTypeOffset() );
              // make it blue and underlined
              hlinkFont.setUnderline( Font.U_SINGLE );
              hlinkFont.setColor( IndexedColors.BLUE.getIndex() );
              CellStyle style = cell.getCellStyle();
              style.setFont( hlinkFont );
              cell.setCellStyle( style );
              data.cacheLinkStyle( fieldNr, cell.getCellStyle() );
            }
          }
        }
      }

      // create comment on cell if requested
      if ( !isTitle && excelField != null && data.commentfieldnrs[fieldNr] >= 0 && data.wb instanceof XSSFWorkbook ) {
        String comment =
            data.inputRowMeta.getValueMeta( data.commentfieldnrs[fieldNr] )
                .getString( row[data.commentfieldnrs[fieldNr]] );
        if ( !Utils.isEmpty( comment ) ) {
          String author =
              data.commentauthorfieldnrs[fieldNr] >= 0
                  ? data.inputRowMeta.getValueMeta( data.commentauthorfieldnrs[fieldNr] )
                      .getString( row[data.commentauthorfieldnrs[fieldNr]] )
                  : "Kettle PDI";
          cell.setCellComment( createCellComment( author, comment ) );
        }
      }
      // cell is getting a formula value or static content
      if ( !isTitle && excelField != null && excelField.isFormula() ) {
        // formula case
        cell.setCellFormula( vMeta.getString( v ) );
      } else {
        // static content case
        switch ( vMeta.getType() ) {
          case ValueMetaInterface.TYPE_DATE:
            if ( v != null && vMeta.getDate( v ) != null ) {
              cell.setCellValue( vMeta.getDate( v ) );
            } else if ( v == null && !meta.isRetainNullValues() ) {
              cell.setCellValue( "" );
            }
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            if ( v != null ) {
              cell.setCellValue( vMeta.getBoolean( v ) );
            } else if ( !meta.isRetainNullValues() ) {
              cell.setCellValue( "" );
            }
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
          case ValueMetaInterface.TYPE_NUMBER:
          case ValueMetaInterface.TYPE_INTEGER:
            if ( v != null ) {
              cell.setCellValue( vMeta.getNumber( v ) );
            } else if ( !meta.isRetainNullValues() ) {
              cell.setCellValue( "" );
            }
            break;
          default:
            // fallthrough: output the data value as a string
            if ( v != null ) {
              cell.setCellValue( vMeta.getString( v ) );
            } else if ( !meta.isRetainNullValues() ) {
              cell.setCellValue( "" );
            }
            break;
        }
      }
    } catch ( Exception e ) {
      logError( "Error writing field (" + data.posX + "," + data.posY + ") : " + e.toString() );
      logError( Const.getStackTracker( e ) );
      throw new KettleException( e );
    }
  }

  /**
   * Set specified cell format
   *
   * @param excelFieldFormat
   *          the specified format
   * @param cell
   *          the cell to set up format
   */
  private void setDataFormat( String excelFieldFormat, Cell cell ) {
    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "ExcelWriterStep.Log.SetDataFormat", excelFieldFormat,
        CellReference.convertNumToColString( cell.getColumnIndex() ), cell.getRowIndex() ) );
    }

    DataFormat format = data.wb.createDataFormat();
    short formatIndex = format.getFormat( excelFieldFormat );
    CellStyle style = data.wb.createCellStyle();
    style.cloneStyleFrom( cell.getCellStyle() );
    style.setDataFormat( formatIndex );
    cell.setCellStyle( style );
  }

  /**
   * Returns the output filename that belongs to this step observing the file split feature
   *
   * @return current output filename to write to
   */
  public String buildFilename( int splitNr ) {
    return meta.buildFilename( this, getCopy(), splitNr );
  }

  /**
   * Copies a VFS File
   *
   * @param in
   *          the source file object
   * @param out
   *          the destination file object
   * @throws KettleException
   */
  public static void copyFile( FileObject in, FileObject out ) throws KettleException {
    try ( BufferedInputStream fis = new BufferedInputStream( KettleVFS.getInputStream( in ) );
        BufferedOutputStream fos = new BufferedOutputStream( KettleVFS.getOutputStream( out, false ) ) ) {
      byte[] buf = new byte[1024 * 1024]; // copy in chunks of 1 MB
      int i = 0;
      while ( ( i = fis.read( buf ) ) != -1 ) {
        fos.write( buf, 0, i );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public void prepareNextOutputFile() throws KettleException {
    try {
      // sheet name shouldn't exceed 31 character
      if ( data.realSheetname != null && data.realSheetname.length() > 31 ) {
        throw new KettleException(
            BaseMessages.getString( PKG, "ExcelWriterStep.Exception.MaxSheetName", data.realSheetname ) );
      }
      // clear style cache
      int numOfFields =
          meta.getOutputFields() != null && meta.getOutputFields().length > 0 ? meta.getOutputFields().length : 0;
      if ( numOfFields == 0 ) {
        numOfFields = data.inputRowMeta != null ? data.inputRowMeta.size() : 0;
      }
      data.clearStyleCache( numOfFields );

      // build new filename
      String buildFilename = buildFilename( data.splitnr );

      data.file = KettleVFS.getFileObject( buildFilename, getTransMeta() );

      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "ExcelWriterStep.Log.OpeningFile", buildFilename ) );
      }

      // determine whether existing file must be deleted
      if ( data.file.exists() && data.createNewFile ) {
        if ( !data.file.delete() ) {
          if ( log.isBasic() ) {
            logBasic( BaseMessages.getString( PKG, "ExcelWriterStep.Log.CouldNotDeleteStaleFile", buildFilename ) );
          }
          setErrors( 1 );
          throw new KettleException( "Could not delete stale file " + buildFilename );
        }
      }

      // adding filename to result
      if ( meta.isAddToResultFiles() ) {
        // Add this to the result file names...
        ResultFile resultFile =
            new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname() );
        resultFile.setComment( "This file was created with an Excel writer step by Pentaho Data Integration" );
        addResultFile( resultFile );
      }

      boolean appendingToSheet;
      if ( data.file.exists() ) {
        appendingToSheet = true;
      } else {
        createFile();
        appendingToSheet = false;
      }

      // file is guaranteed to be in place now
      try ( InputStream inputStream = KettleVFS.getInputStream( data.file ) ) {
        if ( XLSX.equalsIgnoreCase( meta.getExtension() ) ) {
          // Ignore, by now, if it's to use streaming!
          // In that case, one needs to initialize it later, after writing header/template, because
          // SXSSFWorkbook can't read/rewrite existing data, only append.
          data.wb = new XSSFWorkbook( inputStream );
        } else {
          data.wb = new HSSFWorkbook( inputStream );
        }
      }

      int existingActiveSheetIndex = data.wb.getActiveSheetIndex();
      int replacingSheetAt = -1;
      if ( data.createNewSheet ) {
        Sheet sheet = data.wb.getSheet( data.realSheetname );
        if ( sheet != null ) {
          // sheet exists, replace or reuse as indicated by user
          replacingSheetAt = data.wb.getSheetIndex( data.wb.getSheet( data.realSheetname ) );
          data.wb.removeSheetAt( replacingSheetAt );
        }
      }

      // if sheet is now missing, we need to create a new one
      data.sheet = data.wb.getSheet( data.realSheetname );
      if ( data.sheet == null ) {
        createSheet( replacingSheetAt );
        // preserves active sheet selection in workbook
        data.wb.setActiveSheet( existingActiveSheetIndex );
        data.wb.setSelectedTab( existingActiveSheetIndex );
        appendingToSheet = false;
      }
      data.innerSheet = Optional.empty();

      // if use chose to make the current sheet active, do so
      if ( meta.isMakeSheetActive() ) {
        int sheetIndex = data.wb.getSheetIndex( data.sheet );
        data.wb.setActiveSheet( sheetIndex );
        data.wb.setSelectedTab( sheetIndex );
      }
      // handle write protection
      if ( meta.isSheetProtected() ) {
        protectSheet( data.sheet, data.realPassword );
      }

      setSheetPosition( appendingToSheet );

      // may have to write a few empty lines
      if ( !data.createNewSheet && meta.getAppendEmpty() > 0 && appendingToSheet ) {
        for ( int i = 0; i < meta.getAppendEmpty(); i++ ) {
          openLine();
          if ( !data.shiftExistingCells || meta.isAppendLines() ) {
            data.posY++;
          }
        }
      }

      // may have to write a header here
      if ( meta.isHeaderEnabled() && !( !data.createNewSheet && meta.isAppendOmitHeader() && appendingToSheet ) ) {
        writeHeader();
      }

      // If it's to use streaming, initialize it now as we already made all necessary initial calculations.
      if ( data.wb instanceof XSSFWorkbook && meta.isStreamingData() ) {
        data.innerSheet = Optional.of( data.sheet );
        data.wb = new SXSSFWorkbook( (XSSFWorkbook) data.wb, STREAMING_WINDOW_SIZE );
        data.sheet = data.wb.getSheet( data.realSheetname );
      }

      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "ExcelWriterStep.Log.FileOpened", buildFilename ) );
      }
      // this is the number of the new output file
      data.splitnr++;
    } catch ( Exception e ) {
      logError( "Error opening new file", e );
      setErrors( 1 );
      throw new KettleException( e );
    }
  }

  /** Sets data.startingRow, data.startingCol, data.posX, data.posY */
  private void setSheetPosition( boolean appendingToSheet ) {
    // starting cell support
    if ( !Utils.isEmpty( data.realStartingCell ) ) {
      CellReference cellRef = new CellReference( data.realStartingCell );
      data.startingRow = cellRef.getRow();
      data.startingCol = cellRef.getCol();
    } else {
      data.startingRow = 0;
      data.startingCol = 0;
    }

    data.posX = data.startingCol;
    data.posY = data.startingRow;

    // Find last row and append accordingly
    if ( !data.createNewSheet && meta.isAppendLines() && appendingToSheet ) {
      if ( data.sheet.getPhysicalNumberOfRows() > 0 ) {
        data.posY = data.sheet.getLastRowNum() + 1;
      } else {
        data.posY = 0;
      }
    }

    // offset by configured value
    // Find last row and append accordingly
    if ( !data.createNewSheet && meta.getAppendOffset() != 0 && appendingToSheet ) {
      data.posY += meta.getAppendOffset();
    }
  }

  /**
   * Creates a new sheet
   *
   * @param replacingSheetAt
   *          Order to place sheet in, if >= 0
   * @throws KettleException
   */
  private void createSheet( int replacingSheetAt ) throws KettleException {
    if ( meta.isTemplateSheetEnabled() ) {
      Sheet ts = data.wb.getSheet( data.realTemplateSheetName );
      // if template sheet is missing, break
      if ( ts == null ) {
        throw new KettleException(
            BaseMessages.getString( PKG, "ExcelWriterStep.Exception.TemplateNotFound", data.realTemplateSheetName ) );
      }
      data.sheet = data.wb.cloneSheet( data.wb.getSheetIndex( ts ) );
      data.wb.setSheetName( data.wb.getSheetIndex( data.sheet ), data.realSheetname );
      // unhide sheet in case it was hidden
      data.wb.setSheetHidden( data.wb.getSheetIndex( data.sheet ), false );
      if ( meta.isTemplateSheetHidden() ) {
        data.wb.setSheetHidden( data.wb.getSheetIndex( ts ), true );
      }
    } else {
      // no template to use, simply create a new sheet
      data.sheet = data.wb.createSheet( data.realSheetname );
    }
    if ( replacingSheetAt > -1 ) {
      data.wb.setSheetOrder( data.sheet.getSheetName(), replacingSheetAt );
    }
  }

  /**
   * Creates a new Excel file
   */
  private void createFile() throws KettleException, IOException {

    if ( meta.isCreateParentFolders() && !data.file.getParent().exists() ) {
      data.file.getParent().createFolder();
    }

    // if template file is enabled
    if ( meta.isTemplateEnabled() ) {
      // handle template case (must have same format)
      // ensure extensions match
      String templateExt = KettleVFS.getFileObject( data.realTemplateFileName ).getName().getExtension();
      if ( !meta.getExtension().equalsIgnoreCase( templateExt ) ) {
        throw new KettleException(
            "Template Format Mismatch: Template has extension: " + templateExt + ", but output file has extension: "
                + meta.getExtension() + ". Template and output file must share the same format!" );
      }

      if ( KettleVFS.getFileObject( data.realTemplateFileName ).exists() ) {
        // if the template exists just copy the template in place
        copyFile( KettleVFS.getFileObject( data.realTemplateFileName, getTransMeta() ), data.file );
      } else {
        // template is missing, log it and get out
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "ExcelWriterStep.Log.TemplateMissing", data.realTemplateFileName ) );
        }
        setErrors( 1 );
        throw new KettleException( "Template file missing: " + data.realTemplateFileName );
      }
    } else {
      // handle fresh file case, just create a fresh workbook
      try ( Workbook wb = XLSX.equalsIgnoreCase( meta.getExtension() ) ? new XSSFWorkbook() : new HSSFWorkbook();
          BufferedOutputStreamWithCloseDetection out =
              new BufferedOutputStreamWithCloseDetection( KettleVFS.getOutputStream( data.file, false ) ) ) {
        wb.createSheet( data.realSheetname );
        wb.write( out );
      }
    }
  }

  private void openLine() {
    if ( data.shiftExistingCells ) {
      data.sheet.shiftRows( data.posY, Math.max( data.posY, data.sheet.getLastRowNum() ), 1 );
    }
  }

  private void writeHeader() throws KettleException {
    try {
      openLine();
      Row xlsRow = getOrCreateRow( data.posY );
      int posX = data.posX;
      // If we have fields specified: list them in this order!
      if ( meta.getOutputFields() != null && meta.getOutputFields().length > 0 ) {
        for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
          String fieldName =
              !Utils.isEmpty( meta.getOutputFields()[i].getTitle() ) ? meta.getOutputFields()[i].getTitle()
                  : meta.getOutputFields()[i].getName();
          ValueMetaInterface vMeta = new ValueMetaString( fieldName );
          writeField( fieldName, vMeta, meta.getOutputFields()[i], xlsRow, posX++, null, -1, true );
        }
        // Just put all field names in
      } else if ( data.inputRowMeta != null ) {
        for ( int i = 0; i < data.inputRowMeta.size(); i++ ) {
          String fieldName = data.inputRowMeta.getFieldNames()[i];
          ValueMetaInterface vMeta = new ValueMetaString( fieldName );
          writeField( fieldName, vMeta, null, xlsRow, posX++, null, -1, true );
        }
      }
      data.posY++;
      incrementLinesOutput();
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  private Row getOrCreateRow( int rowIdx ) {
    Row xlsRow = data.sheet.getRow( rowIdx );
    if ( xlsRow == null ) {
      xlsRow = data.innerSheet.map( s -> s.getRow( rowIdx ) ).orElseGet( () -> data.sheet.createRow( rowIdx ) );
    }
    return xlsRow;
  }

  /**
   * transformation run initialize, may create the output file if specified by user options
   *
   * @see org.pentaho.di.trans.step.BaseStep#init(org.pentaho.di.trans.step.StepMetaInterface,
   *      org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelWriterStepMeta) smi;
    data = (ExcelWriterStepData) sdi;
    if ( super.init( smi, sdi ) ) {
      data.splitnr = 0;
      data.datalines = 0;
      data.realSheetname = environmentSubstitute( meta.getSheetname() );
      data.realTemplateSheetName = environmentSubstitute( meta.getTemplateSheetName() );
      data.realTemplateFileName = environmentSubstitute( meta.getTemplateFileName() );
      data.realStartingCell = environmentSubstitute( meta.getStartingCell() );
      data.realPassword = Utils.resolvePassword( variables, meta.getPassword() );
      data.realProtectedBy = environmentSubstitute( meta.getProtectedBy() );

      data.shiftExistingCells = ExcelWriterStepMeta.ROW_WRITE_PUSH_DOWN.equals( meta.getRowWritingMethod() );
      data.createNewSheet = ExcelWriterStepMeta.IF_SHEET_EXISTS_CREATE_NEW.equals( meta.getIfSheetExists() );
      data.createNewFile = ExcelWriterStepMeta.IF_FILE_EXISTS_CREATE_NEW.equals( meta.getIfFileExists() );
      return true;
    }
    return false;
  }

  /**
   * transformation run end
   *
   * @see org.pentaho.di.trans.step.BaseStep#dispose(org.pentaho.di.trans.step.StepMetaInterface,
   *      org.pentaho.di.trans.step.StepDataInterface)
   */
  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelWriterStepMeta) smi;
    data = (ExcelWriterStepData) sdi;
    clearWorkbookMem();
    super.dispose( smi, sdi );
  }

  /**
   * Write protect Sheet by setting password works only for xls output at the moment
   */
  protected void protectSheet( Sheet sheet, String password ) throws KettleException {
    if ( sheet instanceof HSSFSheet ) {
      // Write protect Sheet by setting password
      // works only for xls output at the moment
      sheet.protectSheet( password );
    } else {
      // prevented at UI, but still
      throw new KettleException( "Password protection currently only supported for XLS file format." );
    }
  }
}
