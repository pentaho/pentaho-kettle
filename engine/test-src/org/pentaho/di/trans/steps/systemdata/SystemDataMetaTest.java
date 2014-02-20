package org.pentaho.di.trans.steps.systemdata;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metastore.api.IMetaStore;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 * User: Dzmitry Stsiapanau Date: 1/20/14 Time: 3:04 PM
 */
public class SystemDataMetaTest {
  SystemDataMeta expectedSystemDataMeta;
  String expectedXML = "    <fields>\n" + "      <field>\n" + "        <name>hostname_real</name>\n"
      + "        <type>Hostname real</type>\n" + "        </field>\n" + "      <field>\n"
      + "        <name>hostname</name>\n" + "        <type>Hostname</type>\n" + "        </field>\n"
      + "      </fields>\n";

  @Before
  public void setUp() throws Exception {
    expectedSystemDataMeta = new SystemDataMeta();
    expectedSystemDataMeta.allocate( 2 );
    String[] names = expectedSystemDataMeta.getFieldName();
    SystemDataTypes[] types = expectedSystemDataMeta.getFieldType();
    names[0] = "hostname_real";
    names[1] = "hostname";
    types[0] = SystemDataMeta.getType( SystemDataMeta.getTypeDesc( SystemDataTypes.TYPE_SYSTEM_INFO_HOSTNAME_REAL ) );
    types[1] = SystemDataMeta.getType( SystemDataMeta.getTypeDesc( SystemDataTypes.TYPE_SYSTEM_INFO_HOSTNAME ) );
  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testLoadXML() throws Exception {
    SystemDataMeta systemDataMeta = new SystemDataMeta();
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse( new InputSource( new StringReader( expectedXML ) ) );
    Node node = document;
    IMetaStore store = null;
    systemDataMeta.loadXML( node, null, store );
    assertEquals( expectedSystemDataMeta, systemDataMeta );
  }

  @Test
  public void testGetXML() throws Exception {
    String generatedXML = expectedSystemDataMeta.getXML();
    assertEquals( expectedXML.replaceAll( "\n", "" ).replaceAll( "\r", "" ), generatedXML.replaceAll( "\n", "" )
        .replaceAll( "\r", "" ) );
  }
}
