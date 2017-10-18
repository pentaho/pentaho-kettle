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

package org.pentaho.di.job.entries.dostounix;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Andrey Khayrutdinov
 */
public class JobEntryDosToUnix_ConversionIdempotency_Test {

  @BeforeClass
  public static void init() throws Exception {
    KettleEnvironment.init();
  }


  private File tmpFile;
  private String tmpFilePath;
  private JobEntryDosToUnix entry;

  @Before
  public void setUp() throws Exception {
    tmpFile = File.createTempFile( "pdi-14161-", null );
    tmpFilePath = tmpFile.toURI().toString();
    entry = new JobEntryDosToUnix();
  }

  @After
  public void tearDown() throws Exception {
    if ( tmpFile != null ) {
      tmpFile.delete();
      tmpFile = null;
    }
    tmpFilePath = null;
    entry = null;
  }


  @Test
  public void oneSeparator_nix2dos() throws Exception {
    doTest( "\n", false, "\r\n" );
  }

  @Test
  public void oneSeparator_nix2nix() throws Exception {
    doTest( "\n", true, "\n" );
  }

  @Test
  public void oneSeparator_dos2nix() throws Exception {
    doTest( "\r\n", true, "\n" );
  }

  @Test
  public void oneSeparator_dos2dos() throws Exception {
    doTest( "\r\n", false, "\r\n" );
  }


  @Test
  public void charNewLineChar_nix2dos() throws Exception {
    doTest( "a\nb", false, "a\r\nb" );
  }

  @Test
  public void charNewLineChar_nix2nix() throws Exception {
    doTest( "a\nb", true, "a\nb" );
  }

  @Test
  public void charNewLineChar_dos2nix() throws Exception {
    doTest( "a\r\nb", true, "a\nb" );
  }

  @Test
  public void charNewLineChar_dos2dos() throws Exception {
    doTest( "a\r\nb", false, "a\r\nb" );
  }


  @Test
  public void twoCrOneLf_2nix() throws Exception {
    doTest( "\r\r\n", true, "\r\n" );
  }

  @Test
  public void twoCrOneLf_2dos() throws Exception {
    doTest( "\r\r\n", false, "\r\r\n" );
  }


  @Test
  public void crCharCrLf_2nix() throws Exception {
    doTest( "\ra\r\n", true, "\ra\n" );
  }

  @Test
  public void crCharCrLf_2dos() throws Exception {
    doTest( "\ra\r\n", false, "\ra\r\n" );
  }


  @Test
  public void oneSeparator_nix2dos_hugeInput() throws Exception {
    doTestForSignificantInput( "\n", false, "\r\n" );
  }

  @Test
  public void oneSeparator_nix2nix_hugeInput() throws Exception {
    doTestForSignificantInput( "\n", true, "\n" );
  }

  @Test
  public void oneSeparator_dos2nix_hugeInput() throws Exception {
    doTestForSignificantInput( "\r\n", true, "\n" );
  }

  @Test
  public void oneSeparator_dos2dos_hugeInput() throws Exception {
    doTestForSignificantInput( "\r\n", false, "\r\n" );
  }


  private void doTestForSignificantInput( String contentPattern,
                                          boolean toUnix,
                                          String expectedPattern ) throws Exception {
    int copyTimes = ( 8 * 1024 / contentPattern.length() ) + 1;
    String content = copyUntilReachesEightKbs( contentPattern, copyTimes );
    String expected = copyUntilReachesEightKbs( expectedPattern, copyTimes );

    doTest( content, toUnix, expected );
  }

  private String copyUntilReachesEightKbs( String pattern, int times ) {
    StringBuilder sb = new StringBuilder( pattern.length() * times );
    for ( int i = 0; i < times; i++ ) {
      sb.append( pattern );
    }
    return sb.toString();
  }


  private void doTest( String content, boolean toUnix, String expected ) throws Exception {
    try ( OutputStream os = new FileOutputStream( tmpFile ) ) {
      IOUtils.write( content.getBytes(), os );
    }

    entry.convert( KettleVFS.getFileObject( tmpFilePath ), toUnix );

    String converted = KettleVFS.getTextFileContent( tmpFilePath, "UTF-8" );
    assertEquals( expected, converted );
  }
}
