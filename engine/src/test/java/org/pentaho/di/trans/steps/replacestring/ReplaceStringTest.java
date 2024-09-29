/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.replacestring;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 1/31/14 Time: 11:19 AM
 */
public class ReplaceStringTest {

  private static final String LITERAL_STRING = "[a-z]{2,7}";

  private static final String INPUT_STRING = "This is String This Is String THIS IS STRING";

  private final Object[] row = new Object[] { "some data", "another data" };

  private final Object[] expectedRow = new Object[] { "some data", "1nother d1t1", "1no2her d121", null, null, null, null,
    null, null, null, null, null, null };

  private StepMockHelper<ReplaceStringMeta, ReplaceStringData> stepMockHelper;

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<>( "REPLACE STRING TEST", ReplaceStringMeta.class, ReplaceStringData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString() );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), any( Object[].class ) );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), (Throwable) any() );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testGetOneRow() throws Exception {
    ReplaceStringData data = new ReplaceStringData();

    ReplaceString replaceString =
      new ReplaceString( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );
    RowMetaInterface inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta( 0, new ValueMetaString( "SomeDataMeta" ) );
    inputRowMeta.addValueMeta( 1, new ValueMetaString( "AnotherDataMeta" ) );
    replaceString.init( stepMockHelper.processRowsStepMetaInterface, data );
    replaceString.setInputRowMeta( inputRowMeta );
    data.outputRowMeta = inputRowMeta;
    data.outputRowMeta.addValueMeta( new ValueMetaString( "AnotherDataMeta" ) );
    data.inputFieldsNr = 2;
    data.numFields = 2;
    data.inStreamNrs = new int[] { 1, 1 };
    data.patterns = new Pattern[] { Pattern.compile( "a" ), Pattern.compile( "t" ) };
    data.replaceFieldIndex = new int[] { -1, -1 };
    data.outStreamNrs = new String[] { StringUtils.EMPTY, "1" };
    data.replaceByString = new String[] { "1", "2" };
    data.setEmptyString = new boolean[] { false, false };

    Object[] output = replaceString.getOneRow( inputRowMeta, row );
    assertArrayEquals( "Output varies", expectedRow, output );
  }

  //PDI-16472
  @Test
  public void testSynchronizeDifferentFieldsArraysLengths() {

    ReplaceStringData data = new ReplaceStringData();
    ReplaceString replaceString =
      new ReplaceString( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );

    ReplaceStringMeta meta = new ReplaceStringMeta();
    replaceString.init( meta, data );

    meta.setFieldInStream( new String[] { "input1", "input2" } );
    meta.setFieldOutStream( new String[] { "out" } );
    meta.setUseRegEx( new boolean[] { true } );
    meta.setCaseSensitive( new boolean[] { false } );
    meta.setWholeWord( new boolean[] { true } );
    meta.setReplaceString( new String[] { "string" } );
    meta.setReplaceByString( new String[] { "string" } );
    meta.setEmptyString( new boolean[] { true } );
    meta.setFieldReplaceByString( new String[] { "string" } );

    meta.afterInjectionSynchronization();

    assertEquals( meta.getFieldInStream().length, meta.getFieldOutStream().length );
    assertEquals( StringUtils.EMPTY, meta.getFieldOutStream()[ 1 ] );

    assertEquals( meta.getFieldInStream().length, meta.getUseRegEx().length );
    assertFalse( meta.getUseRegEx()[ 1 ] );

    assertEquals( meta.getFieldInStream().length, meta.getCaseSensitive().length );
    assertFalse( meta.getCaseSensitive()[ 1 ] );

    assertEquals( meta.getFieldInStream().length, meta.getWholeWord().length );
    assertFalse( meta.getWholeWord()[ 1 ] );

    assertEquals( meta.getFieldInStream().length, meta.getReplaceString().length );
    assertEquals( StringUtils.EMPTY, meta.getReplaceString()[ 1 ] );

    assertEquals( meta.getFieldInStream().length, meta.getReplaceByString().length );
    assertEquals( StringUtils.EMPTY, meta.getReplaceByString()[ 1 ] );

    assertEquals( meta.getFieldInStream().length, meta.isSetEmptyString().length );
    assertFalse( meta.isSetEmptyString()[ 1 ] );

    assertEquals( meta.getFieldInStream().length, meta.getFieldReplaceByString().length );
    assertEquals( StringUtils.EMPTY, meta.getFieldReplaceByString()[ 1 ] );
  }

  @Test
  public void testBuildPatternWithLiteralParsingAndWholeWord() {
    Pattern actualPattern = ReplaceString.buildPattern( true, true, true, LITERAL_STRING, false );
    Matcher matcher = actualPattern.matcher( INPUT_STRING );
    String actualString = matcher.replaceAll( "are" );
    assertEquals( INPUT_STRING, actualString );
  }

  @Test
  public void testBuildPatternWithNonLiteralParsingAndWholeWord() {
    Pattern actualPattern = ReplaceString.buildPattern( false, true, true, LITERAL_STRING, false );
    Matcher matcher = actualPattern.matcher( INPUT_STRING );
    String actualString = matcher.replaceAll( "are" );
    assertEquals( "This are String This Is String THIS IS STRING", actualString );
  }

  @Test
  public void testProcessRow() throws Exception {
    ReplaceStringData data = new ReplaceStringData();

    ReplaceString replaceString = Mockito.spy(
      new ReplaceString( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans ) );
    RowMetaInterface inputRowMeta = new RowMeta();
    byte[] array = { 0, 97, 0, 65, -1, 65, -1, 33 };
    byte[] matcharray = { -1, 33 };
    String match = new String( matcharray, StandardCharsets.UTF_16BE );
    Object[] _row = new Object[] { new String( array, StandardCharsets.UTF_16BE ), "another data" };
    doReturn( _row ).when( replaceString ).getRow();
    inputRowMeta.addValueMeta( 0, new ValueMetaString( "string" ) );
    ReplaceStringMeta meta = stepMockHelper.processRowsStepMetaInterface;

    doReturn( new String[] { "string" } ).when( meta ).getFieldInStream();
    doReturn( new String[] { "output" } ).when( meta ).getFieldOutStream();
    doReturn( new boolean[] { true } ).when( meta ).isUnicode();
    doReturn( new boolean[] { false } ).when( meta ).getUseRegEx();
    doReturn( new boolean[] { false } ).when( meta ).getCaseSensitive();
    doReturn( new boolean[] { false } ).when( meta ).getWholeWord();
    doReturn( new String[] { match } ).when( meta ).getReplaceString();
    doReturn( new String[] { StringUtils.EMPTY } ).when( meta ).getFieldReplaceByString();
    doReturn( new String[] { "matched" } ).when( meta ).getReplaceByString();
    doReturn( new boolean[] { false } ).when( meta ).isSetEmptyString();

    replaceString.init( meta, data );
    replaceString.setInputRowMeta( inputRowMeta );
    data.outputRowMeta = inputRowMeta;
    data.inputFieldsNr = 1;
    data.numFields = 1;
    data.inStreamNrs = new int[] { 0 };
    data.replaceFieldIndex = new int[] { -1 };
    data.outStreamNrs = new String[] { StringUtils.EMPTY, "1" };
    data.replaceByString = new String[] { "1" };
    data.setEmptyString = new boolean[] { false, false };

    replaceString.processRow( meta, data );
    assertEquals( "aAmatchedmatched", replaceString.getRow()[ 1 ] );
  }
}

