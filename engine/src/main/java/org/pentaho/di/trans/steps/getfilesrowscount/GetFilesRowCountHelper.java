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

package org.pentaho.di.trans.steps.getfilesrowscount;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Arrays;
import java.util.Map;

/**
 * Helper class for the "Get Files Rows Count" step providing UI-triggered actions.
 *
 * <p>This helper implements step-specific actions that can be invoked from the web UI (Webspoon)
 * or desktop Spoon interface. It extends {@link BaseStepHelper} to provide consistent error handling
 * and JSON response formatting.</p>
 *
 * <p>Supported actions:</p>
 * <ul>
 *   <li><strong>showFiles</strong> - Retrieves and returns the list of files that will be processed
 *       by the Get Files Rows Count step based on the current step configuration</li>
 * </ul>
 *
 * <p>Integration with Web UI:</p>
 * <p>This helper integrates with the Webspoon UI through REST API calls. When users interact with
 * the step dialog (e.g., clicking "Show Files" button), the frontend makes HTTP requests to Carte
 * server endpoints, which then delegate to this helper through the BaseStep.doAction() mechanism.</p>
 *
 * @see BaseStepHelper
 * @see GetFilesRowsCountMeta
 */
public class GetFilesRowCountHelper extends BaseStepHelper {

  /**
   * Action name constant for showing files
   */
  private static final String SHOW_FILE_NAME = "showFiles";

  private static final Class<?> PKG = GetFilesRowsCountMeta.class;

  private final GetFilesRowsCountMeta getFilesRowsCountMeta;

  /**
   * Constructs a new GetFilesRowCountHelper instance.
   *
   * @param getFilesRowsCountMeta The step metadata instance
   */
  public GetFilesRowCountHelper( GetFilesRowsCountMeta getFilesRowsCountMeta ) {
    this.getFilesRowsCountMeta = getFilesRowsCountMeta;
  }

  /**
   * Handles step-specific actions for the Get Files Rows Count step.
   *
   * <p>This method routes action requests to the appropriate handler method based on the
   * method name. Currently supports the "showFiles" action for retrieving file lists.</p>
   *
   * @param method      The name of the action method to execute (e.g., "showFiles")
   * @param transMeta   The transformation metadata containing the step configuration
   * @param queryParams A map of query parameters including "stepName" for step identification
   * @return A JSON object containing the action response with status and data
   * @throws RuntimeException if an error occurs during action execution
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      if ( SHOW_FILE_NAME.equals( method ) ) {
        response = getFilesAction( transMeta );
      } else {
        response.put( ACTION_STATUS, FAILURE_METHOD_NOT_FOUND_RESPONSE );
      }
    } catch ( Exception ex ) {
      response.put( ACTION_STATUS, FAILURE_RESPONSE );
    }
    return response;
  }

  /**
   * Retrieves the list of files that will be processed by the Get Files Rows Count step.
   *
   * <p>This action method extracts the file patterns and configurations from the step metadata
   * and returns a JSON response containing the resolved file list. This is typically called
   * from the UI to preview which files will be processed before running the transformation.</p>
   *
   * <p>The response JSON structure:</p>
   * <ul>
   *   <li><strong>files</strong> - JSONArray containing file paths</li>
   *   <li><strong>actionStatus</strong> - "Action successful" if files found</li>
   *   <li><strong>message</strong> - Error message if no files found</li>
   * </ul>
   *
   * @param transMeta   The transformation metadata containing the step configuration
   * @return A JSON object containing the file list and operation status
   * @see FileInputList#getFileStrings()
   */
  public JSONObject getFilesAction( TransMeta transMeta ) {
    JSONObject response = new JSONObject();

    FileInputList fileInputList = getFilesRowsCountMeta.getFiles( transMeta.getBowl(), transMeta );
    String[] files = fileInputList.getFileStrings();

    JSONArray fileList = new JSONArray();

    if ( files == null || files.length == 0 ) {
      response.put( "message", BaseMessages.getString( PKG, "GetFilesRowsCountDialog.NoFileFound.DialogMessage" ) );
    } else {
      fileList.addAll( Arrays.asList( files ) );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    }

    response.put( "files", fileList );
    return response;
  }

}
