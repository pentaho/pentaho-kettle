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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.w3c.dom.Node;

public class SlaveServerJobStatusTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Test
  public void testStaticFinal() {
    assertEquals( "jobstatus", SlaveServerJobStatus.XML_TAG );
  }

  @Test
  public void testNoDate() throws KettleException {
    String jobName = "testNullDate";
    String id = UUID.randomUUID().toString();
    String status = Trans.STRING_FINISHED;
    SlaveServerJobStatus js = new SlaveServerJobStatus( jobName, id, status );
    String resultXML = js.getXML();
    Node newJobStatus = XMLHandler.getSubNode( XMLHandler.loadXMLString( resultXML ), SlaveServerJobStatus.XML_TAG );

    assertEquals( "The XML document should match after rebuilding from XML", resultXML,
      SlaveServerJobStatus.fromXML( resultXML ).getXML() );
    assertEquals( "There should be one \"log_date\" node in the XML", 1,
      XMLHandler.countNodes( newJobStatus, "log_date" ) );
    assertTrue( "The \"log_date\" node should have a null value",
      Utils.isEmpty( XMLHandler.getTagValue( newJobStatus, "log_date" ) ) );
  }

  @Test
  public void testWithDate() throws KettleException {
    String jobName = "testWithDate";
    String id = UUID.randomUUID().toString();
    String status = Trans.STRING_FINISHED;
    Date logDate = new Date();
    SlaveServerJobStatus js = new SlaveServerJobStatus( jobName, id, status );
    js.setLogDate( logDate );
    String resultXML = js.getXML();
    Node newJobStatus = XMLHandler.getSubNode( XMLHandler.loadXMLString( resultXML ), SlaveServerJobStatus.XML_TAG );

    assertEquals( "The XML document should match after rebuilding from XML", resultXML,
      SlaveServerJobStatus.fromXML( resultXML ).getXML() );
    assertEquals( "There should be one \"log_date\" node in the XML", 1,
      XMLHandler.countNodes( newJobStatus, "log_date" ) );
    assertEquals( "The \"log_date\" node should match the original value", XMLHandler.date2string( logDate ),
      XMLHandler.getTagValue( newJobStatus, "log_date" ) );
  }

  @Test
  public void testSerialization() throws KettleException {
    // TODO Add Result
    List<String> attributes = Arrays.asList( "JobName", "Id", "StatusDescription", "ErrorDescription",
      "LogDate", "LoggingString", "FirstLoggingLineNr", "LastLoggingLineNr" );

    Map<String, FieldLoadSaveValidator<?>> attributeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attributeMap.put( "LoggingString", new LoggingStringLoadSaveValidator() );

    SlaveServerJobStatusLoadSaveTester tester =
      new SlaveServerJobStatusLoadSaveTester( SlaveServerJobStatus.class, attributes, attributeMap );

    tester.testSerialization();
  }

  public static class LoggingStringLoadSaveValidator implements FieldLoadSaveValidator<String> {

    @Override
    public String getTestObject() {
      try {
        return HttpUtil.encodeBase64ZippedString( UUID.randomUUID().toString() );
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
    }

    @Override
    public boolean validateTestObject( String testObject, Object actual ) {
      try {
        return HttpUtil.decodeBase64ZippedString( testObject ).equals( actual );
      } catch ( IOException e ) {
        throw new RuntimeException( e );
      }
    }

  }
}
