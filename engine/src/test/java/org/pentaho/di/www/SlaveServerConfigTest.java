/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.www;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Tatsiana_Kasiankova
 *
 */
public class SlaveServerConfigTest {

  public static final String XML_TAG_SLAVE_CONFIG = "slave_config";
  public static final String XML_TAG_JETTY_OPTIONS = "jetty_options";
  public static final String XML_TAG_ACCEPTORS = "acceptors";
  public static final String XML_TAG_ACCEPT_QUEUE_SIZE = "acceptQueueSize";
  public static final String XML_TAG_LOW_RES_MAX_IDLE_TIME = "lowResourcesMaxIdleTime";

  public static final String ACCEPTORS_VALUE = "10";
  public static final String EXPECTED_ACCEPTORS_VALUE = "10";
  public static final String EXPECTED_ACCEPTORS_KEY = Const.KETTLE_CARTE_JETTY_ACCEPTORS;

  public static final String ACCEPT_QUEUE_SIZE_VALUE = "8000";
  public static final String EXPECTED_ACCEPT_QUEUE_SIZE_VALUE = "8000";
  public static final String EXPECTED_ACCEPT_QUEUE_SIZE_KEY = Const.KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE;

  public static final String LOW_RES_MAX_IDLE_TIME_VALUE = "300";
  public static final String EXPECTED_LOW_RES_MAX_IDLE_TIME_VALUE = "300";
  public static final String EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY = Const.KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME;

  Map<String, String> jettyOptions;
  SlaveServerConfig slServerConfig;

  @Before
  public void setup() throws Exception {
    slServerConfig = new SlaveServerConfig();
  }

  @After
  public void tearDown() {
    System.getProperties().remove( Const.KETTLE_CARTE_JETTY_ACCEPTORS );
    System.getProperties().remove( Const.KETTLE_CARTE_JETTY_ACCEPT_QUEUE_SIZE );
    System.getProperties().remove( Const.KETTLE_CARTE_JETTY_RES_MAX_IDLE_TIME );
  }

