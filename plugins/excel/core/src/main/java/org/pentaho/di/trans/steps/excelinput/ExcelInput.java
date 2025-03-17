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


package org.pentaho.di.trans.steps.excelinput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.playlist.FilePlayListAll;
import org.pentaho.di.core.playlist.FilePlayListReplay;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.CompositeFileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandler;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerContentLineNumber;
import org.pentaho.di.trans.step.errorhandling.FileErrorHandlerMissingFiles;
import org.pentaho.di.trans.steps.utils.CommonExcelUtils;
import org.springframework.util.CollectionUtils;

/**
 * This class reads data from one or more Microsoft Excel files.
 *
 * @author Matt
 * @author timh
 * @since 19-NOV-2003
 */
public class ExcelInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = ExcelInputMeta.class; // for i18n purposes, needed by Translator2!!

  private ExcelInputMeta meta;

  private ExcelInputData data;

  private static final String MESSAGE = "message";
  private static final String FIELDS = "fields";
  private static final String SHEETS = "sheets";

  public ExcelInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                     Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    CommonExcelUtils.setZipBombConfiguration();
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return
   */

  private Object[] fillRow( int startcolumn, ExcelInputRow excelInputRow ) throws KettleException {
    Object[] r = new Object[data.outputRowMeta.size()];

    // Keep track whether or not we handled an error for this line yet.
    boolean errorHandled = false;

    // Set values in the row...
    KCell cell = null;

    for ( int i = startcolumn; i < excelInputRow.cells.length && i - startcolumn < meta.getField().length; i++ ) {
      cell = excelInputRow.cells[i];

      int rowcolumn = i - startcolumn;

      if ( cell == null ) {
        r[rowcolumn] = null;
        continue;
      }

      ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta( rowcolumn );
      ValueMetaInterface sourceMeta = null;

      try {
        checkType( cell, targetMeta );
      } catch ( KettleException ex ) {
        if ( !meta.isErrorIgnored() ) {
          ex = new KettleCellValueException( ex, this.data.sheetnr, this.data.rownr, i, "" );
          throw ex;
        }
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "ExcelInput.Log.WarningProcessingExcelFile", "" + targetMeta, ""
            + data.filename, ex.getMessage() ) );
        }

        if ( !errorHandled ) {
          data.errorHandler.handleLineError( excelInputRow.rownr, excelInputRow.sheetName );
          errorHandled = true;
        }

        if ( meta.isErrorLineSkipped() ) {
          return null;
        }
      }

      KCellType cellType = cell.getType();
      if ( KCellType.BOOLEAN == cellType || KCellType.BOOLEAN_FORMULA == cellType ) {
        r[rowcolumn] = cell.getValue();
        sourceMeta = data.valueMetaBoolean;
      } else {
        if ( KCellType.DATE.equals( cellType ) || KCellType.DATE_FORMULA.equals( cellType ) ) {
          Date date = (Date) cell.getValue();
          long time = date.getTime();
          int offset = TimeZone.getDefault().getOffset( time );
          r[rowcolumn] = new Date( time - offset );
          sourceMeta = data.valueMetaDate;
        } else {
          if ( KCellType.LABEL == cellType || KCellType.STRING_FORMULA == cellType ) {
            String string = (String) cell.getValue();
            switch ( meta.getField()[rowcolumn].getTrimType() ) {
              case ExcelInputMeta.TYPE_TRIM_LEFT:
                string = Const.ltrim( string );
                break;
              case ExcelInputMeta.TYPE_TRIM_RIGHT:
                string = Const.rtrim( string );
                break;
              case ExcelInputMeta.TYPE_TRIM_BOTH:
                string = Const.trim( string );
                break;
              default:
                break;
            }
            r[rowcolumn] = string;
            sourceMeta = data.valueMetaString;
          } else {
            if ( KCellType.NUMBER == cellType || KCellType.NUMBER_FORMULA == cellType ) {
              r[rowcolumn] = cell.getValue();
              sourceMeta = data.valueMetaNumber;
            } else {
              if ( log.isDetailed() ) {
                KCellType ct = cell.getType();
                logDetailed( BaseMessages.getString( PKG, "ExcelInput.Log.UnknownType", ( ( ct != null ) ? ct
                  .toString() : "null" ), cell.getContents() ) );
              }
              r[rowcolumn] = null;
            }
          }
        }
      }

      ExcelInputField field = meta.getField()[rowcolumn];

      // Change to the appropriate type if needed...
      //
      try {
        // Null stays null folks.
        //
        if ( sourceMeta != null && sourceMeta.getType() != targetMeta.getType() && r[rowcolumn] != null ) {
          ValueMetaInterface sourceMetaCopy = sourceMeta.clone();
          sourceMetaCopy.setConversionMask( field.getFormat() );
          sourceMetaCopy.setGroupingSymbol( field.getGroupSymbol() );
          sourceMetaCopy.setDecimalSymbol( field.getDecimalSymbol() );
          sourceMetaCopy.setCurrencySymbol( field.getCurrencySymbol() );

          switch ( targetMeta.getType() ) {
            // Use case: we find a numeric value: convert it using the supplied format to the desired data type...
            //
            case ValueMetaInterface.TYPE_NUMBER:
            case ValueMetaInterface.TYPE_INTEGER:
              switch ( field.getType() ) {
                case ValueMetaInterface.TYPE_DATE:
                  // number to string conversion (20070522.00 --> "20070522")
                  //
                  ValueMetaInterface valueMetaNumber = new ValueMetaNumber( "num" );
                  valueMetaNumber.setConversionMask( "#" );
                  Object string = sourceMetaCopy.convertData( valueMetaNumber, r[rowcolumn] );

                  // String to date with mask...
                  //
                  r[rowcolumn] = targetMeta.convertData( sourceMetaCopy, string );
                  break;
                default:
                  r[rowcolumn] = targetMeta.convertData( sourceMetaCopy, r[rowcolumn] );
                  break;
              }
              break;
            // Use case: we find a date: convert it using the supplied format to String...
            //
            default:
              r[rowcolumn] = targetMeta.convertData( sourceMetaCopy, r[rowcolumn] );
          }
        }
      } catch ( KettleException ex ) {
        if ( !meta.isErrorIgnored() ) {
          ex = new KettleCellValueException( ex, this.data.sheetnr, cell.getRow(), i, field.getName() );
          throw ex;
        }
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "ExcelInput.Log.WarningProcessingExcelFile", "" + targetMeta, ""
            + data.filename, ex.toString() ) );
        }
        if ( !errorHandled ) {
          // check if we didn't log an error already for this one.
          data.errorHandler.handleLineError( excelInputRow.rownr, excelInputRow.sheetName );
          errorHandled = true;
        }

        if ( meta.isErrorLineSkipped() ) {
          return null;
        } else {
          r[rowcolumn] = null;
        }
      }
    }

    int rowIndex = meta.getField().length;

    // Do we need to include the filename?
    if ( !Utils.isEmpty( meta.getFileField() ) ) {
      r[rowIndex] = data.filename;
      rowIndex++;
    }

    // Do we need to include the sheetname?
    if ( !Utils.isEmpty( meta.getSheetField() ) ) {
      r[rowIndex] = excelInputRow.sheetName;
      rowIndex++;
    }

    // Do we need to include the sheet rownumber?
    if ( !Utils.isEmpty( meta.getSheetRowNumberField() ) ) {
      r[rowIndex] = new Long( data.rownr );
      rowIndex++;
    }

    // Do we need to include the rownumber?
    if ( !Utils.isEmpty( meta.getRowNumberField() ) ) {
      r[rowIndex] = new Long( getLinesWritten() + 1 );
      rowIndex++;
    }
    // Possibly add short filename...
    if ( !Utils.isEmpty( meta.getShortFileNameField() ) ) {
      r[rowIndex] = data.shortFilename;
      rowIndex++;
    }
    // Add Extension
    if ( !Utils.isEmpty( meta.getExtensionField() ) ) {
      r[rowIndex] = data.extension;
      rowIndex++;
    }
    // add path
    if ( !Utils.isEmpty( meta.getPathField() ) ) {
      r[rowIndex] = data.path;
      rowIndex++;
    }
    // Add Size
    if ( !Utils.isEmpty( meta.getSizeField() ) ) {
      r[rowIndex] = new Long( data.size );
      rowIndex++;
    }
    // add Hidden
    if ( !Utils.isEmpty( meta.isHiddenField() ) ) {
      r[rowIndex] = new Boolean( data.hidden );
      rowIndex++;
    }
    // Add modification date
    if ( !Utils.isEmpty( meta.getLastModificationDateField() ) ) {
      r[rowIndex] = data.lastModificationDateTime;
      rowIndex++;
    }
    // Add Uri
    if ( !Utils.isEmpty( meta.getUriField() ) ) {
      r[rowIndex] = data.uriName;
      rowIndex++;
    }
    // Add RootUri
    if ( !Utils.isEmpty( meta.getRootUriField() ) ) {
      r[rowIndex] = data.rootUriName;
      rowIndex++;
    }

    return r;
  }

  private void checkType( KCell cell, ValueMetaInterface v ) throws KettleException {
    if ( !meta.isStrictTypes() ) {
      return;
    }
    switch ( cell.getType() ) {
      case BOOLEAN:
        if ( !( v.getType() == ValueMetaInterface.TYPE_STRING || v.getType() == ValueMetaInterface.TYPE_NONE || v
          .getType() == ValueMetaInterface.TYPE_BOOLEAN ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "ExcelInput.Exception.InvalidTypeBoolean", v
            .getTypeDesc() ) );
        }
        break;

      case DATE:
        if ( !( v.getType() == ValueMetaInterface.TYPE_STRING || v.getType() == ValueMetaInterface.TYPE_NONE || v
          .getType() == ValueMetaInterface.TYPE_DATE ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "ExcelInput.Exception.InvalidTypeDate", cell
            .getContents(), v.getTypeDesc() ) );
        }
        break;

      case LABEL:
        if ( v.getType() == ValueMetaInterface.TYPE_BOOLEAN
          || v.getType() == ValueMetaInterface.TYPE_DATE || v.getType() == ValueMetaInterface.TYPE_INTEGER
          || v.getType() == ValueMetaInterface.TYPE_NUMBER ) {
          throw new KettleException( BaseMessages.getString( PKG, "ExcelInput.Exception.InvalidTypeLabel", cell
            .getContents(), v.getTypeDesc() ) );
        }
        break;

      case EMPTY:
        // OK
        break;

      case NUMBER:
        if ( !( v.getType() == ValueMetaInterface.TYPE_STRING
          || v.getType() == ValueMetaInterface.TYPE_NONE || v.getType() == ValueMetaInterface.TYPE_INTEGER
          || v.getType() == ValueMetaInterface.TYPE_BIGNUMBER || v.getType() == ValueMetaInterface.TYPE_NUMBER ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "ExcelInput.Exception.InvalidTypeNumber", cell
            .getContents(), v.getTypeDesc() ) );
        }
        break;

      default:
        throw new KettleException( BaseMessages.getString( PKG, "ExcelInput.Exception.UnsupportedType", cell
          .getType().getDescription(), cell.getContents() ) );
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ExcelInputMeta) smi;
    data = (ExcelInputData) sdi;

    if ( first ) {
      first = false;

      data.outputRowMeta = new RowMeta(); // start from scratch!
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( meta.isAcceptingFilenames() ) {
        // Read the files from the specified input stream...
        data.files.getFiles().clear();

        int idx = -1;
        RowSet rowSet = findInputRowSet( meta.getAcceptingStepName() );
        Object[] fileRow = getRowFrom( rowSet );
        while ( fileRow != null ) {
          if ( idx < 0 ) {
            idx = rowSet.getRowMeta().indexOfValue( meta.getAcceptingField() );
            if ( idx < 0 ) {
              logError( BaseMessages.getString( PKG, "ExcelInput.Error.FilenameFieldNotFound", ""
                + meta.getAcceptingField() ) );

              setErrors( 1 );
              stopAll();
              return false;
            }
          }
          String fileValue = rowSet.getRowMeta().getString( fileRow, idx );
          try {
            data.files.addFile( KettleVFS.getFileObject( fileValue, getTransMeta() ) );
          } catch ( KettleFileException e ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "ExcelInput.Exception.CanNotCreateFileObject", fileValue ), e );
          }

          // Grab another row
          fileRow = getRowFrom( rowSet );
        }
      }

      handleMissingFiles();
    }

    // See if we're not done processing...
    // We are done processing if the filenr >= number of files.
    if ( data.filenr >= data.files.nrOfFiles() ) {
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "ExcelInput.Log.NoMoreFiles", "" + data.filenr ) );
      }

      setOutputDone(); // signal end to receiver(s)
      return false; // end of data or error.
    }

    if ( meta.getRowLimit() > 0 && getLinesInput() >= meta.getRowLimit() ) {
      // The close of the openFile is in dispose()
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "ExcelInput.Log.RowLimitReached", "" + meta.getRowLimit() ) );
      }

      setOutputDone(); // signal end to receiver(s)
      return false; // end of data or error.
    }

    Object[] r = getRowFromWorkbooks();
    if ( r != null ) {
      incrementLinesInput();

      // OK, see if we need to repeat values.
      if ( data.previousRow != null ) {
        for ( int i = 0; i < meta.getField().length; i++ ) {
          ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta( i );
          Object valueData = r[i];

          if ( valueMeta.isNull( valueData ) && meta.getField()[i].isRepeated() ) {
            // Take the value from the previous row.
            r[i] = data.previousRow[i];
          }
        }
      }

      // Remember this row for the next time around!
      data.previousRow = data.outputRowMeta.cloneRow( r );

      // Send out the good news: we found a row of data!
      putRow( data.outputRowMeta, r );

      return true;
    } else {
      // This row is ignored / eaten
      // We continue though.
      return true;
    }
  }

  private void handleMissingFiles() throws KettleException {
    List<FileObject> nonExistantFiles = data.files.getNonExistantFiles();

    if ( !nonExistantFiles.isEmpty() ) {
      String message = FileInputList.getRequiredFilesDescription( nonExistantFiles );
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "ExcelInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
          PKG, "ExcelInput.Warning.MissingFiles", message ) );
      }

      if ( meta.isErrorIgnored() ) {
        for ( FileObject fileObject : nonExistantFiles ) {
          data.errorHandler.handleNonExistantFile( fileObject );
        }
      } else {
        throw new KettleException( BaseMessages.getString(
          PKG, "ExcelInput.Exception.MissingRequiredFiles", message ) );
      }
    }

    List<FileObject> nonAccessibleFiles = data.files.getNonAccessibleFiles();
    if ( !nonAccessibleFiles.isEmpty() ) {
      String message = FileInputList.getRequiredFilesDescription( nonAccessibleFiles );
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "ExcelInput.Log.RequiredFilesTitle" ), BaseMessages.getString(
          PKG, "ExcelInput.Log.RequiredFilesMsgNotAccessible", message ) );
      }

      if ( meta.isErrorIgnored() ) {
        for ( FileObject fileObject : nonAccessibleFiles ) {
          data.errorHandler.handleNonAccessibleFile( fileObject );
        }
      } else {
        throw new KettleException( BaseMessages.getString(
          PKG, "ExcelInput.Exception.RequiredFilesNotAccessible", message ) );
      }
    }
  }

  public Object[] getRowFromWorkbooks() {
    // This procedure outputs a single Excel data row on the destination
    // rowsets...

    Object[] retval = null;

    try {
      // First, see if a file has been opened?
      if ( data.workbook == null ) {
        // Open a new openFile..
        data.file = data.files.getFile( data.filenr );
        data.filename = KettleVFS.getFilename( data.file );
        // Add additional fields?
        if ( meta.getShortFileNameField() != null && meta.getShortFileNameField().length() > 0 ) {
          data.shortFilename = data.file.getName().getBaseName();
        }
        if ( meta.getPathField() != null && meta.getPathField().length() > 0 ) {
          data.path = KettleVFS.getFilename( data.file.getParent() );
        }
        if ( meta.isHiddenField() != null && meta.isHiddenField().length() > 0 ) {
          data.hidden = data.file.isHidden();
        }
        if ( meta.getExtensionField() != null && meta.getExtensionField().length() > 0 ) {
          data.extension = data.file.getName().getExtension();
        }
        if ( meta.getLastModificationDateField() != null && meta.getLastModificationDateField().length() > 0 ) {
          data.lastModificationDateTime = new Date( data.file.getContent().getLastModifiedTime() );
        }
        if ( meta.getUriField() != null && meta.getUriField().length() > 0 ) {
          data.uriName = Const.optionallyDecodeUriString( data.file.getName().getURI() );
        }
        if ( meta.getRootUriField() != null && meta.getRootUriField().length() > 0 ) {
          data.rootUriName = data.file.getName().getRootURI();
        }
        if ( meta.getSizeField() != null && meta.getSizeField().length() > 0 ) {
          data.size = data.file.getContent().getSize();
        }

        if ( meta.isAddResultFile() ) {
          ResultFile resultFile =
            new ResultFile( ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), toString() );
          resultFile.setComment( BaseMessages.getString( PKG, "ExcelInput.Log.FileReadByStep" ) );
          addResultFile( resultFile );
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "ExcelInput.Log.OpeningFile", ""
            + data.filenr + " : " + data.filename ) );
        }

        data.workbook = WorkbookFactory.getWorkbook( meta.getSpreadSheetType(), data.filename, meta.getEncoding(), meta.getPassword() );

        data.errorHandler.handleFile( data.file );
        // Start at the first sheet again...
        data.sheetnr = 0;

        // See if we have sheet names to retrieve, otherwise we'll have to get all sheets...
        //
        if ( meta.readAllSheets() ) {
          data.sheetNames = data.workbook.getSheetNames();
          data.startColumn = new int[data.sheetNames.length];
          data.startRow = new int[data.sheetNames.length];
          for ( int i = 0; i < data.sheetNames.length; i++ ) {
            data.startColumn[i] = data.defaultStartColumn;
            data.startRow[i] = data.defaultStartRow;
          }
        }
      }

      boolean nextsheet = false;

      // What sheet were we handling?
      if ( log.isDebug() ) {
        logDetailed( BaseMessages
          .getString( PKG, "ExcelInput.Log.GetSheet", "" + data.filenr + "." + data.sheetnr ) );
      }

      String sheetName = data.sheetNames[data.sheetnr];
      KSheet sheet = data.workbook.getSheet( sheetName );
      if ( sheet != null ) {
        // at what row do we continue reading?
        if ( data.rownr < 0 ) {
          data.rownr = data.startRow[data.sheetnr];

          // Add an extra row if we have a header row to skip...
          if ( meta.startsWithHeader() ) {
            data.rownr++;
          }
        }
        // Start at the specified column
        data.colnr = data.startColumn[data.sheetnr];

        // Build a new row and fill in the data from the sheet...
        try {
          KCell[] line = sheet.getRow( data.rownr );
          // Already increase cursor 1 row
          int lineNr = ++data.rownr;
          // Excel starts counting at 0
          if ( !data.filePlayList.isProcessingNeeded( data.file, lineNr, sheetName ) ) {
            retval = null; // placeholder, was already null
          } else {
            if ( log.isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "ExcelInput.Log.GetLine", "" + lineNr, data.filenr
                + "." + data.sheetnr ) );
            }

            if ( log.isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "ExcelInput.Log.ReadLineWith", "" + line.length ) );
            }

            ExcelInputRow excelInputRow = new ExcelInputRow( sheet.getName(), lineNr, line );
            Object[] r = fillRow( data.colnr, excelInputRow );
            if ( log.isRowLevel() ) {
              logRowlevel( BaseMessages.getString(
                PKG, "ExcelInput.Log.ConvertedLinToRow", "" + lineNr, data.outputRowMeta.getString( r ) ) );
            }

            boolean isEmpty = isLineEmpty( line );
            if ( !isEmpty || !meta.ignoreEmptyRows() ) {
              // Put the row
              retval = r;
            } else {
              if ( data.rownr > sheet.getRows() ) {
                nextsheet = true;
              }
            }

            if ( isEmpty && meta.stopOnEmpty() ) {
              nextsheet = true;
            }
          }
        } catch ( ArrayIndexOutOfBoundsException e ) {
          if ( log.isRowLevel() ) {
            logRowlevel( BaseMessages.getString( PKG, "ExcelInput.Log.OutOfIndex" ) );
          }

          // We tried to read below the last line in the sheet.
          // Go to the next sheet...
          nextsheet = true;
        }
      } else {
        nextsheet = true;
      }

      if ( nextsheet ) {
        // Go to the next sheet
        data.sheetnr++;

        // Reset the start-row:
        data.rownr = -1;

        // no previous row yet, don't take it from the previous sheet!
        // (that would be plain wrong!)
        data.previousRow = null;

        // Perhaps it was the last sheet?
        if ( data.sheetnr >= data.sheetNames.length ) {
          jumpToNextFile();
        }
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "ExcelInput.Error.ProcessRowFromExcel", data.filename + "", e
        .toString() ), e );

      setErrors( 1 );
      stopAll();
      return null;
    }

    return retval;
  }

  private boolean isLineEmpty( KCell[] line ) {
    if ( line.length == 0 ) {
      return true;
    }

    boolean isEmpty = true;
    for ( int i = 0; i < line.length && isEmpty; i++ ) {
      if ( line[i] != null && !Utils.isEmpty( line[i].getContents() ) ) {
        isEmpty = false;
      }
    }
    return isEmpty;
  }

  private void jumpToNextFile() throws KettleException {
    data.sheetnr = 0;

    // Reset the start-row:
    data.rownr = -1;

    // no previous row yet, don't take it from the previous sheet! (that
    // whould be plain wrong!)
    data.previousRow = null;

    // Close the openFile!
    data.workbook.close();
    data.workbook = null; // marker to open again.
    data.errorHandler.close();

    // advance to the next file!
    data.filenr++;
  }

  private void initErrorHandling() {
    List<FileErrorHandler> errorHandlers = new ArrayList<>( 2 );

    if ( meta.getLineNumberFilesDestinationDirectory() != null ) {
      errorHandlers.add( new FileErrorHandlerContentLineNumber(
        getTrans().getCurrentDate(), environmentSubstitute( meta.getLineNumberFilesDestinationDirectory() ),
        meta.getLineNumberFilesExtension(), "Latin1", this ) );
    }
    if ( meta.getErrorFilesDestinationDirectory() != null ) {
      errorHandlers.add( new FileErrorHandlerMissingFiles(
        getTrans().getCurrentDate(), environmentSubstitute( meta.getErrorFilesDestinationDirectory() ), meta
        .getErrorFilesExtension(), "Latin1", this ) );
    }
    data.errorHandler = new CompositeFileErrorHandler( errorHandlers );
  }

  private void initReplayFactory() {
    Date replayDate = getTrans().getReplayDate();
    if ( replayDate == null ) {
      data.filePlayList = FilePlayListAll.INSTANCE;
    } else {
      data.filePlayList =
        new FilePlayListReplay(
          replayDate, environmentSubstitute( meta.getLineNumberFilesDestinationDirectory() ), meta
          .getLineNumberFilesExtension(),
          environmentSubstitute( meta.getErrorFilesDestinationDirectory() ), meta.getErrorFilesExtension(),
          "Latin1" );
    }
  }



  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelInputMeta) smi;
    data = (ExcelInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      initErrorHandling();
      initReplayFactory();
      data.files = meta.getFileList( this );
      if ( data.files.nrOfFiles() == 0 && data.files.nrOfMissingFiles() > 0 && !meta.isAcceptingFilenames() ) {

        logError( BaseMessages.getString( PKG, "ExcelInput.Error.NoFileSpecified" ) );
        return false;
      }

      if ( meta.getEmptyFields().size() > 0 ) {
        // Determine the maximum filename length...
        data.maxfilelength = -1;

        for ( FileObject file : data.files.getFiles() ) {
          String name = KettleVFS.getFilename( file );
          if ( name.length() > data.maxfilelength ) {
            data.maxfilelength = name.length();
          }
        }

        // Determine the maximum sheet name length...
        data.maxsheetlength = -1;
        if ( !meta.readAllSheets() ) {
          data.sheetNames = new String[meta.getSheetName().length];
          data.startColumn = new int[meta.getSheetName().length];
          data.startRow = new int[meta.getSheetName().length];
          for ( int i = 0; i < meta.getSheetName().length; i++ ) {
            data.sheetNames[i] = meta.getSheetName()[i];
            data.startColumn[i] = meta.getStartColumn()[i];
            data.startRow[i] = meta.getStartRow()[i];

            if ( meta.getSheetName()[i].length() > data.maxsheetlength ) {
              data.maxsheetlength = meta.getSheetName()[i].length();
            }
          }
        } else {
          // Allocated at open file time: we want ALL sheets.
          if ( meta.getStartRow().length == 1 ) {
            data.defaultStartRow = meta.getStartRow()[0];
          } else {
            data.defaultStartRow = 0;
          }
          if ( meta.getStartColumn().length == 1 ) {
            data.defaultStartColumn = meta.getStartColumn()[0];
          } else {
            data.defaultStartColumn = 0;
          }
        }

        return true;
      } else {
        logError( BaseMessages.getString( PKG, "ExcelInput.Error.NotInputFieldsDefined" ) );
      }
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExcelInputMeta) smi;
    data = (ExcelInputData) sdi;

    if ( data.workbook != null ) {
      data.workbook.close();
    }
    if ( data.file != null ) {
      try {
        data.file.close();
      } catch ( Exception e ) {
        // Ignore close errors
      }
    }
    try {
      data.errorHandler.close();
    } catch ( KettleException e ) {
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "ExcelInput.Error.CouldNotCloseErrorHandler", e.toString() ) );

        logDebug( Const.getStackTracker( e ) );
      }
    }
    super.dispose( smi, sdi );
  }

  /**
   * Processing excel workbook, filling fields
   *
   * @param fields   RowMetaInterface for filling fields
   * @param info     ExcelInputMeta
   * @param workbook excel workbook for processing
   * @throws KettlePluginException
   */
  public void processingWorkbook( RowMetaInterface fields, ExcelInputMeta info, KWorkbook workbook )
    throws KettlePluginException {
    int nrSheets = workbook.getNumberOfSheets();
    for ( int j = 0; j < nrSheets; j++ ) {
      KSheet sheet = workbook.getSheet( j );

      // See if it's a selected sheet:
      int sheetIndex;
      if ( info.readAllSheets() ) {
        sheetIndex = 0;
      } else {
        sheetIndex = Const.indexOfString( sheet.getName(), info.getSheetName() );
      }
      if ( sheetIndex >= 0 ) {
        // We suppose it's the complete range we're looking for...
        //
        int rownr = 0;
        int startcol = 0;

        if ( info.readAllSheets() ) {
          if ( info.getStartColumn().length == 1 ) {
            startcol = info.getStartColumn()[ 0 ];
          }
          if ( info.getStartRow().length == 1 ) {
            rownr = info.getStartRow()[ 0 ];
          }
        } else {
          rownr = info.getStartRow()[ sheetIndex ];
          startcol = info.getStartColumn()[ sheetIndex ];
        }

        boolean stop = false;
        for ( int colnr = startcol; !stop; colnr++ ) {
          try {
            String fieldname = null;
            int fieldtype = ValueMetaInterface.TYPE_NONE;

            KCell cell = sheet.getCell( colnr, rownr );
            if ( cell == null ) {
              stop = true;
            } else {
              if ( cell.getType() != KCellType.EMPTY ) {
                // We found a field.
                fieldname = cell.getContents();
              }

              // System.out.println("Fieldname = "+fieldname);

              KCell below = sheet.getCell( colnr, rownr + 1 );

              if ( below != null ) {
                if ( below.getType() == KCellType.BOOLEAN ) {
                  fieldtype = ValueMetaInterface.TYPE_BOOLEAN;
                } else if ( below.getType() == KCellType.DATE ) {
                  fieldtype = ValueMetaInterface.TYPE_DATE;
                } else if ( below.getType() == KCellType.LABEL ) {
                  fieldtype = ValueMetaInterface.TYPE_STRING;
                } else if ( below.getType() == KCellType.NUMBER ) {
                  fieldtype = ValueMetaInterface.TYPE_NUMBER;
                } else {
                  fieldtype = ValueMetaInterface.TYPE_STRING;
                }
              } else {
                fieldtype = ValueMetaInterface.TYPE_STRING;
              }

              if ( Utils.isEmpty( fieldname ) ) {
                stop = true;
              } else {
                if ( fieldtype != ValueMetaInterface.TYPE_NONE ) {
                  ValueMetaInterface field = ValueMetaFactory.createValueMeta( fieldname, fieldtype );
                  fields.addValueMeta( field );
                }
              }
            }
          } catch ( ArrayIndexOutOfBoundsException aioobe ) {
            // System.out.println("index out of bounds at column "+colnr+" : "+aioobe.toString());
            stop = true;
          }
        }
      }
    }
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getFilesAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    ExcelInputMeta excelInputMeta = (ExcelInputMeta) getStepMetaInterface();
    String[] files = excelInputMeta.getFilePaths( getTransMeta() );

    if ( files == null || files.length == 0 ) {
      response.put( MESSAGE, BaseMessages.getString( PKG, "ExcelInputDialog.NoFilesFound.DialogMessage" ) );
    } else {
      response.put( "files", Arrays.asList( files ) );
    }

    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getSheetsAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    List<String> sheetNames = new ArrayList<>();
    ExcelInputMeta excelInputMeta = (ExcelInputMeta) getStepMetaInterface();
    FileInputList fileList = excelInputMeta.getFileList( getTransMeta() );

    for ( FileObject fileObject : fileList.getFiles() ) {
      try {
        KWorkbook workbook = getWorkBook( excelInputMeta, fileObject );
        int nrSheets = workbook.getNumberOfSheets();
        for ( int j = 0; j < nrSheets; j++ ) {
          KSheet sheet = workbook.getSheet( j );
          String sheetName = sheet.getName();

          if ( Const.indexOfString( sheetName, sheetNames ) < 0 ) {
            sheetNames.add( sheetName );
          }
        }

        workbook.close();
        response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      } catch ( Exception ex ) {
        errorResponse( response, ex, fileObject );
        return response;
      }
    }

    if ( CollectionUtils.isEmpty( sheetNames ) ) {
      response.put( MESSAGE, BaseMessages.getString( PKG, "ExcelInputDialog.UnableToFindSheets.DialogMessage" ) );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      return response;
    }

    response.put( SHEETS, sheetNames );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getFieldsAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    RowMetaInterface fields = new RowMeta();
    ExcelInputMeta excelInputMeta = (ExcelInputMeta) getStepMetaInterface();
    FileInputList fileList = excelInputMeta.getFileList( getTransMeta() );

    for ( FileObject file : fileList.getFiles() ) {
      try {
        KWorkbook workbook = getWorkBook( excelInputMeta, file );
        processingWorkbook( fields, excelInputMeta, workbook );
        workbook.close();
        response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      } catch ( Exception ex ) {
        errorResponse( response, ex, file );
        return response;
      }
    }

    if ( fields.getValueMetaList() == null || fields.getValueMetaList().isEmpty() ) {
      response.put( MESSAGE, BaseMessages.getString( PKG, "ExcelInputDialog.UnableToFindFields.DialogMessage" ) );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      return response;
    }

    response.put( FIELDS, generateFieldsJSON( fields ) );
    return response;
  }

  private KWorkbook getWorkBook( ExcelInputMeta excelInputMeta, FileObject fileObject ) throws KettleException {
    return WorkbookFactory.getWorkbook( excelInputMeta.getSpreadSheetType(),
      KettleVFS.getFilename( fileObject ),
      excelInputMeta.getEncoding(),
      excelInputMeta.getPassword() );
  }

  private void errorResponse( JSONObject response, Exception ex, FileObject file ) {
    response.put( "errorLabel", BaseMessages
      .getString( PKG, "ExcelInputDialog.ErrorReadingFile.DialogMessage", KettleVFS.getFilename( file ) ) );
    response.put( "errorMessage", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage() );
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
  }

  private JSONObject generateFieldsJSON( RowMetaInterface fields ) {
    JSONArray columnInfoArray = new JSONArray();
    JSONObject stepJSON = new JSONObject();
    JSONArray rowsArray = new JSONArray();
    stepJSON.put( "columnInfo", columnInfoArray );
    stepJSON.put( "rows", rowsArray );

    if ( fields.getValueMetaList() == null ) {
      return stepJSON;
    }

    for ( int i = 0; i < fields.getValueMetaList().size(); i++ ) {
      JSONArray dataArray = new JSONArray();
      JSONObject rowObject = new JSONObject();
      dataArray.add( fields.getValueMeta( i ).getName() );
      dataArray.add( fields.getValueMeta( i ).getTypeDesc() );
      dataArray.add( StringUtils.EMPTY );
      dataArray.add( StringUtils.EMPTY );
      dataArray.add( "none" );
      dataArray.add( "N" );
      dataArray.add( StringUtils.EMPTY );
      dataArray.add( StringUtils.EMPTY );
      dataArray.add( StringUtils.EMPTY );
      dataArray.add( StringUtils.EMPTY );
      rowObject.put( "data", dataArray );
      rowsArray.add( rowObject );
    }

    return stepJSON;
  }

}
