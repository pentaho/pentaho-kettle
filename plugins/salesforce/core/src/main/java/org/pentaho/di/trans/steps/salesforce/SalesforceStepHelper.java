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

package org.pentaho.di.trans.steps.salesforce;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepInterface;

import java.util.Collections;
import java.util.Map;

public class SalesforceStepHelper extends BaseStepHelper  {

  private static final String TEST_BUTTON = "testButton";
  private static final String MODULES = "modules";
  private static final String MODULES_FLAG = "moduleFlag";

  private final SalesforceStepMeta salesforceStepMeta;

  public SalesforceStepHelper( SalesforceStepMeta salesforceStepMeta ) {
    this.salesforceStepMeta = salesforceStepMeta;
  }

  /**
   * Handles step-specific actions for SalesForce steps.
   */
  @Override
  protected JSONObject handleStepAction( String method, TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      switch ( method ) {
        case TEST_BUTTON:
          response = testButtonAction( transMeta );
          break;
        case MODULES:
          queryParams.putIfAbsent( MODULES_FLAG, "false" );
          response = modulesAction( transMeta, queryParams );
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

  public JSONObject testButtonAction( TransMeta transMeta ) {
    JSONObject response = new JSONObject();
    response.put( "connectionStatus", testConnection( transMeta ) );
    return response;
  }

  public boolean testConnection( TransMeta transMeta ) {
    boolean successConnection = true;
    SalesforceConnection connection = null;
    try {
      connection = getConnection( transMeta );
    } catch ( Exception e ) {
      successConnection = false;
      log.logError( e.getMessage() );
    } finally {
      if ( connection != null ) {
        try {
          connection.close();
        } catch ( Exception e ) {
          //Ignore
        }
      }
    }
    return successConnection;
  }

  public JSONObject modulesAction( TransMeta transMeta, Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    try {
      String[] modules = getModules( transMeta, queryParams.get( MODULES_FLAG ) );
      JSONArray modulesList = new JSONArray();
      Collections.addAll( modulesList, modules );
      response.put( MODULES, modulesList );
    } catch ( Exception e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    }
    return response;
  }

  public String[] getModules( TransMeta transMeta, String moduleFlag ) throws KettleException {
    SalesforceConnection connection = getConnection( transMeta );
    return connection.getAllAvailableObjects( Boolean.parseBoolean( moduleFlag ) );
  }

  protected SalesforceConnection getConnection( TransMeta transMeta ) throws KettleException {
    SalesforceConnection connection = null;
    try {
      String realURL = transMeta.environmentSubstitute( salesforceStepMeta.getTargetURL() );
      String realUsername = transMeta.environmentSubstitute( salesforceStepMeta.getUsername() );
      String realPassword = Utils.resolvePassword( transMeta, salesforceStepMeta.getPassword() );
      int realTimeOut = Const.toInt( transMeta.environmentSubstitute( salesforceStepMeta.getTimeout() ), 0 );

      connection = new SalesforceConnection( log, realURL, realUsername, realPassword );
      connection.setTimeOut( realTimeOut );
      connection.connect();
      return connection;
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      if ( connection != null ) {
        try {
          connection.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
    }
  }
}
