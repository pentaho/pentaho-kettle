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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.www.SlaveServerJobStatusTest.LoggingStringLoadSaveValidator;
import org.w3c.dom.Node;

public class SlaveServerTransStatusTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testStaticFinal() {
    assertEquals( "transstatus", SlaveServerTransStatus.XML_TAG );
  }

  @Test
  public void testNoDate() throws KettleException {
    String transName = "testNullDate";
    String id = UUID.randomUUID().toString();
    String status = Trans.STRING_FINISHED;
    SlaveServerTransStatus ts = new SlaveServerTransStatus( transName, id, status );
    String resultXML = ts.getXML();
    Node newTransStatus = XMLHandler.getSubNode( XMLHandler.loadXMLString( resultXML ), SlaveServerTransStatus.XML_TAG );

    assertEquals( "The XML document should match after rebuilding from XML", resultXML,
      SlaveServerTransStatus.fromXML( resultXML ).getXML() );
    assertEquals( "There should be one \"log_date\" node in the XML", 1,
      XMLHandler.countNodes( newTransStatus, "log_date" ) );
    assertTrue( "The \"log_date\" node should have a null value",
      Utils.isEmpty( XMLHandler.getTagValue( newTransStatus, "log_date" ) ) );
  }

  @Test
  public void testWithDate() throws KettleException {
    String transName = "testWithDate";
    String id = UUID.randomUUID().toString();
    String status = Trans.STRING_FINISHED;
    Date logDate = new Date();
    SlaveServerTransStatus ts = new SlaveServerTransStatus( transName, id, status );
    ts.setLogDate( logDate );
    String resultXML = ts.getXML();
    Node newTransStatus = XMLHandler.getSubNode( XMLHandler.loadXMLString( resultXML ), SlaveServerTransStatus.XML_TAG );

    assertEquals( "The XML document should match after rebuilding from XML", resultXML,
      SlaveServerTransStatus.fromXML( resultXML ).getXML() );
    assertEquals( "There should be one \"log_date\" node in the XML", 1,
      XMLHandler.countNodes( newTransStatus, "log_date" ) );
    assertEquals( "The \"log_date\" node should match the original value", XMLHandler.date2string( logDate ),
      XMLHandler.getTagValue( newTransStatus, "log_date" ) );
  }

  @Test
  public void testSerialization() throws KettleException {
    // TODO Add StepStatusList
    List<String> attributes = Arrays.asList( "TransName", "Id", "StatusDescription", "ErrorDescription",
      "LogDate", "Paused", "FirstLoggingLineNr", "LastLoggingLineNr", "LoggingString" );
    Map<String, FieldLoadSaveValidator<?>> attributeMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attributeMap.put( "LoggingString", new LoggingStringLoadSaveValidator() );

    SlaveServerTransStatusLoadSaveTester tester =
      new SlaveServerTransStatusLoadSaveTester( SlaveServerTransStatus.class, attributes, attributeMap );

    tester.testSerialization();
  }

  @Test
  public void testGetXML() throws KettleException {
    SlaveServerTransStatus transStatus = new SlaveServerTransStatus();
    RowMetaAndData rowMetaAndData = new RowMetaAndData();
    String testData = "testData";
    rowMetaAndData.addValue( new ValueMetaString(), testData );
    List<RowMetaAndData> rows = new ArrayList<>();
    rows.add( rowMetaAndData );
    Result result = new Result();
    result.setRows( rows );
    transStatus.setResult( result );
    //PDI-15781
    Assert.assertFalse( transStatus.getXML().contains( testData ) );
    //PDI-17061
    Assert.assertTrue( transStatus.getXML( true ).contains( testData ) );
  }
}