  @Test
  public void testSetUpJettyOptionsAsSystemParameters() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithAllOptions() );

    slServerConfig.setUpJettyOptions( configNode );

    assertTrue( "Expected containing jetty option " + EXPECTED_ACCEPTORS_KEY, System.getProperties().containsKey(
        EXPECTED_ACCEPTORS_KEY ) );
    assertEquals( EXPECTED_ACCEPTORS_VALUE, System.getProperty( EXPECTED_ACCEPTORS_KEY ) );
    assertTrue( "Expected containing jetty option " + EXPECTED_ACCEPT_QUEUE_SIZE_KEY, System.getProperties()
        .containsKey( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
    assertEquals( EXPECTED_ACCEPT_QUEUE_SIZE_VALUE, System.getProperty( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
    assertTrue( "Expected containing jetty option " + EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY, System.getProperties()
        .containsKey( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
    assertEquals( EXPECTED_LOW_RES_MAX_IDLE_TIME_VALUE, System.getProperty( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
  }

  @Test
  public void testDoNotSetUpJettyOptionsAsSystemParameters_WhenNoOptionsNode() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithNoOptionsNode() );

    slServerConfig.setUpJettyOptions( configNode );

    assertFalse( "There should not be any jetty option but it is here:  " + EXPECTED_ACCEPTORS_KEY, System
        .getProperties().containsKey( EXPECTED_ACCEPTORS_KEY ) );
    assertFalse( "There should not be any jetty option but it is here:  " + EXPECTED_ACCEPT_QUEUE_SIZE_KEY, System
        .getProperties().containsKey( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
    assertFalse( "There should not be any jetty option but it is here:  " + EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY, System
        .getProperties().containsKey( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
  }

  @Test
  public void testDoNotSetUpJettyOptionsAsSystemParameters_WhenEmptyOptionsNode() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithEmptyOptionsNode() );

    slServerConfig.setUpJettyOptions( configNode );

    assertFalse( "There should not be any jetty option but it is here:  " + EXPECTED_ACCEPTORS_KEY, System
        .getProperties().containsKey( EXPECTED_ACCEPTORS_KEY ) );
    assertFalse( "There should not be any jetty option but it is here:  " + EXPECTED_ACCEPT_QUEUE_SIZE_KEY, System
        .getProperties().containsKey( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
    assertFalse( "There should not be any jetty option but it is here:  " + EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY, System
        .getProperties().containsKey( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
  }

  @Test
  public void testParseJettyOption_Acceptors() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithAcceptorsOnlyOption() );

    Map<String, String> parseJettyOptions = slServerConfig.parseJettyOptions( configNode );

    assertNotNull( parseJettyOptions );
    assertEquals( 1, parseJettyOptions.size() );
    assertTrue( "Expected containing key=" + EXPECTED_ACCEPTORS_KEY, parseJettyOptions
        .containsKey( EXPECTED_ACCEPTORS_KEY ) );
    assertEquals( EXPECTED_ACCEPTORS_VALUE, parseJettyOptions.get( EXPECTED_ACCEPTORS_KEY ) );
  }

  @Test
  public void testParseJettyOption_AcceptQueueSize() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithAcceptQueueSizeOnlyOption() );

    Map<String, String> parseJettyOptions = slServerConfig.parseJettyOptions( configNode );

    assertNotNull( parseJettyOptions );
    assertEquals( 1, parseJettyOptions.size() );
    assertTrue( "Expected containing key=" + EXPECTED_ACCEPT_QUEUE_SIZE_KEY, parseJettyOptions
        .containsKey( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
    assertEquals( EXPECTED_ACCEPT_QUEUE_SIZE_VALUE, parseJettyOptions.get( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
  }

  @Test
  public void testParseJettyOption_LowResourcesMaxIdleTime() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithLowResourcesMaxIdleTimeeOnlyOption() );

    Map<String, String> parseJettyOptions = slServerConfig.parseJettyOptions( configNode );

    assertNotNull( parseJettyOptions );
    assertEquals( 1, parseJettyOptions.size() );
    assertTrue( "Expected containing key=" + EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY, parseJettyOptions
        .containsKey( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
    assertEquals( EXPECTED_LOW_RES_MAX_IDLE_TIME_VALUE, parseJettyOptions.get( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
  }

  @Test
  public void testParseJettyOption_AllOptions() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithAllOptions() );

    Map<String, String> parseJettyOptions = slServerConfig.parseJettyOptions( configNode );

    assertNotNull( parseJettyOptions );
    assertEquals( 3, parseJettyOptions.size() );
    assertTrue( "Expected containing key=" + EXPECTED_ACCEPTORS_KEY, parseJettyOptions
        .containsKey( EXPECTED_ACCEPTORS_KEY ) );
    assertEquals( EXPECTED_ACCEPTORS_VALUE, parseJettyOptions.get( EXPECTED_ACCEPTORS_KEY ) );
    assertTrue( "Expected containing key=" + EXPECTED_ACCEPT_QUEUE_SIZE_KEY, parseJettyOptions
        .containsKey( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
    assertEquals( EXPECTED_ACCEPT_QUEUE_SIZE_VALUE, parseJettyOptions.get( EXPECTED_ACCEPT_QUEUE_SIZE_KEY ) );
    assertTrue( "Expected containing key=" + EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY, parseJettyOptions
        .containsKey( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
    assertEquals( EXPECTED_LOW_RES_MAX_IDLE_TIME_VALUE, parseJettyOptions.get( EXPECTED_LOW_RES_MAX_IDLE_TIME_KEY ) );
  }

  @Test
  public void testParseJettyOption_EmptyOptionsNode() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithEmptyOptionsNode() );

    Map<String, String> parseJettyOptions = slServerConfig.parseJettyOptions( configNode );

    assertNotNull( parseJettyOptions );
    assertEquals( 0, parseJettyOptions.size() );
  }

  @Test
  public void testParseJettyOption_NoOptionsNode() throws KettleXMLException {
    Node configNode = getConfigNode( getConfigWithNoOptionsNode() );

    Map<String, String> parseJettyOptions = slServerConfig.parseJettyOptions( configNode );
    assertNull( parseJettyOptions );
  }

  private Node getConfigNode( String configString ) throws KettleXMLException {
    Document document = XMLHandler.loadXMLString( configString );
    Node configNode = XMLHandler.getSubNode( document, SlaveServerConfig.XML_TAG );
    return configNode;
  }

  private String getConfigWithAcceptorsOnlyOption() {
    jettyOptions = new HashMap<String, String>();
    jettyOptions.put( XML_TAG_ACCEPTORS, ACCEPTORS_VALUE );
    return getConfig( jettyOptions );
  }

  private String getConfigWithAcceptQueueSizeOnlyOption() {
    jettyOptions = new HashMap<String, String>();
    jettyOptions.put( XML_TAG_ACCEPT_QUEUE_SIZE, ACCEPT_QUEUE_SIZE_VALUE );
    return getConfig( jettyOptions );
  }

  private String getConfigWithLowResourcesMaxIdleTimeeOnlyOption() {
    jettyOptions = new HashMap<String, String>();
    jettyOptions.put( XML_TAG_LOW_RES_MAX_IDLE_TIME, LOW_RES_MAX_IDLE_TIME_VALUE );
    return getConfig( jettyOptions );
  }

  private String getConfigWithAllOptions() {
    jettyOptions = new HashMap<String, String>();
    jettyOptions.put( XML_TAG_ACCEPTORS, ACCEPTORS_VALUE );
    jettyOptions.put( XML_TAG_ACCEPT_QUEUE_SIZE, ACCEPT_QUEUE_SIZE_VALUE );
    jettyOptions.put( XML_TAG_LOW_RES_MAX_IDLE_TIME, LOW_RES_MAX_IDLE_TIME_VALUE );
    return getConfig( jettyOptions );
  }

  private String getConfigWithEmptyOptionsNode() {
    jettyOptions = new HashMap<String, String>();
    return getConfig( jettyOptions );
  }

  private String getConfigWithNoOptionsNode() {
    return getConfig( jettyOptions );
  }

  private String getConfig( Map<String, String> jettyOptions ) {
    StringBuilder xml = new StringBuilder( 50 );
    xml.append( XMLHandler.getXMLHeader( Const.XML_ENCODING ) );
    xml.append( "<" + XML_TAG_SLAVE_CONFIG + ">" ).append( Const.CR );
    if ( jettyOptions != null ) {
      xml.append( "<" + XML_TAG_JETTY_OPTIONS + ">" ).append( Const.CR );
      for ( Entry<String, String> jettyOption : jettyOptions.entrySet() ) {
        xml.append( "<" + jettyOption.getKey() + ">" ).append( jettyOption.getValue() );
        xml.append( "</" + jettyOption.getKey() + ">" ).append( Const.CR );
      }
      xml.append( "</" + XML_TAG_JETTY_OPTIONS + ">" ).append( Const.CR );
    }
    xml.append( "</" + XML_TAG_SLAVE_CONFIG + ">" ).append( Const.CR );
    System.out.println( xml.toString() );
    return xml.toString();
  }

}
