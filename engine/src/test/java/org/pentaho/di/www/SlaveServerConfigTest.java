/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.www;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.RepositoryMeta;
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
    KettleClientEnvironment.init();
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

  @Test
  public void testIsExecuteUrlAllowed_HandlesColonStyleVfsProtocols() {
    assertFalse( slServerConfig.isExecuteUrlAllowed( "file:/tmp/test.ktr" ) );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "file:relative/path/test.ktr" ) );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "zip:/path/to/archive.zip" ) );
    assertTrue( slServerConfig.isExecuteUrlAllowed( "C:/tmp/test.ktr" ) );

    slServerConfig.setEnableUnsafeProtocolsForExecute( true );
    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "file", "zip" ) );

    assertTrue( slServerConfig.isExecuteUrlAllowed( "file:/tmp/test.ktr" ) );
    assertTrue( slServerConfig.isExecuteUrlAllowed( "file:relative/path/test.ktr" ) );
    assertTrue( slServerConfig.isExecuteUrlAllowed( "zip:/path/to/archive.zip" ) );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "s3:/bucket/test.ktr" ) );
  }

  @Test
  public void testIsExecuteUrlAllowed_HandlesEmptyLocalAndPvfsPaths() {
    assertTrue( slServerConfig.isExecuteUrlAllowed( "" ) );
    assertTrue( slServerConfig.isExecuteUrlAllowed( "path/to/relative.ktr" ) );
    assertTrue( slServerConfig.isExecuteUrlAllowed( "pvfs://repo/dir/test.ktr" ) );
    assertTrue( slServerConfig.isExecuteUrlAllowed( "pvfs:/repo/dir/test.ktr" ) );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "s3://bucket/test.ktr" ) );

    slServerConfig.setEnableUnsafeProtocolsForExecute( true );
    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "s3", "", "  " ) );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "s3:/bucket/test.ktr" ) );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "ftp://host/path/test.ktr" ) );
  }

  @Test
  public void testKettleVfsGetScheme_HandlesPathAndProtocolVariants() {
    assertEquals( "http", KettleVFS.getScheme( "http://example.com/test.ktr" ) );
    assertEquals( "file", KettleVFS.getScheme( "file:/tmp/test.ktr" ) );
    assertEquals( "file", KettleVFS.getScheme( "file:relative/path/test.ktr" ) );
    assertEquals( "zip", KettleVFS.getScheme( "zip:/path/to/archive.zip" ) );
    assertNull( KettleVFS.getScheme( "C:/tmp/test.ktr" ) );
    assertNull( KettleVFS.getScheme( "relative/path/test.ktr" ) );
    assertNull( KettleVFS.getScheme( "local-file.ktr" ) );
    assertNull( KettleVFS.getScheme( "" ) );
  }

  @Test
  public void testParseUnsafeAllowedProtocolsForExecute_IgnoresNullAndEmptyEntries() throws KettleXMLException {
    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "file", "zip" ) );
    slServerConfig.parseUnsafeAllowedProtocolsForExecute( null );
    assertEquals( 2, slServerConfig.getUnsafeAllowedProtocolsForExecute().size() );

    String xml = "<slave_config>"
      + "<unsafeAllowedProtocolsForExecute>"
      + "<protocol>file</protocol>"
      + "<protocol> </protocol>"
      + "<protocol>zip</protocol>"
      + "<protocol>ftp</protocol>"
      + "</unsafeAllowedProtocolsForExecute>"
      + "</slave_config>";
    Node configNode = getConfigNode( xml );
    slServerConfig.parseUnsafeAllowedProtocolsForExecute( configNode );

    assertEquals( 3, slServerConfig.getUnsafeAllowedProtocolsForExecute().size() );
    assertEquals( "file", slServerConfig.getUnsafeAllowedProtocolsForExecute().get( 0 ) );
    assertEquals( "zip", slServerConfig.getUnsafeAllowedProtocolsForExecute().get( 1 ) );
    assertEquals( "ftp", slServerConfig.getUnsafeAllowedProtocolsForExecute().get( 2 ) );
  }

  @Test
  public void testGetXmlIncludesUnsafeProtocolSettings() {
    slServerConfig.setEnableUnsafeProtocolsForExecute( true );
    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "file", "zip" ) );

    String xml = slServerConfig.getXML();

    assertTrue( xml.contains( "<enableUnsafeProtocolsForExecute>Y</enableUnsafeProtocolsForExecute>" ) );
    assertTrue( xml.contains( "<unsafeAllowedProtocolsForExecute>" ) );
    assertTrue( xml.contains( "<protocol>file</protocol>" ) );
    assertTrue( xml.contains( "<protocol>zip</protocol>" ) );
  }

  @Test
  public void testIsExecuteUrlAllowed_HandlesUppercaseAllowlistAndNullInputs() {
    slServerConfig.setEnableUnsafeProtocolsForExecute( true );
    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "S3", null, "  ", "ftp" ) );

    assertFalse( slServerConfig.isExecuteUrlAllowed( "S3:/bucket/test.ktr" ) );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "ftps:/secure/test.ktr" ) );

    slServerConfig.setUnsafeAllowedProtocolsForExecute( null );
    assertFalse( slServerConfig.isExecuteUrlAllowed( "s3:/bucket/test.ktr" ) );

    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "file", "zip" ) );
    slServerConfig.parseUnsafeAllowedProtocolsForExecute( null );
    assertEquals( 2, slServerConfig.getUnsafeAllowedProtocolsForExecute().size() );
  }

  @Test
  public void testParseUnsafeAllowedProtocolsForExecute_NoNodeDoesNothing() throws KettleXMLException {
    String xml = "<slave_config><joining>Y</joining></slave_config>";

    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "file", "zip" ) );
    slServerConfig.parseUnsafeAllowedProtocolsForExecute( getConfigNode( xml ) );

    assertEquals( 2, slServerConfig.getUnsafeAllowedProtocolsForExecute().size() );
  }

  @Test
  public void testGetUnsafeAllowedProtocolsForExecute_UsesDefensiveCopy() {
    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "file", "zip" ) );

    java.util.List<String> returnedProtocols = slServerConfig.getUnsafeAllowedProtocolsForExecute();
    assertEquals( 2, returnedProtocols.size() );
    returnedProtocols.add( "ftp" );

    assertEquals( java.util.Arrays.asList( "file", "zip" ), slServerConfig.getUnsafeAllowedProtocolsForExecute() );

    slServerConfig.setUnsafeAllowedProtocolsForExecute( java.util.Arrays.asList( "s3" ) );
    assertEquals( java.util.Arrays.asList( "s3" ), slServerConfig.getUnsafeAllowedProtocolsForExecute() );
  }

  @Test
  public void testGetXmlIncludesPreviouslyBrokenValues() {
    slServerConfig.setReportingToMasters( true );
    slServerConfig.setJoining( true );
    slServerConfig.setMaxLogLines( 123 );
    slServerConfig.setMaxLogTimeoutMinutes( 456 );
    slServerConfig.setObjectTimeoutMinutes( 789 );

    String xml = slServerConfig.getXML();

    assertTrue( xml.contains( "<report_to_masters>Y</report_to_masters>" ) );
    assertTrue( xml.contains( "<joining>Y</joining>" ) );
    assertTrue( xml.contains( "<max_log_lines>123</max_log_lines>" ) );
    assertTrue( xml.contains( "<max_log_timeout_minutes>456</max_log_timeout_minutes>" ) );
    assertTrue( xml.contains( "<object_timeout_minutes>789</object_timeout_minutes>" ) );
  }

  @Test
  public void testAccessorsAndXmlBranchesForRemainingCoverage() throws Exception {
    SlaveServer master = new SlaveServer( "master", "localhost", "8181", null, null );
    SlaveServer slave = new SlaveServer( "slave", "localhost", "8182", null, null );
    java.util.List<SlaveServer> masters = java.util.Arrays.asList( master );

    slServerConfig.setMasters( masters );
    slServerConfig.setSlaveServer( slave );
    slServerConfig.setFilename( "sample.ktr" );
    slServerConfig.setDatabases( java.util.Collections.emptyList() );
    slServerConfig.setSlaveSequences( java.util.Collections.emptyList() );
    slServerConfig.setAutomaticCreationAllowed( true );
    slServerConfig.setPasswordFile( "passwords.kdb" );
    slServerConfig.setRepositoryId( "repo-id" );
    slServerConfig.setRepositoryUsername( "user" );
    slServerConfig.setRepositoryPassword( "pass" );
    slServerConfig.setMetastoreSupplier( () -> null );

    assertEquals( masters, slServerConfig.getMasters() );
    assertSame( slave, slServerConfig.getSlaveServer() );
    assertEquals( "sample.ktr", slServerConfig.getFilename() );
    assertTrue( slServerConfig.getDatabases().isEmpty() );
    assertTrue( slServerConfig.getSlaveSequences().isEmpty() );
    assertTrue( slServerConfig.isAutomaticCreationAllowed() );
    assertEquals( "passwords.kdb", slServerConfig.getPasswordFile() );
    assertEquals( "repo-id", slServerConfig.getRepositoryId() );
    assertEquals( "user", slServerConfig.getRepositoryUsername() );
    assertEquals( "pass", slServerConfig.getRepositoryPassword() );
    assertNull( slServerConfig.getMetaStore() );
    assertNotNull( slServerConfig.getMetastoreSupplier() );

    SlaveSequence autoSequence = new SlaveSequence( "seq", 3, null, "public", "table", "sequence_field", "value_field" );
    slServerConfig.setAutoSequence( autoSequence );
    slServerConfig.setAutomaticCreationAllowed( false );
    String xmlWithAutoSequence = slServerConfig.getXML();
    assertTrue( xmlWithAutoSequence.contains( "<autosequence>" ) );
    assertTrue( xmlWithAutoSequence.contains( "<sequence_field>sequence_field</sequence_field>" ) );

    RepositoryMeta repositoryMeta = (RepositoryMeta) java.lang.reflect.Proxy.newProxyInstance(
        RepositoryMeta.class.getClassLoader(),
        new Class<?>[] { RepositoryMeta.class },
        ( proxy, method, args ) -> {
          if ( "getId".equals( method.getName() ) ) {
            return "repo-id";
          }
          if ( "getXML".equals( method.getName() ) ) {
            return "<repository/>";
          }
          return null;
        } );

    java.lang.reflect.Field repositoryMetaField = SlaveServerConfig.class.getDeclaredField( "repositoryMeta" );
    repositoryMetaField.setAccessible( true );
    repositoryMetaField.set( slServerConfig, repositoryMeta );

    String xmlWithRepository = slServerConfig.getXML();
    assertTrue( xmlWithRepository.contains( "<repository>" ) );
    assertTrue( xmlWithRepository.contains( "<id>repo-id</id>" ) );
    assertTrue( xmlWithRepository.contains( "<username>user</username>" ) );
    assertTrue( xmlWithRepository.contains( "<password>" ) );
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
//    System.out.println( xml.toString() );
    return xml.toString();
  }

}
