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
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.cluster.HttpUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.w3c.dom.Node;

public class SlaveServerJobStatusTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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
