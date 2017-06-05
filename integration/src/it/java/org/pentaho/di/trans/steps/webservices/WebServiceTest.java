/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.webservices;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransformationTestCase;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.injector.InjectorMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * User: Dzmitry Stsiapanau Date: 2/12/14 Time: 5:15 PM
 */
public class WebServiceTest extends TransformationTestCase {
  private static final String WSDL =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<definitions xmlns=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
      + " xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http://webservices.daehosting"
      + ".com/temperature\" name=\"TemperatureConversions\" targetNamespace=\"http://webservices.daehosting"
      + ".com/temperature\">\n"
      + "  <types>\n"
      + "    <xs:schema elementFormDefault=\"qualified\" targetNamespace=\"http://webservices.daehosting"
      + ".com/temperature\">\n"
      + "      <xs:element name=\"CelciusToFahrenheit\">\n"
      + "        <xs:complexType>\n"
      + "          <xs:sequence>\n"
      + "            <xs:element name=\"nCelcius\" type=\"xs:decimal\"/>\n"
      + "          </xs:sequence>\n"
      + "        </xs:complexType>\n"
      + "      </xs:element>\n"
      + "      <xs:element name=\"CelciusToFahrenheitResponse\">\n"
      + "        <xs:complexType>\n"
      + "          <xs:sequence>\n"
      + "            <xs:element name=\"CelciusToFahrenheitResult\" type=\"xs:decimal\"/>\n"
      + "          </xs:sequence>\n"
      + "        </xs:complexType>\n"
      + "      </xs:element>\n"
      + "    </xs:schema>\n"
      + "  </types>\n"
      + "  <message name=\"CelciusToFahrenheitSoapRequest\">\n"
      + "    <part name=\"parameters\" element=\"tns:CelciusToFahrenheit\"/>\n"
      + "  </message>\n"
      + "  <message name=\"CelciusToFahrenheitSoapResponse\">\n"
      + "    <part name=\"parameters\" element=\"tns:CelciusToFahrenheitResponse\"/>\n"
      + "  </message>\n"
      + "  <portType name=\"TemperatureConversionsSoapType\">\n"
      + "    <operation name=\"CelciusToFahrenheit\">\n"
      + "      <documentation>Converts a Celcius Temperature to a Fahrenheit value</documentation>\n"
      + "      <input message=\"tns:CelciusToFahrenheitSoapRequest\"/>\n"
      + "      <output message=\"tns:CelciusToFahrenheitSoapResponse\"/>\n"
      + "    </operation>\n"
      + "  </portType>\n"
      + "  <binding name=\"TemperatureConversionsSoapBinding\" type=\"tns:TemperatureConversionsSoapType\">\n"
      + "    <soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n"
      + "    <operation name=\"CelciusToFahrenheit\">\n"
      + "      <soap:operation soapAction=\"\" style=\"document\"/>\n"
      + "      <input>\n"
      + "        <soap:body use=\"literal\"/>\n"
      + "      </input>\n"
      + "      <output>\n"
      + "        <soap:body use=\"literal\"/>\n"
      + "      </output>\n"
      + "    </operation>\n"
      + "  </binding>\n"
      + "  <service name=\"TemperatureConversions\">\n"
      + "    <documentation>Visual DataFlex Web Service to convert temperature values between Celcius and "
      + "Fahrenheit</documentation>\n"
      + "    <port name=\"TemperatureConversionsSoap\" binding=\"tns:TemperatureConversionsSoapBinding\">\n"
      + "      <soap:address location=\"HTTP_LOCALHOST_PLACEHOLDERwso\"/>\n" + "    </port>\n" + "  </service>\n"
      + "</definitions>\n";

