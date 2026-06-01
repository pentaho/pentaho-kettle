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


package org.pentaho.di.core.xml;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class XMLHandlerUnitTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  /**
   * @see <a href="https://en.wikipedia.org/wiki/Billion_laughs" />
   */
  private static final String MALICIOUS_XML =
    "<?xml version=\"1.0\"?>\n"
      + "<!DOCTYPE lolz [\n"
      + " <!ENTITY lol \"lol\">\n"
      + " <!ELEMENT lolz (#PCDATA)>\n"
      + " <!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n"
      + " <!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n"
      + " <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n"
      + " <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n"
      + " <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n"
      + " <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n"
      + " <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n"
      + " <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n"
      + " <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n"
      + "]>\n"
      + "<lolz>&lol9;</lolz>";

  private static final String cr = Const.CR;

  @Test
  public void openTagWithNotNull() {
    assertEquals( "<qwerty>", XMLHandler.openTag( "qwerty" ) );
  }

  @Test
  public void openTagWithNull() {
    assertEquals( "<null>", XMLHandler.openTag( null ) );
  }

  @Test
  public void openTagWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    XMLHandler.openTag( builder, "rty" );
    assertEquals( "qwe<rty>", builder.toString() );
  }

  @Test
  public void closeTagWithNotNull() {
    assertEquals( "</qwerty>", XMLHandler.closeTag( "qwerty" ) );
  }

  @Test
  public void closeTagWithNull() {
    assertEquals( "</null>", XMLHandler.closeTag( null ) );
  }

  @Test
  public void closeTagWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    XMLHandler.closeTag( builder, "rty" );
    assertEquals( "qwe</rty>", builder.toString() );
  }

  @Test
  public void buildCdataWithNotNull() {
    assertEquals( "<![CDATA[qwerty]]>", XMLHandler.buildCDATA( "qwerty" ) );
  }

  @Test
  public void buildCdataWithNull() {
    assertEquals( "<![CDATA[]]>", XMLHandler.buildCDATA( null ) );
  }

  @Test
  public void buildCdataWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    XMLHandler.buildCDATA( builder, "rty" );
    assertEquals( "qwe<![CDATA[rty]]>", builder.toString() );
  }

  @Test
  public void timestamp2stringTest() {
    String actual = XMLHandler.timestamp2string( null );
    assertNull( actual );
  }

  @Test
  public void date2stringTest() {
    String actual = XMLHandler.date2string( null );
    assertNull( actual );
  }

  @Test
  public void addTagValueBigDecimal() {
    BigDecimal input = new BigDecimal( "1234567890123456789.01" );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>" + cr, XMLHandler.addTagValue( "bigdec",  input ) );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>" + cr, XMLHandler.addTagValue( "bigdec", input, true ) );
    assertEquals( "<bigdec>1234567890123456789.01</bigdec>", XMLHandler.addTagValue( "bigdec", input, false ) );
  }

  @Test
  public void addTagValueBoolean() {
    assertEquals( "<abool>Y</abool>" + cr, XMLHandler.addTagValue( "abool", true ) );
    assertEquals( "<abool>Y</abool>" + cr, XMLHandler.addTagValue( "abool", true, true ) );
    assertEquals( "<abool>Y</abool>", XMLHandler.addTagValue( "abool", true, false ) );
    assertEquals( "<abool>N</abool>" + cr, XMLHandler.addTagValue( "abool", false ) );
    assertEquals( "<abool>N</abool>" + cr, XMLHandler.addTagValue( "abool", false, true ) );
    assertEquals( "<abool>N</abool>", XMLHandler.addTagValue( "abool", false, false ) );
  }

  @Test
  public void addTagValueDate() {
    String result = "2014/12/29 15:59:45.789";
    Calendar aDate = new GregorianCalendar();
    aDate.set( 2014, ( 12 - 1 ), 29, 15, 59, 45 );
    aDate.set( Calendar.MILLISECOND, 789 );

    assertEquals( "<adate>" + result + "</adate>" + cr, XMLHandler.addTagValue( "adate", aDate.getTime() ) );
    assertEquals( "<adate>" + result + "</adate>" + cr, XMLHandler.addTagValue( "adate", aDate.getTime(), true ) );
    assertEquals( "<adate>" + result + "</adate>", XMLHandler.addTagValue( "adate", aDate.getTime(), false ) );
  }

  @Test
  public void addTagValueLong() {
    long input = 123;
    assertEquals( "<along>123</along>" + cr, XMLHandler.addTagValue( "along", input ) );
    assertEquals( "<along>123</along>" + cr, XMLHandler.addTagValue( "along", input, true ) );
    assertEquals( "<along>123</along>", XMLHandler.addTagValue( "along", input, false ) );

    assertEquals( "<along>" + String.valueOf( Long.MAX_VALUE ) + "</along>", XMLHandler.addTagValue( "along", Long.MAX_VALUE, false ) );
    assertEquals( "<along>" + String.valueOf( Long.MIN_VALUE ) + "</along>", XMLHandler.addTagValue( "along", Long.MIN_VALUE, false ) );
  }

  @Test
  public void addTagValueInt() {
    int input = 456;
    assertEquals( "<anint>456</anint>" + cr, XMLHandler.addTagValue( "anint", input ) );
    assertEquals( "<anint>456</anint>" + cr, XMLHandler.addTagValue( "anint", input, true ) );
    assertEquals( "<anint>456</anint>", XMLHandler.addTagValue( "anint", input, false ) );

    assertEquals( "<anint>" + String.valueOf( Integer.MAX_VALUE ) + "</anint>", XMLHandler.addTagValue( "anint", Integer.MAX_VALUE, false ) );
    assertEquals( "<anint>" + String.valueOf( Integer.MIN_VALUE ) + "</anint>", XMLHandler.addTagValue( "anint", Integer.MIN_VALUE, false ) );
  }

  @Test
  public void addTagValueDouble() {
    double input = 123.45;
    assertEquals( "<adouble>123.45</adouble>" + cr, XMLHandler.addTagValue( "adouble", input ) );
    assertEquals( "<adouble>123.45</adouble>" + cr, XMLHandler.addTagValue( "adouble", input, true ) );
    assertEquals( "<adouble>123.45</adouble>", XMLHandler.addTagValue( "adouble", input, false ) );

    assertEquals( "<adouble>" + String.valueOf( Double.MAX_VALUE ) + "</adouble>", XMLHandler.addTagValue( "adouble", Double.MAX_VALUE, false ) );
    assertEquals( "<adouble>" + String.valueOf( Double.MIN_VALUE ) + "</adouble>", XMLHandler.addTagValue( "adouble", Double.MIN_VALUE, false ) );
    assertEquals( "<adouble>" + String.valueOf( Double.MIN_NORMAL ) + "</adouble>", XMLHandler.addTagValue( "adouble", Double.MIN_NORMAL, false ) );
  }

  @Test
  public void addTagValueBinary() throws IOException {
    byte[] input = "Test Data".getBytes();
    String result = "H4sIAAAAAAAA/wtJLS5RcEksSQQAL4PL8QkAAAA=";

    assertEquals( "<bytedata>" + result + "</bytedata>" + cr, XMLHandler.addTagValue( "bytedata", input ) );
    assertEquals( "<bytedata>" + result + "</bytedata>" + cr, XMLHandler.addTagValue( "bytedata", input, true ) );
    assertEquals( "<bytedata>" + result + "</bytedata>", XMLHandler.addTagValue( "bytedata", input, false ) );
  }

  @Test
  public void addTagValueWithSurrogateCharacters() throws Exception {
    String expected = "<testTag attributeTest=\"test attribute value \uD842\uDFB7\" >a\uD800\uDC01\uD842\uDFB7ﻉＤtest \uD802\uDF44&lt;</testTag>";
    String tagValueWithSurrogates = "a\uD800\uDC01\uD842\uDFB7ﻉＤtest \uD802\uDF44<";
    String attributeValueWithSurrogates = "test attribute value \uD842\uDFB7";
    String result = XMLHandler.addTagValue( "testTag", tagValueWithSurrogates, false, "attributeTest", attributeValueWithSurrogates );
    assertEquals( expected, result );
    DocumentBuilder builder = XMLHandler.createDocumentBuilder( false, false );
    builder.parse( new ByteArrayInputStream( result.getBytes() ) );
  }

  @Test
  public void testEscapingXmlBagCharacters() throws Exception {
    String testString = "[value_start (\"\'<&>) value_end]";
    String expectedStrAfterConversion = "<[value_start (&#34;&#39;&lt;&amp;&gt;) value_end] "
      + "[value_start (&#34;&#39;&lt;&amp;&gt;) value_end]=\""
      + "[value_start (&#34;&#39;&lt;&amp;>) value_end]\" >"
      + "[value_start (&#34;&#39;&lt;&amp;&gt;) value_end]"
      + "</[value_start (&#34;&#39;&lt;&amp;&gt;) value_end]>";
    String result = XMLHandler.addTagValue( testString, testString, false, testString, testString );
    assertEquals( expectedStrAfterConversion, result );
  }

  @Test( expected = SAXParseException.class )
  public void createdDocumentBuilderThrowsExceptionWhenParsingXmlWithABigAmountOfExternalEntities() throws Exception {
    DocumentBuilder builder = XMLHandler.createDocumentBuilder( false, false );
    builder.parse( new ByteArrayInputStream( MALICIOUS_XML.getBytes() ) );
  }

  @Test( expected = KettleXMLException.class )
  public void loadingXmlFromStreamThrowsExceptionWhenParsingXmlWithBigAmountOfExternalEntities() throws Exception {
    XMLHandler.loadXMLFile( new ByteArrayInputStream( MALICIOUS_XML.getBytes() ), "<def>", false, false );
  }

  @Test( expected = KettleXMLException.class )
  public void loadingXmlFromURLThrowsExceptionWhenParsingXmlWithBigAmountOfExternalEntities() throws Exception {
    File tmpFile = createTmpFile( MALICIOUS_XML );

    XMLHandler.loadXMLFile( tmpFile.toURI().toURL() );
  }

  private File createTmpFile( String content ) throws Exception {
    File tmpFile = File.createTempFile( "XMLHandlerUnitTest", ".xml" );
    tmpFile.deleteOnExit();

    try ( PrintWriter writer = new PrintWriter( tmpFile ) ) {
      writer.write( content );
    }

    return tmpFile;
  }

  @Test
  public void testGetSubNode() throws Exception {
    String testXML =
      "<?xml version=\"1.0\"?>\n"
        + "<root>\n"
        + "<xpto>A</xpto>\n"
        + "<xpto>B</xpto>\n"
        + "<xpto>C</xpto>\n"
        + "<xpto>D</xpto>\n"
        + "</root>\n";

    DocumentBuilder builder = XMLHandler.createDocumentBuilder( false, false );

    Document parse = builder.parse( new ByteArrayInputStream( testXML.getBytes() ) );
    Node rootNode = parse.getFirstChild();
    Node lastSubNode = XMLHandler.getSubNode( rootNode, "xpto" );
    assertNotNull( lastSubNode );
    assertEquals( "A", lastSubNode.getTextContent() );
  }

  @Test
  public void testGetLastSubNode() throws Exception {
    String testXML =
      "<?xml version=\"1.0\"?>\n"
        + "<root>\n"
        + "<xpto>A</xpto>\n"
        + "<xpto>B</xpto>\n"
        + "<xpto>C</xpto>\n"
        + "<xpto>D</xpto>\n"
        + "</root>\n";

    DocumentBuilder builder = XMLHandler.createDocumentBuilder( false, false );

    Document parse = builder.parse( new ByteArrayInputStream( testXML.getBytes() ) );
    Node rootNode = parse.getFirstChild();
    Node lastSubNode = XMLHandler.getLastSubNode( rootNode, "xpto" );
    assertNotNull( lastSubNode );
    assertEquals( "D", lastSubNode.getTextContent() );
  }

  @Test
  public void testGetSubNodeByNr_WithCache() throws Exception {
    String testXML =
      "<?xml version=\"1.0\"?>\n"
        + "<root>\n"
        + "<xpto>0</xpto>\n"
        + "<xpto>1</xpto>\n"
        + "<xpto>2</xpto>\n"
        + "<xpto>3</xpto>\n"
        + "</root>\n";

    DocumentBuilder builder = XMLHandler.createDocumentBuilder( false, false );

    Document parse = builder.parse( new ByteArrayInputStream( testXML.getBytes() ) );
    Node rootNode = parse.getFirstChild();

    Node subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 0 );
    assertNotNull( subNode );
    assertEquals( "0", subNode.getTextContent() );
    subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 1 );
    assertNotNull( subNode );
    assertEquals( "1", subNode.getTextContent() );
    subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 2 );
    assertNotNull( subNode );
    assertEquals( "2", subNode.getTextContent() );
    subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 3 );
    assertNotNull( subNode );
    assertEquals( "3", subNode.getTextContent() );
  }

  @Test
  public void testGetSubNodeByNr_WithoutCache() throws Exception {
    String testXML =
      "<?xml version=\"1.0\"?>\n"
        + "<root>\n"
        + "<xpto>0</xpto>\n"
        + "<xpto>1</xpto>\n"
        + "<xpto>2</xpto>\n"
        + "<xpto>3</xpto>\n"
        + "</root>\n";

    DocumentBuilder builder = XMLHandler.createDocumentBuilder( false, false );

    Document parse = builder.parse( new ByteArrayInputStream( testXML.getBytes() ) );
    Node rootNode = parse.getFirstChild();

    Node subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 0, false );
    assertNotNull( subNode );
    assertEquals( "0", subNode.getTextContent() );
    subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 1, false );
    assertNotNull( subNode );
    assertEquals( "1", subNode.getTextContent() );
    subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 2, false );
    assertNotNull( subNode );
    assertEquals( "2", subNode.getTextContent() );
    subNode = XMLHandler.getSubNodeByNr( rootNode, "xpto", 3, false );
    assertNotNull( subNode );
    assertEquals( "3", subNode.getTextContent() );
  }

  /**
   * Test that the default cached DocumentBuilder parses XML correctly.
   * Verifies ThreadLocal caching of builders works and produces valid Documents.
   */
  @Test
  public void cachedDefaultBuilderParsesXmlCorrectly() throws Exception {
    String simpleXml = "<?xml version=\"1.0\"?><root><item>test</item></root>";
    Document doc = XMLHandler.loadXMLString( simpleXml );
    assertNotNull( "Document should not be null", doc );
    assertNotNull( "Root element should exist", doc.getDocumentElement() );
    assertEquals( "Root element name", "root", doc.getDocumentElement().getNodeName() );
  }

  /**
   * Test that the namespace-aware cached DocumentBuilder works correctly.
   */
  @Test
  public void cachedNamespaceAwareBuilderParsesXmlCorrectly() throws Exception {
    String nsXml = "<?xml version=\"1.0\"?><root xmlns=\"http://example.com\"><item>test</item></root>";
    Document doc = XMLHandler.loadXMLString( nsXml, true, true );
    assertNotNull( "Document should not be null", doc );
    assertNotNull( "Root element should exist", doc.getDocumentElement() );
    assertEquals( "Root element local name", "root", doc.getDocumentElement().getLocalName() );
  }

  /**
   * Test that cached builders produce the same results as fresh builders.
   * This ensures ThreadLocal caching is functionally equivalent.
   */
  @Test
  public void cachedBuilderProducesIdenticalResultsAsNewBuilder() throws Exception {
    String testXml = "<?xml version=\"1.0\"?><root attr=\"value\"><child>text</child></root>";
    
    // Parse with cached builder
    Document cachedDoc = XMLHandler.loadXMLString( testXml );
    
    // Parse with new builder
    DocumentBuilder freshBuilder = XMLHandler.createDocumentBuilder( false, true );
    Document freshDoc = XMLHandler.loadXMLString( freshBuilder, testXml );
    
    assertNotNull( "Cached document", cachedDoc );
    assertNotNull( "Fresh document", freshDoc );
    assertEquals( "Root element names match",
      cachedDoc.getDocumentElement().getNodeName(),
      freshDoc.getDocumentElement().getNodeName() );
    assertEquals( "Child node count",
      cachedDoc.getDocumentElement().getChildNodes().getLength(),
      freshDoc.getDocumentElement().getChildNodes().getLength() );
  }

  /**
   * Test that non-standard configurations work (creates fresh builder, not cached).
   */
  @Test
  public void nonStandardConfigurationCreatesFreshBuilder() throws Exception {
    String testXml = "<?xml version=\"1.0\"?><root><item>test</item></root>";
    Document doc = XMLHandler.loadXMLString( testXml, true, false );
    assertNotNull( "Non-standard config should parse", doc );
    assertEquals( "Root element name", "root", doc.getDocumentElement().getNodeName() );
  }

  /**
   * Test concurrent access to cached builders.
   * Each thread gets its own DocumentBuilder via ThreadLocal, so no synchronization issues.
   */
  @Test
  public void concurrentAccessToCachedBuilders() throws Exception {
    String testXml = "<?xml version=\"1.0\"?><root><data>concurrent test</data></root>";
    final int threadCount = 10;
    final int iterationsPerThread = 20;
    
    Thread[] threads = new Thread[threadCount];
    final Exception[] exceptions = new Exception[threadCount];
    
    for ( int i = 0; i < threadCount; i++ ) {
      final int threadIndex = i;
      threads[i] = new Thread( () -> {
        try {
          for ( int j = 0; j < iterationsPerThread; j++ ) {
            // Each thread uses ThreadLocal cached builders
            Document doc1 = XMLHandler.loadXMLString( testXml );
            assertNotNull( "Thread " + threadIndex + " iteration " + j, doc1 );
            
            Document doc2 = XMLHandler.loadXMLString( testXml, true, true );
            assertNotNull( "Thread " + threadIndex + " iteration " + j + " (NS aware)", doc2 );
          }
        } catch ( Exception e ) {
          exceptions[threadIndex] = e;
        }
      } );
    }
    
    // Start all threads
    for ( Thread thread : threads ) {
      thread.start();
    }
    
    // Wait for all threads to complete
    for ( Thread thread : threads ) {
      thread.join();
    }
    
    // Check for exceptions
    for ( int i = 0; i < threadCount; i++ ) {
      assertNull( "Thread " + i + " should not throw exception", exceptions[i] );
    }
  }

  /**
   * Test rapid repeated parsing with cached builders.
   * Simulates repeated parsing scenarios and verifies functional correctness.
   */
  @Test
  public void cachedBuilderHandlesRapidRepeatParsing() throws Exception {
    String testXml = "<?xml version=\"1.0\"?><root><item>test</item></root>";

    // Rapid repeated parsing (similar to addOrReplaceSlaveServer loop)
    for ( int i = 0; i < 1000; i++ ) {
      Document doc = XMLHandler.loadXMLString( testXml );
      assertNotNull( doc );
      assertNotNull( doc.getDocumentElement() );
      assertEquals( "root", doc.getDocumentElement().getNodeName() );
    }
  }

  /**
   * Test that complex XML with mixed namespaces parses correctly with cached builder.
   */
  @Test
  public void complexXmlWithMixedContentParsesCorrectly() throws Exception {
    String complexXml = "<?xml version=\"1.0\"?>\n"
      + "<root xmlns=\"http://default.example.com\" xmlns:custom=\"http://custom.example.com\">\n"
      + "  <item>Default namespace</item>\n"
      + "  <custom:item>Custom namespace</custom:item>\n"
      + "</root>";
    
    Document doc = XMLHandler.loadXMLString( complexXml, true, true );
    assertNotNull( "Complex document should parse", doc );
    assertNotNull( "Root element should exist", doc.getDocumentElement() );
  }

  /**
   * Test entity reference handling with cached builders.
   */
  @Test
  public void entityReferencesHandledCorrectlyWithCachedBuilder() throws Exception {
    String xmlWithEntities = "<?xml version=\"1.0\"?><root attr=\"value &amp; more\">"
      + "<item>&lt;test&gt;</item></root>";
    
    Document doc = XMLHandler.loadXMLString( xmlWithEntities );
    assertNotNull( "Document with entities should parse", doc );
    
    String text = doc.getDocumentElement().getFirstChild().getTextContent();
    assertEquals( "Entity references should be expanded", "<test>", text );
  }
}
