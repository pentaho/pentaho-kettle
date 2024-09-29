/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class KettleVariablesList {

  //helper to make the KettleVariablesList thread safe lazy initialized singleton
  private static class KettleVariablesListHelper {
    private static final KettleVariablesList INSTANCE = new KettleVariablesList();
  }

  private static LogChannelInterface logger;

  private KettleVariablesList() {
    logger = new LogChannel( this );
    descriptionMap = new HashMap<String, String>();
    defaultValueMap = new HashMap<String, String>();
  }

  public static KettleVariablesList getInstance() {
    return KettleVariablesListHelper.INSTANCE;
  }

  private Map<String, String> descriptionMap;
  private Map<String, String> defaultValueMap;

  public static void init() throws KettleException {

    InputStream inputStream = null;
    try {
      KettleVariablesList variablesList = getInstance();

      inputStream = variablesList.getClass().getResourceAsStream( Const.KETTLE_VARIABLES_FILE );

      if ( inputStream == null ) {
        inputStream = variablesList.getClass().getResourceAsStream( "/" + Const.KETTLE_VARIABLES_FILE );
      }
      if ( inputStream == null ) {
        throw new KettlePluginException( "Unable to find standard kettle variables definition file: " + Const.KETTLE_VARIABLES_FILE );
      }
      Document doc = XMLHandler.loadXMLFile( inputStream, null, false, false );
      Node varsNode = XMLHandler.getSubNode( doc, "kettle-variables" );
      int nrVars = XMLHandler.countNodes( varsNode, "kettle-variable" );
      for ( int i = 0; i < nrVars; i++ ) {
        Node varNode = XMLHandler.getSubNodeByNr( varsNode, "kettle-variable", i );
        String description = XMLHandler.getTagValue( varNode, "description" );
        String variable = XMLHandler.getTagValue( varNode, "variable" );
        String defaultValue = XMLHandler.getTagValue( varNode, "default-value" );

        variablesList.getDescriptionMap().put( variable, description );
        variablesList.getDefaultValueMap().put( variable, defaultValue );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to read file '" + Const.KETTLE_VARIABLES_FILE + "'", e );
    } finally {
      if ( inputStream != null ) {
        try {
          inputStream.close();
        } catch ( IOException e ) {
          // we do not able to close property file will log it
          logger.logDetailed( "Unable to close file kettle variables definition file", e );
        }
      }
    }
  }

  /**
   * @return A mapping between the name of a standard kettle variable and its description.
   */
  public Map<String, String> getDescriptionMap() {
    return descriptionMap;
  }

  /**
   * @return A mapping between the name of a standard kettle variable and its default value.
   */
  public Map<String, String> getDefaultValueMap() {
    return defaultValueMap;
  }

}