  private static final String STEP_META =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "  <step>\n"
      + "    <name>Get Celcius 2</name>\n"
      + "    <type>WebServiceLookup</type>\n"
      + "    <description/>\n"
      + "    <distribute>Y</distribute>\n"
      + "    <custom_distribution/>\n"
      + "    <copies>1</copies>\n"
      + "         <partitioning>\n"
      + "           <method>none</method>\n"
      + "           <schema_name/>\n"
      + "           </partitioning>\n"
      + "    <wsURL>http&#x3a;&#x2f;&#x2f;localhost&#x3a;9998&#x2f;wsdl</wsURL>\n"
      + "    <wsOperation>CelciusToFahrenheit</wsOperation>\n"
      + "    <wsOperationRequest/>\n"
      + "    <wsOperationNamespace>http&#x3a;&#x2f;&#x2f;webservices.daehosting.com&#x2f;"
      + "temperature</wsOperationNamespace>\n"
      + "    <wsInFieldContainer/>\n" + "    <wsInFieldArgument/>\n"
      + "    <wsOutFieldContainer>CelciusToFahrenheitResult</wsOutFieldContainer>\n"
      + "    <wsOutFieldArgument>CelciusToFahrenheitResult</wsOutFieldArgument>\n" + "    <proxyHost/>\n"
      + "    <proxyPort/>\n" + "    <httpLogin/>\n" + "    <httpPassword/>\n" + "    <callStep>1</callStep>\n"
      + "    <passingInputData>Y</passingInputData>\n" + "    <compatible>Y</compatible>\n"
      + "    <repeating_element/>\n" + "    <reply_as_string>Y</reply_as_string>\n" + "    <fieldsIn>\n"
      + "    <field>\n" + "        <name>integer_20</name>\n" + "        <wsName>nCelcius</wsName>\n"
      + "        <xsdType>decimal</xsdType>\n" + "    </field>\n" + "      </fieldsIn>\n" + "    <fieldsOut>\n"
      + "    <field>\n" + "        <name>CelciusToFahrenheitResult</name>\n"
      + "        <wsName>CelciusToFahrenheitResult</wsName>\n" + "        <xsdType>decimal</xsdType>\n"
      + "    </field>\n" + "      </fieldsOut>\n" + "     <cluster_schema/>\n"
      + " <remotesteps>   <input>   </input>   <output>   </output> </remotesteps>    <GUI>\n"
      + "      <xloc>779</xloc>\n" + "      <yloc>231</yloc>\n" + "      <draw>Y</draw>\n" + "      </GUI>\n"
      + "    </step>\n";
  private static final String RESPONSE_BODY = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
    + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" + "  <soap:Body>\n"
    + "    <CelciusToFahrenheitResponse xmlns=\"http://webservices.daehosting.com/temperature\">\n"
    + "      <CelciusToFahrenheitResult>decimal</CelciusToFahrenheitResult>\n"
    + "    </CelciusToFahrenheitResponse>\n" + "  </soap:Body>\n" + "</soap:Envelope>";

  public static final String host = "localhost";
  public static final int port = 9998;
  public static final String HTTP_LOCALHOST_9998 = "http://localhost:9998/";

  private static HttpServer httpServer;

  public WebServiceTest() throws KettleException {
    super();
  }

  @Override
  public void setUp() throws Exception {
    startHttpAnswer();
  }

  @Override
  public void tearDown() throws Exception {
    httpServer.stop( 0 );
  }

  public void testProcessRow() throws Exception {
    KettleEnvironment.init();

    //
    // Create a new transformation...
    //
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "WebServiceTest" );

    PluginRegistry registry = PluginRegistry.getInstance();

    //
    // create an injector step...
    //
    String injectorStepname = "injector step";
    InjectorMeta im = new InjectorMeta();

    // Set the information of the injector.
    String injectorPid = registry.getPluginId( StepPluginType.class, im );
    StepMeta injectorStep = new StepMeta( injectorPid, injectorStepname, im );
    transMeta.addStep( injectorStep );

    //
    // Create a dummy step 1
    //
    String dummyStepname1 = "dummy step 1";
    DummyTransMeta dm1 = new DummyTransMeta();

    String dummyPid1 = registry.getPluginId( StepPluginType.class, dm1 );
    StepMeta dummyStep1 = new StepMeta( dummyPid1, dummyStepname1, dm1 );
    transMeta.addStep( dummyStep1 );

    TransHopMeta hi = new TransHopMeta( injectorStep, dummyStep1 );
    transMeta.addTransHop( hi );

    //
    // Create a String Cut step
    //
    String webServiceStepname = "web service step";
    WebServiceMeta scm = new WebServiceMeta();

