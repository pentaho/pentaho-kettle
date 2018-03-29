/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.www.SlaveServerJobStatusTest.LoggingStringLoadSaveValidator;
import org.w3c.dom.Node;

public class SlaveServerTransStatusTest {

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
