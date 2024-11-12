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


package org.pentaho.di.trans.steps.constant;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConstantTest {

  private StepMockHelper<ConstantMeta, ConstantData> mockHelper;
  private final ConstantMeta constantMeta = mock( ConstantMeta.class );
  private final ConstantData constantData = mock( ConstantData.class );
  private final RowMetaAndData rowMetaAndData = mock( RowMetaAndData.class );
  private Constant constantSpy;

  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws KettlePluginException {
    ValueMetaPluginType.getInstance().searchPlugins();
  }

  @Before
  public void setUp() throws Exception {

    mockHelper = new StepMockHelper<>( "Add Constants", ConstantMeta.class, ConstantData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
            mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );

    doReturn( rowMetaAndData ).when( mockHelper.stepDataInterface ).getConstants();
    constantSpy = Mockito.spy( new Constant( mockHelper.stepMeta, mockHelper.stepDataInterface, 0,
            mockHelper.transMeta, mockHelper.trans ) );
  }

  @After
  public void tearDown() throws Exception {
    mockHelper.cleanUp();
  }

  @Test
  public void testProcessRow_success() throws Exception {

    doReturn( new Object[1] ).when( constantSpy ).getRow();
    doReturn( new RowMeta() ).when( constantSpy ).getInputRowMeta();
    doReturn( new Object[1] ).when( rowMetaAndData ).getData();

    boolean success = constantSpy.processRow( constantMeta, constantData );
    assertTrue( success );
  }

  @Test
  public void testProcessRow_fail() throws Exception {

    doReturn( null ).when( constantSpy ).getRow();
    doReturn( null ).when( constantSpy ).getInputRowMeta();

    boolean success = constantSpy.processRow( constantMeta, constantData );
    assertFalse( success );
  }
}