    // scm.setUrl(HTTP_LOCALHOST_9998+ "wsdl");
    // scm.setOperationName("CelciusToFahrenheit");
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse( new InputSource( new java.io.StringReader( STEP_META ) ) );
    scm.loadXML( doc.getFirstChild(), null, (IMetaStore) null );

    String webServicePid = registry.getPluginId( StepPluginType.class, scm );
    StepMeta webServiceStep = new StepMeta( webServicePid, webServiceStepname, scm );
    transMeta.addStep( webServiceStep );

    TransHopMeta hi2 = new TransHopMeta( dummyStep1, webServiceStep );
    transMeta.addTransHop( hi2 );

    //
    // Create a dummy step 2
    //
    String dummyStepname2 = "dummy step 2";
    DummyTransMeta dm2 = new DummyTransMeta();

    String dummyPid2 = registry.getPluginId( StepPluginType.class, dm2 );
    StepMeta dummyStep2 = new StepMeta( dummyPid2, dummyStepname2, dm2 );
    transMeta.addStep( dummyStep2 );

    TransHopMeta hi3 = new TransHopMeta( webServiceStep, dummyStep2 );
    transMeta.addTransHop( hi3 );

    // Now execute the transformation...
    Trans trans = new Trans( transMeta );

    trans.prepareExecution( null );

    StepInterface si = trans.getStepInterface( dummyStepname1, 0 );
    RowStepCollector dummyRc1 = new RowStepCollector();
    si.addRowListener( dummyRc1 );

    si = trans.getStepInterface( webServiceStepname, 0 );
    RowStepCollector webServiceRc = new RowStepCollector();
    si.addRowListener( webServiceRc );

    RowProducer rp = trans.addRowProducer( injectorStepname, 0 );
    trans.startThreads();

    // add rows
    List<RowMetaAndData> inputList = createData( createRowMetaInterface(), new Object[][] { new Object[] { 10 } } );
    for ( RowMetaAndData rm : inputList ) {
      rp.putRow( rm.getRowMeta(), rm.getData() );
    }
    rp.finished();

    trans.waitUntilFinished();

    List<RowMetaAndData> goldRows =
      createData( createOutputRowMetaInterface(), new Object[][] { new Object[] { 10,
        new BigDecimal( 20 ) } } );
    List<RowMetaAndData> resultRows2 = webServiceRc.getRowsWritten();
    assertEquals( goldRows, resultRows2 );
  }

  public RowMetaInterface createRowMetaInterface() {
    return createRowMetaInterface( new ValueMetaString( "integer_20" ) );
  }

  public RowMetaInterface createOutputRowMetaInterface() {
    return createRowMetaInterface( new ValueMetaString( "integer_20" ), new ValueMetaBigNumber(
      "CelciusToFahrenheitResult" ) );
  }

  private static void startHttpAnswer() throws IOException {
    httpServer = HttpServer.create( new InetSocketAddress( host, port ), 10 );
    httpServer.createContext( "/wsdl", new HttpHandler() {
      @Override
      public void handle( HttpExchange httpExchange ) throws IOException {
        String response = WSDL.replaceAll( "HTTP_LOCALHOST_PLACEHOLDER", HTTP_LOCALHOST_9998 );
        byte[] bodyBytes = response.getBytes( "UTF-8" );
        httpExchange.sendResponseHeaders( 200, bodyBytes.length );
        httpExchange.getResponseBody().write( bodyBytes );
        httpExchange.close();
      }
    } );
    httpServer.createContext( "/wso", new HttpHandler() {
      @Override
      public void handle( HttpExchange httpExchange ) throws IOException {
        String response = RESPONSE_BODY.replaceAll( "decimal", "20" );
        byte[] bodyBytes = response.getBytes( "UTF-8" );
        httpExchange.getResponseHeaders().add( "Content-Type", "text/xml; charset=utf-8" );
        httpExchange.sendResponseHeaders( 200, bodyBytes.length );
        httpExchange.getResponseBody().write( bodyBytes );
        httpExchange.close();
      }
    } );
    httpServer.start();
  }

}
