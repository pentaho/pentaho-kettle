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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExcelInputHelper extends BaseStepHelper {

  private static final Class<?> PKG = ExcelInputMeta.class;
  private static final String MESSAGE = "message";
  private static final String FIELDS = "fields";
  private static final String SHEETS = "sheets";
  private static final String GET_FILES = "getFiles";
  private static final String GET_SHEETS = "getSheets";
  private static final String GET_FIELDS = "getFields";
  private static final String STEP_NAME = "stepName";

  public ExcelInputHelper() {
    super();
  }

  /**
   * Handles step-specific actions for ExcelInput.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case GET_FILES:
          response = getFilesAction( transMeta, queryParams );
          break;
        case GET_SHEETS:
          response = getSheetsAction( transMeta, queryParams );
          break;
        case GET_FIELDS:
          response = getFieldsAction( transMeta, queryParams );
          break;
        default:
          response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
          break;
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Gets the list of files from the Excel input configuration.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject getFilesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    String stepName = queryParams.get( STEP_NAME );

    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof ExcelInputMeta excelInputMeta ) {
        String[] files = excelInputMeta.getFilePaths( transMeta.getBowl(), transMeta );

        if ( files == null || files.length == 0 ) {
          response.put( MESSAGE, BaseMessages.getString( PKG, "ExcelInputDialog.NoFilesFound.DialogMessage" ) );
        } else {
          response.put( "files", Arrays.asList( files ) );
        }
      }
    }

    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  /**
   * Gets the list of sheets from the Excel files.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject getSheetsAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    List<String> sheetNames = new ArrayList<>();
    String stepName = queryParams.get( STEP_NAME );

    if ( stepName != null && !stepName.isEmpty() ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( stepMeta != null && stepMeta.getStepMetaInterface() instanceof ExcelInputMeta excelInputMeta ) {
        FileInputList fileList = excelInputMeta.getFileList( transMeta.getBowl(), transMeta );

        for ( FileObject fileObject : fileList.getFiles() ) {
          try {
            KWorkbook workbook = getWorkBook( excelInputMeta, fileObject, transMeta );
            getSheetNames( sheetNames, workbook );
            response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
          } catch ( Exception ex ) {
            errorResponse( response, ex, fileObject );
            return response;
          }
        }
      }

      if ( CollectionUtils.isEmpty( sheetNames ) ) {
        response.put( MESSAGE, BaseMessages.getString( PKG, "ExcelInputDialog.UnableToFindSheets.DialogMessage" ) );
        response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
        return response;
      }
    }

    response.put( SHEETS, sheetNames );
    return response;
  }

  /**
   * Gets the field information from the Excel files.
   */
  @SuppressWarnings( "unchecked" )
  public JSONObject getFieldsAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    RowMetaInterface fields = new RowMeta();
    String stepName = queryParams.get( STEP_NAME );

    if ( isValidStepName( stepName ) ) {
      StepMeta stepMeta = transMeta.findStep( stepName );
      if ( isValidStepMeta( stepMeta ) ) {
        ExcelInputMeta excelInputMeta = (ExcelInputMeta) stepMeta.getStepMetaInterface();
        processFilesForFields( transMeta, response, fields, excelInputMeta );
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

  private boolean isValidStepName( String stepName ) {
    return stepName != null && !stepName.isEmpty();
  }

  private boolean isValidStepMeta( StepMeta stepMeta ) {
    return stepMeta != null && stepMeta.getStepMetaInterface() instanceof ExcelInputMeta;
  }

  private void processFilesForFields( TransMeta transMeta, JSONObject response, RowMetaInterface fields,
                                      ExcelInputMeta excelInputMeta ) {
    FileInputList fileList = excelInputMeta.getFileList( transMeta.getBowl(), transMeta );

    for ( FileObject file : fileList.getFiles() ) {
      try {
        KWorkbook workbook = getWorkBook( excelInputMeta, file, transMeta );
        processingWorkbook( fields, excelInputMeta, workbook );
        workbook.close();
        response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
      } catch ( Exception ex ) {
        errorResponse( response, ex, file );
        return ;
      }
    }
  }

  private KWorkbook getWorkBook( ExcelInputMeta excelInputMeta, FileObject fileObject, TransMeta transMeta )
    throws KettleException {
    return WorkbookFactory.getWorkbook( transMeta.getBowl(), excelInputMeta.getSpreadSheetType(),
      KettleVFS.getFilename( fileObject ),
      excelInputMeta.getEncoding(),
      transMeta.environmentSubstitute( excelInputMeta.getPassword() ) );
  }

  private int getSheetNumber( ExcelInputMeta info, KSheet sheet ) {
    return info.readAllSheets() ? 0 : Const.indexOfString( sheet.getName(), info.getSheetName() );
  }

  @SuppressWarnings( "unchecked" )
  private void errorResponse( JSONObject response, Exception ex, FileObject file ) {
    response.put( "errorLabel", BaseMessages
      .getString( PKG, "ExcelInputDialog.ErrorReadingFile.DialogMessage", KettleVFS.getFilename( file ) ) );
    response.put( "errorMessage", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage() );
    response.put( ACTION_STATUS, FAILURE_RESPONSE );
  }

  private int getFieldType( KCell cell ) {
    int fieldType;

    if ( cell != null ) {
      if ( cell.getType() == KCellType.BOOLEAN ) {
        fieldType = ValueMetaInterface.TYPE_BOOLEAN;
      } else if ( cell.getType() == KCellType.DATE ) {
        fieldType = ValueMetaInterface.TYPE_DATE;
      } else if ( cell.getType() == KCellType.LABEL ) {
        fieldType = ValueMetaInterface.TYPE_STRING;
      } else if ( cell.getType() == KCellType.NUMBER ) {
        fieldType = ValueMetaInterface.TYPE_NUMBER;
      } else {
        fieldType = ValueMetaInterface.TYPE_STRING;
      }
    } else {
      fieldType = ValueMetaInterface.TYPE_STRING;
    }

    return fieldType;
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
    for ( int sheetNumber = 0; sheetNumber < nrSheets; sheetNumber++ ) {
      KSheet sheet = workbook.getSheet( sheetNumber );

      // See if it's a selected sheet:
      int sheetIndex = getSheetNumber( info, sheet );
      if ( sheetIndex < 0 ) {
        continue;
      }

      // We suppose it's the complete range we're looking for...
      int startColumn;
      int rowNumber;
      if ( info.readAllSheets() ) {
        startColumn = info.getStartColumn().length == 1 ? info.getStartColumn()[ 0 ] : 0;
        rowNumber = info.getStartRow().length == 1 ? info.getStartRow()[ 0 ] : 0;
      } else {
        startColumn = info.getStartColumn()[ sheetIndex ];
        rowNumber = info.getStartRow()[ sheetIndex ];
      }

      addFieldsFromSheet( startColumn, rowNumber, sheet, fields );
    }
  }

  public static void getSheetNames( List<String> sheetNames, KWorkbook workbook ) {
    int nrSheets = workbook.getNumberOfSheets();
    for ( int j = 0; j < nrSheets; j++ ) {
      KSheet sheet = workbook.getSheet( j );
      String sheetName = sheet.getName();

      if ( Const.indexOfString( sheetName, sheetNames ) < 0 ) {
        sheetNames.add( sheetName );
      }
    }

    workbook.close();
  }

  private void addFieldsFromSheet( int startCol, int rowNumber, KSheet sheet, RowMetaInterface fields )
    throws KettlePluginException {
    boolean stop = false;
    for ( int colnr = startCol; !stop; colnr++ ) {
      try {
        String fieldname = null;

        KCell cell = sheet.getCell( colnr, rowNumber );
        if ( cell == null ) {
          stop = true;
        } else {
          if ( cell.getType() != KCellType.EMPTY ) {
            // We found a field.
            fieldname = cell.getContents();
          }

          KCell below = sheet.getCell( colnr, rowNumber + 1 );
          int fieldType = getFieldType( below );

          if ( Utils.isEmpty( fieldname ) ) {
            stop = true;
          } else {
            ValueMetaInterface field = ValueMetaFactory.createValueMeta( fieldname, fieldType );
            fields.addValueMeta( field );
          }
        }
      } catch ( ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException ) {
        stop = true;
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private JSONObject generateFieldsJSON( RowMetaInterface fields ) {
    JSONArray columnInfoArray = new JSONArray();
    JSONObject stepJSON = new JSONObject();
    JSONArray rowsArray = new JSONArray();
    stepJSON.put( "columnInfo", columnInfoArray );
    stepJSON.put( "rows", rowsArray );

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
