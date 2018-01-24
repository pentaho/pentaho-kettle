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

package org.pentaho.di.trans.streaming.common;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.pentaho.di.core.util.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith( MockitoJUnitRunner.class )
public class BaseStreamStepMetaTest {

  private BaseStreamStepMeta meta;
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;

  @Before
  public void setUp() throws Exception {
    meta = new StuffStreamMeta();
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );
    when( logChannelFactory.create( any() ) ).thenReturn( logChannel );
  }

  @InjectionSupported( localizationPrefix = "stuff" )
  private static class StuffStreamMeta extends BaseStreamStepMeta {
    @Injection( name = "stuff" )
    List<String> stuff = Arrays.asList( "one", "two" );

    @Override
    public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                  TransMeta transMeta,
                                  Trans trans ) {
      return null;
    }

    @Override public StepDataInterface getStepData() {
      return null;
    }
  }

  @Test
  public void testCheckErrorsOnZeroSizeAndDuration() {
    meta.setBatchDuration( "0" );
    meta.setBatchSize( "0" );
    ArrayList<CheckResultInterface> remarks = new ArrayList<>();
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 1, remarks.size() );
    assertEquals(
      "The \"Number of records\" and \"Duration\" fields can’t both be set to 0. Please set a value of 1 or higher for one of the fields.",
      remarks.get( 0 ).getText() );
  }

  @Test
  public void testCheckErrorsOnNaN() throws Exception {
    List<CheckResultInterface> remarks = new ArrayList<>();
    meta.setBatchDuration( "blah" );
    meta.setBatchSize( "blah" );
    meta.check( remarks, null, null, null, null, null, null, new Variables(), null, null );
    assertEquals( 2, remarks.size() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 0 ).getType() );
    assertEquals( "The \"Duration\" field is using a non-numeric value. Please set a numeric value.",
      remarks.get( 0 ).getText() );
    assertEquals( CheckResultInterface.TYPE_RESULT_ERROR, remarks.get( 1 ).getType() );
    assertEquals( "The \"Number of records\" field is using a non-numeric value. Please set a numeric value.",
      remarks.get( 1 ).getText() );
  }

  @Test
  public void testCheckErrorsOnVariables() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    Variables space = new Variables();
    space.setVariable( "something", "1000" );
    meta.setBatchSize( "${something}" );
    meta.setBatchDuration( "0" );
    meta.check( remarks, null, null, null, null, null, null, space, null, null );
    assertEquals( 0, remarks.size() );
  }

  @Test
  public void testCheckErrorsOnVariablesSubstituteError() {
    List<CheckResultInterface> remarks = new ArrayList<>();
    Variables space = new Variables();
    space.setVariable( "something", "0" );
    meta.setBatchSize( "${something}" );
    meta.setBatchDuration( "${something}" );
    meta.check( remarks, null, null, null, null, null, null, space, null, null );
    assertEquals( 1, remarks.size() );
    assertEquals( "The \"Number of records\" and \"Duration\" fields can’t both be set to 0. Please set a value of 1 "
      + "or higher for one of the fields.", remarks.get( 0 ).getText() );
  }

  @Test
  public void testSaveXMLWithInjectionList() {
    meta.setBatchDuration( "1000" );
    meta.setBatchSize( "100" );
    meta.setTransformationPath( "aPath" );
    String xml = meta.getXML();
    assertEquals(
      "<NUM_MESSAGES>100</NUM_MESSAGES>" + Const.CR
        + "<DURATION>1000</DURATION>" + Const.CR
        + "<stuff>one</stuff>" + Const.CR
        + "<stuff>two</stuff>" + Const.CR
        + "<TRANSFORMATION_PATH>aPath</TRANSFORMATION_PATH>" + Const.CR,
      xml );
  }

  @Test
  public void testSaveDefaultEmptyConnection() {
    StuffStreamMeta meta = new StuffStreamMeta();
    String xml = meta.getXML();
    assertEquals(
      "<NUM_MESSAGES>1000</NUM_MESSAGES>" + Const.CR
        + "<DURATION>1000</DURATION>" + Const.CR
        + "<stuff>one</stuff>" + Const.CR
        + "<stuff>two</stuff>" + Const.CR
        + "<TRANSFORMATION_PATH/>" + Const.CR,
      xml );
  }

  @Test
  public void testGetResourceDependencies() {
    String stepId = "KafkConsumerInput";
    String path = "/home/bgroves/fake.ktr";

    StepMeta stepMeta = new StepMeta();
    stepMeta.setStepID( stepId );
    StuffStreamMeta inputMeta = new StuffStreamMeta();
    List<ResourceReference> resourceDependencies = inputMeta.getResourceDependencies( new TransMeta(), stepMeta );
    assertEquals( 0, resourceDependencies.get( 0 ).getEntries().size() );

    inputMeta.setTransformationPath( path );
    resourceDependencies = inputMeta.getResourceDependencies( new TransMeta(), stepMeta );
    assertEquals( 1, resourceDependencies.get( 0 ).getEntries().size() );
    assertEquals( path, resourceDependencies.get( 0 ).getEntries().get( 0 ).getResource() );
    assertEquals( ResourceEntry.ResourceType.ACTIONFILE,
      resourceDependencies.get( 0 ).getEntries().get( 0 ).getResourcetype() );
  }

  @Test
  public void testReferencedObjectHasDescription() {
    BaseStreamStepMeta meta = new StuffStreamMeta();
    assertEquals( 1, meta.getReferencedObjectDescriptions().length );
    assertTrue( meta.getReferencedObjectDescriptions()[ 0 ] != null );
  }

  @Test
  public void testIsReferencedObjectEnabled() {
    BaseStreamStepMeta meta = new StuffStreamMeta();
    assertEquals( 1, meta.isReferencedObjectEnabled().length );
    assertFalse( meta.isReferencedObjectEnabled()[ 0 ] );
    meta.setTransformationPath( "/some/path" );
    assertTrue( meta.isReferencedObjectEnabled()[ 0 ] );
  }

  @Test
  public void testLoadReferencedObject() {
    BaseStreamStepMeta meta = new StuffStreamMeta();
    meta.setFileName( getClass().getResource( "/org/pentaho/di/trans/subtrans-executor-sub.ktr" ).getPath() );
    meta.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    try {
      TransMeta subTrans = (TransMeta) meta.loadReferencedObject( 0, null, null, new Variables() );
      assertEquals( "subtrans-executor-sub", subTrans.getName() );
    } catch ( KettleException e ) {
      fail();
    }
  }
}
