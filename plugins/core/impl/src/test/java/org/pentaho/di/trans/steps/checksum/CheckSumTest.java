/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.checksum;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.RowProducer;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CheckSumTest {
  @ClassRule
  public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String FIELD_SEPARATOR_STRING = "|";

  private static final int CRC32 = 0;
  private static final int ADLER32 = 1;
  private static final int MD5 = 2;
  private static final int SHA1 = 3;
  private static final int SHA256 = 4;

  private ValueMetaBinary binary1Meta;
  private ValueMetaDate date1Meta;
  private ValueMetaNumber number1Meta;
  private ValueMetaNumber number2Meta;
  private ValueMetaString string1Meta;

  private static final String TEST_BINARY1 = "/org/pentaho/di/trans/steps/loadfileinput/files/pentaho_splash.png";
  private Date TEST_DATE1; // Initialized in setUpBeforeTest()
  private static final Double TEST_NUMBER1 = 10.8;
  private static final Double TEST_NUMBER2 = 10.82;
  private static final String TEST_STRING1 = "xyz";

  @Before
  public void setUpBeforeTest() throws Exception {
    // Initialize the Date to be used in testing
    TEST_DATE1 = new SimpleDateFormat( "yyyy/MM/dd'T'HH:mm:ssZ" ).parse( "2021/04/18T12:34:56+0000" );

    // Initialize all used ValueMeta's
    binary1Meta = new ValueMetaBinary( "test" );
    date1Meta = new ValueMetaDate( "test" );
    date1Meta.setConversionMask( "yyyy/MM/dd HH:mm:ss" );
    date1Meta.setDateFormatLocale( Locale.US );
    number1Meta = new ValueMetaNumber( "test" );
    number2Meta = new ValueMetaNumber( "test" );
    string1Meta = new ValueMetaString( "test" );
  }

  @Test
  public void testHexOutput_adler32_bytes() throws Exception {
    MockRowListener results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "47645036" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "32243912" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "48627962" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1797654205" ), results.getWritten().get( 0 )[ 1 ] );

    byte[] input = IOUtils.toByteArray( getFile( TEST_BINARY1 ).getContent().getInputStream() );
    results = executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, input, binary1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1586189688" ), results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_adler32_pentahoStrings() throws Exception {
    MockRowListener results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "47645036" ), results.getWritten().get( 0 )[ 1 ] );

    results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "32243912" ), results.getWritten().get( 0 )[ 1 ] );

    results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "48627962" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "613286842" ), results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_adler32_nativeStrings() throws Exception {
    MockRowListener results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "47645036" ), results.getWritten().get( 0 )[ 1 ] );

    results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "32243912" ), results.getWritten().get( 0 )[ 1 ] );

    results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "48627962" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1797654205" ), results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_crc32_bytes() throws Exception {
    MockRowListener results =
      executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "3951999591" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1857885434" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1205016603" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1417088984" ), results.getWritten().get( 0 )[ 1 ] );

    byte[] input = IOUtils.toByteArray( getFile( TEST_BINARY1 ).getContent().getInputStream() );
    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, input, binary1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1508231614" ), results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_crc32_pentahoStrings() throws Exception {
    MockRowListener results =
      executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "3951999591" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1857885434" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1205016603" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "2653650484" ), results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_crc32_nativeStrings() throws Exception {
    MockRowListener results =
      executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "3951999591" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1857885434" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1205016603" ), results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "1417088984" ), results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_md5_bytes_withoutCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "d16fb36f0911f878998c136191af705e", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "372df98e33ac1bf6b26d225361ba7eb5", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "68b142f87143c917f29d178aa1715957", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "90ad741d365c57fd50a8702e7f9f054f", results.getWritten().get( 0 )[ 1 ] );

    byte[] input = IOUtils.toByteArray( getFile( TEST_BINARY1 ).getContent().getInputStream() );
    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, false, input, binary1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "8d808ff9051fdbfd8050f762daddf813", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_md5_pentahoStrings_withoutCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "d16fb36f0911f878998c136191af705e", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "372df98e33ac1bf6b26d225361ba7eb5", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "68b142f87143c917f29d178aa1715957", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "bee579d3372652ddc611e6a006acda05", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_md5_nativeStrings_withoutCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "d16fb36f0911f878998c136191af705e", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "372df98e33ac1bf6b26d225361ba7eb5", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "68b142f87143c917f29d178aa1715957", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "90ad741d365c57fd50a8702e7f9f054f", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_md5_bytes_withCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "FD6FFD6F0911FD78FDFD1361FDFD705E", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "372DFDFD33FD1BFDFD6D225361FD7EFD", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "68FD42FD7143FD17FD17FDFD715957", results.getWritten().get( 0 )[ 1 ] );

    byte[] input = IOUtils.toByteArray( getFile( TEST_BINARY1 ).getContent().getInputStream() );
    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, true, input, binary1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "FDFDFDFD051FFDFDFD50FD62FDFDFD13", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_md5_pentahoStrings_withCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "FD6FFD6F0911FD78FDFD1361FDFD705E", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "372DFDFD33FD1BFDFD6D225361FD7EFD", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "68FD42FD7143FD17FD17FDFD715957", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_md5_nativeStrings_withCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, true, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "FD6FFD6F0911FD78FDFD1361FDFD705E", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, true, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "372DFDFD33FD1BFDFD6D225361FD7EFD", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, true, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "68FD42FD7143FD17FD17FDFD715957", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha1_bytes_withoutCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "66b27417d37e024c46526c2f6d358a754fc552f3", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "78aef53da0b8d7a80656c80aa35ad6d410b7f068", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "749f3d4c2db67c9f3186563a72ef5da9461f0496", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "0c873595b0c92cd42d5bbe5a90c2d560f4a7b35b", results.getWritten().get( 0 )[ 1 ] );

    byte[] input = IOUtils.toByteArray( getFile( TEST_BINARY1 ).getContent().getInputStream() );
    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, false, input, binary1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "e67d0b5b60663b8a5e0df1d23b44de673738315a", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha1_pentahoStrings_withoutCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "66b27417d37e024c46526c2f6d358a754fc552f3", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "78aef53da0b8d7a80656c80aa35ad6d410b7f068", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "749f3d4c2db67c9f3186563a72ef5da9461f0496", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "bd9503d9f2eb92ad1d8d2ab3ba64d168df57a5fd", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha1_nativeStrings_withoutCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "66b27417d37e024c46526c2f6d358a754fc552f3", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "78aef53da0b8d7a80656c80aa35ad6d410b7f068", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "749f3d4c2db67c9f3186563a72ef5da9461f0496", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "0c873595b0c92cd42d5bbe5a90c2d560f4a7b35b", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha1_bytes_withCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "66FD7417FD7E024C46526C2F6D35FD754FFD52FD", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "78FDFD3DFDFDE80656FD0AFD5AFDFD10FDFD68", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "74FD3D4C2DFD7CFD31FD563A72FD5DFD461F04FD", results.getWritten().get( 0 )[ 1 ] );

    byte[] input = IOUtils.toByteArray( getFile( TEST_BINARY1 ).getContent().getInputStream() );
    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, true, input, binary1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "FD7D0B5B60663BFD5E0DFDFD3B44FD673738315A", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha1_pentahoStrings_withCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, true, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "66FD7417FD7E024C46526C2F6D35FD754FFD52FD", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, true, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "78FDFD3DFDFDE80656FD0AFD5AFDFD10FDFD68", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, true, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "74FD3D4C2DFD7CFD31FD563A72FD5DFD461F04FD", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha1_nativeStrings_withCompatibility() throws Exception {
    MockRowListener results =
      executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, true, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "66FD7417FD7E024C46526C2F6D35FD754FFD52FD", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, true, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "78FDFD3DFDFDE80656FD0AFD5AFDFD10FDFD68", results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, true, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "74FD3D4C2DFD7CFD31FD563A72FD5DFD461F04FD", results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha256_bytes() throws Exception {
    MockRowListener results =
      executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "3608bca1e44ea6c4d268eb6db02260269892c0b42b86bbf1e77a6fa16c3c9282",
      results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "b52b603f9ec86c382a8483cad4f788f2f927535a76ad1388caedcef5e3c3c813",
      results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "45cbb96ff9625490cd675a7a39fecad6c167c1ed9b8957f53224fcb3e4a1e4a1",
      results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "c3346df6a54f0e59d4de50b931040e516f0327ee1e7fd7274039dae039ac4b33",
      results.getWritten().get( 0 )[ 1 ] );

    byte[] input = IOUtils.toByteArray( getFile( TEST_BINARY1 ).getContent().getInputStream() );
    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, false, input, binary1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "6914d0cb9296d658569570c23924ea4822be73f0ee3bc46d11651fb4041a43e1",
      results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha256_pentahoStrings() throws Exception {
    MockRowListener results =
      executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "3608bca1e44ea6c4d268eb6db02260269892c0b42b86bbf1e77a6fa16c3c9282",
      results.getWritten().get( 0 )[ 1 ] );

    results =
      executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "b52b603f9ec86c382a8483cad4f788f2f927535a76ad1388caedcef5e3c3c813",
      results.getWritten().get( 0 )[ 1 ] );

    results =
      executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "45cbb96ff9625490cd675a7a39fecad6c167c1ed9b8957f53224fcb3e4a1e4a1",
      results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "4bff59cfaf24ae2b4a270b45177605b8a95c73b11a04432b1af17fcf73b5d229",
      results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_sha256_nativeStrings() throws Exception {
    MockRowListener results =
      executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_STRING1, string1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "3608bca1e44ea6c4d268eb6db02260269892c0b42b86bbf1e77a6fa16c3c9282",
      results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER1, number1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "b52b603f9ec86c382a8483cad4f788f2f927535a76ad1388caedcef5e3c3c813",
      results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_NUMBER2, number2Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "45cbb96ff9625490cd675a7a39fecad6c167c1ed9b8957f53224fcb3e4a1e4a1",
      results.getWritten().get( 0 )[ 1 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, false, TEST_DATE1, date1Meta );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "c3346df6a54f0e59d4de50b931040e516f0327ee1e7fd7274039dae039ac4b33",
      results.getWritten().get( 0 )[ 1 ] );
  }

  @Test
  public void testHexOutput_adler32_bytes_withSeparator() throws Exception {
    MockRowListener results =
      executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
        new Object[] { "abc", "def" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "188023506" ), results.getWritten().get( 0 )[ 2 ] );

    results = executeHexTest( ADLER32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
      new Object[] { "ab", "cdef" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "189661906" ), results.getWritten().get( 0 )[ 2 ] );
  }

  @Test
  public void testHexOutput_crc32_bytes_withSeparator() throws Exception {
    MockRowListener results =
      executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
        new Object[] { "abc", "def" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "4184808628" ), results.getWritten().get( 0 )[ 2 ] );

    results = executeHexTest( CRC32, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
      new Object[] { "ab", "cdef" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( Long.valueOf( "329603886" ), results.getWritten().get( 0 )[ 2 ] );
  }

  @Test
  public void testHexOutput_md5_bytes_withSeparator() throws Exception {
    MockRowListener results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
      new Object[] { "abc", "def" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "3584771072cb165f8760fc8d72f38380", results.getWritten().get( 0 )[ 2 ] );

    results = executeHexTest( MD5, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
      new Object[] { "ab", "cdef" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "b545524789619f799400bd8cb0466060", results.getWritten().get( 0 )[ 2 ] );
  }

  @Test
  public void testHexOutput_sha1_bytes_withSeparator() throws Exception {
    MockRowListener results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
      new Object[] { "abc", "def" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "2db434455a8c61c214eaa4d4f7f7b05fea829528", results.getWritten().get( 0 )[ 2 ] );

    results = executeHexTest( SHA1, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
      new Object[] { "ab", "cdef" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "ac94fd308bb8b9054862391c0ed2510b4814bcd4", results.getWritten().get( 0 )[ 2 ] );
  }

  @Test
  public void testHexOutput_sha256_bytes_withSeparator() throws Exception {
    MockRowListener results =
      executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
        new Object[] { "abc", "def" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "0def6826e591afbb7b4431daaa6f2a78c1e5af533cb94b6db1635efbf255cb16",
      results.getWritten().get( 0 )[ 2 ] );

    results = executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, false, FIELD_SEPARATOR_STRING,
      new Object[] { "ab", "cdef" }, new ValueMetaString( "a" ), new ValueMetaString( "b" ) );
    assertEquals( 1, results.getWritten().size() );
    assertEquals( "a3aa77adf7a0bc55c91bc2e482db25bb520d5903fe7adcee9f6a09fd5ae200f6",
      results.getWritten().get( 0 )[ 2 ] );
  }

  /**
   * SHA-256 is not supported for compatibility mode: this test is expected to end with an exception
   */
  @Test( expected = KettleException.class )
  public void testHexOutput_sha256_bytes_withCompatibility() throws Exception {
    executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_BYTES, true, TEST_STRING1, string1Meta );
    fail( "SHA-256 is not supported for compatibility mode" );
  }

  /**
   * SHA-256 is not supported for compatibility mode: this test is expected to end with an exception
   */
  @Test( expected = KettleException.class )
  public void testHexOutput_sha256_pentahoStrings_withCompatibility() throws Exception {
    executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_PENTAHO_STRINGS, true, TEST_STRING1, string1Meta );
    fail( "SHA-256 is not supported for compatibility mode" );
  }

  /**
   * SHA-256 is not supported for compatibility mode: this test is expected to end with an exception
   */
  @Test( expected = KettleException.class )
  public void testHexOutput_sha256_nativeStrings_withCompatibility() throws Exception {
    executeHexTest( SHA256, CheckSumMeta.EVALUATION_METHOD_NATIVE_STRINGS, true, TEST_STRING1, string1Meta );
    fail( "SHA-256 is not supported for compatibility mode" );
  }

  private Trans buildHexadecimalChecksumTrans( int checksumType, int evaluationMethod, boolean compatibilityMode,
                                               String fieldSeparatorString, String[] fieldNames ) throws Exception {
    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( getClass().getName() );

    // Create a Checksum Step
    String checksumStepname = "Checksum";
    CheckSumMeta meta = new CheckSumMeta();

    // Set the compatibility mode and other required fields
    meta.setCompatibilityMode( compatibilityMode );
    meta.setResultFieldName( "hex" );
    meta.setCheckSumType( checksumType );
    meta.setResultType( CheckSumMeta.result_TYPE_HEXADECIMAL );
    meta.setFieldName( fieldNames );
    meta.setEvaluationMethod( evaluationMethod );
    meta.setFieldSeparatorString( fieldSeparatorString );

    String checksumPluginPid = PluginRegistry.getInstance().getPluginId( StepPluginType.class, meta );
    StepMeta checksumStep = new StepMeta( checksumPluginPid, checksumStepname, meta );
    transMeta.addStep( checksumStep );

    // Create a Dummy step
    String dummyStepname = "Output";
    DummyTransMeta dummyMeta = new DummyTransMeta();
    String dummyStepPid = PluginRegistry.getInstance().getPluginId( StepPluginType.class, dummyMeta );
    StepMeta dummyStep = new StepMeta( dummyStepPid, dummyStepname, dummyMeta );
    transMeta.addStep( dummyStep );

    // Create a hop from Checksum to Output
    TransHopMeta hop = new TransHopMeta( checksumStep, dummyStep );
    transMeta.addTransHop( hop );

    return new Trans( transMeta );
  }

  private static class MockRowListener extends RowAdapter {
    private final List<Object[]> written = new ArrayList<>();

    private final List<Object[]> read = new ArrayList<>();

    private final List<Object[]> error = new ArrayList<>();

    public List<Object[]> getWritten() {
      return written;
    }

    @Override
    public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      written.add( row );
    }

    @Override
    public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      read.add( row );
    }

    @Override
    public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
      error.add( row );
    }
  }

  /**
   * Create, execute, and return the row listener attached to the output step with complete results from the execution.
   *
   * @param checksumType
   *          Type of checksum to use (the array index of {@link CheckSumMeta#checksumtypeCodes})
   * @param evaluationMethod
   *          the evaluation method to use for calculating the checksum
   * @param compatibilityMode
   *          Use compatibility mode for Checksum
   * @param input
   *          String to calculate checksum for
   * @param meta
   *          meta to be used
   * @return RowListener with results.
   */
  private MockRowListener executeHexTest( int checksumType, int evaluationMethod, boolean compatibilityMode,
                                          Object input, ValueMetaInterface meta ) throws Exception {
    return executeHexTest( checksumType, evaluationMethod, compatibilityMode, null, new Object[] { input }, meta );
  }

  /**
   * Create, execute, and return the row listener attached to the output step with complete results from the execution.
   *
   * @param checksumType
   *          Type of checksum to use (the array index of {@link CheckSumMeta#checksumtypeCodes})
   * @param evaluationMethod
   *          the evaluation method to use for calculating the checksum
   * @param compatibilityMode
   *          Use compatibility mode for Checksum
   * @param fieldSeparatorString
   *          The string separate multiple fields with
   * @param inputs
   *          Array of objects representing row data
   * @param inputValueMetas
   *          metas to be processed
   * @return RowListener with results.
   */
  private MockRowListener executeHexTest( int checksumType, int evaluationMethod, boolean compatibilityMode,
                                          String fieldSeparatorString, Object[] inputs,
                                          ValueMetaInterface... inputValueMetas ) throws Exception {

    String[] fieldNames = new String[ inputValueMetas.length ];
    RowMeta inputRowMeta = new RowMeta();
    for ( int i = 0; i < inputValueMetas.length; i++ ) {
      inputRowMeta.addValueMeta( inputValueMetas[ i ] );
      fieldNames[ i ] = inputValueMetas[ i ].getName();
    }

    Trans trans =
      buildHexadecimalChecksumTrans( checksumType, evaluationMethod, compatibilityMode, fieldSeparatorString,
        fieldNames );

    trans.prepareExecution( null );

    StepInterface output = trans.getRunThread( "Output", 0 );
    MockRowListener listener = new MockRowListener();
    output.addRowListener( listener );

    RowProducer rp = trans.addRowProducer( "Checksum", 0 );

    ( (BaseStep) trans.getRunThread( "Checksum", 0 ) ).setInputRowMeta( inputRowMeta );

    trans.startThreads();

    rp.putRow( inputRowMeta, inputs );
    rp.finished();

    trans.waitUntilFinished();
    trans.stopAll();
    trans.cleanup();
    return listener;
  }

  private FileObject getFile( final String filepath ) {
    try {
      return VFS.getManager().resolveFile( this.getClass().getResource( filepath ) );
    } catch ( Exception e ) {
      throw new RuntimeException( "fail. " + e.getMessage(), e );
    }
  }
}
