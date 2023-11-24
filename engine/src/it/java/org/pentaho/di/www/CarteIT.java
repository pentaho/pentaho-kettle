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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class CarteIT {

  private ServletTester tester = null;

  private CarteSingleton carte = CarteSingleton.getInstance();

  @Before
  public void before() {

    carte.setJobMap( new JobMap() );
    carte.setTransformationMap( new TransformationMap() );
    carte.setSocketRepository( new SocketRepository( new LogChannel( "Carte" ) ) );

    tester = new ServletTester();
    tester.addServlet( GetRootServlet.class, "/*" );
    tester.addServlet( GetStatusServlet.class, GetStatusServlet.CONTEXT_PATH );
    tester.addServlet( AddTransServlet.class, RegisterTransServlet.CONTEXT_PATH );
    tester.addServlet( StartTransServlet.class, StartTransServlet.CONTEXT_PATH );
    tester.addServlet( PauseTransServlet.class, PauseTransServlet.CONTEXT_PATH );
    try {
      tester.start();
      System.out.println( "Started" );
    } catch ( Exception ex ) {
      fail( ex.getMessage() );
    }
  }

  @After
  public void after() {
    try {
      tester.stop();
      carte.getDetections().clear();
      carte.getSocketRepository().closeAll();
      System.out.println( "Stopped" );
    } catch ( Exception ex ) {
      fail( ex.getMessage() );
    }
  }

  //FIXME The tests on the following commented block do not compile!
  // These tests were disabled for a long time and, in the meantime, Jetty project evolved and had some structural
  // changes (making some classes private, for instance).
  // For now, it's a huge job to make things work again!
/*
  @Test
  public void testGetRootServlet() {
    HttpTester request = new HttpTester();
    HttpTester response = new HttpTester();
    request.setMethod( "GET" );
    request.setHeader( "Host", "tester" );
    request.setURI( GetRootServlet.CONTEXT_PATH );
    request.setVersion( "HTTP/1.0" );
    try {
      response.parse( tester.getResponses( request.generate() ) );

      Node document = parse( response.getContent() );
      String title = BaseMessages.getString( GetRootServlet.class, "GetRootServlet.KettleSlaveServer.Title" );
      assertEquals( title, findTextNode( document, "TITLE" ).getTextContent() );
      String menu = BaseMessages.getString( GetRootServlet.class, "GetRootServlet.SlaveServerMenu" );
      assertEquals( menu, findTextNode( document, "H2" ).getTextContent() );
      String status = BaseMessages.getString( GetRootServlet.class, "GetRootServlet.ShowStatus" );
      assertEquals( status, findTextNode( document, "A" ).getTextContent() );

    } catch ( Exception ex ) {
      fail( ex.getMessage() );
    }
  }

  @Test
  public void testGetStatusServlet() {
    HttpTester request = new HttpTester();
    HttpTester response = new HttpTester();
    request.setMethod( "GET" );
    request.setHeader( "Host", "tester" );
    request.setURI( GetStatusServlet.CONTEXT_PATH + "?xml=Y" );
    request.setVersion( "HTTP/1.0" );
    try {
      response.parse( tester.getResponses( request.generate() ) );
      // just test if we actually can create a SlaveServerStatus from the webservice
      // and that it is the same going in as out
      String xml =
        XMLHandler.getXMLHeader( Const.XML_ENCODING )
          + SlaveServerStatus.fromXML( response.getContent() ).getXML();
      assertEquals( response.getContent().trim(), xml.trim() );
    } catch ( Exception ex ) {
      fail( ex.getMessage() );
    }
  }

  public SlaveServerStatus getStatus() {
    HttpTester request = new HttpTester();
    HttpTester response = new HttpTester();
    request.setMethod( "GET" );
    request.setHeader( "Host", "tester" );
    request.setURI( GetStatusServlet.CONTEXT_PATH + "?xml=Y" );
    request.setVersion( "HTTP/1.0" );
    try {
      response.parse( tester.getResponses( request.generate() ) );
      return SlaveServerStatus.fromXML( response.getContent() );
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
    return null;
  }

  @Test
  public void testAddTransServlet() {

    HttpTester request = new HttpTester();
    HttpTester response = new HttpTester();
    request.setMethod( "GET" );
    request.setHeader( "Host", "tester" );
    request.setURI( RegisterTransServlet.CONTEXT_PATH + "?xml=Y" );
    request.setVersion( "HTTP/1.0" );
    try {
      TransExecutionConfiguration transExecConfig = new TransExecutionConfiguration();
      Trans trans = CarteIT.generateTestTransformation();
      TransConfiguration transConfig = new TransConfiguration( trans.getTransMeta(), transExecConfig );
      request.setContent( transConfig.getXML() );
      response.parse( tester.getResponses( request.generate() ) );

      Document document = XMLHandler.loadXMLString( response.getContent() );
      NodeList nodes = document.getElementsByTagName( "result" );
      assertEquals( 1, nodes.getLength() );
      assertEquals( WebResult.STRING_OK, nodes.item( 0 ).getTextContent() );

      SlaveServerStatus status = getStatus();
      SlaveServerTransStatus transStatus = status.findTransStatus( trans.getName(), null ); // find the first one
      assertNotNull( transStatus );
      assertFalse( transStatus.isPaused() );
      assertFalse( transStatus.isRunning() );

    } catch ( Exception ex ) {
      fail( ex.getMessage() );
    }
  }

  @Test
  public void testStartTransServlet() {

    // add our test transformation
    testAddTransServlet();

    HttpTester request = new HttpTester();
    HttpTester response = new HttpTester();
    request.setMethod( "GET" );
    request.setHeader( "Host", "tester" );
    request.setURI( StartTransServlet.CONTEXT_PATH + "?xml=Y&name=CarteUnitTest" );
    request.setVersion( "HTTP/1.0" );
    try {
      response.parse( tester.getResponses( request.generate() ) );

      Document document = XMLHandler.loadXMLString( response.getContent() );
      NodeList nodes = document.getElementsByTagName( "result" );
      assertEquals( 1, nodes.getLength() );
      assertEquals( WebResult.STRING_OK, nodes.item( 0 ).getTextContent() );

      SlaveServerStatus status = getStatus();
      SlaveServerTransStatus transStatus = status.findTransStatus( "CarteUnitTest", null );
      assertNotNull( transStatus );
      assertFalse( transStatus.isPaused() );
      assertTrue( transStatus.isRunning() );

    } catch ( Exception ex ) {
      fail( ex.getMessage() );
    }
  }

  @Test
  public void testPauseTransServlet() {

    // add our test transformation
    testAddTransServlet();
    // start our test transformation
    testStartTransServlet();

    HttpTester request = new HttpTester();
    HttpTester response = new HttpTester();
    request.setMethod( "GET" );
    request.setHeader( "Host", "tester" );
    request.setURI( PauseTransServlet.CONTEXT_PATH + "?xml=Y&name=CarteUnitTest" );
    request.setVersion( "HTTP/1.0" );
    try {

      SlaveServerStatus status = getStatus();
      SlaveServerTransStatus transStatus = status.findTransStatus( "CarteUnitTest", null );
      assertNotNull( transStatus );
      // let's make sure that it is not paused
      assertFalse( transStatus.isPaused() );

      response.parse( tester.getResponses( request.generate() ) );

      Document document = XMLHandler.loadXMLString( response.getContent() );
      NodeList nodes = document.getElementsByTagName( "result" );
      assertEquals( 1, nodes.getLength() );
      assertEquals( WebResult.STRING_OK, nodes.item( 0 ).getTextContent() );

      status = getStatus();
      transStatus = status.findTransStatus( "CarteUnitTest", null );
      assertNotNull( transStatus );
      // now check to be sure it is paused
      assertTrue( transStatus.isPaused() );

    } catch ( Exception ex ) {
      fail( ex.getMessage() );
    }
  }

  public static class XMLTreeHandler extends DefaultHandler {

  }
*/

  public static Node parse( String content ) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    //Parse the content to Document object
    return builder.parse( new InputSource( new StringReader( content ) ) );
  }

  public static Node findTextNode( Node parent, String parentNodeName ) {
    List<Node> nodes = flatten( parent, null );
    for ( Node node : nodes ) {
      if ( node.getNodeType() == Node.TEXT_NODE
        && node.getParentNode().getNodeName().equalsIgnoreCase( parentNodeName ) ) {
        return node;
      }
    }
    return null;
  }

  public static List<Node> flatten( Node parent, List<Node> nodes ) {
    Node child = parent.getFirstChild();
    if ( nodes == null ) {
      nodes = new ArrayList<Node>();
    }
    nodes.add( parent );
    while ( child != null ) {
      flatten( child, nodes );
      child = child.getNextSibling();
    }
    return nodes;
  }

  public static void print( Node node, String indent ) {
    if ( node.getNodeType() == Node.TEXT_NODE && !StringUtils.isEmpty( node.getTextContent().trim() ) ) {
      System.out.println( node.getParentNode().getNodeName() );
      System.out.println( node.getNodeName() + node.getTextContent() );
    }
    Node child = node.getFirstChild();
    while ( child != null ) {
      print( child, indent + " " );
      child = child.getNextSibling();
    }
  }

  //CHECKSTYLE:Indentation:OFF
  public static Trans generateTestTransformation() {
    RowGeneratorMeta A = new RowGeneratorMeta();
    A.allocate( 3 );
    A.setRowLimit( "10000000" );

    A.getFieldName()[0] = "ID";
    A.getFieldType()[0] = ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_INTEGER );
    A.getFieldLength()[0] = 7;
    A.getValue()[0] = "1234";

    A.getFieldName()[1] = "Name";
    A.getFieldType()[1] = ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING );
    A.getFieldLength()[1] = 35;
    A.getValue()[1] = "Some name";

    A.getFieldName()[2] = "Last updated";
    A.getFieldType()[2] = ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_DATE );
    A.getFieldFormat()[2] = "yyyy/MM/dd";
    A.getValue()[2] = "2010/02/09";

    TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation( null, A, "A" );
    transMeta.setName( "CarteUnitTest" );
    transMeta.setSizeRowset( 2500 );
    transMeta.setFeedbackSize( 50000 );
    transMeta.setUsingThreadPriorityManagment( false );

    return new Trans( transMeta );
  }
}
