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

package org.pentaho.amazon.s3;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputData;
import org.pentaho.s3common.TestCleanupUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;

public class S3FileOutputTest {

  private S3FileOutput s3FileOutput;
  private StepMockHelper<S3FileOutputMeta, TextFileOutputData> stepMockHelper;
  private S3FileOutputMeta smi;

  @BeforeClass
  public static void setClassUp() throws KettleException {
    KettleEnvironment.init();
  }

  @AfterClass
  public static void tearDownClass() {
    KettleEnvironment.shutdown();
    TestCleanupUtil.cleanUpLogsDir();
  }

  @Before
  public void setUp() {
    smi = mock( S3FileOutputMeta.class );
    stepMockHelper =
      new StepMockHelper<>( "S3 TEXT FILE OUTPUT TEST", S3FileOutputMeta.class, TextFileOutputData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString() );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), any( Object[].class ) );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), any( Throwable.class ) );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();

    s3FileOutput = new S3FileOutput( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
      stepMockHelper.trans );

    System.setProperty( "aws.accessKeyId", "" );
    System.setProperty( "aws.secretKey", "" );
  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
    System.setProperty( "aws.accessKeyId", "" );
    System.setProperty( "aws.secretKey", "" );
  }

  @Test
  public void initWithDefaultCredentialsTest() {
    System.setProperty( Const.KETTLE_USE_AWS_DEFAULT_CREDENTIALS, "Y" );
    s3FileOutput.init( smi );

    Assert.assertEquals( "", System.getProperty( "aws.accessKeyId" ) );
    Assert.assertEquals( "", System.getProperty( "aws.secretKey" ) );
  }

  @Test
  public void initWithNoCredentialsTest() {
    System.setProperty( Const.KETTLE_USE_AWS_DEFAULT_CREDENTIALS, "N" );
    when( smi.getAccessKey() ).thenReturn( "" );
    when( smi.getSecretKey() ).thenReturn( "" );

    s3FileOutput.init( smi );

    Assert.assertEquals( "", System.getProperty( "aws.accessKeyId" ) );
    Assert.assertEquals( "", System.getProperty( "aws.secretKey" ) );
  }

  @Test
  public void initWithCredentialsTest() {
    System.setProperty( Const.KETTLE_USE_AWS_DEFAULT_CREDENTIALS, "N" );
    when( smi.getAccessKey() ).thenReturn( "accessKey" );
    when( smi.getSecretKey() ).thenReturn( "secretKey" );

    s3FileOutput.init( smi );

    Assert.assertEquals( "accessKey", System.getProperty( "aws.accessKeyId" ) );
    Assert.assertEquals( "secretKey", System.getProperty( "aws.secretKey" ) );
  }

  @Test
  public void initWithCredentialsEncryptedTest() {
    System.setProperty( Const.KETTLE_USE_AWS_DEFAULT_CREDENTIALS, "N" );
    when( smi.getAccessKey() ).thenReturn( "Encrypted 2be98afc86aa7f285a81aab63cdb9aac3" ); // -> accessKey
    when( smi.getSecretKey() ).thenReturn( "Encrypted 2be98afc86aa7f297ae1abc75cab9aac3" ); // -> secretKey

    s3FileOutput.init( smi );

    Assert.assertEquals( "accessKey", System.getProperty( "aws.accessKeyId" ) );
    Assert.assertEquals( "secretKey", System.getProperty( "aws.secretKey" ) );
  }
}
